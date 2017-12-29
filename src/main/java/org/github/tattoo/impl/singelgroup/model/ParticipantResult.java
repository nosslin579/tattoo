package org.github.tattoo.impl.singelgroup.model;

public class ParticipantResult {

    private final Participant participant;
    private int points = 0;
    private int matchesPlayed = 0;

    public ParticipantResult(Participant participant) {
        this.participant = participant;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void increasePoints(int points) {
        this.points += points;
    }

    public int getPoints() {
        return points;
    }

    public void increaseMatchesPlayed(int i) {
        matchesPlayed += i;
    }

    public int getMatchesPlayed() {
        return matchesPlayed;
    }
}
