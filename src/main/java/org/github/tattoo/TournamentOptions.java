package org.github.tattoo;

import org.github.tattoo.singlegroup.model.Variant;

public class TournamentOptions {
  private String serverId = "abb1f9f7c95a";
  private int caps = 1;
  private int lengthOfMatch = 4;
  private int numberOfMatches = 0;
  private String name = "Tattoo " + hashCode();
  private long signUpWaitTime = 60;
  private String map = "EMERALD";
  private int maxPlayers = 12;
  private Variant variant = Variant.CLASSIC;
  private String schedule;//0 0 20 * * MON

  public String getMap() {
    return map;
  }

  public void setMap(String map) {
    this.map = map;
  }

  public int getCaps() {
    return caps;
  }

  public void setCaps(int caps) {
    this.caps = caps;
  }

  public int getLengthOfMatch() {
    return lengthOfMatch;
  }

  public void setLengthOfMatch(int lengthOfMatch) {
    this.lengthOfMatch = lengthOfMatch;
  }

  public int getNumberOfMatches() {
    return numberOfMatches;
  }

  public void setNumberOfMatches(int numberOfMatches) {
    this.numberOfMatches = numberOfMatches;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getSignUpWaitTime() {
    return signUpWaitTime;
  }

  public void setSignUpWaitTime(long signUpWaitTime) {
    this.signUpWaitTime = signUpWaitTime;
  }

  public int getMaxPlayers() {
    return maxPlayers;
  }

  public void setMaxPlayers(int maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  public Variant getVariant() {
    return variant;
  }

  public void setVariant(Variant variant) {
    this.variant = variant;
  }

  public String getServerId() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId = serverId;
  }

  public String getSchedule() {
    return schedule;
  }

  public void setSchedule(String schedule) {
    this.schedule = schedule;
  }

  @Override
  public String toString() {
    return "TournamentOptions{" +
        "serverId='" + serverId + '\'' +
        ", caps=" + caps +
        ", lengthOfMatch=" + lengthOfMatch +
        ", numberOfMatches=" + numberOfMatches +
        ", name='" + name + '\'' +
        ", signUpWaitTime=" + signUpWaitTime +
        ", map='" + map + '\'' +
        ", maxPlayers=" + maxPlayers +
        ", variant=" + variant +
        ", schedule='" + schedule + '\'' +
        '}';
  }
}
