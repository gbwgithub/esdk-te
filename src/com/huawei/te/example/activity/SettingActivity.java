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

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.huawei.esdk.te.TESDK;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.te.example.R;

public class SettingActivity extends Activity
{
	private static String TAG = SettingActivity.class.getSimpleName();
	private CheckBox errorlog;
	private RelativeLayout logSwitchLayout;
	private RelativeLayout transferProtocolLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_layout);

		errorlog = (CheckBox) findViewById(R.id.error_log);
		logSwitchLayout = (RelativeLayout) findViewById(R.id.logSwitchLayout);
		transferProtocolLayout = (RelativeLayout) findViewById(R.id.transferProtocolSwitchLayout);
		boolean isLogSwitch = LogUtil.getLogSwitch();
		errorlog.setChecked(isLogSwitch);

		logSwitchLayout.setOnClickListener(new ErrorTextCliclListener());
		transferProtocolLayout.setOnClickListener(new TransferProtocolClickListener());
	}

	private class ErrorTextCliclListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if (errorlog.isChecked())
			{
				errorlog.setChecked(false);
				TESDK.getInstance().setLogPath(false,"");
				Log.i(TAG, "Log switch:[true->false].");
			} else
			{
				errorlog.setChecked(true);
				TESDK.getInstance().setLogPath(true,"");
				Log.i(TAG, "Log switch:[false->true].");
			}
		}
	}
	
	private class TransferProtocolClickListener implements OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			if(LoginActivity.getProtocolType().equals("TLS")){
				LoginActivity.setProtocolType("UDP");
				Toast.makeText(SettingActivity.this, "传输协议已切换为 -> UDP", Toast.LENGTH_SHORT).show();
			} else {
				LoginActivity.setProtocolType("TLS");
				Toast.makeText(SettingActivity.this, "传输协议已切换为 -> TLS", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
}
