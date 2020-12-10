package messenger_project.catchmindtalk.Item;


/**
 * Created by sonsch94 on 2017-09-22.
 */

public class SearchFriendItem {

    public String ProfileImageUpdateTime ;
    public String Nickname;
    public String ProfileMessage;
    public String FriendId;
    public int Favorite;
    public int Hiding;
    public int Blocked;

    public SearchFriendItem(String friendId, String nickname, String profileMessage, String profileImageUpdateTime, int favorite, int hiding, int blocked){

        this.FriendId = friendId;
        this.Nickname = nickname;
        this.ProfileMessage = profileMessage;
        this.ProfileImageUpdateTime = profileImageUpdateTime;
        this.Favorite = favorite;
        this.Hiding = hiding;
        this.Blocked = blocked;


    }


    public void setFriendId(String friendId){ this.FriendId = friendId; }
    public void setNickname(String nickname) {
        this.Nickname = nickname ;
    }
    public void setProfileMessage(String profileMessage) {
        this.ProfileMessage = profileMessage ;
    }
    public void setProfileImageUpdateTime(String profileImageUpdateTime) { this.ProfileImageUpdateTime = profileImageUpdateTime; }
    public void setFavorite(int favorite){ this.Favorite = favorite; }
    public void setHiding(int hiding){ this.Hiding = hiding; }
    public void setBlocked(int blocked){ this.Blocked = blocked; }

    public String getFriendId() { return this.FriendId; }
    public String getNickname() {
        return this.Nickname ;
    }
    public String getProfileMessage() {
        return this.ProfileMessage;
    }
    public String getProfileImageUpdateTime() {
        return this.ProfileImageUpdateTime ;
    }
    public int getFavorite() { return  this.Favorite; }
    public int getHiding() { return  this.Hiding; }
    public int getBlocked() { return  this.Blocked; }


}
