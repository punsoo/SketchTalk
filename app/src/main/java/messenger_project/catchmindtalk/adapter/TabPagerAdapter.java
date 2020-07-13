package messenger_project.catchmindtalk.adapter;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import messenger_project.catchmindtalk.fragment.ChatRoomListFragment;
import messenger_project.catchmindtalk.fragment.FriendListFragment;
import messenger_project.catchmindtalk.fragment.SettingFragment;


public class TabPagerAdapter extends FragmentStatePagerAdapter {

    // Count number of tabs
    private int tabCount;
    public SharedPreferences mPref;
    public Bundle bundle;
    public FriendListFragment tf1;
    public ChatRoomListFragment tf2;
    public SettingFragment tf3;


    public TabPagerAdapter(FragmentManager fm, int tabCount, SharedPreferences SP, FriendListFragment f1, ChatRoomListFragment f2, SettingFragment f3) {
        super(fm);
        this.tabCount = tabCount;
        this.mPref = SP;
        this.bundle = new Bundle();
        this.bundle.putString("userId",mPref.getString("userId","아이디없음"));
        this.bundle.putString("nickname",mPref.getString("nickname","닉없음"));
        this.bundle.putString("profile",mPref.getString("profile","수정날짜없음"));
        this.bundle.putString("message",mPref.getString("message","메세지없음"));
        tf1 = f1;
        tf2 = f2;
        tf3 = f3;
        tf1.setArguments(bundle);
        tf2.setArguments(bundle);
    }

    @Override
    public Fragment getItem(int position) {

        // Returning the current tabs
        switch (position) {
            case 0:
                return tf1;
            case 1:
                return tf2;
            case 2:
                return tf3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

}
