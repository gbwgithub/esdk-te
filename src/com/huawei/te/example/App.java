package com.huawei.te.example;

import android.util.Log;

import com.huawei.esdk.te.TEApp;
import com.huawei.esdk.te.data.Constants;
import com.huawei.te.example.utils.FileUtil;

public class App extends TEApp {

	private static final String TAG = Constants.GTAG + App.class.getSimpleName();

	private static App ins;

	public static App getIns() {
		return ins;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ins = this;
	}
	
//	public void logout() {
//		if (getmService() != null) {
//			// begin modify by l00211010
//			// 离线时，已经注销过框架，导致离线无法发送消息和消息，只能等待15秒，注销时间过长 DTS2013120902928
//			if (Constant.STATUS_OFFLINE.equals(getOnlineStatus())) {
//				EventHandler.getApplicationHandler().postDelayed(logOutWaitRunnable, LOGOUT_EXIT_TIMEOUT);
//			} else {
//				EventHandler.getApplicationHandler().postDelayed(logOutWaitRunnable, EXIT_TIMEOUT);
//			}
//			// Begin Modified By wx176934 2013/08/23 Reason SIP注销启用
//			getmService().logout();
//		} else {
//			backToLogin();
//		}
//	}
//
//	/**
//	 * 注销的定时器
//	 */
//	private Runnable logOutWaitRunnable = new Runnable() {
//
//		@Override
//		public void run() {
//			doWhenLogoutSuccess();
//		}
//	};
//
//	/**
//	 * Function: 注销或者是退出程序。 执行的场景 1在注销成功后执行 2.等待注销的定时器到时执行 3.当前断网，直接执行
//	 */
//	private synchronized void doWhenLogoutSuccess() {
//		if (ConfigAccount.getIns().getLoginAccount().getExitType() != AccountLogin.EXIT) {
//			backToLogin();
//		} else {
//			exit();
//		}
//	}
//
//	/**
//	 * 退出eSpace 程序 清理缓存数据和销毁UI
//	 */
//	private void exit() {
//		// 退出时去初始化Datamanager
//		DataManager.getIns().uninit();
//		stopImServiceIfInactive();
//
//		// 退出程序注销广播和关闭app
//		unRegister();
//		// 正常退出程序
//		android.os.Process.killProcess(android.os.Process.myPid());
//	}

}
