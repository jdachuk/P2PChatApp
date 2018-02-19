package com.example.jdachuk.face2facechatapp.services;

/**
 * Created by jdachuk on 18.02.18.
 */

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private DatabaseReference mUserDatabase;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        final String newToken = FirebaseInstanceId.getInstance().getToken();

        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(mCurrentUser != null){
            mUserDatabase = FirebaseDatabase.getInstance().getReference()
                    .child("CommonUsers").child(mCurrentUser.getUid());
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("device_token")){
                        mUserDatabase.child("device_token").setValue(newToken);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }
}
