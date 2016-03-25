package com.huawei.te.example.contacts;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.huawei.common.PersonalContact;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.manager.DataManager;
import com.huawei.te.example.R;
import com.huawei.te.example.contacts.adapter.AbsEspaceAdapter;
import com.huawei.te.example.contacts.adapter.ESpaceSearchAdapter;
import com.huawei.te.example.view.CodeSearchView;
import com.huawei.te.example.view.PullDownExpandListView;

/**
 * 类描述：ESpace联系人面板，包含联系人列表（普通浏览和搜索状态）和搜索footer控件
 */
public class EspaceContactPanel
{
	private static final String TAG = EspaceContactPanel.class.getSimpleName();
	
	/**
	 * 类描述：联系人面板操作接口
	 * 修改 增加列表选择器事件
	 */
	public static interface ContactPanelServer extends CodeSearchView.CodeMoveTouch
	{
		/**
		 * 长按联系人项的时候触发
		 *
		 * @param view    点击的view
		 * @param contact 联系人
		 */
		void onContactItemLongClick(View view, PersonalContact contact);

		/**
		 * 点击联系人
		 *
		 * @param view    点击的view
		 * @param contact 联系人
		 */
		void onContactItemClick(View view, PersonalContact contact, int position, AbsEspaceAdapter adapbter);

		void onLoadMoreItemClick();

		boolean isLdapEnterpriseSearching();
	}

	/**
	 * 根布局
	 */
	private View rootView;

	/**
	 * 联系人列表
	 */
	private PullDownExpandListView contactsExpListView;

	/**
	 * 最后一次点击项的位置
	 */
	private int lastClickIndex = -1;

	/**
	 * 是否有展开项
	 */
	private boolean isExp = false;

	/**
	 * 长按和快速定位事件
	 */
	private ContactPanelServer contactListViewServer;


	/**
	 * 构造函数
	 *
	 * @param rootView              根view
	 * @param contactListViewServer server
	 */
	public EspaceContactPanel(View rootView,
	                          ContactPanelServer contactListViewServer)
	{
		this.rootView = rootView;
		this.contactListViewServer = contactListViewServer;

	}


	/**
	 * 设置数据适配器
	 *
	 * @param adapter 适配器
	 */
	public void setDataSource(final AbsEspaceAdapter adapter)
	{
		lastClickIndex = -1;
		if (null == contactsExpListView)
		{
			return;
		}
		contactsExpListView.setAdapter(adapter);

		//点击联系人列表隐藏软键盘
		contactsExpListView.setOnTouchListener(new OnTouchListener()
		{

			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				rootView.findViewById(R.id.searchInputTxt).clearFocus();
				hiddenSoftInputBoard(v);
				return false;
			}
		});

		contactsExpListView.setOnGroupClickListener(new OnGroupClickListener()
		{
			@Override
			public boolean onGroupClick(ExpandableListView parent, View v,
			                            int groupPosition, long id)
			{
				PersonalContact pc = adapter.getItem(groupPosition);
				if (null != pc && null != contactListViewServer)
				{
					contactListViewServer.onContactItemClick(v, pc, groupPosition, adapter);
				}
				return true;
			}
		});
		contactsExpListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
			                        long id)
			{
				if ((adapter instanceof ESpaceSearchAdapter) && !Constants.isAnonymousAccount()
						&& position == parent.getCount() - 1 && !contactListViewServer.isLdapEnterpriseSearching())
				{
					contactListViewServer.onLoadMoreItemClick();
				}
			}
		});

		// 添加联系人长按事件
		contactsExpListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
			                               View view, int position, long id)
			{
				if ((adapter instanceof ESpaceSearchAdapter) && (position == parent.getCount() - 1))
				{
					return false;
				} else
				{
					final StringBuffer sbClick = new StringBuffer("noClick");
					if (null != view.findViewById(R.id.tx_code_name))
					{
						view.findViewById(R.id.tx_code_name).setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								sbClick.setLength(0);
							}
						});
					}

					if (0 == sbClick.length())
					{
						return true;
					}

					int itemPosition = position;

					//如果没有展开项
					if (!isExp)
					{
						PersonalContact contact = (PersonalContact) adapter
								.getGroup(itemPosition);
						if (contact != null)
						{
							contactListViewServer.onContactItemLongClick(
									view.findViewById(R.id.upLayout), contact);
						}
						return true;
					}

					//如果点击的是当前展开的项直接返回
					if (lastClickIndex + 1 == position)
					{
						return true;
					}

					//没有点击，或点击的位置大于当前点击
					if (lastClickIndex == -1
							|| (lastClickIndex > 0 && lastClickIndex > position))
					{
						itemPosition = position;
					}

					//当前点击大于上次点击+1项
					else if (position > lastClickIndex + 1)
					{
						itemPosition = position - 1;
					}

					//获取点击的联系人
					PersonalContact contact = (PersonalContact) adapter
							.getGroup(itemPosition);

					//触发事件
					if (null != contact)
					{
						contactListViewServer.onContactItemLongClick(
								view.findViewById(R.id.upLayout),
								contact);
					}
					return true;
				}

			}

		});
	}


	/**
	 * 设置listview
	 *
	 * @param contactsExpListView 要设置的listview
	 */
	public void setContactsExpListView(PullDownExpandListView contactsExpListView)
	{
		this.contactsExpListView = contactsExpListView;
	}


	/**
	 * 隐藏软键盘
	 * @param view 输入框
	 */
	private void hiddenSoftInputBoard(final View view)
	{
		if (null == contactsExpListView)
		{
			LogUtil.e(TAG, "hideSoftInputBoard Failed because contactsExpListView is null ");
			return;
		}
		final InputMethodManager inputmanager = (InputMethodManager) contactsExpListView.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (null == inputmanager || null == view)
		{
			return;
		}
		inputmanager.hideSoftInputFromWindow(view.getApplicationWindowToken(),
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	/**
	 * 选中字母后触发的动作
	 * @param code 点中的字母
	 */
	public void selectItemByCode(final String code)
	{
		if (null == contactsExpListView)
		{
			LogUtil.e(TAG, "ListView is null");
			return;
		}
		int selectIndex = DataManager.getIns().getContactIndexByCode(code);
		if (selectIndex >= 0)
		{
			contactsExpListView.setSelectedGroup(selectIndex);
			contactsExpListView.setSelected(true);
		}
	}

	/**
	 * 重置列表的选中状态
	 */
	public void resetSelection()
	{
		if (null != contactsExpListView)
		{
			contactsExpListView.setSelection(0);
		}
	}

	/**
	 * 清除数据
	 */
	public void clearData()
	{
	}

	/**
	 * 显示联系人列表
	 */
	public void showContactList()
	{
		if (null != contactsExpListView)
		{
			contactsExpListView.setVisibility(View.VISIBLE);
		}
	}


	/**
	 * 隐藏联系人列表
	 */
	public void hideContactList()
	{
		if (null != contactsExpListView)
		{
			contactsExpListView.setVisibility(View.GONE);
		}
	}


	/**
	 * 获取联系人列表
	 *
	 * @return PullDownGroupView 联系人列表
	 */
	public PullDownExpandListView getContactListView()
	{
		return contactsExpListView;
	}

	/**
	 * 关闭展开项
	 */
	public void closeExp()
	{
		//如果有展开项
		if (isExp)
		{
			if (null != contactsExpListView)
			{
				contactsExpListView.collapseGroup(lastClickIndex);
			}
			lastClickIndex = -1;
			isExp = false;
		}
	}

	/**
	 * 展开上次展开项
	 */
	public void openLastExp()
	{
	}

}
