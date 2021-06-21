package messenger_project.sketchtalk.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import messenger_project.sketchtalk.Item.FriendListItem;
import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;
import messenger_project.sketchtalk.adapter.InviteFriendListAdapter;

public class MakeGroupActivity extends AppCompatActivity {
    public MyDatabaseOpenHelper db;
    ListView friendList;
    InviteFriendListAdapter friendListAdapter;
    Toolbar toolbar;
    TextView groupNumTV;
    int groupNum;
    ArrayList<String> inviteList = new ArrayList<>();
    ArrayList<String> alreadyList = new ArrayList<>();
    ArrayList<String> inviteNicknameList = new ArrayList<>();
    String myId;
    String myNickname;
    SharedPreferences mPref;
    SharedPreferences.Editor editor;
    boolean FCR;
    int roomId;
    String friendId;
    String nickname;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_group);

        toolbar = (Toolbar) findViewById(R.id.toolbarInviteFriend);
        groupNumTV = (TextView) toolbar.findViewById(R.id.groupNum);
        groupNum = 0;
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("대화상대초대");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        myId = mPref.getString("userId","나의아이디");
        myNickname = mPref.getString("nickname", "나의닉네임");
        editor = mPref.edit();

        final ArrayList<FriendListItem> ListData = new ArrayList<>();
        ArrayList<FriendListItem> FListData = new ArrayList<>();

        HashMap<String,Boolean> isChecked = new HashMap<>();

        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);
        Cursor cursor = db.getFriendList();

        while(cursor.moveToNext()) {

            FriendListItem addItem = new FriendListItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3), cursor.getInt(4),cursor.getInt(5),cursor.getInt(6));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
            }
            isChecked.put(cursor.getString(0),false);

        }


        Intent GI = getIntent();
        FCR = GI.getBooleanExtra("FCR",false);
        roomId = GI.getIntExtra("roomId", 0);
        friendId = GI.getStringExtra("friendId");
        nickname = GI.getStringExtra("nickname");
        if(FCR){
            if(roomId == 0){
                alreadyList.add(friendId);
            }else {
                try {
                    JSONArray jarray = new JSONArray(friendId);
                    for (int i = 0; i < jarray.length(); i++) {
                        alreadyList.add(jarray.get(i).toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }


        friendList = (ListView) findViewById(R.id.inviteFriendList);

        friendListAdapter = new InviteFriendListAdapter(this,FListData,ListData,isChecked , alreadyList);

        friendList.setAdapter(friendListAdapter);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String userId = (String)view.getTag(R.id.userId);
                String nickname = (String)view.getTag(R.id.nickname);

                if(friendListAdapter.isChecked.get(userId)) {
                    groupNum = groupNum -1;
                    groupNumTV.setText(groupNum+"");
                    inviteList.remove(userId);
                    inviteNicknameList.remove(nickname);
                }else{
                    groupNum = groupNum +1;
                    groupNumTV.setText(groupNum+"");
                    inviteList.add(userId);
                    inviteNicknameList.add(nickname);
                }

                friendListAdapter.changeIsChecked(userId);
                friendListAdapter.notifyDataSetChanged();

            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }else if(id == R.id.invite_check_button){

            if(inviteList.size() == 0) {
                return false;
            }

            if( (!FCR) && inviteList.size() == 1) {
                Intent intent = new Intent();
                intent.putExtra("roomId",0);
                intent.putExtra("inviteId",inviteList.get(0));
                intent.putExtra("inviteNum", inviteList.size());
                intent.putExtra("nickname",inviteNicknameList.get(0));

                setResult(RESULT_OK, intent);
                finish();
                return true;
            }

            if(roomId == 0) {
                inviteList.add(friendId);
                inviteNicknameList.add(nickname);
            }

            JSONArray jsonArray = new JSONArray();
            String nickname ="";

            for (int i=0; i < inviteList.size(); i++) {
                jsonArray.put(inviteList.get(i));
            }

            Collections.sort(inviteNicknameList);
            for (int i=0; i < inviteNicknameList.size();i++){
                if(i != 0){
                    nickname = nickname + ", ";
                }
                nickname = nickname + inviteNicknameList.get(i);
            }

            Intent intent = new Intent();
            intent.putExtra("roomId",db.getMinRoomId());
            intent.putExtra("inviteId",jsonArray.toString());
            intent.putExtra("nickname",nickname);
            intent.putExtra("inviteNum", inviteList.size());

            Log.d("뭘까MakeGroupActivity",db.getMinRoomId()+"#"+jsonArray.toString());
            setResult(RESULT_OK, intent);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.make_group_menu, menu);
        return true;
    }

}
