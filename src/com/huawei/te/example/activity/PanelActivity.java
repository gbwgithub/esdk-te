package com.huawei.te.example.activity;

import android.support.v4.app.FragmentActivity;

public class PanelActivity extends FragmentActivity
{
//	private static final String TAG = PanelActivity.class.getSimpleName();
//	
//	/**
//	 * 联系人Fragment
//	 */
//	private ContactsFragment contactsFragment;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_panel);
//	}
//
//
//	/**
//	 * 初始化联系人界面
//	 */
//	private void initContactFragment()
//	{
//		if (null != contactsFragment)
//		{
//			return;
//		}
//
//		contactsFragment = new ContactsFragment();
//
//		//放置Manager中
//		FragmentManager fm = this.getSupportFragmentManager();
//		FragmentTransaction ft = fm.beginTransaction();
//
//		//设置到contactAreaLayout区域
//		ft.replace(R.id.contactAreaLayout, contactsFragment);
//		try
//		{
//			ft.commitAllowingStateLoss();
//		} catch (IllegalStateException e)
//		{
//			LogUtil.d(TAG, "IllegalStateException error.");
//		}
//	}
}
