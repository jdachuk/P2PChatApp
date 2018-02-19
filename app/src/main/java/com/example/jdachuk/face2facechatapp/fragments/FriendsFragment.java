package com.example.jdachuk.face2facechatapp.fragments;

/**
 * Created by jdachuk on 18.02.18.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.jdachuk.face2facechatapp.R;
import com.example.jdachuk.face2facechatapp.SearchUserActivity;

public class FriendsFragment extends Fragment
    implements Toolbar.OnMenuItemClickListener,
    BottomNavigationView.OnNavigationItemSelectedListener{

    BottomNavigationView mTabsMenu;

    private FriendListFragment mFriendListFragment;
    private RequestsListFragment mRequestsListFragment;

    private int mSelectedTabId = R.id.friends_tab;

    public FriendsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Toolbar toolbar = view.findViewById(R.id.friends_toolbar);
        toolbar.inflateMenu(R.menu.friends_toolbar_menu);
        toolbar.setOnMenuItemClickListener(this);

        mTabsMenu = view.findViewById(R.id.friends_tabs);
        mTabsMenu.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        if(item.getItemId() == R.id.add_friend) {
            Intent searchFriendIntent = new Intent(getActivity(), SearchUserActivity.class);
            startActivity(searchFriendIntent);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        mTabsMenu.setSelectedItemId(mSelectedTabId);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        switch (id){
            case R.id.friends_tab:
                if(mFriendListFragment == null) {
                    mFriendListFragment = new FriendListFragment();
                }
                fragmentTransaction.replace(R.id.friend_lists_container, mFriendListFragment);
                break;
            case R.id.requests_tab:
                if(mRequestsListFragment == null) {
                    mRequestsListFragment = new RequestsListFragment();
                }
                fragmentTransaction.replace(R.id.friend_lists_container, mRequestsListFragment);
                break;
        }
        fragmentTransaction.commit();

        mSelectedTabId = id;

        return true;
    }
}
