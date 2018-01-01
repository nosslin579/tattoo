package org.github.tattoo;

import org.github.tattoo.impl.singelgroup.SingleGroupTournament;
import org.github.tattoo.impl.singelgroup.model.TournamentStatus;
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

    private final List<SingleGroupTournament> tournaments = Collections.synchronizedList(new ArrayList<>());

//    @PostConstruct
    public void startInitialTournament() {
        log.info("Starting tournament on start up");
        TournamentOptions options = new TournamentOptions();
        options.setTest(true);
        options.setName("Start " + this.hashCode());
        options.setNumberOfMatches(1);
        startTournament(new SingleGroupTournament(options));
    }

//    @Scheduled(cron = "0 55 * * * *")
    public void startTournament() {
        log.info("Starting tournament by schedule");
        startTournament(new SingleGroupTournament());
    }

    public SingleGroupTournament startTournament(SingleGroupTournament newTournament) {
        tournaments.stream()
                .filter(tournament -> !tournament.getTournamentEndFuture().isDone())
                .forEach(tournament -> log.warn("Tournament not done {}", tournament));
        tournaments.add(newTournament);
        newTournament.startTournament();
        newTournament.getTournamentEndFuture().thenAccept(t -> log.info("Tournament ended {}", t));
        return newTournament;
    }


    public List<TournamentStatus> getStatus() {
        return tournaments.stream()
                .map(SingleGroupTournament::getStatus)
                .collect(Collectors.toList());
    }
}
