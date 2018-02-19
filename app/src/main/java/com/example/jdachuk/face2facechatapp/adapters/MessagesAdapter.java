package com.example.jdachuk.face2facechatapp.adapters;

/**
 * Created by jdachuk on 19.02.18.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.models.Message;
import com.example.jdachuk.face2facechatapp.view_holders.MessageViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MessagesAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private DatabaseReference mUsersData;
    private FirebaseUser mCurrentUser;

    private ArrayList<Message> mMessagesCollection;

    public MessagesAdapter(ArrayList<Message> mMessagesCollection) {
        this.mMessagesCollection = mMessagesCollection;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mUsersData = FirebaseDatabase.getInstance().getReference()
                .child("Users");
        mUsersData.keepSynced(true);

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_message_layout, parent, false);

        return new MessageViewHolder(view, mCurrentUser.getUid(), mUsersData);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        Message message = mMessagesCollection.get(position);
        holder.setData(message);
    }

    @Override
    public int getItemCount() {
        return mMessagesCollection.size();
    }
}
