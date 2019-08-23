package org.github.tattoo.singlegroup;

import io.socket.client.Socket;
import org.github.tattoo.TournamentException;
import org.github.tattoo.Util;
import org.github.tattoo.groupsocket.ChatEmitterListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.GroupCommand;
import org.github.tattoo.groupsocket.SocketFactory;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.Participant;
import org.github.tattoo.singlegroup.model.ParticipantResult;
import org.github.tattoo.singlegroup.model.TeamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

@Component
public class MatchManager {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final Comparator<ParticipantResult> noop = (o1, o2) -> 0;

  @Autowired
  CapResultCollector capResultCollector;
  @Autowired
  SocketFactory socketFactory;

  public List<ParticipantResult> runMatches(SingleGroupTournament tournament, Group group) {
    while (tournament.getCompletedMatches().size() < tournament.getOptions().getNumberOfMatches()) {
      Match match = createMatch(tournament, group);

      setupMatch(match, group, tournament);
      if (!isPLayersReady(tournament, match, group)) {
        group.getCommand().chat("Not ready?");
        group.getCommand().chat("Trying again");
        continue;
      }

      tournament.setState(TournamentState.LAUNCHING);
      group.getCommand().launch();
      Util.sleepSeconds(2);
      Socket joinerSocket = socketFactory.joinJoinerSocket(group.getTagProCookie());

      if (isParticipantsAtLocation(match, group, Member.IN_GAME, 60)) {
        joinerSocket.disconnect();
        tournament.setState(TournamentState.MATCH_IN_PROGRESS);
      } else {
        group.getCommand().chat("Match did not start, replaying");
        joinerSocket.disconnect();
        continue;
      }

      if (isParticipantsAtLocation(match, group, Member.IN_HERE, 60 * match.getMaxLength() + 60)) {
        tournament.setState(TournamentState.MATCH_FINISHED);
        updateScore(tournament, match, group);
      }
      joinerSocket.disconnect();//just in case
    }
    return ResultUtil.getParticipantResults(tournament);
  }

  private Match createMatch(SingleGroupTournament tournament, Group group) {
    tournament.setState(TournamentState.CREATING_MATCH);

    Match upcoming = new Match();

    //set options
    upcoming.setNumber(tournament.getCompletedMatches().size() + 1);
    upcoming.setQualification(isNextMatchQualification(tournament));
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

    //sorting, decides who is going to play in the next match, based on previous matches
    List<Participant> qualified = ResultUtil.getParticipantResults(tournament)
        .stream()
        .limit(upcoming.isQualification() ? 99 : 8) //everyone is considered qualified if qualification match
        .map(ParticipantResult::getParticipant)
        .collect(Collectors.toList());

    Comparator<ParticipantResult> byMatchesPlayed = comparingInt(ParticipantResult::getMatchesPlayed);

    Comparator<ParticipantResult> byRanking = comparing((ParticipantResult pr) -> qualified.contains(pr.getParticipant())).reversed()//qualified players goes first
        .thenComparing(upcoming.isQualification() ? byMatchesPlayed : noop)//least matches played goes first, only if qualification match
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

  private boolean isNextMatchQualification(SingleGroupTournament tournament) {
    //todo: move hardcoded values to TournamentOptions
    int matchNr = tournament.getCompletedMatches().size() + 1;
    int participants = tournament.getParticipants().size();

    //8 participants or less can't have any qualification
    boolean exactly9 = participants == 9 && matchNr <= 5;
    boolean exactly10 = participants == 10 && matchNr <= 4;
    boolean above11 = participants >= 11 && matchNr <= 3;

    return exactly9 || exactly10 || above11;
  }


  private void setupMatch(Match match, Group group, SingleGroupTournament tournament) {
    for (Participant player : match.getBlueTeam().getPlayers()) {
      group.getCommand().moveMemberToTeam(player.getTagProId(), TeamId.BLUE);
    }
    for (Participant player : match.getRedTeam().getPlayers()) {
      group.getCommand().moveMemberToTeam(player.getTagProId(), TeamId.RED);
    }

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
  }

  private boolean isPLayersReady(SingleGroupTournament tournament, Match match, Group group) {
    log.info("Launching");

    String gameType = match.isQualification() ? "qualification-game " : "end-game ";
    String message = "Launching " + gameType + match.getNumber() + " of " + tournament.getOptions().getNumberOfMatches();
    group.getCommand().chat(message);
    group.getCommand().chat("Ready?");

    ChatEmitterListener chatListener = group.getChatListener();
    ReadyChatListener readyChatListener = new ReadyChatListener(match);
    chatListener.addListener(readyChatListener);

    tournament.setState(TournamentState.ASK_READY_FOR_LAUNCH);
    synchronized (match) {
      try {
        match.wait(TimeUnit.SECONDS.toMillis(30));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new TournamentException("Interrupted while waiting for ready", e);
      }
    }

    chatListener.removeListener(readyChatListener);
    return readyChatListener.isPlayersReady();
  }

  private boolean isParticipantsAtLocation(Match match, Group group, String location, int maxWaitSeconds) {
    Instant threshHold = Instant.now().plusSeconds(maxWaitSeconds);

    while (Instant.now().isBefore(threshHold)) {
      Util.sleepSeconds(2);
      long atLocation = Stream.of(match.getBlueTeam(), match.getRedTeam())
          .flatMap(team -> team.getPlayers().stream())
          .map(participant -> group.getMemberById(participant.getTagProId()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .map(Member::getLocation)
          .filter(location::equals)
          .count();
      double ratio = atLocation / (match.getRedTeam().getPlayers().size() + match.getRedTeam().getPlayers().size());
      if (atLocation > 6 || ratio > 0.74d) {
        log.info("Players at location {}", location);
        return true;
      }
    }
    log.warn("Location wait timeout {}", location);
    return false;
  }

  private boolean updateScore(SingleGroupTournament tournament, Match match, Group group) {
    tournament.setState(TournamentState.UPDATING_SCORE);
    GroupCommand command = group.getCommand();
    return capResultCollector.getCapResult(match)
        .map(capResult -> {
          log.info("Setting the score. {}", capResult);
          tournament.setState(TournamentState.GOT_SCORE);
          command.chat(capResult.toString());

          match.getRedTeam().setCaps(capResult.getRedCaps());
          match.getBlueTeam().setCaps(capResult.getBlueCaps());
          tournament.getCompletedMatches().add(match);

          ResultUtil.getReadableResult(tournament, 4).forEach(command::chat);
          return true;
        })
        .orElseGet(() -> {
          command.chat("Could not get cap result, ignoring match");
          return false;
        });
  }
}
