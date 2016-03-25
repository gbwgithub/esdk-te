package com.huawei.te.example.contacts.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.common.PersonalContact;
import com.huawei.common.Resource;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.te.example.R;
import com.huawei.te.example.TEDemoApp;
import com.huawei.te.example.contacts.ContactRefreshUtil;
import com.huawei.utils.StringUtil;

import java.util.List;

/**
 * 类描述：联系人列表数据适配器抽象类
 */
public abstract class AbsEspaceAdapter extends BaseExpandableListAdapter
{
	private static final String TAG = AbsEspaceAdapter.class.getSimpleName();

	/**
	 * 显示联系人详情
	 * 回调类
	 */
	public interface ContactServer
	{
		/**
		 * 点击详情
		 *
		 * @param pc 联系人
		 */
		void onDeleteClick(PersonalContact pc);

		/**
		 * 点击编辑
		 *
		 * @param pc 联系人
		 */
		void onEditclick(PersonalContact pc);
	}

	/**
	 * 0:选择联系人界面
	 */
	public static final int TYPE_CHOOSECONTACT = 0;

	/**
	 * 是否处于会议界面
	 */
	protected boolean isInConfView = false;

	protected boolean isInSearchView;

	/**
	 * 1:联系人界面
	 */
	public static final int TYPE_CONTACT = 1;

	/**
	 * 企业联系人
	 */
	public static final int TYPE_ENTERPRISE_CONTACT = 2;

	/**
	 * 进入界面的类型{0:选择联系人界面     1:联系人界面  2:企业联系人}
	 */
	protected int viewType = 1;

	/**
	 * 上下文
	 */
	protected Context mContext;

	/**
	 * 布局加载对象
	 */
	protected LayoutInflater mInflater;

	/**
	 * 事件回调
	 */
	protected ContactServer itemClickCallBack;

	/**
	 * 电话挑选框控制类
	 */
//    private PhonePickerPanel phonePicker;

	/**
	 * 是否PAD布局
	 */
	private static final boolean ISPADLAYOUT = true;

	/**
	 * 类描述：联系人项缓存
	 */
	private static class ContactViewHolder
	{

		/**
		 * 状态
		 */
		private ImageView statePresenceImageView;

		/**
		 * 成员名称
		 */
		private TextView name;

//        /**
//         * 成员部门
//         */
//        private TextView department;

		/**
		 * 头像
		 */
		private ImageView headImg;

		/**
		 * 拨号快捷方式
		 */
		private View callShotcutImgView;

		/**
		 * 联系人选中图标
		 */
		private ImageView contactSelected;

		//begin added by cwx176935 2013/8/13 reason： 增加视频快捷图标 
		/**
		 * 视频快捷方式
		 */
		private View videoShotcutImgView;

		/**
		 * 快速提示字母
		 */
		private TextView nameCodeTxtView;

		//end added by cwx176935 2013/8/13 reason： 增加视频快捷图标 

		/**
		 * 位置标记
		 */
		private int position;

		/**
		 * get方法
		 */
		private int getPosition()
		{
			return position;
		}

		/**
		 * set方法
		 */
		private void setPosition(int position)
		{
			this.position = position;
		}

		@Override
		public String toString()
		{
			return "ContactViewHolder [statePresenceImageView="
					+ statePresenceImageView + ", name=" + name
					+ ", department=" + ", headImg=" + headImg
					+ ", callShotcutImgView=" + callShotcutImgView
					+ ", contactSelected=" + contactSelected
					+ ", videoShotcutImgView=" + videoShotcutImgView
					+ ", nameCodeTxtView=" + nameCodeTxtView + ", position="
					+ position + ']';
		}

	}

	/**
	 * 构造方法
	 *
	 * @param context              设备上下文
	 * @param itemClickCallBackVar 联系人项点击回调
	 * @param type                 进入的页面类型  0:联系人界面     1:选择联系人界面
	 */
	protected AbsEspaceAdapter(Context context,
	                           ContactServer itemClickCallBackVar, int type)
	{
		this.itemClickCallBack = itemClickCallBackVar;
		this.mContext = context;
		this.viewType = type;
		mInflater = LayoutInflater.from(context);
		//begin modify by cwx176935 reason: ANDRIOD-182 视频快捷键代码重构
//        phonePicker = new PhonePickerPanel(context);
		//end modify by cwx176935 reason: ANDRIOD-182 视频快捷键代码重构
	}

	public int getViewType()
	{
		return viewType;
	}

	public void setViewType(int viewType)
	{
		this.viewType = viewType;
	}

	/**
	 * 构造方法
	 *
	 * @param context              设备上下文
	 * @param itemClickCallBackVar 联系人项点击回调
	 */
	protected AbsEspaceAdapter(Context context, ContactServer itemClickCallBackVar)
	{
		this.itemClickCallBack = itemClickCallBackVar;

		mContext = context;

		mInflater = LayoutInflater.from(context);

		//begin modify by cwx176935 reason: ANDRIOD-182 视频快捷键代码重构
//        phonePicker = new PhonePickerPanel(context);
		//end modify by cwx176935 reason: ANDRIOD-182 视频快捷键代码重构
	}

	/**
	 * 是否为Stable Ids
	 *
	 * @return boolean 总是返回false
	 */
	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	/**
	 * 获得视图
	 *
	 * @param position    视图的位置
	 * @param convertView 视图的缓冲
	 * @param parent      视图的父视图
	 * @return 视图
	 */
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final PersonalContact pc = getItem(position);
		if (pc == null)
		{
			LogUtil.d(TAG, "convertView is null!!!");
			return convertView;
		}

		ContactViewHolder contactViewHolder = null;

		if (convertView == null || convertView.getTag() == null
				|| !(convertView.getTag() instanceof ContactViewHolder))
		{
			//根据是否PAD布局来决定加载布局 非PAD布局的时候需要加载的是phone的布局 
			//主要是防止视频通话切换到横屏的时候重新创建时用到横屏的布局
			if (ISPADLAYOUT)
			{
				convertView = mInflater.inflate(R.layout.im_team_member_layout, parent, false);
			} else
			{
				convertView = mInflater.inflate(R.layout.im_team_member_layout_phone, parent, false);
			}
			contactViewHolder = new ContactViewHolder();

			//图片动态加载
			contactViewHolder.headImg = (ImageView) convertView.findViewById(R.id.contactPhoto);
			contactViewHolder.name = (TextView) convertView.findViewById(R.id.nameTxtView);

			contactViewHolder.contactSelected = (ImageView) convertView
					.findViewById(R.id.conf_contact_select_view);

			contactViewHolder.callShotcutImgView = convertView
					.findViewById(R.id.callShortcutImgView);
			contactViewHolder.nameCodeTxtView = (TextView) convertView
					.findViewById(R.id.tx_code_name);
			contactViewHolder.statePresenceImageView = (ImageView) convertView.findViewById(R.id.contact_state);

			contactViewHolder.nameCodeTxtView.setClickable(false);
			contactViewHolder.nameCodeTxtView.setFocusable(false);
			contactViewHolder.nameCodeTxtView.setEnabled(false);
			contactViewHolder.nameCodeTxtView.setPressed(false);

			convertView.setTag(contactViewHolder);

			contactViewHolder.videoShotcutImgView = convertView.findViewById(R.id.videoShotcutImgView);

			// reason:新建联系人界面 通话按钮隐藏
		} else
		{
			contactViewHolder = (ContactViewHolder) convertView.getTag();
		}

		//非会议界面中的联系人界面
		if (!isInConfView)
		{

			contactViewHolder.callShotcutImgView
					.setVisibility(View.VISIBLE);

			contactViewHolder.videoShotcutImgView
					.setVisibility(View.VISIBLE);
		}
		//其他界面
		else
		{
			//语音按钮
			contactViewHolder.callShotcutImgView.setVisibility(View.GONE);

			//视频按钮
			contactViewHolder.videoShotcutImgView.setVisibility(View.GONE);
		}
		contactViewHolder.nameCodeTxtView.setVisibility(View.GONE);
		char groupType = getGroupTypeC(position);
		//获取分组（企业、本地）
		int groupString = 0;
		if (StringUtil.isNotEmpty(getContactGroup(position)))
		{
			groupString = Resource.ENTERPTISE.equals(getContactGroup(position)) ? R.string.enterprise_contacts : R.string.local_contacts;
		}
		if (groupType > 0 && TYPE_ENTERPRISE_CONTACT == viewType && !isInSearchView)
		{
			contactViewHolder.nameCodeTxtView.setVisibility(View.VISIBLE);
			contactViewHolder.nameCodeTxtView.setText(String.valueOf(groupType));
		} else if (groupString != 0 && isInSearchView)
		{
			contactViewHolder.nameCodeTxtView.setVisibility(View.VISIBLE);
			contactViewHolder.nameCodeTxtView.setText(groupString);
		}
		int length = 250;
		// 注释掉了关于HomeActivity NameTxtVielLength()的部分，
		// 关于联系人列表中名称较长的联系人经滚动条滚动后，联系人名称显示方式有误
//		boolean judge = null != HomeActivity.getInstance()
//				&& null != HomeActivity.getInstance().getContactsFragment()
//				&& null != HomeActivity.getInstance().getContactsFragment().getNameTxtViewLegth();
//		if (judge)
//		{
//			length = HomeActivity.getInstance().getContactsFragment().getNameTxtViewLegth().getMeasuredWidth();
//		}
//		LayoutUtil.setEndEllipse(contactViewHolder.name, pc.getName(), length);

		//end 联系人列表中名称较长的联系人经滚动条滚动后，联系人名称显示方式有误
		contactViewHolder.setPosition(position);
		contactViewHolder.statePresenceImageView.setImageDrawable(ContactRefreshUtil.getContactStateDrawable(mContext, pc, null));
		contactViewHolder.headImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.te_phone_user_default_head_rim_200_200));
		if (isInConfView)
		{
			contactViewHolder.contactSelected.setVisibility(View.VISIBLE);
			boolean isContain = ifIsSelected(pc);
			if (isContain)
			{
				contactViewHolder.contactSelected.setImageDrawable(mContext.getResources().getDrawable(TEDemoApp.isUsePadLayout()
						? R.drawable.te_mobile_contact_selected : R.drawable.te_mobile_phone_contact_selected));
			} else
			{
				contactViewHolder.contactSelected
						.setImageDrawable(mContext.getResources().getDrawable(TEDemoApp.isUsePadLayout()
								? R.drawable.te_mobile_contact_unselected : R.drawable.te_mobile_phone_contact_unselected));
			}
		} else
		{
			contactViewHolder.contactSelected.setVisibility(View.GONE);
		}
		addListener(contactViewHolder);

		return convertView;
	}

	public abstract char getGroupTypeC(int position);

	public abstract String getContactGroup(int position);

	/**
	 * Function: Addapter 的 Item中添加Listener事件
	 */
	private void addListener(final ContactViewHolder contactViewHolder)
	{
		final int positionIndex = contactViewHolder.getPosition();

		contactViewHolder.callShotcutImgView
				.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						PersonalContact contact = getItem(positionIndex);
						if (itemClickCallBack != null && contact != null)
						{
							//保存联系人
							v.setTag(contact);
//							phonePicker.doShowNumberPicker(v, false);
						}
					}
				});

		//begin added by cwx176935 2013/8/13 reason： 增加视频快捷图标 
		contactViewHolder.videoShotcutImgView
				.setOnClickListener(new View.OnClickListener()
				{

					@Override
					public void onClick(View v)
					{
						PersonalContact contact = getItem(positionIndex);
						if (itemClickCallBack != null && contact != null)
						{
							//保存联系人
							v.setTag(contact);
//							phonePicker.doShowNumberPicker(v, true);
						}
					}
				});

	}


	/**
	 * 得到一级列表的个数
	 *
	 * @return 位置
	 */
	@Override
	public abstract int getGroupCount();

	/**
	 * 得到二级菜单的个数 目前就只有一个
	 *
	 * @param groupPosition position
	 * @return 个数
	 */
	@Override
	public int getChildrenCount(int groupPosition)
	{
		return viewType;
	}


	/**
	 * 得到父列表的值
	 *
	 * @param groupPosition position
	 * @return 值
	 */
	@Override
	public Object getGroup(int groupPosition)
	{
		return getItem(groupPosition);
	}

	/**
	 * 得到一级菜单位置
	 *
	 * @param groupPosition 一级菜单位置
	 * @return id
	 */
	@Override
	public long getGroupId(int groupPosition)
	{
		return groupPosition;
	}

	/**
	 * 得到父列表项
	 *
	 * @param groupPosition 一级菜单位置
	 * @param isExpanded    是否可展开
	 * @param convertView   缓存view
	 * @param parent        父view
	 * @return 一级菜单view
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
	                         View convertView, ViewGroup parent)
	{
		return getView(groupPosition, convertView, parent);
	}

	/**
	 * 得到子列表项
	 *
	 * @param groupPosition 一级位置
	 * @param childPosition 二级位置
	 * @param isLastChild   是否最后一个
	 * @param convertView   缓冲
	 * @param parent        父视图
	 * @return 视图
	 */
	@Override
	public View getChildView(final int groupPosition, int childPosition,
	                         boolean isLastChild, View convertView, ViewGroup parent)
	{
		//如果是选择联系人界面 不需要子项
		if (TYPE_CHOOSECONTACT == viewType)
		{
			return null;
		}

		if (null == convertView)
		{
			convertView = mInflater.inflate(R.layout.contact_detail_down_layout, parent, false);
		}

		return convertView;
	}

	/**
	 * 设置联系人信息
	 *
	 * @param contactServerVar 接口
	 */
	public void setContactcontactServer(ContactServer contactServerVar)
	{
		this.itemClickCallBack = contactServerVar;
	}

	/**
	 * 子项是否可选择
	 *
	 * @param groupPosition 一级菜单位置
	 * @param childPosition 二级菜单位置
	 * @return 是否可选择
	 */
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition)
	{
		return false;
	}

	/**
	 * 获得某项联系人
	 *
	 * @param position 项的位置
	 * @return 联系人
	 */
	public abstract PersonalContact getItem(int position);

	public abstract List<PersonalContact> getItems();

	public boolean isConfView()
	{
		return isInConfView;
	}

	public void setConfView(boolean isConfView)
	{
		this.isInConfView = isConfView;
	}

	public boolean isSearchView()
	{
		return isInSearchView;
	}

	public void setSearchView(boolean isSearchView)
	{
		this.isInSearchView = isSearchView;
	}

	/**
	 * 是否选中该联系人
	 */
	private boolean ifIsSelected(PersonalContact pc)
	{
//		List<PersonalContact> selectContacts = ConfigApp.getInstance().getSelectedContactList();
//		boolean isContain = false;
//		int contactCount = selectContacts.size();
//		PersonalContact personContact = null;
//		//名称和号码相同的时候才是选中的联系人
//		for (int i = 0; i < contactCount; i++)
//		{
//			personContact = selectContacts.get(i);
//			if (pc.getNumberOne().equals(personContact.getNumberOne())
//					&& pc.getCallType() == personContact.getCallType())
//			{
//				isContain = true;
//				break;
//			}
//		}
//		return isContain;
		return false;
	}
}
