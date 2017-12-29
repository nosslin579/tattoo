package org.github.tattoo.socket.model;

public class Member {
    //roj=CLtm8NuAkgTbI94bjQEAgoSH
    //tattoo=Dtz8ywLoWEI6dqp2iql9Pg8u18WffBRH
    public static final String IN_HERE = "page";
    public static final String JOINING = "joining";
    public static final String IN_GAME = "game";
    private String id;
    private String name;
    private String location;
    private int team;
    private boolean spectator;
    private boolean leader;
    private long lastSeen;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
}
