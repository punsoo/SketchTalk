package messenger_project.catchmindtalk.activity;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import messenger_project.catchmindtalk.chatservice.CallbackMain;
import messenger_project.catchmindtalk.chatservice.ChatService;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.adapter.TabPagerAdapter;
import messenger_project.catchmindtalk.fragment.ChatRoomListFragment;
import messenger_project.catchmindtalk.fragment.FriendListFragment;
import messenger_project.catchmindtalk.fragment.SettingFragment;

public class MainActivity extends AppCompatActivity implements FriendListFragment.sendToActivity{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ChatService mService;

    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;

    public String myUserId;
    public String myNickname;

    public FragmentCommunicator fragmentCommunicator;
    public int tabPosition; // 선택한 탭 위치
    public Handler handler;

    public int RoomId;
    public String FriendId;

    public NetworkChangeReceiver mNCR;

    public static final int MakeGroupActivity = 5409;
    public static final int EditChatRoom = 5828;
    public static final int changeRoomList = 1458;

    BroadcastReceiver NetworkChangeUpdater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        toolbar = (Toolbar)findViewById(R.id.toolbar_MainActivity);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
        getSupportActionBar().setTitle("스케치톡");

        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();
        myUserId = mPref.getString("userId","닉네임없음");
        myNickname = mPref.getString("nickname","메세지없음");

        db = new MyDatabaseOpenHelper(this, "catchMindTalk", null, 1);


        tabPosition = 0;

        // Initializing the TabLayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout_MainActivity);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.profile_icon_act));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.chat_icon_inact));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.setting_icon_inact));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Initializing ViewPager
        viewPager = (ViewPager) findViewById(R.id.pager_MainActivity);

        FriendListFragment FriendListFragment = new FriendListFragment();
        ChatRoomListFragment ChatRoomFragment = new ChatRoomListFragment();
        SettingFragment SettingFragment = new SettingFragment();

        fragmentCommunicator = (FragmentCommunicator) ChatRoomFragment;

        // Creating TabPagerAdapter adapter
        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount(),mPref,FriendListFragment,ChatRoomFragment,SettingFragment);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        // Set TabSelectedListener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                tabPosition = position;
                invalidateOptionsMenu();
                viewPager.setCurrentItem(position);

                if(position==0){
                    tab.setIcon(R.drawable.profile_icon_act);
                }else if(position==1){
                    tab.setIcon(R.drawable.chat_icon_act);
                }else if(position==2){
                    tab.setIcon(R.drawable.setting_icon_act);
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if(position==0){
                    tab.setIcon(R.drawable.profile_icon_inact);
                }else if(position==1){
                    tab.setIcon(R.drawable.chat_icon_inact);
                }else if(position==2){
                    tab.setIcon(R.drawable.setting_icon_inact);
                }

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });

        handler = new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg){

                if(msg.what == changeRoomList){
                    fragmentCommunicator.changeRoomList();
                }


            }
        };

        Intent serviceIntent = new Intent(getApplicationContext(), ChatService.class);
        serviceIntent.putExtra("FromLogin",false);
        getApplicationContext().bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);

        String test = null;
        if(test == null) {
            Log.d("치킨","지코바");
        }else{
            Log.d("치킨","오태식");
        }

    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
//        intent.putExtra("friendId","poon");
//        intent.putExtra("nickname","데빌클로");
//        intent.putExtra("roomId",0);
//        startActivity(intent);
//    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d("확인Main","혹시");
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback_Main(mCallback); //콜백 등록
            mService.mBoundState.boundCheckMain = true;
        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    public void UpdateNetwork(String type){
        if(type.equals("wifi")) {
            Intent serviceIntent = new Intent(this, ChatService.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

        }else{
            Intent serviceIntent = new Intent(this, ChatService.class);
            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
        }
    }


    private CallbackMain mCallback = new CallbackMain() {
        public void changeRoomList(){

            Message message= Message.obtain();
            message.what = changeRoomList;
            handler.sendMessage(message);
        }

    };


    @Override
    protected void onResume() {
        super.onResume();
    }

    public interface FragmentCommunicator {

        void notifyRecvData();
        void changeRoomList();
        void startChatRoomActivity(int roomId, String friendId, String nickname);

    }



    @Override
    public void sendToActivity(String friendId,String nickname) {
        viewPager.setCurrentItem(1);
//        fragmentCommunicator.startChatRoomActivity(0,friendId,nickname);
        Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
        intent.putExtra("friendId",friendId);
        intent.putExtra("nickname",nickname);
        intent.putExtra("roomId",0);
        startActivity(intent);

    }


    public void sendToActivityExit(int roomId, String friendId) {

        RoomId = roomId;
        FriendId = friendId;
        Log.d("확인Exit",roomId +"#"+FriendId);
        DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                try {
                    ExitThread et = new ExitThread(RoomId,FriendId);
                    et.start();
                    et.join();
                    fragmentCommunicator.changeRoomList();

                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

            @Override public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }

        };


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("채팅방에서 나가기를 하면 대화 내용 및 채팅목록에서 모두 삭제됩니다.\n채팅방에서 나가시겠습니까?")
                .setPositiveButton("확인", exitListener)
                .setNegativeButton("취소", cancelListener)
                .create();

        dialog.show();

        Button exitBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        exitBtn.setTextColor(Color.BLACK);

        Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        cancelBtn.setTextColor(Color.BLACK);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(tabPosition == 0) {
            getMenuInflater().inflate(R.menu.friend_menu, menu);
        }else if(tabPosition ==1){
            getMenuInflater().inflate(R.menu.chatroom_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //ActionBar 메뉴 클릭에 대한 이벤트 처리
        String txt = null;
        int id = item.getItemId();
        switch (id){
            case R.id.action_align:
                break;
            case R.id.action_search_friend:

                Intent intentSF = new Intent(this,SearchFriendActivity.class);
                startActivity(intentSF);
                break;

            case R.id.action_search_chatroom:

                Intent intentSR = new Intent(this,SearchRoomActivity.class);
                startActivity(intentSR);
                break;


            case R.id.add_friend:
                Intent intentadd = new Intent(this,AddFriendActivity.class);
                startActivity(intentadd);
                break;

            case R.id.edit_friend:

                Intent intent = new Intent(this,EditFriendActivity.class);
                startActivity(intent);
                break;

            case R.id.add_chatroom:

                Intent intentMakeGroup = new Intent(this,MakeGroupActivity.class);
                intentMakeGroup.putExtra("FCR",false);
                startActivityForResult(intentMakeGroup,MakeGroupActivity);
                break;

            case R.id.edit_chatroom:

                Intent intentEdit = new Intent(this,EditChatRoomActivity.class);
                startActivityForResult(intentEdit,EditChatRoom);
                break;
        }

        return super.onOptionsItemSelected(item);
    }




    public class ExitThread extends Thread{

        String msgContent;
        long now;
        int roomIdExit;
        String friendIdExit;
        boolean deleteDB;

        public ExitThread(int RoomIdExit, String FriendIdExit){
            this.now = System.currentTimeMillis();
            long TimeDiff = mPref.getLong("TimeDiff",0);
            this.now = this.now +TimeDiff;
            this.msgContent = myNickname + "님이 나갔습니다";
            this.roomIdExit = RoomIdExit;
            this.friendIdExit = FriendIdExit;

        }

        @Override
        public void run() {

            Log.d("확인ExitThread", now + "#" + msgContent + "#" + roomIdExit + "#" + friendIdExit);
            db.deleteChatRoomList(roomIdExit, friendIdExit);
            db.deleteChatRoomMemberList(roomIdExit, friendIdExit);
            db.deleteChatMessageList(myUserId, roomIdExit, friendIdExit);

            mService.sendExit(roomIdExit, friendIdExit, msgContent, now);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MakeGroupActivity){
            if(resultCode == RESULT_OK){
                int roomId = data.getExtras().getInt("roomId");
                String friendId = data.getExtras().getString("inviteId");
                Log.d("Main.onactresult",friendId);
                String nickname = data.getExtras().getString("nickname");
//                fragmentCommunicator.startChatRoomActivity(roomId,friendId,nickname);
                Intent intent = new Intent(getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("friendId",friendId);
                intent.putExtra("nickname",nickname);
                intent.putExtra("roomId",roomId);
                startActivity(intent);
            }
        }else if(requestCode == EditChatRoom){
            if(resultCode == RESULT_OK){
                try {
                    String roomSet = data.getExtras().getString("roomSet");

                    JSONArray jarray = new JSONArray(roomSet);

                    String content =  myNickname + "님이 나갔습니다";
                    long now = System.currentTimeMillis();
                    long TimeDiff = mPref.getLong("TimeDiff",0);
                    now = now +TimeDiff;

                    for(int i=0;i<jarray.length();i++){

                        JSONObject jsonObject = new JSONObject(jarray.get(i).toString());

                        int roomId = jsonObject.getInt("roomId");

                        String friendId = jsonObject.getString("friendId");

                        ExitThread et = new ExitThread(roomId,friendId);
                        et.start();

                    }


                }catch (JSONException e){
                    e.printStackTrace();
                }
            }

        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        mService.mBoundState.boundCheckMain = false;
        getApplicationContext().unbindService(mConnection);

//
//        boolean autoLogin = mPref.getBoolean("autoLogin",false);
//        Log.d("MainActivity","onDestroy"+autoLogin);
//        if(!autoLogin){
//            mService.terminateService();
//        }
//
//        unregisterReceiver(mNCR);
//        unregisterReceiver(NetworkChangeUpdater);
//        mNCR = null;

    }

    public void Logout(View v){

        editor.putBoolean("autoLogin",false);
        editor.commit();
        Intent stopIntent = new Intent(this,ChatService.class);
        stopService(stopIntent);
        Log.d("MainActivity","Logout");

        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();

    }
}