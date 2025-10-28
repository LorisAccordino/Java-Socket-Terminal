package model;

public enum CommandType {
    EXIT, // Server asks to disconnect
    REFRESH_REQUEST, // Server asks prompt refresh
    COMMAND,        // Special command
}