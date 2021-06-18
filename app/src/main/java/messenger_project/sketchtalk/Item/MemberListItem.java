package messenger_project.sketchtalk.Item;

public class MemberListItem {


    public String userIdTxt;
    public String nicknameTxt;
    public String profileMessageTxt;
    public String profileImageUpdateTimeTxt ;


    public MemberListItem(String userId,String nickname,String profileMessage, String profileImageUpdateTime){

        this.userIdTxt = userId;
        this.nicknameTxt = nickname;
        this.profileMessageTxt = profileMessage;
        this.profileImageUpdateTimeTxt = profileImageUpdateTime;

    }


    public void setUserId(String userId){ this.userIdTxt = userId; }
    public void setName(String nickname) {
        this.nicknameTxt = nickname ;
    }
    public void setProfileMessage(String profileMessage) { this.profileMessageTxt = profileMessage; }
    public void setProfileImageUpdateTime(String profileImageUpdateTime) {
        this.profileImageUpdateTimeTxt = profileImageUpdateTime ;
    }

    public String getUserId() { return this.userIdTxt; }
    public String getNickname() {
        return this.nicknameTxt ;
    }
    public String getProfileMessage() {
        return this.profileMessageTxt ;
    }
    public String getProfileImageUpdateTime() {
        return this.profileImageUpdateTimeTxt ;
    }

}
