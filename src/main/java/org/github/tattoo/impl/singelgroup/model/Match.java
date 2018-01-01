package org.github.tattoo.impl.singelgroup.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Match {
    private final Map<String, TeamMember> teamMembers = new HashMap<>();
    boolean finished = false;
    private MatchScore score;
    private List<Participant> reserve;
    private String map;
    private int maxLength;
    private int caps;

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

    public void setMap(String map) {
        this.map = map;
    }

    public String getMap() {
        return map;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public void setCaps(int caps) {
        this.caps = caps;
    }

    public int getCaps() {
        return caps;
    }
}
