package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.singlegroup.model.Match;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class ReadyChatListener implements ChatListener {
  private static final List<String> READY_CHAT_TEXTS = Arrays.asList("r", "ready", "rdy", "yes");
  private final Set<String> playersReady = ConcurrentHashMap.newKeySet();
  private final Match match;

  public ReadyChatListener(Match match) {
    this.match = match;
  }

  @Override
  public void onMessage(ChatMessage msg) {
    // todo improve
    // exclude players that aren't ready in next game
    // only listen to players in match
    // make all type ready?
    if (READY_CHAT_TEXTS.contains(msg.getMessage())) {
      playersReady.add(msg.getFrom());
    }
    if (isPlayersReady()) {
      synchronized (match) {
        match.notifyAll();
      }
    }
  }

  public boolean isPlayersReady() {
    return playersReady.size() > 1;
  }
}
