package org.github.tattoo.singlegroup;

import org.github.tattoo.TournamentException;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.Participant;
import org.github.tattoo.singlegroup.model.ParticipantResult;
import org.github.tattoo.singlegroup.model.Team;
import org.github.tattoo.singlegroup.model.TeamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ResultUtil {
  private static final Logger log = LoggerFactory.getLogger(ResultUtil.class);

  public static List<String> getReadableResult(SingleGroupTournament tournament, int maxSize) {
    try {
      final AtomicInteger i = new AtomicInteger(1);
      return getParticipantResults(tournament)
          .stream()
          .limit(maxSize)
          .map(pr -> "#" + i.getAndIncrement() + " points:" + pr.getPoints() + " " + pr.getParticipant().getName())
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Failed to get readable result", e);
    }
    return Collections.emptyList();
  }

  public static List<ParticipantResult> getParticipantResults(SingleGroupTournament tournament) {
    Comparator<ParticipantResult> comparator = Comparator
        .comparingInt(ParticipantResult::getPoints).reversed()
        .thenComparing(Comparator.comparingInt(ParticipantResult::getCapsScored).reversed())
        .thenComparing(Comparator.comparingInt(ParticipantResult::getCapsConceded))
        .thenComparing(Comparator.comparingInt(ParticipantResult::getMatchesPlayed).reversed());

    return tournament.getParticipants()
        .stream()
        .map(participant -> getParticipantResult(participant, tournament))
        .sorted(comparator)
        .collect(Collectors.toList());
  }

  private static ParticipantResult getParticipantResult(Participant participant, SingleGroupTournament tournament) {
    int points = 0, matchesPlayed = 0, capsScored = 0, capsConceded = 0;

    for (Match match : tournament.getCompletedMatches()) {
      for (Team team : Arrays.asList(match.getRedTeam(), match.getBlueTeam())) {
        if (team.getPlayers().contains(participant)) {
          points += getPointsForMatch(match, team, tournament);
          matchesPlayed += 1;
          capsScored += team.getCaps();
          capsConceded += match.getTeam(TeamId.invert(team.getTeamId())).getCaps();
        }
      }
    }

    return new ParticipantResult(participant, points, matchesPlayed, capsScored, capsConceded);
  }

  private static int getPointsForMatch(Match match, Team team, SingleGroupTournament tournament) {
    switch (tournament.getOptions().getVariant()) {
      case CLASSIC:
        if (match.getRedTeam().getCaps() == match.getBlueTeam().getCaps()) {
          return 1;
        }
        return team.getCaps() > match.getTeam(TeamId.invert(team.getTeamId())).getCaps() ? 3 : 0;
      case RELAY:
        return team.getCaps();
      default:
        throw new TournamentException("Unknown variant " + tournament.getOptions().getVariant());
    }
  }


}
