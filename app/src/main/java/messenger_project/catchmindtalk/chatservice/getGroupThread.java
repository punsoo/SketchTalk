package messenger_project.catchmindtalk.chatservice;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import messenger_project.catchmindtalk.MyDatabaseOpenHelper;

public class getGroupThread extends Thread {
    String serverURL;
    String userId;
    public int sRoomId;
    public String sFriendId;
    public String sMsgContent;
    public long sTime;
    public int sMsgType;
    MyDatabaseOpenHelper db;
    boundState mBoundState;
    CallbackMain mCallbackMain;
    CallbackChatRoom mCallbackChatRoom;

    public getGroupThread(String serverURL, String userId, int sRoomId, String sFriendId, String sMsgContent, long sTime, int sMsgType, MyDatabaseOpenHelper db, boundState mBoundState, CallbackMain mCallbackMain, CallbackChatRoom mCallbackChatRoom) {
        this.serverURL = serverURL;
        this.userId = userId;
        this.sRoomId = sRoomId;
        this.sFriendId = sFriendId;
        this.sMsgContent = sMsgContent;
        this.sTime = sTime;
        this.sMsgType = sMsgType;
        this.db = db;
        this.mBoundState = mBoundState;
        this.mCallbackMain = mCallbackMain;
        this.mCallbackChatRoom = mCallbackChatRoom;
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
            Log.d("getGroupThread.data",data);
            db.insertChatRoomMemberListMultiple(data, userId);
            db.insertChatRoomList(sRoomId,"group",0,"",2);
            if(mBoundState.boundCheckMain == true){
                mCallbackMain.changeRoomList();
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
