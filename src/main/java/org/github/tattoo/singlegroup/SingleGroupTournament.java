package org.github.tattoo.singlegroup;

import org.github.tattoo.TournamentOptions;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.Participant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class SingleGroupTournament {
  private final transient Logger log = LoggerFactory.getLogger(this.getClass());

  private final TournamentOptions options;
  private final List<Participant> participants = Collections.synchronizedList(new ArrayList<>());
  private volatile TournamentState state = TournamentState.CREATED;
  private Date startTime = new Date();
  private final Deque<Match> completedMatches = new ConcurrentLinkedDeque<>();


  public SingleGroupTournament(TournamentOptions options) {
    this.options = options;
  }

  public SingleGroupTournament() {
    this(new TournamentOptions());
  }

  public TournamentOptions getOptions() {
    return options;
  }

  public TournamentState getState() {
    return state;
  }

  public void setState(TournamentState state) {
    log.info("Changing state from {} to {}", this.state, state);
    this.state = state;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Deque<Match> getCompletedMatches() {
    return completedMatches;
  }

  public List<Participant> getParticipants() {
    return participants;
  }

}
