package org.github.tattoo;

import org.github.tattoo.TagproServer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TournamentOptions {
    private final static AtomicInteger COUNTER = new AtomicInteger(1);
    private TagproServer server = TagproServer.ORBIT;
    private List<String> maps = Arrays.asList("Bulldog", "Market", "Trebuchet", "Apparition", "Cedar", "Cloud", "EMERALD ", "Gumbo",
            "Pilot ", "Plasma", "Tehuitzingo ", "Transilio", "Wamble", "Wombo Combo", "GeoKoala ", "Hexane",
            "Atomic ", "Bombing Run", "Command Center", "Convoy ", "Cosmic", "Dealer", "GamePad", "Thinking with Portals", "Volt");
    private int caps = 1;
    private int lengthOfMatch = 4;
    public int numberOfMatches = 8;
    private String name = "Tattoo " + COUNTER.getAndIncrement();
    private boolean test = false;

    public TagproServer getServer() {
        return server;
    }

    public void setServer(TagproServer server) {
        this.server = server;
    }

    public List<String> getMaps() {
        return maps;
    }

    public void setMaps(List<String> maps) {
        this.maps = maps;
    }

    public int getCaps() {
        return caps;
    }

    public void setCaps(int caps) {
        this.caps = caps;
    }

    public int getLengthOfMatch() {
        return lengthOfMatch;
    }

    public void setLengthOfMatch(int lengthOfMatch) {
        this.lengthOfMatch = lengthOfMatch;
    }

    public int getNumberOfMatches() {
        return numberOfMatches;
    }

    public void setNumberOfMatches(int numberOfMatches) {
        this.numberOfMatches = numberOfMatches;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTest() {
        return test;
    }

    public void setTest(boolean test) {
        this.test = test;
    }
}
