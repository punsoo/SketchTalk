package messenger_project.sketchtalk.chatservice;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import messenger_project.sketchtalk.MyDatabaseOpenHelper;

public class getInviteFriendThread extends Thread {

    String serverURL;
    String userId;
    int sRoomId;
    String sFriendId;
    String sMsgContent;
    long sTime;
    MyDatabaseOpenHelper db;
    boundState mBoundState;
    CallbackChatRoom mCallbackChatRoom;


    public getInviteFriendThread(String serverURL, String userId, int sRoomId, String sFriendId, String sMsgContent, long sTime, MyDatabaseOpenHelper db, boundState mBoundState, CallbackChatRoom mCallbackChatRoom) {
        this.serverURL = serverURL;
        this.userId = userId;
        this.sRoomId = sRoomId;
        this.sFriendId = sFriendId;
        this.sMsgContent = sMsgContent;
        this.sTime = sTime;
        this.db = db;
        this.mBoundState = mBoundState;
        this.mCallbackChatRoom = mCallbackChatRoom;
    }

    @Override
    public void run() {
        String data="";

        /* 인풋 파라메터값 생성 */
        String param = "friendId=" + this.sFriendId + "&roomId=" + this.sRoomId + "&time=" + this.sTime ;
        try {
            /* 서버연결 */
            URL url = new URL(serverURL+"/getInviteFriend.php");
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

            db.insertChatMessageList("",sRoomId,"",sMsgContent,sTime,5);

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
