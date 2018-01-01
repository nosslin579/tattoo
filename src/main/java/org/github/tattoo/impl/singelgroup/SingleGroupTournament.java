package org.github.tattoo.impl.singelgroup;

import com.google.gson.Gson;
import io.socket.client.Socket;
import org.github.tattoo.*;
import org.github.tattoo.impl.singelgroup.model.*;
import org.github.tattoo.socket.ChatEmitterListener;
import org.github.tattoo.socket.GroupCommand;
import org.github.tattoo.socket.SocketUtil;
import org.github.tattoo.socket.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.*;

public class SingleGroupTournament {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    private final Gson gson = new Gson();
    private final ParticipantManager participantManager = new ParticipantManager();
    private final MemberManager memberManager = new MemberManager();
    private final MatchManager matchManager = new MatchManager();
    private final ChatEmitterListener chatListener = new ChatEmitterListener();
    private final CompletableFuture<SingleGroupTournament> future = new CompletableFuture<>();
    private final TournamentOptions options;

    private GroupCommand groupCommand;
    private String tagProId;
    private String group;
    /**
     * For debugging only, not used as any logic.
     */
    private volatile TournamentState tournamentState = TournamentState.CREATED;
    private Instant startTime;

    public SingleGroupTournament(TournamentOptions options) {
        this.options = options;
    }

    public SingleGroupTournament() {
        this(new TournamentOptions());
    }

    public TournamentStatus getStatus() {
        TournamentStatus ret = new TournamentStatus();
        ret.setParticipants(participantManager.getParticipants());
        ret.setMembers(memberManager.getMembers());
        ret.setPoolStatus(pool.toString() + " isShutdown:" + pool.isShutdown() + " isTerminated:" + pool.isTerminated());
        ret.setTournamentState(tournamentState);
        ret.setGroup(group);
        ret.setParticipantResult(matchManager.getParticipantResults());
        ret.setMatches(matchManager.getMatches());
        ret.setOptions(options);
        ret.setChatListeners(chatListener.toString());
        ret.setCompleted(isComplete());
        ret.setSocketInfo(groupCommand.toString());
        ret.setStartTime(startTime);
        return ret;
    }

    public void startTournament() {
        tournamentState = TournamentState.SIGN_UP_OPEN;
        group = SocketUtil.createGroup(options.getServer(), (id) -> tagProId = id, options.getName());
        Socket groupSocket = SocketUtil.joinGroupSocket(options.getServer(), group, tagProId);
        groupCommand = new GroupCommand(groupSocket);

        chatListener.addListener(new SignUpChatListener(participantManager, groupCommand));
        chatListener.addListener(new SpectatorChatListener(memberManager::getMemberByName, groupCommand));
        chatListener.addListener(new StartMatchChatListener(this::closeSignUp, options.isTest()));

        groupSocket.on("chat", chatListener);
        groupSocket.on("member", memberManager::onMemberUpdate);
        groupSocket.on("removed", memberManager::onMemberLeave);
        groupSocket.on("private", new PrivateListener(groupCommand));
        pool.scheduleAtFixedRate(() -> groupCommand.touch(Member.IN_HERE), 5, 30, TimeUnit.SECONDS);
        pool.scheduleAtFixedRate(this::timeoutTournament, 55, 1, TimeUnit.MINUTES);//using scheduleAtFixedRate() since schedule() blocks shutdown()
        pool.scheduleAtFixedRate(() -> checkIfConnected(groupSocket), 1, 1, TimeUnit.MINUTES);
        if (!options.isTest()) {
            pool.schedule(this::closeSignUp, 10, TimeUnit.MINUTES);
        }
        this.startTime = Instant.now();
    }

    public void closeSignUp() {
        if (pool.isShutdown()) {
            log.info("Pool is shutdown. Not closing sign up.");
            return;
        }
        log.info("Closing sign up");
        tournamentState = TournamentState.SIGN_UP_CLOSED;
        if (memberManager.getMembers().size() < 4) {
            groupCommand.chat("Tournament canceled since not enough ppl in here.");
            endTournament(TournamentState.NOT_ENOUGH_MEMBERS);
            return;
        } else if (participantManager.getParticipants().isEmpty()) {
            groupCommand.chat("Tournament canceled since no one signed up.");
            endTournament(TournamentState.NO_PARTICIPANTS);
            return;
        }
        chatListener.removeListener(SignUpChatListener.class);
        chatListener.removeListener(StartMatchChatListener.class);
        groupCommand.moveMemberToTeam(memberManager.getLeader().getId(), TeamId.SPECTATOR);
        groupCommand.chat("Sign up closed! " + participantManager);
        setupMatch();
    }

    private void setupMatch() {
        tournamentState = TournamentState.CREATING_MATCH;
        Match match = matchManager.create(participantManager.getParticipants(), options);
        memberManager.movePplToCorrectTeam(match, groupCommand::moveMemberToTeam);
        groupCommand.setSettingMap(match.getMap());
        groupCommand.setSettingTime(match.getMaxLength());
        groupCommand.setSettingCaps(match.getCaps());
        groupCommand.chat("Launching match " + matchManager.getMatches().size() + " of " + options.getNumberOfMatches());
        pool.schedule(this::launch, 3, TimeUnit.SECONDS);
    }

    public void launch() {
        log.info("Launching");
        tournamentState = TournamentState.LAUNCHING;
        groupCommand.launch();
        pool.schedule(this::checkIfMatchIsFinished, 30, TimeUnit.SECONDS);
        SocketUtil.joinJoinerSocket(options.getServer(), tagProId);
    }

    private void checkIfMatchIsFinished() {
        log.debug("Checking if match is finished {}", memberManager);
        tournamentState = TournamentState.WAITING_FOR_MATCH_TO_FINISH;
        if (memberManager.isEverybodyInGroup()) {
            log.info("Game is finished.");
            askForScore();
        } else {
            pool.schedule(this::checkIfMatchIsFinished, 10, TimeUnit.SECONDS);
        }
    }

    private void askForScore() {
        log.info("Asking for score");
        tournamentState = TournamentState.ASKING_FOR_SCORE;
        chatListener.addListener(new CollectScoreChatListener(this::setScore));
        groupCommand.chat("What was the score? redscore-bluescore, e.g. 6-2");
    }

    private void setScore(MatchScore matchScore) {
        log.info("Setting the score. {}", matchScore);
        tournamentState = TournamentState.GOT_SCORE;
        chatListener.removeListener(CollectScoreChatListener.class);
        groupCommand.chat(matchScore.toString());
        matchManager.completeMatch(matchScore);
        if (matchManager.hasMoreMatches(options)) {
            matchManager.chatResult(groupCommand::chat, false);
            pool.schedule(this::setupMatch, 10, TimeUnit.SECONDS);
        } else {
            endTournament(TournamentState.ENDED);
        }
    }

    private void endTournament(TournamentState state) {
        log.info("Ending tournament with state {}", state);
        matchManager.chatResult(groupCommand::chat, true);
        tournamentState = state;
        pool.schedule(() -> {
            future.complete(this);
            groupCommand.disconnect();
        }, 10, TimeUnit.SECONDS);
        pool.shutdown();
    }

    private void timeoutTournament() {
        log.error("Tournament time out. State:{}", tournamentState);
        tournamentState = TournamentState.TIME_OUT;
        future.complete(this);
        groupCommand.disconnect();
        pool.shutdown();
    }

    private void checkIfConnected(Socket groupSocket) {
        if (!groupSocket.connected()) {
            log.error("Not connected to group");
            endTournament(TournamentState.DISCONNECTED);
        }
    }

    public CompletableFuture<SingleGroupTournament> getTournamentEndFuture() {
        return future;
    }

    public boolean isComplete() {
        return matchManager.getMatches().stream().allMatch(Match::isFinished) && matchManager.getMatches().size() == options.getMaps().size();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " " + options.getName();
    }
}
