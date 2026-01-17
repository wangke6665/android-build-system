package com.shenma.tvlauncher.network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Volley 网络请求工具类
 * 统一管理支持 Android 4.4 TLS 1.2 的 RequestQueue
 * 解决 Android 4.4 及以下版本的 TLS 兼容性问题
 */
public class VolleyHelper {
    
    /**
     * 创建支持 TLS 1.2 的 RequestQueue
     * 在 Android 4.4 及以下版本会自动使用支持 TLS 1.2 的 ExtHttpStack
     * 
     * @param context 上下文
     * @return RequestQueue
     */
    public static RequestQueue newRequestQueue(Context context) {
        return Volley.newRequestQueue(context, new ExtHttpStack());
    }
}

