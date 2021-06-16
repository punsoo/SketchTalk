package messenger_project.catchmindtalk.chatservice;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import messenger_project.catchmindtalk.MyDatabaseOpenHelper;

public class getInviteGroupThread extends Thread {
    String serverURL;
    String userId;
    public int sRoomId;
    public String sFriendId;
    public String sMsgContent;
    public long sTime;
    MyDatabaseOpenHelper db;
    boundState mBoundState;
    CallbackMain mCallbackMain;

    public getInviteGroupThread(String serverURL, String userId, int sRoomId, String sFriendId, String sMsgContent, long sTime, MyDatabaseOpenHelper db, boundState mBoundState, CallbackMain mCallbackMain) {
        this.serverURL = serverURL;
        this.userId = userId;
        this.sRoomId = sRoomId;
        this.sFriendId = sFriendId;
        this.sMsgContent = sMsgContent;
        this.sTime = sTime;
        this.db = db;
        this.mBoundState = mBoundState;
        this.mCallbackMain = mCallbackMain;
    }

    @Override
    public void run() {

        String data="";

        /* 인풋 파라메터값 생성 */
        String param = "userId="+userId+"&roomId="+this.sRoomId;

        try {
            /* 서버연결 */
            URL url = new URL(serverURL+"/getGroup.php");
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
