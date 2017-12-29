package org.github.tattoo.impl.mock;

import org.github.tattoo.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MockTournament implements Tournament {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void startTournament() {
        log.info("Starting mock");
    }

    @Override
    public CompletableFuture<Tournament> getTournamentEndFuture() {
        return CompletableFuture.completedFuture(this);
    }

    @Override
    public Object getStatus() {
        return "";
    }
}
