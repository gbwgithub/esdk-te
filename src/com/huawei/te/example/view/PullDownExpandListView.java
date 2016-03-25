package com.huawei.te.example.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;

import com.huawei.esdk.te.util.LogUtil;

/**
 * 用于辅助下拉的扩展list
 */
public class PullDownExpandListView extends ExpandableListView implements OnGroupExpandListener
{
    private static final String TAG = PullDownExpandListView.class.getSimpleName();
    
    public static final int SMOOTH_EXP_GROUP = 0;
    
    public static final int SMOOTH_EXP_LAST_INDEX = 1;
    
    /**
     * 上次的最后点击
     */
    private int lastClickIndex = -1;
    
    /**
     * 是否已经展开
     */
    private boolean isExp;
    
    /**
     * 构造器
     * @param context 上下文
     * @param attrs 属性
     */
    public PullDownExpandListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.setOnGroupExpandListener(this);
    }

    @Override
    public int computeVerticalScrollOffset()
    {
        return super.computeVerticalScrollOffset();
    }

    /**
     * 记录上次点击
     */
    public void setLastClickIndex(int lastClickIndex)
    {
        this.lastClickIndex = lastClickIndex;
    }

    /**
     * 设置是否已经展开
     */
    public void setExp(boolean isExp)
    {
        this.isExp = isExp;
    }

    @Override
    public void onGroupExpand(int groupPosition)
    {
        handler.sendEmptyMessageDelayed(SMOOTH_EXP_GROUP, 150);
    }
    
    private Handler handler = new Handler()
    {

        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if (SMOOTH_EXP_GROUP == msg.what)
            {
                if (isExp)
                {
                    if (getLastVisiblePosition() - lastClickIndex <= 1
                            && getLastVisiblePosition() - lastClickIndex >= 0
                            && lastClickIndex != 0)
                    {
                        //                        smoothScrollByOffset(1);
                        smoothScrollToPosition(lastClickIndex + 1);
                    }
                }
            }
            else if (SMOOTH_EXP_LAST_INDEX == msg.what)
            {
                int lastVisibleCount = msg.arg1;
                int nowIndex = msg.arg2;
                int showIndex = nowIndex - lastVisibleCount;
                showIndex = showIndex < 0 ? 0 : showIndex;
                PullDownExpandListView.this.setSelectedGroup(showIndex);
            }
        }

    };

    /**
     * 按原先的位置
     * @param lastVisibleCount 上次显示离显示第一项的个数
     * @param nowIndex 现在的位置
     */
    public void smoothToLastExpIndex(int lastVisibleCount, int nowIndex)
    {
        Message msg = new Message();
        msg.what = SMOOTH_EXP_LAST_INDEX;
        msg.arg1 = lastVisibleCount;
        msg.arg2 = nowIndex;
        handler.sendMessageDelayed(msg, 200);
    }
    

    @Override
    protected void layoutChildren()
    {
        try
        {
            //自动登录被踢自动化测试出现崩溃问题，这里捕获异常
            super.layoutChildren();
        }
        catch (IllegalStateException e)
        {
            LogUtil.e(TAG, "layout children error.");
        }
    }
}
