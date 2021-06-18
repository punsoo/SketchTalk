package messenger_project.sketchtalk.chatservice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SendThread extends Thread {

    String userId;
    int roomId;
    String friendId;
    String msgContent;
    long time;
    int msgType;
    boolean success;
    String sendObj;

    OutputStream sender ;

    DataOutputStream output;

    public SendThread(Socket threadSocket, String userId, int roomId, String friendId, String msgContent, long time, int msgType) {

        this.userId = userId;
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
        }catch (NullPointerException e){
            e.printStackTrace();
            Log.d("소켓연결실패",e.toString());
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
