package com.example.chatapp.Model;

public class Message {
    private String message;
    private String senderID;
    private long timeStamp;

    public Message() {
    }

    public Message(String message, String senderID, long timeStamp) {
        this.message = message;
        this.senderID = senderID;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
