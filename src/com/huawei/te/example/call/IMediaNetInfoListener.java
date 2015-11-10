package com.huawei.te.example.call;

import com.huawei.voip.data.MediaNetInfo;

public interface  IMediaNetInfoListener
{
    void onMediaNetInfoChange(MediaNetInfo netInfo);
}
