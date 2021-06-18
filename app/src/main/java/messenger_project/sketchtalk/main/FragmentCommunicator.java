package messenger_project.sketchtalk.main;

public interface FragmentCommunicator {
    void changeRoomList();
    void notifyRecvData();
    void startChatRoomActivity(int roomId, String friendId, String nickname);
}
