package com.huawei.te.example.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;
import android.widget.Toast;

public abstract class BaseActivity extends FragmentActivity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// 添加日志 加载的是哪个布局
		ActivityStackManager.INSTANCE.push(this);
	}

	@Override
	public void finish()
	{
		if (ActivityStackManager.INSTANCE.getLastShowActivity() == this)
		{
			ActivityStackManager.INSTANCE.setLastShowActivity(null);
		}
		
		
		ActivityStackManager.INSTANCE.popup(this);
		super.finish();

	}

	/**
	 * 不显示手机标题
	 */
	public void showScreenNotitle()
	{
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	/**
	 * 显示手机标题
	 */
	public void showScreenWithTitle()
	{
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	public void onBackPressed()
	{
		
		super.onBackPressed();
	}

	@Override
	protected void onDestroy()
	{
		ActivityStackManager.INSTANCE.popup(this);
		super.onDestroy();
	}

	@Override
	public boolean moveTaskToBack(boolean nonRoot)
	{
		return super.moveTaskToBack(nonRoot);
	}

	/**
	 * 清空 Activity初始化的数据
	 */
	public abstract void clearData();

	/**
	 * 弹出消息对话框
	 * 
	 * @param title
	 *            对话框 title
	 * @param msg
	 *            消息内容
	 * @param firstBtnInfo
	 *            第一个按钮描述
	 * @param firstBtnListener
	 *            第一个按钮事件
	 * @param secondBtnInfo
	 *            第二个按钮描述
	 * @param secondBtnListener
	 *            第二个按钮事件
	 * @param dismissListener
	 *            取消事件
	 */
	public void showAlertDialog(String title, String msg, String firstBtnInfo, DialogInterface.OnClickListener firstBtnListener, String secondBtnInfo,
			DialogInterface.OnClickListener secondBtnListener, DialogInterface.OnDismissListener dismissListener)
	{
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

}
