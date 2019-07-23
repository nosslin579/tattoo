package org.github.tattoo.singlegroup.model;

/**
 * Represents a player that has been assigned to a team before a match
 */
public class TeamMember {
  private final Participant participant;
  private final int teamId;

  public TeamMember(Participant participant, int teamId) {
    this.participant = participant;
    this.teamId = teamId;
  }

  public int getTeamId() {
    return teamId;
  }

  public Participant getParticipant() {
    return participant;
  }

  @Override
  public String toString() {
    return "TeamMember{" +
        "participant=" + participant.getName() +
        ", teamId=" + teamId +
        '}';
  }
}
