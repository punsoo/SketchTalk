package messenger_project.catchmindtalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class MyDatabaseOpenHelper extends SQLiteOpenHelper
{

    SQLiteDatabase dbReader;
    SQLiteDatabase dbWriter;
    public SharedPreferences mPref;


    public MyDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        dbReader = getReadableDatabase();
        dbWriter = getWritableDatabase();
        mPref = context.getSharedPreferences("login",MODE_PRIVATE);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public void createFriendList(){

        String sql = "CREATE TABLE IF NOT EXISTS friendList(friendId TEXT NOT NULL PRIMARY KEY,nickname TEXT NOT NULL,profileMessage TEXT,profileImageUpdateTime TEXT, bookmark INTEGER);";
        try
        {
            dbWriter.execSQL(sql);
        }
        catch (SQLException e)
        {
        }

    }

    public void insertFriendData(String friendId, String nickname, String profileMessage,String profileImageUpdateTime, int bookmark) {

        Log.d("db.insertIFD",friendId+" | "+nickname+" | "+profileMessage+" | "+profileImageUpdateTime+" | "+bookmark);
        dbWriter.beginTransaction();
        String sql="INSERT INTO chatFriendList VALUES('"+friendId+"','"+nickname+"','"+profileMessage+"','"+profileImageUpdateTime+"','"+bookmark+"');";
        try
        {
            dbWriter.execSQL(sql);
            dbWriter.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbWriter.endTransaction();
        }


    }

    public void clearFriendList(){
        dbWriter.beginTransaction();
        String sql="DROP TABLE IF EXISTS friendList;";
        try
        {
            dbWriter.execSQL(sql);
            dbWriter.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbWriter.endTransaction();
        }
    }





    public void createChatRoomList(){

        String sql_del="DROP TABLE IF EXISTS chatRoomList;";
        String sql = "CREATE TABLE IF NOT EXISTS chatRoomList(roomId TEXT NOT NULL,PRIMARY KEY (roomId));";
        try {
            dbWriter.execSQL(sql_del);
            dbWriter.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCRL","noUserId");
        }

    }



    public void insertChatRoomData(String roomId) {

        Log.d("db.insertIFD",roomId);
        dbWriter.beginTransaction();
        String sql="INSERT INTO chatFriendList VALUES('"+roomId+"');";
        try
        {
            dbWriter.execSQL(sql);
            dbWriter.setTransactionSuccessful();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            dbWriter.endTransaction();
        }


    }




    public void createChatMessageList(String userId){


        String sql_del="DROP TABLE IF EXISTS chatMessageList_"+userId+";";
        String sql = "CREATE TABLE IF NOT EXISTS chatMessageData_"+userId+"(friendId TEXT NOT NULL,messageContent TEXT,messageTime INTEGER,type INTEGER,PRIMARY KEY(friendId))";
        try {
            dbWriter.execSQL(sql_del);
            dbWriter.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCFL",sql);
        }

    }



}

