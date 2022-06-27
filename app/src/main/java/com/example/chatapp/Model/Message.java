package com.example.chatapp.Model;

public class Message {
    private String message;
    private String senderID;
    private long timeStamp;
    private boolean type;

    public boolean isType() {
        return type;
    }

    public void setType(boolean type) {
        this.type = type;
    }



    public Message(String message, String senderID, long timeStamp, boolean type) {
        this.message = message;
        this.senderID = senderID;
        this.timeStamp = timeStamp;
        this.type = type;
    }

    public Message() {
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
