package com.example.jdachuk.face2facechatapp.fragments;

/**
 * Created by jdachuk on 19.02.18.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.adapters.ChatsAdapter;
import com.example.jdachuk.face2facechatapp.models.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;

public class ChatsFragment extends Fragment {

    private Query mChatsQuery;

    private ArrayList<Chat> mChatsCollection = new ArrayList<>();
    private ChatsAdapter mAdapter;

    public ChatsFragment() {
        mAdapter = new ChatsAdapter(mChatsCollection);

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mCurrentUser != null) {
            mChatsQuery = FirebaseDatabase.getInstance().getReference().child("UserConversations")
                    .child(mCurrentUser.getUid()).orderByKey();
        }
        LoadChats();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView mChatsContainer = view.findViewById(R.id.chats_container);
        mChatsContainer.setHasFixedSize(true);
        mChatsContainer.setLayoutManager(new LinearLayoutManager(getContext()));

        mChatsContainer.setAdapter(mAdapter);
    }


    private void LoadChats() {

        mChatsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chat = new Chat(dataSnapshot);

                mChatsCollection.add(chat);
                mAdapter.notifyDataSetChanged();
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
}
