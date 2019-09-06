package org.github.tattoo.singlegroup;

import org.github.tattoo.TournamentException;
import org.github.tattoo.groupsocket.ChatListener;
import org.github.tattoo.groupsocket.Group;
import org.github.tattoo.groupsocket.model.ChatMessage;
import org.github.tattoo.groupsocket.model.Member;
import org.github.tattoo.singlegroup.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SignUpChatListener implements ChatListener {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final SingleGroupTournament tournament;
  private final Group group;
  private final Set<String> starts = ConcurrentHashMap.newKeySet();


  public SignUpChatListener(SingleGroupTournament tournament, Group group) {
    this.tournament = tournament;
    this.group = group;
  }

  @Override
  public void onMessage(ChatMessage message) {
    String from = message.getFrom();
    try {

      List<Participant> participants = tournament.getParticipants();
      for (Participant p : participants) {
        String tagProId = p.getTagProId();
        if (group.getMembers().stream().map(Member::getId).noneMatch(tagProId::equals)) {
          log.info("Member left, cancel sign up {}", p.getName());
          cancelSignUp(p.getTagProId(), tournament);
        }
      }

      if ("join".equals(message.getMessage())) {
        Member member = group.getMemberByName(from).orElseThrow(() -> new TournamentException("Member not found"));
        signUp(member, tournament);
        group.getCommand().chat(from + " signed up");
      } else if ("leave".equals(message.getMessage())) {
        Member member = group.getMemberByName(from).orElseThrow(() -> new TournamentException("Member not found"));
        cancelSignUp(member.getId(), tournament);
        group.getCommand().chat(from + " cancelled");
      } else if ("startearly".equals(message.getMessage())) {
        String memberId = group.getMemberByName(from).orElseThrow(() -> new TournamentException("Member not found")).getId();
        if (participants.stream().map(Participant::getTagProId).anyMatch(memberId::equals)) {
          starts.add(memberId);
          if (starts.size() >= 4 && starts.size() == participants.size()) {
            synchronized (tournament) {
              tournament.notifyAll();
            }
          }
        }

        group.getCommand().chat(from + " cancelled");
      }
    } catch (Exception e) {
      log.error("Sign up failed, {}", from, e);
      group.getCommand().chat(e.getMessage());
    }
  }

  private void signUp(Member member, SingleGroupTournament tournament) {
    Participant participant = new Participant(member.getId(), member.getName());
    if (member.getName().equals("Some Ball")) {
      throw new TournamentException("Cant join with that name");
    }
    List<Participant> participants = tournament.getParticipants();
    if (participants.contains(participant)) {
      throw new TournamentException("Can't sign up twice");
    }
    if (participants.size() == tournament.getOptions().getMaxPlayers()) {
      throw new TournamentException("Tournament full");
    }
    participants.add(participant);
    log.info("Added participant: {}", member.getName());

    if (participants.size() >= tournament.getOptions().getMaxPlayers()) {
      synchronized (tournament) {
        tournament.notifyAll();
      }
    }
  }

  public void cancelSignUp(String tagProId, SingleGroupTournament tournament) {
    tournament.getParticipants().removeIf(participant -> Objects.equals(participant.getTagProId(), tagProId));
    starts.remove(tagProId);
  }

}
