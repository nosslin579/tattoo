package org.github.tattoo.groupsocket;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.github.tattoo.TagproServer;
import org.github.tattoo.TournamentException;
import org.github.tattoo.TournamentOptions;
import org.github.tattoo.groupsocket.model.Member;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SocketFactory {
  private static Logger log = LoggerFactory.getLogger(SocketFactory.class);
  private final Gson gson = new Gson();

  public Group createGroupSocket(TournamentOptions options) {
    String name = options.getName();
    Map<String, Member> members = new ConcurrentHashMap<>();
    ChatEmitterListener chatListener = new ChatEmitterListener();

    GroupCreatedInfo group = createGroup(TagproServer.DEFAULT, name);
    Socket socket = joinGroupSocket(group.getGroupId(), group.getTagProCookie());

    ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
    socket.on("chat", chatListener);
    socket.on("member", objects -> onMemberUpdate(objects, members));
    socket.on("removed", objects -> onMemberLeave(objects, members));
    socket.on("disconnect", objects -> {
      log.info("Disconnect from group socket {}", group.getGroupId());
      pool.shutdown();
    });

    GroupCommand command = new GroupCommand(socket);
    pool.scheduleAtFixedRate(() -> command.touch(Member.IN_HERE), 5, 30, TimeUnit.SECONDS);
    pool.scheduleAtFixedRate(command::switchToPug, 10, 99, TimeUnit.SECONDS);
    pool.scheduleAtFixedRate(command::disallowSelfAssignment, 8, 99, TimeUnit.SECONDS);


    return new Group(name, group.getGroupId(), group.getTagProCookie(), members, socket, chatListener);
  }

  public Socket joinJoinerSocket(String tagProCookie) {
    IO.Options opts = new IO.Options();
    opts.forceNew = true;
    opts.reconnection = false;
    final Socket socket = IO.socket(TagproServer.DEFAULT.getUri("/games/find"), opts);
    socket.io().on(Manager.EVENT_TRANSPORT, new SetCookieEmitterListener(tagProCookie));
    socket.on("connect", objects -> log.info("Connected to joiner {}", tagProCookie));
    socket.on("disconnect", objects -> log.info("Disconnect from joiner"));
//    socket.on("FoundWorld", objects -> log.info("FoundWorld:{}", Arrays.toString(objects)));
//        socket.on("WaitingForMembers", objects -> log.info("WaitingForMembers:{}", Arrays.toString(objects)));
    socket.on("CreatingWorld", objects -> log.info("CreatingWorld:{}", Arrays.toString(objects)));
    socket.on("GroupLeaderNotInTheJoiner", objects -> log.error("GroupLeaderNotInTheJoiner:{}", Arrays.toString(objects)));
    socket.on("port", objects -> log.info("port:{}", Arrays.toString(objects)));
    socket.on("TrollControl", objects -> log.warn("TrollControl:{}", Arrays.toString(objects)));
    socket.on("WaitForEligibility", objects -> log.info("WaitForEligibility:{}", Arrays.toString(objects)));
    socket.connect();
    return socket;
  }

  public Socket joinGroupSocket(String group, String tagProCookie) {
    IO.Options opts = new IO.Options();
    opts.forceNew = true;
    opts.reconnection = false;
    final Socket socket = IO.socket(TagproServer.DEFAULT.getUri(group), opts);
    socket.io().on(Manager.EVENT_TRANSPORT, new SetCookieEmitterListener(tagProCookie));
    socket.on("connect", objects -> log.info("Connected to {}", group));
//    socket.on("disconnect", objects -> log.info("Disconnect from {}", group));
//    socket.on("setting", objects -> log.debug("Setting:{}", objects[0]));
    socket.on("full", objects -> log.info("Full:{}", Arrays.toString(objects)));
    socket.connect();

    return socket;
  }

  public GroupCreatedInfo createGroup(TagproServer server, String name) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      HttpPost request = new HttpPost(server.getUrl() + "/groups/create");
      List<BasicNameValuePair> parameters = new ArrayList<>();
      parameters.add(new BasicNameValuePair("name", name));
      parameters.add(new BasicNameValuePair("public", "on"));
      request.setEntity(new UrlEncodedFormEntity(parameters));
      HttpResponse response = httpClient.execute(request);
      if (response.getStatusLine().getStatusCode() < 300) {
        log.error("Response: {}", response);
      }
      Header[] locations = response.getHeaders("Location");
      final Header setCookieHeaders = response.getFirstHeader("Set-Cookie");
      for (HeaderElement headerElement : setCookieHeaders.getElements()) {
        if (headerElement.getName().equals("tagpro2")) {
          String value = URLDecoder.decode(headerElement.getValue(), StandardCharsets.UTF_8.name());
          String gp = locations[0].getValue();
          log.info("Create group with groupPath:{} cookie:{}", gp, value);
          return new GroupCreatedInfo(value, gp);
        }
      }
      throw new TournamentException("TagPro id not found");
    } catch (IOException e) {
      log.error("IO error", e);
      throw new TournamentException("Create group failed", e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        log.error("Failed to close http client", e);
      }
    }
  }

  public String getTagProCookie() {
    CloseableHttpClient client = HttpClients.createDefault();
    try {
      HttpGet request = new HttpGet(TagproServer.DEFAULT.getUrl());
      HttpResponse response = client.execute(request);
      final Header setCookieHeaders = response.getFirstHeader("Set-Cookie");
      for (HeaderElement headerElement : setCookieHeaders.getElements()) {
        if (headerElement.getName().equals("tagpro2")) {
          return URLDecoder.decode(headerElement.getValue(), StandardCharsets.UTF_8.name());
        }
      }
    } catch (IOException e) {
      log.error("IO error", e);
    } finally {
      try {
        client.close();
      } catch (IOException e) {
        log.error("Failed to close http client", e);
      }
    }
    throw new TournamentException("No session could be found");
  }

  public String getCurrentServer(String tagproCookie) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    try {
      HttpGet request = new HttpGet(TagproServer.DEFAULT.getUrl() + "/game");
      request.addHeader("Cookie", "tagpro2=" + tagproCookie);
      HttpResponse response = httpClient.execute(request);
      StringWriter sr = new StringWriter();
      InputStream content = response.getEntity().getContent();
      Stream<String> lines = new BufferedReader(new InputStreamReader(content)).lines();
      List<String> collect = lines.collect(Collectors.toList());
      List<String> filtered = collect.stream()
          .filter(s -> s.contains("tagproConfig.gameSocket"))
          .collect(Collectors.toList());
      if (filtered.size() == 1) {
        String ret = filtered.get(0);
        log.info("Found game server: {}", ret);
        return ret.trim().split("\"")[1];
      } else {
        log.error("No server found {}", collect.stream().filter(s -> s.contains("tagproConfig")).collect(Collectors.joining("\n")));
      }
    } catch (IOException e) {
      log.error("IO error", e);
    } finally {
      try {
        httpClient.close();
      } catch (IOException e) {
        log.error("Failed to close http client", e);
      }
    }

//    log.info("No session could be found");
    return null;
  }

  public Socket joinGameSocket(String server, String tagProCookie) {
    log.info("Connecting to game socket {} {}", server, tagProCookie);
    try {
      IO.Options opts = new IO.Options();
      opts.forceNew = true;
      final Socket socket = IO.socket(server, opts);
      socket.io().on(Manager.EVENT_TRANSPORT, new SetCookieEmitterListener(tagProCookie));
      socket.on("connect", objects -> log.info("Connected to game socket"));
      socket.on("chat", objects -> log.info("Chat {}", Arrays.toString(objects)));
      socket.on("disconnect", objects -> log.info("Disconnect from game socket"));
      socket.connect();

      return socket;
    } catch (Exception e) {
      throw new TournamentException("Failed to join gamesocket cookie:" + tagProCookie);
    }
  }

  public void leaveGroup(TagproServer server, String tagProId) {
    try {
//      HttpGet request = new HttpGet(new URI(server.getUrl() + "/groups/leave"));
//      HttpResponse response = httpClient.execute(request);
//      log.info("Leaving {}", response);
    } catch (Exception e) {
      throw new TournamentException(e);
    }
  }


  private void onMemberUpdate(Object[] objects, Map<String, Member> members) {
    log.debug("Member update:{}", objects[0]);
    final JSONObject chatObject = (JSONObject) objects[0];
    Member member = gson.fromJson(chatObject.toString(), Member.class);
    members.put(member.getId(), member);
  }

  private void onMemberLeave(Object[] objects, Map<String, Member> members) {
    final JSONObject chatObject = (JSONObject) objects[0];
    Member member = gson.fromJson(chatObject.toString(), Member.class);
    log.info("Member leaving, name:{} id:{}", member.getName(), member.getId());
    members.remove(member.getId());
  }
}
