package com.example.jdachuk.face2facechatapp.adapters;

/**
 * Created by jdachuk on 18.02.18.
 */

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.models.User;
import com.example.jdachuk.face2facechatapp.view_holders.UserViewHolder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UserViewHolder> {

    private DatabaseReference mFriendsRef;
    private DatabaseReference mRequestsRef;

    private ArrayList<User> mUsersCollection;
    private final int mDataSetType;

    public final static int DEFAULT_DATA_SET = 0;
    public final static int FRIEND_DATA_SET = 1;
    public final static int FRIEND_REQUEST_DATA_SET = 2;

    public UsersAdapter(ArrayList<User> mUsersCollection, int dataSetType) {
        this.mUsersCollection = mUsersCollection;
        this.mDataSetType = dataSetType;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        switch (dataSetType){
            case DEFAULT_DATA_SET:
                break;
            case FRIEND_DATA_SET:
                if(currentUser != null) {
                    mFriendsRef = FirebaseDatabase.getInstance().getReference()
                            .child("Friends").child(currentUser.getUid());
                    mFriendsRef.keepSynced(true);
                }
                break;
            case FRIEND_REQUEST_DATA_SET:
                if(currentUser != null) {
                    mFriendsRef = FirebaseDatabase.getInstance().getReference()
                            .child("Friends").child(currentUser.getUid());
                    mFriendsRef.keepSynced(true);

                    mRequestsRef = FirebaseDatabase.getInstance().getReference()
                            .child("FriendRequests").child(currentUser.getUid());
                    mRequestsRef.keepSynced(true);
                }
                break;
        }


    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mDataSetType == DEFAULT_DATA_SET) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_user_layout, parent, false);

            return new UserViewHolder(view);
        } else if(mDataSetType == FRIEND_DATA_SET) {
            //TODO: create new layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_user_layout, parent, false);

            return new UserViewHolder(view, UserViewHolder.FRIEND_DATA,null , mFriendsRef);
        } else if(mDataSetType == FRIEND_REQUEST_DATA_SET) {
            //TODO: create new layout
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.single_user_layout, parent, false);

            return new UserViewHolder(view, UserViewHolder.REQUEST_DATA, mRequestsRef, mFriendsRef);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {

        User user = mUsersCollection.get(position);
        holder.SetData(user);
    }

    @Override
    public int getItemCount() {
        return mUsersCollection.size();
    }
}
