package model;

import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;
    private final String content;
    public Message(MessageType type, String content) {
        this.type = type;
        this.content = content;
    }

    public MessageType type() {
        return type;
    }

    public String content() {
        return content;
    }
}