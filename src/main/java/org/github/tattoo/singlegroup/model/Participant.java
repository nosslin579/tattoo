package org.github.tattoo.singlegroup.model;

import java.util.Date;

/**
 * Represents a player that has signed up for the tournament
 */
public final class Participant {
  private final String tagProId;
  private final String name;
  private final Date signUp = new Date();

  public Participant(String id, String name) {
    this.tagProId = id;
    this.name = name;
  }

  public String getTagProId() {
    return tagProId;
  }

  public String getName() {
    return name;
  }

  public Date getSignUp() {
    return signUp;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    Participant that = (Participant) o;

    return tagProId.equals(that.tagProId);
  }

  @Override
  public int hashCode() {
    return tagProId.hashCode();
  }
}
