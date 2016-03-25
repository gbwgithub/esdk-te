package com.huawei.te.example.contacts.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.huawei.common.PersonalContact;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.manager.DataManager;
import com.huawei.te.example.R;
import com.huawei.te.example.contacts.ContactsFragmentRewrite;
import com.huawei.utils.HanYuPinYin;
import com.huawei.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
  * Function: 联系人搜索 Adapter
  */
public class ESpaceSearchAdapter extends AbsEspaceAdapter implements Filterable, ContactsFragmentRewrite.ContactLoadState
{
    private static final String TAG = ESpaceSearchAdapter.class.getSimpleName();
    /**
     * 联系人列表
     */
    private static final int CONTACT_TYPE = 0;

    /**
     * 搜索结果为空
     */
    private static final int SEARCH_ENMPTY = -1;

    /**
     * 搜索或者过滤条件
     */
    private String searchCondition;

    /**
     * 搜索过滤器
     */
    private SearchFilter mFilter;

    /**
     * 过滤的联系人列表
     */
    private List<PersonalContact> filterList = new ArrayList<PersonalContact>(0);
    
    /**
     * 用于搜索
     */
    private List<PersonalContact> filterAllData;
    
    
    /**
     * 前一次搜索条件
     */
    private String prefixCondition = "";
    
    /**
     * true 重新搜索 
     */
    private boolean isRefilter = false;
    
    /**
     * 是否正在加载数据
     */
    private boolean isInLoading = false;
    
    private final byte[] filterListLock = new byte[0];    
    
    /**
     * 构造方法
     * @param context 设备上下文
     * @param itemClickCallBack 联系人项点击回调接口实例对象
     */
    public ESpaceSearchAdapter(Context context, ContactServer itemClickCallBack)
    {
        super(context, itemClickCallBack);
    }
    
    /**
     * 构造方法
     * @param context 设备上下文
     * @param itemClickCallBack 联系人项点击回调接口实例对象
     * @param viewType 要显示的类型
     */
    public ESpaceSearchAdapter(Context context, ContactServer itemClickCallBack, int viewType)
    {
        super(context, itemClickCallBack, viewType);
    }
    
    /**
     * 添加数据
     */
    public void loadData(List<PersonalContact> contacts)
    {
        synchronized (filterListLock)
        {
            filterAllData = contacts;
            filterList.clear();
        }
    } 
    
    public void loadLdapData(List<PersonalContact> contacts)
    {
        synchronized (filterListLock)
        {
            filterList = contacts;
        }
    }
    
    /**
     * 重置过滤数据 回到本地搜索
     */
    private void resetFilterData()
    {
        synchronized (filterListLock)
        {
            filterList.clear();
        }
        searchCondition = "";
    }

    /**
     * 退出搜索模式
     */
    public void clear()
    {
        new Thread(new Runnable()
        {
            
            @Override
            public void run()
            {
                resetFilterData(); 
            }
        }).start();
       
        notifyDataSetChanged();
    }

    /**
     *过滤联系人
     * @param condition 过滤条件字符串
     * @since  1.1 
     * @history  
     * 2013-8-6    v1.0.0    cWX176935    create
     */
    public void filter(String condition)
    {
        if (null == condition)
        {
            return;
        }
        isRefilter = true;
        this.searchCondition = condition;
        //begin add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
        if (isInLoading)
        {
            LogUtil.d(TAG, "is loading data can not do search");
            return;
        }
        //end add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
        getFilter().filter(searchCondition);
    }
    
    /**
     * 得到过滤器
     * @return Filter 过滤器
     */
    @Override
    public Filter getFilter()
    {
        if (mFilter == null)
        {
            mFilter = new SearchFilter();
        }
        return mFilter;
    }

    /***************************Filter******************************/
    /**
     * 
     * 需要重写设计
     * chifl
     * 类名称：SearchFilter
     * 类描述：搜索过滤器
     * 版权声明 : Copyright (C) 2008-2010 华为技术有限公司(Huawei Tech.Co.,Ltd)
     */
    private class SearchFilter extends Filter
    {

        /**
         * 通过字符序列过滤
         * @param prefix 字符序列
         * @return FilterResults 过滤结果集
         */
        @Override
        protected FilterResults performFiltering(CharSequence prefix)
        {
            synchronized (filterListLock)
            {
                if ((prefix == null || prefix.length() == 0))
                {
                    //情况搜索条件时不显示联系人
                    filterList.clear();
                    prefixCondition = "";
                }
                else
                {
                    String prefixStr = "";
                    prefixStr = prefix.toString();
                    //过滤联系人
                    int lastCount = filterList.size();
                    //如果上次搜索结果为空，且这次又是在上次基础上搜索，并且不是刚刚开始搜索，不执行搜索 
                    if (!isRefilter && lastCount == 0 && StringUtil.isNotEmpty(prefixCondition) 
                            && prefixStr.startsWith(prefixCondition) && prefixStr.length() != 1)
                    {
                        prefixCondition = prefixStr;
                        return null;
                    }
                    //如果是接着上次的搜索，则直接在上次搜索结果里去查找
                    if (StringUtil.isNotEmpty(prefixCondition) && prefixStr.startsWith(prefixCondition) && !isRefilter)
                    {
                        filterList = DataManager.getIns().filterContacts(prefix.toString(), filterList);
                    }
                    else
                    {
                        filterList = DataManager.getIns().filterContacts(prefix.toString(), filterAllData);
                    }
                    prefixCondition = prefixStr; 
                    //会议界面搜索联系人 需要添加一个以搜索条件为名字和号码的联系人
                    if (isInConfView)
                    {
                        PersonalContact pc = new PersonalContact();
                        pc.setName(prefix.toString());
                        pc.setnumber1(prefix.toString());
                        filterList.add(0, pc);
                    }
                }
                
            }
            return null;
        }

        /**
         * 通过字符序列和过滤结果集刷新结果
         * @param constraint 字符序列
         * @param results 过滤结果集
         */
        @Override
        protected void publishResults(CharSequence constraint,
                FilterResults results)
        {
            notifyDataSetChanged();
            isRefilter = false;
        }

    }

    /**
     * 得到个数
     * @return 个数
     * @since  1.1 
     * @history  
     * 2013-8-6    v1.0.0    cWX176935    create
     */
    public synchronized int getCount()
    {
        synchronized (filterListLock)
        {
            return filterList.size();
        }
    }

    /**
     * 得到view的类型
     * @param position position
     * @return 类型
     * @since  1.1 
     * @history  
     * 2013-8-6    v1.0.0    cWX176935    create
     */
    public int getItemViewType(int position)
    {
        synchronized (filterListLock)
        {
            if (filterList.isEmpty())
            {
                return SEARCH_ENMPTY;
            }            
        }
        return CONTACT_TYPE;
    }

    /**
     * 得到显示的view
     * @param position position
     * @param convertView 缓冲
     * @param parent 父视图
     * @return view
     * @since 1.1 
     * @history  
     * 2013-8-6    v1.0.0    cWX176935    create
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        switch (getItemViewType(position))
        {
            case CONTACT_TYPE:

                return super.getView(position, convertView, parent);

            case SEARCH_ENMPTY:

                return mInflater.inflate(R.layout.contact_footer_item_layout,
                        null);
            default:
                return null;
        }
        
    }

    /**
     *获取List列表对应的数据
     *列表项中有  title  search 项 ，需要特别处理 非联系人列表Item
     * @param position 位置
     * @return 联系人
     * @since 1.1 
     * @history  
     * 2013-8-6    v1.0.0    cWX176935    create
     */
    @Override
    public PersonalContact getItem(int position)
    {
        synchronized (filterListLock)
        {
            if (null == filterList)
            {
                return null;
            }
            return filterList.get(position);
        }
    }

    /**
     * 得到对应项的值
     * @param groupPosition groupPosition
     * @param childPosition childPosition
     * @return null
     * @since 1.1 
     * @history  
     * 2013-8-2    v1.0.0    cWX176935    create
     */
    @Override
    public Object getChild(int groupPosition, int childPosition)
    {
        return null;
    }

    /**
     * 得到子项的id
     * @param groupPosition groupPosition
     * @param childPosition childPosition
     * @return 0
     * @since 1.1 
     * @history  
     * 2013-8-2    v1.0.0    cWX176935    create
     */
    @Override
    public long getChildId(int groupPosition, int childPosition)
    {
        return 0;
    }

    /**
     * 得到项的个数
     * @return 个数
     * @since 1.1 
     * @history  
     * 2013-8-2    v1.0.0    cWX176935    create
     */
    @Override
    public int getGroupCount()
    {
        return getCount();
    }

    /**
     * 得到groupId
     * @param groupPosition groupPosition
     * @return 0
     * @since 1.1 
     * @history  
     * 2013-8-2    v1.0.0    cWX176935    create
     */
    @Override
    public long getGroupId(int groupPosition)
    {
        return groupPosition;
    }

    /**
     * 得到Group类型
     * @param groupPosition groupPosition
     * @return 类型
     * @since 1.1 
     * @history  
     * 2013-8-2    v1.0.0    cWX176935    create
     */
    @Override
    public int getGroupType(int groupPosition)
    {
        return getItemViewType(groupPosition);
    }
    
    
    /**
     * 获得列表
     * @return 得到items
     * @since 1.1 
     * @history  
     * 2013-9-5    v1.0.0    cWX176935    create
     */
    @Override
    public List<PersonalContact> getItems()
    {
        synchronized (filterListLock)
        {
            return filterList;
        }
    }

    /**
    * 联系人过滤 
    * @param prefix 关键词
    * @param values 匹配数据
    * @return 返回过滤结果
    */
    public List<PersonalContact> filterContacts(String prefix,
            List<PersonalContact> values)
    {
        boolean unsatisfyCond = (null == values || StringUtil.isStringEmpty(prefix));
        if (unsatisfyCond)
        {
            return null;
        }        
        String prefixString = prefix.toLowerCase(Locale.ENGLISH);
        int count = values.size();
        ArrayList<PersonalContact> newValues = new ArrayList<PersonalContact>(count);
        StringBuffer buf = new StringBuffer();
        String name = null;
        String nativeName = null;
        String namePinyin = "";
        PersonalContact value = null;
        int i = 0;
        StringBuffer chinese = new StringBuffer();
        while (i<count)
        {
            value = values.get(i);
            i++;
            if (value == null)
            {
                continue;
            }
            buf.setLength(0);
            name = value.getName();
            
            //先挑选其中的汉字，再将汉字转换为拼音
            chinese.setLength(0);
            chinese = chinese.append(HanYuPinYin.selectIsoLateChinese(name));
            namePinyin = HanYuPinYin.toHanYuPinyin(chinese.toString());
            
            //begin modify by cwx176935 reason: DTS2013112714661 搜索出多余的联系人
            //  如果name不为空, name 会包含中文和英文。  匹配   中文字符，中文字符首字母，全拼 。
            if (!StringUtil.isStringEmpty(name))  
            {
                name = name.toLowerCase(Locale.ENGLISH);
                buf.append(name).append(Constants.CHARACTER_MARK.BLANK_MARK);
            }
            nativeName = value.getNativeName();
            if (!StringUtil.isStringEmpty(nativeName))
            {
                nativeName = nativeName.toLowerCase(Locale.ENGLISH);
                buf.append(nativeName).append(Constants.CHARACTER_MARK.BLANK_MARK);
            }
            matchStringAppend(buf, value);
            //end modify by cwx176935 reason: DTS2013112714661 搜索出多余的联系人
            //如果是多条件 需要多条件包含
            //根据多分组的需求，需要做过滤结果的唯一性判断
            if (!newValues.contains(value))
            {
                boolean judge = matchNamePinyin(namePinyin, prefixString.trim())/**汉语拼音搜索*/
                        || (buf.indexOf(prefixString.trim()) != -1) 
                        || matchNameEnglish(name, prefixString.trim());/**英文首字母搜索*/
                if (judge)     
                {
                    newValues.add(value);
                }
            }
        }
        return newValues;
    }
    
    /**
     * @param buf
     * @param value
     */
    private void matchStringAppend(StringBuffer buf, PersonalContact value)
    {
        if (!StringUtil.isStringEmpty(value.getNumberOne()))
        {
            buf.append(value.getNumberOne().toLowerCase(Locale.ENGLISH)).append(Constants.CHARACTER_MARK.BLANK_MARK);
        }
        if (!StringUtil.isStringEmpty(value.getEmail()))
        {
            buf.append(value.getEmail() .toLowerCase(Locale.ENGLISH)).append(Constants.CHARACTER_MARK.BLANK_MARK);
        }
        if (!StringUtil.isStringEmpty(value.getAddress()))
        {
            buf.append(value.getAddress() .toLowerCase(Locale.ENGLISH)).append(Constants.CHARACTER_MARK.BLANK_MARK);
        }
        if (!StringUtil.isStringEmpty(value.getDepartmentName()))
        {
            buf.append(value.getDepartmentName() .toLowerCase(Locale.ENGLISH)).append(Constants.CHARACTER_MARK.BLANK_MARK);
        }
        if (!StringUtil.isStringEmpty(value.getMobilePhone()))
        {
            buf.append(value.getMobilePhone() .toLowerCase(Locale.ENGLISH)).append(Constants.CHARACTER_MARK.BLANK_MARK);
        }
        if (!StringUtil.isStringEmpty(value.getOfficePhone()))
        {
            buf.append(value.getOfficePhone() .toLowerCase(Locale.ENGLISH)).append(Constants.CHARACTER_MARK.BLANK_MARK);
        }
    }
    
    /**
     * 英文名字匹配名字匹配
     * @param name 名字
     * add by l00220604 204-03-21 Reason: DTS2014032009101  DTS2014032009109 输入搜索条件，结果有误
     * @return true 匹配 false 不匹配
     */
    private boolean matchNameEnglish(String name, String part)
    {
        if (StringUtil.isStringEmpty(name) || StringUtil.isStringEmpty(part))
        {
            return false;
        }

        StringBuilder firstLetter = new StringBuilder();
        boolean isFirstLetter = true;  //第一个字符默认是首字母，0代表不是
        char indexChar = 0;
        int nameLength = name.length();
        for (int i = 0; i < nameLength; i++)
        {
            indexChar = name.charAt(i);
            
            if (isFirstLetter)
            {
                if (indexChar == ' ')
                {
                    continue;
                }
                
                isFirstLetter = false;
                //如果不是字母，则进入下一个字符判断
                if ((indexChar < 'a') || (indexChar > 'z'))
                {
                    continue;
                }
                
                firstLetter.append(indexChar);
            }
            
            //如果为' '，则代表下一个字母是首字母
            if (indexChar == ' ')
            {
                isFirstLetter = true;
            }
        }
        
        if (firstLetter.indexOf(part) != -1)
        {
            return true;
        }

        return false;
    }
    
    /**
     * 拼音名字匹配
     * @param whole 名字
     * @param part 匹配字符
     * add by l00220604 204-03-21 Reason: DTS2014032009101  DTS2014032009109 输入搜索条件，结果有误
     * @return true 匹配 false 不匹配
     */
    private boolean matchNamePinyin(String whole, String part)
    {
        if (StringUtil.isStringEmpty(whole) || StringUtil.isStringEmpty(part))
        {
            LogUtil.d(TAG, "matchNamePinyin : null == whole or null == part");
            return false;
        }
        
        whole = whole.toLowerCase(Locale.getDefault());
        part = part.toLowerCase(Locale.getDefault());
        int wholeLength = whole.length();
        int partLength = part.length();
        //获取各个汉字首字母
        StringBuilder firstLetter = new StringBuilder();
        boolean isFirstLetter = true;  //第一个字符默认是首字母，0代表不是
        int size = whole.length();
        for (int i = 0; i < size; i++)
        {
            if (isFirstLetter)
            {
                firstLetter.append(whole.charAt(i));
                isFirstLetter = false;
            }
            
            //如果为$，则代表下一个字母是首字母
            if (whole.charAt(i) == '$')
            {
                isFirstLetter = true;
            }
        }
        
        if (firstLetter.indexOf(part) != -1)
        {
            return true;
        }
        
        //去除$后开始字串匹配,part只有事首字母开头的字串，才匹配出来
        isFirstLetter = true;
        for (int k = 0; k < wholeLength; k++)
        {
            if (isFirstLetter)
            {
                //比较part是否是现在这个首字母开头的子串
                int j = k;   //whole首字符开头的下标
                int l = 0;   //part的下标
                while ((j < wholeLength) && (l < partLength))
                {
                    if (whole.charAt(j) == '$')
                    {
                        j++;
                        continue;
                    }
                    
                    //有不相同的，就不用比较了
                    if (whole.charAt(j) != part.charAt(l))
                    {
                        break;
                    }
                    j++;
                    l++;
                }
                if (l == part.length())   //表面有满足条件的匹配
                {
                    return true;
                }
                               
                isFirstLetter = false;
            }
            
            //如果为$，则代表下一个字母是首字母
            if (whole.charAt(k) == '$')
            {
                isFirstLetter = true;
            }
        }

        return false;
    }
    
    //begin add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
    /**
     * 正在加载联系人
     * @since 1.1 
     * @history  
     * 2013-11-13    v1.0.0    cWX176935    create
     */
    @Override
    public void isLoading()
    {
        isInLoading = true;
    }

    /**
     * 联系人加载完成
     * @since 1.1 
     * @history  
     * 2013-11-13    v1.0.0    cWX176935    create
     */
    @Override
    public void loadEnd(boolean isLocal)
    {
        isInLoading = false;
        if (null != searchCondition && !searchCondition.isEmpty())
        {
            filter(searchCondition);
        }
    }
    //end add by cwx176935 reason: DTS2013111209377 正在加载联系人过程中点击联系人界面中的搜索框，导致系统崩溃
    
    /**
     * 重新搜索
     * @since  1.1 
     * @history  
     * 2013-9-13    v1.0.0    cWX176935    create
     */
    public void reFilter()
    {
        filter(searchCondition);
    }

    /**
     * 
     * @param position
     * @return
     * @since 1.1 
     * @history  
     * 2013-9-27    v1.0.0    cWX176935    create
     */
    @Override
    public char getGroupTypeC(int position)
    {
        return 0;
    }

    @Override
    public String getContactGroup(int position)
    {
        synchronized (filterListLock)
        {
            if (null == filterList)
            {
                return "";
            }
            if (filterList.isEmpty())
            {
                return "";
            }
            try
            {
                return filterList.get(position).getGroupString();
            }
            catch (IndexOutOfBoundsException e)
            {
                LogUtil.e(TAG, "getContactGroup error.");
            }
            return "";
        }
    }
    
    /**
     * 刷新搜索联系人状态
     */
    public void refreshConfSearchState()
    {
        synchronized (filterListLock)
        {
            DataManager.getIns().matchPresenceState(filterList);
        }
    }

}
