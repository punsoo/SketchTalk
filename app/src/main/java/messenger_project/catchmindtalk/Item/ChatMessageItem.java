package messenger_project.catchmindtalk.Item;


public class ChatMessageItem {

    public int Type;
    public String UserId;
    public String Nickname;
    public String ProfileImageUpdateTime;
    public String MsgContent;
    public long DateTime;
    public String Time;
    public String Day;




    public ChatMessageItem(int type,String userId, String nickname, String profileIUT, String msgContent,long dateTime, String time,String day){

        this.Type = type;
        this.UserId = userId;
        this.Nickname = nickname;
        this.ProfileImageUpdateTime = profileIUT;
        this.MsgContent = msgContent;
        this.DateTime =dateTime;
        this.Time =  time;
        this.Day = day;
    }


    public void setType(int type) { this.Type = type; }

    public void setUserId(String userId) { this.UserId = userId ; }

    public void setNickname(String nickname) { this.Nickname = nickname ; }

    public void setProfileImageUpdateTime(String profileIUT) { this.ProfileImageUpdateTime = profileIUT ; }

    public void setContent(String msgContent) {
        this.MsgContent = msgContent ;
    }

    public void setDateTime(long dateTime) {this.DateTime =dateTime;}
    public void setTime(String time) { this.Time = time ; }
    public void setDay(String day) { this.Day = day ; }


    public int getType() { return this.Type; }

    public String getUserId() { return this.UserId; }

    public String getNickname() { return this.Nickname; }

    public String getProfileImageUpdateTime() { return this.ProfileImageUpdateTime;}

    public String getMsgContent() {
        return this.MsgContent;
    }

    public long getDateTime() {return this.DateTime;}
    public String getTime() {
        return this.Time;
    }
    public String getDay() {
        return this.Day;
    }


}
