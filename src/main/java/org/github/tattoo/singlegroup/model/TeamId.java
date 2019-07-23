package org.github.tattoo.singlegroup.model;

public class TeamId {
  public static final int RED = 1;
  public static final int BLUE = 2;
  public static final int SPECTATOR = 3;
  public static final int WAITING = 4;

  private TeamId() {
  }

  public static int invert(int teamId) {
    if (teamId == RED) {
      return BLUE;
    } else if (teamId == BLUE) {
      return RED;
    }
    throw new IllegalArgumentException("Can not invert: " + teamId);
  }

  public static class Toggle {

    private int teamId = RED;

    public int getAndToggle() {
      int ret = teamId;
      teamId = TeamId.invert(teamId);
      return ret;
    }

  }

}