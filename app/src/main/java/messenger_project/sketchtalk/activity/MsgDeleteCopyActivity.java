package messenger_project.sketchtalk.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;

public class MsgDeleteCopyActivity extends Activity {

    int roomId;
    long time;
    String friendId;
    int position;
    String myId;
    public MyDatabaseOpenHelper db;

    TextView deleteTV;
    TextView copyTV;
    TextView shareTV;

    String subType;
    String msgContent;

    boolean preMsgDelete;

    LinearLayout WholeMDC;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_msg_deletecopy);


        deleteTV = (TextView)findViewById(R.id.MessageDC_DeleteTV);
        copyTV = (TextView)findViewById(R.id.MessageDC_CopyTV);
        shareTV = (TextView)findViewById(R.id.MessageDC_ShareTV);



        WholeMDC = (LinearLayout)findViewById(R.id.wholeMDC);



        Intent intent = getIntent();


        friendId = intent.getExtras().getString("friendId");
        roomId = intent.getExtras().getInt("roomId");
        myId = intent.getExtras().getString("myId");
        time = intent.getExtras().getLong("time");
        position = intent.getExtras().getInt("position");
        subType = intent.getExtras().getString("subType");
        msgContent = intent.getExtras().getString("msgContent");
        preMsgDelete = intent.getExtras().getBoolean("preMsgDelete");

        if(!subType.equals("text")){

            copyTV.setVisibility(View.GONE);
            WholeMDC.getLayoutParams().height = (int) (150 * Resources.getSystem().getDisplayMetrics().density);

        }


        db = new MyDatabaseOpenHelper(this,"catchMindTalk",null,1);

        deleteTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                db.deleteChatMessageData(myId,roomId,friendId,time);

                Intent resultIntent = new Intent();

                resultIntent.putExtra("position",position);
                resultIntent.putExtra("type","del");
                resultIntent.putExtra("preMsgDelete",preMsgDelete);

                setResult(RESULT_OK,resultIntent);

                finish();
            }
        });


        copyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent resultIntent = new Intent();

                resultIntent.putExtra("position",position);
                resultIntent.putExtra("type","copy");
                resultIntent.putExtra("subType",subType);
                resultIntent.putExtra("msgContent",msgContent);

                setResult(RESULT_OK,resultIntent);

                finish();

            }
        });


        shareTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent resultIntent = new Intent();

                resultIntent.putExtra("position",position);
                resultIntent.putExtra("type","share");
                resultIntent.putExtra("subType",subType);
                resultIntent.putExtra("msgContent",msgContent);

                setResult(RESULT_OK,resultIntent);

                finish();


            }
        });

    }


}
