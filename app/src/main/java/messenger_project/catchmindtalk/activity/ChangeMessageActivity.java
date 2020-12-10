package messenger_project.catchmindtalk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.view.Window;
import android.widget.EditText;

import androidx.annotation.Nullable;
import messenger_project.catchmindtalk.R;


/**
 * Created by sonsch94 on 2018-08-20.
 */

public class ChangeMessageActivity extends Activity {

    public EditText editMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_change_message);
        editMessage = (EditText)findViewById(R.id.editMessage);
        Intent intent = getIntent();
        editMessage.setText(intent.getExtras().getString("message"));
    }




    public void okChange(View v){

        Intent resultIntent = new Intent();

        resultIntent.putExtra("message",editMessage.getText().toString());

        setResult(RESULT_OK,resultIntent);

        finish();


    }


    public void cancelChange(View v){

        finish();

    }




}