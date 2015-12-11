package com.huawei.te.example;

import java.util.List;

import object.StreamInfo;
import android.content.Intent;
import android.util.Log;

import com.huawei.application.BaseApp;
import com.huawei.common.CallErrorCode;
import com.huawei.esdk.te.call.Call;
import com.huawei.esdk.te.call.CallConstants.BFCPStatus;
import com.huawei.esdk.te.call.CallConstants.CallStatus;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.call.CallNotification;
import com.huawei.esdk.te.call.CallService;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.CallConstant;
import com.huawei.esdk.te.data.Constants.MSG_FOR_HOMEACTIVITY;
import com.huawei.esdk.te.data.Constants.MsgCallFragment;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.esdk.te.util.MediaUtil;
import com.huawei.te.example.activity.CallActivity;
import com.huawei.te.example.activity.CallComingActivity;
import com.huawei.te.example.activity.CallFragment;
import com.huawei.utils.StringUtil;

public class CallControl implements CallNotification
{

	private static final String TAG = CallControl.class.getSimpleName();

	/** 语音通话添加视频（被叫4112） **/
	private static final String VIDEOADD = "add";

	/** 语音通话添加视频（主叫4113） **/
	private static final String VIDEOMOD = "mod";

	private static CallControl instance;

	// 增加用于保存呼叫进来的ID
	private String comingCallID = null;

	/**
	 * Call ID
	 */
	private String callID = null;

	/**
	 * 呼叫状态,初始状态默认为挂断
	 */
	private int callStatus = CallStatus.STATUS_CLOSE;

	public int getCallStatus()
	{
		return callStatus;
	}

	public void setCallStatus(int callStatus)
	{
		this.callStatus = callStatus;
	}

	/**
	 * 返回的失败原因
	 */
	private String reasonText = null;

	// 音视频切换时状态通知，用于界面控制和刷新
	/**
	 * 
	 * 呼叫变更通知类型（用于通知界面展示）
	 */
	public enum ModifyNoticeType
	{
		/**
		 * 默认类型
		 */
		defaultType, /**
		 * 语音转视频
		 */
		VoiceToVideo, /**
		 * 视频转语音
		 */
		VideoToVoice, /**
		 * 变更请求失败
		 */
		ModifyRequestFalied,
		/**
		 * 对方取消升级视频操作
		 */
		ModifyRequestCancel
	}

	public static CallControl getInstance()
	{
		if (null == instance)
		{
			Log.d(TAG, "CallControl construct");
			instance = new CallControl();
		}
		return instance;
	}

	/**
	 * 初始化参数
	 * 
	 * @param service
	 *            服务代理
	 */
	private CallControl()
	{
		Log.d(TAG, "CallControl() construct");
		CallService.getInstance().registerNotification(this);
	}

	/**
	 * 通知会话界面刷新 - callFragment update ui (未接听)
	 */
	private void notifyCallActivityUpdateUI()
	{
		notifyCallViewUpdate(false);
	}

	/**
	 * 通知会话界面刷新 - callFragment update ui (未接听)
	 */
	private void notifyCallActivityUpdateUI(boolean isBfcpEnabled)
	{
		notifyCallViewUpdate(isBfcpEnabled);
	}

	/**
	 * 通知通话界面刷新
	 * 
	 * @param answer
	 *            是否接听 为True时候，这个参数用于TEMobile返回HomeActivity，即返CallActivity，暂时不需要
	 */
	public void notifyCallViewUpdate(boolean answer)
	{
		Log.d(TAG, "notifyCallViewUpdate()");
		CallActivity callActivity = CallActivity.getInstance();
		CallFragment callFragment = callActivity.getCallFragment();
		if (null != callActivity && null != callFragment)
		{
			callFragment.sendHandlerMessage(MsgCallFragment.MSG_CALL_UPDATE_UI, answer);
		}
	}

	/**
	 * _______________________________________ 这些是消息
	 * _________________________________________________
	 */

	/**
	 * 收到通话结束，上报的消息，最终结果在oncalldestroy中处理，此处只用于获取挂断原因
	 */
	private void processCallNtfEnded(Call currentCall)
	{
		if (currentCall == null)
		{
			Log.e(TAG, "session is null.");
			return;
		}
		String callid = currentCall.getCallID();
		if (StringUtil.isStringEmpty(callid))
		{
			Log.e(TAG, "callid is null.");
			return;
		}

		// bye原因
		String reason = currentCall.getReleaseReason();
		Log.i(TAG, "exceedingly call close : " + reason);
		reasonText = setCloseReasonText(reason, currentCall);
	}

	/**
	 * 通话挂断通知 4105
	 * 
	 * @param callsession
	 *            会话对象
	 */
	private void processCallNtfClosed(Call currentCall)
	{
		Log.d(TAG, "processCallNtfClosed()");
		if (currentCall == null)
		{
			Log.e(TAG, "session is null.");
			Log.i(TAG, "processCallNtfClosed leave.");
			return;
		}

		String callid = currentCall.getCallID();
		if (StringUtil.isStringEmpty(callid))
		{
			Log.e(TAG, "callid is null.");
			Log.i(TAG, "processCallNtfClosed leave.");
			return;
		}

		Log.d(TAG, "callid->" + callid);
		if (StringUtil.isNotEmpty(callid) && StringUtil.isNotEmpty(this.callID) && callid.equals(this.callID))
		{
			if (!StringUtil.isStringEmpty(reasonText))
			{
				Log.d(TAG, "for test ~  oncallDestroy is currentCall && reasonText is not empty");
				// 刷voip通话ui
				Log.d(TAG, "processCallNtfClosed reason:" + reasonText);

				notifyCallActivityUpdateUI();
			}

			// 通知界面会话挂断，并给出挂断原因
			notifyHomeActivityUpdateUI(reasonText);

			CallActivity.getInstance().getCallFragment().sendHandlerMessage(Constants.MSG_NOTIFY_CALL_END, null);

			setCallStatus(CallStatus.STATUS_CLOSE);
		} else
		{
			Log.d(TAG, "voip status:" + CallService.getInstance().getVoipStatus());
			// 对 当对方呼叫进来，本端还没有接听的时候对端挂断的情况
			// 进行处理，此时还没有接听呼叫，所以isCurrentCall(callid)返回false;
			// 如果有插入记录说明已经接听的会话(解决多路会话导致更新，插入记录无法判断)
			if (null != comingCallID && callid.equals(comingCallID))
			{
				CallActivity.getInstance().sendHandlerMessage(MSG_FOR_HOMEACTIVITY.MSG_NOTIFY_CALLCLOSE, callid);
				comingCallID = null;
				Log.d(TAG, "voip status:" + CallService.getInstance().getVoipStatus());
			}
		}
		Log.i(TAG, "processCallNtfClosed leave.");
	}

	/**
	 * 接听通知 & 会开始通知 4104(接听后，对方接听后)
	 */
	private void processCallNtfTalk(Call currentCall)
	{
		if (currentCall == null)
		{
			Log.e(TAG, "processCallNtfTalk:session data is null");
			return;
		}
		String callid = currentCall.getCallID();
		if (StringUtil.isStringEmpty(callid))
		{
			Log.e(TAG, "processCallNtfTalk:callid is empty.");
			return;
		}

		String currentCallID = CallService.getInstance().getCurrentCallID();
		boolean notSameidAndlogicIsClose = (!callid.equals(currentCallID) || CallStatus.STATUS_CLOSE == CallService.getInstance().getVoipStatus());
		if (notSameidAndlogicIsClose)
		{
			Log.e(TAG, "is not currentCallID or the CallLogic state is not STATUS_CLOSE, so CallNtfTalk return.");
			return;
		}

		if (!currentCall.isVideoCall())
		{
			// 本方视频呼叫，对方音频接听时，释放视频数据
			if (CallStatus.STATUS_VIDEOINIT == callStatus)
			{
				CallActivity.getInstance().sendHandlerMessage(Constants.MSG_UNINIT_VIDEO, null);
			}
			setCallStatus(CallStatus.STATUS_TALKING);
		} else
		{
			setCallStatus(CallStatus.STATUS_VIDEOING);
		}

		notifyCallActivityUpdateUI();
	}

	/**
	 * 会话变更 4113
	 * 
	 * @param mCallSession
	 *            会话
	 */
	private void processCallNtfModified(final Call currentCall)
	{
		Log.d(TAG, "processCallNtfModified()");
		if (!isCurrentCall(currentCall.getCallID()))
		{
			Log.d(TAG, "[session=" + currentCall + "] [callID=" + currentCall.getCallID() + ']');
			return;
		}

		String oper = currentCall.getOperation();
		int videoModifyState = currentCall.getVideoModifyState();
		Log.d(TAG, "videoModifyState = " + videoModifyState);

		int voipStatus = callStatus;
		Log.d(TAG, "voipStatus = " + callStatus);

		// 关闭视频成功 || 对端请求关闭视频
		boolean isVideoClose = (0 == videoModifyState && CallStatus.STATUS_VIDEOING == voipStatus);
		// 主动升级视频成功
		boolean isActiveUpdateVideo = (1 == videoModifyState && CallStatus.STATUS_VIDEOINIT == voipStatus);
		// 主动升级视频失败
		boolean isVideoUpFailed = (0 == videoModifyState && CallStatus.STATUS_VIDEOINIT == voipStatus);
		// 升级取消
		boolean isVideoUpCanceled = (0 == videoModifyState && CallStatus.STATUS_TALKING == voipStatus);

		if (isVideoClose)
		{
			setCallStatus(CallStatus.STATUS_TALKING);
			CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, ModifyNoticeType.VideoToVoice);
			// 转为音频的时候重置扬声器和听筒
			// resetAudioRoute(false);
		}
		// 主动升级
		// TE30对接，会话重协商。
		else if (isActiveUpdateVideo)
		{
			if (currentCall.getRemoteVideoState() != 0)
			{
				boolean isRemoteVideoClose = currentCall.getRemoteVideoState() == 1;
				CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_REMOTE_VIDEO_UPDATE, isRemoteVideoClose);
			}
			Log.d(TAG, "Upgrade To Video Call");
			setCallStatus(CallStatus.STATUS_VIDEOING);
			// resetAudioRoute(true);
			notifyCallActivityUpdateUI();
		} else if (isVideoUpFailed)
		{
			setCallStatus(CallStatus.STATUS_TALKING);
			notifyCallViewUpdate(false);
			CallActivity.getInstance().getCallFragment()
					.sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallControl.ModifyNoticeType.ModifyRequestFalied);
		} else if (VIDEOMOD.equals(oper))
		{
			if (currentCall.getRemoteVideoState() != 0)
			{
				notifyCallActivityUpdateRemoteVideo(currentCall.getRemoteVideoState() == 1);
			}
			// CallService.getInstance().setEnableBfcp(currentCall.isBFCPSuccess());
			// 与VCT对接辅流时音视频变换，界面异常
			boolean statusOfBFCP = (BFCPStatus.BFCP_RECEIVE.equals(CallService.getInstance().getBfcpStatus()) || BFCPStatus.BFCP_START.equals(CallService
					.getInstance().getBfcpStatus()));
			if (statusOfBFCP)
			{
				Log.i(TAG, "not refresh ui the bfcpStatus is " + CallService.getInstance().getBfcpStatus());
				return;
			}
			// 与VCT对接辅流时音视频变换，界面异常
			notifyCallActivityUpdateUI();
		}
		// 通知界面取消升级视频
		else if (isVideoUpCanceled)
		{
			CallActivity.getInstance().getCallFragment()
					.sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallControl.ModifyNoticeType.ModifyRequestCancel);
		}
	}

	private void processCallNtfModifyAlert(Call currentCall)
	{
		CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallControl.ModifyNoticeType.VoiceToVideo);
	}

	private void processMediaDirectionModified(Call currentCall)
	{

	}

	private void processCallNtfRinging(Call currentCall)
	{
		Log.d(TAG, "processCallNtfRinging()  sessionId->" + currentCall.getCallID());
		// 本地嘟嘟音
		MediaUtil.getIns().playCallRspRing();
	}

	/**
	 * 协商结果处理 将BFCP重协商结果上报界面层
	 * 
	 * @param callid
	 *            呼叫唯一标识
	 * @param ConsultRet
	 *            协商结果/重协商结果
	 */
	private void processBFCPConsultRet(String callid, boolean isBfcpEnabled)
	{
		/* 判断是否是当前会话，不是则返回 */
		if (!isCurrentCall(callid))
		{
			return;
		}
		/* 通知刷新会话界面 */
		notifyCallActivityUpdateUI(isBfcpEnabled);
	}

	/**
	 * 通知CallActivity会话挂断
	 */
	private void notifyHomeActivityUpdateUI(String reasonText)
	{
		CallActivity.getInstance().sendHandlerMessage(CallConstant.VOIP_CALL_HANG_UP, reasonText);
	}

	/**
	 * 对端关闭视频通知
	 * 
	 * @param isRemoteVideoClose
	 *            true 关闭
	 */
	private void notifyCallActivityUpdateRemoteVideo(boolean isRemoteVideoClose)
	{
		CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_REMOTE_VIDEO_UPDATE, isRemoteVideoClose);
	}

	/**
	 * 来电通知 4102
	 * 
	 * @param callsession
	 *            会话对象
	 */
	private void processCallNtfComing(Call currentCall)
	{

		comingCallID = currentCall.getCallID();

		int callType = Constants.COMING_CALL;

		if (currentCall.isVideoCall())
		{
			callType = Constants.COMING_VIDEO_CALL;
		}

		// 启动CallComingActivity界面
		Intent intent = new Intent();
		intent.putExtra(CallConstant.VOIP_CALLNUMBER, currentCall.getCallerNumber());
		intent.putExtra(CallConstant.VOIP_CALL_DISPLAY_NAME, currentCall.getCallerDisplayname());
		intent.putExtra(Constants.COMING_VIEW_TYPE, callType);
		intent.putExtra(CallConstant.VOIP_CALLID, currentCall.getCallID());

		intent.setClass(BaseApp.getApp(), CallComingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BaseApp.getApp().startActivity(intent);
	}

	/**
	 * BFCP接收开始
	 * 
	 * @param callid
	 *            呼叫唯一标识
	 */
	public void processBFCPAccptedStart(String callid)
	{
		notifyPDFViewUpdate(BFCPStatus.BFCP_RECEIVE);
	}

	/**
	 * BFCP接收开始
	 * 
	 * @param callid
	 *            呼叫唯一标识
	 */
	public void processBFCPStoped(String callid)
	{
		if (null != callid && !callid.equals(CallService.getInstance().getCurrentCallID()))
		{
			LogUtil.d(TAG, "stop bfcp recevice do non because callid != currentCallId");
			return;
		}

		// notice the GUI side the bfcp is stoped
		LogUtil.i(TAG, " BFCP is stoped,callid=" + callid);
		notifyPDFViewUpdate(BFCPStatus.BFCP_END);
	}

	/**
	 * _______________________________________ 这些是动作
	 * _________________________________________________
	 */

	/**
	 * 通知通话界面刷新
	 */
	public void notifyPDFViewUpdate(String bfcpState)
	{
		if (CallStatus.STATUS_TALKING == CallService.getInstance().getVoipStatus())
		{
			return;
		}

		CallActivity.getInstance().sendHandlerMessage(CallConstant.VOIP_PDF_UPDATE_UI, bfcpState);
	}

	/**
	 * 发起呼叫
	 * 
	 * @param fromPhone
	 *            呼叫号码
	 * @param domain
	 *            域，暂时无用，可传空
	 * @param isVideoCall
	 *            是否视频通话
	 * @param vcaps
	 *            视频通话时，需设置的视频参数，
	 * @param dataCaps
	 *            bfcp参数
	 * @return CallErrorCode 成功："0" 失败：CallErrorCode.isFail(callCodeString)为true
	 */
	public synchronized String dialCall(String fromPhone, String domain, boolean isVideoCall)
	{
		setCallStatus(isVideoCall ? CallStatus.STATUS_VIDEOINIT : CallStatus.STATUS_CALLING);
		String callCodeString = CallService.getInstance().dialCall(fromPhone, domain, isVideoCall);
		if ((StringUtil.isStringEmpty(callCodeString) || CallErrorCode.isFail(callCodeString)))
		{
			setCallStatus(CallStatus.STATUS_CLOSE);
		} else
		{
			this.callID = CallService.getInstance().getCurrentCallID();
		}

		return callCodeString;
	}

	/**
	 * 二次拨号
	 * 
	 * @param code
	 *            号码
	 */
	public boolean sendDTMF(String code)
	{
		return CallService.getInstance().sendDTMF(code);
	}

	/**
	 * 本地麦克风静音
	 * 
	 * @param isRefer
	 *            是否会议中转移 true: 会议中转移， false：非会议中转移，对设备原来的静音状态取反。
	 * @param isMute
	 *            是否静音 true: 静音， false：取消静音
	 */
	public boolean setLocalMute(boolean isRefer, boolean isMute)
	{
		return CallService.getInstance().setLocalMute(isRefer, isMute);
	}

	/**
	 * 扬声器静音
	 */
	public boolean oratorMute(boolean isMute)
	{
		return CallService.getInstance().oratorMute(isMute);
	}

	/**
	 * 接听呼叫，接听一个呼叫，包括音、视频呼叫，返回接听是否成功
	 * 
	 * @param callid
	 *            接听会话的唯一标识callid
	 * @param isNeedAnswerVideo
	 *            是否需要接入视频， 普通通话传入false
	 * @return true表示成功，false表示失败
	 */
	public boolean callAnswer(String callid, boolean isVideo)
	{
		this.callID = callid;

		CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_UPDATE_UI, true);

		// 接听来电后将来电标志位重置
		comingCallID = null;

		boolean ret = CallService.getInstance().callAnswer(callid, isVideo);

		if (isVideo)
		{
			setCallStatus(CallStatus.STATUS_VIDEOACEPT);
		} else
		{
			setCallStatus(CallStatus.STATUS_TALKING);
		}

		return ret;
	}

	/**
	 * 拒绝来电呼叫
	 * 
	 * @param callid
	 *            来电的callid
	 * @return 执行拒绝呼叫结果， true 为成功
	 */
	public boolean rejectCall(String callid)
	{
		comingCallID = null;
		boolean ret = CallService.getInstance().rejectCall(callid);
		return ret;
	}

	/**
	 * 挂断呼叫
	 * 
	 * @return 执行是否完成
	 */
	public synchronized void closeCall()
	{
		Log.d(TAG, "closeCall()");
		// 当前没有会话，不执行此操作
		String currentCallID = CallService.getInstance().getCurrentCallID();
		if (StringUtil.isStringEmpty(currentCallID))
		{
			Log.e(TAG, "currentCallID is null, notify call end.");
			CallActivity.getInstance().getCallFragment().sendHandlerMessage(Constants.MSG_NOTIFY_CALL_END, null);
			Log.i(TAG, "closeCall leave.");
			return;
		}

		if (null != CallActivity.getInstance().getCallFragment())
		{
			CallActivity.getInstance().getCallFragment().onCallClosed();
		} else
		{
			Log.e(TAG, "closeCall CallFragment is null");
		}

		// 重置Demo的CallStatus
		setCallStatus(CallStatus.STATUS_CLOSE);
	}

	/**
	 * 视频通话中关闭视频 对方响应 4113 Param.E_CALL_ID.FAST_CALL_NTF_SESSION_MODIFIED
	 * 
	 * @return 执行结果 true 执行成功 false 执行失败
	 */
	public boolean closeVideo()
	{
		boolean ret = CallService.getInstance().closeVideo();
		// 如果执行失败提示
		if (!ret)
		{
			CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CLOSE_VIDEO_FAIL, null);
		}
		return ret;
	}

	/**
	 * 同意视频升级
	 * 
	 * @param caps
	 *            视频参数
	 * @return 执行结果 true 为执行成功 false 执行失败
	 */
	public boolean agreeUpgradeVideo()
	{
		boolean ret = CallService.getInstance().agreeUpgradeVideo();
		if (ret)
		{
			setCallStatus(CallStatus.STATUS_VIDEOING);
			CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, ModifyNoticeType.defaultType);
		}
		return ret;
	}

	/**
	 * 拒绝视频升级
	 * 
	 * @return true / false 执行结果 true 执行成功 false 执行失败
	 */
	public boolean disAgreeUpgradeVideo()
	{
		return CallService.getInstance().rejectUpgradeVideo();
	}

	/**
	 * 音频通话过程中请求升级到视频通话
	 * 
	 * @param caps
	 *            视频参数
	 * @return 执行结果 true 执行成功 false 执行失败
	 */
	public boolean upgradeVideo()
	{
		boolean ret = CallService.getInstance().upgradeVideo();
		if (ret)
		{
			setCallStatus(CallStatus.STATUS_VIDEOINIT);
			notifyCallViewUpdate(false);
		}

		// 低带宽升级失败
		else if (CallErrorCode.UPDATE_FAIL_LOW_BW.equals(ret))
		{
			setCallStatus(CallStatus.STATUS_TALKING);
			CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_LOW_BW_UPDATE_FAIL, null);
		}

		return ret;
	}

	/**
	 * 获取当前通话媒体信息
	 */
	public StreamInfo getMediaInfo()
	{
		return CallService.getInstance().getMediaInfo();
	}

	/**
	 * 
	 * 关闭本地摄像头
	 * 
	 * @return 执行完成
	 * @param isCloseAction
	 *            true表示关闭本地摄像头操作，false表示打开操作
	 */
	public boolean localCameraControl(boolean isCloseAction)
	{
		return CallService.getInstance().localCameraControl(isCloseAction);
	}

	/**
	 * 获取当前支持的音频路由 第一个为正在使用的音频路由
	 */
	public List<Integer> getAudioRouteList()
	{
		return CallService.getInstance().getAudioRouteList();
	}

	/**
	 * _______________________________________ 这些是动作 end
	 * _________________________________________________
	 */

	/**
	 * 设置挂断reason提示文本
	 * 
	 * @param reason
	 *            挂断reason
	 */
	private String setCloseReasonText(String reason, Call currentCall)
	{
		reasonText = null;
		if (StringUtil.isStringEmpty(reason))
		{
			reasonText = BaseApp.getApp().getString(R.string.callover);
		} else
		{
			reasonText = BaseApp.getApp().getString(R.string.callfailed); // 默认提示呼叫结束
			if ("cancelled".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.cancelled);
			} else if ("not-found".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.inaccessible);
			} else if ("forbidden".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.inaccessible);
				if (checkHasHeader(currentCall))
				{
					reasonText = "VoIP Unavailable";
				}
			}
			// RV_RETURN_VALUE_IGNORED:Method ignores return value

			else if ("busy".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.oppositebusying);
			} else if ("reject".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.bereject);
			} else if ("network-failure".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.callfailed);
			} else if ("no-answer".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.voipstatusnoreply);
			} else if ("temp-unvailable".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.voipstatusnocnt);
			} else if ("media-not-acceptable".equals(reason))
			{
				reasonText = BaseApp.getApp().getString(R.string.errorcall);
			}
		}
		return reasonText;
	}

	/**
	 * voip呼叫区域检查
	 * 
	 * @param callSession
	 *            CallSession对象
	 * @return reason-header字段 有：true 无：false
	 */
	private boolean checkHasHeader(Call currentCall)
	{
		boolean hasHeader = false;
		String reasonHeader = currentCall.getReasonHeader();
		hasHeader = !StringUtil.isStringEmpty(reasonHeader)
				&& (reasonHeader.indexOf("VoIP Unavailable") != -1 || reasonHeader.replaceAll(Constants.CHARACTER_MARK.BLANK_MARK, "").indexOf("cause=1") != -1);
		return hasHeader;
	}

	private boolean isCurrentCall(String callid)
	{
		boolean ret = false;
		String currentCallID = CallService.getInstance().getCurrentCallID();
		if (StringUtil.isNotEmpty(callid) && StringUtil.isNotEmpty(currentCallID) && callid.equals(currentCallID))
		{
			ret = true;
		}
		return ret;
	}

	@Override
	public void onCallComing(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onCallComing(),but the sessionbean is null.");
			return;
		}
		Log.d(TAG, "onCallComing()");
		processCallNtfComing(currentCall);
	}

	@Override
	public void onCallConnect(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onCallConnect(),but the sessionbean is null.");
			return;
		}
		Log.d(TAG, "onCallConnect()");
		processCallNtfTalk(currentCall);
	}

	/**
	 * 对方挂断
	 */
	@Override
	public void onCallend(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onCallend(),but the sessionbean is null.");
			return;
		}
		Log.d(TAG, " - onCallend()");
		processCallNtfEnded(currentCall);
	}

	/**
	 * 收到通话结束释放资源后消息
	 */
	@Override
	public void onCallDestroy(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onCallDestroy(),but the sessionbean is null.");
			return;
		}
		Log.d(TAG, " - onCallDestroy()");
		processCallNtfClosed(currentCall);
	}

	// 协商结果处理
	// BFCP（共享）
	@Override
	public void onDataReady(int callId, int bfcpRet)
	{
		Log.d(TAG, " - onDataReady()");
		boolean isBfcpEnabled = (bfcpRet == 1 ? true : false);
		processBFCPConsultRet(callId + "", isBfcpEnabled);
	}

	@Override
	public void onCallViedoResult(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onCallDestroy(),but the sessionbean is null.");
			return;
		}
		Log.d(TAG, " - onCallViedoResult()");
		processCallNtfModified(currentCall);
	}

	/**
	 * 添加视频请求
	 */
	@Override
	public void onCallAddVideo(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onCallAddVideo(),but the sessionbean is null.");
			return;
		}
		processCallNtfModifyAlert(currentCall);
	}

	/**
	 * 对端降音频结果通知
	 */
	@Override
	public void onCallDelViedo(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onCallDelViedo(),but the sessionbean is null.");
			return;
		}
		processCallNtfModified(currentCall);
	}

	/**
	 * 对端降音频结果通知
	 */
	@Override
	public void onSessionModified(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onSessionModified(),but the sessionbean is null.");
			return;
		}
		processMediaDirectionModified(currentCall);
	}

	@Override
	public void onRingBack(Call currentCall)
	{
		if (null == currentCall.getValue())
		{
			Log.e(TAG, "onRingBack(),but the sessionbean is null.");
			return;
		}
		processCallNtfRinging(currentCall);
	}

	@Override
	public void onDataReceiving(String callId)
	{
		processBFCPAccptedStart(callId);
	}

	@Override
	public void onDataStopped(String callId)
	{
		processBFCPStoped(callId);
	}

	public void clear()
	{
		CallService.getInstance().unregisterNotification(this);
		instance = null;
	}

}
