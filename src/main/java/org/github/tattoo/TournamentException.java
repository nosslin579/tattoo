package org.github.tattoo;

import java.io.IOException;
import java.net.URISyntaxException;

public class TournamentException extends RuntimeException {
    public TournamentException(String message) {
        super(message);
    }

    public TournamentException(Throwable e) {
        super(e);
    }

    public TournamentException(String s, Throwable e) {
        super(s, e);
    }
}
