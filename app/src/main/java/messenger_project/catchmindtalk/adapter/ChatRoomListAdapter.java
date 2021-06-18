package messenger_project.catchmindtalk.adapter;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import messenger_project.catchmindtalk.Item.ChatRoomItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.viewholder.ChatRoomViewHolder;

public class ChatRoomListAdapter extends BaseAdapter {

    public ArrayList<ChatRoomItem> chatRoomList = new ArrayList<ChatRoomItem>();
    public Context mContext;
    public LayoutInflater inflater;
    public MyDatabaseOpenHelper db;
    public String userId;
    public SimpleDateFormat sdfTime;
    public SimpleDateFormat sdfDate;
    public String ServerURL;

    // ListViewAdapter의 생성자
    public ChatRoomListAdapter(Context context, ArrayList<ChatRoomItem> ListData, String myId) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatRoomList = ListData;
        this.userId = myId;
        this.sdfTime = new SimpleDateFormat("HH:mm");
        this.sdfDate = new SimpleDateFormat("yyyy.MM.dd");
        this.ServerURL = context.getResources().getString(R.string.ServerUrl);
        db = new MyDatabaseOpenHelper(mContext, "catchMindTalk", null, 1);
    }

    public void setChatRoomList(ArrayList<ChatRoomItem> ListData) {
        this.chatRoomList = ListData;
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return chatRoomList.size();
    }


    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ChatRoomViewHolder viewHolder;


        if (convertView == null) {

            convertView = this.inflater.inflate(R.layout.chatroom_item, parent, false);

            viewHolder = new ChatRoomViewHolder();
            viewHolder.chatRoomName = (TextView) convertView.findViewById(R.id.chatRoomName);
            viewHolder.chatRoomContent = (TextView) convertView.findViewById(R.id.chatRoomContent);
            viewHolder.memberNum = (TextView) convertView.findViewById(R.id.chatRoomMemberNum);
            viewHolder.chatRoomDate = (TextView) convertView.findViewById(R.id.chatRoomDate);
            viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.chatRoomImage);
            viewHolder.unReadMessageNum = (TextView) convertView.findViewById(R.id.unReadMessageNum);

            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ChatRoomViewHolder) convertView.getTag();
        }


        String lastMessageContent = "";
        long lastMessageTime = 0;
        long lastReadTime = 0;
        int unReadMessageNum = 0;
        Date lastMsgDateFormat;
        String lastMsgTime;
        String lastMsgDate;
        Date nowDateFormat;
        String nowDate;

        if (chatRoomList.get(position).getLastMessageType() == 1 || chatRoomList.get(position).getLastMessageType() == 2) {
            if (!TextUtils.isEmpty(chatRoomList.get(position).getLastMessageContent())) {
                lastMessageContent = chatRoomList.get(position).getLastMessageContent();
            } else {
                lastMessageContent = "";
            }
        } else if (chatRoomList.get(position).getLastMessageType() == 51 || chatRoomList.get(position).getLastMessageType() == 52) {
            lastMessageContent = "<사진>";
        }


        lastMessageTime = chatRoomList.get(position).getLastMessageTime();
        lastReadTime = chatRoomList.get(position).getLastReadTime();
        unReadMessageNum = chatRoomList.get(position).getUnreadNum();
        
        if(lastMessageTime == 0) {
            lastMsgTime = "";
        }else{
            lastMsgDateFormat = new Date(lastMessageTime);
            nowDateFormat = new Date(System.currentTimeMillis());
            lastMsgDate = sdfDate.format(lastMsgDateFormat);
            nowDate = sdfDate.format(nowDateFormat);
            if(lastMsgDate.equals(nowDate)) {
                lastMsgTime = sdfTime.format(lastMsgDateFormat);
            }else {
                lastMsgTime = lastMsgDate;
            }
        }
        

        Vector<String[]> ChatRoomMemberList = chatRoomList.get(position).getChatRoomMemberList();


        if (unReadMessageNum == 0) {
            viewHolder.unReadMessageNum.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.unReadMessageNum.setVisibility(View.VISIBLE);
            viewHolder.unReadMessageNum.setText(unReadMessageNum + "");
        }

        String RoomName = chatRoomList.get(position).getRoomName();

        if (RoomName == null || RoomName.equals("")) {
            for (int i = 0; i < ChatRoomMemberList.size(); i++) {
                if (i == 0) {
                    RoomName = ChatRoomMemberList.get(0)[1];
                } else {
                    RoomName += ", " + ChatRoomMemberList.get(i)[1];
                }
            }

        }

        viewHolder.chatRoomName.setText(RoomName);
        viewHolder.chatRoomContent.setText(lastMessageContent);
        viewHolder.chatRoomDate.setText(lastMsgTime);
        viewHolder.memberNum.setText("" + chatRoomList.get(position).getMemberNum());

        if (chatRoomList.get(position).getRoomType() == 1) {
            String FriendId = "";
            String ProfileImageUpdateTime = "none";
            if (ChatRoomMemberList.get(0)[0].equals(userId)) {
                FriendId = ChatRoomMemberList.get(1)[0];
                ProfileImageUpdateTime = ChatRoomMemberList.get(1)[3];
            } else {
                FriendId = ChatRoomMemberList.get(0)[0];
                ProfileImageUpdateTime = ChatRoomMemberList.get(0)[3];
            }


            Glide.with(mContext).load(ServerURL + "/profile_image/" + FriendId + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new ObjectKey(ProfileImageUpdateTime))
                    .into(viewHolder.profileImage);
        } else {
            viewHolder.profileImage.setImageResource(R.drawable.group_icon);
        }

        convertView.setTag(R.id.roomId, chatRoomList.get(position).getRoomId());
        convertView.setTag(R.id.friendId, chatRoomList.get(position).getFriendId());
        Log.d("확인CRLA", chatRoomList.get(position).getFriendId() + "#");
        convertView.setTag(R.id.roomName, chatRoomList.get(position).getRoomName());


        return convertView;

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return chatRoomList.get(position);
    }

    public void ChangeList(ArrayList<ChatRoomItem> ListData) {
        this.chatRoomList = ListData;
    }


}
