package org.github.tattoo.web;

import org.github.tattoo.TattooManager;
import org.github.tattoo.TournamentOptions;
import org.github.tattoo.singlegroup.SingleGroupTournament;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TournamentController {

  @Autowired
  private TattooManager manager;

  @RequestMapping(value = "/tournament", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public List<SingleGroupTournament> getTournament() {
    return manager.getTournaments();
  }

  @RequestMapping(value = "/tournament", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> startTournament(HttpEntity<TournamentOptions> entity) {
    TournamentOptions options = entity.getBody();
    if (options == null || options.getSignUpWaitTime() > 600) {
      return ResponseEntity.badRequest().build();
    }
    manager.startTournamentAsync(options);
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/schedule", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public TournamentOptions[] getTournamentOptions() {
    return manager.getSchedule();
  }

  @RequestMapping(value = "/result", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Result[] getResults(@RequestParam(required = false, defaultValue = "10") int limit) {
    return manager.getResults(limit);
  }


}