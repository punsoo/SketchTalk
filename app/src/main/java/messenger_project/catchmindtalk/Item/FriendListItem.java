package messenger_project.catchmindtalk.Item;

import android.util.Log;

import java.util.Objects;

public class FriendListItem {

    public String profileImageUpdateTime ;
    public String nickname;
    public String profileMessage;
    public String userId;
    public int favortie;
    public int hiding;
    public int blocked;



    public FriendListItem(String _userId,String _nickname,String _profileMessage, String _profileImageUpdateTime, int _favorite, int _hiding, int _blocked){

        this.userId = _userId;
        this.nickname = _nickname;
        this.profileMessage = _profileMessage;
        this.profileImageUpdateTime = _profileImageUpdateTime;
        this.favortie = _favorite;
        this.hiding = _hiding;
        this.blocked = _blocked;
    }


    public void setProfile(String _profileImageUpdateTime) { this.profileImageUpdateTime = _profileImageUpdateTime; }
    public void setId(String _userId){ this.userId = _userId; }
    public void setName(String _nickname) {
        this.nickname = _nickname ;
    }
    public void setMessage(String _profileMessage) {
        this.profileMessage = _profileMessage ;
    }

    public String getId() { return this.userId; }
    public String getProfileImageUpdateTime() {
        return this.profileImageUpdateTime ;
    }
    public String getNickname() {
        return this.nickname ;
    }
    public String getProfileMessage() {
        return this.profileMessage ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendListItem that = (FriendListItem) o;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
