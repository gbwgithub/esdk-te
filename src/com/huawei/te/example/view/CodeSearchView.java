package com.huawei.te.example.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.huawei.esdk.te.util.LayoutUtil;


/**
 *  联系人右边字母选择器
 */
public class CodeSearchView extends View implements OnTouchListener
{
    
    private final String[] codes = {"#","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O",
                              "P","Q","R","S","T","U","V","W","X","Y","Z"};
    
    /**
     * 字母对应的Rect
     */
    private RectF[] codesRect;
    /**
     * 字母的高
     */
    private float codeHeight = 20 * LayoutUtil.getInstance().getScreenPXScale();
    
    /**
     * 字母间的间隙
     */
    private int gip = Float.valueOf(4 * LayoutUtil.getInstance().getScreenPXScale()).intValue();
    //begin add by cwx176935 reason: DTS2013112105526 联系人右边快速定位
    
    /**
     * 字母距离顶部
     */
    private final int topGip = Float.valueOf(10 * LayoutUtil.getInstance().getScreenPXScale()).intValue();    
    /**
     * 字体距离的高度 
     */
    private float gipHeight = 0;
    
    /**
     *  字体距离的宽度
     */
    private float gipWidth = 0;
    
    /**
     * 
     */
    private float txtSize = 0;
    
    /**
     * 未选中的颜色
     */
    private int comColor = Color.rgb(34, 124, 209);
    
    
    /**
     * 画笔
     */
    private final Paint paint = new Paint();
    
    /**
     * view的高度
     */
    private int viewHeight;
    
    /**
     * view的宽度
     */
    private int viewWidth;
    
    /**
     * 初始高度
     */
    private int viewInitHeight;
    
    /**
     * 选中的回调类
     */
    private CodeMoveTouch codeServer = null;
    
    /**
     * 选中索引
     */
    private int selectIndex = -1;
    
    /**
     * 是否已经创建过
     */
    private boolean show = false;
    
    /**
     * 特殊处理，是否在搜索中 
     */
    private boolean isSearch = false;
    
    /**
     * 是否已经被选中
     */
    private boolean hasSelected = false;
    
    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            codeServer.codeSelect(codes[msg.what]);
        }
        
    };
    
    /**
     * 构造函数
     * @param context 上下文
     */
    public CodeSearchView(Context context)
    {
        this(context,null);
    }
    
    /**
     * 构造函数
     * @param context 上下文
     */
    public CodeSearchView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
    
    /**
     *构造函数
     * @param context 上下文
     */
    public CodeSearchView(Context context, AttributeSet attrs)
    {
        this(context, attrs,0);
    }
    
    /**
     * 初始化  将Rect初始化
     */
    private void init()
    {
        // 联系人右边快速定位
        codesRect = new RectF[codes.length];
        
        for (int i = 0; i < codes.length; i++)
        {
            codesRect[i] = new RectF(0 + topGip, i * codeHeight, viewWidth,
                    (i + 1) * codeHeight + topGip);
        }
        
        /**
         * 设置Listener
         */
        this.setOnTouchListener(this);
    }
    
    /**
     * 设置回调Listener
     *
     * @param codeMoveTouch 回调器
     */
    public void setCodeMoveTouchServer(CodeMoveTouch codeMoveTouch)
    {
        this.codeServer = codeMoveTouch;
    }
    
    /**
     * 布局改变
     * @param changed 是否改变
     * @param left 左坐标
     * @param top 上坐标
     * @param right 右坐标
     * @param bottom 下坐标
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);
        if (hasSelected)
        {
            return;
        }
        int height = bottom - top;
        int width = right - left;
        if (!show)
        {
            viewInitHeight = height;
        }
        //如果已经是搜索的位置，则不再重新计算
        if (isSearch && viewInitHeight == height)
        {
            return;
        }
        reCalSize(width, height);
    }
    
    /**
     * @param width
     * @param height
     */
    public void reCalSize(int width, int height)
    {
        this.viewHeight = height;
        
        viewWidth = width;
        viewHeight = height;
        
        this.measure(viewWidth, viewHeight);
        
        // 联系人右边快速定位
        codeHeight = ((height - topGip / 2) / (codes.length) - 1);
        
        if (Float.valueOf(codeHeight).intValue() > Float.valueOf(viewWidth).intValue())
        {
            android.view.ViewGroup.LayoutParams params = this.getLayoutParams();
            params.width = Float.valueOf(codeHeight).intValue();
            this.setLayoutParams(params);
        }
        
        if (codeHeight >= viewWidth)
        {
            txtSize = viewWidth;
            gipWidth = 0.5f;
            gipHeight = (codeHeight - viewWidth) / 2 + 0.5f;
        }
        else
        {
            txtSize = codeHeight;
            gipWidth = (viewWidth - codeHeight) / 2 + 0.5f;
            gipHeight = 0.5f;
        }
        codeHeight += 0.5f;
        init();
        paint.setTextSize(txtSize);
        //end modify by cwx176935 reason: DTS2013112105526 联系人右边快速定位
        paint.setTextAlign(Align.CENTER);
        paint.setDither(true);
        this.invalidate();
        show = true;
    }
    
    /**
     * 计算Y轴
     * @param reduce 增加或减少量
     */
    public void zoomHeight(int reduce)
    {
        if (reduce > 0 && viewInitHeight < viewHeight + reduce)
        {
            return;
        }
        
        if (reduce < 0 && viewHeight - viewInitHeight <= reduce)
        {
            return;
        }
        
        reCalSize(viewWidth, viewHeight + reduce);
    }
    
    /**
     * 画图
     * @param canvas 画布
     */
    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawColor(Color.TRANSPARENT);
        for (int i = 0;i < codes.length;i++)
        {
            paint.setColor(comColor);
            // 联系人右边快速定位
            canvas.drawText(codes[i], gipWidth + txtSize / 2,
                    topGip + (i + 1) * codeHeight - gipHeight - gip, paint);
        }
        
    }
    
    /**
     * 点击操作
     * @param v view
     * @param event 点击事件
     */
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        float y = event.getY() - codesRect[0].top;
        
        int eventAction = event.getAction();
        
        if (eventAction == MotionEvent.ACTION_MOVE || eventAction == MotionEvent.ACTION_DOWN)
        {
            // 联系人右边快速定位
            selectIndex = Float.valueOf(y / codeHeight).intValue();
            if (selectIndex <= 0)
            {
                selectIndex = 0;
            }
            else if (selectIndex >= codesRect.length)
            {
                selectIndex = codesRect.length - 1;
            }
            hasSelected = true;
            handler.sendEmptyMessage(selectIndex);
            CodeSearchView.this.invalidate();
        }
        else if (eventAction == MotionEvent.ACTION_UP)
        {
            //当手势抬起 重置selectIndex 并重画一次
            selectIndex = -1;
            this.invalidate();
            hasSelected = false;
        }
        
        return true;
    }
    
    /**
     * 是否选中
     * @return true 选中
     */
    public boolean isHasSelected()
    {
        return hasSelected;
    }
    
    
    public int getViewHeight()
    {
        return viewHeight;
    }

    public int getViewWidth()
    {
        return viewWidth;
    }

    
    public boolean isSearch()
    {
        return isSearch;
    }

    public void setSearch(boolean isSearch)
    {
        this.isSearch = isSearch;
    }

    /**
     * 界面改变
     * @param w width 
     * @param h height
     * @param oldw old width
     * @param oldh old height
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    
    /**
     * 点击事件回调
     */
    public interface CodeMoveTouch
    {
        /**
         * 回调事件
         */
        void codeSelect(String code);
    }

}
