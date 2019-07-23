package org.github.tattoo.singlegroup.model;

public class ParticipantResult {

  private final Participant participant;
  private final int points;
  private final int matchesPlayed;
  private final int capsScored;
  private final int capsConceded;

  public ParticipantResult(Participant participant, int points, int matchesPlayed, int capsScored, int capsConceded) {
    this.participant = participant;
    this.points = points;
    this.matchesPlayed = matchesPlayed;
    this.capsScored = capsScored;
    this.capsConceded = capsConceded;
  }

  public int getPoints() {
    return points;
  }

  public int getMatchesPlayed() {
    return matchesPlayed;
  }

  public Participant getParticipant() {
    return participant;
  }

  public int getCapsScored() {
    return capsScored;
  }

  public int getCapsConceded() {
    return capsConceded;
  }

  @Override
  public String toString() {
    return "ParticipantResult{" +
        "participant=" + participant.getName() +
        ", points=" + points +
        ", matchesPlayed=" + matchesPlayed +
        ", capsScored=" + capsScored +
        ", capsConceded=" + capsConceded +
        '}';
  }
}
