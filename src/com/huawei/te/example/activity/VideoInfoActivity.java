package com.huawei.te.example.activity;

import java.util.Timer;
import java.util.TimerTask;

import object.AudioStreamInfo;
import object.StreamInfo;
import object.VideoStreamInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.huawei.esdk.te.call.CallConstants.BFCPStatus;
import com.huawei.esdk.te.call.CallConstants.CallStatus;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.call.CallService;
import com.huawei.esdk.te.util.LayoutUtil;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;
import com.huawei.te.example.call.IMediaNetInfoListener;
import com.huawei.utils.StringUtil;
import com.huawei.voip.data.MediaNetInfo;
import common.TransportType;

public class VideoInfoActivity extends BaseActivity implements IMediaNetInfoListener
{
	private static final String TAG = VideoInfoActivity.class.getSimpleName();
	
	private static VideoInfoActivity instance = null;
	
	public static VideoInfoActivity getInstance()
	{
		return instance;
	}

	/**
	 * 定时
	 */
	private Timer timer;

	/**
	 * 根布局
	 */
	private View rootView;

	/**
	 * 详细信息界面
	 */
	private View detailLayout;

	/**
	 * 音频发送编码
	 */
	private TextView audioInfoCodeOut;

	/**
	 * 音频接收编码
	 */
	private TextView audioInfoCodeIn;

	/**
	 * 视频发送编码
	 */
	private TextView videoInfoCodeOut;

	/**
	 * 视频接收编码
	 */
	private TextView videoInfoCodeIn;

	/**
	 * 视频发送帧率
	 */
	private TextView videoInfoFrameRateOut;

	/**
	 * 视频接收帧率
	 */
	private TextView videoInfoFrameRateIn;

	/**
	 * 视频发送分辨率
	 */
	private TextView videoInfoFramsizeOut;

	/**
	 * 视频接收分辨率
	 */

	private TextView videoInfoFramsizeIn;
	/**
	 * 视频发送码率
	 */
	private TextView videoInfoDatarateOut;

	/**
	 * 视频接收码率
	 */
	private TextView videoInfoDatarateIn;

	/**
	 * bfcp辅流发送编码
	 */
	private TextView bfcpInfoCodeOut;

	/**
	 * bfcp辅流接收编码
	 */
	private TextView bfcpInfoCodeIn;

	/**
	 * bfcp辅流发送帧率
	 */
	private TextView bfcpInfoFrameRateOut;

	/**
	 * bfcp辅流接收帧率
	 */
	private TextView bfcpInfoFrameRateIn;

	/**
	 * bfcp辅流发送分辨率
	 */
	private TextView bfcpInfoFramsizeOut;

	/**
	 * bfcp辅流接收分辨率
	 */
	private TextView bfcpInfoFramsizeIn;

	/**
	 * bfcp辅流发送码率
	 */
	private TextView bfcpInfoDatarateOut;

	/**
	 * bfcp辅流接收码率
	 */
	private TextView bfcpInfoDatarateIn;

	/**
	 * 定时中操作
	 */
	private TimerTask task;

	/**
	 * 视频信息
	 */
	private StreamInfo mediaInfo;

	/**
	 * Cvoip对象
	 */
	// private CVoip cvoip;
	private CallControl callControl;

	/**
	 * 当前对象
	 */
	private VideoInfoActivity videoInfoActivity;

	/**
	 * 音频协议显示
	 */
	private static final String PCMU = "PCMU";
	private static final String PCMA = "PCMA";
	private static final String G711U = "G.711U";
	private static final String G711A = "G.711A";

	/**
	 * 音频编码信息
	 */
	private String audioCodeString = "";

	/**
	 * 视频发送编码信息
	 */
	private String videoSendCodeString = "";

	/**
	 * 视频发送分辨率
	 */
	private String videoSendFramsize = "";

	/**
	 * 视频发送帧率
	 */
	private String videoSendFrameRate = "";

	/**
	 * 视频发送码率
	 */
	private StringBuffer videoSendDatarate = new StringBuffer(0);

	/**
	 * 视频接收编码
	 */
	private String videoRecvCodeString = "";

	/**
	 * 视频接收帧率
	 */
	private String videoRecvFrameRate = "";

	/**
	 * 视频接收分辨率
	 */
	private String videoRecvFramsize = "";

	/**
	 * 视频接收码率
	 */
	private StringBuffer videoRecvDatarate = new StringBuffer(0);

	/**
	 * 辅流发送编码
	 */
	private String dataSendCodeString = "";

	/**
	 * 辅流发送帧率
	 */
	private String dataSendFrameRate = "";

	/**
	 * 辅流发送分辨率
	 */
	private String dataSendFramsize = "";

	/**
	 * 辅流发送频率
	 */
	private StringBuffer dataSendDatarate = new StringBuffer(0);

	/**
	 * 辅流接收编码
	 */
	private String dataRecvCodeString = "";

	/**
	 * 辅流接收帧率
	 */
	private String dataRecvFrameRate = "";

	/**
	 * 辅流接收分辨率
	 */
	private String dataRecvFramsize = "";

	/**
	 * 辅流接收码率
	 */
	private StringBuffer dataRecvDatarate = new StringBuffer(0);

	/**
	 * 接收音频加密TextView
	 */
	private TextView audioEncriptionInText;

	/**
	 * 发送音频加密TextView
	 */
	private TextView audioEncriptionOutText;

	/**
	 * 视频接收加密TextView
	 */
	private TextView videoEncriptionInText;

	/**
	 * 视频发送加密TextView
	 */
	private TextView videoEncriptionOutText;

	/**
	 * 辅流接收加密TextView
	 */
	private TextView dataEncriptionInText;

	/**
	 * 辅流发送加密TextView
	 */
	private TextView dataEncriptionOutText;

	/**
	 * 音频丢包率
	 */
	private String audioPacketLossProbability;

	/**
	 * 音频延时
	 */
	private String audioDelay;

	/**
	 * 音频抖动
	 */
	private String audioJitter;

	/**
	 * 音频接收加密
	 */
	private String audioEncriptionIn;

	/**
	 * 音频发送界面
	 */
	private String audioEncriptionOut;

	/**
	 * 视频丢包率
	 */
	private String videoPacketLossProbability;

	/**
	 * 视频延时
	 */
	private String videoDelay;

	/**
	 * 视频抖动
	 */
	private String videoJitter;

	/**
	 * 视频接收加密
	 */
	private String videoEncriptionIn;

	/**
	 * 视频发送加密
	 */
	private String videoEncriptionOut;

	/**
	 * 辅流丢包率
	 */
	private String dataPacketLossProbability;

	/**
	 * 辅流延时
	 */
	private String dataDelay;

	/**
	 * 辅流抖动
	 */
	private String dataJitter;

	/**
	 * 辅流接收加密
	 */
	private String dataEncriptionIn;

	/**
	 * 辅流发送加密
	 */
	private String dataEncriptionOut;

	private TextView audioPacketLossInText;

	// begin modified by pwx178217 reason:辅流添加丢包延时抖动显示
	/**
	 * 辅流接收丢包率
	 */
	private TextView dataPacketLossInText;

	/**
	 * 辅流接收延时
	 */
	private TextView dataDelayInText;

	/**
	 * 辅流接收抖动
	 */
	private TextView dataJitterInText;
	// end modified by pwx178217 reason:辅流添加丢包延时抖动显示
	private TextView videoJitterInText;

	private TextView videoPacketLossInText;

	private TextView audioJitterInText;

	private TextView audioDelayInText;

	private TextView videoDelayInText;

	private enum TextViewENum
	{
		delay, audioJitter, videoJitter, audioPacketLoss, videoPacketLoss;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 手机视频呼叫通话时加载的布局 与横屏布局时加载的通话信息显示不同
//		if (!ConfigApp.getInstance().isUsePadLayout()
//				&& (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE))
//		{
//			setContentView(R.layout.phone_video_info_layout);
//		} else
//		{
			setContentView(R.layout.video_info_layout);
//		}
		videoInfoActivity = this;
		initComponent();

		instance = this;
	}
	

	private void initComponent()
	{
		// 根布局
		rootView = findViewById(R.id.video_info_layout);
		ViewGroup.LayoutParams lp = rootView.getLayoutParams();
//		if (ConfigApp.getInstance().isUsePadLayout())
//		{
			lp.width = LayoutUtil.getInstance().getScreenWidth() / 2;
			rootView.setLayoutParams(lp);
//		}
		detailLayout = findViewById(R.id.video_info_detail_layout);
		// 关闭按钮设置点击事件
		View closeBtn = findViewById(R.id.info_close);
		if (null != closeBtn)
		{
			closeBtn.setOnClickListener(new OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					finish();
				}
			});
		}
		DetailOnclickListener detalListener = new DetailOnclickListener();
		detailLayout.setOnClickListener(detalListener);
		// 音频发送编码
		audioInfoCodeOut = (TextView) findViewById(R.id.audio_info_code_out);

		// 音频接收编码
		audioInfoCodeIn = (TextView) findViewById(R.id.audio_info_code_in);

		// 音频丢包率
		audioPacketLossInText = (TextView) findViewById(R.id.packet_loss_probability_in);

		// 音频延时
		audioDelayInText = (TextView) findViewById(R.id.delay_in);

		// 音频抖动
		audioJitterInText = (TextView) findViewById(R.id.jitter_in);

		// 音频加密 发送 接受
		audioEncriptionInText = (TextView) findViewById(R.id.srtp_encription_in);
		audioEncriptionOutText = (TextView) findViewById(R.id.srtp_encription_out);

		// 视频丢包率
		videoPacketLossInText = (TextView) findViewById(R.id.video_packet_loss_probability_in);

		// 视频延时
		videoDelayInText = (TextView) findViewById(R.id.video_delay_in);

		// 视频抖动
		videoJitterInText = (TextView) findViewById(R.id.video_jitter_in);

		// 视频加密 发送 接受
		videoEncriptionInText = (TextView) findViewById(R.id.video_srtp_encription_in);
		videoEncriptionOutText = (TextView) findViewById(R.id.video_srtp_encription_out);
		// begin modified by pwx178217 reason:辅流添加丢包延时抖动显示
		// 辅流丢包率
		dataPacketLossInText = (TextView) findViewById(R.id.bfcp_packet_loss_probability_in);

		// 辅流延时
		dataDelayInText = (TextView) findViewById(R.id.bfcp_delay_in);

		// 辅流抖动
		dataJitterInText = (TextView) findViewById(R.id.bfcp_jitter_in);
		// end modified by pwx178217 reason:辅流添加丢包延时抖动显示
		// 辅流加密 发送接收
		dataEncriptionInText = (TextView) findViewById(R.id.bfcp_srtp_encription_in);
		dataEncriptionOutText = (TextView) findViewById(R.id.bfcp_srtp_encription_out);

		// 视频发送编码
		videoInfoCodeOut = (TextView) findViewById(R.id.video_info_code_out);

		// 视频接收编码
		videoInfoCodeIn = (TextView) findViewById(R.id.video_info_code_in);

		// 视频发送帧率
		videoInfoFrameRateOut = (TextView) findViewById(R.id.video_info_resolution_out);

		// 视频接收帧率
		videoInfoFrameRateIn = (TextView) findViewById(R.id.video_info_resolution_in);

		// 视频发送分辨率
		videoInfoFramsizeOut = (TextView) findViewById(R.id.video_info_width_out);

		// 视频接收分辨率
		videoInfoFramsizeIn = (TextView) findViewById(R.id.video_info_width_in);

		// 视频发送码率
		videoInfoDatarateOut = (TextView) findViewById(R.id.video_info_frequency_out);

		// 视频接收码率
		videoInfoDatarateIn = (TextView) findViewById(R.id.video_info_frequency_in);

		// bfcp辅流发送编码
		bfcpInfoCodeOut = (TextView) findViewById(R.id.bfcp_info_code_out);

		// bfcp辅流接收编码
		bfcpInfoCodeIn = (TextView) findViewById(R.id.bfcp_info_code_in);

		// bfcp辅流发送帧率
		bfcpInfoFrameRateOut = (TextView) findViewById(R.id.bfcp_info_resolution_out);

		// bfcp辅流接收帧率
		bfcpInfoFrameRateIn = (TextView) findViewById(R.id.bfcp_info_resolution_in);

		// bfcp辅流发送分辨率
		bfcpInfoFramsizeOut = (TextView) findViewById(R.id.bfcp_info_width_out);

		// bfcp辅流接收分辨率
		bfcpInfoFramsizeIn = (TextView) findViewById(R.id.bfcp_info_width_in);

		// bfcp辅流发送码率
		bfcpInfoDatarateOut = (TextView) findViewById(R.id.bfcp_info_frequency_out);

		// bfcp辅流接收码率
		bfcpInfoDatarateIn = (TextView) findViewById(R.id.bfcp_info_frequency_in);
//		cvoip = CommonManager.getInstance().getVoip();
		callControl = CallControl.getInstance();

		// encryptState = cvoip.getMediaSEncryptState();
		// if ((encryptState & MediaNetInfo.MediaEncrytState.ENCRYPT_AUDIO) !=
		// 0)
		// {
		// audioEncriptionIn = getString(R.string.encription);
		// audioEncriptionOut = getString(R.string.encription);
		// }
		// else
		// {
		// audioEncriptionIn = getString(R.string.unencription);
		// audioEncriptionOut = getString(R.string.unencription);
		// }
		//
		//
		// if ((encryptState & MediaNetInfo.MediaEncrytState.ENCRYPT_DATA) != 0)
		// {
		// dataEncriptionIn = getString(R.string.encription);
		// dataEncriptionOut = getString(R.string.encription);
		// }
		// else
		// {
		// dataEncriptionIn = getString(R.string.unencription);
		// dataEncriptionOut = getString(R.string.unencription);
		// }
		//
		// if ((encryptState & MediaNetInfo.MediaEncrytState.ENCRYPT_VIDEO) !=
		// 0)
		// {
		// videoEncriptionIn = getString(R.string.encription);
		// videoEncriptionOut = getString(R.string.encription);
		// }
		// else
		// {
		// videoEncriptionIn = getString(R.string.unencription);
		// videoEncriptionOut = getString(R.string.unencription);
		// }
	}

	/**
	 * 设置textview的数字颜色显示
	 */
	private void setTextViewColor(TextView textView, String str, TextViewENum textEnum)
	{
		if ((textView == null) || (str == null))
		{
			return;
		}

		// textview显示值处于中间橙色状态的，上限值和下限值
		int minValue = 0;
		int maxValue = 0;
		switch (textEnum) {
		case delay:
			minValue = 199;
			maxValue = 999;
			break;

		case audioJitter:
			minValue = 40;
			maxValue = 70;
			break;

		case videoJitter:
			minValue = 149;
			maxValue = 199;
			break;

		case audioPacketLoss:
			minValue = 14;
			maxValue = 30;
			break;

		case videoPacketLoss:
			minValue = 3;
			maxValue = 9;
			break;

		default:
			return;
		}
		setTextViewColor(textView, str, minValue, maxValue);
	}

	private void setTextViewColor(TextView textView, String str, int minValue, int maxValue)
	{
		int id = 0;
		// str里面有不是数字的部分
		if ("".equals(str.trim()) || (!str.matches("^\\d+(\\.\\d+)?$")))
		{
//			if (ConfigApp.getInstance().isUsePadLayout())
//			{
				id = R.color.prompt_gray;
//			} else
//			{
//				id = R.color.white;
//			}
		} else
		{
			float value = Float.parseFloat(str);

			// 显示白色
			if ((value >= 0) && (value <= minValue))
			{
//				if (ConfigApp.getInstance().isUsePadLayout())
//				{
					id = R.color.prompt_gray;
//				} else
//				{
//					id = R.color.white;
//				}
			} else if ((value > minValue) && (value <= maxValue)) // 显示橙色
			{
				id = R.color.video_info_orange;
			} else if (value > maxValue)// 显示红色
			{
				id = R.color.video_info_red;
			} else
			{
				return;
			}
		}

		textView.setTextColor(getResources().getColor(id));
	}

	/**
	 * 设置信息数据
	 * 
	 * @since 1.1
	 * @history 2013-11-5 v1.0.0 pWX178217 create
	 */
	private void initData()
	{
		mediaInfo = callControl.getMediaInfo();
		if (null == mediaInfo)
		{
			return;
		}
		// LogUI.i(mediaInfo);
		// audioInfo = StringUtil.findElemString(mediaInfo, "audio", "--");
		// //编码
		// audioCodeString = StringUtil.findElemString(audioInfo, "codec",
		// "--");
		//
		// String audioNetInfo = StringUtil.findElemString(mediaInfo,
		// "audio_net", "");
		// audioPacketLossProbability = StringUtil.findElemString(audioNetInfo,
		// "lost", "--");
		// audioDelay = StringUtil.findElemString(audioNetInfo, "delay", "--");
		// audioJitter = StringUtil.findElemString(audioNetInfo, "jitter",
		// "--");

		// audio
		AudioStreamInfo audioInfo = mediaInfo.getAudioStreamInfo();
		audioCodeString = audioInfo.getDecodeProtocol();
		// begin 上层处理 PCMU 和PCMA 编码
		if (PCMU.equalsIgnoreCase(audioCodeString))
		{
			audioCodeString = G711U;
		}
		if (PCMA.equalsIgnoreCase(audioCodeString))
		{
			audioCodeString = G711A;
		}
		// end 上层处理 PCMU 和PCMA 编码
		audioPacketLossProbability = audioInfo.getUlRecvTotalLostPacket() + "";
		audioDelay = audioInfo.getfRecvDelay() + "";
		audioJitter = audioInfo.getfRecvJitter() + "";

		// videoSend = StringUtil.findElemString(mediaInfo, "video_send", "--");
		//
		//
		// //begin modified by c00292094 reason:bphp编码显示
		// //编码
		// videoSendCodeString = StringUtil.findElemString(videoSend, "codec",
		// "--")
		// + ("".equals(StringUtil.findElemString(videoSend, "profile", ""))
		// ? "" : ' ') + StringUtil.findElemString(videoSend, "profile", "");
		// //end modified by c00292094 reason:bphp编码显示
		// //帧率
		// videoSendFrameRate = StringUtil.findElemString(videoSend,
		// "framerate", "--");
		// //分辨率
		// videoSendFramsize = StringUtil.findElemString(videoSend, "framesize",
		// "--");
		// //码率
		// videoSendDatarate = StringUtil.findElemString(videoSend, "datarate",
		// "--");

		VideoStreamInfo videoInfo = mediaInfo.getVideoStreamInfo();
		// 编码 bp hp
		videoSendCodeString = videoInfo.getEncodeName() + ' ' + videoInfo.getEncoderProfile();
		// 帧率
		videoSendFrameRate = videoInfo.getSendFrameRate() + "";
		// 分辨率
		videoSendFramsize = videoInfo.getEncoderSize();
		// 码率
		// begin modified by c00349133 reason：为视频发送码率添加单位
		// videoSendDatarate = videoInfo.getVideoSendBitRate()/1000+"";
		videoSendDatarate.setLength(0);
		videoSendDatarate.append(videoInfo.getVideoSendBitRate() / 1000);
		videoSendDatarate.append('k');
		// end modified by c00349133 reason：为视频发送码率添加单位
		//
		//
		// videoRecv = StringUtil.findElemString(mediaInfo, "video_recv", "--");
		//
		// //begin modified by c00292094 reason:bphp编码显示
		// //编码
		// videoRecvCodeString = StringUtil.findElemString(videoRecv, "codec",
		// "--")
		// +' '+StringUtil.findElemString(videoRecv, "profile", "");
		// //end modified by c00292094 reason:bphp编码显示
		// //帧率
		// videoRecvFrameRate = StringUtil.findElemString(videoRecv,
		// "framerate", "--");
		// //分辨率
		// videoRecvFramsize = StringUtil.findElemString(videoRecv, "framesize",
		// "--");
		// //码率
		// videoRecvDatarate = StringUtil.findElemString(videoRecv, "datarate",
		// "--");

		// 解码
		videoRecvCodeString = videoInfo.getDecodeName() + ' ' + videoInfo.getDecoderProfile();
		// 帧率
		videoRecvFrameRate = videoInfo.getRecvFrameRate() + "";
		// 分辨率
		videoRecvFramsize = videoInfo.getDecoderSize();
		// 码率
		// begin modified by c00349133 reason：为视频接收码率添加单位
		// videoRecvDatarate = videoInfo.getVideoRecvBitRate()/1000+"";
		videoRecvDatarate.setLength(0);
		videoRecvDatarate.append(videoInfo.getVideoRecvBitRate() / 1000);
		videoRecvDatarate.append('k');
		// end modified by c00349133 reason：为视频接收码率添加单位
		//
		// String videoNetInfo = StringUtil.findElemString(mediaInfo,
		// "video_net", "");
		// videoPacketLossProbability = StringUtil.findElemString(videoNetInfo,
		// "lost", "--");
		// videoDelay = StringUtil.findElemString(videoNetInfo, "delay", "--");
		// videoJitter = StringUtil.findElemString(videoNetInfo, "jitter",
		// "--");
		videoPacketLossProbability = Float.valueOf(videoInfo.getVideoRecvLossFraction()).intValue() + "";// TODO
																											// 用发送还是接收丢包率
		videoDelay = Float.valueOf(videoInfo.getVideoRecvDelay()).intValue() + "";
		videoJitter = Float.valueOf(videoInfo.getVideoRecvJitter()).intValue() + "";
		//
		//
		// dataSend = StringUtil.findElemString(mediaInfo, "data_send", "--");
		// //begin modified by c00292094 reason:bphp编码显示
		// //编码
		// dataSendCodeString = StringUtil.findElemString(dataSend, "codec",
		// "--")
		// + ("".equals(StringUtil.findElemString(dataSend, "profile", ""))
		// ? "" : ' ') + StringUtil.findElemString(dataSend, "profile", "");
		// //end modified by c00292094 reason:bphp编码显示
		// //帧率
		// dataSendFrameRate = StringUtil.findElemString(dataSend, "framerate",
		// "--");
		// //分辨率
		// dataSendFramsize = StringUtil.findElemString(dataSend, "framesize",
		// "--");
		// //码率
		// dataSendDatarate = StringUtil.findElemString(dataSend, "datarate",
		// "--");
		//

		VideoStreamInfo dataInfo = mediaInfo.getDataStreamInfo();
		// 编码
		dataSendCodeString = dataInfo.getDecodeName() + ' ' + dataInfo.getDecoderProfile();
		// 帧率
		dataSendFrameRate = dataInfo.getSendFrameRate() + "";
		// 分辨率
		dataSendFramsize = dataInfo.getEncoderSize();
		// 码率
		// begin modified by c00349133 reason：为辅流发送码率添加单位
		// dataSendDatarate = dataInfo.getVideoSendBitRate()/1000+"";
		dataSendDatarate.setLength(0);
		dataSendDatarate.append(dataInfo.getVideoSendBitRate() / 1000);
		dataSendDatarate.append('k');
		// end modified by c00349133 reason：为辅流发送码率添加单位

		//
		// dataRecv = StringUtil.findElemString(mediaInfo, "data_recv", "--");
		// //begin modified by c00292094 reason:bphp编码显示
		// //编码
		// dataRecvCodeString = StringUtil.findElemString(dataRecv, "codec",
		// "--")
		// +' '+StringUtil.findElemString(dataRecv, "profile", "");
		// //end modified by c00292094 reason:bphp编码显示
		// //帧率
		// dataRecvFrameRate = StringUtil.findElemString(dataRecv, "framerate",
		// "--");
		// //分辨率
		// dataRecvFramsize = StringUtil.findElemString(dataRecv, "framesize",
		// "--");
		// //码率
		// dataRecvDatarate = StringUtil.findElemString(dataRecv, "datarate",
		// "--");

		// 编码
		dataRecvCodeString = dataInfo.getDecodeName() + ' ' + dataInfo.getDecoderProfile();
		// 帧率
		dataRecvFrameRate = dataInfo.getRecvFrameRate() + "";
		// 分辨率
		dataRecvFramsize = dataInfo.getDecoderSize();
		// 码率
		// dataRecvDatarate = dataInfo.getVideoRecvBitRate()/1000+"";
		// begin modified by c00349133 reason：为辅流接收码率添加单位
		dataRecvDatarate.setLength(0);
		dataRecvDatarate.append(dataInfo.getVideoRecvBitRate() / 1000);
		dataRecvDatarate.append('k');
		// end modified by c00349133 reason：为辅流接收码率添加单位
		//
		// String dataNetInfo = StringUtil.findElemString(mediaInfo, "data_net",
		// "");
		// dataPacketLossProbability = StringUtil.findElemString(dataNetInfo,
		// "lost", "--");
		// dataDelay = StringUtil.findElemString(dataNetInfo, "delay", "--");
		// dataJitter = StringUtil.findElemString(dataNetInfo, "jitter", "--");

		dataPacketLossProbability = Float.valueOf(dataInfo.getVideoRecvLossFraction()).intValue() + "";
		dataDelay = Float.valueOf(dataInfo.getVideoRecvDelay()).intValue() + "";
		dataJitter = Float.valueOf(dataInfo.getVideoRecvJitter()).intValue() + "";

		TransportType isSRTP = audioInfo.getIsSRTP();

		if (TransportType.MEDIASERVICE_TRANS_SRTP.equals(isSRTP))
		{
			audioEncriptionIn = getString(R.string.encription);
			audioEncriptionOut = getString(R.string.encription);

			videoEncriptionIn = getString(R.string.encription);
			videoEncriptionOut = getString(R.string.encription);

			dataEncriptionIn = getString(R.string.encription);
			dataEncriptionOut = getString(R.string.encription);
		} else
		{
			audioEncriptionIn = getString(R.string.unencription);
			audioEncriptionOut = getString(R.string.unencription);

			dataEncriptionIn = getString(R.string.unencription);
			dataEncriptionOut = getString(R.string.unencription);

			videoEncriptionIn = getString(R.string.unencription);
			videoEncriptionOut = getString(R.string.unencription);
		}

		// if ((encryptState & MediaNetInfo.MediaEncrytState.ENCRYPT_AUDIO) !=
		// 0)
		// {
		// audioEncriptionIn = getString(R.string.encription);
		// audioEncriptionOut = getString(R.string.encription);
		// }
		// else
		// {
		// audioEncriptionIn = getString(R.string.unencription);
		// audioEncriptionOut = getString(R.string.unencription);
		// }
		//
		//
		// if ((encryptState & MediaNetInfo.MediaEncrytState.ENCRYPT_DATA) != 0)
		// {
		// dataEncriptionIn = getString(R.string.encription);
		// dataEncriptionOut = getString(R.string.encription);
		// }
		// else
		// {
		// dataEncriptionIn = getString(R.string.unencription);
		// dataEncriptionOut = getString(R.string.unencription);
		// }
		//
		// if ((encryptState & MediaNetInfo.MediaEncrytState.ENCRYPT_VIDEO) !=
		// 0)
		// {
		// videoEncriptionIn = getString(R.string.encription);
		// videoEncriptionOut = getString(R.string.encription);
		// }
		// else
		// {
		// videoEncriptionIn = getString(R.string.unencription);
		// videoEncriptionOut = getString(R.string.unencription);
		// }

		audioEncriptionInText.setText(audioEncriptionIn);
		audioEncriptionOutText.setText(audioEncriptionOut);
		videoEncriptionInText.setText(videoEncriptionIn);
		videoEncriptionOutText.setText(videoEncriptionOut);
		dataEncriptionInText.setText(dataEncriptionIn);
		dataEncriptionOutText.setText(dataEncriptionOut);
		// 软终端没有发送辅流，但软终端通话状态还显示辅流发送的码率与帧率。。
		// 音频通话中
		if (CallService.getInstance().getVoipStatus() == CallStatus.STATUS_TALKING && !CallLogic.getInstance().isVideoCall())
		{
			videoPacketLossProbability = "";
			videoDelay = "";
			videoJitter = "";
			videoSendCodeString = "--";
			// 帧率
			videoSendFrameRate = "--";
			// 分辨率
			videoSendFramsize = "--";
			// 码率
			// add by c00349133
			// videoSendDatarate = "--";
			videoSendDatarate.setLength(0);
			videoSendDatarate.append("--");
			// end by c00349133

			// 编码
			videoRecvCodeString = "--";
			// 帧率
			videoRecvFrameRate = "--";
			// 分辨率
			videoRecvFramsize = "--";
			// 码率
			// add by c00349133
			// videoRecvDatarate = "--";
			videoRecvDatarate.setLength(0);
			videoRecvDatarate.append("--");
			// end by c00349133

			dataPacketLossProbability = "";
			dataDelay = "";
			dataJitter = "";

			setDataEmptyTip();
			// end added by pwx178217 reason:辅流延时抖动和丢包
			// 视频是否加密 发送接收
			videoEncriptionInText.setText("--");
			videoEncriptionOutText.setText("--");
			dataEncriptionInText.setText("--");
			dataEncriptionOutText.setText("--");
		}
		// 视频通话中
		else if (CallService.getInstance().getVoipStatus() == CallStatus.STATUS_VIDEOING)
		{
			if (BFCPStatus.BFCP_END.equals(CallService.getInstance().getBfcpStatus()))
			{

				setDataEmptyTip();
				dataEncriptionOutText.setText("--");
				dataEncriptionInText.setText("--");
			} else if (BFCPStatus.BFCP_START.equals(CallService.getInstance().getBfcpStatus()))
			{

				// 编码
				dataRecvCodeString = "--";
				// 帧率
				dataRecvFrameRate = "--";
				// 分辨率
				dataRecvFramsize = "--";
				// 码率
				// start modified by c00349133 reason:
				// dataRecvDatarate = "--";
				dataRecvDatarate.setLength(0);
				dataRecvDatarate.append("--");
				// end modified by c00349133 reason:
				// begin added by pwx178217 reason:辅流延时抖动和丢包
				dataPacketLossProbability = "";
				dataDelay = "";
				dataJitter = "";
				// end added by pwx178217 reason:辅流延时抖动和丢包
			} else
			{

				dataSendCodeString = "--";
				// 帧率
				dataSendFrameRate = "--";
				// 分辨率
				dataSendFramsize = "--";
				// 码率
				// start modified by c00349133
				// dataSendDatarate = "--";
				dataSendDatarate.setLength(0);
				dataSendDatarate.append("--");
				// end modified by c00349133

			}
		} else
		{
			audioEncriptionInText.setText("--");
			audioEncriptionOutText.setText("--");
			videoEncriptionInText.setText("--");
			videoEncriptionOutText.setText("--");
			dataEncriptionInText.setText("--");
			dataEncriptionOutText.setText("--");
		}
		// end added by pwx178217 2013/12/13 reason:DTS2013121204870
		// 软终端没有发送辅流，但软终端通话状态还显示辅流发送的码率与帧率。。
		// 音频信息
		audioInfoCodeOut.setText(audioCodeString);
		audioInfoCodeIn.setText(audioCodeString);

		// 音频丢包率
		// audioPacketLossInText.setText(audioPacketLossProbability);
		setTextViewColor(audioPacketLossInText, audioPacketLossProbability, TextViewENum.audioPacketLoss);
		// begin modified by c00349133 reason： 为音频丢包率添加单位
		audioPacketLossInText
				.setText(StringUtil.isStringEmpty(audioPacketLossProbability) ? audioPacketLossProbability : audioPacketLossProbability + '%');
		// end modified by c00349133 reason：为音频丢包率添加单位
		// 音频延时
		try
		{
			if (Integer.parseInt(audioDelay) > 200000)
			{
				audioDelay = "0";
			}
		} catch (NumberFormatException e)
		{
			Log.d(TAG, "audioDelay format error.[audioDelay=" + audioDelay + ']');
			audioDelay = "0";
		}
		// audioDelayInText.setText(audioDelay);
		setTextViewColor(audioDelayInText, audioDelay, TextViewENum.delay);
		// begin modified by c00349133 reason： 为音频延时添加单位
		audioDelayInText.setText(StringUtil.isStringEmpty(audioDelay) ? audioDelay : audioDelay + "ms");
		// end modified by c00349133 reason： 为音频延时添加单位
		// 音频抖动
		// audioJitterInText.setText(audioJitter);
		setTextViewColor(audioJitterInText, audioJitter, TextViewENum.audioJitter);
		// begin modified by c00349133 reason： 为音频抖动添加单位
		audioJitterInText.setText(StringUtil.isStringEmpty(audioJitter) ? audioJitter : audioJitter + "ms");
		// end modified by c00349133 reason: 为音频抖动添加单位

		// 视频信息
		videoInfoCodeOut.setText(videoSendCodeString);
		videoInfoCodeIn.setText(videoRecvCodeString);

		videoInfoFrameRateOut.setText(videoSendFrameRate);
		videoInfoFrameRateIn.setText(videoRecvFrameRate);

		videoInfoFramsizeOut.setText(videoSendFramsize);
		videoInfoFramsizeIn.setText(videoRecvFramsize);

		videoInfoDatarateOut.setText(videoSendDatarate);
		videoInfoDatarateIn.setText(videoRecvDatarate);

		// 视频丢包率
		// videoPacketLossInText.setText(videoPacketLossProbability);
		setTextViewColor(videoPacketLossInText, videoPacketLossProbability, TextViewENum.videoPacketLoss);
		// begin modified by c00349133 reason：为视频丢包率添加单位
		videoPacketLossInText
				.setText(StringUtil.isStringEmpty(videoPacketLossProbability) ? videoPacketLossProbability : videoPacketLossProbability + '%');
		// end modified by c00349133 reason： 为视频丢包率添加单位
		// 视频延时
		// videoDelayInText.setText(videoDelay);
		setTextViewColor(videoDelayInText, videoDelay, TextViewENum.delay);
		// begin modified by c00349133 reason：为视频延时添加单位
		videoDelayInText.setText(StringUtil.isStringEmpty(videoDelay) ? videoDelay : videoDelay + "ms");
		// end modified by c00349133 reason：为视频延时添加单位
		// 视频抖动
		// videoJitterInText.setText(videoJitter);
		setTextViewColor(videoJitterInText, videoJitter, TextViewENum.videoJitter);
		// begin modified by c00349133 reason：为视频抖动添加单位
		videoJitterInText.setText(StringUtil.isStringEmpty(videoJitter) ? videoJitter : videoJitter + "ms");
		// begin modified by c00349133 reason：为视频抖动添加单位

		// 辅流信息
		bfcpInfoCodeOut.setText(dataSendCodeString);
		bfcpInfoCodeIn.setText(dataRecvCodeString);

		bfcpInfoFrameRateOut.setText(dataSendFrameRate);
		bfcpInfoFrameRateIn.setText(dataRecvFrameRate);

		bfcpInfoFramsizeOut.setText(dataSendFramsize);
		bfcpInfoFramsizeIn.setText(dataRecvFramsize);

		bfcpInfoDatarateOut.setText(dataSendDatarate);
		bfcpInfoDatarateIn.setText(dataRecvDatarate);

		// begin modified by pwx178217 reason:辅流添加丢包延时抖动显示
		// dataPacketLossInText.setText(dataPacketLossProbability);
		setTextViewColor(dataPacketLossInText, dataPacketLossProbability, TextViewENum.videoPacketLoss);
		// begin modified by c00349133 reason:为辅流丢包率添加单位
		dataPacketLossInText.setText(StringUtil.isStringEmpty(dataPacketLossProbability) ? dataPacketLossProbability : dataPacketLossProbability + '%');
		// end modified by c00349133 reason:为辅流丢包率添加单位
		// dataDelayInText.setText(dataDelay);
		setTextViewColor(dataDelayInText, dataDelay, TextViewENum.delay);
		// begin modified by c00349133 reason:为辅流延时添加单位
		dataDelayInText.setText(StringUtil.isStringEmpty(dataDelay) ? dataDelay : dataDelay + "ms");
		// end modified by c00349133 reason:为辅流延时添加单位
		// dataJitterInText.setText(dataJitter);
		setTextViewColor(dataJitterInText, dataJitter, TextViewENum.videoJitter);
		// begin modified by c00349133 reason:为辅流抖动添加单位
		dataJitterInText.setText(StringUtil.isStringEmpty(dataJitter) ? dataJitter : dataJitter + "ms");
		// end modified by c00349133 reason:为辅流抖动添加单位
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mediaInfo = null;

		cancelTimer();
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		timer = new Timer();
		task = new TimerTask()
		{
			public void run()
			{
				videoInfoActivity.runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						initData();
					}
				});
			}
		};
		timer.schedule(task, 1000, 3000);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		mediaInfo = null;
		cancelTimer();
		instance = null;
	}

	/**
	 * 结束定时
	 * 
	 * @since 1.1
	 * @history 2013-11-5 v1.0.0 pWX178217 create
	 */
	private void cancelTimer()
	{
		if (null != timer)
		{
			timer.cancel();
			timer.purge();
			timer = null;
			task = null;
		}
	}

	
	@Override
	public void clearData()
	{
		// 话机视频通话一段时间后，注销再重新登录，出现异常退出现象
		if (null != timer)
		{
			timer.cancel();
		}
		if (null != rootView)
		{
			((ViewGroup) rootView).removeAllViews();
		}
		timer = null;
		rootView = null;
		audioInfoCodeOut = null;
		audioInfoCodeIn = null;
		videoInfoCodeOut = null;
		videoInfoCodeIn = null;
		videoInfoFrameRateOut = null;
		videoInfoFrameRateIn = null;
		videoInfoFramsizeOut = null;
		videoInfoFramsizeIn = null;
		videoInfoDatarateOut = null;
		videoInfoDatarateIn = null;
		bfcpInfoCodeOut = null;
		bfcpInfoCodeIn = null;
		bfcpInfoFrameRateOut = null;
		bfcpInfoFrameRateIn = null;
		bfcpInfoFramsizeOut = null;
		bfcpInfoFramsizeIn = null;
		bfcpInfoDatarateOut = null;
		bfcpInfoDatarateIn = null;
		task = null;
		videoInfoActivity = null;
		// 话机视频通话一段时间后，注销再重新登录，出现异常退出现象
	}

	@Override
	public void onMediaNetInfoChange(MediaNetInfo netInfo)
	{
		MediaNetInfo.MEDIA_TYPE mediaType = netInfo.getMediaType();
		String delay = netInfo.getDelay();
		String jitter = netInfo.getJitter();
		String lost = netInfo.getLost();
		if (MediaNetInfo.MEDIA_TYPE.AUDIO == mediaType)
		{
			audioPacketLossProbability = lost;
			audioDelay = delay;
			try
			{
				if (Integer.parseInt(audioDelay) > 200000)
				{
					audioDelay = "0";
				}
			} catch (NumberFormatException e)
			{
				Log.d(TAG, "audioDelay format error.[audioDelay=" + audioDelay + ']');
				audioDelay = "0";
			}
			audioJitter = jitter;
		} else if (MediaNetInfo.MEDIA_TYPE.VIDEO == mediaType)
		{
			videoPacketLossProbability = lost;
			videoDelay = delay;
			videoJitter = jitter;
		} else
		{
			dataPacketLossProbability = lost;
			dataDelay = delay;
			dataJitter = jitter;
		}
	}

	/**
	 * 详情点击Listener 
	 */
	private static class DetailOnclickListener implements OnClickListener
	{
		@Override
		public void onClick(View arg0)
		{
		}
	}

	/**
	 * 设置辅流空数值
	 */
	private void setDataEmptyTip()
	{
		dataSendCodeString = "--";
		// 帧率
		dataSendFrameRate = "--";
		// 分辨率
		dataSendFramsize = "--";
		// 码率
		// add modified by c00349133 reason:
		// dataSendDatarate = "--";
		dataSendDatarate.setLength(0);
		dataSendDatarate.append("--");
		// add modified by c00349133 reason:

		// 编码
		dataRecvCodeString = "--";
		// 帧率
		dataRecvFrameRate = "--";
		// 分辨率
		dataRecvFramsize = "--";
		// 码率
		// dataRecvDatarate = "--";
		dataRecvDatarate.setLength(0);
		dataRecvDatarate.append("--");
		// 辅流延时抖动和丢包
		dataPacketLossProbability = "";
		dataDelay = "";
		dataJitter = "";
	}
}
