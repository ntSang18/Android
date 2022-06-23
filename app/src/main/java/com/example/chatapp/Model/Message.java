package com.example.chatapp.Model;

public class Message {
    private String message;
    private String senderID;
    private String imageMessage;
    private long timeStamp;

    public Message() {
    }

    public Message(String message, String senderID, String imageMessage, long timeStamp) {
        this.message = message;
        this.senderID = senderID;
        this.imageMessage = imageMessage;
        this.timeStamp = timeStamp;
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

    public String getImageMessage() {
        return imageMessage;
    }

    public void setImageMessage(String imageMessage) {
        this.imageMessage = imageMessage;
    }
}
