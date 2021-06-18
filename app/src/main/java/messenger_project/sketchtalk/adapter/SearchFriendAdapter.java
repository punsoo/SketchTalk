package messenger_project.sketchtalk.adapter;


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

import messenger_project.sketchtalk.Item.SearchFriendItem;
import messenger_project.sketchtalk.R;
import messenger_project.sketchtalk.viewholder.FriendViewHolder;

/**
 * Created by sonsch94 on 2017-09-22.
 */

public class SearchFriendAdapter extends BaseAdapter {

    public ArrayList<SearchFriendItem> SearchFriendList = new ArrayList<SearchFriendItem>() ;
    public Context mContext;
    public LayoutInflater inflater ;
    public String ServerURL ;

    public SearchFriendAdapter(Context context, ArrayList<SearchFriendItem> searchFriendList) {

        this.mContext = context;
        this.inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.SearchFriendList = searchFriendList;
        this.ServerURL = context.getResources().getString(R.string.ServerUrl);

    }

    public void clearList(){
        this.SearchFriendList = new ArrayList<>();
    }

    public void addSFItem(SearchFriendItem addItem){
        this.SearchFriendList.add(addItem);
    }

    @Override
    public int getCount() {

        return SearchFriendList.size();

    }



    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        FriendViewHolder viewHolder;
        String friendId = "";
        String nickname = "";
        String profileMessage = "";
        String profileImageUpdateTime = "";


        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {

            convertView = this.inflater.inflate(R.layout.search_friend_item, parent, false);

            viewHolder = new FriendViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.profile_image_search);
            viewHolder.nickname = (TextView) convertView.findViewById(R.id.textView1_search);
            viewHolder.profileMessage = (TextView) convertView.findViewById(R.id.textView2_search);

            convertView.setTag(viewHolder);

        }else{

            viewHolder = (FriendViewHolder) convertView.getTag();

        }


        friendId = SearchFriendList.get(position).getFriendId();
        nickname = SearchFriendList.get(position).getNickname();
        profileMessage = SearchFriendList.get(position).getProfileMessage();
        profileImageUpdateTime = SearchFriendList.get(position).getProfileImageUpdateTime();


        viewHolder.nickname.setText(nickname);
        viewHolder.profileMessage.setText(profileMessage);


        if(profileImageUpdateTime.equals("none")){

            viewHolder.icon.setImageResource(R.drawable.default_profile_image);

        }else {

            Glide.with(mContext).load(ServerURL+"/profile_image/" + friendId + ".png")
                    .error(R.drawable.default_profile_image)
                    .signature(new ObjectKey(profileImageUpdateTime))
                    .into(viewHolder.icon);

        }


        convertView.setTag(R.id.userId,friendId);
        convertView.setTag(R.id.nickname,nickname);
        convertView.setTag(R.id.profileImageUpdateTime,profileImageUpdateTime);

//        convertView.setTag(R.id.checkIcon,viewHolder.check);

        Log.d("SearchFriendAdapter",""+position);

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
        return SearchFriendList.get(position) ;
    }

}
