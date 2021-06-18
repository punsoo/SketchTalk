package messenger_project.sketchtalk.chatservice;

public interface CallbackChatRoom {
    public void recvData(String friendId,String msgContent,long time,int msgType); //액티비티에서 선언한 콜백 함수.
    public void changeRoomId(int roomId);
    public void sendMessageMark(String friendId, String msgContent,long time,int msgType);
    public void sendInviteMark(String msgContent,long time,boolean resetMemberList);
    public void sendExitMark(String friendId,String msgContent,long time);
    public void sendImageMark(String friendId,String msgContent, long time , int kind);
    public void reset();
    public void recvUpdate();
    public String getFriendId();
    public void receivePath(String PATH);
    public void receiveClear();
    public void receiveDrawChat(String friendId,String msgContent);
}
