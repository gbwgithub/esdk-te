package com.huawei.te.example;

import com.huawei.application.BaseApp;
import com.huawei.esdk.te.TESDK;

import android.app.Application;

public class TEDemoApp extends BaseApp
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		TESDK.initSDK(this);
		TESDK.getInstance().setLogPath(true, "");
	}
}
