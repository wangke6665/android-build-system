package com.forcetech.android;

import android.util.Log;

/**
 * @author zengwenamn
 * @brief 主接口地址获取
 */

public class MainUrl {

    static {
        System.loadLibrary("Url");
    }

    private static native String getUrl();

    public String interfa() {
        Log.d("joychang", "getUrl()=" + getUrl());
        return getUrl();
    }

}
