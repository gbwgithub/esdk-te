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

package com.huawei.te.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.Log;

import com.huawei.common.CustomBroadcastConst;
import com.huawei.common.LogSDK;
import com.huawei.service.eSpaceService;

public class NetConnectStatus extends BroadcastReceiver {

	private static final String TAG = NetConnectStatus.class.getSimpleName();

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
		}
	}

}
