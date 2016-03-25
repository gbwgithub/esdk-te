/*
 *    Copyright 2015 Huawei Technologies Co., Ltd. All rights reserved.
 *    eSDK is licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.te.example.call;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.huawei.esdk.te.util.LogUtil;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;
import com.huawei.te.example.activity.CallActivity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

/**
 * 通话状态改变控制类
 */
public class VoipCallModifyLogic
{
	private static final String TAG = VoipCallModifyLogic.class.getSimpleName();

	/**
	 * 消除提示框计时器
	 */
	private Timer dismisDialogTimer;

	private Handler handler = new Handler();

	/**
	 * 是否已经在显示
	 */
	private boolean showing = false;

	// begin add by cwx176935 reason:ANDRIOD-167 视频升级对话框
	// 协议栈为32秒超时,界面上25秒超时
	private static final int CANCLE_TIME = 25000;
	// end add by cwx176935 reason:ANDRIOD-167 视频升级对话框

	private final CallControl callConrol = CallControl.getInstance();

	// 确认、接受升级对话框
	private AlertDialog dialog;

	/**
	 * 通话升级视频通知
	 */
	public void voiceToVideo()
	{
		// getCurrentActivity 获取的不一定是HomeActivity 所以显示不出来
		// final BaseActivity ba = (BaseActivity)
		// ActivityStackManager.INSTANCE.getCurrentActivity();
		final CallActivity callActivity = CallActivity.getInstance();
		if (callActivity == null)
		{
			return;
		}
		callActivity.runOnUiThread(new Runnable()
		{

			@Override
			public void run()
			{
				LogUtil.i(TAG, "voiceToVideo alert dialog!~");
				// begin added by pwx178217 reason:返回按钮点击拒绝不消失
				OnClickListener ok = new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						cancelDisDiaTimer();
						// begin add by cWX176935 reason: 解决对话框消失慢
						Executors.newSingleThreadExecutor().execute(new Runnable()
						{
							@Override
							public void run()
							{
								if (null == Looper.myLooper())
								{
									Looper.prepare();
								}
//								VideoCaps caps = (VideoCaps) VideoHandler.getIns().initCallVideo(callActivity);
//								VideoCaps dataCaps = VideoHandler.getIns().getDataCaps();
								callConrol.agreeUpgradeVideo();
							}
						});
						// end add by cWX176935 reason: 解决对话框消失慢
					}
				};
				OnClickListener cancel = new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (callConrol != null)
						{
							cancelDisDiaTimer();
							callConrol.disAgreeUpgradeVideo();
						}
					}
				};
				OnDismissListener dismiss = new DialogInterface.OnDismissListener()
				{
					@Override
					public void onDismiss(DialogInterface dialog)
					{
						if (callConrol != null)
						{
							if (dismisDialogTimer != null)
							{
								cancelDisDiaTimer();
							}
						}
					}
				};

				// callActivity.showUpdataVideoDialog(ba.getString(R.string.msg_tip),
				// ba.getString(R.string.ntf_upgrade_videocall),
				// ba.getString(R.string.accept), ok,
				// ba.getString(R.string.refuse), cancel, dismiss);
				// startDisDiaTimer(null, cvoip);
				// //获取builder构造器
				// BuilderIOS builder = new BuilderIOS(ba);
				// builder.setTitle(R.string.msg_tip);
				// //设置提示信息
				// builder.setMessage(ba.getString(R.string.ntf_upgrade_videocall));
				// //设置确认按钮及点击事件
				// builder.setPositiveButton(ba.getString(R.string.accept), ok);
				// //设置（取消）拒绝按钮及点击事件
				// builder.setNegativeButton(ba.getString(R.string.refuse),
				// cancel);
				// //创建Dialog
				// dialogRender = builder.create();
				// //设置外部点击不消失
				// dialogRender.setCanceledOnTouchOutside(false);
				// //设置取消事件
				// dialogRender.setOnDismissListener(dismiss);
				// dialogRender.show();

				AlertDialog.Builder builder = new AlertDialog.Builder(callActivity);
				builder.setTitle(callActivity.getString(R.string.msg_tip));
				builder.setMessage(callActivity.getString(R.string.ntf_upgrade_videocall));
				builder.setPositiveButton(callActivity.getString(R.string.accept), ok);
				builder.setNegativeButton(callActivity.getString(R.string.refuse), cancel);
//				builder.setOnDismissListener(dismiss);
				dialog = builder.create();
				dialog.setOnDismissListener(dismiss);
				dialog.show();

				startDisDiaTimer();

			}
		});
	}

	/**
	 * 弹出通话升级视频视频失败对话框 询问是否稍后重试
	 */
	public void modifyRequestFalied()
	{
		final CallActivity callActivity = CallActivity.getInstance();
		if (callConrol == null || callActivity == null)
		{
			return;
		}
		callActivity.runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				LogUtil.i(TAG, "modifyRequestFalied alert!~");

				OnClickListener retry = new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						// 解决对话框消失慢
						handler.postAtFrontOfQueue(new Runnable()
						{
							/**
							 * 线程执行耗时工作
							 */
							@Override
							public void run()
							{
								callConrol.upgradeVideo();
							}
						});
					}
				};

				OnClickListener ok = new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder(callActivity);
				builder.setTitle(callActivity.getString(R.string.msg_tip));
				builder.setMessage(callActivity.getString(R.string.video_upgrade_failure_try_again));
				builder.setPositiveButton(callActivity.getString(R.string.retry), retry);
				builder.setNegativeButton(callActivity.getString(R.string.ok), ok);
				dialog = builder.create();
				dialog.show();
			}
		});
	}

	/**
	 * 响应对方取消升级视频操作
	 */
	public void modifyRequestCancel()
	{
		LogUtil.i(TAG, "modifyRequestCancel");
		final CallActivity callActivity = CallActivity.getInstance();
		if (null == callActivity)
		{
			return;
		}

		if (null == dialog)
		{
			return;
		}
		// 升级视频对话框取消显示
		if (dialog != null && dialog.isShowing() && !callActivity.isFinishing())
		{
			dialog.dismiss();
		}

		Toast.makeText(callActivity, callActivity.getString(R.string.cancel_video_update), Toast.LENGTH_LONG).show();
	}

	private void cancelDisDiaTimer()
	{
		if (dismisDialogTimer != null)
		{
			dismisDialogTimer.cancel();
			dismisDialogTimer = null;
			showing = false;
		}
	}

	/**
	 * 描述：开始自动取消提示框计时器
	 */
	private void startDisDiaTimer()
	{
		cancelDisDiaTimer();

		dismisDialogTimer = new Timer("Dismis Dialog");
		DismisDialogTimerTask dismisDialogTimerTask = new DismisDialogTimerTask();
		dismisDialogTimer.schedule(dismisDialogTimerTask, CANCLE_TIME);
	}

	/**
	 * 是否已经显示
	 * 
	 * @return true已经显示
	 * @since 1.1
	 * @history 2013-9-13 v1.0.0 cWX176935 create
	 */
	public boolean isShowing()
	{
		return showing;
	}

	private class DismisDialogTimerTask extends TimerTask
	{
		@Override
		public void run()
		{
			if (null != dialog)
			{
				dialog.dismiss();
			}
			// begin add by cwx176935 reason:ANDRIOD-167 视频升级对话框
			callConrol.disAgreeUpgradeVideo();
			LogUtil.d(TAG, "dialog time out disAgreeUpg");
		}

	}
}
