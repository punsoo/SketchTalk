package messenger_project.sketchtalk.chatservice;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;

public class ChatService extends Service {

        Handler handler;
        Socket socket;
        public SharedPreferences mPref;
        public SharedPreferences.Editor editor;
        String userId;
        public MyDatabaseOpenHelper db;
        public boolean connectable;
        String ServerAddress;
        String ServerURL;
        int Port;

        public boundState mBoundState;

        static final int PostConnect = 3333;
        static final int ConnectThread = 5555;
        static final int UpdateRead = 2020;



        @Override
        public void onCreate() {

            super.onCreate();

            Log.d("확인ChatServiceOnCreate","크리에이트");
            ServerAddress = getResources().getString(R.string.ServerAddress);
            ServerURL = getResources().getString(R.string.ServerUrl);
            Port = Integer.valueOf(getResources().getString(R.string.Port));
            connectable = true;

            mPref = getSharedPreferences("login",MODE_PRIVATE);
            editor = mPref.edit();
            mBoundState = boundState.getInstance();

            handler = new Handler(){
                @Override
                public void handleMessage(Message msg){

                    if(msg.what == PostConnect) {
                        postConnect();
                    }else if(msg.what == ConnectThread){
                        ConnectThread ct = new ConnectThread();
                        ct.start();
                        Log.d("잃고난뒤4로 재연결","4로재연결");
                    }

                }
            };


        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            editor.putBoolean("Service", true);
            editor.commit();

            db = new MyDatabaseOpenHelper(this, "catchMindTalk", null, 1);

            mPref = getSharedPreferences("login", MODE_PRIVATE);
            editor = mPref.edit();
            userId = mPref.getString("userId", "아이디없음");

            Log.d("ChatServiceOnStart", userId);

            if (socket == null) {

                ConnectThread ct = new ConnectThread();
                ct.start();

            } else {

                if (socket.isClosed() || !socket.isConnected()) {
                    ConnectThread ct = new ConnectThread();
                    ct.start();
                }

            }
            return START_STICKY;
        }


        @Override
        public void onDestroy() {
            super.onDestroy();
            editor.putBoolean("Service",false);
            editor.commit();

            Log.d("ChatServiceOnDestroy","디스트로이");
        }

        public class ChatServiceBinder extends Binder {
            public ChatService getService() {
                return ChatService.this; //현재 서비스를 반환.
            }
        }


        private final IBinder mBinder = new ChatServiceBinder();

        @Override
        public IBinder onBind(Intent intent) {
            // TODO: Return the communication channel to the service.
            //throw new UnsupportedOperationException("Not yet implemented");
            return mBinder;
        }


        private CallbackChatRoom mCallbackChatRoom;
        private CallbackMain mCallbackMain;

        //액티비티에서 콜백 함수를 등록하기 위함.
        public void registerCallback_ChatRoom(CallbackChatRoom cb) {
            mCallbackChatRoom = cb;
        }

        public void registerCallback_Main(CallbackMain cb) {
            mCallbackMain = cb;
        }



        //액티비티에서 메세지 전송
        public void sendMessage(int roomId, String friendId, String msgContent, long time,int msgType){

            if(roomId < 0 ){
                try {
                    Log.d("위치확인",roomId+"#"+friendId+"#"+msgContent+"#"+time);
                    getRoomIdThread grt = new getRoomIdThread(ServerURL,userId,roomId,friendId,time);
                    grt.start();
                    grt.join();
                    roomId = grt.returnRoomId();
                    if(roomId >0){
                        if(mBoundState.boundCheckChatRoom) {
                            mCallbackChatRoom.changeRoomId(roomId);
                            mBoundState.boundedRoomId = roomId;
                        }
                    }
                    if(!db.haveChatRoom(roomId,friendId)) {
                        db.insertChatRoomMemberListMultipleByJoin(roomId,friendId);
                        db.insertChatRoomList(roomId, "group", time,"",2);

                        if (mBoundState.boundCheckChatRoom) {
                            mCallbackChatRoom.reset();
                            mCallbackChatRoom.recvUpdate();
                            mCallbackChatRoom.changeRoomId(roomId);
                        }

                        if(mBoundState.boundCheckMain == true){
                            mCallbackMain.changeRoomList();
                        }

                    }
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }


            if(roomId ==0 && !db.haveChatRoom(roomId,friendId)){

                Cursor cursor = db.getFriendData(friendId);
                cursor.moveToNext();
                String nickname = cursor.getString(1);
                String profileMessage = cursor.getString(2);
                String profileIUT = cursor.getString(3);

                db.insertChatRoomMemberList(0,friendId,nickname,profileMessage,profileIUT,0);
                db.insertChatRoomList(0,friendId,0,"",1);

                getFriendThread gft = new getFriendThread(ServerURL,userId,friendId,time, db, mBoundState, mCallbackMain);
                gft.start();
            }

            SendThread st = new SendThread(socket, userId, roomId, friendId, msgContent, time , msgType);
            st.start();

            try {
                st.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int dbMsgType;
            if(msgType ==1){
                dbMsgType = 2;
            }else{
                dbMsgType = 52;
            }


            if(st.isSuccess()){

                Log.d("st.isSucess",roomId+"#"+friendId+"#"+msgContent+"#"+time+"#"+dbMsgType);
                if(roomId==0) {
                    db.insertChatMessageList(userId, roomId, friendId, msgContent, time, dbMsgType);
                }else{
                    db.insertChatMessageList(userId, roomId, userId, msgContent, time, dbMsgType);
                }

                Log.d("메세지마크2",msgContent+"");
                mCallbackChatRoom.sendMessageMark(friendId,msgContent,time,msgType);

            }else{

                if(roomId ==0) {

                }else{

                }

            }
        }



    public void sendRead(int roomId, String friendId, long time){
        Log.d("확인sendRead",roomId+"#"+friendId+"#"+time);
        if (roomId < 0){
            return;
        }

        SendThread st = new SendThread(socket, userId, roomId, friendId, "justUpdateTime", time , UpdateRead);
        st.start();

    }


    public void sendExit(int roomId,String friendId, String msgContent, long time){
        if (roomId < 0){
            return;
        }

        SendThread st = new SendThread(socket, userId, roomId, friendId, msgContent, time, 4);
        st.start();

    }

    public void sendInvite(int roomId, String friendId, String msgContent, long time, String inviteId ){


        if(roomId < 0){
            return;
        }


        db.insertChatRoomMemberListMultipleByJoin(roomId,inviteId);


        JSONObject jobject = new JSONObject();


        try {
            jobject.put("msgContent", msgContent);
            jobject.put("inviteId", inviteId);
        }catch (JSONException e){
            e.printStackTrace();
        }


        SendThread st = new SendThread(socket, userId, roomId, friendId, jobject.toString(), time, 5);
        st.start();


        db.insertChatMessageList(userId, roomId, userId, msgContent, time, 5);
        mCallbackChatRoom.sendInviteMark(msgContent,time,true);


    }

    public void sendPATH(int roomId, String friendId, String msgContent, long time){
        if (roomId < 0){
            return;
        }

        SendThread st = new SendThread(socket, userId, roomId, friendId, msgContent, time , 10);
        st.start();

    }

    public void sendClear(int roomId,String friendId, String msgContent, long time){
        if (roomId < 0){
            return;
        }

        SendThread st = new SendThread(socket, userId, roomId, friendId, msgContent, time , 11);
        st.start();

    }


    public void sendDrawChat(int roomId,String friendId, String content, long time){
        if (roomId < 0){
            return;
        }

        SendThread st = new SendThread(socket, userId, roomId, friendId, content, time , 88);
        st.start();

    }

    public void Reconnect(){

        Log.d("ChatService","Reconnect");

        try {
            socket.close();
            socket = null ;
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        Message message= Message.obtain();
        message.what = ConnectThread;
        handler.sendMessage(message);
    }

    public class ReceiveThread extends Thread {

        InputStream receiver;
        DataInputStream input;
        String response;
        String checkId;


        public ReceiveThread(Socket threadsocket) {
            this.checkId = userId;

            try{
                this.receiver = threadsocket.getInputStream();
                this.input = new DataInputStream(receiver);

            }catch (Exception e){

            }
        }


        @Override
        public void run() {

            while(true) {

                if(!userId.equals(checkId)){
                    Log.d("ChatServiceCheckId","checkId: "+checkId+", userId: "+userId);
                    if(socket != null) {
                        try {
                            socket.close();
                        }catch(IOException e){
                            e.printStackTrace();
                            Log.d("Reconnect","1");
                            Reconnect();
                        }
                    }
                    break;
                }
                try {

                    Log.d("ChatServiceReceive",userId +"####"+response);

                    try {
                        response = input.readUTF();
                    }catch (EOFException e){
                        e.printStackTrace();
                        Log.d("Reconnect","2");
                        Reconnect();
                        return;
                    }

                    int roomId = 0;
                    String friendId = "";
                    String msgContent = "";
                    long time = 0 ;
                    int msgType = 0;

                    try {
                        JSONObject obj = new JSONObject(response);
                        roomId = obj.getInt("roomId");
                        friendId = obj.getString("friendId");
                        msgContent = obj.getString("msgContent");
                        time = obj.getLong("time");
                        msgType = obj.getInt("msgType");

                        ReceiveMessageThread rmt = new ReceiveMessageThread(socket,ServerURL,userId,roomId,friendId,msgContent,time,msgType,db,mBoundState,mCallbackChatRoom,mCallbackMain);
                        rmt.start();

                        Log.d("ChatServiceReceiveData","roomId: "+roomId+", friendId: "+friendId+", msgContent: "+msgContent+", time: "+time+", msgType: "+msgType);

                    }catch(JSONException e){
                        Log.d("ChatServiceReceive","JSONException");
                    }


                } catch (IOException e) {
                    Log.d("ChatServiceReceiveIOE",userId +"####"+response);
                    Log.d("Reconnect","3");
                    Reconnect();
                    e.printStackTrace();

                    break;
                } catch (NullPointerException e){
                    Log.d("ChatServiceReceiveNPE",userId +"####"+response);
                    Log.d("Reconnect","4");
                    Reconnect();
                    e.printStackTrace();
                    break;
                }

            }


            Log.d("ChatServiceReceiveDead",userId +"####"+response);

        }
    }

    public void postConnect(){
        ReceiveThread startReceive = new ReceiveThread(socket);
        startReceive.start();
    }

    public class ConnectThread extends Thread {

        String serverAddress;
        int port;

        public ConnectThread(){
            this.serverAddress = ServerAddress;
            this.port = Port;

            Log.d("ServiceConnectThread생성자",this.serverAddress+"##"+this.port+"##"+userId);
        }

        @Override
        public void run() {

            if(socket != null){
                if(socket.isConnected()){
                    return;
                }
            }

            try {

                if(socket != null) {
                    socket.close();
                    socket = null;
                }

                socket = new Socket(serverAddress, port);

            } catch (UnknownHostException e) {
                Log.d("ConnectThread","UnknowHostException");
                e.printStackTrace();
            } catch (IOException e) {

                Log.d("ConnectThread","IOException");
                if(mPref.getBoolean("Service",false)) {

                    ConnectThread ct = new ConnectThread();
                    ct.start();

                }

                e.printStackTrace();
                return;
            }


            try {

                OutputStream sender = socket.getOutputStream();
                DataOutputStream output = new DataOutputStream(sender);
                String sendData = userId;
                output.writeUTF(sendData);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                long ServerTime = Long.parseLong(input.readUTF());
                long now = System.currentTimeMillis();
                editor.putLong("TimeDiff",ServerTime - now);
                editor.commit();
                Log.d("타임디프",(ServerTime-now)+"");

                Message message= Message.obtain();
                message.what = PostConnect;
//                    try { // 스레드에게 수행시킬 동작들 구현
//                        Thread.sleep(1000); // 1초간 Thread를 잠재운다
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                handler.sendMessage(message);
                Log.d("ServiceConnectThread완료",""+sendData);

            }catch (IOException e){
                e.printStackTrace();
                Log.d("ServiceConnectThread","IOE예외");
            }catch (NullPointerException e){
                e.printStackTrace();
                Log.d("ServiceConnectThread","Null에외");
            }

        }
    }
}
