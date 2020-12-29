package messenger_project.catchmindtalk.Item;


public class ChatMessageItem {

    public int Type;
    public String UserId;
    public String Nickname;
    public String ProfileImageUpdateTime;
    public String MsgContent;
    public long Time;



    public ChatMessageItem(int type,String userId, String nickname, String profileIUT, String msgContent, long time){

        this.Type = type;
        this.UserId = userId;
        this.Nickname = nickname;
        this.ProfileImageUpdateTime = profileIUT;
        this.MsgContent = msgContent;
        this.Time =  time;

    }


    public void setType(int type) { this.Type = type; }

    public void setUserId(String userId) { this.UserId = userId ; }

    public void setNickname(String nickname) { this.Nickname = nickname ; }

    public void setProfileImageUpdateTime(String profileIUT) { this.ProfileImageUpdateTime = profileIUT ; }

    public void setContent(String msgContent) {
        this.MsgContent = msgContent ;
    }

    public void setTime(long time) { this.Time = time ; }



    public int getType() { return this.Type; }

    public String getUserId() { return this.UserId; }

    public String getNickname() { return this.Nickname; }

    public String getProfileImageUpdateTime() { return this.ProfileImageUpdateTime;}

    public String getMsgContent() {
        return this.MsgContent;
    }

    public long getTime() {
        return this.Time;
    }


}
