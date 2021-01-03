package messenger_project.catchmindtalk.Item;

import java.util.Vector;

public class ChatRoomItem {


    public int RoomId;
    public String FriendId;
    public String RoomName;
    public long LastReadTime;
    public int RoomType;

    public String LastMessageContent;
    public int LastMessageType;
    public long LastMessageTime;

    public Vector<String[]> ChatRoomMemberList;
    public int MemberNum;

    public int UnreadNum;



    public ChatRoomItem(int roomId, String friendId, long lastReadTime, String roomName, int roomType, String lastMessageContent, long lastMessageTime, int lastMessageType, Vector<String[]> chatRoomMemberList, int unreadNum) {
        // roomId friendId lastReadTime(chatRoom) roomName roomType cm1roomId cm1friendId maxTime roomId friendId messageContent messageTime messageType

        this.RoomId = roomId;
        this.FriendId = friendId;
        this.LastReadTime = lastReadTime;
        this.RoomName = roomName;
        this.RoomType = roomType;
        this.LastMessageContent = lastMessageContent;
        this.LastMessageTime = lastMessageTime;
        this.LastMessageType = lastMessageType;
        this.ChatRoomMemberList = chatRoomMemberList;
        this.MemberNum = chatRoomMemberList.size();
        this.UnreadNum = unreadNum;


    }




    public int getRoomId() {
        return this.RoomId;
    }
    public String getFriendId() { return this.FriendId;}
    public long getLastReadTime(){
        return this.LastReadTime;
    }
    public int getRoomType(){ return this.RoomType; }
    public String getRoomName(){
        return this.RoomName;
    }
    public String getLastMessageContent(){
        return this.LastMessageContent;
    }
    public long getLastMessageTime(){
        return this.LastMessageTime;
    }
    public int getLastMessageType(){
        return this.LastMessageType;
    }
    public Vector<String[]> getChatRoomMemberList() {
        return this.ChatRoomMemberList;
    }
    public int getMemberNum() {
        return this.MemberNum;
    }
    public int getUnreadNum(){
        return this.UnreadNum;
    }

    public void setRoomId(){}
    public void setFriendId(){}
    public void setLastReadTime(){
    }
    public void setRoomType(){
    }
    public void setRoomName(){}
    public void setLastMessageContent() {
    }
    public void setLastMessageTime(){}
    public void setLastMessageType(){}
    public void setChatRoomMemberList(){}
    public void setMemberNum(){}
    public void setUnreadNum(int unreadNum){
        this.UnreadNum = unreadNum;
    }

}

