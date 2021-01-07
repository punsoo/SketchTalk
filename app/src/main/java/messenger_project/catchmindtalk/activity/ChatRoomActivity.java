package messenger_project.catchmindtalk.activity;


import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;
import messenger_project.catchmindtalk.ChatRoomViewPager;
import messenger_project.catchmindtalk.ChatService;
import messenger_project.catchmindtalk.DrawLine;
import messenger_project.catchmindtalk.Item.MemberListItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.adapter.ChatRoomPagerAdapter;
import messenger_project.catchmindtalk.adapter.MemberListAdapter;
import messenger_project.catchmindtalk.fragment.DrawRoomFragment;
import messenger_project.catchmindtalk.fragment.MessageRoomFragment;

public class ChatRoomActivity extends BaseActivity implements DrawLine.sendToActivity, NavigationView.OnNavigationItemSelectedListener{



    //    private ViewPager viewPager;
    private ChatRoomViewPager viewPager;
    String friendId;
    String friendNickname;
    String friendProfile;
    int roomId;
    String roomName;
    String myUserId;
    String myNickname;
    String myProfileMessage;
    String myProfileImageUpdateTime;
    Toolbar toolbar;
    Socket socket;
    EditText sendcontent;
    final private static String LOG = "ChatRoomActivity";
    public static Handler handler;
    MessageRoomFragment mf;
    DrawRoomFragment df;
    FragmentCommunicator fragmentCommunicator;
    DrawCommunicator drawCommunicator;
    public String sendName;
    public String sendContent;
    private ChatService mService;
    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    public MyDatabaseOpenHelper db;

    public HashMap<String,String> NickHash = new HashMap<>();
    public HashMap<String,String> ProfileIUTHash = new HashMap<>();
    BroadcastReceiver NetworkChangeUpdater;
    public ImageButton plusBtn;
    public Button drawModeBtn;
    public Button sendMsgBtn;
    public Button drawChatBtn;
    public ImageButton alarmActive;
    DrawerLayout drawer;

    public int delPosition;

    public static final int MakeGroupActivity = 6839;

    public static final int DeleteImage = 3102;

    public static final int DeleteMessage = 2013;
    public static final int UpdateRead = 2020;



    MemberListAdapter memberListAdapter;
    ArrayList<MemberListItem> memberListItemList;

    NavigationView navigationView;

    final String upLoadServerUri = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private Uri mImageCaptureUri;

    String alarmKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom_nav);

        Intent serviceIntent = new Intent(this, ChatService.class);
        getApplicationContext().bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);

        sendcontent = (EditText)findViewById(R.id.messageContent);

        // Adding Toolbar to the activity
        toolbar = (Toolbar) findViewById(R.id.toolbarChatRoom);
        plusBtn = (ImageButton) findViewById(R.id.plus_btn);
        drawModeBtn = (Button) findViewById(R.id.drawMode_btn);
        sendMsgBtn = (Button) findViewById(R.id.SendMsgBtn);
        drawChatBtn = (Button) findViewById(R.id.drawChatBtn);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
//        actionBar.setDisplayShowTitleEnabled(false);


        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);
        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();


        myUserId = mPref.getString("userId","아이디없음");
        myNickname = mPref.getString("nickname","닉없음");
        myProfileMessage = mPref.getString("profileMessage","");
        myProfileImageUpdateTime = mPref.getString("profileImageUpdateTime","");


        Intent GI = getIntent();

        friendId = GI.getStringExtra("friendId");
        friendNickname = GI.getStringExtra("nickname");

        Log.d("확인chatRoom",friendId+"#"+friendNickname);
        roomId = GI.getIntExtra("roomId",0);
        roomName = GI.getStringExtra("roomName");
//        if(friendId.equals("noti")){
//            getFriendId(no);
//        }


        Reset();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        NetworkChangeUpdater = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //UI update here
                if (intent != null) {
//                    Toast.makeText(context, "액티비티의 리시버작동!"+intent.toString(), Toast.LENGTH_LONG).show();
                    String networkType = intent.getExtras().getString("wifi");
                    UpdateNetwork(networkType);

                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("receiver.to.activity.transfer");

        registerReceiver(NetworkChangeUpdater, filter);

        // Initializing ViewPager
        viewPager = (ChatRoomViewPager) findViewById(R.id.pagerChatRoom);
        mf = new MessageRoomFragment();
        df = new DrawRoomFragment();
        fragmentCommunicator = (FragmentCommunicator) mf;
        drawCommunicator = (DrawCommunicator) df;
        ChatRoomPagerAdapter pagerAdapter = new ChatRoomPagerAdapter(getSupportFragmentManager(),mf,df,mPref,roomId,friendId);

        Log.d("chatRoomActivity",myUserId +"###"+friendId);

        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if(position == 0){
                    ChatRoomViewPager.DrawMode = false;
                    drawModeBtn.setVisibility(View.GONE);
                    plusBtn.setVisibility(View.VISIBLE);
                    drawChatBtn.setVisibility(View.GONE);
                    sendMsgBtn.setVisibility(View.VISIBLE);

                }else if(position == 1){
                    ChatRoomViewPager.DrawMode = false;
                    plusBtn.setVisibility(View.GONE);
                    drawModeBtn.setVisibility(View.VISIBLE);
                    sendMsgBtn.setVisibility(View.GONE);
                    drawChatBtn.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });





        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){


                if(msg.what == 1) {

                    String friendId = msg.getData().getString("friendId");
                    String msgContent = msg.getData().getString("msgContent");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData(friendId, NickHash.get(friendId), ProfileIUTHash.get(friendId), msgContent, time, 1);
                    Log.d("확인pass",NickHash.get(friendId)+"#");

                }else if(msg.what ==2){
                    String friendId = msg.getData().getString("friendId");
                    String msgContent = msg.getData().getString("msgContent");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData(friendId,NickHash.get(friendId), ProfileIUTHash.get(friendId), msgContent, time, 2);
                }else if(msg.what ==3){
                    String msgContent = msg.getData().getString("msgContent");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData("내아아이디","내닉네임","내프로필", msgContent, time, 3);
                }else if(msg.what==UpdateRead){
                    fragmentCommunicator.alertChange();
                }else if(msg.what==10){
                    String path = msg.getData().getString("path");
                    drawCommunicator.receivePath(path);
                }else if(msg.what==11){
                    drawCommunicator.receiveClear();
                }else if(msg.what==99){
                    memberListAdapter.notifyDataSetChanged();
                }else if(msg.what==88){

                    String friendId = msg.getData().getString("friendId");
                    String content = msg.getData().getString("content");
                    String nickname;
                    if(true) {
                        nickname = friendNickname;
                    }else{
                        nickname = NickHash.get(friendId);
                    }
                    drawCommunicator.drawChat(nickname,content);
                }else if(msg.what==51){

                    String msgContent = msg.getData().getString("msgContent");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData(friendId, NickHash.get(friendId), ProfileIUTHash.get(friendId), msgContent, time, 51);

                }else if(msg.what==52){

                    String friendId = msg.getData().getString("friendId");
                    String content = msg.getData().getString("content");
                    long time = msg.getData().getLong("time");
                    fragmentCommunicator.passData("내아이디","내닉네임","내프로필", content, time, 52);

                }else if(msg.what == 365){
                    fragmentCommunicator.bottomSelect();
                }


            }
        };


        attachKeyboardListeners();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ListView lv = (ListView) findViewById(R.id.memberList);

        if(false) {

            View header = getLayoutInflater().inflate(R.layout.member_invite_header, null, false);

            header.setOnClickListener(mClickListener);

            lv.addHeaderView(header);

        }

        memberListItemList = new ArrayList<>();

        Vector<String[]> MemberList;

        MemberList = db.getChatRoomMemberList(roomId, friendId);

        MemberListItem myItem = new MemberListItem(myUserId,myNickname,myProfileMessage,myProfileImageUpdateTime);
        memberListItemList.add(myItem);

        for(int i=0;i<MemberList.size();i++){
            MemberListItem addItem = new MemberListItem(MemberList.get(i)[0],MemberList.get(i)[1],MemberList.get(i)[2],MemberList.get(i)[3]);
            memberListItemList.add(addItem);
        }


        memberListAdapter = new MemberListAdapter(this,memberListItemList);

        lv.setAdapter(memberListAdapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("아이템",""+position);
            }
        });

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        alarmActive = (ImageButton) findViewById(R.id.alarmImageBtn);

//        if(no == 0){
//            alarmKey = friendId;
//        }else{
//            alarmKey = no + "";
//        }
//        if(!mPref.getBoolean(alarmKey,true)){
//            alarmActive.setBackgroundResource(R.drawable.alarm_disable_icon);
//        }else{
//            alarmActive.setBackgroundResource(R.drawable.alarm_active_icon);
//        }



    }

    private ServiceConnection mConnection = new ServiceConnection() {
        // Called when the connection with the service is established
        public void onServiceConnected(ComponentName className, IBinder service) {
            ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback_ChatRoom(mCallback); //콜백 등록
            mService.boundCheck_ChatRoom = true;
            mService.boundStart = true;
            mService.boundedRoomId = roomId;
            mService.boundedFriendId = friendId;
            long now = System.currentTimeMillis();
            mService.sendRead(roomId, friendId, now);

        }

        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
                db.updateChatRoomLastReadTime(roomId,friendId,System.currentTimeMillis());
        if(mService != null) {
            Log.d("확인onStart","mService");
            mService.boundStart = true;
            long now = System.currentTimeMillis();
            mService.sendRead(roomId, friendId, now);
        }
        if(mService == null){
            Log.d("확인onStart","mServiceNull");
        }
        fragmentCommunicator.alertChange();
    }


    @Override
    protected void onStop() {
        super.onStop();
        mService.boundStart = false;
    }


    public void activeAlarm(View v){

        if(mPref.getBoolean(alarmKey,true)){
            editor.putBoolean(alarmKey,false);
            editor.commit();
            alarmActive.setBackgroundResource(R.drawable.alarm_disable_icon);

        }else{

            editor.putBoolean(alarmKey,true);
            editor.commit();
            alarmActive.setBackgroundResource(R.drawable.alarm_active_icon);


        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {


            Intent intentMakeGroup = new Intent(getApplicationContext(),MakeGroupActivity.class);
            intentMakeGroup.putExtra("FCR",true);
            intentMakeGroup.putExtra("friendId",friendId);
            Log.d("설마",friendId);
            startActivityForResult(intentMakeGroup,MakeGroupActivity);


        }
    };

    public void ResetMemberList(){
        memberListAdapter.clearList();
        memberListItemList = new ArrayList<>();

        Vector<String[]> MemberList;

        MemberList = db.getChatRoomMemberList(roomId, friendId);

        MemberListItem myItem = new MemberListItem(myUserId,myNickname,myProfileMessage,myProfileImageUpdateTime);
        memberListAdapter.addMemberItem(myItem);
        for(int i=0;i<MemberList.size();i++){
            MemberListItem addItem = new MemberListItem(MemberList.get(i)[0],MemberList.get(i)[1],MemberList.get(i)[2],MemberList.get(i)[3]);
            memberListAdapter.addMemberItem(addItem);
        }

        Message message= Message.obtain();
        message.what = 99;

        handler.sendMessage(message);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){

            if(requestCode == MakeGroupActivity){
                long now = System.currentTimeMillis();
                String content = data.getExtras().getString("content");
                String inviteId = data.getExtras().getString("inviteId");

//                mService.sendInvite(roomId,friendId,content,now,inviteId);

            }else if(requestCode == PICK_FROM_CAMERA){

                try {


                    String imgpath = data.getExtras().getString("CustomPath");

                    if(imgpath.equals("none")) {
                        Toast.makeText(this,"사진촬영 실패",Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Log.d("이미지경로",imgpath);


                    ImageSendThread ist = new ImageSendThread(imgpath);
                    ist.start();

                    ist.join();

                    SetBottomThread sbt = new SetBottomThread();
                    sbt.start();

                }catch(Exception e){
                    e.printStackTrace();
                }


            }else if(requestCode == PICK_FROM_ALBUM ){

                try {

                    Log.d("이미지경로",getPath(data.getData()));

                    ImageSendThread ist = new ImageSendThread(getPath(data.getData()));

                    ist.start();

                    ist.join();

                    SetBottomThread sbt = new SetBottomThread();
                    sbt.start();



                }catch(Exception e){
                    e.printStackTrace();
                }

            }else if(requestCode == DeleteImage){


                int position = data.getExtras().getInt("position");
                fragmentCommunicator.deleteMessage(position);



            }else if(requestCode == DeleteMessage){


                delPosition = data.getExtras().getInt("position");
                String type = data.getExtras().getString("type");

                if(type.equals("del")) {

                    DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragmentCommunicator.deleteMessage(delPosition);
                        }

                    };


                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    };


                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setMessage("선택한 메시지를 삭제하시겠습니까 \n \n 삭제한 메시지는 내 채팅방에서만 적용되며 상대방의 채팅방에서는 삭제되지 않습니다.")
                            .setPositiveButton("확인", deleteListener)
                            .setNegativeButton("취소", cancelListener)
                            .create();


                    dialog.show();


                    Button deleteBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    deleteBtn.setTextColor(Color.BLACK);

                    Button cancelBtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    cancelBtn.setTextColor(Color.BLACK);


                }else if(type.equals("copy")){


                    String subType = data.getExtras().getString("subType");

                    if(subType.equals("text")) {

                        String content = data.getExtras().getString("content");

                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("catchMind", content);
                        clipboard.setPrimaryClip(clip);
                    }


                }else if(type.equals("share")){



                    String subType = data.getExtras().getString("subType");

                    if(subType.equals("text")) {

                        String content = data.getExtras().getString("content");

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, content);
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);

                    }else if(subType.equals("image")){

//                        String content = data.getExtras().getString("content");
//
////                        ImageShareThread ist = new ImageShareThread(content,this);
////                        ist.start();


                    }


                }

            }

        }



    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, R.string.open_drawer, R.string.close_drawer);

        drawer.addDrawerListener(toggle);
        toggle.syncState();


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawer_menu, menu);
        return true;
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    protected void onShowKeyboard(int keyboardHeight) {
        // do things when keyboard is shown
//        Toast.makeText(this,"show",Toast.LENGTH_SHORT).show();
        Log.d("키보드","show"+keyboardHeight);
        drawCommunicator.resizeSketchBook();
    }

    @Override
    protected void onHideKeyboard() {
        // do things when keyboard is hidden
//        Toast.makeText(this,"hide",Toast.LENGTH_SHORT).show();
        Log.d("키보드","hide");
        drawCommunicator.resizeSketchBook();
    }





    public void UpdateNetwork(String type){
//        if(type.equals("wifi")) {
//            Intent serviceIntent = new Intent(this, ChatService.class);
//            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
//            Log.d("UpdateNetwork","UPDATE wifi##"+type);
//        }else{
//            Intent serviceIntent = new Intent(this, ChatService.class);
//            bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);
//            Log.d("UpdateNetwork","UPDATE nonewifi##"+type);
//        }
    }

    public void Reset(){


        NickHash = new HashMap<>();
        ProfileIUTHash = new HashMap<>();
        Vector<String[]> chatRoomMemberList = db.getChatRoomMemberList(roomId,friendId);
        String setRoomName ="";
        if(chatRoomMemberList.size()>0) {
            for(int i=0;i<chatRoomMemberList.size();i++) {

                if (i == 0) {
                    setRoomName = chatRoomMemberList.get(0)[1];
                } else {
                    setRoomName += ", " + chatRoomMemberList.get(i)[1];
                }

                NickHash.put(chatRoomMemberList.get(i)[0],chatRoomMemberList.get(i)[1]);
                ProfileIUTHash.put(chatRoomMemberList.get(i)[0],chatRoomMemberList.get(i)[1]);
            }

        }else{
            setRoomName = friendNickname;
        }

        if(roomId > 0){

            JSONArray jarray = new JSONArray();
            for(int i=0;i<chatRoomMemberList.size();i++) {
                jarray.put(chatRoomMemberList.get(i)[0]);

            }
            friendId = jarray.toString();
        }

        if(roomName == null || roomName.equals("")) {
            getSupportActionBar().setTitle(setRoomName);
        }else{
            getSupportActionBar().setTitle(roomName);
        }


    }



    public interface FragmentCommunicator {

        void passData(String friendId, String nickname, String profileIUT, String msgContent, long time,int type);
        void alertChange();
        void changeRoomId(int sRoomId);
        void deleteMessage(int position);
        void bottomSelect();

    }

    public interface DrawCommunicator {

        void receivePath(String PATH);
        void resizeSketchBook();
        void MinusWidth();
        void PlusWidth();
        void receiveClear();
        void drawChat(String Nickname,String Content);

    }



    private ChatService.ICallback_ChatRoom mCallback = new ChatService.ICallback_ChatRoom() {

        public void recvData(String friendId,String msgContent,long time) {

            Message message= Message.obtain();
            message.what = 1;
            Bundle bundle = new Bundle();
            bundle.putString("friendId",friendId);
            bundle.putString("msgContent",msgContent);
            bundle.putLong("time",time);
            message.setData(bundle);
            handler.sendMessage(message);

        }

        public void recvUpdate(){
            Message message= Message.obtain();
            message.what =2020;
            handler.sendMessage(message);
        }
        public void changeRoomId(int passRoomId){
                roomId = passRoomId;
                fragmentCommunicator.changeRoomId(passRoomId);
        }

        public void sendMessageMark(String friendId, String msgContent,long time){
            Message message= Message.obtain();
            message.what = 2;

            Bundle bundle = new Bundle();
            bundle.putString("friendId",friendId);
            bundle.putString("msgContent",msgContent);
            bundle.putLong("time",time);


            message.setData(bundle);

            handler.sendMessage(message);
        }

        public void sendInviteMark(String inviteId,String content,long time,boolean resetMemberList){

        }

        @Override
        public void sendExitMark(String sFriendId, String msgContent, long time) {
            ResetMemberList();
            Reset();
            Message message= Message.obtain();
            message.what = 3;

            Bundle bundle = new Bundle();
            bundle.putString("msgContent",msgContent);
            bundle.putLong("time",time);

            message.setData(bundle);

            handler.sendMessage(message);

        }

        @Override
        public void sendImageMark(String friendId, String content, long time ,int kind) {

        }

        public void reset(){
            Reset();
            ResetMemberList();
        }

        public String getFriendId(){
            return friendId;
        }

        public void receivePath(String PATH){

        }

        @Override
        public void receiveClear() {

        }

        @Override
        public void receiveDrawChat(String friendId, String content) {

        }
    };



    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mService.boundCheck_ChatRoom){
            getApplicationContext().unbindService(mConnection);
        }
        mService.boundCheck_ChatRoom = false;
        mService.boundStart = false;
        mService.boundedRoomId = -1;
        mService.boundedFriendId ="";
        unregisterReceiver(NetworkChangeUpdater);
        ChatRoomViewPager.DrawMode = false;
    }

    @Override
    public void sendPath(String PATH){
//        long now = System.currentTimeMillis();
//        mService.sendPATH(no,friendId,PATH,now);
    }

    @Override
    public void sendClear() {
//        long now = System.currentTimeMillis();
//        mService.sendClear(no,friendId,"just Clear",now);
    }

    public void sendMessage(View v){


        long now = System.currentTimeMillis();

        String et = sendcontent.getText().toString();
        sendcontent.setText("");
        Log.d("sendMessage,db.insert",myUserId+"####"+friendId+"####"+et);
        mService.sendMessage(roomId,friendId,et,now);

    }


    public String getUserId() {
        return myUserId;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            if(drawer.isDrawerOpen(navigationView)){
                drawer.closeDrawer(navigationView);
            }else {
                finish(); // close this activity and return to preview activity (if there is any)
            }
        }else if(item.getItemId() == R.id.drawer_menu_icon){
            if(drawer.isDrawerOpen(navigationView)){
                drawer.closeDrawer(navigationView);
            }else {
                drawer.openDrawer(navigationView);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void ImageSendBtn(View v){
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                doTakePhotoAction();
            }

        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                doTakeAlbumAction();

            }

        };


        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){

            @Override public void onClick(DialogInterface dialog, int which){
                dialog.dismiss();
            }

        };




        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("이미지 전송")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("취소", cancelListener)
                .setNegativeButton("앨범선택", albumListener)
                .create();

        dialog.show();

        Button pbtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbtn.setTextColor(Color.BLACK);
        Button neubtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        neubtn.setTextColor(Color.BLACK);
        Button negbtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        negbtn.setTextColor(Color.BLACK);
    }

    public void DrawModeBtn(View v){
        if(ChatRoomViewPager.DrawMode){
            drawModeBtn.setBackgroundResource(R.drawable.btn_border);
            ChatRoomViewPager.DrawMode = false;
        }else{
            drawModeBtn.setBackgroundResource(R.drawable.btn_border_active);
            ChatRoomViewPager.DrawMode = true;
        }
    }


    public void minusWidth(View v){
        drawCommunicator.MinusWidth();
    }

    public void plusWidth(View v){
        drawCommunicator.PlusWidth();
    }


    public void drawChat(View v){
//        String et = sendcontent.getText().toString();
//        drawCommunicator.drawChat(userNickname,et);
//        sendcontent.setText("");
//        mService.sendDrawChat(no,friendId,et,0);
    }

    public void exitRoom(View v){

        DialogInterface.OnClickListener exitListener = new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which){
                exitRoom();
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

    public void exitRoom(){

//        db.deleteRoom(no,friendId);
//        db.deleteChatFriendAll(no,friendId);
//        db.deleteMessageData(no,friendId);
//        String content = userNickname + "님이 나갔습니다";
//        long now = System.currentTimeMillis();
//        mService.sendExit(no,friendId,content,now);
        finish();

    }

    public void getFriendId(int roomId){
//        Cursor cursor = db.getChatFriendListByNo(no);
//        JSONArray idArray = new JSONArray();
//
//        while(cursor.moveToNext()){
//            idArray.put(cursor.getString(1));
//        }
//
//        friendId = idArray.toString();
    }




//    public int uploadFile(String sourceFileUri) {

//        Log.d("이미지업로드시작CRA",sourceFileUri);
//        String fileName = sourceFileUri;
//
//        HttpURLConnection conn = null;
//        DataOutputStream dos = null;
//        String lineEnd = "\r\n";
//        String twoHyphens = "--";
//        String boundary = "*****";
//        int bytesRead, bytesAvailable, bufferSize;
//        byte[] buffer;
//        int maxBufferSize = 1 * 1024 * 1024;
////        File sourceFile = new File(sourceFileUri);
//
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp.png";
//
//        Bitmap b= BitmapFactory.decodeFile(sourceFileUri);
//
//        Log.d("뭘기대",b.getWidth()+"###"+b.getHeight());
//
//        float height = 400 * (float)b.getHeight() /  (float)b.getWidth();
//
//        Bitmap out = Bitmap.createScaledBitmap(b, 400, (int)height, false);
//
//
//        File sourceFile = new File(path);
//        FileOutputStream fOut;
//
//
//
//        try {
//
//            Log.d("이미지새파일경로1",sourceFile.getAbsolutePath());
//            fOut = new FileOutputStream(sourceFile);
//            Log.d("이미지새파일경로2",sourceFile.getAbsolutePath());
//            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//
//        } catch (Exception e) {
//
//        }
//
//
//
//        if (!sourceFile.isFile()) {
//
//            Log.d("이미지경로에없음","경로에없나?");
////            dialog.dismiss();
//
//            return 0;
//
//        }else{
//
//            int serverResponseCode = 123;
//
//            try {
//
//                // open a URL connection to the Servlet
//                FileInputStream fileInputStream = new FileInputStream(sourceFile);
//                URL url = new URL(upLoadServerUri);
//                // Open a HTTP  connection to  the URL
//                conn = (HttpURLConnection) url.openConnection();
//                conn.setDoInput(true); // Allow Inputs
//                conn.setDoOutput(true); // Allow Outputs
//                conn.setUseCaches(false); // Don't use a Cached Copy
//                conn.setRequestMethod("POST");
//                conn.setRequestProperty("Connection", "Keep-Alive");
//                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
//                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
//                conn.setRequestProperty("uploaded_file", fileName);
//
//                dos = new DataOutputStream(conn.getOutputStream());
//                dos.writeBytes(twoHyphens + boundary + lineEnd);
//
//
//                long now = System.currentTimeMillis();
//                String imageName = userId + "_" + now + ".png";
//
//
//                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
//                        + imageName + "\"" + lineEnd);
//
//                dos.writeBytes(lineEnd);
//                // create a buffer of  maximum size
//                bytesAvailable = fileInputStream.available();
//
//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                buffer = new byte[bufferSize];
//
//                // read file and write it into form...
//                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                while (bytesRead > 0) {
//
//                    dos.write(buffer, 0, bufferSize);
//                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
//                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
//
//                }
//
//                // send multipart form data necesssary after file data...
//                dos.writeBytes(lineEnd);
//                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
//
//                // Responses from the server (code and message)
//                serverResponseCode = conn.getResponseCode();
//                String serverResponseMessage = conn.getResponseMessage();
//
//                Log.d("이미지uploadFile", "HTTP Response is : "
//                        + serverResponseMessage + ": " + serverResponseCode);
//
//
//                //close the streams //
//                fileInputStream.close();
//                dos.flush();
//                dos.close();
//
//
//                InputStream is = null;
//                BufferedReader in = null;
//
//
//                is = conn.getInputStream();
//                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
//                String line = null;
//                StringBuffer buff = new StringBuffer();
//                while ( ( line = in.readLine() ) != null )
//                {
//                    buff.append(line + "\n");
//                }
//                String data = buff.toString().trim();
//                Log.d("이미지성공실패",data);
//
//                if(data.equals("OK")){
//                    mService.sendImage(no,friendId,imageName,now);
//                }
//
//
//            } catch (MalformedURLException ex) {
//
//
//                ex.printStackTrace();
//
//
//
//                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
//            } catch (Exception e) {
//
//                e.printStackTrace();
//
//
//            }
//
////            dialog.dismiss();
//            return serverResponseCode;
//
//        } // End else block

//    }




    public class ImageSendThread extends Thread {

        public String filePath;

        public ImageSendThread (String uri){
            this.filePath = uri;
        }

        @Override
        public void run() {

//            uploadFile(filePath);

        }


    }


    public void doTakePhotoAction(){

//        Intent intent = new Intent (this, CustomCameraActivity.class);
//
//        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    public void doTakeAlbumAction(){

        Intent intent = new Intent (Intent.ACTION_PICK);
        intent.setType ("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }

    public String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }




    public class SetBottomThread extends Thread {


        public SetBottomThread (){

        }

        @Override
        public void run() {

            try {

                Thread.sleep(700);

                Message message= Message.obtain();
                message.what = 365;

                handler.sendMessage(message);


            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }


    }



//    public class ImageShareThread extends Thread {

//        String sContent;
//        Context mContext;
//
//        public ImageShareThread (String content,Context context) {
//
//            sContent = content;
//            mContext = context;
//
//        }
//
//        @Override
//        public void run() {
//
//            try {
//
//
//                Bitmap bitmap = Glide.
//                        with(mContext).
//                        load(sContent).
//                        asBitmap().
//                        into(-1, -1).
//                        get();
//
//                String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"title", null);
//                Uri bitmapUri = Uri.parse(bitmapPath);
//
//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("image/*");
//                intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
//                startActivity(Intent.createChooser(intent, "Share"));
//
//
//            }catch (InterruptedException e){
//                e.printStackTrace();
//            }catch (ExecutionException e){
//                e.printStackTrace();
//            }
//
//        }
//
//
//    }


}
