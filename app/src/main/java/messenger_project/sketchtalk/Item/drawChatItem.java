package messenger_project.sketchtalk.Item;

public class drawChatItem {

    String Nickname;
    String Content;


    public drawChatItem(String nickname , String content){

        this.Nickname = nickname;
        this.Content = content;

    }



    public String getNickname() {
        return this.Nickname;
    }

    public String getContent() {
        return this.Content;
    }

}
