package com.shenma.tvlauncher;

import com.shenma.tvlauncher.utils.NativeHelper;

public class Api {

    /*云Cos地址 - 从SO库获取*/
    public static String COS_MIAN_URL = NativeHelper.getCosUrl();
    /*主控地址 - 从SO库获取*/
    public static String MIAN_URL = NativeHelper.getMainUrl();
    /*APPID - 从SO库获取*/
    public static String APPID = NativeHelper.getAppId();
}
