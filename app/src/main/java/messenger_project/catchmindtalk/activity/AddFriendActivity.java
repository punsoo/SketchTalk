package messenger_project.catchmindtalk.activity;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

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
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;

public class AddFriendActivity extends AppCompatActivity {

    Toolbar toolbar;

    Button idAddbtn;
    Button nicknameAddbtn;

    FrameLayout idFL;
    FrameLayout nicknameFL;
    String idData;
    String nicknameData;
    String idId;
    String nicknameId;
    EditText idEdit;
    EditText nicknameEdit;
    View noDataId,noDataNickname;
    TextView noDataTVId,noDataTVNickname;

    View friendViewId,friendViewNickname;

    ImageView profileViewId,profileViewNickname;
    TextView nameViewId,nameViewNickname;
    TextView messageViewId,messageViewNickname;
    LinearLayout sectionLinearId,sectionLinearNickname;
    public MyDatabaseOpenHelper db;
    public SharedPreferences mPref;
    String myId;
    ArrayList<String> friendList = new ArrayList<String>();

    String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        toolbar = (Toolbar) findViewById(R.id.toolbarAddFriend);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("친구추가");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);
        mPref = getSharedPreferences("login",MODE_PRIVATE);
        myId = mPref.getString("userId","아이디없음");

        Cursor cursor = db.getFriendList();

        while(cursor.moveToNext()) {

            friendList.add(cursor.getString(0));

        }


        idEdit = (EditText) findViewById(R.id.idfindedit);
        nicknameEdit = (EditText) findViewById(R.id.nicknamefindedit);

        noDataId = getLayoutInflater().inflate(R.layout.nodata,null);
        noDataNickname = getLayoutInflater().inflate(R.layout.nodata,null);

        noDataTVId = (TextView)noDataTVId.findViewById(R.id.nodatatxt);
        noDataTVNickname = (TextView)noDataTVNickname.findViewById(R.id.nodatatxt);

        friendViewId = getLayoutInflater().inflate(R.layout.friendlist_item,null);
        friendViewNickname = getLayoutInflater().inflate(R.layout.friendlist_item,null);

        profileViewId = (ImageView) friendViewId.findViewById(R.id.profileImage);
        nameViewId = (TextView) friendViewId.findViewById(R.id.nickname);
        messageViewId = (TextView) friendViewId.findViewById(R.id.profileMessage);
        sectionLinearId = (LinearLayout) friendViewId.findViewById(R.id.sectionHeader);
        sectionLinearId.setVisibility(View.GONE);

        profileViewNickname = (ImageView) friendViewNickname.findViewById(R.id.profileImage);
        nameViewNickname = (TextView) friendViewNickname.findViewById(R.id.nickname);
        messageViewNickname = (TextView) friendViewNickname.findViewById(R.id.profileMessage);
        sectionLinearNickname = (LinearLayout) friendViewNickname.findViewById(R.id.sectionHeader);
        sectionLinearNickname.setVisibility(View.GONE);

        idAddbtn = (Button) findViewById(R.id.idaddbtn);
        nicknameAddbtn = (Button) findViewById(R.id.nicknameaddbtn);

        idFL = (FrameLayout) findViewById(R.id.idFL);
        nicknameFL = (FrameLayout) findViewById(R.id.nicknameFL);




    }


    public void idSearch(View v){

        FindThread ft = new FindThread(idEdit.getText().toString(),"id");
        ft.start();

        try{
            ft.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        idFL.removeAllViewsInLayout();

        if(idData.equals("아이디")){
            noDataTVId.setText("검색결과가 없습니다");
            idFL.addView(noDataTVId);
            idAddbtn.setVisibility(View.INVISIBLE);

        }else{


            try {
                JSONObject jobject = new JSONObject(idData);

                String userId = jobject.getString("friendId");
                String nickname = jobject.getString("nickname");
                String profileMessage = jobject.getString("profileMessage");
                String profileImageUpateTime = jobject.getString("profileImageUpdateTime");

                if(friendList.contains(userId)){
                    noDataTVId.setText("이미 친구입니다");
                    idFL.addView(noDataTVId);
                    idAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                if(userId.equals(myId)){
                    noDataTVId.setText("본인아이디입니다");
                    idFL.addView(noDataTVId);
                    idAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                nameViewId.setText(nickname);
                messageViewId.setText(profileMessage);

                Glide.with(this).load(ServerURL + "/profile_image/"+userId+".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new ObjectKey(String.valueOf(profileImageUpateTime)))
                        .into(profileViewId);

                idFL.addView(friendViewId);
                idAddbtn.setVisibility(View.VISIBLE);

            }catch (JSONException e){
                e.printStackTrace();
            }

        }

    }

    public void idAddBtn(View v){

        try {
            JSONObject jobject = new JSONObject(idData);

            String userId = jobject.getString("friendId");
            String nickname = jobject.getString("nickname");
            String profileMessage = jobject.getString("profileMessage");
            String profileImageUpdateTime = jobject.getString("profileImageUpdateTime");

            if(friendList.contains(userId)){
                idFL.removeAllViewsInLayout();
                noDataTVId.setText("이미 친구입니다");
                idFL.addView(noDataTVId);
                idAddbtn.setVisibility(View.INVISIBLE);
                return;
            }

            AddThread at = new AddThread(userId);
            at.start();

            db.insertFriendList(userId, nickname, profileMessage, profileImageUpdateTime, 0,0,0);
            friendList.add(userId);

            idFL.removeAllViewsInLayout();
            idAddbtn.setVisibility(View.INVISIBLE);

        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    public void nicknameSearch(View v){

        FindThread ft = new FindThread(nicknameEdit.getText().toString(),"nickname");
        ft.start();

        try{
            ft.join();
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        nicknameFL.removeAllViewsInLayout();

        if(nicknameData.equals("아이디")){
            noDataTVNickname.setText("검색결과가 없습니다");
            nicknameFL.addView(noDataTVNickname);
            nicknameAddbtn.setVisibility(View.INVISIBLE);

        }else{

            try {
                JSONObject jobject = new JSONObject(nicknameData);

                String userId = jobject.getString("friendId");
                String nickname = jobject.getString("nickname");
                String profileMessage = jobject.getString("profileMessage");
                String profileImageUpdateTime = jobject.getString("profileImageUpdateTime");

                if(friendList.contains(userId)){
                    noDataTVNickname.setText("이미 친구입니다");
                    nicknameFL.addView(noDataTVNickname);
                    nicknameAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                if(userId.equals(myId)){
                    noDataTVNickname.setText("본인닉네임입니다");
                    nicknameFL.addView(noDataTVNickname);
                    nicknameAddbtn.setVisibility(View.INVISIBLE);
                    return;
                }

                nameViewNickname.setText(nickname);
                messageViewNickname.setText(profileMessage);



                Glide.with(this).load(ServerURL + "/profile_image/"+userId+".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                        .into(profileViewNickname);

                nicknameFL.addView(friendViewNickname);
                nicknameAddbtn.setVisibility(View.VISIBLE);

            }catch (JSONException e){
                e.printStackTrace();
            }

        }

    }

    public void nickAddBtn(View v){
        try {
            JSONObject jobject = new JSONObject(nicknameData);

            String userId = jobject.getString("friendId");
            String nickname = jobject.getString("nickname");
            String profileMessage = jobject.getString("profileMessage");
            String profileImageUpdateTime = jobject.getString("profileImageUpdateTime");

            if(friendList.contains(userId)){
                nicknameFL.removeAllViewsInLayout();
                noDataTVNickname.setText("이미 친구입니다");
                nicknameFL.addView(noDataTVNickname);
                nicknameAddbtn.setVisibility(View.INVISIBLE);
                return;
            }

            AddThread at = new AddThread(userId);
            at.start();

            db.insertFriendList(userId, nickname, profileMessage, profileImageUpdateTime, 0,0,0);
            friendList.add(userId);

            nicknameFL.removeAllViewsInLayout();
            nicknameAddbtn.setVisibility(View.INVISIBLE);

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public class FindThread extends Thread {

        String sdata;
        String sMode;

        public FindThread(String userData,String mode) {
            this.sdata = userData;
            this.sMode = mode;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userData="+ sdata + "&mode=" +sMode +"";
            try {
                /* 서버연결 */
                URL url = new URL(ServerURL + "/findFriend.php");
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
                Log.d("FindThread Result",data.toString());

                if(sMode.equals("id")) {
                    idData = data;
                }else{
                    nicknameData = data;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public class AddThread extends Thread {

        String sUserId;

        public AddThread(String userId) {
            this.sUserId = userId;
        }


        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+ myId + "&friendId=" + sUserId ;

            try {
                /* 서버연결 */
                URL url = new URL(ServerURL + "/addFriend.php");
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
                Log.d("AddThread Result",data.toString());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }





    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}
