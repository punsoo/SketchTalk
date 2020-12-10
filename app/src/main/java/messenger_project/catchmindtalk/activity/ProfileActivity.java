package messenger_project.catchmindtalk.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import messenger_project.catchmindtalk.R;

/**
 * Created by sonsch94 on 2018-08-19.
 */

public class ProfileActivity extends AppCompatActivity {


    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private static final int Profile_Change = 4292;
    private static final int Nickname_Change = 3279;
    private static final int Message_Change = 9510;
    private static final String TAG = "ProfileActivity_openCV";
    private Uri mImageCaptureUri;
    private String absolutePath;
    ProgressDialog dialog = null;
    int serverResponseCode = 0;
    final String upLoadServerUri = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com/UploadToServer.php";
    String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";

    public TextView profileTitleView;
    public TextView profileMessageView;
    public Button profilebtn;
    public Button talkbtn;
    public Button videoBtn;
    public Button nicknameBtn;
    public Button messageBtn;
    public ImageView profileImageView;
    public Bitmap photo;
    public File sourceFile ;
    public FileOutputStream fOut;
    Bitmap out;

//    private ChatService mService;

    public String userId;
    public String nickname;
    String profileMessage;

    public SharedPreferences mPref;
    public SharedPreferences.Editor editor;
    public String myNickname;






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileTitleView = (TextView)findViewById(R.id.ProfileTitle);
        profileMessageView = (TextView)findViewById(R.id.ProfileMessage);
        profileImageView = (ImageView)findViewById(R.id.ProfileImage);
        profilebtn = (Button)findViewById(R.id.profilebtn);
        talkbtn = (Button)findViewById(R.id.talkbtn);
        videoBtn = (Button)findViewById(R.id.VideoCallBtn);
        nicknameBtn = (Button)findViewById(R.id.nicknamebtn);
        messageBtn = (Button)findViewById(R.id.messagebtn);
        Intent intent = getIntent();
        int position = intent.getIntExtra("position",0);
        if(position == 1){
            profilebtn.setVisibility(View.VISIBLE);
            nicknameBtn.setVisibility(View.VISIBLE);
            messageBtn.setVisibility(View.VISIBLE);
            talkbtn.setVisibility(View.GONE);
            videoBtn.setVisibility(View.GONE);
        }
        nickname = intent.getStringExtra("nickname");
        userId = intent.getStringExtra("userId");
        String profileImageUpdateTime = intent.getStringExtra("profileImageUpdateTime");
        profileTitleView.setText(nickname+"님의 프로필");
        profileMessage = intent.getStringExtra("message");
        profileMessageView.setText(profileMessage);
        if(position ==1){
            if(profileImageUpdateTime.equals("none")){
                profileImageView.setImageResource(R.drawable.default_profile_image);
            }else {
                Glide.with(this).load(ServerURL+"/profile_image/" + userId + ".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                        .into(profileImageView);
            }
        }else {
//            Toast.makeText(this,profile,Toast.LENGTH_SHORT).show();
            if(profileImageUpdateTime.equals("none")){
                profileImageView.setImageResource(R.drawable.default_profile_image);
            }else {
                Glide.with(this).load(ServerURL+"/profile_image/" + userId + ".png")
                        .error(R.drawable.default_profile_image)
                        .signature(new ObjectKey(profileImageUpdateTime))
                        .into(profileImageView);
            }
        }
        profileImageView.setBackgroundResource(R.drawable.profile_border);




        mPref = getSharedPreferences("login",MODE_PRIVATE);
        editor = mPref.edit();

        myNickname = mPref.getString("nickname","메세지없음");




//
//        Intent serviceIntent = new Intent(this, ChatService.class);
//        serviceIntent.putExtra("FromLogin",false);
//        bindService(serviceIntent, mConnection, this.BIND_AUTO_CREATE);


    }


    public void imageSend(View v){

//        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which){
//                doTakePhotoAction();
//            }
//
//        };
//
//        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener(){
//
//            @Override
//            public void onClick(DialogInterface dialog, int which){
//                doTakeAlbumAction();
//
//            }
//
//        };
//
//
//        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener(){
//
//            @Override public void onClick(DialogInterface dialog, int which){
//                dialog.dismiss();
//            }
//
//        };
//
//
//
//
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle("업로드할 이미지 선택")
//                .setPositiveButton("사진촬영", cameraListener)
//                .setNeutralButton("취소", cancelListener)
//                .setNegativeButton("앨범선택", albumListener)
//                .create();
//
//        dialog.show();
//
//        Button pbtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        pbtn.setTextColor(Color.BLACK);
//        Button neubtn = dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
//        neubtn.setTextColor(Color.BLACK);
//        Button negbtn = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//        negbtn.setTextColor(Color.BLACK);

        Intent ICintent = new Intent(this,ProfileChangeActivity.class);
        startActivityForResult(ICintent,Profile_Change);


    }

    public void doTakePhotoAction(){

        Intent intent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);

        String url = "tmp" + ".png";
//        mImageCaptureUri = Uri.fromFile ( new File(Environment.getExternalStorageDirectory(), url));
        mImageCaptureUri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName()+".provider", new File(Environment.getExternalStorageDirectory(), url));

        Log.d("사진_doTakePhoto", Environment.getExternalStorageState().toString());
        Log.d("사진_doTakePhoto", mImageCaptureUri.toString());


        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);

    }

    public void doTakeAlbumAction(){

        Intent intent = new Intent (Intent.ACTION_PICK);
//        intent.setType (MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType ("image/*");
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);


        switch ( requestCode ){

            case PICK_FROM_CAMERA: {

                if(resultCode != RESULT_OK){

                    return;
                }

                try {

                    String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String imgpath = ex_storage + "/tmp.png";
                    photo = BitmapFactory.decodeFile(imgpath);
                    profileImageView.setImageBitmap(photo);

                    Log.d("이미지경로",imgpath);

                    dialog = ProgressDialog.show(this, "", "Uploading file...", true);


                        ImageSendThread ist = new ImageSendThread(imgpath);
                        ist.start();

                        ist.join();

                        Glide.with(this).load( ServerURL + "/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));


                }catch(Exception e){
                    e.printStackTrace();
                }

//                Log.d("피곤해좆같앙",mImageCaptureUri.getPath());
//                try {
//                    String ex_storage =Environment.getExternalStorageDirectory().getAbsolutePath();
//                    String imgpath = ex_storage+"/tmp.jpg";
//
//                }catch (Exception e){
//
//                }



                break;
            }

            case PICK_FROM_ALBUM: {

                if(data == null){
                    return;
                }
                Log.d("이미지1",data.toString());
                Log.d("이미지2",getPath(data.getData()));

                if(resultCode != RESULT_OK){
                    Log.d("이미지픽프롬앨범실패",getPath(data.getData()));
                    return;
                }

//                photo = BitmapFactory.decodeFile(getPath(data.getData()));
//                profileIV.setImageBitmap(photo);

//                try {
//                    photo = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                    Log.d("이미지external",MediaStore.Images.Media.getContentUri("external").toString());
//                    profileIV.setImageBitmap(photo);
//                }catch(IOException e){
//                    e.printStackTrace();
//                }
                Log.d("이미지픽프롬앨범",getPath(data.getData()));
//                dialog = ProgressDialog.show(this, "", "Uploading file...", true);




//                Bitmap bitmap = BitmapFactory.decodeFile(getPath(data.getData()));






                    ImageSendThread ist = new ImageSendThread(getPath(data.getData()));
                    ist.start();
                    try {

                        ist.join();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Glide.with(this).load(ServerURL+"/profile_image/" + userId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
                            .into(profileImageView);

                    mImageCaptureUri = data.getData();
                    Log.d("이미지앨범onactivityresult", mImageCaptureUri.toString());


//                Intent intent = new Intent ("com.android.camera.action.CROP");
//                intent.setDataAndType(mImageCaptureUri, "image/*");
//                intent.putExtra("outputX", 200);
//                intent.putExtra("outputY", 200);
//                intent.putExtra("aspectX", 1);
//                intent.putExtra("aspectY", 1);
//                intent.putExtra("scale",true);
//                intent.putExtra("return-data", true);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
//
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//
//                startActivityForResult(intent, CROP_FROM_IMAGE);


                break;

            }


            case Profile_Change: {

                if(resultCode == RESULT_OK) {
                    String type = data.getExtras().getString("IC");

                    if (type.equals("album")) {
                        doTakeAlbumAction();
                    } else if (type.equals("camera")) {
                        doTakePhotoAction();
                    } else {
                        profileImageView.setImageResource(R.drawable.default_profile_image);
                        ImageDefaultThread idt = new ImageDefaultThread(userId);
                        idt.start();
                    }
                }

                break;
            }


            case Nickname_Change: {
                if(resultCode == RESULT_OK) {
                    String nick = data.getExtras().getString("nickname");
                    profileTitleView.setText(nick+"님의 프로필");

                    editor.putString("nickname",nick);
                    editor.commit();

                    ChangeNicknameThread cnt = new ChangeNicknameThread(nick);
                    cnt.start();

                }
                break;
            }


            case Message_Change: {

                if(resultCode == RESULT_OK) {
                    String msg = data.getExtras().getString("profileMessage");
                    profileMessageView.setText(msg);

                    editor.putString("profileMessage",msg);
                    editor.commit();

                    ChangeMessageThread cmt = new ChangeMessageThread(msg);
                    cmt.start();
                }
                break;
            }

//            case CROP_FROM_IMAGE: {
//
//                if ( resultCode != RESULT_OK){
//                    return;
//                }
//
//                final Bundle extras = data.getExtras();
//
//                if (extras != null){
//
//                    photo = extras.getParcelable("data");
//                    profileIV.setImageBitmap(photo);
//                    dialog = ProgressDialog.show(this, "", "Uploading file...", true);
//                    ImageSendThread ist = new ImageSendThread(getPath(data.getData()));
//                    ist.start();
//
//                }
//
//                break;
//
//            }


        }




    }


    public String getPath(Uri uri) {

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }


    public int uploadFile(String sourceFileUri) {

        Log.d("이미지업로딩시작",sourceFileUri);
        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
//        File sourceFile = new File(sourceFileUri);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tmp.png";

        Bitmap b= BitmapFactory.decodeFile(sourceFileUri);
        Bitmap out = Bitmap.createScaledBitmap(b, 400, 400, false);


        sourceFile = new File(path);

        try {
            Log.d("이미지새파일경로1",sourceFile.getAbsolutePath());
            fOut = new FileOutputStream(sourceFile);
            Log.d("이미지새파일경로2",sourceFile.getAbsolutePath());
            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//            Log.d("이미지새파일경로3",sourceFile.getAbsolutePath());
//            fOut.flush();
//            Log.d("이미지새파일경로4",sourceFile.getAbsolutePath());
//            fOut.close();
            Log.d("이미지새파일경로5",sourceFile.getAbsolutePath());

        } catch (Exception e) {}



        if (!sourceFile.isFile()) {

            Log.d("이미지경로에없음","경로에없나?");
//            dialog.dismiss();

            return 0;

        }else{

            try {

                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + userId+".png" + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.d("이미지uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

//                if(serverResponseCode == 200){
//
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//
//
//                            Toast.makeText(UploadToServer.this, "File Upload Complete.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }


                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();


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
                String data = buff.toString().trim();
                Log.d("이미지성공실패",data);


            } catch (MalformedURLException ex) {

//                dialog.dismiss();
                ex.printStackTrace();

//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        messageText.setText("MalformedURLException Exception : check script url.");
//                        Toast.makeText(UploadToServer.this, "MalformedURLException",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

//                dialog.dismiss();
                e.printStackTrace();

//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        messageText.setText("Got Exception : see logcat ");
//                        Toast.makeText(UploadToServer.this, "Got Exception : see logcat ",
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//                Log.e("Upload file to server Exception", "Exception : "
//                        + e.getMessage(), e);
            }

//            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }


    public class ChangeNicknameThread extends Thread {

        String sNickname;

        public ChangeNicknameThread (String Nickname){
            this.sNickname = Nickname;
        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&nickname="+this.sNickname;

            try {
                /* 서버연결 */
                URL url = new URL(ServerURL + "/changeNickname.php");
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
                Log.e("acft.data",data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }


    public class ChangeMessageThread extends Thread {


        String sMessage;

        public ChangeMessageThread (String Message){
            this.sMessage = Message;
        }

        @Override
        public void run() {

            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+userId+"&message="+this.sMessage;

            try {
                /* 서버연결 */
                URL url = new URL(ServerURL + "/changeMessage.php");
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
                Log.e("acft.data",data);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


    }



    public class ImageSendThread extends Thread {

        public String filePath;

        public ImageSendThread (String uri){
            this.filePath = uri;
        }

        @Override
        public void run() {

            uploadFile(filePath);

        }


    }

    public class ImageDefaultThread extends Thread {

        String sUserId;

        public ImageDefaultThread (String userId){
            this.sUserId = userId;
        }

        @Override
        public void run() {
            String data="";

            /* 인풋 파라메터값 생성 */
            String param = "userId="+ sUserId;
            try {
                /* 서버연결 */
                URL url = new URL(ServerURL + "/defaultImage.php");
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
                Log.d("디폴트스레드결과",data.toString());


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void talk(View v){

        Intent intent = new Intent();
        intent.putExtra("friendId",userId);
        intent.putExtra("nickname",nickname);
        intent.putExtra("no",0);
        setResult(RESULT_OK, intent);
        finish();

    }

    public void messageChange(View v){
        Intent intent = new Intent(this,ChangeMessageActivity.class);
        intent.putExtra("profileMessage",profileMessage);
        startActivityForResult(intent, Message_Change);
    }

    public void nicknameChange(View v){
        Intent intent = new Intent(this,ChangeNicknameActivity.class);
        intent.putExtra("nickname",nickname);
        startActivityForResult(intent, Nickname_Change);
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(mConnection);
    }




}
