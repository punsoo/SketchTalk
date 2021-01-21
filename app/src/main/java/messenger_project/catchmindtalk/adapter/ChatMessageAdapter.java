package messenger_project.catchmindtalk.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.squareup.picasso.Picasso;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import messenger_project.catchmindtalk.Item.ChatMessageItem;
import messenger_project.catchmindtalk.MyDatabaseOpenHelper;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.activity.MsgDeleteCopyActivity;
import messenger_project.catchmindtalk.viewholder.MessageViewHolder;

public class ChatMessageAdapter extends BaseAdapter {


    public ArrayList<ChatMessageItem> chatMessageList = new ArrayList<ChatMessageItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public MyDatabaseOpenHelper db;
    public String myId;
    public int roomId;
    public SimpleDateFormat sdfTime ;
    public SimpleDateFormat sdfDate ;
    public static final int DeleteImage = 3102;
    public static final int DeleteMessage = 2013;
    public String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";

    // ListViewAdapter의 생성자
    public ChatMessageAdapter(Context context,ArrayList<ChatMessageItem> ListData,String myId ,int roomId, String friendId) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.chatMessageList = ListData;
        this.myId = myId;
        this.roomId = roomId;
        this.sdfTime = new SimpleDateFormat("HH:mm");
        this.sdfDate = new SimpleDateFormat("yyyy년 MM월 dd일 E요일");
        db = new MyDatabaseOpenHelper(mContext,"catchMindTalk",null,1);
    }

    public void setChatRoomList(ArrayList<ChatMessageItem> ListData) {
        this.chatMessageList = ListData;
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }



    @Override
    public int getItemViewType(int position) {
        return chatMessageList.get(position).getType();
    }

    public void deleteMessage(int position){
        this.chatMessageList.remove(position);
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return chatMessageList.size();
    }




    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        MessageViewHolder viewHolder;
        String friendId = "";
        String profileIUT = "";

        friendId = chatMessageList.get(position).getUserId();
        String preId = "";
        String nextId ="";
        profileIUT = chatMessageList.get(position).getProfileImageUpdateTime();
        String time = chatMessageList.get(position).getTime();
        String preTime = "";
        String nextTime ="";
        String day = chatMessageList.get(position).getDay();
        String preDay = "";
        String nextDay ="";

        Log.d("호출",position+"#"+friendId+"#"+chatMessageList.get(position).getMsgContent()+"#"+chatMessageList.get(position).getType());
        if (position != getCount() - 1) {
            nextId = chatMessageList.get(position + 1).getUserId();
            nextTime = chatMessageList.get(position + 1).getTime();
            nextDay = chatMessageList.get(position + 1).getDay();
        }
        if (position != 0) {
            preId = chatMessageList.get(position - 1).getUserId();
            preTime = chatMessageList.get(position - 1).getTime();
            preDay = chatMessageList.get(position - 1).getDay();
        }


        int msgType = getItemViewType(position);


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null || convertView.getTag(R.id.viewType) == null ||(Integer)convertView.getTag(R.id.viewType) != msgType) {
            viewHolder = new MessageViewHolder();

            if(msgType == 1) {
                convertView = this.inflater.inflate(R.layout.chatmessage_item_left, parent, false);
                viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.msgProfileImage);
                viewHolder.nickname = (TextView) convertView.findViewById(R.id.msgNickname);
                viewHolder.msgContent = (TextView) convertView.findViewById(R.id.msgContentLeft);
                viewHolder.msgContent.setBackgroundResource(R.drawable.inchat);
                viewHolder.time = (TextView) convertView.findViewById(R.id.msgTimeLeft);
                viewHolder.unRead = (TextView) convertView.findViewById(R.id.unReadLeft);

            }else if(msgType == 2) {
                convertView = this.inflater.inflate(R.layout.chatmessage_item_right, parent, false);
                viewHolder.msgContent = (TextView) convertView.findViewById(R.id.msgContentRight);
                viewHolder.msgContent.setBackgroundResource(R.drawable.outchat);
                viewHolder.time = (TextView) convertView.findViewById(R.id.msgTimeRight);
                viewHolder.unRead = (TextView) convertView.findViewById(R.id.unReadRight);

            }else if(msgType == 3 || msgType == 4 || msgType == 5){
                convertView = this.inflater.inflate(R.layout.chatmessage_item_center, parent, false);
                viewHolder.msgContent = (TextView) convertView.findViewById(R.id.msgCenterContent);
            }


            convertView.setTag(viewHolder);

        }else{
            viewHolder = (MessageViewHolder) convertView.getTag();
        }

        convertView.setTag(R.id.viewType,msgType);



            if (msgType == 1) {

                try {
                    Glide.with(mContext).load(ServerURL + "/profile_image/" + friendId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(profileIUT))
                            .into(viewHolder.profileImage);
                } catch (NullPointerException e) {
                    Log.d("ChatMessageAdapter", "NullpointerException, " + friendId);
                }
                viewHolder.nickname.setText(chatMessageList.get(position).getNickname());
                viewHolder.profileImage.setVisibility(View.VISIBLE);
                viewHolder.nickname.setVisibility(View.VISIBLE);
                if ((position != 0) && friendId.equals(preId) && day.equals(preDay) && time.equals((preTime)) && chatMessageList.get(position - 1).getType() == 1) {
                    viewHolder.profileImage.setVisibility(View.INVISIBLE);
                    viewHolder.nickname.setVisibility(View.GONE);
                }

                viewHolder.msgContent.setText(chatMessageList.get(position).getMsgContent());
                viewHolder.time.setText(time);
                viewHolder.time.setVisibility(View.GONE);
                if ((position == getCount() - 1) || !friendId.equals(nextId) || !day.equals(nextDay) || !time.equals(nextTime)) {
                    viewHolder.time.setVisibility(View.VISIBLE);
                }

                long dateTime = chatMessageList.get(position).getDateTime();
                if (roomId == 0) {
                    viewHolder.unRead.setText("");
                } else {
                    int unRead = db.getMessageUnReadNum(myId, roomId, friendId, dateTime);
                    if (unRead <= 0) {
                        viewHolder.unRead.setText("");
                    } else {
                        viewHolder.unRead.setText(unRead + "");
                    }
                }

            } else if (msgType == 2) {

                viewHolder.msgContent.setText(chatMessageList.get(position).getMsgContent());
                viewHolder.time.setText(chatMessageList.get(position).getTime());
                viewHolder.time.setText(time);
                viewHolder.time.setVisibility(View.GONE);
                if ((position == getCount() - 1) || !friendId.equals(nextId) || !day.equals(nextDay) || !time.equals(nextTime) || chatMessageList.get(position + 1).getType() != 2) {
                    viewHolder.time.setVisibility(View.VISIBLE);
                }
                long dateTime = chatMessageList.get(position).getDateTime();
                int unRead = db.getMessageUnReadNum(myId, roomId, friendId, dateTime);
                if (unRead <= 0) {
                    viewHolder.unRead.setText("");
                } else {
                    viewHolder.unRead.setText(unRead + "");
                }
            } else if (msgType == 3) {
                viewHolder.msgContent.setText(chatMessageList.get(position).getDay());
            } else if (msgType == 4 || msgType == 5) {
                viewHolder.msgContent.setText(chatMessageList.get(position).getMsgContent());
            }


            convertView.setTag(R.id.index,position);
            convertView.setTag(R.id.userId,friendId);
            convertView.setTag(R.id.time,chatMessageList.get(position).getDateTime());

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                int pos = (int) v.getTag(R.id.index);
                String friendId = (String)v.getTag(R.id.userId);
                long time = (long)v.getTag(R.id.time);
                boolean preMsgDelete= false;
                if(pos != 0 && (chatMessageList.get(pos-1).getType()==3 && (chatMessageList.size() == (pos+1) || !chatMessageList.get(pos+1).getDay().equals( chatMessageList.get(pos).getDay() )))){
                    preMsgDelete = true;
                };
                Intent intent = new Intent(mContext, MsgDeleteCopyActivity.class );

                intent.putExtra("roomId",roomId);
                intent.putExtra("myId",myId);
                intent.putExtra("friendId",friendId);
                intent.putExtra("time",time);
                intent.putExtra("position",pos);
                intent.putExtra("preMsgDelete",preMsgDelete);

                if(chatMessageList.get(pos).Type == 51 || chatMessageList.get(pos).Type == 52 ){

                    intent.putExtra("subType","image");
                    intent.putExtra("msgContent",ServerURL+"/sendImage/"+chatMessageList.get(pos).getMsgContent());

                }else{

                    intent.putExtra("subType","text");
                    intent.putExtra("msgContent",chatMessageList.get(pos).getMsgContent());

                }

                ((Activity)mContext).startActivityForResult(intent,DeleteMessage);
                return true;
            }
        });

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
        return chatMessageList.get(position) ;
    }



    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
//    public void addItem(int type, String nickname, String content, String time) {
//        ChatMessageItem item = new ChatMessageItem(type, nickname, content, time);
//
//        chatMessageList.add(item);
//    }

}
