package com.example.jdachuk.face2facechatapp;

/**
 * Created by jdachuk on 17.02.18.
 */

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.example.jdachuk.face2facechatapp.fragments.ChatsFragment;
import com.example.jdachuk.face2facechatapp.fragments.FriendsFragment;
import com.example.jdachuk.face2facechatapp.fragments.SettingsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
    implements BottomNavigationView.OnNavigationItemSelectedListener{

    private FirebaseUser mCurrentUser;
    private DatabaseReference mUsersDatabase;

    private BottomNavigationView mMainMenu;

    private FriendsFragment mFriendsFragment;
    private ChatsFragment mChatsFragment;
    private SettingsFragment mSettingsFragment;

    private int mSelectedItemId = R.id.chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialize();

        checkAuth();

        mMainMenu.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainMenu.setSelectedItemId(mSelectedItemId);
    }

    public void checkAuth() {
        if(mCurrentUser == null){
            sendToLogIn();
        } else {
            Map<String, Object> onlineMap = new HashMap<>();
            onlineMap.put("online", true);
            onlineMap.put("last_seen", null);
            mUsersDatabase.child(mCurrentUser.getUid()).updateChildren(onlineMap);
        }
    }

    public void sendToLogIn() {
        Intent logInIntent = new Intent(this, LogInActivity.class);
        startActivity(logInIntent);
        finish();
    }

    private void initialize() {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mMainMenu = findViewById(R.id.main_bottom_menu);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        switch (id) {
            case R.id.chats:
                if(mChatsFragment == null) {
                    mChatsFragment = new ChatsFragment();
                }
                fragmentTransaction.replace(R.id.main_container, mChatsFragment);
                break;
            case R.id.friends:
                if(mFriendsFragment == null) {
                    mFriendsFragment = new FriendsFragment();
                }
                fragmentTransaction.replace(R.id.main_container, mFriendsFragment);
                break;
            case R.id.settings:
                if(mSettingsFragment == null) {
                    mSettingsFragment = new SettingsFragment();
                }
                fragmentTransaction.replace(R.id.main_container, mSettingsFragment);
                break;
        }
        fragmentTransaction.commit();

        mSelectedItemId = id;

        return true;
    }
}
