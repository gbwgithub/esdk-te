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

package com.huawei.te.example.activity;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.huawei.common.CustomBroadcastConst;
import com.huawei.common.LogSDK;
import com.huawei.common.Resource;
import com.huawei.esdk.te.TESDK;
import com.huawei.esdk.te.call.CallConstants.BFCPStatus;
import com.huawei.esdk.te.call.CallConstants.CallStatus;
import com.huawei.esdk.te.call.CallService;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.CallConstant;
import com.huawei.esdk.te.data.Constants.MSG_FOR_HOMEACTIVITY;
import com.huawei.esdk.te.data.Constants.MsgCallFragment;
import com.huawei.esdk.te.util.LayoutUtil;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.manager.DataManager;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;
import com.huawei.te.example.ResponseErrorCodeHandler;
import com.huawei.utils.StringUtil;

public class CallActivity extends BaseActivity
{

	private static final String TAG = CallActivity.class.getSimpleName();

	private static Instance instance = new Instance();

	private RegReceiver regReceiver;

	private static class Instance
	{
		/**
		 * 主界面实例
		 */
		private CallActivity ins;

		@Override
		public String toString()
		{
			return "Instance [ins=" + ins + ']';
		}
	}

	private Handler handler;

	private Button audioCallBtn;
	private Button videoCallBtn;
	private Button exitBtn;
	private EditText callNumEt;

	/**
	 * 通话区域layout
	 */
	private LinearLayout callAreaLayout;
	private CallFragment callFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "CallActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		initComponent();
		initHandler();
		registerBroadcast();
		LayoutUtil.getInstance().initialize();
		instance.ins = this;
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		// 视频通话中锁屏的处理，已经迁移到SDK中执行了

		// if (null != LocalHideRenderServer.getInstance())
		// {
		// LocalHideRenderServer.getInstance().doInBackground();
		// }
	}

	@Override
	protected void onStart()
	{
		super.onStart();
	}

	public static CallActivity getInstance()
	{
		return instance.ins;
	}

	public CallFragment getCallFragment()
	{
		return callFragment;
	}

	private void registerBroadcast()
	{
		Log.d(TAG, "registerBroadcast enter.");
		// 注册界面刷新
		if (regReceiver == null)
		{
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);
			iFilter.addAction(CustomBroadcastConst.ACTION_LOGIN_RESPONSE);
			iFilter.addAction(CustomBroadcastConst.ACTION_REFRESHLICENSEFAILED_NOTIFY);
			// 注册重登陆广播
			regReceiver = new RegReceiver();
			registerReceiver(regReceiver, iFilter);
		}
		Log.d(TAG, "registerBroadcast leave.");
	}

	/**
	 * 注销刷新界面的广播
	 */
	private void unRegister()
	{
		if (regReceiver != null)
		{
			unregisterReceiver(regReceiver);
			regReceiver = null;
		}
	}

	private void initComponent()
	{

		// 初始化通话fragment
		initCallFragment();

		callAreaLayout = (LinearLayout) findViewById(R.id.linear_local);

		// 呼叫号码编辑框
		callNumEt = (EditText) findViewById(R.id.et_call_number);
		callNumEt.setText("01058889");
		// 语音拨号按键
		audioCallBtn = (Button) findViewById(R.id.btn_audio_call);
		// 视频拨号按键
		videoCallBtn = (Button) findViewById(R.id.btn_video_call);
		// 注销Button
		exitBtn = (Button) findViewById(R.id.btn_logout);

		audioCallBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (CallStatus.STATUS_CLOSE == CallService.getInstance().getVoipStatus())
				{
					String callNumber = callNumEt.getText().toString();
					if (null == callNumber || 0 == callNumber.length())
					{
						return;
					}
					callFragment.sendHandlerMessage(MsgCallFragment.MSG_SHOW_AUDIOVIEW, callNumber);
				} else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
					builder.setTitle("提示");
					builder.setMessage("当前不能发起新呼叫");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			}
		});
		videoCallBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (CallStatus.STATUS_CLOSE == CallService.getInstance().getVoipStatus())
				{
					String callNumber = callNumEt.getText().toString();
					if (null == callNumber || 0 == callNumber.length())
					{
						return;
					}
					callFragment.sendHandlerMessage(MsgCallFragment.MSG_SHOW_VIDEOVIEW, callNumber);
				} else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
					builder.setTitle("提示");
					builder.setMessage("当前不能发起新呼叫");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			}
		});

		exitBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				logoutApp();
			}
		});
	}

	/**
	 * 初始化呼叫界面
	 */
	private void initCallFragment()
	{
		if (null != callFragment)
		{
			return;
		}
		// 获取fragment对象
		callFragment = new CallFragment();

		// 获取manager
		FragmentManager manager = this.getFragmentManager();
		FragmentTransaction transation = manager.beginTransaction();
		transation.replace(R.id.call_frag_layout, callFragment);
		try
		{
			transation.commitAllowingStateLoss();
		} catch (IllegalStateException e)
		{
			Log.d(TAG, "IllegalStateException error.");
		}
	}

	private void initHandler()
	{

		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				Log.d(TAG, "what:" + msg.what);
				parallelHandleMessageOne(msg);
				parallelHandleMessageTwo(msg);
				// parallelHandleMessageThree(msg);
				parallelHandleMessageFour(msg);
				parallelHandleMessageFive(msg);
				parallelHandleMessageSix(msg);
				// parallelHandleMessageConf(msg);
				super.handleMessage(msg);
			}
		};
	}

	private void parallelHandleMessageOne(Message msg)
	{
		switch (msg.what) {
		case MSG_FOR_HOMEACTIVITY.MSG_LOGOUT_AND_REGISTE:
			// setSelfStatus(SelfSettingWindow.AWAY);
			// serviceProxy.getCallManager().unRegister();
			// EspaceApp.getIns().setOnlineStatus(Constant.STATUS_OFFLINE);
			// doLogin();
			break;
		case Constants.MSG_SELF_CHANGE_STATE:
			// if (null == msg.obj)
			// {
			// return;
			// }
			// // 只有通话抛此消息，建立通话是busy，结束通话：恢复到上次设置的状态
			// setSelfFieldStatus((Byte) msg.obj == PersonalContact.BUSY ?
			// PersonalContact.BUSY :
			// ConfigAccount.getIns().getLoginAccount().getStatus());
			break;
		// begin added by pwx178217 reason：点击打开关于页签，锁屏后解锁关于页面关闭
		// 呼叫对端超时 关于界面关闭
		case Constants.DISMISS_ABOUT:
			// if (null == homeAboutLayout)
			// {
			// break;
			// }
			// showAbout = false;
			// homeAboutLayout.setVisibility(View.GONE);
			// if (null != shadView)
			// {
			// shadView.setVisibility(View.GONE);
			// }
			// clearTmpLicenceHtml();
			break;
		// end added by pwx178217 reason：点击打开关于页签，锁屏后解锁关于页面关闭
		case MSG_FOR_HOMEACTIVITY.BROADCAST_EVENT:
			Intent intent = (Intent) msg.obj;
			handlerBroadcastEvent(intent);
			break;
		// begin added by pwx178217 2013/8/24 reason：添加全屏 退出全屏操作
		case Constants.MSG_FULL_SCREEN:
			// showFullScreen();
			break;
		default:
			break;
		}
	}

	private void parallelHandleMessageTwo(Message msg)
	{
		switch (msg.what) {
		case Constants.MSG_PART_SCREEN:
			// exitFullScreen();
			// // 取消全屏时显示联系人界面
			// // showContactsFragment();
			break;
		case Constants.MSG_CALL_CLOSE_BACK_TO_HOME:
			// 收到挂断消息
			closeCallBackToHome();
			break;

		// 视频按钮可用
		case Constants.MSG_ENABLE_PREVIEWBTN:
			// setPreviewBtnUserable();
			break;

		case Constants.MSG_SHOW_CHATVIEW:
			// LogUI.d("[UC_UI] MSG_SHOW_CHATVIEW");
			// showChatView(msg);
			break;
		case Constants.MSG_INCOMING_INVITE:
			// // 防止界面还存在
			// removeCallComingActivity();
			// showCallInComingActivity((Intent) msg.obj);
			// ConfigApp.getInstance().setDestoryedCallActivity(false);
			// // isDestoryedCallActivity = false;
			break;
		case Constants.ADCONFIRMATION:
			// if (!(msg.obj instanceof Boolean)) {
			// LogUI.e("msg.obj not instanceof Boolean");
			// return;
			// }
			// if (null != settingFragment) {
			// settingFragment.setPassItemVisible((Boolean) msg.obj);
			// }
			break;
		default:
			break;
		}
	}

	private void parallelHandleMessageFour(Message msg)
	{
		switch (msg.what) {
		case MSG_FOR_HOMEACTIVITY.SET_SELF_SYSTEM_SETTING:
			// startSettingActivity(-1);
			break;
		case MSG_FOR_HOMEACTIVITY.SET_SELF_DISMISS:
			// dismissShadView();
			// end modified by pwx178217 2013/8/15 reason：取消遮罩层
			break;
		case Constants.CONTACT_EXPORT_OPERATE:
			// contactExport(msg.obj.toString(), fileTitleString);
			break;
		case Constants.CONTACT_IMPORT_OPERATE:
			// contactImport(msg.obj.toString(), fileTitleString);
			break;
		// 以后都由此转处理界面类的通知
		case CallConstant.VOIP_UPDATE_SINGLE:
			// 通知界面网络信号变更
			// VoiceQualityLevel level = (VoiceQualityLevel) msg.obj;
			// callFragment.updateSignal(level);
			break;
		case Constants.MSG_NOTIFY_FRAMESIZE_RESET:
			// callFragment.reloadLocalHideView();
			break;
		// end add by l00208218 9.04 通知重新设置分辨率后刷新视频窗口
		case CallConstant.VOIP_CALL_HANG_UP:
			// 关闭弹出对话框
			// HomeActivity.this.dismissAlertDialog();
			Toast.makeText(CallActivity.this, ((String) msg.obj), Toast.LENGTH_LONG).show();
			break;
		case Constants.CONTACT_DOC_SHARE:
			// ActivityStackManager.INSTANCE.getImgFileListActivityAndRemove();
			// showPdfview(msg.obj);
			break;
		default:
			break;
		}
	}

	private void parallelHandleMessageFive(Message msg)
	{
		switch (msg.what) {
		case Constants.REQUEST_GOTO_SHOW_CALLRECORD:
			// refreshNavigation();
			// doCallRecField(callRecField);
			break;
		case CallConstant.VOIP_CALL_RECORD:
			// callFragment.recodeImg((Boolean) msg.obj);
			break;
		case CallConstant.VOIP_PDF_UPDATE_UI:
			updatePDFView((String) msg.obj);
			break;
		case Constants.RESULT_UNREAD_MISSCALL_COUNT:
			// 显示未读未接来电
			// refreshUnreadMissCallCount((Integer) msg.obj);
			break;
		case CallConstant.SLIENT_VOICE:
			break;
		case Constants.MSG_NEED_SET_GRAY:
			// setGrayEnable(false);
			break;
		case Constants.MSG_NO_NEED_SET_GRAY:
			// setGrayEnable(true);
			break;
		// case ENTERPRISE_BOOK_TYPE.LDAP:
		// case ENTERPRISE_BOOK_TYPE.FTPS:
		// contactsFragment.notifyContactsFragmentChange((Boolean) msg.obj);
		// confFragment.notifyConferenceFragmentChange((Boolean) msg.obj);
		// break;
		default:
			break;
		}
	}

	private void parallelHandleMessageSix(Message msg)
	{
		switch (msg.what) {
		// 将呼叫的逻辑移到callfragment里
		case CallConstant.SHOW_CALL_LAYOUT:
			// 显示呼出界面
			showCallLayout();
			break;
		case CallConstant.CLOSE_CAMERA:
			// 关闭本地视频预览
			closeLocalCamera();
			break;
		// 将呼叫的逻辑移到callfragment里
		case Constants.BACK_TO_MAIN_VIEW:
			// if (!ConfigApp.getInstance().isUsePadLayout()) {
			// showScreenWithTitle();
			// }
			// CallService.getInstance().setMainView(true);
			// if (homeLeft.isShown()) {
			// homeTipButton.setText((String) msg.obj);
			// return;
			// }
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			// homeLeft.setVisibility(View.VISIBLE);
			// callAreaLayout.setVisibility(View.GONE);
			// homeTipButton.setVisibility(View.VISIBLE);
			// homeTipButton.setText((String) msg.obj);
			// break;
			// 添加用户反馈功能
		case Constants.USER_FEEDBACK:
			// try {
			// // 用户反馈
			// String httpUri = "http://" +
			// ConfigApp.getInstance().getServerIp() +
			// ":8081/limesurvey/index.php/1/lang-zh-Hans";
			// // 非中文
			// if
			// (!ConfigApp.LANGUAGE_CN.equals(ConfigApp.getInstance().getCurLanguage()))
			// {
			// httpUri = "http://" + ConfigApp.getInstance().getServerIp() +
			// ":8081/limesurvey/index.php/1/lang-en";
			// }
			// Uri uri = Uri.parse(httpUri);
			// Intent it = new Intent(Intent.ACTION_VIEW, uri);
			// it.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// startActivity(it);
			// Log.i(TAG, "try to give some Feedback");
			// } catch (ActivityNotFoundException e) {
			// Log.e(TAG, "no browser error");
			// showToastMsg(HomeActivity.this.getString(R.string.no_browser));
			// }
			break;
		case MSG_FOR_HOMEACTIVITY.MSG_NOTIFY_CALLCLOSE:
			removeCallComingActivity();
			// 由SettingActivity设置StatusHandler，但是没有写SettingActivity，所以这里没有设置当前状态的~
			// setSelfFieldStatus(ConfigAccount.getIns().getLoginAccount().getStatus());
			break;
		case Constants.MSG_ONREVTERMINATE:
			// showToastMsg(getString(R.string.module_error_1login));
			// setSelfStatus(SelfSettingWindow.AWAY);
			break;
		default:
			break;
		}
	}

	/**
	 * 更新PDF预览界面
	 */
	private void updatePDFView(String bfcpState)
	{
		LogUtil.i(TAG, "enter updatePDFView bfcpState:" + bfcpState);
		// 双方同时点击共享
		if (BFCPStatus.BFCP_START.equals(bfcpState))
		{
			// LogUtil.i(TAG, "begin execu bfcp_start");
			// callFragment.setBaseTime(callFragment.getBaseTime() + 1);
			// callFragment.setSendBfcpTime(callFragment.getBaseTime());
			// callFragment.setBfcpSendTag(false);
			// //辅流发送功能目前没有要求，这里发送的相关接口还没有添加
			// // callFragment.startDocShare();
			// LogUtil.i(TAG, "end execu bfcp_start");
		} else if (BFCPStatus.BFCP_END.equals(bfcpState))
		{
			LogUtil.i(TAG, "begin execu bfcp_end");
			// 与pc互通，pad抢发辅流成功后，几秒之内自动停止辅流返回视频画面
			final int oldBaseTime = callFragment.getBaseTime();
			// 收到停止事件，马上将一些状态还原，以免一些操作出现混乱
			callFragment.stopedDocShareState();
			// 等待500毫秒，为解决辅流被抢占时，先回到视频节目再拉起辅流界面
			LogUtil.i(TAG, "end execu bfcp_end and start thread");
			// 与pc互通，pc抢发辅流，pad端先显示视频画面，再显示pc辅流画面
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					threadSleep(500);

					if (0 < callFragment.getRecvBfpcTime())
					{
						return;
					}
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							// 如果收到共享停止后，快速还有其他发送或者接收事件进来，则不处理停止，以防界面快速切换
							if (callFragment.getBaseTime() != oldBaseTime)
							{
								LogUtil.i(TAG, "has receive other return;");
								return;
							}
							LogUtil.i(TAG, "execu stopedDocShare");
							// 刷新界面用UI线程
							callFragment.stopedDocShare();
						}
					});
				}
			}).start();
			// end modify by cwx176935 reason :DTS2014032003425
			// 与pc互通，pad抢发辅流成功后，几秒之内自动停止辅流返回视频画面
			// end add by cwx176935 reason: DTS2014012204924
			// 与pc互通，pc抢发辅流，pad端先显示视频画面，再显示pc辅流画面
		} else if (BFCPStatus.BFCP_RECEIVE.equals(bfcpState))
		{
			LogUtil.i(TAG, "begin execu bfcp_receive");
			callFragment.setBaseTime(callFragment.getBaseTime() + 1);
			callFragment.setRecvBfpcTime(callFragment.getBaseTime());

			LogUtil.i(TAG, "end execu bfcp_receive and start thread");
			// 内部有while循环等待，需新起线程，以防卡住
			new Thread(new Runnable()
			{

				@Override
				public void run()
				{
					// tag不为0时，即执行共享中，则收到辅流共享，则等待主动共享结果
					boolean condition = callFragment.isBfcpSendTag();
					while (condition)
					{
						// 睡眠100毫秒，防止空转，耗资源
						threadSleep(100);
						condition = callFragment.isBfcpSendTag();
					}

					if (callFragment.getRecvBfpcTime() >= callFragment.getSendBfcpTime())
					{
						LogUtil.i(TAG, "execu bfcp receive in ui thread");
						callFragment.setRecvBfpcTime(-1);
						// 刷新界面用UI线程
						runOnUiThread(new Runnable()
						{

							@Override
							public void run()
							{
								callFragment.receiveDoc();
							}
						});
					}
				}
			}).start();
		}
		// begin modified by cwx176934 2013/11/02 Reason:DTS2013103106988
		// 添加共享失败弹窗
		else if (BFCPStatus.BFCP_FAIL.equals(bfcpState))
		{
			// LogUtil.i(TAG, "execu bfcp_fail");
			// callFragment.failShareBfcp(getString(R.string.share_fail));
		}
		// end modified by cwx176934 2013/11/11 Reason:DTS2013103106782 双方同时点击共享
		// end modified by cwx176934 2013/11/02 Reason:DTS2013103106988 添加共享失败弹窗
		LogUtil.i(TAG, "leave updatePDFView bfcpState:" + bfcpState);
	}

	private void threadSleep(int sleepTime)
	{
		try
		{
			Thread.sleep(sleepTime);
		} catch (InterruptedException e)
		{
			LogUtil.e(TAG, "threadSleep has been   Interrupted");
		}
	}

	/**
	 * 注销程序
	 */
	private void logoutApp()
	{
		// 在注销和登出时判断是否需要挂断,没设置VoipStatus,直接退出
		if (CallService.getInstance().getVoipStatus() == CallStatus.STATUS_CLOSE)
		{
			logoutProcess();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
		builder.setTitle("注销");
		builder.setMessage("您确定要注销吗？");
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("注销", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Log.i(TAG, "logout~~");
				CallService.getInstance().forceCloseCall();
				logoutProcess();
				dialog.dismiss();
			}

		});
		builder.create().show();
	}

	/**
	 * 登出
	 */
	private void logoutProcess()
	{
		// 注销后点击到注销 导致崩溃
		if (null == handler)
		{
			return;
		}
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				// 系统设置取消记住密码，注销以后再登录界面还会记住登录密码；
				Constants.setNeedToDelete(true);
				CallControl.getInstance().clear();
				TESDK.getInstance().logout();
				backToLogin();
				LogSDK.setUser("");
			}
		}, 200);// 延时，如果在通话中不延时的话 显示不出进度框
	}

	/**
	 * 回到登录页面，并清理账号相关的数据
	 * 
	 * @param context
	 *            context对象， 用于处理 -6 被踢的 弹窗口
	 * @param errorCode
	 *            错误码 , 如果 == 0 不做提示处理
	 * @param desc
	 *            svn被踢描述由客户端提供，-6/-9时传null
	 */
	public void backToLogin()
	{
		ActivityStackManager.INSTANCE.loginOut();
		// setAutoReLogin(false);
		// 回到登录界面时去初始化Datamanager
		DataManager.getIns().uninit();
		// Intent intent = new Intent(this, LoginActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// startActivity(intent);
	}

	/**
	 * 移除来电界面
	 */
	private void removeCallComingActivity()
	{
		Log.d(TAG, "removeCallComingActivity");
		// 不显示CallFragment的部分
		callAreaLayout.setVisibility(View.GONE);

		if (null != CallComingActivity.getInstance())
		{
			CallComingActivity.getInstance().finish();
		}
	}

	/**
	 * 更新呼叫界面显示内容
	 */
	public void showCallLayout()
	{
		Log.d(TAG, "showCallLayout()");
		// 设置呼叫界面可见
		callAreaLayout.setVisibility(View.VISIBLE);
		// //设置本地视频 欢迎界面 不可见
		// welcomeLayout.setVisibility(View.GONE);
		// confEnterLayout.setVisibility(View.GONE);
		// if (isFullScreen)
		// {
		// startFullScreen();
		// }
	}

	/**
	 * 收到挂断消息后操作
	 */
	public void closeCallBackToHome()
	{
		// 挂断 返回欢迎界面
		backToWelcome();
	}

	/**
	 * 关闭已打开的摄像头
	 */
	private void closeLocalCamera()
	{
		// previewVideoBtnLayout.setEnabled(false);
		// previewVideoBtn.setEnabled(false);
		// previewVideoBtn.setVisibility(View.GONE);
		// previewVideoBtn.setImageDrawable(getResources().getDrawable(R.drawable.te_mobile_home_camera_open));
		// if (localVideoAreaLayout.getVisibility() == View.VISIBLE)
		// {
		// //调用关闭摄像头方法
		// previewFragment.openOrCloseCamera(true);
		//
		// localVideoAreaLayout.setVisibility(View.GONE);
		// }
	}

	/**
	 * 挂断 返回欢迎界面
	 */
	public synchronized void backToWelcome()
	{
		Log.d(TAG, "backToWelcome() ");
		// dialFragment.reset();
		// 欢迎界面可见
		// welcomeLayout.setVisibility(View.VISIBLE);
		// confEnterLayout.setVisibility(View.VISIBLE);

		// 设置呼叫界面 本地视频 不可见
		callAreaLayout.setVisibility(View.GONE);
		// TODO Final，添加本地视频时需要设置下边两个
		// localVideoAreaLayout.setVisibility(View.GONE);
		// 设置本地视频按钮可用
		// setPreviewBtnUserable();

		// isFullScreen = true;
	}

	/*******************************************************************
	 * 处理HandlerMessage
	 *******************************************************************/

	/**
	 * 异步处理 广播事件
	 */
	private void handlerBroadcastEvent(final Intent intent)
	{
		if (intent != null)
		{
			String action = intent.getAction();
			if (CustomBroadcastConst.ACTION_CONNECT_TO_SERVER.equals(action))
			{
				// 在主页面时非主动操作，登录消息收到，1，断网重连了；2，在线心跳超时，重注册了。 ANDRIOD-195
				// 2013.11.26
				// 这两种情况都要进行挂断所有会话。TODO 是否需要增加提示？
				CallService.getInstance().forceCloseCall();
				// end ANDRIOD-195 l00211010 2013.11.26
				// 关闭注册窗口并提示登录失败，如果成功的话则关闭，修改在线状态
				// dismissProgressDialog();// 将进度条先关闭
				// setSelfStatus(SelfSettingWindow.AWAY);
				// // begin add modify by l00211010 reason:注册失败需要设置离线状态
				// // DTS2014012301259
				// EspaceApp.getIns().setOnlineStatus(Constant.STATUS_OFFLINE);
				// end add modify by l00211010 reason:注册失败需要设置离线状态
				// DTS2014012301259
				String loginErrorType = intent.getStringExtra(Resource.SERVICE_ERROR_DATA);
				if (null != loginErrorType && !StringUtil.isStringEmpty(loginErrorType))
				{
					handleRequestError(loginErrorType);
				}

			} else if (CustomBroadcastConst.ACTION_LOGIN_RESPONSE.equals(action))
			{
				// dismissProgressDialog();
				// LogUtil.i(TAG, "loginSuccessResp in home");
				// loginSuccessResp();

				// ===
				// removeResponseFilter();
			} else if (CustomBroadcastConst.ACTION_REFRESHLICENSEFAILED_NOTIFY.equals(action))
			{
				Toast.makeText(CallActivity.this, "license保活失败,请重新注册license", Toast.LENGTH_LONG).show();
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						CallService.getInstance().forceCloseCall();
						logoutProcess();
					}
				}, 1500);
			}
		}
	}

	/**
	 * 
	 * 此方法用于登陆错误 弹窗展示
	 * 
	 * @param errorType
	 *            登陆错误类型
	 * @since 1.1
	 * @history 2013-9-10 v1.0.0 l00211010 create
	 */
	private void handleRequestError(final String errorType)
	{
		Log.w(TAG, "RequestError  errorType = " + errorType);
		int errorCode = 0;
		// 目前三种类型，鉴权失败，超时，服务器错误（作为服务器连接失败处理）
		if (errorType.equals(Resource.LOGIN_ACCOUNT_ERROR))
		{
			// 确认按钮点击事件
			DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					// TODO 需要先注销并卸载框架

					logoutApp();
				}
			};
			// 在此处鉴权失败将推出到主界面
			this.showAlertDialog(null, getString(R.string.account_mistake), null, null, null, null, null);
			// 取消按钮点击事件
			return;
		}
		if (errorType.equals(Resource.LOGIN_SERVER_ERROR))
		{
			errorCode = ResponseErrorCodeHandler.CONNECT_ERROR;
		}
		if (errorType.equals(Resource.LOGIN_SERVER_TIMEOUT))
		{
			errorCode = Resource.REQUEST_TIMEOUT;
		}
		if (errorType.equals(Resource.LOGIN_ACCOUNTNUM_OVERLIMIT))
		{
			errorCode = ResponseErrorCodeHandler.LOGIN_ACCOUNTNUM_OVERLIMIT;
		}
		// 用于修改登录成功后修改系统时间再次重新登录 证书过期认证失败时提示信息错误 原提示为终端请求异常
		if (errorType.equals(Resource.CERTIFICATE_ERROR))
		{
			errorCode = ResponseErrorCodeHandler.CERTIFICATE_ERROR;
		}
		if (errorType.equals(Resource.NETWORK_INVALID))
		{
			errorCode = ResponseErrorCodeHandler.NETWORK_INVALID;
		}
		ResponseErrorCodeHandler.handleRequestError(errorCode, this);
	}

	/**
	 * 方法名称：sendHandlerMessage 方法描述： 发送消息
	 */
	public void sendHandlerMessage(int what, Object object)
	{
		Log.i(TAG, "sendHandlerMessage exec ");
		Log.d(TAG, "handler->" + handler);
		Log.d(TAG, "what->" + what + "; object->" + object);
		if (handler == null)
		{
			Log.d(TAG, "sendHandlerMessage() handler is null");
			return;
		}
		Message msg = handler.obtainMessage(what, object);
		handler.sendMessage(msg);
	}

	/*******************************************************************
	 * 处理HandlerMessage finish
	 *******************************************************************/

	private int cameraOritation = 0;
	private int localOritation = 0;

	public void setCameraOritation(View v)
	{
		CallService.getInstance().setCameraDegree(++cameraOritation, localOritation);
	}

	public void setLocalOritation(View v)
	{
		CallService.getInstance().setCameraDegree(cameraOritation, ++localOritation);
	}

	@Override
	public void onBackPressed()
	{
		super.moveTaskToBack(true);
	}

	@Override
	public void clearData()
	{
		if (null != handler)
		{
			handler.removeMessages(MSG_FOR_HOMEACTIVITY.MSG_LOGOUT_AND_REGISTE);
			handler = null;
		}

		unRegister();
		regReceiver = null;
		instance.ins = null;
	}

	/**
	 * 方法名称：sendHandlerMessage 方法描述： 发送消息
	 */
	public static void sendHandlerMessageByBroadcast(int what, Object object)
	{
		if (null == instance.ins)
		{
			Log.e(TAG, "CallActivity is null.");
			return;
		}
		instance.ins.sendHandlerMessage(what, object);
	}

	/**
	 * 类描述：登录页面的广播。
	 */
	private static class RegReceiver extends BroadcastReceiver
	{
		/**
		 * 界面广播接收
		 */
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent != null)
			{
				sendHandlerMessageByBroadcast(MSG_FOR_HOMEACTIVITY.BROADCAST_EVENT, intent);
			}
		}
	}

	public void setCallBandWidth(View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("设置呼叫带宽");
		builder.setMessage("请输入要设置的带宽数值：  (Kbit/s)");
		final EditText et = new EditText(this);
		et.setInputType(InputType.TYPE_CLASS_NUMBER);
		et.setHint("请输入呼叫带宽");
		builder.setView(et);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String bandwidth = et.getText().toString().trim();
				if (bandwidth.equals(""))
				{
					Toast.makeText(CallActivity.this, "输入内容不能为空", Toast.LENGTH_SHORT).show();
				} else
				{
					long longBandwidth = Integer.parseInt(bandwidth);
					if (longBandwidth <= 64)
					{
						if (CallService.getInstance().setBandwidth(64))
						{
							Toast.makeText(CallActivity.this, "呼叫带宽设置为：64 Kbit/s", Toast.LENGTH_LONG).show();
						}
					} else if (longBandwidth <= 128)
					{
						if (CallService.getInstance().setBandwidth(128))
						{
							Toast.makeText(CallActivity.this, "呼叫带宽设置为：128 Kbit/s", Toast.LENGTH_LONG).show();
						}
					} else if (longBandwidth <= 256)
					{
						if (CallService.getInstance().setBandwidth(256))
						{
							Toast.makeText(CallActivity.this, "呼叫带宽设置为：256 Kbit/s", Toast.LENGTH_LONG).show();
						}
					} else if (longBandwidth <= 384)
					{
						if (CallService.getInstance().setBandwidth(384))
						{
							Toast.makeText(CallActivity.this, "呼叫带宽设置为：384 Kbit/s", Toast.LENGTH_LONG).show();
						}
					} else if (longBandwidth <= 512)
					{
						if (CallService.getInstance().setBandwidth(512))
						{
							Toast.makeText(CallActivity.this, "呼叫带宽设置为：512 Kbit/s", Toast.LENGTH_LONG).show();
						}
					} else
					{
						if (CallService.getInstance().setBandwidth(768))
						{
							Toast.makeText(CallActivity.this, "呼叫带宽设置为：768 Kbit/s", Toast.LENGTH_LONG).show();
						}
					}
				}
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	private int videoMode = 0;

	public void setVideoMode(View view)
	{
		Log.d(TAG, "Last mode -> " + videoMode);
		if (CallService.getInstance().setVideoMode((++videoMode) % 2))
		{
			Toast.makeText(CallActivity.this, "已设置" + (Constants.VideoMode.VIDEO_PROCESS_MODE == videoMode % 2 ? "流畅优先" : "画质优先"), Toast.LENGTH_LONG)
					.show();
		}
	}
}
