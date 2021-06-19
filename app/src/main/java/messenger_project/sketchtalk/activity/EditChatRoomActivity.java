package messenger_project.sketchtalk.activity;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import messenger_project.sketchtalk.Item.ChatRoomItem;
import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;
import messenger_project.sketchtalk.adapter.EditChatRoomAdapter;

public class EditChatRoomActivity extends AppCompatActivity {

    public ListView editRoomList;
    public EditChatRoomAdapter editChatRoomAdapter;
    public ArrayList<ChatRoomItem> chatRoomListData;
    public HashMap<String,Boolean> IsChecked;
    public ImageView allCheck;

    MyDatabaseOpenHelper db;

    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    public String myId;
    String roomSet;

    public Toolbar toolbar;

    public boolean allChecked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chatroom);

        toolbar = (Toolbar) findViewById(R.id.toolbarEditRoom);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("편집");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        editRoomList = (ListView) findViewById(R.id.editRoomList);
        allCheck = (ImageView) findViewById(R.id.all_check_IV);
        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);
        chatRoomListData = new ArrayList<>();
        IsChecked = new HashMap<>();

        allChecked = false;

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        myId = mPref.getString("userId","닉없음");

        Cursor CRC = db.getChatRoomListJoinOnMessageList(myId); //ChatRoomCursor

        while(CRC.moveToNext()) {

            int UnreadNum = db.getChatRoomUnReadNum(myId,CRC.getInt(0),CRC.getString(1),CRC.getLong(2));
            Vector<String[]> ChatRoomMemberList  = db.getChatRoomMemberList(CRC.getInt(0),CRC.getString(1));
            ChatRoomItem addItem = new ChatRoomItem(CRC.getInt(0),CRC.getString(1),CRC.getLong(2),CRC.getString(3),CRC.getInt(4),CRC.getString(10),CRC.getLong(11),CRC.getInt(12),ChatRoomMemberList, UnreadNum);
            chatRoomListData.add(addItem);
            if(CRC.getInt(0) == 0){
                IsChecked.put(CRC.getString(1),false);
            }else{
                IsChecked.put(CRC.getInt(0)+"",false);
            }

        }


        editChatRoomAdapter = new EditChatRoomAdapter(this,chatRoomListData,myId,IsChecked);

        editRoomList.setAdapter(editChatRoomAdapter);

        editRoomList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String userId = (String)view.getTag(R.id.friendId);
                int roomId = (int)view.getTag(R.id.roomId);
                if(roomId==0) {
                    editChatRoomAdapter.changeIsChecked(userId);
                }else{
                    editChatRoomAdapter.changeIsChecked(roomId+"");
                }

                editChatRoomAdapter.notifyDataSetChanged();

            }
        });

        allCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allChecked){
                    allCheck.setImageResource(R.drawable.check_icon_inact);
                    allChecked = false;
                    editChatRoomAdapter.changeAll(false);
                }else{
                    allCheck.setImageResource(R.drawable.check_icon);
                    allChecked = true;
                    editChatRoomAdapter.changeAll(true);
                }
                editChatRoomAdapter.notifyDataSetChanged();
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }else if(id == R.id.exit_room_btn){
            if(editChatRoomAdapter.isAllFalse()){
                return false;
            }
            DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which){
                    Intent exitIntent = new Intent();
                    roomSet = editChatRoomAdapter.exitCheckedRoom();
                    exitIntent.putExtra("roomSet",roomSet);
                    setResult(RESULT_OK,exitIntent);
                    finish();
                }

            };


            DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

                @Override public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }

            };




            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setMessage("채팅방에서 나가기를 하면 대화 내용 및 채팅목록에서 모두 삭제됩니다.\n선택한 채팅방에서 나가시겠습니까?")
                    .setPositiveButton("확인", exitListener)
                    .setNegativeButton("취소", cancelListener)
                    .create();

            dialog.show();

            Button exitBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            exitBtn.setTextColor(Color.BLACK);

            Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            cancelBtn.setTextColor(Color.BLACK);

        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.edit_room_menu, menu);
        return true;

    }


}
