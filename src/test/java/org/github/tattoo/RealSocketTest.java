package org.github.tattoo;

import io.socket.client.Socket;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.github.tattoo.groupsocket.GameCommand;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.GroupCommand;
import org.github.tattoo.groupsocket.SocketFactory;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.TeamId;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ContextConfiguration(classes = RealSocketTest.RealSocketMockConfig.class)
public class RealSocketTest {
  private Logger log = LoggerFactory.getLogger(this.getClass());
  static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  SocketFactory socketFactory;

  @Test
  @Ignore("Only for manual testing")
  public void createGroupAndStartGame() throws InterruptedException {
    LocalDateTime now = LocalDateTime.now();
    int h = now.getHour();
    int m = now.getMinute();
    TournamentOptions options = new TournamentOptions();
    options.setName("Asdf" + System.currentTimeMillis());
    Group group = socketFactory.createGroupSocket(options);
    GroupCommand groupCommand = group.getCommand();
    Thread.sleep(10000);
    groupCommand.allowSelfAssignment();
    groupCommand.setDiscoverable(true);
    Thread.sleep(1000);
    groupCommand.setDiscoverable(false);
    groupCommand.setServerSelect(true);
    Thread.sleep(1000);
    groupCommand.setServer("abb1f9f7c95a");
    groupCommand.setSettingMap("Duel");
    groupCommand.setSettingCaps(1);
    groupCommand.setSettingTime(1);

    List<RealSocketTest.DoNothingBot> bots = new ArrayList<>();
    bots.add(new RealSocketTest.DoNothingBot(socketFactory));
    bots.add(new RealSocketTest.DoNothingBot(socketFactory));

    bots.forEach(doNothingBot -> doNothingBot.setTagProCookie(socketFactory.getTagProCookie()));
    bots.forEach(doNothingBot -> doNothingBot.joinGroup(group.getGroupId()));

    Thread.sleep(10000);

    bots.forEach(doNothingBot -> groupCommand.moveMemberToTeam(doNothingBot.getTagProId(), TeamId.RED));

    groupCommand.moveMemberToTeam(group.getTagProId(), TeamId.SPECTATOR);
    groupCommand.moveMemberToTeam("ZJApetqOYncN2OCDUhZMEshN3yCmrldn", TeamId.BLUE);

    Thread.sleep(10000);

    groupCommand.launch();

    Socket joinerSocket = socketFactory.joinJoinerSocket(group.getTagProCookie());
    bots.forEach(DoNothingBot::joinJoiner);


    joinerSocket.on("FoundWorld", objects -> {
      log.info("Found world {}", Arrays.toString(objects));
      while (true) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        String currentServer = socketFactory.getCurrentServer(group.getTagProCookie());
        if (currentServer != null) {
//          bots.forEach(doNothingBot -> doNothingBot.joinGame(currentServer));
          joinerSocket.disconnect();
          Socket gameSocket = socketFactory.joinGameSocket(currentServer, group.getTagProCookie());
          GameCommand gameCommand = new GameCommand(gameSocket);
          pool.scheduleAtFixedRate(gameCommand::heartbeat, 10, 2, TimeUnit.SECONDS);
          return;
        }
      }
    });


    Thread.sleep(120000);
    log.info("Exit1");
    groupCommand.disconnect();
    log.info("Exit2");
    bots.forEach(RealSocketTest.DoNothingBot::disconnect);
    log.info("Exit3");
    pool.shutdownNow();
//    pool.shutdown();

    log.info("Exit");
    System.exit(0);
  }

  private Socket addToJoinerSocket(String groupId) {
    String tagProId = socketFactory.getTagProCookie();
    Socket socket1 = socketFactory.joinGroupSocket(groupId, tagProId);
    GroupCommand command = new GroupCommand(socket1);
    pool.scheduleAtFixedRate(() -> command.touch(Member.IN_HERE), 5, 30, TimeUnit.SECONDS);
    return socket1;
  }


  public static class DoNothingBot {

    private String tagProCookie;
    private Socket groupSocket;
    private Socket gameSocket;
    private SocketFactory socketFactory;

    public DoNothingBot(SocketFactory socketFactory) {
      this.socketFactory = socketFactory;
    }

    public void setTagProCookie(String tagProCookie) {
      this.tagProCookie = tagProCookie;
    }

    public String getTagProCookie() {
      return tagProCookie;
    }

    public void joinGroup(String groupId) {
      groupSocket = socketFactory.joinGroupSocket(groupId, tagProCookie);
      GroupCommand command = new GroupCommand(groupSocket);
      pool.scheduleAtFixedRate(() -> command.touch(Member.IN_HERE), 5, 30, TimeUnit.SECONDS);
    }

    public String getTagProId() {
      return tagProCookie.substring(2, 34);
    }

    public void joinGame(String server) {
      gameSocket = socketFactory.joinGameSocket(server, tagProCookie);
      GameCommand gameCommand = new GameCommand(gameSocket);
      pool.scheduleAtFixedRate(gameCommand::heartbeat, 5, 2, TimeUnit.SECONDS);
    }

    public void disconnect() {
//      gameSocket.disconnect();
      groupSocket.disconnect();
    }

    public void joinJoiner() {
      socketFactory.joinJoinerSocket(tagProCookie);
    }
  }

  public static class RealSocketMockConfig {
    @Bean
    public SocketFactory socketFactory() {
      return new SocketFactory();
    }
  }
}
