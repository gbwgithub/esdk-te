package com.huawei.te.example.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 *
 * ListView item中的ImageView类
 * 用于listView中的ImageView组件在item点击的时候不会被点击
 */
public class ListItemImageView extends ImageView
{

    public ListItemImageView(Context context)
    {
        super(context);
    }

    public ListItemImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public ListItemImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void setPressed(boolean pressed)
    {
        //设置父组件被点击的时候自身不被点击
        if (pressed && getParent() instanceof View && ((View) getParent()).isPressed()) 
        {
            return;
        }
        super.setPressed(pressed);
    }
}
