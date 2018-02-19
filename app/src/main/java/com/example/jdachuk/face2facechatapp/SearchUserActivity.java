package com.example.jdachuk.face2facechatapp;

/**
 * Created by jdachuk on 18.02.18.
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

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

import java.util.ArrayList;

public class SearchUserActivity extends AppCompatActivity {

    private DatabaseReference mUsersRef;
    private FirebaseUser mCurrentUser;

    private RecyclerView mUsersContainer;
    private EditText mSearch;

    private UsersAdapter mAdapter;
    private ArrayList<User> mUsersCollection = new ArrayList<>();
    private String mLastKey, mLastName;
    private Boolean mShouldLoadMore = true;

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if(dy > 0) {
                if(!mUsersContainer.canScrollVertically(dy)) {
                    if(mShouldLoadMore)
                        LoadMoreData();
                }
            }
        }
    };

    private final static int TOTAL_ITEMS_TO_LOAD = 20;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        initialize();

        checkAuth();

        mUsersContainer.addOnScrollListener(mOnScrollListener);

        LoadData();

        mSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mUsersCollection.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!TextUtils.isEmpty(s)){
                    mUsersContainer.clearOnScrollListeners();
                    SearchData(s);
                }else {
                    mUsersContainer.addOnScrollListener(mOnScrollListener);
                    LoadData();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void checkAuth() {
        if(mCurrentUser == null) {
            finish();
        }
    }

    private void SearchData(CharSequence searchReq) {
        Query searchQuery = mUsersRef.orderByChild("name").startAt(searchReq.toString())
                .endAt(searchReq + "\uf8ff");

        searchQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                mLastKey = dataSnapshot.getKey();
                mLastName = dataSnapshot.child("name").getValue() + "";

                if (!dataSnapshot.getKey().equals(mCurrentUser.getUid())) {

                    User user = new User(dataSnapshot);
                    mUsersCollection.add(user);
                    mAdapter.notifyDataSetChanged();

                }
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

    private void LoadData() {
        mShouldLoadMore = true;

        Query usersQuery = mUsersRef.orderByChild("name").limitToFirst(TOTAL_ITEMS_TO_LOAD);

        usersQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                mLastKey = dataSnapshot.getKey();
                mLastName = dataSnapshot.child("name").getValue() + "";

                if (!dataSnapshot.getKey().equals(mCurrentUser.getUid())) {

                    User user = new User(dataSnapshot);
                    mUsersCollection.add(user);
                    mAdapter.notifyDataSetChanged();

                }
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

    private void LoadMoreData() {
        mShouldLoadMore = false;

        Query usersQuery = mUsersRef.orderByChild("name").startAt(mLastName, mLastKey)
                .limitToFirst(TOTAL_ITEMS_TO_LOAD);

        usersQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String currentKey = dataSnapshot.getKey();
                String currentName = dataSnapshot.child("name").getValue() + "";

                if(!mLastKey.equals(currentKey)) {
                    if(!currentKey.equals(mCurrentUser.getUid())) {
                        User user = new User(dataSnapshot);

                        mUsersCollection.add(user);
                        mAdapter.notifyDataSetChanged();

                    }

                    mShouldLoadMore = true;

                }

                mLastKey = currentKey;
                mLastName = currentName;

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

    private void initialize() {
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersRef.keepSynced(true);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.search_user_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new UsersAdapter(mUsersCollection, UsersAdapter.DEFAULT_DATA_SET);

        mUsersContainer = findViewById(R.id.users_container);
        mUsersContainer.setHasFixedSize(true);
        mUsersContainer.setLayoutManager(mLayoutManager);
        mUsersContainer.setAdapter(mAdapter);

        mSearch = findViewById(R.id.search);

    }
}
