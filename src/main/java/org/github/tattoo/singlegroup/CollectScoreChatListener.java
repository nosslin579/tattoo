package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.singlegroup.model.CapResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

class CollectScoreChatListener implements ChatListener {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Consumer<CapResult> scoreConsumer;

    private Map<String, CapResult> score = new ConcurrentHashMap<>();

    public CollectScoreChatListener(Consumer<CapResult> scoreConsumer) {
        this.scoreConsumer = scoreConsumer;
    }

    @Override
    public void onMessage(ChatMessage message) {
        String text = message.getMessage();
        if (text.matches("^\\d+-\\d+$")) {
            String[] split = text.split("-");
            int redScore = Integer.parseInt(split[0]);
            int blueScore = Integer.parseInt(split[1]);
            score.put(message.getFrom(), new CapResult(redScore, blueScore));
            getScore().ifPresent(scoreConsumer::accept);
        }

    }

    public Optional<CapResult> getScore() {
        if (score.isEmpty()) {
            log.debug("No one has told the score");
            return Optional.empty();
        }
        return score.values().stream().findAny();
    }

}
