package com.shenma.tvlauncher.utils;

/**
 * Native层辅助类
 * 对接API信息存储在SO库中，通过JNI调用获取
 */
public class NativeHelper {
    
    // 加载SO库
    static {
        System.loadLibrary("native-lib");
    }
    
    /**
     * 获取主控地址（加密的）
     */
    public static native String getMainUrl();
    
    /**
     * 获取COS地址（加密的）
     */
    public static native String getCosUrl();
    
    /**
     * 获取APPID
     */
    public static native String getAppId();
}
