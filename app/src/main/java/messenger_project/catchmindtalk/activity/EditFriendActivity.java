package messenger_project.catchmindtalk.activity;


import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;

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
    SearchView searchView;
    ListView friendList;
    EditFriendListAdapter friendListAdapter;
    Toolbar toolbar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friend);

        toolbar = (Toolbar) findViewById(R.id.toolbarEditFriend);
        searchView = (SearchView) findViewById(R.id.searchView);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("편집");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        ArrayList<FriendListItem> ListData = new ArrayList<>();
        final ArrayList<FriendListItem> allListData = new ArrayList<>();
        ArrayList<FriendListItem> FListData = new ArrayList<>();
        final ArrayList<FriendListItem> allFListData = new ArrayList<>();


        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);
        Cursor cursor = db.getFriendList();

        while(cursor.moveToNext()) {

            FriendListItem addItem = new FriendListItem(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getInt(4),cursor.getInt(5),cursor.getInt(6));
            ListData.add(addItem);
            allListData.add(addItem);
            if(cursor.getInt(4) == 1){
                FListData.add(addItem);
                allFListData.add(addItem);
            }

            //Log.d("EditFriendActivity", cursor.getString(0)+"#####"+cursor.getString(1) + "" +cursor.getString(2));
        }


        friendList = (ListView) findViewById(R.id.editFriendList);

        friendListAdapter = (new EditFriendListAdapter(this,allFListData,allListData,FListData,ListData));

        friendList.setAdapter(friendListAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                friendListAdapter.clearList();

                for(int i=0; i<allListData.size();i++){
                    if(allListData.get(i).getNickname().contains(s)){
                        friendListAdapter.addItem(allListData.get(i));
                    }
                }
                for(int i=0; i<allFListData.size();i++){
                    if(allFListData.get(i).getNickname().contains(s)){
                        friendListAdapter.addFItem(allFListData.get(i));
                    }
                }
                friendListAdapter.sizeReset();
                friendListAdapter.notifyDataSetChanged();
                return true;
            }
        });
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
