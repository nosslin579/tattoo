package org.github.tattoo.singlegroup;

import org.github.tattoo.Util;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.GroupCommand;
import org.github.tattoo.groupsocket.SocketFactory;
import org.github.tattoo.singlegroup.model.ParticipantResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class SingleGroupTournamentManager {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  MatchManager matchManager;
  @Autowired
  SocketFactory socketFactory;

  public SingleGroupTournament runTournament(SingleGroupTournament tournament) {
    tournament.setStartTime(new Date());
    Group group = socketFactory.createGroupSocket(tournament.getOptions());
    GroupCommand cmd = group.getCommand();
    try {

      Thread thread = Thread.currentThread();
      Executors.newSingleThreadScheduledExecutor().schedule(thread::interrupt, 3, TimeUnit.HOURS);

      group.getChatListener().addListener(new CurrentStateListener(cmd, tournament));
      startSignUpPeriod(tournament, group);
      Util.sleepSeconds(tournament.getOptions().getSignUpWaitTime());

      TournamentState stateAfterClosingSignUp = closeSignUp(tournament, group);
      tournament.setState(stateAfterClosingSignUp);
      if (stateAfterClosingSignUp != TournamentState.SIGN_UP_CLOSED) {
        cmd.disconnect();
        return tournament;
      }

      List<ParticipantResult> results = matchManager.runMatches(tournament, group);

      log.info("Ending tournament");
      cmd.chat(".");
      Util.sleepSeconds(1);
      cmd.chat(".");
      Util.sleepSeconds(1);
      cmd.chat("Balls and ballettes, we have a winner:");
      Util.sleepSeconds(3);
      cmd.chat("###########################");
      cmd.chat("_ _ _ " + results.iterator().next().getParticipant().getName() + " _ _ _");
      cmd.chat("###########################");
      tournament.setState(TournamentState.ENDED);
    } catch (Exception e) {
      log.error("Unexpected error during tournament", e);
      tournament.setState(Thread.currentThread().isInterrupted() ? TournamentState.TIME_OUT : TournamentState.ERROR);
    }
    cmd.disconnect();
    return tournament;
  }

  public void startSignUpPeriod(SingleGroupTournament tournament, Group group) {
    tournament.setState(TournamentState.SIGN_UP_OPEN);
    group.getChatListener().addListener(new SignUpChatListener(tournament, group));
    group.getChatListener().addListener(new SpectatorChatListener(group));
  }

  public TournamentState closeSignUp(SingleGroupTournament tournament, Group group) {
    log.info("Closing sign up");

    group.getChatListener().removeListener(SignUpChatListener.class);
    group.getChatListener().removeListener(StartMatchChatListener.class);

    if (group.getMembers().size() < 4) {
      group.getCommand().chat("Tournament canceled since not enough ppl in here.");
      return TournamentState.NOT_ENOUGH_MEMBERS;
    } else if (tournament.getParticipants().size() < 2) {
      group.getCommand().chat("Tournament canceled since not enough ppl signed up.");
      return TournamentState.NOT_ENOUGH_PARTICIPANTS;
    }

    group.getCommand().chat("Sign up closed!");
    return TournamentState.SIGN_UP_CLOSED;
  }
}
