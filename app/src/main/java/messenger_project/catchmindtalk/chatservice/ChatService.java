package messenger_project.catchmindtalk.chatservice;


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;

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
                    getRoomIdThread grt = new getRoomIdThread(roomId,friendId,time);
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

                getFriendThread gft = new getFriendThread(friendId,time);
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
//                    long ServerTime = Long.parseLong(input.readUTF());
//                    long now = System.currentTimeMillis();
//                    editor.putLong("TimeDiff",ServerTime - now);
//                    editor.commit();
//                    Log.d("타임디프",(ServerTime-now)+"");

                    Message message= Message.obtain();
                    message.what = PostConnect;
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



    public class ReceiveThread extends Thread {

        String response;
        InputStream receiver;
        DataInputStream input;
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

                        ReceiveMessageThread rmt = new ReceiveMessageThread(roomId,friendId,msgContent,time,msgType);
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

//    public void loseReceive(){
//
//        Log.d("ChatService","loseReceive");
//
//        if(!socket.isClosed()) {
//            Log.d("ChatService","loseReceive해제메시지");
//            SendThread st = new SendThread(socket,0,"해제자","해체",0,33 );
//            st.start();
//            try {
//                st.join();
//            }catch (InterruptedException e){
//                e.printStackTrace();
//            }
//
//        }
//
//        try {
//            socket.close();
//            socket = null ;
//        }catch (NullPointerException e){
//            e.printStackTrace();
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//        Message message= Message.obtain();
//        message.what = ConnectThread;
//        handler.sendMessage(message);
//    }

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




    public class ReceiveMessageThread extends Thread {

            public int sRoomId;
            public String sFriendId;
            public String sMsgContent;
            public long sTime;
            public int sMsgType;

            public ReceiveMessageThread(int roomId, String friendId, String msgContent, long time, int msgType) {

                this.sRoomId = roomId;
                this.sFriendId = friendId;
                this.sMsgContent = msgContent;
                this.sTime = time;
                this.sMsgType = msgType;
            }

            @Override
            public void run() {
                Log.d("ReceiveMessageThread",sRoomId+"#"+sFriendId+"#"+sMsgContent+"#"+sTime+"#"+sMsgType);
                db.updateChatRoomMemberLastReadTime(sRoomId,sFriendId,sTime);
                if(sMsgType == 1 || sMsgType == 51){

                    db.insertChatMessageList(userId,sRoomId,sFriendId,sMsgContent,sTime,sMsgType);

                    if(mBoundState.boundStart) {
                        if(sRoomId == 0 ) {
                            if(mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                                db.updateChatRoomLastReadTime(sRoomId, sFriendId, sTime);
                                sendRead(sRoomId,mCallbackChatRoom.getFriendId(),sTime);
                            }
                        }else{
                            if(mBoundState.boundedRoomId == sRoomId) {
                                db.updateChatRoomLastReadTime(sRoomId, sFriendId, sTime);
                                sendRead(sRoomId,mCallbackChatRoom.getFriendId(),sTime);
                            }
                        }
                    }


                    if(sRoomId ==0 && !db.haveChatRoom(sRoomId,sFriendId)){
                        try {
                            getFriendThread gft = new getFriendThread(sFriendId, sTime);
                            gft.start();
                            gft.join();
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }else if(sRoomId > 0 && !db.haveChatRoom(sRoomId,sFriendId)){
                        try {
                            getGroupThread ggt = new getGroupThread(sRoomId, sFriendId, sMsgContent, sTime, sMsgType);
                            ggt.start();
                            ggt.join();
                        }catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }else{
                        if(mBoundState.boundCheckChatRoom) {
                            if (sRoomId == 0) {

                                if (mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                                    mCallbackChatRoom.recvData(sFriendId, sMsgContent, sTime, sMsgType);
                                }


                            } else {
                                if (mBoundState.boundedRoomId == sRoomId) {
                                    mCallbackChatRoom.recvData(sFriendId, sMsgContent, sTime, sMsgType);
                                }
                            }
                        }
                    }

                    if(mBoundState.boundCheckMain) {
                        mCallbackMain.changeRoomList();
                    }

                }else if(sMsgType == UpdateRead){

//                    db.updateChatRoomMemberLastReadTime(sRoomId,sFriendId,sTime);
                    if(mBoundState.boundStart){
                        if(sRoomId == 0){
                            if(mBoundState.boundedRoomId ==0 && mBoundState.boundedFriendId.equals(sFriendId)){
                                mCallbackChatRoom.recvUpdate();
                            }
                        }else{
                            if(mBoundState.boundedRoomId == sRoomId){
                                mCallbackChatRoom.recvUpdate();
                            }
                        }


                    }

                }else if(sMsgType == 4){

                    db.deleteChatRoomMemberList(sRoomId,sFriendId);
                    db.insertChatMessageList(userId,sRoomId,sFriendId,sMsgContent,sTime,sMsgType);
                    if(mBoundState.boundCheckChatRoom){
                        if(mBoundState.boundedRoomId == sRoomId){
                            mCallbackChatRoom.sendExitMark(sFriendId,sMsgContent,sTime);
                        }
                    }
                }else if(sMsgType ==5) {
                    if(db.haveChatRoom(sRoomId,sFriendId)) {
                        try {
                            JSONObject jobject = new JSONObject(sMsgContent);
                            String inviteId = jobject.getString("inviteId");
                            String realContent = jobject.getString("msgContent");
                            getInviteFriendThread gift = new getInviteFriendThread(sRoomId, inviteId, realContent, sTime);
                            gift.start();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            JSONObject jobject = new JSONObject(sMsgContent);
                            String inviteId = jobject.getString("inviteId");
                            String realContent = jobject.getString("msgContent");
                            getInviteGroupThread gigt = new getInviteGroupThread(sRoomId, sFriendId, realContent, sTime);
                            gigt.start();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else if(sMsgType == 10){
                    if(mBoundState.boundCheckChatRoom) {
                        if(sRoomId == 0 ) {
                            if(mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                                mCallbackChatRoom.receivePath(sMsgContent);
                            }
                        }else{
                            if(mBoundState.boundedRoomId == sRoomId) {
                                mCallbackChatRoom.receivePath(sMsgContent);
                            }
                        }

                    }
                }else if(sMsgType == 11){
                    if(mBoundState.boundCheckChatRoom) {
                        if(sRoomId == 0 ) {
                            if(mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                                mCallbackChatRoom.receiveClear();
                            }
                        }else{
                            if(mBoundState.boundedRoomId == sRoomId) {
                                mCallbackChatRoom.receiveClear();
                            }
                        }

                    }
                }else if(sMsgType == 88){

                    if(mBoundState.boundCheckChatRoom) {


                        if(sRoomId == 0 ) {
                            if(mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                                mCallbackChatRoom.receiveDrawChat(sFriendId,sMsgContent);
                            }
                        }else{
                            if(mBoundState.boundedRoomId == sRoomId) {
                                mCallbackChatRoom.receiveDrawChat(sFriendId,sMsgContent);
                            }
                        }


                    }
                }

            }
    }



    public class getInviteGroupThread extends Thread{

        public int sRoomId;
        public String sFriendId;
        public String sMsgContent;
        public long sTime;

        public getInviteGroupThread(int roomId,String friendId,String msgContent,long time){

            this.sRoomId = roomId;
            this.sFriendId = friendId;
            this.sMsgContent = msgContent;
            this.sTime = time;

        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&roomId="+this.sRoomId;

            try {
                /* 서버연결 */
                URL url = new URL(ServerURL+"/getGroup.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();


                /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.e("getIGT.data",data);
                JSONArray chatArray = new JSONArray(data);
                db.insertChatMessageList(userId,sRoomId,sFriendId,sMsgContent, sTime, 5);
                db.insertChatRoomMemberListMultiple(data,userId);
                db.insertChatRoomList(sRoomId,"group",0,"",2);
                if( mBoundState.boundCheckMain == true){
                    mCallbackMain.changeRoomList();
                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
                Log.d("getGroupInvite","JSONException");
            }
        }




    }



    public class getFriendThread extends Thread{


        public String sFriendId;
        public long sTime;


        public getFriendThread(String friendId, long time){

            this.sFriendId = friendId;
            this.sTime = time;
        }

        @Override
        public void run() {
            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "friendId=" + this.sFriendId +"&userId=" + userId + "&time=" + sTime;
            try {
                /* 서버연결 */
                URL url = new URL(ServerURL+"/getFriend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();


                /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.d("getFriendThread",data);
                try {
                    JSONObject jobj = new JSONObject(data);
                    String friendId = jobj.getString("friendId");
                    String nickname = jobj.getString("nickname");
                    String profileMessage = jobj.getString("profileMessage");
                    String profileImageUpdateTime = jobj.getString("profileImageUpdateTime");

                    db.insertChatRoomMemberList(0,friendId,nickname,profileMessage,profileImageUpdateTime,sTime);
                    db.insertChatRoomList(0,friendId,0,"",1);

                    if(mBoundState.boundCheckMain == true){
                        mCallbackMain.changeRoomList();
                        mCallbackMain.recvData();
                    }


                }catch (JSONException e){
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class getGroupThread extends Thread{

        public int sRoomId;
        public String sFriendId;
        public String sMsgContent;
        public long sTime;
        public int sMsgType;

        public getGroupThread(int roomId,String friendId,String msgContent,long time,int msgType){

            this.sRoomId = roomId;
            this.sFriendId = friendId;
            this.sMsgContent = msgContent;
            this.sTime = time;
            this.sMsgType = msgType;

        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&roomId="+this.sRoomId;

            try {
                /* 서버연결 */
                URL url = new URL(ServerURL+"/getGroup.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();


                /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.d("getGroupThread.data",data);
                db.insertChatRoomMemberListMultiple(data, userId);
                db.insertChatRoomList(sRoomId,"group",0,"",2);
                if(mBoundState.boundCheckMain == true){
                    mCallbackMain.changeRoomList();
                    mCallbackMain.recvData();
                }


                if(mBoundState.boundCheckChatRoom == true) {
                    if(mBoundState.boundedRoomId == sRoomId ) {
                        mCallbackChatRoom.recvData(sFriendId, sMsgContent, sTime, sMsgType);
                    }else{
//                        if(sMsgType == 55) {
//                            NotificationAlarm(sFriendId, 0, "#없음", "<사진>");
//                        }else{
//                            NotificationAlarm(sFriendId, 0, "#없음", sContent);
//                        }
                    }
                }else{
//                    if(sKind == 55) {
////                        NotificationAlarm(sFriendId, 0, "#없음", "<사진>");
////                    }else{
////                        NotificationAlarm(sFriendId, 0, "#없음", sContent);
////                    }
                }




            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d("getGroup","MalformedURLException");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("getGroup","IOException");
            }
        }


        public int returnRoomId(){
            return this.sRoomId;
        }


    }

    public class getRoomIdThread extends Thread{

        public String sFriendId;
        public int sRoomId;
        public long sTime;

        public getRoomIdThread(int roomId,String friendId,long time){

            this.sRoomId = roomId;
            this.sFriendId = friendId;
            this.sTime = time;

        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&friendId="+this.sFriendId+"&time="+this.sTime;
            Log.d("위치확인2",sRoomId+"#"+sFriendId+"#"+sTime);

            try {
                /* 서버연결 */
                URL url = new URL(ServerURL+"/getRoomId.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();


                /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.d("위치확인3",sRoomId+"#"+sFriendId+"#"+sTime);
                Log.d("getRoomIdThread.data",data);
                this.sRoomId = Integer.parseInt(data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public int returnRoomId(){
            return this.sRoomId;
        }


    }

    public class getInviteFriendThread extends Thread{


        public int sRoomId;
        public String sFriendId;
        public String sMsgContent;
        public long sTime;


        public getInviteFriendThread(int roomId, String friendId, String msgContent, long time){

            this.sRoomId = roomId;
            this.sFriendId = friendId;
            this.sMsgContent = msgContent;
            this.sTime = time;
            Log.d("getInviteFriend",roomId+"#"+friendId+"#"+msgContent+"#"+time);

        }

        @Override
        public void run() {
            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "friendId=" + this.sFriendId + "&roomId=" + this.sRoomId + "&time=" + this.sTime ;
            try {
                /* 서버연결 */
                URL url = new URL(ServerURL+"/getInviteFriend.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라메터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();


                /* 서버 -> 안드로이드 파라메터값 전달 */
                InputStream is = null;
                BufferedReader in = null;

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ( ( line = in.readLine() ) != null )
                {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();
                Log.e("getInviteFriendData",data);


                db.insertChatRoomMemberListMultiple(data,userId);


                Log.d("db.ICFDM",userId+"###"+sFriendId);

                db.insertChatMessageList(userId,sRoomId,userId,sMsgContent,sTime,5);

                if(mBoundState.boundCheckChatRoom) {
                    if(mBoundState.boundedRoomId == sRoomId) {
                        mCallbackChatRoom.sendInviteMark(sMsgContent,sTime,true);
                    }
                }



            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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


}
