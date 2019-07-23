package org.github.tattoo.groupsocket;

import org.github.tattoo.groupsocket.model.ChatMessage;

public interface ChatListener {
    void onMessage(ChatMessage message);
}
