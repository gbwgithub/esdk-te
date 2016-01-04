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

package com.huawei.te.example.menubar;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.huawei.common.Resource;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;

/**
 * 类名称：RecallPopWindow.java 类描述：二次拨号popwindow
 */
public class RecallPopWindow extends PopupWindow
{
	private static final String TAG = RecallPopWindow.class.getSimpleName();

	/**
	 * pop 界面
	 */
	private View topWindow;

	/**
	 * 布局映射器
	 */
	private LayoutInflater inflater;

	/**
	 * 二次拨号的号码显示控件
	 */
	private EditText compRecallNum;

	/**
	 * 保存拨号盘按钮数组。
	 */
	private Button[] buttons;

	/**
	 * 0-9[0-9] *--10 #--11
	 */
	private int[] numValueInt = { KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5,
			KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_STAR, KeyEvent.KEYCODE_POUND };

	private View atchorView;

	/**
	 * inflate加载速度,需要ViewGroup
	 */
	private RelativeLayout viewGroup;

	/**
	 * 构造器
	 */
	public RecallPopWindow()
	{
		super();
	}

	/**
	 * 构造器
	 */
	public RecallPopWindow(Context context, View atchorView)
	{
		super(context);
		this.atchorView = atchorView;
		viewGroup = new RelativeLayout(context);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setBackgroundDrawable(null);
		initReCallPopWindow();
		// 修改拨号盘界面贴图
		measureView(topWindow);
		setHeight(topWindow.getMeasuredHeight());
		setWidth(topWindow.getMeasuredWidth());
	}

	/**
	 * 方法名称：initReCallPopWindow 方法描述：初始化二次拨号盘
	 */
	public void initReCallPopWindow()
	{
		// 使用ViewGroup 加快效率
		Log.i(TAG, "before inflate ");
		topWindow = inflater.inflate(R.layout.recall_pop_window, viewGroup, false);
		Log.i(TAG, "end inflate ");
		compRecallNum = (EditText) topWindow.findViewById(R.id.recallNum);

		// 设置二次拨号盘不能点击弹出拨号盘
		compRecallNum.setFocusable(false);
		compRecallNum.setClickable(false);

		compRecallNum.setCursorVisible(false);
		// 点击二次拨号盘的popwindow以外的区域，隐藏二次拨号盘
		topWindow.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				RecallPopWindow.this.dismiss();
				atchorView.setSelected(false);
				compRecallNum.setText("");
				return true;
			}
		});
		setFocusable(true);
		setOutsideTouchable(true);
		setContentView(topWindow);

		buttons = new Button[Resource.Num.TWELVE];
		buttons[Resource.Num.ZERO] = (Button) topWindow.findViewById(R.id.zero);
		buttons[Resource.Num.ONE] = (Button) topWindow.findViewById(R.id.one);
		buttons[Resource.Num.TWO] = (Button) topWindow.findViewById(R.id.two);
		buttons[Resource.Num.THREE] = (Button) topWindow.findViewById(R.id.three);
		buttons[Resource.Num.FOUR] = (Button) topWindow.findViewById(R.id.four);
		buttons[Resource.Num.FIVE] = (Button) topWindow.findViewById(R.id.five);
		buttons[Resource.Num.SIX] = (Button) topWindow.findViewById(R.id.six);
		buttons[Resource.Num.SEVEN] = (Button) topWindow.findViewById(R.id.seven);
		buttons[Resource.Num.EIGHT] = (Button) topWindow.findViewById(R.id.eight);
		buttons[Resource.Num.NINE] = (Button) topWindow.findViewById(R.id.nine);
		buttons[Resource.Num.TEN] = (Button) topWindow.findViewById(R.id.star);
		buttons[Resource.Num.ELEVEN] = (Button) topWindow.findViewById(R.id.jing);
		int lens = buttons.length;
		for (int i = 0; i < lens; i++)
		{
			setButtonListener(buttons[i], i);
		}
	}

	/**
	 * 方法描述：设置二次拨号Listener
	 */
	private void setButtonListener(final Button button, final int i)
	{
		// 点击事件
		button.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_DOWN)
				{
					// MediaUtil.getIns().playKeypadSound(v.getId());
					if (compRecallNum != null)
					{
						compRecallNum.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, numValueInt[i]));
					}
					CallControl callControl = CallControl.getInstance();
					if (callControl != null)
					{
						// DTMF带内送号， 0~9 *对应10 # 对应11
						callControl.sendDTMF(i + "");
					}
				}
				return false;
			}
		});
	}

	// 修改拨号盘界面贴图
	private void measureView(View view)
	{
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
	}
}
