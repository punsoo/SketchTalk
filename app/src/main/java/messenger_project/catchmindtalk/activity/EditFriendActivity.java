package messenger_project.catchmindtalk.activity;


import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import messenger_project.catchmindtalk.Item.FriendListItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.adapter.EditFriendListAdapter;

public class EditFriendActivity extends AppCompatActivity {

    public MyDatabaseOpenHelper db;
    ListView friendList;
    EditFriendListAdapter friendListAdapter;
    Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friend);

        toolbar = (Toolbar) findViewById(R.id.toolbarEditFriend);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("편집");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        final ArrayList<FriendListItem> ListData = new ArrayList<>();
        ArrayList<FriendListItem> FListData = new ArrayList<>();
        ArrayList<String> favoriteList = new ArrayList<String>();


        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);
        Cursor cursor = db.getFriendList();

        while(cursor.moveToNext()) {

            FriendListItem addItem = new FriendListItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(5),cursor.getInt(6));
            ListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
                favoriteList.add(addItem.getId());
            }

            //Log.d("EditFriendActivity", cursor.getString(0)+"#####"+cursor.getString(1) + "" +cursor.getString(2));
        }


        friendList = (ListView) findViewById(R.id.editFriendList);

        friendListAdapter = (new EditFriendListAdapter(this,FListData,ListData,favoriteList));

        friendList.setAdapter(friendListAdapter);


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
