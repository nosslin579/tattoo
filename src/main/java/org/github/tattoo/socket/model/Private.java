package org.github.tattoo.socket.model;

public class Private {
    private int maxPlayers;
    private boolean noScript;
    private int maxSpectators;
    private boolean selfAssignment;
    private boolean respawnWarnings;
    private boolean isPrivate;

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isNoScript() {
        return noScript;
    }

    public void setNoScript(boolean noScript) {
        this.noScript = noScript;
    }

    public int getMaxSpectators() {
        return maxSpectators;
    }

    public void setMaxSpectators(int maxSpectators) {
        this.maxSpectators = maxSpectators;
    }

    public boolean isSelfAssignment() {
        return selfAssignment;
    }

    public void setSelfAssignment(boolean selfAssignment) {
        this.selfAssignment = selfAssignment;
    }

    public boolean isRespawnWarnings() {
        return respawnWarnings;
    }

    public void setRespawnWarnings(boolean respawnWarnings) {
        this.respawnWarnings = respawnWarnings;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
