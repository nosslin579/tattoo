package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.GroupCommand;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.singlegroup.model.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AfterMatchChatListener implements ChatListener {
  public static final String RESTARTGAME = "restartgame";
  public static final String CHECKSCORE = "checkscore";
  private final Logger log = LoggerFactory.getLogger(AfterMatchChatListener.class);
  private final CapResultCollector capResultCollector;
  private final SingleGroupTournament tournament;
  private final Group group;
  private final Match match;
  private final Map<String, String> votes = new ConcurrentHashMap<>();

  private volatile Instant coolDown = Instant.now();


  public AfterMatchChatListener(CapResultCollector capResultCollector, SingleGroupTournament tournament, Group group, Match match) {
    this.capResultCollector = capResultCollector;
    this.tournament = tournament;
    this.group = group;
    this.match = match;
  }

  @Override
  public void onMessage(ChatMessage message) {
    String text = message.getMessage() == null ? "" : message.getMessage();

    if (CHECKSCORE.equals(text)) {
      if (coolDown.isAfter(Instant.now())) {
        return;
      }
      coolDown = Instant.now().plusSeconds(5);
      if (updateScore()) {
        complete();
      }
    } else if (RESTARTGAME.equals(text)) {
      votes.put(message.getFrom(), text);
      if (votes.values().stream().filter(RESTARTGAME::equals).count() > 5) {
        complete();
      }
    }
  }

  private void complete() {
    synchronized (this) {
      this.notifyAll();
    }
  }

  private boolean updateScore() {
    tournament.setState(TournamentState.UPDATING_SCORE);
    GroupCommand command = group.getCommand();
    return capResultCollector.getCapResult(match)
        .map(capResult -> {
          log.info("Setting the score. {}", capResult);
          tournament.setState(TournamentState.GOT_SCORE);
          command.chat(capResult.toString());

          match.getRedTeam().setCaps(capResult.getRedCaps());
          match.getBlueTeam().setCaps(capResult.getBlueCaps());
          tournament.getCompletedMatches().add(match);

          ResultUtil.getReadableResult(tournament, 4).forEach(command::chat);
          return true;
        })
        .orElseGet(() -> {
          command.chat("Could not get cap result");
          return false;
        });
  }
}
