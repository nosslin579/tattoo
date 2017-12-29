package org.github.tattoo;

import java.util.concurrent.CompletableFuture;

public interface Tournament {
    void startTournament();

    CompletableFuture<Tournament> getTournamentEndFuture();

    Object getStatus();
}
