package org.github.tattoo.singlegroup;


import org.github.tattoo.TournamentOptions;
import org.github.tattoo.groupsocket.ChatEmitterListener;
import org.github.tattoo.groupsocket.SocketFactory;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.ParticipantResult;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.github.tattoo.singlegroup.TournamentState.ASK_READY_FOR_LAUNCH;
import static org.github.tattoo.singlegroup.TournamentState.ENDED;
import static org.github.tattoo.singlegroup.TournamentState.MATCH_IN_PROGRESS;
import static org.github.tattoo.singlegroup.TournamentState.SIGN_UP_OPEN;
import static org.junit.Assert.assertEquals;

@ContextConfiguration(classes = {SingleGroupMockConfig.class})
public class SingleGroupTest {

  @ClassRule
  public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();
  @Rule
  public final SpringMethodRule springMethodRule = new SpringMethodRule();

  @Autowired
  SingleGroupTournamentManager singleGroupTournamentManager;
  @Autowired
  SocketFactory socketFactory;
  @Autowired
  ChatEmitterListener chatEmitterListener;
  @Autowired
  Map<String, Member> members;

  @Test
  public void testMatchResult() throws InterruptedException {
    TournamentOptions options = new TournamentOptions();
    options.setNumberOfMatches(5);
    options.setMaxPlayers(10);
    SingleGroupTournament tournament = new SingleGroupTournament(options);
    CompletableFuture.supplyAsync(() -> singleGroupTournamentManager.runTournament(tournament), Executors.newSingleThreadExecutor());

    waitForState(tournament, SIGN_UP_OPEN);
    setAllMembers(Member.IN_HERE);
    members.values()
        .stream()
        .sorted(Comparator.comparing(Member::getName).reversed())
        .forEach(member -> chat(member.getName(), "join"));

    for (int i = 1; tournament.getState() != ENDED; i++) {

      waitForState(tournament, ASK_READY_FOR_LAUNCH);
      members.values().forEach(member -> chat(member.getName(), "ready"));

      setAllMembers(Member.IN_GAME);
      waitForState(tournament, MATCH_IN_PROGRESS);

      setAllMembers(Member.IN_HERE);
      waitForState(tournament, ASK_READY_FOR_LAUNCH, ENDED);

      Match match = tournament.getCompletedMatches().peekLast();
      assertEquals(i, match.getNumber());
      assertEquals(4, match.getRedTeam().getPlayers().size());
      assertEquals(4, match.getBlueTeam().getPlayers().size());
    }

    waitForState(tournament, ENDED);

    assertEquals(10, tournament.getParticipants().size());

    List<Match> completedMatches = new ArrayList<>(tournament.getCompletedMatches());
    assertEquals(5, completedMatches.size());

    ParticipantResult winner = ResultUtil.getParticipantResults(tournament).get(0);
    ParticipantResult runnerUp = ResultUtil.getParticipantResults(tournament).get(1);
    assertEquals(5, winner.getMatchesPlayed());
    assertEquals(5, runnerUp.getMatchesPlayed());
    assertEquals("Wrong winner", "9", winner.getParticipant().getName());
    assertEquals("Wrong runner up", "8", runnerUp.getParticipant().getName());
    assertEquals(795, winner.getCapsScored());
    assertEquals(748, runnerUp.getCapsScored());
    assertEquals(15, winner.getPoints());
    assertEquals(9, runnerUp.getPoints());

    Match match1 = completedMatches.get(0);
    assertEquals(164, match1.getRedTeam().getCaps());
    assertEquals(120, match1.getBlueTeam().getCaps());

    Match match2 = completedMatches.get(1);
    assertEquals(171, match2.getRedTeam().getCaps());
    assertEquals(94, match2.getBlueTeam().getCaps());


  }

  private TournamentState waitForState(SingleGroupTournament tournament, TournamentState... state) throws InterruptedException {
    List<TournamentState> states = Arrays.asList(state);
    for (int i = 0; i < 200; i++) {
      TournamentState ret = tournament.getState();
      if (states.contains(ret)) {
        return ret;
      }
      Thread.sleep(100);
    }
    Assert.fail("Waiting for " + states + " but got " + tournament.getState());
    return null;
  }

  private void chat(String from, String text) {
    ChatMessage message = new ChatMessage();
    message.setFrom(from);
    message.setMessage(text);
    chatEmitterListener.notifyAllListeners(message);
  }

  private void setAllMembers(String location) {
    IntStream.range(0, 10)
        .mapToObj(String::valueOf)
        .forEach(id -> setMember(id, location));
  }

  private void setMember(String id, String location) {
    Member member = new Member();
    member.setName(id);
    member.setId(id);
    member.setLocation(location);
    members.put(member.getId(), member);
  }

}
