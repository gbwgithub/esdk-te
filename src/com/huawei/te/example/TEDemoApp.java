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

package com.huawei.te.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import com.huawei.application.BaseApp;
import com.huawei.common.CustomBroadcastConst;
import com.huawei.esdk.te.TESDK;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.te.example.activity.ActivityStackManager;
import com.huawei.te.example.activity.CallActivity;
import com.huawei.te.example.activity.CallComingActivity;

public class TEDemoApp extends Application
{
	//test123
	private static final String TAG = TEDemoApp.class.getSimpleName();

	public static String getTENumber()
	{
		return TENumber;
	}

	public static void setTENumber(String TENumber)
	{
		TEDemoApp.TENumber = TENumber;
	}

	/**
	 * 登录成功后存储的espaceNumber
	 */
	private static String TENumber;

	public static boolean isLdapEnterprise()
	{
		return isLdapEnterprise;
	}

	public static void setIsLdapEnterprise(boolean isLdapEnterprise)
	{
		TEDemoApp.isLdapEnterprise = isLdapEnterprise;
	}

	/**
	 * 是否为Ldap网络地址本
	 */
	private static boolean isLdapEnterprise = true;


	public static boolean isUsePadLayout()
	{
		return usePadLayout;
	}

	public static void setUsePadLayout(boolean usePadLayout)
	{
		TEDemoApp.usePadLayout = usePadLayout;
	}

	/**
	 * 是否使用Pad布局
	 */
	private static boolean usePadLayout = true;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		// 初始化SDK
		TESDK.initSDK(this);
		//注册全局广播
		registerBroadcastReceiver();
		//设置日志开关
		// TESDK.getInstance().setLogPath(true, "");
		TESDK.getInstance().setLogPath(true, getFilesDir().getPath() + "/log/");
	}


	/**
	 * 注册 Application级别的广播 , 应用程序全局可用
	 */
	private void registerBroadcastReceiver()
	{
		// 注册全局的广播事件
		registerReceiver(mReceiver, getIntentFilter());
	}


	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			BaseApp.getApp().removeStickyBroadcast(intent);
			if (intent != null)
			{
				LogUtil.w(TAG, "onReceive broadcast ->" + intent);

				handlerBroadcastEvent(intent);
			}
		}
	};

	/**
	 * 生成需要注册的广播过滤器
	 */
	private IntentFilter getIntentFilter()
	{
		IntentFilter filter = new IntentFilter();
		// SIP被踢广播
		filter.addAction(CustomBroadcastConst.ACTION_KICKOFF_NOTIFY); // 被踢
		// 登录 连接服务器广播
//		filter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);
		// filter.addAction(Constants.BROADCAST_PATH.ACTION_HOMEACTIVITY_SHOW);
		// filter.addAction(CustomBroadcastConst.ACTION_SVN_AUTHENTICATION_RESPONSE);
		// filter.addAction(HeartBeatConfig.ACTION_RECONNECT);

//		filter.addAction(Intent.ACTION_LOCALE_CHANGED);// 语言变更 系统行为
//		filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);// 电话呼出  系统行为
//		filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);//  声音路由变更   耳机 - 扬声器 
//		filter.addAction(CustomBroadcastConst.BACK_TO_LOGIN_VIEW); // UI_退会到登录页面
//		filter.addAction(Constants.BROADCAST_PATH.ACTION_RESTART);
//		filter.addAction(HeartBeatConfig.ACTION_RECONNECT);
//		filter.addAction(CustomBroadcastConst.ACTION_HB_TYPE_FRIEND_INVITE);
//		filter.addAction(CustomBroadcastConst.ACTION_REJECT_FRIEND_RESP);
//		filter.addAction(CustomBroadcastConst.ACTION_LOGIN_RESPONSE);
//		filter.addAction(CustomBroadcastConst.ACTION_CTD_CALL_RESPONSE);
//		filter.addAction(CustomBroadcastConst.ACTION_GETADDRESSBOOK_CONFIG);
//		// 添加广播 关机 （删除程序 ） （替换程序）
//		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
//		filter.addAction(Intent.ACTION_SHUTDOWN);
//		filter.addAction(Intent.ACTION_PACKAGE_RESTARTED);
//		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
//		// 添加广播
//		filter.addAction(CustomBroadcastConst.ACTION_VIEW_HEADPHOTO); // 头像
//		filter.addAction(CustomBroadcastConst.ACTION_BULLETIN_PUSH_NOTIFY); // 公告推送
//		filter.addAction(CustomBroadcastConst.ACTION_LOGINOUT_SUCCESS); // 注销成功
//
//		filter.addAction(CustomBroadcastConst.DEVICE_NETSTATUSCHANGED_NOTIFY);//网络变化
//		filter.addAction(CustomBroadcastConst.ACTION_TERMINATE_NOTIFY);
//		filter.addAction(Intent.ACTION_SCREEN_OFF);
		return filter;
	}

	/**
	 * 异步处理 广播事件
	 */
	private void handlerBroadcastEvent(final Intent intent)
	{
		if (intent == null)
		{
			LogUtil.e(TAG, "handlerBroadcastEvent get intent -> null. So do noting.");
			return;
		}

		LogUtil.d(TAG, "handlerBroadcastEvent ->" + intent.getAction());
		String action = intent.getAction();

		if (CustomBroadcastConst.ACTION_KICKOFF_NOTIFY.equals(action))
		{
			actionKickOffNotify(intent);
		} else if (CustomBroadcastConst.DEVICE_NETSTATUSCHANGED_NOTIFY.equals(action))
		{
			//处理网络状态变化
//			actionNetStatusChangedNotify(intent);
		}
	}


	/**
	 * 账号被踢回调
	 */
	private void actionKickOffNotify(Intent intent)
	{

		LogUtil.i(TAG, "be kicked forceCloseCall");
		CallLogic.getInstance().forceCloseCall();

		//得到被T的IP
		String kickedIP = intent.getStringExtra("kickedIP");

		//开机启动未拉起界面时收到被踢的时候需要在onresume执行
		if (null == CallActivity.getInstance())
		{
			LogUtil.e(TAG, "CallActivity is null show be kicked in another way(resume). //TODO");
//			ConfigApp.getInstance().setActionKickOff(true);
//			ConfigApp.getInstance().setKickedIP(kickedIP);
		} else
		{
			//向呼叫页面发送状态改变 通知消息；
//			CallActivity.getInstance().sendHandlerMessage(Contacts.MSG_FOR_HOMEACTIVITY.SET_SELF_STATUS, 0);
		}

		// 解决切换异常wifi后，重登失败后，再切换正常网络无法重登
		LogUtil.i(TAG, "be kicked off, setAutoReLogin false.");
		// 该处先设置，不要放到退回登陆界面时设置，以防弹被踢框未点击确认时还会进行重新连接
//		setAutoReLogin(false);

		// 处于振铃状态的被叫被踢后，软终端无提示信息，且没有退回到登录界面
//		ActivityStackManager.INSTANCE.popupActivityToHomeActivity();
		if (null != CallComingActivity.getInstance())
		{
			CallComingActivity.getInstance().finish();
		}
		Activity tempActivity = ActivityStackManager.INSTANCE.getCurrentActivity();
		if (null == tempActivity)
		{
			LogUtil.d(TAG, "CurrentActivity is null do not do clicked action");
			return;
		}
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(tempActivity);
		builder.setTitle(getString(R.string.module_error_6));
		builder.setMessage(kickedIP);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				CallLogic.getInstance().forceCloseCall();
				CallActivity.getInstance().sendHandlerMessage(Constants.MSG_FOR_HOMEACTIVITY.MSG_LOGOUT, null);
//				ActivityStackManager.INSTANCE.loginOut();
//				dialog.dismiss();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				CallLogic.getInstance().forceCloseCall();
				dialog.dismiss();
//				ConfigApp.getInstance().setProcessKilled(true);
//				//点击重新登录，通知界面重新注册
//				HomeActivity.sendHandlerMessage(MSG_FOR_HOMEACTIVITY.MSG_LOGOUT_AND_REGISTE, null);
			}
		});
		AlertDialog alertDialog = builder.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
		
	}
}
