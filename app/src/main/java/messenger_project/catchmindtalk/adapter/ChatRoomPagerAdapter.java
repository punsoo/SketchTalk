package messenger_project.catchmindtalk.adapter;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ChatRoomPagerAdapter extends FragmentPagerAdapter {

    public Fragment mf;
    public Fragment df;
    public SharedPreferences mPref;
    public Bundle bundle;

    public ChatRoomPagerAdapter(FragmentManager fm, Fragment mf, Fragment df, SharedPreferences SP, int roomId, String friendId) {
        super(fm);
        this.mf = mf;
        this.df = df;
        mPref = SP;
        this.bundle = new Bundle();
        this.bundle.putString("userId",mPref.getString("userId","아이디없음"));
        this.bundle.putInt("roomId",roomId);
        this.bundle.putString("friendId",friendId);
        mf.setArguments(bundle);
        df.setArguments(bundle);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mf;
            case 1:
                return df;
            default:
                return mf;

        }
    }
}