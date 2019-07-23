package org.github.tattoo.groupsocket;

import io.socket.client.Socket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Commands that can be sent to group socket
 */
public class GroupCommand {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  private final Socket socket;

  public GroupCommand(Socket socket) {
    this.socket = socket;
  }

  private void setting(String name, Object value) {
    log.info("Setting: {} {}", name, value);
    final JSONObject object = new JSONObject();
    object.put("name", name);
    object.put("value", value.toString());
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

  public void switchToPug() {
    socket.emit("pug");
  }

  public void switchToPub() {
    socket.emit("pub");
  }

  public void setDiscoverable(boolean value) {
    socket.emit("discoverable", value);
  }

  public void moveMemberToTeam(String memberId, int team) {
    log.info("Team(moving member): {} {}", memberId, team);
    final JSONObject object = new JSONObject();
    object.put("id", memberId);
    object.put("team", team);
    socket.emit("team", object);
  }

  public void launch() {
    log.info("Group play");
    socket.emit("groupPlay");
  }

  public void setSettingMap(String mapName) {
    setting("map", mapName);
  }

  public void setSettingTime(int minuntes) {
    setting("time", String.valueOf(minuntes));
  }

  public void setSettingCaps(int amount) {
    setting("caps", String.valueOf(amount));
  }

  public void setSettingSelfAssignment(boolean allowed) {
    setting("selfAssignment", allowed);
  }

  public void disallowSelfAssignment() {
    setSettingSelfAssignment(false);
  }

  public void allowSelfAssignment() {
    setSettingSelfAssignment(true);
  }

  public void touch(String location) {
    socket.emit("touch", location);
  }

  public void setRegion(String... regions) {
    setting("regions", regions);
  }

  public void setServerSelect(boolean b) {
    setting("serverSelect", b);
  }

  public void setServer(String server) {
    setting("server", server);
  }

  public void disconnect() {
    log.info("Disconnect");
    socket.disconnect();
  }

  @Override
  public String toString() {
    return "Connected: " + String.valueOf(socket.connected());
  }

  public void setRedTeamName(String name) {
    setting("redTeamName", name);
  }
  public void setBlueTeamName(String name) {
    setting("blueTeamName", name);
  }
}
