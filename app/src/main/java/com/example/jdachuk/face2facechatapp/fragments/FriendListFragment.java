package com.example.jdachuk.face2facechatapp.fragments;

/**
 * Created by jdachuk on 18.02.18.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jdachuk.face2facechatapp.MainActivity;
import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.adapters.UsersAdapter;
import com.example.jdachuk.face2facechatapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendListFragment extends Fragment {


    private DatabaseReference mUsersRef;
    private Query mFriendsQuery;

    private View mView;

    private UsersAdapter mFriendsAdapter;
    private ArrayList<User> mFriendsCollection = new ArrayList<>();

    public FriendListFragment() {
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersRef.keepSynced(true);
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mFriendsAdapter = new UsersAdapter(mFriendsCollection, UsersAdapter.FRIEND_DATA_SET);

        if(mCurrentUser == null){
            if ((getActivity()) != null) {
                ((MainActivity)getActivity()).sendToLogIn();
            }
        }else {
            mFriendsQuery = FirebaseDatabase.getInstance().getReference()
                    .child("Friends").child(mCurrentUser.getUid()).orderByChild("online");
            mFriendsQuery.keepSynced(true);

            LoadFriends();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_friend_list, container, false);

        initializeViews();

        return mView;
    }

    private void initializeViews() {
        LinearLayoutManager mFriendsLayoutManager = new LinearLayoutManager(getContext());

        RecyclerView mFriendsContainer = mView.findViewById(R.id.friends_container);
        mFriendsContainer.setHasFixedSize(true);
        mFriendsContainer.setLayoutManager(mFriendsLayoutManager);
        mFriendsContainer.setAdapter(mFriendsAdapter);
    }

    private void LoadFriends() {
        mFriendsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String userUID = dataSnapshot.getKey();

                mUsersRef.child(userUID)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                User user = new User(dataSnapshot);

                                mFriendsCollection.add(user);
                                mFriendsAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String userUID = dataSnapshot.getKey();

                User user = new User(userUID, true);

                int position = user.getIndexIn(mFriendsCollection);

                if(position >= 0) {
                    mFriendsCollection.remove(position);
                    mFriendsAdapter.notifyDataSetChanged();
                }
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
