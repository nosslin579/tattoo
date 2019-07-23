package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.TeamId;

class SpectatorChatListener implements ChatListener {
  private final Group group;

  public SpectatorChatListener(Group group) {
    this.group = group;
  }

  @Override
  public void onMessage(ChatMessage message) {
    String from = message.getFrom();
    if (message.getMessage().equals("spec") && !from.equals("Some Ball")) {
      long count = group.getMembers()
          .stream()
          .map(Member::getName)
          .filter(from::equals)
          .count();
      if (count != 1) {
        group.getCommand().chat("Can't do that, found " + count + " ppl with name " + from);
        return;
      }
      group.getMemberByName(from)
          .ifPresent(member -> group.getCommand().moveMemberToTeam(member.getId(), TeamId.SPECTATOR));
    }
  }
}
