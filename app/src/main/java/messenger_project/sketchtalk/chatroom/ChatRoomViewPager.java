package messenger_project.sketchtalk.chatroom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class ChatRoomViewPager extends ViewPager {

    public static boolean DrawMode = false;
    public ChatRoomViewPager(Context context) {
        super(context);
    }
    public ChatRoomViewPager(Context context, AttributeSet attrs){
        super(context,attrs);
    }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return DrawMode ? false : super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return DrawMode ? false : super.onTouchEvent(event);
    }

}