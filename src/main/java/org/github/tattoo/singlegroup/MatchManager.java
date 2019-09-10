package org.github.tattoo.singlegroup;

import io.socket.client.Socket;
import org.github.tattoo.TournamentException;
import org.github.tattoo.Util;
import org.github.tattoo.groupsocket.ChatEmitterListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.SocketFactory;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.Participant;
import org.github.tattoo.singlegroup.model.ParticipantResult;
import org.github.tattoo.singlegroup.model.Team;
import org.github.tattoo.singlegroup.model.TeamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static java.util.Comparator.comparingInt;

@Component
public class MatchManager {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  CapResultCollector capResultCollector;
  @Autowired
  SocketFactory socketFactory;

  public List<ParticipantResult> runMatches(SingleGroupTournament tournament, Group group) {
    while (tournament.getCompletedMatches().size() < tournament.getOptions().getNumberOfMatches()) {
      Match match = createMatch(tournament, group);

      ScheduledExecutorService setupMatch = setupMatch(match, group, tournament);
      if (!isPLayersReady(tournament, match, group)) {
        group.getCommand().chat("Not ready?");
        group.getCommand().chat("Trying again");
        setupMatch.shutdownNow();
        continue;
      }

      tournament.setState(TournamentState.LAUNCHING);
      group.getCommand().launch();
//      group.getCommand().allowSelfAssignment();//only set when participant request it
      Util.sleepSeconds(5);
      Socket joinerSocket = socketFactory.joinJoinerSocket(group.getTagProCookie());

      AfterMatchChatListener listener = new AfterMatchChatListener(capResultCollector, tournament, group, match);
      group.getChatListener().addListener(listener);
      tournament.setState(TournamentState.MATCH_IN_PROGRESS);
      synchronized (listener) {
        try {
          listener.wait(TimeUnit.MINUTES.toMillis(match.getMaxLength() + 5));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new TournamentException("Interrupted while waiting for match to finish", e);
        }
      }
      group.getChatListener().removeListener(listener);

      setupMatch.shutdownNow();

      joinerSocket.disconnect();//just in case
      group.getCommand().disallowSelfAssignment();
    }
    return ResultUtil.getParticipantResults(tournament);
  }

  private Match createMatch(SingleGroupTournament tournament, Group group) {
    tournament.setState(TournamentState.CREATING_MATCH);

    Match upcoming = new Match();

    //set options
    upcoming.setNumber(tournament.getCompletedMatches().size() + 1);
    upcoming.setMap(tournament.getOptions().getMap());
    upcoming.setMaxLength(tournament.getOptions().getLengthOfMatch());
    upcoming.setCapLimit(tournament.getOptions().getCaps());

    //set team names to be able to find it at tagpro.eu
    upcoming.getRedTeam().setName(Util.getRandomString());
    upcoming.getBlueTeam().setName(Util.getRandomString());


    //filter if someone has left the group
    Predicate<ParticipantResult> inHereFilter = pr -> group.getMemberById(pr.getParticipant().getTagProId())
        .map(Member::getLocation)
        .map(Member.IN_HERE::equals)
        .orElse(false);

    Comparator<ParticipantResult> byRanking = comparingInt(ParticipantResult::getMatchesPlayed)//least matches played goes first
        .thenComparing(comparingInt(ParticipantResult::getPoints).reversed()) //most points goes first
        .thenComparing(comparingInt(ParticipantResult::getCapsScored).reversed()) //most caps scored goes first
        .thenComparing(ParticipantResult::getCapsConceded) //least caps conceded goes goes first
        .thenComparing(pr -> pr.getParticipant().getSignUp());//first to sign up goes first

    //avoids uneven team, i.e. 4v3, and no more than 8 players
    int participants = tournament.getParticipants().size();
    int limit = (participants >= 8) ? 8 : (participants - (participants % 2));

    //add players to team
    TeamId.Toggle toggle = new TeamId.Toggle();
    ResultUtil.getParticipantResults(tournament)
        .stream()
        .filter(inHereFilter)
        .sorted(byRanking)
        .limit(limit)
        .map(ParticipantResult::getParticipant)
        .forEach(p -> upcoming.getTeam(toggle.getAndToggle()).getPlayers().add(p));

    return upcoming;
  }

  private ScheduledExecutorService setupMatch(Match match, Group group, SingleGroupTournament tournament) {
    ScheduledExecutorService checkPlayerTeamExecutor = Executors.newSingleThreadScheduledExecutor();
    checkPlayerTeamExecutor.scheduleAtFixedRate(() -> {

      for (Team team : Arrays.asList(match.getBlueTeam(), match.getRedTeam())) {
        for (Participant player : match.getBlueTeam().getPlayers()) {
          group.getMemberById(player.getTagProId())
              .map(Member::getTeam)
              .filter(teamId -> teamId != team.getTeamId())
              .ifPresent(integer -> group.getCommand().moveMemberToTeam(player.getTagProId(), team.getTeamId()));
        }
      }
    }, 0, 1, TimeUnit.SECONDS);

    tournament.getParticipants()
        .stream()
        .filter(p -> !match.getBlueTeam().getPlayers().contains(p))
        .filter(p -> !match.getRedTeam().getPlayers().contains(p))
        .forEach(participant -> group.getCommand().moveMemberToTeam(participant.getTagProId(), TeamId.WAITING));

    group.getCommand().setRedTeamName(match.getRedTeam().getName());
    group.getCommand().setBlueTeamName(match.getBlueTeam().getName());

    group.getCommand().setSettingMap(match.getMap());
    group.getCommand().setSettingTime(match.getMaxLength());
    group.getCommand().setSettingCaps(match.getCapLimit());
    group.getCommand().setServerSelect(true);
    group.getCommand().setServer(tournament.getOptions().getServerId());
    return checkPlayerTeamExecutor;
  }

  private boolean isPLayersReady(SingleGroupTournament tournament, Match match, Group group) {
    log.info("Launching");

    String message = "Launching " + match.getNumber() + " of " + tournament.getOptions().getNumberOfMatches();
    group.getCommand().chat(message);
    group.getCommand().chat("Ready?");

    ChatEmitterListener chatListener = group.getChatListener();
    ReadyChatListener readyChatListener = new ReadyChatListener(match, group);
    chatListener.addListener(readyChatListener);

    tournament.setState(TournamentState.ASK_READY_FOR_LAUNCH);
    synchronized (match) {
      try {
        match.wait(TimeUnit.SECONDS.toMillis(60));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new TournamentException("Interrupted while waiting for ready", e);
      }
    }

    chatListener.removeListener(readyChatListener);
    return readyChatListener.isPlayersReady();
  }
}
