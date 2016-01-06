package com.huawei.te.example.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;

import com.huawei.common.Resource;
import com.huawei.esdk.te.util.LayoutUtil;

public class AddTouchEventUtil
{
    /**
     * 小窗口边框
     */
    private View leftView;
    private View rightView;
    private View topView;
    private View bottomView;
    
    /**
     * 本地视频显示区域
     */
    private RelativeLayout localVideoLayout;
    
	public AddTouchEventUtil(RelativeLayout localVideoLayout, View leftView, View rightView, View topView, View bottomView)
	{
		super();
		this.leftView = leftView;
		this.rightView = rightView;
		this.topView = topView;
		this.bottomView = bottomView;
		this.localVideoLayout = localVideoLayout;
	}

	private static int transFloatToInt(float floatValue)
	{
		return Float.valueOf(floatValue).intValue();
	}

    /**
     * 本远端切换时间
     */
    private long renderSwitchTick;

    /**
     * 本远端切换时间间隔 400毫秒
     */
    private static final int RENDERSWITCHINTERVAL = 400;
	
	/**
	 * 用于判断是否点击
	 */
	private long clickTime;

	/**
	 * 当前的X坐标值
	 */
	private int currentX;

	/**
	 * 当前的Y坐标值
	 */
	private int currentY;
	/**
	 * 以view的左上角为（0,0）点的点击处X坐标
	 */
	private int clickedX;

	/**
	 * 以view的左上角为（0,0）点的点击处Y坐标
	 */
	private int clickedy;
	/**
	 * 初始化时小窗口离屏幕左侧的距离
	 */
	private int viewX;

	/**
	 * 初始化时小窗口离屏幕顶端的距离
	 */
	private int viewY;

	/**
	 * 小窗口第一次被移动的标示位
	 */
	private boolean isFirstTime = true;

	/**
	 * 小窗口移动的X距离
	 */
	private int moveX;

	/**
	 * 小窗口移动的y距离
	 */
	private int moveY;

	/**
	 * 添加事件
	 * 
	 * @param view
	 *            需要添加的视图
	 */
	public void addTouchEvent(final View view)
	{
		view.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View touchedView, MotionEvent event)
			{
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					actionDown(event);
					break;
				case MotionEvent.ACTION_MOVE:
					boolean result = actionMove(event, view);
					if (result)
					{
						return true;
					}
					break;
				case MotionEvent.ACTION_UP:
					actionUp(event);
					break;
				default:
					break;

				}
				return true;
			}
		});
	}

	/**
	 * @param event
	 */
	private void actionDown(MotionEvent event)
	{
		leftView.setVisibility(View.GONE);
		rightView.setVisibility(View.GONE);
		topView.setVisibility(View.GONE);
		bottomView.setVisibility(View.GONE);

		if (isFirstTime)
		{
			// 小窗口的初始坐标
			viewX = transFloatToInt(event.getRawX() - event.getX());
			viewY = transFloatToInt(event.getRawY() - event.getY());
			isFirstTime = false;
		}
		// 解决半屏状态下本远端切换无效
		clickTime = System.currentTimeMillis();
		// 手指点击处的坐标
		currentX = transFloatToInt(event.getRawX());
		currentY = transFloatToInt(event.getRawY());
		// 以view的左上为（0,0）点的点击处坐标
		// if (!isFullScreen)
		// {
		// // 半屏时，要减去导航栏的高度，宽度
		// topHeight =
		// getActivity().findViewById(R.id.navigate_right).getHeight();
		// leftWidth =
		// getActivity().findViewById(R.id.navigation_bar).getWidth();
		// clickedX = transFloatToInt(event.getX()) + leftWidth;
		// clickedy = transFloatToInt(event.getY()) + topHeight;
		// return;
		// }
		// 全屏时
		clickedX = transFloatToInt(event.getX());
		clickedy = transFloatToInt(event.getY());
	}

	/**
	 * @param event
	 * @param touchedView
	 * @return
	 */
	private boolean actionMove(MotionEvent event, View touchedView)
	{
		int movedLengthX = transFloatToInt(event.getRawX());
		int movedLengthY = transFloatToInt(event.getRawY());
		// 离屏幕左侧的距离
		int left = transFloatToInt(movedLengthX - clickedX);
		// 离屏幕顶端的距离
		int top = transFloatToInt(movedLengthY - clickedy);
		// begin add by cwx176935 reason:ANDRIOD-141 视频中 点击本地图像切换，会无意中移动
		if (Math.abs(movedLengthX - currentX) <= 10 && Math.abs(movedLengthY - currentY) <= Resource.Num.TEN)
		{
			return true;
		}
		// end add by cwx176935 reason:ANDRIOD-141 视频中 点击本地图像切换，会无意中移动
		// 小窗口的宽度 （半屏时要加上导航栏的宽度）
		// int width = !isFullScreen ? touchedView.getWidth() + leftWidth :
		// touchedView.getWidth();
		int width = touchedView.getWidth();

		// 小窗口的高度（半屏时要加上导航栏的高度）
		// int height = !isFullScreen ? touchedView.getHeight() + topHeight :
		// touchedView.getHeight();
		int height = touchedView.getHeight();
		// 小窗口移动
		move(left, top, width, height, movedLengthX, movedLengthY);
		currentX = movedLengthX;
		currentY = movedLengthY;
		return false;
	}

	private void actionUp(MotionEvent event)
	{
		leftView.setVisibility(View.VISIBLE);
		rightView.setVisibility(View.VISIBLE);
		topView.setVisibility(View.VISIBLE);
		bottomView.setVisibility(View.VISIBLE);

		long curTick = System.currentTimeMillis();
		if (curTick - clickTime < 100)
		{

			if (Math.abs(curTick - renderSwitchTick) > RENDERSWITCHINTERVAL)
			{
				//TODO 控制本远端切换的暂时没加
				
//				if (ConfigApp.getInstance().isUsePadLayout() && isPdfView)
//				{
//					changeRenderInLocal();
//				} else
//				{
//					changeLocalRemote();
//				}

				renderSwitchTick = curTick;
			}
		}

		// 获取view的最终位置
		viewPosition(event);
	}

	/**
	 * 
	 * 移动小窗口
	 * 
	 * @param left
	 *            离屏幕左侧的距离
	 * @param top
	 *            离屏幕顶端的距离
	 * @param width
	 *            小窗口的宽度 （半屏时要加上导航栏的宽度）
	 * @param height
	 *            小窗口的高度（半屏时要加上导航栏的高度）
	 * @param clickedPointX
	 *            手指点击处得X坐标
	 * @param clickedPointY
	 *            手指点击处得Y坐标
	 */
	private void move(int left, int top, int width, int height, int clickedPointX, int clickedPointY)
	{
		int screenWidth = LayoutUtil.getInstance().getScreenWidth();
		int screenHeight = LayoutUtil.getInstance().getScreenHeight();
		if (screenWidth < screenHeight)
		{
			int tmpGip = screenWidth;
			screenWidth = screenHeight;
			screenHeight = tmpGip;
		}

		// 左上
		if (left <= 0 && top <= 0)
		{
			localVideoLayout.scrollBy(0, 0);
		}

		// 右上
		else if (left + width >= screenWidth && top <= 0)
		{
			localVideoLayout.scrollBy(0, 0);
		}

		// 左下
		else if (left <= 0 && top + height >= screenHeight)
		{
			localVideoLayout.scrollBy(0, 0);
		}

		// 右下
		else if (left + width >= screenWidth && top + height >= screenHeight)
		{
			localVideoLayout.scrollBy(0, 0);
		}

		// 左侧 右侧
		else if (left <= 0 || left + width >= screenWidth)
		{
			localVideoLayout.scrollBy(0, currentY - clickedPointY);
		}

		// 底部 顶部
		else if (top <= 0 || top + height >= screenHeight)
		{
			localVideoLayout.scrollBy(currentX - clickedPointX, 0);
		}
		// 正常
		else
		{
			localVideoLayout.scrollBy(currentX - clickedPointX, currentY - clickedPointY);
		}

	}

	/**
	 * view移动后的位置
	 */
	private void viewPosition(MotionEvent event)
	{
		// 离屏幕左侧的距离
		int resultLeft = transFloatToInt(event.getRawX()) - transFloatToInt(event.getX());
		// 离屏幕顶端的距离
		int resultTop = transFloatToInt(event.getRawY()) - transFloatToInt(event.getY());
		moveX = resultLeft - viewX;
		moveY = resultTop - viewY;
	}

}
