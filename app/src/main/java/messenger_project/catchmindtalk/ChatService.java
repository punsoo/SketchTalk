package messenger_project.catchmindtalk;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

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
import java.util.ArrayList;

public class ChatService extends Service {

        Handler handler;
        Socket socket;
        public SharedPreferences mPref;
        public SharedPreferences.Editor editor;
        String userId;
        public MyDatabaseOpenHelper db;
        public boolean connectable;
        String ServerAddress = "54.180.196.239";
        String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";

        static final int PostConnect = 3333;
        static final int ConnectThread = 5555;

        public class ChatServiceBinder extends Binder {
            public ChatService getService() {
                return ChatService.this; //현재 서비스를 반환.
            }
        }

        @Override
        public void onCreate() {

            super.onCreate();

            Log.d("ChatServiceOnCreate","크리에이트");

            connectable = true;

            mPref = getSharedPreferences("login",MODE_PRIVATE);
            editor = mPref.edit();

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


            editor.putBoolean("Service",true);
            editor.commit();

            db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);

            mPref = getSharedPreferences("login",MODE_PRIVATE);
            editor = mPref.edit();
            userId = mPref.getString("userId","아이디없음");

            Log.d("ChatServiceOnStart",userId);

            if( socket == null ) {

                ConnectThread ct = new ConnectThread();
                ct.start();

            }else{

                if( socket.isClosed() || !socket.isConnected() ){
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

        private final IBinder mBinder = new ChatServiceBinder();

        @Override
        public IBinder onBind(Intent intent) {
            // TODO: Return the communication channel to the service.
            //throw new UnsupportedOperationException("Not yet implemented");
            return mBinder;
        }

        //콜백 인터페이스 선언
        public interface ICallback {
            public void recvData(String friendId,String msgContent,long time); //액티비티에서 선언한 콜백 함수.
            public void changeRoomId(int roomId);
            public void sendMessageMark(String msgContent,long time);
            public void sendInviteMark(String inviteId,String msgContent,long time,boolean resetMemberList);
            public void sendExitMark(String friendId,String msgContent,long time);
            public void sendImageMark(String friendId,String msgContent, long time , int kind);
            public void resetHash();
            public void recvUpdate();
            public String getFriendId();
            public void resetToolbar();
            public void receivePath(String PATH);
            public void receiveClear();
            public void receiveDrawChat(String friendId,String msgContent);

        }

        public interface ICallback_2{
            public void recvData(); //액티비티에서 선언한 콜백 함수.
            public void changeRoomList();
        }

        private ICallback mCallback;
        private ICallback_2 mCallback_2;

        //액티비티에서 콜백 함수를 등록하기 위함.
        public void registerCallback(ICallback cb) {
            mCallback = cb;
        }

        public void registerCallback_2(ICallback_2 cb_2) {
            mCallback_2 = cb_2;
        }



        //액티비티에서 메세지 전송
        public void sendMessage(int roomId,String friendId, String msgContent, long time){

            if(roomId ==0 && !db.haveChatRoom(friendId)){

                getFriendThread gft = new getFriendThread(friendId,time);
                gft.start();
            }

            SendThread st = new SendThread(socket, roomId, friendId, msgContent, time , 1);
            st.start();

            try {
                st.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                st.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            if(st.isSuccess()){

                Log.d("st.isSucess",roomId+"#"+friendId+"#"+msgContent+"#"+time);
                if(roomId==0) {
                    db.insertChatMessageList(userId, roomId, friendId, msgContent, time, 1);
                }else{
                    db.insertChatMessageList(userId, roomId, userId, msgContent, time, 1);
                }


                mCallback.sendMessageMark(msgContent,time);

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

            String dstAddress;
            int dstPort;
            SharedPreferences tPref;

            public ConnectThread(){
                this.dstAddress = ServerAddress;
                this.dstPort = 5000;
                this.tPref = getSharedPreferences("login",MODE_PRIVATE);

                Log.d("ServiceConnectThread생성자",this.dstAddress+"##"+this.dstPort+"##"+userId);
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

                    socket = new Socket(dstAddress, dstPort);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {


                    if(tPref.getBoolean("Service",false)) {

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


        public class SendThread extends Thread {

            String msgContent;
            int roomId;
            String friendId;
            long time;
            boolean success;
            int msgType;
            String sendObj;

            OutputStream sender ;

            DataOutputStream output;

            public SendThread(Socket threadSocket,int roomId, String friendId, String msgContent,long time,int msgType) {

                this.roomId = roomId;
                this.friendId = friendId;
                this.msgContent = msgContent;
                this.time = time;
                this.msgType = msgType;
                this.success = true;
                this.sendObj = "default";


                try {
                    this.sender = threadSocket.getOutputStream();
                    this.output = new DataOutputStream(sender);
                }catch (IOException e){
                    e.printStackTrace();
                }

                Log.d("SendThread.Socket: ",threadSocket.toString());
                Log.d("SendThread내용",roomId + "###" + friendId + "###" + msgContent + "###" + time + "###" +msgType);

            }



            @Override
            public void run() {
                Log.d("sendThreadId",friendId);


                try {

                    JSONObject obj = new JSONObject();

                    obj.put("userId", userId);
                    obj.put("roomId", this.roomId);
                    obj.put("friendId", friendId);
                    obj.put("msgContent", this.msgContent);
                    obj.put("time", time);
                    obj.put("msgType", msgType);


                    this.sendObj = obj.toString();

                    output.writeUTF(sendObj);

                    Log.d("SendThread.Output: ",output.toString());


                }catch (IOException e){
                    Log.d("SendThreadIOException",this.sendObj);
                    e.printStackTrace();
                }catch (JSONException e){
                    Log.d("SendThreadJSONException",this.sendObj);
                    e.printStackTrace();
                }catch (NullPointerException e){
                    Log.d("SendThreadNullException",this.sendObj);
                    e.printStackTrace();
                }


            }

            public boolean isSuccess(){
                return this.success;
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

    public void loseReceive(){

        Log.d("ChatService","loseReceive");

        if(!socket.isClosed()) {
            Log.d("ChatService","loseReceive해제메시지");
            SendThread st = new SendThread(socket,0,"해제자","해체",0,33 );
            st.start();
            try {
                st.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }

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

                if(sMsgType == 1){

                    db.insertChatMessageList(userId,sRoomId,sFriendId,sMsgContent,sTime,sMsgType);

                    if(sRoomId ==0 && !db.haveChatRoom(sFriendId)){
                        getFriendThread gft = new getFriendThread(sFriendId,sTime);
                        gft.start();
                    }
                    mCallback.recvData(sFriendId,sMsgContent,sTime);
                }

            }
    }


    public class getFriendThread extends Thread{


        public String sFriendId;
        public long sTime;

//        public long sTime;

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

                    db.insertChatRoomMemberList(0,friendId,nickname,profileMessage,profileImageUpdateTime,0);
                    db.insertChatRoomList(0,friendId,sTime,"",1);

//                    if(boundCheck_2 == true){
//                        mCallback_2.changeRoomList();
//                    }

//                    if(boundCheck_2 == true) {
//                        mCallback_2.recvData();
//                    }



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

}
