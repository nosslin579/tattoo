package org.github.tattoo.groupsocket.model;

/**
 * Represents a player that is connected to group socket
 * Values sent from server, thus variables here can not be changed
 */

public class Member {
  public static final String IN_HERE = "page";
  public static final String JOINING = "joining";
  public static final String IN_GAME = "game";
  public static final String DISCONNECTED = "???";
  private String id;
  private String name;
  private String location;
  private int team;
  private boolean spectator;
  private boolean leader;
  private long lastSeen;

  /**
   * The tagPro id
   */
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Member member = (Member) o;

    return id.equals(member.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
