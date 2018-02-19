package com.example.jdachuk.face2facechatapp.adapters;

/**
 * Created by jdachuk on 19.02.18.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.models.Chat;
import com.example.jdachuk.face2facechatapp.view_holders.ChatViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ChatsAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    private DatabaseReference mChatsInfo;

    private ArrayList<Chat> mChatsCollection;

    public ChatsAdapter(ArrayList<Chat> mChatsCollection) {
        this.mChatsCollection = mChatsCollection;

        mChatsInfo = FirebaseDatabase.getInstance().getReference()
                .child("Users");
        mChatsInfo.keepSynced(true);
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_chat_layout, parent, false);

        return new ChatViewHolder(view, mChatsInfo);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        Chat chat = mChatsCollection.get(position);
        holder.SetData(chat);
    }

    @Override
    public int getItemCount() {
        return mChatsCollection.size();
    }
}
