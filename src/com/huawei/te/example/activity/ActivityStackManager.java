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

import java.util.Stack;

import com.huawei.esdk.te.util.LogUtil;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

/**
 * 类描述：界面管理堆栈
 */
public final class ActivityStackManager
{
	private static final String TAG = ActivityStackManager.class.getSimpleName();

	/**
	 * 单实例
	 */
	public static final ActivityStackManager INSTANCE = new ActivityStackManager();

	/**
	 * 获取到当前Activity栈大小, 在LoginActivity时 size 为 1
	 */
	public int getStackSize()
	{
		return activityStack.size();
	}

	/**
	 * 界面堆栈
	 */
	private Stack<BaseActivity> activityStack;

	/**
	 * 拉起通话时，显示的Activity
	 */
	private BaseActivity lastShowActivity;

	/**
	 * 构造方法
	 */
	private ActivityStackManager()
	{
		activityStack = new Stack<BaseActivity>();
	}

	/**
	 * 方法描述：加入一个界面到堆栈
	 */
	public void push(BaseActivity activity)
	{
		if (activity != null)
		{
			LogUtil.d(TAG, "ActivityTask  push :" + activity.toString());
			activityStack.push(activity);
		}
	}

	/**
	 * 退出程序， finish所有的View 退出程序时候调用
	 */
	public void finishAllViewInTask()
	{
		final int size = activityStack.size();
		BaseActivity temp = null;
		for (int i = 0; i < size; i++)
		{
			temp = activityStack.pop();
			if (temp != null)
			{
				LogUtil.i(TAG, "finishAllViewInTask() activity : " + temp.toString());
				temp.clearData(); // 清空数据
				temp.finish(); // 销毁页面
			}
		}
	}

	/**
	 * 注销，销毁除LoginActivity之外的所有Activity
	 */
	public void loginOut()
	{
		LogUtil.i(TAG, "loginOut() activity enter size:" + activityStack.size());
		BaseActivity inAc = null;
		BaseActivity temp = null;
		int size = activityStack.size();
		for (int i = 0; i < size; i++)
		{
			temp = activityStack.pop();
			if (temp != null)
			{
				if ((temp instanceof LoginActivity))
				{
					LogUtil.i(TAG, "loginOut()  activity : " + temp.toString());
					inAc = temp;
				} else
				{
					LogUtil.i(TAG, "loginOut() activity : " + temp.toString());
					temp.clearData(); // 清空数据
					temp.finish();// 销毁页面
					temp = null;
				}
			}
		}
		if (inAc != null)
		{
			activityStack.push(inAc);
		}
		LogUtil.i(TAG, "loginOut() activity leave ");
	}

	/**
	 * 获取任意一个栈中的Activity
	 * 
	 * @param index
	 *            在栈中的索引
	 * @return activity
	 */
	public Activity getActivityByIndex(int index)
	{
		return activityStack.get(index);
	}

	/**
	 * 返回栈顶的Activity
	 */
	public BaseActivity getCurrentActivity()
	{
		if (!activityStack.isEmpty())
		{
			return activityStack.lastElement();
		} else
			return null;
	}

	/**
	 * 方法名称：popupNoFinish 作者：Administrator 方法描述：指定界面出栈 不关闭界面 输入参数:@param curAc
	 * 出栈activity 输入参数:@return boolean 返回类型：void 备注：
	 */
	public boolean popup(Activity curAc)
	{
		if (curAc != null)
		{
			boolean reg = activityStack.removeElement(curAc);
			LogUtil.d(TAG, "ActivityTask  remove :" + curAc.toString() + " , result = " + reg);
			return reg;
		}
		return false;
	}

	public void stackTrace()
	{
		LogUtil.i(TAG, "stackTrace() activityStack:" + activityStack.toString());
	}

	/**
	 * 当通话结束需要拉起上次的界面
	 */
	public void whenCallEndShowLastActivity()
	{
		if (null == lastShowActivity)
		{
			return;
		}
		LogUtil.i(TAG, "show last activity:" + lastShowActivity);
		// 将堆栈里的所有lastActivity popup出来只保存一个
		Intent intent = lastShowActivity.getIntent();
		intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		lastShowActivity.startActivity(intent);
		// popup(lastShowActivity);
		lastShowActivity.finish();

		/**
		 * 由于启动模式是FLAG_ACTIVITY_BROUGHT_TO_FRONT，启动的是全新的Activity，所以这里就不需要再push，
		 * 在activity的onCreate中会push，而且这次push的会是上次的Activity造成错误
		 */
		// push(lastShowActivity);
		lastShowActivity = null;
	}

	/**
	 * 结束通话后 下次要显示的Activity
	 * 
	 * @return
	 */
	public BaseActivity getLastShowActivity()
	{
		return lastShowActivity;
	}

	/**
	 * 设置下次要显示的Activity
	 * 
	 * @param lastShowActivity
	 */
	public void setLastShowActivity(BaseActivity lastShowActivity)
	{
		this.lastShowActivity = lastShowActivity;
	}

}
