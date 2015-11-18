package com.huawei.te.example.utils;

import java.io.IOException;
import java.io.InputStream;

import com.huawei.esdk.te.data.Constants;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Copyright (C) 2008-2013 华为技术有限公司(Huawei Tech.Co.,Ltd)
 */
public class ImageResourceUtil
{
	
	private static final String TAG = ImageResourceUtil.class.getSimpleName();
    private static ImageResourceUtil ins;
    public static ImageResourceUtil getIns()
    {
        if (null == ins)
        {
            ins = new ImageResourceUtil();
        }
        return ins;
    }
    public Bitmap readBitMap(Context context, int resId)
    {
        InputStream is = null;
        try
        {
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            //获取资源图片  
            is = context.getResources().openRawResource(resId);
            return BitmapFactory.decodeStream(is, null, opt);
        }
        catch (NotFoundException e)
        {
            Log.e(TAG, "Progress get an NotFoundException");
        }
        finally
        {
            closeInputStream(is);
            
        }
        return null;
    }
    
    /**
     * @param is
     */
    private void closeInputStream(InputStream is)
    {
        if (null == is)
        {
            return;
        }
        
        try
        {
            is.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, "Progress get an IOException");
        }
    }
}
