package messenger_project.catchmindtalk.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import messenger_project.catchmindtalk.Item.ChatRoomItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.adapter.SearchRoomAdapter;


public class SearchRoomActivity extends AppCompatActivity {


    ListView roomList;
    ImageView backBtn;
    ImageView searchBtn;
    EditText editText;

    SearchRoomAdapter searchRoomAdapter;

    ArrayList<ChatRoomItem> searchRoomList;
    ArrayList<ChatRoomItem> allList;

    HashMap<String,String> nicknameList;

    public MyDatabaseOpenHelper db;

    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_room);

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        String myId = mPref.getString("userId","닉없음");

        backBtn = (ImageView) findViewById(R.id.search_room_back);
        editText = (EditText) findViewById(R.id.search_room_editText);
        searchBtn = (ImageView) findViewById(R.id.search_room_search);

        roomList = (ListView) findViewById(R.id.list_room_search);

        searchRoomList = new ArrayList<>();
        allList = new ArrayList<>();

        nicknameList = new HashMap<>();

        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);

        Cursor CRC = db.getChatRoomListJoinOnMessageList(myId); //ChatRoomCursor

        while(CRC.moveToNext()) {

            int UnreadNum = db.getUnReadNum(myId,CRC.getString(0),CRC.getLong(2));
            Cursor CRMC = db.getChatRoomMemberJoinOnFriendList(CRC.getString(0));
            Vector<String[]> ChatRoomMemberList = new Vector<>();
            while(CRMC.moveToNext()){
                String[] ChatRoomMemberData = new String[3];
                ChatRoomMemberData[0] = CRMC.getString(0);
                ChatRoomMemberData[1] = CRMC.getString(1);
                ChatRoomMemberData[2] = CRMC.getString(2);
                ChatRoomMemberList.add(ChatRoomMemberData);
            }

            ChatRoomItem addItem = new ChatRoomItem(CRC.getString(0),CRC.getString(1),CRC.getLong(2),CRC.getInt(3),CRC.getString(4),CRC.getLong(5),CRC.getInt(6),ChatRoomMemberList, UnreadNum);


            searchRoomList.add(addItem);
            allList.add(addItem);


        }


        searchRoomAdapter = new SearchRoomAdapter(this,searchRoomList,myId);
        roomList.setAdapter(searchRoomAdapter);


        final TextWatcher editTextWatcher = new TextWatcher() {


            @Override
            public void afterTextChanged(Editable s) {

                searchRoomAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String editContent = editText.getText().toString();
                searchRoomAdapter.clearList();

                for(int i=0; i<allList.size();i++){


                    if (allList.get(i).getRoomId().contains(editContent)) {

                        searchRoomAdapter.addCRItem(allList.get(i));

                    }



                }


            }



        };

        editText.addTextChangedListener(editTextWatcher);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        roomList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String roomId = (String) view.getTag(R.id.roomId);
                String nickname = (String) view.getTag(R.id.nickname);

//                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
//                intent.putExtra("friendId",friendId);
//                intent.putExtra("nickname",nickname);
//                startActivity(intent);

                finish();
            }
        });



    }

}
