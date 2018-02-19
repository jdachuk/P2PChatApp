package com.example.jdachuk.face2facechatapp.models;

/**
 * Created by jdachuk on 19.02.18.
 */

import com.google.firebase.database.DataSnapshot;

public class Chat {
    private String chatID;
    private Message last_message;

    public Chat(DataSnapshot dataSnapshot) {
        this.chatID = dataSnapshot.getKey();
        this.last_message = dataSnapshot.child("last_message").getValue(Message.class);
    }

    public Message getLast_message() {
        return last_message;
    }

    public String getChatID() {
        return chatID;
    }
}
