package messenger_project.catchmindtalk.chatservice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import messenger_project.catchmindtalk.MyDatabaseOpenHelper;

public class getFriendThread extends Thread {

    String serverURL;
    String userId;
    String sFriendId;
    long sTime;
    MyDatabaseOpenHelper db;
    boundState mBoundState;
    CallbackMain mCallbackMain;

    public getFriendThread(String serverURL, String userId, String sFriendId, long sTime, MyDatabaseOpenHelper db, boundState mBoundState, CallbackMain mCallbackMain) {
        this.serverURL = serverURL;
        this.userId = userId;
        this.sFriendId = sFriendId;
        this.sTime = sTime;
        this.db = db;
        this.mBoundState = mBoundState;
        this.mCallbackMain = mCallbackMain;
    }

    @Override
    public void run() {
        String data="";

        /* 인풋 파라메터값 생성 */
        String param = "friendId=" + this.sFriendId +"&userId=" + userId + "&time=" + sTime;
        try {
            /* 서버연결 */
            URL url = new URL(serverURL+"/getFriend.php");
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
