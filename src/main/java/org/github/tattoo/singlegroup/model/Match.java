package org.github.tattoo.singlegroup.model;

public class Match {
  private String map;
  private int maxLength;
  private int capLimit;
  private int number;
  private Team redTeam = new Team(TeamId.RED);
  private Team blueTeam = new Team(TeamId.BLUE);

  public Team getTeam(int teamId) {
    if (teamId == TeamId.RED) {
      return redTeam;
    } else if (teamId == TeamId.BLUE) {
      return blueTeam;
    }
    throw new IllegalArgumentException("No such team " + teamId);
  }

  public Team getRedTeam() {
    return redTeam;
  }

  public void setRedTeam(Team redTeam) {
    this.redTeam = redTeam;
  }

  public Team getBlueTeam() {
    return blueTeam;
  }

  public void setBlueTeam(Team blueTeam) {
    this.blueTeam = blueTeam;
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

  public void setCapLimit(int capLimit) {
    this.capLimit = capLimit;
  }

  public int getCapLimit() {
    return capLimit;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }

}
