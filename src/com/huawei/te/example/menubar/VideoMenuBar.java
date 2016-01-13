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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.te.example.R;

public class VideoMenuBar implements View.OnClickListener, Runnable
{
	private static final String TAG = VideoMenuBar.class.getSimpleName();
	/**
	 * 音频全屏、半屏切换 ，视频在popwindow里
	 */
	public static final String AUDIO_SCREEN = "audio_screen";

	/**
	 * 更多
	 */
	public static final String MORE = "more";

	/**
	 * mic
	 */
	public static final String MIC = "mic";

	/**
	 * 扬声器
	 */
	public static final String SPEAKER = "speaker";

	/**
	 * 会场列表
	 */
	public static final String CONFLIST = "conflist";

	/**
	 * 蓝牙
	 */
	public static final String BLUETOOTH = "bluetooth";

	public static final String AUDIO_VIDEO = "audio2video";

	/**
	 * 二次拨号盘
	 */
	public static final String REDIAL_BOARD = "redialBoard";

	/**
	 * 辅流共享
	 */
	public static final String SHOW_DATA = "shareData";

	/**
	 * 挂断电话
	 */
	public static final String HANG_UP = "hangup";

	private static final String ISGONE = "gone";
	/**
	 * 休眠时间
	 */
	private static final int DURATION = 100;

	/**
	 * 开始启动线程的时间
	 */
	private long startTime = 0;

	/**
	 * 事件回调类
	 */
	private MenuItemServer itemServer;

	/**
	 * 菜单项
	 */
	private Map<String, MenuBarItem> items = new HashMap<String, MenuBarItem>(10);

	/**
	 * 菜单项
	 */
	private List<MenuBarItem> itemsList = new ArrayList<MenuBarItem>(10);

	/**
	 * 同步的view
	 */
	private List<View> linkViews = new ArrayList<View>(10);

	/**
	 * 根布局
	 */
	private View rootView;

	/**
	 * 菜单
	 */
	private View menuBar;

	// /**
	// * 菜单项，仅用于手机时，更换背景
	// */
	// private View menuBarItems;

	/**
	 * 是否自动隐藏
	 */
	private boolean autoHidden;

	/**
	 * 是否需要弹出对话框
	 */
	private boolean isNeedShowDialog = true;
	/**
	 * 线程名
	 */
	private static final String THREAD_NAME = "hiddenThread";

	private boolean threadStart = false;
	private Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			dismiss();
		}
	};

	/**
	 * 构造函数
	 * 
	 * @param rootViewVar
	 *            根不具
	 */
	public VideoMenuBar(View rootViewVar)
	{
		this.rootView = rootViewVar;
		menuBar = rootView.findViewById(R.id.menu_bar);
		initListItemView();
		menuBar.setVisibility(View.GONE);

		// menuBarItems = rootView.findViewById(R.id.menu_bar_items);

		// 动态加载底部的图片
		// View menuBarBottom =
		// (View)rootView.findViewById(R.id.menu_bar_bottom);
		// menuBarBottom.setBackgroundDrawable(new
		// BitmapDrawable(ImageResourceUtil.getIns()
		// .readBitMap(rootView.getContext()
		// , R.drawable.te_state_menu_bottom)));
	}

	/**
	 * 获得回调事件
	 * 
	 * @return 回调事件
	 */
	public MenuItemServer getItemServer()
	{
		return itemServer;
	}

	/**
	 * 设置回调事件
	 * 
	 * @param itemServer
	 *            菜单项的触发事件
	 */
	public void setItemServer(MenuItemServer itemServer)
	{
		this.itemServer = itemServer;
	}

	/**
	 * 设置menuItems背景，仅用于手机
	 * 
	 * @param itemServer
	 *            菜单项的触发事件
	 */
	// public void setItemsBackground(int visible, Boolean isEraphone)
	// {
	// if (null == menuBarItems)
	// {
	// return;
	// }
	//
	// 如果是耳机，背景只有3分割
	// if (isEraphone)
	// {
	// menuBarItems.setBackgroundDrawable(new
	// BitmapDrawable(ImageResourceUtil.getIns().readBitMap(rootView.getContext()
	// , R.drawable.te_phone_menubar_audio_less_background)));
	// return;
	// }
	//
	// if (visible == View.VISIBLE)
	// {
	// menuBarItems.setBackgroundDrawable(new
	// BitmapDrawable(ImageResourceUtil.getIns().readBitMap(rootView.getContext()
	// , R.drawable.te_phone_menubar_audio_background)));
	// }
	// }

	/**
	 * 创建菜单
	 * 
	 * @return 菜单列表
	 */
	protected Map<String, MenuBarItem> initListItemView()
	{
		MenuBarItem menuItem = null;

		View audioScreen = rootView.findViewById(R.id.audio_screen);
		audioScreen.setOnClickListener(this);
		menuItem = new MenuBarItem(audioScreen, rootView.findViewById(R.id.line_audio_screen));
		itemsList.add(menuItem);
		items.put(AUDIO_SCREEN, menuItem);

		View more = rootView.findViewById(R.id.more);
		more.setOnClickListener(this);
		menuItem = new MenuBarItem(more, rootView.findViewById(R.id.line_more));
		itemsList.add(menuItem);
		items.put(MORE, menuItem);

		View videoMic = rootView.findViewById(R.id.video_mic);
		videoMic.setOnClickListener(this);
		menuItem = new MenuBarItem(videoMic, rootView.findViewById(R.id.line_video_mic));
		itemsList.add(menuItem);
		items.put(MIC, menuItem);

		View videoSpeaker = rootView.findViewById(R.id.video_speaker);
		videoSpeaker.setOnClickListener(this);
		menuItem = new MenuBarItem(videoSpeaker, rootView.findViewById(R.id.line_video_speaker));
		itemsList.add(menuItem);
		items.put(SPEAKER, menuItem);

		// 显示会场列表item
		View videoConfList = rootView.findViewById(R.id.video_conf_list);
		videoConfList.setOnClickListener(this);
		menuItem = new MenuBarItem(videoConfList, rootView.findViewById(R.id.line_video_conf_list));
		itemsList.add(menuItem);
		items.put(CONFLIST, menuItem);

		View voiceBluetooth = rootView.findViewById(R.id.voice_bluetooth);
		voiceBluetooth.setOnClickListener(this);
		menuItem = new MenuBarItem(voiceBluetooth, rootView.findViewById(R.id.line_bluetooth));
		itemsList.add(menuItem);
		items.put(BLUETOOTH, menuItem);

		View audioSwitchVideo = rootView.findViewById(R.id.audio_switch_video);
		audioSwitchVideo.setOnClickListener(this);
		menuItem = new MenuBarItem(audioSwitchVideo, rootView.findViewById(R.id.line_audio_switch_video));
		itemsList.add(menuItem);
		items.put(AUDIO_VIDEO, menuItem);

		View shareData = rootView.findViewById(R.id.share_data);
		shareData.setOnClickListener(this);
		menuItem = new MenuBarItem(shareData, rootView.findViewById(R.id.line_share_data));
		itemsList.add(menuItem);
		items.put(SHOW_DATA, menuItem);

		View redialBoard = rootView.findViewById(R.id.redial_board);
		redialBoard.setOnClickListener(this);
		menuItem = new MenuBarItem(redialBoard, rootView.findViewById(R.id.line_redial_board));
		itemsList.add(menuItem);
		items.put(REDIAL_BOARD, menuItem);

		View videoHangup = rootView.findViewById(R.id.video_hangup);
		videoHangup.setOnClickListener(this);
		menuItem = new MenuBarItem(videoHangup, null);
		itemsList.add(menuItem);
		items.put(HANG_UP, menuItem);

		resetAllMenuItems();
		return items;
	}

	/**
	 * 获得菜单的某一项
	 * 
	 * @param item
	 *            项名称
	 * @return 菜单项
	 */
	public View getMenuItems(String item)
	{
		LogUtil.d(TAG, "getMenuItems()");
		if (null == items)
		{
			LogUtil.d(TAG, "item is null");
		} else
		{
			LogUtil.d(TAG, "test items it not null");
		}
		if (null == items.get(item))
		{
			LogUtil.d(TAG, "items.get(item) is null" + "item :" + item);
		} else
		{
			LogUtil.d(TAG, "items.get(item) is not null" + "item :" + item);
		}
		return items.get(item).getItem();
	}

	/**
	 * 设置item的可见性
	 * 
	 * @param item
	 *            项名称
	 * @param visible
	 *            View.GONE不可见
	 */
	public void setMenuItemVisible(String item, int visible)
	{
		if (null == items.get(item))
		{
			return;
		}
		items.get(item).setItemVisible(visible);
	}

	// /**
	// * 设置item的背景是否显示，主要针对音频通话时的情况
	// * @param item 项名称
	// * @param visible View.GONE不可见
	// * 2014-4-16 v1.0.0 l00220604 create
	// */
	// public void setMenuItemBackground(String item, int visible)
	// {
	// if (null == items.get(item))
	// {
	// return;
	// }
	//
	// if (visible == View.VISIBLE)
	// {
	// items.get(item).setItemBackground(R.drawable.te_phone_menu_background_left);
	// }
	// }

	public void isVideoAudioGONE()
	{
		if (null != items.get(AUDIO_VIDEO).getItemLine() && ISGONE.equals(items.get(AUDIO_VIDEO).getItemLine().getTag()))
		{
			items.get(AUDIO_VIDEO).setItemVisible(View.GONE);
		}
	}

	/**
	 * @param itemName
	 * @param visible
	 */
	public void setItemLineVisibility(String itemName, int visibility)
	{
		if (null != items.get(itemName).getItemLine())
		{
			items.get(itemName).getItemLine().setVisibility(visibility);
		}
	}

	/**
	 * 获得每项的图片View
	 * 
	 * @param item
	 *            图片view
	 */
	public ImageView getMenuItemsImg(String item)
	{
		if (null == items.get(item))
		{
			LogUtil.d(TAG, "items.get(item) is null, item:" + item);
		} else
		{
			LogUtil.d(TAG, "items.get(item) is not null, item:" + item);
		}
		return items.get(item).getItemImg();
	}

	/**
	 * 恢复隐藏计时时间
	 */
	public void coverTime()
	{
		startTime = System.currentTimeMillis();
	}

	/**
	 * 菜单点击动作
	 * 
	 * @param viewVar
	 *            点击的菜单
	 */
	@Override
	public void onClick(View viewVar)
	{
		startTime = System.currentTimeMillis();
		if (null == itemServer)
		{
			return;
		}
		int id = viewVar.getId();
		if (id == R.id.more)
		{
			// 菜单栏消失弹出框不消失
			if (View.VISIBLE == menuBar.getVisibility())
			{
				itemServer.showMoreOpre(viewVar);
			}
		} else if (id == R.id.video_mic)
		{
			itemServer.closeMIC(getMenuItemsImg(MIC));
			itemServer.setMicClose(!Constants.CLICK.equals(getMenuItemsImg(MIC)));
		} else if (id == R.id.video_speaker)
		{
			itemServer.closeSpeaker(getMenuItemsImg(SPEAKER));
			itemServer.setSpeakerClose(!Constants.CLICK.equals(getMenuItemsImg(SPEAKER)));
		} else if (id == R.id.voice_bluetooth)
		{
			itemServer.blueToothClick(getMenuItemsImg(BLUETOOTH));
		} else if (id == R.id.audio_switch_video)
		{
			itemServer.videoToAudio(viewVar);
		} else if (id == R.id.redial_board)
		{
			itemServer.audioRecall(viewVar);
		} else if (id == R.id.share_data)
		{
			itemServer.shareFile();
			if (View.VISIBLE == menuBar.getVisibility())
			{
				itemServer.showShareMorePopWindow(viewVar);
			}
		} else if (id == R.id.video_hangup)
		{
			endCall(viewVar);
		} else if (id == R.id.audio_screen)
		{
			itemServer.setAudioScreen(getMenuItemsImg(AUDIO_SCREEN));
		} else if (id == R.id.video_conf_list)
		{
			itemServer.showConfList();
		} else
		{
			endCall(viewVar);
		}
	}

	/**
	 * 重置所有菜单
	 */
	public void resetAllMenuItems()
	{
		MenuBarItem item = null;
		int itemSize = itemsList.size();
		for (int i = 0; i < itemSize; i++)
		{
			item = itemsList.get(i);
			item.getItem().setVisibility(View.VISIBLE);
			if (null != item.getItemImg())
			{
				item.getItemImg().setEnabled(true);
				item.getItemImg().getDrawable().setAlpha(255);
				item.getItemImg().setVisibility(View.VISIBLE);
			}
			if (null != item.getItemLine() && ISGONE.equals(item.getItemLine().getTag()))
			{
				item.getItem().setVisibility(View.GONE);
			}
		}
	}

	// 音视频通话页面中通话时间显示不对
	/**
	 * 重置所有的view的tag
	 */
	public void resetAllMenuTag()
	{
		MenuBarItem item = null;
		int itemSize = itemsList.size();
		for (int i = 0; i < itemSize; i++)
		{
			item = itemsList.get(i);
			item.getItem().setTag("");
			if (null != item.getItemImg())
			{
				item.getItemImg().setTag("");
			}
		}
	}

	/**
	 * 挂断电话方法
	 * 
	 * @param view
	 *            点击的view
	 */
	private void endCall(View view)
	{
		// 若已挂断则不需要再弹出对话框
		// pad视频呼出，对端无响应，本端“挂机”与“取消”按钮无响应
		if (!isNeedShowDialog)
		{
			LogUtil.i(TAG, "No need to show confirm dialog because the call is ended ,return here!");
			isNeedShowDialog = true;
			return;
		}

		// 挂断弹出二次对话框 修改为封装好的dialog
		// DialogClickListener dialogClickListener = new DialogClickListener();
		// ((BaseActivity)
		// view.getContext()).showAlertDialogTwo(view.getContext()
		// .getString(R.string.hangup), view
		// .getContext().getString(R.string.sure_hangup), view
		// .getContext().getString(R.string.ok),
		// //确认按钮点击事件
		// dialogClickListener, view.getContext().getString(R.string.cancel)
		//
		// //取消按钮点击事件
		// , dialogClickListener, null);

		// TO invoke
		itemServer.endVideoCall();
	}

	/**
	 * 是否显示对话框
	 * 
	 * @return true 显示
	 */
	public boolean isNeedShow()
	{
		return isNeedShowDialog;
	}

	/**
	 * 是否显示对话框
	 * 
	 * @param isNeedShow
	 *            true 显示
	 */
	public void setNeedShow(boolean isNeedShow)
	{
		this.isNeedShowDialog = isNeedShow;
	}

	/**
	 * 得到菜单长度
	 * 
	 * @return 菜单长度
	 */
	public int getMenuWidth()
	{
		return menuBar.getMeasuredWidth();
	}

	/**
	 * 得到菜单栏
	 * 
	 * @return 菜单栏
	 */
	public View getMenuBar()
	{
		return menuBar;
	}

	/**
	 * 自动隐藏线程
	 */
	@Override
	public void run()
	{
		// 5s内不消失
		long timeSpacing = System.currentTimeMillis() - startTime;
		try
		{
			while (timeSpacing < 5000)
			{
				sleepThread();
				timeSpacing = System.currentTimeMillis() - startTime;
			}
		} catch (Exception e)
		{
			LogUtil.d(TAG, "AbsMenuBar thread error");
		}
		handler.sendEmptyMessage(0);
		threadStart = false;

	}

	public void dismiss()
	{
		if (!autoHidden)
		{
			return;
		}
		// 对方挂断电话时取消Popupwindow
		itemServer.dismissMorePopWindow();

		if (null != linkViews)
		{
			View view = null;
			int size = linkViews.size();
			for (int i = 0; i < size; i++)
			{
				view = linkViews.get(i);
				view.setAnimation(null);
				// 视频信息按钮在菜单栏消失的情况下不改变位置
				// 设置菜单栏消失为Invisible 占位置而不显示
				view.setVisibility(View.GONE);
			}
		}
		menuBar.setVisibility(View.GONE);
	}

	/**
	 * 显示
	 */
	public void showAndGone()
	{
		// 点击显示变隐藏，隐藏变显示
		if (View.VISIBLE == menuBar.getVisibility())
		{
			// 如果是显示的则隐藏
			startTime += 5000;
			menuBar.setAnimation(null);
			dismiss();
			return;
		}

		startTime = System.currentTimeMillis();
		animationIn(menuBar);
		menuBar.setVisibility(View.VISIBLE);
		LogUtil.d(TAG, "menubar show()");
		View view = null;
		int linkSize = linkViews.size();
		for (int i = 0; i < linkSize; i++)
		{
			view = linkViews.get(i);
			if (null != view.getAnimation())
			{
				view.getAnimation().startNow();
				continue;
			}
			view.setVisibility(View.VISIBLE);
			animationIn(view);

		}

		startTime = System.currentTimeMillis();
		if (!threadStart)
		{
			new Thread(this, THREAD_NAME).start();
			threadStart = true;
		}
	}

	/**
	 * 同步view
	 * 
	 * @param view
	 *            同步的view
	 */
	public void addLink(View view)
	{
		if (null != view)
		{
			if (null == linkViews)
			{
				linkViews = new ArrayList<View>(10);
			}
			if (!linkViews.contains(view))
			{
				linkViews.add(view);
			}
		}
	}

	/**
	 * 解除所有绑定的view
	 */
	public void clearAllLinkedView()
	{
		linkViews.clear();
	}

	/**
	 * 解除绑定的view
	 * 
	 * @param view
	 *            要解除的view
	 */
	public void removeLinkView(View view)
	{
		if (null == linkViews || !linkViews.contains(view))
		{
			return;
		}
		view.clearAnimation();
		view.setAnimation(null);
		linkViews.remove(view);
	}

	/**
	 * 是否可以自动隐藏
	 * 
	 * @return true可以，false不可以
	 */
	public boolean isAutoHidden()
	{
		return autoHidden;
	}

	/**
	 * 设置是否可以自动隐藏
	 * 
	 * @param autoHidden
	 *            true可以，false不可以
	 */
	public void setAutoHidden(boolean autoHidden)
	{
		this.autoHidden = autoHidden;
	}

	/**
	 * 进入 动画
	 */
	protected void animationIn(final View view)
	{
		AlphaAnimation anim = null;
		anim = new AlphaAnimation(0f, 1.0f);
		anim.setDuration(500);
		anim.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				if (linkViews.contains(view) || view == menuBar)
				{
					view.setVisibility(View.VISIBLE);
				}
				view.postInvalidate();
			}
		});

		view.startAnimation(anim);
	}

	// 话机视频通话一段时间后，注销再重新登录，出现异常退出现象
	/**
	 * 清除数据
	 */
	public void clearData()
	{
		if (null != itemsList)
		{
			itemsList.clear();
		}
		itemsList = null;
		if (null != items)
		{
			items.clear();
		}
		items = null;
		if (null != linkViews)
		{
			linkViews.clear();
		}
		linkViews = null;
		handler = null;
	}

	/**
	 * 菜单项
	 */
	private static final class MenuBarItem
	{
		private View item;
		private ImageView itemImg;
		private View lineView;

		private MenuBarItem(View itemVar, View lineViewVar)
		{
			if (null == itemVar)
			{
				return;
			}
			this.item = itemVar;
			if (itemVar instanceof ViewGroup)
			{
				itemImg = (ImageView) ((ViewGroup) item).getChildAt(0);
			}
			this.lineView = lineViewVar;
		}

		/**
		 * 设置item的可见性
		 */
		public void setItemVisible(int visible)
		{
			if (null != item)
			{
				item.setVisibility(visible);
			}
			if (null != lineView)
			{
				lineView.setVisibility(visible);
			}
		}

		// /**
		// * 设置item的背景
		// */
		// public void setItemBackground(int resid)
		// {
		// if (null != item)
		// {
		// item.setBackgroundDrawable(new
		// BitmapDrawable(ImageResourceUtil.getIns().readBitMap(rootView.getContext(),
		// resid)));
		// }
		// }

		/**
		 * 得到菜单项
		 * 
		 * @return 菜单项
		 */
		public View getItem()
		{
			return item;
		}

		/**
		 * 得到菜单项的图片
		 * 
		 * @return 菜单项图片
		 */
		public ImageView getItemImg()
		{
			return itemImg;
		}

		public View getItemLine()
		{
			return lineView;
		}
	}

	private class DialogClickListener implements DialogInterface.OnClickListener
	{

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			// TODO Auto-generated method stub
			if (DialogInterface.BUTTON_POSITIVE == which)
			{
				itemServer.endVideoCall();
			}
		}

	}

	private void sleepThread()
	{
		try
		{
			Thread.sleep(DURATION);
		} catch (InterruptedException e)
		{
			LogUtil.e(TAG, "thread sleep error.");
		}
	}

	public interface MenuItemServer
	{

		/**
		 * 显示更多
		 */
		void showMoreOpre(View view);

		void showShareMorePopWindow(View view);

		/**
		 * 关闭本地MIC
		 */
		void closeMIC(ImageView view);

		/**
		 * 关闭扬声器
		 */
		void closeSpeaker(ImageView view);

		/**
		 * 蓝牙
		 */
		void blueToothClick(ImageView view);

		/**
		 * 摄像头切换
		 */
		void switchCamere(View view);

		/**
		 * 视频转语音
		 */
		void videoToAudio(View view);

		/**
		 * 二次拨号盘图标点击
		 */
		void audioRecall(View view);

		/**
		 * 共享
		 */
		void shareFile();

		/**
		 * 退出
		 */
		void endVideoCall();

		/**
		 * 清除弹出的PopupWindow
		 */
		void dismissPopupWindow();

		/**
		 * 清除弹出的PopupWindow
		 */
		void dismissMorePopWindow();

		/**
		 * 设置音频模式下是否是全屏
		 */
		void setAudioScreen(ImageView view);

		/**
		 * 设置是否关闭麦克风
		 */
		void setMicClose(boolean isClose);

		/**
		 * 设置是否关闭扬声器
		 */
		void setSpeakerClose(boolean isClose);

		/**
		 * 查看会场列表
		 */
		void showConfList();
	}

}
