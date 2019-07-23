package org.github.tattoo.groupsocket;

import io.socket.client.Socket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commands that can be sent to group socket
 */
public class GameCommand {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Socket socket;
  private int packetId=0;

  public GameCommand(Socket socket) {
    this.socket = socket;
  }

  private void setting(String name, Object value) {
    log.info("Setting: {} {}", name, value);
    final JSONObject object = new JSONObject();
    object.put("name", name);
    object.put("value", value);
    socket.emit("setting", object);
  }

  public void chat(String message) {
    if (message.length() > 120) {
      String truncated = message.substring(0, 120);
      log.warn("Chat message longer than 120 chars. Truncating to {}", truncated);
      socket.emit("chat", truncated);
    } else {
      log.info("Chat: {}", message);
      socket.emit("chat", message);
    }
  }

  public void disconnect() {
    log.info("Disconnect");
    socket.disconnect();
  }

  public void heartbeat() {
    final JSONObject p = new JSONObject();
    p.put("id", packetId++);
    p.put("c", 60);
    socket.emit("p", p);
  }
}
