package com.huawei.te.example.view.ui;

import android.view.View;
import android.widget.TextView;

import com.huawei.te.example.R;

public class TabTitle
{
    /**
     * 根节点
     */
    private View rootView;

    /**
     * title控件
     */
    private TextView title;

    /**
     * 标题上的按钮
     */
    private View titleControlBtn;

    /**
     * 接口回调对象
     */
    private TabTitleServer tabTitleServer;

    private View titleControlBtnMore;

    /**
     * 构造方法
     * @param rootView 根节点
     * @param tabTitleServer 接口回调对象
     */
    public TabTitle(View rootView, TabTitleServer tabTitleServer)
    {
        this.rootView = rootView;
        this.tabTitleServer = tabTitleServer;
        initComp();
    }

    /**
     * 初始化
     */
    private void initComp()
    {
        title = (TextView) rootView.findViewById(R.id.tabTitleTxt);
        titleControlBtn = rootView.findViewById(R.id.labelSwitch);
        titleControlBtnMore = rootView.findViewById(R.id.contact_more);
        titleControlBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                tabTitleServer.onLabelSwitchClick(v);
            }
        });
    }

    /**
     * 获得根布局
     * @return 根节点
     */
    public View getRootView()
    {
        return rootView;
    }

    /**
     * 标题栏是否可用
     * @param enabled 是否可用
     */
    public void setLabelSwitchEnable(boolean enabled)
    {
        titleControlBtn.setEnabled(enabled);
        if (enabled)
        {
            titleControlBtn.setVisibility(View.VISIBLE);
            if (null != titleControlBtnMore)
            {
                titleControlBtnMore.getBackground().setAlpha(255);
                titleControlBtnMore.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            titleControlBtn.setVisibility(View.INVISIBLE);
            if (null != titleControlBtnMore)
            {
                titleControlBtnMore.getBackground().setAlpha(127);
                titleControlBtnMore.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 设置tab标题名
     * @param titleName 标题栏名
     */
    public void setTitleName(CharSequence titleName)
    {
        title.setText(titleName);
    }

    /**
     * 设置tab标题名
     * @param titleName 标题栏名
     * @param enabled tab标题栏是否可交互
     */
    public void setTitleName(CharSequence titleName, boolean enabled)
    {
        setTitleName(titleName);
        setLabelSwitchEnable(enabled);
    }

    /**
     * 重置控件
     */
    public void reset()
    {
    }

    /**
     * 类描述：Tab标题回调接口
     */
    public static interface TabTitleServer
    {

        /**
         * 联系人标签面板开关点击回调
         * @param view 当前的View
         */
        void onLabelSwitchClick(View view);
    }

}
