package org.github.tattoo;

import org.github.tattoo.impl.singelgroup.SingleGroupTournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TattooManager {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final List<Tournament> tournaments = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void startInitialTournament() {
        log.info("Starting tournament on start up");
        TournamentOptions options = new TournamentOptions();
        options.setTest(true);
        options.setName("Start " + this.hashCode());
        startTournament(new SingleGroupTournament(options));
    }

    @Scheduled(cron = "0 55 * * * *")
    public void startTournament() {
        log.info("Starting tournament by schedule");
        startTournament(new SingleGroupTournament());
    }

    public Tournament startTournament(Tournament newTournament) {
        tournaments.stream()
                .filter(tournament -> !tournament.getTournamentEndFuture().isDone())
                .forEach(tournament -> log.warn("Tournament not done {}", tournament));
        tournaments.add(newTournament);
        newTournament.startTournament();
        newTournament.getTournamentEndFuture().thenAccept(t -> log.info("Tournament ended {}", t));
        return newTournament;
    }


    public List<Object> getStatus() {
        return tournaments.stream()
                .map(Tournament::getStatus)
                .collect(Collectors.toList());
    }
}
