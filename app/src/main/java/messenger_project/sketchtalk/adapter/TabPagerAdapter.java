package messenger_project.sketchtalk.adapter;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import messenger_project.sketchtalk.fragment.ChatRoomListFragment;
import messenger_project.sketchtalk.fragment.FriendListFragment;
import messenger_project.sketchtalk.fragment.SettingFragment;


public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;
    public SharedPreferences mPref;
    public Bundle bundle;
    public FriendListFragment friendListFragment;
    public ChatRoomListFragment chatRoomListFragment;
    public SettingFragment settingFragment;


    public TabPagerAdapter(FragmentManager fm, int tabCount, SharedPreferences SP, FriendListFragment _friendListFragment, ChatRoomListFragment _chatRoomListFragment, SettingFragment _settingRoomFragment) {

        super(fm);
        this.tabCount = tabCount;
        this.mPref = SP;
        this.bundle = new Bundle();
        this.bundle.putString("userId",mPref.getString("userId","아이디없음"));
        this.bundle.putString("nickname",mPref.getString("nickname","닉없음"));
        this.bundle.putString("profileImageUpdateTime",mPref.getString("profileImageUpdateTime","수정날짜없음"));
        this.bundle.putString("profileMessage",mPref.getString("profileMessage","메세지없음"));
        friendListFragment = _friendListFragment;
        chatRoomListFragment = _chatRoomListFragment;
        settingFragment = _settingRoomFragment;
        friendListFragment.setArguments(bundle);
        chatRoomListFragment.setArguments(bundle);

    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                return friendListFragment;
            case 1:
                return chatRoomListFragment;
            case 2:
                return settingFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

}
