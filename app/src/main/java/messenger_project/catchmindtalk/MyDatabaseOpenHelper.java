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

import java.util.Vector;

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

        String sql = "CREATE TABLE IF NOT EXISTS friendList(friendId TEXT NOT NULL PRIMARY KEY,nickname TEXT NOT NULL,profileMessage TEXT DEFAULT '',profileImageUpdateTime TEXT, favorite INTEGER, hiding INTEGER, blocked INTEGER);";
        try
        {
            dbWriter.execSQL(sql);
        }
        catch (SQLException e)
        {
        }

    }

    public void insertFriendList(String friendId, String nickname, String profileMessage,String profileImageUpdateTime, int favorite, int hiding, int blocked) {

        Log.d("db.insertIFD",friendId+" | "+nickname+" | "+profileMessage+" | "+profileImageUpdateTime+" | "+ favorite+" | "+ hiding+" | "+ blocked);
        dbWriter.beginTransaction();
        String sql="INSERT INTO friendList VALUES('"+friendId+"','"+nickname+"','"+profileMessage+"','"+profileImageUpdateTime+"','"+favorite+"','"+hiding+"','"+ blocked+ "')";
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

    public Cursor getFriendList(){

        String sql = "SELECT * FROM friendList";
        Cursor cursor = dbReader.rawQuery(sql,null);

        return cursor;
    }

    public Cursor getFriendData(String friendId){
        Log.d("db.getFL",friendId);
//        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM FriendList WHERE friendId='"+friendId+"'";
        Cursor cursor = dbReader.rawQuery(sql,null);

        return cursor;
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
        String sql = "CREATE TABLE IF NOT EXISTS chatRoomList(roomId Integer NOT NULL, friendId TEXT NOT NULL, lastReadTime Integer, roomName TEXT NOT NULL, roomType Integer, PRIMARY KEY (roomId, friendId));";
        try {
            dbWriter.execSQL(sql_del);
            dbWriter.execSQL(sql);
        }
        catch (SQLException e) {
            Log.d("db.exeptionCRL","noUserId");
        }

    }



    public void insertChatRoomList(int roomId, String friendId, long lastReadTime, String roomName, int roomType) {

        Log.d("db.insertICD",roomId+"");
        dbWriter.beginTransaction();
        String sql="INSERT INTO chatRoomList VALUES('"+roomId+"','"+ friendId+"','"+lastReadTime+"','"+ roomName +"','"+roomType +"');";
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

    public int getMinRoomId(){

        String sql = "SELECT * FROM chatRoomList ORDER BY roomId ASC LIMIT 1; ";
        Cursor cursor = dbReader.rawQuery(sql,null);
        int result = -1;
        while(cursor.moveToNext()){
            int tmp = cursor.getInt(0)-1;
            result = tmp < -1 ? tmp : -1;
        }
        return result;
    }

    public Cursor getChatRoomList(){

        String sql = "SELECT * FROM chatRoomList ";
        Cursor cursor = dbReader.rawQuery(sql,null);

        return cursor;
    }

    public void updateChatRoomLastReadTime(int roomId, String friendId, long time){
        dbWriter.beginTransaction();
        Log.d("확인ucrlrt",roomId+"#"+friendId+"#"+time);
        String sql;
        if(roomId==0){
            sql = "UPDATE chatRoomList SET lastReadTime='" + time + "' WHERE friendId='"+friendId+"' AND roomId='"+roomId+"';";
        }else {
            sql = "UPDATE chatRoomList SET lastReadTime='" + time + "' WHERE roomId='"+roomId+"';";
        }

        try {
            dbWriter.execSQL(sql);
            dbWriter.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbWriter.endTransaction();
        }

    }

    public Cursor getChatRoomListJoinOnMessageList(String myId){

        Log.d("db.getCRLJOML",myId);
        String userId = mPref.getString("userId", myId);


        String sql = "SELECT * FROM chatRoomList AS cr LEFT JOIN (SELECT * FROM ( SELECT roomId AS cm1roomId, friendId AS cm1friendId, MAX(messageTime) AS maxTime FROM chatMessageList_"+userId+" WHERE roomId <> 0 GROUP BY roomId UNION SELECT roomId AS cm1roomId, friendId AS cm1friendId, MAX(messageTime) AS maxTime FROM chatMessageList_"+userId+" WHERE roomId = 0 GROUP BY roomId, friendId ) AS cm1 INNER JOIN chatMessageList_"+userId+" cm2 ON cm1.cm1roomId = cm2.roomId AND cm1.cm1friendId = cm2.friendId AND cm1.maxTime = cm2.messageTime )AS cm ON CASE WHEN cr.roomId IN (0) THEN (cm.friendId = cr.friendId AND cm.roomId = cr.roomId) ELSE (cm.roomId = cr.roomId) END ORDER BY maxTime DESC";
        // roomId friendId lastReadTime(chatRoom) roomName roomType cm1roomId cm1friendId maxTime roomId friendId messageContent messageTime messageType
        Cursor cursor = dbReader.rawQuery(sql,null);

        return cursor;

//        String sql = "SELECT * FROM chatRoomList AS cr LEFT JOIN (SELECT * FROM (SELECT roomId AS cm1roomId, friendId AS cm1friendId, MAX(messageTime) AS maxTime FROM chatMessageList_"+userId+" GROUP BY roomId, friendId) cm1 INNER JOIN chatMessageList_"+userId+" cm2 ON cm1.cm1roomId = cm2.roomId AND cm1.cm1friendId = cm2.friendId AND cm1.maxTime = cm2.messageTime )AS cm ON CASE WHEN cr.roomId IN (0) THEN (cm.friendId = cr.friendId AND cm.roomId = cr.roomId) ELSE (cm.roomId = cr.roomId) END ORDER BY maxTime DESC";
//        String sql = "SELECT * FROM chatRoomList AS cr LEFT JOIN messageData_"+userId+" AS md ON md.friendId = (SELECT md1.friendId FROM messageData_"+userId+" AS md1 WHERE cr.no = md1.no AND cr.friendId = md1.friendId AND (md1.type = 1 OR md1.type = 2) ORDER BY md1.time DESC LIMIT 1)";
//        String sql = "SELECT R.*, M.messageContent, M.messageTime, M.messageType FROM ChatRoomList AS R LEFT JOIN (SELECT MAX(M.messageTime), M.roomId FROM chatMessageList_"+userId+" GROUP BY M.roomId) AS lastestM ON R.roomId = lastestM.roomId LEFT JOIN chatMessageList_"+userId+" AS M ON R.roomId = M.roomId AND M.messageTime = lastestM.messageTime ORDER BY lastestM.messageTime DESC";
//        String sql = "WITH lastestM (messageTime, roomId) AS (SELECT MAX(MM.messageTime), MM.roomId FROM chatMessageList_"+userId+" AS MM GROUP BY MM.roomId), M AS chatMessageList_"+userId+ "SELECT R.*, M.messageContent, M.messageTime, M.messageType FROM ChatRoomList AS R LEFT JOIN lastestM ON R.roomId = lastestM.roomId LEFT JOIN M ON R.roomId = M.roomId AND M.messageTime = lastestM.messageTime ORDER BY lastestM.messageTime DESC";
//        String sql = "SELECT R.*, M.messageContent, M.messageTime, M.messageType FROM ChatRoomList AS R LEFT JOIN (SELECT MAX(MM.messageTime) AS messageTime, MM.roomId AS roomId, MM.friendId AS friendId FROM chatMessageList_"+userId+" AS MM GROUP BY MM.roomId, MM.friendId) AS lastestM ON R.roomId = lastestM.roomId AND R.friendId = latestM.friendId LEFT JOIN chatMessageList_"+userId+" AS M ON R.roomId = M.roomId AND M.messageTime = lastestM.messageTime ORDER BY lastestM.messageTime DESC";
//        String sql = "SELECT R.*, M.messageContent, M.messageTime, M.messageType FROM ChatRoomList AS R LEFT JOIN chatMessageList_"+userId+" AS M ON R.roomId = M.roomId";
//      roomId roomName lastReadMessage roomId messageContent messageTime messageType friendId nickname profileMessage profileUpdateTime bookmark

    }

    public boolean haveChatRoom(int roomId, String friendId){

        String sql;
        if(roomId ==0) {
            sql = "SELECT COUNT(*) FROM chatRoomList WHERE roomId = '0' AND friendId = '" + friendId + "'";
        }else{
            sql = "SELECT COUNT(*) FROM chatRoomList WHERE roomId = '"+roomId+"'";
        }
        Cursor cursor = dbReader.rawQuery(sql,null);
        cursor.moveToNext();
        int result = cursor.getInt(0) ;
        if(result==1){
            Log.d("haveChatRoom",result+", "+friendId);
            return true;
        }else{
            Log.d("haveChatRoom",result+", "+friendId);
            return false;
        }

    }


    public void deleteChatRoomList(int roomId, String friendId) {

        dbWriter.beginTransaction();
        String sql;
        if(roomId==0){
            sql = "DELETE FROM chatRoomList WHERE roomId = '" + roomId + "' AND friendId ='" + friendId + "'";
            Log.d("확인DCRL",roomId+"#"+friendId);
        }else {
            sql = "DELETE FROM chatRoomList WHERE roomId = '" + roomId + "'";
            Log.d("확인DCRL",roomId+"#");
        }

        try {
            dbWriter.execSQL(sql);
            dbWriter.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbWriter.endTransaction();
        }

    }


    public void createChatRoomMemberList(){

        String sql_del="DROP TABLE IF EXISTS chatRoomMemberList;";
        String sql = "CREATE TABLE IF NOT EXISTS chatRoomMemberList(roomId Integer NOT NULL, friendId TEXT NOT NULL, nickname TEXT NOT NULL, profileMessage TEXT, profileImageUpdateTime TEXT, lastReadTime INTEGER, PRIMARY KEY (roomId, friendId));";
        try {
            dbWriter.execSQL(sql_del);
            dbWriter.execSQL(sql);
            Log.d("db.CRML",sql);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Log.d("db.exeptionCRML","noUserId");
        }

    }

    public void insertChatRoomMemberList(int roomId, String userId, String nickname, String profileMessage, String profileImageUpdateTime, long time) {

        String myId = mPref.getString("userId","");
        if(userId.equals(myId)){
            return;
        }
        Log.d("db.insertICMD",roomId+"");
        dbWriter.beginTransaction();
        String sql="INSERT INTO chatRoomMemberList VALUES('"+roomId+"','"+ userId+"','"+ nickname+"','"+profileMessage+"','"+profileImageUpdateTime+"','"+time+"');";
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


    public void insertChatRoomMemberListMultiple(String jarray, String userId) {
        String myId = mPref.getString("userId",userId);
        try
        {
            JSONArray chatArray = new JSONArray(jarray);


            String sql="INSERT INTO chatRoomMemberList VALUES ";
            boolean commaCheck = false;
            for(int i=0;i<chatArray.length();i++){

                JSONObject jobject = new JSONObject(chatArray.get(i).toString());
                int roomId = (int) jobject.getInt("roomId");
                String friendId = (String) jobject.getString("friendId");
                if(friendId.equals(myId)){
                    continue;
                }
                long lastReadTime = (long) jobject.getLong("lastReadTime");
                String nickname = (String) jobject.getString("nickname");
                String profileMessage = (String) jobject.getString("profileMessage");
                String profileImageUpdateTime = (String) jobject.getString("profileImageUpdateTime");

                if(commaCheck){
                    sql = sql + ",";
                }
                sql = sql + "('"+roomId+"','"+friendId+"','"+nickname+"','"+profileMessage+"','"+profileImageUpdateTime+"','"+lastReadTime+"')";
                commaCheck = true;
            }
            dbWriter.beginTransaction();
            dbWriter.execSQL(sql);
            Log.d("db.icrmlm",sql);
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

    public void insertChatRoomMemberListMultipleByJoin(int roomId, String friendId) {
        try {
            JSONArray jarray = new JSONArray(friendId);

            String sql = "SELECT * FROM friendList WHERE friendId='" + jarray.getString(0) + "'";
            for (int i = 1; i < jarray.length(); i++) {
                sql = sql + " OR friendId='" + jarray.getString(i) + "'";
            }
            Cursor cursor = dbReader.rawQuery(sql, null);

            Log.d("여기가 안되네1","#"+sql);

            String sql_2 = "INSERT INTO chatRoomMemberList VALUES ";

            cursor.moveToNext();

            sql_2 = sql_2 + "('" + roomId + "','" + cursor.getString(0) + "','" + cursor.getString(1) + "','" + cursor.getString(2) + "','" + cursor.getString(3) + "','0')";
            while (cursor.moveToNext()) {
                sql_2 = sql_2 + ",('" + roomId + "','" + cursor.getString(0) + "','" + cursor.getString(1) + "','" + cursor.getString(2) + "','" + cursor.getString(3) + "','0')";
            }
            Log.d("여기가 안되네2","#"+sql_2);
            try {
                dbWriter.beginTransaction();
                dbWriter.execSQL(sql_2);
                Log.d("여기가 안되네3","#"+sql_2);
                dbWriter.setTransactionSuccessful();
            } catch (Exception e) {
                Log.d("여기가 안되네4","#"+sql_2);
                e.printStackTrace();
            } finally {
                dbWriter.endTransaction();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void updateChatRoomMemberLastReadTime(int roomId, String friendId, long time){
        dbWriter.beginTransaction();
        String sql;

        sql = "UPDATE chatRoomMemberList SET lastReadTime='" + time + "' WHERE friendId='"+friendId+"' AND roomId='"+roomId+"';";
        Log.d("확인ucrmlrt",sql);

        try {
            dbWriter.execSQL(sql);
            dbWriter.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbWriter.endTransaction();
        }

    }


    public String getChatRoomName(int roomId, String friendId){

        String sql;
        if(roomId==0) {
            sql = "SELECT roomName FROM chatRoomList WHERE roomId = '" + roomId + "' AND friendId = '" + friendId + "'";
        }else{
            sql = "SELECT roomName FROM chatRoomList WHERE roomId = '" + roomId + "'";
        }
        Cursor cursor;
        try {
            cursor = dbReader.rawQuery(sql,null);
            Log.d("db.gcrn", sql);

        }catch (SQLException e) {
            cursor = null;
            Log.d("db.gcrnException", sql);
        }

        String roomName ="" ;
        while(cursor.moveToNext()){
            roomName = cursor.getString(0);
        }
        return roomName;
    }



    public Vector<String[]> getChatRoomMemberList(int roomId, String friendId){

        String sql ;
        if(roomId == 0) {
            sql = "SELECT friendId, nickname, profileMessage, profileImageUpdateTime FROM chatRoomMemberList WHERE roomId = '" + roomId + "' AND friendId = '" + friendId + "'";
        }else{
            sql = "SELECT friendId, nickname, profileMessage, profileImageUpdateTime FROM chatRoomMemberList WHERE roomId = '" + roomId + "'";
        }
        Cursor cursor = dbReader.rawQuery(sql,null);

        Log.d("뭘까sql", sql);
        Vector<String[]> ChatRoomMemberList = new Vector<>();

        while(cursor.moveToNext()){
            String[] ChatRoomMemberData = new String[4];
            ChatRoomMemberData[0] = cursor.getString(0);
            ChatRoomMemberData[1] = cursor.getString(1);
            ChatRoomMemberData[2] = cursor.getString(2);
            ChatRoomMemberData[3] = cursor.getString(3);
            ChatRoomMemberList.add(ChatRoomMemberData);
            Log.d("뭘까gcrm", ChatRoomMemberData[0] + "#" +ChatRoomMemberData[1] + "#" +ChatRoomMemberData[2] +"#" +ChatRoomMemberData[3]);
        }

        return ChatRoomMemberList;

    }

    public void deleteChatRoomMemberList(int roomId, String friendId) {

        dbWriter.beginTransaction();
        String sql;
        if(roomId==0){
            sql = "DELETE FROM chatRoomMemberList WHERE roomId = '" + roomId + "' AND friendId ='" + friendId + "'";
        }else {
            sql = "DELETE FROM chatRoomMemberList WHERE roomId = '" + roomId + "'";
        }

        try {
            dbWriter.execSQL(sql);
            dbWriter.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbWriter.endTransaction();
        }

    }


    public void createChatMessageList(String myId){


//        String sql_del="DROP TABLE IF EXISTS chatMessageList_"+mPref.getString("userId",myId)+";";
        String sql = "CREATE TABLE IF NOT EXISTS chatMessageList_"+mPref.getString("userId",myId)+"(roomId INTEGER NOT NULL,friendId TEXT NOT NULL, messageContent TEXT,messageTime INTEGER,messageType INTEGER)";
        try {
//            dbWriter.execSQL(sql_del);
            dbWriter.execSQL(sql);
            Log.d("db.CML", sql);
        }catch (SQLException e) {
            Log.d("db.exeptionCML",sql);
        }

    }

    public void insertChatMessageList(String myId, int roomId, String friendId, String msgContent, long msgTime, int msgType){
        String sql = "INSERT INTO chatMessageList_"+mPref.getString("userId",myId)+" VALUES ('"+roomId+"','"+friendId+"','"+msgContent+"','"+msgTime+"','"+msgType+"')";
        try {
            dbWriter.execSQL(sql);
            Log.d("db.ICML", sql);
        }catch (SQLException e) {
            Log.d("db.exeptionICML",sql);
        }

    }

    public void deleteChatMessageList(String myId,int roomId,String friendId){


        dbWriter.beginTransaction();
        String sql;
        if(roomId==0){
            sql = "DELETE FROM chatMessageList_"+mPref.getString("userId",myId)+" WHERE roomId='"+roomId+"' AND friendId = '"+friendId+"'";
        }else {
            sql = "DELETE FROM chatMessageList_" + mPref.getString("userId", myId) + " WHERE roomId='" + roomId + "'";
        }

        Log.d("deleteMessageData",sql);
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

    public void deleteChatMessageData(String myId,int roomId,String friendId,long time){


        dbWriter.beginTransaction();
        String sql;
        if(roomId==0){
            sql = "DELETE FROM chatMessageList_"+mPref.getString("userId",myId)+" WHERE roomId='"+roomId+"' AND friendId = '"+friendId+"' AND messageTime ='"+time+"'";
        }else {
            sql = "DELETE FROM chatMessageList_" + mPref.getString("userId", myId) + " WHERE roomId='" + roomId + "' AND messageTime ='"+time+"'";
        }

        Log.d("deleteMessageData",sql);
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


    public Cursor getChatMessageListJoinChatRoomMemberList(String myId,int roomId,String friendId){

        String userId = mPref.getString("userId",myId);
        String sql;
            if(roomId==0) {
                sql = "SELECT * FROM chatMessageList_" + userId + " AS cml LEFT JOIN chatRoomMemberList AS crml ON cml.roomId = crml.roomId AND cml.friendId = crml.friendId WHERE cml.roomId = '" + roomId + "' AND cml.friendId = '" + friendId + "' ORDER BY cml.messageTime";
            }else{
                sql = "SELECT * FROM chatMessageList_" + userId + " AS cml LEFT JOIN chatRoomMemberList AS crml ON cml.roomId = crml.roomId AND cml.friendId = crml.friendId WHERE cml.roomId = '" + roomId + "' ORDER BY cml.messageTime";
            }
        Cursor cursor;
        try {
            cursor = dbReader.rawQuery(sql,null);
            Log.d("db.gcmljcrml", sql);

        }catch (SQLException e) {
            cursor = null;
            Log.d("db.gcmljcrmlException", sql);
        }
        return cursor;

    }


    public Cursor getLastChatMessageOnChatRoom(String myId,int roomId){
        Log.d("db.getLR",roomId+"");
//        SQLiteDatabase db = this.getReadableDatabase();

        String sql;

//        if(no == 0) {
//            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList ON messageData_" + userId + ".no = chatRoomList.no AND messageData_" + userId + ".friendId = chatRoomList.friendId WHERE messageData_" + userId + ".friendId='" + friendId + "' AND messageData_"+userId+".no='"+no+"' AND ( messageData_"+ userId +".type = '1' OR messageData_" + userId +".type = '2') ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
//        }else{
//            sql = "SELECT * FROM messageData_" + userId + " INNER JOIN chatRoomList ON messageData_" + userId + ".no = chatRoomList.no WHERE messageData_" + userId + ".no='" + no + "' AND ( messageData_"+ userId +".type = '1' OR messageData_" + userId +".type = '2') ORDER BY messageData_" + userId + ".idx DESC LIMIT 1;";
//        }


//        sql = "SELECT * FROM chatMessageList_"+mPref.getString("userId",myId)+ "AS M INNER JOIN chatRoomList AS C ON M.roomId = C.roomId WHERE M.roomId='" + roomId + "' AND ( M.type = '1' OR M.type = '2' OR M.type = '51' OR M.type = '52') ORDER BY M.idx DESC LIMIT 1;";
        sql = "SELECT * FROM chatMessageList_"+mPref.getString("userId",myId)+ "AS M INNER JOIN chatRoomList AS C ON M.roomId = C.roomId WHERE M.roomId='" + roomId + "' AND ( M.type = 'Text' OR M.type = 'Photo') LIMIT 1;";

            // rooID, messageContent, messageTime, messageType, roomId, lastReadTime
        Log.d("getLastRow",sql);
        Cursor cursor = dbReader.rawQuery(sql,null);

        return cursor;
    }


    public int getChatRoomUnReadNum(String myId,int roomId,String friendId, long lastReadTime){

//        SQLiteDatabase db = this.getReadableDatabase();
        String sql;
        String userId = mPref.getString("userId",myId);

        // meesageType 에 관한 조건도 추가
        if(roomId == 0){
            sql = "SELECT COUNT(*) FROM chatMessageList_" + userId + " WHERE roomId = '" + roomId + "' AND friendId = '"+ friendId + "' AND (messageType ='1' OR messageType = '51')  AND messageTime >'" + lastReadTime + "';";
        }else {
            sql = "SELECT COUNT(*) FROM chatMessageList_" + userId + " WHERE roomId = '" + roomId + "' AND (messageType ='1' OR messageType = '51') AND messageTime >"+ lastReadTime + ";";
        }
        Log.d("getChatRoomUnReadNum",sql);
        Cursor cursor = dbReader.rawQuery(sql,null);
        cursor.moveToNext();
        int result = cursor.getInt(0) ;

        return result;

    }

    public int getMessageUnReadNum(String myId,int roomId,String friendId, long time){

//        SQLiteDatabase db = this.getReadableDatabase();
        String sql;
        String userId = mPref.getString("userId",myId);

        // meesageType 에 관한 조건도 추가
        if(roomId == 0){
            sql = "SELECT COUNT(*) FROM chatRoomMemberList WHERE roomId = '" + roomId + "' AND friendId = '"+ friendId + "' AND lastReadTime <'" + time + "';";
        }else {
            sql = "SELECT COUNT(*) FROM chatRoomMemberList WHERE roomId = '" + roomId + "' AND friendId <> '"+ friendId + "' AND lastReadTime <'" + time + "';";
        }
        Log.d("getMessageUnReadNum",sql);
        Cursor cursor = dbReader.rawQuery(sql,null);
        cursor.moveToNext();
        int result = cursor.getInt(0) ;

        return result;

    }





}

