package com.example.jdachuk.face2facechatapp;

/**
 * Created by jdachuk on 19.02.18.
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.jdachuk.face2facechatapp.adapters.MessagesAdapter;
import com.example.jdachuk.face2facechatapp.adapters.TimeStampConvertor;
import com.example.jdachuk.face2facechatapp.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    private CircleImageView mUserPhoto;
    private TextView mUserName, mUserStatus;
    private EditText mMessageText;
    private ImageView mSendBtn;
    private RecyclerView mMessageContainer;

    private ArrayList<Message> mMessageCollection = new ArrayList<>();
    private MessagesAdapter messagesAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    private String mUserID, mLastKey;
    private Boolean mShouldLoadMore = true;
    private int mItemPos = 0;

    private final static int TOTAL_ITEMS_TO_LOAD = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initialize();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRootRef.child("Users").child(mUserID)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue() + "";
                String userImage = dataSnapshot.child("image").getValue() + "";
                String userInfo;
                if(dataSnapshot.hasChild("last_seen")){
                    userInfo = TimeStampConvertor.getTimeAgo(
                            (long)dataSnapshot.child("last_seen").getValue()
                    );
                }else {
                    userInfo = "Online";
                }

                fillChatInfo(name, userImage, userInfo);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        LoadMessages();

        mMessageContainer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy < 0){
                    if(!recyclerView.canScrollVertically(dy)){
                        if(mShouldLoadMore){
                            mItemPos = 0;
                            LoadMoreMessages();
                        }
                    }
                }
            }
        });

        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void LoadMoreMessages() {

        mShouldLoadMore = false;

        Query messageQuery = mRootRef.child("Messages").child(mCurrentUser.getUid())
                .child(mUserID).orderByKey();

        messageQuery.endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Message message = new Message(dataSnapshot);

                        if(!mLastKey.equals(dataSnapshot.getKey())){
                            mMessageCollection.add(mItemPos++, message);
                            messagesAdapter.notifyDataSetChanged();
                            mLinearLayoutManager.scrollToPositionWithOffset(mItemPos,0);
                            mShouldLoadMore = true;
                        }

                        mLastKey = dataSnapshot.getKey();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void LoadMessages() {

        Query messageQuery = mRootRef.child("Messages").child(mCurrentUser.getUid())
                .child(mUserID).limitToLast(TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message message = new Message(dataSnapshot);

                mLastKey = dataSnapshot.getKey();

                mMessageCollection.add(message);
                messagesAdapter.notifyDataSetChanged();
                mMessageContainer.scrollToPosition(mMessageCollection.size() - 1);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        String newMessageText = mMessageText.getText().toString();
        Long newMessageTime = System.currentTimeMillis();
        String newMessageId = newMessageTime + mCurrentUser.getUid();

        if(!TextUtils.isEmpty(newMessageText)){

            Message message = new Message(mCurrentUser.getUid(), newMessageText, newMessageTime, false);

                String currentUserRef = "Messages/" + mCurrentUser.getUid() + "/" + mUserID;
                String chatUserRef = "Messages/" + mUserID + "/" + mCurrentUser.getUid();
                String currentUserChatRef = "UserConversations/" + mCurrentUser.getUid() + "/" + mUserID;
                String chatUserChatRef = "UserConversations/" + mUserID + "/" + mCurrentUser.getUid();

                Map<String, Object> userMessageMap = new HashMap<>();
                userMessageMap.put(currentUserRef + "/" + newMessageId, message);
                userMessageMap.put(chatUserRef + "/" + newMessageId, message);
                userMessageMap.put(currentUserChatRef + "/last_message", message);
                userMessageMap.put(chatUserChatRef + "/last_message", message);

                mRootRef.updateChildren(userMessageMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Log.d("CHAT_LOG", databaseError.getMessage());
                        }
                    }
                });

            }

        mMessageText.setText(null);
    }

    private void fillChatInfo(String name, final String image, String info) {

        mUserName.setText(name);
        mUserStatus.setText(info);
        if(!image.equals("default")) {
            Picasso.with(ChatActivity.this).load(image)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(mUserPhoto, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ChatActivity.this).load(image)
                                    .into(mUserPhoto);
                        }
                    });
        }
    }

    private void initialize() {
        Toolbar mToolbar = findViewById(R.id.chat_toolbar);

        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mMessageText = findViewById(R.id.message_text);
        mMessageContainer = findViewById(R.id.messages_container);
        mSendBtn = findViewById(R.id.send_btn);
        mUserName = findViewById(R.id.user_name);
        mUserStatus = findViewById(R.id.user_status);
        mUserPhoto = findViewById(R.id.user_photo);

        mUserID = getIntent().getStringExtra("user_id");

        messagesAdapter = new MessagesAdapter(mMessageCollection);
        mLinearLayoutManager = new LinearLayoutManager(this);

        mMessageContainer.setHasFixedSize(true);
        mMessageContainer.setLayoutManager(mLinearLayoutManager);
        mMessageContainer.setAdapter(messagesAdapter);
    }
}