package messenger_project.sketchtalk.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;


import java.util.ArrayList;

import messenger_project.sketchtalk.Item.FriendListItem;
import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;
import messenger_project.sketchtalk.activity.MainActivity;
import messenger_project.sketchtalk.activity.ProfileActivity;
import messenger_project.sketchtalk.adapter.FriendListAdapter;
import messenger_project.sketchtalk.main.SendToActivity;

import static android.app.Activity.RESULT_OK;

public class FriendListFragment extends Fragment {

    public Cursor cursor;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    public FriendListAdapter myListAdapter;
    public static final int sendVideoCall = 235711;
    ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_friend_list, container, false);

        ArrayList<FriendListItem> ListData = new ArrayList<>();
        ArrayList<FriendListItem> FListData = new ArrayList<>();


        mPref = getActivity().getSharedPreferences("login",getActivity().MODE_PRIVATE);

        String myId = mPref.getString("userId","아이디없음");
        String myNickname = mPref.getString("nickname","닉네임없음");
        String myProfileImageUpdateTime = mPref.getString("profileImageUpdateTime","none");
        String myProfileMessage = mPref.getString("profileMessage","");

        FriendListItem myItem = new FriendListItem(myId,myNickname,myProfileImageUpdateTime,myProfileMessage,0,0,0);

        db = new MyDatabaseOpenHelper(getContext(),"catchMindTalk",null,1);
        Cursor cursor = db.getFriendList();

        while(cursor.moveToNext()) {

            FriendListItem addItem = new FriendListItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3), cursor.getInt(4),cursor.getInt(5),cursor.getInt(6));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }
        }


        lv = (ListView) rootView.findViewById(R.id.friendListView);

        myListAdapter = (new FriendListAdapter(getActivity().getApplicationContext(),myItem,FListData,ListData));

        lv.setAdapter(myListAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String userId =(String) view.getTag(R.id.userId);
                String nickname =(String) view.getTag(R.id.nickname);
                String profileImageUpdateTime =(String) view.getTag(R.id.profileImageUpdateTime);
                String profileMessage = (String) view.getTag(R.id.profileMessage);

                if(userId.equals("")){
                    return;
                }

                Intent intent = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
                intent.putExtra("position",position);
                intent.putExtra("userId",userId);
                intent.putExtra("nickname",nickname);
                intent.putExtra("profileImageUpdateTime",profileImageUpdateTime);
                intent.putExtra("profileMessage",profileMessage);
                startActivityForResult(intent,1234);

            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        String myId = mPref.getString("userId","아이디없음");
        String myNickname = mPref.getString("nickname","닉네임없음");
        String myProfileImageUpdateTime = mPref.getString("profileImageUpdateTime","none");
        String myProfileMessage = mPref.getString("profileMessage","");


        FriendListItem myItem= new FriendListItem(myId,myNickname,myProfileMessage,myProfileImageUpdateTime,0,0,0);

        myListAdapter.changeMyItem(myItem);

        ArrayList<FriendListItem> ListData = new ArrayList<FriendListItem>();
        ArrayList<FriendListItem> FListData = new ArrayList<FriendListItem>();

        db = new MyDatabaseOpenHelper(getContext(),"catchMindTalk",null,1);
        Cursor cursor = db.getFriendList();

        while(cursor.moveToNext()) {

            FriendListItem addItem = new FriendListItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(5),cursor.getInt(6));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }

        }

        myListAdapter.ChangeList(FListData,ListData);
        myListAdapter.sizeReset();
        myListAdapter.notifyDataSetChanged();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);


        if(resultCode == RESULT_OK) {
            String friendId = data.getExtras().getString("friendId");
            String nickname = data.getExtras().getString("nickname");
            STA.sendToActivity(friendId,nickname, 0);
        }

//        if(resultCode == sendVideoCall){
//            String friendId = data.getExtras().getString("friendId");
//            String roomId = data.getExtras().getString("roomId");
//            Toast.makeText(getContext(),friendId,Toast.LENGTH_SHORT).show();
//
//
//        }
    }




    SendToActivity STA;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity MA = (MainActivity) context;
        try {
            STA = (SendToActivity) MA;
        } catch (ClassCastException e) {
            throw new ClassCastException(MA.toString() + " must implement onSomeEventListener");
        }
    }


}
