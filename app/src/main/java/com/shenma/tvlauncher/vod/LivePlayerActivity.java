package com.shenma.tvlauncher.vod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.RelativeLayout;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
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
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
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
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.LivePlayUtils;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.view.AlwaysMarqueeTextView;
import com.shenma.tvlauncher.vod.adapter.LiveMenuAdapter;
import com.shenma.tvlauncher.vod.domain.RequestVo;
import com.shenma.tvlauncher.vod.domain.VideoDetailInfo;
import com.shenma.tvlauncher.vod.domain.VideoInfo;
import com.shenma.tvlauncher.vod.domain.VodUrl;
import com.shenma.tvlauncher.vod.domain.VodUrlList;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.example.widget.media.IRenderView;
import tv.danmaku.ijk.media.example.widget.media.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkMediaMeta;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;


/**
 * @author joychang
 * @Description 开始播放
 */
public class LivePlayerActivity extends Activity implements OnClickListener {
    private static final String TAG = "LivePlayerActivity";
    private static final int TIME = 6000;/*显示时间*/
    private static final int TOUCH_BRIGHTNESS = 2;/*触摸亮度*/
    private static final int TOUCH_NONE = 0;/*停止触摸*/
    private static final int TOUCH_VOLUME = 1;/*触摸音量*/
    public static int hmblposition = 0;/*画面*/
    public static int jmposition = 0;/*解码*/
    public static int phszposition = 0;/*偏好*/
    public static int xjposition = 0;/*选集*/
    public static int nhposition = 0;/*播放器内核 0=自动 1=系统 2=IJK 3=EXO 4=阿里*/
    public static int qlposition = 0;/*清理数据*/
    public static int bsposition = 0;/*倍速*/
    public static int ptposition = 0;/*片头*/
    public static int pwposition = 0;/*片尾*/
    private static int controlHeight = 0;
    private static int menutype;/*菜单列表*/
    private static String rxByte;
    private final Object SYNC_Playing = new Object();
    private float Lightness;
    private View controlView;
    private PopupWindow controler;
    private int currentVolume;
    private long firstTime = 0;
    private IjkVideoView iVV = null;
    private ImageButton ib_playStatus;
    private boolean isBack = false;/*是否返回*/
    private boolean isControllerShow = false;
    private Boolean isDestroy = Boolean.valueOf(false);
    private Boolean isLast = Boolean.valueOf(false);
    private boolean isMenuItemShow = false;
    private boolean isMenuItemShows = true;
    private boolean isMenuItemShowss = true;
    private boolean isMenuShow = false;
    private Boolean isNext = Boolean.valueOf(false);
    private Boolean isPause = Boolean.valueOf(false);
    private boolean isSwitch = true;
    private long lastRxByte;
    private long lastSpeedTime;
    private AudioManager mAudioManager = null;
    private GestureDetector mGestureDetector = null;/*手势*/
    private HandlerThread mHandlerThread;
    private boolean mIsHwDecode = true;/*设置解码模式*/
    private int mLastPos = 0;/*最后播放进度*/
    private PLAYER_STATUS mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
    private ImageView mProgressBar;
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
    private ListView menulists;
    private ListView menulistss;
    private PopupWindow menupopupWindow;
    private PopupWindow menupopupWindows;
    private int playIndex = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, "LIVEs", 0);/*读取上次退出时的频道*/
    private int gsplayIndex = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, "gsLIVEs", 0);/*读取上次退出时的频道*/
    private int playPreCode;
    private int screenHeight;
    private int screenWidth;
    private SeekBar seekBar;
    private EventHandler mEventHandler;
    private SharedPreferences sp,SP;
    private long speed;
    private Runnable speedRunnable;
    private Runnable speedRunnables;
    private Runnable speedRunnabless;
    private View time_controlView;
    private PopupWindow time_controler;
    private TextView tv_currentTime;/*当前播放进度*/
    private TextView tv_menu;
    private TextView tv_mv_name;
    private TextView tv_mv_speed;
    private TextView tv_progress_time;
    private TextView tv_time;
    private TextView tv_totalTime;/*视频总时长*/
    private AlwaysMarqueeTextView tv_notice;/*视频跑马公告内容*/
    private LinearLayout tv_notice_root;/*视频跑马框架*/
    private String url;/*选集中的一集*/
    private int videoLength;/*视频总时长*/
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 调用窗口消息处理函数
            onMessage(msg);
        }
    };
    private LiveMenuAdapter vmAdapter;
    private LiveMenuAdapter vmAdapters;
    private LiveMenuAdapter vmAdapterss;
    private final Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WindowMessageID.DATA_PREPARE_OK:
                    onCreateMenus();
                    onCreateMenu();
                    return;
                case WindowMessageID.PREPARE_VOD_DATA:
                    PrepareVodData(playIndex);
                    return;
                case WindowMessageID.SHOW_TV:
                    //mProgressBar.setVisibility(View.VISIBLE);
                    if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.GONE);////
                    }
                    startSpeed();
                    return;
                case WindowMessageID.COLSE_SHOW_TV:
                    if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.GONE);
                    }
                    endSpeed();
                    return;
                case WindowMessageID.PROGRESSBAR_PROGRESS_RESET:
                    // ImageView 不需要 setProgress 方法
                    return;
                case WindowMessageID.SELECT_SCALES:
                    selectScales(hmblposition);
                    return;
                case WindowMessageID.RESET_MOVIE_TIME:
                    ResetMovieTime();
                    return;
                case WindowMessageID.NOTICE:
                    tv_notice.setSelected(true);
                    tv_notice.setMarqueeRepeatLimit(-1);
                    tv_notice.startScroll();
                    int Vod_Notice_end_time = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.Fi, 0);
                    mediaHandler.sendEmptyMessageDelayed(WindowMessageID.NOTICE_GONE, Vod_Notice_end_time * 1000);
                    return;
                case WindowMessageID.NOTICE_GONE:
                    tv_notice_root.setVisibility(View.GONE);
                    return;
                case WindowMessageID.START_NOTICE_GONE:
                    getVodGongGao();
                    return;
                case WindowMessageID.START_LOGO:
                    if (!logo_url.equals("null") && !logo_url.equals("")){
                        vodlogoloadImg();
                        break;
                    }
                    tv_logo.setImageDrawable(getResources().getDrawable(R.drawable.sm_logo));
                    tv_logo.setVisibility(View.VISIBLE);
                    return;
                case WindowMessageID.SELECT_CHANNE:
                    // 选台功能
                    if (!keyChanne.equals("")) {
                        int position = Integer.parseInt(keyChanne);
                        if (position < videoInfo.size()){
                            playIndex = Integer.parseInt(keyChanne);
                            xjposition = Integer.parseInt(keyChanne);

                            int maxUrlValueSoFar = -1; // 用于存储小于或等于position的最大url值
                            int maxUrlIndex = -1; // 用于存储对应最大url值的索引

                            for (int i = 0; i < videoInfos.size(); i++) {
                                try {
                                    int currentUrlValue = Integer.parseInt(videoInfos.get(i).url);

                                    if (currentUrlValue <= position + 1 && currentUrlValue > maxUrlValueSoFar) {
                                        // 更新最大url值和对应的索引
                                        maxUrlValueSoFar = currentUrlValue;
                                        maxUrlIndex = i;
                                    } else if (currentUrlValue > position + 1) {
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (maxUrlIndex != -1) {
                                gsplayIndex = maxUrlIndex;
                                gsposition = gsplayIndex;
                            }

                            SelecteVod(position);
                            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        }
                    }
                    keyChanne = "";
                    mProgramNum.setText(keyChanne);
                    return;
                case WindowMessageID.TV_COVER:
                    /*换台遮挡*/
                    ll_forfend.setVisibility(View.GONE);////换台遮挡
                    return;
                case WindowMessageID.EPG:
                    ll_epg1.setVisibility(View.GONE);////EPG1隐藏
                    ll_epg2.setVisibility(View.GONE);////EPG2隐藏
                    return;
                default:
                    return;
            }
        }
    };
    private RequestQueue mQueue;
    private ImageView tv_logo;/*视频logo*/
    private List<VideoInfo> videoInfo = null;
    private List<VideoInfo> videoInfos = null;
    private int ClientID = 1;//当前解析客户端
    private int MaxClientID = 1;//最大解析客户端
    private int Type = 0;//播放类型 0=JSON
    /*远程logo地址*/
    private String logo_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(LivePlayerActivity.this, Constant.yv, null),Constant.d);
    /*解析客户端*/
    private String Client = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.Nr, ""),Constant.d);
    /*自动换源*/
    private int Auto_Source = 1;
    private Map<String, String> headers;//解析给的请求头
    private String userAgent;//播放器头
    private String Referer;//播放器来源
    /*导航模式*/
    private int Navigation_mode = SharePreferenceDataUtil.getSharedIntData(this, Constant.bw, 0);
    /*跑马公告起始时间*/
    int Vod_Notice_starting_time = SharePreferenceDataUtil.getSharedIntData(this, Constant.lf, 0);
    private int load = 0;
    private int Maxtimeout = 300;
    private int timeout = Maxtimeout;
    private WebView webView;
    private boolean isCodeBlockExecuted = false; // 播放器监听错误添加一个标志变量用于跟踪代码块是否已经执行过
    private boolean isParsing = false; // 标记是否正在解析中
    public static String Failed = "";//防盗版验证
    private  List<VodUrl> now_source = null;
    private String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.bs, ""),Constant.d);
    private String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.fn, ""),Constant.d);
    private int splitCount;
    private String[] parts;
    private int PlayersNumber;/*当前播放源*/

    private boolean KEYCODE_DPAD_UP = false;
    private boolean KEYCODE_DPAD_DOWN = true;

    private ConstraintLayout ll_epg1;////EPG1框架
    private TextView tv_channelname1;////EPG1频道名
    private TextView tv_srcinfo1;////EPG1源信息
    private TextView tv_channelnum1;////EPG1频道号

    private LinearLayout ll_epg2;////EPG2框架
    private TextView tv_channelname2;////EPG2频道名
    private TextView tv_srcinfo2;////EPG2源信息
    private TextView tv_channelnum2;////EPG2频道号

    // 当前节目和下一节目信息显示
    private com.view.ScrollTextView tv_current_program_name;////当前节目名称
    private TextView tv_current_program_time;////当前节目时间
    private TextView tv_next_program_name;////下一节目名称（MarqueeTextView或TextView）
    private TextView tv_next_program_time;////下一节目时间

    private LinearLayout ll_forfend;////换台遮挡

    public static String keyChanne = "";////数字选台
    private TextView mProgramNum = null;////频道按键
    private TextView tv_netspeedinfo;////实时网速
    private long lastRxBytes;
    private long lastSpeedTimes;
    private long speeds;
    private static String rxBytes;////实时网速

    /*卡顿校验
     *  0 = 不校验(默认)
     *  1 = 校验
     */
//    int caton_check = 0;////卡顿校验
//    private int caton_check = sp.getInt("caton_check", 0);////卡顿校验
    int caton_check = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.th, 0);////卡顿校验


    /*卡顿校验时间(默认10秒)
    */
//    int check_time = 10;////卡顿校验时间
    //    private int check_time = sp.getInt("check_time", 10);////卡顿校验时间
    int check_time = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.jd, 10);////卡顿校验时间

    /*卡顿解决方式
     * 0 = 重载
     * 1 = 换源(默认)
     */
//    int Tackle_mode = 1;////卡顿解决方式
//    private int Tackle_mode = sp.getInt("Tackle_mode", 1);////卡顿解决方式
    private int Tackle_mode = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.yx, 1);////卡顿解决方式

    /*显示网速
     * 0 = 不显示
     * 1 = 显示(默认)
     */
//    int networkspeed = 1;////显示网速
//    private int networkspeed = sp.getInt("networkspeed", 1);////显示网速
    private int networkspeed = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.id, 1);////显示网速

    /*EPG风格
     * 0 = 点播风格
     * 1 = EPG现代风格(默认)
     * 2 = EPG经典风格
     */
//    int epg = 1;////EPG风格
//    private int epg = sp.getInt("epg", 1);////EPG风格
    private int epg = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.user, 1);////EPG风格

    /*换台方式
     * 0 = 定帧
     * 1 = 遮挡(默认)
     */
//    int switchs = 1;////换台方式
//    private int switchs = sp.getInt("switchs", 1);////换台方式
    private int switchs = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.gid, 1);////换台方式

    /*记忆播放源
     * 0 = 不记忆
     * 1 = 记忆(默认)
     */
//    int memory_source = 1;////记忆播放源
//    private int memory_source = sp.getInt("memory_source", 1);////记忆播放源
    private int memory_source = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.yms, 1);////记忆播放源

    /*记忆频道
     * 0 = 不记忆
     * 1 = 记忆(默认)
     */
//    int memory_channel = 1;////记忆频道
//    private int memory_channel = sp.getInt("memory_channel", 1);////记忆频道
    private int memory_channel = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.oi, 1);////记忆频道

    /*源用尽处理方式
     * 0 = 从头换(默认)
     * 1 = 切换频道
     */
//    int live_no_source = 0;
//    private int live_no_source = sp.getInt("live_no_source", 0);////源用尽处理方式
    private int live_no_source = SharePreferenceDataUtil.getSharedIntData(LivePlayerActivity.this, Constant.ykf, 0);////源用尽处理方式

    public static int gsposition = 0;/*归属*/

    private int core = 99;//获取核心 99=用户默认 0=自动 1=系统 2=IJK 3=EXO 4=阿里
    private int safe_mode = 0;//指定内核安全模式 0=安全模式(低于5.1系统强制使用IJK播放器) 1=强制使用指定内核
    /*播放器指定内核授权是否开启
     * 0=关闭(默认)
     * 1=开启*/
    private int core_mode = SharePreferenceDataUtil.getSharedIntData(this, Constant.ldb, 0);

    /*换台键反转
     * 0=关闭(默认)
     * 1=开启*/
    private int reverse = SharePreferenceDataUtil.getSharedIntData(this, Constant.gida, 0);
    private BroadcastReceiver pauseVideoReceiver;






    /*创建时的回调函数*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mv_videoplayer);
        Utils.stopAutoBrightness(this);
        if (Navigation_mode == 1){
            Navigation();
        }
        /*标记进入直播模式*/
        SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "LIVE",1);
        // 重置解析标志
        isParsing = false;
        isCodeBlockExecuted = false;
        initView();
        initData();
        //收听广播
        pauseVideoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.example.MY_ACTION_FINISH_LIVE")) {

                    if (memory_channel == 1){
                        SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "LIVEs",playIndex);
                        SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "gsLIVEs",gsplayIndex);
                    }else{
                        SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "LIVEs",0);
                    }
                    isDestroy = Boolean.valueOf(true);
                    stopPlayback();
                    finish();
                }
            }
        };
        // 注册广播
        LocalBroadcastManager.getInstance(this).registerReceiver(pauseVideoReceiver, new IntentFilter("com.example.MY_ACTION_FINISH_LIVE"));
    }

    /*销毁时*/
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = Boolean.valueOf(true);
        stopPlayback();
        
        // 取消所有网络请求
        if (mQueue != null) {
            mQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
            mQueue = null;
        }
        
        if (mSpeedHandler != null) {
            mSpeedHandler.removeCallbacks(speedRunnables);////移除实时网速
            mSpeedHandler.removeCallbacks(speedRunnabless);////移除卡顿校验
        }
        //mSpeedHandler.removeCallbacks(speedRunnables);////移除实时网速
        //mSpeedHandler.removeCallbacks(speedRunnabless);////移除卡顿校验
        Utils.startAutoBrightness(this);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //销毁广播
        if (pauseVideoReceiver != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(pauseVideoReceiver);
        }
    }

    /*停止时*/
    protected void onStop() {
        super.onStop();
        isDestroy = Boolean.valueOf(true);
        
        // 取消所有网络请求
        if (mQueue != null) {
            mQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
        
        // 重置解析标志
        isParsing = false;
        isCodeBlockExecuted = false;
        
        mHandler.removeMessages(WindowMessageID.SWITCH_CODE);
        mHandler.removeMessages(WindowMessageID.HIDE_CONTROLER);
        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        xjposition = 0;
        mHandlerThread.quit();
    }

    /*恢复时*/
    protected void onResume() {
        hideMenu();//隐藏侧边菜单栏
        xjposition = playIndex;//恢复剧集选择
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
        Map m_value = new HashMap();
        MobclickAgent.onEvent(this, "VOD_PLAY", m_value);
        isDestroy = Boolean.valueOf(false);
        // 重置解析标志，确保每次恢复时都是干净的状态
        isParsing = false;
        isCodeBlockExecuted = false;
        acquireWakeLock();

        /*开启后台事件处理线程*/
        if(!mHandlerThread.isAlive()){
            mHandlerThread = new HandlerThread("event handler thread", Process.THREAD_PRIORITY_BACKGROUND);
            mHandlerThread.start();
            mEventHandler = new EventHandler(mHandlerThread.getLooper());
        }

        /*发起一次播放任务,当然您不一定要在这发起*/
        if(null!=mVideoSource && !"".equals(mVideoSource) && mPlayerStatus== PLAYER_STATUS.PLAYER_IDLE){
            mEventHandler.sendEmptyMessage(WindowMessageID.EVENT_PLAY);
        }

        mSpeedHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case WindowMessageID.START_SPEED:
                        if (tv_mv_speed != null) {
                            tv_mv_speed.setText(rxByte);
                        }
                        return;
                    case WindowMessageID.START_SPEEDS:


                        if (networkspeed == 1){
                            tv_netspeedinfo.setText(rxBytes);////实时网速
                            tv_netspeedinfo.setVisibility(View.VISIBLE);
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        /*获取网速*/
        speedRunnable = new Runnable() {
            public void run() {
                load = load + 1;
                if (load >= timeout * 2){
                    if (ClientID < MaxClientID){
                        isParsing = false; // 重置解析标志
                        setVideoUrl(Client + Integer.toString(ClientID + 1 ));
                        load = 0;
                    }else{
                        load = 0;
                        timeout = 300;
                        if (Auto_Source == 1){
                            /*换源*/
                            Switchsource(1);
                        }else{
                            /*关闭菊花*/
                            mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                            Switchsource(3);
                        }
                    }
                }
                if (!(lastRxByte == 0 || lastSpeedTime == 0)) {
                    long nowtime = System.currentTimeMillis();
                    long nowRxbyte = TrafficStats.getTotalRxBytes();
                    long rxbyte = nowRxbyte - lastRxByte;
                    long time = nowtime - lastSpeedTime;
                    if (!(rxbyte == 0 || time == 0)) {
                        speed = ((rxbyte / time) * 1000) / IjkMediaMeta.AV_CH_SIDE_RIGHT;
                        if (speed >= IjkMediaMeta.AV_CH_SIDE_RIGHT) {
                            rxByte = new StringBuilder(String.valueOf(String.valueOf(speed / IjkMediaMeta.AV_CH_SIDE_RIGHT))).append("MB/S").toString();
                        } else {
                            rxByte = new StringBuilder(String.valueOf(String.valueOf(speed))).append("KB/S").toString();
                        }
                        mSpeedHandler.sendEmptyMessage(WindowMessageID.START_SPEED);
                    }
                    lastRxByte = nowRxbyte;
                    lastSpeedTime = nowtime;
                }
                mSpeedHandler.postDelayed(speedRunnable, 500);
            }
        };

        speedRunnables = new Runnable() {
            public void run() {
                if (!(lastRxBytes == 0 || lastSpeedTimes == 0)) {
                    long nowtime = System.currentTimeMillis();
                    long nowRxbyte = TrafficStats.getTotalRxBytes();
                    long rxbyte = nowRxbyte - lastRxBytes;
                    long time = nowtime - lastSpeedTimes;
                    if (!(rxbyte == 0 || time == 0)) {
                        speeds = ((rxbyte / time) * 1000) / IjkMediaMeta.AV_CH_SIDE_RIGHT;
                        if (speeds >= IjkMediaMeta.AV_CH_SIDE_RIGHT) {
                            rxBytes = new StringBuilder(String.valueOf(String.valueOf(speeds / IjkMediaMeta.AV_CH_SIDE_RIGHT))).append("MB/S").toString();
                        } else {
                            rxBytes = new StringBuilder(String.valueOf(String.valueOf(speeds))).append("KB/S").toString();
                        }
                        mSpeedHandler.sendEmptyMessage(WindowMessageID.START_SPEEDS);
                    }
                    lastRxBytes = nowRxbyte;
                    lastSpeedTimes = nowtime;
                }
                mSpeedHandler.postDelayed(speedRunnables, 500);
            }
        };

        speedRunnabless = new Runnable() {
            private long lastPosition = -1;
            public void run() {
                long currentPosition = iVV.getCurrentPosition();
                if (currentPosition == lastPosition) {
                    if (Tackle_mode  == 0){
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                    }else{
                        Switchsource(1);
                    }
//                    System.out.println("直播进度卡住了！" + currentPosition + "-" + lastPosition);
                } else {
                    // 更新上一次的位置
//                    System.out.println("直播进度正常！");
                    lastPosition = currentPosition;
                }
                mSpeedHandler.postDelayed(speedRunnabless, check_time * 1000);
            }
        };



        if (mPlayerStatus == PLAYER_STATUS.PLAYER_BACKSTAGE) {
            if (!iVV.isPlaying()) {
                /*修复后台暂停后一直转圈的问题*/
                iVV.start();
            }else{
                mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
                iVV.start();
                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
            }
        } else {
            // ImageView 不需要 setProgress 方法
            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        }
    }

    /*暂停时*/
    protected void onPause() {
        super.onPause();
        isDestroy = Boolean.valueOf(true);
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
        releaseWakeLock();
        
        // 取消所有网络请求
        if (mQueue != null) {
            mQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
        
        // 重置解析标志
        isParsing = false;
        isCodeBlockExecuted = false;
        
        if (iVV.isPlaying()) {
            iVV.pause();
            mLastPos = iVV.getCurrentPosition();
            mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
            mPlayerStatus = PLAYER_STATUS.PLAYER_BACKSTAGE;
        }
        hideController();
        mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
    }

    /*初始化视频数据*/
    private void PrepareVodData(int postion) {
        isSwitch = true;
        if (videoInfo != null && videoInfo.size() > 0 && videoInfo.size() >= playIndex) {
            TextView textView = tv_mv_name;
            textView.setText(videoInfo.get(playIndex).title);
            tv_channelname1.setText(videoInfo.get(playIndex).title);////EPG1频道名
            tv_channelname2.setText(videoInfo.get(playIndex).title);////EPG2频道名
            tv_channelnum1.setText(Integer.toString(playIndex));////EPG1频道号
            tv_channelnum2.setText(Integer.toString(playIndex));////EPG2频道号
            
            // 更新当前节目信息显示
            if (tv_current_program_name != null) {
                tv_current_program_name.setText(videoInfo.get(playIndex).title);////当前节目名称
            }
            // 如果频道信息包含时间信息，可以在这里设置，目前先设置为空或默认值
            if (tv_current_program_time != null) {
                // tv_current_program_time.setText(""); // 可以根据实际EPG数据设置时间
            }
            // 更新下一节目信息（如果有下一个频道）
            if (tv_next_program_name != null && playIndex + 1 < videoInfo.size()) {
                tv_next_program_name.setText(videoInfo.get(playIndex + 1).title);////下一节目名称
            } else if (tv_next_program_name != null) {
                tv_next_program_name.setText(""); // 没有下一节目时清空
            }

            String str = videoInfo.get(postion).url;
            parts = str.split("#");
            splitCount = parts.length - 1;
            /*读取保存源*/
            if (memory_source == 1){
                PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
            }
//            PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
            if (PlayersNumber > splitCount){
                PlayersNumber = 0;
                this.url = parts[0];
            }else{
                if (memory_source == 1){
                    PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
                }else{
                    PlayersNumber = 0;
                }
//                PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
                this.url = parts[PlayersNumber];
            }
//            tv_srcinfo1.setText((PlayersNumber + 1) + "/" + splitCount);////
            if (PlayersNumber + 1 > parts.length){
                tv_srcinfo1.setText("源:" + PlayersNumber + "/" + parts.length);////EPG1源信息
                tv_srcinfo2.setText("源:" + PlayersNumber + "/" + parts.length);////EPG2源信息
            }else{
                tv_srcinfo1.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG1源信息
                tv_srcinfo2.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG2源信息
            }
//            PlayersNumber = 0;
//            this.url = parts[0];
            mediaHandler.sendEmptyMessage(WindowMessageID.DATA_PREPARE_OK);
            mEventHandler.sendEmptyMessage(WindowMessageID.EVENT_PLAY);
        }
    }

    /*切换剧集*/
    private void SelecteVod(int postion) {
        stopPlayback();

//        System.out.println("解码模式：硬解1-" + postion + "-" + Integer.parseInt(videoInfos.get(gsplayIndex).url) + "-" + gsposition);


        if (postion == 0){
            gsplayIndex = 0;
            gsposition = 0;
        }

        if (postion == videoInfo.size()-1){
            gsplayIndex = videoInfos.size()-1;
            gsposition = gsplayIndex;
        }

//        if (gsplayIndex < videoInfos.size() - 1 && (postion + 1) >= Integer.parseInt(videoInfos.get(gsplayIndex + 1).url)) {
//            gsplayIndex++;
//            gsposition = gsplayIndex;
//        }
//
//
//
//        if ((postion + 2)  <= Integer.parseInt(videoInfos.get(gsplayIndex).url)) {
//            gsplayIndex--;
//            gsposition = gsplayIndex;
//        }


        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        isSwitch = true;
        stopPlayback();
        mHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
        mLastPos = 0;
        TextView textView = tv_mv_name;
        textView.setText( videoInfo.get(playIndex).title);
        tv_channelname1.setText(videoInfo.get(playIndex).title);////EPG1频道名
        tv_channelname2.setText(videoInfo.get(playIndex).title);////EPG2频道名
        tv_channelnum1.setText(Integer.toString(playIndex));////EPG1频道号
        tv_channelnum2.setText(Integer.toString(playIndex));////EPG2频道号
        
        // 更新当前节目信息显示
        if (tv_current_program_name != null) {
            tv_current_program_name.setText(videoInfo.get(playIndex).title);////当前节目名称
        }
        // 更新下一节目信息（如果有下一个频道）
        if (tv_next_program_name != null && playIndex + 1 < videoInfo.size()) {
            tv_next_program_name.setText(videoInfo.get(playIndex + 1).title);////下一节目名称
        } else if (tv_next_program_name != null) {
            tv_next_program_name.setText(""); // 没有下一节目时清空
        }
        String str = videoInfo.get(postion).url;
        parts = str.split("#");
        splitCount = parts.length - 1;
        /*读取保存源*/
        if (memory_source == 1){
            PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
        }
//        PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
        if (PlayersNumber > splitCount){
            PlayersNumber = 0;
            this.url = parts[0];
        }else{
            if (memory_source == 1){
                PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
            }else{
                PlayersNumber = 0;
            }
//            PlayersNumber = SP.getInt(videoInfo.get(playIndex).title, 0);
            this.url = parts[PlayersNumber];
        }
//        tv_srcinfo1.setText((PlayersNumber + 1) + "/" + splitCount);////
        if (PlayersNumber + 1 > parts.length){
            tv_srcinfo1.setText("源:" + PlayersNumber + "/" + parts.length);////EPG1源信息
            tv_srcinfo2.setText("源:" + PlayersNumber + "/" + parts.length);////EPG2源信息
        }else{
            tv_srcinfo1.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG1源信息
            tv_srcinfo2.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG2源信息
        }
//        PlayersNumber = 0;
//        url = parts[0];
        isParsing = false; // 重置解析标志
        setVideoUrl(Client);
    }

    /*停止播放*/
    private void stopPlayback() {
        // 取消准备超时检测
        if (mHandler != null) {
            mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
        }
        if(iVV != null){
            iVV.stopPlayback();
        }
    }

    /*初始化数据*/
    private void initData() {
        RequestVo vo = new RequestVo();
//            vo.requestUrl = Api_url + "/api/" + BASE_HOST + "/iptv";
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        vo.requestUrl = User_url + "/api?app="  + Api.APPID +"&act=iptv";
            getDataFromServer(vo);
        int vod_Logo = SharePreferenceDataUtil.getSharedIntData(this, Constant.kF, 0);
        if (vod_Logo == 1){
            mediaHandler.sendEmptyMessage(WindowMessageID.START_LOGO);
        }
    }


    /*请求直播数据地址*/
    protected void getDataFromServer(RequestVo reqVo) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
            GsonRequest<VideoDetailInfo> mVodData = new GsonRequest<VideoDetailInfo>(Request.Method.POST, reqVo.requestUrl,
                    VideoDetailInfo.class, createVodDataSuccessListener(), createVodDataErrorListener()){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    try {
                        params.put("data", AES.encrypt_Aes(Md5Encoder.encode(Constant.c), Md5Encoder.encode(Constant.d),Constant.c));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    params.put("sign", Base64.encodeToString(Utils.strRot13(Constant.c).getBytes(), Base64.DEFAULT));
                    params.put("time", GetTimeStamp.timeStamp());
                    params.put("key", encry_RC4_string(GetTimeStamp.timeStamp(),GetTimeStamp.timeStamp()));
                    params.put("os",  Integer.toString(Build.VERSION.SDK_INT));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(LivePlayerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                    return headers;
                }
            };
            mQueue.add(mVodData);
    }

    /*直播详细数据请求成功*/
    private Response.Listener<VideoDetailInfo> createVodDataSuccessListener() {
        return new Response.Listener<VideoDetailInfo>() {
            @SuppressLint("ResourceType")
            @Override
            public void onResponse(VideoDetailInfo paramObject) {
                if (null != paramObject) {
//                   Log.e("vodUrlLists==", new Gson().toJson(paramObject) );
                    VideoDetailInfo VideoDetailInfo = (VideoDetailInfo) paramObject;
                    List<VodUrl> video_list = VideoDetailInfo.getVideo_list();
                    now_source = video_list;
                    List<VodUrlList> list = null;
                    for (int i = 0; i < now_source.size(); i++) {
                            list = now_source.get(i).getList();
                    }
                    ArrayList arrayList = new ArrayList();
                    for (int i = 0; i < list.size(); i++) {
                        VideoInfo vinfo = new VideoInfo();
//                        vinfo.title = "[ " + i+ " ] "+list.get(i).getTitle();
                        vinfo.title = list.get(i).getTitle();
                        vinfo.url = list.get(i).getUrl();
                        arrayList.add(vinfo);
                    }
                    videoInfo = new ArrayList();
                    videoInfo = arrayList;

                    List<VodUrlList> lists = null;
                    for (int i = 0; i < now_source.size(); i++) {
                        lists = now_source.get(i).getLists();
                    }

//                    System.out.println("解码模式：硬解" + lists );


                    ArrayList arrayLists = new ArrayList();
                    for (int i = 0; i < lists.size(); i++) {
                        VideoInfo vinfo = new VideoInfo();
                        vinfo.title = lists.get(i).getTitle();
                        vinfo.url = lists.get(i).getUrl();
                        arrayLists.add(vinfo);
                    }
                    videoInfos = new ArrayList();
                    videoInfos = arrayLists;

//                    Log.e("vodUrlLists==", new Gson().toJson(lists) );
//                    System.out.println("解码模式：硬解" + video_list);
                    gsposition = gsplayIndex;

                    xjposition = playIndex;
                    if (playIndex >= videoInfo.size()){//选定线路不包含续播内容
                        playIndex = 0;
                    }
                    tv_mv_name.setText(videoInfo.get(playIndex).title);
                    PrepareVodData(playIndex);
                    //Log.e("vodUrlLists==", new Gson().toJson(list) );
                }
            }
        };
    }

    /*直播详细数据请求失败*/
    private Response.ErrorListener createVodDataErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(LivePlayerActivity.this,
                        getString(R.string.str_data_loading_error),
                        R.drawable.toast_err);
                if (error instanceof TimeoutError) {
                    //Logger.e(TAG, "请求超时");
                } else if (error instanceof AuthFailureError) {
                    //Logger.e(TAG, "AuthFailureError=" + error.toString());
                }
            }
        };
    }

    /*上报/换源*/
    private void Switchsource(int msg) {
        stopPlayback();
        isCodeBlockExecuted = false; // 重置错误标志，允许新源再次触发错误处理
        isParsing = false; // 重置解析标志，允许新源进行解析
        
        if (msg == 3){
            if (live_no_source == 0){
                // 重新加载当前频道
                xjposition = playIndex;
                PlayersNumber = 0;
                SelecteVod(playIndex);
                mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
            }else{
                if (KEYCODE_DPAD_UP) {
                    if (playIndex > 0) {
                        playIndex--;
                        xjposition = playIndex;
                        mLastPos = 0;
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        iVV.start();
                    } else {
                        playIndex = 0;
                        xjposition = playIndex;
                        mLastPos = 0;
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        iVV.start();
                    }
                }
                if (KEYCODE_DPAD_DOWN) {
                    if (videoInfo.size() > playIndex + 1) {
                        playIndex++;
                        xjposition = playIndex;
                        mLastPos = 0;
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        iVV.start();
                    } else {
                        playIndex = 0;
                        xjposition = playIndex;
                        mLastPos = 0;
                        SelecteVod(playIndex);
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        iVV.start();
                    }
                }

            }
        }

        if (msg == 1){

            if (epg == 1){
                ll_epg1.setVisibility(View.VISIBLE);////EPG1显示
            }else if (epg == 2){
                ll_epg2.setVisibility(View.VISIBLE);////EPG2显示
            }else{
//                ll_epg1.setVisibility(View.GONE);////EPG1隐藏
//                ll_epg2.setVisibility(View.GONE);////EPG2隐藏
                //showController();
            }

            if (switchs == 1){
                ll_forfend.setVisibility(View.VISIBLE);////换台遮挡
            }

            // 先增加PlayersNumber
            PlayersNumber = PlayersNumber + 1;
            
            // 检查是否超出范围
            if (PlayersNumber >= parts.length){
                // 所有源都尝试过了，重新从第一个源开始
                PlayersNumber = 0;
                url = parts[0];
                SP.edit().putInt(videoInfo.get(playIndex).title, 0).commit();
                Utils.showToast(this, "所有播放源均失败，正在重试...", R.drawable.toast_err);
                Switchsource(3);
                return; // 递归调用后直接返回，避免重复执行
            }
            
            url = parts[PlayersNumber];
            Utils.showToast(this, "正在切换到播放源 " + (PlayersNumber + 1) + "/" + parts.length, R.drawable.toast_smile);

            if (PlayersNumber + 1 > parts.length){
                tv_srcinfo1.setText("源:" + "1/" + parts.length);////EPG1源信息
                tv_srcinfo2.setText("源:" + "1/" + parts.length);////EPG2源信息
            }else{
                tv_srcinfo1.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG1源信息
                tv_srcinfo2.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG2源信息
            }

            load = 0;
            setVideoUrl(Client);
        }
    }

    /*初始化视图*/
    private void initView() {
        sp = getSharedPreferences("shenma", 0);
        SP = getSharedPreferences("live", 0);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        String playratio = sp.getString(Constant.oh, "全屏拉伸");
        if ("原始比例".equals(playratio)) {
            hmblposition = 0;
        } else if ("4:3 缩放".equals(playratio)) {
            hmblposition = 1;
        } else if ("16:9缩放".equals(playratio)) {
            hmblposition = 2;
        } else if ("全屏拉伸".equals(playratio)) {
            hmblposition = 3;
        }else if ("等比缩放".equals(playratio)) {
            hmblposition = 4;
        }else if ("全屏裁剪".equals(playratio)) {
            hmblposition = 5;
        }
        String live_core = sp.getString("live_core", "自动");
        if ("自动".equals(live_core)) {
            nhposition = 0;
        } else if ("系统".equals(live_core)) {
            nhposition = 1;
        } else if ("IJK".equals(live_core)) {
            nhposition = 2;
        } else if ("EXO".equals(live_core)) {
            nhposition = 3;
        } else if ("阿里".equals(live_core)) {
            nhposition = 4;
        }
        qlposition = 0;
        setDecode();
        getPlayPreferences();
        getScreenSize();
        loadViewLayout();
        findViewById();
        setListener();
        setvvListener();
         /*开启后台事件处理线程*/
        mHandlerThread = new HandlerThread("event handler thread", Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mEventHandler = new EventHandler(mHandlerThread.getLooper());
    }

    /*设置解码*/
    private void setDecode() {
        if (sp.getInt("mIsHwDecode", 1) == 0) {
            mIsHwDecode = false;
            jmposition = 0;
            return;
        }
        mIsHwDecode = true;
        jmposition = 1;
    }

    /*偏好设置*/
    private void getPlayPreferences() {
        playPreCode = sp.getInt("playPre", 0);
        if (playPreCode == 0) {
            /*选集*/
            phszposition = 0;
        } else {
            /*调节音量*/
            phszposition = 1;
        }
    }

    /*按ID查找视图*/
    private void findViewById() {
        webView = findViewById(R.id.webview);
        tv_logo = findViewById(R.id.tv_logo);
        tv_notice_root = findViewById(R.id.tv_notice_root);/*视频跑马公告内容*/
        tv_notice = findViewById(R.id.tv_notice);/*视频跑马公告内容*/
        seekBar = controlView.findViewById(R.id.seekbar);
        tv_currentTime = controlView.findViewById(R.id.tv_currentTime);
        tv_totalTime = controlView.findViewById(R.id.tv_totalTime);
        tv_menu = controlView.findViewById(R.id.tv_menu);
        ib_playStatus = controlView.findViewById(R.id.ib_playStatus);
        ib_playStatus.setOnClickListener(this);
        mProgressBar = findViewById(R.id.progressBar);
        ll_epg1 = findViewById(R.id.ll_epg1);////EPG1框架
        tv_channelname1 = findViewById(R.id.tv_channelname1);////EPG1频道名
        tv_srcinfo1 = findViewById(R.id.tv_srcinfo1);////EPG1源信息
        tv_channelnum1 = findViewById(R.id.tv_channelnum1);////EPG1频道号

        ll_epg2 = (LinearLayout) findViewById(R.id.ll_epg2);////EPG12框架
        tv_channelname2 = findViewById(R.id.tv_channelname2);////EPG2频道名
        tv_srcinfo2 = findViewById(R.id.tv_srcinfo2);////EPG2源信息
        tv_channelnum2 = findViewById(R.id.tv_channelnum2);////EPG2频道号
        mProgramNum = (TextView) findViewById(R.id.program_num);////频道按键
        tv_netspeedinfo = (TextView)findViewById(R.id.tv_netspeedinfo);////实时网速
        
        // 获取当前节目和下一节目显示控件
        tv_current_program_name = findViewById(R.id.tv_current_program_name);////当前节目名称
        tv_current_program_time = findViewById(R.id.tv_current_program_time);////当前节目时间
        tv_next_program_name = findViewById(R.id.tv_next_program_name);////下一节目名称
        tv_next_program_time = findViewById(R.id.tv_next_program_time);////下一节目时间





        ll_forfend = (LinearLayout) findViewById(R.id.ll_forfend);////换台遮挡
        tv_progress_time = findViewById(R.id.tv_progress_time);
        tv_mv_speed = findViewById(R.id.tv_mv_speed);
        tv_time = time_controlView.findViewById(R.id.tv_time);
        tv_mv_name = time_controlView.findViewById(R.id.tv_mv_name);
        if (mProgressBar != null) {
        mProgressBar.setVisibility(View.GONE);
        }

//        int epg = 0;
//        if (epg == 1){
//            ll_epg1.setVisibility(View.VISIBLE);////EPG1显示
//        }else if (epg == 2){
//            ll_epg2.setVisibility(View.VISIBLE);////EPG2显示
//        }else{
//            ll_epg1.setVisibility(View.GONE);////EPG1隐藏
//            ll_epg2.setVisibility(View.GONE);////EPG2隐藏
//        }

        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        iVV = findViewById(R.id.i_video_view);
        iVV.setHudView((TableLayout) findViewById(R.id.hubview));
        int Decode = sp.getInt("mIsHwDecode", 1);
//        System.out.println("解码模式：" + Decode);
        if (Decode == 1) {
            iVV.setDecode(Boolean.valueOf(true));
        } else if (Decode == 0) {
            iVV.setDecode(Boolean.valueOf(false));
        }
    }

    /*加载视图布局*/
    private void loadViewLayout() {
        controlView = getLayoutInflater().inflate(R.layout.mv_media_controlers, null);
        controler = new PopupWindow(controlView);
        time_controlView = getLayoutInflater().inflate(R.layout.mv_media_time_controler, null);
        time_controler = new PopupWindow(time_controlView);
    }

    /*事件处理程序*/
    class EventHandler extends Handler {
        public EventHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WindowMessageID.EVENT_PLAY:
                    /*如果已经播放了，等待上一次播放结束*/
                    if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                        synchronized (SYNC_Playing) {
                            try {
                                SYNC_Playing.wait();
                                Logger.i(TAG, "SYNC_Playing.wait()");
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public final void run() {
                            /*访问解析*/
                            isParsing = false; // 重置解析标志
                            setVideoUrl(Client);
                        }
                    });
                    return;
                case WindowMessageID.SUCCESS:
                    iVV.start();
                    mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARING;
                    return;
                default:
                    return;
            }
        }

    }

    /*拼接解析参数*/
    public void setVideoUrl(String Client) {
        /*读取提交方式 0=GET 1=POST*/
        int Submission_method = SharePreferenceDataUtil.getSharedIntData(this, Constant.JE, 0);
        /*视频地址编码 - 添加随机参数和时间戳，确保每次请求都是唯一的，避免服务器缓存*/
        long timestamp = System.currentTimeMillis();
        int random = (int)(Math.random() * 10000);
        String urlEncode = Utils.UrlEncodeChinese(Client + "/?url=" + url + "&_t=" + timestamp + "&_r=" + random);
        /*开始请求解析*/
        analysisUrl(urlEncode,Submission_method);
    }

    /*开始请求解析*/
    private void analysisUrl(String urls ,int way) {
        int Timeout = SharePreferenceDataUtil.getSharedIntData(this, Constant.WM, 25);
        
        // 取消之前的所有请求
        if (mQueue != null) {
            mQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true; // 取消所有请求
                }
            });
        }
        
        /*加载get访问*/
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        /*提交参数*/
        final String account = sp.getString("userName", "");/*帐号*/
        final String password = sp.getString("passWord", "");/*密码*/
        final String token = sp.getString("ckinfo", "");/*token*/
        final String machineid = Utils.GetAndroidID(this);/*安卓ID*/
        final double value = Double.valueOf(Utils.getVersion(LivePlayerActivity.this).toString());/*版本号*/
        final String name = Utils.getEcodString(((VideoInfo) videoInfo.get(playIndex)).title);/*片名*/

        /*GET提交方式*/
        if (way ==  0){
            String url = urls + "&app=" + Api.APPID + "&account=" + account + "&password=" + password + "&token=" + token + "&machineid=" + machineid + "&edition=" + value + "&vodname=" + name + "&line=live" + "&new=1";
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        public void onResponse(String response) {
                            analysisUrlResponse(response);
                        }
                    }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    analysisUrlError(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    // 添加您的请求参数
                    params.put("app", Api.APPID);
                    params.put("account", account);
                    params.put("password", password);
                    params.put("token", token);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(LivePlayerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(Timeout * 1000,//请求的超时时间（以毫秒为单位），即请求在等待响应的最长时间
                    0,//最大重试次数。如果请求失败，将会重试的次数。
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//重试的时间间隔倍数。每次重试的等待时间将是前一个重试时间乘以该倍数。
            mQueue.add(stringRequest);

        }

        /*POST提交方式*/
        if (way ==  1){
            String url = urls;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        public void onResponse(String response) {
                            analysisUrlResponse(response);
                        }
                    }, new Response.ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    analysisUrlError(error);
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    // 添加您的请求参数
                    params.put("app", Api.APPID);
                    params.put("account", account);
                    params.put("password", password);
                    params.put("token", token);
                    params.put("machineid", machineid);
                    params.put("edition", String.valueOf(value));
                    params.put("vodname", name);
                    params.put("line", "live");
                    params.put("new", "1");
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(LivePlayerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(Timeout * 1000,//请求的超时时间（以毫秒为单位），即请求在等待响应的最长时间
                    0,//最大重试次数。如果请求失败，将会重试的次数。
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//重试的时间间隔倍数。每次重试的等待时间将是前一个重试时间乘以该倍数。
            mQueue.add(stringRequest);

        }
    }

    /*解析地址获取成功*/
    public void analysisUrlResponse(String response) {
        // 检查是否正在解析中
        if (isParsing) {
            return;
        }
        isParsing = true; // 设置解析标志
        
        try {
            /*解析结果不加密*/
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            int encrypt = jSONObject.optInt("encrypt");/*加密信息 0=不加密 1=加密*/
            if (code == 200){
                JSONObject data = jSONObject.getJSONObject("data");/*url地址*/
                JSONObject header = data.getJSONObject("header");/*头等配置信息*/
                userAgent = header.getString("User-Agent");
                if (header.has("Referer")) {
                    Referer = header.getString("Referer");
                }
                if (header.has("referer")) {
                    Referer = header.getString("referer");
                }
                final String url = data.getString("url");
                
                // 检查url是否为空
                if (url == null || url.trim().isEmpty()) {
                    Logger.e(TAG, "解析失败: url为空");
                    Utils.showToast(this, "解析失败: 未获取到播放地址", R.drawable.toast_err);
                    
                    // 提交反馈记录
                    submitFeedback();
                    
                    // 重置解析标志
                    isParsing = false;
                    
                    // 根据后端配置决定是否自动换源
                    if (Auto_Source == 1) {
                        if (splitCount > 1) {
                            Utils.showToast(this, "正在切换播放源...", R.drawable.toast_err);
                            Switchsource(1);
                        } else {
                            Switchsource(3);
                        }
                    } else {
                    }
                    return;
                }
                
                ClientID = data.getInt("ClientID");
                MaxClientID = data.getInt("MaxClientID");
                Maxtimeout = data.getInt("Maxtimeout");
                timeout = Maxtimeout;
                if (data.has("Core")) {
                    core = data.getInt("Core");
                }
                if (data.has("Safe")) {
                    safe_mode = data.getInt("Safe");
                }
                if (core_mode == 1){
//                    System.out.println("解码模式：硬解有指定内核功能的授权-直播");
                    /*指定内核*/
                    if (core != 99){
//                        System.out.println("解码模式：硬解指定内核-直播");
                        if (safe_mode == 1){
                            nhposition = core;
//                            System.out.println("解码模式：硬解指定内核强制模式-直播" + nhposition);
                        }else{
//                            System.out.println("解码模式：硬解指定内核安全模式-直播");
                            if (Build.VERSION.SDK_INT < 22){
                                core = 99;
//                                String live_core = sp.getString(Constant.vy, "IJK");
//                                if ("自动".equals(live_core)) {
//                                    nhposition = 0;
//                                } else if ("系统".equals(live_core)) {
//                                    nhposition = 1;
//                                } else if ("IJK".equals(live_core)) {
////                                    nhposition = 2;
//                                } else if ("EXO".equals(live_core)) {
//                                    nhposition = 3;
//                                } else if ("阿里".equals(live_core)) {
//                                    nhposition = 4;
//                                }
                                nhposition = 2;
                            }else{
                                nhposition = core;
                            }
                        }

                    }else{
//                        System.out.println("解码模式：硬解不指定内核-直播");
                        core = 99;
                        String live_core = sp.getString(Constant.vy, "IJK");
                        if ("自动".equals(live_core)) {
                            nhposition = 0;
                        } else if ("系统".equals(live_core)) {
                            nhposition = 1;
                        } else if ("IJK".equals(live_core)) {
                            nhposition = 2;
                        } else if ("EXO".equals(live_core)) {
                            nhposition = 3;
                        } else if ("阿里".equals(live_core)) {
                            nhposition = 4;
                        }
                    }
                }

                Type = data.getInt("Type");
                iVV.setUserAgent(userAgent);
                iVV.setReferer(Referer);
                headers = new HashMap<>();
                Iterator<String> keys = header.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = header.getString(key);
                    headers.put(key, value);
                    //Log.d("key==", key);
                    //Log.d("value==", value);
                }
                if (Type == 0){
                    webView.setVisibility(View.GONE);
                    /*直接播放*/
                    if (encrypt == 1){
                        String decryptedUrl = Rc4.decryptBase64(url,Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d));
                        /*播放地址如果中文用url编码和User-Agent头等信息发送被播放器*/
                        // 先取消之前的超时检测
                        mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
                        if (Failed.equals("")){
                            iVV.setVideoPath(Utils.UrlEncodeChinese(decryptedUrl), headers);
                        }else{
                            iVV.setVideoPath(Utils.UrlEncodeChinese(Failed), headers);
                        }
                        // 启动15秒准备超时检测
                        mHandler.sendEmptyMessageDelayed(WindowMessageID.PREPARE_TIMEOUT, 15000);
                        mEventHandler.sendEmptyMessage(WindowMessageID.SUCCESS);
                        // 重置解析标志
                        isParsing = false;
                    }else{
                        /*播放地址如果中文用url编码和User-Agent头等信息发送被播放器*/
                        // 先取消之前的超时检测
                        mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
                        if (Failed.equals("")){
                            iVV.setVideoPath(Utils.UrlEncodeChinese(url), headers);
                        }else{
                            iVV.setVideoPath(Utils.UrlEncodeChinese(Failed), headers);
                        }
                        // 启动15秒准备超时检测
                        mHandler.sendEmptyMessageDelayed(WindowMessageID.PREPARE_TIMEOUT, 15000);
                        mEventHandler.sendEmptyMessage(WindowMessageID.SUCCESS);
                        // 重置解析标志
                        isParsing = false;
                    }
                }
            }else{
                // 解析失败，获取错误信息
                String errorMsg = "解析失败";
                if (jSONObject.has("msg")) {
                    errorMsg = jSONObject.getString("msg");
                } else if (jSONObject.has("message")) {
                    errorMsg = jSONObject.getString("message");
                }
                Logger.e(TAG, "解析失败: code=" + code + ", msg=" + errorMsg);
                Utils.showToast(this, errorMsg, R.drawable.toast_err);
                
                // 提交反馈记录
                submitFeedback();
                
                timeout = 1;
                // 重置解析标志
                isParsing = false;
                
                // 根据后端配置决定是否自动换源
                if (Auto_Source == 1) {
                    if (ClientID < MaxClientID) {
                        // 尝试下一个解析接口
                        Utils.showToast(this, "正在尝试其他解析接口...", R.drawable.toast_err);
                        setVideoUrl(Client + Integer.toString(ClientID + 1));
                    } else if (splitCount > 1) {
                        Utils.showToast(this, "正在切换播放源...", R.drawable.toast_err);
                        Switchsource(1);
                    } else {
                        Switchsource(3);
                    }
                } else {
                    // 后端关闭了自动换源
                    if (ClientID < MaxClientID) {
                        Utils.showToast(this, "请手动切换解析接口", R.drawable.toast_err);
                    } else if (splitCount > 1) {
                        Utils.showToast(this, "请手动切换播放源", R.drawable.toast_err);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Logger.e(TAG, "解析响应JSON异常: " + e.getMessage());
            Utils.showToast(this, "解析失败: 响应格式错误", R.drawable.toast_err);
            
            // 提交反馈记录
            submitFeedback();
            
            // 重置解析标志
            isParsing = false;
            
            // 根据后端配置决定是否自动换源
            if (Auto_Source == 1) {
                if (ClientID < MaxClientID) {
                    Utils.showToast(this, "正在尝试其他解析接口...", R.drawable.toast_err);
                    setVideoUrl(Client + Integer.toString(ClientID + 1));
                } else if (splitCount > 1) {
                    Utils.showToast(this, "正在切换播放源...", R.drawable.toast_err);
                    Switchsource(1);
                } else {
                    Switchsource(3);
                }
            } else {
                if (ClientID < MaxClientID) {
                    Utils.showToast(this, "请手动切换解析接口", R.drawable.toast_err);
                } else if (splitCount > 1) {
                    Utils.showToast(this, "请手动切换播放源", R.drawable.toast_err);
                }
            }
        }
    }

    /*解析地址获取失败*/
    public void analysisUrlError(VolleyError volleyError) {
        Logger.e(TAG, "解析URL失败: " + volleyError.toString() + ", Auto_Source=" + Auto_Source);
        String errorMsg = "解析失败";
        
        if (volleyError instanceof TimeoutError) {
            Maxtimeout = 5;
            errorMsg = "请求超时";
        }
        if (volleyError instanceof AuthFailureError) {
            Maxtimeout = 5;
            errorMsg = "认证失败";
        }
        if(volleyError instanceof NetworkError) {
            Maxtimeout = 5;
            errorMsg = "网络错误，请检查网络连接";
        }
        if(volleyError instanceof ServerError) {
            Maxtimeout = 5;
            errorMsg = "服务器错误";
        }
        
        // 提交反馈记录
        submitFeedback();
        
        // 重置错误标志
        isCodeBlockExecuted = false;
        // 重置解析标志
        isParsing = false;
        
        // 根据后端配置决定是否自动换源
        if (Auto_Source == 1) {
            // 后端开启了自动换源
            if (splitCount > 1) {
                Utils.showToast(this, errorMsg + "，正在切换播放源...", R.drawable.toast_err);
                Switchsource(1);
            } else {
                Utils.showToast(this, errorMsg + "，当前只有一个播放源", R.drawable.toast_err);
                Switchsource(3);
            }
        } else {
            // 后端关闭了自动换源
            Utils.showToast(this, errorMsg + "，请手动切换播放源", R.drawable.toast_err);
        }
    }
    
    /*提交反馈记录*/
    private void submitFeedback() {
        try {
            String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
            if (User_url == null || User_url.isEmpty()) {
                Logger.e(TAG, "提交反馈失败: User_url为空");
                return;
            }
            
            final String account = sp.getString("userName", "");
            final String name = Utils.getEcodString(((VideoInfo) videoInfo.get(playIndex)).title);
            final String feedbackUrl = url; // 原始播放地址
            
            if (account == null || account.isEmpty() || name == null || name.isEmpty() || feedbackUrl == null || feedbackUrl.isEmpty()) {
                Logger.e(TAG, "提交反馈失败: 参数不完整 - account=" + account + ", name=" + name + ", url=" + feedbackUrl);
                return;
            }
            
            String feedbackApiUrl = User_url + "/feedback";
            StringRequest feedbackRequest = new StringRequest(Request.Method.POST, feedbackApiUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Logger.d(TAG, "反馈提交成功: " + response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Logger.e(TAG, "反馈提交失败: " + error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("line", "live");
                    params.put("account", account);
                    params.put("vodname", name);
                    params.put("url", feedbackUrl);
                    return params;
                }
            };
            
            // 使用独立的请求队列，避免影响主请求
            RequestQueue feedbackQueue = Volley.newRequestQueue(this, new ExtHttpStack());
            feedbackRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            feedbackQueue.add(feedbackRequest);
            
            Logger.d(TAG, "提交反馈: line=live, account=" + account + ", vodname=" + name + ", url=" + feedbackUrl);
        } catch (Exception e) {
            Logger.e(TAG, "提交反馈异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*设置侦听器*/
    private void setListener() {
        tv_menu.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                /*单击*/
                /*手势监听*/
                hideController();
                showMenu();
            }
        });
        mGestureDetector = new GestureDetector(new SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                /*双击*/
                hideController();
                showMenu();
                return true;
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                // TODO Auto-generated method stub
                /*单击时*/
                if (isControllerShow) {
                    cancelDelayHide();
                    hideController();
                } else {
                    showController();
                    hideControllerDelay();
                }
                hideMenu();
                //return super.onSingleTapConfirmed(e);
                return true;
            }

            public void onLongPress(MotionEvent e) {
                // TODO Auto-generated method stub
                /*长按*/
                //super.onLongPress(e);
                hideController();
                showMenus();
            }

            public boolean onDown(MotionEvent e) {
                // TODO Auto-generated method stub
                /*向下*/
                Logger.i(TAG, "mGestureDetector...onDown");
                return super.onDown(e);
            }

            public boolean onSingleTapUp(MotionEvent e) {
                // TODO Auto-generated method stub
                /*单次点击*/
                Logger.i(TAG, "mGestureDetector...onSingleTapUp");
                return super.onSingleTapUp(e);
            }

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Logger.i(TAG, "mGestureDetector...onFling");
                return false;
            }

            public boolean onDoubleTapEvent(MotionEvent e) {
                // TODO Auto-generated method stub
                Logger.i(TAG, "mGestureDetector...onDoubleTapEvent");
                return super.onDoubleTapEvent(e);
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Logger.i(TAG, "mGestureDetector...onScroll");
                return false;
            }
        });
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                hideControllerDelay();
                mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                /*SeekBar开始seek时停止更新*/
                mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                //ib_rewind.setImageResource(R.drawable.media_rewind);
                //ib_fastForward.setImageResource(R.drawable.media_fastforward)
                updateTextViewWithTimeFormat(tv_currentTime, progress);
                updateprogress();
            }
        });
    }

    /*播放器VV监听*/
    private void setvvListener() {
        /*设置缓冲监听*/
        iVV.setOnPlayingBufferCacheListener(new OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(IMediaPlayer mp, int percent) {

                if (percent > 1){
                    if (percent > 98){
                        seekBar.setSecondaryProgress(iVV.getDuration() / 1000);
                        return;
                    }
                    seekBar.setSecondaryProgress(iVV.getDuration() /1000 * iVV.getBufferPercentage() /100);
                }
            }
        });

        /*设置错误监听*/
        iVV.setOnErrorListener(new OnErrorListener() {
            public boolean onError(IMediaPlayer arg0, int what, int extra) {
                Logger.e(TAG, "========== 播放错误 ==========");
                Logger.e(TAG, "what=" + what + ", extra=" + extra);
                Logger.e(TAG, "isCodeBlockExecuted=" + isCodeBlockExecuted + ", PlayersNumber=" + PlayersNumber);
                
                synchronized (SYNC_Playing) {
                    if (isCodeBlockExecuted) {
                        return true; // 如果代码块已经执行过，则直接返回，避免重复执行
                    }
                    SYNC_Playing.notify();
                    Logger.i(TAG, "onError...SYNC_Playing.notify()");
                    
                    // 根据错误类型显示不同提示
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
                    Logger.e(TAG, "错误详情: " + errorMsg);

                    /*最后添加失败不换解码尝试*/
                    mHandler.sendEmptyMessage(WindowMessageID.PLAY_ERROR);

                }
                isCodeBlockExecuted = true; // 标记代码块已经执行过
                return true;
            }
        });

        /*设置就绪监听*/
        iVV.setOnPreparedListener(new OnPreparedListener() {
            public void onPrepared(IMediaPlayer arg0) {
                // TODO Auto-generated method stub
                //Log.v(TAG, "setOnPreparedListener");
                // 取消准备超时检测
                mHandler.removeMessages(WindowMessageID.PREPARE_TIMEOUT);
                isCodeBlockExecuted = false; // 重置错误标志
                
                /*清除缓冲进度*/
                seekBar.setSecondaryProgress(0);
                if (memory_source  == 1){
                    /*保存当前频道源*/
                    SP.edit().putInt(videoInfo.get(playIndex).title, PlayersNumber).commit();
                }
                /*开启视频跑马公告*/
                mediaHandler.sendEmptyMessageDelayed(WindowMessageID.START_NOTICE_GONE, Vod_Notice_starting_time * 1000);


                mediaHandler.sendEmptyMessageDelayed(WindowMessageID.TV_COVER, 600);////换台遮挡
                mediaHandler.sendEmptyMessageDelayed(WindowMessageID.EPG, 1000);////EPG显示框延迟关闭


                mPlayerStatus = PLAYER_STATUS.PLAYER_PREPARED;
                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                mediaHandler.sendEmptyMessage(WindowMessageID.SELECT_SCALES);
                mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
            }
        });

        /*设置完成监听*/
        iVV.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(IMediaPlayer arg0) {
                // TODO Auto-generated method stub
                synchronized (SYNC_Playing) {
                    SYNC_Playing.notify();
                    Logger.i(TAG, "onCompletion...SYNC_Playing.notify()");
                }
                mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
                mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                Logger.i(TAG, "isNext=" + isNext + ".....isLast=" + isLast + "....isPause=" + isPause + "....isDestroy=" + isDestroy);
                if (isNext.booleanValue()) {
//                    if (videoInfo.size() > playIndex + 1) {
//                        playIndex = playIndex + 1;
//                        xjposition = playIndex;
//                        mLastPos = 0;
//                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
//                    } else {
//                        iVV.stopPlayback();
//                        finish();
//                    }
                    isNext = Boolean.valueOf(false);
                } else if (isLast.booleanValue()) {
                    mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                    if (playIndex > 0) {
                        playIndex = playIndex - 1;
                        xjposition = playIndex;
                        mLastPos = 0;
                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                    }
                    isLast = Boolean.valueOf(false);
                } else if (isPause.booleanValue()) {
                    isPause = Boolean.valueOf(false);
                } else if (!isDestroy.booleanValue()) {
                    mHandler.sendEmptyMessage(WindowMessageID.HIDE_MENU);
//                    if (videoInfo.size() > playIndex + 1) {
//                        playIndex = playIndex + 1;
//                        xjposition = playIndex;
//                        mLastPos = 0;
//                        SelecteVod(playIndex);
//                        iVV.start();
//                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
//                        return;
//                    }
//                    iVV.stopPlayback();
//                    finish();
                }
            }
        });

        /*设置信息监听*/
        iVV.setOnInfoListener(new OnInfoListener() {
            public boolean onInfo(IMediaPlayer arg0, int what, int extra) {
                //Log.v(TAG, "setOnInfoListener" + what + "--" + extra);
                switch (what) {
                    /*开始缓冲*/
                    case IjkMediaPlayer.MEDIA_INFO_BUFFERING_START:/*701*/
                        /*显示缓冲转圈*/
//                        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
//                        mHandler.removeMessages(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                        break;
                    case IjkMediaPlayer.MEDIA_INFO_BUFFERING_END:/*702*/
                        /*不显示缓冲转圈*/
//                        mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
//                        mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                        break;
                }
                return true;
            }
        });

    }

    /*获取屏幕大小*/
    private void getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
        controlHeight = screenHeight / 4;
    }

    /*显示控制器*/
    private void showController() {
        if (iVV != null) {
            tv_time.setText(Utils.getStringTime(":"));
            time_controler.setAnimationStyle(R.style.AnimationTimeFade);
            time_controler.showAtLocation(iVV, Gravity.TOP, 0, 0);
            controler.setAnimationStyle(R.style.AnimationFade);
            controler.showAtLocation(iVV, Gravity.BOTTOM, 0, 0);
            time_controler.update(0, 0, screenWidth, controlHeight / 3);
            controler.update(0, 0, screenWidth, controlHeight / 2);
            isControllerShow = true;
            mHandler.sendEmptyMessageDelayed(WindowMessageID.HIDE_CONTROLER, TIME);
        }
    }

    /*隐藏控制器*/
    private void hideController() {
        if (controler != null && controler.isShowing()) {
            controler.dismiss();
            time_controler.dismiss();
            isControllerShow = false;
        }
    }

    /*取消延迟隐藏*/
    private void cancelDelayHide() {
        mHandler.removeMessages(WindowMessageID.HIDE_CONTROLER);
    }

    /*隐藏控制器延迟*/
    private void hideControllerDelay() {
        cancelDelayHide();
        mHandler.sendEmptyMessageDelayed(WindowMessageID.HIDE_CONTROLER, TIME);
    }

    /*显示菜单*/
    private void showMenu() {
        if (menupopupWindow != null) {
            vmAdapter = new LiveMenuAdapter(this, LivePlayUtils.getData(0), 6, Boolean.valueOf(isMenuItemShow));
            menulist.setAdapter(vmAdapter);
            menupopupWindow.setAnimationStyle(R.style.AnimationMenu);
            menupopupWindow.showAtLocation(iVV, Gravity.TOP | Gravity.RIGHT, 0, 0);
            menupopupWindow.update(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_364), screenHeight);
            isMenuShow = true;
            isMenuItemShow = false;
            return;
        }
        Utils.showToast(this, R.string.incomplete, R.drawable.toast_shut);
    }

    /*显示二级菜单*/
    private void showMenus() {
        if (menupopupWindows != null) {
//            vmAdapters = new LiveMenuAdapter(this, videoInfos, 0, Boolean.valueOf(isMenuItemShows));
//            vmAdapterss = new LiveMenuAdapter(this, videoInfo, 0, Boolean.valueOf(isMenuItemShowss));
            vmAdapters = new LiveMenuAdapter(this, videoInfos, 0, false,true,false);
            vmAdapterss = new LiveMenuAdapter(this, videoInfo, 0, false,false,true);
            menulists.setAdapter(vmAdapters);
            menulists.setSelection(gsposition);
            menulistss.setAdapter(vmAdapterss);
            menulistss.setSelection(xjposition);
            menulistss.requestFocus();
            menupopupWindows.showAtLocation(iVV, Gravity.TOP | Gravity.LEFT, 0, 0);
            menupopupWindows.update(0, 0, getResources().getDimensionPixelSize(R.dimen.sm_683), screenHeight);
            return;
        }
        Utils.showToast(this, R.string.incomplete, R.drawable.toast_shut);
    }

    /*隐藏菜单*/
    private void hideMenu() {
        if (Navigation_mode == 1){
            Navigation();
        }
        if (menupopupWindow != null && menupopupWindow.isShowing()) {
            menupopupWindow.dismiss();
        }
        if (menupopupWindows != null && menupopupWindows.isShowing()) {
            menupopupWindows.dismiss();
        }
    }

    /*窗口消息处理函数*/
    /*msg 窗口消息*/
    private void onMessage(Message msg) {
        if (msg != null) {
            switch (msg.what) {
                case WindowMessageID.ERROR:
                    if (ClientID < MaxClientID){
                        isParsing = false; // 重置解析标志
                        setVideoUrl(Client + Integer.toString(ClientID + 1 ));
                    }else{
                        if (Auto_Source == 1){
                            /*换源*/
                            Switchsource(1);
                        }else{
                            /*关闭菊花*/
                            Switchsource(3);
                        }
                    }
                    return;
                case WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION:
                    int currPosition = iVV.getCurrentPosition();
                    videoLength = iVV.getDuration();
                    updateTextViewWithTimeFormat(tv_currentTime, currPosition / 1000);
                    updateTextViewWithTimeFormat(tv_totalTime, videoLength / 1000);
                    seekBar.setMax(videoLength / 1000);
                    seekBar.setProgress(currPosition / 1000);
                    mLastPos = currPosition;
                    mHandler.sendEmptyMessageDelayed(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION, 200);
                    return;
                case WindowMessageID.HIDE_CONTROLER:
                    hideController();
                    return;
                case WindowMessageID.HIDE_PROGRESS_TIME:
                    tv_progress_time.setVisibility(View.GONE);
                    return;
                case WindowMessageID.HIDE_MENU:
                    hideMenu();
                    return;
                case WindowMessageID.PLAY_ERROR:/*上报*/
                    if (ClientID < MaxClientID){
                        Utils.showToast(LivePlayerActivity.this, "播放失败，正在尝试其他解析接口...", R.drawable.toast_err);
                        isParsing = false; // 重置解析标志
                        setVideoUrl(Client + Integer.toString(ClientID + 1));
                    } else {
                        // 根据后端配置决定是否自动换源
                        if (Auto_Source == 1) {
                            // 后端开启了自动换源
                            if (splitCount > 1) {
                                Utils.showToast(LivePlayerActivity.this, "播放失败，正在切换播放源...", R.drawable.toast_err);
                                Switchsource(1);
                            } else {
                                Utils.showToast(LivePlayerActivity.this, "播放失败，当前只有一个播放源", R.drawable.toast_err);
                                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                                Switchsource(3);
                            }
                        } else {
                            // 后端关闭了自动换源
                            Utils.showToast(LivePlayerActivity.this, "播放失败，请手动切换播放源", R.drawable.toast_err);
                            mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                        }
                    }
                    return;
                case WindowMessageID.SWITCH_CODE:
                    switchCode();
                    isSwitch = false;
                    return;
                case WindowMessageID.PREPARE_TIMEOUT:
                    isCodeBlockExecuted = false; // 重置错误标志
                    isParsing = false; // 重置解析标志
                    if (ClientID < MaxClientID){
                        Utils.showToast(LivePlayerActivity.this, "加载超时，正在尝试其他解析接口...", R.drawable.toast_err);
                        setVideoUrl(Client + Integer.toString(ClientID + 1));
                    } else {
                        // 根据后端配置决定是否自动换源
                        if (Auto_Source == 1) {
                            // 后端开启了自动换源
                            if (splitCount > 1) {
                                Utils.showToast(LivePlayerActivity.this, "加载超时，正在切换播放源...", R.drawable.toast_err);
                                Switchsource(1);
                            } else {
                                Utils.showToast(LivePlayerActivity.this, "加载超时，当前只有一个播放源", R.drawable.toast_err);
                                mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                                Switchsource(3);
                            }
                        } else {
                            // 后端关闭了自动换源
                            Utils.showToast(LivePlayerActivity.this, "加载超时，请手动切换播放源", R.drawable.toast_err);
                            mediaHandler.sendEmptyMessage(WindowMessageID.COLSE_SHOW_TV);
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /*使用时间格式更新文本视图*/
    private void updateTextViewWithTimeFormat(TextView view, int second) {
        int mm = (second % 3600) / 60;
        int ss = second % 60;
        view.setText(String.format("%02d:%02d:%02d", Integer.valueOf(second / 3600), Integer.valueOf(mm), Integer.valueOf(ss)));
    }

    private void initMessage(int what) {
        Message msg = new Message();
        msg.what = WindowMessageID.SELECT_CHANNE;
        //mediaHandler.removeMessages(WindowMessageID.SELECT_CHANNE);
        keyChanne = keyChanne + what;
        mProgramNum.setText(keyChanne);
//        if (Integer.parseInt(keyChanne) < videoInfo.size()){
//            playIndex = Integer.parseInt(keyChanne);
//            xjposition = Integer.parseInt(keyChanne);
//        }
        mediaHandler.sendMessageDelayed(msg, 2000);
    }

    /*调度键盘事件*/
    private boolean isUpKeyPressed = false;
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            if (isBack) {
                hideControllerDelay();
                iVV.seekTo(mLastPos);
                mHandler.sendEmptyMessage(WindowMessageID.UI_EVENT_UPDATE_CURRPOSITION);
                isBack = false;
            }
            return super.dispatchKeyEvent(event);
        }
        int keyCode = event.getKeyCode();
        long secondTime;
        switch (keyCode) {
            case KeyEvent.KEYCODE_0:
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
            case KeyEvent.KEYCODE_9:
                initMessage(event.getKeyCode() - KeyEvent.KEYCODE_0);
                break;
            case KeyEvent.KEYCODE_BACK:/*返回键*/
                hideController();
                secondTime = System.currentTimeMillis();
                if (secondTime - firstTime <= 2000) {
                    /*记忆上次的频道*/
                    if (memory_channel == 1){
                        SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "LIVEs",playIndex);
                        SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "gsLIVEs",gsplayIndex);
                    }else{
                        SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "LIVEs",0);
                    }
                    isDestroy = Boolean.valueOf(true);
                    stopPlayback();
                    finish();
                    break;
                }
                Utils.showToast(this, R.string.onbackpressed, R.drawable.toast_err);
                firstTime = secondTime;
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:/*上键*/
                /*正常按键*/
                /*
                KEYCODE_DPAD_UP = true;
                KEYCODE_DPAD_DOWN = false;
                if (playPreCode != 0) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
                    break;
                }
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                    if (playIndex <= 0) {
                        playIndex = videoInfo.size()-1;
                    }else{
                        playIndex--;
                    }
                    xjposition = playIndex;
                    SelecteVod(playIndex);
                    mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                }
                if (!isControllerShow) {
                    showController();
                }
                mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                break;
                */
                /*防止连按*/
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (!isUpKeyPressed) {
                        isUpKeyPressed = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isUpKeyPressed = false;
                            }
                        }, 1000);
                        KEYCODE_DPAD_UP = true;
                        KEYCODE_DPAD_DOWN = false;
                        if (playPreCode != 0) {
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
                            return true;
                        }
                        if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                            if (reverse == 0){
                                if (playIndex <= 0) {
                                    playIndex = videoInfo.size()-1;
                                } else {
                                    playIndex--;
                                }
                            }else{
                                if (videoInfo.size() <= playIndex + 1) {
                                    playIndex = 0;
                                }else{
                                    playIndex++;
                                }
                            }
                            xjposition = playIndex;



                            if ((playIndex + 2)  <= Integer.parseInt(videoInfos.get(gsplayIndex).url)) {
                                gsplayIndex--;
                                gsposition = gsplayIndex;
                            }

                            SelecteVod(playIndex);
                            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        }
                        if (!isControllerShow) {
                            //showController();////
                        }
                        mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                    }
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_DPAD_DOWN:/*下键*/
                /*正常按键*/
                /*
                KEYCODE_DPAD_UP = false;
                KEYCODE_DPAD_DOWN = true;
                if (playPreCode != 0) {
                    mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
                    break;
                }
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                    if (videoInfo.size() <= playIndex + 1) {
                        playIndex = 0;
                    }else{
                        playIndex = playIndex + 1;
                    }
                    xjposition = playIndex;
                    SelecteVod(playIndex);
                    mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                }
                if (!isControllerShow) {
                    showController();
                }
                mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                break;
                */
                /*防止连按*/
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (!isUpKeyPressed) {
                        isUpKeyPressed = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isUpKeyPressed = false;
                            }
                        }, 1000);
                        KEYCODE_DPAD_UP = false;
                        KEYCODE_DPAD_DOWN = true;
                        if (playPreCode != 0) {
                            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
                            return true;
                        }
                        if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                            if (reverse == 0) {
                                if (videoInfo.size() <= playIndex + 1) {
                                    playIndex = 0;
                                } else {
                                    playIndex++;
                                }
                            }else{
                                if (playIndex <= 0) {
                                    playIndex = videoInfo.size()-1;
                                } else {
                                    playIndex--;
                                }
                            }
                            xjposition = playIndex;


                            if (gsplayIndex < videoInfos.size() - 1 && (playIndex + 1) >= Integer.parseInt(videoInfos.get(gsplayIndex + 1).url)) {
                                gsplayIndex++;
                                gsposition = gsplayIndex;
                            }




                            SelecteVod(playIndex);
                            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        }
                        if (!isControllerShow) {
                            //showController();////
                        }
                        mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                    }
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            case WindowMessageID.HIDE_PROGRESS_TIME:
                /*正常按键*/
                /*if (!isControllerShow) {
                    showController();
                }
                Utils.showToast(LivePlayerActivity.this, "已切换备用源", R.drawable.toast_smile);
                rewind(1);
                break;*/
                /*防止连按*/
                if (keyCode == WindowMessageID.HIDE_PROGRESS_TIME) {
                    if (!isUpKeyPressed) {
                        isUpKeyPressed = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isUpKeyPressed = false;
                            }
                        }, 3000);
                        if (!isControllerShow) {
                            //showController();////
                        }
//                        Utils.showToast(LivePlayerActivity.this, "已切换备用源", R.drawable.toast_smile);
                        rewind(1);
                    }
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            case WindowMessageID.HIDE_MENU:
                /*正常按键*/
                /*
                if (!isControllerShow) {
                    showController();
                }
                Utils.showToast(LivePlayerActivity.this, "已切换备用换源", R.drawable.toast_smile);
                fastForward(1);
                break;
                */
                /*防止连按*/
                if (keyCode == WindowMessageID.HIDE_MENU) {
                    if (!isUpKeyPressed) {
                        isUpKeyPressed = true;
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isUpKeyPressed = false;
                            }
                        }, 3000);
                        if (!isControllerShow) {
                            //showController();////
                        }
//                        Utils.showToast(LivePlayerActivity.this, "已切换备用换源", R.drawable.toast_smile);
                        fastForward(1);
                    }
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_MENU:
                hideController();
                showMenu();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER://遥控器OK键
                hideController();
                showMenus();
                break;
            case KeyEvent.KEYCODE_ENTER://键盘回车键
                hideController();
                showMenus();
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    /*快进*/
    private void fastForward(int postion) {
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        isSwitch = true;
        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
        mLastPos = 0;
        TextView textView = tv_mv_name;
        textView.setText( videoInfo.get(playIndex).title);

        stopPlayback();
        if (PlayersNumber + postion > splitCount){
            PlayersNumber = 0;
            url = parts[0];
        }else{
            PlayersNumber = PlayersNumber + postion;
            url = parts[PlayersNumber];
        }
        if (PlayersNumber + 1 > parts.length){
            tv_srcinfo1.setText("源:" + PlayersNumber + "/" + parts.length);////EPG1源信息
            tv_srcinfo2.setText("源:" + PlayersNumber + "/" + parts.length);////EPG2源信息
        }else{
            tv_srcinfo1.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG1源信息
            tv_srcinfo2.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG2源信息
        }
        isParsing = false; // 重置解析标志
        setVideoUrl(Client);
    }

    /*快退*/
    private void rewind(int postion) {
        mPlayerStatus = PLAYER_STATUS.PLAYER_IDLE;
        isSwitch = true;
        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
        mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
        mLastPos = 0;
        TextView textView = tv_mv_name;
        textView.setText( videoInfo.get(playIndex).title);

        stopPlayback();
        if (PlayersNumber - postion < 0){
            PlayersNumber = splitCount;
            url = parts[splitCount];
        }else{
            PlayersNumber = PlayersNumber - postion;
            url = parts[PlayersNumber];
        }
//        if (PlayersNumber - 1 > parts.length){
//            tv_srcinfo1.setText("源:" + PlayersNumber + "/" + parts.length);////EPG1源信息
//            tv_srcinfo2.setText("源:" + PlayersNumber + "/" + parts.length);////EPG2源信息
//        }else{
//            tv_srcinfo1.setText("源:" + (PlayersNumber - 1) + "/" + parts.length);////EPG1源信息
//            tv_srcinfo2.setText("源:" + (PlayersNumber - 1) + "/" + parts.length);////EPG2源信息
//        }


        tv_srcinfo1.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG1源信息
        tv_srcinfo2.setText("源:" + (PlayersNumber + 1) + "/" + parts.length);////EPG2源信息

        isParsing = false; // 重置解析标志
        setVideoUrl(Client);

    }

    /*重置电影时间*/
    private void ResetMovieTime() {
        updateTextViewWithTimeFormat(tv_currentTime, 0);
        updateTextViewWithTimeFormat(tv_totalTime, 0);
        seekBar.setProgress(0);
    }

    /*菜单信息*/
    public void onCreateMenu() {
        View menuView = View.inflate(this, R.layout.mv_controler_menu, null);
        menulist = menuView.findViewById(R.id.media_controler_menu);
        menupopupWindow = new PopupWindow(menuView, -2, -2);
        menupopupWindow.setOutsideTouchable(true);
        menupopupWindow.setTouchable(true);
        menupopupWindow.setFocusable(true);
        /*鼠标点击*/
        menulist.setOnItemClickListener(new OnItemClickListener() {
            @SuppressLint("WrongConstant")
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                if (isMenuShow) {
                    isMenuShow = false;
                    isMenuItemShow = true;
                    switch (position) {
//                        case 0:
//                            /*选集*/
//                            menutype = 0;
//                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, videoInfo, 0, Boolean.valueOf(isMenuItemShow)));
//                            menulist.setSelection(xjposition);
//                            return;
                        case 0:
                            /*解码*/
                            menutype = 1;
                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, LivePlayUtils.getData(1), 1, Boolean.valueOf(isMenuItemShow)));
                            menulist.setSelection(jmposition);
                            return;
                        case 1:
                            /*比例*/
                            menutype = 2;
                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, LivePlayUtils.getData(2), 2, Boolean.valueOf(isMenuItemShow)));
                            menulist.setSelection(hmblposition);
                            return;
                        case 2:
                            /*偏好*/
                            menutype = 3;
                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, LivePlayUtils.getData(3), 3, Boolean.valueOf(isMenuItemShow)));
                            menulist.setSelection(phszposition);
                            return;
                        case 3:
                            /*内核*/
                            menutype = 4;
                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, LivePlayUtils.getData(4), 4, Boolean.valueOf(isMenuItemShow)));
                            menulist.setSelection(nhposition);
                            return;
                        case 4:
                            /*清理数据*/
                            menutype = 5;
                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, LivePlayUtils.getData(5), 5, Boolean.valueOf(isMenuItemShow)));
                            menulist.setSelection(qlposition);
                            return;
                        default:
                            return;
                    }
                } else if (isMenuItemShow) {
                    Editor editor;
                    switch (menutype) {
                        case 0:
                            /*选集*/
                            if (videoInfo.size() > position) {
                                isNext = Boolean.valueOf(true);
                                isSwitch = true;
                                playIndex = position;
                                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                                    SelecteVod(playIndex);
                                    mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                                }
                                if (!isControllerShow) {
                                    //showController();////
                                }
                                mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                            }
                            xjposition = position;
                            hideMenu();
                            return;
                        case 1:
                            /*解码*/
                            jmposition = position;
                            if (position == 0) {
                                editor = sp.edit();
                                editor.putInt("mIsHwDecode", 0);
                                editor.putString(Constant.mg, "软解码");
                                editor.commit();
                            } else if (position == 1) {
                                editor = sp.edit();
                                editor.putInt("mIsHwDecode", 1);
                                editor.putString(Constant.mg, "硬解码");
                                editor.commit();
                            }
                            setDecode();
                            if (position == 1) {
                                iVV.setDecode(Boolean.valueOf(true));
                            } else if (position == 0) {
                                iVV.setDecode(Boolean.valueOf(false));
                            }
                            isPause = Boolean.valueOf(true);
                            if (iVV.isPlaying()) {
                                mLastPos = iVV.getCurrentPosition();
                            }
                            if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                                mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                                iVV.resume();
                            }
                            hideMenu();
                            return;
                        case 2:
                            /*画面比例*/
                            hmblposition = position;
                            selectScales(hmblposition);
                            editor = sp.edit();
                            if (position == 0) {
                                editor.putString(Constant.oh, "原始比例");
                                editor.commit();
                            } else if (position == 1) {
                                editor.putString(Constant.oh, "4:3 缩放");
                                editor.commit();
                            } else if (position == 2) {
                                editor.putString(Constant.oh, "16:9缩放");
                                editor.commit();
                            } else if (position == 3) {
                                editor.putString(Constant.oh, "全屏拉伸");
                                editor.commit();
                            }else if (position == 4) {
                                editor.putString(Constant.oh, "等比缩放");
                                editor.commit();
                            }else if (position == 5) {
                                editor.putString(Constant.oh, "全屏裁剪");
                                editor.commit();
                            }
                            hideMenu();
                            return;
                        case 3:
                            /*偏好设置*/
                            phszposition = position;
                            editor = sp.edit();
                            if (position == 0) {
                                editor.putInt("playPre", 0);
                                editor.commit();
                            } else if (position == 1) {
                                editor.putInt("playPre", 1);
                                editor.commit();
                            }
                            getPlayPreferences();
                            hideMenu();
                            return;
                        case 4:
                            //内核
                            nhposition = position;
                            if (position == 0) {
                                editor = sp.edit();
                                editor.putString("live_core", "自动");
                                editor.commit();
                            } else if (position == 1) {
                                editor = sp.edit();
                                editor.putString("live_core", "系统");
                                editor.commit();
                            } else if (position == 2) {
                                editor = sp.edit();
                                editor.putString("live_core", "IJK");
                                editor.commit();
                            } else if (position == 3) {
                                editor = sp.edit();
                                editor.putString("live_core", "EXO");
                                editor.commit();
                            } else if (position == 4) {
                                editor = sp.edit();
                                editor.putString("live_core", "阿里");
                                editor.commit();
                            }
                            if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {
                                SelecteVod(playIndex);
                                mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                            }
                            hideMenu();
                            return;
                        case 5:
                            /*清理数据*/
                            qlposition = position;
                            if (position == 0) {
                                SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "LIVEs",0);
                                editor = SP.edit();
                                editor.clear(); // 清空所有数据
                                editor.apply(); // 或者使用 editor.commit();
                                stopPlayback();
                                finish();
                                Utils.showToast(LivePlayerActivity.this, R.string.Data_cache_cleaning_successful, R.drawable.toast_smile);
                            } else if (position == 1) {
                                SharePreferenceDataUtil.setSharedIntData(LivePlayerActivity.this, "LIVEs",0);
                                stopPlayback();
                                finish();
                                Utils.showToast(LivePlayerActivity.this, R.string.memory_channel, R.drawable.toast_smile);
                            } else if (position == 2) {
                                editor = SP.edit();
                                editor.clear(); // 清空所有数据
                                editor.apply(); // 或者使用 editor.commit();
                                stopPlayback();
                                finish();
                                Utils.showToast(LivePlayerActivity.this, R.string.memory_source, R.drawable.toast_smile);
                            }
                            hideMenu();
                            return;
                        default:
                            return;
                    }
                }
            }
        });


        /*键盘按下*/
        menulist.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != 1) {
                    switch (keyCode) {
                        case 4:
                            if (!isMenuItemShow) {
                                if (isMenuShow) {
                                    menupopupWindow.dismiss();
                                    break;
                                }
                            }
                            isMenuShow = true;
                            isMenuItemShow = false;
                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, LivePlayUtils.getData(0), 5, Boolean.valueOf(isMenuItemShow)));
//                            menulist.setAdapter(new LiveMenuAdapter(LivePlayerActivity.this, LivePlayUtils.getData(0), 6, Boolean.valueOf(isMenuItemShow)));
                            break;
                    }
                }
                return false;
            }
        });

    }

    /*二级菜单信息*/
    public void onCreateMenus() {
        View menuView = View.inflate(this, R.layout.mv_controler_menus, null);
        menulists = menuView.findViewById(R.id.media_controler_menu_left);
        menulistss = menuView.findViewById(R.id.media_controler_menu_right);
        menupopupWindows = new PopupWindow(menuView, -2, -2);
        menupopupWindows.setOutsideTouchable(true);
        menupopupWindows.setTouchable(true);
        menupopupWindows.setFocusable(true);
        /*鼠标点击*/
        menulists.setOnItemClickListener(new OnItemClickListener() {
            @SuppressLint("WrongConstant")
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                menulists.setSelection(Integer.parseInt(videoInfos.get(position).url));
//                gsposition = Integer.parseInt(videoInfos.get(position).url);
//                menulistss.setSelection(Integer.parseInt(videoInfos.get(position).url));
                gsplayIndex = position;
                gsposition = gsplayIndex;
                menulists.setSelection(gsposition);
                showMenus();
                menulistss.setSelection(Integer.parseInt(videoInfos.get(position).url)-1);
            }
        });


        /*键盘按下*/
        menulists.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != 1) {
                    switch (keyCode) {
                        case 4:
//                            if (!isMenuItemShow) {
//                                if (isMenuShow) {
//                                    menupopupWindows.dismiss();
//                                    break;
//                                }
//                            }
//                            isMenuShow = true;
//                            isMenuItemShow = false;
                            menupopupWindows.dismiss();
                            break;
                    }
                }
                return false;
            }
        });

        /*鼠标点击*/
        menulistss.setOnItemClickListener(new OnItemClickListener() {
            @SuppressLint("WrongConstant")
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                menulistss.setSelection(position);

                if (videoInfo.size() > position) {
                        isNext = Boolean.valueOf(true);
                        isSwitch = true;
                        playIndex = position;
                        if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE) {

                            int maxUrlValueSoFar = -1; // 用于存储小于或等于position的最大url值
                            int maxUrlIndex = -1; // 用于存储对应最大url值的索引

                            for (int i = 0; i < videoInfos.size(); i++) {
                                try {
                                    int currentUrlValue = Integer.parseInt(videoInfos.get(i).url);

                                    if (currentUrlValue <= position + 1 && currentUrlValue > maxUrlValueSoFar) {
                                        // 更新最大url值和对应的索引
                                        maxUrlValueSoFar = currentUrlValue;
                                        maxUrlIndex = i;
                                    } else if (currentUrlValue > position + 1) {
                                        break;
                                    }
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (maxUrlIndex != -1) {
                                gsplayIndex = maxUrlIndex;
                                gsposition = gsplayIndex;
                            }





//                            if (gsplayIndex < videoInfos.size() - 1 && (playIndex + 1) >= Integer.parseInt(videoInfos.get(gsplayIndex + 1).url)) {
//                                gsplayIndex++;
//                                gsposition = gsplayIndex;
//                            }



//                            if ((playIndex + 2)  <= Integer.parseInt(videoInfos.get(gsplayIndex).url)) {
//                                gsplayIndex--;
//                                gsposition = gsplayIndex;
//                            }

                            SelecteVod(playIndex);
                            mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
                        }
                        if (!isControllerShow) {
                            //showController();////
                        }
                        mediaHandler.sendEmptyMessage(WindowMessageID.RESET_MOVIE_TIME);
                    }
                    xjposition = position;
                    hideMenu();
                    return;
            }
        });

        /*键盘按下*/
        menulistss.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != 1) {
                    switch (keyCode) {
                        case 4:
                            if (!isMenuItemShow) {
                                if (isMenuShow) {
                                    menupopupWindows.dismiss();
                                    break;
                                }
                            }
                            isMenuShow = true;
                            isMenuItemShow = false;
                            break;
                    }
                }
                return false;
            }
        });

    }

    /*分辨率切换*/
    private void selectScales(int paramInt) {
        if (iVV != null) {
            /*比例切换*/
            switch (paramInt) {
                case 0:
                    /*原始比例*/
                    iVV.toggleAspectRatio(IRenderView.AR_ASPECT_WRAP_CONTENT);
                    break;
                case 1:
                    /*4:3比例*/
                    iVV.toggleAspectRatio(IRenderView.AR_4_3_FIT_PARENT);
                    break;
                case 2:
                    /*16:9比例*/
                    iVV.toggleAspectRatio(IRenderView.AR_16_9_FIT_PARENT);
                    break;
                case 3:
                    /*全屏拉伸*/
                    iVV.toggleAspectRatio(IRenderView.AR_MATCH_PARENT);
                    break;
                case 4:
                    /*等比缩放*/
                    iVV.toggleAspectRatio(IRenderView.AR_ASPECT_FIT_PARENT);
                    break;
                case 5:
                    /*全屏裁剪*/
                    iVV.toggleAspectRatio(IRenderView.AR_ASPECT_FILL_PARENT);
                    break;
            }
        }
    }

    /*触摸式事件*/
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        boolean result = mGestureDetector.onTouchEvent(event);
        DisplayMetrics screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);
        if (mSurfaceYDisplayRange == 0)
            mSurfaceYDisplayRange = Math.min(screen.widthPixels,
                    screen.heightPixels);
        float y_changed = event.getRawY() - mTouchY;
        float x_changed = event.getRawX() - mTouchX;

        float coef = Math.abs(y_changed / x_changed);
        float xgesturesize = ((x_changed / screen.xdpi) * 2.54f);
        Logger.i("joychang", "y_changed="+y_changed+"...x_changed="+x_changed+"...coef="+coef+"...xgesturesize="+xgesturesize);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Logger.i(TAG, "MotionEvent.ACTION_DOWN.......");
                boolean isSeekTouch = true;
                mTouchAction = TOUCH_NONE;
                mTouchY = event.getRawY();
                mTouchX = event.getRawX();
                maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Lightness = Utils.GetLightness(LivePlayerActivity.this);
                break;
            case MotionEvent.ACTION_MOVE:
                Logger.i(TAG, "MotionEvent.ACTION_MOVE.......");
                if(coef > 2){
                    isSeekTouch = false;
                    /*音量和亮度*/
                    if(mTouchX > (screenWidth / 2)){
                        /*音量*/
                        doVolumeTouch(y_changed);
                    }
                    if (mTouchX < (screenWidth / 2)) {
                        /*亮度*/
                        doBrightnessTouch(y_changed);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mPlayerStatus != PLAYER_STATUS.PLAYER_IDLE){
//                    switch (bsposition) {
//                        case 0:
//                            setSpeed(0.5f,1);
//                            break;
//                        case 1:
//                            setSpeed(0.75f,1);
//                            break;
//                        case 2:
//                            setSpeed(1.0f,1);
//                            break;
//                        case 3:
//                            setSpeed(1.25f,1);
//                            break;
//                        case 4:
//                            setSpeed(1.5f,1);
//                            break;
//                        case 5:
//                            setSpeed(2.0f,1);
//                            break;
//                        case 6:
//                            setSpeed(3.0f,1);
//                            break;
//                        case 7:
//                            setSpeed(4.0f,1);
//                            break;
//                        case 8:
//                            setSpeed(5.0f,1);
//                            break;
//                    }
                }
                Logger.i(TAG, "MotionEvent.ACTION_UP.......");
                break;
        }
        return true;
    }

    /*调节音量*/
    private void doVolumeTouch(float y_changed){
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_VOLUME)
            return;
        mTouchAction = TOUCH_VOLUME;
        int delta = -(int) ((y_changed / mSurfaceYDisplayRange) * maxVolume);
        int vol = (int) Math.min(Math.max(currentVolume + delta, 0), maxVolume);
        Logger.d("doVolumeTouch", "vol===="+vol+"...delta="+delta);
        if (delta != 0) {
            if(vol < 1 ){
                showVolumeToast(R.drawable.mv_ic_volume_mute, maxVolume, vol,true);
            }else if(vol >= 1 && vol < maxVolume / 2){
                showVolumeToast(R.drawable.mv_ic_volume_low, maxVolume, vol,true);
            }else if(vol >= maxVolume / 2){
                showVolumeToast(R.drawable.mv_ic_volume_high, maxVolume, vol,true);
            }
        }
    }

    /*调节亮度*/
    private void doBrightnessTouch(float y_changed){
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_BRIGHTNESS)
            return;
        mTouchAction = TOUCH_BRIGHTNESS;
        float delta = -y_changed / mSurfaceYDisplayRange * 2f;
        int vol = (int) ((Math.min(Math.max(Lightness + delta, 0.01f)*255, 255)));
        if (delta != 0) {
            if(vol<5){
                showVolumeToast(R.drawable.mv_ic_brightness, 255, 0,false);
            }else{
                showVolumeToast(R.drawable.mv_ic_brightness, 255,vol,false);
            }
            Logger.d("doBrightnessTouch", "Lightness="+Lightness+"....vol="+vol+"...delta="+delta+"....mSurfaceYDisplayRange="+mSurfaceYDisplayRange);
        }
    }

    /*单击事件*/
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.ib_playStatus:
                if(!isControllerShow)
                    showController();
                break;
        }
    }

    /*显示音量吐司*/
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
        mToast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL,0, 0);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.show();
    }

    /*显示网速*/
    private void startSpeed() {
        mSpeedHandler.removeCallbacks(speedRunnabless);////移除卡顿校验
        mSpeedHandler.removeCallbacks(speedRunnable);
        lastRxByte = TrafficStats.getTotalRxBytes();
        lastSpeedTime = System.currentTimeMillis();
        mSpeedHandler.postDelayed(speedRunnable, 0);
        if (tv_mv_speed != null) {
            tv_mv_speed.setVisibility(View.VISIBLE);
        }


        if (epg == 1){
            ll_epg1.setVisibility(View.VISIBLE);////EPG1显示
        }else if (epg == 2){
            ll_epg2.setVisibility(View.VISIBLE);////EPG2显示
        }else{
//            ll_epg1.setVisibility(View.GONE);////EPG1隐藏
//            ll_epg2.setVisibility(View.GONE);////EPG2隐藏
            //showController();
        }

//        ll_epg1.setVisibility(View.VISIBLE);////EPG1显示
//        ll_epg2.setVisibility(View.VISIBLE);////EPG2显示


        if (switchs == 1){
            ll_forfend.setVisibility(View.VISIBLE);////换台遮挡
        }
    }

    /*结束网速*/
    private void endSpeed() {
        load = 0;
        mSpeedHandler.removeCallbacks(speedRunnable);
        if (tv_mv_speed != null) {
            tv_mv_speed.setVisibility(View.GONE);
        }
//        ll_epg1.setVisibility(View.GONE);////EPG1隐藏
//        ll_epg2.setVisibility(View.GONE);////EPG2隐藏
////        ll_forfend.setVisibility(View.GONE);////换台遮挡
//        mediaHandler.sendEmptyMessageDelayed(WindowMessageID.TV_COVER, 1000);////换台遮挡


        mSpeedHandler.removeCallbacks(speedRunnables);////移除实时网速
        lastRxBytes = TrafficStats.getTotalRxBytes();////实时网速
        lastSpeedTimes = System.currentTimeMillis();////实时网速
        mSpeedHandler.postDelayed(speedRunnables, 0);////设置实时网速


        if (caton_check == 1){
            mSpeedHandler.postDelayed(speedRunnabless, check_time * 1000);////设置卡顿校验
        }

    }

    /*转换解码方式*/
    private void switchCode() {
        int Decode = sp.getInt("mIsHwDecode", 1);
        if (Decode == 1) {
            mIsHwDecode = true;
            jmposition = 1;
        } else {
            mIsHwDecode = false;
            jmposition = 0;
        }
        if (Decode == 1) {
            iVV.setDecode(Boolean.valueOf(true));
        } else if (Decode == 0) {
            iVV.setDecode(Boolean.valueOf(false));
        }
        isPause = Boolean.valueOf(true);
        iVV.resume();
        xjposition = playIndex;
        mediaHandler.sendEmptyMessage(WindowMessageID.SHOW_TV);
    }

    /*获取唤醒锁定*/
    private void acquireWakeLock() {
        if (mWakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,this.getClass().getCanonicalName());
            mWakeLock.acquire();

        }
    }

    /*释放唤醒锁定*/
    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    /*播放器状态*/
    private enum PLAYER_STATUS {
        PLAYER_IDLE,//空闲
        PLAYER_PREPARING,//准备
        PLAYER_PREPARED,//准备好
        PLAYER_BACKSTAGE//后台
    }

    /*获取视频跑马公告*/
    private void getVodGongGao() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=live_notice",
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(LivePlayerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*视频logo*/
    private void vodlogoloadImg() {
        tv_logo.setVisibility(View.VISIBLE);
        Glide.with(this).load(logo_url).into(tv_logo);
    }

    /*公告获取成功*/
    public void VodGongGaoResponse(String response) {
        Log.d(TAG, "========== 直播公告调试 ==========");
        Log.d(TAG, "直播公告原始响应: " + response);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            String msg = jSONObject.optString("msg");/*状态信息*/
            Log.d(TAG, "直播公告code: " + code + ", msg: " + msg);
            if (code == 200){
                tv_notice_root.setVisibility(View.VISIBLE);
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                Log.d(TAG, "加密类型miType: " + miType);
                if (miType == 1) {
                    tv_notice.setText(URLDecoder.decode(Rc4.decry_RC4(msg,RC4KEY), "UTF-8"));
                } else if (miType == 2) {
                    tv_notice.setText(URLDecoder.decode(Rsa.decrypt_Rsa(msg,RSAKEY), "UTF-8"));
                } else if (miType == 3) {
                    tv_notice.setText(URLDecoder.decode(AES.decrypt_Aes(AESKEY,msg, AESIV), "UTF-8"));
                }
                mediaHandler.sendEmptyMessage(WindowMessageID.NOTICE);

            }else{
                tv_notice_root.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*公告获取成失败*/
    public void VodGongGaoError(VolleyError volleyError) {
        //Log.i(TAG, "RequestError: " + volleyError);
        if (volleyError instanceof TimeoutError) {
            tv_notice_root.setVisibility(View.GONE);
//            System.out.println("请求超时");
        }
        if (volleyError instanceof AuthFailureError) {
            tv_notice_root.setVisibility(View.GONE);
            //System.out.println("身份验证失败错误");
        }
        if(volleyError instanceof NetworkError) {
            tv_notice_root.setVisibility(View.GONE);
//            System.out.println("请检查网络");
        }
        if(volleyError instanceof ServerError) {
            tv_notice_root.setVisibility(View.GONE);
            //System.out.println("错误404");
        }

    }

    /*检查当前视频播放进度*/
    private void updateprogress(){
        /*导航是否显示*/
        if (Navigation_mode == 1){
            Navigation();
        }
    }

    /*隐藏导航*/
    public void Navigation(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    /*内部消息ID定义类*/
    private class WindowMessageID {

        /**
         * @brief 服务请求成功。
         */
        public static final int SUCCESS = 0x00000001;
        /**
         * @brief 请求出错。
         */
        public final static int ERROR = 0x00000003;
        /**
         * @brief 播放。
         */
        public final static int EVENT_PLAY = 0x00000004;
        /**
         * @brief 刷新时间。
         */
        public static final int UI_EVENT_UPDATE_CURRPOSITION = 0x00000005;
        /**
         * @brief 隐藏进度条。
         */
        public final static int HIDE_CONTROLER = 0x00000007;
        /**
         * @brief 播放数据准备OK。
         */
        public static final int DATA_PREPARE_OK = 0x00000008;

        /**
         * @brief 准备数据
         */
        public static final int PREPARE_VOD_DATA = 0x000000010;

        public static final int SHOW_TV = 0x00000011;

        public static final int COLSE_SHOW_TV = 0x00000012;

        public static final int PROGRESSBAR_PROGRESS_RESET = 0x00000013;
        /**
         * @brief 设置视频显示比例
         */
        public static final int SELECT_SCALES = 0x00000014;
        /**
         * @brief 快进时间显示隐藏
         */
        public static final int HIDE_PROGRESS_TIME = 0x00000015;

        /**
         * @brief 菜单隐藏
         */
        public static final int HIDE_MENU = 0x00000016;

        /**
         * @brief 初始网速
         */
        public static final int START_SPEED = 0x00000017;

        /**
         * @brief 重置时间
         */
        public static final int RESET_MOVIE_TIME = 0x00000018;

        /**
         * @brief 播放异常
         */
        public static final int PLAY_ERROR = 0x00000019;

        /**
         * @brief 切换解码
         */
        public static final int SWITCH_CODE = 0x00000020;

        /**
         * @brief 视频跑马
         */
        public static final int NOTICE = 0x00000024;

        /**
         * @brief 视频跑马隐藏
         */
        public static final int NOTICE_GONE = 0x00000025;

        /**
         * @brief 启动跑马公告
         */
        public static final int START_NOTICE_GONE = 0x00000026;

        /**
         * @brief 停止跑马公告
         */
        public static final int START_LOGO = 0x00000027;

        /**
         * @brief 数字换台键
         */
        public static final int SELECT_CHANNE = 0x00000028;

        /**
         * @brief 实时网速
         */
        public static final int START_SPEEDS = 0x00000029;

        /**
         * @brief 换台遮挡
         */
        public static final int TV_COVER = 0x00000030;

        /**
         * @brief EPG显示框
         */
        public static final int EPG = 0x00000031;

        /**
         * @brief 准备超时
         */
        public static final int PREPARE_TIMEOUT = 0x00000032;

    }

}
