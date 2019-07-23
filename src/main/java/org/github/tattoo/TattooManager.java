package org.github.tattoo;

import com.google.gson.Gson;
import org.github.tattoo.singlegroup.ResultUtil;
import org.github.tattoo.singlegroup.SingleGroupTournament;
import org.github.tattoo.singlegroup.SingleGroupTournamentManager;
import org.github.tattoo.singlegroup.model.Match;
import org.github.tattoo.singlegroup.model.Participant;
import org.github.tattoo.web.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

@Component
public class TattooManager {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final TournamentOptions[] tournamentOptions;

  @Autowired
  private SingleGroupTournamentManager singleGroupTournamentManager;
  @Autowired
  private TaskScheduler taskScheduler;

  private final List<SingleGroupTournament> tournaments = Collections.synchronizedList(new ArrayList<>());
  private final Gson gson = new Gson();

  public TattooManager() throws FileNotFoundException {
    Reader r = new FileReader("toa.json");//tournament options array
    tournamentOptions = gson.fromJson(r, TournamentOptions[].class);
  }

  @PostConstruct
  public void startInitialTournament() {
    TournamentOptions options = null;
    if (false) {
      log.info("Starting test tournament on start up");
      options = new TournamentOptions();
      options.setName("Test" + this.hashCode());
      options.setNumberOfMatches(2);
      CompletableFuture.supplyAsync(() -> singleGroupTournamentManager.runTournament(new SingleGroupTournament(options)), Executors.newSingleThreadExecutor());
    }

    if (false) {
      SingleGroupTournament mockData = new SingleGroupTournament(options);
      Participant participant = new Participant("asdf", "qwer");
      mockData.getParticipants().add(participant);
      Match match = new Match();
      match.getRedTeam().getPlayers().add(participant);
      mockData.getCompletedMatches().add(match);
      tournaments.add(mockData);
    }

    for (TournamentOptions to : tournamentOptions) {
      log.info("Scheduling tournament {}", to);
      taskScheduler.schedule(() -> startTournament(to), new CronTrigger(to.getSchedule()));
    }
  }

  public void startTournament(TournamentOptions options) {
    SingleGroupTournament tournament = new SingleGroupTournament(options);
    tournaments.add(tournament);
    singleGroupTournamentManager.runTournament(tournament);
  }

  public List<SingleGroupTournament> getTournaments() {
    return tournaments;
  }

  public TournamentOptions[] getTournamentOptions() {
    return tournamentOptions;
  }

  public Result[] getResults(int limit) {
    List<SingleGroupTournament> list = new ArrayList<>(tournaments);
    Collections.reverse(list);
    return list.stream()
        .filter(tournament -> !tournament.getParticipants().isEmpty())
        .limit(limit)
        .map(singleGroupTournament -> new Result(ResultUtil.getParticipantResults(singleGroupTournament), singleGroupTournament))
        .toArray(Result[]::new);
  }
}
