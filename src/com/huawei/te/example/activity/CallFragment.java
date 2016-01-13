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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.esdk.te.call.CallConstants.CallStatus;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.call.CallService;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.CallConstant;
import com.huawei.esdk.te.data.Constants.MsgCallFragment;
import com.huawei.esdk.te.util.LayoutUtil;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.esdk.te.video.LocalHideRenderServer;
import com.huawei.esdk.te.video.VariationView;
import com.huawei.esdk.te.video.VideoHandler;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.CallControl.ModifyNoticeType;
import com.huawei.te.example.R;
import com.huawei.te.example.call.VoipCallModifyLogic;
import com.huawei.te.example.menubar.MenuBarContalPanel;
import com.huawei.te.example.menubar.MenuBarContalPanel.MenuItemServer;
import com.huawei.te.example.menubar.MenuBarContalPanel.Mode;
import com.huawei.te.example.utils.AddTouchEventUtil;
import com.huawei.utils.StringUtil;
import com.huawei.voip.data.VoiceQuality.VoiceQualityLevel;

/**
 * render 操作流程 stop --> remove --> add --> start
 * addView必须在hme_videorender_start之前，原因没addview相当于后台渲染，opengl操作有风险
 * removeView必须在hme_videorender_stop之后 呼叫fragment
 */

public class CallFragment extends Fragment implements OnClickListener
{

	private static final String TAG = CallFragment.class.getSimpleName();

	private static final Object VIDEO_HANGUP_LOCK = new Object();

	private TextView numberAudioTV;
	private TextView hintAudioTV;

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

	/**
	 * doc锁
	 */
	private static final Object DOCLOCK = new Object();

	/**
	 * 是否正在接受辅流
	 */
	private boolean isReceiving;

	/**
	 * 是否正在共享
	 */
	private boolean isDocSharing;

	/**
	 * 是否在文档预览
	 */
	private boolean isPdfView;

	/**
	 * 是否处于辅流界面
	 */
	private boolean isBfcpView = false;

	/**
	 * 接收和发送事件基准
	 */
	private int baseTime = 0;

	public int getBaseTime()
	{
		return baseTime;
	}

	public void setBaseTime(int baseTime)
	{
		this.baseTime = baseTime;
	}

	/*
	 * 主动发送辅流相对时间点
	 */
	private int sendBfcpTime = 0;

	public int getSendBfcpTime()
	{
		return sendBfcpTime;
	}

	public void setSendBfcpTime(int sendBfcpTime)
	{
		this.sendBfcpTime = sendBfcpTime;
	}

	/**
	 * 辅流发送标志
	 */
	private boolean bfcpSendTag = false;

	public boolean isBfcpSendTag()
	{
		return bfcpSendTag;
	}

	public void setBfcpSendTag(boolean bfcpSendTag)
	{
		this.bfcpSendTag = bfcpSendTag;
	}

	/*
	 * 收到辅流共享相对时间点
	 */
	private int recvBfpcTime = 0;

	public int getRecvBfpcTime()
	{
		return recvBfpcTime;
	}

	public void setRecvBfpcTime(int recvBfpcTime)
	{
		this.recvBfpcTime = recvBfpcTime;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);

		rootView = (ViewGroup) inflater.inflate(R.layout.call_fraglayout, container, false);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		// 初始化组件
		initComponent();
		
		// 设置全局点击事件

		// 设置小窗口可拖动
		AddTouchEventUtil addTouchUtil = new AddTouchEventUtil(localVideoLayout, rootView.findViewById(R.id.line_left),
				rootView.findViewById(R.id.line_right), rootView.findViewById(R.id.line_top), rootView.findViewById(R.id.line_bottom));
		addTouchUtil.addTouchEvent(localVideoView);
		
		callCtlThreadPool = Executors.newSingleThreadExecutor();
		setRootViewListener();
	}

	private void initComponent()
	{
		initMenuItemServer();
		numberAudioTV = (TextView) getActivity().findViewById(R.id.tv_audio_number);
		hintAudioTV = (TextView) getActivity().findViewById(R.id.tv_audio_hint);

		// 音频呼叫区域
		audioCallLayout = (RelativeLayout) rootView.findViewById(R.id.audio_calllayout);

		// 视频通话区域
		videoChatLayout = (RelativeLayout) rootView.findViewById(R.id.video_chatlayout);

		// 本地视频显示区域
		localVideoLayout = (RelativeLayout) rootView.findViewById(R.id.local_layout);
		initHandler();

		// 远端视频
		remoteVideoView = (RelativeLayout) rootView.findViewById(R.id.remote_videoview);

		// 本地视频区域
		localVideoView = (RelativeLayout) rootView.findViewById(R.id.local_videoview);

		// 呼叫时本地视频显示区域
		previewLayout = (LinearLayout) rootView.findViewById(R.id.pre_local_video);

	}

	private void initHandler()
	{
		if (null != handler)
		{
			LogUtil.d(TAG, "the handler has init.");
			return;
		}
		handler = new Handler()
		{
			@Override
			public void dispatchMessage(Message msg)
			{
				LogUtil.d(TAG, "what:" + msg.what);
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
			LogUtil.d(TAG, "the initMenuItemServer has init.");
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
				LogUtil.i(TAG, "close PIP " + isPip);
				if (isPip)
				{
					switchView();
				} else
				{
					closeLocalView();
				}
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

	/**
	 * 切换摄像头
	 */
	private void switchView()
	{
		localVideoLayout.setVisibility(View.VISIBLE);
		if (null != localVideoView.getChildAt(0))
		{
			localVideoView.getChildAt(0).setVisibility(View.VISIBLE);
		}
		// isCloseLocal = false;
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

	public void sendHandlerMessage(int what, Object object)
	{
		if (handler == null)
		{
			LogUtil.d(TAG, "sendHandlerMessage() handler is null");
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
			// if (null == msg.obj || null ==
			// LocalHideRenderServer.getInstance())
			// {
			// return;
			// }
			// refreshView已经添加到SDK中执行
			// refreshView(msg);
			break;
		// case MsgCallFragment.MSG_ADD_VIDEO_TIME_OUT:
		// showToast(R.string.add_video_time_out);
		// break;
		// case MsgCallFragment.MSG_DATA_DECODE_SUCCESS:
		// LogUtil.i(TAG, "data decode success UI receive");
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
		// LogUtil.i(TAG, "enable ConfControl");
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
			LogUtil.d(TAG, "handlerMessageView receives:" + msg.what);
		}
		switch (msg.what) {
		case MsgCallFragment.MSG_SHOW_AUDIOVIEW:
			if (!(msg.obj instanceof String))
			{
				LogUtil.i(TAG, "msg.obj is not instanceof String");
				return;
			}
			showCallingLayout((String) msg.obj, false);
			break;
		case MsgCallFragment.MSG_SHOW_VIDEOVIEW:
			if (!(msg.obj instanceof String))
			{
				LogUtil.i(TAG, "msg.obj is not instanceof String");
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
			LogUtil.d(TAG, "handlerMessageCall receives:" + msg.what);
		}
		switch (msg.what) {
		case MsgCallFragment.MSG_DIALCALL_AUDIO:
			if (!(msg.obj instanceof String))
			{
				LogUtil.i(TAG, "msg.obj is not instanceof String");
				return;
			}
			dialCall((String) msg.obj, false);
			break;
		case MsgCallFragment.MSG_DIALCALL_VIDEO:
			dialCall((String) msg.obj, true);
			break;
		case MsgCallFragment.MSG_CALL_END_REQUEST:
			// 停止呼叫
			CallControl.getInstance().closeCall();
			break;
		case MsgCallFragment.MSG_CALL_UPDATE_UI:
			// 处理会话保持情况
			LogUtil.i(TAG, "MsgCallFragment.MSG_CALL_UPDATE_UI");
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
			LogUtil.d(TAG, "MsgCallFragment.MSG_CALL_MODIFY_UI");
			ModifyNoticeType modifyType = (ModifyNoticeType) msg.obj;
			voipCallModify(modifyType);
			break;
		// start by c00349133 reason: 会话保持时，通信界面显示“通话被保持”
		case MsgCallFragment.MSG_SHOW_SESSION_HOLD:
			LogUtil.i(TAG, "MsgCallFragment.MSG_SHOW_SESSION_HOLD");
			isSessionHold = true;
			// boolean isVideo = CallService.getInstance().isVideoCall();
			// LogUtil.i(TAG, "MsgCallFragment.MSG_SHOW_SESSION_HOLD" + isVideo);
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
			LogUtil.i(TAG, "MsgCallFragment.MSG_NOTIFY_CALLCOMING_DESTORY");
			// setScrean针对手机屏幕做特殊处理
			// CallService.getInstance().isVideoCall());
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
		// 视频通话中，TE30关闭本地视频，软终端显示对端残留的最后一帧图像
		case MsgCallFragment.MSG_REMOTE_VIDEO_UPDATE:
			LogUtil.i(TAG, "MSG_REMOTE_VIDEO_UPDATE" + isSessionHold);
			if (isSessionHold)
			{
				if (CallService.getInstance().isVideoCall())
				{
					// videocallTipView.setText(getString(R.string.video_chat));
					// reason:解决64k带宽下，二点转多点，会话恢复时，转语音不可用
					if (!CallService.getInstance().isEnableBfcp())
					{
						menuBarPanel.changeMode(Mode.BFCP_NOT_ENABLED);
					}
				} else
				{
					// audiocallTipView.setText(getString(R.string.audio_chat));
					// 与TE30 音频通话后，TE30保持通话再恢复，软终端音频转视频功能不可用
					menuBarPanel.changeMode(Mode.AUDIO_CALL);
				}
				isSessionHold = false;
			}
			// Hold功能尚未添加，此处暂留
			if (null != msg.obj && null != VideoHandler.getIns().getRemoteCallView())
			{
				LogUtil.i(TAG, "callFragment recevie remote video update close:[" + msg.obj + ']');
				cleanRemoteFrame((Boolean) msg.obj);
			}
			break;
		case MsgCallFragment.MSG_AUDIO_ROUTE_UPDATE:
			updateAudioRoute();
			break;
		default:
			break;
		}
	}

	// Hold功能尚未添加，此处暂留
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
			LogUtil.i(TAG, "remote view is null; return;");
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
			LogUtil.i(TAG, "now remote has close [" + isClean + ']');
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
	 * 显示呼出界面
	 */
	public void showCallingLayout(final String callNumber, final boolean isVideoCall)
	{
		if (StringUtil.isStringEmpty(callNumber))
		{
			LogUtil.i(TAG, "empty CallNumber return!!!");
			return;
		}

		// 关闭本地视频预览
		// HomeActivity.sendHandlerMessage(CallConstant.CLOSE_CAMERA, null);

		CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);

		if (isVideoCall)
		{
			// 初始化视频参数
			// CallService.getInstance().initCallVideo();
			sendHandlerMessage(MsgCallFragment.MSG_DIALCALL_VIDEO, callNumber);
		} else
		{
			sendHandlerMessage(MsgCallFragment.MSG_DIALCALL_AUDIO, callNumber);
		}
		tipTxt = getTipTxt(true, isVideoCall, false, false, false);
		updateLayout(CallStatus.STATUS_CALLING, callNumber, isVideoCall, tipTxt);
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
		final String callRet = CallControl.getInstance().dialCall(callNumber, null, isVideoCall);
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

		LogUtil.d(TAG, "updateLayout()");

		// 更新界面的时候重置标志位为需要点击挂断时显示对话框
		if (null != menuBarPanel)
		{
			menuBarPanel.setNeedShow(true);
		} else
		{
			LogUtil.d(TAG, "menuBarPanel is null");
		}

		// 设置是否视频通话
		// CallService.getInstance().setVideoCall(isVideoCall);

		// 开启采集点的服务
		if (null == LocalHideRenderServer.getInstance())
		{
			if (null != CallActivity.getInstance())
			{
				CallActivity.getInstance().startService(new Intent(CallActivity.getInstance(), LocalHideRenderServer.class));
			} else
			{
				LogUtil.e(TAG, " --- CallActivity instance is null ,start LocalHideRenderServer failed!");
			}
		}

		// 保持屏幕长亮
		// DeviceUtil.setKeepScreenOn(getActivity());

		// 显示视频通话title Layout
		// mobileVideoLayout.setVisibility(View.VISIBLE);

		// 更新界面到挂断
		if (CallStatus.STATUS_CLOSE == voipState)
		{
			LogUtil.i(TAG, "end hangup");
			return;
		}

		if (null != callNumber)
		{
			numberAudioTV.setText(callNumber);
		}

		if (null == menuBarPanel)
		{
			LogUtil.d(TAG, "menuBarPanel is null , create it.");
			menuBarPanel = new MenuBarContalPanel(rootView, menuItemServer);
		}
		menuBarPanel.setRemoteNumber(callNumber);
		updateByState(callNumber, voipState, isVideoCall);
	}

	/**
	 * 根据状态处理界面更新
	 */
	private void updateByState(String callNumber, int voipState, boolean isVideoCall)
	{

		LogUtil.d(TAG, "updateByState() voipStateis:" + voipState);

		if (CallStatus.STATUS_CALLING == voipState)
		{
			showCallLayout(callNumber, isVideoCall, tipTxt);
			// HomeActivity.sendHandlerMessage(Constants.MSG_SELF_CHANGE_STATE,
			// PersonalContact.BUSY);
			LogUtil.i(TAG, "to talking state is video call =>" + isVideoCall);
			return;
		}
		// 通话状态 语音通话 视频通话
		if (CallStatus.STATUS_TALKING == voipState || CallStatus.STATUS_VIDEOING == voipState)
		{
			showChatLayout(callNumber, isVideoCall, tipTxt);
			LogUtil.i(TAG, "to chat state is video chat =>" + isVideoCall);
		}
	}

	public void onCallClosed()
	{
		LogUtil.i(TAG, "onCallClosed enter.");
		LogUtil.i(TAG, "onCallClosed destroyUtilActivity.");
		destroyUtilActivity();

		resetRender();

		// 视频呼叫时开启 视频信息 手机界面会显示竖屏，且呼叫超时时视频信息界面不会自动关闭
		// 视频信息界面关闭
		localVideoView.removeAllViews();
		remoteVideoView.removeAllViews();

		// // 小窗口相关 返回初始状态 显示关闭本地视频按钮 本地视频区域 不显示开启本地视频按钮
		localVideoLayout.setVisibility(View.VISIBLE);
		//
		LogUtil.i(TAG, "onCallClosed resetData.");
		resetData();

		// callModifyLogic = null;

		if (null != LocalHideRenderServer.getInstance())
		{
			LogUtil.i(TAG, "onCallClosed destroy LocalHideRenderServer.");
			// LocalHideRenderServer第二次Destroy的时候可能有问题，所以修改上边代码为直接调用onDestroy试试
			// 直接调用onDestroy可以解决问题，可能是Server定义的问题吧
			LocalHideRenderServer.getInstance().onDestroy();
		}
		// LogUtil.i(TAG, "onCallClosed sendHandlerMessage set state online.");
		// HomeActivity.sendHandlerMessage(Constant.MSG_SELF_CHANGE_STATE,
		// PersonalContact.ON_LINE);

		LogUtil.i(TAG, "onCallClosed sendHandlerMessage back to home.");
		if (null != CallActivity.getInstance())
		{
			CallActivity.getInstance().sendHandlerMessage(Constants.MSG_CALL_CLOSE_BACK_TO_HOME, null);
		}
		// ActivityStackManager.INSTANCE.whenCallEndShowLastActivity();
		// // 如果后台运行或屏幕暗掉，提示对端已经挂断
		// LogUtil.i(TAG, "onCallClosed lightScreen.");
		// lightScreen(DeviceUtil.LIGHT_TIME_SHORT);
		// pad视频呼出，对端无响应，本端“挂机”与“取消”按钮无响应
		// 对方已经挂断的情况下不需要显示dialog
		if (null != menuBarPanel)
		{
			menuBarPanel.setNeedShow(false);
		}

		LogUtil.i(TAG, "onCallClosed leave.");
	}

	/**
	 * 重新激活时视频动作
	 */
	@Override
	public void onResume()
	{
		super.onResume();
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
		if (null != menuBarPanel)
		{
			menuBarPanel.resetData();
		}

		// 关闭采集点服务
		if (null != LocalHideRenderServer.getInstance())
		{
			LocalHideRenderServer.getInstance().removeView();
		}
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
			if (CallService.getInstance().isEnableBfcp())
			{
				menuBarPanel.changeMode(Mode.VIDEO_CALL);
			} else
			{
				// BFCP不可用
				menuBarPanel.changeMode(Mode.BFCP_NOT_ENABLED);
			}
		} else
		{
			menuBarPanel.changeMode(Mode.AUDIO_CALL);
			menuBarPanel.reSetVideoToAudioState();
			menuBarPanel.setPipTips(true);
		}

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
		LogUtil.i(TAG, "enter showAudioChat");

		// audioCallImg.setVisibility(View.GONE);

		// 设置显示内容
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
		LogUtil.e(TAG, "showVideoChat()");
		// 显示本地视频区域 关闭本地视频按钮 视频通话区域
		localVideoLayout.setVisibility(View.VISIBLE);
		menuBarPanel.setPipTips(true);
		audioCallLayout.setVisibility(View.GONE);
		videoChatLayout.setVisibility(View.VISIBLE);

		// 不显示关闭本地视频按钮 语音通话按钮
		audioCallLayout.setVisibility(View.GONE);

		menuBarPanel.show();
		// 只有通话建立 才去addView
		if (CallService.getInstance().getVoipStatus() == CallStatus.STATUS_VIDEOING)
		{
			LogUtil.i(TAG, "STATUS_VIDEOING addVideoView");
			addVideoView();
		}
	}

	/**
	 * 添加视频
	 */
	private void addVideoView()
	{
		// 获取远端视频
		remoteVideoView.setVisibility(View.VISIBLE);
		CallService.getInstance().openCallVideo(localVideoView, remoteVideoView, true);
	}

	/**
	 * render控制锁
	 */
	private static final Object RENDER_CHANGE_LOCK = new Object();

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
		LogUtil.d(TAG, "showCallLayout()");
		// 视频呼叫中
		if (isVideoCall)
		{
			// 视频呼叫区域显示
			audioCallLayout.setVisibility(View.GONE);
			videoChatLayout.setVisibility(View.GONE);

			// 改变菜单栏模式
			changeMode(Mode.VIDEO_CALLING);

			if (menuBarPanel != null)
			{
				menuBarPanel.show();
			}

			CallService.getInstance().openLocalPreview(previewLayout);

			return;
		}

		// 语音通话中
		// 不显示视频呼叫 挂断按钮 语音通话 视频通话
		// audioChatImg.setVisibility(View.GONE);

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
			LogUtil.i(TAG, "menuBarPanel is   null  return");
			return;
		}
		menuBarPanel.changeMode(mode);
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
		LogUtil.i(TAG, "voipCallModify modifytype:" + modifyType);
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
			// (CallLogic.BFCP_START.equals(CallService.getInstance().getBfcpStatus()))
			// {
			// stopDocShare();
			// }
			updateCallLayout();
			break;
		// 弹出通话升级视频视频失败对话框 询问是否稍后重试
		case ModifyRequestFalied:
			callModifyLogic.modifyRequestFalied();
			break;
		// 添加响应对方取消升级视频操作
		case ModifyRequestCancel:
			callModifyLogic.modifyRequestCancel();
			break;
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
		LogUtil.d(TAG, "updateCallLayout()");
		// 开启采集点的服务
		if (null == LocalHideRenderServer.getInstance())
		{
			if (null != CallActivity.getInstance())
			{
				CallActivity.getInstance().startService(new Intent(CallActivity.getInstance(), LocalHideRenderServer.class));
			} else
			{
				LogUtil.e(TAG, " --- CallActivity instance is null ,start LocalHideRenderServer failed!");
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
		int voipStatus = CallService.getInstance().getVoipStatus();
		// 音频1 视频9
		LogUtil.d(TAG, "voipStatus is:" + voipStatus);
		String callNumber = CallService.getInstance().getCallNumber();
		// 呼叫中刷新，用于呼叫转移
		boolean isVideo = CallService.getInstance().isVideoCall();
		// 变更menuBarPanel的音频路由显示
		updateAudioRoute();
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
		if (CallStatus.STATUS_VIDEOINIT == voipStatus)
		{
			isCallToVideo = true;
		}
		if (CallStatus.STATUS_TALKING == voipStatus)
		{
			isAudioChat = true;
			// resetDocShareState();
		}
		tipTxt = getTipTxt(isCalling, isVideo, isRefer, isCallToVideo, isAudioChat);

		// 关闭本地摄像头，没什么用的感觉
		// HomeActivity.sendHandlerMessage(CallConstant.CLOSE_CAMERA, null);
		switch (voipStatus) {

		// 通话状态、通话恢复状态
		case CallStatus.STATUS_TALKING:
			updateLayout(CallStatus.STATUS_TALKING, callNumber, false, tipTxt);
			// HomeActivity.sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT,
			// null);
			// TO invite
			CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
			break;

		// 挂断状态
		case CallStatus.STATUS_CLOSE:
			updateLayout(CallStatus.STATUS_CLOSE, callNumber, false, tipTxt);
			// HomeActivity.sendHandlerMessage(Constants.MSG_CALL_CLOSE_BACK_TO_HOME,
			// null);
			// TO invite
			if (null != CallActivity.getInstance())
			{
				// CallActivity.getInstance().closeCallBackToHome();
				CallActivity.getInstance().sendHandlerMessage(Constants.MSG_CALL_CLOSE_BACK_TO_HOME, null);
			}
			break;
		case CallStatus.STATUS_CALLING:// 目前只有呼转进入
			LogUtil.i(TAG, "CallStatus.STATUS_CALLING:");
			updateLayout(CallStatus.STATUS_CALLING, callNumber, isVideo, tipTxt);
			CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
			break;
		// 呼叫中刷新，用于呼叫转移
		// 视频通话初始化状态
		case CallStatus.STATUS_VIDEOINIT:// 目前只有视频升级请求
			// 视频通话进行中
			updateLayout(CallStatus.STATUS_VIDEOINIT, callNumber, false, tipTxt);
			CallActivity.getInstance().sendHandlerMessage(CallConstant.SHOW_CALL_LAYOUT, null);
			break;
		case CallStatus.STATUS_VIDEOACEPT:
		case CallStatus.STATUS_VIDEOING:
			updateLayout(CallStatus.STATUS_VIDEOING, callNumber, true, tipTxt);
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
	 * 接收文档
	 */
	public void receiveDoc()
	{
		LogUtil.i(TAG, "ender recevieDoc()");
		synchronized (DOCLOCK)
		{
			resetRender();
			// 与pc互通，pc抢发辅流，pad端先显示视频画面，再显示pc辅流画面
			boolean bRet = recvDocCondition();
			if (!bRet)
			{
				return;
			}
			synchronized (RENDER_CHANGE_LOCK)
			{
				bRet = CallService.getInstance().openBFCPReceive(localVideoView, remoteVideoView);
			}
			if (bRet)
			{
				return;
			}

			// 与pc互通，pc抢发辅流，pad端先显示视频画面，再显示pc辅流画面
			// 从后台启动拉起界面
			// 接收辅流，点亮屏幕1s
			// lightScreen(DeviceUtil.LIGHT_TIME_MIN);
			// 设置正在共享标识位
			isReceiving = true;
			isDocSharing = true;
			isBfcpView = true;
			isPdfView = false;

			// 改变菜单栏模式
			menuBarPanel.changeMode(Mode.PDF_IS_SHARED);

			LogUtil.i(TAG, "menuBarPanel is not null");
			// menuBarPanel.removeLinkedView(topMenuLayout);
			// menuBarPanel.addLink(mobileVideoLayout);

			if (!LayoutUtil.isPhone())
			{
				menuBarPanel.setRemoteNumberVisible(true);
			}

			// topMenuLayout.setVisibility(View.GONE);
			// mobileVideoLayout.setVisibility(View.VISIBLE);
			// processTipLayout.setVisibility(View.VISIBLE);
			// videocallTextView.setVisibility(View.GONE);
			// videoShareTip.setVisibility(View.VISIBLE);
			menuBarPanel.setPipEnable(false);

			// 显示状态：远端正在共享
			// videocallTipView.setVisibility(View.VISIBLE);
			// videocallTipView.setText(getString(R.string.pdf_doc_sharing));

			if (null != remoteVideoView.getChildAt(0))
			{
				remoteVideoView.getChildAt(0).setVisibility(View.GONE);
			}

			// 手机的图标置为左
			// videoShareTip.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.te_phone_vedio_share_right));

			menuBarPanel.show();
			// 返回，共享，停止共享按钮不可见
			// backBtn.setVisibility(View.GONE);
			// shareBtn.setVisibility(View.GONE);
			// stopShareBtn.setVisibility(View.GONE);
			// 设置小窗口
			localVideoLayout.setVisibility(View.VISIBLE);
			menuBarPanel.setPipTips(true);

			localVideoLayout.setVisibility(View.GONE);
			menuBarPanel.setPipTips(false);
			menuBarPanel.setPipEnable(false);

			// 为防止此方法比第一帧解码还要慢的时候
			// 如果还没有解码，就不设置
			// remoteBfcpView.setVisibility(View.GONE);
			// addBFCPRender();
		}
		LogUtil.i(TAG, "leave recevieDoc()");
	}

	private boolean recvDocCondition()
	{
		if (null == remoteVideoView || null == localVideoView || null == menuBarPanel || isReceiving)
		{
			LogUtil.e(TAG, "receiveDoc error. [remoteVideoView=" + remoteVideoView + "] [localVideoView=" + localVideoView + "] [menuBarPanel="
					+ menuBarPanel + "] [isReceiving=" + isReceiving + ']');
			LogUtil.i(TAG, "leave recevieDoc()");

			if (isReceiving)
			{
				isDocSharing = true;
				isPdfView = false;
			}
			return false;
		}
		return true;
	}

	/**
	 * 对端停止共享
	 */
	public void stopedDocShare()
	{
		LogUtil.i(TAG, "enter stopedDocShare()");
		synchronized (DOCLOCK)
		{
			if (menuBarPanel == null)
			{
				LogUtil.e(TAG, "menuBarPanel is null !");
				return;
			}
			// isRecvDataDecode = false;
			LogUtil.i(TAG, "bfcp stoped,pdfView show:" + isPdfView);

			// 后台运行，拉起界面
			// 停止辅流，点亮屏幕1s
			// lightScreen(DeviceUtil.LIGHT_TIME_MIN);
			// 设置正在共享标识位
			isDocSharing = false;
			isReceiving = false;
			// shareBtn.setEnabled(true);
			// 改变菜单栏模式,停止后回到视频模式
			if (CallStatus.STATUS_VIDEOING == CallLogic.getInstance().getVoipStatus())
			{
				// start 会话保持阶段显示“通话被保持”
				if (isSessionHold)
				{
					LogUtil.i(TAG, "isSessionHold =" + isSessionHold);
					// videocallTipView.setText(getString(R.string.session_holding));
					menuBarPanel.changeMode(Mode.SESSION_HOLD);
				} else
				{
					// videocallTipView.setText(getString(R.string.video_chat));
					menuBarPanel.changeMode(Mode.VIDEO_CALL);
				}
			}
			menuBarPanel.setPipTips(true);

			// 本地小窗口可见
			// isCloseLocal = false;// 重置关闭本地图标状态
			localVideoLayout.setVisibility(View.VISIBLE);

			// 如果是pad时，联系人提示显示出来
			if (!LayoutUtil.isPhone())
			{
				// menuBarPanel.addLink(mobileVideoLayout);
				menuBarPanel.setRemoteNumberVisible(false);
				// mobileVideoLayout.setVisibility(View.VISIBLE);
				// 显示的联系人名字或号码
				// videocallTextView.setVisibility(View.VISIBLE);
				// videoShareTip.setVisibility(View.GONE);
			}
			// 手机模式
			if (LayoutUtil.isPhone())
			{
				// 显示的联系人名字或号码
				// videocallTextView.setVisibility(View.VISIBLE);
				// videoShareTip.setVisibility(View.GONE);
				// 停止共享后，显示状态：正在视频通话
				// videocallTipView.setText(getString(R.string.video_chat));
				// videocallTipView.setVisibility(View.VISIBLE);
				CallService.getInstance().openCallVideo(localVideoView, remoteVideoView, true);

				resetRender();
				LogUtil.i(TAG, "leave stopedDocShare() phone");
				return;
			}

			// 取消菜单栏绑定（5秒后消失）
			// menuBarPanel.removeLinkedView(topMenuLayout);
			menuBarPanel.show();
			// 预览时停止共享
			if (isPdfView && isReceiving)
			{
				// begin add by wx183960 reason:共享停止添加提示
				showToast(R.string.remote_stop_share);
				// topMenuLayout.setVisibility(View.VISIBLE);
				// shareText.setVisibility(View.GONE);
				// stopShareBtn.setVisibility(View.GONE);
				// backBtn.setVisibility(View.VISIBLE);
				// shareBtn.setVisibility(View.VISIBLE);
				// shareBtn.setEnabled(true);
			} else
			{
				// 在主动共享时，收到令牌剥夺也会执行此分支
				// 共享停止添加提示
				showToast(R.string.pdf_stoped);
				// 设置共享菜单栏不可见
				// topMenuLayout.setVisibility(View.GONE);
				// shareText.setVisibility(View.GONE);
				// backBtn.setVisibility(View.VISIBLE);
				// shareBtn.setVisibility(View.VISIBLE);

				// begin modify by cwx176935 reason: DTS2013103000918
				// 软终端视频通话时，本地与远端视频切换时，软终端概率性异常退出
				CallService.getInstance().openCallVideo(localVideoView, remoteVideoView, true);
				// end modify by cwx176935 reason: DTS2013103000918
				// 软终端视频通话时，本地与远端视频切换时，软终端概率性异常退出
				resetRender();
			}
		}
		LogUtil.i(TAG, "leave stopedDocShare() pad");
	}

	/**
	 * 对端停止辅流时需调用的 目前接收到对端停止辅流事件会等待一段时间 看是否需要执行stopedDocShare，
	 * 但是在接收到辅流的时候一些状态必须先置回去,, eg:解码成功消息，主发也会抛解码成功，如果执行等待 则解码成功标志就一直是true，会出现残留帧
	 */
	public void stopedDocShareState()
	{
		// isRecvDataDecode = false;
		LogUtil.i(TAG, "bfcp stoped, reset same doc share state,pdfView show:" + isPdfView);

		// 后台运行，拉起界面
		// 停止辅流，点亮屏幕1s
		// lightScreen(DeviceUtil.LIGHT_TIME_MIN);
		// 设置正在共享标识位
		isDocSharing = false;
	}

	/**
	 * 销毁fragment时候自动调用
	 */
	@Override
	public void onDestroy()
	{

		LogUtil.d(TAG, "onDestroy start!~");

		// if (null != timer) {
		// timer = null;
		// }
		// cancelCallTask();
		// stopDocShare();
		// 退出安卓客户端，然后在设置中把语言改为英文，客户端会自动登录
		if (null != LocalHideRenderServer.getInstance())
		{
			// ConfigApp.getInstance().setKillPro(true);
			LogUtil.e(TAG, "onDestroy start!  LocalHideRenderServer onDestroy~~~~~~~~~~~~~~~~~~~~~~~~~");
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
				LogUtil.d(TAG, "rootView is clicked");
				// 点击显示菜单栏
				if (null == menuBarPanel)
				{
					return;
				}
				menuBarPanel.showAndGone();
				LogUtil.i(TAG, "show menuBar");
			}
		});

	}

	@Override
	public void onClick(View v)
	{
	}

}
