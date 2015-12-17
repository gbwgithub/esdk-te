package com.huawei.te.example.menubar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.esdk.te.call.CallConstants.CallStatus;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.call.CallService;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.MsgCallFragment;
import com.huawei.esdk.te.util.LayoutUtil;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.esdk.te.video.VideoHandler;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;
import com.huawei.te.example.activity.CallActivity;
import com.huawei.te.example.utils.ImageResourceUtil;
import com.huawei.voip.data.EarpieceMode;
import com.huawei.voip.data.VoiceQuality.VoiceQualityLevel;

/**
 * 视频菜单栏的控制类
 */
public class MenuBarContalPanel implements OnClickListener, com.huawei.te.example.menubar.VideoMenuBar.MenuItemServer
{

	private static final String TAG = MenuBarContalPanel.class.getSimpleName();

	/**
	 * 界面回调类
	 */
	public interface MenuItemServer
	{
		/**
		 * 全屏、半屏切换
		 */
		void setScreen(boolean isFullScreen);

		/**
		 * 关闭、打开画中画
		 */
		void setPip(boolean isPip);

		/**
		 * 显示通话信息
		 */
		void showCallInfo();

		/**
		 * 显示会场列表
		 */
		void showConfListView();
	}

	private static final int REFRESH_UI_OPERATE_LOCAL_CAMERA = 0x0002;

	/**
	 * 图片背景透明度 半透明float 0.5
	 */
	public static final int HALF_ALPHA = 127;

	/**
	 * more弹出框的高度
	 */
	public static final int MORE_POP_WIDTH = 320;
	/**
	 * more弹出框的宽度
	 */
	public static final float MORE_POP_HEIGHT = 352.666f;
	/**
	 * phone版的more弹出框的高度
	 */
	public static final float PHONE_MORE_POP_HEIGHT = 120.666F;
	/**
	 * phone版的more弹出框的宽度
	 */
	// 手机more弹出框界面显示问题修改 原长度有字符会显示成三行
	public static final int PHONE_MORE_POP_WIDTH = 534;
	/**
	 * more弹出框句menu栏距离
	 */
	public static final float MORE_POP_DISTANCE = 6.666F;

	/**
	 * 图片背景透明度 float 1
	 */
	public static final int NOT_ALPHA = 255;

	/**
	 * 菜单的模式
	 */
	public enum Mode
	{
		/**
		 * 视频通话中
		 */
		VIDEO_CALL,

		/**
		 * 正在视频呼叫
		 */
		VIDEO_CALLING,

		/**
		 * 正在共享文档
		 */
		PDF_SHARE,

		/**
		 * 正在被共享文档
		 */
		PDF_IS_SHARED,

		/**
		 * 辅流不可用
		 */
		BFCP_NOT_ENABLED,

		/**
		 * 正在语音呼叫
		 */
		AUDIO_CALLING,

		/**
		 * 语音通话中
		 */
		AUDIO_CALL, /**
		 * 会话保持中
		 */
		SESSION_HOLD,

		/**
		 * 没有模式
		 */
		NO_MODE
	}

	/**
	 * 菜单模式
	 */
	private Mode menuMode;

	/**
	 * 界面事件回调
	 */
	private MenuItemServer menuItemServer;

	private CallControl callControl;
	/**
	 * 二次拨号盘弹窗PopWindow
	 */
	private PopupWindow recallPopWindow;

	/**
	 * 更多popupWindow
	 */
	private PopupWindow morePopWindow;

	/**
	 * 更多界面
	 */
	private ViewGroup moreView;

	/**
	 * 开启画中画
	 */
	private LinearLayout closePip;

	/**
	 * 开启画中画的图标
	 */
	private ImageView closePipImg;

	/**
	 * 转音频
	 */
	private LinearLayout switchAudio;

	/**
	 * 转音频的图标
	 */
	private ImageView switchAudioImg;

	private VideoMenuBar menuBar;

	/**
	 * 切换摄像头
	 */
	private LinearLayout switchCamera;
	private ImageView switchCameraImg;

	/**
	 * 共享文档
	 */
	private LinearLayout shareData;
	private ImageView shareDataImg;
	private View shareDataLine;

	/**
	 * 共享图片
	 */
	private LinearLayout sharePicLayout;
	private ImageView sharePicImg;
	private View sharePicLine;

	/**
	 * 空item
	 */
	// private LinearLayout emptyitem;

	/**
	 * 空item分割线
	 */
	// private View emptyitemUpLine;

	/**
	 * 开关扬声器布局
	 */
	private LinearLayout speekerControl;

	/**
	 * 分割线条
	 */
	// private View speekerControlLine;
	/**
	 * 开关扬声器图标
	 */
	private ImageView speekerControlImg;
	/**
	 * 打开扬声器、关闭扬声器
	 */
	// private TextView speekerControlTxt;

	/**
	 * 是否全屏显示,默认全屏
	 */
	private boolean isFullScreen = true;

	/**
	 * 各个按钮的互斥标志
	 */
	private boolean isDone = false;
	// ===============底部菜单栏控件=========================

	/**
	 * 对端号码
	 */
	private TextView remoteNumberView;

	/**
	 * 远端号码布局
	 */
	private View remoteNumberLayout;

	/**
	 * 通话时间
	 */
	private TextView showTimeView;

	/**
	 * 通话时间1主要是用在手机上
	 */
	private TextView showTimeView1;

	/**
	 * toast提示
	 */
	// private ToastHelp toast;

	/**
	 * 通话时间
	 */
	private long autoTime = 0;

	/**
	 * 是否开始计数
	 */
	private boolean isCount = false;

	// 关闭本地摄像头后切换前后摄像头，实际不生效但是图标有变化
	/**
	 * 是否关闭摄像头
	 */
	private boolean isCameraClose = false;

	/**
	 * 定时是否在运行
	 */
	private boolean isRun = false;

	private View rootView;

	/**
	 * 定时器
	 */
	private ScheduledExecutorService service;

	/**
	 * 线程池
	 */
	private ExecutorService operPool;
	// ============区分手机和pad按下的效果图==========================

	/**
	 * 麦克图标 正常、关闭
	 */
	private int[][] micClickRes = new int[1][2];

	/**
	 * 关闭视频图标 正常，关闭
	 */
	private int[][] videoClickRes = new int[1][2];

	/**
	 * 蓝牙、 扬声器、 听筒
	 */
	private int[][] blueToothRes = new int[1][3];

	/**
	 * 声音输出开关
	 */
	private int[][] outputClickRes = new int[1][2];

	/**
	 * 音频模式下最大话，最小话按钮
	 */
	private int[][] audioScreenRes = new int[1][2];

	/**
	 * 操作摄像头控件
	 */
	private LinearLayout closeCamera;

	/**
	 * 是否关闭麦克风标志
	 */
	private boolean isMicClosed;

	/**
	 * 是否关闭扬声器标志
	 */
	private boolean isSpeakerClosed;

	private static final Object MENULOCK = new Object();

	/**
	 * 是否会控可用
	 */
	private boolean isConfCtrlEnable = false;

	// ============区分手机和pad按下的效果图==========================

	private Handler timeHandler = new Handler()
	{
		/**
		 * 接收方法
		 * 
		 * @param msg
		 *            接收的Message
		 */
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (null == showTimeView)
			{
				return;
			}
			if (null == showTimeView1)
			{
				return;
			}

			// 呼叫界面中不显示时间，只有在通话过程中显示时间
			if (isCount)
			{
				showTimeView.setVisibility(View.VISIBLE);
				showTimeView.setText(formatTimeFString(autoTime));

				// 只有是视频通话时，才显示
				if (menuMode != Mode.AUDIO_CALL)
				{
					showTimeView1.setVisibility(View.VISIBLE);
					showTimeView1.setText(formatTimeFString(autoTime));
				} else
				{
					showTimeView1.setVisibility(View.GONE);
				}
			} else
			{
				showTimeView.setVisibility(View.INVISIBLE);
				showTimeView1.setVisibility(View.GONE);
			}
		}

	};

	// private ViewGroup shareMoreView;
	//
	// private View sharePicView;
	//
	// private View shareDataView;
	//
	// private PopupWindow shareMorePopWindow;

	/**
	 * 构造函数
	 * 
	 * @param rootView
	 *            根布局
	 * @param modeVar
	 *            菜单模式
	 * @param menuItemServerVar
	 *            界面事件
	 */
	public MenuBarContalPanel(View rootView, MenuItemServer menuItemServerVar)
	{
		this.menuItemServer = menuItemServerVar;
		// cVoip = CommonManager.getInstance().getVoip();
		callControl = CallControl.getInstance();
		this.rootView = rootView;

		initResId();

		// 一定要放在initResId后面，需要用到里面的id
		init();

		// 初始化信号强度为最大
		setSignalStrength(VoiceQualityLevel.EXCELLENT);
	}

	private void initResId()
	{
		// 加载图标

		micClickRes[0][0] = R.drawable.te_phone_menu_mic;
		micClickRes[0][1] = R.drawable.te_phone_menu_close_mic;

		videoClickRes[0][0] = R.drawable.te_state_camera;
		videoClickRes[0][1] = R.drawable.te_state_close_camera;

		// 声音外放开关图片 扬声器图片
		outputClickRes[0][0] = R.drawable.te_phone_menu_speaker;
		outputClickRes[0][1] = R.drawable.te_phone_menu_close_speaker;

		audioScreenRes[0][0] = R.drawable.te_state_audio_minimum;
		audioScreenRes[0][1] = R.drawable.te_state_audio_maximum;

		blueToothRes[0][0] = R.drawable.te_state_menu_route_bluetooth;
		blueToothRes[0][1] = R.drawable.te_state_menu_route_loudspeaker;
		blueToothRes[0][2] = R.drawable.te_state_menu_route_earpiece;
	}

	private void init()
	{
		operPool = Executors.newSingleThreadExecutor();
		menuBar = new VideoMenuBar(rootView);

		// 设置自动隐藏功能
		menuBar.setAutoHidden(true);

		menuBar.setItemServer(this);

		// 添加菜单栏的底部
		remoteNumberView = (TextView) rootView.findViewById(R.id.remote_number);
		showTimeView = (TextView) rootView.findViewById(R.id.audio_time);
		showTimeView1 = (TextView) rootView.findViewById(R.id.audio_time1);
		remoteNumberLayout = rootView.findViewById(R.id.remote_number_layout);

		// 初始化更多弹出框
		initMorePopWindow();
		initShareMorePop();
		// 设置信号按钮Listener
		ImageView signalView = (ImageView) rootView.findViewById(R.id.vedio_menu_single);
		signalView.setOnClickListener(this);
	}

	/**
	 * 初始化更多弹出框
	 * 
	 */
	private void initMorePopWindow()
	{
		// 更多界面
		LinearLayout grouplayout = new LinearLayout(rootView.getContext());
		moreView = (ViewGroup) ((LayoutInflater) rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.video_call_menu_popupwindow, grouplayout, false);

		// 全屏显示
		LinearLayout fullScreen = (LinearLayout) moreView.findViewById(R.id.full_screen);
		fullScreen.setOnClickListener(this);
		ImageView fullScreenImg = (ImageView) moreView.findViewById(R.id.full_screen_img);
		fullScreenImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_minimum));

		// 关闭摄像头
		closeCamera = (LinearLayout) moreView.findViewById(R.id.close_camera);
		closeCamera.setOnClickListener(this);
		ImageView closeCameraImg = (ImageView) moreView.findViewById(R.id.close_camera_img);
		closeCameraImg.setImageDrawable(rootView.getResources().getDrawable(videoClickRes[0][1]));

		// 开启画中画
		closePip = (LinearLayout) moreView.findViewById(R.id.close_pip);
		closePip.setOnClickListener(this);
		closePip.setTag(Constants.CLICK); // 默认开启画中画

		closePipImg = (ImageView) moreView.findViewById(R.id.close_pip_img);
		closePipImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_close_pip));

		// 转语音
		switchAudio = (LinearLayout) moreView.findViewById(R.id.switch_audio);
		switchAudio.setOnClickListener(this);

		switchAudioImg = (ImageView) moreView.findViewById(R.id.switch_audio_img);
		switchAudioImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_video_switch_audio));

		// 前后摄像头切换
		switchCamera = (LinearLayout) moreView.findViewById(R.id.switch_camera);
		switchCamera.setOnClickListener(this);
		switchCameraImg = (ImageView) moreView.findViewById(R.id.switch_camera_img);
		switchCameraImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_switch_camera));
		// ================================
		// 此之间只有手机布局存在 使用时需注意判空
		// 关闭扬声器
		speekerControl = (LinearLayout) moreView.findViewById(R.id.video_speaker);
		// speekerControlLine =
		// moreView.findViewById(R.id.video_speaker_up_line);
		// emptyitem = (LinearLayout) moreView.findViewById(R.id.empty_layout);
		// emptyitemUpLine = moreView.findViewById(R.id.empty_layout_up_line);
		if (null != speekerControl)
		{
			speekerControl.setTag(true);
			speekerControl.setOnClickListener(this);
		}
		speekerControlImg = (ImageView) moreView.findViewById(R.id.video_speaker_img);
		if (null != speekerControlImg)
		{
			speekerControlImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_phone_more_open_speaker));
		}
		// speekerControlTxt = (TextView)
		// moreView.findViewById(R.id.video_speaker_txt);
		// // end 此之间只有手机布局存在 使用时需注意判空 pwx178217 20150827
		// // begin 此之间手机与pad都存在，只有pad用到 pwx178217 20150827
		shareData = (LinearLayout) moreView.findViewById(R.id.share_data);
		shareData.setOnClickListener(this);
		shareDataImg = (ImageView) moreView.findViewById(R.id.share_data_img);
		shareDataImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.tp_call_control_document));
		sharePicLayout = (LinearLayout) moreView.findViewById(R.id.share_pic);
		sharePicLayout.setOnClickListener(this);
		sharePicImg = (ImageView) moreView.findViewById(R.id.share_pic_img);
		sharePicImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.tp_call_control_photo));
		sharePicLine = moreView.findViewById(R.id.share_pic_line);
		shareDataLine = moreView.findViewById(R.id.share_data_line);
		// end 此之间手机与pad都存在，只有pad用到
		// ========================================
		switchCameraImg.setImageBitmap(ImageResourceUtil.getIns().readBitMap(rootView.getContext(), R.drawable.te_state_switch_camera));

		if (morePopWindow == null)
		{
			morePopWindow = new PopupWindow(rootView.getContext())
			{

				@Override
				public void showAsDropDown(View anchor, int xoff, int yoff)
				{
					super.showAsDropDown(anchor, xoff, yoff);
					menuBar.getMenuItems(VideoMenuBar.MORE).setSelected(true);
				}

				@Override
				public void dismiss()
				{
					super.dismiss();
					menuBar.getMenuItems(VideoMenuBar.MORE).setSelected(false);
				}

			};
		}

		// 判断是手机还是pad
		moreView.measure(0, 0);
		// if (!ConfigApp.getInstance().isUsePadLayout()) {
		// morePopWindow.setWidth(parseFloatToInt(PHONE_MORE_POP_WIDTH * 1.6F *
		// LayoutUtil.getInstance().getScreenPXScale()));
		// } else {
		morePopWindow.setWidth(parseFloatToInt(MORE_POP_WIDTH * 2 * LayoutUtil.getInstance().getScreenPXScale()));
		// }
		morePopWindow.setHeight(moreView.getMeasuredHeight());
		morePopWindow.setBackgroundDrawable(new BitmapDrawable());
		morePopWindow.setOutsideTouchable(true);
		morePopWindow.setFocusable(true);
		morePopWindow.setContentView(moreView);
	}

	private void initShareMorePop()
	{
		// LinearLayout grouplayout = new LinearLayout(rootView.getContext());
		// shareMoreView = (ViewGroup) ((LayoutInflater)
		// rootView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
		// .inflate(R.layout.share_more_layout, grouplayout, false);
		//
		// sharePicView = shareMoreView.findViewById(R.id.share_pic);
		// sharePicView.setOnClickListener(this);
		// shareDataView = shareMoreView.findViewById(R.id.share_data);
		// shareDataView.setOnClickListener(this);
		//
		// if (shareMorePopWindow == null) {
		// shareMorePopWindow = new PopupWindow(rootView.getContext());
		// }
		//
		// // 判断是手机还是pad
		// shareMoreView.measure(0, 0);
		// if (!ConfigApp.getInstance().isUsePadLayout()) {
		// shareMorePopWindow.setWidth(parseFloatToInt(PHONE_MORE_POP_WIDTH *
		// 1.6F * LayoutUtil.getInstance().getScreenPXScale()));
		// } else {
		// shareMorePopWindow.setWidth(parseFloatToInt(MORE_POP_WIDTH *
		// LayoutUtil.getInstance().getScreenPXScale()));
		// }
		// shareMorePopWindow.setHeight(shareMoreView.getMeasuredHeight());
		// shareMorePopWindow.setBackgroundDrawable(new BitmapDrawable());
		// shareMorePopWindow.setOutsideTouchable(true);
		// shareMorePopWindow.setFocusable(true);
		// shareMorePopWindow.setContentView(shareMoreView);
	}

	private int parseFloatToInt(float values)
	{
		return Float.valueOf(values).intValue();
	}

	/**
	 * 点击事件
	 */
	@Override
	public void onClick(View v)
	{

		if (menuBar != null)
		{
			menuBar.coverTime();
		} else
		{
			Log.i(TAG, "menuBar is null!");
			return;
		}

		switch (v.getId()) {

		case R.id.full_screen:
			refreshFullScreen();
			break;
		case R.id.close_camera:
			ImageView closeCameraImg = (ImageView) moreView.findViewById(R.id.close_camera_img);
			operateCamera(closeCameraImg);
			break;
		case R.id.close_pip:
			closePipUI();
			break;
		case R.id.switch_audio:
			videoToAudio(v);
			break;
		case R.id.switch_camera:
			switchCamere(switchCameraImg);
			break;
		case R.id.vedio_menu_single:
			menuItemServer.showCallInfo();
			break;
		case R.id.share_data:
			shareFile();
			dismissMorePopWindow();
			break;
		case R.id.share_pic:
			sharePic();
			dismissMorePopWindow();
			break;
		case R.id.video_speaker:
			// 针对手机布局才在这里调用
			// boolean isClose = (Boolean) speekerControl.getTag();
			// speekerControl.setTag(!isClose);
			// if (isClose)
			// {
			// closeSpeakerComfirm();
			// } else
			// {
			// openSpeakerComfirm();
			// }
			break;
		default:
			break;
		}
	}

	/**
	 * 画中画
	 */
	private void closePipUI()
	{
		TextView closePipTxt = (TextView) moreView.findViewById(R.id.close_pip_txt);
		if (Constants.CLICK.equals(closePip.getTag()))
		{
			menuItemServer.setPip(false);
			closePip.setTag(null);
			closePipTxt.setText(R.string.open_pip);
			closePipImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_open_pip));
		} else
		{
			menuItemServer.setPip(true);
			closePip.setTag(Constants.CLICK);
			closePipTxt.setText(R.string.close_pip);
			closePipImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_close_pip));
		}
		closePipImg.getDrawable().setAlpha(NOT_ALPHA);
	}

	/**
	 * 设置远端号码
	 */
	public void setRemoteNumber(String remoteNumberVar)
	{
		// 字符串长时省略显示
		LayoutUtil.setEndEllipse(remoteNumberView, remoteNumberVar, 300);
	}

	/**
	 * 手机版，被共享辅流时，设置开启画中画可不可用
	 */
	public void setPipEnable(boolean isEnable)
	{
		if (isEnable)
		{
			closePipImg.getDrawable().setAlpha(NOT_ALPHA);
			closePip.setEnabled(true);
		} else
		{
			closePipImg.getDrawable().setAlpha(HALF_ALPHA);
			closePip.setEnabled(false);
		}
	}

	/**
	 * 设置画中画文字提示
	 */
	public void setPipTips(boolean isOpen)
	{
		TextView closePipTxt = (TextView) moreView.findViewById(R.id.close_pip_txt);
		if (!isOpen)
		{
			closePip.setTag(null);
			closePipTxt.setText(R.string.open_pip);
			closePipImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_open_pip));
		} else
		{
			closePip.setTag(Constants.CLICK);
			closePipTxt.setText(R.string.close_pip);
			closePipImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_close_pip));
		}
		closePipImg.getDrawable().setAlpha(NOT_ALPHA);
	}

	/**
	 * 设置是否自动隐藏
	 */
	public void setAutoHidden(boolean auto)
	{
		menuBar.setAutoHidden(auto);
	}

	/**
	 * 显示或隐藏菜单
	 */
	public void showAndGone()
	{
		menuBar.showAndGone();
	}

	/**
	 * 显示菜单
	 */
	public void show()
	{

		if (menuBar.getMenuBar().getVisibility() == View.VISIBLE)
		{
			menuBar.getMenuBar().setVisibility(View.GONE);
		}
		menuBar.showAndGone();
	}

	/**
	 * 显示菜单， //用于控制与menuBar同步显示/隐藏的View
	 */
	public void addLink(View view)
	{
		menuBar.addLink(view);
	}

	/**
	 * 取消绑定
	 */
	public void clearAllLinkedView()
	{
		menuBar.clearAllLinkedView();
	}

	/**
	 * 解除绑定
	 * 
	 * @param view
	 *            要解除的view
	 */
	public void removeLinkedView(View view)
	{
		menuBar.removeLinkView(view);
	}

	/**
	 * 更改模式
	 * 
	 * @param modeVar
	 *            模式
	 */
	public void changeMode(Mode modeVar)
	{
		synchronized (MENULOCK)
		{
			changeMode(modeVar, "");

			// 会场列表
			menuBar.setMenuItemVisible(VideoMenuBar.CONFLIST, View.GONE);
			menuBar.setMenuItemVisible(VideoMenuBar.SHOW_DATA, View.GONE);
			// 音频全屏、半屏切换 ，视频的在popwindow里
			menuBar.setMenuItemVisible(VideoMenuBar.AUDIO_SCREEN, View.GONE);

			// public static final String HANG_UP = "hangup";
		}
	}

	/**
	 * 更改模式
	 * 
	 * @param modeVar
	 *            模式
	 * @param remoteNumberVar
	 *            重新设置对端名称
	 */
	public void changeMode(Mode modeVar, String remoteNumberVar)
	{
		menuBar.resetAllMenuItems();
		closeAllPopWindow();
		this.menuMode = modeVar;
		isDone = false;
		if (null != remoteNumberVar && !"".equals(remoteNumberVar))
		{
			remoteNumberView.setText(remoteNumberVar);
		}
		// 更改菜单模式
		switch (menuMode) {
		case VIDEO_CALL:

			isCount = true;
			videoCallMode();
			break;
		case VIDEO_CALLING:

			isCount = false;
			videoCallingMode();
			break;
		case PDF_SHARE:
			isCount = true;
			pdfShareMode();
			break;
		case PDF_IS_SHARED:

			isCount = true;
			pdfSharedMode();
			break;
		case BFCP_NOT_ENABLED:

			isCount = true;
			bfcpIsEnable();
			break;
		case AUDIO_CALL:

			isCount = true;
			audioCallMode();
			break;
		case AUDIO_CALLING:

			isCount = false;
			audioCallingMode();
			break;

		// 会话保持状态
		case SESSION_HOLD:

			isCount = true;
			sessionHoldMode();
			break;
		default:
			// 挂机按钮结束辅流后，下次音视频通话时长显示不对
			Log.i(TAG, "no menu mode");
			isRun = false;
			break;
		}

		// 启动定时
		if (!isRun)
		{
			// 开启通话计时
			if (isCount)
			{
				isRun = true;
				startTimer();
			}
		}

		// 显示菜单栏，如果是视频呼出时，先不显示，在callfragment里面统一显示
		boolean show = (menuMode != Mode.VIDEO_CALLING) && (menuMode != Mode.VIDEO_CALL);
		if (show)
		{
			showAndGone();
		}

		// 刷新音频路由图标
		refreshAudioRouteItem();
	}

	/**
	 * 刷新音频路由图标
	 */
	private void refreshAudioRouteItem()
	{
		List<Integer> audioRouteList = CallLogic.getInstance().getAudioRouteList();
		// Pad上只有扬声器时，则不显示图标， 另：使用有线耳机时不支持切换
		if (audioRouteList.size() <= 1 || EarpieceMode.TYPE_EARPHONE == audioRouteList.get(0))
		{
			menuBar.setMenuItemVisible(VideoMenuBar.BLUETOOTH, View.GONE);

		} else
		{
			int curAudioRoute = audioRouteList.get(0);
			int resId = 0;
			Log.i(TAG, "Handset switch -> " + curAudioRoute);
			switch (curAudioRoute) {
			// 蓝牙
			case EarpieceMode.TYPE_BLUETOOTH:
				resId = blueToothRes[0][0];
				break;

			// 扬声器
			case EarpieceMode.TYPE_LOUD_SPEAKER:
				resId = blueToothRes[0][1];
				break;

			// 听筒
			case EarpieceMode.TYPE_TELRECEIVER:
				resId = blueToothRes[0][2];
				break;

			default:
				break;
			}
			if (0 < resId)
			{
				menuBar.setMenuItemVisible(VideoMenuBar.BLUETOOTH, View.VISIBLE);
				menuBar.getMenuItemsImg(VideoMenuBar.BLUETOOTH).setImageResource(resId);
			}
			// 判断是否在SIM卡通话状态中
			// if (DeviceUtil.isInSIMCall())
			// {
			// enableMenu(false);
			// }
		}
	}

	/**
	 * 刷新是否全屏显示
	 */
	private void refreshFullScreen()
	{
		TextView fullScreenTxt = (TextView) moreView.findViewById(R.id.full_screen_txt);
		ImageView fullScreenImg = (ImageView) moreView.findViewById(R.id.full_screen_img);
		if (isFullScreen)
		{
			isFullScreen = false;
			menuItemServer.setScreen(false);
			dismissMorePopWindow();
			// 视频是否全屏刷新
			fullScreenTxt.setText(R.string.full_screen);
			fullScreenImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_maximum));

			// 音频是否全屏刷新
			menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_SCREEN).setImageResource(audioScreenRes[0][1]);
		} else
		{
			isFullScreen = true;
			menuItemServer.setScreen(true);
			dismissMorePopWindow();
			fullScreenTxt.setText(R.string.part_screen);
			fullScreenImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_minimum));

			menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_SCREEN).setImageResource(audioScreenRes[0][0]);
		}
	}

	/**
	 * 视频通话模式
	 */
	private void videoCallMode()
	{
		// if (null == cVoip) {
		// Log.e(TAG, "error: CVoip is null");
		// return;
		// }
		if (null == callControl)
		{
			Log.e(TAG, "callControl is null");
			return;
		}
		menuBar.getMenuItems(VideoMenuBar.AUDIO_VIDEO).setEnabled(true);
		menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_VIDEO).getDrawable().setAlpha(NOT_ALPHA);
		// 二次拨号盘
		menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD).setEnabled(true);
		menuBar.getMenuItemsImg(VideoMenuBar.REDIAL_BOARD).getDrawable().setAlpha(NOT_ALPHA);

		// 文件共享可用
		menuBar.getMenuItems(VideoMenuBar.SHOW_DATA).setEnabled(true);
		menuBar.getMenuItemsImg(VideoMenuBar.SHOW_DATA).getDrawable().setAlpha(NOT_ALPHA);
		// 画面切换不可见
		remoteNumberLayout.setVisibility(View.GONE);
		menuBar.setMenuItemVisible(VideoMenuBar.HANG_UP, View.VISIBLE);
		menuBar.setMenuItemVisible(VideoMenuBar.SHOW_DATA, View.VISIBLE);
		// 适配手机用
		menuBar.isVideoAudioGONE();
		menuBar.setMenuItemVisible(VideoMenuBar.REDIAL_BOARD, View.VISIBLE);

		// 会控可用 则显示会控
		switchMenubar(isConfCtrlEnable);

		moreView.measure(0, 0);
		if (null != morePopWindow)
		{
			morePopWindow.setHeight(moreView.getMeasuredHeight());
			// 刷新menubar时同时要刷新popwindow 先dismiss
			if (morePopWindow.isShowing())
			{
				morePopWindow.dismiss();
			}
		}
		menuBar.setMenuItemVisible(VideoMenuBar.MORE, View.VISIBLE);
		menuBar.setMenuItemVisible(VideoMenuBar.AUDIO_VIDEO, View.GONE);
		menuBar.setMenuItemVisible(VideoMenuBar.AUDIO_SCREEN, View.GONE);

		// if (!ConfigApp.getInstance().isUsePadLayout()) {
		// if (DeviceUtil.isInSIMCall()) {
		// closeMICConfirm();
		// closeSpeakerComfirm();
		// CommonManager.getInstance().getVoip().localCameraControl(true);
		// enableMenu(false);
		// }
		// }

		// 转音频可用
		switchAudioImg.getDrawable().setAlpha(NOT_ALPHA);
		switchAudio.setEnabled(true);

		// 画中画可用
		closePipImg.getDrawable().setAlpha(NOT_ALPHA);
		closePip.setEnabled(true);

		// if (ConfigApp.getInstance().isUsePadLayout()) {
		// 暂时不判断是不是padLayout
		if (true)
		{
			// 共享文档可用
			shareDataImg.getDrawable().setAlpha(NOT_ALPHA);
			shareData.setEnabled(true);

			// 共享图片可用
			sharePicImg.getDrawable().setAlpha(NOT_ALPHA);
			sharePicLayout.setEnabled(true);
		}

		menuBar.setAutoHidden(false);
		Log.i(TAG, "now videoCallMode");
	}

	/**
	 * 会话保持模式
	 */
	private void sessionHoldMode()
	{
		boolean isVideo = CallService.getInstance().isVideoCall();
		if (isVideo)
		{
			this.menuMode = Mode.VIDEO_CALL;
			videoCallMode();
			// 转音频按钮不可用]
			switchAudioImg.getDrawable().setAlpha(HALF_ALPHA);
			switchAudio.setEnabled(false);

			// 文件共享不可用
			menuBar.getMenuItems(VideoMenuBar.SHOW_DATA).setEnabled(false);
			menuBar.getMenuItemsImg(VideoMenuBar.SHOW_DATA).getDrawable().setAlpha(HALF_ALPHA);
		} else
		{
			this.menuMode = Mode.AUDIO_CALL;
			audioCallMode();
			// 转视频不可用
			menuBar.getMenuItems(VideoMenuBar.AUDIO_VIDEO).setEnabled(false);
			menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_VIDEO).getDrawable().setAlpha(HALF_ALPHA);
		}
	}

	/**
	 * 视频通话拨打模式
	 */
	private void videoCallingMode()
	{
		videoCallMode();
		// 二次拨号盘
		menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD).setEnabled(false);
		menuBar.getMenuItemsImg(VideoMenuBar.REDIAL_BOARD).getDrawable().setAlpha(HALF_ALPHA);
		// 文件共享不可用
		menuBar.getMenuItems(VideoMenuBar.SHOW_DATA).setEnabled(false);
		menuBar.getMenuItemsImg(VideoMenuBar.SHOW_DATA).getDrawable().setAlpha(HALF_ALPHA);

		// 转音频不可用
		switchAudioImg.getDrawable().setAlpha(HALF_ALPHA);
		switchAudio.setEnabled(false);

		// 画中画不可用
		closePipImg.getDrawable().setAlpha(HALF_ALPHA);
		closePip.setEnabled(false);
		menuBar.setItemLineVisibility(VideoMenuBar.REDIAL_BOARD, View.VISIBLE);
		Log.i(TAG, "now video--->videoCallingMode");
	}

	/**
	 * PDF文档共享模式
	 */
	private void pdfShareMode()
	{
		pdfSharedMode();

		// 转音频不可用
		switchAudioImg.getDrawable().setAlpha(HALF_ALPHA);
		switchAudio.setEnabled(false);

		// 文件共享不可用
		menuBar.getMenuItems(VideoMenuBar.SHOW_DATA).setEnabled(false);
		menuBar.getMenuItemsImg(VideoMenuBar.SHOW_DATA).getDrawable().setAlpha(HALF_ALPHA);

		if (isConfCtrlEnable)
		{
			// 共享文档不可用
			shareDataImg.getDrawable().setAlpha(HALF_ALPHA);
			shareData.setEnabled(false);

			// 共享图片不可用
			sharePicImg.getDrawable().setAlpha(HALF_ALPHA);
			sharePicLayout.setEnabled(false);
		}
		Log.i(TAG, "pdfShareMode");
	}

	/**
	 * 正在被共享PDF文档
	 */
	private void pdfSharedMode()
	{
		videoCallMode();

		// 转音频不可用
		switchAudioImg.getDrawable().setAlpha(HALF_ALPHA);
		switchAudio.setEnabled(false);

		// 文件共享可用
		menuBar.getMenuItems(VideoMenuBar.SHOW_DATA).setEnabled(true);
		menuBar.getMenuItemsImg(VideoMenuBar.SHOW_DATA).getDrawable().setAlpha(NOT_ALPHA);
		Log.i(TAG, "pdfSharedMode()");
	}

	/**
	 * BFCP不可用模式
	 */
	private void bfcpIsEnable()
	{
		videoCallMode();

		// 文件共享不可用
		menuBar.getMenuItems(VideoMenuBar.SHOW_DATA).setEnabled(false);
		menuBar.getMenuItemsImg(VideoMenuBar.SHOW_DATA).getDrawable().setAlpha(HALF_ALPHA);

		Log.i(TAG, "bfcpIsEnable()");
	}

	/**
	 * 语音通话模式
	 */
	private void audioCallMode()
	{
		// if (null == cVoip) {
		// Log.e(TAG, "error: CVoip is null");
		// return;
		// }
		if (null == callControl)
		{
			Log.e(TAG, "callControl is null");
			return;
		}
		menuBar.getMenuItems(VideoMenuBar.AUDIO_VIDEO).setEnabled(true);
		menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_VIDEO).getDrawable().setAlpha(NOT_ALPHA);
		// 二次拨号盘
		menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD).setEnabled(true);
		menuBar.getMenuItemsImg(VideoMenuBar.REDIAL_BOARD).getDrawable().setAlpha(NOT_ALPHA);

		remoteNumberLayout.setVisibility(View.GONE);
		// 音频不显示会控
		menuBar.setMenuItemVisible(VideoMenuBar.CONFLIST, View.GONE);

		menuBar.setMenuItemVisible(VideoMenuBar.SHOW_DATA, View.GONE);
		menuBar.setMenuItemVisible(VideoMenuBar.AUDIO_VIDEO, View.VISIBLE);
		menuBar.setMenuItemVisible(VideoMenuBar.REDIAL_BOARD, View.VISIBLE);
		menuBar.setMenuItemVisible(VideoMenuBar.MIC, View.VISIBLE);

		menuBar.setMenuItemVisible(VideoMenuBar.MORE, View.GONE);

		// 如果是手机
		// 暂时不判断是不是手机，先注释掉
		// if (!ConfigApp.getInstance().isUsePadLayout()) {
		// menuBar.setMenuItemVisible(VideoMenuBar.AUDIO_SCREEN, View.GONE);
		// menuBar.setMenuItemVisible(VideoMenuBar.HANG_UP, View.GONE);
		// menuBar.setItemLineVisibility(VideoMenuBar.REDIAL_BOARD, View.GONE);
		//
		// // 设置menubar的背景,只有手机有作用
		// // cVoip.resetAudioRoute(false);
		// // if (DeviceUtil.isInSIMCall()) {
		// // closeMICConfirm();
		// // closeSpeakerComfirm();
		// // enableMenu(false);
		// // }
		// } else {
		menuBar.setMenuItemVisible(VideoMenuBar.AUDIO_SCREEN, View.VISIBLE);
		menuBar.setMenuItemVisible(VideoMenuBar.HANG_UP, View.VISIBLE);
		// }

		menuBar.setAutoHidden(false);
		Log.i(TAG, "audioCallMode()");
	}

	/**
	 * 语音通话拨打模式
	 */
	private void audioCallingMode()
	{
		audioCallMode();
		// 语音视频转换不可用
		menuBar.getMenuItems(VideoMenuBar.AUDIO_VIDEO).setEnabled(false);
		menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_VIDEO).getDrawable().setAlpha(HALF_ALPHA);
		// 二次拨号盘
		menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD).setEnabled(false);
		menuBar.getMenuItemsImg(VideoMenuBar.REDIAL_BOARD).getDrawable().setAlpha(HALF_ALPHA);
		// if (!ConfigApp.getInstance().isUsePadLayout()) {
		// menuBar.setItemLineVisibility(VideoMenuBar.REDIAL_BOARD, View.GONE);
		// }
		Log.i(TAG, "audioCallingMode()");
	}

	@Override
	public void showMoreOpre(View view)
	{
		if (morePopWindow != null && !morePopWindow.isShowing())
		{
			int distance = Float.valueOf(MORE_POP_DISTANCE * LayoutUtil.getInstance().getScreenPXScale()).intValue();
			// 会控可用
			if (isConfCtrlEnable || !switchAudio.isEnabled())
			{
				// 转音频不可用
				switchAudioImg.getDrawable().setAlpha(HALF_ALPHA);
				switchAudio.setEnabled(false);
			} else
			{
				// 转音频可用
				switchAudioImg.getDrawable().setAlpha(NOT_ALPHA);
				switchAudio.setEnabled(true);
			}

			if (isConfCtrlEnable && !CallService.getInstance().isEnableBfcp())
			{
				// 共享文档不可用
				shareDataImg.getDrawable().setAlpha(HALF_ALPHA);
				shareData.setEnabled(false);

				// 共享图片不可用
				sharePicImg.getDrawable().setAlpha(HALF_ALPHA);
				sharePicLayout.setEnabled(false);
			}
			morePopWindow.showAsDropDown(view, 0, distance);
		}

	}

	@Override
	public void showShareMorePopWindow(View view)
	{
		// if (shareMorePopWindow != null && !shareMorePopWindow.isShowing()) {
		// int distance = Float.valueOf(MORE_POP_DISTANCE *
		// LayoutUtil.getInstance().getScreenPXScale()).intValue();
		// shareMorePopWindow.showAsDropDown(view,
		// -(shareMorePopWindow.getWidth() - view.getWidth()) / 2, distance);
		// }
	}

	/**
	 * 关闭视频图像
	 */
	public void operateCamera(final ImageView view)
	{

		if (null == callControl)
		{
			Log.e(TAG, "error: callControl is null");
			return;
		}
		// 软终端视频通话时，本地与远端视频切换时，软终端死机
		// 视频通话，打开手机自带相机功能，软终端本地视频卡住，不能通过开关摄像头按钮进行恢复
		boolean operate = isDone
				|| (CallStatus.STATUS_VIDEOING != CallService.getInstance().getVoipStatus() && CallStatus.STATUS_VIDEOINIT != CallService.getInstance()
						.getVoipStatus());
		if (operate)
		{
			Log.i(TAG, "last close video click was not readly");
			return;
		}

		isDone = true;
		// 开启摄像头
		operPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (MENULOCK)
				{
					// 获取是否已经关闭摄像头
					boolean bHasCloseCamera = Constants.CLICK.equals(view.getTag());

					// 已经关闭则当前操作为开启
					callControl.localCameraControl(!bHasCloseCamera);
					Log.i(TAG, "operate camera, isClose: " + !bHasCloseCamera);

					// 通知界面操作
					Message msg = new Message();
					msg.what = REFRESH_UI_OPERATE_LOCAL_CAMERA;
					msg.obj = !bHasCloseCamera;
					handlerUI.sendMessage(msg);
					isCameraClose = !bHasCloseCamera;
					isDone = false;
				}
			}
		});
	}

	/**
	 * 关闭MIC
	 */
	@Override
	public void closeMIC(ImageView view)
	{
		Log.v(TAG, "closeMIC is clicked()");
		if (null == callControl)
		{
			Log.e(TAG, "error: callControl is null");
			return;
		}

		if (isDone)
		{
			Log.i(TAG, "other click not readly");
			return;
		}
		isDone = true;

		if (Constants.CLICK.equals(view.getTag()))
		{
			view.setImageResource(micClickRes[0][0]);
			view.setTag("");

			// 会议状态下点击取消闭音，执行取消会议闭音方法
			// if (callControl.isInConfCall())
			// {
			// TEAttendee attendee = new TEAttendee();
			// cVoip.muteAttendee(attendee, TupBool.TUP_FALSE);
			// }
			callControl.setLocalMute(true, false);
			Log.i(TAG, "open local MIC Success");
			isDone = false;
		} else
		{
			view.setImageResource(micClickRes[0][1]);
			view.setTag(Constants.CLICK);

			callControl.setLocalMute(true, true);
			Log.i(TAG, "close local MIC Success");
			isDone = false;
		}
	}

	/**
	 * 是否要关闭mic 用于外部调用/
	 */
	public void closeMIC()
	{
		ImageView micView = menuBar.getMenuItemsImg(VideoMenuBar.MIC);
		closeMIC(micView);
	}

	/**
	 * 确认关闭mic 用于外部调用/
	 */
	public void closeMICConfirm()
	{
		ImageView micView = menuBar.getMenuItemsImg(VideoMenuBar.MIC);
		micView.setTag("");
		closeMIC(micView);
	}

	/**
	 * 确认打开mic 用于外部调用/
	 */
	public void openMICConfirm()
	{
		ImageView micView = menuBar.getMenuItemsImg(VideoMenuBar.MIC);
		micView.setTag(Constants.CLICK);
		closeMIC(micView);
	}

	int oritation = 0;

	/**
	 * 是否要关闭speaker 用于外部调用/
	 */
	public void closeSpeaker()
	{
		ImageView micView = menuBar.getMenuItemsImg(VideoMenuBar.SPEAKER);
		closeSpeaker(micView);
	}

	/**
	 * 确认关闭speaker 用于外部调用/
	 */
	public void closeSpeakerComfirm()
	{
		// morePopControlSpeaker(true);
		ImageView micView = menuBar.getMenuItemsImg(VideoMenuBar.SPEAKER);
		micView.setTag("");
		closeSpeaker(micView);
	}

	/**
	 * 打开speaker 用于外部调用/
	 */
	public void openSpeakerComfirm()
	{
		// morePopControlSpeaker(false);
		ImageView micView = menuBar.getMenuItemsImg(VideoMenuBar.SPEAKER);
		micView.setTag(Constants.CLICK);
		closeSpeaker(micView);
	}

	/**
	 * 关闭扬声器
	 * 
	 * @param view
	 *            点击的view
	 */
	@Override
	public void closeSpeaker(ImageView view)
	{
		if (null == callControl)
		{
			Log.e(TAG, "error: callControl is null");
			return;
		}

		if (isDone)
		{
			Log.i(TAG, "other click not readly");
			return;
		}
		isDone = true;

		if (Constants.CLICK.equals(view.getTag()))
		{
			view.setImageResource(outputClickRes[0][0]);
			view.setTag("");

			callControl.oratorMute(false);
			Log.i(TAG, "open local Speaker");
			isDone = false;
		} else
		{
			view.setImageResource(outputClickRes[0][1]);
			view.setTag(Constants.CLICK);

			callControl.oratorMute(true);
			Log.i(TAG, "close local Speaker");
			isDone = false;
		}
	}

	/**
	 * 音频路由编号
	 */
	public void onAudioRouteChange()
	{
		refreshAudioRouteItem();
		Log.i(TAG, "onAudioRouteChange refreshAudioRouteItem");
	}

	/**
	 * 设置是否可点击
	 */
	public void enableMenu(boolean isEnable)
	{
		((View) menuBar.getMenuItemsImg(VideoMenuBar.BLUETOOTH).getParent()).setEnabled(isEnable);
		((View) menuBar.getMenuItemsImg(VideoMenuBar.SPEAKER).getParent()).setEnabled(isEnable);
		((View) menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_VIDEO).getParent()).setEnabled(isEnable);
		((View) menuBar.getMenuItemsImg(VideoMenuBar.MIC).getParent()).setEnabled(isEnable);
		((View) menuBar.getMenuItemsImg(VideoMenuBar.MORE).getParent()).setEnabled(isEnable);
		menuBar.getMenuItemsImg(VideoMenuBar.BLUETOOTH).getDrawable().setAlpha(isEnable ? NOT_ALPHA : HALF_ALPHA);
		menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_VIDEO).getDrawable().setAlpha(isEnable ? NOT_ALPHA : HALF_ALPHA);
		menuBar.getMenuItemsImg(VideoMenuBar.MIC).getDrawable().setAlpha(isEnable ? NOT_ALPHA : HALF_ALPHA);
		menuBar.getMenuItemsImg(VideoMenuBar.MORE).getDrawable().setAlpha(isEnable ? NOT_ALPHA : HALF_ALPHA);
		menuBar.getMenuItemsImg(VideoMenuBar.SPEAKER).getDrawable().setAlpha(isEnable ? NOT_ALPHA : HALF_ALPHA);
	}

	/**
	 * 蓝牙菜单的点击操作
	 * 
	 * @param view
	 *            点击的view
	 */
	@Override
	public void blueToothClick(ImageView view)
	{
		if (null == callControl)
		{
			Log.e(TAG, "error: callControl is null");
			return;
		}

		if (isDone)
		{
			Log.i(TAG, "other click not readly");
			return;
		}
		isDone = true;

		List<Integer> audioRouteList = callControl.getAudioRouteList();
		// 如果是扬声器，则切换到听筒模式（蓝牙，耳机，听筒），但不知道是什么听筒模式，等待刷新
		if (EarpieceMode.TYPE_LOUD_SPEAKER == audioRouteList.get(0))
		{
			Log.i(TAG, "click change to telreceiver");
		}
		// 如果是听筒模式，则切到扬声器
		else
		{
			// 如果是蓝牙，设为扬声器
			view.setImageResource(blueToothRes[0][1]);
			Log.i(TAG, "click change to loudspeaker");
		}

		// operPool.execute(new Runnable()
		// {
		// @Override
		// public void run()
		// {
		// CallService.getInstance().changeAudioRoute();
		// Log.i(TAG, "blue click");
		// isDone = false;
		// }
		// });

		// 试试看改为在主线程中执行会不会有问题

		CallService.getInstance().changeAudioRoute();
		Log.i(TAG, "blue click");
		isDone = false;

		refreshAudioRouteItem();
	}

	/**
	 * 摄像头切换
	 * 
	 * @param view
	 *            点击的view
	 */
	@Override
	public void switchCamere(final View view)
	{
		if (VideoHandler.getIns().getCameraCapacty(VideoHandler.BACK_CAMERA) == VideoHandler.CAMERA_NON
				|| VideoHandler.getIns().getCameraCapacty(VideoHandler.FRONT_CAMERA) == VideoHandler.CAMERA_NON)
		{
			showToast(R.string.camera_bad);
			return;
		}
		// 与PC视频通话过程中，将摄像头前后切换后，远端视频卡住
		if (VideoHandler.getIns().getCameraCapacty(VideoHandler.BACK_CAMERA) == VideoHandler.CAMERA_SCARCE_CAPACITY)
		{
			showToast(R.string.device_performance_warn);
			Log.w(TAG, "this phone has no ability to switch camera");
			return;
		}

		// 闭本地摄像头后切换前后摄像头，实际不生效但是图标有变化
		if (isDone
				|| isCameraClose
				|| (CallStatus.STATUS_VIDEOING != CallService.getInstance().getVoipStatus() && CallStatus.STATUS_VIDEOINIT != CallService.getInstance()
						.getVoipStatus()))
		{
			Log.i(TAG, "other click not readly or camera is closed");
			return;
		}

		isDone = true;
		operPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (MENULOCK)
				{
					boolean result = CallService.getInstance().switchCamera();
					if (result)
					{
						Log.i(TAG, "switch local camera Success");
					}
					isDone = false;
				}
			}
		});
	}

	/**
	 * 视频转语音
	 * 
	 * @param view
	 *            点击的view
	 */
	@Override
	public void videoToAudio(View view)
	{
		if (null == callControl)
		{
			Log.e(TAG, "error: callControl is null");
		}

		if (isDone)
		{
			Log.i(TAG, "other click not readly");
			return;
		}
		isDone = true;

		if (CallStatus.STATUS_TALKING == CallService.getInstance().getVoipStatus())
		{
			// 语音升级视频
			operPool.execute(new Runnable()
			{
				@Override
				public void run()
				{
					if (null == Looper.myLooper())
					{
						Looper.prepare();
					}
					synchronized (MENULOCK)
					{
						callControl.upgradeVideo();
						Log.i(TAG, "upgradevideo");
						isDone = false;
					}
				}
			});
		} else if (CallStatus.STATUS_VIDEOING == CallService.getInstance().getVoipStatus())
		{
			// 视频到语音
			operPool.execute(new Runnable()
			{
				@Override
				public void run()
				{
					synchronized (MENULOCK)
					{
						// boolean result = cVoip.closeVideo();
						boolean result = callControl.closeVideo();
						isDone = false;
						if (!result)
						{
							return;
						}
						Log.i(TAG, "video -- > audio");
					}
				}
			});
		} else
		{
			isDone = false;
		}
	}

	/**
	 * 对方音视频转换时，状态重置
	 */
	public void reSetVideoToAudioState()
	{
		isCameraClose = false;
		isDone = false;

		refreshMenuItemLocalCamera(false);
	}

	/**
	 * 二次拨号盘
	 * 
	 * @param view
	 *            点击的view
	 */
	@Override
	public void audioRecall(View view)
	{
		// 手机中音频为竖屏，视频为横屏，每次需重新创建
		recallPopWindow = new RecallPopWindow(rootView.getContext(), menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD))
		{
			@Override
			public void showAsDropDown(View anchor, int xoff, int yoff)
			{
				super.showAsDropDown(anchor, xoff, yoff);
				menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD).setSelected(true);
			}

			@Override
			public void dismiss()
			{
				super.dismiss();
				menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD).setSelected(false);
			}
		};
		int distance = Float.valueOf((MORE_POP_DISTANCE * LayoutUtil.getInstance().getScreenPXScale())).intValue();

		recallPopWindow.showAsDropDown(menuBar.getMenuItems(VideoMenuBar.REDIAL_BOARD), (-recallPopWindow.getWidth()) / 2, distance);
	}

	/**
	 * 共享文档
	 */
	@Override
	public void shareFile()
	{
		// if ((CallStatus.STATUS_VIDEOING !=
		// CallService.getInstance().getVoipStatus()
		// && CallStatus.STATUS_VIDEOINIT !=
		// CallService.getInstance().getVoipStatus()))
		// {
		// Log.i(TAG, "not in video mode");
		// return;
		// }
		// Intent intent = new Intent(rootView.getContext(),
		// FileBrowserActivity.class);
		// intent.putExtra("type", FileBrowserActivity.PDF);
		// rootView.getContext().startActivity(intent);
	}

	private void sharePic()
	{
		// if ((CallStatus.STATUS_VIDEOING !=
		// CallService.getInstance().getVoipStatus()
		// && CallStatus.STATUS_VIDEOINIT !=
		// CallService.getInstance().getVoipStatus()))
		// {
		// Log.i(TAG, "not in video mode");
		// return;
		// }
		// Intent intent = new Intent(rootView.getContext(),
		// ImgFileListActivity.class);
		// rootView.getContext().startActivity(intent);
	}

	/**
	 * 音频模式下全屏切换
	 * 
	 * @param view
	 *            点击的view
	 */
	@Override
	public void setAudioScreen(ImageView view)
	{
		refreshFullScreen();
	}

	/**
	 * 结束通话 - 挂断
	 */
	@Override
	public void endVideoCall()
	{
		Log.i(TAG, "endVideoCall send end call request.");
		CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_END_REQUEST, null);
	}

	// 调用挂断按钮
	// @Override
	// public void run()
	// {
	// synchronized (MENULOCK)
	// {
	// Log.i(TAG, "endVideoCall send end call request.");
	// CallActivity.getInstance().getCallFragment().sendHandlerMessage(MsgCallFragment.MSG_CALL_END_REQUEST,
	// null);
	// }
	// }

	/**
	 * 清除弹出的PopupWindow
	 */
	@Override
	public void dismissPopupWindow()
	{
		if (null != recallPopWindow)
		{
			recallPopWindow.dismiss();
		}

		if (null != morePopWindow)
		{
			morePopWindow.dismiss();
		}
	}

	/**
	 * 在菜单栏消失时清除弹出的更多PopupWindow
	 */
	@Override
	public void dismissMorePopWindow()
	{
		if (null != morePopWindow)
		{
			morePopWindow.dismiss();
		}
		// if (null != shareMorePopWindow) {
		// shareMorePopWindow.dismiss();
		// }
	}

	private void showToast(int resId)
	{
		Toast.makeText(rootView.getContext(), rootView.getContext().getString(resId), Toast.LENGTH_LONG).show();
	}

	/**
	 * 还原麦克
	 */
	private void resetMIC()
	{
		if (null == callControl)
		{
			Log.e(TAG, "error: callControl is null");
			return;
		}

		// 一路通话结束还原麦克
		menuBar.getMenuItemsImg(VideoMenuBar.MIC).setImageResource(micClickRes[0][0]);
		operPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				callControl.setLocalMute(true, false);
			}
		});
	}

	/**
	 * 还原扬声器
	 */
	private void resetSpeaker()
	{
		if (null == callControl)
		{
			Log.e(TAG, "error: callControl is null");
			return;
		}

		// 一路通话结束还原扬声器
		menuBar.getMenuItemsImg(VideoMenuBar.SPEAKER).setImageResource(outputClickRes[0][0]);
		operPool.execute(new Runnable()
		{
			@Override
			public void run()
			{
				callControl.oratorMute(false);
			}
		});
	}

	/**
	 * 获得菜单栏
	 * 
	 * @return 菜单栏
	 */
	public View getMenuBar()
	{
		return menuBar.getMenuBar();
	}

	/**
	 * 关闭所有弹出框
	 */
	public void closeAllPopWindow()
	{
		// // 软终端在二次拨号拨号盘界面结束通话时，二次拨号拨号盘不消失
		// if (ActivityStackManager.INSTANCE.getCurrentActivity() instanceof
		// FileBrowserActivity)
		// {
		// if (((FileBrowserActivity)
		// ActivityStackManager.INSTANCE.getCurrentActivity()).getType() ==
		// FileBrowserActivity.PDF)
		// {
		// ActivityStackManager.INSTANCE.getCurrentActivity().finish();
		// }
		// }
		//
		// ActivityStackManager.INSTANCE.getImgFileListActivityAndRemove();
		// ActivityStackManager.INSTANCE.getImgShowActivityAndRemove();
	}

	/**
	 * 格式化时间
	 * 
	 * @param longTime
	 *            时间
	 * @return 格式后的时间
	 */
	private String formatTimeFString(long longTime)
	{
		String time = "%2d:%2d:%2d";
		int hour = parseLongToInt(longTime / (60 * 60));
		int min = parseLongToInt((longTime - hour * (60L * 60)) / 60);
		int sec = parseLongToInt(longTime % 60);
		time = String.format(time, hour, min, sec);

		return time.replace(' ', '0');
	}

	private int parseLongToInt(long value)
	{
		return Long.valueOf(value).intValue();
	}

	/**
	 * 设置信号强度
	 * 
	 * @param level
	 *            信号强度
	 */
	public void setSignalStrength(VoiceQualityLevel level)
	{
		ImageView signalView = (ImageView) rootView.findViewById(R.id.vedio_menu_single);
		ImageView signalView1 = (ImageView) rootView.findViewById(R.id.vedio_menu_single1);
		switch (level) {
		// 这里要根据信号强度来设置图像
		case POOL:// 差
			signalView.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_signal_01));
			signalView1.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_phone_signal_01));
			break;
		case NORMAL_1:
			signalView.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_signal_02));
			signalView1.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_phone_signal_02));
			break;
		case NORMAL_2:
			signalView.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_signal_03));
			signalView1.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_phone_signal_03));
			break;
		case NORMAL_3:
			signalView.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_signal_04));
			signalView1.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_phone_signal_04));
			break;
		case EXCELLENT:// 信号强
			signalView.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_signal_05));
			signalView1.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_phone_signal_05));
			break;
		default:
			break;
		}

	}

	/**
	 * 是否显示对话框
	 * 
	 * @return true 显示
	 */
	public boolean isNeedShow()
	{
		return menuBar.isNeedShow();
	}

	/**
	 * 是否显示对话框
	 * 
	 * @param isNeedShow
	 *            true 显示
	 */
	public void setNeedShow(boolean isNeedShow)
	{
		menuBar.setNeedShow(isNeedShow);
	}

	/**
	 * 重置数据，如计时时间,蓝牙,MIC
	 */
	public void resetData()
	{
		resetMIC();
		resetTime();
		resetSpeaker();
		isCameraClose = false;
		isDone = false;
		showTimeView.setVisibility(View.INVISIBLE);
		showTimeView1.setVisibility(View.GONE);

		// 重置画中画为打开
		TextView closePipTxt = (TextView) moreView.findViewById(R.id.close_pip_txt);
		closePip.setTag(Constants.CLICK);
		closePipTxt.setText(R.string.close_pip);
		closePipImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_close_pip));

		menuBar.getMenuItemsImg(VideoMenuBar.BLUETOOTH).setImageResource(blueToothRes[0][0]);

		menuBar.resetAllMenuTag();
		refreshMenuItemLocalCamera(false);

		// 信号也置为初始化
		ImageView signalView = (ImageView) rootView.findViewById(R.id.vedio_menu_single);
		ImageView signalView1 = (ImageView) rootView.findViewById(R.id.vedio_menu_single1);
		signalView.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_signal_05));
		signalView1.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_phone_signal_05));

		// 刷新是否全屏
		isFullScreen = true;
		menuBar.getMenuItemsImg(VideoMenuBar.AUDIO_SCREEN).setImageResource(audioScreenRes[0][0]);
		TextView fullScreenTxt = (TextView) moreView.findViewById(R.id.full_screen_txt);
		ImageView fullScreenImg = (ImageView) moreView.findViewById(R.id.full_screen_img);
		fullScreenTxt.setText(R.string.part_screen);
		fullScreenImg.setImageDrawable(rootView.getResources().getDrawable(R.drawable.te_state_minimum));

		menuBar.setMenuItemVisible(VideoMenuBar.BLUETOOTH, View.GONE);
		closeAllPopWindow();
		Log.i(TAG, "reset menu data");
	}

	/**
	 * 重置时间
	 */
	private void resetTime()
	{
		// 通话计时
		// 重置时间关闭计时
		if (null != service)
		{
			service.shutdown();
			service = null;
		}
		autoTime = 0;
		isCount = false;
		showTimeView.setText("");
		showTimeView1.setText("");
		closeAllPopWindow();

		// 关闭线程
		setRun(false);
	}

	private Handler handlerUI = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			if (null == menuItemServer)
			{
				return;
			}
			if (REFRESH_UI_OPERATE_LOCAL_CAMERA == msg.what)
			{
				boolean isClose = (Boolean) msg.obj;
				// 已经关闭，则操作为打开
				refreshMenuItemLocalCamera(isClose);

				Log.i(TAG, "handle refresh UI operate camera.");
			}
		}

	};

	private void refreshMenuItemLocalCamera(boolean bIsClose)
	{
		Log.d(TAG, "refreshMenuItemLocalCamera enter: " + bIsClose);

		ImageView view = (ImageView) moreView.findViewById(R.id.close_camera_img);
		// 刷新文字
		TextView closeCameraTxt = (TextView) moreView.findViewById(R.id.close_camera_txt);

		Log.i(TAG, "refreshMenuItemLocalCamera bIsClose: " + bIsClose);

		// 摄像头关闭状态则需要disable 切换摄像头
		switchCamera.setEnabled(!bIsClose);
		switchCameraImg.getDrawable().setAlpha(!bIsClose ? NOT_ALPHA : HALF_ALPHA);

		// 如果已关闭过，则当前操作为打开
		if (!bIsClose)
		{
			view.setImageDrawable(rootView.getResources().getDrawable(videoClickRes[0][1]));
			view.setTag("");
			closeCameraTxt.setText(R.string.close_camera);
		} else
		{
			view.setImageDrawable(rootView.getResources().getDrawable(videoClickRes[0][0]));
			view.setTag(Constants.CLICK);
			closeCameraTxt.setText(R.string.open_camera);
		}

		Log.d(TAG, "refreshMenuItemLocalCamera leave.");

	}

	public boolean isCameraClose()
	{
		return isCameraClose;
	}

	public boolean isMicClosed()
	{
		return isMicClosed;
	}

	public void setMicClosed(boolean isMicClosed)
	{
		this.isMicClosed = isMicClosed;
	}

	public void setSpeakerClosed(boolean isSpeakerClosed)
	{
		this.isSpeakerClosed = isSpeakerClosed;
	}

	public boolean isSpeakerClosed()
	{
		return isSpeakerClosed;
	}

	/**
	 * @return 是否在运行
	 */
	public boolean isRun()
	{
		return isRun;
	}

	/**
	 * @param isRun
	 *            设置是否及时线程开启
	 */
	public void setRun(boolean isRun)
	{
		this.isRun = isRun;
	}

	/**
	 * @param isVisible
	 *            设置是视频远程号码是否在menubar栏显示
	 */
	public void setRemoteNumberVisible(boolean isVisible)
	{
		if (remoteNumberLayout != null)
		{
			if (isVisible)
			{
				remoteNumberLayout.setVisibility(View.VISIBLE);
			} else
			{
				remoteNumberLayout.setVisibility(View.GONE);
			}
		}
	}

	// 话机视频通话一段时间后，注销再重新登录，出现异常退出现象
	/**
	 * 清除数据
	 */
	public void clearData()
	{
		this.isRun = false;
		isFullScreen = true;
		timeHandler = null;
		handlerUI = null;
		recallPopWindow = null;
		morePopWindow = null;
		remoteNumberView = null;
		remoteNumberLayout = null;
		showTimeView = null;
		showTimeView1 = null;
		micClickRes = null;
		videoClickRes = null;
		blueToothRes = null;
		outputClickRes = null;
		audioScreenRes = null;
		rootView = null;
		closePip = null;
		switchAudio = null;
		switchAudioImg = null;
		closePipImg = null;
		if (null != menuBar)
		{
			menuBar.clearData();
		}
	}

	// 话机视频通话一段时间后，注销再重新登录，出现异常退出现象
	// 开启通话计时
	/**
	 * 通话计时
	 */
	private void startTimer()
	{
		if (null == service)
		{
			service = Executors.newScheduledThreadPool(1);
		}
		service.scheduleAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				++autoTime;
				timeHandler.sendEmptyMessage(0);
			}
		}, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	public void setMicClose(boolean isClose)
	{
		setMicClosed(isClose);
	}

	@Override
	public void setSpeakerClose(boolean isClose)
	{
		setSpeakerClosed(isClose);
	}

	/**
	 * 显示会场列表
	 */
	@Override
	public void showConfList()
	{
		menuItemServer.showConfListView();
	}

	/**
	 * 设置会议列表显示按钮是否可用
	 */
	public void setShowConfListEnable(boolean isEnable)
	{
		isConfCtrlEnable = isEnable;
		// 是否显示会场列表按钮
		if (isEnable && CallStatus.STATUS_TALKING != CallService.getInstance().getVoipStatus())
		{
			switchMenubar(true);
		} else
		{
			switchMenubar(false);
		}
		moreView.measure(0, 0);
		if (null != morePopWindow)
		{
			morePopWindow.setHeight(moreView.getMeasuredHeight());
			// 刷新menubar时同时要刷新popwindow 先dismiss
			if (morePopWindow.isShowing())
			{
				morePopWindow.dismiss();
			}
		}
	}

	/**
	 * 根据会议信息改变mic图标
	 */
	public void changeMicImg(boolean isMicClose)
	{
		// 本地闭音的时候不切换mic图标
		// if (cVoip.isMicrophoneMute()) {
		// return;
		// }
		if (isMicClose)
		{
			menuBar.getMenuItemsImg(VideoMenuBar.MIC).setImageResource(micClickRes[0][1]);
			menuBar.getMenuItemsImg(VideoMenuBar.MIC).setTag(Constants.CLICK);
			return;
		}
		menuBar.getMenuItemsImg(VideoMenuBar.MIC).setImageResource(micClickRes[0][0]);
		menuBar.getMenuItemsImg(VideoMenuBar.MIC).setTag("");
	}

	/**
	 * 切换菜单栏 会控/非会控 界面 菜单栏 有会控能力 和 无会控能力布局切换
	 */
	private void switchMenubar(boolean isConfenable)
	{
		// 会控可用情况下 手机：菜单栏扬声器开关处于更多弹窗中 转语音和关闭摄像头处于最顶端。 PAD：共享图片和共享处于更多弹窗中可见并处于最顶端
		// if (!ConfigApp.getInstance().isUsePadLayout()) {
		// menuBar.setMenuItemVisible(VideoMenuBar.SPEAKER, isConfenable ?
		// View.GONE : View.VISIBLE);
		// switchAudio.setBackgroundDrawable(switchCamera.getResources()
		// .getDrawable(isConfenable ? R.drawable.te_pad_menu_item :
		// R.drawable.te_phone_more_background_left_bottom));
		// closeCamera.setBackgroundDrawable(switchCamera.getResources()
		// .getDrawable(isConfenable ? R.drawable.te_pad_menu_item :
		// R.drawable.te_phone_more_background_right_bottom));
		// morePopSpeakerVisibility(isConfenable);
		// } else {
		shareData.setVisibility(isConfenable ? View.VISIBLE : View.GONE);
		sharePicLayout.setVisibility(isConfenable ? View.VISIBLE : View.GONE);
		sharePicLine.setVisibility(isConfenable ? View.VISIBLE : View.GONE);
		shareDataLine.setVisibility(isConfenable ? View.VISIBLE : View.GONE);
		menuBar.setMenuItemVisible(VideoMenuBar.SHOW_DATA, isConfenable ? View.GONE : View.VISIBLE);
		closePip.setBackgroundDrawable(switchAudio.getResources().getDrawable(
				isConfenable ? R.drawable.te_pad_menu_item : R.drawable.te_state_menu_top_left));
		switchCamera.setBackgroundDrawable(switchCamera.getResources().getDrawable(
				isConfenable ? R.drawable.te_pad_menu_item : R.drawable.te_state_menu_top_right));
		// }
		menuBar.setMenuItemVisible(VideoMenuBar.CONFLIST, isConfenable ? View.VISIBLE : View.GONE);
	}
}
