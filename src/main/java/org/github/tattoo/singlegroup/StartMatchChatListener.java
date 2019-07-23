package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartMatchChatListener implements ChatListener {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Runnable closeSignUp;

    public StartMatchChatListener(Runnable closeSignUp) {
        this.closeSignUp = closeSignUp;
    }

    @Override
    public void onMessage(ChatMessage message) {
        if ("go666".equals(message.getMessage())) {
            closeSignUp.run();
        }
    }
}
