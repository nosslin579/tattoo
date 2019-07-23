package org.github.tattoo;

import java.net.URI;
import java.net.URISyntaxException;

public enum TagproServer {
    DEFAULT("https://tagpro.koalabeast.com", 443),
    ORBIT("http://tagpro-orbit.koalabeast.com", 443),
    CHORD("http://tagpro-CHORD.koalabeast.com", 443);

    private final String url;
    private final int port;

    TagproServer(String url, int port) {
        this.url = url;
        this.port = port;
    }

    public String getUrl() {
        return url;
    }

    public int getPort() {
        return port;
    }

    public URI getUri(String path) {
        String uri = url + ":" + port + path;
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new TournamentException(e);
        }

    }
}
