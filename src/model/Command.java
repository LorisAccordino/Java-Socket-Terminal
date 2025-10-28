package model;

import java.io.Serializable;

public class Command implements Serializable {
    private final CommandType type;
    private final String args;
    public Command(CommandType type, String args) {
        this.type = type;
        this.args = args;
    }

    public CommandType type() {
        return type;
    }

    public String args() {
        return args;
    }

    public String[] argsSplitted() {
        return null;
    }
}