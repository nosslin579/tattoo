package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.GroupCommand;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class CurrentStateListener implements ChatListener {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final GroupCommand cmd;
  private final SingleGroupTournament tournament;
  private Instant coolDown = Instant.now();

  public CurrentStateListener(GroupCommand cmd, SingleGroupTournament tournament) {
    this.cmd = cmd;
    this.tournament = tournament;
  }

  @Override
  public void onMessage(ChatMessage message) {
    if (message.getMessage().equals("poke")) {
      if (coolDown.isAfter(Instant.now())) {
        log.info("Ignoring poke because cool down");
        return;
      }
      coolDown = Instant.now().plusSeconds(3);
      cmd.chat(tournament.getState().toString());
    }
  }
}
