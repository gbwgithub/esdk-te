package com.huawei.te.example;

import android.app.Application;

import com.huawei.esdk.te.TESDK;

public class TEDemoApp extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		TESDK.initSDK(this);
		TESDK.getInstance().setLogPath(true, "");
	}
}
