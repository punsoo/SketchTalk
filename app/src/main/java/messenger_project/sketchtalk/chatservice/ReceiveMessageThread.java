package messenger_project.sketchtalk.chatservice;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;

import messenger_project.sketchtalk.MyDatabaseOpenHelper;

import static messenger_project.sketchtalk.chatservice.ChatService.UpdateRead;

public class ReceiveMessageThread extends Thread {
    Socket socket;
    String serverURL;
    String userId;
    int sRoomId;
    String sFriendId;
    String sMsgContent;
    long sTime;
    int sMsgType;
    MyDatabaseOpenHelper db;
    boundState mBoundState;
    CallbackChatRoom mCallbackChatRoom;
    CallbackMain mCallbackMain;

    public ReceiveMessageThread(Socket socket, String serverURL, String userId, int sRoomId, String sFriendId, String sMsgContent, long sTime, int sMsgType, MyDatabaseOpenHelper db, boundState mBoundState, CallbackChatRoom mCallbackChatRoom, CallbackMain mCallbackMain) {
        this.socket = socket;
        this.serverURL = serverURL;
        this.userId = userId;
        this.sRoomId = sRoomId;
        this.sFriendId = sFriendId;
        this.sMsgContent = sMsgContent;
        this.sTime = sTime;
        this.sMsgType = sMsgType;
        this.db = db;
        this.mBoundState = mBoundState;
        this.mCallbackChatRoom = mCallbackChatRoom;
        this.mCallbackMain = mCallbackMain;
    }

    @Override
    public void run() {
        Log.d("ReceiveMessageThread", sRoomId + "#" + sFriendId + "#" + sMsgContent + "#" + sTime + "#" + sMsgType);
        db.updateChatRoomMemberLastReadTime(sRoomId, sFriendId, sTime);
        if (sMsgType == 1 || sMsgType == 51) {

            db.insertChatMessageList(userId, sRoomId, sFriendId, sMsgContent, sTime, sMsgType);

            if (mBoundState.boundStart) {
                if (sRoomId == 0) {
                    if (mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                        db.updateChatRoomLastReadTime(sRoomId, sFriendId, sTime);
                        sendRead(sRoomId, mCallbackChatRoom.getFriendId(), sTime);
                    }
                } else {
                    if (mBoundState.boundedRoomId == sRoomId) {
                        db.updateChatRoomLastReadTime(sRoomId, sFriendId, sTime);
                        sendRead(sRoomId, mCallbackChatRoom.getFriendId(), sTime);
                    }
                }
            }


            if (sRoomId == 0 && !db.haveChatRoom(sRoomId, sFriendId)) {
                try {
                    getFriendThread gft = new getFriendThread(serverURL, userId, sFriendId, sTime, db, mBoundState, mCallbackMain);
                    gft.start();
                    gft.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if (sRoomId > 0 && !db.haveChatRoom(sRoomId, sFriendId)) {
                try {
                    getGroupThread ggt = new getGroupThread(serverURL, userId, sRoomId, sFriendId, sMsgContent, sTime, sMsgType, db, mBoundState, mCallbackMain, mCallbackChatRoom);
                    ggt.start();
                    ggt.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (mBoundState.boundCheckChatRoom) {
                    if (sRoomId == 0) {

                        if (mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                            mCallbackChatRoom.recvData(sFriendId, sMsgContent, sTime, sMsgType);
                        }


                    } else {
                        if (mBoundState.boundedRoomId == sRoomId) {
                            mCallbackChatRoom.recvData(sFriendId, sMsgContent, sTime, sMsgType);
                        }
                    }
                }
            }

            if (mBoundState.boundCheckMain) {
                mCallbackMain.changeRoomList();
            }

        } else if (sMsgType == UpdateRead) {

//                    db.updateChatRoomMemberLastReadTime(sRoomId,sFriendId,sTime);
            if (mBoundState.boundStart) {
                if (sRoomId == 0) {
                    if (mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                        mCallbackChatRoom.recvUpdate();
                    }
                } else {
                    if (mBoundState.boundedRoomId == sRoomId) {
                        mCallbackChatRoom.recvUpdate();
                    }
                }


            }

        } else if (sMsgType == 4) {

            db.deleteChatRoomMemberList(sRoomId, sFriendId);
            db.insertChatMessageList(userId, sRoomId, sFriendId, sMsgContent, sTime, sMsgType);
            if (mBoundState.boundCheckChatRoom) {
                if (mBoundState.boundedRoomId == sRoomId) {
                    mCallbackChatRoom.sendExitMark(sFriendId, sMsgContent, sTime);
                }
            }
        } else if (sMsgType == 5) {
            if (db.haveChatRoom(sRoomId, sFriendId)) {
                try {
                    JSONObject jobject = new JSONObject(sMsgContent);
                    String inviteId = jobject.getString("inviteId");
                    String realContent = jobject.getString("msgContent");
                    getInviteFriendThread gift = new getInviteFriendThread(serverURL,userId, sRoomId, inviteId, realContent, sTime,db,mBoundState,mCallbackChatRoom);
                    gift.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    JSONObject jobject = new JSONObject(sMsgContent);
                    String inviteId = jobject.getString("inviteId");
                    String realContent = jobject.getString("msgContent");
                    getInviteGroupThread gigt = new getInviteGroupThread(serverURL, userId, sRoomId, sFriendId, realContent, sTime, db, mBoundState, mCallbackMain);
                    gigt.start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (sMsgType == 10) {
            if (mBoundState.boundCheckChatRoom) {
                if (sRoomId == 0) {
                    if (mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                        mCallbackChatRoom.receivePath(sMsgContent);
                    }
                } else {
                    if (mBoundState.boundedRoomId == sRoomId) {
                        mCallbackChatRoom.receivePath(sMsgContent);
                    }
                }

            }
        } else if (sMsgType == 11) {
            if (mBoundState.boundCheckChatRoom) {
                if (sRoomId == 0) {
                    if (mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                        mCallbackChatRoom.receiveClear();
                    }
                } else {
                    if (mBoundState.boundedRoomId == sRoomId) {
                        mCallbackChatRoom.receiveClear();
                    }
                }

            }
        } else if (sMsgType == 88) {

            if (mBoundState.boundCheckChatRoom) {


                if (sRoomId == 0) {
                    if (mBoundState.boundedRoomId == 0 && mBoundState.boundedFriendId.equals(sFriendId)) {
                        mCallbackChatRoom.receiveDrawChat(sFriendId, sMsgContent);
                    }
                } else {
                    if (mBoundState.boundedRoomId == sRoomId) {
                        mCallbackChatRoom.receiveDrawChat(sFriendId, sMsgContent);
                    }
                }


            }
        }

    }

    public void sendRead(int roomId, String friendId, long time) {
        Log.d("확인sendRead", roomId + "#" + friendId + "#" + time);
        if (roomId < 0) {
            return;
        }

        SendThread st = new SendThread(socket, userId, roomId, friendId, "justUpdateTime", time, UpdateRead);
        st.start();

    }
}
