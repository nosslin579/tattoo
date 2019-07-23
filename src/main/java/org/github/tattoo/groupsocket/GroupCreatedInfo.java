package org.github.tattoo.groupsocket;

public class GroupCreatedInfo {
  private final String tagProCookie;
  private final String groupId;

  public GroupCreatedInfo(String tagProCookie, String groupId) {
    this.tagProCookie = tagProCookie;
    this.groupId = groupId;
  }

  public String getTagProCookie() {
    return tagProCookie;
  }

  public String getTagProId() {
    return tagProCookie.substring(2, 34);
  }

  public String getGroupId() {
    return groupId;
  }
}
