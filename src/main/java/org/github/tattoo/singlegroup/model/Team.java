package org.github.tattoo.singlegroup.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
  private List<Participant> players = new ArrayList<>();
  private int caps;
  private String name;
  private final int teamId;

  public Team(int teamId) {
    this.teamId = teamId;
  }

  public List<Participant> getPlayers() {
    return players;
  }

  public void setPlayers(List<Participant> players) {
    this.players = players;
  }

  public int getCaps() {
    return caps;
  }

  public void setCaps(int caps) {
    this.caps = caps;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getTeamId() {
    return teamId;
  }
}