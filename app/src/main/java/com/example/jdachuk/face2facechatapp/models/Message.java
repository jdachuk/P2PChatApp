package com.example.jdachuk.face2facechatapp.models;

/**
 * Created by jdachuk on 19.02.18.
 */

import com.google.firebase.database.DataSnapshot;

public class Message {
    private String messageID;
    private String author;
    private String text;
    private Long time;
    private Boolean seen;

    public Message(){}

    public Message(String author, String text, Long time, Boolean seen) {
        this.author = author;
        this.text = text;
        this.time = time;
        this.seen = seen;
    }

    public Message(DataSnapshot message) {
        this.messageID = message.getKey();
        if(message.hasChild("author"))
            this.author = message.child("author").getValue() + "";
        if(message.hasChild("text"))
            this.text = message.child("text").getValue() + "";
        if(message.hasChild("time"))
            this.time = (long) message.child("time").getValue();
        if(message.hasChild("seen"))
            this.seen = (boolean) message.child("seen").getValue();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getMessageID() {
        return messageID;
    }
}
