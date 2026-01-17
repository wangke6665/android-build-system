package com.shenma.tvlauncher.vod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.Response;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.EmpowerActivity;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.ui.ProgressLayout;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.utils.VideoPlayUtils;
import com.shenma.tvlauncher.view.AlwaysMarqueeTextView;
import com.shenma.tvlauncher.view.HomeDialog;
import com.shenma.tvlauncher.view.PlayerProgressBar;
import com.shenma.tvlauncher.vod.adapter.VodMenuAdapter;
import com.shenma.tvlauncher.vod.dao.VodDao;
import com.shenma.tvlauncher.vod.db.Album;
import com.shenma.tvlauncher.vod.domain.VideoInfo;
import com.shenma.tvlauncher.vod.domain.VodUrl;
import com.shenma.tvlauncher.vod.domain.VodUrlList;
import com.shenma.tvlauncher.vod.domain.VodDataInfo;
import com.shenma.tvlauncher.vod.domain.VideoDetailInfo;
import com.shenma.tvlauncher.vod.domain.AboutInfo;
import com.shenma.tvlauncher.network.GsonRequest;
import com.umeng.analytics.MobclickAgent;
import android.util.Base64;
import com.android.volley.Request.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.example.widget.media.IRenderView;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IjkMediaMeta;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoDetailsActivity extends Activity implements OnClickListener {
    private static final String TAG = "VideoDetailsActivity";
    private static final int TIME = 6000;
    private static final int TOUCH_BRIGHTNESS = 2;
    private static final int TOUCH_NONE = 0;
    private static final int TOUCH_SEEK = 3;
    private static final int TOUCH_VOLUME = 1;
    
    public static int hmblposition = 0;
    public static int jmposition = 0;
    public static int phszposition = 0;
    public static int qxdposition = 0;
    public static int xjposition = 0;
    public static int bsposition = 2;
    public static int ptposition = 0;
    public static int pwposition = 0;
    public static int nhposition = 2;
    // 弹幕设置位置
    public static int dmSwitchPosition = 1; // 弹幕开关：0关闭 1开启
    public static int dmSizePosition = 2;   // 弹幕大小：0极小 1较小 2标准 3较大 4极大
    public static int dmSpeedPosition = 2;  // 弹幕速度：0极慢 1较慢 2标准 3较快 4极快
    public static int dmLinePosition = 2;   // 弹幕行数：0-3行 1-5行 2-8行 3-10行 4-不限
    public static int dmAlphaPosition = 0;  // 弹幕透明：0-100% 1-80% 2-60% 3-40% 4-20%
    private static int dmSubMenuType = 0;   // 弹幕子菜单类型
    private static int controlHeight = 0;
    private static int menutype;
    public static String Failed = "";
    public static long currentPosition;
    public static SharedPreferences Sd;
    public static List<VodUrl> now_source;
    
    private boolean isFullscreen = false;
    private View focusBeforeFullscreen;
    private boolean isNavigating = false; // 防止重复跳转标志
    private Handler autoFullscreenHandler; // 自动全屏Handler
    private Runnable autoFullscreenRunnable; // 自动全屏Runnable
    
    private View detailsScrollView;
    private View contentLayer;
    private FrameLayout videoFrame;
    private LinearLayout episodeArea;
    private ScrollView contentScroll;
    private RelativeLayout rootLayout;
    private TextView tvDetailsName, tvDetailsAreaYear, tvDetailsActors, tvDetailsIntro;
    private TextView tvEpisodeCount, tvEpisodeOrder;
    private LinearLayout episodeContainer, pageContainer, recommendContainer;
    private LinearLayout sourceListContainer, coreListContainer;
    private FrameLayout sourcePanel, corePanel;
    private Button btnFullscreen, btnFavorite, btnSource, btnPlayerCore;
    
    private int currentPageIndex = 0;
    private boolean isEpisodeAscending = true;
    private int currentSourceIndex = 0;
    private int currentCoreIndex = 2;
    private String[] playerCores = {"自动", "系统", "IJK", "EXO", "阿里"};
    private List<VodUrlList> G;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    
    private final Object SYNC_Playing = new Object();
    private float Lightness;
    private String albumPic = null;
    private int collectionTime = 0;
    private View controlView;
    private PopupWindow controler;
    private int currentVolume;
    private VodDao dao;
    private String domain;
    private long firstTime = 0;
    private IjkVideoView iVV = null;
    private ImageButton ib_playStatus;
    private boolean isBack = false;
    private boolean isControllerShow = false;
    private Boolean isDestroy = Boolean.valueOf(false);
    private Boolean isLast = Boolean.valueOf(false);
    private boolean isMenuItemShow = false;
    private boolean isMenuShow = false;
    private Boolean isNext = Boolean.valueOf(false);
    private Boolean isPause = Boolean.valueOf(false);
    private boolean isSwitch = true;
    private String jump_time;
    private String jump_time_end;
    private long firstTimes = 0;
    private long firstTimen = 0;
    
    private AudioManager mAudioManager = null;
    private GestureDetector mGestureDetector = null;
    private HandlerThread mHandlerThread;
    private boolean mIsHwDecode = true;
    private int mLastPos = 0;
    private int mLastPos2 = 0;
    private int vipstate;
    private String time;
    private int Trytime;
    private PLAYER_STATUS mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
    private ProgressBar mProgressBar;
    private Handler mSpeedHandler;
    private int mSurfaceYDisplayRange;
    private Toast mToast = null;
    private int mTouchAction;
    private float mTouchX;
    private float mTouchY;
    private String mVideoSource = null;
    private WakeLock mWakeLock = null;
    private int maxVolume;
    private ListView menulist;
    private PopupWindow menupopupWindow;
    private String nextlink;
    private int playIndex = 0;
    private int playPreCode;
    private int screenHeight;
    private int screenWidth;
    private SeekBar seekBar;
    private String sourceId;
    private EventHandler mEventHandler;
    private SharedPreferences sp;
    
    private View time_controlView;
    private PopupWindow time_controler;
    private TextView tv_currentTime;
    private TextView tv_menu;
    private TextView tv_mv_name;
    private TextView tv_progress_time;
    private TextView tv_time;
    private TextView tv_totalTime;
    private TextView tv_resolution;
    
    private ProgressLayout progressLayout;
    private AlwaysMarqueeTextView tv_notice;
    private LinearLayout tv_notice_root;
    private String url;
    private String videoId;
    private int videoLength;
    private VodMenuAdapter vmAdapter;
    private String vodname;
    private String vodstate;
    private String vodtype = null;
    private String top_type;
    private String Api_url;
    private String BASE_HOST;
    
    private RequestQueue mQueue;
    private String Fburl;
    private ImageView tv_logo;
    private List<VideoInfo> videoInfo = null;
    private int ClientID = 1;
    private int MaxClientID = 1;
    private int Type = 0;
    private String logo_url;
    private String Client;
    private int Auto_Source;
    private String jxurl;
    private int jxurls = 0;
    private Map<String, String> headers;
    private HashMap<String, String> webHeaders;
    private String Conditions;
    private String userAgent;
    private String Referer;
    private int Sniff_debug_mode;
    private int Play_timeout_debug;
    private int Navigation_mode = 0;
    private int Vod_Notice_starting_time;
    private int Number;
    private List<VodUrlList> list;
    private int PlayersNumber;
    private int Numbermax = 0;
    private int load = 0;
    private int Maxtimeout = 300;
    private int timeout = Maxtimeout;
    private WebView webView;
    private String Exclude_content;
    private List<VodUrl> source;
    private ArrayList<VodDataInfo> aboutlist;
    private boolean isDialogShowing = false;
    private boolean isCodeBlockExecuted = false;
    private boolean isParsing = false; // 标记是否正在解析中
    private boolean ptpositions = false;
    
    private int seizing;
    private int seizings;
    private long currentPositions;
    private Runnable speedRunnabless;
    private Runnable autoSaveProgressRunnable;
    
    private long lastRxByte = 0;
    private long lastSpeedTime = 0;
    private long speed = 0;
    private String rxByte = "";
    private Runnable speedRunnable;
    
    private int core = 99;
    private int safe_mode = 0;
    private int Ad_block = 0;
    private int position = 0;
    private int core_mode;
    private int Adblock;
    
    private LinearLayout Ad_block_up;
    private LinearLayout Ad_block_down;
    private LinearLayout Ad_block_right;
    
    private int EwmWidth = 0;
    private int EwmHeight = 0;
    private int Moviesize = 120;
    private int Tvplaysize = 70;
    private int headposition = 0;
    
    private int vod_caton_check;
    private int seizing_time;
    
    private List<Album> albums;
    private Album album;
    
    private boolean isPlaying = false;
    private int currentEpisodeIndex = 0;
    
    // 弹幕相关
    private master.flame.danmaku.ui.widget.DanmakuView danmakuView;
    private com.shenma.tvlauncher.danmaku.DanmakuManager danmakuManager;
    private com.shenma.tvlauncher.danmaku.DanmakuConfig danmakuConfig;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onMessage(msg);
        }
    };
    
    private final Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WindowMessageID.NET_FAILED:
                    Logger.d(TAG, "mPlayerStatus=" + mPlayerStatus);
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        iVV.stopPlayback();
                    }
                    finish();
                    return;
                case WindowMessageID.DATA_PREPARE_OK:
                    onCreateMenu();
                    return;
                case WindowMessageID.DATA_BASE64_PREPARE_OK:
                    VodUrlList vodurl = (VodUrlList) msg.obj;
                    mVideoSource = vodurl.getUrl();
                    mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
                    if(mEventHandler.hasMessages(WindowMessageID.EVENT_PLAY)){
                        mEventHandler.removeMessages(WindowMessageID.EVENT_PLAY);
                    }
                    mEventHandler.sendEmptyMessage(WindowMessageID.EVENT_PLAY);
                    return;
                case WindowMessageID.PREPARE_VOD_DATA:
                    PrepareVodData(playIndex);
                    return;
                case WindowMessageID.SHOW_TV:
                    // 先显示进度条，避免阻塞UI
                    if (mSpeedHandler != null) {
                        mSpeedHandler.removeCallbacks(speedRunnabless);
                    }
                    if (progressLayout != null) progressLayout.showProgress();
                    if (speedRunnable != null && mSpeedHandler != null) {
                        mSpeedHandler.post(speedRunnable);
                    }
                    ptpositions = false;
                    
                    // 延迟暂停弹幕渲染
                    if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (danmakuManager != null) {
                                    pauseDanmaku();
                                }
                            }
                        }, 50); // 延迟50ms，让进度条动画先执行
                    }
                    return;
                case WindowMessageID.COLSE_SHOW_TV:
                    if (progressLayout != null) progressLayout.showContent();
                    if (speedRunnable != null && mSpeedHandler != null) {
                        mSpeedHandler.removeCallbacks(speedRunnable);
                    }
                    
                    if (progressLayout != null) {
                        progressLayout.hideSpeedInfo();
                    }
                    
                    // 延迟恢复弹幕渲染
                    if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (danmakuManager != null) {
                                    resumeDanmaku();
                                }
                            }
                        }, 100); // 延迟100ms，让进度条关闭动画完成
                    }
                    return;
                case WindowMessageID.PROGRESSBAR_PROGRESS_RESET:
                    if (mProgressBar != null) mProgressBar.setProgress(0);
                    return;
                case WindowMessageID.SELECT_SCALES:
                    selectScales(hmblposition);
                    return;
                case WindowMessageID.RESET_MOVIE_TIME:
                    ResetMovieTime();
                    return;
                case WindowMessageID.Try:
                    startActivity(new Intent(VideoDetailsActivity.this, EmpowerActivity.class));
                    finish();
                    Utils.showToast(VideoDetailsActivity.this, getString(R.string.Try_to_see_expired) + Trytime + getString(R.string.end), R.drawable.toast_err);
                    return;
                case WindowMessageID.NOTICE:
                    if (tv_notice != null) {
                        tv_notice.setSelected(true);
                        tv_notice.setMarqueeRepeatLimit(-1);
                        tv_notice.startScroll();
                    }
                    int Vod_Notice_end_time = SharePreferenceDataUtil.getSharedIntData(VideoDetailsActivity.this, Constant.Fi, 0);
                    mediaHandler.sendEmptyMessageDelayed(WindowMessageID.NOTICE_GONE, Vod_Notice_end_time * 1000);
                    return;
                case WindowMessageID.NOTICE_GONE:
                    if (tv_notice_root != null) tv_notice_root.setVisibility(View.GONE);
                    return;
                case WindowMessageID.START_NOTICE_GONE:
                    getVodGongGao();
                    return;
                case WindowMessageID.START_LOGO:
                    if (logo_url != null && !logo_url.equals("null") && !logo_url.equals("")){
                        vodlogoloadImg();
                        break;
                    }
                    if (tv_logo != null) {
                        tv_logo.setImageDrawable(getResources().getDrawable(R.drawable.sm_logo));
                        tv_logo.setVisibility(View.VISIBLE);
                    }
                    return;
                case WindowMessageID.Sniffing:
                    if (webView != null) {
                        webView.stopLoading();
                        webView.setWebViewClient(null);
                    }
                    jxurls = 0;
                    // 先取消之前的超时检测
                    mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
                    if (Failed.equals("")){
                        iVV.setVideoPath(Utils.UrlEncodeChinese(jxurl), webHeaders);
                    }else{
                        iVV.setVideoPath(Utils.UrlEncodeChinese(Failed), webHeaders);
                    }
                    // 启动10秒准备超时检测
                    mHandler.sendEmptyMessageDelayed(WindowMessageID.PREPARE_TIMEOUT, 10000);
                    Fburl = Utils.UrlEncodeChinese(jxurl);
                    if (!jxurl.equals("")){
                        mEventHandler.sendEmptyMessage(WindowMessageID.SUCCESS);
                    }
                    return;
                default:
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details_new3);
        
        // 初始化标志
        isParsing = false;
        isCodeBlockExecuted = false;
        
        // 清除所有HTTP缓存，确保每次进入都重新解析
        clearAllHttpCache();
        
        initConfig();
        dao = new VodDao(this);
        Utils.stopAutoBrightness(this);
        
        if (Navigation_mode == 1) {
            Navigation();
        }
        
        Sd = getSharedPreferences("TempData", MODE_PRIVATE);
        SharePreferenceDataUtil.setSharedIntData(this, "LIVE", 0);
        
        initView();
        initData();
        setupListeners();
        
        // 初始化自动全屏Handler和Runnable
        autoFullscreenHandler = new Handler();
        autoFullscreenRunnable = new Runnable() {
            @Override
            public void run() {
                // 3秒后自动进入全屏（只在非全屏状态下）
                if (!isFullscreen && (isPlaying || (iVV != null && iVV.isPlaying()))) {
                    Log.d(TAG, "小窗播放3秒后自动进入全屏");
                    try {
                        enterFullscreen();
                    } catch (Exception e) {
                        Log.e(TAG, "自动全屏失败: " + e.getMessage());
                    }
                }
            }
        };
    }
    
    private void initConfig() {
        logo_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yv, null), Constant.d);
        Client = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.Nr, ""), Constant.d);
        Auto_Source = SharePreferenceDataUtil.getSharedIntData(this, Constant.Jv, 0);
        Sniff_debug_mode = SharePreferenceDataUtil.getSharedIntData(this, Constant.mr, 0);
        Play_timeout_debug = SharePreferenceDataUtil.getSharedIntData(this, Constant.Fg, 0);
        Vod_Notice_starting_time = SharePreferenceDataUtil.getSharedIntData(this, Constant.lf, 0);
        core_mode = SharePreferenceDataUtil.getSharedIntData(this, Constant.ldb, 0);
        Adblock = SharePreferenceDataUtil.getSharedIntData(this, Constant.ykfa, 0);
        vod_caton_check = SharePreferenceDataUtil.getSharedIntData(this, Constant.guda, 0);
        seizing_time = SharePreferenceDataUtil.getSharedIntData(this, Constant.oftn, 8);
    }
    
    /**
     * 清除所有HTTP缓存，确保每次进入都像第一次一样重新解析
     */
    private void clearAllHttpCache() {
        try {
            // 1. 清除HttpURLConnection的响应缓存
            try {
                java.net.ResponseCache.setDefault(null);
            } catch (Exception e) {
            }
            
            // 2. 清除WebView缓存（如果使用了WebView）
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CookieManager.getInstance().removeAllCookies(null);
                    CookieManager.getInstance().flush();
                } else {
                    CookieSyncManager.createInstance(this);
                    CookieManager.getInstance().removeAllCookie();
                    CookieSyncManager.getInstance().sync();
                }
            } catch (Exception e) {
            }
            
            // 3. 清除应用缓存目录
            try {
                deleteDir(getCacheDir());
            } catch (Exception e) {
            }
            
            // 4. 如果有旧的RequestQueue，先取消所有请求
            if (mQueue != null) {
                mQueue.cancelAll(this);
                mQueue.stop();
                mQueue = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 递归删除目录
     */
    private boolean deleteDir(java.io.File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new java.io.File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        }
        return false;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = Boolean.valueOf(true);
        stopPlayback();
        
        // 清理自动全屏Handler
        if (autoFullscreenHandler != null && autoFullscreenRunnable != null) {
            autoFullscreenHandler.removeCallbacks(autoFullscreenRunnable);
            autoFullscreenHandler = null;
            autoFullscreenRunnable = null;
        }
        
        if (mSpeedHandler != null) {
            mSpeedHandler.removeCallbacks(speedRunnabless);
            mSpeedHandler = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mediaHandler != null) {
            mediaHandler.removeCallbacksAndMessages(null);
        }
        if (mQueue != null) {
            mQueue.cancelAll(this);
            mQueue = null;
        }
        // 释放弹幕资源
        releaseDanmaku();
        
        // 重置所有标志
        isParsing = false;
        isCodeBlockExecuted = false;
        currentPosition = 0;
        Utils.startAutoBrightness(this);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        isDestroy = Boolean.valueOf(true);
        
        // 保存播放进度
        savePlayProgress(false);
        
        mHandler.removeMessages(WindowMessageID.SWITCH_CODE);
        mHandler.removeMessages(WindowMessageID.HIDE_CONTROLER);
        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        xjposition = 0;
        if (mHandlerThread != null) {
            mHandlerThread.quit();
        }
    }
    
    @Override
    protected void onResume() {
        hideMenu();
        xjposition = playIndex;
        super.onResume();
        // 重置跳转标志，允许用户再次点击
        isNavigating = false;
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
        Map m_value = new HashMap();
        m_value.put("vodname", vodname);
        m_value.put("domain", domain);
        m_value.put("vodtype", vodtype);
        MobclickAgent.onEvent(this, "VOD_PLAY", m_value);
        isDestroy = Boolean.valueOf(false);
        acquireWakeLock();
        
        if (mHandlerThread != null && !mHandlerThread.isAlive()) {
            mHandlerThread = new HandlerThread("event handler thread", Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();
            mEventHandler = new EventHandler(mHandlerThread.getLooper());
        }
        
        if (null != mVideoSource && !"".equals(mVideoSource) && mPlayerStatus == PLAYER_STATUS.PLAYER_IDLE) {
            mEventHandler.sendEmptyMessage(WindowMessageID.EVENT_PLAY);
        }
        
        initSpeedHandlers();
        
        if (mPlayerStatus == PLAYER_STATUS.PLAYER_BACKSTAGE) {
            if (iVV != null && !iVV.isPlaying()) {
                iVV.start();
                mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                if (ib_playStatus != null) ib_playStatus.setImageResource(R.drawable.media_pause);
            } else {
                mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
                if (iVV != null) iVV.start();
                mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                if (ib_playStatus != null) ib_playStatus.setImageResource(R.drawable.media_pause);
                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
            }
            // 恢复弹幕
            resumeDanmaku();
        } else {
            if (mProgressBar != null) mProgressBar.setProgress(0);
            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        }
        
        // 恢复分辨率显示（无论从哪里返回）
        updateResolutionDisplay();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        isDestroy = Boolean.valueOf(true);
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
        releaseWakeLock();
        
        // 保存播放进度（防止应用被切换到后台时丢失进度）
        savePlayProgress(false);
        
        if (iVV != null && iVV.isPlaying()) {
            iVV.pause();
            mLastPos = iVV.getCurrentPosition();
            mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
            mPlayerStatus = PLAYER_STATUS.PLAYER_BACKSTAGE;
            if (ib_playStatus != null) ib_playStatus.setImageResource(R.drawable.media_playstatus);
        }
        
        // 暂停弹幕
        pauseDanmaku();
        
        hideController();
        mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
    }
    
    @Override
    public void onBackPressed() {
        // 先关闭面板
        if (sourcePanel != null && sourcePanel.getVisibility() == View.VISIBLE) {
            sourcePanel.setVisibility(View.GONE);
            return;
        }
        if (corePanel != null && corePanel.getVisibility() == View.VISIBLE) {
            corePanel.setVisibility(View.GONE);
            return;
        }
        
        if (isFullscreen) {
            exitFullscreen();
            return;
        }
        
        long secondTime = System.currentTimeMillis();
        if (secondTime - firstTime <= 2000) {
            savePlayProgress();
            super.onBackPressed();
        } else {
            Utils.showToast(this, R.string.onbackpressed, R.drawable.toast_err);
            firstTime = secondTime;
        }
    }
    
    private void savePlayProgress() {
        savePlayProgress(true);
    }
    
    private void savePlayProgress(boolean stopPlayer) {
        
        if (iVV == null) {
            Log.e(TAG, "保存失败: iVV为null");
            if (stopPlayer) {
                isDestroy = Boolean.valueOf(true);
                stopPlayback();
            }
            return;
        }
        
        if (videoId == null) {
            Log.e(TAG, "保存失败: videoId为null");
            if (stopPlayer) {
                isDestroy = Boolean.valueOf(true);
                stopPlayback();
            }
            return;
        }
        
        try {
            collectionTime = iVV.getCurrentPosition();
            
            // 只有播放位置大于5秒才保存（避免保存无意义的进度）
            if (collectionTime > 5000) {
                long secondTime = System.currentTimeMillis();
                Album album = new Album();
                album.setAlbumId(videoId);
                album.setAlbumSourceType(sourceId);
                album.setCollectionTime(collectionTime);
                album.setPlayIndex(playIndex);
                album.setAlbumPic(albumPic);
                album.setAlbumType(vodtype);
                album.setAlbumTitle(vodname);
                album.setAlbumState("观看:" + collectionTime / 1000 / 60 + "分钟");
                album.setNextLink(nextlink);
                album.setTime(String.valueOf(secondTime));
                album.setTypeId(2);
                
                
                dao.addAlbums(album);
            } else {
            }
        } catch (Exception e) {
            Log.e(TAG, "保存播放进度异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        if (stopPlayer) {
            isDestroy = Boolean.valueOf(true);
            stopPlayback();
        }
    }

    private void initSpeedHandlers() {
        mSpeedHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == WindowMessageID.START_SPEED) {
                    if (progressLayout != null) {
                        progressLayout.updateSpeedInfo(rxByte);
                    }
                }
            }
        };
        
        speedRunnabless = new Runnable() {
            private long lastPosition = -1;
            public void run() {
                if (iVV == null) return;
                currentPositions = iVV.getCurrentPosition();
                if (currentPositions == lastPosition) {
                    currentPosition = currentPositions;
                    if (!iVV.isPlaying()) {
                        seizing = 0;
                    } else {
                        seizing = 1;
                        if (Number > 1) {
                            seizings = 1;
                            if (Play_timeout_debug == 1) {
                                Utils.showToast(VideoDetailsActivity.this, getString(R.string.seizing), R.drawable.toast_shut);
                            }
                            if (PlayersNumber >= Number) {
                                PlayersNumber = 0;
                                if (Play_timeout_debug == 1) {
                                    Utils.showToast(VideoDetailsActivity.this, getString(R.string.no_source), R.drawable.toast_shut);
                                }
                            } else {
                                mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                                Switchsource(4);
                            }
                        } else {
                            if (Play_timeout_debug == 1) {
                                Utils.showToast(VideoDetailsActivity.this, getString(R.string.single), R.drawable.toast_shut);
                            }
                        }
                    }
                } else {
                    lastPosition = currentPositions;
                    currentPosition = 0;
                }
                if (seizing != 1 && mSpeedHandler != null) {
                    mSpeedHandler.postDelayed(speedRunnabless, seizing_time * 1000);
                }
            }
        };
        
        speedRunnable = new Runnable() {
            public void run() {
                long nowtime = System.currentTimeMillis();
                long nowRxbyte = TrafficStats.getTotalRxBytes();
                
                if (lastRxByte == 0 || lastSpeedTime == 0) {
                    lastRxByte = nowRxbyte;
                    lastSpeedTime = nowtime;
                    rxByte = "0KB/S";
                    if (mSpeedHandler != null) mSpeedHandler.sendEmptyMessage(WindowMessageID.START_SPEED);
                } else {
                    long rxbyte = nowRxbyte - lastRxByte;
                    long timeDiff = nowtime - lastSpeedTime;
                    if (timeDiff > 0) {
                        speed = ((rxbyte / timeDiff) * 1000) / 1024;
                        if (speed >= 1024) {
                            rxByte = String.valueOf(speed / 1024) + "MB/S";
                        } else {
                            rxByte = String.valueOf(speed) + "KB/S";
                        }
                        if (mSpeedHandler != null) mSpeedHandler.sendEmptyMessage(WindowMessageID.START_SPEED);
                    }
                    lastRxByte = nowRxbyte;
                    lastSpeedTime = nowtime;
                }
                if (mSpeedHandler != null) mSpeedHandler.postDelayed(speedRunnable, 500);
            }
        };
    }
    
    private void initView() {
        sp = getSharedPreferences("shenma", 0);
        time = sp.getString("vip", "");
        Trytime = SharePreferenceDataUtil.getSharedIntData(this, Constant.gD, 0);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        String playratio = sp.getString(Constant.oh, "全屏拉伸");
        if ("原始比例".equals(playratio)) hmblposition = 0;
        else if ("4:3 缩放".equals(playratio)) hmblposition = 1;
        else if ("16:9缩放".equals(playratio)) hmblposition = 2;
        else if ("全屏拉伸".equals(playratio)) hmblposition = 3;
        else if ("等比缩放".equals(playratio)) hmblposition = 4;
        else if ("全屏裁剪".equals(playratio)) hmblposition = 5;
        
        String playcore = sp.getString(Constant.hd, "IJK");
        if ("自动".equals(playcore)) nhposition = 0;
        else if ("系统".equals(playcore)) nhposition = 1;
        else if ("IJK".equals(playcore)) nhposition = 2;
        else if ("EXO".equals(playcore)) nhposition = 3;
        else if ("阿里".equals(playcore)) nhposition = 4;
        
        jump_time = Utils.stringDrawNum(sp.getString("play_jump", "0"));
        jump_time_end = Utils.stringDrawNum(sp.getString("play_jump_end", "0"));
        getJumpdata();
        setDecode();
        getPlayPreferences();
        getScreenSize();
        loadViewLayout();
        findViews();
        setPlayerListener();
        setupGestureDetector();
        
        mHandlerThread = new HandlerThread("event handler thread", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());
    }
    
    private void findViews() {
        
        // 详情页UI
        rootLayout = findViewById(R.id.root_layout);
        contentScroll = findViewById(R.id.content_scroll);
        contentLayer = findViewById(R.id.content_layer);
        detailsScrollView = findViewById(R.id.details_scroll_view);
        videoFrame = findViewById(R.id.video_frame);
        episodeArea = findViewById(R.id.episode_area);
        
        // 小屏幕点击/按键进入全屏（适配TV遥控器和手机/平板触摸）
        if (videoFrame != null) {
            videoFrame.setFocusable(true);
            videoFrame.setFocusableInTouchMode(true);
            videoFrame.setClickable(true);
            videoFrame.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            
            // 禁用系统默认焦点高亮
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                videoFrame.setDefaultFocusHighlightEnabled(false);
            }
            
            // 焦点变化时显示醒目边框（使用前景，不影响视频大小）
            videoFrame.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.d(TAG, "videoFrame onFocusChange: hasFocus=" + hasFocus + ", isFullscreen=" + isFullscreen);
                    if (hasFocus && !isFullscreen) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            v.setForeground(getResources().getDrawable(R.drawable.video_frame_border));
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            v.setForeground(null);
                        }
                    }
                }
            });
            
            // 小窗口模式点击进入全屏
            videoFrame.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isFullscreen) {
                        enterFullscreen();
                    }
                }
            });
            
            // 遥控器按键监听
            videoFrame.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    Log.d(TAG, "videoFrame onKey: keyCode=" + keyCode + ", action=" + event.getAction());
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || 
                            keyCode == KeyEvent.KEYCODE_ENTER ||
                            keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                            if (!isFullscreen) {
                                Log.d(TAG, "videoFrame: entering fullscreen");
                                enterFullscreen();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
        }
        
        tvDetailsName = findViewById(R.id.tv_details_name);
        tvDetailsAreaYear = findViewById(R.id.tv_details_area_year);
        tvDetailsActors = findViewById(R.id.tv_details_actors);
        tvDetailsIntro = findViewById(R.id.tv_details_intro);
        tvEpisodeCount = findViewById(R.id.tv_episode_count);
        tvEpisodeOrder = findViewById(R.id.tv_episode_order);
        
        episodeContainer = findViewById(R.id.episode_container);
        pageContainer = findViewById(R.id.page_container);
        recommendContainer = findViewById(R.id.recommend_container);
        sourceListContainer = findViewById(R.id.source_list_container);
        coreListContainer = findViewById(R.id.core_list_container);
        sourcePanel = findViewById(R.id.source_panel);
        corePanel = findViewById(R.id.core_panel);
        
        // 点击面板背景关闭
        if (sourcePanel != null) {
            sourcePanel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sourcePanel.setVisibility(View.GONE);
                }
            });
        }
        if (corePanel != null) {
            corePanel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    corePanel.setVisibility(View.GONE);
                }
            });
        }
        
        btnFullscreen = findViewById(R.id.btn_fullscreen);
        btnFavorite = findViewById(R.id.btn_favorite);
        btnSource = findViewById(R.id.btn_source);
        btnPlayerCore = findViewById(R.id.btn_player_core);
        
        // 设置按钮图标大小
        int iconSize = getResources().getDimensionPixelSize(R.dimen.sm_28);
        setButtonDrawableSize(btnFullscreen, R.drawable.quanpin, iconSize);
        setButtonDrawableSize(btnFavorite, R.drawable.ic_control_keep_ng, iconSize);
        setButtonDrawableRight(btnSource, R.drawable.huanyuan, iconSize);
        setButtonDrawableRight(btnPlayerCore, R.drawable.huanbofangqi, iconSize);
        
        // 播放器UI
        Ad_block_up = findViewById(R.id.Ad_block_up);
        Ad_block_down = findViewById(R.id.Ad_block_down);
        Ad_block_right = findViewById(R.id.Ad_block_right);
        
        // 弹幕视图
        danmakuView = findViewById(R.id.danmaku_view);
        if (danmakuView != null) {
            // 渲染模式由DanmakuManager根据Android版本自动选择（Android 5.0+使用硬件加速，低版本使用软件渲染）
            // 这里不再强制设置，避免覆盖DanmakuManager的优化设置
            
            // 禁止弹幕视图获取焦点和拦截按键事件
            danmakuView.setFocusable(false);
            danmakuView.setFocusableInTouchMode(false);
            danmakuView.setClickable(false);
            
            // 弹幕视图将触摸事件转发给Activity处理
            danmakuView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return VideoDetailsActivity.this.onTouchEvent(event);
                }
            });
        }
        initDanmaku();
        
        webView = findViewById(R.id.webview);
        tv_logo = findViewById(R.id.tv_logo);
        tv_notice_root = findViewById(R.id.tv_notice_root);
        tv_notice = findViewById(R.id.tv_notice);
        mProgressBar = findViewById(R.id.progressBar);
        tv_progress_time = findViewById(R.id.tv_progress_time);
        progressLayout = findViewById(R.id.progress_layout);
        if (progressLayout != null) {
            progressLayout.setLoadingGif(R.drawable.loading_animation);
        }
        
        if (progressLayout != null && progressLayout.getParent() instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup) progressLayout.getParent();
            // 立即执行一次
            parent.bringChildToFront(progressLayout);
            // 确保时间进度视图在进度条之后（不会遮挡）
            if (tv_progress_time != null && tv_progress_time.getParent() == parent) {
                // 先移除时间进度，再添加到进度条之后
                parent.removeView(tv_progress_time);
                parent.addView(tv_progress_time);
            }
            // 使用 post 确保在布局完成后执行
            progressLayout.post(new Runnable() {
                @Override
                public void run() {
                    parent.bringChildToFront(progressLayout);
                    progressLayout.bringToFront();
                    // 确保时间进度不会遮挡进度条
                    if (tv_progress_time != null && tv_progress_time.getParent() == parent) {
                        // 将时间进度移到进度条之后
                        int progressIndex = parent.indexOfChild(progressLayout);
                        int timeIndex = parent.indexOfChild(tv_progress_time);
                        if (timeIndex >= 0 && progressIndex >= 0 && timeIndex < progressIndex) {
                            parent.removeViewAt(timeIndex);
                            parent.addView(tv_progress_time, progressIndex + 1);
                        }
                    }
                    // 强制刷新视图层级
                    parent.invalidate();
                    progressLayout.invalidate();
                }
            });
        }
        tv_mv_name = findViewById(R.id.tv_mv_name);
        tv_resolution = findViewById(R.id.tv_resolution);
        
        if (controlView != null) {
            seekBar = controlView.findViewById(R.id.seekbar);
            tv_currentTime = controlView.findViewById(R.id.tv_currentTime);
            tv_totalTime = controlView.findViewById(R.id.tv_totalTime);
            tv_menu = controlView.findViewById(R.id.tv_menu);
            ib_playStatus = controlView.findViewById(R.id.ib_playStatus);
            if (ib_playStatus != null) ib_playStatus.setOnClickListener(this);
        }
        
        if (time_controlView != null) {
            tv_time = time_controlView.findViewById(R.id.tv_time);
        }
        
        if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
        
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        
        iVV = findViewById(R.id.i_video_view);
        
        if (iVV != null) {
            // 禁止播放器视图获取焦点，确保焦点在 videoFrame 上
            iVV.setFocusable(false);
            iVV.setFocusableInTouchMode(false);
            iVV.setClickable(false);
            
            // 先停止并释放之前的播放器实例（如果存在）
            try {
                iVV.stopPlayback();
                iVV.release(true);
            } catch (Exception e) {
                Log.w(TAG, "清理旧播放器实例时出错: " + e.getMessage());
                e.printStackTrace();
            }
            
            TableLayout hubview = findViewById(R.id.hubview);
            if (hubview != null) {
                iVV.setHudView(hubview);
            }
            
            int Decode = sp.getInt("mIsHwDecode", 1);
            if (Decode == 1) {
                iVV.setDecode(Boolean.valueOf(true));
            } else {
                iVV.setDecode(Boolean.valueOf(false));
            }
        } else {
            Log.e(TAG, "错误: 无法找到IjkVideoView!");
        }
        
        // 读取内核设置并更新按钮
        currentCoreIndex = nhposition;
        if (btnPlayerCore != null && currentCoreIndex < playerCores.length) {
            btnPlayerCore.setText(playerCores[currentCoreIndex]);
        }
        
    }
    
    private void loadViewLayout() {
        controlView = getLayoutInflater().inflate(R.layout.mv_media_controler, null);
        controler = new PopupWindow(controlView);
        // 确保PopupWindow在Android 4上正确显示，避免进度条被覆盖
        controler.setFocusable(false);
        controler.setOutsideTouchable(false);
        controler.setTouchable(true);
        controler.setClippingEnabled(false);
        
        time_controlView = getLayoutInflater().inflate(R.layout.mv_media_time_controler, null);
        time_controler = new PopupWindow(time_controlView);
        time_controler.setFocusable(false);
        time_controler.setOutsideTouchable(false);
        time_controler.setTouchable(true);
        time_controler.setClippingEnabled(false);
    }
    
    private void initData() {
        Intent intent = getIntent();
        vodtype = intent.getStringExtra("vodtype");
        vodstate = intent.getStringExtra("vodstate");
        nextlink = intent.getStringExtra("nextlink");
        videoId = intent.getStringExtra("videoId");
        vodname = intent.getStringExtra("vodname");
        albumPic = intent.getStringExtra("albumPic");
        domain = intent.getStringExtra("domain");
        sourceId = intent.getStringExtra("sourceId");
        playIndex = intent.getIntExtra("playIndex", 0);
        mLastPos = intent.getIntExtra("collectionTime", 0);
        
        
        // 初始化图片加载
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.hao260x366)
                .showImageForEmptyUri(R.drawable.hao260x366)
                .showImageOnFail(R.drawable.hao260x366)
                .resetViewBeforeLoading(true).cacheInMemory(true)
                .cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
                .build();
        imageLoader = ImageLoader.getInstance();
        
        // 设置详情信息
        if (tvDetailsName != null) tvDetailsName.setText(vodname);
        
        // 加载播放源数据
        loadSourceData();
    }
    
    private void loadSourceData() {
        // 从服务器请求数据
        if (nextlink == null || nextlink.isEmpty()) {
            Utils.showToast(this, R.string.No_data_source, R.drawable.toast_err);
            return;
        }
        
        // 初始化API地址
        Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.bs, ""), Constant.d);
        BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.fn, ""), Constant.d);
        
        Utils.loadingShow_tv(this, R.string.str_data_loading);
        String requestUrl = Api_url + "/api.php/" + BASE_HOST + "/vod/" + nextlink;
        
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        GsonRequest<VideoDetailInfo> request = new GsonRequest<VideoDetailInfo>(
                Method.POST, requestUrl, VideoDetailInfo.class,
                new com.android.volley.Response.Listener<VideoDetailInfo>() {
                    @Override
                    public void onResponse(VideoDetailInfo response) {
                        Utils.loadingClose_Tv();
                        if (response != null) {
                            onDataLoaded(response);
                        } else {
                            Utils.showToast(VideoDetailsActivity.this, R.string.No_data_source, R.drawable.toast_err);
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.loadingClose_Tv();
                        Utils.showToast(VideoDetailsActivity.this, R.string.No_data_source, R.drawable.toast_err);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                try {
                    params.put("data", AES.encrypt_Aes(Md5Encoder.encode(Constant.c), Md5Encoder.encode(Constant.d), Constant.c));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                params.put("sign", Base64.encodeToString(Utils.strRot13(Constant.c).getBytes(), Base64.DEFAULT));
                params.put("time", GetTimeStamp.timeStamp());
                params.put("key", Rc4.encry_RC4_string(GetTimeStamp.timeStamp(), GetTimeStamp.timeStamp()));
                params.put("os", Integer.toString(android.os.Build.VERSION.SDK_INT));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VideoDetailsActivity.this, "Authorization", ""), Constant.d));
                return headers;
            }
        };
        mQueue.add(request);
    }
    
    private void onDataLoaded(VideoDetailInfo response) {
        List<VodUrl> video_list = response.getVideo_list();
        now_source = video_list;
        source = video_list;
        
        if (now_source == null || now_source.size() == 0) {
            Utils.showToast(this, R.string.No_data_source, R.drawable.toast_err);
            return;
        }
        
        Number = now_source.size();
        vodname = response.getTitle();
        videoId = response.getId();
        albumPic = response.getImg_url();
        top_type = response.getTop_type();
        
        // 更新标题
        if (tvDetailsName != null) tvDetailsName.setText(vodname);
        
        // 更新详情信息
        if (tvDetailsAreaYear != null) {
            StringBuilder sb = new StringBuilder();
            String[] areas = response.getArea();
            if (areas != null && areas.length > 0) {
                sb.append(areas[0]);
            }
            String pubtime = response.getPubtime();
            if (pubtime != null && !pubtime.isEmpty()) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(pubtime);
            }
            tvDetailsAreaYear.setText(sb.toString());
        }
        
        if (tvDetailsActors != null) {
            String[] actors = response.getActor();
            if (actors != null && actors.length > 0) {
                StringBuilder sb = new StringBuilder("演员: ");
                for (int i = 0; i < Math.min(actors.length, 5); i++) {
                    if (i > 0) sb.append(" / ");
                    sb.append(actors[i]);
                }
                tvDetailsActors.setText(sb.toString());
            }
        }
        
        if (tvDetailsIntro != null) {
            String intro = response.getIntro();
            if (intro != null && !intro.isEmpty()) {
                tvDetailsIntro.setText(intro);
            }
        }
        
        // 查询播放记录
        albums = dao.queryAlbumById(videoId, 2);
        
        if (albums != null && albums.size() > 0) {
            album = albums.get(0);
            currentEpisodeIndex = album.getPlayIndex();
            playIndex = currentEpisodeIndex;
            collectionTime = album.getCollectionTime();
            mLastPos = collectionTime;
            
            
            String sourceType = album.getAlbumSourceType();
            
            if (sourceType != null && !sourceType.isEmpty()) {
                try {
                    currentSourceIndex = Integer.parseInt(sourceType) - 1;
                    if (currentSourceIndex < 0) currentSourceIndex = 0;
                } catch (NumberFormatException e) {
                    currentSourceIndex = 0;
                    Log.e(TAG, "  播放源索引解析失败: " + e.getMessage());
                }
            }
        } else {
            mLastPos = 0;
        }
        
        // 初始化播放源和剧集数据
        if (currentSourceIndex >= now_source.size()) {
            currentSourceIndex = 0;
        }
        domain = now_source.get(currentSourceIndex).getType();
        sourceId = String.valueOf(currentSourceIndex + 1);
        G = now_source.get(currentSourceIndex).getList();
        
        // 构建videoInfo
        if (G != null && G.size() > 0) {
            videoInfo = new ArrayList<>();
            for (int i = 0; i < G.size(); i++) {
                VideoInfo vinfo = new VideoInfo();
                vinfo.title = G.get(i).getTitle();
                vinfo.url = G.get(i).getUrl();
                videoInfo.add(vinfo);
            }
        }
        
        // 更新剧集数
        if (tvEpisodeCount != null && G != null) {
            tvEpisodeCount.setText(G.size() + "集全");
        }
        
        // 更新播放源按钮
        if (btnSource != null && now_source != null && now_source.size() > 0) {
            btnSource.setText(now_source.get(currentSourceIndex).getName());
        }
        
        // 构建UI
        buildEpisodeList();
        buildPageList();
        buildSourceList();
        buildCoreList();
        
        // 相关推荐
        AboutInfo about = response.getAbout();
        if (about != null) {
            ArrayList<VodDataInfo> similary = (ArrayList<VodDataInfo>) about.getSimilary();
            ArrayList<VodDataInfo> actor = (ArrayList<VodDataInfo>) about.getActor();
            if (similary != null && similary.size() > 0) {
                aboutlist = similary;
            } else if (actor != null && actor.size() > 0) {
                aboutlist = actor;
            }
            buildRecommendList();
        }
        
        // 检查收藏状态
        checkFavoriteStatus();
        
        // 设置焦点到播放器区域，方便遥控器操作
        if (videoFrame != null) {
            videoFrame.requestFocus();
        } else if (btnFullscreen != null) {
            btnFullscreen.requestFocus();
        }
        
        // 开始播放
        if (videoInfo != null && videoInfo.size() > 0) {
            xjposition = playIndex;
            
            if (playIndex >= videoInfo.size()) {
                playIndex = 0;
                currentEpisodeIndex = 0;
            }
            
            if (vodtype != null && vodtype.equals("MOVIE") && videoInfo.size() == 1) {
                if (tv_mv_name != null) tv_mv_name.setText(vodname);
            } else {
                if (tv_mv_name != null) tv_mv_name.setText(videoInfo.get(playIndex).title);
            }
            
            PrepareVodData(playIndex);
        }
    }

    private void buildEpisodeList() {
        if (episodeContainer == null || G == null) return;
        episodeContainer.removeAllViews();
        
        int pageSize = 10;
        int start = currentPageIndex * pageSize;
        int end = Math.min(start + pageSize, G.size());
        
        for (int i = start; i < end; i++) {
            // 倒序时：第0页显示最后10集(35,34,33...)，第1页显示(25,24,23...)
            final int index = isEpisodeAscending ? i : (G.size() - 1 - i);
            if (index < 0 || index >= G.size()) continue;
            
            Button btn = new Button(this);
            btn.setText(G.get(index).getTitle());
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            btn.setTextColor(index == currentEpisodeIndex ? Color.parseColor("#2196F3") : Color.WHITE);
            btn.setBackgroundResource(R.drawable.bg_episode_button);
            btn.setFocusable(true);
            int padH = getResources().getDimensionPixelSize(R.dimen.sm_20);
            int padV = getResources().getDimensionPixelSize(R.dimen.sm_10);
            btn.setPadding(padH, padV, padH, padV);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_12), 0);
            btn.setLayoutParams(params);
            
            final int finalIndex = index;
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    playEpisode(finalIndex);
                    updateEpisodeSelection(finalIndex);
                }
            });
            
            btn.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                            playEpisode(finalIndex);
                            updateEpisodeSelection(finalIndex);
                            return true;
                        }
                    }
                    return false;
                }
            });
            
            episodeContainer.addView(btn);
        }
        
        if (tvEpisodeCount != null) {
            tvEpisodeCount.setText("选集 (" + G.size() + "集)");
        }
    }
    
    private void updateEpisodeSelection(int newIndex) {
        currentEpisodeIndex = newIndex;
        if (episodeContainer == null) return;
        
        for (int i = 0; i < episodeContainer.getChildCount(); i++) {
            View child = episodeContainer.getChildAt(i);
            if (child instanceof Button) {
                Button btn = (Button) child;
                int pageSize = 50;
                int start = currentPageIndex * pageSize;
                int actualIndex = isEpisodeAscending ? (start + i) : (G.size() - 1 - i);
                btn.setTextColor(actualIndex == newIndex ? Color.parseColor("#2196F3") : Color.WHITE);
            }
        }
    }
    
    private void buildPageList() {
        if (pageContainer == null || G == null) return;
        pageContainer.removeAllViews();
        
        int pageSize = 10;
        int totalPages = (G.size() + pageSize - 1) / pageSize;
        
        if (totalPages <= 1) {
            pageContainer.setVisibility(View.GONE);
            return;
        }
        pageContainer.setVisibility(View.VISIBLE);
        
        for (int i = 0; i < totalPages; i++) {
            final int pageIndex = i;
            int start, end;
            if (isEpisodeAscending) {
                // 正序：1-10, 11-20, 21-30...
                start = i * pageSize + 1;
                end = Math.min((i + 1) * pageSize, G.size());
            } else {
                // 倒序：36-27, 26-17, 16-7, 6-1...
                start = G.size() - i * pageSize;
                end = Math.max(G.size() - (i + 1) * pageSize + 1, 1);
            }
            
            Button btn = new Button(this);
            btn.setText(start + "-" + end);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            btn.setTextColor(i == currentPageIndex ? Color.parseColor("#2196F3") : Color.WHITE);
            btn.setBackgroundResource(R.drawable.bg_episode_button);
            btn.setFocusable(true);
            int pad = getResources().getDimensionPixelSize(R.dimen.sm_8);
            btn.setPadding(pad, pad / 2, pad, pad / 2);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_6), 0);
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    currentPageIndex = pageIndex;
                    buildEpisodeList();
                    buildPageList();
                }
            });
            
            pageContainer.addView(btn);
        }
    }
    
    private void buildSourceList() {
        
        if (sourceListContainer == null || source == null) {
            Log.w(TAG, "sourceListContainer或source为null，跳过构建");
            return;
        }
        
        sourceListContainer.removeAllViews();
        
        for (int i = 0; i < source.size(); i++) {
            final int index = i;
            VodUrl vodUrl = source.get(i);
            
            // 获取集数
            int episodeCount = 0;
            if (vodUrl.getList() != null) {
                episodeCount = vodUrl.getList().size();
            } else if (vodUrl.getLists() != null) {
                episodeCount = vodUrl.getLists().size();
            }
            
            Button btn = new Button(this);
            String displayText = vodUrl.getName();
            if (episodeCount > 0) {
                displayText += " (" + episodeCount + "集)";
            }
            btn.setText(displayText);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            
            // 高亮当前选中的播放源
            boolean isSelected = (i == currentSourceIndex);
            btn.setTextColor(isSelected ? Color.parseColor("#2196F3") : Color.WHITE);
            
            btn.setBackgroundResource(R.drawable.bg_episode_button);
            btn.setFocusable(true);
            int pad = getResources().getDimensionPixelSize(R.dimen.sm_12);
            btn.setPadding(pad, pad / 2, pad, pad / 2);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.sm_6));
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchSource(index);
                }
            });
            
            sourceListContainer.addView(btn);
            
            // 如果是当前选中的播放源，设置焦点
            if (isSelected && sourcePanel != null && sourcePanel.getVisibility() == View.VISIBLE) {
                btn.requestFocus();
            }
        }
        
    }
    
    private void buildCoreList() {
        if (coreListContainer == null) return;
        coreListContainer.removeAllViews();
        
        for (int i = 0; i < playerCores.length; i++) {
            final int index = i;
            Button btn = new Button(this);
            btn.setText(playerCores[i]);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            btn.setTextColor(i == currentCoreIndex ? Color.parseColor("#2196F3") : Color.WHITE);
            btn.setBackgroundResource(R.drawable.bg_episode_button);
            btn.setFocusable(true);
            int pad = getResources().getDimensionPixelSize(R.dimen.sm_12);
            btn.setPadding(pad, pad / 2, pad, pad / 2);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.sm_6));
            btn.setLayoutParams(params);
            
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchCore(index);
                }
            });
            
            coreListContainer.addView(btn);
            
            // 如果是当前选中的内核，设置焦点
            if (i == currentCoreIndex && corePanel != null && corePanel.getVisibility() == View.VISIBLE) {
                btn.requestFocus();
            }
        }
    }
    
    private void buildRecommendList() {
        if (recommendContainer == null || aboutlist == null || aboutlist.size() == 0) return;
        recommendContainer.removeAllViews();
        
        int itemWidth = getResources().getDimensionPixelSize(R.dimen.sm_160);
        int itemHeight = getResources().getDimensionPixelSize(R.dimen.sm_220);
        
        for (int i = 0; i < Math.min(aboutlist.size(), 10); i++) {
            final VodDataInfo vod = aboutlist.get(i);
            
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setFocusable(true);
            itemLayout.setBackgroundResource(R.drawable.bg_episode_button);
            int itemPad = getResources().getDimensionPixelSize(R.dimen.sm_4);
            itemLayout.setPadding(itemPad, itemPad, itemPad, itemPad);
            
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(itemWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
            itemParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_10), 0);
            itemLayout.setLayoutParams(itemParams);
            
            ImageView poster = new ImageView(this);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight);
            poster.setLayoutParams(imgParams);
            poster.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (vod.getPic() != null) {
                imageLoader.displayImage(vod.getPic(), poster, options);
            }
            
            TextView title = new TextView(this);
            title.setText(vod.getTitle());
            title.setTextColor(Color.WHITE);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            title.setMaxLines(1);
            title.setGravity(Gravity.CENTER);
            title.setPadding(0, getResources().getDimensionPixelSize(R.dimen.sm_4), 0, 0);
            
            itemLayout.addView(poster);
            itemLayout.addView(title);
            
            itemLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 防止重复跳转
                    if (isNavigating) {
                        return;
                    }
                    isNavigating = true;
                    Intent intent = new Intent(VideoDetailsActivity.this, VideoDetailsActivity.class);
                    intent.putExtra("nextlink", vod.getNextlink());
                    intent.putExtra("vodtype", vodtype);
                    intent.putExtra("vodstate", vod.getState());
                    startActivity(intent);
                    finish();
                }
            });
            
            recommendContainer.addView(itemLayout);
        }
    }
    
    private void switchSource(int index) {
        if (index == currentSourceIndex || source == null || index >= source.size()) return;
        
        
        currentSourceIndex = index;
        sourceId = String.valueOf(index + 1);
        PlayersNumber = index + 1;
        G = source.get(index).getList();
        domain = source.get(index).getType();
        
        // 更新播放源按钮文本
        if (btnSource != null && now_source != null && index < now_source.size()) {
            btnSource.setText(now_source.get(index).getName());
        }
        
        if (G != null && G.size() > 0) {
            videoInfo = new ArrayList<>();
            for (int i = 0; i < G.size(); i++) {
                VideoInfo vinfo = new VideoInfo();
                vinfo.title = G.get(i).getTitle();
                vinfo.url = G.get(i).getUrl();
                videoInfo.add(vinfo);
            }
        }
        
        currentPageIndex = 0;
        buildEpisodeList();
        buildPageList();
        buildSourceList();
        
        if (sourcePanel != null) sourcePanel.setVisibility(View.GONE);
        
        // 立即保存播放源选择到数据库
        saveSourcePreference();
        
        int newPlayIndex = Math.min(currentEpisodeIndex, G.size() - 1);
        playEpisode(newPlayIndex);
    }
    
    /**
     * 保存播放源选择到数据库
     */
    private void saveSourcePreference() {
        if (videoId == null || sourceId == null) {
            Log.w(TAG, "saveSourcePreference: videoId或sourceId为null，跳过保存");
            return;
        }
        
        
        try {
            // 查询是否已有播放记录
            List<Album> existingAlbums = dao.queryAlbumById(videoId, 2);
            
            if (existingAlbums != null && existingAlbums.size() > 0) {
                // 更新现有记录
                Album album = existingAlbums.get(0);
                album.setAlbumSourceType(sourceId);
                album.setPlayIndex(playIndex);
                
                // 如果正在播放，也更新播放进度
                if (iVV != null) {
                    int currentPos = iVV.getCurrentPosition();
                    if (currentPos > 0) {
                        album.setCollectionTime(currentPos);
                    }
                }
                
                dao.addAlbums(album);
            } else {
                // 创建新记录
                Album album = new Album();
                album.setAlbumId(videoId);
                album.setAlbumSourceType(sourceId);
                album.setCollectionTime(0);
                album.setPlayIndex(playIndex);
                album.setAlbumPic(albumPic);
                album.setAlbumType(vodtype);
                album.setAlbumTitle(vodname);
                album.setAlbumState("观看中");
                album.setNextLink(nextlink);
                album.setTime(String.valueOf(System.currentTimeMillis()));
                album.setTypeId(2);
                
                dao.addAlbums(album);
            }
        } catch (Exception e) {
            Log.e(TAG, "保存播放源记录异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void switchCore(int index) {
        if (index == currentCoreIndex) return;
        
        currentCoreIndex = index;
        nhposition = index;
        
        Editor editor = sp.edit();
        editor.putString(Constant.hd, playerCores[index]);
        editor.commit();
        
        buildCoreList();
        
        // 更新播放内核按钮文本
        if (btnPlayerCore != null) {
            btnPlayerCore.setText(playerCores[index]);
        }
        
        if (corePanel != null) corePanel.setVisibility(View.GONE);
        
        if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE && videoInfo != null && playIndex < videoInfo.size()) {
            SelecteVod(playIndex);
            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        }
    }
    
    private void checkFavoriteStatus() {
        if (btnFavorite == null || videoId == null) return;
        int iconSize = getResources().getDimensionPixelSize(R.dimen.sm_28);
        albums = dao.queryAlbumById(videoId, 1);
        if (albums != null && albums.size() > 0) {
            btnFavorite.setText("已收藏");
            setButtonDrawableSize(btnFavorite, R.drawable.ic_control_keep_ok, iconSize);
        } else {
            btnFavorite.setText("收藏");
            setButtonDrawableSize(btnFavorite, R.drawable.ic_control_keep_ng, iconSize);
        }
    }
    
    private void setupListeners() {
        if (btnFullscreen != null) {
            btnFullscreen.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterFullscreen();
                }
            });
        } else {
            Log.e(TAG, "setupListeners: btnFullscreen 为 null!");
        }
        
        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleFavorite();
                }
            });
        }
        
        if (btnSource != null) {
            btnSource.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sourcePanel != null) {
                        if (sourcePanel.getVisibility() == View.VISIBLE) {
                            sourcePanel.setVisibility(View.GONE);
                        } else {
                            sourcePanel.setVisibility(View.VISIBLE);
                            // 聚焦到当前选中的播放源按钮
                            if (sourceListContainer != null && sourceListContainer.getChildCount() > 0) {
                                int focusIndex = Math.min(currentSourceIndex, sourceListContainer.getChildCount() - 1);
                                sourceListContainer.getChildAt(focusIndex).requestFocus();
                            }
                        }
                    }
                    if (corePanel != null) corePanel.setVisibility(View.GONE);
                }
            });
        }
        
        if (btnPlayerCore != null) {
            btnPlayerCore.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (corePanel != null) {
                        if (corePanel.getVisibility() == View.VISIBLE) {
                            corePanel.setVisibility(View.GONE);
                        } else {
                            corePanel.setVisibility(View.VISIBLE);
                            // 聚焦到当前选中的内核按钮
                            if (coreListContainer != null && coreListContainer.getChildCount() > 0) {
                                int focusIndex = Math.min(currentCoreIndex, coreListContainer.getChildCount() - 1);
                                coreListContainer.getChildAt(focusIndex).requestFocus();
                            }
                        }
                    }
                    if (sourcePanel != null) sourcePanel.setVisibility(View.GONE);
                }
            });
        }
        
        if (tvEpisodeOrder != null) {
            tvEpisodeOrder.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    isEpisodeAscending = !isEpisodeAscending;
                    tvEpisodeOrder.setText(isEpisodeAscending ? "剧集正序" : "剧集倒序");
                    currentPageIndex = 0; // 切换排序时重置到第一页
                    buildEpisodeList();
                    buildPageList(); // 重建分页按钮以更新显示
                }
            });
        }
        
        if (tv_menu != null) {
            tv_menu.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hideController();
                    showMenu();
                }
            });
        }
        
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    hideControllerDelay();
                    int iseekPos = seekBar.getProgress();
                    showLoading();
                    if (iVV != null) iVV.seekTo(iseekPos * 1000);
                    // 同步弹幕位置
                    seekDanmaku(iseekPos * 1000);
                    mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                }
                
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                }
                
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    updateTextViewWithTimeFormat(tv_currentTime, progress);
                    updateprogress(progress);
                }
            });
        }
    }
    
    private void toggleFavorite() {
        if (videoId == null) return;
        
        int iconSize = getResources().getDimensionPixelSize(R.dimen.sm_28);
        albums = dao.queryAlbumById(videoId, 1);
        if (albums != null && albums.size() > 0) {
            dao.deleteByWhere(videoId, vodtype, 1);
            if (btnFavorite != null) {
                btnFavorite.setText("收藏");
                setButtonDrawableSize(btnFavorite, R.drawable.ic_control_keep_ng, iconSize);
            }
            Utils.showToast(this, R.string.Cancel_collection_successful, R.drawable.toast_smile);
        } else {
            long secondTime = System.currentTimeMillis();
            Album album = new Album();
            album.setAlbumId(videoId);
            album.setAlbumSourceType(sourceId);
            album.setCollectionTime(0);
            album.setPlayIndex(playIndex);
            album.setAlbumPic(albumPic);
            album.setAlbumType(vodtype);
            album.setAlbumTitle(vodname);
            album.setAlbumState(vodstate);
            album.setNextLink(nextlink);
            album.setTime(String.valueOf(secondTime));
            album.setTypeId(1);
            dao.addAlbums(album);
            if (btnFavorite != null) {
                btnFavorite.setText("已收藏");
                setButtonDrawableSize(btnFavorite, R.drawable.ic_control_keep_ok, iconSize);
            }
            Utils.showToast(this, R.string.Collection_successful, R.drawable.toast_smile);
        }
    }

    private void enterFullscreen() {
        if (iVV != null) {
        }
        
        // 检查是否有视频在播放（手动全屏时放宽限制，只要有视频播放器实例即可）
        // 注意：手动全屏按钮应该允许在任何时候点击，只要播放器已初始化
        boolean hasPlayer = iVV != null;
        if (!hasPlayer) {
            Log.d(TAG, "播放器未初始化，无法进入全屏");
            return;
        }
        
        // 取消自动全屏定时器
        if (autoFullscreenHandler != null && autoFullscreenRunnable != null) {
            autoFullscreenHandler.removeCallbacks(autoFullscreenRunnable);
            Log.d(TAG, "进入全屏，取消自动全屏定时器");
        }
        
        isFullscreen = true;
        focusBeforeFullscreen = getCurrentFocus();
        
        // 优化：简化进入全屏流程，减少卡顿
        if (videoFrame != null) {
            // 启用硬件加速层，提升动画性能
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                videoFrame.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            
            // 清除焦点边框
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                videoFrame.setForeground(null);
            }
            
            // 优化1：直接隐藏视图，不使用动画（减少卡顿）
            if (contentScroll != null) {
                contentScroll.setVisibility(View.GONE);
            }
            if (detailsScrollView != null) {
                detailsScrollView.setVisibility(View.GONE);
            }
            
            // 优化2：简化全屏动画，使用更短的时长和更简单的插值器
            RelativeLayout.LayoutParams fullParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            fullParams.setMargins(0, 0, 0, 0);
            
            // 使用View的animate()方法，比ValueAnimator更高效
            videoFrame.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200) // 从300ms减少到200ms
                .setInterpolator(new android.view.animation.DecelerateInterpolator()) // 使用更简单的插值器
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        // 在动画开始时直接设置为全屏布局
                        videoFrame.setLayoutParams(fullParams);
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // 动画完成后恢复普通渲染层
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            videoFrame.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }
                })
                .start();
        } else {
            // 如果videoFrame为null，直接隐藏视图
            if (contentScroll != null) {
                contentScroll.setVisibility(View.GONE);
            }
            if (detailsScrollView != null) {
                detailsScrollView.setVisibility(View.GONE);
            }
        }
        
        // 优化3：直接显示视频名称和分辨率，不使用渐变动画
        if (tv_mv_name != null) {
            tv_mv_name.setVisibility(View.VISIBLE);
        }
        if (tv_resolution != null) {
            tv_resolution.setVisibility(View.VISIBLE);
        }
        
        // 立即隐藏导航栏
        Navigation();
        
        if (videoFrame != null) {
            videoFrame.requestFocus();
        }
    }
    
    private void exitFullscreen() {
        isFullscreen = false;
        
        // 优化：简化退出全屏流程，减少卡顿
        if (videoFrame != null) {
            // 启用硬件加速层，提升动画性能
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                videoFrame.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            
            // 优化1：直接隐藏视频名称和分辨率，不使用动画
            if (tv_mv_name != null) {
                tv_mv_name.setVisibility(View.GONE);
            }
            
            // 计算目标位置和大小（小窗）
            int width = getResources().getDimensionPixelSize(R.dimen.sm_580);
            int height = getResources().getDimensionPixelSize(R.dimen.sm_326);
            int marginLeft = getResources().getDimensionPixelSize(R.dimen.sm_40);
            int marginTop = getResources().getDimensionPixelSize(R.dimen.sm_40);
            
            final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
            params.setMargins(marginLeft, marginTop, 0, 0);
            
            // 优化2：使用更简单快速的动画
            videoFrame.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(200) // 从300ms减少到200ms
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        // 在动画开始时直接设置为小窗布局
                        videoFrame.setLayoutParams(params);
                    }
                })
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // 动画完成后恢复普通渲染层
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            videoFrame.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                        
                        // 优化3：直接显示ScrollView和详情区域，不使用渐变动画
                        if (contentScroll != null) {
                            contentScroll.setVisibility(View.VISIBLE);
                        }
                        if (detailsScrollView != null) {
                            detailsScrollView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .start();
        } else {
            // 如果videoFrame为null，直接显示视图
            if (contentScroll != null) {
                contentScroll.setVisibility(View.VISIBLE);
            }
            if (detailsScrollView != null) {
                detailsScrollView.setVisibility(View.VISIBLE);
            }
        }
        
        if (tv_resolution != null) tv_resolution.setVisibility(View.VISIBLE);
        
        hideController();
        hideMenu();
        
        // 退出全屏后不再触发自动全屏
        if (autoFullscreenHandler != null && autoFullscreenRunnable != null) {
            autoFullscreenHandler.removeCallbacks(autoFullscreenRunnable);
            Log.d(TAG, "退出全屏，取消自动全屏定时器，不再触发");
        }
        
        if (focusBeforeFullscreen != null) {
            focusBeforeFullscreen.requestFocus();
        }
    }
    
    private void playEpisode(int index) {
        
        if (videoInfo == null || videoInfo.size() <= index) {
            Log.e(TAG, "playEpisode: videoInfo无效或index越界");
            return;
        }
        
        isPlaying = true;
        playIndex = index;
        xjposition = index;
        currentEpisodeIndex = index;
        collectionTime = 0;
        mLastPos = 0;  // 切换剧集时重置播放位置
        mLastPos2 = 0; // 同时重置备用播放位置，避免切换集数后从旧进度开始
        Numbermax = 0;
        PlayersNumber = currentSourceIndex + 1;
        currentPosition = 0;
        seizing = 0;
        
        // 切换剧集时重置弹幕状态
        if (danmakuManager != null) {
            danmakuManager.reset();
        }
        
        if (iVV != null) {
            iVV.stopPlayback();
        }
        
        url = videoInfo.get(index).url;
        
        if (vodtype != null && !vodtype.equals("MOVIE") || videoInfo.size() != 1) {
            if (tv_mv_name != null) tv_mv_name.setText(vodname + "-" + videoInfo.get(index).title);
        } else {
            if (tv_mv_name != null) tv_mv_name.setText(vodname);
        }
        
        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        setVideoUrl(Client);
    }
    
    private void PrepareVodData(int postion) {
        
        // 使用数据库恢复的播放源，如果没有则使用第1个播放源
        if (PlayersNumber == 0) {
            // currentSourceIndex是从0开始的索引，PlayersNumber是从1开始的编号
            PlayersNumber = currentSourceIndex + 1;
            if (PlayersNumber <= 0) {
                PlayersNumber = 1;
            }
        }
        
        isSwitch = true;
        if (videoInfo != null && videoInfo.size() > 0 && videoInfo.size() >= playIndex) {
            if (vodtype != null && !vodtype.equals("MOVIE") || videoInfo.size() != 1) {
                if (tv_mv_name != null) tv_mv_name.setText(vodname + "-" + videoInfo.get(playIndex).title);
            } else {
                if (tv_mv_name != null) tv_mv_name.setText(vodname);
            }
            url = videoInfo.get(postion).url;
            mediaHandler.sendEmptyMessage(WindowMessageID.DATA_PREPARE_OK);
            mEventHandler.sendEmptyMessage(WindowMessageID.EVENT_PLAY);
        }
    }
    
    private void SelecteVod(int postion) {
        
        String playcore = sp.getString(Constant.hd, "IJK");
        if ("自动".equals(playcore)) nhposition = 0;
        else if ("系统".equals(playcore)) nhposition = 1;
        else if ("IJK".equals(playcore)) nhposition = 2;
        else if ("EXO".equals(playcore)) nhposition = 3;
        else if ("阿里".equals(playcore)) nhposition = 4;
        
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        isSwitch = true;
        stopPlayback();
        mHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        
        if (vodtype != null && !vodtype.equals("MOVIE") || videoInfo.size() == 1) {
            if (tv_mv_name != null) tv_mv_name.setText(vodname + "-" + videoInfo.get(playIndex).title);
        } else {
            if (tv_mv_name != null) tv_mv_name.setText(vodname);
        }
        
        url = videoInfo.get(postion).url;
        setVideoUrl(Client);
    }
    
    private void stopPlayback() {
        
        // 取消准备超时检测
        if (mHandler != null) {
            mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
            mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        }
        
        if (iVV != null) {
            try {
                iVV.stopPlayback();
                iVV.release(true);
            } catch (Exception e) {
                Log.e(TAG, "停止播放器时出错: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.w(TAG, "iVV为null，无需停止");
        }
        
        // 重置播放状态
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        isCodeBlockExecuted = false;
    }
    
    public void setVideoUrl(String Client) {
        int Submission_method = SharePreferenceDataUtil.getSharedIntData(this, Constant.JE, 0);
        // 不在这里添加时间戳，在analysisUrl中统一添加
        String urlEncode = Utils.UrlEncodeChinese(Client + "/?url=" + url);
        analysisUrl(urlEncode, Submission_method);
    }
    
    private void analysisUrl(String urls, int way) {
        int Timeout = SharePreferenceDataUtil.getSharedIntData(this, Constant.WM, 25);
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        
        final String account = sp.getString("userName", "");
        final String password = sp.getString("passWord", "");
        final String token = sp.getString("ckinfo", "");
        final String machineid = Utils.GetAndroidID(this);
        final double value = Double.valueOf(Utils.getVersion(this).toString());
        final String name = Utils.getEcodString(vodname + "-" + videoInfo.get(playIndex).title);
        
        // 添加时间戳参数，确保每次请求都是唯一的，避免缓存
        final String timestamp = String.valueOf(System.currentTimeMillis());
        
        if (way == 0) {
            String requestUrl = urls + "&app=" + Api.APPID + "&account=" + account + "&password=" + password + "&token=" + token + "&machineid=" + machineid + "&edition=" + value + "&vodname=" + name + "&line=" + domain + "&new=1&_t=" + timestamp;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                    new com.android.volley.Response.Listener<String>() {
                        public void onResponse(String response) {
                            analysisUrlResponse(response);
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    analysisUrlError(error);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VideoDetailsActivity.this, "Authorization", ""), Constant.d));
                    headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
                    headers.put("Pragma", "no-cache");
                    headers.put("Expires", "0");
                    return headers;
                }
            };
            stringRequest.setShouldCache(false);  // 禁用Volley缓存
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(Timeout * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(stringRequest);
        }
        
        if (way == 1) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, urls,
                    new com.android.volley.Response.Listener<String>() {
                        public void onResponse(String response) {
                            analysisUrlResponse(response);
                        }
                    }, new com.android.volley.Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    analysisUrlError(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("app", Api.APPID);
                    params.put("account", account);
                    params.put("password", password);
                    params.put("token", token);
                    params.put("machineid", machineid);
                    params.put("edition", String.valueOf(value));
                    params.put("vodname", name);
                    params.put("line", domain);
                    params.put("new", "1");
                    params.put("_t", timestamp);  // 添加时间戳参数
                    return params;
                }
                
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VideoDetailsActivity.this, "Authorization", ""), Constant.d));
                    headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
                    headers.put("Pragma", "no-cache");
                    headers.put("Expires", "0");
                    return headers;
                }
            };
            stringRequest.setShouldCache(false);  // 禁用Volley缓存
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(Timeout * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(stringRequest);
        }
    }
    
    public void analysisUrlResponse(String response) {
        if (response != null && response.length() < 1000) {
        }
        
        // 检查是否正在解析中
        if (isParsing) {
            return;
        }
        isParsing = true; // 设置解析标志
        
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            int encrypt = jSONObject.optInt("encrypt");
            
            if (code != 200) {
                Log.e(TAG, "解析失败: code=" + code);
                String msg = jSONObject.optString("msg", "未知错误");
                Log.e(TAG, "错误信息: " + msg);
                Utils.showToast(this, "解析失败: " + msg, R.drawable.toast_err);
                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                return;
            }
            
            // code == 200，开始解析数据
            JSONObject data = jSONObject.getJSONObject("data");
            JSONObject header = data.getJSONObject("header");
            userAgent = header.getString("User-Agent");
            if (header.has("Referer")) Referer = header.getString("Referer");
            if (header.has("referer")) Referer = header.getString("referer");
            
            final String videoUrl = data.getString("url");
            int maxtimeout = data.optInt("Maxtimeout", 0);
            
            // 检查videoUrl是否为空或Maxtimeout是否为0（表示解析失败）
            if (videoUrl == null || videoUrl.trim().isEmpty() || maxtimeout == 0) {
                Log.e(TAG, "解析失败: videoUrl为空或Maxtimeout=0, videoUrl=" + videoUrl + ", Maxtimeout=" + maxtimeout);
                Utils.showToast(this, "解析失败: 解析接口无法获取播放地址，请尝试切换其他线路", R.drawable.toast_err);
                
                // 根据后端配置决定是否自动换源
                if (Auto_Source == 1 && Number > 1) {
                    Utils.showToast(this, "正在切换播放源...", R.drawable.toast_err);
                    isParsing = false; // 清除解析标志，允许换源后重新解析
                    Switchsource(1);
                } else {
                    mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                    isParsing = false; // 清除解析标志
                }
                return;
            }
            
            ClientID = data.getInt("ClientID");
            MaxClientID = data.getInt("MaxClientID");
            Maxtimeout = maxtimeout;
            Exclude_content = data.getString("Exclude_content");
            timeout = Maxtimeout;
            core = data.getInt("Core");
            safe_mode = data.getInt("Safe");
            Ad_block = data.getInt("Ad_block");
            position = data.getInt("position");
            EwmWidth = data.getInt("EwmWidth");
            EwmHeight = data.getInt("EwmHeight");
            Moviesize = data.getInt("Moviesize");
            Tvplaysize = data.getInt("Tvplaysize");
            headposition = data.getInt("headposition");
                
                if (core_mode == 1) {
                    if (core != 99) {
                        if (safe_mode == 1) {
                            nhposition = core;
                        } else {
                            if (Build.VERSION.SDK_INT < 22) {
                                nhposition = 2;
                            } else {
                                nhposition = core;
                            }
                        }
                    }
                }
            
            Conditions = data.getString("Conditions");
            Type = data.getInt("Type");
            
            if (iVV != null) {
                iVV.setUserAgent(userAgent);
                iVV.setReferer(Referer);
            } else {
                Log.e(TAG, "警告: iVV为null!");
            }
            
            headers = new HashMap<>();
            Iterator<String> keys = header.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = header.getString(key);
                headers.put(key, value);
            }
            
            if (Type == 0) {
                
                if (iVV == null) {
                    Log.e(TAG, "严重错误: iVV为null，无法播放视频!");
                    Utils.showToast(this, "播放器初始化失败", R.drawable.toast_err);
                    mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                    return;
                }
                
                if (webView != null) webView.setVisibility(View.GONE);
                if (encrypt == 1) {
                    String decryptedUrl = Rc4.decryptBase64(videoUrl, Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""), Constant.d));
                    
                    // 先取消之前的超时检测
                    mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
                    
                    String playUrl = Failed.equals("") ? Utils.UrlEncodeChinese(decryptedUrl) : Utils.UrlEncodeChinese(Failed);
                    iVV.setVideoPath(playUrl, headers);
                    
                    // 启动10秒准备超时检测
                    mHandler.sendEmptyMessageDelayed(WindowMessageID.PREPARE_TIMEOUT, 10000);
                    Fburl = Utils.UrlEncodeChinese(decryptedUrl);
                    mEventHandler.sendEmptyMessage(WindowMessageID.SUCCESS);
                } else {
                    
                    // 先取消之前的超时检测
                    mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
                    
                    String playUrl = Failed.equals("") ? Utils.UrlEncodeChinese(videoUrl) : Utils.UrlEncodeChinese(Failed);
                    iVV.setVideoPath(playUrl, headers);
                    
                    // 启动10秒准备超时检测
                    mHandler.sendEmptyMessageDelayed(WindowMessageID.PREPARE_TIMEOUT, 10000);
                    Fburl = Utils.UrlEncodeChinese(videoUrl);
                    mEventHandler.sendEmptyMessage(WindowMessageID.SUCCESS);
                }
            } else {
                if (Build.VERSION.SDK_INT < 23) {
                    if (Sniff_debug_mode == 1) {
                        Toast.makeText(this, R.string.Sniffing_messages, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (encrypt == 1) {
                        jxurls = 0;
                        initializeWebView();
                        String decryptedUrl = Rc4.decryptBase64(videoUrl, Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""), Constant.d));
                        Sniffing(Utils.UrlEncodeChinese(decryptedUrl));
                    } else {
                        jxurls = 0;
                        initializeWebView();
                        Sniffing(videoUrl);
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "========== JSON解析异常 ==========");
            Log.e(TAG, "异常信息: " + e.getMessage());
            e.printStackTrace();
            Utils.showToast(this, "数据解析失败: " + e.getMessage(), R.drawable.toast_err);
            mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
            isParsing = false; // 异常时立即清除标志
        } catch (Exception e) {
            Log.e(TAG, "========== 未知异常 ==========");
            Log.e(TAG, "异常信息: " + e.getMessage());
            e.printStackTrace();
            Utils.showToast(this, "播放失败: " + e.getMessage(), R.drawable.toast_err);
            mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
            isParsing = false; // 异常时立即清除标志
        } finally {
            // 延迟清除解析标志，给播放器初始化一些时间
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isParsing = false;
                }
            }, 500); // 缩短到500毫秒
        }
    }
    
    public void analysisUrlError(VolleyError volleyError) {
        Log.e(TAG, "========== analysisUrlError ==========");
        Log.e(TAG, "错误: " + volleyError.toString());
        Log.e(TAG, "Auto_Source=" + Auto_Source + ", Number=" + Number);
        String errorMsg = "解析失败";
        
        if (volleyError instanceof ServerError) {
            Utils.showToast(this, "客户端不存在!", R.drawable.toast_shut);
            if (iVV != null) iVV.stopPlayback();
            finish();
            return;
        } else if (volleyError instanceof TimeoutError) {
            errorMsg = "请求超时";
        } else if (volleyError instanceof NetworkError) {
            errorMsg = "网络错误，请检查网络连接";
        } else if (volleyError instanceof AuthFailureError) {
            errorMsg = "认证失败";
        }
        
        // 重置错误标志
        isCodeBlockExecuted = false;
        
        // 根据后端配置决定是否自动换源
        if (Auto_Source == 1) {
            // 后端开启了自动换源
            if (Number > 1) {
                Utils.showToast(this, errorMsg + "，正在切换播放源...", R.drawable.toast_err);
                Switchsource(1);
            } else {
                Utils.showToast(this, errorMsg + "，当前只有一个播放源", R.drawable.toast_err);
                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
            }
        } else {
            // 后端关闭了自动换源
            Utils.showToast(this, errorMsg + "，请手动切换播放源", R.drawable.toast_err);
            mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
        }
    }

    private void initializeWebView() {
        if (webView == null) return;
        webView.stopLoading();
        
        // 清除WebView缓存，确保每次都重新加载
        webView.clearCache(true);
        webView.clearHistory();
        
        if (Sniff_debug_mode == 1) {
            webView.setVisibility(View.VISIBLE);
            Toast.makeText(this, R.string.Sniffing_message, Toast.LENGTH_SHORT).show();
        }
        webView.setWebViewClient(new WebViewClient());
        WebSettings settings = webView.getSettings();
        settings.setUserAgentString(userAgent);
        settings.setJavaScriptEnabled(true);
        settings.setDefaultTextEncodingName("utf-8");
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);  // 强制不使用缓存
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSavePassword(true);
        settings.setSaveFormData(true);
        settings.setTextZoom(100);
        settings.setDomStorageEnabled(true);
        settings.setSupportMultipleWindows(true);
        if (Build.VERSION.SDK_INT >= 21) settings.setMixedContentMode(0);
        if (Build.VERSION.SDK_INT >= 17) settings.setMediaPlaybackRequiresUserGesture(true);
        if (Build.VERSION.SDK_INT >= 16) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAppCacheEnabled(false);  // 禁用应用缓存
        settings.setDatabaseEnabled(true);
        settings.setGeolocationDatabasePath(getDir("database", 0).getPath());
        settings.setGeolocationEnabled(true);
        
        // 清除Cookie（可选，根据需要）
        CookieManager cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < 21) CookieSyncManager.createInstance(getApplicationContext());
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= 21) cookieManager.setAcceptThirdPartyCookies(webView, true);
        
    }
    
    private void Sniffing(String sniffUrl) {
        if (webView == null) return;
        webView.loadUrl(sniffUrl);
        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                processSniffUrl(url, null);
                return null;
            }
            
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String requestUrl = request.getUrl().toString();
                    Map<String, String> headers = request.getRequestHeaders();
                    processSniffUrl(requestUrl, headers);
                }
                return null;
            }
        });
    }
    
    private void processSniffUrl(String requestUrl, Map<String, String> headers) {
        try {
            JSONObject jsonObject = new JSONObject(Conditions);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (requestUrl.contains(jsonObject.getString(key)) && !requestUrl.contains(Exclude_content)) {
                    webHeaders = new HashMap<>();
                    try {
                        if (headers != null) {
                            for (String k : headers.keySet()) {
                                if (k.equalsIgnoreCase("user-agent") || k.equalsIgnoreCase("referer") || k.equalsIgnoreCase("origin")) {
                                    webHeaders.put(k, " " + headers.get(k));
                                }
                            }
                        }
                    } catch (Throwable th) {}
                    jxurl = requestUrl;
                    if (jxurls != 0) return;
                    jxurls = 1;
                    mediaHandler.sendEmptyMessage(WindowMessageID.Sniffing);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void setPlayerListener() {
        
        if (iVV == null) {
            Log.e(TAG, "iVV为null，无法设置监听器");
            return;
        }
        
        
        iVV.setOnPlayingBufferCacheListener(new OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {
                if (percent > 1 && seekBar != null) {
                    if (percent > 98) {
                        seekBar.setSecondaryProgress(iVV.getDuration() / 1000);
                        return;
                    }
                    seekBar.setSecondaryProgress(iVV.getDuration() / 1000 * iVV.getBufferPercentage() / 100);
                }
            }
        });
        
        iVV.setOnErrorListener(new OnErrorListener() {
            public boolean onError(IMediaPlayer arg0, int what, int extra) {
                Log.e(TAG, "========== 播放错误 ==========");
                Log.e(TAG, "what=" + what + ", extra=" + extra);
                Log.e(TAG, "isFullscreen=" + isFullscreen + ", isCodeBlockExecuted=" + isCodeBlockExecuted);
                
                // 根据错误类型记录详细信息
                String errorMsg = "播放出错";
                if (what == -1004 || what == -1007) {
                    errorMsg = "网络连接失败";
                } else if (what == -1010) {
                    errorMsg = "视频格式不支持";
                } else if (what == -110) {
                    errorMsg = "连接超时";
                } else if (what == 1 || what == -1) {
                    errorMsg = "播放器错误";
                }
                Log.e(TAG, "错误详情: " + errorMsg);
                
                synchronized (SYNC_Playing) {
                    if (isCodeBlockExecuted) {
                        return true;
                    }
                    SYNC_Playing.notify();
                    mHandler.sendEmptyMessage(WindowMessageID.PLAY_ERROR);
                }
                isCodeBlockExecuted = true;
                return true;
            }
        });
        
        iVV.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(IMediaPlayer arg0) {
                // 取消准备超时检测
                mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
                
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int dpi = displayMetrics.densityDpi;
                
                if (Adblock == 1 && Ad_block == 1) {
                    Navigation_mode = 0;
                    setupAdBlock();
                } else {
                    Navigation_mode = SharePreferenceDataUtil.getSharedIntData(VideoDetailsActivity.this, Constant.bw, 0);
                    if (Ad_block_up != null) Ad_block_up.setVisibility(View.GONE);
                    if (Ad_block_down != null) Ad_block_down.setVisibility(View.GONE);
                    if (Ad_block_right != null) Ad_block_right.setVisibility(View.GONE);
                }
                
                if (Auto_Source == 1 && vod_caton_check == 1 && mSpeedHandler != null) {
                    mSpeedHandler.postDelayed(speedRunnabless, seizing_time * 1000);
                }
                
                seizings = 0;
                if (seekBar != null) seekBar.setSecondaryProgress(0);
                mediaHandler.sendEmptyMessageDelayed(WindowMessageID.START_NOTICE_GONE, Vod_Notice_starting_time * 1000);
                
                if (mLastPos > 0 && vodtype != null && !vodtype.equals("LIVE")) {
                    mLastPos2 = mLastPos;
                    mLastPos = 0;
                } else {
                }
                
                mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
                Numbermax = 0;
                isPlaying = true;
                // 确保隐藏所有进度条
                hideLoading();
                // 再次确保隐藏，防止被覆盖
                if (mProgressBar != null) mProgressBar.setVisibility(View.GONE);
                View loadingLayout = findViewById(R.id.loading_layout);
                if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                
                // 显示视频分辨率
                updateResolutionDisplay();
                
                // 启动2秒后自动全屏定时器（只在非全屏状态下）
                // 优化：缩短延迟时间，从4秒改为2秒，提升用户体验
                if (!isFullscreen && autoFullscreenHandler != null && autoFullscreenRunnable != null) {
                    autoFullscreenHandler.removeCallbacks(autoFullscreenRunnable);
                    autoFullscreenHandler.postDelayed(autoFullscreenRunnable, 2000);
                    Log.d(TAG, "启动2秒自动全屏定时器");
                }
                
                // 加载并启动弹幕
                try {
                loadDanmakuData();
                startDanmaku();
                } catch (Exception e) {
                    Log.e(TAG, "加载弹幕失败: " + e.getMessage());
                }
                
                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                mediaHandler.sendEmptyMessage(WindowMessageID.SELECT_SCALES);
                mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
            }
        });
        
        iVV.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(IMediaPlayer arg0) {
                synchronized (SYNC_Playing) {
                    SYNC_Playing.notify();
                }
                mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
                mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                
                if (isNext.booleanValue()) {
                    playNextEpisode();
                    isNext = Boolean.valueOf(false);
                } else if (isLast.booleanValue()) {
                    playPreviousEpisode();
                    isLast = Boolean.valueOf(false);
                } else if (isPause.booleanValue()) {
                    isPause = Boolean.valueOf(false);
                } else if (!isDestroy.booleanValue()) {
                    mHandler.sendEmptyMessage(WindowMessageID.HIDE_MENU);
                    playNextEpisode();
                }
            }
        });
        
        iVV.setOnInfoListener(new OnInfoListener() {
            public boolean onInfo(IMediaPlayer arg0, int what, int extra) {
                switch (what) {
                    case IjkMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        showLoading();
                        if (speedRunnable != null && mSpeedHandler != null) mSpeedHandler.post(speedRunnable);
                        break;
                    case IjkMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        hideLoading();
                        if (speedRunnable != null && mSpeedHandler != null) mSpeedHandler.removeCallbacks(speedRunnable);
                        if (progressLayout != null) progressLayout.hideSpeedInfo();
                        break;
                }
                return true;
            }
        });
    }
    
    private void setupAdBlock() {
        // 计算遮挡层尺寸
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int dpi = displayMetrics.densityDpi;
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        
        // 获取视频尺寸计算黑边
        int videoWidth = 0;
        int videoHeight = 0;
        if (iVV != null) {
            videoWidth = iVV.getVideoWidth();
            videoHeight = iVV.getVideoHeight();
        }
        
        int zdsd = 0; // 上下黑边高度
        int blackBarSize = 0; // 左右黑边宽度
        
        if (videoWidth > 0 && videoHeight > 0) {
            // 计算显示宽高比（考虑SAR）
            float specAspectRatio = (float) screenWidth / (float) screenHeight;
            float displayAspectRatio = (float) videoWidth / (float) videoHeight;
            if (iVV.getVideoSarNum() > 0 && iVV.getmVideoSarDen() > 0) {
                displayAspectRatio = displayAspectRatio * iVV.getVideoSarNum() / iVV.getmVideoSarDen();
            }
            
            // 计算实际显示尺寸
            int width, height;
            boolean shouldBeWider = displayAspectRatio > specAspectRatio;
            if (shouldBeWider) {
                width = screenWidth;
                height = (int) (width / displayAspectRatio);
            } else {
                height = screenHeight;
                width = (int) (height * displayAspectRatio);
            }
            
            double pmzhb = (double) width / height; // 视频纵横比
            double zdspgd = screenWidth / pmzhb; // 最大视频高度
            zdsd = (int) (screenHeight - zdspgd) / 2; // 黑边高度
            if (zdsd < 0) zdsd = 0;
            
            double scaledVideoWidth = Math.min(screenWidth, screenHeight * pmzhb);
            int extraSpace = screenWidth - (int) scaledVideoWidth;
            blackBarSize = extraSpace / 2;
            if (blackBarSize < 0) blackBarSize = 0;
        }
        
        // 设置右侧二维码遮挡尺寸
        if (Ad_block_right != null) {
            int ewmwidth = blackBarSize + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EwmWidth, displayMetrics);
            int ewmheight = zdsd + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EwmHeight, displayMetrics);
            ViewGroup.LayoutParams rightParams = Ad_block_right.getLayoutParams();
            rightParams.width = ewmwidth;
            rightParams.height = ewmheight;
            Ad_block_right.setLayoutParams(rightParams);
        }
        
        // 设置上下遮挡尺寸
        int blockHeight;
        if (vodtype != null && vodtype.equals("MOVIE")) {
            if (dpi > 240) {
                blockHeight = zdsd + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Moviesize, displayMetrics) / 2;
            } else {
                blockHeight = zdsd + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Moviesize, displayMetrics);
            }
        } else {
            if (dpi > 240) {
                blockHeight = zdsd + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Tvplaysize, displayMetrics) / 2;
            } else {
                blockHeight = zdsd + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Tvplaysize, displayMetrics);
            }
        }
        
        if (Ad_block_up != null) {
            ViewGroup.LayoutParams upParams = Ad_block_up.getLayoutParams();
            upParams.height = blockHeight;
            Ad_block_up.setLayoutParams(upParams);
        }
        if (Ad_block_down != null) {
            ViewGroup.LayoutParams downParams = Ad_block_down.getLayoutParams();
            downParams.height = blockHeight;
            Ad_block_down.setLayoutParams(downParams);
        }
        
        // 设置可见性
        if (position == 0) {
            if (Ad_block_up != null) Ad_block_up.setVisibility(View.VISIBLE);
            if (Ad_block_down != null) Ad_block_down.setVisibility(View.GONE);
            if (Ad_block_right != null) Ad_block_right.setVisibility(View.GONE);
        } else if (position == 1) {
            if (Ad_block_down != null) Ad_block_down.setVisibility(View.VISIBLE);
            if (Ad_block_up != null) Ad_block_up.setVisibility(View.GONE);
            if (Ad_block_right != null) Ad_block_right.setVisibility(View.GONE);
        } else if (position == 2) {
            if (Ad_block_up != null) Ad_block_up.setVisibility(View.VISIBLE);
            if (Ad_block_down != null) Ad_block_down.setVisibility(View.VISIBLE);
            if (Ad_block_right != null) Ad_block_right.setVisibility(View.GONE);
        } else if (position == 3) {
            if (Ad_block_up != null) Ad_block_up.setVisibility(View.VISIBLE);
            if (Ad_block_down != null) Ad_block_down.setVisibility(View.VISIBLE);
            if (Ad_block_right != null) Ad_block_right.setVisibility(View.VISIBLE);
        } else if (position == 4) {
            if (Ad_block_up != null) Ad_block_up.setVisibility(View.VISIBLE);
            if (Ad_block_right != null) Ad_block_right.setVisibility(View.VISIBLE);
            if (Ad_block_down != null) Ad_block_down.setVisibility(View.GONE);
        } else if (position == 5) {
            if (Ad_block_down != null) Ad_block_down.setVisibility(View.VISIBLE);
            if (Ad_block_right != null) Ad_block_right.setVisibility(View.VISIBLE);
            if (Ad_block_up != null) Ad_block_up.setVisibility(View.GONE);
        } else if (position == 6) {
            if (Ad_block_down != null) Ad_block_down.setVisibility(View.GONE);
            if (Ad_block_right != null) Ad_block_right.setVisibility(View.VISIBLE);
            if (Ad_block_up != null) Ad_block_up.setVisibility(View.GONE);
        }
        
        // 调试模式下显示半透明背景便于查看遮挡区域
        if (Play_timeout_debug == 1) {
            if (Ad_block_up != null) Ad_block_up.setBackgroundResource(R.drawable.button_normal_bga);
            if (Ad_block_down != null) Ad_block_down.setBackgroundResource(R.drawable.button_normal_bga);
            if (Ad_block_right != null) Ad_block_right.setBackgroundResource(R.drawable.button_normal_bga);
        }
        
        Logger.d(TAG, "setupAdBlock: position=" + position + ", blockHeight=" + blockHeight + ", zdsd=" + zdsd);
    }
    
    private void playNextEpisode() {
        if (videoInfo != null && videoInfo.size() > playIndex + 1) {
            Numbermax = 0;
            playIndex = playIndex + 1;
            xjposition = playIndex;
            collectionTime = 0;
            mLastPos = 0;
            mLastPos2 = 0; // 重置备用播放位置
            PlayersNumber = currentSourceIndex + 1;
            currentPosition = 0;
            seizing = 0;
            currentEpisodeIndex = playIndex;
            // 切换剧集时重置弹幕状态
            if (danmakuManager != null) {
                danmakuManager.reset();
            }
            updateEpisodeSelection(playIndex);
            SelecteVod(playIndex);
            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
            if (iVV != null) iVV.start();
            Utils.showToast(VideoDetailsActivity.this, getString(R.string.Coming_soon) + vodname + "-" + videoInfo.get(playIndex).title, R.drawable.toast_smile);
        } else {
            if (iVV != null) iVV.stopPlayback();
            finish();
        }
    }
    
    private void playPreviousEpisode() {
        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        if (playIndex > 0) {
            Numbermax = 0;
            playIndex = playIndex - 1;
            collectionTime = 0;
            xjposition = playIndex;
            mLastPos = 0;
            mLastPos2 = 0; // 重置备用播放位置
            currentEpisodeIndex = playIndex;
            // 切换剧集时重置弹幕状态
            if (danmakuManager != null) {
                danmakuManager.reset();
            }
            updateEpisodeSelection(playIndex);
            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        }
    }
    
    private void setupGestureDetector() {
        mGestureDetector = new GestureDetector(new SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                // 双击暂停/播放
                if (iVV != null) {
                    if (iVV.isPlaying()) {
                        iVV.pause();
                        pauseDanmaku();
                    } else {
                        iVV.start();
                        resumeDanmaku();
                    }
                }
                return true;
            }
            
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isControllerShow) {
                    cancelDelayHide();
                    hideController();
                } else {
                    showController();
                    hideControllerDelay();
                }
                hideMenu();
                return true;
            }
            
            public void onLongPress(MotionEvent e) {
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                    setSpeed(2.0f, 0);
                }
            }
            
            public boolean onDown(MotionEvent e) {
                return super.onDown(e);
            }
            
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
            
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }
        });
    }

    private void showController() {
        if (iVV != null && isFullscreen) {
            // 先显示控制器，避免阻塞UI
            if (tv_time != null) tv_time.setText(Utils.getStringTime(":"));
            if (time_controler != null) {
                time_controler.setAnimationStyle(R.style.AnimationTimeFade);
                time_controler.showAtLocation(iVV, Gravity.TOP, 0, 0);
                time_controler.update(0, 0, screenWidth, controlHeight / 3);
            }
            if (controler != null) {
                controler.setAnimationStyle(R.style.AnimationFade);
                controler.showAtLocation(iVV, Gravity.BOTTOM, 0, 0);
                controler.update(0, 0, screenWidth, controlHeight / 2);
            }
            isControllerShow = true;
            mHandler.sendEmptyMessageDelayed(WindowMessageID.HIDE_CONTROLER, TIME);
            
            // 延迟暂停弹幕渲染
            if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isControllerShow && danmakuManager != null) {
                            pauseDanmaku();
                        }
                    }
                }, 50); // 延迟50ms，让控制器动画先执行
            }
        }
    }
    
    private void hideController() {
        if (controler != null && controler.isShowing()) {
            controler.dismiss();
        }
        if (time_controler != null && time_controler.isShowing()) {
            time_controler.dismiss();
        }
        isControllerShow = false;
        
        // 延迟恢复弹幕渲染
        if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isControllerShow && danmakuManager != null) {
                        resumeDanmaku();
                    }
                }
            }, 100); // 延迟100ms，让控制器关闭动画完成
        }
    }
    
    private void cancelDelayHide() {
        mHandler.removeMessages(WindowMessageID.HIDE_CONTROLER);
    }
    
    private void hideControllerDelay() {
        cancelDelayHide();
        mHandler.sendEmptyMessageDelayed(WindowMessageID.HIDE_CONTROLER, TIME);
    }
    
    private void showMenu() {
        if (menupopupWindow != null && isFullscreen) {
            // 先显示菜单，避免阻塞UI
            vmAdapter = new VodMenuAdapter(this, VideoPlayUtils.getData(0), 8, Boolean.valueOf(isMenuItemShow));
            menulist.setAdapter(vmAdapter);
            menupopupWindow.setAnimationStyle(R.style.AnimationMenu);
            menupopupWindow.showAtLocation(iVV, Gravity.TOP | Gravity.RIGHT, 0, 0);
            menupopupWindow.update(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_350), screenHeight);
            isMenuShow = true;
            isMenuItemShow = false;
            
            // 延迟暂停弹幕渲染
            if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isMenuShow && danmakuManager != null) {
                            pauseDanmaku();
                        }
                    }
                }, 50); // 延迟50ms，让菜单动画先执行
            }
        }
    }
    
    private void hideMenu() {
        if (Navigation_mode == 1) Navigation();
        if (menupopupWindow != null && menupopupWindow.isShowing()) {
            menupopupWindow.dismiss();
        }
        
        // 延迟恢复弹幕渲染
        if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isMenuShow && danmakuManager != null) {
                        resumeDanmaku();
                    }
                }
            }, 100); // 延迟100ms，让菜单关闭动画完成
        }
    }
    
    private void onMessage(Message msg) {
        if (msg == null) return;
        switch (msg.what) {
            case WindowMessageID.ERROR:
                if (ClientID < MaxClientID) {
                    setVideoUrl(Client + Integer.toString(ClientID + 1));
                } else {
                    if (Auto_Source == 1) {
                        Switchsource(1);
                    } else {
                        Switchsource(3);
                    }
                }
                return;
            case WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION:
                if (iVV == null) return;
                int currPosition = iVV.getCurrentPosition();
                videoLength = iVV.getDuration();
                updateTextViewWithTimeFormat(tv_currentTime, currPosition / 1000);
                updateTextViewWithTimeFormat(tv_totalTime, videoLength / 1000);
                if (seekBar != null) {
                    seekBar.setMax(videoLength / 1000);
                    seekBar.setProgress(currPosition / 1000);
                }
                mLastPos = currPosition;
                
                // 更新弹幕的播放位置
                if (danmakuManager != null) {
                    danmakuManager.updatePlayPosition(currPosition);
                }
                
                mHandler.sendEmptyMessageDelayed(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION, 200);
                return;
            case WindowMessageID.HIDE_CONTROLER:
                hideController();
                return;
            case WindowMessageID.HIDE_PROGRESS_TIME:
                if (tv_progress_time != null) tv_progress_time.setVisibility(View.GONE);
                return;
            case WindowMessageID.HIDE_MENU:
                hideMenu();
                return;
            case WindowMessageID.PLAY_ERROR:
                handlePlaybackError("播放失败");
                return;
            case WindowMessageID.PREPARE_TIMEOUT:
                handlePlaybackError("加载超时");
                return;
            case WindowMessageID.SWITCH_CODE:
                switchCode();
                isSwitch = false;
                return;
        }
    }
    
    /**
     * 处理播放错误，尝试换源
     * @param errorType 错误类型描述
     */
    private void handlePlaybackError(String errorType) {
        
        if (ClientID < MaxClientID) {
            Utils.showToast(this, errorType + "，正在尝试其他解析接口...", R.drawable.toast_err);
            setVideoUrl(Client + Integer.toString(ClientID + 1));
        } else {
            // 根据后端配置决定是否自动换源
            if (Auto_Source == 1) {
                // 后端开启了自动换源
                if (Number > 1) {
                    Utils.showToast(this, errorType + "，正在切换播放源...", R.drawable.toast_err);
                    Switchsource(1);
                } else {
                    Utils.showToast(this, errorType + "，当前只有一个播放源", R.drawable.toast_err);
                    mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                    Switchsource(3);
                }
            } else {
                // 后端关闭了自动换源，显示提示让用户手动切换
                Utils.showToast(this, errorType + "，请手动切换播放源", R.drawable.toast_err);
                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
            }
        }
    }
    
    private void Switchsource(int msg) {
        if (msg == 2) {
            Utils.showToast(this, R.string.str_no_data_error, R.drawable.toast_err);
            if (iVV != null) iVV.stopPlayback();
            finish();
        }
        if (msg == 3) {
            if (SharePreferenceDataUtil.getSharedIntData(this, Constant.fb, 0) == 0) {
                showUpdateDialog(getString(R.string.so_sorry) + vodname + "-" + videoInfo.get(playIndex).title + getString(R.string.abnormal), this);
                if (iVV != null) iVV.stopPlayback();
            } else {
                Utils.showToast(this, getString(R.string.so_sorry) + vodname + "-" + videoInfo.get(playIndex).title + getString(R.string.abnormal), R.drawable.toast_err);
                playNextEpisode();
                isCodeBlockExecuted = false;
                getFailreport(vodname, videoInfo.get(playIndex).title, domain);
            }
        }
        if (msg == 1) {
            
            if (Number > 1) {
                // 检查是否已经在使用这个播放源（避免重复换源）
                int nextPlayerNumber = PlayersNumber + 1;
                if (nextPlayerNumber > Number) {
                    nextPlayerNumber = 1;
                }
                
                // 如果下一个播放源编号小于等于当前编号，说明已经循环一圈了
                if (Numbermax > 0 && nextPlayerNumber <= PlayersNumber) {
                    Switchsource(3);
                    return;
                }
                
                Numbermax = Numbermax + 1;
                if (Numbermax >= Number) {
                    Switchsource(3);
                } else {
                    sourceId = Integer.toString(nextPlayerNumber);
                    PlayersNumber = nextPlayerNumber;
                    isCodeBlockExecuted = false; // 重置错误标志，允许新源再次触发错误处理
                    Utils.showToast(this, "正在切换到播放源 " + PlayersNumber, R.drawable.toast_smile);
                    PrepareVodDataa();
                }
            } else {
                Switchsource(3);
            }
        }
        if (msg == 4) {
            // 卡顿检测触发的换源（只有在 vod_caton_check=1 且 Auto_Source=1 时才会触发）
            seizing = 0;
            if (Number > 1) {
                Numbermax = Numbermax + 1;
                if (Numbermax >= Number) {
                    if (Play_timeout_debug == 1) {
                        Utils.showToast(this, "所有播放源均已尝试", R.drawable.toast_err);
                    }
                } else {
                    if (PlayersNumber + 1 > Number) {
                        sourceId = Integer.toString(1);
                        PlayersNumber = 1;
                    } else {
                        sourceId = Integer.toString(PlayersNumber + 1);
                        PlayersNumber++;
                    }
                    // 更新当前播放源索引
                    currentSourceIndex = PlayersNumber - 1;
                    
                    if (Play_timeout_debug == 1) {
                        Utils.showToast(this, "检测到卡顿，正在切换到播放源 " + PlayersNumber, R.drawable.toast_smile);
                    }
                    isCodeBlockExecuted = false; // 重置错误标志
                    PrepareVodDataa();
                }
            } else {
                if (Play_timeout_debug == 1) {
                    Utils.showToast(this, "当前只有一个播放源", R.drawable.toast_err);
                }
            }
        }
    }
    
    private void PrepareVodDataa() {
        
        // 打印所有播放源信息
        if (source != null) {
            for (int i = 0; i < source.size(); i++) {
                String sourceDomain = source.get(i).getType();
                List<VodUrlList> sourceList = source.get(i).getList();
                String firstUrl = (sourceList != null && sourceList.size() > 0) ? sourceList.get(0).getUrl() : "null";
            }
        }
        
        isCodeBlockExecuted = false; // 重置错误标志
        list = null;
        
        // 直接获取指定播放源（PlayersNumber从1开始，数组索引从0开始）
        if (source != null && PlayersNumber > 0 && PlayersNumber <= source.size()) {
            int sourceIndex = PlayersNumber - 1;
            domain = source.get(sourceIndex).getType();
            list = source.get(sourceIndex).getList();
        } else {
            Log.e(TAG, "播放源索引越界: PlayersNumber=" + PlayersNumber + ", source.size=" + (source != null ? source.size() : 0));
        }
        
        if (list == null) {
            Log.e(TAG, "list为空，无法换源");
            return;
        }
        
        // 更新播放源按钮文本和当前播放源索引
        
        if (btnSource != null && now_source != null && PlayersNumber > 0 && PlayersNumber <= now_source.size()) {
            String sourceName = now_source.get(PlayersNumber - 1).getName();
            btnSource.setText(sourceName);
            
            // 更新当前播放源索引（PlayersNumber从1开始，索引从0开始）
            currentSourceIndex = PlayersNumber - 1;
            
            // 重新构建播放源列表以更新高亮显示
            buildSourceList();
        } else {
            Log.w(TAG, "无法更新播放源UI，条件不满足");
            // 即使条件不满足，也要更新currentSourceIndex
            currentSourceIndex = PlayersNumber - 1;
            // 尝试重新构建列表
            if (sourceListContainer != null && source != null) {
                buildSourceList();
            }
        }
        
        // 立即保存播放源选择到数据库
        saveSourcePreference();
        
        ArrayList<VideoInfo> arrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            VideoInfo vinfo = new VideoInfo();
            vinfo.title = list.get(i).getTitle();
            vinfo.url = list.get(i).getUrl();
            arrayList.add(vinfo);
        }
        
        if (arrayList.size() >= playIndex + 1) {
            videoInfo = arrayList;
            SelecteVod(playIndex);
        } else {
            Log.e(TAG, "新播放源集数不足，继续换源");
            Switchsource(1);
        }
    }
    
    private void showUpdateDialog(String str, Context context) {
        if (isDialogShowing) return;
        // 检查Activity是否已销毁，避免BadTokenException
        if (isFinishing() || isDestroyed()) {
            return;
        }
        HomeDialog.Builder builder = new HomeDialog.Builder(context);
        builder.setTitle(R.string.Tips);
        builder.setMessage(str);
        builder.setPositiveButton(R.string.continues, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                playNextEpisode();
                dialog.dismiss();
                isDialogShowing = false;
            }
        });
        builder.setNeutralButton(R.string.forget_it, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
                isDialogShowing = false;
            }
        });
        builder.create().show();
        isDialogShowing = true;
        isCodeBlockExecuted = false;
        getFailreport(vodname, videoInfo.get(playIndex).title, domain);
    }
    
    private void getFailreport(String vodname, String episode, String line) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String account = sp.getString("userName", "");
        final String name = vodname;
        final String vodepisode = episode;
        final String vodline = line;
        String Fb_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.ot, ""), Constant.d);
        
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Fb_url,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {}
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {}
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("account", account);
                params.put("line", vodline);
                params.put("url", Fburl);
                params.put("vodname", name + "-" + vodepisode);
                return params;
            }
            
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VideoDetailsActivity.this, "Authorization", ""), Constant.d));
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 非全屏模式：检查是否在 videoFrame 上按确认键进入全屏
        if (!isFullscreen) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int keyCode = event.getKeyCode();
                View focusedView = getCurrentFocus();
                Log.d(TAG, "dispatchKeyEvent(小窗模式): keyCode=" + keyCode + ", focusedView=" + 
                    (focusedView != null ? focusedView.getClass().getSimpleName() + "@" + focusedView.getId() : "null"));
                
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || 
                    keyCode == KeyEvent.KEYCODE_ENTER ||
                    keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    // 检查当前焦点是否在 videoFrame 上
                    if (focusedView != null && focusedView.getId() == R.id.video_frame) {
                        Log.d(TAG, "dispatchKeyEvent: videoFrame focused, entering fullscreen");
                        enterFullscreen();
                        return true;
                    }
                }
            }
            return super.dispatchKeyEvent(event);
        }
        
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (isBack && iVV != null) {
                hideControllerDelay();
                iVV.seekTo(mLastPos);
                seekDanmaku(mLastPos);
                mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                isBack = false;
            }
            return super.dispatchKeyEvent(event);
        }
        
        int keyCode = event.getKeyCode();
        long secondTime;
        
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                hideController();
                exitFullscreen();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (playPreCode != 0) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
                    break;
                }
                secondTime = System.currentTimeMillis();
                if (secondTime - firstTime <= 2000) {
                    if (playIndex <= 0) {
                        Utils.showToast(this, R.string.vod_onpressed_play_frist, R.drawable.toast_shut);
                        break;
                    }
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        playIndex--;
                        Numbermax = 0;
                        xjposition = playIndex;
                        mLastPos = 0;
                        mLastPos2 = 0; // 重置播放位置，避免从旧进度开始
                        PlayersNumber = currentSourceIndex + 1;
                        currentPosition = 0;
                        seizing = 0;
                        currentEpisodeIndex = playIndex;
                        updateEpisodeSelection(playIndex);
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                    }
                    if (!isControllerShow) showController();
                    mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                    break;
                }
                Utils.showToast(this, R.string.vod_onpressed_play_last, R.drawable.toast_smile);
                firstTime = secondTime;
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (playPreCode != 0) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
                    break;
                }
                secondTime = System.currentTimeMillis();
                if (secondTime - firstTime <= 5000) {
                    if (videoInfo == null || videoInfo.size() <= playIndex + 1) {
                        Utils.showToast(this, R.string.finale, R.drawable.toast_shut);
                        break;
                    }
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        Numbermax = 0;
                        playIndex = playIndex + 1;
                        xjposition = playIndex;
                        mLastPos = 0;
                        mLastPos2 = 0; // 重置播放位置，避免从旧进度开始
                        PlayersNumber = currentSourceIndex + 1;
                        currentPosition = 0;
                        seizing = 0;
                        currentEpisodeIndex = playIndex;
                        updateEpisodeSelection(playIndex);
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                    }
                    if (!isControllerShow) showController();
                    mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                    break;
                }
                Utils.showToast(this, R.string.vod_onpressed_play_next, R.drawable.toast_smile);
                firstTime = secondTime;
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!isControllerShow) showController();
                rewind();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!isControllerShow) showController();
                fastForward();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE: // 空格键暂停/播放
                if (!isControllerShow) showController();
                if (iVV != null) {
                    if (!iVV.isPlaying()) {
                        iVV.start();
                        resumeDanmaku();
                        mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                        if (ib_playStatus != null) ib_playStatus.setImageResource(R.drawable.media_pause);
                    } else {
                        iVV.pause();
                        pauseDanmaku();
                        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                        if (ib_playStatus != null) ib_playStatus.setImageResource(R.drawable.media_playstatus);
                    }
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                hideController();
                showMenu();
                break;
        }
        return super.dispatchKeyEvent(event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isFullscreen) return super.onTouchEvent(event);
        
        boolean result = mGestureDetector.onTouchEvent(event);
        DisplayMetrics screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);
        if (mSurfaceYDisplayRange == 0) {
            mSurfaceYDisplayRange = Math.min(screen.widthPixels, screen.heightPixels);
        }
        float y_changed = event.getRawY() - mTouchY;
        float x_changed = event.getRawX() - mTouchX;
        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchAction = TOUCH_NONE;
                mTouchY = event.getRawY();
                mTouchX = event.getRawX();
                maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Lightness = Utils.GetLightness(this);
                break;
            case MotionEvent.ACTION_MOVE:
                if (coef > 2) {
                    if (mTouchX > (screenWidth / 2)) {
                        doVolumeTouch(y_changed);
                    }
                    if (mTouchX < (screenWidth / 2)) {
                        doBrightnessTouch(y_changed);
                    }
                }
                doSeekTouch(coef, xgesturesize, false);
                break;
            case MotionEvent.ACTION_UP:
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                    restoreSpeed();
                }
                doSeekTouch(coef, xgesturesize, true);
                break;
        }
        return true;
    }
    
    private void restoreSpeed() {
        switch (bsposition) {
            case 0: setSpeed(0.5f, 1); break;
            case 1: setSpeed(0.75f, 1); break;
            case 2: setSpeed(1.0f, 1); break;
            case 3: setSpeed(1.25f, 1); break;
            case 4: setSpeed(1.5f, 1); break;
            case 5: setSpeed(2.0f, 1); break;
            case 6: setSpeed(3.0f, 1); break;
            case 7: setSpeed(4.0f, 1); break;
            case 8: setSpeed(5.0f, 1); break;
        }
    }
    
    private void doVolumeTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME) return;
        mTouchAction = TOUCH_VOLUME;
        int delta = -(int) ((y_changed / mSurfaceYDisplayRange) * maxVolume);
        int vol = (int) Math.min(Math.max(currentVolume + delta, 0), maxVolume);
        if (delta != 0) {
            if (vol < 1) {
                showVolumeToast(R.drawable.mv_ic_volume_mute, maxVolume, vol, true);
            } else if (vol >= 1 && vol < maxVolume / 2) {
                showVolumeToast(R.drawable.mv_ic_volume_low, maxVolume, vol, true);
            } else if (vol >= maxVolume / 2) {
                showVolumeToast(R.drawable.mv_ic_volume_high, maxVolume, vol, true);
            }
        }
    }
    
    private void doBrightnessTouch(float y_changed) {
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS) return;
        mTouchAction = TOUCH_BRIGHTNESS;
        float delta = -y_changed / mSurfaceYDisplayRange * 2f;
        int vol = (int) ((Math.min(Math.max(Lightness + delta, 0.01f) * 255, 255)));
        if (delta != 0) {
            if (vol < 5) {
                showVolumeToast(R.drawable.mv_ic_brightness, 255, 0, false);
            } else {
                showVolumeToast(R.drawable.mv_ic_brightness, 255, vol, false);
            }
        }
    }
    
    private void doSeekTouch(float coef, float gesturesize, boolean seek) {
        if (((double) coef) <= 0.5d && Math.abs(gesturesize) >= 1.0f) {
            if (mTouchAction == TOUCH_NONE || mTouchAction == TOUCH_SEEK) {
                mTouchAction = TOUCH_SEEK;
                if (iVV == null) return;
                int time = iVV.getCurrentPosition() / 1000;
                int jump = (int) ((((double) Math.signum(gesturesize)) * ((600000.0d * Math.pow(gesturesize / 8.0f, 4.0d)) + 3000.0d)) / 1000.0d);
                if (jump > 0 && time + jump > videoLength) jump = videoLength - time;
                if (jump < 0 && time + jump < 0) jump = -time;
                if (videoLength > 0) {
                    if (tv_progress_time != null) {
                        tv_progress_time.setVisibility(View.VISIBLE);
                        updateTextViewWithTimeFormat(tv_progress_time, time + jump);
                    }
                    mHandler.removeMessages(WindowMessageID.HIDE_PROGRESS_TIME);
                    mHandler.sendEmptyMessageDelayed(WindowMessageID.HIDE_PROGRESS_TIME, 2000);
                    if (seek) {
                        if ((time + jump) * 1000 > videoLength) {
                            playNextEpisode();
                        } else {
                            iVV.seekTo((time + jump) * 1000);
                            seekDanmaku((time + jump) * 1000);
                            mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                        }
                    }
                }
            }
        }
    }
    
    private void showVolumeToast(int resId, int max, int current, Boolean isVolume) {
        if (isVolume.booleanValue()) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, current, 0);
        } else {
            Utils.SetLightness(this, current);
        }
        View view;
        ImageView center_image;
        ProgressBar center_progress;
        if (mToast == null) {
            mToast = new Toast(this);
            view = LayoutInflater.from(this).inflate(R.layout.mv_media_volume_controler, null);
            center_image = view.findViewById(R.id.center_image);
            center_progress = view.findViewById(R.id.center_progress);
            center_progress.setMax(max);
            center_progress.setProgress(current);
            center_image.setImageResource(resId);
            mToast.setView(view);
        } else {
            view = mToast.getView();
            center_image = view.findViewById(R.id.center_image);
            center_progress = view.findViewById(R.id.center_progress);
            center_progress.setMax(max);
            center_progress.setProgress(current);
            center_image.setImageResource(resId);
        }
        mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }
    
    private void fastForward() {
        if ((videoLength / 1000) - (mLastPos / 1000) > 30) {
            mLastPos += 30000;
        } else {
            mLastPos = videoLength - 10000;
        }
        isBack = true;
        cancelDelayHide();
        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        if (seekBar != null) seekBar.setProgress(mLastPos / 1000);
        showLoading();
    }
    
    private void rewind() {
        if (mLastPos > 30000) {
            mLastPos -= 30000;
        } else {
            mLastPos = 0;
        }
        isBack = true;
        cancelDelayHide();
        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        if (seekBar != null) seekBar.setProgress(mLastPos / 1000);
        showLoading();
    }
    
    private void ResetMovieTime() {
        updateTextViewWithTimeFormat(tv_currentTime, 0);
        updateTextViewWithTimeFormat(tv_totalTime, 0);
        if (seekBar != null) seekBar.setProgress(0);
    }
    
    private void updateTextViewWithTimeFormat(TextView view, int second) {
        if (view == null) return;
        int mm = (second % 3600) / 60;
        int ss = second % 60;
        view.setText(String.format("%02d:%02d:%02d", Integer.valueOf(second / 3600), Integer.valueOf(mm), Integer.valueOf(ss)));
    }
    
    private void showLoading() {
        if (progressLayout != null) {
            progressLayout.showProgress();
            if (progressLayout.getParent() instanceof ViewGroup) {
                final ViewGroup parent = (ViewGroup) progressLayout.getParent();
                if (tv_progress_time != null && tv_progress_time.getParent() == parent) {
                    tv_progress_time.setVisibility(View.GONE);
                }
                parent.bringChildToFront(progressLayout);
                progressLayout.bringToFront();
                if (tv_progress_time != null && tv_progress_time.getParent() == parent) {
                    int progressIndex = parent.indexOfChild(progressLayout);
                    int timeIndex = parent.indexOfChild(tv_progress_time);
                    if (progressIndex >= 0 && timeIndex >= 0 && timeIndex < progressIndex) {
                        parent.removeViewAt(timeIndex);
                        parent.addView(tv_progress_time, progressIndex + 1);
                    }
                }
                progressLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        parent.bringChildToFront(progressLayout);
                        progressLayout.bringToFront();
                        if (tv_progress_time != null && tv_progress_time.getParent() == parent) {
                            int progressIndex = parent.indexOfChild(progressLayout);
                            int timeIndex = parent.indexOfChild(tv_progress_time);
                            if (progressIndex >= 0 && timeIndex >= 0 && timeIndex < progressIndex) {
                                parent.removeViewAt(timeIndex);
                                parent.addView(tv_progress_time, progressIndex + 1);
                            }
                            tv_progress_time.setVisibility(View.GONE);
                        }
                        parent.invalidate();
                        progressLayout.invalidate();
                    }
                });
            }
        }
        View loadingLayout = findViewById(R.id.loading_layout);
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.GONE);
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }
    
    private void hideLoading() {
        if (progressLayout != null) {
            progressLayout.showContent();
        }
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        View loadingLayout = findViewById(R.id.loading_layout);
        if (loadingLayout != null) {
            loadingLayout.setVisibility(View.GONE);
        }
        if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
            resumeDanmaku();
        }
    }
    
    private void updateResolutionDisplay() {
        if (iVV == null) return;
        
        // 延迟获取分辨率，确保视频信息已加载
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (iVV == null) return;
                
                int videoWidth = iVV.getVideoWidth();
                int videoHeight = iVV.getVideoHeight();
                
                Logger.d(TAG, "视频分辨率: " + videoWidth + "x" + videoHeight);
                
                if (videoWidth > 0 && videoHeight > 0) {
                    final String resolutionText = videoWidth + "x" + videoHeight;
                    
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tv_resolution != null) {
                                tv_resolution.setText(resolutionText);
                                tv_resolution.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    // 如果还没获取到分辨率，再延迟重试
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (iVV == null) return;
                            int w = iVV.getVideoWidth();
                            int h = iVV.getVideoHeight();
                            if (w > 0 && h > 0 && tv_resolution != null) {
                                tv_resolution.setText(w + "x" + h);
                                tv_resolution.setVisibility(View.VISIBLE);
                            }
                        }
                    }, 1000);
                }
            }
        }, 500);
    }
    
    private void setButtonDrawableSize(Button button, int drawableResId, int size) {
        if (button == null) return;
        android.graphics.drawable.Drawable drawable = getResources().getDrawable(drawableResId);
        if (drawable != null) {
            drawable.setBounds(0, 0, size, size);
            button.setCompoundDrawables(drawable, null, null, null);
        }
    }
    
    private void setButtonDrawableRight(Button button, int drawableResId, int size) {
        if (button == null) return;
        android.graphics.drawable.Drawable drawable = getResources().getDrawable(drawableResId);
        if (drawable != null) {
            drawable.setBounds(0, 0, size, size);
            button.setCompoundDrawables(null, null, drawable, null);
        }
    }

    public void onCreateMenu() {
        View menuView = View.inflate(this, R.layout.mv_controler_menu, null);
        menulist = menuView.findViewById(R.id.media_controler_menu);
        menupopupWindow = new PopupWindow(menuView, -2, -2);
        menupopupWindow.setOutsideTouchable(true);
        menupopupWindow.setTouchable(true);
        menupopupWindow.setFocusable(true);
        menulist.setOnItemClickListener(new OnItemClickListener() {
            @SuppressLint("WrongConstant")
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                handleMenuItemClick(position);
            }
        });
        menulist.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != 1) {
                    if (keyCode == 4) {
                        if (!isMenuItemShow) {
                            if (isMenuShow) {
                                menupopupWindow.dismiss();
                            }
                        } else {
                            // 弹幕子菜单返回到弹幕设置主菜单
                            if (menutype >= 9 && menutype <= 13) {
                                menutype = 8;
                                menulist.setAdapter(new VodMenuAdapter(VideoDetailsActivity.this, VideoPlayUtils.getData(8), 8, Boolean.valueOf(isMenuItemShow)));
                                menulist.setSelection(dmSubMenuType);
                                return true;
                            }
                            isMenuShow = true;
                            isMenuItemShow = false;
                            menulist.setAdapter(new VodMenuAdapter(VideoDetailsActivity.this, VideoPlayUtils.getData(0), 8, Boolean.valueOf(isMenuItemShow)));
                        }
                    }
                }
                return false;
            }
        });
    }
    
    private void handleMenuItemClick(int position) {
        if (isMenuShow) {
            isMenuShow = false;
            isMenuItemShow = true;
            switch (position) {
                case 0: menutype = 0; menulist.setAdapter(new VodMenuAdapter(this, videoInfo, 0, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(xjposition); return;
                case 1: menutype = 1; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(1), 1, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(bsposition); return;
                case 2: menutype = 2; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(2), 2, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(jmposition); return;
                case 3: menutype = 3; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(3), 3, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(hmblposition); return;
                case 4: menutype = 4; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(4), 4, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(phszposition); return;
                case 5: menutype = 5; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(5), 5, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(ptposition); return;
                case 6: menutype = 6; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(6), 6, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(pwposition); return;
                case 7: menutype = 7; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(7), 7, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(nhposition); return;
                case 8: menutype = 8; menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(8), 8, Boolean.valueOf(isMenuItemShow))); menulist.setSelection(0); return; // 弹幕设置
            }
        } else if (isMenuItemShow) {
            handleMenuSelection(position);
        }
    }
    
    private void handleMenuSelection(int position) {
        Editor editor;
        switch (menutype) {
            case 0: // 选集
                if (videoInfo != null && videoInfo.size() > position) {
                    isNext = Boolean.valueOf(true);
                    isSwitch = true;
                    Numbermax = 0;
                    playIndex = position;
                    PlayersNumber = currentSourceIndex + 1;
                    currentPosition = 0;
                    seizing = 0;
                    currentEpisodeIndex = position;
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                    }
                    if (!isControllerShow) showController();
                    mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                }
                xjposition = position;
                updateEpisodeSelection(position);
                hideMenu();
                return;
            case 1: // 倍速
                bsposition = position;
                float[] speeds = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f, 4.0f, 5.0f};
                if (position < speeds.length) setSpeed(speeds[position], 0);
                hideMenu();
                return;
            case 2: // 解码
                jmposition = position;
                editor = sp.edit();
                if (position == 0) {
                    editor.putInt("mIsHwDecode", 0);
                    editor.putString(Constant.mg, "软解码");
                } else {
                    editor.putInt("mIsHwDecode", 1);
                    editor.putString(Constant.mg, "硬解码");
                }
                editor.commit();
                setDecode();
                if (iVV != null) iVV.setDecode(Boolean.valueOf(position == 1));
                isPause = Boolean.valueOf(true);
                if (iVV != null && iVV.isPlaying()) mLastPos = iVV.getCurrentPosition();
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                    mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                    if (iVV != null) { iVV.resume(); iVV.seekTo(mLastPos); seekDanmaku(mLastPos); }
                }
                hideMenu();
                return;
            case 3: // 画面比例
                hmblposition = position;
                selectScales(hmblposition);
                editor = sp.edit();
                String[] ratios = {"原始比例", "4:3 缩放", "16:9缩放", "全屏拉伸", "等比缩放", "全屏裁剪"};
                if (position < ratios.length) editor.putString(Constant.oh, ratios[position]);
                editor.commit();
                hideMenu();
                return;
            case 4: // 偏好设置
                phszposition = position;
                editor = sp.edit();
                editor.putInt("playPre", position);
                editor.commit();
                getPlayPreferences();
                hideMenu();
                return;
            case 5: // 跳过片头
                String[] jumpTimes = {"0秒", "10秒", "15秒", "20秒", "30秒", "60秒", "90秒", "120秒", "150秒", "180秒", "240秒", "300秒"};
                int[] jumpValues = {0, 10, 15, 20, 30, 60, 90, 120, 150, 180, 240, 300};
                ptposition = position;
                editor = sp.edit();
                if (position < jumpTimes.length) editor.putString("play_jump", jumpTimes[position]);
                editor.commit();
                if (position < jumpValues.length) setJump(jumpValues[position]);
                hideMenu();
                return;
            case 6: // 跳过片尾
                String[] jumpEndStrings = {"0秒", "10秒", "15秒", "20秒", "30秒", "60秒", "90秒", "120秒", "150秒", "180秒", "240秒", "300秒"};
                int[] jumpEndValues = {0, 10, 15, 20, 30, 60, 90, 120, 150, 180, 240, 300};
                pwposition = position;
                editor = sp.edit();
                if (position < jumpEndStrings.length) editor.putString("play_jump_end", jumpEndStrings[position]);
                editor.commit();
                if (position < jumpEndValues.length) setJump_end(jumpEndValues[position]);
                hideMenu();
                return;
            case 7: // 内核
                nhposition = position;
                String[] cores = {"自动", "系统", "IJK", "EXO", "阿里"};
                editor = sp.edit();
                if (position < cores.length) editor.putString(Constant.hd, cores[position]);
                editor.commit();
                currentCoreIndex = position;
                if (btnPlayerCore != null && position < playerCores.length) btnPlayerCore.setText(playerCores[position]);
                buildCoreList();
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                    SelecteVod(playIndex);
                    mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                }
                hideMenu();
                return;
            case 8: // 弹幕设置主菜单
                dmSubMenuType = position;
                final int subMenuSelection;
                switch (position) {
                    case 0: // 弹幕开关
                        menutype = 9;
                        subMenuSelection = dmSwitchPosition;
                        break;
                    case 1: // 弹幕大小
                        menutype = 10;
                        subMenuSelection = dmSizePosition;
                        break;
                    case 2: // 弹幕速度
                        menutype = 11;
                        subMenuSelection = dmSpeedPosition;
                        break;
                    case 3: // 弹幕行数
                        menutype = 12;
                        subMenuSelection = dmLinePosition;
                        break;
                    case 4: // 弹幕透明
                        menutype = 13;
                        subMenuSelection = dmAlphaPosition;
                        break;
                    default:
                        subMenuSelection = 0;
                        break;
                }
                menulist.setAdapter(new VodMenuAdapter(this, VideoPlayUtils.getData(menutype), menutype, Boolean.valueOf(isMenuItemShow)));
                // 延迟设置选中位置，确保adapter已加载完成
                menulist.post(new Runnable() {
                    @Override
                    public void run() {
                        menulist.setSelection(subMenuSelection);
                        menulist.requestFocusFromTouch();
                    }
                });
                return;
            case 9: // 弹幕开关
                dmSwitchPosition = position;
                if (position == 0) {
                    hideDanmaku();
                    Utils.showToast(this, "弹幕已关闭", R.drawable.toast_smile);
                } else {
                    showDanmaku();
                    Utils.showToast(this, "弹幕已开启", R.drawable.toast_smile);
                }
                hideMenu();
                return;
            case 10: // 弹幕大小
                dmSizePosition = position;
                float[] sizes = {0.6f, 0.8f, 1.0f, 1.2f, 1.5f};
                if (danmakuManager != null && position < sizes.length) {
                    danmakuManager.setTextSize(sizes[position]);
                }
                String[] sizeNames = {"极小", "较小", "标准", "较大", "极大"};
                if (position < sizeNames.length) Utils.showToast(this, "弹幕大小: " + sizeNames[position], R.drawable.toast_smile);
                hideMenu();
                return;
            case 11: // 弹幕速度
                dmSpeedPosition = position;
                float[] dmSpeeds = {0.5f, 0.75f, 1.0f, 1.5f, 2.0f};
                if (danmakuManager != null && position < dmSpeeds.length) {
                    danmakuManager.setSpeed(dmSpeeds[position]);
                }
                String[] speedNames = {"极慢", "较慢", "标准", "较快", "极快"};
                if (position < speedNames.length) Utils.showToast(this, "弹幕速度: " + speedNames[position], R.drawable.toast_smile);
                hideMenu();
                return;
            case 12: // 弹幕行数
                dmLinePosition = position;
                int[] lines = {3, 5, 8, 10, -1}; // -1表示不限
                if (danmakuManager != null && position < lines.length) {
                    danmakuManager.setMaxLines(lines[position]);
                }
                String[] lineNames = {"3行", "5行", "8行", "10行", "不限"};
                if (position < lineNames.length) Utils.showToast(this, "弹幕行数: " + lineNames[position], R.drawable.toast_smile);
                hideMenu();
                return;
            case 13: // 弹幕透明
                dmAlphaPosition = position;
                float[] alphas = {1.0f, 0.8f, 0.6f, 0.4f, 0.2f};
                if (danmakuManager != null && position < alphas.length) {
                    danmakuManager.setAlpha(alphas[position]);
                }
                String[] alphaNames = {"100%", "80%", "60%", "40%", "20%"};
                if (position < alphaNames.length) Utils.showToast(this, "弹幕透明度: " + alphaNames[position], R.drawable.toast_smile);
                hideMenu();
                return;
        }
    }
    
    private void selectScales(int paramInt) {
        if (iVV == null) return;
        restoreSpeed();
        switch (paramInt) {
            case 0: iVV.toggleAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT); break;
            case 1: iVV.toggleAspectRatio(IRenderView.AR_4_3_FIT_PARENT); break;
            case 2: iVV.toggleAspectRatio(IRenderView.AR_16_9_FIT_PARENT); break;
            case 3: iVV.toggleAspectRatio(IRenderView.AR_MATCH_PARENT); break;
            case 4: iVV.toggleAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT); break;
            case 5: iVV.toggleAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT); break;
        }
    }
    
    private void setSpeed(float speed, int source) {
        if (iVV == null) return;
        if (source == 1) {
            iVV.setSpeed(speed);
            if (speed != 1.0) return;
        } else {
            float Speed;
            if (nhposition == 2 || nhposition == 0) {
                if (speed > 2.0f) {
                    iVV.setSpeed(2.0f);
                    bsposition = 5;
                    Speed = 2;
                } else {
                    iVV.setSpeed(speed);
                    Speed = speed;
                }
            } else {
                iVV.setSpeed(speed);
                Speed = speed;
            }
            if (speed != 1.0) {
                Utils.showToast(this, getString(R.string.have) + Speed + getString(R.string.speed), R.drawable.toast_smile);
            }
        }
    }
    
    private void setJump(int speed) {
        if (speed == 0) {
            Utils.showToast(this, R.string.cancellationpt, R.drawable.toast_smile);
        }
        jump_time = Integer.toString(speed);
    }
    
    private void setJump_end(int speed) {
        if (speed == 0) {
            Utils.showToast(this, R.string.cancellationpw, R.drawable.toast_smile);
        }
        jump_time_end = Integer.toString(speed);
    }
    
    private void updateprogress(int progress) {
        if (Navigation_mode == 1) Navigation();
        int Videoduration = videoLength / 1000;
        int sum1 = Integer.parseInt(jump_time);
        int sum2 = Integer.parseInt(jump_time_end);
        int sum3 = sum1 + sum2 + 30;
        
        if (vipstate == 1 && progress >= Trytime * 60) {
            if (iVV != null) iVV.stopPlayback();
            mediaHandler.sendEmptyMessageDelayed(WindowMessageID.Try, 500);
        }
        
        if (mLastPos2 > 0 && iVV != null) {
            iVV.seekTo(mLastPos2);
            // 同步弹幕位置
            seekDanmaku(mLastPos2);
            mLastPos2 = 0;
        } else {
        }
        
        if (Videoduration > sum3) {
            if (Videoduration > sum1 && !jump_time.equals("0") && progress < sum1) {
                if (mLastPos / 1000 != 0) {
                    if (ptpositions) return;
                    ptpositions = true;
                    Utils.showToast(this, vodname + "-" + videoInfo.get(playIndex).title + getString(R.string.titles) + jump_time + getString(R.string.seconds), R.drawable.toast_smile);
                    if (iVV != null) iVV.seekTo(sum1 * 1000);
                    // 同步弹幕位置（跳过片头）
                    seekDanmaku(sum1 * 1000);
                }
            }
            if (Videoduration > sum2 && !jump_time_end.equals("0") && progress > Videoduration - sum2) {
                Utils.showToast(this, vodname + "-" + videoInfo.get(playIndex).title + getString(R.string.trailer) + jump_time_end + getString(R.string.seconds), R.drawable.toast_smile);
                playNextEpisode();
            }
        }
    }
    
    private void getJumpdata() {
        switch (jump_time) {
            case "0": ptposition = 0; break;
            case "10": ptposition = 1; break;
            case "15": ptposition = 2; break;
            case "20": ptposition = 3; break;
            case "30": ptposition = 4; break;
            case "60": ptposition = 5; break;
            case "90": ptposition = 6; break;
            case "120": ptposition = 7; break;
            case "150": ptposition = 8; break;
            case "180": ptposition = 9; break;
            case "240": ptposition = 10; break;
            case "300": ptposition = 11; break;
            default: ptposition = 0; break;
        }
        switch (jump_time_end) {
            case "0": pwposition = 0; break;
            case "10": pwposition = 1; break;
            case "15": pwposition = 2; break;
            case "20": pwposition = 3; break;
            case "30": pwposition = 4; break;
            case "60": pwposition = 5; break;
            case "90": pwposition = 6; break;
            case "120": pwposition = 7; break;
            case "150": pwposition = 8; break;
            case "180": pwposition = 9; break;
            case "240": pwposition = 10; break;
            case "300": pwposition = 11; break;
            default: pwposition = 0; break;
        }
    }
    
    private void setDecode() {
        if (sp.getInt("mIsHwDecode", 1) == 0) {
            mIsHwDecode = false;
            jmposition = 0;
            return;
        }
        mIsHwDecode = true;
        jmposition = 1;
    }
    
    private void getPlayPreferences() {
        playPreCode = sp.getInt("playPre", 0);
        phszposition = playPreCode == 0 ? 0 : 1;
    }
    
    private void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
        controlHeight = screenHeight / 4;
    }
    
    private void switchCode() {
        int Decode = sp.getInt("mIsHwDecode", 1);
        if (Decode == 1) { mIsHwDecode = true; jmposition = 1; }
        else { mIsHwDecode = false; jmposition = 0; }
        if (iVV != null) iVV.setDecode(Boolean.valueOf(Decode == 1));
        isPause = Boolean.valueOf(true);
        if (iVV != null) iVV.resume();
        xjposition = playIndex;
        collectionTime = 0;
        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
    }
    
    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, this.getClass().getCanonicalName());
            mWakeLock.acquire();
        }
    }
    
    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }
    
    private void Navigation() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
    
    private void getVodGongGao() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""), Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""), Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""), Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""), Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""), Constant.d);
        final int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=vod_notice",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        VodGongGaoResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                VodGongGaoError(error);
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
                    rc4data = AES.encrypt_Aes(AESKEY, codedata, AESIV);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VideoDetailsActivity.this, "Authorization", ""), Constant.d));
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }
    
    public void VodGongGaoResponse(String response) {
        Log.d(TAG, "========== 播放器公告调试 ==========");
        Log.d(TAG, "公告原始响应: " + response);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""), Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""), Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""), Constant.d);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            String msg = jSONObject.optString("msg");
            Log.d(TAG, "公告code: " + code + ", msg: " + msg);
            if (code == 200) {
                if (tv_notice_root != null) tv_notice_root.setVisibility(View.VISIBLE);
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                Log.d(TAG, "加密类型miType: " + miType);
                if (tv_notice != null) {
                    String decryptedMsg = "";
                    if (miType == 1) {
                        decryptedMsg = URLDecoder.decode(Rc4.decry_RC4(msg, RC4KEY), "UTF-8");
                    } else if (miType == 2) {
                        decryptedMsg = URLDecoder.decode(Rsa.decrypt_Rsa(msg, RSAKEY), "UTF-8");
                    } else if (miType == 3) {
                        decryptedMsg = URLDecoder.decode(AES.decrypt_Aes(AESKEY, msg, AESIV), "UTF-8");
                    }
                    Log.d(TAG, "解密后公告内容: " + decryptedMsg);
                    tv_notice.setText(decryptedMsg);
                }
                mediaHandler.sendEmptyMessage(WindowMessageID.NOTICE);
            } else {
                Log.d(TAG, "公告获取失败，code不是200");
                if (tv_notice_root != null) tv_notice_root.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "公告解析异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void VodGongGaoError(VolleyError volleyError) {
        if (tv_notice_root != null) tv_notice_root.setVisibility(View.GONE);
    }
    
    private void vodlogoloadImg() {
        if (tv_logo != null && logo_url != null) {
            tv_logo.setVisibility(View.VISIBLE);
            Glide.with(this).load(logo_url).into(tv_logo);
        }
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_playStatus:
                if (!isControllerShow) showController();
                if (iVV != null) {
                    if (iVV.isPlaying()) {
                        iVV.pause();
                        pauseDanmaku();
                        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                        if (ib_playStatus != null) ib_playStatus.setImageResource(R.drawable.media_playstatus);
                    } else {
                        iVV.start();
                        resumeDanmaku();
                        mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                        if (ib_playStatus != null) ib_playStatus.setImageResource(R.drawable.media_pause);
                    }
                }
                break;
        }
    }
    
    class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WindowMessageID.EVENT_PLAY:
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        synchronized (SYNC_Playing) {
                            try { SYNC_Playing.wait(); } catch (InterruptedException e) { e.printStackTrace(); }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setVideoUrl(Client);
                        }
                    });
                    return;
                case WindowMessageID.SUCCESS:
                    if (Adblock == 1 && headposition != 0) {
                        if (headposition * 1000 > mLastPos && iVV != null) {
                            iVV.seekTo(headposition * 1000);
                            seekDanmaku(headposition * 1000);
                        } else if (iVV != null) {
                            iVV.seekTo(mLastPos);
                            seekDanmaku(mLastPos);
                        }
                    }
                    if (seizings == 1 && iVV != null) {
                        iVV.seekTo((int) currentPosition);
                        seekDanmaku((int) currentPosition);
                    }
                    if (mLastPos > 0 && vodtype != null && !vodtype.equals("LIVE")) {
                        mLastPos2 = mLastPos;
                        mLastPos = 0;
                    }
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        if (new Date(System.currentTimeMillis()).getTime() > format.parse(GetTimeStamp.timeStamp2Date(time, "")).getTime()) {
                            vipstate = 1;
                        } else {
                            vipstate = 0;
                        }
                    } catch (ParseException e) { e.printStackTrace(); }
                    if (time != null && time.equals("999999999")) vipstate = 0;
                    if (iVV != null) iVV.start();
                    mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARING;
                    isPlaying = true;
                    long secondTime = System.currentTimeMillis();
                    Album album = new Album();
                    album.setAlbumId(videoId);
                    album.setAlbumSourceType(sourceId);
                    album.setCollectionTime(collectionTime);
                    album.setPlayIndex(playIndex);
                    album.setAlbumPic(albumPic);
                    album.setAlbumType(vodtype);
                    album.setAlbumTitle(vodname);
                    album.setAlbumState("观看时间:未知");
                    album.setNextLink(nextlink);
                    album.setTime(String.valueOf(secondTime));
                    album.setTypeId(2);
                    dao.addAlbums(album);
                    return;
            }
        }
    }
    
    private enum PLAYER_STATUS {
        PLAYER_IDLE,
        PLAYER_PREPARING,
        PLAYER_PREPARED,
        PLAYER_BACKSTAGE
    }
    
    private class WindowMessageID {
        public static final int SUCCESS = 0x00000001;
        public static final int NET_FAILED = 0x00000002;
        public final static int ERROR = 0x00000003;
        public final static int EVENT_PLAY = 0x00000004;
        public static final int UI_EVENT_UPDATE_CURRPOSITION = 0x00000005;
        public final static int PROGRESS_CHANGED = 0x00000006;
        public final static int HIDE_CONTROLER = 0x00000007;
        public static final int DATA_PREPARE_OK = 0x00000008;
        public static final int DATA_BASE64_PREPARE_OK = 0x00000009;
        public static final int PREPARE_VOD_DATA = 0x000000010;
        public static final int SHOW_TV = 0x00000011;
        public static final int COLSE_SHOW_TV = 0x00000012;
        public static final int PROGRESSBAR_PROGRESS_RESET = 0x00000013;
        public static final int SELECT_SCALES = 0x00000014;
        public static final int HIDE_PROGRESS_TIME = 0x00000029;
        public static final int HIDE_MENU = 0x0000002A;
        public static final int RESET_MOVIE_TIME = 0x00000018;
        public static final int PLAY_ERROR = 0x00000019;
        public static final int SWITCH_CODE = 0x00000020;
        public static final int Try = 0x00000023;
        public static final int NOTICE = 0x00000024;
        public static final int NOTICE_GONE = 0x00000025;
        public static final int START_NOTICE_GONE = 0x00000026;
        public static final int START_LOGO = 0x00000027;
        public static final int Sniffing = 0x000000028;
        public static final int START_SPEED = 0x000000029;
        public static final int PREPARE_TIMEOUT = 0x000000030;
    }
    
    // ==================== 弹幕相关方法 ====================
    private void initDanmaku() {
        Log.d("Danmaku", "========== 初始化弹幕开始 ==========");
        Log.d("Danmaku", "danmakuView: " + (danmakuView != null ? "已找到" : "未找到"));
        
        danmakuManager = new com.shenma.tvlauncher.danmaku.DanmakuManager(this);
        danmakuConfig = new com.shenma.tvlauncher.danmaku.DanmakuConfig();
        
        Log.d("Danmaku", "danmakuManager: " + (danmakuManager != null ? "已创建" : "创建失败"));
        Log.d("Danmaku", "danmakuConfig: " + (danmakuConfig != null ? "已创建" : "创建失败"));
        
        // 从服务器获取弹幕配置
        loadDanmakuConfig();
    }
    
    private void loadDanmakuConfig() {
        Log.d("Danmaku", "========== 加载弹幕配置开始 ==========");
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""), Constant.d);
        Log.d("Danmaku", "User_url: " + User_url);
        
        if (User_url == null || User_url.isEmpty()) {
            Log.e("Danmaku", "User_url为空，无法加载弹幕配置");
            return;
        }
        
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""), Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""), Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""), Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""), Constant.d);
        final int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        
        String configUrl = User_url + "/api?app=" + Api.APPID + "&act=danmaku_config";
        Log.d("Danmaku", "弹幕配置请求URL: " + configUrl);
        
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
            Log.d("Danmaku", "创建新的RequestQueue");
        }
        
        StringRequest request = new StringRequest(Request.Method.POST, configUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.d("Danmaku", "弹幕配置响应: " + response);
                        JSONObject json = new JSONObject(response);
                        int code = json.optInt("code", -1);
                        Log.d("Danmaku", "响应code: " + code);
                        
                        if (code == 200) {
                            // msg字段包含数据（可能是加密字符串）
                            String msgStr = json.optString("msg", "");
                            Log.d("Danmaku", "msg原始: " + msgStr);
                            
                            // 解密数据
                            String decryptedMsg = null;
                            if (miType == 1) {
                                decryptedMsg = Rc4.decry_RC4(msgStr, RC4KEY);
                            } else if (miType == 2) {
                                try {
                                    decryptedMsg = Rsa.decrypt_Rsa(msgStr, RSAKEY);
                                } catch (Exception e) {
                                    Log.e("Danmaku", "RSA解密失败: " + e.getMessage());
                                }
                            } else if (miType == 3) {
                                decryptedMsg = AES.decrypt_Aes(AESKEY, msgStr, AESIV);
                            }
                            Log.d("Danmaku", "解密后msg: " + decryptedMsg);
                            
                            JSONObject data = null;
                            if (decryptedMsg != null && !decryptedMsg.isEmpty()) {
                                try {
                                    data = new JSONObject(decryptedMsg);
                                } catch (Exception e) {
                                    Log.e("Danmaku", "解密后JSON解析失败: " + e.getMessage());
                                }
                            }
                            
                            if (data != null) {
                                Log.d("Danmaku", "解析到的data: " + data.toString());
                                int switchOn = data.optInt("switch", 0);
                                danmakuConfig.setSwitchOn(switchOn);
                                
                                // 如果弹幕开关关闭，只设置开关状态，不设置其他配置，也不初始化弹幕
                                if (switchOn == 0) {
                                    Log.w("Danmaku", "弹幕开关已关闭，不加载弹幕配置和初始化弹幕");
                                    danmakuConfig.setApi("");
                                    danmakuConfig.setDefaultShow(0);
                                    danmakuConfig.setMaxCount(0);
                                    danmakuConfig.setOpacity(0);
                                    danmakuConfig.setFontSize(0);
                                    danmakuConfig.setSpeed(0);
                                    return; // 直接返回，不进行后续初始化
                                }
                                
                                // 开关开启时，加载完整配置
                                danmakuConfig.setApi(data.optString("api", ""));
                                danmakuConfig.setDefaultShow(data.optInt("default_show", 1));
                                danmakuConfig.setMaxCount(data.optInt("max_count", 100));
                                danmakuConfig.setOpacity(data.optInt("opacity", 80));
                                danmakuConfig.setFontSize(data.optInt("font_size", 25));
                                danmakuConfig.setSpeed(data.optInt("speed", 100));
                                
                                Log.d("Danmaku", "弹幕配置: switch=" + danmakuConfig.getSwitchOn() 
                                    + ", api=" + danmakuConfig.getApi()
                                    + ", isEnabled=" + danmakuConfig.isEnabled()
                                    + ", danmakuView=" + (danmakuView != null));
                                
                                // 初始化弹幕视图
                                if (danmakuConfig.isEnabled() && danmakuView != null) {
                                    Log.d("Danmaku", "开始初始化弹幕视图");
                                    danmakuManager.init(danmakuView, danmakuConfig);
                                } else {
                                    Log.w("Danmaku", "弹幕未启用或视图为空，跳过初始化");
                                }
                            } else {
                                Log.e("Danmaku", "data为null");
                            }
                        } else {
                            Log.e("Danmaku", "响应code不是200: " + code);
                        }
                    } catch (Exception e) {
                        Log.e("Danmaku", "解析弹幕配置失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("Danmaku", "加载弹幕配置网络错误: " + (error != null ? error.getMessage() : "unknown"));
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
                    rc4data = AES.encrypt_Aes(AESKEY, codedata, AESIV);
                }
                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(codedata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                params.put("data", rc4data);
                params.put("sign", sign);
                // 添加应用签名验证
                params.put("app_sign", com.shenma.tvlauncher.utils.AppSignature.getSignatureMD5(VideoDetailsActivity.this));
                params.put("app_name", com.shenma.tvlauncher.utils.AppSignature.getPackageName(VideoDetailsActivity.this));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VideoDetailsActivity.this, "Authorization", ""), Constant.d));
                return headers;
            }
        };
        
        if (mQueue != null) {
            mQueue.add(request);
            Log.d("Danmaku", "请求已加入队列");
        } else {
            Log.e("Danmaku", "mQueue为null，无法发送请求");
        }
    }
    
    private void loadDanmakuData() {
        if (danmakuManager == null || danmakuConfig == null || !danmakuConfig.isEnabled()) {
            Log.d("Danmaku", "loadDanmakuData: 弹幕未启用或管理器为空");
            return;
        }
        
        // 获取后端基础地址
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""), Constant.d);
        if (User_url == null || User_url.isEmpty()) {
            Log.e("Danmaku", "User_url为空，无法加载弹幕");
            return;
        }
        
        // 使用原始URL作为弹幕标识（加密与否由后端控制）
        // url: 原始URL（可能是加密的ID），这是从视频列表获取的原始标识
        // jxurl: 后端解析后返回的实际播放URL（不是原始URL，不应该用于弹幕请求）
        // 必须使用 url（原始URL），后端会根据Resource_Key决定是否需要解密
        String videoUrl = null;
        
        // 必须使用 url（原始URL，可能是加密的ID）
        if (url != null && !url.isEmpty()) {
            videoUrl = url;
            Log.d("Danmaku", "使用 url 作为弹幕标识（原始URL，可能是加密的）");
        } else {
            Log.e("Danmaku", "url为空，无法加载弹幕（不应使用jxurl，因为那是后端返回的解析后URL）");
            return;
        }
        
        if (videoUrl != null && !videoUrl.isEmpty()) {
            // 对URL进行编码（确保特殊字符正确编码）
            String danmakuUrl;
            try {
                danmakuUrl = java.net.URLEncoder.encode(videoUrl, "UTF-8");
            } catch (Exception e) {
                danmakuUrl = videoUrl;
            }
            
            // 构建后端dmku的完整URL（通过api路由）
            String backendUrl = User_url + "/api?app=" + Api.APPID + "&act=dmku&ac=dm&url=" + danmakuUrl;
            Log.d("Danmaku", "加载弹幕数据, 视频标识: " + videoUrl);
            Log.d("Danmaku", "后端弹幕API URL: " + backendUrl);
            
            // 直接传递完整的后端URL给DanmakuManager
            danmakuManager.loadDanmaku(backendUrl);
        } else {
            Log.w("Danmaku", "视频URL为空，无法加载弹幕");
        }
    }
    
    private void startDanmaku() {
        if (danmakuManager != null && danmakuConfig != null && danmakuConfig.isEnabled()) {
            danmakuManager.start();
        }
    }
    
    private void pauseDanmaku() {
        if (danmakuManager != null) {
            danmakuManager.pause();
        }
    }
    
    private void resumeDanmaku() {
        if (danmakuManager != null) {
            danmakuManager.resume();
        }
    }
    
    private void seekDanmaku(long position) {
        if (danmakuManager != null) {
            danmakuManager.seekTo(position);
        }
    }
    
    private void toggleDanmaku() {
        if (danmakuManager != null) {
            danmakuManager.toggle();
        }
    }
    
    private void showDanmaku() {
        if (danmakuManager != null) {
            danmakuManager.show();
        }
    }
    
    private void hideDanmaku() {
        if (danmakuManager != null) {
            danmakuManager.hide();
        }
    }
    
    private void releaseDanmaku() {
        if (danmakuManager != null) {
            danmakuManager.release();
            danmakuManager = null;
        }
    }
}
