package org.github.tattoo.web;

import org.github.tattoo.singlegroup.SingleGroupTournament;
import org.github.tattoo.singlegroup.model.ParticipantResult;

import java.util.List;

public class Result {
  private final SingleGroupTournament tournament;
  private final List<ParticipantResult> participantResults;

  public Result(List<ParticipantResult> participantResults, SingleGroupTournament singleGroupTournament) {
    this.participantResults = participantResults;
    tournament = singleGroupTournament;
  }

  public SingleGroupTournament getTournament() {
    return tournament;
  }

  public List<ParticipantResult> getParticipantResults() {
    return participantResults;
  }

}
