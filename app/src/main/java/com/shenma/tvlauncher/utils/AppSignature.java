package com.shenma.tvlauncher.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.util.Log;

import java.security.MessageDigest;

/**
 * 应用签名工具类
 * 用于获取APK签名，防止应用被重打包
 */
public class AppSignature {
    private static final String TAG = "AppSignature";
    private static String cachedSignature = null;
    private static String cachedPackageName = null;
    
    /**
     * 获取应用包名
     * @param context 上下文
     * @return 包名
     */
    public static String getPackageName(Context context) {
        if (cachedPackageName != null) {
            return cachedPackageName;
        }
        cachedPackageName = context.getPackageName();
        Log.d(TAG, "App package name: " + cachedPackageName);
        return cachedPackageName;
    }
    
    /**
     * 获取应用签名的MD5值
     * @param context 上下文
     * @return 签名MD5（32位小写）
     */
    public static String getSignatureMD5(Context context) {
        if (cachedSignature != null) {
            return cachedSignature;
        }
        
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = info.signatures;
            
            if (signatures != null && signatures.length > 0) {
                byte[] cert = signatures[0].toByteArray();
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] digest = md.digest(cert);
                cachedSignature = bytesToHex(digest);
                Log.d(TAG, "App signature MD5: " + cachedSignature);
                return cachedSignature;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get app signature: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * 获取应用签名的SHA1值
     * @param context 上下文
     * @return 签名SHA1（40位小写）
     */
    public static String getSignatureSHA1(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = info.signatures;
            
            if (signatures != null && signatures.length > 0) {
                byte[] cert = signatures[0].toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA1");
                byte[] digest = md.digest(cert);
                return bytesToHex(digest);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get app signature SHA1: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * 生成请求签名
     * @param context 上下文
     * @param timestamp 时间戳
     * @param data 请求数据
     * @return 签名字符串
     */
    public static String generateRequestSign(Context context, long timestamp, String data) {
        String appSign = getSignatureMD5(context);
        String raw = appSign + timestamp + data + "shenma_tv_2024";
        return md5(raw);
    }
    
    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * MD5加密
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            return bytesToHex(digest);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * 为请求参数Map添加签名参数
     * @param context 上下文
     * @param params 请求参数Map
     * @return 添加签名后的参数Map
     */
    public static java.util.Map<String, String> addSignatureParams(Context context, java.util.Map<String, String> params) {
        if (params == null) {
            params = new java.util.HashMap<>();
        }
        params.put("app_sign", getSignatureMD5(context));
        params.put("app_name", getPackageName(context));
        return params;
    }
    
    // 默认配置
    private static String illegalTitle = "温馨提示";
    private static String illegalMessage = "检测到您使用的是非官方版本，为了您的账号安全和使用体验，请下载正版应用。";
    private static String downloadUrl = "";
    private static String downloadBtnText = "立即下载";
    private static String exitBtnText = "退出应用";
    
    /**
     * 设置非法客户端提示配置
     */
    public static void setIllegalConfig(String title, String message, String url, String downloadBtn, String exitBtn) {
        if (title != null && !title.isEmpty()) illegalTitle = title;
        if (message != null && !message.isEmpty()) illegalMessage = message;
        if (url != null && !url.isEmpty()) downloadUrl = url;
        if (downloadBtn != null && !downloadBtn.isEmpty()) downloadBtnText = downloadBtn;
        if (exitBtn != null && !exitBtn.isEmpty()) exitBtnText = exitBtn;
    }
    
    /**
     * 显示非法客户端提示框
     * @param context 上下文（Activity）
     */
    public static void showIllegalClientDialog(final Context context) {
        showIllegalClientDialog(context, null, null, null, null, null);
    }
    
    /**
     * 显示非法客户端提示框（带自定义配置）
     * @param context 上下文（Activity）
     * @param title 标题
     * @param message 消息
     * @param url 下载地址
     * @param downloadBtn 下载按钮文字
     * @param exitBtn 退出按钮文字
     */
    public static void showIllegalClientDialog(final Context context, String title, String message, 
            final String url, String downloadBtn, String exitBtn) {
        // 使用传入的配置或默认配置
        final String finalTitle = (title != null && !title.isEmpty()) ? title : illegalTitle;
        final String finalMessage = (message != null && !message.isEmpty()) ? message : illegalMessage;
        final String finalUrl = (url != null && !url.isEmpty()) ? url : downloadUrl;
        final String finalDownloadBtn = (downloadBtn != null && !downloadBtn.isEmpty()) ? downloadBtn : downloadBtnText;
        final String finalExitBtn = (exitBtn != null && !exitBtn.isEmpty()) ? exitBtn : exitBtnText;
        
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(finalTitle);
            builder.setMessage(finalMessage);
            builder.setCancelable(false);
            
            // 只有配置了下载地址才显示下载按钮
            if (finalUrl != null && !finalUrl.isEmpty()) {
                builder.setPositiveButton(finalDownloadBtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                            context.startActivity(intent);
                        } catch (Exception e) {
                            Log.e(TAG, "打开下载链接失败: " + e.getMessage());
                        }
                        // 退出应用
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                });
            }
            
            builder.setNegativeButton(finalExitBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            });
            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "显示对话框失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查API响应是否为非法客户端错误
     * @param responseCode HTTP状态码或响应中的code
     * @param responseMsg 响应消息
     * @return 是否为非法客户端错误
     */
    public static boolean isIllegalClientError(int responseCode, String responseMsg) {
        return responseCode == 403 || (responseMsg != null && responseMsg.contains("非法客户端"));
    }
}
