package org.github.tattoo.groupsocket;

import io.socket.client.Socket;
import org.github.tattoo.groupsocket.model.Member;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Group {

  private final ChatEmitterListener chatListener;

  private final String name;
  private final Socket socket;
  private final Map<String, Member> members;
  private final String groupId;
  private final String tagProCookie;//s:cmTjyF_0SN4QeB2tyfD8rI8OJM5GT33X.lKqVJ1sjcr89S2xIic61bGppFMjxTzw70AzjA7KsHiE

  public Group(String name, String groupId, String tagProCookie, Map<String, Member> members, Socket socket, ChatEmitterListener chatListener) {
    this.name = name;
    this.groupId = groupId;
    this.tagProCookie = tagProCookie;
    this.members = members;
    this.socket = socket;
    this.chatListener = chatListener;
  }

  public String getName() {
    return name;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getTagProCookie() {
    return tagProCookie;
  }

  public String getTagProId() {
    return tagProCookie.substring(2, 34);
  }

  public Socket getSocket() {
    return socket;
  }

  public ChatEmitterListener getChatListener() {
    return chatListener;
  }

  public GroupCommand getCommand() {
    return new GroupCommand(socket);
  }

  public Collection<Member> getMembers() {
    return Collections.unmodifiableCollection(members.values());
  }

  public Optional<Member> getMemberByName(String name) {
    return members
        .values()
        .stream()
        .filter(member -> member.getLocation().equals(Member.IN_HERE))
        .filter(member -> Objects.equals(member.getName(), name))
        .findFirst();
  }

  public Optional<Member> getMemberById(String id) {
    return Optional.ofNullable(members.get(id));
  }
}
