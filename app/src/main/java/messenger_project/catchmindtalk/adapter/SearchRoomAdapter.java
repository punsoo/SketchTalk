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


public class SearchRoomAdapter extends BaseAdapter {


    public ArrayList<ChatRoomItem> SearchRoomList = new ArrayList<>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String userId;
    public SimpleDateFormat sdfNow ;
    public String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";


    public SearchRoomAdapter(Context context, ArrayList<ChatRoomItem> searchRoomList, String myId) {

        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.SearchRoomList = searchRoomList;
        db = new MyDatabaseOpenHelper(mContext,"catchMindTalk",null,1);
        this.userId = myId;
        this.sdfNow = new SimpleDateFormat("HH:mm");

    }


    public void clearList(){
        this.SearchRoomList = new ArrayList<>();
    }


    public void addCRItem(ChatRoomItem addItem){
        this.SearchRoomList.add(addItem);
    }


    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public int getCount() {

        return SearchRoomList.size();

    }



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

        }else{
            viewHolder = (ChatRoomViewHolder) convertView.getTag();
        }


        String lastMessageContent = "";
        long lastMessageTime = 0;
        long lastReadTime = 0;
        int unReadMessageNum = 0;
        Date lastMsgTime;
        String lastMsgtime;



        if(SearchRoomList.get(position).getLastMessageType() == 1 ) {
            if(!TextUtils.isEmpty( SearchRoomList.get(position).getLastMessageContent() )){
                lastMessageContent = SearchRoomList.get(position).getLastMessageContent();
            }else {
                lastMessageContent = "";
            }
        }else if(SearchRoomList.get(position).getLastMessageType() == 2) {
            lastMessageContent = "<사진>";
        }


            lastMessageTime = SearchRoomList.get(position).getLastMessageTime();
            lastReadTime = SearchRoomList.get(position).getLastReadTime();
            unReadMessageNum = SearchRoomList.get(position).getUnreadNum();
            lastMsgTime = new Date(lastMessageTime);
            lastMsgtime = this.sdfNow.format(lastMsgTime);

        Vector<String []> ChatRoomMemberList = SearchRoomList.get(position).getChatRoomMemberList();



        if(unReadMessageNum==0){
            viewHolder.unReadMessageNum.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.unReadMessageNum.setVisibility(View.VISIBLE);
            viewHolder.unReadMessageNum.setText(unReadMessageNum+"");
        }

        String RoomName = SearchRoomList.get(position).getRoomName();

        if(RoomName == null || RoomName.equals("")){
            RoomName = "";
            for(int i=0;i<ChatRoomMemberList.size();i++){
                if(!ChatRoomMemberList.get(i)[0].equals(userId)) {
                    if(!RoomName.equals("")) {
                        RoomName += ", " + ChatRoomMemberList.get(i)[1];
                    }
                }
            }
        }

        viewHolder.chatRoomName.setText(RoomName);
        viewHolder.chatRoomContent.setText(SearchRoomList.get(position).getLastMessageContent());
        viewHolder.chatRoomDate.setText(lastMsgtime);
        viewHolder.memberNum.setText("" + SearchRoomList.get(position).getMemberNum());

        if( SearchRoomList.get(position).getRoomType() == 1) {
            String FriendId;
            String ProfileImageUpdateTime;
            if(ChatRoomMemberList.get(0)[0].equals(userId)){
                 FriendId = ChatRoomMemberList.get(1)[0];
                 ProfileImageUpdateTime = ChatRoomMemberList.get(1)[3];
            }else{
                 FriendId = ChatRoomMemberList.get(0)[0];
                 ProfileImageUpdateTime = ChatRoomMemberList.get(0)[3];
            }

             Glide.with(mContext).load(ServerURL + "/profile_image/" + FriendId + ".png")
                  .error(R.drawable.default_profile_image)
                  .signature(new ObjectKey(ProfileImageUpdateTime))
                  .into(viewHolder.profileImage);
        }else {
             viewHolder.profileImage.setImageResource(R.drawable.group_icon);
        }

        convertView.setTag(R.id.roomId, SearchRoomList.get(position).getRoomId());
        convertView.setTag(R.id.roomname, SearchRoomList.get(position).getRoomName());



        return convertView;

    }



    @Override
    public long getItemId(int position) {
        return position ;
    }


    @Override
    public Object getItem(int position) {
        return SearchRoomList.get(position) ;
    }



}
