package com.example.jdachuk.face2facechatapp.view_holders;

/**
 * Created by jdachuk on 18.02.18.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jdachuk.face2facechatapp.ProfileActivity;
import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.models.User;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {

    private DatabaseReference mFriendsRef;
    private DatabaseReference mRequestsRef;

    private View mView, mActionsLayout;
    private TextView mUserName, mUserStatus;
    private CircleImageView mUserPhoto;
    private Button mAcceptBtn, mDeclineBtn;

    private final int mDataState;

    public final static int FRIEND_DATA = 1;
    public final static int REQUEST_DATA = 2;

    public UserViewHolder(View view){
        super(view);
        this.mView = view;
        this.mDataState = 0;

        mUserName = mView.findViewById(R.id.user_name);
        mUserPhoto = mView.findViewById(R.id.user_photo);
    }

    public UserViewHolder(View itemView, int DataState, DatabaseReference mRequestsRef, DatabaseReference mFriendsRef) {
        super(itemView);
        this.mView = itemView;
        this.mDataState = DataState;

        mUserName = mView.findViewById(R.id.user_name);
        mUserPhoto = mView.findViewById(R.id.user_photo);

        if(mDataState == FRIEND_DATA) {

            mUserStatus = mView.findViewById(R.id.user_status);
            this.mFriendsRef = mRequestsRef;

        } else if(mDataState == REQUEST_DATA) {

            this.mRequestsRef = mRequestsRef;
            this.mFriendsRef = mFriendsRef;

            mActionsLayout = mView.findViewById(R.id.actions_layout);
            mAcceptBtn = mView.findViewById(R.id.accept_btn);
            mDeclineBtn = mView.findViewById(R.id.decline_btn);
        }
    }

    public void SetData(User user){
        if(mDataState == FRIEND_DATA){
            SetFriendData(user);
        }else if(mDataState == REQUEST_DATA) {
            SetRequestData(user);
        }else {
            SetDefaultData(user);
        }
    }

    private void SetDefaultData(User user) {
        SetUserName(user.getName());
        SetUserPhoto(user.getThumb_image());
        SetUserProfile(user.getUserUID());
    }

    private void SetUserPhoto(final String thumb_image) {
        if(!thumb_image.equals("default")) {
            Picasso.with(mView.getContext()).load(thumb_image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mUserPhoto, new Callback() {
                        @Override
                        public void onSuccess() {}

                        @Override
                        public void onError() {
                            Picasso.with(mView.getContext()).load(thumb_image)
                                    .into(mUserPhoto);
                        }
                    });
        }
    }

    private void SetFriendData(User user) {
        SetUserName(user.getName());
        SetUserPhoto(user.getThumb_image());
        SetUserStatus(user.getStatus());
        SetUserOnline(user.getOnline());
        SetUserProfile(user.getUserUID());
        SetMenuOnLongClick(user);
    }

    private void SetUserStatus(String status) {
        mUserStatus.setText(status);
    }

    private void SetRequestData(User user) {
        SetUserName(user.getName());
        SetUserPhoto(user.getThumb_image());
        SetUserOnline(user.getOnline());
        SetUserProfile(user.getUserUID());
        SetRequestActions(user);
    }

    private void SetMenuOnLongClick(final User user) {
        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String title = "Choose Action.";
                CharSequence[] options = new CharSequence[]{
                        "Send message.", "Delete friend."
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(mView.getContext());
                builder.setTitle(title);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:

                                break;
                            case 1:
                                Map<String, Object> deleteFriend = new HashMap<>();
                                deleteFriend.put(user.getUserUID(), null);

                                mFriendsRef.updateChildren(deleteFriend);

                        }
                    }
                });
                builder.show();

                return true;
            }
        });

    }

    private void SetUserProfile(final String userUID) {
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(v.getContext(), ProfileActivity.class);
                profileIntent.putExtra("user_id", userUID);
                v.getContext().startActivity(profileIntent);
            }
        });
    }

    private void SetUserName(String name){
        mUserName.setText(name);
    }

    private void SetUserOnline(Boolean online) {
        if(online) {
            mUserPhoto.setBorderWidth(4);
        }else {
            mUserPhoto.setBorderWidth(0);
        }
    }

    private void SetRequestActions(User user) {
        mActionsLayout.setVisibility(View.VISIBLE);
        SetAcceptAction(user.getUserUID());
        SetDeclineAction(user.getUserUID());
    }

    private void SetDeclineAction(final String mUserUID) {
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> cancelRequest = new HashMap<>();
                cancelRequest.put(mUserUID, null);

                mRequestsRef.updateChildren(cancelRequest,
                        new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError != null){

                            String error = databaseError.getMessage();
                            Toast.makeText(mView.getContext(), error, Toast.LENGTH_SHORT).show();

                        }

                    }
                });
            }
        });
    }

    private void SetAcceptAction(final String mUserUID){
        mAcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> acceptRequest = new HashMap<>();
                acceptRequest.put(mUserUID + "/date", ServerValue.TIMESTAMP);

                mFriendsRef.updateChildren(acceptRequest,
                        new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        if(databaseError == null){

                            Toast.makeText(mView.getContext(),
                                    "Request Accepted.", Toast.LENGTH_SHORT).show();

                        }else {

                            String error = databaseError.getMessage();
                            Toast.makeText(mView.getContext(), error, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });
    }
}