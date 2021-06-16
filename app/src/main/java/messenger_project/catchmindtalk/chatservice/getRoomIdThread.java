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

public class getRoomIdThread extends Thread {

    String serverURL;
    String userId;
    int sRoomId;
    String sFriendId;
    long sTime;

    public getRoomIdThread(String serverURL, String userId, int sRoomId, String sFriendId, long sTime) {
        this.serverURL = serverURL;
        this.userId = userId;
        this.sRoomId = sRoomId;
        this.sFriendId = sFriendId;
        this.sTime = sTime;
    }

    @Override
    public void run() {

        String data="";

        /* 인풋 파라메터값 생성 */
        String param = "userId="+userId+"&friendId="+this.sFriendId+"&time="+this.sTime;
        Log.d("위치확인2",sRoomId+"#"+sFriendId+"#"+sTime);

        try {
            /* 서버연결 */
            URL url = new URL(serverURL+"/getRoomId.php");
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
