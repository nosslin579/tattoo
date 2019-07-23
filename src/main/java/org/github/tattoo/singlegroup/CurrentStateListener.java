package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.GroupCommand;
import org.github.tattoo.groupsocket.model.ChatMessage;

public class CurrentStateListener implements ChatListener {
  private final GroupCommand cmd;
  private final SingleGroupTournament tournament;

  public CurrentStateListener(GroupCommand cmd, SingleGroupTournament tournament) {
    this.cmd = cmd;
    this.tournament = tournament;
  }

  @Override
  public void onMessage(ChatMessage message) {
    if (message.getMessage().equals("poke")) {
      cmd.chat(tournament.getState().toString());
    }
  }
}
