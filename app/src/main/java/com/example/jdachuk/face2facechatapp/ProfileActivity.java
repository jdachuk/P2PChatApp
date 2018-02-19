package com.example.jdachuk.face2facechatapp;

/**
 * Created by jdachuk on 18.02.18.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private TextView mUserName, mUserStatus;
    private CircleImageView mUserPhoto;
    private Button mSendBtn, mReceiveBtn;

    private String mUserUID;
    private int mCurrentFriendshipState;

    private final static int FRIENDSHIP_STATE_NOT_FRIENDS = 0;
    private final static int FRIENDSHIP_STATE_REQUEST_SENT = 1;
    private final static int FRIENDSHIP_STATE_REQUEST_RECEIVED = 2;
    private final static int FRIENDSHIP_STATE_FRIENDS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initialize();

        checkAuth();

        LoadUserData();
        CheckRequestState();

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentFriendshipState == FRIENDSHIP_STATE_NOT_FRIENDS){
                    SendFriendRequest();
                }else if(mCurrentFriendshipState == FRIENDSHIP_STATE_REQUEST_SENT
                        || mCurrentFriendshipState == FRIENDSHIP_STATE_REQUEST_RECEIVED) {
                    DeleteFriendRequest();
                }else if(mCurrentFriendshipState == FRIENDSHIP_STATE_FRIENDS) {
                    SendMessage();
                }
            }
        });

        mReceiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentFriendshipState) {

                    case FRIENDSHIP_STATE_FRIENDS: {
                        RemoveFriend();
                        break;
                    }
                    case FRIENDSHIP_STATE_REQUEST_RECEIVED: {
                        AcceptFriendRequest();
                        break;
                    }
                }
            }
        });
    }

    private void SendMessage() {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("user_id", mUserUID);
        startActivity(chatIntent);
    }

    private void checkAuth() {
        if(mCurrentUser == null || mUserUID == null){
            finish();
        }
    }

    private void initialize() {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mUserName = findViewById(R.id.user_name);
        mUserStatus = findViewById(R.id.user_status);
        mUserPhoto = findViewById(R.id.user_photo);
        mSendBtn = findViewById(R.id.send_btn);
        mReceiveBtn = findViewById(R.id.receive_btn);

        mUserUID = getIntent().getStringExtra("user_id");
    }

    private void CheckRequestState() {
        mRootRef.child("FriendRequests").child(mCurrentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mUserUID)) {
                    String requestType = dataSnapshot.child(mUserUID)
                            .child("request_type").getValue() + "";

                    if(requestType.equals("sent")){

                        mCurrentFriendshipState = FRIENDSHIP_STATE_REQUEST_SENT;
                        mSendBtn.setVisibility(View.VISIBLE);
                        mSendBtn.setText(R.string.cancel_request_string);
                        mSendBtn.setEnabled(true);
                        mReceiveBtn.setVisibility(View.GONE);
                        mReceiveBtn.setEnabled(false);

                    } else if(requestType.equals("received")) {

                        mCurrentFriendshipState = FRIENDSHIP_STATE_REQUEST_RECEIVED;
                        mSendBtn.setVisibility(View.VISIBLE);
                        mSendBtn.setText(R.string.decline_request_string);
                        mSendBtn.setEnabled(true);
                        mReceiveBtn.setVisibility(View.VISIBLE);
                        mReceiveBtn.setText(R.string.accept_request_string);
                        mReceiveBtn.setEnabled(true);

                    }

                }else {
                    CheckFriendshipState();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void CheckFriendshipState() {
        mRootRef.child("Friends").child(mCurrentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(mUserUID)) {

                    mCurrentFriendshipState = FRIENDSHIP_STATE_FRIENDS;
                    mSendBtn.setEnabled(true);
                    mSendBtn.setText(R.string.send_message_string);
                    mSendBtn.setVisibility(View.VISIBLE);
                    mReceiveBtn.setEnabled(true);
                    mReceiveBtn.setText(R.string.delete_friend_string);
                    mReceiveBtn.setVisibility(View.VISIBLE);
                } else {

                    mCurrentFriendshipState = FRIENDSHIP_STATE_NOT_FRIENDS;
                    mSendBtn.setEnabled(true);
                    mSendBtn.setText(R.string.send_friend_request_string);
                    mSendBtn.setVisibility(View.VISIBLE);
                    mReceiveBtn.setEnabled(false);
                    mReceiveBtn.setVisibility(View.GONE);

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void LoadUserData() {
        mRootRef.child("Users").child(mUserUID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("name").getValue() + "";
                String userStatus = dataSnapshot.child("status").getValue() + "";
                String thumb_image = dataSnapshot.child("thumb_image").getValue() + "";

                mUserName.setText(userName);
                mUserStatus.setText(userStatus);
                if(!thumb_image.equals("default")) {
                    Picasso.with(getApplicationContext()).load(thumb_image).into(mUserPhoto);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void AcceptFriendRequest() {

        Map<String, Object> acceptRequest = new HashMap<>();
        acceptRequest.put("Friends/" + mCurrentUser.getUid() + "/" + mUserUID + "/date", ServerValue.TIMESTAMP);

        mRootRef.updateChildren(acceptRequest, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    Toast.makeText(ProfileActivity.this, "Request Accepted.", Toast.LENGTH_SHORT).show();

                }else {

                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void RemoveFriend() {

        Map<String, Object> removeFriend = new HashMap<>();
        removeFriend.put("Friends/" + mCurrentUser.getUid() + "/" + mUserUID, null);

        mRootRef.updateChildren(removeFriend, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null){

                    Toast.makeText(ProfileActivity.this,
                            "Congrats, you are not friends anymore",
                            Toast.LENGTH_LONG).show();

                }else{

                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    private void DeleteFriendRequest() {

        Map<String, Object> cancelRequest = new HashMap<>();
        cancelRequest.put("FriendRequests/" + mCurrentUser.getUid() + "/" + mUserUID, null);

        mRootRef.updateChildren(cancelRequest, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError != null){

                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

    private void SendFriendRequest() {

        Map<String, Object> sendRequest = new HashMap<>();
        sendRequest.put("FriendRequests/" + mCurrentUser.getUid() + "/" + mUserUID + "/request_type",
                "sent");

        mRootRef.updateChildren(sendRequest, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                if(databaseError == null) {

                    Toast.makeText(ProfileActivity.this, "Request Sent Successfully.",
                            Toast.LENGTH_SHORT).show();

                }else{

                    String error = databaseError.getMessage();
                    Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
}
