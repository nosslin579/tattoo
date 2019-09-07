package org.github.tattoo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.github.tattoo.singlegroup.ResultUtil;
import org.github.tattoo.singlegroup.SingleGroupTournament;
import org.github.tattoo.singlegroup.SingleGroupTournamentManager;
import org.github.tattoo.web.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class TattooManager {
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  private SingleGroupTournamentManager singleGroupTournamentManager;
  @Autowired
  private TaskScheduler taskScheduler;

  private final ThreadPoolExecutor onDemandPool = new ThreadPoolExecutor(1, 1, 1L, TimeUnit.SECONDS, new SynchronousQueue<>());
  ;
  private final List<SingleGroupTournament> tournaments = Collections.synchronizedList(new ArrayList<>());
  private final Gson gson;
  private final TournamentOptions[] schedule;

  public TattooManager() {
    gson = new GsonBuilder().setPrettyPrinting().create();

    try {
      FileReader reader = new FileReader("tournaments.json");
      SingleGroupTournament[] fromJson = gson.fromJson(reader, SingleGroupTournament[].class);
      reader.close();
      Stream.of(fromJson)
          .filter(singleGroupTournament -> !singleGroupTournament.getParticipants().isEmpty())
          .forEach(tournaments::add);
    } catch (Exception e) {
      log.error("Failed to load previous tournaments", e);
    }

    try {
      FileReader reader = new FileReader("schedule.json");
      this.schedule = gson.fromJson(reader, TournamentOptions[].class);
      reader.close();
    } catch (Exception e) {
      throw new RuntimeException("Failed to fetch schedule", e);
    }
  }

  @PostConstruct
  public void scheduleTournamnets() {
    for (TournamentOptions to : schedule) {
      log.info("Scheduling tournament {}", to);
      taskScheduler.schedule(() -> startTournament(to), new CronTrigger(to.getSchedule()));
    }
  }

  public void startTournamentAsync(TournamentOptions options) {
    log.info("Starting tournament on demand, {}", options);
    onDemandPool.submit(() -> startTournament(options));
  }

  private void startTournament(TournamentOptions options) {
    try {
      SingleGroupTournament tournament = new SingleGroupTournament(options);
      tournaments.add(tournament);
      singleGroupTournamentManager.runTournament(tournament);
      FileWriter writer = new FileWriter("tournaments.json");
      gson.toJson(tournaments, writer);
      writer.close();
    } catch (Exception e) {
      log.error("Failed to run tournament {}", options, e);
    }
  }

  public List<SingleGroupTournament> getTournaments() {
    return tournaments;
  }

  public TournamentOptions[] getSchedule() {
    return schedule;
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
