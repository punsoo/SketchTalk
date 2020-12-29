package messenger_project.catchmindtalk.fragment;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import messenger_project.catchmindtalk.Item.ChatMessageItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.activity.ChatRoomActivity;
import messenger_project.catchmindtalk.adapter.ChatMessageAdapter;

public class MessageRoomFragment extends Fragment implements ChatRoomActivity.FragmentCommunicator{

    ChatMessageAdapter chatListAdapter;
    ArrayList<ChatMessageItem> ListData;
    SharedPreferences mPref;
    SharedPreferences.Editor editor;

    String userId;
    String friendId;
    int roomId;
    public MyDatabaseOpenHelper db;
    ListView lv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.message_room_fragment, container, false);

        roomId = getArguments().getInt("roomId");
        userId = getArguments().getString("userId");
        friendId = getArguments().getString("friendId");

        ListData = new ArrayList<ChatMessageItem>();

        db = new MyDatabaseOpenHelper(getContext(),"catchMindTalk",null,1);
        Cursor cursor = db.getChatMessageListJoinChatRoomMemberList(userId,roomId,friendId);

        while(cursor.moveToNext()) {


            ChatMessageItem addItem = new ChatMessageItem(cursor.getInt(4),cursor.getString(1),cursor.getString(7),cursor.getString(9),cursor.getString(2),cursor.getLong(3));
            ListData.add(addItem);

            Log.d("ChatMessageItem",cursor.getInt(0)+"#"+cursor.getString(1)+"#"+cursor.getString(2)+"#"+cursor.getLong(3)+"#"+cursor.getInt(4)+"#"+cursor.getInt(5)+"#"+cursor.getString(6)+"#"+cursor.getString(7)+"#"+cursor.getString(8)+"#"+cursor.getString(9)+"#"+cursor.getLong(10));


        }

        lv = (ListView) rootView.findViewById(R.id.messageList);

        chatListAdapter = new ChatMessageAdapter(getActivity(),ListData,userId,roomId,friendId);

        lv.setAdapter(chatListAdapter);

        lv.setSelection(ListData.size()-1);

        return rootView;

    }



    @Override
    public void passData(String friendId,String nickname, String profileIUT,String msgContent,long now,int type) {


        if(type == 1) {
            ChatMessageItem addItem = new ChatMessageItem(1, friendId, nickname, profileIUT, msgContent, now);
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
            lv.setSelection(ListData.size()-1);

        }else if(type ==2){
            ChatMessageItem addItem = new ChatMessageItem(2, friendId, nickname, profileIUT, msgContent, now);
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
            lv.setSelection(ListData.size()-1);
        }else if(type == 3){
            ChatMessageItem addItem = new ChatMessageItem(3, friendId, nickname, profileIUT, msgContent, now);
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();

        }else if(type == 51){
            ChatMessageItem addItem = new ChatMessageItem(51, friendId, nickname, profileIUT, msgContent, now);
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
            lv.setSelection(ListData.size()-1);

        }else if(type == 52){
            ChatMessageItem addItem = new ChatMessageItem(52, friendId, nickname, profileIUT, msgContent, now);
            ListData.add(addItem);
            chatListAdapter.notifyDataSetChanged();
            lv.setSelection(ListData.size()-1);

        }



    }




    @Override
    public void alertChange() {
        Log.d("MessageRoomF","alertChange");
        if(chatListAdapter != null) {
            chatListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void changeNo(int sRoomId) {
        this.roomId = sRoomId ;
        chatListAdapter.roomId = sRoomId;
    }




    @Override
    public void deleteMessage(int position) {
        chatListAdapter.deleteMessage(position);
        chatListAdapter.notifyDataSetChanged();
    }


    @Override
    public void bottomSelect() {

        lv.setSelection(ListData.size()-1);

    }


}
