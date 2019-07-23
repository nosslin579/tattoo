package org.github.tattoo.singlegroup;

import org.github.tattoo.singlegroup.model.CapResult;
import org.github.tattoo.singlegroup.model.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CapResultCollector {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  TagProAnalyticsClient tagProAnalyticsClient;


  public Optional<CapResult> getCapResult(Match match) {
    return tagProAnalyticsClient.getMatchByTeamName(match.getRedTeam().getName())
        .flatMap(taMatch -> convertToCapResult(match, taMatch));
  }

  private Optional<CapResult> convertToCapResult(Match match, TagProAnalyticsClient.Match taMatch) {
    TagProAnalyticsClient.Team redTeam = taMatch.getTeams()[0];
    TagProAnalyticsClient.Team blueTeam = taMatch.getTeams()[1];
    String redTeamName = match.getRedTeam().getName();
    String blueTeamName = match.getBlueTeam().getName();

    if (redTeam.getName().equals(redTeamName) || blueTeam.getName().equals(blueTeamName)) {
      return Optional.of(new CapResult(redTeam.getScore(), blueTeam.getScore()));
    } else {
      log.error("Wrong team name, expected: {} {}, got: {} {}", redTeamName, blueTeamName, redTeam.getName(), blueTeam.getName());
      return Optional.empty();
    }

  }
}
