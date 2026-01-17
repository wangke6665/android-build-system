package com.forcetech.android;


/**
 * @author zengwenman
 * @brief 加密地址获取
 */
public class MdUrl {
    static {
        System.loadLibrary("md");
    }

    private static native String getMdUrl();

    public String getUrl() {
        return getMdUrl();
    }
}
