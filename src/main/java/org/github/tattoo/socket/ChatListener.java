package org.github.tattoo.socket;

import org.github.tattoo.socket.model.ChatMessage;

public interface ChatListener {
    void onMessage(ChatMessage message);
}
