package com.huawei.te.example.contacts;

/**
 * 项目名称：TEWorkspace
 * 类描述：联系人Fragment
 * 创建人：gWX289620
 * 创建时间：2016/2/25 14:33
 */
public class ContactsFragment
{
//public class ContactsFragment extends Fragment implements ContactStatePresenceListener, OnTouchListener,
//		ContactListener, ContactListener.EnterpriseUpdateListener, LdapListener
//{
//	//提供以下接口，以解决，正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//
//	/**
//	 * 联系人正在加载接口
//	 */
//	public interface ContactLoadState
//	{
//		/**
//		 * 正在加载
//		 */
//		void isLoading();
//
//		/**
//		 * 加载完成
//		 */
//		void loadEnd(boolean isLocal);
//	}
//
//	/**
//	 * 弹框的类型
//	 */
//	public enum TYPE
//	{
//		/**
//		 * 新建联系人
//		 */
//		CREATE_NEW_CONTACT,
//
//		/**
//		 * 导入联系人
//		 */
//		IMPORT_CONTACT,
//
//		/**
//		 * 导出联系人
//		 */
//		EXPORT_CONTACT
//	}
//
//	/**********************界面***************************/
//
//	/**
//	 * 根布局
//	 */
//	private View rootView;
//
//	/**
//	 * 联系人主界面
//	 */
//	private View contactsView;
//
//	/**
//	 * 联系人列表
//	 */
//	private View contactsListContain;
//
//	//用于解决，正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//	/**
//	 * 联系人加载回调
//	 */
//	private List<ContactLoadState> localLoadStates;
//
//	/**
//	 * 企业联系人加载回调
//	 */
//	private List<ContactLoadState> enterpriseLoadStates;
//
//	/**
//	 * 显示号码的长度
//	 */
//	private TextView textViewLength;
//
//	/**
//	 * 显示名称的长度
//	 */
//	private TextView nameTxtViewLegth;
//
//	// 用于 TQE清零
//	private final Object codeLock = new Object();
//
//	private TextView networkDisconnect;
//
//	/******************选项卡数据*****************************/
//	/**
//	 * 联系人中的选项卡
//	 */
//	private ViewPager contactViewPage;
//
//	/**
//	 * 选项卡的数据
//	 */
//	private List<View> viewPageData;
//
//	/**
//	 * 本地选项卡标题
//	 */
//	private View tabLocalTitle;
//
//	private View tabTitleContact;
//
//	private View recentTabLayout;
//
//	/**
//	 * 搜索界面编辑框
//	 */
//	private EditText searchContactsViewTxt;
//
//	/**
//	 * 搜索界面取消按钮
//	 */
//	private View searchContactsCancel;
//
//	/**
//	 * 本地联系人卡片布局
//	 */
//	private View localContactsView;
//
//	/**
//	 * 企业联系人卡片布局
//	 */
//	private View enterpriseView;
//
//	/**
//	 * 无本地联系人图标
//	 */
//	private View noLocalContactView;
//
//	/**
//	 * 无企业联系人图标
//	 */
//	private View noEnterpriseContactView;
//
//	/**
//	 * 搜索view
//	 */
//	private View searchInputView;
//
//	/**
//	 * 当前显示的卡片
//	 */
//	private int curShowTab;
//
//	/**
//	 * 企业联系人是否在加载
//	 */
//	private boolean isEnterpriseLoading = false;
//
//	/**
//	 * 第一次加载企业联系人的定时器
//	 */
//	private Timer enterpriseTimer;
//
//	//需要定义全局变量，在销毁Fragment的时候需要停止异步任务
//	/**
//	 * 本地联系人数据加载异步任务
//	 */
//	private AsyncTask<String, Void, Boolean> localLoadTask;
//
//	/**
//	 * 企业联系人数据加载异步任务
//	 */
//	private AsyncTask<String, Void, Boolean> enterpriseLoadTask;
//	/**********************************************/
//
//	/**
//	 * 联系人Fragment 选中入会联系人集合
//	 */
//	private List<PersonalContact> confragSelectedContacts = new ArrayList<PersonalContact>(0);
//
//	/**
//	 * LDAP搜索联系人集合
//	 */
//	private List<PersonalContact> ldapSearchResultList = new ArrayList<PersonalContact>(0);
//
//	/**
//	 * 点击查找企业和上拉刷新的footerview
//	 */
//	private View footView;
//
//	/**
//	 * 点击查找企业通讯录的TextView
//	 */
//	private TextView searchEnterprise;
//
//	/**
//	 * 上拉刷新的View
//	 */
//	private View footLoading;
//
//	/**
//	 * Ldap搜索页，一个关键字可能有多页
//	 */
//	private int iLdapSearchPage = 0;
//
//	/**
//	 * 搜索事件SeqNo
//	 */
//	private int iLdapSearchSeqNo = -1;
//
//	/**
//	 * 是否正在加载ldap企业联系人
//	 */
//	private boolean bIsLoadingLdapEnterprise = false;
//
//	/**
//	 * 是否搜索查询到最后一页
//	 */
//	private boolean bLastPage = false;
//
//	/**
//	 * 是否搜索LDAP企业联系人
//	 */
//	private boolean searchLdapEnterprise = false;
//
//	public boolean isSearchLdapEnterprise()
//	{
//		return searchLdapEnterprise;
//	}
//
//	public void setSearchLdapEnterprise(boolean searchLdapEnterprise)
//	{
//		this.searchLdapEnterprise = searchLdapEnterprise;
//	}
//
//	/**
//	 * @param savedInstanceState 下次启动时携带的数据
//	 */
//	@Override
//	public void onCreate(Bundle savedInstanceState)
//	{
//		super.onCreate(savedInstanceState);
//	}
//
//	/**
//	 * 加载xml布局
//	 *
//	 * @param inflater           xml加载器
//	 * @param container          父容器
//	 * @param savedInstanceState 下次启动时携带的数据
//	 */
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//	                         Bundle savedInstanceState)
//	{
//		//TQE清零
//		super.onCreateView(inflater, container, savedInstanceState);
//		rootView = inflater.inflate(R.layout.contacts_layout, container, false);
//		rootView.setOnTouchListener(this);
//
//		//选中入会Adapter
//		selectedContactsAdapter = new SelectedContactAdapter(inflater, null, rootView.getContext());
//		//设置数据源
//		selectedContactsAdapter.setConfSelectedContactList(confragSelectedContacts);
//		if (null != HomeActivity.getInstance().getSelectedContactsGridView())
//		{
//			HomeActivity.getInstance().getSelectedContactsGridView().setAdapter(selectedContactsAdapter);
//		}
//		initComp(inflater);
//
//		initData();
//
//		DataManager.getIns().regContactEventListen(this);
//		DataManager.getIns().regContactStateListen(this);
//		DataManager.getIns().regEnterpriseUpdateListen(this);
//
//		if (ConfigApp.getInstance().isLdapEnterprise())
//		{
//			LdapManager.getIns().regLdapEventListen(this);
//		}
//		return rootView;
//	}
//
//	/**
//	 * activity状态
//	 *
//	 * @param outState A mapping from String values to various Parcelable types
//	 */
//	@Override
//	public void onSaveInstanceState(Bundle outState)
//	{
//		super.onSaveInstanceState(outState);
//	}
//
//	/**
//	 * 加载控件
//	 */
//	private void initComp(LayoutInflater inflater)
//	{
//		textViewLength = (TextView) rootView.findViewById(R.id.et_text_length);
//		nameTxtViewLegth = (TextView) rootView.findViewById(R.id.nameTxtView_show);
//		contactsView = rootView.findViewById(R.id.contactsLayout);
//		//begin add by wx183960 reason:点击隐藏软键盘 DTS2013120907881
//		contactsView.setOnClickListener(new OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				hiddenSoftInputBoard(v);
//			}
//		});
//
//		//======================卡片控件=============================
//		recentTabLayout = rootView.findViewById(R.id.recenttablayout);
//		tabTitleContact = rootView.findViewById(R.id.tab_contact);
//		tabLocalTitle = rootView.findViewById(R.id.tab_local_contact);
//		tabEnterpriseTitle = rootView.findViewById(R.id.tab_enterprise_contact);
//
//		footView = inflater.inflate(R.layout.loadmore_footer, null);
//
//		searchEnterprise = (TextView) footView.findViewById(R.id.searchmore_text);
//		footLoading = footView.findViewById(R.id.foot_loading);
//
//		contactViewPage = (ViewPager) rootView.findViewById(R.id.contact_viewpager);
//		viewPageData = getUIObject(inflater);
//		networkDisconnect = (TextView) localContactsView.findViewById(R.id.tip_view);
//		if (networkDisconnect != null)
//		{
//			if (!DeviceManager.isNetworkAvailable(getActivity()))
//			//初始化时 如果网络不可用 则显示网络不可用提示 手机布局时联系人界面
//			{
//				networkDisconnect.setVisibility(View.VISIBLE);
//			}
//		}
//		searchInputView = localContactsView.findViewById(R.id.searchInputView);
//		//begin modified by c00292094 reason:fortify清零，在调引用对象前判空
//		int viewPageDataSize = 0;
//		if (null != viewPageData)
//		{
//			viewPageDataSize = viewPageData.size();
//			//end modified by c00292094 reason:fortify清零，在调引用对象前判空
//			for (int i = 0; i < viewPageDataSize; i++)
//			{
//				((ImageView) viewPageData.get(i).findViewById(R.id.searchImgView))
//						.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.te_input_search_btn));
//			}
//		}
//		HomeActivity.measureView(searchInputView);
//		noLocalContactView = localContactsView.findViewById(R.id.contactsTipLayout);
//		noEnterpriseContactView = enterpriseView.findViewById(R.id.contactsEnterpriseTipLayout);
//
//		codeSearchView = (CodeSearchView) enterpriseView.findViewById(R.id.code_view);
//		codeSearchView.setCodeMoveTouchServer(this);
//		tabFocusDrawable = rootView.getResources().getDrawable(R.drawable.te_fragment_tag_bg_focus);
//		tabNormalDrawable = rootView.getResources().getDrawable(R.drawable.te_fragment_tag_bg_normal);
//	}
//
//	/**
//	 * 方法名称：initData
//	 * 作者：LiQiao
//	 * 方法描述：初始化数据
//	 */
//	private void initData()
//	{
//		Context context = getActivity();
//		contactDetailPanel = new ContactDetailPanel((BaseActivity) getActivity(), this, rootView.findViewById(R.id.new_contact));
//
//		contactsListContain = rootView.findViewById(R.id.contacts_list_contain);
//		if (ConfigApp.getInstance().isUsePadLayout())
//		{
//			if (ConfigApp.getInstance().isLdapEnterprise())
//			{
//				rootView.findViewById(R.id.tabTitle).setVisibility(View.VISIBLE);
//				contactsListContain.setVisibility(View.GONE);
//			}
//			tabTitle = new TabTitle(rootView.findViewById(R.id.tabTitle), this);
//			tabTitleLdap = new TabTitle(rootView.findViewById(R.id.contacts_list_contain), this);
//		} else
//		{
//			tabTitle = new TabTitle(rootView.findViewById(R.id.tabTitle), this);
//			if (ConfigApp.getInstance().isLdapEnterprise())
//			{
//				contactsListContain.setVisibility(View.GONE);
//			}
//		}
//		tabTitle.reset();
//
//		searchContactsList = (PullDownExpandListView) rootView.findViewById(R.id.search_contacts_list);
//		//匿名登录时不显示企业地址本
//		if (Constant.isAnonymousAccount())
//		{
//			tabLocalTitle.setVisibility(View.INVISIBLE);
//			tabEnterpriseTitle.setVisibility(View.INVISIBLE);
//			if (null != tabTitleContact)
//			{
//				tabTitleContact.setVisibility(View.VISIBLE);
//			}
//			if (!ConfigApp.getInstance().isUsePadLayout())
//			{
//				recentTabLayout.setVisibility(View.GONE);
//			}
//		}
//		searchContactsView = rootView.findViewById(R.id.search_contacts_input);
//		searchContactsViewTxt = (EditText) rootView.findViewById(R.id.searchInputTxt);
//
//		searchContactsCancel = rootView.findViewById(R.id.search_contacts_cancel);
//		searchContactsCancel.setOnClickListener(new OnClickListener()
//		{
//
//			@Override
//			public void onClick(View v)
//			{
//				//取消按钮点击
////                tabTitle.setLabelSwitchEnable();
//				if (!eContactAdapter.isConfView() && curShowTab == 0)
//				{
//					setTabTitleVisibility(true);
//				}
//				setSearchLdapEnterprise(false);
//				iLdapSearchSeqNo = -1;
//				iLdapSearchPage = 0;
//				ldapSearchResultList.clear();
//				showSearchView(false);
//				localContactsView.setVisibility(View.VISIBLE);
//				if (!ConfigApp.getInstance().isLdapEnterprise())
//				{
//					setenterpriseViewAndTabVisibility(true);
//				}
//				notifyChange();
//				hiddenSoftInputBoard(v);
//			}
//		});
//		searchContactsList.addFooterView(footView);
//		eSearchAdapter = new ESpaceSearchAdapter(context, this, ESpaceContactAdapter.TYPE_CHOOSECONTACT);
//		searchContactsList.setAdapter(eSearchAdapter);
//		searchContactsList.setOnScrollListener(new OnLdapScrollListener());
//
//		eContactAdapter = new ESpaceContactAdapter(context, this,
//				ESpaceContactAdapter.TYPE_CONTACT);
//		eSpaceEnterpriseContactPanel = new EspaceContactPanel(rootView, this);
//		eSpaceLocalContactPanel = new EspaceContactPanel(rootView, this);
//		eSpaceSearchContactPanel = new EspaceContactPanel(rootView, this);
//		//end add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//		((PullDownExpandListView) localContactsView.findViewById(R.id.contactsExpListView)).setAdapter(eContactAdapter);
//
//		eEnterpriseAdapter = new ESpaceContactAdapter(context, this, ESpaceContactAdapter.TYPE_ENTERPRISE_CONTACT);
//		setSelectContact(false);
//
//		execLoadingListener(false);
//		((PullDownExpandListView) enterpriseView.findViewById(R.id.contactsEnterpriseView)).setAdapter(eEnterpriseAdapter);
//		//======================卡片控件数据==========================
//		contactPagerAdapter = new ContactPagerAdapter();
//		contactPagerAdapter.setViewPageData(viewPageData);
//		contactViewPage.setAdapter(contactPagerAdapter);
//		ContactPageChangeListener contactPageChangeListener = new ContactPageChangeListener();
//		contactViewPage.setOnPageChangeListener(contactPageChangeListener);
//		contactViewPage.setCurrentItem(0);
//		changeShowTap(0);
//		localSearchEditor = new SearchEditor(localContactsView, this);
//		enterpriseSearchEditor = new SearchEditor(enterpriseView, this);
//		searchContactEditor = new SearchEditor(searchContactsView, this);
//
//		tabLocalTitle.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				// TODO Auto-generated method stub
//				if (null == contactViewPage)
//				{
//					return;
//				}
//				contactViewPage.setCurrentItem(0);
//				changeShowTap(0);
//			}
//		});
//
//		tabEnterpriseTitle.setOnClickListener(new View.OnClickListener()
//		{
//			@Override
//			public void onClick(View v)
//			{
//				// TODO Auto-generated method stub
//				if (null == contactViewPage)
//				{
//					return;
//				}
//				contactViewPage.setCurrentItem(1);
//				changeShowTap(1);
//			}
//		});
//
//		eSpaceEnterpriseContactPanel
//				.setContactsExpListView((PullDownExpandListView) enterpriseView
//						.findViewById(R.id.contactsEnterpriseView));
//		eSpaceEnterpriseContactPanel.setDataSource(eEnterpriseAdapter);
//		eSpaceLocalContactPanel.setContactsExpListView((PullDownExpandListView) localContactsView
//				.findViewById(R.id.contactsExpListView));
//		eSpaceLocalContactPanel.setDataSource(eContactAdapter);
//
//		//搜索
//		eSpaceSearchContactPanel.setContactsExpListView((PullDownExpandListView) rootView
//				.findViewById(R.id.search_contacts_list));
//		eSpaceSearchContactPanel.setDataSource(eSearchAdapter);
//
//		execuLocalTask();
//
//		// 匿名登录不需要获取
//		if (Constant.ANONYMOUS_ACCOUNT.equals(ConfigAccount.getIns().getLoginAccount().geteSpaceNumber()))
//		{
//			noEnterpriseContactView.setVisibility(View.VISIBLE);
//			updateContactLoadingEnterpriseTip();
//			execLoadEndListener(false);
//			return;
//		}
//		/* 定时获取企业地址本,最多执行3次，获取不到，提示加载失败*/
//		noEnterpriseContactView.setVisibility(View.GONE);
//		isEnterpriseLoading = true;
//		enterpriseTimer = new Timer();
//		enterpriseTimer.scheduleAtFixedRate(new TimerTask()
//		{
//			private int count = 0;
//			private static final int MAX_COUNT = 15;
//
//			@Override
//			public void run()
//			{
//				// TODO Auto-generated method stub
//				eEnterpriseAdapter.loadData();
//				count++;
//				LogUI.d("load enterprise contact  [load count=" + count + "].");
//				//获取3次都没有获取到数据，提示加载失败 || /不为则表示获取到企业地址本
//				if (count >= MAX_COUNT || !eEnterpriseAdapter.isEmpty())
//				{
//					if (null != enterpriseTimer)
//					{
//						enterpriseTimer.cancel();
//					}
//					enterpriseTimer = null;
//					//刷新界面
//					execuEnterpriseTask();
//				}
//			}
//		}, 0, 1000);
//	}
//
//	/**
//	 * 要切换的界面
//	 *
//	 * @param inflater 加载器
//	 * @return 界面布局数组
//	 */
//	private ArrayList<View> getUIObject(LayoutInflater inflater)
//	{
//		ArrayList<View> pageViews = new ArrayList<View>(0);
//		if (ConfigApp.getInstance().isUsePadLayout() && LayoutUtil.isPhone())
//		{
//			localContactsView = inflater.inflate(R.layout.contacts_local_pageview_layout_h, null);
//			enterpriseView = inflater.inflate(R.layout.contacts_enterprise_pageview_layout_h, null);
//		} else
//		{
//			localContactsView = inflater.inflate(R.layout.contacts_local_pageview_layout, null);
//			enterpriseView = inflater.inflate(R.layout.contacts_enterprise_pageview_layout, null);
//		}
//
//		if (ConfigApp.getInstance().isLdapEnterprise())
//		{
//			pageViews.add(localContactsView);
//		} else
//		{
//			pageViews.add(localContactsView);
//			//匿名登录时不显示企业地址本
//			if (!Constant.isAnonymousAccount())
//			{
//				pageViews.add(enterpriseView);
//			}
//		}
//		return pageViews;
//	}
//
//	/**
//	 * @param curIndex 当前显示的选项卡
//	 */
//	private void changeShowTap(int curIndex)
//	{
//		if (curIndex == 0)
//		{
//			searchInputView = localContactsView.findViewById(R.id.searchInputView);
//		} else if (curIndex == 1)
//		{
//			searchInputView = enterpriseView.findViewById(R.id.enterpriseSearchInputView);
//		}
//		//选择联系人入会是 不显示联系人功能按钮（新建 导入 导出）
//		if (eContactAdapter.isConfView())
//		{
//			setTabTitleVisibility(false);
////            tabTitle.setLabelSwitchEnable(false);
//		} else
//		{
//			setTabTitleVisibility(curIndex == 0);
////            tabTitle.setLabelSwitchEnable(curIndex == 0);
//		}
//		curShowTab = curIndex;
//		tabLocalTitle.setBackgroundDrawable(curShowTab == 0 ? tabFocusDrawable : tabNormalDrawable);
//		tabEnterpriseTitle.setBackgroundDrawable(curShowTab == 1 ? tabFocusDrawable : tabNormalDrawable);
//	}
//
//	/**
//	 * 隐藏软键盘
//	 *
//	 * @param view 输入框
//	 * @history 2013-7-31    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	private void hiddenSoftInputBoard(final View view)
//	{
//		final InputMethodManager inputmanager = (InputMethodManager) getActivity()
//				.getSystemService(Context.INPUT_METHOD_SERVICE);
//		if (null == inputmanager || null == view)
//		{
//			return;
//		}
//		inputmanager.hideSoftInputFromWindow(view.getApplicationWindowToken(),
//				InputMethodManager.HIDE_NOT_ALWAYS);
//	}
//
//	/******************************提示布局**********************************/
//
//	/**
//	 * 方法名称：updateContactLoadingLayout
//	 * 作者：LiQiao
//	 * 方法描述：控制联系人同步的进度条显示和隐藏.
//	 *
//	 * @return void
//	 * 备注：
//	 */
//	private void updateContactLoadingTip()
//	{
//		if (rootView == null)
//		{
//			return;
//		}
//
//		View loadingLayout = localContactsView.findViewById(R.id.contactLoadingLayout);
//
//		if (loadingLayout == null)
//		{
//			return;
//		}
//		loadingLayout.setVisibility(View.GONE);
//	}
//
//	/**
//	 * 企业联系人加载图标
//	 */
//	private void updateContactLoadingEnterpriseTip()
//	{
//		View enterpriseLoadingLayout = enterpriseView.findViewById(R.id.contactEnterpriseLoadingLayout);
//
//		if (enterpriseLoadingLayout == null)
//		{
//			return;
//		}
//		enterpriseLoadingLayout.setVisibility(View.GONE);
//		enterpriseView.findViewById(R.id.contactsEnterpriseView).setEnabled(true);
//	}
//
//	//begin modify by cwx176935 reason:增加less条件
//
//	/**
//	 * 搜索动作
//	 *
//	 * @param condition  条件
//	 * @param 是否是上次条件的减少
//	 * @history
//	 * @since 1.1
//	 */
//	@Override
//	public void doSearch(String condition)
//	{
//		//如果搜索条件为空，标志为非搜索模式
//		if ("".equals(condition))
//		{
//			searchInputView.setTag(null);
//		}
//		//只有在非搜索条件下 才去设置搜索，已经设置过就不需要再次设置
//		else if (searchInputView.getTag() == null)
//		{
//			searchInputView.setTag("");
//		}
//
//		eSearchAdapter.filter(condition);
//	}
//
//	//end modify by cwx176935 reason:增加less条件
//
//	/**
//	 * 刷新联系人列表
//	 */
//	public void notifyContactsDataSetChanged()
//	{
//		if (null != eContactAdapter)
//		{
//			eContactAdapter.notifyDataSetChanged();
//		}
//
//		//是否有数据，没有显示没有联系人
//		//如果在搜索中不去更新
//		if (eContactAdapter != null && noLocalContactView != null)
//		{
//			//如果没有联系人，显示无联系人图片
//			int count = eContactAdapter.getCount();
//			noLocalContactView.setVisibility((count == 0) ? View.VISIBLE
//					: View.GONE);
//		}
//	}
//
//	/**
//	 * 当前是否是企业联系人选项卡
//	 *
//	 * @return true 是
//	 */
//	public boolean curIsEnterpriseView()
//	{
//		return curShowTab == 1;
//	}
//
//	/**
//	 * 企业联系人数据变化
//	 */
//	public void notifyEnterpriseDataChange()
//	{
//		if (null != eEnterpriseAdapter)
//		{
//			eEnterpriseAdapter.notifyDataSetChanged();
//		}
//
//		if (eEnterpriseAdapter != null && noEnterpriseContactView != null)
//		{
//			int count = eEnterpriseAdapter.getCount();
//			noEnterpriseContactView.setVisibility((count == 0) ? View.VISIBLE : View.INVISIBLE);
//		}
//	}
//
//	/**
//	 * 是否在搜索模式
//	 *
//	 * @return true 是
//	 */
//	public boolean isSearch()
//	{
//		if (searchInputView == null)
//		{
//			return false;
//		}
//		//begin modified by pwx178217 reason:findbugs
//		return "".equals(searchInputView.getTag());
//		//end modified by pwx178217 reason:findbugs
//	}
//
//	/**
//	 * 方法名称：reset
//	 * 作者：LiQiao
//	 * 方法描述：重置联系人Fragment界面和相关数据
//	 * 输入参数:
//	 * 返回类型：void
//	 * 备注：
//	 */
//	public void reset()
//	{
//		//界面重置
//		if (rootView != null)
//		{
//			//pad界面中联系人界面与选中入会界面公用同一个界面
//			if (!ConfigApp.getInstance().isUsePadLayout())
//			{
//				setSelectContact(false);
//			}
//			if (eSearchAdapter.isSearchView())
//			{
//				if (!isSearchLdapEnterprise())
//				{
//					eSearchAdapter.reFilter();
//				}
//			}
//			notifyChange();
//			//end add by wx183960 reason:联系人搜索框中输入关键字后，切换到其他界面再切换回来，搜索框中的关键字自动消失
//			LogUI.d("reset ContactsFragment  view");
//		}
//	}
//
//
//	/**
//	 * 点击标签的弹框
//	 *
//	 * @param v 点击的view
//	 */
//	@Override
//	public void onLabelSwitchClick(View v)
//	{
//		//begin added by cwx176935 2013/8/13 reason：添加弹框功能
//		if (null == contactMorePopWindow)
//		{
//			//begin modified by pwx178217 reason:手机竖屏新建联系人弹窗修改
//			if (!ConfigApp.getInstance().isUsePadLayout())
//			{
//				contactMorePopWindow = new ContactAddPopWindow(getActivity());
//				contactMorePopWindow.setWidth(Double.valueOf(334 * 1.6 * LayoutUtil
//						.getInstance().getScreenPXScale()).intValue());
//				contactMorePopWindow.setLongClicked(false);
//			} else
//			{
//				contactMorePopWindow = new CommPopWindow(getActivity());
//				//不是手机情况下设置为自动位置
//				contactMorePopWindow.setLongClicked(false);
//			}
//			//end modified by pwx178217 reason:手机竖屏新建联系人弹窗修改
//			List<MenuItem> menuItems = new ArrayList<MenuItem>(0);
//			//新建联系人
//			MenuItem menuItem = new MenuItem(R.drawable.tp_ic_arrow_up, this
//					.getActivity().getString(R.string.create_new_contact),
//					new ESpaceAction()
//					{
//
//						@Override
//						public void start()
//						{
//							onShowView(TYPE.CREATE_NEW_CONTACT);
//						}
//					});
//			menuItems.add(menuItem);
//
//			//导入联系人
//			menuItem = new MenuItem(R.drawable.tp_ic_arrow_up, this
//					.getActivity().getString(R.string.import_contact),
//					new ESpaceAction()
//					{
//
//						@Override
//						public void start()
//						{
//							onShowView(TYPE.IMPORT_CONTACT);
//						}
//					});
//			menuItems.add(menuItem);
//
//			//导出联系人
//			menuItem = new MenuItem(R.drawable.tp_ic_arrow_up, this
//					.getActivity().getString(R.string.export_contact),
//					new ESpaceAction()
//					{
//
//						@Override
//						public void start()
//						{
//							onShowView(TYPE.EXPORT_CONTACT);
//						}
//					});
//			menuItems.add(menuItem);
//
//			contactMorePopWindow.setMenuItems(menuItems);
//		}
//		contactMorePopWindow.show(v);
//		//end added by cwx176935 2013/8/13 reason：添加弹框功能
//	}
//
//	/**
//	 * 退出搜索模式
//	 *
//	 * @history 2013-8-8    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void exitSearchMode()
//	{
//		if (!eContactAdapter.isConfView() && curShowTab == 0)
//		{
//			setTabTitleVisibility(true);
//		}
////        tabTitle.setLabelSwitchEnable(true);
//		showSearchView(false);
//	}
//
//	//begin added by cwx176935 2013/8/13 reason：添加弹框功能对应的功能
//
//	/**
//	 * 这是继承ContactMoreService实现
//	 *
//	 * @param type 类型
//	 */
//	public void onShowView(TYPE type)
//	{
//		switch (type)
//		{
//			case CREATE_NEW_CONTACT:
//				Intent intentCreate = new Intent(this.getActivity(),
//						ContactDialogActivity.class);
//				intentCreate.putExtra("isCreate", true);
//				startActivity(intentCreate);
//				break;
//
//			case EXPORT_CONTACT:
//				//BEGIN modified by cWX183956 2013/11/1 Reason:DTS2013103106797 没有联系人时不可导出 
//				if (null == DataManager.getIns().getContacts()
//						|| DataManager.getIns().getContacts().isEmpty())
//				{
//					//begin added by pwx178217 reason:DTS2014012105175 界面中相关提示显示的时长随单击次数增加而增加 
//					showToast(R.string.no_contacts);
//					//end added by pwx178217 reason:DTS2014012105175 界面中相关提示显示的时长随单击次数增加而增加 
//					break;
//				}
//				//END modified by cWX183956 2013/11/1 Reason:DTS2013103106797 没有联系人时不可导出 
//			case IMPORT_CONTACT:
//				Intent intent = new Intent(this.getActivity(),
//						FileBrowserActivity.class);
//
//				int contactCtlType = (type == TYPE.IMPORT_CONTACT) ? FileBrowserActivity.IMPORT
//						: FileBrowserActivity.EXPORT;
//
//				//设置启动类型
//				intent.putExtra("type", contactCtlType);
//
//				this.getActivity().startActivity(intent);
//				break;
//
//			default:
//				break;
//		}
//	}
//
//	//end added by cwx176935 2013/8/13 reason：添加弹框功能
//
//	/**
//	 * 长按点击事件
//	 *
//	 * @param v  长按的view
//	 * @param pc 联系人
//	 * @see com.huawei.app.ui.ContactLabelSelector.LabelSelectorServer#onLabelLongClick(int)
//	 */
//	@Override
//	public void onContactItemLongClick(View v, final PersonalContact pc)
//	{
//		CommPopWindow longClickPop = new CommPopWindow(this.getActivity());
//		List<MenuItem> longClickItems = new ArrayList<MenuItem>(0);
//		MenuItem menuItem = null;
//		//企业联系人
//		if (pc.isEnterprise())
//		{
//			if (StringUtil.isNotEmpty(pc.getDefinition()))
//			{
//				PersonalContact localPersonalContact = DataManager.getIns().getContactByDN(pc.getDefinition());
//				if (null != localPersonalContact && StringUtil.isNotEmpty(localPersonalContact.getDefinition()))
//				{
//					LogUI.i("The person has added to localcontacts");
//					return;
//				}
//			} else
//			{
//				PersonalContact localPc = DataManager.getIns().getContactByNumber(pc.getNumberOne());
//				//名字和号码都相同不显示
//				if (localPc != null && localPc.getName().equals(pc.getName()))
//				{
//					LogUI.i("Name and Number is not the same");
//					return;
//				}
//			}
//			menuItem = new MenuItem(MenuItem.INVALID_IMG_ID,
//					getString(R.string.add_to_contacts), new ESpaceAction()
//			{
//				@Override
//				public void start()
//				{
//					// TODO Auto-generated method stub
//					addToLocal(pc);
//				}
//			});
//			longClickItems.add(menuItem);
//			longClickPop.setMenuItems(longClickItems).setLongClicked(true).show(v);
//			return;
//		}
//
//		//编辑联系人
//		if (StringUtil.isStringEmpty(pc.getDefinition()))
//		{
//			menuItem = new MenuItem(MenuItem.INVALID_IMG_ID,
//					getString(R.string.edit), new ESpaceAction()
//			{
//
//				@Override
//				public void start()
//				{
//					//  这个需要在Fragment里操作
//					editContact(pc);
//				}
//			});
//			longClickItems.add(menuItem);
//		}
//
//		//删除联系人
//		menuItem = new MenuItem(MenuItem.INVALID_IMG_ID, this.getActivity()
//				.getString(R.string.delete), new ESpaceAction()
//		{
//
//			@Override
//			public void start()
//			{
//				deleteContact(pc);
//			}
//		});
//		longClickItems.add(menuItem);
//		longClickPop.setMenuItems(longClickItems).setLongClicked(true).show(v);
//
//	}
//
//	/* (non-Javadoc)
//	 * @see com.huawei.app.ui.EspaceContactPanel.ContactPanelServer#onContactItemClick(android.view.View, com.huawei.common.PersonalContact)
//	 */
//	public void onContactItemClick(View v, final PersonalContact pc, int posision, AbsEspaceAdapter adapter)
//	{
//		if (!ConfigApp.getInstance().isUsePadLayout())
//		{
//
//			Intent intent = new Intent(rootView.getContext(), ContactDetailActivity.class);
//			Bundle extras = new Bundle();
//			extras.putSerializable("personal", pc);
//			intent.putExtras(extras);
//			rootView.getContext().startActivity(intent);
//		} else
//		{
//			if (ConfigApp.getInstance().isConfView())
//			{
//				boolean isContain = ifIsSelected(pc);
//
//				LogUI.i("contacts item clicked");
//				if (isContain)
//				{
//					removeContactsInList(pc);
//				} else
//				{
//					//最多选择400联系人
//					if (confragSelectedContacts.size() >= Constant.MAX_CONF_COUNT)
//					{
//						LogUI.e("Too much selected contacts Max is " + Constant.MAX_CONF_COUNT);
//						showToast(R.string.create_conference_max);
//						return;
//					}
//					confragSelectedContacts.add(pc);
//				}
//				//设置选中联系人
//				ConfigApp.getInstance().setSelectedContactList(confragSelectedContacts);
//
//				//设置选择联系人加入会议提示可见性设置完成按钮显示文字
//				HomeActivity.getInstance().contactsGridViewVisibility(confragSelectedContacts.size());
//				notifyChange();
//				return;
//			}
//			/**
//			 * 刷新界面显示
//			 */
//			contactDetailPanel.showDetail(pc);
//			AnimationUtil.slideInFromRight(contactDetailPanel.getDetailView());
//		}
//	}
//
//
//	/**
//	 * 将企业联系人添加到本地
//	 *
//	 * @param pc 要添加的企业联系人
//	 */
//	public void addToLocal(final PersonalContact pc)
//	{
//		if (null == pc)
//		{
//			return;
//		}
//		if (null != pc.getNumberOne() && pc.getNumberOne().length() > 100)
//		{
//			showToast(R.string.contact_num_to_long);
//			return;
//		}
//
//		final PersonalContact localPc = DataManager.getIns().getContactByName(pc.getName());
//		final PersonalContact insertPc = (PersonalContact) ObjectClone.deepClone(pc);
//		if (StringUtil.isStringEmpty(pc.getDefinition()))
//		{
//			//如果找到联系人，提示替换
//			if (null != localPc && !"".equals(localPc.getContactId())
//					&& StringUtil.isStringEmpty(localPc.getDefinition()))
//			{
//				final IOSStyleDialog dialog = new Builder(
//						getActivity()).create();
//				dialog.setMessage(getActivity().getString(R.string.replace_local_contact));
//				dialog.setTitle(R.string.replace);
//
//				DialogClickListener dialogClickListener = new DialogClickListener(pc, DialogClickListener.TYPE_ONE);
//				dialog.setButton(IOSStyleDialog.BUTTON_NEGATIVE, getActivity()
//						.getText(R.string.cancel), dialogClickListener);
//
//				dialog.setButton(IOSStyleDialog.BUTTON_POSITIVE, getActivity()
//						.getText(R.string.replace), dialogClickListener);
//				dialog.show();
//				return;
//			}
//		}
//		insertPc.setEnterprise(false);
//		addContact(insertPc);
//		LogUI.i("add Enterprise Contact to Local Success");
//	}
//
//	/**
//	 * 添加联系人
//	 *
//	 * @param pc 被添加的联系人
//	 * @return 添加结果
//	 */
//	private boolean addContact(PersonalContact pc)
//	{
//		int ret = DataManager.getIns().addContact(pc);
//		if (ret == ErrorCode.IMPORT_FAIL_OUT)
//		{
//			showToast(R.string.contact_full);
//			return false;
//		} else if (ret == ErrorCode.OPERATE_SUCCESS)
//		{
//			showToast(R.string.add_success);
//		}
//		return true;
//	}
//
//	/**
//	 * 编辑联系人
//	 *
//	 * @param pc 联系人
//	 * @history 2013-8-2    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	public void editContact(PersonalContact pc)
//	{
//		if (null == pc)
//		{
//			return;
//		}
//		//编辑的人正在通话 返回
//		if (isTalkingStatus() && isCallingPerson(pc))
//		{
//			//begin added by pwx178217 reason:DTS2014012105175 界面中相关提示显示的时长随单击次数增加而增加 
//			showToast(R.string.talking_status);
//			//end added by pwx178217 reason:DTS2014012105175 界面中相关提示显示的时长随单击次数增加而增加 
//			return;
//		}
//
//
//		//正常操作
//		Intent intentCreate = new Intent(this.getActivity(),
//				ContactDialogActivity.class);
//		intentCreate.putExtra("isCreate", false);
//		intentCreate.putExtra("contact", pc);
//		startActivityForResult(intentCreate, 1);
//	}
//
//
//	//end add
//
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data)
//	{
//		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
//		if (null != contactDetailPanel
//				&& null != data
//				&& null != contactDetailPanel.getDetailView()
//				&& contactDetailPanel.getDetailView().getVisibility() == View.VISIBLE
//				&& requestCode == 1)
//		{
//			PersonalContact pc = (PersonalContact) data.getSerializableExtra("contact");
//
//			contactDetailPanel.showDetail(pc);
//		}
//	}
//
//	/**
//	 * 字母被选中
//	 *
//	 * @param code 选中的字母
//	 * @history 2013-8-3    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void codeSelect(final String code)
//	{
//		if (codeDialog == null)
//		{
//			codeDialog = new AutoDissmissDialog(enterpriseView);
//		}
//		if (codeDialog.isShowing())
//		{
//			codeDialog.resetTime();
//		} else
//		{
//			codeDialog.show();
//		}
//		codeDialog.setMessage(code);
//
//		if (isEnterpriseLoading)
//		{
//			return;
//		}
//		/**
//		 * 这里操作选择适配
//		 */
//		eSpaceEnterpriseContactPanel.selectItemByCode(code);
//
//	}
//
//	/**
//	 * resume
//	 *
//	 * @history 2013-8-5    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onResume()
//	{
//		super.onResume();
//	}
//
//	/**
//	 * 删除动作
//	 *
//	 * @param pc 联系人
//	 * @history 2013-8-3    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onDeleteClick(final PersonalContact pc)
//	{
//		//删除的人正在通话 返回
//		if (isTalkingStatus() && isCallingPerson(pc))
//		{
//			//begin added by pwx178217 reason:DTS2014012105175 界面中相关提示显示的时长随单击次数增加而增加 
//			showToast(R.string.talking_status);
//			//end added by pwx178217 reason:DTS2014012105175 界面中相关提示显示的时长随单击次数增加而增加 
//			return;
//		}
//
//		final IOSStyleDialog dialog = new Builder(getActivity())
//				.create();
//		dialog.setMessage(String.format(
//				getActivity().getString(R.string.delete_content), pc.getName()));
//		dialog.setTitle(R.string.delete);
//
//		DialogClickListener dialogClickListener = new DialogClickListener(pc, DialogClickListener.TYPE_TWO);
//
//		dialog.setButton(IOSStyleDialog.BUTTON_NEGATIVE,
//				getActivity().getText(R.string.cancel), dialogClickListener);
//
//		dialog.setButton(IOSStyleDialog.BUTTON_POSITIVE,
//				getActivity().getText(R.string.delete), dialogClickListener);
//		dialog.show();
//	}
//
//	/**
//	 * 编辑动作
//	 *
//	 * @param pc 联系人
//	 * @history 2013-8-3    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onEditclick(PersonalContact pc)
//	{
//		editContact(pc);
//	}
//
//	/**
//	 * 删除动作
//	 *
//	 * @param pc 联系人
//	 * @history 2013-8-3    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	public void deleteContact(PersonalContact pc)
//	{
//		onDeleteClick(pc);
//	}
//
//	/**
//	 * 添加触摸事件Listener
//	 *
//	 * @param v     事件触发的view
//	 * @param event 触发的事件
//	 * @return false 表示此层不处理交给上层处理
//	 * @history 2013-8-19    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public boolean onTouch(View v, MotionEvent event)
//	{
//		synchronized (codeLock)
//		{
//			if (codeDialog == null)
//			{
//				return false;
//			}
//			if (codeDialog.isShowing())
//			{
//				codeSearchView.onTouch(v, event);
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * 联系人添加回调
//	 *
//	 * @param arg0 联系人
//	 * @history 2013-8-24    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onContactAdded(List<PersonalContact> arg0)
//	{
//		if (!eSearchAdapter.isSearchView())
//		{
//			reFilter();
//		}
//
//		getActivity().runOnUiThread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				// 刷新列表
//				notifyContactsDataSetChanged();
//				execLoadEndListener(true);
//			}
//		});
//
//	}
//
//	/**
//	 * 联系人删除回调
//	 *
//	 * @param contacts 联系人
//	 * @history 2013-8-24    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onContactRemoved(List<PersonalContact> contacts)
//	{
//		reFilter();
//		//下层开线程刷界面
//		getActivity().runOnUiThread(new Runnable()
//		{
//
//			@Override
//			public void run()
//			{
//				notifyContactsDataSetChanged();
//				execLoadEndListener(true);
//			}
//		});
//	}
//
//	/**
//	 * 联系人修改Listener
//	 *
//	 * @param contacts 修改的联系人
//	 * @history 2013-8-24    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onContactModified(List<PersonalContact> contacts)
//	{
//		if (!eSearchAdapter.isSearchView())
//		{
//			reFilter();
//		}
//
//		//下层开线程刷界面
//		getActivity().runOnUiThread(new Runnable()
//		{
//
//			@Override
//			public void run()
//			{
//				notifyContactsDataSetChanged();
//				execLoadEndListener(true);
//			}
//		});
//	}
//
//	/**
//	 * 数据重新搜索
//	 */
//	public void reFilter()
//	{
//		if (null != eSearchAdapter)
//		{
//			//eSearchAdapter.loadData(DataManager.getIns().getAllContacts());
//			eSearchAdapter.loadData(DataManager.getIns().getContacts());
//			eSearchAdapter.reFilter();
//		}
//	}
//
//	/**
//	 * 销毁界面
//	 *
//	 * @history 2013-8-24    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onDestroy()
//	{
//		super.onDestroy();
//
//		LogUI.d("clear contactsFragment data");
//		clearData();
//	}
//
//	/**
//	 * 获得当前状态
//	 *
//	 * @return true 通话状态
//	 * @history 2013-9-22    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	public static boolean isTalkingStatus()
//	{
//		return (CallLogic.getIns().getVoipStatus() == CallLogic.STATUS_VIDEOING
//				|| CallLogic.getIns().getVoipStatus() == CallLogic.STATUS_TALKING
//				|| CallLogic.getIns().getVoipStatus() == CallLogic.STATUS_VIDEOACEPT
//				|| CallLogic.getIns().getVoipStatus() == CallLogic.STATUS_VIDEOINIT || CallLogic
//				.getIns().getVoipStatus() == CallLogic.STATUS_CALLING);
//	}
//
//	/**
//	 * 联系人是否在通话
//	 *
//	 * @param pc 联系人
//	 * @return true是
//	 * @history 2013-10-12    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	public static boolean isCallingPerson(PersonalContact pc)
//	{
//		String callingNumber = CallLogic.getIns().getCallNumber();
//		if (null == callingNumber || "".equals(callingNumber))
//		{
//			return false;
//		}
//		PersonalContact pContact = DataManager.getIns().getContactByNumber(
//				callingNumber);
//		if (null == pContact)
//		{
//			return false;
//		} else
//		{
//			return pc.getContactId().equals(pContact.getContactId());
//		}
//
//	}
//
//	/**
//	 * 添加联系人加载事件Listener
//	 *
//	 * @param listener 对象
//	 */
//	public void addLocalLoadListener(ContactLoadState listener)
//	{
//		if (null == listener)
//		{
//			return;
//		}
//		if (null == localLoadStates)
//		{
//			localLoadStates = new ArrayList<ContactLoadState>(0);
//		}
//
//		localLoadStates.add(listener);
//	}
//
//	/**
//	 * 添加联系人加载事件Listener
//	 *
//	 * @param listener Listener对象
//	 */
//	public void addEnterpriseLoadListener(ContactLoadState listener)
//	{
//		if (null == listener)
//		{
//			return;
//		}
//		if (null == enterpriseLoadStates)
//		{
//			enterpriseLoadStates = new ArrayList<ContactLoadState>(10);
//		}
//
//		enterpriseLoadStates.add(listener);
//	}
//
//
//	/**
//	 * 移除事件Listener
//	 *
//	 * @param listener Listener对象
//	 * @return 移除结果
//	 */
//	public boolean removeLocalLoadListener(ContactLoadState listener)
//	{
//		if (null == localLoadStates || null == listener)
//		{
//			return true;
//		}
//		return localLoadStates.remove(listener);
//	}
//
//	/**
//	 * 移除事件Listener
//	 *
//	 * @param listener Listener对象
//	 * @return 移除结果
//	 */
//	public boolean removeEnterpriseLoadListener(ContactLoadState listener)
//	{
//		if (null == enterpriseLoadStates || null == listener)
//		{
//			return true;
//		}
//		return enterpriseLoadStates.remove(listener);
//	}
//
//	/**
//	 * 移除所有加载Listener
//	 */
//	public void removeAllLocalLoadListener()
//	{
//		if (null == localLoadStates)
//		{
//			return;
//		}
//		localLoadStates.clear();
//	}
//
//	/**
//	 * 移除所有加载Listener
//	 */
//	public void removeAllEnterpriseLoadListener()
//	{
//		if (null == enterpriseLoadStates)
//		{
//			return;
//		}
//		enterpriseLoadStates.clear();
//	}
//
//	/**
//	 * 正在加载数据事件分发
//	 *
//	 * @param isLocal true本地
//	 */
//	private void execLoadingListener(boolean isLocal)
//	{
//		List<ContactLoadState> listeners = enterpriseLoadStates;
//		if (isLocal)
//		{
//			listeners = localLoadStates;
//		}
//		if (null == listeners)
//		{
//			return;
//		}
//		ContactLoadState listener = null;
//		int size = listeners.size();
//		for (int i = 0; i < size; i++)
//		{
//			listener = listeners.get(i);
//			listener.isLoading();
//		}
//	}
//
//	/**
//	 * 数据加载完成事件分发
//	 *
//	 * @param isLocal true 本地
//	 */
//	private void execLoadEndListener(boolean isLocal)
//	{
//		List<ContactLoadState> listeners = enterpriseLoadStates;
//		if (isLocal)
//		{
//			listeners = localLoadStates;
//		}
//		if (null == listeners)
//		{
//			return;
//		}
//		int listenerSize = listeners.size();
//		for (int i = 0; i < listenerSize; i++)
//		{
//			listeners.get(i).loadEnd(isLocal);
//		}
//	}
//	//begin add by cwx176935 reason:当数据过多的时候后台加载数据
//
//
//	//begin add by cwx176935 reason: DTS2013112808491 话机视频通话一段时间后，注销再重新登录，出现异常退出现象
//
//	/**
//	 * 清除数据
//	 *
//	 * @history 2013-12-11    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	private void clearData()
//	{
//
//		// begin add by cwx176935 reason: DTS2014091501098 自动化被T崩溃
//		if (null != DataManager.getIns())
//		{
//			DataManager.getIns().unregContactEventListen(this);
//			DataManager.getIns().unregContactStateListen(this);
//			DataManager.getIns().unregEnterpriseUpdateListen(this);
//
//			if (ConfigApp.getInstance().isLdapEnterprise())
//			{
//				LdapManager.getIns().unregLdapEventListen(this);
//			}
//		}
//
//		if (null != enterpriseTimer)
//		{
//			enterpriseTimer.cancel();
//			enterpriseTimer = null;
//		}
//
//		if (null != localLoadTask && localLoadTask.getStatus() != AsyncTask.Status.FINISHED)
//		{
//			localLoadTask.cancel(true);
//		}
//		if (null != enterpriseLoadTask && enterpriseLoadTask.getStatus() != AsyncTask.Status.FINISHED)
//		{
//			enterpriseLoadTask.cancel(true);
//		}
//		localLoadTask = null;
//		enterpriseLoadTask = null;
//		// end add by cwx176935 reason: DTS2014091501098 自动化被T崩溃
//
//		if (null != eSpaceEnterpriseContactPanel)
//		{
//			eSpaceEnterpriseContactPanel.clearData();
//		}
//		eSpaceEnterpriseContactPanel = null;
//
//		if (null != eSpaceLocalContactPanel)
//		{
//			eSpaceLocalContactPanel.clearData();
//		}
//		eSpaceLocalContactPanel = null;
//
//		if (null != contactMorePopWindow)
//		{
//			contactMorePopWindow.dismiss();
//		}
//		contactMorePopWindow = null;
//		textViewLength = null;
//	}
//
//	//end add by cwx176935 reason: DTS2013112808491 话机视频通话一段时间后，注销再重新登录，出现异常退出现象
//
//	public TextView getTextViewLength()
//	{
//		return textViewLength;
//	}
//
//	public TextView getNameTxtViewLegth()
//	{
//		return nameTxtViewLegth;
//	}
//
//	/**
//	 * 加载数据
//	 * Copyright (C) 2008-2013 华为技术有限公司(Huawei Tech.Co.,Ltd)
//	 *
//	 * @history 2013-9-26    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	private class LoadDataTask extends AsyncTask<String, Void, Boolean>
//	{
//
//		/**
//		 * @param params
//		 * @return
//		 * @history 2013-9-26    v1.0.0    cWX176935    create
//		 * @since 1.1
//		 */
//		@Override
//		protected Boolean doInBackground(String... params)
//		{
//			if (null == params)
//			{
//				LogUI.d("params is null");
//			}
//			//begin add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//			execLoadingListener(true);
//			//end add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//			if (null != noLocalContactView)
//			{
//				noLocalContactView.setVisibility(View.GONE);
//			}
//			eContactAdapter.loadData();
//			return true;
//		}
//
//
//		/**
//		 * @history 2013-9-26    v1.0.0    cWX176935    create
//		 * @since 1.1
//		 */
//		@Override
//		protected void onPreExecute()
//		{
//			super.onPreExecute();
//
//		}
//
//		/**
//		 * @param result true执行完毕
//		 * @history 2013-9-26    v1.0.0    cWX176935    create
//		 * @since 1.1
//		 */
//		@Override
//		protected void onPostExecute(Boolean result)
//		{
//			if (null == result)
//			{
//				execLoadEndListener(true);
//				LogUI.d("result is null");
//				return;
//			}
//			super.onPostExecute(result);
//			if (null == eContactAdapter)
//			{
//				return;
//			}
//			if (result) //成功执行完成
//			{
//				notifyContactsDataSetChanged();
//				//begin add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//				execLoadEndListener(true);
//				updateContactLoadingTip();
//				//end add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//			}
//			localLoadTask = null;
//
//		}
//	}
//
//	/**
//	 * 加载数据
//	 * Copyright (C) 2008-2013 华为技术有限公司(Huawei Tech.Co.,Ltd)
//	 *
//	 * @history 2013-9-26    v1.0.0    cWX176935    create
//	 * @since 1.1
//	 */
//	private class LoadEnterpriseDataTask extends AsyncTask<String, Void, Boolean>
//	{
//
//		/**
//		 * @param params
//		 * @return
//		 * @history 2013-9-26    v1.0.0    cWX176935    create
//		 * @since 1.1
//		 */
//		@Override
//		protected Boolean doInBackground(String... params)
//		{
//			if (null == params)
//			{
//				LogUI.d("params is null");
//			}
//			//begin add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//			isEnterpriseLoading = true;
//			//end add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
//
//			if (null == getActivity())
//			{
//				return false;
//			}
//			getActivity().runOnUiThread(new Runnable()
//			{
//
//				@Override
//				public void run()
//				{
//					// TODO Auto-generated method stub
//					enterpriseView.findViewById(R.id.contactEnterpriseLoadingLayout).setVisibility(View.VISIBLE);
//					noEnterpriseContactView.setVisibility(View.GONE);
//				}
//
//			});
//			execLoadingListener(false);
//			eEnterpriseAdapter.loadData();
//			return true;
//		}
//
//		/**
//		 * @history 2013-9-26    v1.0.0    cWX176935    create
//		 * @since 1.1
//		 */
//		@Override
//		protected void onPreExecute()
//		{
//			super.onPreExecute();
//
//		}
//
//		/**
//		 * @param result true执行完毕
//		 * @history 2013-9-26    v1.0.0    cWX176935    create
//		 * @since 1.1
//		 */
//		@Override
//		protected void onPostExecute(Boolean result)
//		{
//			super.onPostExecute(result);
//			if (result) //成功执行完成
//			{
//				updateContactLoadingEnterpriseTip();
//			}
//			notifyEnterpriseDataChange();
//			isEnterpriseLoading = false;
//			execLoadEndListener(false);
//			enterpriseLoadTask = null;
//		}
//
//	}
//	//end add by cwx176935 reason:当数据过多的时候后台加载数据
//
//	/**
//	 * 显示Toast 提示
//	 *
//	 * @param strInt 字符串id
//	 * @history 2014-1-26    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	private void showToast(int strInt)
//	{
//		if (null == mToast)
//		{
//			mToast = new ToastHelp(getActivity());
//		}
//		mToast.setText(getActivity().getString(strInt));
//		mToast.showToast(2000);
//	}
//
//	@Override
//	public void onStatePresenceRefresh(List<PersonalContact> contacts)
//	{
//		// TODO Auto-generated method stub
//		Runnable runnable = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				// 刷新列表
//				notifyContactsDataSetChanged();
//				//刚登录主界面的时候 不刷新底下抛上的事件, 因为第一刷新的时候会显示出无联系人图标
//				if (enterpriseTimer == null)
//				{
//					notifyEnterpriseDataChange();
//				}
//				//刷新搜索界面
//				eSearchAdapter.refreshConfSearchState();
//				eSearchAdapter.notifyDataSetChanged();
//				selectedContactsAdapter.refreshConfListState();
//				selectedContactsAdapter.notifyDataSetChanged();
//			}
//		};
//		getActivity().runOnUiThread(runnable);
//	}
//
//	/**
//	 * 手机布局时断网提示
//	 */
//	public void onNetworkStatusChange(boolean isConnected, boolean isChangeState)
//	{
//		if (isConnected)
//		{
//			networkDisconnect.setVisibility(View.GONE);
//		} else
//		{
//			networkDisconnect.setVisibility(View.VISIBLE);
//			setNetDisConnectTxt(isChangeState
//					? getString(R.string.offline_calldisable) : getString(R.string.nowifitip));
//		}
//	}
//
//	/**
//	 * 设置显示文字
//	 *
//	 * @param str
//	 * @history 2015-2-5    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public void setNetDisConnectTxt(String str)
//	{
//		networkDisconnect.setText(str);
//	}
//
//	/**
//	 * 页面滑动Listener
//	 */
//	private class ContactPageChangeListener implements OnPageChangeListener
//	{
//
//		@Override
//		public void onPageScrollStateChanged(int arg0)
//		{
//			// TODO Auto-generated method stub  
//		}
//
//		@Override
//		public void onPageScrolled(int arg0, float arg1, int arg2)
//		{
//			// TODO Auto-generated method stub  
//		}
//
//		@Override
//		public void onPageSelected(int arg0)
//		{
//			curShowTab = arg0;
//			changeShowTap(curShowTab);
//			AbsMenuPopWindow.disMissAllPop();
//		}
//	}
//
//	@Override
//	public void pullDownStart()
//	{
//		// TODO Auto-generated method stub
//
//	}
//
//
//	@Override
//	public void pullDownEnd()
//	{
//		// TODO Auto-generated method stub
//
//	}
//
//
//	@Override
//	public boolean isTabVisible()
//	{
//		// TODO Auto-generated method stub
//		return (searchInputView.getTag() == null);
//	}
//
//
//	@Override
//	public void setTabvisible(boolean isActived)
//	{
//		// TODO Auto-generated method stub
//		/**
//		 * 用于view的滑动，目前已经没有用，下列代码不会走到
//		 */
//		if (isActived)
//		{
//			searchInputView.setTag("");
//		} else
//		{
//			searchInputView.setTag(null);
//			// TODO Auto-generated method stub
//			if (curShowTab == 0)
//			{
//				localSearchEditor.clearSearchCondition();
//			} else
//			{
//				enterpriseSearchEditor.clearSearchCondition();
//			}
//		}
//		if (curShowTab == 0)
//		{
//			localSearchEditor.setActivated(isActived);
//		} else
//		{
//			enterpriseSearchEditor.setActivated(isActived);
//			codeSearchView.zoomHeight(isActived ? -searchInputView.getMeasuredHeight() : searchInputView.getMeasuredHeight());
//		}
//		codeSearchView.setSearch(isActived);
//	}
//
//	@Override
//	public boolean isMoveEnabled()
//	{
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public void contactDetailBack()
//	{
//		// TODO Auto-generated method stub
//		if (null != contactDetailPanel)
//		{
//			AnimationUtil.slideOutToRight(contactDetailPanel.getDetailView());
//		}
//	}
//
//	@Override
//	public void contactDetailEdit(PersonalContact pc)
//	{
//		// TODO Auto-generated method stub
//		editContact(pc);
//	}
//
//	@Override
//	public void contactDetaildelete(PersonalContact pc)
//	{
//		// TODO Auto-generated method stub
//		deleteContact(pc);
//	}
//
//	/**
//	 * 企业联系人解析完成回调
//	 *
//	 * @param contacts 企业联系人
//	 */
//	@Override
//	public void onEnterpriseContactAnalyDone(final List<PersonalContact> contacts)
//	{
//		if (null != enterpriseTimer)
//		{
//			enterpriseTimer.cancel();
//			enterpriseTimer = null;
//		}
//		execuEnterpriseTask();
//	}
//
//	/**
//	 * 启动企业联系人异步任务
//	 */
//	private void execuEnterpriseTask()
//	{
//		if (null != enterpriseLoadTask)
//		{
//			if (enterpriseLoadTask.getStatus() == AsyncTask.Status.FINISHED)
//			{
//				enterpriseLoadTask = null;
//				enterpriseLoadTask = new LoadEnterpriseDataTask();
//				enterpriseLoadTask.execute("do");
//			} else if (enterpriseLoadTask.getStatus() == AsyncTask.Status.PENDING)
//			{
//				enterpriseLoadTask.execute("do");
//			}
//		} else
//		{
//			enterpriseLoadTask = new LoadEnterpriseDataTask();
//			enterpriseLoadTask.execute("do");
//		}
//	}
//
//	/**
//	 * 启动本地联系人异步任务
//	 */
//	private void execuLocalTask()
//	{
//		if (null != localLoadTask)
//		{
//			if (localLoadTask.getStatus() == AsyncTask.Status.FINISHED)
//			{
//				localLoadTask = null;
//				localLoadTask = new LoadDataTask();
//				localLoadTask.execute("do");
//			} else if (localLoadTask.getStatus() == AsyncTask.Status.PENDING)
//			{
//				localLoadTask.execute("do");
//			}
//		} else
//		{
//			localLoadTask = new LoadDataTask();
//			localLoadTask.execute("do");
//		}
//	}
//
//	@Override
//	public void beforeEnterpriseUpdate()
//	{
//		if (null == enterpriseView)
//		{
//			return;
//		}
//		Runnable runnable = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				if (null != noEnterpriseContactView)
//				{
//					noEnterpriseContactView.setVisibility(View.GONE);
//				}
//				// TODO Auto-generated method stub
//				enterpriseView.findViewById(R.id.contactEnterpriseLoadingLayout).setVisibility(View.VISIBLE);
//				enterpriseView.findViewById(R.id.contactsEnterpriseView).scrollTo(0, 0);
//				enterpriseView.findViewById(R.id.contactsEnterpriseView).setEnabled(false);
//			}
//		};
//		// TODO Auto-generated method stub
//		this.getActivity().runOnUiThread(runnable);
//	}
//
//	@Override
//	public void afterEnterpriseUpdate()
//	{
//		// TODO Auto-generated method stub
//		if (null == enterpriseView)
//		{
//			return;
//		}
//		Runnable runnable = new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				// TODO Auto-generated method stub
//				notifyEnterpriseDataChange();
//				enterpriseView.findViewById(R.id.contactEnterpriseLoadingLayout).setVisibility(View.GONE);
//				enterpriseView.findViewById(R.id.contactsEnterpriseView).setEnabled(true);
//			}
//		};
//		this.getActivity().runOnUiThread(runnable);
//	}
//
//	/**
//	 * @author cWX176935
//	 */
//	private class DialogClickListener implements DialogInterface.OnClickListener
//	{
//		protected static final int TYPE_ONE = 1;
//
//		protected static final int TYPE_TWO = 2;
//
//		private PersonalContact pc;
//
//		private int type;
//
//		DialogClickListener(PersonalContact pcVar, int typeVar)
//		{
//			this.pc = pcVar;
//			this.type = typeVar;
//		}
//
//		@Override
//		public void onClick(DialogInterface dialog, int which)
//		{
//			if (TYPE_ONE == type)
//			{
//				if (IOSStyleDialog.BUTTON_NEGATIVE == which)
//				{
//					dialog.dismiss();
//				} else if (IOSStyleDialog.BUTTON_POSITIVE == which)
//				{
//					if (null == pc)
//					{
//						LogUI.e("personalContact is null.");
//						return;
//					}
//
//					PersonalContact localPc = DataManager.getIns().getContactByName(pc.getName());
//					PersonalContact insertPc = (PersonalContact) ObjectClone.deepClone(pc);
//					if (null == localPc || null == insertPc)
//					{
//						LogUI.i("localPc is null or insertPc is null.");
//						return;
//					}
//
//					insertPc.setEnterprise(false);
//					insertPc.setContactId(localPc.getContactId());
//					DataManager.getIns().modifyContact(insertPc);
//					LogUI.i("add Enterprise Contact to Local Success");
//					dialog.dismiss();
//				}
//			} else if (TYPE_TWO == type)
//			{
//				if (IOSStyleDialog.BUTTON_NEGATIVE == which)
//				{
//					dialog.dismiss();
//				} else if (IOSStyleDialog.BUTTON_POSITIVE == which)
//				{
//					DataManager.getIns().delContact(pc);
//					dialog.dismiss();
//
//					if (null != contactDetailPanel)
//					{
//						AnimationUtil.slideOutToRight(contactDetailPanel.getDetailView());
//					}
//				}
//			}
//		}
//
//	}
//
//	/**
//	 * 获取本地联系人适配器
//	 *
//	 * @return
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public ESpaceContactAdapter geteContactAdapter()
//	{
//		return eContactAdapter;
//	}
//
//	/**
//	 * 设置本地联系人适配器
//	 *
//	 * @param eContactAdapter
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public void seteContactAdapter(ESpaceContactAdapter eContactAdapter)
//	{
//		this.eContactAdapter = eContactAdapter;
//	}
//
//	/**
//	 * 获取企业联系人适配器
//	 *
//	 * @return
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public ESpaceContactAdapter geteEnterpriseAdapter()
//	{
//		return eEnterpriseAdapter;
//	}
//
//	/**
//	 * 设置企业联系人适配器
//	 *
//	 * @param eEnterpriseAdapter
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public void seteEnterpriseAdapter(ESpaceContactAdapter eEnterpriseAdapter)
//	{
//		this.eEnterpriseAdapter = eEnterpriseAdapter;
//	}
//
//	public ESpaceSearchAdapter geteSearchAdapter()
//	{
//		return eSearchAdapter;
//	}
//
//	public void seteSearchAdapter(ESpaceSearchAdapter eSearchAdapter)
//	{
//		this.eSearchAdapter = eSearchAdapter;
//	}
//
//	/**
//	 * 设置是否选择联系人
//	 *
//	 * @param isConfView
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public void setSelectContact(boolean isConfView)
//	{
//		//主叫呼集界面时 不能新建联系人导入导出联系人等
//		setTabTitleVisibility(!isConfView
//				&& curShowTab == 0);
////        tabTitle.setLabelSwitchEnable(!isConfView 
////                && curShowTab == 0);
//		if (null != eContactAdapter)
//		{
//			eContactAdapter.setConfView(isConfView);
//		}
//		if (null != eEnterpriseAdapter)
//		{
//			eEnterpriseAdapter.setConfView(isConfView);
//		}
//		if (null != eSearchAdapter)
//		{
//			eSearchAdapter.setConfView(isConfView);
//		}
//
//	}
//
//	/**
//	 * 获取会议选中联系人列表
//	 *
//	 * @return
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public List<PersonalContact> getConfragSelectedContacts()
//	{
//		return confragSelectedContacts;
//	}
//
//	/**
//	 * 入会号码列表
//	 *
//	 * @return
//	 * @history 2015-5-30    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public List<Terminal> getConfSelectNumber()
//	{
//		List<Terminal> contactNumberList = new ArrayList<Terminal>(0);
//		String confNumber = "";
//		PersonalContact pc = null;
//		String contactsFragSelfNumber = ConfigApp.getInstance().getVoipNumber();
//		int size = confragSelectedContacts.size();
//		for (int i = 0; i < size; i++)
//		{
//			pc = confragSelectedContacts.get(i);
//			confNumber = pc.getNumberOne();
//			if (confNumber.equals(contactsFragSelfNumber))
//			{
//				continue;
//			}
//			contactNumberList.add(getTerminal(pc));
//		}
//		Terminal terminal = new Terminal();
//		String contactsFragSipUri = ConfigApp.getInstance().getSipUriNotAnonymous();
//		String contactsFragServerIP = ConfigApp.getInstance().getServerIp();
//		if (StringUtil.isStringEmpty(contactsFragSipUri))
//		{
//			contactsFragSipUri = StringUtil.getSipUri(contactsFragSelfNumber, contactsFragServerIP);
//		}
//		terminal.setTerminalNumber(contactsFragSipUri);
//		contactNumberList.add(terminal);
//		return contactNumberList;
//	}
//
//	/**
//	 * 获取号码和类型
//	 */
//	private Terminal getTerminal(PersonalContact pc)
//	{
//		Terminal terminal = new Terminal();
//		terminal.setCallType(pc.getCallType());
//		terminal.setTerminalNumber(pc.getNumberOne());
//		return terminal;
//	}
//
//	/**
//	 * 设置会议选中联系人列表
//	 *
//	 * @param confragSelectedContacts
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public void setConfragSelectedContacts(
//			List<PersonalContact> confragSelectedContacts)
//	{
//		this.confragSelectedContacts = confragSelectedContacts;
//	}
//
//	/**
//	 * 清空选中的联系人
//	 *
//	 * @history 2015-4-2    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public void clearSelectedContacts()
//	{
//		if (null != confragSelectedContacts)
//		{
//			//选中入会列表 本地联系人 企业联系人 搜索界面选中联系人列表清空
//			confragSelectedContacts.clear();
//		}
//	}
//
//	/**
//	 * 刷新界面 搜索界面   本地联系人 企业联系人 选中入会的联系人
//	 *
//	 * @history 2015-4-3    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	public void notifyChange()
//	{
//		selectedContactsAdapter.notifyDataSetChanged();
//		eContactAdapter.notifyDataSetChanged();
//		eEnterpriseAdapter.notifyDataSetChanged();
//		eSearchAdapter.notifyDataSetChanged();
//	}
//
//	/**
//	 * 根据底层上抛的网络地址本类型，刷新不同风格的界面
//	 *
//	 * @history 2015-7-30    v1.0.0    x00348090    create
//	 * @since 1.1
//	 */
//	public void notifyContactsFragmentChange(boolean bLdapEnterprise)
//	{
//		LogUI.d("bLdapEnterprise:" + bLdapEnterprise);
//		bIsLoadingLdapEnterprise = false;
//		//当上抛的信息与当前一致时，说明是同一种界面风格，不刷新
//		if (bLdapEnterprise != isLdapEnterprise)
//		{
//			if (!bLdapEnterprise)
//			{
//				contactsListContain.setVisibility(View.VISIBLE);
//				if (ConfigApp.getInstance().isUsePadLayout())
//				{
//					rootView.findViewById(R.id.tabTitle).setVisibility(View.GONE);
////                    tabTitle = new TabTitle(rootView.findViewById(R.id.contacts_list_contain), this);
//				}
//				viewPageData.add(enterpriseView);
//			} else
//			{
//				contactsListContain.setVisibility(View.GONE);
//				rootView.findViewById(R.id.tabTitle).setVisibility(View.VISIBLE);
////                tabTitle = new TabTitle(rootView.findViewById(R.id.tabTitle), this);
//				contactViewPage.setAdapter(null);
//				if (1 < viewPageData.size())
//				{
//					viewPageData.remove(enterpriseView);
//				}
//				contactViewPage.setAdapter(contactPagerAdapter);
//				LdapManager.getIns().regLdapEventListen(this);
//			}
//			contactPagerAdapter.notifyDataSetChanged();
//			notifyChange();
//
//			//如果当前界面位于搜索界面，则重新从本地开始搜索
//			if (eSearchAdapter.isSearchView())
//			{
//				eSearchAdapter.loadData(DataManager.getIns().getContacts());
//				setSearchLdapEnterprise(false);
//				setFooterViewVisibility(true);
//				doSearch(Normalizer.normalize(searchContactsViewTxt.getText().toString(), Form.NFKC));
//			}
//
//			isLdapEnterprise = bLdapEnterprise;
//		}
//	}
//
//	/**
//	 * 进入搜索界面/退出搜索界面
//	 *
//	 * @param isInsearch
//	 * @history 2015-5-18    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	private void showSearchView(boolean isInsearch)
//	{
//		if (null != searchContactsList
//				&& View.VISIBLE == searchContactsList.getVisibility() && isInsearch)
//		{
//			LogUI.d("already in SearchMode do Nothing");
//			return;
//		}
//		if (null != searchContactsList)
//		{
//			searchContactsList.setVisibility(isInsearch ? View.VISIBLE : View.GONE);
//
//			if (0 == searchContactsViewTxt.getText().toString().length())
//			{
//				searchContactsList.setVisibility(View.GONE);
//				localContactsView.setVisibility(isInsearch ? View.GONE : View.VISIBLE);
//				if (!ConfigApp.getInstance().isLdapEnterprise())
//				{
//					setenterpriseViewAndTabVisibility(!isInsearch);
//				}
//			}
//		}
//		if (null != searchContactsView)
//		{
//			searchContactsView.setVisibility(isInsearch ? View.VISIBLE : View.GONE);
//		}
//		if (isInsearch)
//		{
//			searchContactsViewTxt.requestFocus();
//			if (0 != searchContactsViewTxt.getText().toString().length())
//			{
//				eSearchAdapter.loadData(DataManager.getIns().getContacts());
//			}
//			Timer timer = new Timer();
//			timer.schedule(new InputMethodTask(searchContactsViewTxt), 500);
//		}
//		eSearchAdapter.setSearchView(isInsearch);
//		clearSearchCondition();
//	}
//
//	public SelectedContactAdapter getSelectedContactsAdapter()
//	{
//		return selectedContactsAdapter;
//	}
//
//	public void setSelectedContactsAdapter(
//			SelectedContactAdapter selectedContactsAdapter)
//	{
//		this.selectedContactsAdapter = selectedContactsAdapter;
//	}
//
//	@Override
//	public void inSearchMode()
//	{
//		setTabTitleVisibility(false);
////        tabTitle.setLabelSwitchEnable(false);
//		showSearchView(true);
//	}
//
//	/**
//	 * 清空搜索条件
//	 */
//	public void clearSearchCondition()
//	{
//		searchContactEditor.clearSearchCondition();
//	}
//
//	/**
//	 * 匹配该联系人是否选中
//	 *
//	 * @param pc
//	 * @history 2015-06-09    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	private boolean ifIsSelected(PersonalContact pc)
//	{
//		boolean isContain = false;
//		int contactCount = confragSelectedContacts.size();
//		PersonalContact personContact = null;
//		//名称和号码都相同 则为选中
//		for (int i = 0; i < contactCount; i++)
//		{
//			personContact = confragSelectedContacts.get(i);
//			if (pc.getNumberOne().equals(personContact.getNumberOne())
//					&& pc.getCallType() == personContact.getCallType())
//			{
//				isContain = true;
//				break;
//			}
//		}
//		return isContain;
//	}
//
//	/**
//	 * 在选中的联系人中删除号码和名字与点击联系人相同的联系人
//	 *
//	 * @param pc 点击联系人
//	 * @history 2015-6-9    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	private void removeContactsInList(PersonalContact pc)
//	{
//		int index = -1;
//		int contactsCount = confragSelectedContacts.size();
//		PersonalContact personalContact = null;
//		for (int i = 0; i < contactsCount; i++)
//		{
//			personalContact = confragSelectedContacts.get(i);
//			if (pc.getNumberOne().equals(personalContact.getNumberOne())
//					&& pc.getCallType() == personalContact.getCallType())
//			{
//				index = i;
//				break;
//			}
//		}
//		if (-1 == index)
//		{
//			return;
//		}
//		confragSelectedContacts.remove(index);
//	}
//
//	/**
//	 * 点击查找企业通讯录
//	 *
//	 * @param
//	 * @history 2015-6-27    v1.0.0    x00347090    create
//	 * @since 1.1
//	 */
//	@Override
//	public void onLoadMoreItemClick()
//	{
//		setSearchEnterpriseVisibility(false);
//		ldapSearchResultList.clear();
//		if (ConfigApp.getInstance().isLdapEnterprise())
//		{
//			//离线不支持搜索
//			if (Constant.STATUS_OFFLINE.equals(EspaceApp.getIns().getOnlineStatus()))
//			{
//				setFooterViewVisibility(false);
//				return;
//			}
//
//			setFootLoadingVisibility(true);
//			bIsLoadingLdapEnterprise = true;
//
//			//设置搜索LDAP企业联系人
//			setSearchLdapEnterprise(true);
//			iLdapSearchSeqNo = LdapManager.getIns().ldapSearchByKey(searchContactsViewTxt.getText().toString(), iLdapSearchPage);
//			if (0 >= iLdapSearchSeqNo)
//			{
//				setFooterViewVisibility(false);
//				bIsLoadingLdapEnterprise = false;
//				LogUI.e("ldap search fail.");
//			}
//		} else
//		{
//			//如果FTP地址本下载完成，则开始搜索，否则直接return，防止在ftp地址本没有下载完成的情况下搜索出现界面崩溃的现象
//			if (!isEnterpriseLoading)
//			{
//				eSearchAdapter.loadData(DataManager.getIns().getAddressBook());
//				//获取当前搜索条件，进行搜索
//				doSearch(Normalizer.normalize(searchContactsViewTxt.getText().toString(), Form.NFKC));
//			} else
//			{
//				setFooterViewVisibility(false);
//				LogUI.e("enterprise has not loaded end.");
//			}
//		}
//	}
//
//	/**
//	 * 设置eSearchAdapter数据
//	 *
//	 * @param
//	 * @history 2015-6-27    v1.0.0    x00347090    create
//	 * @since 1.1
//	 */
//	@Override
//	public void setSearchAdapterLocalData()
//	{
//		// TODO Auto-generated method stub
//		bIsLoadingLdapEnterprise = false;
//		if (0 != searchContactsViewTxt.getText().toString().length())
//		{
//			eSearchAdapter.loadData(DataManager.getIns().getContacts());
//			searchContactsList.setSelection(0);
//		}
//		if (ConfigApp.getInstance().isLdapEnterprise())
//		{
//			iLdapSearchSeqNo = -1;
//			iLdapSearchPage = 0;
//			setSearchLdapEnterprise(false);
//		}
//	}
//
//	/**
//	 * 加载下一页数据
//	 *
//	 * @param
//	 * @history 2015-7-29    v1.0.0    x00347090    create
//	 * @since 1.1
//	 */
//	public void onLoadNextPage()
//	{
//		iLdapSearchSeqNo = LdapManager.getIns().ldapSearchByKey(searchContactsViewTxt.getText().toString(), ++iLdapSearchPage);
//		if (0 >= iLdapSearchSeqNo)
//		{
//			setFooterViewVisibility(false);
//			bIsLoadingLdapEnterprise = false;
//			LogUI.e("ldap search fail.");
//			return;
//		}
//	}
//
//	/**
//	 * 设置footerview是否可见
//	 *
//	 * @param
//	 * @history 2015-7-29    v1.0.0    x00347090    create
//	 * @since 1.1
//	 */
//	@Override
//	public void setFooterViewVisibility(boolean isVisible)
//	{
//		this.footView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
//		if (isVisible)
//		{
//			this.footView.setPadding(0, 0, 0, 0);
//			setFootLoadingVisibility(isSearchLdapEnterprise());
//			setSearchEnterpriseVisibility(!isSearchLdapEnterprise());
//		} else
//		{
//			this.footView.setPadding(0, -footView.getHeight(), 0, 0);
//		}
//	}
//
//	//设置“正在加载”是否可见
//	public void setFootLoadingVisibility(boolean isVisible)
//	{
//		this.footLoading.setVisibility(isVisible ? View.VISIBLE : View.GONE);
//	}
//
//	//设置“点击查找企业通讯录”是否可见
//	public void setSearchEnterpriseVisibility(boolean isVisible)
//	{
//		this.searchEnterprise.setVisibility(isVisible ? View.VISIBLE : View.GONE);
//	}
//
//	/**
//	 * 滑动，上拉加载更多的监  听类
//	 *
//	 * @param
//	 * @history 2015-7-26    v1.0.0    x00347090    create
//	 * @since 1.1
//	 */
//	private class OnLdapScrollListener implements OnScrollListener
//	{
//		// 是否滚动到最后一行
//		private boolean isLastRow = false;
//
//		public void onScrollStateChanged(AbsListView view, int scrollState)
//		{
//			if (!isSearchLdapEnterprise())
//			{
//				LogUI.i("contactsFragment isSearchLdapEnterprise is false,return");
//				return;
//			}
//
//			if (!isLastRow)
//			{
//				LogUI.i("contactsFragment isLastRow is false,return");
//				return;
//			}
//
//
//			//如果查询回调回来的数据不是最后一页，则显示“正在加载”，继续下一次查询
//			if (!bLastPage)
//			{
//				if (!bIsLoadingLdapEnterprise)
//				{
//					LogUI.i("contactsFragment bIsLoadingLdapEnterprise is true,return");
//					setFooterViewVisibility(true);
//					onLoadNextPage();
//					bIsLoadingLdapEnterprise = true;
//				}
//			} else
//			{
//				setFooterViewVisibility(false);
//			}
//
//			isLastRow = false;
//		}
//
//		public void onScroll(AbsListView view, int firstVisibleItem,
//		                     int visibleItemCount, int totalItemCount)
//		{
//			if (isSearchLdapEnterprise())
//			{
//				//离线不支持搜索
//				if (Constant.STATUS_OFFLINE.equals(EspaceApp.getIns().getOnlineStatus()))
//				{
//					setFooterViewVisibility(false);
//					return;
//				}
//
//				//如果正在加载，滑动屏幕不产生搜索查询的操作
//				if (bIsLoadingLdapEnterprise)
//				{
//					return;
//				}
//				//滚动到最后一行
//				if ((firstVisibleItem + visibleItemCount == totalItemCount) && (totalItemCount > 0))
//				{
//					isLastRow = true;
//				}
//			}
//		}
//	}
//
//	/**
//	 * 收到搜索数据，刷新搜索界面
//	 *
//	 * @param bLastPageFlag
//	 * @param
//	 * @history 2015-7-29    v1.0.0    x00347090    create
//	 * @since 1.1
//	 */
//	private void refreshLdapSearchResult(List<PersonalContact> ldapSearchResult, boolean bLastPageFlag)
//	{
//		int resultcount = ldapSearchResultList.size();
//		ldapSearchResultList.addAll(ldapSearchResult);
//		bIsLoadingLdapEnterprise = false;
//		bLastPage = bLastPageFlag;
//		//将搜索的联系人设置为企业分组
//		if ((ldapSearchResultList.size() > 0) && (0 == resultcount))
//		{
//			PersonalContact contactsPersonalContact = ldapSearchResultList.get(0);
//			if (null != contactsPersonalContact)
//			{
//				contactsPersonalContact.setGroupString(Resource.ENTERPTISE);
//			}
//		}
//		eSearchAdapter.loadLdapData(ldapSearchResultList);
//		if (bLastPage)
//		{
//			setFooterViewVisibility(false);
//		}
//		eSearchAdapter.notifyDataSetChanged();
//		if (0 == iLdapSearchPage)
//		{
//			//第一次搜索回来的数据要置顶显示
//			searchContactsList.setSelection(0);
//		}
//	}
//
//	@Override
//	public void onLdapSearchResult(int iSeqNo,
//	                               final List<PersonalContact> searchResultList, final boolean bLastPageFlag)
//	{
//		LogUI.d("ldap onLdapSearchResult, iSeqNo: " + iSeqNo + " searchSeqNo:" + iLdapSearchSeqNo);
//		LogUI.d("ldap onLdapSearchResult, result size: " + searchResultList.size());
//		if (iSeqNo != iLdapSearchSeqNo)
//		{
//			return;
//		}
//		getActivity().runOnUiThread(new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				refreshLdapSearchResult(searchResultList, bLastPageFlag);
//			}
//		});
//
//	}
//
//	/**
//	 * 设置顶部title可见性
//	 *
//	 * @param enable
//	 * @history 2015-8-20    v1.0.0    pWX178217    create
//	 * @since 1.1
//	 */
//	private void setTabTitleVisibility(boolean enable)
//	{
//		if (null != tabTitle)
//		{
//			tabTitle.setLabelSwitchEnable(enable);
//		}
//		if (null != tabTitleLdap)
//		{
//			tabTitleLdap.setLabelSwitchEnable(enable);
//		}
//	}
//
//	@Override
//	public void setSearchContactsListVisibility(boolean isVisible)
//	{
//		// TODO Auto-generated method stub
//		searchContactsList.setVisibility(isVisible ? View.VISIBLE : View.GONE);
//		localContactsView.setVisibility(View.GONE);
//		if (!ConfigApp.getInstance().isLdapEnterprise())
//		{
//			setenterpriseViewAndTabVisibility(false);
//		}
//	}
//
//	/**
//	 * 退出搜索，显示通讯录界面
//	 *
//	 * @param enable
//	 * @history 2015-9-07    v1.0.0    x00347090    create
//	 * @since 1.1
//	 */
//	public void showContactsView()
//	{
//		exitSearchMode();
//		localContactsView.setVisibility(View.VISIBLE);
//		if (!ConfigApp.getInstance().isLdapEnterprise())
//		{
//			setenterpriseViewAndTabVisibility(true);
//		}
//	}
//
//	private void setenterpriseViewAndTabVisibility(boolean isVisible)
//	{
//		enterpriseView.setVisibility(isVisible ? View.VISIBLE : View.GONE);
//		contactsListContain.setVisibility(isVisible ? View.VISIBLE : View.GONE);
//	}
//
//	@Override
//	public boolean isLdapEnterpriseSearching()
//	{
//		// TODO Auto-generated method stub
//		return bIsLoadingLdapEnterprise;
//	}
}
