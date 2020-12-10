package messenger_project.catchmindtalk.fragment;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Vector;

import androidx.fragment.app.Fragment;
import messenger_project.catchmindtalk.Item.ChatRoomItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.activity.MainActivity;
import messenger_project.catchmindtalk.adapter.ChatRoomListAdapter;
import messenger_project.catchmindtalk.adapter.SearchRoomAdapter;

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


        getChatRoomList();



        chatRoomListAdapter = new ChatRoomListAdapter(getActivity().getApplicationContext(),chatRoomListData,myId);

        chatRoomListView.setAdapter(chatRoomListAdapter);
        chatRoomListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//                String friendId = (String) view.getTag(R.id.userId);
//                String nickname = (String) view.getTag(R.id.nickname);
//
//
//                Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
//                intent.putExtra("friendId",friendId);
//                intent.putExtra("nickname",nickname);
//
//                startActivity(intent);

            }
        });


        chatRoomListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

//                String friendId = (String) view.getTag(R.id.userId);
//                int no = (int) view.getTag(R.id.no);
//
//                mainActivity.sendToActivity2(no,friendId);

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

        getChatRoomList();
        chatRoomListAdapter.notifyDataSetChanged();

    }

    @Override
    public void startChatRoomActivity(int no,String friendId,String nickname) {

//        Intent intent = new Intent(getActivity().getApplicationContext(), ChatRoomActivity.class);
//        intent.putExtra("friendId",friendId);
//        intent.putExtra("nickname",nickname);
//        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        changeRoomList();
//        myListAdapter.notifyDataSetChanged();
    }


    public void getChatRoomList(){

        Cursor CRC = db.getChatRoomListJoinOnMessageList(myId); //ChatRoomCursor

        while(CRC.moveToNext()) {

            int UnreadNum = db.getUnReadNum(myId,CRC.getString(0),CRC.getLong(2));
            Cursor CRMC = db.getChatRoomMemberJoinOnFriendList(CRC.getString(0));
            Vector<String[]> ChatRoomMemberList = new Vector<>();

            while(CRMC.moveToNext()){
                String[] ChatRoomMemberData = new String[3];
                ChatRoomMemberData[0] = CRMC.getString(0);
                ChatRoomMemberData[1] = CRMC.getString(1);
                ChatRoomMemberData[2] = CRMC.getString(2);
                ChatRoomMemberList.add(ChatRoomMemberData);
            }

            ChatRoomItem addItem = new ChatRoomItem(CRC.getString(0),CRC.getString(1),CRC.getLong(2),CRC.getInt(3),CRC.getString(4),CRC.getLong(5),CRC.getInt(6),ChatRoomMemberList, UnreadNum);
            chatRoomListData.add(addItem);

        }

    }





}

