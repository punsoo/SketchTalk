package messenger_project.catchmindtalk.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Vector;

import androidx.fragment.app.Fragment;
import messenger_project.catchmindtalk.Item.ChatRoomItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.activity.ChatRoomActivity;
import messenger_project.catchmindtalk.activity.MainActivity;
import messenger_project.catchmindtalk.adapter.ChatRoomListAdapter;

public class ChatRoomListFragment extends Fragment implements MainActivity.FragmentCommunicator{


    ChatRoomListAdapter chatRoomListAdapter;
    ArrayList<ChatRoomItem> chatRoomListData;
    MyDatabaseOpenHelper db;
    String myId;
    String myNickname;

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_chatroom_list, container, false);

        mainActivity = (MainActivity)getActivity();
        myId = getArguments().getString("userId");
        myNickname = getArguments().getString("nickname");

        ListView chatRoomListView = (ListView) rootView.findViewById(R.id.list);

        chatRoomListData = new ArrayList<>();


        db = new MyDatabaseOpenHelper(getContext(),"catchMindTalk",null,1);


        Cursor CRC = db.getChatRoomListJoinOnMessageList(myId); //ChatRoomCursor

        while(CRC.moveToNext()) {

            int UnreadNum = db.getChatRoomUnReadNum(myId,CRC.getInt(0),CRC.getString(1),CRC.getLong(2));
            Vector<String[]> ChatRoomMemberList  = db.getChatRoomMemberList(CRC.getInt(0),CRC.getString(1));

            String friendId ;
            if(CRC.getInt(0)==0){
                if(ChatRoomMemberList.get(0)[0].equals(myId)) {
                    friendId = ChatRoomMemberList.get(1)[0];
                }else{
                    friendId = ChatRoomMemberList.get(0)[0];
                }
            }else {
                JSONArray jarray = new JSONArray();
                for (int i = 0; i < ChatRoomMemberList.size(); i++) {
                    jarray.put(ChatRoomMemberList.get(i)[0]);
                }
                friendId=jarray.toString();
            }

            ChatRoomItem addItem = new ChatRoomItem(CRC.getInt(0),friendId,CRC.getLong(2),CRC.getString(3),CRC.getInt(4),CRC.getString(10),CRC.getLong(11),CRC.getInt(12),ChatRoomMemberList, UnreadNum);
            chatRoomListData.add(addItem);

        }


        chatRoomListAdapter = new ChatRoomListAdapter(getActivity().getApplicationContext(),chatRoomListData,myId);

        chatRoomListView.setAdapter(chatRoomListAdapter);
        chatRoomListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int roomId = (int) view.getTag(R.id.roomId);
                String friendId = (String) view.getTag(R.id.friendId);
                String roomName = (String) view.getTag(R.id.roomName);



                Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
                intent.putExtra("roomId",roomId);
                intent.putExtra("friendId",friendId);
                intent.putExtra("roomName",roomName);

                startActivity(intent);

            }
        });


        chatRoomListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                int roomId = (int) view.getTag(R.id.roomId);
                String friendId = (String) view.getTag(R.id.friendId);


                mainActivity.sendToActivityExit(roomId,friendId);

                return true;
            }
        });



        return rootView;
    }


    @Override
    public void notifyRecvData(){
        if(chatRoomListAdapter != null) {
            chatRoomListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void changeRoomList() {

        if(db ==null){
            return;
        }

        ArrayList<ChatRoomItem> changeList = new ArrayList<>();

        Cursor CRC = db.getChatRoomListJoinOnMessageList(myId); //ChatRoomCursor

        while(CRC.moveToNext()) {

            int UnreadNum = db.getChatRoomUnReadNum(myId,CRC.getInt(0),CRC.getString(1),CRC.getLong(2));
            Vector<String[]> ChatRoomMemberList  = db.getChatRoomMemberList(CRC.getInt(0),CRC.getString(1));

            String friendId ;
            if(CRC.getInt(0)==0){
                if(ChatRoomMemberList.get(0)[0].equals(myId)) {
                    friendId = ChatRoomMemberList.get(1)[0];
                }else{
                    friendId = ChatRoomMemberList.get(0)[0];
                }
            }else {
                JSONArray jarray = new JSONArray();
                for (int i = 0; i < ChatRoomMemberList.size(); i++) {
                    jarray.put(ChatRoomMemberList.get(i)[0]);
                }
                friendId=jarray.toString();
            }

            ChatRoomItem addItem = new ChatRoomItem(CRC.getInt(0),friendId,CRC.getLong(2),CRC.getString(3),CRC.getInt(4),CRC.getString(10),CRC.getLong(11),CRC.getInt(12),ChatRoomMemberList, UnreadNum);
            changeList.add(addItem);

        }

        chatRoomListAdapter.ChangeList(changeList);
        chatRoomListAdapter.notifyDataSetChanged();

    }

    @Override
    public void startChatRoomActivity(int roomId, String friendId,String nickname) {

        Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
        intent.putExtra("friendId",friendId);
        intent.putExtra("nickname",nickname);
        intent.putExtra("roomId",roomId);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        changeRoomList();
    }
}

