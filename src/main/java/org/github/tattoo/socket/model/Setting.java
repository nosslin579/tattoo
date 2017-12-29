package org.github.tattoo.socket.model;

public class Setting {
    private final String name;
    private final String value;

    public Setting(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
