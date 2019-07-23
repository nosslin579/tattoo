package org.github.tattoo.groupsocket;

import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetCookieEmitterListener implements Emitter.Listener {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final String tagPro2Cookie;

  public SetCookieEmitterListener(String tagProCookie) {
    this.tagPro2Cookie = "tagpro2=" + tagProCookie + ";";
  }

  @Override
  public void call(Object... args) {
    Transport transport = (Transport) args[0];

    transport.on(Transport.EVENT_REQUEST_HEADERS, new Emitter.Listener() {
      @Override
      public void call(Object... args) {
        log.debug("Got event request header {}", Arrays.toString(args));
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>) args[0];
        headers.put("Cookie", Collections.singletonList(tagPro2Cookie));
      }
    });

    transport.on(Transport.EVENT_RESPONSE_HEADERS, new Emitter.Listener() {
      @Override
      public void call(Object... args) {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers = (Map<String, List<String>>)args[0];
//        log.info("Got response headers: {}",headers);
      }
    });

  }
}
