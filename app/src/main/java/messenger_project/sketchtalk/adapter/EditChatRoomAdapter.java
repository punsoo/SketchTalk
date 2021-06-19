package messenger_project.sketchtalk.adapter;


import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import messenger_project.sketchtalk.Item.ChatRoomItem;
import messenger_project.sketchtalk.MyDatabaseOpenHelper;
import messenger_project.sketchtalk.R;
import messenger_project.sketchtalk.viewholder.ChatRoomViewHolder;

public class EditChatRoomAdapter extends BaseAdapter {

    public ArrayList<ChatRoomItem> chatRoomList = new ArrayList<ChatRoomItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String userId;
    public String ServerURL;
    HashMap<String, Boolean> isChecked = new HashMap<>();

    // ListViewAdapter의 생성자
    public EditChatRoomAdapter(Context context,ArrayList<ChatRoomItem> ListData,String myId , HashMap<String, Boolean> IsChecked ) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatRoomList = ListData;
        this.userId = myId;
        this.isChecked = IsChecked;
        this.ServerURL = context.getResources().getString(R.string.ServerUrl);
        db = new MyDatabaseOpenHelper(mContext,"catchMindTalk",null,1);

    }

    public void changeIsChecked(String userId){

        if(this.isChecked.get(userId)){
            this.isChecked.put(userId,false);
        }else{
            this.isChecked.put(userId,true);
        }

    }

    public void changeAll(boolean allCheck){

        Iterator<String> iterator = isChecked.keySet().iterator();

        while(iterator.hasNext()){

            isChecked.put(iterator.next(),allCheck);

        }


    }

    public String exitCheckedRoom(){

        Iterator<String> iterator = isChecked.keySet().iterator();

        JSONArray jarray = new JSONArray();

        while(iterator.hasNext()){
            try {

                String key = iterator.next();
                if (isChecked.get(key)) {

                    JSONObject jsonObject = new JSONObject();

                    if (Character.isDigit(key.charAt(0))) {

                        jsonObject.put("group", true);

                        int roomId = Integer.parseInt(key);

                        jsonObject.put("roomId",roomId);


                        Vector<String[]> chatRoomMemberList= db.getChatRoomMemberList(roomId,"group");

                        JSONArray jsonArray = new JSONArray();

                        for(int i=0;i<chatRoomMemberList.size();i++){
                            jsonArray.put(chatRoomMemberList.get(i)[0]);
                        }

                        String friendId = jsonArray.toString();
                        jsonObject.put("friendId",friendId);


                    } else {

                        jsonObject.put("group", false);
                        jsonObject.put("roomId",0);
                        jsonObject.put("friendId",key);

                    }

                    jarray.put(jsonObject);

                }

            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return jarray.toString();

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

            convertView = this.inflater.inflate(R.layout.chatroom_item_check, parent, false);

            viewHolder = new ChatRoomViewHolder();
            viewHolder.chatRoomName = (TextView) convertView.findViewById(R.id.chatRoomNameCheck);
            viewHolder.chatRoomContent = (TextView) convertView.findViewById(R.id.chatRoomContentCheck);
            viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.chatRoomImageCheck);
            viewHolder.check = (ImageView) convertView.findViewById(R.id.check_icon);
            viewHolder.memberNum = (TextView) convertView.findViewById(R.id.chatRoomMemberNumCheck);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (ChatRoomViewHolder) convertView.getTag();
        }


        String lastMessageContent = "";

        if (chatRoomList.get(position).getLastMessageType() == 1 || chatRoomList.get(position).getLastMessageType() == 2) {
            if (!TextUtils.isEmpty(chatRoomList.get(position).getLastMessageContent())) {
                lastMessageContent = chatRoomList.get(position).getLastMessageContent();
            } else {
                lastMessageContent = "";
            }
        } else if (chatRoomList.get(position).getLastMessageType() == 51 || chatRoomList.get(position).getLastMessageType() == 52) {
            lastMessageContent = "<사진>";
        }



        Vector<String[]> ChatRoomMemberList = chatRoomList.get(position).getChatRoomMemberList();


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

            viewHolder.memberNum.setVisibility(View.INVISIBLE);

            if(isChecked.get(chatRoomList.get(position).getFriendId())){
                viewHolder.check.setImageResource(R.drawable.check_icon);
            }else{
                viewHolder.check.setImageResource(R.drawable.check_icon_inact);
            }
        } else {
            viewHolder.profileImage.setImageResource(R.drawable.group_icon);
            viewHolder.memberNum.setVisibility(View.VISIBLE);

            if(isChecked.get(chatRoomList.get(position).getRoomId()+"")){
                viewHolder.check.setImageResource(R.drawable.check_icon);
            }else{
                viewHolder.check.setImageResource(R.drawable.check_icon_inact);
            }
        }

        convertView.setTag(R.id.roomId, chatRoomList.get(position).getRoomId());
        convertView.setTag(R.id.friendId, chatRoomList.get(position).getFriendId());
        convertView.setTag(R.id.roomName, chatRoomList.get(position).getRoomName());


        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return chatRoomList.get(position) ;
    }


    public void ChangeList(ArrayList<ChatRoomItem> ListData){
        this.chatRoomList = ListData;
    }



}
