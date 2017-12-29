package org.github.tattoo.impl.singelgroup;

import org.github.tattoo.socket.ChatListener;
import org.github.tattoo.socket.model.ChatMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartMatchChatListener implements ChatListener {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Runnable closeSignUp;
    private final boolean test;

    public StartMatchChatListener(Runnable closeSignUp, boolean test) {
        this.closeSignUp = closeSignUp;
        this.test = test;
    }

    @Override
    public void onMessage(ChatMessage message) {
        if (test && "go666".equals(message.getMessage())) {
            closeSignUp.run();
        }
    }
}
