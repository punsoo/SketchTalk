package messenger_project.catchmindtalk.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.ArrayList;

import messenger_project.catchmindtalk.Item.FriendListItem;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.viewholder.FriendViewHolder;

public class FriendListAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    public String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";
    public FriendListItem MyProfile;
    public ArrayList<FriendListItem> listViewItemList = new ArrayList<>() ;
    public ArrayList<FriendListItem> FlistViewItemList = new ArrayList<>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public int FlistSize;
    public int listSize;


    // ListViewAdapter의 생성자
    public FriendListAdapter(Context context,FriendListItem MyData,ArrayList<FriendListItem> FListData,ArrayList<FriendListItem> ListData ) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listViewItemList = ListData;
        this.FlistViewItemList = FListData;
        this.listSize = ListData.size();
        this.FlistSize = FListData.size();
        this.MyProfile = MyData;

    }

    public void setListViewItemList(ArrayList<FriendListItem> ListData) {
        this.listViewItemList = ListData;
    }

    public void setFListViewItemList(ArrayList<FriendListItem> FListData) {
        this.FlistViewItemList = FListData;
    }

    public void changeMyItem(FriendListItem MyItem){
        this.MyProfile = MyItem;
    }

    public void sizeReset(){
        this.listSize = listViewItemList.size();
        this.FlistSize = FlistViewItemList.size();
    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        int total;
        if(FlistSize >0){
            total = 2+1+FlistSize+1+listSize;
        }else{
            total = 2+1+listSize;
        }
        return total ;
    }

//    @Override
//    public boolean isEnabled(int position) {
//       if(position <5){
//           return false;
//       }else{
//           return true;
//       }
//    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FriendViewHolder viewHolder;
        String userId = "";
        String nickname = "";
        String profileMessage = "";
        String profileImageUpdateTime = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {

            convertView = this.inflater.inflate(R.layout.friendlist_item, parent, false);


            viewHolder = new FriendViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profileImage);
            viewHolder.nickname = (TextView) convertView.findViewById(R.id.nickname);
            viewHolder.profileMessage = (TextView) convertView.findViewById(R.id.profileMessage);
            viewHolder.section = (LinearLayout) convertView.findViewById(R.id.sectionHeader);
            viewHolder.sectionTxt = (TextView) convertView.findViewById(R.id.sectionText);
            viewHolder.profile_container = (RelativeLayout) convertView.findViewById(R.id.profile_container);


            convertView.setTag(viewHolder);

        }else{

            viewHolder = (FriendViewHolder) convertView.getTag();

        }

        if(position ==0){

            viewHolder.sectionTxt.setText(R.string.my_profile);
            viewHolder.section.setVisibility(View.VISIBLE);
            viewHolder.profile_container.setVisibility(View.GONE);

        }else if(position == 1){

            viewHolder.nickname.setText(MyProfile.getNickname());
            viewHolder.profileMessage.setText(MyProfile.getProfileMessage());
            viewHolder.section.setVisibility(View.GONE);
            viewHolder.profile_container.setVisibility(View.VISIBLE);
            userId = MyProfile.getId();
            nickname = MyProfile.getNickname();
            profileMessage = MyProfile.getProfileMessage();
            profileImageUpdateTime = MyProfile.getProfileImageUpdateTime();
            Glide.with(mContext).load(ServerURL+"/profile_image/" + userId + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                    .into(viewHolder.icon);

        }else{

            if (this.FlistSize > 0) {

                if (position == 2) {
                    viewHolder.sectionTxt.setText(R.string.bookmark);
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else if (position < (3 + FlistSize)) {

                    viewHolder.nickname.setText(FlistViewItemList.get(position - 3).getNickname());
                    viewHolder.profileMessage.setText(FlistViewItemList.get(position - 3).getProfileMessage());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = FlistViewItemList.get(position-3).getId();
                    nickname = FlistViewItemList.get(position-3).getNickname();
                    profileMessage = FlistViewItemList.get(position-3).getProfileMessage();
                    profileImageUpdateTime = FlistViewItemList.get(position-3).getProfileImageUpdateTime();
                    if(profileImageUpdateTime.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                    }else {
                        Glide.with(mContext).load(ServerURL+"/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                                .into(viewHolder.icon);
                    }

                } else if (position == (3 + FlistSize)) {
                    viewHolder.sectionTxt.setText(R.string.friend);
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else {
                    viewHolder.nickname.setText(listViewItemList.get(position - 4 - FlistSize).getNickname());
                    viewHolder.profileMessage.setText(listViewItemList.get(position - 4 - FlistSize).getProfileMessage());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = listViewItemList.get(position-4-FlistSize).getId();
                    nickname = listViewItemList.get(position-4-FlistSize).getNickname();
                    profileMessage = listViewItemList.get(position-4-FlistSize).getProfileMessage();
                    profileImageUpdateTime = listViewItemList.get(position-4-FlistSize).getProfileImageUpdateTime();
                    if(profileImageUpdateTime.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                    }else {
                        Glide.with(mContext).load(ServerURL+"/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                                .into(viewHolder.icon);
                    }
                }


            } else {

                if (position == 2) {

                    viewHolder.sectionTxt.setText(R.string.friend);
                    viewHolder.section.setVisibility(View.VISIBLE);
                    viewHolder.profile_container.setVisibility(View.GONE);

                } else {

                    viewHolder.nickname.setText(listViewItemList.get(position - 3).getNickname());
                    viewHolder.profileMessage.setText(listViewItemList.get(position - 3).getProfileMessage());
                    viewHolder.section.setVisibility(View.GONE);
                    viewHolder.profile_container.setVisibility(View.VISIBLE);
                    userId = listViewItemList.get(position-3).getId();
                    nickname = listViewItemList.get(position-3).getNickname();
                    profileMessage = listViewItemList.get(position-3).getProfileMessage();
                    profileImageUpdateTime = listViewItemList.get(position-3).getProfileImageUpdateTime();

                    if(profileImageUpdateTime.equals("none")){
                        viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                    }else {
                        Glide.with(mContext).load(ServerURL+"/profile_image/" + userId + ".png")
                                .error(R.drawable.default_profile_image)
                                .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                                .into(viewHolder.icon);
                    }

                }

            }

        }

        convertView.setTag(R.id.userId,userId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profileImageUpdateTime,profileImageUpdateTime);
        convertView.setTag(R.id.profileMessage,profileMessage);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position ;
    }


    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }


    public void ChangeList(ArrayList<FriendListItem> FListData, ArrayList<FriendListItem> ListData){
        this.FlistViewItemList = FListData;
        this.listViewItemList = ListData;
    }


}

