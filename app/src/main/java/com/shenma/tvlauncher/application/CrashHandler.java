package com.shenma.tvlauncher.application;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author joychang
 * @Description UncaughtException处理类, 当程序发生Uncaught异常的时候, 由该类来接管程序, 并记录发送错误报告.
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";
    private static CrashHandler INSTANCE = new CrashHandler();// CrashHandler实例
    private Thread.UncaughtExceptionHandler mDefaultHandler;// 系统默认的UncaughtException处理类
    private Context mContext;// 程序的Context对象   
    private Map<String, String> info = new HashMap<String, String>();// 用来存储设备信息和异常信息   
    private SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss");// 用于格式化日期,作为日志文件名的一部分

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器   
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器   
    }

    /**
     * 当UncaughtException发生时会转入该重写的方法来处理
     */
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果自定义的没有处理则让系统默认的异常处理器来处理   
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(3000);// 如果处理了，让程序继续运行3秒再退出，保证文件保存并上传到服务器   
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 退出程序   
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常信息
     * @return true 如果处理了该异常信息;否则返回false.
     */
    public boolean handleException(final Throwable ex) {
        if (ex == null)
            return false;
        
        // 收集设备参数信息   
        collectDeviceInfo(mContext);
        // 保存日志文件   
        saveCrashInfo2File(ex);
        
        // 不再显示对话框，避免无限循环
        // 直接打印错误信息到日志
        Log.e(TAG, "Application crashed:", ex);
        
        return true;
    }
    
    /**
     * 获取错误信息字符串
     */
    private String getErrorInfo(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        sb.append("Android SDK: ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("Device: ").append(Build.MODEL).append("\n\n");
        
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        pw.close();
        sb.append(writer.toString());
        
        return sb.toString();
    }
    
    /**
     * 显示崩溃信息弹窗
     */
    private void showCrashDialog(final String errorMsg) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
        builder.setTitle("应用崩溃 - 错误信息");
        
        // 创建可滚动的TextView
        final android.widget.ScrollView scrollView = new android.widget.ScrollView(mContext);
        final android.widget.TextView textView = new android.widget.TextView(mContext);
        textView.setText(errorMsg);
        textView.setTextSize(12);
        textView.setPadding(20, 20, 20, 20);
        textView.setTextIsSelectable(true);
        scrollView.addView(textView);
        
        builder.setView(scrollView);
        builder.setCancelable(false);
        builder.setPositiveButton("退出应用", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        
        android.app.AlertDialog dialog = builder.create();
        // 设置窗口类型，允许在任何地方显示
        dialog.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    /**
     * 收集设备参数信息
     *
     * @param context
     */
    public void collectDeviceInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();// 获得包管理器   
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        Field[] fields = Build.class.getDeclaredFields();// 反射机制   
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                info.put(field.getName(), field.get("").toString());
                Log.d(TAG, field.getName() + ":" + field.get(""));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer();
        final String time = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());

        // 收集设备信息
        String versionName = info.get("versionName") != null ? info.get("versionName") : "unknown";
        String versionCode = info.get("versionCode") != null ? info.get("versionCode") : "0";
        
        sb.append("************* Crash Head ****************\n");
        sb.append("Time Of Crash      : ").append(time).append("\n");
        sb.append("Device Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        sb.append("Device Model       : ").append(Build.MODEL).append("\n");
        sb.append("Android Version    : ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("Android SDK        : ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("App VersionName    : ").append(versionName).append("\n");
        sb.append("App VersionCode    : ").append(versionCode).append("\n");
        sb.append("************* Crash Head ****************\n\n");

        // 获取完整的异常堆栈
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();
        String result = writer.toString();
        sb.append(result);
        
        // 保存到SD卡
        try {
            long timestamp = System.currentTimeMillis();
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            
            // 保存到外部存储的根目录
            File sdCard = android.os.Environment.getExternalStorageDirectory();
            File crashDir = new File(sdCard, "shenma_crash");
            if (!crashDir.exists()) {
                crashDir.mkdirs();
            }
            
            File file = new File(crashDir, fileName);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();
            
            Log.e(TAG, "Crash log saved to: " + file.getAbsolutePath());
            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "Failed to save crash log", e);
        }
        return null;
    }
}  

