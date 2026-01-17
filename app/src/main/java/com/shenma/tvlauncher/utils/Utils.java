package com.shenma.tvlauncher.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.shenma.tvlauncher.application.MyApplication;
import com.shenma.tvlauncher.vod.domain.VodUrlList;
import com.wepower.live.parser.IPlay;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.view.LiveLoadingDialog;
import com.shenma.tvlauncher.view.LoadingDialog;
import com.shenma.tvlauncher.tvlive.network.ThreadPoolManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {
    private final static String DEF_ZH_PATTERN = "[\u4e00-\u9fa5]+";
    /**
     * @brief TAG
     */
    private static final String TAG = "Utils";

    /**
     * @brief 对话框。
     */
    private static LiveLoadingDialog Loadingdialog = null;

    /**
     * @brief 小马加载。
     */
    private static LoadingDialog lDialog = null;

    private static String mtext = "";
    private static long start = 0;
    private static Toast toast;

    IPlay iplay = new IPlay() {
        public String returnPlayUrl(String arg0) {
            return null;
        }

        public String returnIP() {
            return null;
        }
    };


    /**
     * 最省内存的方式读取本地图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static String stringDrawNum(String string) {
        String str2 = "";
        int i = 0;
        while (i < string.length()) {
            if (string.charAt(i) >= '0' && string.charAt(i) <= '9') {
                str2 = new StringBuilder(String.valueOf(str2)).append(string.charAt(i)).toString();
            }
            i++;
        }
        return str2;
    }

    /*获取安卓ID*/
    public static String GetAndroidID(Context context) {
        return Secure.getString(context.getContentResolver(), "android_id");
    }


    /*获取MAC地址*/
    public static String getMacAddress() {
        String macAddress = "";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            String wifiInterfaceName = null;
            String wiredInterfaceName = null;
            for (NetworkInterface intf : interfaces) {
                if (intf.getName().equalsIgnoreCase("wlan0")) {
                    wifiInterfaceName = intf.getName();
                }
                if (intf.getName().equalsIgnoreCase("eth0")) {
                    wiredInterfaceName = intf.getName();
                }
            }
            if (wifiInterfaceName != null) {
                byte[] macBytes = NetworkInterface.getByName(wifiInterfaceName).getHardwareAddress();
                if (macBytes != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (byte b : macBytes) {
                        stringBuilder.append(String.format("%02X", b));
                    }
                    macAddress = stringBuilder.toString();
                }
            } else if (wiredInterfaceName != null) {
                byte[] macBytes = NetworkInterface.getByName(wiredInterfaceName).getHardwareAddress();
                if (macBytes != null) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (byte b : macBytes) {
                        stringBuilder.append(String.format("%02X", b));
                    }
                    macAddress = stringBuilder.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (macAddress != null && (macAddress.equals("020000000000") || macAddress.equals("000000000000"))) {
            macAddress = "";
        }

        return macAddress;
    }

    /**
     * 读取配置文件 3.20
     */
    /*public static String getFormInfo(Class c, int i) {
        String frominfo = "";
        Properties prop = new Properties();
        try {
            prop.load(c.getResourceAsStream("/assets/config.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String from[] = ((String) prop.get("from")).split("[|]");
        frominfo = from[i];
        return frominfo;

    }*/

    /**
     * @brief 显示函数。
     * @author joychang
     * @param[in] context 上下文。
     * @param[in] message 内容文字。
     * @note 显示加载对话框处理。
     */
    public static void loadingShow(Context context, String message) {
        if (lDialog != null) {
            loadingClose();
            //return;
            // 关闭加载框
        }

        // 使用以下方式显示对话框，按返回键可关闭对话框
        lDialog = new LoadingDialog(context, message);
        lDialog.setMessage(message);
        // lDialog.setIndeterminate(true);
        lDialog.setCancelable(true);
        lDialog.setCanceledOnTouchOutside(false);
        lDialog.show();
    }

    /**
     * @brief 关闭函数。
     * @author joychang
     * @note 关闭加载对话框处理。
     */
    public static void loadingClose() {
        if (null != lDialog) {
            lDialog.dismiss();
            lDialog.cancel();
            lDialog = null;
        } else {
            //Logger.w(TAG, "close(): LoadingDialog is not showing");
        }

        //Log.d(TAG, "LoadingDialog close() end");
    }

    /**
     * 获得网络连接是否可用
     *
     * @param context
     * @return
     */
    public static boolean hasNetwork(Context context) {
        try {
        ConnectivityManager con = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (con == null) {
                return false;
            }
        NetworkInfo workinfo = con.getActiveNetworkInfo();
            if (workinfo == null || !workinfo.isAvailable() || !workinfo.isConnected()) {
            //showNetDialog(context);
            return false;
        }
            // Android 4上额外检查网络类型（可选，用于更详细的网络状态判断）
            // 如果网络已连接，直接返回true
            // 网络类型检查仅用于日志记录，不影响返回值
            int networkType = workinfo.getType();
            // 检查是否是移动网络或WiFi或以太网
            if (networkType == ConnectivityManager.TYPE_WIFI || 
                networkType == ConnectivityManager.TYPE_MOBILE ||
                networkType == ConnectivityManager.TYPE_ETHERNET) {
        return true;
            }
            // 其他网络类型（如蓝牙等）也认为有网络
            return true;
        } catch (Exception e) {
            // 异常情况下，尝试返回true，让网络请求自己判断
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 安装一个apk文件
     *
     * @param file
     */
    public static void installApk(File file, Context context) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 获取当前应用版本号
     *
     * @return
     */
    @SuppressWarnings("unused")
    public static String getVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 获取当前时间
     *
     * @return 时间字符串 24小时制
     * @author drowtram
     */
    public static String getStringTime(String type) {
        Time t = new Time();
        t.setToNow(); // 取得系统时间。
        String hour = t.hour < 10 ? "0" + (t.hour) : t.hour + ""; // 默认24小时制
        String minute = t.minute < 10 ? "0" + (t.minute) : t.minute + "";
        return hour + type + minute;
    }


    /**
     * 自定义Toast原版
     *
     * @param context
     * @param text
     * @param image
     */
//    public static void showToast(Context context, String text, int image) {
//        Long end = java.lang.System.currentTimeMillis();
//        ;
//        Logger.d(TAG, "start=" + start);
//        Logger.d(TAG, "end=" + end);
//        if (end - start < 1000 && text.equals(mtext)) {
//            return;
//        } else if (end - start < 2000 && text.equals(mtext)) {
//            View view = LayoutInflater.from(context).inflate(R.layout.tv_toast, null);
//            TextView tv_toast = (TextView) view.findViewById(R.id.tv_smtv_toast);
//            ImageView iv_toast = (ImageView) view.findViewById(R.id.iv_smtv_toast);
//            tv_toast.setText("你也太无聊了吧...");
//            iv_toast.setBackgroundResource(image);
//            Toast toast = new Toast(context);
//            toast.setView(view);
//            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.show();
//            start = end;
//        } else {
//            start = end;
//            View view = LayoutInflater.from(context).inflate(R.layout.tv_toast, null);
//            TextView tv_toast = (TextView) view.findViewById(R.id.tv_smtv_toast);
//            ImageView iv_toast = (ImageView) view.findViewById(R.id.iv_smtv_toast);
//            tv_toast.setText(text);
//            iv_toast.setBackgroundResource(image);
//            Toast toast = new Toast(context);
//            toast.setView(view);
//            toast.setDuration(Toast.LENGTH_SHORT);
//            toast.show();
//        }
//        mtext = text;
//    }

    /**
     * 自定义Toast
     *
     * @param context
     * @param text
     * @param image
     */
    public static void showToast(Context context, String text, int image) {
        Long end = Long.valueOf(java.lang.System.currentTimeMillis());
        //Logger.d(TAG, "start=" + start);
        //Logger.d(TAG, "end=" + end);
        if (end.longValue() - start >= 1000 || !text.equals(mtext)) {
            if (end.longValue() - start >= 1500 || !text.equals(mtext)) {
                start = end.longValue();
                View view = LayoutInflater.from(context).inflate(R.layout.tv_toast, (ViewGroup) null);
                TextView tv_toast = (TextView) view.findViewById(R.id.tv_smtv_toast);
                ImageView iv_toast = (ImageView) view.findViewById(R.id.iv_smtv_toast);
                tv_toast.setText(text);
                iv_toast.setBackgroundResource(image);
                Toast toast2 = new Toast(context);
                toast2.setView(view);
                toast2.setDuration(Toast.LENGTH_SHORT);
                toast2.show();
            } else {
                View view2 = LayoutInflater.from(context).inflate(R.layout.tv_toast, (ViewGroup) null);
                TextView tv_toast2 = (TextView) view2.findViewById(R.id.tv_smtv_toast);
                ImageView iv_toast2 = (ImageView) view2.findViewById(R.id.iv_smtv_toast);
                tv_toast2.setText(R.string.bored);
                iv_toast2.setBackgroundResource(image);
                Toast toast3 = new Toast(context);
                toast3.setView(view2);
                toast3.setDuration(Toast.LENGTH_SHORT);
                toast3.show();
                start = end.longValue();
            }
            mtext = text;
        }
    }

    /**
     * 显示土司
     * fix the toast Repeat display by zhouchuan
     *
     * @param context
     * @param text
     * @param image
     */
    public static void showToast(String text, Context context, int image) {
        View view = null;
        if (toast == null) {
            toast = new Toast(context);
            view = LayoutInflater.from(context).inflate(R.layout.tv_toast, null);
        } else {
            view = toast.getView();
        }
        TextView tv_toast = (TextView) view.findViewById(R.id.tv_smtv_toast);
        ImageView iv_toast = (ImageView) view.findViewById(R.id.iv_smtv_toast);
        tv_toast.setText(text);
        iv_toast.setBackgroundResource(image);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 显示土司
     * fix the toast Repeat display by zhouchuan
     *
     * @param context
     * @param text
     * @param image
     */
    public static void showToast(Context context, int text, int image) {
        View view = null;
        if (toast == null) {
            toast = new Toast(context);
            view = LayoutInflater.from(context).inflate(R.layout.tv_toast, null);
        } else {
            view = toast.getView();
        }
        TextView tv_toast = (TextView) view.findViewById(R.id.tv_smtv_toast);
        ImageView iv_toast = (ImageView) view.findViewById(R.id.iv_smtv_toast);
        tv_toast.setText(text);
        iv_toast.setBackgroundResource(image);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }


    /**
     * menu主界面目录
     *
     * @return
     */
    public static ArrayList<String> getUserData(int type) {
        ArrayList<String> list = new ArrayList<String>();
        list.add("删除");
        list.add("全部清空");
//		if (type == REMOVE_FROM_FAV) {
//			list.add("从常用中删除");
//			list.add("卸载");
//		} else if (type == ADD_TO_FAV) {
//			list.add("添加到常用");
//			list.add("卸载");
//		}

        return list;
    }

    /**************************回看工具函数*******************************/

    /**
     * @param strLink 链接地址
     * @return false     该地址非空
     * @brief 判断该链接是否为空
     * @author joychang
     */
    public static boolean isValidLink(String strLink) {
        Log.d("UiUtil", "isValidLink() start.");
        boolean result = false;
        if (strLink != null && strLink.length() > 0) {
            URL url;
            try {
                url = new URL(strLink);
                HttpURLConnection connt = (HttpURLConnection) url.openConnection();
                connt.setConnectTimeout(5 * 1000);
                connt.setRequestMethod("HEAD");
                int code = connt.getResponseCode();
                if (code == 404) {
                    result = true;
                }
                connt.disconnect();
            } catch (Exception e) {
                result = true;
            }
        } else {
            result = true;
        }
        Log.d("UiUtil", "isValidLink() end.");
        return result;
    }


    /**
     * @return UTF8形式字符串。
     * @throws UnsupportedEncodingException 不支持的字符集
     * @brief 中文字符串转换函数。
     * @author joychang
     * @param[in] str 要转换的字符串。
     * @param[in] charset 字符串编码。
     * @note 将str中的中文字符转换为UTF8编码形式。
     */
    public static String encode(String str, String charset) throws UnsupportedEncodingException {
        Log.d(TAG, "_encode() start");

        String result = null;

        if ((str != null) && (charset != null)) {
            try {
                Pattern p = Pattern.compile(DEF_ZH_PATTERN, 0);
                Matcher m = p.matcher(str);

                StringBuffer b = new StringBuffer();
                while (m.find()) {
                    m.appendReplacement(b, URLEncoder.encode(m.group(0), charset));
                }

                m.appendTail(b);

                result = b.toString();
            } catch (PatternSyntaxException e) {
                e.printStackTrace();
            }
        } else {
            if (str == null) {
                Log.e(TAG, "encode(): str is null");
            }

            if (charset == null) {
                Log.e(TAG, "encode(): charset is null");
            }
        }

        Log.d(TAG, "encode() end");

        return result;
    }


    /**
     * @brief 显示函数。
     * @author joychang
     * @param[in] context 上下文。
     * @param[in] title   标题文字。
     * @param[in] message 内容文字。
     * @note 显示加载对话框处理。
     */
    public static void loadingShow_tv(Context context, int message) {
        Log.d(TAG, "show() start");
        if (Loadingdialog != null && Loadingdialog.isShowing()) {
            Loadingdialog.dismiss();
            //Loadingdialog = null;
        }
        //使用以下方式显示对话框，按返回键可关闭对话框
        Loadingdialog = new LiveLoadingDialog(context);
        Loadingdialog.setLoadingMsg(message);
        Loadingdialog.setCanceledOnTouchOutside(false);
        Loadingdialog.show();
        Log.d(TAG, "show() end");
    }

    /**
     * @brief 关闭函数。
     * @author joychang
     * @note 关闭加载对话框处理。
     */
    public static void loadingClose_Tv() {
        Log.d(TAG, "close() start");
        if (Loadingdialog != null) {
            Loadingdialog.cancel();
            Loadingdialog = null;
        } else {
            Log.w(TAG, "close(): mDialog is not showing");
        }

        Log.d(TAG, "close() end");
    }

    /**
     * @return false 对话框非显示中。
     * @brief 判断加载对话框是否显示函数。
     * @author joychang
     */
    public static boolean isShowing() {
        Log.d(TAG, "isShowing() start");

        boolean result = false;

        if (Loadingdialog != null) {
            result = true;
        }

        Log.d(TAG, "isShowing() end");

        return result;
    }

    /**
     * 根据日期获取对应的星期
     *
     * @param mdate
     * @return 星期
     */
    public static String getWeekToDate(String mdate) {
        String week = null;
        mdate = mdate.replace("/", "-");
//    	mdate = mdate.replace("月", "-");
//    	mdate = mdate.replace("日", "");
//    	mdate = "2014-"+mdate;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = sdf.parse(mdate);
            week = getWeek(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return week;
    }

    /*根据日期获取对应的星期子项*/
    public static String getWeek(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date);
        return week;
    }

    /**
     * 根据节目Url获取节目的时间
     *
     * @param src 节目地址
     * @return 时间
     */
    public static String getTimeToSrc(String src) {
        String time = null;
        String stringarray[] = src.split("-");
        String srcnew = stringarray[0];
        String timenew = srcnew.substring(srcnew.length() - 4, srcnew.length());
        String start = timenew.substring(0, 2);
        String end = timenew.substring(timenew.length() - 2, timenew.length());
        time = start + ":" + end;
        return time;
    }

    /**
     * 根据label名字截取时间
     */
    public static String getTimeToLabel(String label) {
        String time = "";
        if (label.length() > 5) {
            time = label.substring(0, 5);
        }
        return time;
    }

    /**
     * 根据label名字截取名字
     * @param label
     * @return name
     */
    public static String getNameToLabel(String label) {
        String name = "";
        if (label.length() > 6) {
            name = label.substring(6, label.length());
        }
        return name;
    }

    /**
     * 时间格式转换
     *
     * @param time
     * @return
     */
    public static String toTime(int time) {

        time /= 1000;
        int minute = time / 60;
        int hour = minute / 60;
        int second = time % 60;
        minute %= 60;
        return String.format("%02d:%02d:%02d", hour,
                minute, second);
    }


    /**
     * 从Assets目录下拷贝文件到指定目录
     *
     * @param context  上下文对象
     * @param fileName Assets目录下的指定文件名
     * @param path     要拷贝到的目录
     * @return true 拷贝成功  false 拷贝失败
     * @author drowtram
     */
    public static boolean copyApkFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            File f = new File(path);
            if (f.exists()) {
                f.delete(); //如果存在这个文件，则删除重新拷贝
            }
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }

    /* 获取一个路径的文件名
     *
     * @param urlpath
     * @return
     */
    public static String getFilename(String urlpath) {
        return urlpath
                .substring(urlpath.lastIndexOf("/") + 1, urlpath.length());
    }

    /*获取播放剧集废弃*/
//    public static List<String> getVideoBottomDatas(List<VodUrl> datas, int index, Boolean isList) {
//        List<String> gv_list = null;
//        int j;
//        int i;
//        if (isList.booleanValue()) {
//            j = index * 10;
//            for (i = 0; i < 10; i++) {
//                gv_list.add(datas.get(j + i).getTitle());
//            }
//            return gv_list;
//        }
//        gv_list = new ArrayList<String>();
//        datas.size();
//        i = 0;
//        j = datas.size() / 10;
//        while (i < j) {
//            gv_list.add(new StringBuilder(String.valueOf((i * 10) + 1)).append("-").append((i + 1) * 10).toString());
//            i++;
//        }
//        gv_list.add(new StringBuilder(String.valueOf((i * 10) + 1)).append("-").append(datas.size()).toString());
//        return gv_list;
//    }
//    public static List<VodUrl> getVideolvDatas(List<VodUrl> datas, int index) {
//        List<VodUrl> lv_list = new ArrayList<VodUrl>();
//        int j = index * 10;
//        int i;
//        VodUrl vodurl;
//        if (datas == null || datas.size() - j < 10) {
//            for (i = 0; i < datas.size() - j; i++) {
//                vodurl = new VodUrl();
//                vodurl.setTitle(((VodUrl) datas.get(j + i)).getTitle());
//                vodurl.setUrl(datas.get(j + i).getUrl());
//                lv_list.add(vodurl);
//            }
//        } else {
//            for (i = 0; i < 10; i++) {
//                vodurl = new VodUrl();
//                vodurl.setTitle(((VodUrl) datas.get(j + i)).getTitle());
//                vodurl.setUrl(datas.get(j + i).getUrl());
//                lv_list.add(vodurl);
//            }
//        }
//        return lv_list;
//    }
//    public static List<String> getVideogvDatas(List<VodUrl> datas, Boolean isList) {
//        List<String> gv_list = new ArrayList<String>();
//        datas.size();
//        int i = 0;
//        int j = datas.size() / 10;
//        while (i < j) {
//            gv_list.add(new StringBuilder(String.valueOf((i * 10) + 1)).append("-").append((i + 1) * 10).toString());
//            i++;
//        }
//        if ((i * 10) + 1 <= datas.size()) {
//            gv_list.add(new StringBuilder(String.valueOf((i * 10) + 1)).append("-").append(datas.size()).toString());
//        }
//        return gv_list;
//    }

    /*获取播放剧集(列表)*/
    public static List<VodUrlList> getVideolvDatas(List<VodUrlList> datas, int index) {
        ArrayList arrayList = new ArrayList();
        int i2 = index * 20;
        int i3 = 0;
        if (datas == null || datas.size() - i2 < 20) {
            while (i3 < datas.size() - i2) {
                VodUrlList vodUrlList = new VodUrlList();
                int i4 = i2 + i3;
                vodUrlList.setTitle(datas.get(i4).getTitle());
                vodUrlList.setUrl(datas.get(i4).getUrl());
                arrayList.add(vodUrlList);
                i3++;
            }
        } else {
            while (i3 < 20) {
                VodUrlList vodUrlList2 = new VodUrlList();
                int i5 = i2 + i3;
                vodUrlList2.setTitle(datas.get(i5).getTitle());
                vodUrlList2.setUrl(datas.get(i5).getUrl());
                arrayList.add(vodUrlList2);
                i3++;
            }
        }
        return arrayList;
    }

    /*获取播放剧集*/
    public static List<VodUrlList> getVideolvDatas(List<VodUrlList> datas, int index,int number) {
        ArrayList arrayList = new ArrayList();
        int i2 = index * number;
        int i3 = 0;
        if (datas == null || datas.size() - i2 < number) {
            while (i3 < datas.size() - i2) {
                VodUrlList vodUrlList = new VodUrlList();
                int i4 = i2 + i3;
                vodUrlList.setTitle(datas.get(i4).getTitle());
                vodUrlList.setUrl(datas.get(i4).getUrl());
                arrayList.add(vodUrlList);
                i3++;
            }
        } else {
            while (i3 < number) {
                VodUrlList vodUrlList2 = new VodUrlList();
                int i5 = i2 + i3;
                vodUrlList2.setTitle(datas.get(i5).getTitle());
                vodUrlList2.setUrl(datas.get(i5).getUrl());
                arrayList.add(vodUrlList2);
                i3++;
            }
        }
        return arrayList;
    }

    /**
     * 将字符串使用base64加密
     *
     * @param url 路径
     * @return
     * @throws Exception
     */
    public static String encodeBase64String(String url) throws Exception {
        return android.util.Base64.encodeToString(url.getBytes(),
                Base64.DEFAULT);
    }

    /**
     * 筛选条件编码
     *
     * @param filter
     * @return
     */
    public static String getEcodString(String filter) {
        String s = "";
        try {
            s = URLEncoder.encode(filter, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return s;
    }


    /**
     * 检测本地apk文件
     *
     * @param fileName
     * @author drowtram
     */
    public static boolean startCheckLoaclApk(Context context, String fileName) {
        File file = new File(MyApplication.PUBLIC_DIR);
        try {
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fName = f.getName();
                    if (fName.equals(fileName)) {
                        installApk(context, MyApplication.PUBLIC_DIR + fName);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // 改变亮度
    public static void SetLightness(Activity act, int value) {
        try {
            WindowManager.LayoutParams lp = act.getWindow().getAttributes();
            lp.screenBrightness = (value <= 0 ? 1 : value) / 255f;
            act.getWindow().setAttributes(lp);
        } catch (Exception e) {
            Toast.makeText(act, "无法改变亮度", Toast.LENGTH_SHORT).show();
        }
    }

    // 获取亮度
    public static float GetLightness(Activity act) {
        WindowManager.LayoutParams lp = act.getWindow().getAttributes();
        float brightness = lp.screenBrightness;
        //Logger.d("doBrightnessTouch", "GetLightness====" + brightness);
        return brightness;
    }

    // 停止自动亮度调节
    public static void stopAutoBrightness(Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        activity.getWindow().setAttributes(lp);
    }

    // 开启亮度自动调节
    public static void startAutoBrightness(Activity activity) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        activity.getWindow().setAttributes(lp);
    }

    public static void deleteAppApks(String dir) {
        File file = new File(dir);
        try {
            if (file.exists() && file.isDirectory()) {
                for (File f : file.listFiles()) {
                    String fileName = f.getName();
                    if (f.isFile() && fileName.endsWith(".apk") && f.delete()) {
                        Log.d("zhouchuan", "delete the " + fileName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载apk文件进行安装
     *
     * @param context
     * @param mHandler 更新显示进度的handler
     * @param url
     * @author drowtram
     */
    public static void startDownloadApk(final Context context, final String url, final Handler mHandler) {
        Utils.showToast(context, "正在后台下载，完成后提示安装...", R.drawable.toast_smile);
        // 优化：使用线程池替代直接创建Thread
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
//				File file = new File(Constant.PUBLIC_DIR);
                String apkName = url.substring(url.lastIndexOf("/") + 1);
//				File file = new File(context.getCacheDir(),apkName);
                Log.d("zhouchuan", "文件路径" + apkName);
//				if(!file.exists()){
//					file.mkdirs();
//				}
                FileOutputStream fos = null;
                InputStream is = null;
                try {
                    HttpGet hGet = new HttpGet(url.replaceAll(" ", "%20"));//替换掉空格字符串，不然下载不成功
                    HttpResponse hResponse = new DefaultHttpClient().execute(hGet);
                    if (hResponse.getStatusLine().getStatusCode() == 200) {
                        is = hResponse.getEntity().getContent();
                        float downsize = 0;
                        if (mHandler != null) {
                            //获取下载的文件大小
                            float size = hResponse.getEntity().getContentLength();
                            mHandler.obtainMessage(1001, size).sendToTarget();//发消息给handler处理更新信息
                        }
                        fos = context.openFileOutput(apkName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                        byte[] buffer = new byte[8192];
                        int count = 0;
                        while ((count = is.read(buffer)) != -1) {
                            if (mHandler != null) {
                                downsize += count;
                                mHandler.obtainMessage(1002, downsize).sendToTarget();//发消息给handler处理更新信息
                            }
                            fos.write(buffer, 0, count);
//							Log.d("zhouchuan", "下载进度"+(int)(downsize/size*100)+"%"+" downsize="+downsize+" size="+size);
                        }
                        installApk(context,"/data/data/" + getPackageName(context) + "/files/" + apkName);//借鉴293
                        //installApk(context, "/data/data/com.shenma.tvlauncher/files/" + apkName);
                    }
                } catch (IOException e) {
                    Log.e("Utils", "Download APK IO error: " + e.getMessage(), e);
                    if (mHandler != null) {
                        mHandler.obtainMessage(1003, "下载失败: " + e.getMessage()).sendToTarget();
                    }
                } catch (Exception e) {
                    Log.e("Utils", "Download APK error: " + e.getMessage(), e);
                    if (mHandler != null) {
                        mHandler.obtainMessage(1003, "下载失败: " + e.getMessage()).sendToTarget();
                    }
                } finally {
                    // 优化：确保资源正确关闭，避免资源泄漏
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Log.e("Utils", "Close FileOutputStream error", e);
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Log.e("Utils", "Close InputStream error", e);
                        }
                    }
                }
            }
        });
    }


    /**
     * 下载zip文件进行安装
     *
     * @param context
     * @param mHandler 更新显示进度的handler
     * @param url
     * @author drowtram
     */
    public static void startDownloadzip(final Context context, final String url, final Handler mHandler) {
        // 优化：使用线程池替代直接创建Thread
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                String apkName = url.substring(url.lastIndexOf("/") + 1);
                FileOutputStream fos = null;
                InputStream is = null;
                try {
                    HttpGet hGet = new HttpGet(url.replaceAll(" ", "%20"));//替换掉空格字符串，不然下载不成功
                    HttpResponse hResponse = new DefaultHttpClient().execute(hGet);
                    if (hResponse.getStatusLine().getStatusCode() == 200) {
                        is = hResponse.getEntity().getContent();
                        float downsize = 0;
//                        if (mHandler != null) {
//                            //获取下载的文件大小
//                            float size = hResponse.getEntity().getContentLength();
//                            mHandler.obtainMessage(1001, size).sendToTarget();//发消息给handler处理更新信息
//                        }
                        fos = context.openFileOutput(apkName, Context.MODE_WORLD_READABLE | Context.MODE_WORLD_WRITEABLE);
                        byte[] buffer = new byte[8192];
                        int count = 0;
                        while ((count = is.read(buffer)) != -1) {
                            if (mHandler != null) {
                                downsize += count;
                                mHandler.obtainMessage(1002, downsize).sendToTarget();//发消息给handler处理更新信息
                            }
                            fos.write(buffer, 0, count);
//							Log.d("zhouchuan", "下载进度"+(int)(downsize/size*100)+"%"+" downsize="+downsize+" size="+size);
                        }
                        tarZip(context,apkName);
                    }
                } catch (IOException e) {
                    Log.e("Utils", "Download ZIP IO error: " + e.getMessage(), e);
                } catch (Exception e) {
                    Log.e("Utils", "Download ZIP error: " + e.getMessage(), e);
                } finally {
                    // 优化：确保资源正确关闭，避免资源泄漏
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Log.e("Utils", "Close FileOutputStream error", e);
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            Log.e("Utils", "Close InputStream error", e);
                        }
                    }
                }
            }
        });
    }

    /*检测文件的md5*/
    public static String getMD5Checksum(String filePath) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(filePath);

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }

            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            fis.close();
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*解压ZIP*/
    public static void tarZip(Context context,String apkName) {
        String zipFilePath = "data/data/" + getPackageName(context) + "/files/" + apkName;
        String destDirectory = "data/data/" + getPackageName(context) + "/files";

        try {
            unzip(zipFilePath, destDirectory);
        } catch (IOException e) {
        }
    }

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }

        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                extractFile(zipIn, filePath);
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    /**
     * 安装apk文件
     *
     * @param fileName
     * @author drowtram
     */
    public static void installApk(Context context, String fileName) {
        if (getUninatllApkInfo(context, fileName)) {
            File updateFile = new File(fileName);
            try {
                String[] args2 = {"chmod", "604", updateFile.getPath()};
                Runtime.getRuntime().exec(args2);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*------------------------*/
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(updateFile),
                    "application/vnd.android.package-archive");
            context.startActivity(intent);
//			File file = new File(fileName);
//			Intent intent = new Intent();
//			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.setAction(Intent.ACTION_VIEW);     //浏览网页的Action(动作)
//			String type = "application/vnd.android.package-archive";
//			intent.setDataAndType(Uri.fromFile(file), type);  //设置数据类型
//			context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.wait, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 判断apk文件是否可以安装
     *
     * @param context
     * @param filePath
     * @return
     */
    public static boolean getUninatllApkInfo(Context context, String filePath) {
        boolean result = false;
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if (info != null) {
                result = true;
            }
        } catch (Exception e) {
            result = false;
            Log.e("zhouchuan", "*****  解析未安装的 apk 出现异常 *****" + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 获取当前日期，包含星期几
     *
     * @return 日期字符串 xx月xx号 星期x
     * @author drowtram
     */
    public static String getStringData() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
        String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
        String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        if ("1".equals(mWay)) {
            mWay = "日";
        } else if ("2".equals(mWay)) {
            mWay = "一";
        } else if ("3".equals(mWay)) {
            mWay = "二";
        } else if ("4".equals(mWay)) {
            mWay = "三";
        } else if ("5".equals(mWay)) {
            mWay = "四";
        } else if ("6".equals(mWay)) {
            mWay = "五";
        } else if ("7".equals(mWay)) {
            mWay = "六";
        }
        return mMonth + "月" + mDay + "日\n" + "星期" + mWay;
    }

    /**
     * 获取IP
     *
     * @return
     */
    public static String localipget() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
        }
        return null;
    }

    /**
     * 获取IPV4
     *
     * @return
     */

    public static String localIPv4get() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && inetAddress instanceof Inet4Address) {
                        // 如果地址是 IPv4 地址，则直接返回
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace(); // 至少应该记录异常
        }
        return null; // 如果没有找到 IPv4 地址，则返回 null
    }

    /**
     * 根据apk路径获取包名
     *
     * @param context
     * @param strFile apk路径
     * @return apk包名
     */
    public static String getPackageName(Context context, String strFile) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(strFile, PackageManager.GET_ACTIVITIES);
        String mPackageName = null;
        if (packageInfo != null) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            mPackageName = applicationInfo.packageName;
        }
        return mPackageName;
    }

    public static String getPackageName(Context context) {//借鉴293
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
            return info.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 根据包名获取apk名称
     *
     * @param context
     * @param packageName 包名
     * @return apk名称
     */
    public static String getApkName(Context context, String packageName) {
        String apkName = null;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                apkName = pm.getApplicationLabel(applicationInfo).toString();
                // int lable = applicationInfo.labelRes;
                // apkName = context.getResources().getString(lable);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return apkName;
    }

    /**
     * 根据apk文件获取app应用名称
     *
     * @param context
     * @param apkFilePath apk文件路径
     * @return
     */
    public static String getAppNameByApkFile(Context context, String apkFilePath) {
        String apkName = null;
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            apkName = pm.getApplicationLabel(packageInfo.applicationInfo).toString();
        }
        return apkName;
    }

    /*
    * Url中文编码
    *
    * @param url Url地址
    */
    public static String UrlEncodeChinese(String url) {
        try {
            Matcher matcher = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(url);
            String tmp = "";
            while (matcher.find()) {
                tmp = matcher.group();
                url = url.replaceAll(tmp, URLEncoder.encode(tmp, "UTF-8"));
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url.replace(" ","%20");
    }

    /**
     * 生成简单二维码
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @return BitMap
     */
    public static Bitmap createQRCodeBitmap(String content, int width,int height, String character_set,String error_correction_level, String margin,int color_black, int color_white) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }
            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 向二维码中间添加logo图片(图片合成)
     *
     * @param srcBitmap 原图片（生成的简单二维码图片）
     * @param logoBitmap logo图片
     * @param logoPercent 百分比 (用于调整logo图片在原图片中的显示大小, 取值范围[0,1] )
     * @return
     */
    private static Bitmap addLogo(Bitmap srcBitmap,  Bitmap logoBitmap, float logoPercent){
        if(srcBitmap == null){
            return null;
        }
        if(logoBitmap == null){
            return srcBitmap;
        }
        //传值不合法时使用0.2F
        if(logoPercent < 0F || logoPercent > 1F){
            logoPercent = 0.2F;
        }

        /** 1. 获取原图片和Logo图片各自的宽、高值 */
        int srcWidth = srcBitmap.getWidth();
        int srcHeight = srcBitmap.getHeight();
        int logoWidth = logoBitmap.getWidth();
        int logoHeight = logoBitmap.getHeight();

        /** 2. 计算画布缩放的宽高比 */
        float scaleWidth = srcWidth * logoPercent / logoWidth;
        float scaleHeight = srcHeight * logoPercent / logoHeight;

        /** 3. 使用Canvas绘制,合成图片 */
        Bitmap bitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(srcBitmap, 0, 0, null);
        canvas.scale(scaleWidth, scaleHeight, srcWidth/2, srcHeight/2);
        canvas.drawBitmap(logoBitmap, srcWidth/2 - logoWidth/2, srcHeight/2 - logoHeight/2, null);

        return bitmap;
    }

    /**
     *
     * @param content                字符串内容
     * @param width                  二维码宽度
     * @param height                 二维码高度
     * @param character_set          编码方式（一般使用UTF-8）
     * @param error_correction_level 容错率 L：7% M：15% Q：25% H：35%
     * @param margin                 空白边距（二维码与边框的空白区域）
     * @param color_black            黑色色块
     * @param color_white            白色色块
     * @param logoBitmap             logo图片
     * @param logoPercent            logo所占百分比
     * @return
     */
    public static Bitmap createQRCodeBitmap(String content, int width, int height, String character_set, String error_correction_level,String margin, int color_black, int color_white,Bitmap logoBitmap, float logoPercent) {
        // 字符串内容判空
        if (TextUtils.isEmpty(content)) {
            return null;
        }
        // 宽和高>=0
        if (width < 0 || height < 0) {
            return null;
        }
        try {
            /** 1.设置二维码相关配置,生成BitMatrix(位矩阵)对象 */
            Hashtable<EncodeHintType, String> hints = new Hashtable<>();
            // 字符转码格式设置
            if (!TextUtils.isEmpty(character_set)) {
                hints.put(EncodeHintType.CHARACTER_SET, character_set);
            }
            // 容错率设置
            if (!TextUtils.isEmpty(error_correction_level)) {
                hints.put(EncodeHintType.ERROR_CORRECTION, error_correction_level);
            }
            // 空白边距设置
            if (!TextUtils.isEmpty(margin)) {
                hints.put(EncodeHintType.MARGIN, margin);
            }
            /** 2.将配置参数传入到QRCodeWriter的encode方法生成BitMatrix(位矩阵)对象 */
            BitMatrix bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            /** 3.创建像素数组,并根据BitMatrix(位矩阵)对象为数组元素赋颜色值 */
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    //bitMatrix.get(x,y)方法返回true是黑色色块，false是白色色块
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = color_black;//黑色色块像素设置
                    } else {
                        pixels[y * width + x] = color_white;// 白色色块像素设置
                    }
                }
            }

            /** 4.创建Bitmap对象,根据像素数组设置Bitmap每个像素点的颜色值,并返回Bitmap对象 */
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

            /** 5.为二维码添加logo图标 */
            if(logoBitmap != null){
                return addLogo(bitmap, logoBitmap, logoPercent);
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*判断邮箱*/
    public static boolean Email(String strEmail) {
        String strPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }

    /*判断手机*/
    public static boolean Phone(String strEmail) {
        String strPattern = "^1[0-9]{10}$";
        if (TextUtils.isEmpty(strPattern)) {
            return false;
        } else {
            return strEmail.matches(strPattern);
        }
    }

    /*判断邮箱或手机*/
    public static boolean isEmail(String strEmail) {
        // 邮箱正则表达式
        String emailPattern = "^[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]$";
        // 手机号码正则表达式
        String phonePattern = "^1[0-9]{10}$";

        if (TextUtils.isEmpty(strEmail)) {
            return false;
        } else {
            return strEmail.matches(emailPattern) || strEmail.matches(phonePattern);
        }
    }

    /**
     * 检测是否开启vpn或抓包
     */
    public static boolean isVpnConnected() {
        try {
            Enumeration<NetworkInterface> niList = NetworkInterface.getNetworkInterfaces();
            if (niList == null) {
                return false;
            }
            Iterator<NetworkInterface> it = Collections.list(niList).iterator();
            while (it.hasNext()) {
                NetworkInterface intf = it.next();
                if (intf.isUp() && intf.getInterfaceAddresses().size() != 0) {
                    if ("tun0".equals(intf.getName()) || "ppp0".equals(intf.getName())) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 检测是否开启vpn或抓包
     * @param mContext
     */

    public static boolean isWifiProxy(Context mContext) {
        String proxyAddress;
        int proxyPort;
        if (Build.VERSION.SDK_INT >= 14) {
            proxyAddress = java.lang.System.getProperty("http.proxyHost");
            String portStr = java.lang.System.getProperty("http.proxyPort");
            if (portStr == null) {
                portStr = "-1";
            }
            proxyPort = Integer.parseInt(portStr);
        } else {
            proxyAddress = Proxy.getHost(mContext);
            proxyPort = Proxy.getPort(mContext);
        }
        if (TextUtils.isEmpty(proxyAddress) || proxyPort == -1) {
            return false;
        }
        return true;
    }

    /**
     * 检测是否存在Hook
     */
    public static boolean isHookByStack() {
        boolean isHook = false;
        try {
            throw new Exception("blah");
        } catch (Exception e) {
            int zygoteInitCallCount = 0;
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if (stackTraceElement.getClassName().equals("com.android.internal.os.ZygoteInit") && (zygoteInitCallCount = zygoteInitCallCount + 1) == 2) {
                    isHook = true;
                }
                if (stackTraceElement.getClassName().equals("com.saurik.substrate.MS$2") && stackTraceElement.getMethodName().equals("invoked")) {
                    isHook = true;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge") && stackTraceElement.getMethodName().equals("main")) {
                    isHook = true;
                }
                if (stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge") && stackTraceElement.getMethodName().equals("handleHookedMethod")) {
                    isHook = true;
                }
            }
            return isHook;
        }
    }

    /**
     * 检测是否存在xp框架
     */
    public static boolean checkXpFormMap() {
        File maps;
        String readLine;
        int Pid = Process.myPid();
        if (Pid < 0) {
            maps = new File("/proc/self/maps");
        } else {
            maps = new File("/proc/" + Pid + "/maps");
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(maps)));
            do {
                readLine = reader.readLine();
                if (readLine == null) {
                    return false;
                }
                if (readLine.contains("libdexposed") || readLine.contains("libsubstrate.so") || readLine.contains("libepic.so")) {
                    return true;
                }
            } while (!readLine.contains("libxposed"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 获取当前应用签名的MD5
     * @param context
     * @return 当前应用MD5
     */
    public static String getMD5(Context context) {
        StringBuffer md5StringBuffer = new StringBuffer();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] bytes = packageInfo.signatures[0].toByteArray();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(bytes);
            byte[] digest = messageDigest.digest();
            for (int i = 0; i < digest.length; i++) {
                String hexString = Integer.toHexString(digest[i] & 0xff);
                if (hexString.length() == 1)
                    md5StringBuffer.append("0");
                md5StringBuffer.append(hexString);
            }
            //Log.e("getMD5",md5StringBuffer.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return md5StringBuffer.toString();
    }

    /*空指针闪退*/
    public static void exit(){
        Thread.currentThread().setUncaughtExceptionHandler(new java.lang.Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                //遇到不可抓取的异常会到这里来,就不会弹出对话框了,完美结局
                //在这里最好让所有的activity全finish了，也另加入关闭进程的方法
            }
        });
        String meIsNull = null;
        //在这里肯定是空指针异常，遇到异常之后，执行上面的回调代码，就不会弹出对话框了
        meIsNull.equals("空指针");
    }


    public static String strRot13(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'a' && c <= 'm') {
                c += 13;
            } else if (c >= 'n' && c <= 'z') {
                c -= 13;
            } else if (c >= 'A' && c <= 'M') {
                c += 13;
            } else if (c >= 'N' && c <= 'Z') {
                c -= 13;
            }
            result.append(c);
        }
        return result.toString();
    }

    /**
     * 删除特殊字符后面的数据
     *
     * @param str
     * @param separators
     * @return
     */
    public static int findLastIndexOfSeparators(String str, char... separators) {
        int lastIndex = -1;
        for (char separator : separators) {
            int index = str.lastIndexOf(separator);
            if (index > lastIndex) {
                lastIndex = index;
            }
        }
        return lastIndex;
    }


    /**
     * 解码
     *
     * @param uri
     * @return
     */
    public static String sanitizeUri(String uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            try {
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            } catch (UnsupportedEncodingException e1) {
                throw new Error();
            }
        }
        return uri;
    }

    /**
     * 检查端口是否被占用
     *
     * @param port
     * @return
     */
    public static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}

