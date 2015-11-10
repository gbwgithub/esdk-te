package com.huawei.te.example.activity;

import com.huawei.common.PersonalContact;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.CallConstant;
import com.huawei.esdk.te.util.DeviceUtil;
import com.huawei.esdk.te.util.LayoutUtil;
import com.huawei.esdk.te.util.MediaUtil;
import com.huawei.manager.DataManager;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 类名称：CallComingActivity.java 类描述：语音或会议来电
 */
public class CallComingActivity extends BaseActivity {
	private static final String TAG = Constants.GTAG + CallComingActivity.class.getSimpleName();

	private static CallComingActivity instance;

	public static CallComingActivity getInstance() {
		return instance;
	}

	/**
	 * 通话ID
	 */
	private String callid;

	/**
	 * 视频接听按钮
	 */
	private Button acceptVideoBtn;

	/**
	 * 音频接听按钮
	 */
	private Button accepAudioBtn;

	/**
	 * 拒绝按钮
	 */
	private Button rejectBtn;

	/**
	 * 来电图片控件
	 */
	private ImageView callComingImage;

	/**
	 * 来电背景
	 */
	private View callComingBackground;

	/**
	 * 来电标头name显示控件
	 */
	private TextView incomingNameTextView;

	/**
	 * 来电号码显示控件
	 */
	private TextView incomingNumberTextView;

	/**
	 * 来电类型
	 */
	// private TextView incomingTypeTextView;

	/**
	 * 来电类型
	 */
	private int incomingType = 0;

	/**
	 * 来电号码
	 */
	private String incomingNumber = "";

	/*
	 * 来电匿称
	 */
	private String incomingDisplayname = "";

	/**
	 * 通过呼叫号码查询到的来电联系人
	 */
	private PersonalContact incomingContact;

	/**
	 * 呼叫拒绝定时器
	 */
	// private ThreadTimer callRejectTimer = null;

	/**
	 * 定时时间
	 */
	private static final int DELAYTIME = 100000;// 100秒后执行

	private Handler handler = new Handler();

	/**
	 * activity初始化
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("ggg", "start CallComingActivity");
		super.onCreate(null);
		// 设置锁屏之上
		LayoutUtil.setFrontToLock(this);

		// 保持屏幕长亮
		DeviceUtil.setKeepScreenOn(this);

		Intent intent = getIntent();
		incomingNumber = intent.getStringExtra(CallConstant.VOIP_CALLNUMBER);
		// SX20做主叫SIP呼叫TE Mobile时，显示sx20的号码不对
		incomingDisplayname = intent.getStringExtra(CallConstant.VOIP_CALL_DISPLAY_NAME);
		callid = intent.getStringExtra(CallConstant.VOIP_CALLID);
		incomingType = intent.getIntExtra(Constants.COMING_VIEW_TYPE, -1);
		incomingContact = DataManager.getIns().getContactByNumber(incomingNumber);

		// 来电铃声
		startRing();

		// 初始化控件
		initComp();

		// 初始化Voip来电界面
		initCallComing();
		instance = this;
	}

	/**
	 * 初始化控件
	 */
	private void initComp() {
		if (Constants.COMING_CALL == incomingType) {
			this.setContentView(R.layout.audio_callcoming);
		}
		if (Constants.COMING_VIDEO_CALL == incomingType) {
			this.setContentView(R.layout.call_coming);
		}
		Log.d(TAG, "initView...");
		// 来电姓名
		incomingNameTextView = (TextView) findViewById(R.id.incoming_name);

		// 来电号码
		incomingNumberTextView = (TextView) findViewById(R.id.incoming_number);

		// 来电类型
		// incomingTypeTextView = (TextView) findViewById(R.id.incoming_type);
		accepAudioBtn = (Button) findViewById(R.id.callaccept);
		acceptVideoBtn = (Button) findViewById(R.id.accept);
		rejectBtn = (Button) findViewById(R.id.refuse);
		callComingImage = (ImageView) findViewById(R.id.img_incoming);
		callComingBackground = (View) findViewById(R.id.call_coming_background);
		Log.d(TAG, "initWidget...");
		// begin added by l00220604 reason:图片动态加载
		ImageView callComingImageHead = (ImageView) findViewById(R.id.img_incoming_head);
		if (null != callComingImageHead) {
			// if (!ConfigApp.getInstance().isUsePadLayout()) {
			// callComingImageHead.setImageDrawable(getResources().getDrawable(R.drawable.te_phone_call_head_photo));
			// //
			// callComingImageHead.setImageBitmap(ImageResourceUtil.getIns().readBitMap(this,
			// // R.drawable.te_phone_call_head_photo));
			// } else {
			callComingImageHead.setImageDrawable(getResources().getDrawable(R.drawable.te_call_coming_out_head_photo));
			// //
			// callComingImageHead.setImageBitmap(ImageResourceUtil.getIns().readBitMap(this,
			// // R.drawable.te_call_coming_out_head_photo));
			// }
		}
	}

	/**
	 * 方法描述：初始化Voip来电界面 void 备注：
	 */
	private void initCallComing() {
		String callInName = null;
		// String callInType = null;
		String callInNumber = null;

		if (Constants.COMING_VIDEO_CALL != incomingType && Constants.COMING_CALL != incomingType) {
			return;
		}
		// if (!ConfigApp.getInstance().isUsePadLayout()) {
		// callComingBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.te_phone_callcoming_background));
		// // callComingBackground.setBackgroundDrawable(new
		// // BitmapDrawable(ImageResourceUtil.getIns().readBitMap(this
		// // , R.drawable.te_phone_callcoming_background)));
		// } else {
		callComingBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.te_pad_callcoming_background));
		// // callComingBackground.setBackgroundDrawable(new
		// // BitmapDrawable(ImageResourceUtil.getIns().readBitMap(this
		// // , R.drawable.te_pad_callcoming_background)));
		// }
		// 来电显示中，包含两个部分：来电名和来电号码
		// 如果是陌生人,来电名直接显示号码
		if (null == incomingContact) {
			// SX20做主叫SIP呼叫TE Mobile时，显示sx20的号码不对.
			// 陌生人，匿称和号码不同时，显示匿称和号码
			if (!incomingDisplayname.equals(incomingNumber)) {
				callInName = incomingDisplayname;
				callInNumber = incomingNumber;
			}
			// 匿称和号码相同时，只需要显示号码
			else {
				callInName = incomingNumber;
				callInNumber = "";
			}
			// SX20做主叫SIP呼叫TE Mobile时，显示sx20的号码不对
		}
		// 如果是联系人，则显示联系人名和来电号码
		else {
			callInName = incomingContact.getName();
			callInNumber = incomingNumber;
		}

		int isShowIncomingNumberView = callInNumber.isEmpty() ? View.GONE : View.VISIBLE;

		incomingNumberTextView.setVisibility(isShowIncomingNumberView);
		// AnimationUtil.startBackgroundAnimation(callComingImage, 200);
		// 设置来电人名字，号码，来电类型

		// 字符串长时省略显示
		// 根据控件最大长度控制是否显示...
		incomingNameTextView.setText(callInName);
		LayoutUtil.setViewEndEllipse(incomingNameTextView);
		incomingNumberTextView.setText(callInNumber);
		LayoutUtil.setViewEndEllipse(incomingNumberTextView);
		// incomingTypeTextView.setText(callInType);
		Log.d(TAG, "initDate...");
		// 语音接听按钮点击事件
		accepAudioBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 语音接听时，将视频呼叫设置为false
				acceptVoipPhone(false);
			}
		});

		// 视频接听按钮事件
		acceptVideoBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				acceptVoipPhone(true);
			}
		});

		// 拒绝按钮事件
		rejectBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "rejectBtn is clicked.");
				rejectVoipPhone();
			}
		});

		// 新建呼叫拒绝线程计时器
		cancelCallRejectTask();
		handler.postDelayed(callRejectTask, DELAYTIME);
		// callRejectTimer = new ThreadTimer(callRejectTask, DELAYTIME,
		// "rejectcall", ThreadTimer.TimerType.TIMER_ONESHOT);
		// callRejectTimer.start();
		Log.d(TAG, "set callRejectTimer");
	}

	/**
	 * voip来电接听后事件
	 */
	private void acceptVoipPhone(final boolean isVideo) {
		Log.d(TAG, "accept...isVideo=" + isVideo);
		acceptVideoBtn.setClickable(false);
		accepAudioBtn.setClickable(false);
		rejectBtn.setClickable(false);

		// 启动一个空的Activity，1s后消失，使用户不能随意点击其他界面、按钮
		// Intent intent = new Intent(this, UpdateTmpActivity.class);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// TEApp.getIns().startActivity(intent);
		// 接听来电
		// // AsynProcess.getInstance().execute(new SimpleESpaceProcess() {
		// // @Override
		// // public boolean doInBackground() {
		// receiveCall(isVideo);
		// // 不能先Finish 掉Activity，否则横竖屏切换，会导致最后的界面拉伸一下
		// finish();
		// // return true;
		// // }
		// // });

		// 由于接听视频通话，刚刚接听，PC端就挂断，所以这里去掉异步线程试试。
		// TO invoke
		// Thread thread = new Thread(new Runnable() {
		// @Override
		// public void run() {
		receiveCall(isVideo);
		finish();
		// }
		// });
		// thread.start();
	}

	/**
	 * 来电拒绝事件
	 */
	private void rejectVoipPhone() {
		Log.d(TAG, "rejectVoipPhone()");
		// 屏幕感光取消
		DeviceUtil.releaseWakeLock(this);

		acceptVideoBtn.setClickable(false);
		accepAudioBtn.setClickable(false);
		rejectBtn.setClickable(false);
		// HomeActivity.sendHandlerMessage(Constant.MSG_SELF_CHANGE_STATE,
		// PersonalContact.ON_LINE);
		// 拒绝来电
		// AsynProcess.getInstance().execute(new SimpleESpaceProcess() {
		// @Override
		// public boolean doInBackground() {
		CallControl callColtrol = CallControl.getInstance();
		if (null != callColtrol) {
			callColtrol.rejectCall(callid);
		}
		// return true;
		// }
		// });
		// android中相关的view和控件不是线程安全的，我们必须单独做处理
		new Thread() {
			public void run() {
				handler.post(runnableUi);
			}
		}.start();
	}

	private Runnable runnableUi = new Runnable() {
		@Override
		public void run() {
			// 销毁界面
			finish();
		}

	};

	/**
	 * 描述：接听
	 */
	private void receiveCall(boolean isVideo) {
		Log.d(TAG, "receiveCall()");
		// 接听
		// CVoip cvoip = CommonManager.getInstance().getVoip();
		CallControl callControl = CallControl.getInstance();
		boolean answerRet = false;
		if (callControl != null) {
			if (null == Looper.myLooper()) {
				Looper.prepare();
			}

			// // 判断是否接入视频
			// if (isVideo) {
			// VideoCaps caps = VideoHandler.getIns().initCallVideo(this);
			// VideoCaps dataCaps = VideoHandler.getIns().getDataCaps();
			// answerRet = callControl.callAnswer(callid, isVideo, caps,
			// dataCaps);
			// } else {
			// answerRet = callControl.callAnswer(callid, isVideo, null, null);
			// }

			answerRet = callControl.callAnswer(callid, isVideo);

			// 防止接听同时对端取消
			if (!answerRet) {
				// if (null == mToastHelp) {
				// mToastHelp = new
				// ToastHelp(ActivityStackManager.INSTANCE.getCurrentActivity());
				// }
				// mToastHelp.setText(getString(R.string.behangedup));
				// mToastHelp.showToast(2000);
			}
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// 当该activity已经在栈顶时，该activity又被启动，则不调用oncreate，直接调onNewIntent
		// 多路呼叫，收到来电后，又收到一路呼叫，则进行刷新来电界面
		incomingType = intent.getIntExtra(Constants.COMING_VIEW_TYPE, -1);
		Log.i(TAG, "onNewIntent() type:" + incomingType);
		incomingNumber = intent.getStringExtra(CallConstant.VOIP_CALLNUMBER);
		// Begin Modified by z00199735 Reason: 快速呼入挂断后，点击取消导致，呼叫泄露，无法呼入呼出
		callid = intent.getStringExtra(CallConstant.VOIP_CALLID);
		Log.i(TAG, "onNewIntent() callId:" + callid);
		// End Modified by z00199735 Reason: 快速呼入挂断后，点击取消导致，呼叫泄露，无法呼入呼出
		// BEGIN Added by z00199735 2014/03/07 Reason: DTS2014030605381
		// SX20做主叫SIP呼叫TE Mobile时，显示sx20的号码不对
		incomingDisplayname = intent.getStringExtra(CallConstant.VOIP_CALL_DISPLAY_NAME);
		// END Added by z00199735 2014/03/07 Reason: DTS2014030605381
		// SX20做主叫SIP呼叫TE Mobile时，显示sx20的号码不对
		incomingContact = DataManager.getIns().getContactByNumber(incomingNumber);
		if (incomingType == Constants.COMING_CALL) {
			return;
		}
		super.onNewIntent(intent);
	}

	/**
	 * 销毁页面
	 */
	@Override
	public void finish() {
		// 注销广播，停止振玲
		stopRing();
		Log.d(TAG, "callComing finish");
		cancelCallRejectTask();
		super.finish();
		if (null != handler) {
			handler.removeMessages(0);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CallActivity.getInstance().getCallFragment().sendHandlerMessage(Constants.MsgCallFragment.MSG_NOTIFY_CALLCOMING_DESTORY, null);
		instance = null;
	}

	/**
	 * 清空 Activity初始化的数据
	 */
	public void clearData() {
	}

	// 采用在堆栈中直接finish后，不再需要广播
	/**
	 * 停止铃音，振动
	 * 
	 * @since 1.1
	 * @history 2013-8-27 v1.0.0 wWX183960 create
	 */
	private void stopRing() {
		Log.d(TAG, "Stop Ring.");

		MediaUtil.getIns().cancelVibrate();
		MediaUtil.getIns().stopPlayer();
	}

	/**
	 * 铃声，振动
	 */
	private void startRing() {
		// 铃声控制使用新的接口
		// 铃声
		MediaUtil.getIns().playCallComingRing();
	}

	/**
	 * 来电后设置回退键不可点击
	 */
	public void onBackPressed() {

	}

	/**
	 * 新建呼叫超时拒绝任务 ,没用这个任务目前
	 */
	private final Runnable callRejectTask = new Runnable() {
		@Override
		public void run() {
			cancelCallRejectTask();
			// 和硬终端对接时超时后发出拒绝通话操作，防止来电窗口不消失
			rejectVoipPhone();
		}
	};

	/**
	 * 重置callRejectTimer 终止callRejectTask
	 */
	public void cancelCallRejectTask() {
		if (null != handler) {
			handler.removeCallbacks(callRejectTask);
		}
		// if (null != callRejectTimer)
		// {
		// callRejectTimer.stop();
		// callRejectTimer = null;
		// }
	}

}
