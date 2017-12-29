package org.github.tattoo.impl.singelgroup.model;

public class TeamMember {
    private final Participant participant;
    private final int teamId;

    public TeamMember(Participant participant, int teamId) {
        this.participant = participant;
        this.teamId = teamId;
    }

    public Participant getParticipant() {
        return participant;
    }

    public int getTeamId() {
        return teamId;
    }
}
