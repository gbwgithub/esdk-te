package com.huawei.te.example.contacts.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.common.PersonalContact;
import com.huawei.esdk.te.util.LogUtil;
import com.huawei.manager.DataManager;

import java.util.List;

/**
 * 类描述：联系人数据适配器类
 */
public class ESpaceContactAdapter extends AbsEspaceAdapter
{
    private static final String TAG = ESpaceContactAdapter.class.getSimpleName();
    /**
     * 在界面上显示的数据列表(本地、企业或搜索的的数据)
     */
    private List<PersonalContact> contacts = null;
    
    
    /**
     * 构造函数
     * @param context 设备上下文
     * @param itemClickCallBack 联系人项点击回调
     * @param viewType 显示类型
     */
    public ESpaceContactAdapter(Context context,
            AbsEspaceAdapter.ContactServer itemClickCallBack, int viewType)
    {
        super(context, itemClickCallBack, viewType);
        isInConfView = true;
    }

    /**
     * 设置数据
     * @since  1.1 
     * @history  
     * 2013-9-26    v1.0.0    cWX176935    create
     */
    public void loadData()
    {
        if (viewType == AbsEspaceAdapter.TYPE_ENTERPRISE_CONTACT)
        {
            contacts = DataManager.getIns().getAddressBook();
        }
        else if(viewType == AbsEspaceAdapter.TYPE_CONTACT)
        {
            contacts = DataManager.getIns().getContacts();
        }
//        filterAllData = contacts;
//        filterList = contacts;
    }
    
    /**
     * 获得一项的联系人
     * @param position position
     * @return 联系人
     * @since 1.1 
     * @history  
     * 2013-8-6    v1.0.0    cWX176935    create
     */
    @Override
    public PersonalContact getItem(int position)
    {
        //begin add by cwx176935 reason: ANDRIOD-148 联系人未作判空处理
        if (null == contacts)
        {
            return null;
        }
        //end add by cwx176935 reason: ANDRIOD-148 联系人未作判空处理
        try
        {
            return contacts.get(position);
        }
        catch (IndexOutOfBoundsException e)
        {
            LogUtil.e(TAG, "error.");
        }
        return null;
    }
    

    /**
     * 得到列表的个数
     * @return 列表个数
     * @since  1.1 
     * @history  
     * 2013-8-14    v1.0.0    cWX176935    create
     */
    public int getCount()
    {
        //begin add by cwx176935 reason: ANDRIOD-148 联系人未作判空处理
        if (null == contacts)
        {
            return 0;
        }
        //end add by cwx176935 reason: ANDRIOD-148 联系人未作判空处理
        return contacts.size();
    }

    
    /**
     * 得到一项的id
     * @param position position
     * @return position
     * @since  1.1 
     * @history  
     * 2013-8-6    v1.0.0    cWX176935    create
     */
    public long getItemId(int position)
    {
        return position;
    }


    /**
     * 得到二级菜单的Id
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
     * 得到子项
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
    
    

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (null == contacts || contacts.isEmpty())
        {
            return null;
        }
        return super.getView(position, convertView, parent);
    }

    /**
     *
     * @return
     * @since 1.1 
     * @history  
     * 2013-9-5    v1.0.0    cWX176935    create
     */
    @Override
    public List<PersonalContact> getItems()
    {
        return contacts;
    }
    
    /**
     * 获得grouptype
     * @param groupPosition
     * @return
     * @since 1.1 
     * @history  
     * 2013-9-27    v1.0.0    cWX176935    create
     */
    public char getGroupTypeC(int groupPosition)
    {
        if (null == contacts || viewType != AbsEspaceAdapter.TYPE_ENTERPRISE_CONTACT)
        {
            return 0;
        }
        if (contacts.isEmpty())
        {
            return 0;
        }
        try
        {
            if (' ' == contacts.get(groupPosition).getNameCode())
            {
                return 0;
            }
            return contacts.get(groupPosition).getNameCode();
        }
        catch (IndexOutOfBoundsException e)
        {
            LogUtil.e(TAG, "getGroupTypeC error.");
        }
        return 0;
    }
    
    //=======================搜索逻辑========================

    @Override
    public String getContactGroup(int position)
    {
        return "";
    }
}
