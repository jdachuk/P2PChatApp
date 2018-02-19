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

import com.example.jdachuk.face2facechatapp.adapters.UsersAdapter;
import com.example.jdachuk.face2facechatapp.MainActivity;
import com.example.jdachuk.face2facechatapp.R;
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

public class RequestsListFragment extends Fragment {

    private DatabaseReference mUsersRef;
    private Query mRequestsQuery;

    private View mView;

    private UsersAdapter mRequestsAdapter;
    private ArrayList<User> mRequestsCollection = new ArrayList<>();


    public RequestsListFragment() {
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersRef.keepSynced(true);
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mRequestsAdapter = new UsersAdapter(mRequestsCollection, UsersAdapter.FRIEND_REQUEST_DATA_SET);

        if(mCurrentUser == null){
            if ((getActivity()) != null) {
                ((MainActivity)getActivity()).sendToLogIn();
            }
        }else {

            mRequestsQuery = FirebaseDatabase.getInstance().getReference()
                    .child("FriendRequests").child(mCurrentUser.getUid()).orderByKey();
            mRequestsQuery.keepSynced(true);

            LoadRequests();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_requests_list, container, false);

        initializeViews();

        return mView;
    }

    private void initializeViews() {
        LinearLayoutManager mFriendsLayoutManager = new LinearLayoutManager(getContext());

        RecyclerView mRequestsContainer = mView.findViewById(R.id.requests_container);
        mRequestsContainer.setHasFixedSize(true);
        mRequestsContainer.setLayoutManager(mFriendsLayoutManager);
        mRequestsContainer.setAdapter(mRequestsAdapter);
    }

    private void LoadRequests(){
        mRequestsQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userUID = dataSnapshot.getKey();
                String requestType = dataSnapshot.child("request_type").getValue() + "";

                if(requestType.equals("received")) {
                    mUsersRef.child(userUID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = new User(dataSnapshot);

                                    mRequestsCollection.add(user);
                                    mRequestsAdapter.notifyDataSetChanged();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String userUID = dataSnapshot.getKey();

                User user = new User(userUID, false);

                int position = user.getIndexIn(mRequestsCollection);

                if(position >= 0) {
                    mRequestsCollection.remove(position);
                    mRequestsAdapter.notifyDataSetChanged();
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
