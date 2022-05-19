package com.example.chatapp.Model;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String name;
    private String email;
    private String imageUri;
    private String status;
    private long recentActivity;

    public User() {
    }

    public User(String uid, String name, String email, String imageUri, String status, long recentActivity) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.imageUri = imageUri;
        this.status = status;
        this.recentActivity = recentActivity;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(long recentActivity) {
        this.recentActivity = recentActivity;
    }
}
