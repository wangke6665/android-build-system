package com.shenma.tvlauncher;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.MacUtils;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.view.JSONService;
import com.shenma.tvlauncher.view.MyServices;
import com.shenma.tvlauncher.view.WiFiDialog;
import com.shenma.tvlauncher.vod.LivePlayerActivity;
import com.shenma.tvlauncher.vod.SearchActivity;
import com.umeng.analytics.MobclickAgent;
import com.shenma.tvlauncher.adapter.FragAdapter;
import com.shenma.tvlauncher.application.MyApplication;
import com.shenma.tvlauncher.fragment.RecommendFragment;
import com.shenma.tvlauncher.fragment.SettingFragment;
import com.shenma.tvlauncher.fragment.TVFragment;
import com.shenma.tvlauncher.fragment.TopicFragment;
import com.shenma.tvlauncher.ui.DepthPageTransformer;
import com.shenma.tvlauncher.ui.FixedSpeedScroller;
import com.shenma.tvlauncher.utils.BlurUtils;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.LruCacheUtils;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.view.AlwaysMarqueeTextView;
import com.shenma.tvlauncher.view.HomeDialog.Builder;
import com.shenma.tvlauncher.vod.VodTypeActivity;
import com.shenma.tvlauncher.tvlive.network.ThreadPoolManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author joychang
 * @Description 首页
 */

public class HomeActivity extends BaseActivity {
    public static String homeFrom;
    public static String homeParams;
    protected static Boolean ISTV;
    private static int titile_position = 0;
    private final String TAG = "HomeActivity";
    public FrameLayout fl_main;
    public RequestQueue mQueue;
    public List<PackageInfo> packLst;
    public ImageView whiteBorder = null;//白色背景框
    public RequestQueue RequestQueue;
    protected String technology = "";
    private FragAdapter adapter;
    private String app_nshow;
    private String player_name;
    private String player_update;
    private String player_zip_md5;
    private String player_download_url;
    private String app_nurl;
    private float countSize;
    private float currentSize;
    private List<Fragment> fragments;
    private float fromXDelta;
    private Boolean isHasFouse;
    private boolean isRunning = false;
    private ImageView iv_net_state;
    private ImageView iv_titile;
    private LinearLayout ll_rb;
    private AnimationSet mAnimationSet;
    private LruCacheUtils mCacheUtils;
    private TranslateAnimation mTranslateAnimation;
    private String password;
    private RadioButton rb_recommend;//热门推荐
    private RadioButton rb_Internet;//网络电视
    private RadioButton rb_bm_tvplay;//影视专区
    private RadioButton rb_settings;//我的设置
    private RadioGroup rg_video_type_bottom;
    private RelativeLayout rl_bg;
    private RecommendFragment rf;//热门推荐
    private TopicFragment mf;//影视专区
    private TVFragment tf;//网络电视
    private SettingFragment sf;//我的设置
    private TextView time_colon;
    private LinearLayout title_group;
    private TextView tv_main_date;
    private TextView tv_time;
    private TextView tv_update_msg;
    private Handler homeHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 调用窗口消息处理函数
            onMessage(msg);
        }
    };
    private String username;
    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(HomeActivity.this, R.string.request_failure, R.drawable.toast_err);
                    return;
                case 2:
                    //Utils.showToast(HomeActivity.this, username + R.string.Welcome_back, R.drawable.toast_smile);
                    Utils.showToast(HomeActivity.this, username + getString(R.string.Welcome_back), R.drawable.toast_smile);
                    return;
                case 3:
                    Utils.showToast(HomeActivity.this, R.string.Data_cleared, R.drawable.toast_shut);
                    return;
                case 4:
                    UpdateDialog(app_nshow, app_nurl);
                    return;
                case 5:
                    logoloadImg();
                    return;
                case 6:
                    notice();
                    return;
                case 7:
                    Utils.startDownloadzip(context, player_download_url, homeHandler);
                    return;
                default:
                    return;
            }
        }
    };
    private ViewPager vpager;
    private AlwaysMarqueeTextView gongGao;
    private LinearLayout gongGaoRoot;
    private RadioButton rb_video_type;
    private String Logo_url;//logo地址
    private ImageView logo;//首页logo
    private ImageView home_top_search;//搜索
    private ImageView home_top_record;//历史记录
    /*新UI圆角*/
    private LinearLayout home_top_searchs;//搜索
    private LinearLayout home_top_records;//历史记录
    private LinearLayout home_user;//用户中心
    private LinearLayout home_collect;//个性设置
    private LinearLayout home_play_set;//播放器设置
    private LinearLayout home_clear;//清理记录
    private LinearLayout home_wallpaper;//关于我们

    private String category;//类目地址
    private String gonggaoText = "";//公告内容
    
    // 获取分类数据
    public String getCategory() {
        return category;
    }
    
    // 获取公告内容
    public String getGonggaoText() {
        return gonggaoText;
    }
    
    private Dialog mDialog;
    private String En;
    private String Name;
    private String PWD = null;//类目密码
    private int compel;//是否强制升级
    private String categorypwd;
    public static String lib;
    private int live;
    private int search;
    private int searchport;

    private String Mac;

    /**
     * 注册网络变动的广播接收
     *
     * @author joychang
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectMgr.getActiveNetworkInfo();
                if (networkInfo == null || networkInfo.isConnected()) {
                    // 没有网络
                    iv_net_state.setImageResource(R.drawable.wifi_n);
                }
                NetworkInfo ethNetworkInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                if (ethNetworkInfo != null && ethNetworkInfo.isConnected()) {
                    // 有线网
                    iv_net_state.setImageResource(R.drawable.enh);
                }
                NetworkInfo wifiNetworkInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
                    // 无线网
                    iv_net_state.setImageResource(R.drawable.wifi);
                }
                NetworkInfo mobilNetworkInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobilNetworkInfo != null && mobilNetworkInfo.isConnected()) {
                    // 流量
                    iv_net_state.setImageResource(R.drawable.mobile);
                }
            }
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            queryInstalledApp();
        }
    };
    private BroadcastReceiver mWallReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String fileName = intent.getStringExtra("wallpaperFileName");
            if (fileName == null)
                return;
            sp.edit().putString("wallpaperFileName", fileName).commit();
            changeBackImage(fileName);
            Utils.showToast(context,R.string.Wallpaper_replacement_successful, R.drawable.toast_smile);
        }
    };
    private Bitmap bitmap;
    private boolean noticeState = true;

    public static boolean getIsTV() {
        return ISTV == null ? true : ISTV.booleanValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 默认UI为无界UI(4)
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(context, "Interface_Style", 4);
        if (Interface_Style == 0||Interface_Style == 3){
            /*旧UI*/
            int Topic = SharePreferenceDataUtil.getSharedIntData(this, "Topic", 0);
            if (Topic == 0){
                /*无专题*/
                setContentView(R.layout.activity_main);
            }else{
                /*有专题*/
                setContentView(R.layout.activity_mains);
            }
        }else if (Interface_Style == 1){
            /*新UI*/
            setContentView(R.layout.activity_mainss);/*切换UI*/
        }else if (Interface_Style == 2){
            /*新UI*/
            setContentView(R.layout.activity_mainsss);/*切换UI*/
        }else if (Interface_Style == 4){
            getWindow().setBackgroundDrawableResource(android.R.color.black);
            setContentView(R.layout.activity_main_infinity);
            initInfinityUIAdapter();
        }
        MyApplication mApp = (MyApplication) getApplication();
        technology = mApp.getTechnology();
        mCacheUtils = LruCacheUtils.getInstance();
        homeFrom = from;
        homeParams = params;
        if ("".equals(technology)) {
            if (screenSize > 9) {
                ISTV = true;
                devicetype = "TV";
            } else {
                ISTV = false;
                devicetype = "MOBILE";
            }
        } else {
            if (null != technology && !"null".equals(technology)) {
                if (screenSize > 9) {
                    ISTV = true;
                    devicetype = "TV";
                } else {
                    ISTV = false;
                    devicetype = "MOBILE";
                }
            } else {
                ISTV = true;
                devicetype = "TV";
            }
        }
        live = SharePreferenceDataUtil.getSharedIntData(this, Constant.ogy, 0);
        search = SharePreferenceDataUtil.getSharedIntData(this, Constant.hym, 0);
        searchport = SharePreferenceDataUtil.getSharedIntData(this, Constant.vtk, 9978);
        
        Intent intent = getIntent();
        category = intent.getStringExtra("category");
        initView();
        initData();
        runTime();
        initwhiteBorder();

        PackageManager packageManager = getPackageManager();
        ComponentName myServicesComponent = new ComponentName(this, "com.shenma.tvlauncher.view.MyServices");
        try {
            ServiceInfo myServicesInfo = packageManager.getServiceInfo(myServicesComponent, 0);
            if (!myServicesInfo.enabled) {
                throw new RuntimeException("");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("");
        }
        ComponentName myServiceComponent = new ComponentName(this, "com.shenma.tvlauncher.view.MyService");
        try {
            ServiceInfo myServiceInfo = packageManager.getServiceInfo(myServiceComponent, 0);
            if (!myServiceInfo.enabled) {
                throw new RuntimeException("");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("");
        }
        ComponentName jsonServiceComponent = new ComponentName(this, "com.shenma.tvlauncher.view.JSONService");
        try {
            ServiceInfo jsonServiceInfo = packageManager.getServiceInfo(jsonServiceComponent, 0);
            if (!jsonServiceInfo.enabled) {
                throw new RuntimeException("");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("");
        }
        lib = getPackageName();
    }

    private void runTime() {
        startService(new Intent(this, MyServices.class));
        startService(new Intent(this, JSONService.class));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        rl_bg.setPadding(AttachedToWindow(), 0, 0, 0);
    }

    private void getGongGao() {
        // 优化：先尝试从缓存加载
        String cachedGonggao = sp.getString("cached_gonggao", null);
        long cachedTime = sp.getLong("cached_gonggao_time", 0);
        long currentTime = System.currentTimeMillis();
        // 缓存有效期5分钟
        if (cachedGonggao != null && (currentTime - cachedTime) < 5 * 60 * 1000) {
            // 使用缓存数据
            if (gongGao != null && gongGaoRoot != null) {
                gongGaoRoot.setVisibility(View.VISIBLE);
                gongGao.setText(cachedGonggao);
                gonggaoText = cachedGonggao;
                gongGao.setMarqueeRepeatLimit(-1);
                gongGao.startScroll();
                int currentStyle = SharePreferenceDataUtil.getSharedIntData(this, "Interface_Style", 4);
                if (currentStyle == 4) {
                    updateInfinityGonggao(cachedGonggao);
                }
            }
            // 后台更新缓存（静默刷新）
            loadGongGaoFromServer(false);
            return;
        }
        
        // 缓存过期或不存在，从服务器加载
        loadGongGaoFromServer(true);
    }
    
    // 优化：从服务器加载跑马公告
    private void loadGongGaoFromServer(final boolean showLoading) {
        initRequestQueue();
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=notices",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        RequestResponse(response, showLoading);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (showLoading) {
                RequestError(error);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String codedata = "t=" + GetTimeStamp.timeStamp();
                String rc4data = null;
                if (miType == 1) {
                    rc4data = Rc4.encry_RC4_string(codedata, RC4KEY);
                } else if (miType == 2) {
                    try {
                        rc4data = Rsa.encrypt_Rsa(codedata, RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    rc4data = AES.encrypt_Aes(AESKEY,codedata, AESIV);
                }

                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(codedata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  
                return headers;
            }
        };
        // 优化：设置超时时间
        stringRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                5000, // 5秒超时
                1, // 重试1次
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(stringRequest);
    }

    public void RequestResponse(String response) {
        RequestResponse(response, true);
    }
    
    // 优化：支持静默刷新模式
    public void RequestResponse(String response, boolean showLoading) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code"); 
            String msg = jSONObject.optString("msg"); 
            if (code == 200){
                gongGaoRoot.setVisibility(View.VISIBLE);
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                String decodedMsg = "";
                if (miType == 1) {
                    decodedMsg = URLDecoder.decode(Rc4.decry_RC4(msg,RC4KEY), "UTF-8");
                } else if (miType == 2) {
                    decodedMsg = URLDecoder.decode(Rsa.decrypt_Rsa(msg,RSAKEY), "UTF-8");
                } else if (miType == 3) {
                    decodedMsg = URLDecoder.decode(AES.decrypt_Aes(AESKEY,msg, AESIV), "UTF-8");
                }
                // 优化：保存到缓存
                sp.edit().putString("cached_gonggao", decodedMsg)
                        .putLong("cached_gonggao_time", System.currentTimeMillis())
                        .apply();
                
                if (showLoading && gongGaoRoot != null) {
                    gongGaoRoot.setVisibility(View.VISIBLE);
                }
                if (gongGao != null) {
                gongGao.setText(decodedMsg);
                gongGao.setMarqueeRepeatLimit(-1);
                gongGao.startScroll();
                }
                gonggaoText = decodedMsg;
                
                int currentStyle = SharePreferenceDataUtil.getSharedIntData(this, "Interface_Style", 4);
                if (currentStyle == 4) {
                    updateInfinityGonggao(decodedMsg);
                }
            }else{
                if (showLoading && gongGaoRoot != null) {
                gongGaoRoot.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateInfinityGonggao(String text) {
        try {
            if (rf != null) {
                rf.updateGonggao(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("titile_position", titile_position);
        outState.putFloat("fromXDelta", fromXDelta);
        if (whiteBorder != null) {
            int wWidth = whiteBorder.getWidth();
            int wHeight = whiteBorder.getHeight();
            float wX = whiteBorder.getX();
            float wY = whiteBorder.getY();
            outState.putInt("wWidth", wWidth);
            outState.putFloat("wX", wX);
            outState.putInt("wHeight", wHeight);
            outState.putFloat("wY", wY);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        titile_position = savedInstanceState.getInt("titile_position");
        float toX = savedInstanceState.getFloat("fromXDelta");
        if (titile_position != 0) {
            mAnimationSet = new AnimationSet(true);
            mTranslateAnimation = new TranslateAnimation(
                    fromXDelta, toX, 0f, 0f);
            initAnimation(mAnimationSet, mTranslateAnimation);
            iv_titile.startAnimation(mAnimationSet); 
            fromXDelta = toX;
        }
        if (whiteBorder != null) {
            whiteBorder.setVisibility(View.GONE);
            fl_main.removeView(whiteBorder);
            whiteBorder = null;
        }
        int wWidth = savedInstanceState.getInt("wWidth");
        int wHeight = savedInstanceState.getInt("wHeight");
        float wX = savedInstanceState.getFloat("wX");
        float wY = savedInstanceState.getFloat("wY");
        this.whiteBorder = new ImageView(this);
        fl_main.addView(whiteBorder);
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(context, "Interface_Style", 4);
        if (Interface_Style == 0){
            whiteBorder.setBackgroundResource(R.drawable.white_border);
        }else if (Interface_Style == 1){

        }else if (Interface_Style == 2){

        }else if (Interface_Style == 3){

        }
        FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(wWidth, wHeight);
        layoutparams.leftMargin = (int) wX;
        layoutparams.topMargin = (int) wY;
        whiteBorder.setLayoutParams(layoutparams);
        whiteBorder.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mQueue) {
            mQueue.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
        queryInstalledApp();
        isRunning = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(mReceiver);
        unregisterReceiver(mWallReceiver);
        if (null != mQueue) {
            mQueue.cancelAll(this);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                double value = Double.valueOf(Utils.getVersion(HomeActivity.this).toString());
                String bt = getString(R.string.app_name) + "_v" + value;
                if (noticeState){
                    if (bitmap != null) {
                        View viewById = findViewById(R.id.notice);
                        viewById.setVisibility(View.INVISIBLE);
                        noticeState = false;
                    }else if (isRunning) {
                        showExitDialog(bt, HomeActivity.this);
                        //Logger.v("HomeActivity", "Activity isRunning");
                    } else {
                        //Logger.v("HomeActivity", "Activity not isRunning");
                    }
                } else {
                    if (isRunning) {
                        showExitDialog(bt, HomeActivity.this);
                        //Logger.v("HomeActivity", "Activity isRunning");
                    } else {
                        //Logger.v("HomeActivity", "Activity not isRunning");
                    }
                }
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    protected void initView() {
        loadViewLayout();
        findViewById();
        setListener();
        // Style 4 没有固定的 rb_recommend，需要跳过
        int currentStyle = SharePreferenceDataUtil.getSharedIntData(this, "Interface_Style", 4);
        if (currentStyle != 4 && rb_recommend != null) {
            rb_recommend.setChecked(true);
        }
        registerNetworkReceiver();
        registerPackageReceiver();
        registerWallpaperReceiver();
        homeHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME, 1000);// 刷新时间
    }

    /**
     * 安装一个apk文件
     *
     * @param file
     */
    protected void installApk(String file) {
        File updateFile = new File(file.trim());
        try {
            String[] args2 = {"chmod", "604", updateFile.getPath()};
            Runtime.getRuntime().exec(args2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(updateFile), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void registerNetworkReceiver() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(receiver, filter);
    }

    private void registerPackageReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        mFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        mFilter.addDataScheme("package");
        registerReceiver(mReceiver, mFilter);
    }

    private void initTitle(int position) {
        // Style 4 不使用 title_group 动画
        int currentStyle = SharePreferenceDataUtil.getSharedIntData(this, "Interface_Style", 4);
        if (currentStyle == 4) {
            return;
        }
        if (position != 0) {
            float toX = title_group.getChildAt(position).getX();
            mAnimationSet = new AnimationSet(true);
            mTranslateAnimation = new TranslateAnimation(fromXDelta, toX, 0.0f, 0.0f);
            initAnimation(mAnimationSet, mTranslateAnimation);
            iv_titile.startAnimation(mAnimationSet);// titile蓝色横条图片的动画切换
            fromXDelta = toX;
        }
    }

    private void registerWallpaperReceiver() {
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("com.hd.changewallpaper");
        registerReceiver(mWallReceiver, mFilter);
    }

    private void initData() {
        checkUpdate();
        // 优化：复用单例RequestQueue，并行加载公告
        initRequestQueue();
        // 并行加载跑马公告和弹窗公告
        getGongGao();
        initNotice();
        initLogin();
        initLogo();
        if (search == 1){
            start(searchport);
        }
    }
    
    // 优化：初始化RequestQueue（复用单例）
    private void initRequestQueue() {
        if (mQueue == null) {
            try {
                // 使用MyVolley的单例RequestQueue
                mQueue = com.shenma.tvlauncher.application.MyVolley.getRequestQueue();
            } catch (Exception e) {
                // 如果MyVolley未初始化，则创建新的
                mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
            }
        }
    }

    private void initLogo(){
        String logo_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Logo_url", null),Constant.d);
        if (!logo_url.equals("")){
            Logo_url = logo_url;
            mediaHandler.sendEmptyMessage(5);
        }
    }

    private void logoloadImg() {
        Glide.with(this).load(Logo_url).into(this.logo);
    }

    private void initLogin() {

        String[] keys = {"play_decode", "play_ratio", "play_core", "live_core", "play_jump", "play_jump_end"};
        String[] defaultValues = {"硬解码", "全屏拉伸", "IJK", "自动", "0秒", "0秒"};
        int[] sharedIntData = {1, -1, -1, -1, -1, -1}; // 对应mIsHwDecode的默认值为1，其他的默认值为-1
        for (int i = 0; i < keys.length; i++) {
            String value = sp.getString(keys[i], null);
            if (value == null) {
                sp.edit()
                        .putString(keys[i], Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, keys[i], defaultValues[i]), Constant.d))
                        .commit();
                if (i == 0) {
                    sp.edit()
                            .putInt("mIsHwDecode", sharedIntData[i])
                            .commit();
                }
            }
        }
        int login_type = SharePreferenceDataUtil.getSharedIntData(this, "login_type", 0);
        if (login_type == 1){
            /*机器码登录*/
            username = sp.getString("userName", null);
            password = sp.getString("passWord", null);
            if (null == username && null == password){
                username = Utils.GetAndroidID(this);
                password = Utils.GetAndroidID(this);
                requestlogin(username, password,1);
            }else{
                requestlogin(username, password,1);
            }
        }else if (login_type == 0|| login_type == 2){
            /*账户登录*/
            username = sp.getString("userName", null);
            password = sp.getString("passWord", null);
            if (null != username && null != password){
                requestlogin(username, password,0);
            }
        }else if(login_type == 3){
            /*MAC地址登录*/
            username = sp.getString("userName", null);
            password = sp.getString("passWord", null);
            Mac = MacUtils.getMac(true).replace(":", "").toUpperCase();
            if (null == username && null == password){
                username = Mac;
                password = Mac;
                requestlogin(username, password,2);
            }else{
                requestlogin(username, password,2);
            }
        }
    }

    private void requestlogin(String username, String password, int type) {
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        if (type == 0){
            String logindate = "account=" + username + "&password=" + password + "&markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            if (miType == 1) {
                accountLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", Rc4.encry_RC4_string(logindate, RC4KEY), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
            } else if (miType == 2) {
                try {
                    accountLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", Rsa.encrypt_Rsa(logindate, RSAKEY), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (miType == 3) {
                accountLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", AES.encrypt_Aes(AESKEY,logindate, AESIV), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
            }



        }
        if (type == 1){
            String logindate = "account=" + username + "&password=" + password + "&markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            if (miType == 1) {
                markcodeLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", Rc4.encry_RC4_string(logindate, RC4KEY), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
            } else if (miType == 2) {
                try {
                    markcodeLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", Rsa.encrypt_Rsa(logindate, RSAKEY), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (miType == 3) {
                markcodeLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", AES.encrypt_Aes(AESKEY,logindate, AESIV), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
            }


        }
        if (type == 2){
            String logindate = "account=" + username + "&password=" + password + "&markcode=" + Utils.GetAndroidID(this) + "&t=" + GetTimeStamp.timeStamp();
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            if (miType == 1) {
                MacLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", Rc4.encry_RC4_string(logindate, RC4KEY), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
            } else if (miType == 2) {
                try {
                    MacLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", Rsa.encrypt_Rsa(logindate, RSAKEY), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (miType == 3) {
                MacLogin(User_url + "/api?app=" + Api.APPID + "&act=user_logon", AES.encrypt_Aes(AESKEY,logindate, AESIV), Md5Encoder.encode(new StringBuilder(String.valueOf(logindate)).append("&").append(Appkey).toString()));
            }
        }
    }

    private void accountLogin(String url,final String rc4data,final String sign) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url ,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        AccountResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                RequestError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    public void AccountResponse(String response) {
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jo = new JSONObject(response);
            if (jo.optInt("code") == 200) {
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                String msg = null;
                if (miType == 1) {
                    msg = Rc4.decry_RC4(jo.optString("msg"), RC4KEY);
                } else if (miType == 2) {
                    msg = Rsa.decrypt_Rsa(jo.optString("msg"), RSAKEY);
                } else if (miType == 3) {
                    msg = AES.decrypt_Aes(AESKEY,jo.optString("msg"), AESIV);
                }
                JSONObject jot = new JSONObject(msg);
                JSONObject job = new JSONObject(jot.getString("info"));
                String ck = jot.getString("token");
                String vip = job.getString("vip");
                int Exit = job.getInt("Exit");
                mediaHandler.sendEmptyMessage(2);
                sp.edit().putString("userName", username).putString("passWord", password).putString("ckinfo", ck).putString("vip", vip).putInt("Exit", Exit).commit();
                return;
            }
            mediaHandler.sendEmptyMessage(3);
            sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void markcodeLogin(String url,final String rc4data,final String sign) {

        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url ,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        MarkcodeResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                RequestError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    public void MarkcodeResponse(String response) {
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jo = new JSONObject(response);

            if (jo.optInt("code") == 200) {
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                String msg = null;
                if (miType == 1) {
                    msg = Rc4.decry_RC4(jo.optString("msg"), RC4KEY);
                } else if (miType == 2) {
                    msg = Rsa.decrypt_Rsa(jo.optString("msg"), RSAKEY);
                } else if (miType == 3) {
                    msg = AES.decrypt_Aes(AESKEY,jo.optString("msg"), AESIV);
                }
                JSONObject jot = new JSONObject(msg);
                JSONObject job = new JSONObject(jot.getString("info"));
                String ck = jot.getString("token");
                String vip = job.getString("vip");
                int Exit = job.getInt("Exit");
                mediaHandler.sendEmptyMessage(2);
                sp.edit().putString("userName", username).putString("passWord", password).putString("ckinfo", ck).putString("vip", vip).putInt("Exit", Exit).commit();
                return;
            }else if(jo.optInt("code") == 122){
                AutoRegister();
            }
            sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void AutoRegister() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=user_reg",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        Response(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                RequestError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String regdata = "user=" + Utils.GetAndroidID(context) + "&password=" + Utils.GetAndroidID(context) + "&markcode=" + Utils.GetAndroidID(context) + "&t=" + GetTimeStamp.timeStamp();
                String rc4data = null;
                if (miType == 1) {
                    rc4data = Rc4.encry_RC4_string(regdata, RC4KEY);
                } else if (miType == 2) {
                    try {
                        rc4data = Rsa.encrypt_Rsa(regdata, RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    rc4data = AES.encrypt_Aes(AESKEY,regdata, AESIV);
                }

                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(regdata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    public void Response(String response) {
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                requestlogin(username, password,1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void MacLogin(String url,final String rc4data,final String sign) {

        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        StringRequest stringRequest = new StringRequest(Request.Method.POST,url ,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        MacResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                RequestError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    public void MacResponse(String response) {
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jo = new JSONObject(response);

            if (jo.optInt("code") == 200) {
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                String msg = null;
                if (miType == 1) {
                    msg = Rc4.decry_RC4(jo.optString("msg"), RC4KEY);
                } else if (miType == 2) {
                    msg = Rsa.decrypt_Rsa(jo.optString("msg"), RSAKEY);
                } else if (miType == 3) {
                    msg = AES.decrypt_Aes(AESKEY,jo.optString("msg"), AESIV);
                }
                JSONObject jot = new JSONObject(msg);
                JSONObject job = new JSONObject(jot.getString("info"));
                String ck = jot.getString("token");
                String vip = job.getString("vip");
                int Exit = job.getInt("Exit");
                mediaHandler.sendEmptyMessage(2);
                sp.edit().putString("userName", username).putString("passWord", password).putString("ckinfo", ck).putString("vip", vip).putInt("Exit", Exit).commit();
                return;
            }else if(jo.optInt("code") == 122){
                AutoMacRegister();
            }
            sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void AutoMacRegister() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=user_reg",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        RegisterMacResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                RequestError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String regdata = "user=" + Mac + "&password=" + Mac + "&markcode=" + Utils.GetAndroidID(context) + "&t=" + GetTimeStamp.timeStamp();
                String rc4data = null;
                if (miType == 1) {
                    rc4data = Rc4.encry_RC4_string(regdata, RC4KEY);
                } else if (miType == 2) {
                    try {
                        rc4data = Rsa.encrypt_Rsa(regdata, RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    rc4data = AES.encrypt_Aes(AESKEY,regdata, AESIV);
                }

                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(regdata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    public void RegisterMacResponse(String response) {
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                requestlogin(username, password,2);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkUpdate() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=ini",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        CheckUpdateResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                RequestError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String regdata = "user=" + Utils.GetAndroidID(context) + "&password=" + Utils.GetAndroidID(context) + "&markcode=" + Utils.GetAndroidID(context) + "&t=" + GetTimeStamp.timeStamp();
                String rc4data = null;
                if (miType == 1) {
                    rc4data = Rc4.encry_RC4_string(regdata, RC4KEY);
                } else if (miType == 2) {
                    try {
                        rc4data = Rsa.encrypt_Rsa(regdata, RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    rc4data = AES.encrypt_Aes(AESKEY,regdata, AESIV);
                }

                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(regdata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    public void CheckUpdateResponse(String response) {
        try {
            JSONObject jo = new JSONObject(response);
            String code = jo.optString("code");
            JSONObject jot = new JSONObject(jo.optString("msg"));
            Double app_bb = Double.valueOf(jot.optDouble("app_bb"));
            app_nshow = jot.optString("app_nshow");
            app_nurl = jot.optString("app_nurl");
            compel = jot.optInt("compel");
            double value = Double.valueOf(Utils.getVersion(HomeActivity.this).toString()).doubleValue();
            if (response != null && "200".equals(code) && value < app_bb.doubleValue()) {
                if (app_nshow != null) {
                    mediaHandler.sendEmptyMessage(4);
                }
            }

            JSONObject exten = new JSONObject(jot.optString(Constant.f));
            player_name = exten.optString(Constant.u);
            player_update = exten.optString(Constant.ev);
            player_zip_md5 = exten.optString(Constant.sr);
            player_download_url = exten.optString(Constant.kw);

            if (response != null && "200".equals(code) && player_update.equals("1")) {
                if (player_download_url != null) {
                    if (!player_zip_md5.equals(Utils.getMD5Checksum("/data/data/" + getPackageName() + "/files/" + player_name))){
                        mediaHandler.sendEmptyMessage(7);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void loadViewLayout() {
    }

    protected void findViewById() {
        rl_bg = (RelativeLayout) findViewById(R.id.rl_bg);
        Bitmap bmp = mCacheUtils.getBitmapFromMemCache(String.valueOf(R.drawable.bg));
        if (bmp == null) {
            bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
            mCacheUtils.addBitmapToMemoryCache(String.valueOf(R.drawable.bg), bmp);
        }
        rl_bg.setBackgroundDrawable(new BitmapDrawable(getResources(), bmp));
        fl_main = (FrameLayout) findViewById(R.id.fl_main);
        tv_time = (TextView) findViewById(R.id.tv_main_time);
        time_colon = (TextView) findViewById(R.id.time_colon);
        iv_net_state = (ImageView) findViewById(R.id.iv_net_state);
        iv_titile = (ImageView) findViewById(R.id.iv_titile);
        vpager = (ViewPager) findViewById(R.id.pager);
        title_group = (LinearLayout) findViewById(R.id.title_group);
        rb_recommend = (RadioButton) findViewById(R.id.rb_recommend);
        rb_Internet = (RadioButton) findViewById(R.id.rb_Internet);
        rb_video_type = (RadioButton) findViewById(R.id.rb_video_type);
        rb_settings = (RadioButton) findViewById(R.id.rb_settings);
        rg_video_type_bottom = (RadioGroup) findViewById(R.id.rg_video_type_bottom);
        tv_main_date = (TextView) findViewById(R.id.tv_main_date);
        ll_rb = (LinearLayout) findViewById(R.id.ll_rb);
        tv_update_msg = (TextView) findViewById(R.id.tv_update_msg);
        gongGao = (AlwaysMarqueeTextView) findViewById(R.id.gonggao);
        gongGaoRoot = (LinearLayout) findViewById(R.id.gonggao_root);
        logo = (ImageView) findViewById(R.id.logo);//首页logo
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(context, "Interface_Style", 4);
        if (Interface_Style == 0||Interface_Style == 3){
            /*旧UI*/

        }else if (Interface_Style == 1){
            /*新UI*/
            home_top_search = (ImageView) findViewById(R.id.home_top_search);//搜索
            this.home_top_search.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    username = sp.getString("userName", null);
                    if (username == null) {
                        Utils.showToast(context,R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                        startActivity(new Intent(context, UserActivity.class));
                        return;
                    }
                    Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                    intent.putExtra("TYPE", "ALL");
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            home_top_record = (ImageView) findViewById(R.id.home_top_record);//历史记录
            this.home_top_record.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    username = sp.getString("userName", null);
                    if (username == null) {
                        Utils.showToast(context,R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                        startActivity(new Intent(context, UserActivity.class));
                        return;
                    }
                    Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

        }else if (Interface_Style == 2){
            /*新UI*/
            home_top_searchs = (LinearLayout) findViewById(R.id.home_top_searchs);//搜索
            this.home_top_searchs.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    username = sp.getString("userName", null);
                    if (username == null) {
                        Utils.showToast(context,R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                        startActivity(new Intent(context, UserActivity.class));
                        return;
                    }
                    Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                    intent.putExtra("TYPE", "ALL");
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            home_top_records = (LinearLayout) findViewById(R.id.home_top_records);//历史记录
            this.home_top_records.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    username = sp.getString("userName", null);
                    if (username == null) {
                        Utils.showToast(context,R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                        startActivity(new Intent(context, UserActivity.class));
                        return;
                    }
                    Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            home_user = (LinearLayout) findViewById(R.id.home_user);//个人中心
            this.home_user.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(HomeActivity.this, UserActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            home_collect = (LinearLayout) findViewById(R.id.home_collect);//个性设置
            this.home_collect.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(HomeActivity.this, SettingWallpaperActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            home_play_set = (LinearLayout) findViewById(R.id.home_play_set);//播放设置
            this.home_play_set.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(HomeActivity.this, SettingPlayActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            home_clear = (LinearLayout) findViewById(R.id.home_clear);//清理记录
            this.home_clear.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(HomeActivity.this, ClearActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

            home_wallpaper = (LinearLayout) findViewById(R.id.home_wallpaper);//关于我们
            this.home_wallpaper.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
                }
            });

        }
        fragments = new ArrayList<Fragment>();
        Bundle args = new Bundle();

        if (Interface_Style == 0||Interface_Style == 3){
            /*旧UI*/
            int Topic = SharePreferenceDataUtil.getSharedIntData(this, "Topic", 0);
            if (Topic == 0){
                /*无专题*/
                rf = new RecommendFragment();
                args.putInt("num", 0);
                rf.setArguments(args);
                fragments.add(rf);
                sf = new SettingFragment();
                args.putInt("num",1);
                sf.setArguments(args);
                fragments.add(sf);
            }else{
                /*有专题*/
                rf = new RecommendFragment();
                args.putInt("num", 0);
                rf.setArguments(args);
                fragments.add(rf);
                mf = new TopicFragment();
                args.putInt("num", 1);
                mf.setArguments(args);
                fragments.add(mf);
                sf = new SettingFragment();
                args.putInt("num",2);
                sf.setArguments(args);
                fragments.add(sf);
            }
        }else if (Interface_Style == 1){
            int Topic = SharePreferenceDataUtil.getSharedIntData(this, "Topic", 0);
            if (Topic == 0){
                /*无专题*/
                rf = new RecommendFragment();
                args.putInt("num", 0);
                rf.setArguments(args);
                fragments.add(rf);
            }else{
                /*有专题*/
                rf = new RecommendFragment();
                args.putInt("num", 0);
                rf.setArguments(args);
                fragments.add(rf);
                mf = new TopicFragment();
                args.putInt("num", 1);
                mf.setArguments(args);
                fragments.add(mf);
            }

        }else if (Interface_Style == 2){
            /*新UI*/
            int Topic = SharePreferenceDataUtil.getSharedIntData(this, "Topic", 0);
            if (Topic == 0){
                /*无专题*/
                rf = new RecommendFragment();
                args.putInt("num", 0);
                rf.setArguments(args);
                fragments.add(rf);
            }else{
                /*有专题*/
                rf = new RecommendFragment();
                args.putInt("num", 0);
                rf.setArguments(args);
                fragments.add(rf);
                mf = new TopicFragment();
                args.putInt("num", 1);
                mf.setArguments(args);
                fragments.add(mf);
            }

        }else if (Interface_Style == 4){
            /*无界UI - 只显示推荐页面，不添加设置页面*/
            rf = new RecommendFragment();
            args.putInt("num", 0);
            rf.setArguments(args);
            fragments.add(rf);
        }

        adapter = new FragAdapter(getSupportFragmentManager(), fragments);
        vpager.setAdapter(adapter);
        vpager.setCurrentItem(0);
        vpager.setPageTransformer(true, new DepthPageTransformer());
        if (ISTV) {
            try {
                Field field = ViewPager.class.getDeclaredField("mScroller");
                field.setAccessible(true);
                FixedSpeedScroller scroller = new FixedSpeedScroller(
                        vpager.getContext(), new AccelerateInterpolator());
                field.set(vpager, scroller);
                scroller.setmDuration(700);
            } catch (Exception e) {
            }
        }
    }

    public void initwhiteBorder() {
        this.whiteBorder = new ImageView(this);
        fl_main.addView(whiteBorder);


        FrameLayout.LayoutParams layoutparams = new FrameLayout.LayoutParams(128, 130);
        layoutparams.leftMargin = 42;
        layoutparams.topMargin = 183;
        whiteBorder.setLayoutParams(layoutparams);
        whiteBorder.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void setListener() {
        // 更换背景
        String fileName = sp.getString("wallpaperFileName", null);
        if (fileName != null && !"".equals(fileName)) {
            changeBackImage(fileName);
        }

        // Style 4 使用动态加载的分类按钮，不需要这段初始化逻辑
        int currentStyle = SharePreferenceDataUtil.getSharedIntData(context, "Interface_Style", 4);
        if (currentStyle != 4 && title_group.getChildCount() > 0) {
            fromXDelta = title_group.getChildAt(0).getX();
            int j = title_group.getChildCount();
            for (int i = 0; i < j; i++) {
                final int index = i;
                View v = title_group.getChildAt(i);
                v.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            // if(ISTV){
                            whiteBorder.clearAnimation();
                            whiteBorder.setVisibility(View.INVISIBLE);
                            // }
                            ((RadioButton) title_group.getChildAt(index)).setSelected(true);
                            vpager.setCurrentItem(index, true);
                        } else {
                            ((RadioButton) title_group.getChildAt(index)).setSelected(false);
                        }
                    }
                });
                v.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        // if(ISTV){
                        whiteBorder.clearAnimation();
                        whiteBorder.setVisibility(View.INVISIBLE);
                        // }
                        vpager.setCurrentItem(index, true);
                    }
                });
            }
        }

//        String json = "[{\"type_en\":\"TVPLAY\",\"type_name\":\"电视剧\",\"type_status\":1},{\"type_en\":\"MOVIE\",\"type_name\":\"电影\",\"type_status\":1},{\"type_en\":\"TVSHOW\",\"type_name\":\"综艺\",\"type_status\":1},{\"type_en\":\"COMIC\",\"type_name\":\"动漫\",\"type_status\":1},{\"type_en\":\"OUMEIJU\",\"type_name\":\"美剧\",\"type_status\":1},{\"type_en\":\"HANGUOJU\",\"type_name\":\"韩剧\",\"type_status\":1},{\"type_en\":\"MOVIE_4K\",\"type_name\":\"4K\",\"type_status\":1},{\"type_en\":\"MOVIE_ZB\",\"type_name\":\"少儿\",\"type_status\":1}]";

        String json = category;
        List<Map<String, Object>> dataList = new Gson().fromJson(json, new TypeToken<List<Map<String, Object>>>(){}.getType());
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(context, "Interface_Style", 4);
        
        // 无界UI的分类标签将在RecommendFragment中添加到category_container_infinity
        // 这里不再添加到title_group
        
        for (int ii = 0; ii < dataList.size(); ii++) {
            Map<String, Object> item = dataList.get(ii);
            final String typeEn = (String) item.get("type_en");
            final String typeName = (String) item.get("type_name");
            final Number typestatus = (Number) item.get("type_status");
            categorypwd = (String) item.get("categorypwd");
            final int typeStatus = typestatus.intValue();
            int width = getResources().getDimensionPixelSize(R.dimen.sm_140);
            int height = 0;

            if (Interface_Style == 0||Interface_Style == 3) {
                height = getResources().getDimensionPixelSize(R.dimen.sm_40);
            }else if (Interface_Style == 1) {
                height = getResources().getDimensionPixelSize(R.dimen.sm_40);
            } else if (Interface_Style == 2) {
                height = getResources().getDimensionPixelSize(R.dimen.sm_40);
            } else if (Interface_Style == 4) {
                // 无界主题：水平按钮 自适应宽度 x 40dp
                width = LinearLayout.LayoutParams.WRAP_CONTENT;
                height = getResources().getDimensionPixelSize(R.dimen.sm_40);
            }

            RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(width, height);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(width, height);
            if (Interface_Style == 4) {
                // 添加右边间距（水平布局）
                linearLayoutParams.setMargins(0, 0, 10, 0);
            }else{
                layoutParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.sm_10));
            }

            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(ii);
            radioButton.setText(typeName);
            if (Interface_Style == 4) {
                // 无界主题：使用18sp字体
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            } else {
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.sm_24));
            }
            radioButton.setLayoutParams(layoutParams);
            radioButton.setButtonDrawable(null);
            radioButton.setButtonDrawable(android.R.color.transparent);/*兼容安卓4.4*/

            radioButton.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            radioButton.setGravity(Gravity.CENTER); //文字居中
            radioButton.setMarqueeRepeatLimit(Integer.MAX_VALUE); // 设置为最大整数值以实现无限循环
            radioButton.setSingleLine(true); // 跑马灯效果需要设置为单行

            if (Interface_Style == 0||Interface_Style == 3){
                Drawable backgroundDrawable = context.getResources().getDrawable(R.drawable.button_focuss);
                radioButton.setBackground(backgroundDrawable);
            }else if (Interface_Style == 1){
                Drawable backgroundDrawable = context.getResources().getDrawable(R.drawable.home_top_item_bgs);
                radioButton.setBackground(backgroundDrawable);
            }else if (Interface_Style == 2){
                Drawable backgroundDrawable = context.getResources().getDrawable(R.drawable.home_top_item_bg);
                radioButton.setBackground(backgroundDrawable);
            }else if (Interface_Style == 4){
                // 无界主题：使用垂直导航栏样式
                Drawable backgroundDrawable = context.getResources().getDrawable(R.drawable.button_focuss);
                radioButton.setBackground(backgroundDrawable);
                radioButton.setTextColor(context.getResources().getColor(R.color.white));
            }
            radioButton.setFocusable(true);
            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle pBundle = new Bundle();
                    pBundle.putString("TYPE", typeEn);
                    pBundle.putString("TYPENAME", typeName);
                    if (typeStatus != 1){
                        En = typeEn;
                        Name = typeName;
                        if (PWD != null){//记住密码
                            if (typeEn.equals("LIVE")){
                                Log.d(TAG, "========== 点击直播标签(有密码) ==========");
                                Log.d(TAG, "live value = " + live);
                                String username = sp.getString("userName", null);
                                Log.d(TAG, "username = " + username);
                                if(TextUtils.isEmpty(username)) {
                                    Log.d(TAG, "用户未登录，跳转登录页");
                                    Intent mIntent = new Intent();
                                    mIntent.setClass(context, UserActivity.class);
                                    startActivity(mIntent);
                                }else{
                                    long time = System.currentTimeMillis() / 1000;
                                    long vip = Long.parseLong(sp.getString("vip", ""));
                                    Log.d(TAG, "当前时间戳 = " + time + ", VIP到期时间 = " + vip);
                                    if (time > vip&&!sp.getString("vip", "").equals("999999999")){
                                        Log.d(TAG, "VIP已过期，跳转续费页");
                                        Intent mIntent = new Intent();
                                        mIntent.setClass(context, EmpowerActivity.class);
                                        startActivity(mIntent);
                                    }else{
                                        Log.d(TAG, "VIP有效，检查live值: " + live);
                                        if (live == 1){
                                            Log.d(TAG, "live=1，跳转直播页面");
                                            Intent mIntent = new Intent();
                                            mIntent.setClass(context, LivePlayerActivity.class);
                                            startActivity(mIntent);
                                        }else{
                                            Log.d(TAG, "live!=1，显示未激活提示");
                                            Utils.showToast(context, R.string.Not_yet_activated, R.drawable.toast_shut);
                                        }
                                    }
                                }
                                return;
                            }
                            openActivity(VodTypeActivity.class, pBundle);//无需密码
                        }else{
                            if (typeEn.equals("USER")){
                                Intent mIntent = new Intent();
                                mIntent.setClass(context, UserActivity.class);
                                startActivity(mIntent);
                                return;
                            }else if (typeEn.equals("EMPOWER")){
                                String username = sp.getString("userName", null);
                                if(TextUtils.isEmpty(username)) {
                                    Intent mIntent = new Intent();
                                    mIntent.setClass(context, UserActivity.class);
                                    startActivity(mIntent);
                                }else {
                                    Intent mIntent = new Intent();
                                    mIntent.setClass(context, EmpowerActivity.class);
                                    startActivity(mIntent);
                                }
                                return;
                            }else{
                                showUserDialog();//需要密码
                            }
                        }
                    }else{
                        if (typeEn.equals("LIVE")){
                            Log.d(TAG, "========== 点击直播标签(无密码) ==========");
                            Log.d(TAG, "live value = " + live);
                            String username = sp.getString("userName", null);
                            Log.d(TAG, "username = " + username);
                            if(TextUtils.isEmpty(username)) {
                                Log.d(TAG, "用户未登录，跳转登录页");
                                Intent mIntent = new Intent();
                                mIntent.setClass(context, UserActivity.class);
                                startActivity(mIntent);
                            }else{
                                long time = System.currentTimeMillis() / 1000;
                                long vip = Long.parseLong(sp.getString("vip", ""));
                                Log.d(TAG, "当前时间戳 = " + time + ", VIP到期时间 = " + vip);
                                if (time > vip&&!sp.getString("vip", "").equals("999999999")){
                                    Log.d(TAG, "VIP已过期，跳转续费页");
                                    Intent mIntent = new Intent();
                                    mIntent.setClass(context, EmpowerActivity.class);
                                    startActivity(mIntent);
                                }else{
                                    Log.d(TAG, "VIP有效，检查live值: " + live);
                                    if (live == 1){
                                        Log.d(TAG, "live=1，跳转直播页面");
                                        Intent mIntent = new Intent();
                                        mIntent.setClass(context, LivePlayerActivity.class);
                                        startActivity(mIntent);
                                    }else{
                                        Log.d(TAG, "live!=1，显示未激活提示");
                                        Utils.showToast(context, R.string.Not_yet_activated, R.drawable.toast_shut);
                                    }
                                }
                            }
                            return;
                        }else if (typeEn.equals("USER")){
                            Intent mIntent = new Intent();
                            mIntent.setClass(context, UserActivity.class);
                            startActivity(mIntent);
                            return;
                        }else if (typeEn.equals("EMPOWER")){
                            String username = sp.getString("userName", null);
                            if(TextUtils.isEmpty(username)) {
                                Intent mIntent = new Intent();
                                mIntent.setClass(context, UserActivity.class);
                                startActivity(mIntent);
                            }else {
                                Intent mIntent = new Intent();
                                mIntent.setClass(context, EmpowerActivity.class);
                                startActivity(mIntent);
                            }
                            return;
                        }
                        openActivity(VodTypeActivity.class, pBundle);
                    }
                }
            });
            
            // 根据主题添加到不同的容器
            // 无界UI的分类标签在RecommendFragment中添加，这里跳过
            if (Interface_Style == 4) {
                // 不添加到title_group，由RecommendFragment处理
            } else {
                rg_video_type_bottom.addView(radioButton, layoutParams);
            }
        }
        int k = dataList.size();
        for (int i = 0; i < k; i++) {
            final int paramInt = i;
            // if(ISTV){
            // Style 4 使用 title_group，不需要设置 rg_video_type_bottom 的焦点监听
            if (Interface_Style == 4) {
                continue;
            }
            rg_video_type_bottom.getChildAt(i).setOnFocusChangeListener(new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        whiteBorder.startAnimation(breathingAnimation);
                        whiteBorder.setVisibility(View.VISIBLE);
                        int[] location = new int[2];
                        v.getLocationOnScreen(location);
                        int width = v.getWidth() - 25;
                        int height = v.getHeight() - 20;
                        float x = (float) location[0];
                        float y = (float) location[1];
                        if (mHeight > 1000 && mWidth > 1000) {
                            y = 947;
                            switch (paramInt) {
                                case 0:
                                    x = 115;
                                    break;
                                case 1:
                                    x = 341;
                                    break;
                                case 2:
                                    x = 566;
                                    break;
                                case 3:
                                    x = 788;
                                    break;
                                case 4:
                                    x = 1011;
                                    break;
                                case 5:
                                    x = 1234;
                                    break;
                                case 6:
                                    x = 1460;
                                    break;
                                case 7:
                                    x = 1685;
                                    break;
                            }
                        } else {
                            if (mHeight == 800 || mHeight == 752) {
                                y = (float) 643;
                            } else if (mHeight == 736) {
                                y = (float) 643 - 16;
                            } else {
                                y = (float) 643 - 32;
                            }
                            switch (paramInt) {
                                case 0:
                                    x = (float) 44 + 9;
                                    break;
                                case 1:
                                    x = (float) 190 + 14;
                                    break;
                                case 2:
                                    x = (float) 340 + 14;
                                    break;
                                case 3:
                                    x = (float) 488 + 14;
                                    break;
                                case 4:
                                    x = (float) 636 + 15;
                                    break;
                                case 5:
                                    x = (float) 784 + 15;
                                    break;
                                case 6:
                                    x = (float) 932 + 18;
                                    break;
                                case 7:
                                    x = (float) 1080 + 20;
                                    break;

                            }
                        }
                        flyWhiteBorder(0, 0, x, y);
                    }
                }
            });
        }
        // 导航按下监听 - Style 4 不使用 RadioGroup 的监听器
        // 注意：这里不能使用 title_group.setOnCheckedChangeListener，因为 title_group 现在是 LinearLayout
        // 这个功能已经在动态创建的 RadioButton 的点击事件中处理

        // Style 4 不使用 title_group 的焦点监听
        if (currentStyle != 4) {
            title_group.setOnFocusChangeListener(new OnFocusChangeListener() {

                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    //Logger.v(TAG, "title_group获取焦点=" + hasFocus);
                }
            });
        }

        vpager.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //Logger.v(TAG, "vpager获取焦点=" + hasFocus);
            }
        });
        /**
         * ViewPager的PageChangeListener(页面改变的监听器)
         */
        vpager.setOnPageChangeListener(new OnPageChangeListener() {

            /**
             * 滑动viewPage页面获取焦点时更新导航标记
             */
            @Override
            public void onPageSelected(int position) {
                // Style 4 不使用 title_group 作为 RadioGroup
                int currentStyle = SharePreferenceDataUtil.getSharedIntData(HomeActivity.this, "Interface_Style", 4);
                if (currentStyle == 4) {
                    return;
                }
                int i = title_group.getChildCount();
                //Logger.v(TAG, "position=" + position + "..i=" + i);
                if (!ISTV) {
                    if (position < i) {
                        ((RadioButton) title_group.getChildAt(position)).setChecked(true);
                    }
                }
                int Topic = SharePreferenceDataUtil.getSharedIntData(HomeActivity.this, "Topic", 0);
                if (Topic == 0){
                    /*无专题*/
                    switch (position) {
                        case 0:
                            if(!title_group.getChildAt(position).isSelected()){
                                if(null!=rf.re_typeLogs){
                                    rf.re_typeLogs[0].requestFocus();
                                }
                            }
                            break;
                        case 1:
                            if(!title_group.getChildAt(position).isSelected()){
                                if(null!=sf.st_typeLogs){
                                    sf.st_typeLogs[0].requestFocus();
                                }
                            }
                            break;
                    }

                }else{
                    /*有专题*/
                    switch (position) {
                        case 0:
                            if(!title_group.getChildAt(position).isSelected()){
                                if(null!=rf.re_typeLogs){
                                    rf.re_typeLogs[0].requestFocus();
                                }
                            }
                            break;
                        case 1:
                            if(!title_group.getChildAt(position).isSelected()){
                                if(null!=mf.mv_typeLogs){
                                    mf.mv_typeLogs[0].requestFocus();
                                }
                            }
                            break;
                        case 2:
                            if(!title_group.getChildAt(position).isSelected()){
                                if(null!=sf.st_typeLogs){
                                    sf.st_typeLogs[0].requestFocus();
                                }
                            }
                            break;
                    }
                }
                float toX = title_group.getChildAt(position).getX();
                //Logger.v(TAG, "viewpage值=" + position);
                //Logger.v(TAG, "toX=" + toX);
                mAnimationSet = new AnimationSet(true);
                mTranslateAnimation = new TranslateAnimation(
                        fromXDelta, toX, 0f, 0f);
                initAnimation(mAnimationSet, mTranslateAnimation);
                iv_titile.startAnimation(mAnimationSet);//titile蓝色横条图片的动画切换
                fromXDelta = toX;
                titile_position = position;
            }


            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int position) {

            }
        });

    }

    /**
     * titile动画
     *
     * @param _AnimationSet
     * @param _TranslateAnimation
     */
    private void initAnimation(AnimationSet _AnimationSet, TranslateAnimation _TranslateAnimation) {
        _AnimationSet.addAnimation(_TranslateAnimation);
        _AnimationSet.setFillBefore(true);
        _AnimationSet.setFillAfter(true);
        _AnimationSet.setDuration(250L);
    }

    /**
     * @brief 窗口消息处理函数。
     * @author joychang
     * @param[in] msg 窗口消息。
     */
    private void onMessage(final Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case WindowMessageID.ERROR:
                    Toast.makeText(getApplicationContext(), "服务器内部异常", Toast.LENGTH_LONG).show();
                    break;
                case WindowMessageID.DOWNLOAD_ERROR:
                    Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_LONG).show();
                    break;
                case WindowMessageID.GET_INFO_SUCCESS:

                    break;
                case WindowMessageID.DOWNLOAD_SUCCESS:
                    // 安装apk
                    installApk(msg.obj.toString());
                    break;
                case WindowMessageID.REFLESH_TIME:
//                    tv_time.setText(Utils.getStringTime("   "));
                    tv_time.setText(Utils.getStringTime(" "));//时间中间的空格
                    tv_main_date.setText(Utils.getStringData());
                    if (time_colon.getVisibility() == View.VISIBLE) {
                        time_colon.setVisibility(View.GONE);
                    } else {
                        time_colon.setVisibility(View.VISIBLE);
                    }
                    homeHandler.sendEmptyMessageDelayed(WindowMessageID.REFLESH_TIME, 1000);
                    break;
                case 1001:// 软件更新 总大小
                    countSize = (Float) msg.obj;
                    break;
                case 1002:// 软件更新 当前下载大小
                    currentSize = (Float) msg.obj;
                    tv_update_msg.setText("正在下载更新 " + (int) (currentSize / countSize * 100) + "%");
                    if (currentSize >= countSize) {
                        tv_update_msg.setVisibility(View.GONE);
                    }
                    break;

            }
        }
    }

    /*初始化弹窗*/
    private void initNotice() {
        // 优化：先尝试从缓存加载
        String cachedNoticeUrl = sp.getString("cached_notice_url", null);
        long cachedTime = sp.getLong("cached_notice_time", 0);
        long currentTime = System.currentTimeMillis();
        // 缓存有效期10分钟（弹窗公告更新频率较低）
        if (cachedNoticeUrl != null && !cachedNoticeUrl.isEmpty() && (currentTime - cachedTime) < 10 * 60 * 1000) {
            // 使用缓存数据
            returnBitMap(cachedNoticeUrl);
            // 后台更新缓存（静默刷新）
            loadNoticeFromServer(false);
            return;
        }
        
        // 缓存过期或不存在，从服务器加载
        loadNoticeFromServer(true);
    }
    
    // 优化：从服务器加载弹窗公告
    private void loadNoticeFromServer(final boolean showLoading) {
        initRequestQueue();
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=notice",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        NoticeResponse(response, showLoading);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                // 静默刷新模式下不处理错误
                if (showLoading) {
                RequestError(error);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String codedata = "t=" + GetTimeStamp.timeStamp();
                String rc4data = null;
                if (miType == 1) {
                    rc4data = Rc4.encry_RC4_string(codedata, RC4KEY);
                } else if (miType == 2) {
                    try {
                        rc4data = Rsa.encrypt_Rsa(codedata, RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    rc4data = AES.encrypt_Aes(AESKEY,codedata, AESIV);
                }

                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(codedata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("data", rc4data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        // 优化：设置超时时间
        stringRequest.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                5000, // 5秒超时
                1, // 重试1次
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(stringRequest);
    }

    /*弹窗获取成功*/
    public void NoticeResponse(String response) {
        NoticeResponse(response, true);
    }
    
    // 优化：支持静默刷新模式
    public void NoticeResponse(String response, boolean showLoading) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "NoticeResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            if (code == 200){
                String msg = jSONObject.optString("msg");/*状态信息*/
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                JSONObject jSON = null;
                if (miType == 1) {
                    jSON = new JSONObject(Rc4.decry_RC4(msg,RC4KEY));
                } else if (miType == 2) {
                    jSON = new JSONObject(Rsa.decrypt_Rsa(msg,RSAKEY));
                } else if (miType == 3) {
                    jSON = new JSONObject(AES.decrypt_Aes(AESKEY,msg, AESIV));
                }
                String url = jSON.optString("url");/*公告地址*/
                if (url != null && !url.isEmpty()) {
                    // 优化：保存到缓存
                    sp.edit().putString("cached_notice_url", url)
                            .putLong("cached_notice_time", System.currentTimeMillis())
                            .apply();

                    if (showLoading) {
                        // 下载公告图片，下载完成后立即弹出
                        returnBitMap(url);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*开始弹窗*/
    private void notice(){
        if (bitmap != null){
            final ImageView imageView = findViewById(R.id.notice_iamge_url);
            final TextView notice_button = findViewById(R.id.notice_button);//公告按钮id
            if (notice_button != null) {
                notice_button.setVisibility(View.VISIBLE);//布局是否可见 VISIBLE 可见 GONE 不可见
            }
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            View viewById = findViewById(R.id.notice);
                            if (viewById != null) {
                                viewById.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                );
            }
        }
    }

    /*设置弹窗*/
    public Bitmap returnBitMap(final String url){
        // 优化：使用线程池替代直接创建Thread
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                URL imageurl = null;

                try {
                    imageurl = new URL(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    HttpURLConnection conn = (HttpURLConnection)imageurl.openConnection();
                    conn.setRequestProperty("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(HomeActivity.this, "Authorization", ""),Constant.d));
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream is = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                // 图片下载完成后，立即在主线程触发弹窗显示
                HomeActivity.this.mediaHandler.sendEmptyMessage(6);
            }
        });
        return bitmap;
    }

    /**
     * 显示升级提示的对话框
     */
    private void UpdateDialog(String remark, final String updateurl) {
        Builder builder = new Builder(context);
        builder.setTitle(R.string.Upgrade);
        String[] remarks = remark.split(";");
        String str = "";
        for (int i = 0; i < remarks.length; i++) {
            if (i == remarks.length - 1) {
                str = str + remarks[i];
                continue;
            } else {
                str = str + remarks[i] + "\n";
            }
        }
        //Logger.d(TAG, "msg=" + str);
        builder.setMessage(str);
        if (compel == 1){/*强制升级*/
            builder.setPositiveButton(R.string.Update_Now, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.startDownloadApk(context, updateurl, homeHandler);
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }else {/*可选升级*/
            builder.setPositiveButton(R.string.Update_Now, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.startDownloadApk(context, updateurl, homeHandler);
                    dialog.dismiss();
                }
            });
            builder.setNeutralButton(R.string.Update_later, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.creates().shows();
        }
    }

    /*查询已安装的应用程序*/
    public void queryInstalledApp() {
        // 优化：使用线程池替代直接创建Thread
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                packLst = getPackageManager().getInstalledPackages(0);
            }
        });
    }

    /*更改背景图像*/
    // 优化：将Bitmap处理移到后台线程，避免阻塞UI线程
    private void changeBackImage(String fileName) {
        if (fileName == null)
            return;
        if (!context.getFilesDir().exists()) {
            return;
        }
        
        final String filePath = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        final boolean needBlur = "开".equals(sp.getString("open_blur", "关"));
        final String blurCacheKey = filePath + "_blur";
        
        // 先检查缓存，如果有则直接使用（快速路径）
        Bitmap cachedBmp = needBlur 
            ? mCacheUtils.getBitmapFromMemCache(blurCacheKey)
            : mCacheUtils.getBitmapFromMemCache(filePath);
            
        if (cachedBmp != null) {
            rl_bg.setBackgroundDrawable(new BitmapDrawable(getResources(), cachedBmp));
            return;
        }
        
        // 缓存中没有，在后台线程处理
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = null;
                try {
                    if (needBlur) {
                        // 开启高斯模糊背景效果
                        bmp = mCacheUtils.getBitmapFromMemCache(blurCacheKey);
                        if (bmp == null) {
                            Bitmap tempBmp = mCacheUtils.getBitmapFromMemCache(filePath);
                            if (tempBmp == null) {
                                // 优化：使用采样率加载，减少内存占用
                                tempBmp = decodeSampledBitmapFromFile(filePath, 
                                    rl_bg.getWidth(), rl_bg.getHeight());
                                if (tempBmp != null) {
                                    mCacheUtils.addBitmapToMemoryCache(filePath, tempBmp);
                                }
                            }
                            if (tempBmp != null) {
                                // 模糊处理在后台线程执行
                                bmp = BlurUtils.doBlur(tempBmp, 7, false);
                                if (bmp != null) {
                                    mCacheUtils.addBitmapToMemoryCache(blurCacheKey, bmp);
                                }
                            }
                        }
                    } else {
                        bmp = mCacheUtils.getBitmapFromMemCache(filePath);
                        if (bmp == null) {
                            // 优化：使用采样率加载
                            bmp = decodeSampledBitmapFromFile(filePath, 
                                rl_bg.getWidth(), rl_bg.getHeight());
                            if (bmp != null) {
                                mCacheUtils.addBitmapToMemoryCache(filePath, bmp);
                            }
                        }
                    }
                    
                    // 在主线程更新UI
                    final Bitmap finalBmp = bmp;
                    homeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (finalBmp != null && rl_bg != null) {
                                rl_bg.setBackgroundDrawable(new BitmapDrawable(getResources(), finalBmp));
                            }
                        }
                    });
                } catch (OutOfMemoryError oom) {
                    Log.e("HomeActivity", "OutOfMemoryError in changeBackImage", oom);
                    mCacheUtils.clearAllImageCache();
                    // 重试一次
                    homeHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            changeBackImage(fileName);
                        }
                    });
                } catch (Exception e) {
                    Log.e("HomeActivity", "Error in changeBackImage", e);
                }
            }
        });
    }
    
    /**
     * 优化：使用采样率加载Bitmap，减少内存占用
     */
    private Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
        // 第一次解析只获取图片尺寸
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        
        // 计算采样率
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        
        // 使用采样率重新解析
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565; // 使用更省内存的配置
        return BitmapFactory.decodeFile(filePath, options);
    }
    
    /**
     * 计算采样率
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (reqWidth <= 0) reqWidth = 1920; // 默认宽度
        if (reqHeight <= 0) reqHeight = 1080; // 默认高度
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

    /**
     * 飞框动画
     *
     * @param width
     * @param height
     * @param paramFloat1
     * @param paramFloat2
     */
    public void flyWhiteBorder(int width, int height, float paramFloat1, float paramFloat2) {
        if (whiteBorder == null)
            return;
        int mWidth = whiteBorder.getWidth();
        int mheight = whiteBorder.getHeight();
        ViewPropertyAnimator localViewPropertyAnimator = whiteBorder.animate();
        localViewPropertyAnimator.setDuration(300L);// 400
        localViewPropertyAnimator.scaleX((float) width / (float) mWidth);
        localViewPropertyAnimator.scaleY((float) height / (float) mheight);
        localViewPropertyAnimator.x(paramFloat1);
        localViewPropertyAnimator.y(paramFloat2);
        localViewPropertyAnimator.start();
    }

    /*输入密码对话框*/
    private void showUserDialog() {
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        WiFiDialog.Builder builder = new WiFiDialog.Builder(context);
        View mView = View.inflate(context, R.layout.user_pwd, null);
        TextView Categorypwd = (TextView) mView.findViewById(R.id.Categorypwd);
        Categorypwd.setText(Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Pwd_text", ""),Constant.d));
        final EditText user_pwd_et = (EditText) mView.findViewById(R.id.user_pwd_et);
        builder.setContentView(mView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String pwd = user_pwd_et.getText().toString().trim();
//                if (TextUtils.isEmpty(pwd)) {
//                    Utils.showToast(HomeActivity.this, "请输入密码!", R.drawable.toast_err);
//                    return;
//                }
                if (Md5Encoder.encode(pwd).equals(categorypwd)){
                    PWD = "200";
                    if (En.equals("LIVE")){
                        String username = sp.getString("userName", null);
                        if(TextUtils.isEmpty(username)) {
                            Intent mIntent = new Intent();
                            mIntent.setClass(context, UserActivity.class);
                            startActivity(mIntent);
                        }else{
                            long time = System.currentTimeMillis() / 1000;
                            long vip = Long.parseLong(sp.getString("vip", ""));
                            if (time > vip&&!sp.getString("vip", "").equals("999999999")){
                                Intent mIntent = new Intent();
                                mIntent.setClass(context, EmpowerActivity.class);
                                startActivity(mIntent);
                            }else{
                                if (live == 1){
                                    Intent mIntent = new Intent();
                                    mIntent.setClass(context, LivePlayerActivity.class);
                                    startActivity(mIntent);
                                }else{
                                    Utils.showToast(context, R.string.Not_yet_activated, R.drawable.toast_shut);
                                }
                            }
                        }
                        mDialog.dismiss();
                        return;
                    }
                    Bundle pBundle = new Bundle();
                    pBundle.putString("TYPE", En);
                    pBundle.putString("TYPENAME", Name);
                    openActivity(VodTypeActivity.class, pBundle);
                    mDialog.dismiss();
                }else{
                    PWD = null;
                    Utils.showToast(HomeActivity.this, R.string.network_setting_wireless_network_pager_list_item_link_state_wrong, R.drawable.toast_err);
                }
                /*请求易如意验证密码暂时废弃*/
                //Login(user_pwd_et, User_url + "/api?app=" + Api.APPID + "&act=pwd");
            }
        });
        builder.setNeutralButton(R.string.popup_confirmation_dialog_Negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mDialog = builder.create();
        mDialog.show();
    }

    /*类目密码获取成功-暂时废弃*/
    public void LoginResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "LoginResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            if (code == 200){
                PWD = "200";
                if (En.equals("LIVE")){
                    Intent mIntent = new Intent();
                    mIntent.setClass(context, LivePlayerActivity.class);
                    startActivity(mIntent);
                    mDialog.dismiss();
                    return;
                }
                Bundle pBundle = new Bundle();
                pBundle.putString("TYPE", En);
                pBundle.putString("TYPENAME", Name);
                openActivity(VodTypeActivity.class, pBundle);
                mDialog.dismiss();
            }else{
                PWD = null;
                Utils.showToast(HomeActivity.this, R.string.network_setting_wireless_network_pager_list_item_link_state_wrong, R.drawable.toast_err);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*获取失败*/
    public void RequestError(VolleyError volleyError) {
        //Log.i(TAG, "RequestError: " + volleyError);
        if (volleyError instanceof TimeoutError) {
//            System.out.println("请求超时");
        }
        if (volleyError instanceof AuthFailureError) {
            //System.out.println("身份验证失败错误");
        }
        if(volleyError instanceof NetworkError) {
//            System.out.println("请检查网络");
        }
        if(volleyError instanceof ServerError) {
            //System.out.println("错误404");
        }

    }

    /**
     * 初始化无界UI屏幕适配
     * 设置底部渐变遮罩高度等Activity级别的适配
     */
    private void initInfinityUIAdapter() {
        com.shenma.tvlauncher.utils.ScreenAdapterUtil adapter = 
            com.shenma.tvlauncher.utils.ScreenAdapterUtil.getInstance();
        adapter.init(this);
        
        // 设置底部渐变遮罩高度（占屏幕高度的40%，覆盖底部推荐区域）
        View bottomGradient = findViewById(R.id.bottom_gradient_mask);
        if (bottomGradient != null) {
            int gradientHeight = adapter.getHeightPercent(40);
            android.view.ViewGroup.LayoutParams params = bottomGradient.getLayoutParams();
            params.height = gradientHeight;
            bottomGradient.setLayoutParams(params);
        }
    }

    private class UiID {
        public static final int AUTO_LOGIN_FAIL = 3;
        public static final int AUTO_LOGIN_SUCCESS = 2;
        public static final int RESPONSE_NO_SUCCESS = 1;
        public static final int SHOW_UPDATE_DIALOG = 4;

        private UiID() {
        }
    }

    private class WindowMessageID {
        public static final int ERROR = 4;
        public static final int REFLESH_TIME = 5;
        private static final int DOWNLOAD_ERROR = 16;
        private static final int DOWNLOAD_SUCCESS = 18;
        private static final int GET_INFO_SUCCESS = 19;
        private WindowMessageID() {
        }
    }
}
