package messenger_project.catchmindtalk.chatservice;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;


import messenger_project.catchmindtalk.MyDatabaseOpenHelper;

import static messenger_project.catchmindtalk.chatservice.ChatService.ConnectThread;

public class ReceiveThread extends Thread {

    Socket socket;
    String serverURL;
    String userId;
    InputStream receiver;
    DataInputStream input;
    MyDatabaseOpenHelper db;
    boundState mBoundState;
    CallbackChatRoom mCallbackChatRoom;
    CallbackMain mCallbackMain;
    Handler handler;

    public ReceiveThread(Socket socket, String serverURL, String userId, MyDatabaseOpenHelper db, boundState mBoundState, CallbackChatRoom mCallbackChatRoom, CallbackMain mCallbackMain, Handler handler) {
        this.socket = socket;
        this.serverURL = serverURL;
        this.userId = userId;
        this.db = db;
        this.mBoundState = mBoundState;
        this.mCallbackChatRoom = mCallbackChatRoom;
        this.mCallbackMain = mCallbackMain;
        this.handler = handler;

        try{
            this.receiver = socket.getInputStream();
            this.input = new DataInputStream(receiver);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String response = "";
        while(true) {

//            if(!userId.equals(checkId)){
//                Log.d("ChatServiceCheckId","checkId: "+checkId+", userId: "+userId);
//                if(socket != null) {
//                    try {
//                        socket.close();
//                    }catch(IOException e){
//                        e.printStackTrace();
//                        Reconnect();
//                    }
//                }
//                break;
//            }

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

                    ReceiveMessageThread rmt = new ReceiveMessageThread(socket,serverURL,userId,roomId,friendId,msgContent,time,msgType,db,mBoundState,mCallbackChatRoom,mCallbackMain);
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
}
