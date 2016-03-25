package com.huawei.te.example.contacts;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.huawei.common.CallRecordInfo;
import com.huawei.common.PersonalContact;
import com.huawei.te.example.R;

public class ContactRefreshUtil
{
    /**
     * 根据联系人状态获取相应的图片
     * @param pc 既有联系人也有通话记录的，状态根据联系人取
     */
    public static Drawable getContactStateDrawable(Context mContext,PersonalContact pc,CallRecordInfo calliInfo)
    {
        if(null == pc && null == calliInfo){
            return mContext.getResources().getDrawable(R.drawable.te_mobile_home_state_offline);
        }
        int stateId = 0;
        switch (null != pc ? pc.getStatePresence() : calliInfo.getStatePresence())
        {
            case PersonalContact.ON_LINE:
                stateId = R.drawable.te_mobile_home_state_online;
                break;
            case PersonalContact.BUSY:
                stateId = R.drawable.te_mobile_home_state_busy;
                break;
            case PersonalContact.AWAY://离线
                stateId = R.drawable.te_mobile_home_state_offline;
                break;
            default:
                stateId = R.drawable.te_mobile_home_state_offline;
                break;
        }
        return mContext.getResources().getDrawable(stateId);
    }
    
}
