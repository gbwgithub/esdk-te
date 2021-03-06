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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.common.CustomBroadcastConst;
import com.huawei.common.Resource;
import com.huawei.common.ResponseCodeHandler.ResponseCode;
import com.huawei.common.ThreadTimer;
import com.huawei.esdk.te.TESDK;
import com.huawei.esdk.te.TESDK.LoginParameter;
import com.huawei.esdk.te.call.CallService;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;
import com.huawei.te.example.ResponseErrorCodeHandler;
import com.huawei.te.example.TEDemoApp;
import com.huawei.te.example.utils.FileUtil;
import com.huawei.utils.StringUtil;

public class LoginActivity extends BaseActivity
{
	// 公网环境
	// /**
	// * TCP\UDP 222.92.146.245：15060 -> 172.19.27.171：5060
	// * TCP\UDP 222.92.146.245：15061 -> 172.19.27.171：5061
	// */
	// private static final String ACCOUNT = "20150512";
	// private static final String PASSWORD = "huawei123";
	// private static final String SERVER = "222.92.146.245";
	// private static final String PORT = "15061";
	// private static final String SIPURI = "20150512@222.92.146.245";
	// private static final String LICENSESERVER = "222.92.146.245";

	// 产品环境
	// Wifi w00327180_DW_Test
	// TEMobile需要手动在LoginInfo中set,设置licenseServer
	// private static final String ACCOUNT = "20150512";
	// private static final String PASSWORD = "huawei123";
	// private static final String SERVER = "10.174.4.226";
	// private static final String PORT = "5061";
	// private static final String SIPURI = "20150512@10.174.4.226";
	// private static final String LICENSESERVER = "10.174.199.239";

	// 城云环境
	// Wifi w00327180_DW_Test
	// TEMobile需要手动在LoginInfo中set,设置licenseServer
	// private static final String ACCOUNT = "wgl.cci";
	// private static final String PASSWORD = "123";
	// private static final String SERVER = "tp.onecc.me";
	// private static final String PORT = "5061";
	// private static final String LICENSESERVER = "10.10.160.99";
	// // private static final String SIPURI = "wgl.cci@tp.onecc.me";
	// private static final String SIPURI = "wgl.cci@onecc.me";

	// //172.22.8.4环境
	// private static final String SERVER = "172.22.8.4";
	// private static final String PORT = "5061";
	// private static final String ACCOUNT = "01058888";
	// private static final String PASSWORD = "Huawei@123";
	// private static final String SIPURI = "";
	// private static final String LICENSESERVER = "";

	// //172.22.8.4环境
	// private static final String SERVER = "172.22.8.4";
	// private static final String PORT = "5061";
	// private static final String ACCOUNT = "01058888";
	// private static final String PASSWORD = "Huawei@123";
	// private static final String SIPURI = "01058888@172.22.8.4";
	// private static final String LICENSESERVER = "172.22.9.21";

	// 开启license环境 8.4
	private static final String SERVER = "172.22.8.4";
	private static final String PORT = "5061";
	private static final String ACCOUNT = "01058888";
	private static final String PASSWORD = "Huawei@123";
	private static final String SIPURI = "";
	private static final String LICENSESERVER = "172.22.9.22";

	// 进入思科虚拟会议室-VMR
	//城云环境 - 关闭license验证
//	private static final String SERVER = "tp.onecc.me";
//	private static final String PORT = "5061";
//	private static final String ACCOUNT = "crj.cci";
//	private static final String PASSWORD = "123456";
//	private static final String SIPURI = "crj.cci@onecc.me";
//	private static final String LICENSESERVER = "172.22.9.22";

	// //产品环境
	// private static final String SERVER = "172.22.8.4";
	// private static final String PORT = "5061";
	// private static final String ACCOUNT = "01052430";
	// private static final String PASSWORD = "Huawei@123";
	// private static final String SIPURI = "01052430@172.22.9.21";
	// private static final String LICENSESERVER = "";

	private static final String TAG = LoginActivity.class.getSimpleName();
	private static LoginActivity instance;
	private String serverIP; // 登录服务器地址
	private String serverPort; // 登录服务器地址
	private String sipURI; // 代理服务器地址
	private String licenseServer; // 验证服务器地址
	private static String protocolType = "TLS"; // 传输协议

	public static String getProtocolType()
	{
		return protocolType;
	}

	public static void setProtocolType(String protocolType)
	{
		LoginActivity.protocolType = protocolType;
	}

	private EditText edServerIP;
	private EditText edServerPort;
	private EditText edSipURI;
	private EditText edLicenseServer;
	private EditText edUsername;
	private EditText edPassword;
	private Button btnLogin;
	private TextView tvMore;
	private LinearLayout loginMoreLayout;
	private LinearLayout settingLayout;

	/**
	 * 用户eSpace账号
	 */
	private String eSpaceNumber;
	/**
	 * 用户eSpace密码
	 */
	private String eSpaceWordPass;
	/**
	 * 登录延时计时线程
	 */
	private ThreadTimer loginDelayTimer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		LogUtil.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		initView();
		registeBroadcast();
		instance = this;
	}

	private void initView()
	{
		loginMoreLayout = (LinearLayout) findViewById(R.id.linearlayout_login_more);
		settingLayout = (LinearLayout) findViewById(R.id.linearlayout_setting_more);
		edUsername = (EditText) findViewById(R.id.et_username);
		edPassword = (EditText) findViewById(R.id.et_password);
		edServerIP = (EditText) findViewById(R.id.et_server_ip);
		edServerPort = (EditText) findViewById(R.id.et_server_port);
		edSipURI = (EditText) findViewById(R.id.et_sip_server_ip);
		edLicenseServer = (EditText) findViewById(R.id.et_license_server);
		btnLogin = (Button) findViewById(R.id.btn_login);
		btnLogin.setOnClickListener(listener);
		tvMore = (TextView) findViewById(R.id.tv_login_more);
		tvMore.setOnClickListener(listener);

		edUsername.setText(ACCOUNT);
		edPassword.setText(PASSWORD);
		edServerIP.setText(SERVER);
		edServerPort.setText(PORT);
		edSipURI.setText(SIPURI);
		edLicenseServer.setText(LICENSESERVER);
	}

	// 点击设置按钮显示设置界面
	public void showSettingLayout(View v)
	{
		Intent intent = new Intent(this, SettingActivity.class);
		startActivityForResult(intent, Constants.REQUEST_GOTO_SETTINGVIEW);
	}

	// 保存日志
	public void saveLog(View v)
	{
		LogUtil.i(TAG, "save Fault Report.");
		FileUtil.getIns().sendTEMobileLog(this, FileUtil.LOG_FILE_TYPE);
	}

	/**
	 * 点击登录按钮
	 */
	private void login()
	{
		String account = edUsername.getText().toString().trim();
		String password = edPassword.getText().toString();
		serverIP = edServerIP.getText().toString().trim();
		serverPort = edServerPort.getText().toString().trim();
		sipURI = edSipURI.getText().toString().trim();
		if (sipURI.equals("") && !serverIP.equals("") && !account.equals(""))
		{
			// 暂时用于测试，在没有填写sipUri的时候自动生成sipUri
			// edSipURI.setText(account + "@" + serverIP);
			sipURI = account + "@" + serverIP;
		}
		licenseServer = edLicenseServer.getText().toString().trim();
		if (null == serverIP || serverIP.equals(""))
		{
			Toast.makeText(this, "请输入服务器地址", Toast.LENGTH_SHORT).show();
			return;
		}
		if (null == account || null == password || account.equals("") || password.equals(""))
		{
			Toast.makeText(this, "请输入账号或密码", Toast.LENGTH_SHORT).show();
			return;
		}
		eSpaceNumber = account;
		eSpaceWordPass = password;
		doLoginClicked();
	}

	private void doLoginClicked()
	{
		LoginParameter info = new LoginParameter();
		info.setLicenseServer(licenseServer);
		info.setServerIP(serverIP);
		info.setServerPort(serverPort);
		info.setSipuri(sipURI);
		LogUtil.d(TAG, "info.setSipuri -> " + sipURI);
		boolean isAnonymous = false;
		// 匿名呼叫自动使用UDP传输协议 -- TLS TCP UDP协议登录时记录设置端口
		info.setProtocolType(protocolType);
		// Log传输协议
		LogUtil.i(TAG, "ProtocolType is " + info.getProtocolType());
		// 设置心跳
		info.setSupportSipSessionTimer(true);
		// 设置bfcpState
		info.setBfcpEnable(true);
		info.setLoginName(eSpaceNumber);
		info.setLoginPwd(eSpaceWordPass);
		// 设置SRTP，安全传输协议：2：加密 3:非强制性加密(最大互通性) 1：不加密
		info.setEncryptMode(3);
		int callBandWidth = 512;
		info.setCallBandWidth(callBandWidth);
		info.setIsILBCPri(0);
		if (callBandWidth < 512)
		{
			info.setIsILBCPri(1);
		}
		// ct值设置成和总带宽一样
		info.setCT(callBandWidth);
		// // 画质优先
		// info.setVideoMode(Constants.VideoMode.VIDEO_QUALITY_MODE);
		// 流畅优先于画质
		info.setVideoMode(Constants.VideoMode.VIDEO_PROCESS_MODE);
		// 添加本地Sip端口和媒体端口
		info.setSipPort(5060);
		info.setMediaPort(10002);

		TESDK.getInstance().login(info);
	}

	@Override
	protected void onDestroy()
	{
		LogUtil.d(TAG, "onDestroy()");
		super.onDestroy();
		unRegister();
		instance = null;
	}

	/**
	 * 注册登录广播
	 */
	public void registeBroadcast()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(CustomBroadcastConst.ACTION_LOGIN_RESPONSE);
		filter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);
		filter.addAction(Constants.BROADCAST_PATH.ACTION_HOMEACTIVITY_SHOW);
		// filter.addAction(CustomBroadcastConst.ACTION_SVN_AUTHENTICATION_RESPONSE);
		// filter.addAction(HeartBeatConfig.ACTION_RECONNECT);
		registerReceiver(mReceiver, filter);
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent != null)
			{
				removeStickyBroadcast(intent);
				LogUtil.i(TAG, "[BROADCAST_ACTION]" + "|action = " + intent.getAction() + Constants.CHARACTER_MARK.VERTICAL_MARK);

				handlerBroadcastEvent(intent);
			}
		}
	};

	/**
	 * 注销登录界面的广播
	 */
	private void unRegister()
	{
		if (mReceiver != null)
		{
			try
			{
				unregisterReceiver(mReceiver);
			} catch (UnsupportedOperationException e)
			{
				LogUtil.e(TAG, e.getMessage());
			}
			mReceiver = null;
		}
	}

	/**
	 * 登录返回
	 */
	private void onLoginResponse(final Intent intent)
	{
		LogUtil.d(TAG, "onLoginResponse() result -> " + intent.getIntExtra(Resource.SERVICE_RESPONSE_RESULT, Resource.REQUEST_FAIL));
		int result = intent.getIntExtra(Resource.SERVICE_RESPONSE_RESULT, Resource.REQUEST_FAIL);
		if (result == Resource.REQUEST_OK)
		{

			CallService callService = CallService.getInstance();
			if (null != callService)
			{
				CallControl.getInstance();
				onLoginRespOK();
			} else
			{
				LogUtil.e(TAG, "callService is null !");
			}
		}
	}

	/**
	 * 登录操作
	 */
	private void onLoginRespOK()
	{
		LogUtil.d(TAG, "loginResp");

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (TESDK.getInstance().getSynLock())
				{
					LogUtil.i(TAG, "Login Success.");

					//登录成功后，存储号码
					TEDemoApp.setTENumber(eSpaceNumber);
					
					Intent intent = new Intent();
					intent.setClass(LoginActivity.this, CallActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
					return;
				}
			}
		}).start();
	}

	/**
	 * 连接成功回调
	 */
	private void onConnectToServer(final Intent intent)
	{
		boolean flag = intent.getBooleanExtra(Resource.SERVICE_RESPONSE_DATA, false);
		if (flag)
		{
			if (TESDK.getInstance().getmService() != null)
			{
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						if (TESDK.getInstance().getmService() != null)
						{
							try
							{
								// 登录成功后将是否取消过登录重置
								// isCancelLogin = false;
							} catch (Exception e)
							{
								LogUtil.e(TAG, "connect server error.");
							}
						}
					}
				}, "CheckVersion Request Thread").start();
			} else
			{
				handleResponseError(ResponseCode.COMMON_ERROR, null);
			}
		} else
		{
			// 登陆错误提示 9.10
			String loginErrorType = intent.getStringExtra(Resource.SERVICE_ERROR_DATA);
			int laveCount = intent.getIntExtra(Resource.SERVICE_ERRORLOGIN_LAVECOUNT, 0);
			LogUtil.i(TAG, "login error,loginErrorType:" + loginErrorType + ",laveCount:" + laveCount);
			if (null != loginErrorType && !StringUtil.isStringEmpty(loginErrorType))
			{
				handleRequestError(loginErrorType);
			}
		}
	}

	/**
	 * 异步处理 广播事件
	 */
	private void handlerBroadcastEvent(final Intent intent)
	{
		if (intent != null)
		{
			LogUtil.d(TAG, "handlerBroadcastEvent:" + intent.getAction());
			String action = intent.getAction();

			if (CustomBroadcastConst.ACTION_LOGIN_RESPONSE.equals(action))
			{
				LogUtil.d(TAG, "login response");
				onLoginResponse(intent);
			} else if (CustomBroadcastConst.ACTION_CONNECT_TO_SERVER.equals(action))
			{
				LogUtil.d(TAG, "connect to server");
				onConnectToServer(intent);
			} else if (Constants.BROADCAST_PATH.ACTION_HOMEACTIVITY_SHOW.equals(action))
			{
				LogUtil.d(TAG, "home activity show");
				// 收到主界面显示广播之后关闭登陆界面
				finish();
			}
		}
	}

	/**
	 * 此方法用于登陆错误 弹窗展示
	 *
	 * @param errorType 登陆错误类型
	 */
	private void handleRequestError(final String errorType)
	{
		LogUtil.w(TAG, "RequestError  errorType = " + errorType);
		// isLoading = false;
		// sendHandlerMessage(LOGINACTIVITY_MSG.ON_BACK_TO_LOGINVIEW, null);
//		TESDK.getInstance().stopSDKService();
		int errorCode = 0;
		// 目前三种类型，鉴权失败，超时，服务器错误（作为服务器连接失败处理）
		if (errorType.equals(Resource.LICENSEAPPLY_FAILED))
		{
			errorCode = Resource.REG_ERR_CODE.ERR_LICENSE_APPLY_FAILED;
		}
		if (errorType.equals(Resource.LOGIN_ACCOUNT_ERROR))
		{
			errorCode = ResponseErrorCodeHandler.LOGIN_ACCOUNT_ERROR;
		}
		if (errorType.equals(Resource.LOGIN_SERVER_ERROR))
		{
			errorCode = ResponseErrorCodeHandler.CONNECT_ERROR;
		}
		if (errorType.equals(Resource.NETWORK_INVALID))
		{
			errorCode = ResponseErrorCodeHandler.NETWORK_INVALID;
		}
		if (errorType.equals(Resource.LOGIN_SERVER_TIMEOUT))
		{
			errorCode = Resource.REQUEST_TIMEOUT;
		}
		if (errorType.equals(Resource.LOGIN_ACCOUNTNUM_OVERLIMIT))
		{
			errorCode = ResponseErrorCodeHandler.LOGIN_ACCOUNTNUM_OVERLIMIT;
		}
		if (errorType.equals(Resource.CERTIFICATE_ERROR))
		{
			errorCode = ResponseErrorCodeHandler.CERTIFICATE_ERROR;
		}
		ResponseErrorCodeHandler.handleRequestError(errorCode, LoginActivity.this);
	}

	private void handleResponseError(final ResponseCode errorCode, final String info)
	{
		LogUtil.w(TAG, "ResponseError  code = " + errorCode + Constants.CHARACTER_MARK.VERTICAL_MARK + info + Constants.CHARACTER_MARK.VERTICAL_MARK);
		// isLoading = false;
		// sendHandlerMessage(LOGINACTIVITY_MSG.ON_BACK_TO_LOGINVIEW, null);
//		TESDK.getInstance().stopSDKService();
		ResponseErrorCodeHandler.handleError(errorCode, info, LoginActivity.this, true);
	}

	private OnClickListener listener = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			int id = v.getId();
			if (id == R.id.btn_login)
			{
				login();
			} else if (id == R.id.tv_login_more)
			{
				showLoginMore();
			}
		}
	};

	protected void showLoginMore()
	{
		if (View.VISIBLE == loginMoreLayout.getVisibility())
		{
			loginMoreLayout.setVisibility(View.GONE);
			settingLayout.setVisibility(View.GONE);
			tvMore.setVisibility(View.VISIBLE);
		} else
		{
			loginMoreLayout.setVisibility(View.VISIBLE);
			settingLayout.setVisibility(View.VISIBLE);
			tvMore.setVisibility(View.GONE);
		}
	}

	public void showAlertDialog(String title, String msg, String firstBtnInfo, DialogInterface.OnClickListener firstBtnListener, String secondBtnInfo,
	                            DialogInterface.OnClickListener secondBtnListener, DialogInterface.OnDismissListener dismissListener)
	{
		if (null != instance)
		{
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void clearData()
	{

	}
}
