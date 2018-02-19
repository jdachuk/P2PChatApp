package com.example.jdachuk.face2facechatapp.models;

/**
 * Created by jdachuk on 18.02.18.
 */

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Objects;

public class User {

    private String userUID;
    private String name;
    private String device_token;
    private String image;
    private String thumb_image;
    private String status;
    private Boolean online;
    private Boolean friend;
    private Long last_seen;

    public User() {}

    public User(String name, String image, String thumb_image, String status, String device_token) {
        this.name = name;
        this.image = image;
        this.thumb_image = thumb_image;
        this.status = status;
        this.device_token = device_token;
    }

    public User(DataSnapshot user) {
        this.userUID = user.getKey();
        if(user.hasChild("name"))
            this.name = user.child("name").getValue() + "";
        if(user.hasChild("image"))
            this.image = user.child("image").getValue() + "";
        if(user.hasChild("status"))
            this.status = user.child("status").getValue() + "";
        if(user.hasChild("thumb_image"))
            this.thumb_image = user.child("thumb_image").getValue() + "";
        if(user.hasChild("online"))
            this.online = (boolean) user.child("online").getValue();
        if(user.hasChild("last_seen"))
            this.last_seen = (long) user.child("last_seen").getValue();
    }

    public int getIndexIn(ArrayList<User> users){
        for (User u: users) {
            if(this.equals(u)) return users.indexOf(u);
        }
        return -1;
    }

    public User(DataSnapshot user, Boolean friend) {
        this.userUID = user.getKey();
        this.friend = friend;
        if(user.hasChild("name"))
            this.name = user.child("name").getValue() + "";
        if(user.hasChild("image"))
            this.image = user.child("image").getValue() + "";
        if(user.hasChild("online"))
            this.online = (boolean) user.child("online").getValue();
        if(user.hasChild("last_seen"))
            this.last_seen = (long) user.child("last_seen").getValue();
    }

    public User(String uid, Boolean friend) {
        this.userUID = uid;
        this.friend = friend;
    }

    private boolean equals(User user) {
        return Objects.equals(this.userUID, user.userUID)
                && this.friend == user.friend;
    }

    public String getUserUID() {
        return userUID;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Boolean getFriend() {
        return friend;
    }

    public void setFriend(Boolean friend) {
        this.friend = friend;
    }

    public Long getLast_seen() {
        return last_seen;
    }

    public void setLast_seen(Long last_seen) {
        this.last_seen = last_seen;
    }
}
