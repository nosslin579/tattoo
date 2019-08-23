package org.github.tattoo.singlegroup;

import io.socket.client.Socket;
import org.github.tattoo.groupsocket.ChatEmitterListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.SocketFactory;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.CapResult;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.Participant;
import org.github.tattoo.singlegroup.model.TeamId;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@ComponentScan(basePackages = "org.github.tattoo.singlegroup")
public class SingleGroupMockConfig {

  @Bean
  public Map<String, Member> members() {
    return new HashMap<>();
  }

  @Bean
  public ChatEmitterListener chatEmitterListener() {
    return new ChatEmitterListener();
  }

  @Bean
  public SocketFactory socketFactory(Map<String, Member> members, ChatEmitterListener chatListener) {
    SocketFactory mock = Mockito.mock(SocketFactory.class);
    Socket socket = Mockito.mock(Socket.class);
    Group group = new Group("name", "groupid", "cookie", members, socket, chatListener);
    Mockito.when(mock.createGroupSocket(Mockito.any())).thenReturn(group);
    Mockito.when(mock.joinJoinerSocket(Mockito.any())).thenReturn(socket);
    return mock;
  }

  @Bean
  public CapResultCollector capResultCollector() {
    return new CapResultCollector() {
      @Override
      public Optional<CapResult> getCapResult(Match match) {
        return Optional.of(new CapResult(getCaps(match, TeamId.RED), getCaps(match, TeamId.BLUE)));
      }
    };
  }

  private int getCaps(Match match, int teamId) {
    return match.getTeam(teamId)
        .getPlayers()
        .stream()
        .map(Participant::getName)
        .mapToInt(Integer::valueOf)
        .map(operand -> operand * operand)
        .sum();
  }
}
