package messenger_project.catchmindtalk.Item;

import java.util.Vector;

public class ChatRoomItem {


    public String RoomId;
    public String RoomName;
    public long LastReadTime;
    public int RoomType;

    public String LastMessageContent;
    public int LastMessageType;
    public long LastMessageTime;

    public Vector<String[]> ChatRoomMemberList;
    public int MemberNum;

    public int UnreadNum;



    public ChatRoomItem(String roomId, String roomName, long lastReadTime, int roomType, String lastMessageContent, long lastMessageTime, int lastMessageType, Vector<String[]> chatRoomMemberList, int unreadNum) {


        this.RoomId = roomId;
        this.RoomName = roomName;
        this.LastReadTime = lastReadTime;
        this.RoomType = roomType;
        this.LastMessageContent = lastMessageContent;
        this.LastMessageTime = lastMessageTime;
        this.LastMessageType = lastMessageType;
        this.ChatRoomMemberList = chatRoomMemberList;
        this.MemberNum = chatRoomMemberList.size();
        this.UnreadNum = unreadNum;

    }




    public String getRoomId() {
        return this.RoomId;
    }
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

