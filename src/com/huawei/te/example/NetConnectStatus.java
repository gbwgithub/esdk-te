package com.huawei.te.example;

import com.huawei.common.CustomBroadcastConst;
import com.huawei.common.LogSDK;
import com.huawei.esdk.te.data.Constants;
import com.huawei.service.eSpaceService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

/**
 * Copyright (C) 2008-2013 华为技术有限公司(Huawei Tech.Co.,Ltd)
 */
public class NetConnectStatus extends BroadcastReceiver {

	private static final String TAG = Constants.GTAG + NetConnectStatus.class.getSimpleName();

	/**
	 * 
	 * 收到设备变更通知
	 * 
	 * @param context
	 *            上下文文本
	 * @param intent
	 *            消息内容
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// 方案三
		boolean isBreak = false;

		// 方案二
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = connManager.getActiveNetworkInfo();
		if (null != ni) {
			LogSDK.d("net Change receive:" + ni.toString());
			isBreak = (State.CONNECTED == ni.getState()) ? false : true;
		} else {
			isBreak = true;
		}
		Intent netIntent = new Intent();
		netIntent.setAction(CustomBroadcastConst.DEVICE_NETSTATUSCHANGED_NOTIFY);
		netIntent.putExtra("NetUnconnected", isBreak);
		eSpaceService.postBroadcast(netIntent);
		// 在没有网络的情况下开机启动 进入主界面之后 再打开网络，由于service为null 所以发送不过去设置连接状态
		if (eSpaceService.getService() == null) {
			Log.i(TAG, "eSpaceService is null only set connected is " + !isBreak);
			// 主动设置网络状态 isConnected 为连接状态
			// TEApp.getIns().setConnected(!isBreak);
		}
	}

}
