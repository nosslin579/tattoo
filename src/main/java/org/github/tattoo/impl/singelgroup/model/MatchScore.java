package org.github.tattoo.impl.singelgroup.model;

public class MatchScore {
    private final int redScore;
    private final int blueScore;

    public MatchScore(int redScore, int blueScore) {
        this.redScore = redScore;
        this.blueScore = blueScore;
    }

    public int getRedScore() {
        return redScore;
    }

    public int getBlueScore() {
        return blueScore;
    }

    public int getScore(int teamId) {
        if (teamId == TeamId.RED) {
            return redScore;
        } else if (teamId == TeamId.BLUE) {
            return blueScore;
        } else {
            throw new IllegalArgumentException("No such team:" + teamId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchScore)) return false;

        MatchScore that = (MatchScore) o;

        if (redScore != that.redScore) return false;
        if (blueScore != that.blueScore) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = redScore;
        result = 31 * result + blueScore;
        return result;
    }

    @Override
    public String toString() {
        return "Score:" + redScore + "-" + blueScore;
    }
}
