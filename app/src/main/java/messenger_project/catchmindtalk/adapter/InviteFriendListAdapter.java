package messenger_project.catchmindtalk.adapter;


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.core.content.ContextCompat;
import messenger_project.catchmindtalk.Item.FriendListItem;
import messenger_project.catchmindtalk.R;
import messenger_project.catchmindtalk.viewholder.FriendViewHolder;

public class InviteFriendListAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList

    public ArrayList<FriendListItem> listViewItemList = new ArrayList<>() ;
    public ArrayList<FriendListItem> FlistViewItemList = new ArrayList<>() ;
    public ArrayList<String> alreadyList = new ArrayList<>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public int FlistSize;
    public HashMap<String, Boolean> isChecked = new HashMap<>();
    public String ServerURL = "http://ec2-54-180-196-239.ap-northeast-2.compute.amazonaws.com";

    // ListViewAdapter의 생성자
    public InviteFriendListAdapter(Context context,ArrayList<FriendListItem> FListData,ArrayList<FriendListItem> ListData ,HashMap<String,Boolean> IC , ArrayList<String> AlreadyList) {
        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listViewItemList = ListData;
        this.FlistViewItemList = FListData;
        this.FlistSize = FlistViewItemList.size();
        this.isChecked = IC;
        this.alreadyList = AlreadyList;
    }

    public void changeIsChecked(String userId){

        if(this.isChecked.get(userId)){
            this.isChecked.put(userId,false);
        }else{
            this.isChecked.put(userId,true);
        }

    }

    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        int total;
        if(FlistSize >0){
            total = 1+FlistSize+1+listViewItemList.size();
        }else{
            total = 1+listViewItemList.size();
        }
        return total ;
    }



    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FriendViewHolder viewHolder;
        String userId = "";
        String nickname = "";
        String profileImageUpdateTime = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {

            convertView = this.inflater.inflate(R.layout.friendlist_item_check, parent, false);


            viewHolder = new FriendViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profile_image_check);
            viewHolder.nickname = (TextView) convertView.findViewById(R.id.textView1_check);
            viewHolder.section = (LinearLayout) convertView.findViewById(R.id.sectionHeader_check);
            viewHolder.sectionTxt = (TextView) convertView.findViewById(R.id.sectionText_check);
            viewHolder.profile_container = (RelativeLayout) convertView.findViewById(R.id.profile_container_check);
            viewHolder.check = (ImageView) convertView.findViewById(R.id.check_image);
            viewHolder.api_frame = (FrameLayout) convertView.findViewById(R.id.api_frame);

            convertView.setTag(viewHolder);

        }else{

            viewHolder = (FriendViewHolder) convertView.getTag();

        }

        convertView.setClickable(false);
        viewHolder.api_frame.setForeground(null);


        viewHolder.check.setImageResource(R.drawable.check_icon_inact);

        if (this.FlistSize > 0) {

            if (position == 0) {
                viewHolder.sectionTxt.setText("즐겨찾기");
                viewHolder.section.setVisibility(View.VISIBLE);
                viewHolder.profile_container.setVisibility(View.GONE);
                viewHolder.api_frame.setVisibility(View.GONE);
                viewHolder.check.setVisibility(View.GONE);

            } else if (position < (1 + FlistSize)) {

                viewHolder.nickname.setText(FlistViewItemList.get(position - 1).getNickname());
                viewHolder.section.setVisibility(View.GONE);
                viewHolder.profile_container.setVisibility(View.VISIBLE);
                viewHolder.api_frame.setVisibility(View.VISIBLE);
                viewHolder.check.setVisibility(View.VISIBLE);
                userId = FlistViewItemList.get(position-1).getId();
                nickname = FlistViewItemList.get(position-1).getNickname();
                profileImageUpdateTime = FlistViewItemList.get(position-1).getProfileImageUpdateTime();
                if(profileImageUpdateTime.equals("none")){
                    viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                }else {
                    Glide.with(mContext).load(ServerURL+"/profile_image/" + userId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                            .into(viewHolder.icon);
                }
                if(isChecked.get(userId)){
                    viewHolder.check.setImageResource(R.drawable.check_icon);
                }

            } else if (position == (1 + FlistSize)) {
                viewHolder.sectionTxt.setText("친구");
                viewHolder.section.setVisibility(View.VISIBLE);
                viewHolder.profile_container.setVisibility(View.GONE);
                viewHolder.api_frame.setVisibility(View.GONE);
                viewHolder.check.setVisibility(View.GONE);

            } else {
                viewHolder.nickname.setText(listViewItemList.get(position - 2 - FlistSize).getNickname());
                viewHolder.section.setVisibility(View.GONE);
                viewHolder.profile_container.setVisibility(View.VISIBLE);
                viewHolder.api_frame.setVisibility(View.VISIBLE);
                viewHolder.check.setVisibility(View.VISIBLE);
                userId = listViewItemList.get(position-2-FlistSize).getId();
                nickname = listViewItemList.get(position-2-FlistSize).getNickname();
                profileImageUpdateTime = listViewItemList.get(position-2-FlistSize).getProfileImageUpdateTime();
                if(profileImageUpdateTime.equals("none")){
                    viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                }else {
                    Glide.with(mContext).load(ServerURL+"/profile_image/" + userId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                            .into(viewHolder.icon);
                }
                if(isChecked.get(userId)){
                    viewHolder.check.setImageResource(R.drawable.check_icon);
                }
            }


        } else {

            if (position == 0) {
                viewHolder.sectionTxt.setText("친구");
                viewHolder.section.setVisibility(View.VISIBLE);
                viewHolder.profile_container.setVisibility(View.GONE);
                viewHolder.api_frame.setVisibility(View.GONE);
                viewHolder.check.setVisibility(View.GONE);

            } else {

                viewHolder.nickname.setText(listViewItemList.get(position - 1).getNickname());
                viewHolder.section.setVisibility(View.GONE);
                viewHolder.profile_container.setVisibility(View.VISIBLE);
                viewHolder.api_frame.setVisibility(View.VISIBLE);
                viewHolder.check.setVisibility(View.VISIBLE);
                userId = listViewItemList.get(position-1).getId();
                nickname = listViewItemList.get(position-1).getNickname();
                profileImageUpdateTime = listViewItemList.get(position-1).getProfileImageUpdateTime();
                if(profileImageUpdateTime.equals("none")){
                    viewHolder.icon.setImageResource(R.drawable.default_profile_image);
                }else {
                    Glide.with(mContext).load(ServerURL+"/profile_image/" + userId + ".png")
                            .error(R.drawable.default_profile_image)
                            .signature(new ObjectKey(String.valueOf(profileImageUpdateTime)))
                            .into(viewHolder.icon);
                }
                if(isChecked.get(userId)){
                    viewHolder.check.setImageResource(R.drawable.check_icon);
                }

            }

        }

        if(alreadyList.contains(userId)){
            convertView.setClickable(true);
            viewHolder.api_frame.setForeground(new ColorDrawable(ContextCompat.getColor(mContext, R.color.unClickable)));
        }

        convertView.setTag(R.id.userId,userId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profileImageUpdateTime,profileImageUpdateTime);


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
        return listViewItemList.get(position) ;
    }


    public void ChangeList(ArrayList<FriendListItem> FListData, ArrayList<FriendListItem> ListData){
        this.FlistViewItemList = FListData;
        this.listViewItemList = ListData;
    }


}
