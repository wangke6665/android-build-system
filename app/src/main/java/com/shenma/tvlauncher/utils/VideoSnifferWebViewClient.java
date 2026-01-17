package com.shenma.tvlauncher.utils;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;



public class VideoSnifferWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        //System.out.println("载入完毕:" + str);
    }

    @Override
    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        super.onPageStarted(webView, str, bitmap);
        //System.out.println("开始载入:" + str);
    }

    @Override
    public void onReceivedError(WebView webView, int i, String str, String str2) {
        super.onReceivedError(webView, i, str, str2);
        //System.out.println("载入失败:" + i + "," + str2);
    }

    @Override
    public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
        sslErrorHandler.proceed();
    }

//    @Override
//    public WebResourceResponse shouldInterceptRequest(WebView webView, String str) {
//        System.out.println("返回数据链接:" + str);
//        if ((!str.contains(".m3u8") && !str.contains(".mp4") && !str.contains("m3u8play.php?url=")) || str.contains("?url=")) {
//            return null;
//        }
//        System.out.println("返回数据链接:" + str + ",包含:.m3u8");
//
//        return super.shouldInterceptRequest(webView, str);
////        return null;
//    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        //System.out.println("网站所有URL=" + str);
        if (!str.contains("http://") && !str.contains("https://")) {
            //System.out.println("触发应用链接:" + str);
            return true;
        } else if (str.contains(".apk")) {
            //System.out.println("含有.apk");
            return true;
        } else {
            //System.out.println("浏览器即将跳转:" + str);
            return false;
        }
    }
}
