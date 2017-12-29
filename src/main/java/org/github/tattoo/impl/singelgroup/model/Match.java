package org.github.tattoo.impl.singelgroup.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Match {
    private final Map<String, TeamMember> teamMembers = new HashMap<>();
    boolean finished = false;
    private MatchScore score;
    private List<Participant> reserve;

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Map<String, TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public void setScore(MatchScore score) {
        this.score = score;
    }

    public MatchScore getScore() {
        return score;
    }

    public void setReservePlayers(List<Participant> reserve) {
        this.reserve = reserve;
    }

    public List<Participant> getReserve() {
        return reserve;
    }
}
