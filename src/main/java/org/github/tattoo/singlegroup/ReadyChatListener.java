package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

class ReadyChatListener implements ChatListener {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private static final List<String> READY_CHAT_TEXTS = Arrays.asList("r", "ready", "rdy", "yes");
  private final Set<String> playersReady = ConcurrentHashMap.newKeySet();
  private final Match match;
  private final Group group;

  ReadyChatListener(Match match, Group group) {
    this.match = match;
    this.group = group;
  }

  @Override
  public void onMessage(ChatMessage msg) {

    if (READY_CHAT_TEXTS.contains(msg.getMessage())) {
      log.info("Player is ready {}, total {} ready", msg.getFrom(), playersReady.size());
      group.getMemberByName(msg.getFrom())
          .filter(m -> Stream.of(match.getBlueTeam(), match.getRedTeam())
              .flatMap(team -> team.getPlayers().stream())
              .map(Participant::getTagProId)
              .anyMatch(p -> m.getId().equals(p)))
          .ifPresent(member -> playersReady.add(member.getId()));
    }

    if (isPlayersReady()) {
      synchronized (match) {
        match.notifyAll();
      }
    }
  }

  boolean isPlayersReady() {
    int players = match.getRedTeam().getPlayers().size() + match.getBlueTeam().getPlayers().size();
    return playersReady.size() == players;
  }
}
