package org.github.tattoo.socket;

import com.google.gson.Gson;
import io.socket.emitter.Emitter;
import org.github.tattoo.socket.model.ChatMessage;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatEmitterListener implements Emitter.Listener {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Gson gson = new Gson();
    private final List<ChatListener> listeners = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void call(Object... objects) {
        log.debug("Chat: {}", objects[0]);
        final JSONObject chatObject = (JSONObject) objects[0];
        ChatMessage message = gson.fromJson(chatObject.toString(), ChatMessage.class);
        new ArrayList<>(listeners).forEach(listener -> listener.onMessage(message));
    }

    public void addListener(ChatListener listener) {
        if (listeners.contains(listener)) {
            log.warn("Add listener again {}", listener);
        }
        listeners.add(listener);
    }

    public void removeListener(ChatListener listener) {
        boolean remove = listeners.remove(listener);
        log.debug("Removed listener {} {}", listener, remove);
    }

    public void removeListener(Class<? extends ChatListener> listenerClass) {
        listeners.removeIf(listener -> listenerClass.equals(listener.getClass()));
    }

    @Override
    public String toString() {
        return listeners.toString();
    }
}
