package com.example.jdachuk.face2facechatapp.view_holders;

/**
 * Created by jdachuk on 19.02.18.
 */

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.jdachuk.face2facechatapp.ChatActivity;
import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.models.Chat;
import com.example.jdachuk.face2facechatapp.models.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatViewHolder extends RecyclerView.ViewHolder {

    private DatabaseReference mChatsInfo;

    private View mView;
    private CircleImageView mChatPhoto;
    private TextView mChatName, mLastMessage;

    public ChatViewHolder(View itemView, DatabaseReference mChatsInfo) {
        super(itemView);

        mView = itemView;
        mChatName = mView.findViewById(R.id.chat_name);
        mChatPhoto = mView.findViewById(R.id.chat_photo);
        mLastMessage = mView.findViewById(R.id.chat_last_message);
        this.mChatsInfo = mChatsInfo;
    }

    public void SetData(Chat chat) {
        SetUpChatInfo(chat.getChatID());
        SetLastMessage(chat.getLast_message());
    }

    private void SetUpChatInfo(final String id) {
        mChatsInfo.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String photo = dataSnapshot.child("thumb_image").getValue() + "";
                String name = dataSnapshot.child("name").getValue() + "";

                mChatName.setText(name);
                if(!photo.equals("default")) {
                    Picasso.with(mView.getContext()).load(photo)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mChatPhoto, new Callback() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onError() {
                                    Picasso.with(mView.getContext()).load(photo)
                                            .into(mChatPhoto);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChat(id);
            }
        });
    }

    private void openChat(String id) {
        Intent chatIntent = new Intent(mView.getContext(), ChatActivity.class);
        chatIntent.putExtra("user_id", id);
        mView.getContext().startActivity(chatIntent);
    }

    private void SetLastMessage(Message last_message) {
        mLastMessage.setText(last_message.getText());
    }
}
