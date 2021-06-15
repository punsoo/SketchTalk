package messenger_project.catchmindtalk.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.ArrayList;

import messenger_project.catchmindtalk.Item.MemberListItem;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.viewholder.memberListViewHolder;

public class MemberListAdapter extends BaseAdapter {



    public ArrayList<MemberListItem> MemberListItemList = new ArrayList<MemberListItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public String ServerURL;


    public MemberListAdapter(Context context,ArrayList<MemberListItem> MemberList) {

        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.MemberListItemList = MemberList;
        this.ServerURL = context.getResources().getString(R.string.ServerUrl);

    }

    public void clearList(){
        this.MemberListItemList = new ArrayList<>();
    }

    public void addMemberItem(MemberListItem addItem){
        this.MemberListItemList.add(addItem);
    }

    @Override
    public int getCount() {

        return MemberListItemList.size();

    }



    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        memberListViewHolder viewHolder;
        String userId = "";
        String nickname = "";
        String profileImageUpdateTime = "";



        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {

            convertView = this.inflater.inflate(R.layout.chatroom_member_item, parent, false);

            viewHolder = new memberListViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profile_image_memberList);
            viewHolder.nickname = (TextView) convertView.findViewById(R.id.nickname_memberList);
            viewHolder.addFriendBtn = (ImageView) convertView.findViewById(R.id.add_friend_icon);

            convertView.setTag(viewHolder);

        }else{
            viewHolder = (memberListViewHolder) convertView.getTag();

        }


        userId = MemberListItemList.get(position).getUserId();
        nickname = MemberListItemList.get(position).getNickname();
        profileImageUpdateTime = MemberListItemList.get(position).getProfileImageUpdateTime();

        viewHolder.nickname.setText(nickname);


        if(profileImageUpdateTime.equals("none")){

            viewHolder.icon.setImageResource(R.drawable.default_profile_image);

        }else {

            Glide.with(mContext).load(ServerURL + userId + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new ObjectKey(profileImageUpdateTime))
                    .into(viewHolder.icon);

        }


        convertView.setTag(R.id.userId,userId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profileImageUpdateTime,profileImageUpdateTime);

//        convertView.setTag(R.id.checkIcon,viewHolder.check);

        Log.d("MemberListAdapter",""+position);

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
        return MemberListItemList.get(position) ;
    }


    public void ChangeList( ArrayList<MemberListItem> ListData){

        this.MemberListItemList = ListData;

    }
}
