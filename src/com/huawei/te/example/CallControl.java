package com.huawei.te.example;

import object.StreamInfo;
import android.content.Intent;
import android.util.Log;

import com.huawei.application.BaseApp;
import com.huawei.common.CallErrorCode;
import com.huawei.esdk.te.TESDK;
import com.huawei.esdk.te.call.Call;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.call.CallNotification;
import com.huawei.esdk.te.call.CallService;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.CallConstant;
import com.huawei.esdk.te.data.Constants.MSG_FOR_HOMEACTIVITY;
import com.huawei.esdk.te.data.Constants.MsgCallFragment;
import com.huawei.esdk.te.util.MediaUtil;
import com.huawei.te.example.activity.CallActivity;
import com.huawei.te.example.activity.CallComingActivity;
import com.huawei.te.example.activity.CallFragment;
import com.huawei.utils.StringUtil;
import com.huawei.voip.data.CameraViewRefresh;
import com.huawei.voip.data.EventData;
import com.huawei.voip.data.VideoCaps;

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
	 * 返回的失败原因
	 */
	private String reasonText = null;

	public static CallControl getInstance()
	{
		if (null == instance)
		{
			Log.d(TAG, "CallControl construct");
			return new CallControl();
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
		CallService callPresenter = CallService.getInstance();
		if (null != callPresenter)
		{
			instance = this;
			callPresenter.registerNotification(this);
		} else
		{
			Log.e(TAG, "callPresenter is null !");
		}
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
	 * 清除视频数据
	 */
	private void clearVideoSurface()
	{
		Log.d(TAG, "clearVideoSurface()");
		int voipStatus = CallLogic.getIns().getVoipStatus();
		boolean voipStatusIsTrue = (voipStatus == CallLogic.STATUS_VIDEOING || voipStatus == CallLogic.STATUS_VIDEOACEPT || voipStatus == CallLogic.STATUS_VIDEOINIT);
		if (voipStatusIsTrue)
		{
			// 释放视频数据
			Log.d(TAG, "释放视频数据");
			CallActivity.getInstance().sendHandlerMessage(Constants.MSG_UNINIT_VIDEO, null);
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

		// bye原因
		String reason = currentCall.getReleaseReason();

		Log.d(TAG, "callid->" + callid);
		Log.d(TAG, "isCurrentCall->" + isCurrentCall(callid));
		if (isCurrentSDKCall(callid))
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

			clearVideoSurface();

			CallActivity.getInstance().getCallFragment().sendHandlerMessage(Constants.MSG_NOTIFY_CALL_END, null);
			CallService.getInstance().setCurSDKCallID(null);

		} else
		{
			Log.d(TAG, "voip status:" + CallLogic.getIns().getVoipStatus());
			// 对 当对方呼叫进来，本端还没有接听的时候对端挂断的情况
			// 进行处理，此时还没有接听呼叫，所以isCurrentCall(callid)返回false;
			// 如果有插入记录说明已经接听的会话(解决多路会话导致更新，插入记录无法判断)
			if (null != comingCallID && callid.equals(comingCallID))
			{
				CallActivity.getInstance().sendHandlerMessage(MSG_FOR_HOMEACTIVITY.MSG_NOTIFY_CALLCLOSE, callid);
				comingCallID = null;
				Log.d(TAG, "voip status:" + CallLogic.getIns().getVoipStatus());
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

		String currentCallID = CallService.getInstance().getCurSDKCallID();
		boolean notSameidAndlogicIsClose = (!callid.equals(currentCallID) || CallLogic.STATUS_CLOSE == CallLogic.getIns().getVoipStatus());
		if (notSameidAndlogicIsClose)
		{
			Log.e(TAG, "is not currentCallID or the CallLogic state is not STATUS_CLOSE, so CallNtfTalk return.");
			return;
		}

		if (!currentCall.isVideoCall())
		{
			// 本方视频呼叫，对方音频接听时，释放视频数据
			boolean isNeedClearVideo = CallService.getInstance().isNeedClearVideo();
			if (isNeedClearVideo)
			{
				CallService.getInstance().setNeedClearVideo(false);
				CallActivity.getInstance().sendHandlerMessage(Constants.MSG_UNINIT_VIDEO, null);
			}
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
		// 不是当前呼叫，return;
		if (null == currentCall)
		{
			Log.d(TAG, "session is null!");
			return;
		} else
		{
			if (!isCurrentCall(currentCall.getCallID()))
			{
				Log.d(TAG, "[session=" + currentCall + "] [callID=" + currentCall.getCallID() + ']');
				return;
			}
		}
		int voipStatus = CallLogic.getIns().getVoipStatus();
		Log.d(TAG, "voipStatus = " + voipStatus);

		String oper = currentCall.getOperation();
		int videoModifyState = currentCall.getVideoModifyState();
		Log.d(TAG, "videoModifyState = " + videoModifyState);

		// 关闭视频成功 || 对端请求关闭视频
		boolean isVideoClose = (0 == videoModifyState && CallLogic.STATUS_VIDEOING == voipStatus);
		// boolean isActiveUpdateVideo = ((VIDEOADD.equals(oper) && 1 ==
		// videoModifyState) || VIDEOMOD.equals(oper));
		// 主动升级视频成功
		boolean isActiveUpdateVideo = (1 == videoModifyState && CallLogic.STATUS_VIDEOINIT == voipStatus);
		// 主动升级视频失败
		boolean isVideoUpFailed = (0 == videoModifyState && CallLogic.STATUS_VIDEOINIT == voipStatus);

		// 升级取消
		boolean isVideoUpCanceled = (0 == videoModifyState && CallLogic.STATUS_TALKING == voipStatus);

		if (isVideoClose)
		{
			CallLogic.getIns().setVideoCall(false);
			// begin modified by pwx178217 reason：发辅流过程中对端切音频，本端切视频，远端图像还是辅流画面
			clearVideoSurface();
			CallLogic.getIns().setVoipStatus(CallLogic.STATUS_TALKING);
			// end modified by pwx178217 reason：发辅流过程中对端切音频，本端切视频，远端图像还是辅流画面
			// begin modified by cwx176934 2014/01/08 Reason:DTS2014010303079
			// 与VCT对接辅流时音视频变换，界面异常
			// CallLogic.getIns().setBfcpStatus(CallLogic.BFCP_END);
			// end modified by cwx176934 2014/01/08 Reason:DTS2014010303079
			// 与VCT对接辅流时音视频变换，界面异常
			// begin modify by l00211010 视频到音频通知界面刷新
			// CallActionNotifyActivty.getIns().notifyCallModify(CallLogic.ModifyNoticeType.VideoToVoice);
			// TO invoke
			CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallLogic.ModifyNoticeType.VideoToVoice);
			// end modify by l00211010 视频到音频通知界面刷新
			// 转为音频的时候重置扬声器和听筒
			// resetAudioRoute(false);
		}
		// 主动升级
		// begin modified by cwx176934 2013/12/02 Reason:ANDROID-205
		// TE30对接，会话重协商。
		else if (isActiveUpdateVideo)
		{
			if (currentCall.getRemoteVideoState() != 0)
			{
				// notifyCallActivityUpdateRemoteVideo(currentCall.getRemoteVideoState()
				// == 1);
				// TO invoke
				boolean isRemoteVideoClose = currentCall.getRemoteVideoState() == 1;
				CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_REMOTE_VIDEO_UPDATE, isRemoteVideoClose);
			}
			Log.d(TAG, "Upgrade To Video Call");
			CallLogic.getIns().setVoipStatus(CallLogic.STATUS_VIDEOING);
			// resetAudioRoute(true);
			CallLogic.getIns().setEnableBfcp(currentCall.isBFCPSuccess());
			// begin modified by cwx176934 2014/01/08 Reason:DTS2014010303079
			// 与VCT对接辅流时音视频变换，界面异常
			boolean statusOfBFCP = (CallLogic.BFCP_RECEIVE.equals(CallLogic.getIns().getBfcpStatus()) || CallLogic.BFCP_START.equals(CallLogic.getIns()
					.getBfcpStatus()));
			if (statusOfBFCP)
			{
				Log.i(TAG, "not refresh ui the bfcpStatus is " + CallLogic.getIns().getBfcpStatus());
				return;
			}
			// end modified by cwx176934 2014/01/08 Reason:DTS2014010303079
			// 与VCT对接辅流时音视频变换，界面异常
			notifyCallActivityUpdateUI();
		} else if (isVideoUpFailed)
		{
			CallLogic.getIns().setVoipStatus(CallLogic.STATUS_TALKING);
			notifyCallViewUpdate(false);
			CallActivity.getInstance().getCallFragment()
					.sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallLogic.ModifyNoticeType.ModifyRequestFalied);
		} else if (VIDEOMOD.equals(oper))
		{
			if (currentCall.getRemoteVideoState() != 0)
			{
				notifyCallActivityUpdateRemoteVideo(currentCall.getRemoteVideoState() == 1);
			}
			CallLogic.getIns().setEnableBfcp(currentCall.isBFCPSuccess());
			// begin modified by cwx176934 2014/01/08 Reason:DTS2014010303079
			// 与VCT对接辅流时音视频变换，界面异常
			boolean statusOfBFCP = (CallLogic.BFCP_RECEIVE.equals(CallLogic.getIns().getBfcpStatus()) || CallLogic.BFCP_START.equals(CallLogic.getIns()
					.getBfcpStatus()));
			if (statusOfBFCP)
			{
				Log.i(TAG, "not refresh ui the bfcpStatus is " + CallLogic.getIns().getBfcpStatus());
				return;
			}
			// end modified by cwx176934 2014/01/08 Reason:DTS2014010303079
			// 与VCT对接辅流时音视频变换，界面异常
			notifyCallActivityUpdateUI();
		}
		// 通知界面取消升级视频
		else if (isVideoUpCanceled)
		{
			CallActivity.getInstance().getCallFragment()
					.sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallLogic.ModifyNoticeType.ModifyRequestCancel);
		}
	}

	private void processCallNtfModifyAlert(Call currentCall)
	{
		CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallLogic.ModifyNoticeType.VoiceToVideo);
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
	 * 刷新view
	 */
	private void refrershView(EventData data)
	{

		Log.i(TAG, "refresh view()");
		boolean cameraDataStatus = (null != data && data instanceof CameraViewRefresh);
		if (cameraDataStatus)
		{
			// 目前只有本地采集点， 后续有做render事件时 再添加
			CameraViewRefresh viewData = (CameraViewRefresh) data;

			if (viewData.getMediaType() == CameraViewRefresh.MEDIA_TYPE_VIDEO || viewData.getMediaType() == CameraViewRefresh.MEDIA_TYPE_PREVIEW)
			{
				if (viewData.getViewType() == CameraViewRefresh.VIEW_TYPE_LOCAL_ADD)
				{
					CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_REFRESH_VIEW, true);
				} else if (viewData.getViewType() == CameraViewRefresh.VIEW_TYPE_LOCAL_REMOVE)
				{
					CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_REFRESH_VIEW, false);
				}
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					Log.e(TAG, "Progress get an Exception.");
				}
			}
		}
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
	 * _______________________________________ 这些是动作
	 * _________________________________________________
	 */

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
	 * @return int -1 为失败 0为 成功
	 */
	public synchronized String dialCall(String fromPhone, String domain, boolean isVideoCall)
	{
		return CallService.getInstance().dialCall(fromPhone, domain, isVideoCall);
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

		CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_UPDATE_UI, true);

		// 接听来电后将来电标志位重置
		comingCallID = null;

		boolean ret = CallService.getInstance().callAnswer(callid, isVideo, TESDK.getInstance().getApplication());

		if (!ret)
		{
			// 主要将id置空不然这个时候去注销会出现空指针异常
			CallService.getInstance().setCurSDKCallID(null);
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

		int voipStatus = CallLogic.getIns().getVoipStatus();
		Log.d(TAG, "voipStatus->" + voipStatus);
		boolean voipStatusIsTrue = (voipStatus == CallLogic.STATUS_VIDEOING || voipStatus == CallLogic.STATUS_VIDEOACEPT || voipStatus == CallLogic.STATUS_VIDEOINIT);
		Log.d(TAG, "voipStatusIsTrue->" + voipStatusIsTrue);
		if (voipStatusIsTrue)
		{
			// 释放视频数据
			clearVideoSurface();
		}

		boolean ret = CallService.getInstance().closeCall();
		Log.d(TAG, "close call,SDK 层是否执行完成->" + ret);

		if (null != CallActivity.getInstance().getCallFragment())
		{
			CallActivity.getInstance().getCallFragment().onCallClosed();
		} else
		{
			Log.e(TAG, "closeCall CallFragment is null");
		}
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
			CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_MODIFY_UI, CallLogic.ModifyNoticeType.defaultType);
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
		return CallService.getInstance().disAgreeUpgradeVideo();
	}

	/**
	 * 通话过程中请求升级到视频通话
	 * 
	 * @param caps
	 *            视频参数
	 * @return 执行结果 true 执行成功 false 执行失败
	 */
	public boolean upgradeVideo(VideoCaps caps, VideoCaps dataCaps)
	{
		boolean ret = CallService.getInstance().upgradeVideo(caps, dataCaps);
		if (ret)
		{
			notifyCallViewUpdate(false);
		}

		// 低带宽升级失败
		else if (CallErrorCode.UPDATE_FAIL_LOW_BW.equals(ret))
		{
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

	private boolean isCurrentSDKCall(String callid)
	{
		Log.i(TAG, "isCurrentCall exec ");
		boolean ret = false;
		String currentCallID = CallService.getInstance().getCurSDKCallID();
		if (StringUtil.isNotEmpty(callid) && StringUtil.isNotEmpty(currentCallID) && callid.equals(currentCallID))
		{
			ret = true;
		}
		Log.d(TAG, "isCurrentCall return ret->" + ret);
		return ret;
	}

	private boolean isCurrentCall(String callid)
	{
		Log.i(TAG, "isCurrentCall exec ");
		boolean ret = CallService.getInstance().isCurrentCall(callid);
		Log.d(TAG, "isCurrentCall return ret->" + ret);
		return ret;
	}

	@Override
	public void onCallComing(Call currentCall)
	{
		Log.d(TAG, "onCallComing()");
		processCallNtfComing(currentCall);
	}

	@Override
	public void onCallConnect(Call currentCall)
	{
		Log.d(TAG, "onCallConnect()");
		processCallNtfTalk(currentCall);
	}

	/**
	 * view刷新通知
	 */
	@Override
	public void onCallRefreshView(CameraViewRefresh data)
	{
		Log.d(TAG, "onCallRefreshView()");
		// 刷新本地render显示，暂时用不上
		refrershView(data);
	}

	/**
	 * 对方挂断
	 */
	@Override
	public void onCallend(Call currentCall)
	{
		Log.d(TAG, " - onCallend()");
		processCallNtfEnded(currentCall);
	}

	/**
	 * 收到通话结束释放资源后消息
	 */
	@Override
	public void onCallDestroy(Call currentCall)
	{
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
		Log.d(TAG, " - onCallViedoResult()");
		processCallNtfModified(currentCall);
	}

	/**
	 * 添加视频请求
	 */
	@Override
	public void onCallAddVideo(Call currentCall)
	{
		processCallNtfModifyAlert(currentCall);
	}

	/**
	 * 对端降音频结果通知
	 */
	@Override
	public void onCallDelViedo(Call currentCall)
	{
		processCallNtfModified(currentCall);
	}

	@Override
	public void onRingBack(Call currentCall)
	{
		processCallNtfRinging(currentCall);
	}

	/**
	 * render控制
	 * 
	 * @param witch
	 *            本远端
	 * @param isOpen
	 *            true开启
	 */
	public boolean controlRenderVideo(int renderModule, boolean isStart)
	{
		return CallService.getInstance().controlRenderVideo(renderModule, isStart);
	}

	public void clear()
	{
		CallService.getInstance().unRegisterNofitication(this);
		instance = null;
	}

}
