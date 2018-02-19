package com.example.jdachuk.face2facechatapp.view_holders;

/**
 * Created by jdachuk on 19.02.18.
 */

import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.adapters.TimeStampConvertor;
import com.example.jdachuk.face2facechatapp.models.Message;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    private DatabaseReference mUsersInfo;

    private View mView;
    private CircleImageView mAuthorPhoto;
    private TextView mAuthorName, mMessageText, mMessageTime;
    private ConstraintLayout mMessageBackground;
    private RelativeLayout mMessageLayout;

    private String mCurrentUserUID;

    public MessageViewHolder(View itemView, String currentUserUID, DatabaseReference mUsersInfo) {
        super(itemView);

        mView = itemView;
        this.mUsersInfo = mUsersInfo;
        mCurrentUserUID = currentUserUID;

        mAuthorName = mView.findViewById(R.id.user_name);
        mAuthorPhoto = mView.findViewById(R.id.user_photo);
        mMessageText = mView.findViewById(R.id.message_text);
        mMessageTime = mView.findViewById(R.id.message_time);
        mMessageBackground = mView.findViewById(R.id.message_content);
        mMessageLayout = mView.findViewById(R.id.message);
    }

    public void setData(Message message) {
        setAuthorInfo(message.getAuthor());
        setMessageData(message);
    }

    private void setAuthorInfo(String author) {
        if(author.equals(mCurrentUserUID)){

            mAuthorPhoto.setVisibility(View.INVISIBLE);
            mMessageLayout.setGravity(Gravity.END);
            mMessageBackground.setBackgroundResource(R.drawable.background_message_sent);

        }else {

            mAuthorPhoto.setVisibility(View.VISIBLE);
            mMessageLayout.setGravity(Gravity.START);
            mMessageBackground.setBackgroundResource(R.drawable.background_message_received);

        }

        mUsersInfo.child(author)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final String authorPhoto = dataSnapshot.child("image").getValue() + "";
                String authorName = dataSnapshot.child("name").getValue() + "";

                mAuthorName.setText(authorName);

                Picasso.with(mView.getContext()).load(authorPhoto)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mAuthorPhoto, new Callback() {
                            @Override
                            public void onSuccess() {}

                            @Override
                            public void onError() {
                                Picasso.with(mView.getContext()).load(authorPhoto)
                                        .into(mAuthorPhoto);
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setMessageData(Message message) {
        mMessageText.setText(message.getText());
        mMessageTime.setText(TimeStampConvertor.getTimeAgo(message.getTime()));
    }
}
