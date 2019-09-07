package org.github.tattoo.singlegroup;

import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SignUpChatListener implements ChatListener {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final SingleGroupTournament tournament;
  private final Group group;
  private final Set<String> starts = ConcurrentHashMap.newKeySet();
  private final Map<String, Instant> coolDowns = new ConcurrentHashMap<>();


  public SignUpChatListener(SingleGroupTournament tournament, Group group) {
    this.tournament = tournament;
    this.group = group;
  }

  @Override
  public void onMessage(ChatMessage message) {
    try {
      String from = message.getFrom() == null ? "" : message.getFrom();

      Instant coolDown = coolDowns.getOrDefault(from, Instant.EPOCH);
      coolDowns.put(from, Instant.now().plusSeconds(1));
      if (coolDown.isAfter(Instant.now())) {
        return;
      }

      List<Participant> participants = tournament.getParticipants();

      //check if any participant has left
      starts.removeIf(tagProId -> group.getMembers().stream().map(Member::getId).noneMatch(tagProId::equals));
      if (participants.removeIf(p -> group.getMembers().stream().map(Member::getId).noneMatch(p.getTagProId()::equals))) {
          log.info("Member left, sign up canceled");
      }

      if ("join".equals(message.getMessage())) {
        signUp(from);
      } else if ("startearly".equals(message.getMessage())) {
        startEarly(from, participants);
      }
    } catch (Exception e) {
      log.error("Handle sign up message failed, {} {}", message.getMessage(), message.getFrom(), e);
    }
  }

  private void signUp(String from) {
    Member member = group.getMemberByName(from).orElse(null);
    if (member == null) {
      group.getCommand().chat("Sign up failed, " + from + " not found");
      return;
    }
    Participant participant = new Participant(member.getId(), member.getName());
    if (member.getName().equals("Some Ball")) {
      group.getCommand().chat("Sign up failed, cant join as Some Ball");
      return;
    }
    List<Participant> participants = this.tournament.getParticipants();
    if (participants.contains(participant)) {
      group.getCommand().chat("Sign up failed, can't sign up twice");
      return;
    }
    if (participants.size() == this.tournament.getOptions().getMaxPlayers()) {
      group.getCommand().chat("Sign up failed, tournament full");
      return;
    }

    participants.add(participant);
    log.info("Added participant: {}", member.getName());
    group.getCommand().chat(from + " signed up");

    if (participants.size() >= this.tournament.getOptions().getMaxPlayers()) {
      synchronized (this.tournament) {
        this.tournament.notifyAll();
      }
    }
  }

  private void startEarly(String from, List<Participant> participants) {
    String memberId = group.getMemberByName(from).map(Member::getId).orElse(null);
    if (memberId == null) {
      group.getCommand().chat("Member not found");
      return;
    }
    if (participants.stream().map(Participant::getTagProId).anyMatch(memberId::equals)) {
      starts.add(memberId);
      if (starts.size() >= 4 && starts.size() == participants.size()) {
        synchronized (tournament) {
          tournament.notifyAll();
        }
      }
    }
  }
}
