package cn.ChatRoom.entity;

import lombok.Data;

@Data
public class TextMessage extends Message {
    private String text;

    @Override
    public String toString() {
        return "TextMessage{" +
                "text='" + text + '\'' +
                ", sequenceId=" + sequenceId +
                ", messageType=" + messageType +
                '}';
    }
}
