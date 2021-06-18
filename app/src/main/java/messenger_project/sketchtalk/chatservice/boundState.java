package messenger_project.sketchtalk.chatservice;

public enum boundState {
    INSTANCE(false,false,false,0,"");
    public boolean boundCheckChatRoom;
    public boolean boundCheckMain;
    public boolean boundStart;
    public int boundedRoomId;
    public String boundedFriendId;

    private boundState(boolean boundCheckChatRoom, boolean boundCheckMain, boolean boundStart, int boundedRoomId, String boundedFriendId) {
        this.boundCheckChatRoom = boundCheckChatRoom;
        this.boundCheckMain = boundCheckMain;
        this.boundStart = boundStart;
        this.boundedRoomId = boundedRoomId;
        this.boundedFriendId = boundedFriendId;
    }


    public static boundState getInstance() {
        return INSTANCE;
    }

    public boolean isBoundCheckChatRoom() {
        return boundCheckChatRoom;
    }

    public void setBoundCheckChatRoom(boolean boundCheckChatRoom) {
        this.boundCheckChatRoom = boundCheckChatRoom;
    }

    public boolean isBoundCheckMain() {
        return boundCheckMain;
    }

    public void setBoundCheckMain(boolean boundCheckMain) {
        this.boundCheckMain = boundCheckMain;
    }

    public boolean isBoundStart() {
        return boundStart;
    }

    public void setBoundStart(boolean boundStart) {
        this.boundStart = boundStart;
    }

    public int getBoundedRoomId() {
        return boundedRoomId;
    }

    public void setBoundedRoomId(int boundedRoomId) {
        this.boundedRoomId = boundedRoomId;
    }

    public String getBoundedFriendId() {
        return boundedFriendId;
    }

    public void setBoundedFriendId(String boundedFriendId) {
        this.boundedFriendId = boundedFriendId;
    }
}
