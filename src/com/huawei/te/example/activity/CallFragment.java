package com.huawei.te.example.activity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.huawei.common.CallErrorCode;
import com.huawei.esdk.te.LocalHideRenderServer;
import com.huawei.esdk.te.VariationView;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.call.CallLogic.ModifyNoticeType;
import com.huawei.esdk.te.call.VideoHandler;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.CallConstant;
import com.huawei.esdk.te.data.Constants.MsgCallFragment;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;
import com.huawei.te.example.call.VoipCallModifyLogic;
import com.huawei.te.example.menubar.MenuBarContalPanel;
import com.huawei.te.example.menubar.MenuBarContalPanel.MenuItemServer;
import com.huawei.te.example.menubar.MenuBarContalPanel.Mode;
import com.huawei.utils.StringUtil;
import com.huawei.voip.data.CallCommandParams;
import com.huawei.voip.data.VoiceQuality.VoiceQualityLevel;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * render 操作流程 stop --> remove --> add --> start
 * addView必须在hme_videorender_start之前，原因没addview相当于后台渲染，opengl操作有风险
 * removeView必须在hme_videorender_stop之后 呼叫fragment
 */

public class CallFragment extends Fragment implements OnClickListener
{

	private static final String TAG = Constants.GTAG + CallFragment.class.getSimpleName();

	private static final Object VIDEO_HANGUP_LOCK = new Object();

	private TextView numberAudioTV;
	private TextView hintAudioTV;
	// 此按钮功能由MenuBar所提供的hangup所替代，这里就先注释掉，
	// private Button hangupAudioBtn;
	private Button hangupVideoBtn;

	/**
	 * 呼叫时本地视频预览区域
	 */
	private LinearLayout previewLayout;
	/**
	 * 远端视频
	 */
	private RelativeLayout remoteVideoView;
	/**
	 * 本地视频
	 */
	private RelativeLayout localVideoView;

	/**
	 * 本地视频显示区域
	 */
	private RelativeLayout localVideoLayout;

	private VoipCallModifyLogic callModifyLogic;

	private OnClickListener listener;

	/**
	 * 音频呼叫显示区域
	 */
	private RelativeLayout audioCallLayout;

	/**
	 * 视频通话显示区域
	 */
	private RelativeLayout videoChatLayout;

	/**
	 * 呼叫通话界面提示语
	 */
	private String tipTxt;

	/**
	 * 根布局
	 */
	private ViewGroup rootView;

	/**
	 * 呼叫内部广播
	 */
	private Handler handler;

	/**
	 * 菜单栏
	 */
	private MenuBarContalPanel menuBarPanel;

	/**
	 * MenuControlPanel工具条接口
	 */
	private MenuItemServer menuItemServer;

	/**
	 * 呼叫线程池
	 */
	private ExecutorService callCtlThreadPool;

	/**
	 * 是否处于会话保持状态
	 */
	private boolean isSessionHold = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);

		rootView = (ViewGroup) inflater.inflate(R.layout.call_fraglayout, container, false);
		// 设置全局点击事件

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		// 初始化组件
		initComponent();
		callCtlThreadPool = Executors.newSingleThreadExecutor();
		setRootViewListener();
	}

	private void initComponent()
	{
		initListener();
		initMenuItemServer();
		numberAudioTV = (TextView) getActivity().findViewById(R.id.tv_audio_number);
		hintAudioTV = (TextView) getActivity().findViewById(R.id.tv_audio_hint);
		// hangupAudioBtn = (Button)
		// getActivity().findViewById(R.id.btn_audio_hangup);
		// hangupAudioBtn.setOnClickListener(listener);

		hangupVideoBtn = (Button) getActivity().findViewById(R.id.btn_video_hangup);
		hangupVideoBtn.setOnClickListener(listener);

		// 音频呼叫区域
		audioCallLayout = (RelativeLayout) rootView.findViewById(R.id.audio_calllayout);

		// 视频通话区域
		videoChatLayout = (RelativeLayout) rootView.findViewById(R.id.video_chatlayout);

		// 本地视频显示区域
		localVideoLayout = (RelativeLayout) rootView.findViewById(R.id.local_layout);
		initHandler();

		// 远端视频
		remoteVideoView = (RelativeLayout) rootView.findViewById(R.id.remote_videoview);
		VideoHandler.getIns().setRemoteVideoView(remoteVideoView);

		// 本地视频区域
		localVideoView = (RelativeLayout) rootView.findViewById(R.id.local_videoview);

		// 呼叫时本地视频显示区域
		previewLayout = (LinearLayout) rootView.findViewById(R.id.pre_local_video);
	}

	private void initHandler()
	{
		if (null != handler)
		{
			Log.d(TAG, "the handler has init.");
			return;
		}
		handler = new Handler()
		{
			@Override
			public void dispatchMessage(Message msg)
			{
				Log.d(TAG, "what:" + msg.what);
				handlerMessageNotity(msg);
				handlerMessageView(msg);
				handlerMessageCall(msg);
				handlerMessageOperate(msg);
				super.dispatchMessage(msg);
			}
		};
	}

	private void initMenuItemServer()
	{
		if (null != menuItemServer)
		{
			Log.d(TAG, "the initMenuItemServer has init.");
			return;
		}
		menuItemServer = new MenuItemServer()
		{

			@Override
			public void setScreen(boolean isFullScreen)
			{

			}

			@Override
			public void setPip(boolean isPip)
			{

			}

			@Override
			public void showCallInfo()
			{
				showVideoInfo();
			}

			@Override
			public void showConfListView()
			{

			}
		};
	}

	private void initListener()
	{
		listener = new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if (null == view)
				{
					Log.i(TAG, "view is null return");
					return;
				}
				// if (null == menuBarPanel) {
				// Log.i(TAG, "menuBarPanel is null return");
				// return;
				// }
				switch (view.getId()) {
				// 挂断 取消呼叫
				// case R.id.audio_call_cancel_button:
				// case R.id.audio_chat_hang_up_button:
				case R.id.btn_audio_hangup:
					Log.d(TAG, "onclick video_hangup --- end Audio Call()");
					// cancelCallTask();
					synchronized (VIDEO_HANGUP_LOCK)
					{
						sendHandlerMessage(MsgCallFragment.MSG_CALL_END_REQUEST, null);
					}

					break;

				case R.id.btn_video_hangup:
					Log.d(TAG, "onclick video_hangup --- end Video Call() ");
					synchronized (VIDEO_HANGUP_LOCK)
					{
						sendHandlerMessage(MsgCallFragment.MSG_CALL_END_REQUEST, null);
					}

					break;

				// // 关闭本地视频
				// case R.id.close_localview:
				// closeLocalView();
				// break;
				//
				// // 切换摄像头
				// case R.id.switch_view:
				// switchView();
				//
				// // 设置切换键
				// setViewPosition();
				// break;
				// // PDF预览界面返回
				// case R.id.doc_share_local_layout_btn_back:
				// pdfViewBack();
				// break;
				// case R.id.doc_share_local_layout_btn_stop_share:
				// // 改变菜单栏模式,停止后回到视频模式
				// menuBarPanel.changeMode(Mode.VIDEO_CALL);
				// stopDocShare();
				// break;
				// // 切换本端和远端是视频界面
				// case R.id.switch_localview:
				// changeRenderInLocal();
				// break;
				//
				// case R.id.back_main_view:
				// ActivityStackManager.INSTANCE.whenCallEndShowLastActivity();
				// HomeActivity.sendHandlerMessage(Constant.BACK_TO_MAIN_VIEW,
				// tipTxt);
				// break;
				// case R.id.callcoming_signal_btn:
				// showVideoInfo();
				// break;
				// default:
				// Log.i(TAG, "on click default");
				// break;
				}
			}
		};
	}

	public void sendHandlerMessage(int what, Object object)
	{
		if (handler == null)
		{
			Log.d(TAG, "sendHandlerMessage() handler is null");
			return;
		}
		Message msg = handler.obtainMessage(what, object);
		handler.sendMessage(msg);
	}

	/**
	 * notify相关handler
	 */
	private void handlerMessageNotity(Message msg)
	{
		switch (msg.what) {

		case MsgCallFragment.MSG_REFRESH_VIEW:// 这里只考虑本地采集点
			if (null == msg.obj || null == LocalHideRenderServer.getInstance())
			{
				return;
			}
			if (null == msg.obj)
			{
				return;
			}
			refreshView(msg);
			break;
		// case MsgCallFragment.MSG_ADD_VIDEO_TIME_OUT:
		// showToast(R.string.add_video_time_out);
		// break;
		// case MsgCallFragment.MSG_DATA_DECODE_SUCCESS:
		// Log.i(TAG, "data decode success UI receive");
		// isRecvDataDecode = true;
		// addBFCPRender();
		// break;
		case MsgCallFragment.MSG_LOW_BW_UPDATE_FAIL:
			// showToast(R.string.low_bw_update_fail, 5000);
			Toast.makeText(getActivity(), R.string.low_bw_update_fail, 5000).show();
			break;
		// case MsgCallFragment.MSG_LOW_BW_AUDIO_NEGO_FAIL:
		// showToast(R.string.low_bw_nego_fail_far_end);
		// break;
		// 操作失败提示
		case MsgCallFragment.MSG_CLOSE_VIDEO_FAIL:
			showToast(R.string.oper_failure);
			break;
		// // 会控可用
		// case MsgCallFragment.MSG_NOTIFY_CONF_CONTROL_ENABLE:
		// Log.i(TAG, "enable ConfControl");
		// if (null != menuBarPanel) {
		// menuBarPanel.setShowConfListEnable(true);
		// }
		// break;
		// // begin add by cwx176935 reason: DTS2013111209672
		// // 挂机按钮结束辅流后，下次音视频通话时长显示不对
		case Constants.MSG_NOTIFY_CALL_END:// 真正最后挂断
			onCallClosed();
			// 挂断时 状态刷新
			if (null != menuBarPanel)
			{
				menuBarPanel.setShowConfListEnable(false);
			}
			// end add by cwx176935 reason: DTS2013111209672
			// 挂机按钮结束辅流后，下次音视频通话时长显示不对
			// add by l00208218 收到刷新VIEW通知后重新添加采集点
			break;
		default:
			break;
		}
	}

	/**
	 * View相关handler
	 */
	private void handlerMessageView(Message msg)
	{
		if (msg.obj instanceof String)
		{
			Log.d(TAG, "handlerMessageView receives:" + msg.what);
		}
		switch (msg.what) {
		case MsgCallFragment.MSG_SHOW_AUDIOVIEW:
			if (!(msg.obj instanceof String))
			{
				Log.i(TAG, "msg.obj is not instanceof String");
				return;
			}
			showCallingLayout((String) msg.obj, false);
			break;
		case MsgCallFragment.MSG_SHOW_VIDEOVIEW:
			if (!(msg.obj instanceof String))
			{
				Log.i(TAG, "msg.obj is not instanceof String");
				return;
			}
			showCallingLayout((String) msg.obj, true);
			break;
		case MsgCallFragment.MSG_SVMSUNG_PHONE_REMOVE_VIEW:
			// removeViewInBack();
			break;
		case MsgCallFragment.MSG_SVMSUNG_PHONE_ADD_VIEW:
			// addViewFromBack();
			break;
		case MsgCallFragment.MSG_DO_FROM_BACKGROUND:
			// doFromBackground();
			break;
		default:
			break;
		}
	}

	/**
	 * 呼叫相关Handler消息
	 */
	private void handlerMessageCall(Message msg)
	{
		if (msg.obj instanceof String)
		{
			Log.d(TAG, "handlerMessageCall receives:" + msg.what);
		}
		switch (msg.what) {
		case MsgCallFragment.MSG_DIALCALL_AUDIO:
			if (!(msg.obj instanceof String))
			{
				Log.i(TAG, "msg.obj is not instanceof String");
				return;
			}
			dialCall((String) msg.obj, false);
			break;
		case MsgCallFragment.MSG_DIALCALL_VIDEO:
			dialCall((String) msg.obj, true);
			break;
		case MsgCallFragment.MSG_CALL_END_REQUEST:
			sendCallCloseRequest();
			break;
		case MsgCallFragment.MSG_CALL_UPDATE_UI:
			// 处理会话保持情况
			Log.i(TAG, "MsgCallFragment.MSG_CALL_UPDATE_UI");
			if (isSessionHold)
			{
				isSessionHold = false;
			}
			Boolean ans = (Boolean) msg.obj;
			// if (null != ans) {
			// if (ans) {
			// ActivityStackManager.INSTANCE.bringHomeActivityToFront(CallFragment.this.getActivity());
			// }
			// }
			updateCallLayout();
			break;
		case MsgCallFragment.MSG_CALL_MODIFY_UI:
			Log.d(TAG, "MsgCallFragment.MSG_CALL_MODIFY_UI");
			ModifyNoticeType modifyType = (ModifyNoticeType) msg.obj;
			voipCallModify(modifyType);
			break;
		// start by c00349133 reason: 会话保持时，通信界面显示“通话被保持”
		case MsgCallFragment.MSG_SHOW_SESSION_HOLD:
			Log.i(TAG, "MsgCallFragment.MSG_SHOW_SESSION_HOLD");
			isSessionHold = true;
			// boolean isVideo = CallLogic.getIns().isVideoCall();
			// Log.i(TAG, "MsgCallFragment.MSG_SHOW_SESSION_HOLD" + isVideo);
			// if (isVideo) {
			// videocallTipView.setText(getString(R.string.session_holding));
			// if (isDocSharing) {
			// shareText.setText(getString(R.string.session_holding));
			// }
			// } else {
			// audiocallTipView.setText(getString(R.string.session_holding));
			// }
			break;
		// end by c00349133 reason: 会话保持时，通信界面显示“通话被保持”

		// 来电界面销毁
		case MsgCallFragment.MSG_NOTIFY_CALLCOMING_DESTORY:
			Log.i(TAG, "MsgCallFragment.MSG_NOTIFY_CALLCOMING_DESTORY");
			// setScrean针对手机屏幕做特殊处理
			// setScrean(CallLogic.getIns().getVoipStatus(),
			// CallLogic.getIns().isVideoCall());
			// ConfigApp.getInstance().setDestoryedCallActivity(true);
			// HomeActivity.isDestoryedCallActivity = true;
			break;
		default:
			break;
		}
	}

	/**
	 * 操作相关handler
	 */
	private void handlerMessageOperate(Message msg)
	{
		boolean bRet = true;
		switch (msg.what) {
		case Constants.OPERATECAMERA:
			// bRet = operateCam(msg);
			// if (!bRet)
			// {
			// return;
			// }
			break;
		// 操作声音
		case Constants.OPERATEVOLUME:
			// bRet = operateVolume(msg);
			// if (!bRet)
			// {
			// return;
			// }
			break;
		// begin by cwx176935 reason: DTS2014011603694
		// 视频通话中，TE30关闭本地视频，软终端显示对端残留的最后一帧图像
		case MsgCallFragment.MSG_REMOTE_VIDEO_UPDATE:
			Log.i(TAG, "MSG_REMOTE_VIDEO_UPDATE" + isSessionHold);
			if (isSessionHold)
			{
				if (CallLogic.getIns().isVideoCall())
				{
					// videocallTipView.setText(getString(R.string.video_chat));
					// start added by c00349133
					// reason:解决64k带宽下，二点转多点，会话恢复时，转语音不可用
					if (!CallLogic.getIns().isEnableBfcp())
					{
						menuBarPanel.changeMode(Mode.BFCP_NOT_ENABLED);
					}
					// end added by c00349133 reason:解决64k带宽下，二点转多点，会话恢复时，转语音不可用
				} else
				{
					// audiocallTipView.setText(getString(R.string.audio_chat));
					// begin added by c00349133 reason:DTS2015071410476 android
					// 与TE30 音频通话后，TE30保持通话再恢复，软终端音频转视频功能不可用
					menuBarPanel.changeMode(Mode.AUDIO_CALL);
					// end added by c00349133 reason:DTS2015071410476 android
					// 与TE30 音频通话后，TE30保持通话再恢复，软终端音频转视频功能不可用
				}
				isSessionHold = false;
			}
			if (null != msg.obj && null != VideoHandler.getIns().getRemoteCallView())
			{
				Log.i(TAG, "callFragment recevie remote video update close:[" + msg.obj + ']');
				cleanRemoteFrame((Boolean) msg.obj);
			}
			break;
		// end by cwx176935 reason: DTS2014011603694
		// 视频通话中，TE30关闭本地视频，软终端显示对端残留的最后一帧图像
		case MsgCallFragment.MSG_AUDIO_ROUTE_UPDATE:
			updateAudioRoute();
			break;
		default:
			break;
		}
	}

	/**
	 * 远端无图像显示黑屏
	 * 
	 * @param isClean
	 */
	private void cleanRemoteFrame(boolean isClean)
	{
		final SurfaceView remoteVV = VideoHandler.getIns().getRemoteCallView();
		if (null == remoteVV)
		{
			Log.i(TAG, "remote view is null; return;");
			return;
		}
		synchronized (RENDER_CHANGE_LOCK)
		{
			if (isClean)
			{
				remoteVV.setBackgroundColor(Color.BLACK);
			} else
			{
				// begin modify by cwx176935 reason: DTS2014061607963
				// 与VCT点对点视频通话，VCT开启本地视频，软终端一侧有残留帧
				handler.postDelayed(new ChangeViewBackgroudRunnable(remoteVV), 1000);
				// end modify by cwx176935 reason: DTS2014061607963
				// 与VCT点对点视频通话，VCT开启本地视频，软终端一侧有残留帧
			}
			Log.i(TAG, "now remote has close [" + isClean + ']');
		}
	}

	private static class ChangeViewBackgroudRunnable implements Runnable
	{
		private SurfaceView remoteVV;

		ChangeViewBackgroudRunnable(SurfaceView remoteVVVar)
		{
			remoteVV = remoteVVVar;
		}

		@Override
		public void run()
		{
			remoteVV.setBackgroundColor(Color.alpha(0));
		}
	}

	/**
	 * 停止呼叫
	 */
	private void sendCallCloseRequest()
	{
		Log.d(TAG, "sendCallCloseRequest()");
		// if (null == callCtlThreadPool) {
		// Log.e(TAG, "sendCallCloseRequest() --- callCtlThreadPool is null");
		// CallPresenter.getInstance().closeCall();
		// return;
		// }
		// callCtlThreadPool.execute(new Runnable() {
		// @Override
		// public void run() {
		// // 重新设置一次手机竖屏,重新初始化联系人和通话记录的adapt,重新计算缓存item的大小，否则会bianxiao
		// 只针对手机屏幕，这里可以不调用下面一行代码
		setScrean(CallLogic.STATUS_CLOSE, true);
		//
		// // CommonManager.getInstance().getVoip().closeCall();
		// // TO invoke
		CallControl.getInstance().closeCall();
		// }
		// });
		//

		// new Thread(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// //重新设置一次手机竖屏,重新初始化联系人和通话记录的adapt,重新计算缓存item的大小，否则会bianxiao
		// setScrean(CallLogic.STATUS_CLOSE, true);
		// CallControl.getInstance().closeCall();
		// }
		// }).run();
	}

	/**
	 * 显示呼出界面
	 */
	public void showCallingLayout(final String callNumber, final boolean isVideoCall)
	{
		if (StringUtil.isStringEmpty(callNumber))
		{
			Log.i(TAG, "empty CallNumber return!!!");
			return;
		}

		// 关闭本地视频预览
		// HomeActivity.sendHandlerMessage(CallConstant.CLOSE_CAMERA, null);

		// 显示呼出界面
		// HomeActivity.sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
		// TO invoke
		CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);

		if (isVideoCall)
		{
			// 初始化视频参数
			VideoHandler.getIns().initCallVideo(getActivity());
			sendHandlerMessage(MsgCallFragment.MSG_DIALCALL_VIDEO, callNumber);
		} else
		{
			sendHandlerMessage(MsgCallFragment.MSG_DIALCALL_AUDIO, callNumber);
		}
		tipTxt = getTipTxt(true, isVideoCall, false, false, false);
		updateLayout(CallLogic.STATUS_CALLING, callNumber, isVideoCall, tipTxt);
	}

	/**
	 * 获得提示信息，用于在刷新呼叫界面
	 * 
	 * @param isCalling
	 *            是呼叫中，还是通话中
	 * @param isRef
	 *            是否是呼转
	 * @param isCallToVideo
	 *            是否是请求升级视频
	 * @return 提示信息
	 */
	private String getTipTxt(boolean isCalling, boolean isVideo, boolean isRef, boolean isCallToVideo, boolean isAudioChat)
	{
		int showCallStringId = 0;
		if (isRef)
		{
			return ("Call forward");
		} else if (isCallToVideo)
		{
			return ("Switching to a video call...");
		} else if (isCalling)
		{
			return (isVideo ? "Video call..." : "Dialing...");
		} else
		{
			return (isAudioChat ? "Voice call..." : "Video chat...");
		}
	}

	/**
	 * 封装呼叫到单线程池中，保证呼叫的时序
	 * 
	 * @param callNumber
	 *            呼叫的号码
	 * @param isVideoCall
	 *            是否视频呼叫 true是
	 */
	private synchronized void dialCall(final String callNumber, final boolean isVideoCall)
	{
		// 由于异步执行可能出现的问题，所以能同步执行的都改为同步执行
		// if (null == callCtlThreadPool) {
		// Log.e(TAG, "callCtlThreadPool is null");
		// return;
		// }
		// callCtlThreadPool.execute(new Runnable() {
		// @Override
		// public void run() {
		// processDialCall(callNumber, isVideoCall);
		// }
		// });

		// TO invoke
		processDialCall(callNumber, isVideoCall);
	}

	/**
	 * 执行呼叫方法
	 * 
	 * @param callNumber
	 *            呼叫号码
	 * @param isVideoCall
	 *            是否视频呼叫
	 */
	private synchronized void processDialCall(final String callNumber, final boolean isVideoCall)
	{
		// // 设置VideoCaps
		// VideoCaps vcaps = null;
		// VideoCaps dataCaps = null;
		// if (isVideoCall) {
		// // 初始化
		// vcaps = VideoHandler.getIns().getCaps();
		// dataCaps = VideoHandler.getIns().getDataCaps();
		// }

		// 发起呼叫方法
		// final String callRet = CallControl.getInstance().dialCall(callNumber,
		// null, isVideoCall, vcaps, dataCaps);
		final String callRet = CallControl.getInstance().dialCall(callNumber, null, isVideoCall);

		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{

				if (!CallErrorCode.isFail(callRet))
				{
					return;
				}
				Log.i(TAG, "call ret is error");
				if (isVideoCall)
				{
					VideoHandler.getIns().clearCallVideo();
				}
				// cancelCallTask();
				sendHandlerMessage(MsgCallFragment.MSG_CALL_END_REQUEST, null);
				// ARMv6不支持任何呼叫
				if (CallErrorCode.CALL_ERROR_AMRV6.equals(callRet))
				{
					Log.e(TAG, "Your device CPU uses the ARMv6 architecture and does not support calling currently");
					return;
				}
				Log.e(TAG, "Call failed");
			}
		};
		CallFragment.this.getActivity().runOnUiThread(runnable);
	}

	/**
	 * 更新界面
	 * 
	 * @param voipState
	 *            voip状态
	 * @param callNumber
	 *            呼叫号码 不需要判空
	 * @param isVideoCall
	 *            是否视频呼叫 true是视频呼叫 false 不是视频呼叫
	 * @param tipTxt
	 *            提示文字
	 */
	public void updateLayout(int voipState, String callNumber, boolean isVideoCall, String tipTxt)
	{

		Log.d(TAG, "updateLayout()");

		// 更新界面的时候重置标志位为需要点击挂断时显示对话框
		if (null != menuBarPanel)
		{
			menuBarPanel.setNeedShow(true);
		} else
		{
			Log.d(TAG, "menuBarPanel is null");
		}

		setScrean(voipState, isVideoCall);

		// 设置是否视频通话
		CallLogic.getIns().setVideoCall(isVideoCall);

		// 开启采集点的服务
		if (null == LocalHideRenderServer.getInstance())
		{
			if (null != CallActivity.getInstance())
			{
				CallActivity.getInstance().startService(new Intent(CallActivity.getInstance(), LocalHideRenderServer.class));
			} else
			{
				Log.e(TAG, " --- CallActivity instance is null ,start LocalHideRenderServer failed!");
			}
		}

		// 保持屏幕长亮
		// DeviceUtil.setKeepScreenOn(getActivity());

		// 显示视频通话title Layout
		// mobileVideoLayout.setVisibility(View.VISIBLE);

		// 更新界面到挂断
		if (CallLogic.STATUS_CLOSE == voipState)
		{
			Log.i(TAG, "end hangup");
			return;
		}

		if (null != callNumber)
		{
			numberAudioTV.setText(callNumber);
		}

		if (null == menuBarPanel)
		{
			Log.d(TAG, "menuBarPanel is null , create it.");
			menuBarPanel = new MenuBarContalPanel(rootView, menuItemServer);
		}
		menuBarPanel.setRemoteNumber(callNumber);
		updateByState(callNumber, voipState, isVideoCall);
	}

	private void setScrean(int voipState, boolean isVideoCall)
	{
		// 只针对手机屏幕，所以这里不考虑
		// if (!ConfigApp.getInstance().isUsePadLayout())
		if (false)
		{
			if (voipState == CallLogic.STATUS_VIDEOING || (voipState == CallLogic.STATUS_CALLING && isVideoCall))
			{
				// 手机界面语音呼叫通话时状态图标不显示
				if (!CallLogic.getIns().isMainView())
				{
					getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
				return;
			}
			getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	/**
	 * 根据状态处理界面更新
	 */
	private void updateByState(String callNumber, int voipState, boolean isVideoCall)
	{

		Log.d(TAG, "updateByState() voipStateis:" + voipState);
		// 修改变音频时辅流框仍显示
		// if (CallLogic.STATUS_TALKING == voipState || CallLogic.STATUS_CALLING
		// == voipState) {
		// // 取消菜单栏绑定（5秒后消失）
		// menuBarPanel.removeLinkedView(topMenuLayout);
		// topMenuLayout.setVisibility(View.GONE);
		// }
		// 修改变音频时辅流框仍显示

		if (CallLogic.STATUS_CALLING == voipState)
		{
			showCallLayout(callNumber, isVideoCall, tipTxt);
			// HomeActivity.sendHandlerMessage(Constants.MSG_SELF_CHANGE_STATE,
			// PersonalContact.BUSY);
			Log.i(TAG, "to talking state is video call =>" + isVideoCall);
			return;
		}
		// 通话状态 语音通话 视频通话
		if (CallLogic.STATUS_TALKING == voipState || CallLogic.STATUS_VIDEOING == voipState)
		{
			showChatLayout(callNumber, isVideoCall, tipTxt);
			Log.i(TAG, "to chat state is video chat =>" + isVideoCall);
		}
	}

	public void onCallClosed()
	{
		Log.i(TAG, "onCallClosed enter.");
		// orientationEventListener.disable();
		// VideoHandler.getIns().resetTurnDirc();
		// if (!ConfigApp.getInstance().isUsePadLayout())
		// {
		// showScreenWithTitle();
		// }
		// // 取消长亮
		// DeviceUtil.releaseWakeLock();
		//
		// Log.i(TAG, "onCallClosed VoipCallModifyLogic dismissAllDialogs.");
		// // VoipCallModifyLogic.dismissAllDialogs();
		// //取消显示升级对话框
		// BaseActivity basAct = ActivityStackManager.INSTANCE
		// .getCurrentActivity();
		// if (null != basAct)
		// {
		// basAct.dismissUpdateDialog();
		// basAct.dismissAllDialogs();
		// }
		//
		// //取消通知
		// NotificationUtil.getIns().clearBackgroundNotification();
		//
		// // 更新界面时取消所有dialog显示
		// Log.i(TAG, "onCallClosed dismissAllDialogs.");
		// //杀进程时需要挂断通话但此时界面已经不存在 会有异常
		// BaseActivity mBaseActivity = (BaseActivity) getActivity();
		// if (null != mBaseActivity)
		// {
		// mBaseActivity.dismissAllDialogs();
		// }
		//
		// // 取消呼叫超时任务
		// Log.i(TAG, "onCallClosed cancelCallTask.");
		// cancelCallTask();
		//
		// //停止数据共享
		// Log.i(TAG, "onCallClosed stopDocShare.");
		// stopDocShare();
		//
		// 关闭网络状态等通话附属activity
		Log.i(TAG, "onCallClosed destroyUtilActivity.");
		destroyUtilActivity();
		//
		// //停止呼叫界面动画
		// Log.i(TAG, "onCallClosed stopBackgroundAnimation.");
		// AnimationUtil.stopBackgroundAnimation();
		//
		// // 挂断时再呼叫菜单还在
		// topMenuLayout.setVisibility(View.GONE);
		// if (menuBarPanel != null)
		// {
		// menuBarPanel.removeLinkedView(topMenuLayout);
		// menuBarPanel.removeLinkedView(mobileVideoLayout);
		// }

		resetRender();

		// 视频呼叫时开启 视频信息 手机界面会显示竖屏，且呼叫超时时视频信息界面不会自动关闭
		// 视频信息界面关闭
		localVideoView.removeAllViews();
		remoteVideoView.removeAllViews();

		// // 小窗口相关 返回初始状态 显示关闭本地视频按钮 本地视频区域 不显示开启本地视频按钮
		localVideoLayout.setVisibility(View.VISIBLE);
		//
		Log.i(TAG, "onCallClosed resetData.");
		resetData();

		// callModifyLogic = null;

		if (null != LocalHideRenderServer.getInstance())
		{
			Log.i(TAG, "onCallClosed destroy LocalHideRenderServer.");
			// LocalHideRenderServer第二次Destroy的时候可能有问题，所以修改上边代码为直接调用onDestroy试试
			// 直接调用onDestroy可以解决问题，可能是Server定义的问题吧
			LocalHideRenderServer.getInstance().onDestroy();
		}
		//
		// Log.i(TAG, "onCallClosed sendHandlerMessage set state online.");
		// HomeActivity.sendHandlerMessage(Constant.MSG_SELF_CHANGE_STATE,
		// PersonalContact.ON_LINE);

		Log.i(TAG, "onCallClosed sendHandlerMessage back to home.");
		// HomeActivity.sendHandlerMessage(Constants.MSG_CALL_CLOSE_BACK_TO_HOME,
		// null);
		// TO invite
		if (null != CallActivity.getInstance())
		{
			// CallActivity.getInstance().closeCallBackToHome();
			CallActivity.getInstance().sendHandlerMessage(Constants.MSG_CALL_CLOSE_BACK_TO_HOME, null);
		}
		// Log.i(TAG, "onCallClosed whenCallEndShowLastActivity.");
		// ActivityStackManager.INSTANCE.whenCallEndShowLastActivity();
		// // 如果后台运行或屏幕暗掉，提示对端已经挂断
		// Log.i(TAG, "onCallClosed lightScreen.");
		// lightScreen(DeviceUtil.LIGHT_TIME_SHORT);
		// pad视频呼出，对端无响应，本端“挂机”与“取消”按钮无响应
		// 对方已经挂断的情况下不需要显示dialog
		if (null != menuBarPanel)
		{
			menuBarPanel.setNeedShow(false);
		}

		Log.i(TAG, "onCallClosed leave.");
	}

	/**
	 * 销毁通话附属activity
	 */
	private void destroyUtilActivity()
	{

		if (null != VideoInfoActivity.getInstance())
		{
			VideoInfoActivity.getInstance().finish();
		}

		// Activity pdfBrowserActivity = null;
		// int length = ActivityStackManager.INSTANCE.getStackSize();
		// for (int i = 0; i < length; i++)
		// {
		// LayoutUtil.releaseFrontToLock(ActivityStackManager.INSTANCE.getActivityByIndex(i));
		// // 文件浏览的Activity只有在通话中有
		// if (ActivityStackManager.INSTANCE.getActivityByIndex(i) instanceof
		// FileBrowserActivity)
		// {
		// if (((FileBrowserActivity)
		// ActivityStackManager.INSTANCE.getActivityByIndex(i)).getType() ==
		// FileBrowserActivity.PDF)
		// {
		// pdfBrowserActivity =
		// ActivityStackManager.INSTANCE.getActivityByIndex(i);
		// }
		// }
		// }
		// if (null != pdfBrowserActivity)
		// {
		// pdfBrowserActivity.finish();
		// }
		// ActivityStackManager.INSTANCE.getImgFileListActivityAndRemove();
		// ActivityStackManager.INSTANCE.getImgShowActivityAndRemove();
	}

	/**
	 * 关闭本地摄像头
	 */
	private void closeLocalView()
	{
		if (null != localVideoView.getChildAt(0))
		{
			localVideoView.getChildAt(0).setVisibility(View.GONE);
		}

		// isCloseLocal = true;
		localVideoLayout.setVisibility(View.GONE);
	}

	/**
	 * 开关mic
	 */
	public void confMuteMic(boolean isConfMute)
	{
		if (null != menuBarPanel)
		{
			menuBarPanel.changeMicImg(isConfMute);
		}
	}

	/**
	 * 更新音频路由
	 */
	public void updateAudioRoute()
	{
		if (null == menuBarPanel)
		{
			return;
		}
		menuBarPanel.onAudioRouteChange();
	}

	/**
	 * 刷新信号强度
	 * 
	 * @param level
	 *            信号强度
	 */
	public void updateSignal(VoiceQualityLevel level)
	{
		if (null == menuBarPanel)
		{
			return;
		}
		menuBarPanel.setSignalStrength(level);
	}

	/**
	 * 通话结束 重置数据
	 */
	private void resetData()
	{
		// 重置小窗口位置
		// resetLocalRender();
		// isCloseLocal = false;

		// 是否显示Record图标
		// recodeImg(false);
		// 重置PDF初始位置标识位
		// isFirstTime = true;

		// 重置大小屏标志位 默认为true
		// isFullScreen = true;

		// 取消菜单栏绑定（5秒后消失）
		// if (null != menuBarPanel)
		// {
		// menuBarPanel.removeLinkedView(topMenuLayout);
		// }
		if (null != menuBarPanel)
		{
			menuBarPanel.resetData();
		}

		// topMenuLayout.setVisibility(View.GONE);

		// 关闭采集点服务
		if (null != LocalHideRenderServer.getInstance())
		{
			LocalHideRenderServer.getInstance().removeView();
		}

		// 双方同时点击共享
		// resetDocShareState();

		// 重置手机版本
		// videoShareTip.setImageDrawable(rootView.getResources()
		// .getDrawable(R.drawable.te_phone_vedio_share_right));
		// videoShareTip.setImageBitmap(ImageResourceUtil.getIns()
		// .readBitMap(rootView.getContext(),
		// R.drawable.te_phone_vedio_share_right));
	}

	/**
	 * 还原缩放后的视图
	 */
	private void resetRender()
	{
		// 还原缩放后的视图
		if (remoteVideoView != null && remoteVideoView instanceof VariationView)
		{
			VariationView variationView = (VariationView) remoteVideoView;
			variationView.resetData();
		}
	}

	/**
	 * 显示通话中界面 视频通话 语音通话
	 */
	private void showChatLayout(String callNumber, boolean isVideoCall, String tiptxt)
	{

		if (isVideoCall)
		{
			// BFCP是否可用
			if (CallLogic.getIns().isEnableBfcp())
			{
				menuBarPanel.changeMode(Mode.VIDEO_CALL);
			} else
			{
				// BFCP不可用
				menuBarPanel.changeMode(Mode.BFCP_NOT_ENABLED);
			}
		} else
		{
			// isCloseLocal = false;// 重置关闭本地图标状态
			menuBarPanel.changeMode(Mode.AUDIO_CALL);
			menuBarPanel.reSetVideoToAudioState();
			menuBarPanel.setPipTips(true);
			// 是否显示Record图标
			// recodeImg(false);
		}
		// 手机界面时，从呼叫界面跳到主界面，进入通话时，还是主界面时，要刷新主界面的提示语,暂注释
		// if (CallLogic.getIns().isMainView()) {
		// HomeActivity.getInstance().homeStateTip();
		// }

		// 视频通话中
		if (isVideoCall)
		{
			showVideoChat(callNumber, tiptxt);
			return;
		}
		showAudioChat(callNumber, tiptxt);
	}

	/**
	 * 音频通话界面
	 */
	private void showAudioChat(String callNumber, String tiptxt)
	{
		Log.i(TAG, "enter showAudioChat");

		// audioCallImg.setVisibility(View.GONE);

		// 显示语音通话 挂断按钮
		// audioChatImg.setVisibility(View.VISIBLE);
		// TO invoke
		// hangupAudioBtn.setVisibility(View.VISIBLE);

		// 设置显示内容
		// setAudioCallShowText(callNumber, tiptxt);
		// TO invoke
		numberAudioTV.setText(callNumber);
		hintAudioTV.setText(tiptxt);

		// //更改手机语音挂断提示
		// if (!ConfigApp.getInstance().isUsePadLayout())
		// {
		// audiocancelBtn.setText(getString(R.string.hangup));
		// }
		// else
		// {
		// //解除绑定
		// menuBarPanel.clearAllLinkedView();
		// mobileVideoLayout.setVisibility(View.GONE);
		// }

		// 音频通话区域显示
		// showAudioCallLayout();
		// TO invoke
		audioCallLayout.setVisibility(View.VISIBLE);
		videoChatLayout.setVisibility(View.GONE);
		// // 手机界面语音呼叫通话时状态图标不显示
		// if (isTitleShow) {
		// titleMenu.setVisibility(View.VISIBLE);
		// } else {
		// titleMenu.setVisibility(View.INVISIBLE);
		// }
		// processTipLayout.setVisibility(View.GONE);
	}

	/**
	 * 显示视频通话
	 */
	private void showVideoChat(String callNumber, String tiptxt)
	{
		Log.e(TAG, "showVideoChat()");
		// 显示本地视频区域 关闭本地视频按钮 视频通话区域
		localVideoLayout.setVisibility(View.VISIBLE);
		menuBarPanel.setPipTips(true);
		// showVideoChatLayout();
		// TO invoke
		audioCallLayout.setVisibility(View.GONE);
		videoChatLayout.setVisibility(View.VISIBLE);

		// AnimationUtil.stopBackgroundAnimation();

		// 不显示关闭本地视频按钮 语音通话按钮
		audioCallLayout.setVisibility(View.GONE);

		// BFCP 应该是共享相关的
		// Log.i(TAG, "getBfcpStatus(): " + CallLogic.getIns().getBfcpStatus());
		// if (CallLogic.BFCP_END.equals(CallLogic.getIns().getBfcpStatus())) {
		// // 设置显示内容
		// setVideoCallShowText(callNumber, tiptxt);
		// videocallTipView.setVisibility(View.VISIBLE);
		// videocallTextView.setVisibility(View.VISIBLE);
		//
		// processTipLayout.setVisibility(View.VISIBLE);
		// mobileVideoLayout.setVisibility(View.VISIBLE);
		//
		// menuBarPanel.addLink(mobileVideoLayout);
		// }
		// // 处理恢复时本端在发辅流
		// else if
		// (CallLogic.BFCP_START.equals(CallLogic.getIns().getBfcpStatus())) {
		// changeToBfcpView();
		// }
		// // end added by c00349133 reason:处理恢复时本端在发辅流
		// // 音频接听后转视频，如果对方本来是辅流发送状态的 要设置成辅流模式的menubar
		// else {
		// // 显示对方正在共享文档的提示信息
		// // topMenuLayout.setVisibility(View.VISIBLE);
		// // 停止辅流 开始发送 返回按钮不显示
		// stopShareBtn.setVisibility(View.GONE);
		// shareBtn.setVisibility(View.GONE);
		// backBtn.setVisibility(View.GONE);
		// // *** 视频通话中.. 不显示
		// videocallTipView.setVisibility(View.GONE);
		// processTipLayout.setVisibility(View.GONE);
		// videocallTextView.setVisibility(View.GONE);
		// mobileVideoLayout.setVisibility(View.GONE);
		// }
		menuBarPanel.show();
		// 只有通话建立 才去addView
		if (CallLogic.getIns().getVoipStatus() == CallLogic.STATUS_VIDEOING)
		{
			Log.i(TAG, "STATUS_VIDEOING addVideoView");
			// 设置下发图片格式 - 重协商或者视频通话的时候
			// resetFramesize();
			View remoteVV = VideoHandler.getIns().getRemoteCallView();
			if (null != remoteVV && null == remoteVV.getParent())
			{
				// 只有第一次进入视频通话的时候才去添加view，如果是视频参数更改之类的就不去做此操作
				addVideoView();
			}
			// 保持屏幕长亮
			// DeviceUtil.setKeepScreenOn(this.getActivity());
		}
	}

	/**
	 * 添加视频
	 */
	private void addVideoView()
	{
		// 在Fragment中显示不需要NoTitle
		// showScreenNoTitle();
		// 将小窗口的位置还原
		// resetLocalRender();

		View localHide = VideoHandler.getIns().getLocalHideView();
		if (null != localHide && null != LocalHideRenderServer.getInstance())
		{
			Log.d(TAG, "Add Hide");
		}

		// 获取远端视频
		remoteVideoView.setVisibility(View.VISIBLE);
		// isRenderChange = false;
		addRenderToContain(localVideoView, remoteVideoView, true);
		VideoHandler.getIns().orientationPreChange();
		// 软终端视频通话时，本地与远端视频切换时，软终端概率性异常退出
		// if (LayoutUtil.isPhone())
		// {
		// }

	}

	/**
	 * render控制锁
	 */
	private static final Object RENDER_CHANGE_LOCK = new Object();

	/**
	 * 设置哪个view在最上面
	 * 
	 * @param localView
	 *            包含本地render
	 * @param remoteView
	 *            包含远端render
	 * @param isLocal
	 *            true 本地最上面 false远端最上面
	 */
	private void addRenderToContain(ViewGroup localViewContain, ViewGroup remoteViewContain, boolean isLocal)
	{
		Log.d(TAG, "addRenderToContain()");
		synchronized (RENDER_CHANGE_LOCK)
		{
			CallControl.getInstance().controlRenderVideo(CallCommandParams.MMV_SWITCH_LCLRENDER | CallCommandParams.MMV_SWITCH_RMTRENDER, false);
			localViewContain.removeAllViews();
			remoteViewContain.removeAllViews();
			SurfaceView localVV = VideoHandler.getIns().getLocalCallView();
			SurfaceView remoteVV = VideoHandler.getIns().getRemoteCallView();

			if (null == localVV || null == remoteVV)
			{
				return;
			}

			if (isLocal)
			{
				remoteVV.setZOrderMediaOverlay(false);
				localVV.setZOrderMediaOverlay(true);

				addViewToContain(remoteVV, remoteViewContain);
				addViewToContain(localVV, localViewContain);
			} else
			{
				localVV.setZOrderMediaOverlay(false);
				remoteVV.setZOrderMediaOverlay(true);
				addViewToContain(localVV, localViewContain);
				addViewToContain(remoteVV, remoteViewContain);
			}
			CallControl.getInstance().controlRenderVideo(CallCommandParams.MMV_SWITCH_LCLRENDER | CallCommandParams.MMV_SWITCH_RMTRENDER, true);

			CallControl.getInstance().controlRenderVideo(CallCommandParams.MMV_SWITCH_CAPTURE, true);

			// int mediaSwitch = isStart ? CallCommandParams.MMV_CONTROL_START
			// : CallCommandParams.MMV_CONTROL_STOP;
			// return videoControl(CallCommandParams.MMV_SWITCH_CAPTURE,
			// mediaSwitch);
		}
	}

	/**
	 * 更新呼叫中界面显示 （视频呼叫 语音呼叫）
	 * 
	 * @param isVideoCall
	 *            是否是视频通话
	 * @param tipTxt
	 *            提示文字
	 */
	private void showCallLayout(String callNumber, boolean isVideoCall, String tiptxt)
	{
		Log.d(TAG, "showCallLayout()");
		// 视频呼叫中
		if (isVideoCall)
		{
			// 设置显示text内容
			// setVideoCallShowText(callNumber, tiptxt);
			// videocallTipView.setVisibility(View.VISIBLE);
			// videocallTextView.setVisibility(View.VISIBLE);

			// 视频呼叫区域显示
			// showVideoCallLayout();
			// TO invoke
			audioCallLayout.setVisibility(View.GONE);
			videoChatLayout.setVisibility(View.GONE);

			// 改变菜单栏模式
			changeMode(Mode.VIDEO_CALLING);

			// 设置voido 标题栏显示
			// processTipLayout.setVisibility(View.VISIBLE);
			// mobileVideoLayout.setVisibility(View.VISIBLE);

			if (menuBarPanel != null)
			{
				// 用于控制与menuBar同步显示/隐藏的View，本Demo应该不需要此功能
				// menuBarPanel.addLink(mobileVideoLayout);
				menuBarPanel.show();
			}

			View localVV = VideoHandler.getIns().getLocalCallView();
			if (null != localVV)
			{
				addViewToContain(localVV, previewLayout);
			}
			return;
		}

		// 语音通话中
		// 不显示视频呼叫 挂断按钮 语音通话 视频通话
		// audioChatImg.setVisibility(View.GONE);

		// 显示语音呼叫 取消按钮
		// hangupAudioBtn.setVisibility(View.VISIBLE);
		hintAudioTV.setText(tiptxt);

		// 设置显示内容
		if (null != callNumber)
		{
			numberAudioTV.setText(callNumber);
		} else
		{
			numberAudioTV.setText("");
		}

		// 语音通话区域显示
		audioCallLayout.setVisibility(View.VISIBLE);
		videoChatLayout.setVisibility(View.GONE);

		// processTipLayout.setVisibility(View.GONE);

		// 改变菜单栏模式
		changeMode(Mode.AUDIO_CALLING);
	}

	/**
	 * 改变菜单栏模式
	 * 
	 * @param mode
	 *            模式
	 */
	private void changeMode(com.huawei.te.example.menubar.MenuBarContalPanel.Mode mode)
	{
		if (null == menuBarPanel)
		{
			Log.i(TAG, "menuBarPanel is   null  return");
			return;
		}
		menuBarPanel.changeMode(mode);
	}

	/**
	 * 添加视频到布局
	 */
	private void addViewToContain(View videoView, ViewGroup videoContain)
	{
		if (null == videoView || null == videoContain)
		{
			Log.i(TAG, "Some Is Null");
			return;
		}
		ViewGroup container = (ViewGroup) videoView.getParent();
		videoView.setVisibility(View.GONE);
		videoContain.removeAllViews();
		if (null == container)
		{
			Log.i(TAG, "No Parent");
			videoContain.addView(videoView);
		} else if (!container.equals(videoContain))
		{
			container.removeView(videoView);
			Log.i(TAG, "Diferent Parent");
			videoContain.addView(videoView);
		} else
		{
			Log.i(TAG, "Same Parent");
		}
		videoContain.setVisibility(View.VISIBLE);
		videoView.setVisibility(View.VISIBLE);
	}

	/**
	 * 会话更改 三种形式
	 * 
	 * @param modifyType
	 *            更改类型
	 */
	public void voipCallModify(ModifyNoticeType modifyType)
	{
		if (null == callModifyLogic)
		{
			callModifyLogic = new VoipCallModifyLogic();
		}
		// TODO 通知通话界面刷新
		Log.i(TAG, "voipCallModify modifytype:" + modifyType);
		// if (!ConfigApp.getInstance().isUsePadLayout())
		// {
		// HomeActivity.getInstance().homeStateTip();
		// }
		switch (modifyType) {
		case VoiceToVideo:
			if (callModifyLogic.isShowing())
			{
				return;
			}
			// 升级视频时先停止视频信息界面显示
			if (null != VideoInfoActivity.getInstance())
			{
				VideoInfoActivity.getInstance().finish();
			}

			// lightScreen(DeviceUtil.LIGHT_TIME_LONG);
			callModifyLogic.voiceToVideo();
			updateCallLayout();
			break;
		case VideoToVoice:
			if (null != VideoInfoActivity.getInstance())
			{
				VideoInfoActivity.getInstance().finish();
			}
			// lightScreen(DeviceUtil.LIGHT_TIME_SHORT);
			//
			// // 发辅流中时 转音频 关闭辅流切换状态为音频通话中
			// if
			// (CallLogic.BFCP_START.equals(CallLogic.getIns().getBfcpStatus()))
			// {
			// stopDocShare();
			// }
			// end added by pwx178217 2013/12/14 reason:升级视频时先停止视频信息界面显示
			updateCallLayout();
			break;
		case ModifyRequestFalied:
			callModifyLogic.modifyRequestFalied();
			break;
		// begin modified by cwx176934 2013/11/22 Reason:ANDROID-181
		// 添加响应对方取消升级视频操作
		case ModifyRequestCancel:
			callModifyLogic.modifyRequestCancel();
			break;
		// end modified by cwx176934 2013/11/22 Reason:ANDROID-181
		// 添加响应对方取消升级视频操作
		case defaultType:
			updateCallLayout();
			break;
		default:
			break;

		}
	}

	/**
	 * 更新呼叫界面
	 */
	private void updateCallLayout()
	{
		Log.d(TAG, "updateCallLayout()");
		// 开启采集点的服务
		if (null == LocalHideRenderServer.getInstance())
		{
			if (null != CallActivity.getInstance())
			{
				CallActivity.getInstance().startService(new Intent(CallActivity.getInstance(), LocalHideRenderServer.class));
			} else
			{
				Log.e(TAG, " --- CallActivity instance is null ,start LocalHideRenderServer failed!");
			}
		}

		// //对方接听或拒绝建立会话也未能取消此定时器
		// cancelCallTask();

		// //对方挂断电话时取消Popupwindow
		if (null == menuBarPanel)
		{
			menuBarPanel = new MenuBarContalPanel(rootView, menuItemServer);
		}
		// menuBarPanel.addLink(rootView.findViewById(R.id.tool_bar_layout));
		menuBarPanel.dismissPopupWindow();
		//
		// //防止异常挂断时菜单栏还存在
		// menuBarPanel.removeLinkedView(topMenuLayout);
		// topMenuLayout.setVisibility(View.GONE);
		//
		// cancelCallTask();
		int voipStatus = CallLogic.getIns().getVoipStatus();
		// 音频1 视频9
		Log.d(TAG, "voipStatus is:" + voipStatus);
		String callNumber = CallLogic.getIns().getCallNumber();
		// 呼叫中刷新，用于呼叫转移
		boolean isVideo = CallLogic.getIns().isVideoCall();
		// 变更menuBarPanel的音频路由显示
		// updateAudioRoute();
		// 将判断逻辑发在外层，便于修改
		boolean isCalling = false;
		boolean isRefer = false;
		boolean isCallToVideo = false;
		boolean isAudioChat = false;
		// if (CallLogic.STATUS_CALLING == voipStatus) {
		// isCalling = true;
		// isRefer = true;
		// //二点转多点时，转移前本方在发辅流，转移后将辅流状态重置
		// if (isDocSharing) {
		// resetDocShareState();
		// }
		// }
		if (CallLogic.STATUS_VIDEOINIT == voipStatus)
		{
			isCallToVideo = true;
		}
		if (CallLogic.STATUS_TALKING == voipStatus)
		{
			isAudioChat = true;
			// resetDocShareState();
		}
		tipTxt = getTipTxt(isCalling, isVideo, isRefer, isCallToVideo, isAudioChat);

		// 关闭本地摄像头，没什么用的感觉
		// HomeActivity.sendHandlerMessage(CallConstant.CLOSE_CAMERA, null);
		switch (voipStatus) {

		// 通话状态、通话恢复状态
		case CallLogic.STATUS_TALKING:
			updateLayout(CallLogic.STATUS_TALKING, callNumber, false, tipTxt);
			// HomeActivity.sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT,
			// null);
			// TO invite
			CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
			break;

		// 挂断状态
		case CallLogic.STATUS_CLOSE:
			updateLayout(CallLogic.STATUS_CLOSE, callNumber, false, tipTxt);
			// HomeActivity.sendHandlerMessage(Constants.MSG_CALL_CLOSE_BACK_TO_HOME,
			// null);
			// TO invite
			if (null != CallActivity.getInstance())
			{
				// CallActivity.getInstance().closeCallBackToHome();
				CallActivity.getInstance().sendHandlerMessage(Constants.MSG_CALL_CLOSE_BACK_TO_HOME, null);
			}
			break;
		case CallLogic.STATUS_CALLING:// 目前只有呼转进入
			Log.i(TAG, "CallLogic.STATUS_CALLING:");
			if (isVideo)
			{
				// 初始化视频参数
				VideoHandler.getIns().initCallVideo(getActivity());
			}
			updateLayout(CallLogic.STATUS_CALLING, callNumber, isVideo, tipTxt);
			// HomeActivity.sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT,
			// null);
			// TO invite
			CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
			break;
		// 呼叫中刷新，用于呼叫转移
		// 视频通话初始化状态
		case CallLogic.STATUS_VIDEOINIT:// 目前只有视频升级请求
			// 视频通话进行中
			updateLayout(CallLogic.STATUS_VIDEOINIT, callNumber, false, tipTxt);
			// setAudioCallShowText(callNumber, tipTxt);
			// HomeActivity.sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT,
			// null);
			// TO invite
			CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
			break;
		case CallLogic.STATUS_VIDEOACEPT:
		case CallLogic.STATUS_VIDEOING:
			// 如果是视频升级需要重新创建
			if (null == VideoHandler.getIns().getRemoteCallView())
			{
				VideoHandler.getIns().initCallVideo(this.getActivity());
			}
			updateLayout(CallLogic.STATUS_VIDEOING, callNumber, true, tipTxt);

			CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
			break;

		default:
			break;
		}
	}

	/**
	 * 显示Toast 提示
	 * 
	 * @param strInt
	 *            字符串id
	 */
	private void showToast(int strInt)
	{
		Toast.makeText(getActivity(), getActivity().getString(strInt), Toast.LENGTH_LONG).show();
	}

	/**
	 * 显示视频信息
	 */
	private void showVideoInfo()
	{
		Intent intent = new Intent(rootView.getContext(), VideoInfoActivity.class);
		startActivity(intent);
	}

	/**
	 * 刷新LocalRender视频界面
	 */
	private void refreshView(Message msg)
	{

		synchronized (LocalHideRenderServer.class)
		{
			Log.i(TAG, "refresh_view");
			Boolean str = (Boolean) msg.obj;
			refreshLocalHide(str);
		}

		// TO invoke
		// // 刷新下view
		// // reason：新增程序后台运行的时候关闭摄像头，回来的时候要打开，在开打的时候偶现本远端画面出现黑屏，只有在本远端切换的时候才能恢复
		synchronized (LocalHideRenderServer.class)
		{
			if (null != VideoHandler.getIns().getRemoteCallView() && null != VideoHandler.getIns().getLocalCallView())
			{
				VideoHandler.getIns().getRemoteCallView().postInvalidate();
				VideoHandler.getIns().getLocalCallView().postInvalidate();
			}
		}
	}

	/**
	 * 底下事件刷新
	 * 
	 * @param isAdd
	 *            true添加
	 */
	private void refreshLocalHide(boolean isAdd)
	{
		View localHI = VideoHandler.getIns().getLocalHideView();
		if (localHI == null)
		{
			Log.i(TAG, "localHI is null");
			return;
		}
		if (null == LocalHideRenderServer.getInstance())
		{
			Log.i(TAG, "localHideRenderServer is null");
			return;
		}
		if (!isAdd)
		{
			LocalHideRenderServer.getInstance().removeView(localHI);
		} else
		{
			LocalHideRenderServer.getInstance().addView(localHI);
			// isRenderRemoveDone = true;

			// 刷新下view
			// reason：新增程序后台运行的时候关闭摄像头，回来的时候要打开，在开打的时候偶现本远端画面出现黑屏，只有在本远端切换的时候才能恢复
			if (null != VideoHandler.getIns().getRemoteCallView() && null != VideoHandler.getIns().getLocalCallView())
			{
				VideoHandler.getIns().getRemoteCallView().postInvalidate();
				VideoHandler.getIns().getLocalCallView().postInvalidate();
			}
		}
	}

	// /**
	// * 暂停fragment时候自动调用
	// */
	// @Override
	// public void onPause() {
	// super.onPause();
	// // isRunBehind = true;
	// // 安卓移动软终端入会过程中查看通话状态，仍会启动屏保
	// // 软终端后台运行的时候释放常亮锁
	// if (LocalHideRenderServer.getInstance() != null &&
	// LocalHideRenderServer.getInstance().isBackground()) {
	// DeviceUtil.releaseWakeLock();
	// }
	// }
	/**
	 * 销毁fragment时候自动调用
	 */
	@Override
	public void onDestroy()
	{

		Log.d(TAG, "onDestroy start!~");

		// if (null != timer) {
		// timer = null;
		// }
		// cancelCallTask();
		// stopDocShare();
		// 退出安卓客户端，然后在设置中把语言改为英文，客户端会自动登录
		if (null != LocalHideRenderServer.getInstance())
		{
			// ConfigApp.getInstance().setKillPro(true);
			Log.e(TAG, "onDestroy start!  LocalHideRenderServer onDestroy~~~~~~~~~~~~~~~~~~~~~~~~~");
			LocalHideRenderServer.getInstance().onDestroy();
		}
		// 退出安卓客户端，然后在设置中把语言改为英文，客户端会自动登录
		if (null != callCtlThreadPool)
		{
			callCtlThreadPool.shutdown();
			callCtlThreadPool = null;
		}
		// callModifyLogic = null;
		super.onDestroy();
		// clearData();
	}

	/**
	 * 显示工具条
	 */
	private void setRootViewListener()
	{
		rootView.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "rootView is clicked");
				// 点击显示菜单栏
				if (null == menuBarPanel)
				{
					return;
				}
				menuBarPanel.showAndGone();
				Log.i(TAG, "show menuBar");
			}
		});

		// if ((remoteVideoView instanceof VariationView))
		// {
		// ((VariationView) remoteVideoView).regReaderChangeListener(this);
		// }
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

	}

}
