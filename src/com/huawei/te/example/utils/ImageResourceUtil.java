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

package com.huawei.te.example.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

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
