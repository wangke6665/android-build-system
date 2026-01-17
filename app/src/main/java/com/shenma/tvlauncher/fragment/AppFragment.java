package com.shenma.tvlauncher.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ViewSwitcher.ViewFactory;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.AppManageActivity;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.SettingWallpaperActivity;
import com.shenma.tvlauncher.application.MyApplication;
import com.shenma.tvlauncher.application.MyVolley;
import com.shenma.tvlauncher.domain.RecApp;
import com.shenma.tvlauncher.domain.RecAppInfo;
import com.shenma.tvlauncher.domain.Upload;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.ScaleAnimEffect;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.tvlive.network.ThreadPoolManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

/**
 * 应用
 *
 * @author joychang
 */
public class AppFragment extends BaseFragment implements OnFocusChangeListener,
        OnClickListener {

    private final int SHOW_NEXT_IMG = 1001;
    public ImageView[] app_typeLogs;
    ScaleAnimEffect animEffect;
    private ErrorListener mErrListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof TimeoutError) {
                Logger.e("zhouchuan", "请求超时");
            } else if (error instanceof AuthFailureError) {
                Logger.e("zhouchuan",
                        "AuthFailureError=" + error.toString());
            }
        }
    };
    private Listener<Upload> mSucListener = new Listener<Upload>() {
        @Override
        public void onResponse(Upload response) {
            if (null != response && "200".equals(response.getCode())) {
                Logger.e("zhouchuan", "上传成功");
            }
        }
    };
    private ViewFactory mFactory = new ViewFactory() {

        @Override
        public View makeView() {
            ImageView i = new ImageView(getActivity());
            i.setBackgroundColor(0x00000000);
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i.setLayoutParams(new ImageSwitcher.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            return i;
        }
    };
    private FrameLayout[] app_fls;
    private ImageView[] appbgs;
    private View view;
    private RequestQueue mQueue;
    private ImageLoader imageLoader;
    private int bigImageIndexDef = 0;
    private int smallImageIndexDef = 6;
    private ScheduledExecutorService executorService;
    private ImageSwitcher app_iv_0;
    private int currentIndex = 0;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_NEXT_IMG:
                    String drawable = (String) msg.obj;
                    if (drawable != null) {
                        imageLoader.get(drawable, ImageLoader.getImageListener((ImageView) app_iv_0.getNextView(), R.drawable.app_iv_0, R.drawable.app_iv_0));
                        app_iv_0.showNext();
//					app_iv_0.setImageDrawable(drawable);
                        currentIndex++;
                        if (currentIndex > 5) {
                            currentIndex = 0;
                        }
                    }
                    break;
            }
        }

        ;
    };
    private String imgurls[] = new String[6];
    private Runnable mScrollTask = new Runnable() {
        @Override
        public void run() {

//				BitmapDrawable drawable = new BitmapDrawable(getResources(),
//						createBitMapFromNet(imgurls[currentIndex]));
            Message msg = mHandler.obtainMessage(SHOW_NEXT_IMG, imgurls[currentIndex]);
            mHandler.sendMessage(msg);
        }
    };
    private Map<Integer, RecAppInfo> map = new HashMap<Integer, RecAppInfo>();
    private List<RecAppInfo> data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        if (null == view) {
            view = inflater.inflate(R.layout.layout_app, container, false);
            init();
        } else {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null)
                viewGroup.removeView(view);
        }
        if (data == null) {
            initData();
        }
        return view;
    }

    /**
     * 上传下载统计信息
     *
     * @param packName 包名
     */
    private void uploadInfo(String packName) {
        String VERSION_RELEASE = android.os.Build.VERSION.RELEASE;
        String MODEL = android.os.Build.MODEL;
        TelephonyManager telephonyManager = (TelephonyManager) home.getSystemService(Context.TELEPHONY_SERVICE);
        String DeviceId = telephonyManager.getDeviceId();
        String devicecode = VERSION_RELEASE + "-" + MODEL + "-" + DeviceId;
        params = params + "&devicecode=" + devicecode + "&packname=" + packName;
        params = params.replaceAll(" ", "%20");
        GsonRequest<Upload> Upload = new GsonRequest<Upload>(Method.POST, Constant.UPLOAD_URL + params, Upload.class,
                mSucListener, mErrListener) {
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
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(Upload);
    }

    private void initData() {
        mQueue = Volley.newRequestQueue(context, new ExtHttpStack());
        imageLoader = MyVolley.getImageLoader();
        GsonRequest<RecApp> mRecApps = new GsonRequest<RecApp>(Method.POST,
                Constant.RECAPPS + params, RecApp.class,
                createMyReqSuccessListener(), createMyReqErrorListener()) {
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
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(mRecApps);
    }

//	/**
//	 * 从网络图片地址得到一个bitmap
//	 * 
//	 * @author drowtram
//	 * @param url
//	 * @return
//	 */
//	public Bitmap createBitMapFromNet(String url) {
//		Logger.i("joychang", "url="+url);
//		URL myFileUrl = null;
//		Bitmap bitmap = null;
//		if(null!=url&&"null"!=url){
//			try {
//				myFileUrl = new URL(url.replaceAll(" ", "%20"));//替换空格的字符串
//				HttpURLConnection conn = (HttpURLConnection) myFileUrl
//						.openConnection();
//				conn.setDoInput(true);
//				conn.connect();
//				InputStream is = conn.getInputStream();
//				BitmapFactory.Options options = new BitmapFactory.Options();
//				options.inJustDecodeBounds = false;
//				options.inSampleSize = 3; // width，hight设为原来的三分一
//				bitmap = BitmapFactory.decodeStream(is, null, options);
//				is.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		return bitmap;
//	}

    private ErrorListener createMyReqErrorListener() {

        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Logger.e("zhouchuan", "请求超时");
                } else if (error instanceof AuthFailureError) {
                    Logger.e("zhouchuan",
                            "AuthFailureError=" + error.toString());
                }
            }
        };
    }

    private Listener<RecApp> createMyReqSuccessListener() {

        return new Response.Listener<RecApp>() {

            @Override
            public void onResponse(RecApp response) {
                if (null != response && "200".equals(response.getCode())) {
                    data = response.getData();
                    for (RecAppInfo recAppInfo : data) {
                        String picUrl = recAppInfo.getTjpicur();
                        if (picUrl == null || picUrl.trim().isEmpty()) {
                            continue;
                        }
                        
                        // 处理URL：支持绝对地址，兼容空格
                        String fullUrl = buildFullUrl(picUrl);
                        
                        if ("1".equals(recAppInfo.getTjtype())) {// 大图
                            if (bigImageIndexDef > 5)
                                bigImageIndexDef = 0;
                            map.put(bigImageIndexDef, recAppInfo);
                            loadImageWithRetry(app_typeLogs[bigImageIndexDef], fullUrl,
                                    R.drawable.app_iv_1, R.drawable.app_iv_1, 2, 400);
                            imgurls[bigImageIndexDef] = fullUrl;
                            bigImageIndexDef++;
                        } else { // 小图
                            if (smallImageIndexDef > 11) {
                                smallImageIndexDef = 6;
                            }
                            if (smallImageIndexDef < 6 || smallImageIndexDef >= app_typeLogs.length) {
                                continue;
                            }
                            map.put(smallImageIndexDef, recAppInfo);
                            loadImageWithRetry(app_typeLogs[smallImageIndexDef], fullUrl,
                                    R.drawable.app_iv_2, R.drawable.app_iv_2, 2, 400);
                            smallImageIndexDef++;
                        }
                    }
                }
            }
        };
    }

    /**
     * 构造完整图片URL：如果已是 http/https 则直接使用，否则拼接 HEARD_URL，并替换空格
     */
    private String buildFullUrl(String picUrl) {
        String url = picUrl == null ? "" : picUrl.trim();
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url.replace(" ", "%20");
        }
        return (Constant.HEARD_URL + url).replace(" ", "%20");
    }

    /**
     * 图片加载带重试，减少首帧加载失败的空白
     */
    private void loadImageWithRetry(final ImageView target, final String url,
                                    final int defaultRes, final int errorRes,
                                    final int maxRetry, final int delayMs) {
        if (target == null || imageLoader == null || url == null) return;

        imageLoader.get(url, new ImageLoader.ImageListener() {
            int retry = 0;

            @Override
            public void onErrorResponse(VolleyError error) {
                if (retry < maxRetry) {
                    retry++;
                    target.postDelayed(() -> imageLoader.get(url, this), delayMs);
                } else if (errorRes != 0) {
                    target.setImageResource(errorRes);
                }
            }

            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response != null && response.getBitmap() != null) {
                    target.setImageBitmap(response.getBitmap());
                } else if (!isImmediate && retry < maxRetry) {
                    // 图片仍在加载，稍后再试，避免首帧空白
                    retry++;
                    target.postDelayed(() -> imageLoader.get(url, this), delayMs);
                } else if (defaultRes != 0 && target.getDrawable() == null) {
                    target.setImageResource(defaultRes);
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // 初始化
    private void init() {
        loadViewLayout();
        findViewById();
        setListener();
    }

    @Override
    public void onResume() {
        // 查询所有已安装的app
        // home.queryInstalledApp();
        super.onResume();
    }

    @Override
    public void onStart() {
        initscheduledExecutorService();
        super.onStart();
    }

    @Override
    public void onStop() {
        shutdownScheduledExecutorService();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    protected void loadViewLayout() {
        app_fls = new FrameLayout[12];
        app_typeLogs = new ImageView[12];
        appbgs = new ImageView[12];
        animEffect = new ScaleAnimEffect();
    }

    protected void findViewById() {
        // app_fls[0] = (FrameLayout) view.findViewById(R.id.app_fl_0);
        app_fls[0] = (FrameLayout) view.findViewById(R.id.app_fl_1);
        app_fls[1] = (FrameLayout) view.findViewById(R.id.app_fl_2);
        app_fls[2] = (FrameLayout) view.findViewById(R.id.app_fl_3);
        app_fls[3] = (FrameLayout) view.findViewById(R.id.app_fl_4);
        app_fls[4] = (FrameLayout) view.findViewById(R.id.app_fl_5);
        app_fls[5] = (FrameLayout) view.findViewById(R.id.app_fl_6);
        app_fls[6] = (FrameLayout) view.findViewById(R.id.app_fl_7);
        app_fls[7] = (FrameLayout) view.findViewById(R.id.app_fl_8);
        app_fls[8] = (FrameLayout) view.findViewById(R.id.app_fl_9);
        app_fls[9] = (FrameLayout) view.findViewById(R.id.app_fl_10);
        app_fls[10] = (FrameLayout) view.findViewById(R.id.app_fl_11);
        app_fls[11] = (FrameLayout) view.findViewById(R.id.app_fl_12);

        app_iv_0 = (ImageSwitcher) view.findViewById(R.id.app_iv_0);

        app_typeLogs[0] = (ImageView) view.findViewById(R.id.app_iv_1);
        app_typeLogs[1] = (ImageView) view.findViewById(R.id.app_iv_2);
        app_typeLogs[2] = (ImageView) view.findViewById(R.id.app_iv_3);
        app_typeLogs[3] = (ImageView) view.findViewById(R.id.app_iv_4);
        app_typeLogs[4] = (ImageView) view.findViewById(R.id.app_iv_5);
        app_typeLogs[5] = (ImageView) view.findViewById(R.id.app_iv_6);
        app_typeLogs[6] = (ImageView) view.findViewById(R.id.app_iv_7);
        app_typeLogs[7] = (ImageView) view.findViewById(R.id.app_iv_8);
        app_typeLogs[8] = (ImageView) view.findViewById(R.id.app_iv_9);
        app_typeLogs[9] = (ImageView) view.findViewById(R.id.app_iv_10);
        app_typeLogs[10] = (ImageView) view.findViewById(R.id.app_iv_11);
        app_typeLogs[11] = (ImageView) view.findViewById(R.id.app_iv_12);

        // appbgs[0] = (ImageView) view.findViewById(R.id.app_bg_0);
        appbgs[0] = (ImageView) view.findViewById(R.id.app_bg_1);
        appbgs[1] = (ImageView) view.findViewById(R.id.app_bg_2);
        appbgs[2] = (ImageView) view.findViewById(R.id.app_bg_3);
        appbgs[3] = (ImageView) view.findViewById(R.id.app_bg_4);
        appbgs[4] = (ImageView) view.findViewById(R.id.app_bg_5);
        appbgs[5] = (ImageView) view.findViewById(R.id.app_bg_6);
        appbgs[6] = (ImageView) view.findViewById(R.id.app_bg_7);
        appbgs[7] = (ImageView) view.findViewById(R.id.app_bg_8);
        appbgs[8] = (ImageView) view.findViewById(R.id.app_bg_9);
        appbgs[9] = (ImageView) view.findViewById(R.id.app_bg_10);
        appbgs[10] = (ImageView) view.findViewById(R.id.app_bg_11);
        appbgs[11] = (ImageView) view.findViewById(R.id.app_bg_12);
    }

    protected void setListener() {
        app_iv_0.setFactory(mFactory);
        app_iv_0.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_in));
        app_iv_0.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.fade_out));

        for (int i = 0; i < app_typeLogs.length; i++) {
            appbgs[i].setVisibility(View.GONE);
            app_typeLogs[i].setOnClickListener(this);
            //if(ISTV){
            app_typeLogs[i].setOnFocusChangeListener(this);
            //}
        }
    }

    @Override
    public void onClick(View v) {
        if (map.size() == 0 && v.getId() != R.id.app_iv_12)
            return;
        switch (v.getId()) {
            case R.id.app_iv_12:// 点击本地应用
//			boolean flag = false;
//			for (PackageInfo pack : home.packLst) {
//				if ("com.shenma.appmgr".equals(pack.packageName) && pack.versionName.equals(Utils.getApkFileInfoAnother(home))) {
//					// 已安装了apk，则直接打开
//					Intent mIntent = new Intent();
//					mIntent.setAction("com.shenma.appmgr.start");
//					mIntent.addCategory("android.intent.category.DEFAULT");
//					startActivity(mIntent);
//					return;
//				}else if ("com.shenma.appmgr".equals(pack.packageName) && !pack.versionName.equals(Utils.getApkFileInfoAnother(home))) {
//					flag = true;
//					break;
//				}
//			}
//			if(flag){
//				Utils.showToast(home, "正在更新本地应用程序", R.drawable.toast_smile);
//			}else {
//				Utils.showToast(home, "正在安装本地应用程序", R.drawable.toast_smile);
//			}
//			// 从assets目录下拿到对应的apk进行安装，
//			if (Utils.copyApkFromAssets(getActivity(), "AppManager.apk",Constant.PUBLIC_DIR + "AppManager.apk")) {
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.setDataAndType(
//						Uri.parse("file://" + Constant.PUBLIC_DIR
//								+ "AppManager.apk"),
//						"application/vnd.android.package-archive");
//				getActivity().startActivity(intent);
//			}
                startActivity(new Intent(context, AppManageActivity.class));
                break;
            case R.id.app_iv_7:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(6).getTjapk(),
                        map.get(6).getPackname(),
                        map.get(6)
                                .getTjapk()
                                .substring(
                                        map.get(6).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_8:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(7).getTjapk(),
                        map.get(7).getPackname(),
                        map.get(7)
                                .getTjapk()
                                .substring(
                                        map.get(7).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_9:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(8).getTjapk(),
                        map.get(8).getPackname(),
                        map.get(8)
                                .getTjapk()
                                .substring(
                                        map.get(8).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_10:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(9).getTjapk(),
                        map.get(9).getPackname(),
                        map.get(9)
                                .getTjapk()
                                .substring(
                                        map.get(9).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_11:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(10).getTjapk(),
                        map.get(10).getPackname(),
                        map.get(10)
                                .getTjapk()
                                .substring(
                                        map.get(10).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_1:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(0).getTjapk(),
                        map.get(0).getPackname(),
                        map.get(0)
                                .getTjapk()
                                .substring(
                                        map.get(0).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_2:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(1).getTjapk(),
                        map.get(1).getPackname(),
                        map.get(1)
                                .getTjapk()
                                .substring(
                                        map.get(1).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_3:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(2).getTjapk(),
                        map.get(2).getPackname(),
                        map.get(2)
                                .getTjapk()
                                .substring(
                                        map.get(2).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_4:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(3).getTjapk(),
                        map.get(3).getPackname(),
                        map.get(3)
                                .getTjapk()
                                .substring(
                                        map.get(3).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_5:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(4).getTjapk(),
                        map.get(4).getPackname(),
                        map.get(4)
                                .getTjapk()
                                .substring(
                                        map.get(4).getTjapk().lastIndexOf("/") + 1));
                break;
            case R.id.app_iv_6:
                startOpenOrDownload(
                        Constant.HEARD_URL + map.get(5).getTjapk(),
                        map.get(5).getPackname(),
                        map.get(5)
                                .getTjapk()
                                .substring(
                                        map.get(5).getTjapk().lastIndexOf("/") + 1));
                break;
        }
        home.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    /**
     * 根据状态来下载或者打开app
     *
     * @param apkurl
     * @param packName
     * @author drowtram
     */
    private void startOpenOrDownload(String apkurl, String packName,
                                     String fileName) {
        // 判断当前应用是否已经安装
        for (PackageInfo pack : home.packLst) {
            if (pack.packageName.equals(packName)) {
                // 已安装了apk，则直接打开
                Intent intent = getActivity().getPackageManager()
                        .getLaunchIntentForPackage(packName);
                startActivity(intent);
                return;
            }
        }
        // 如果没有安装，则查询本地是否有安装包文件，有则直接安装
        if (!startCheckLoaclApk(fileName)) {
            // 如果没有安装包 则进行下载安装
            startDownloadApk(packName, apkurl);
        }
    }

    /**
     * 下载apk文件进行安装
     *
     * @param context
     * @param url
     * @author drowtram
     */
    private void startDownloadApk(final String packName, final String url) {
        Utils.showToast(context, R.string.app_downlond, R.drawable.toast_smile);
        // 优化：使用线程池替代直接创建Thread
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            @Override
            public void run() {
                File file = new File(MyApplication.PUBLIC_DIR);
                String apkName = url.substring(url.lastIndexOf("/") + 1);
                if (!file.exists()) {
                    file.mkdirs();
                }
                try {
                    HttpGet hGet = new HttpGet(url.replaceAll(" ", "%20"));
                    HttpResponse hResponse = new DefaultHttpClient().execute(hGet);
                    if (hResponse.getStatusLine().getStatusCode() == 200) {
                        InputStream is = hResponse.getEntity().getContent();
                        FileOutputStream fos = new FileOutputStream(MyApplication.PUBLIC_DIR + apkName);
                        byte[] buffer = new byte[8192];
                        int count = 0;
                        while ((count = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, count);
                        }
                        fos.close();
                        is.close();
                        installApk(MyApplication.PUBLIC_DIR + apkName);
                        uploadInfo(packName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 检测本地apk文件
     *
     * @param fileName
     * @author drowtram
     */
    private boolean startCheckLoaclApk(String fileName) {
        File file = new File(MyApplication.PUBLIC_DIR);
        try {
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fName = f.getName();
                    if (fName.equals(fileName)) {
                        installApk(MyApplication.PUBLIC_DIR + fName);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 安装apk文件
     *
     * @param fileName
     */
    private void installApk(String fileName) {
        // 安装前判断apk是否是完整包，即是否可以执行安装
        if (Utils.getUninatllApkInfo(context, fileName)) {
            File file = new File(fileName);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW); // 浏览网页的Action(动作)
            String type = "application/vnd.android.package-archive";
            intent.setDataAndType(Uri.fromFile(file), type); // 设置数据类型
            startActivity(intent);
        } else {
            Utils.showToast(home, R.string.app_downlond_no_ok, R.drawable.toast_smile);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int paramInt = 0;
        switch (v.getId()) {
            case R.id.app_iv_0:
                // paramInt = 0;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.

                break;
            case R.id.app_iv_1:
                paramInt = 0;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.
                if (hasFocus) {
                    currentIndex = 0;
                    // 优化：使用线程池替代直接创建Thread
                    ThreadPoolManager.getInstance().addTask(mScrollTask);
                    shutdownScheduledExecutorService();
                } else {
                    startScheduledExecutorService();
                }
                break;
            case R.id.app_iv_2:
                paramInt = 1;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.
                if (hasFocus) {
                    currentIndex = 1;
                    // 优化：使用线程池替代直接创建Thread
                    ThreadPoolManager.getInstance().addTask(mScrollTask);
                    shutdownScheduledExecutorService();
                } else {
                    startScheduledExecutorService();
                }
                break;
            case R.id.app_iv_3:
                paramInt = 2;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.
                if (hasFocus) {
                    currentIndex = 2;
                    // 优化：使用线程池替代直接创建Thread
                    ThreadPoolManager.getInstance().addTask(mScrollTask);
                    shutdownScheduledExecutorService();
                } else {
                    startScheduledExecutorService();
                }
                break;
            case R.id.app_iv_4:
                paramInt = 3;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.
                if (hasFocus) {
                    currentIndex = 3;
                    // 优化：使用线程池替代直接创建Thread
                    ThreadPoolManager.getInstance().addTask(mScrollTask);
                    shutdownScheduledExecutorService();
                } else {
                    startScheduledExecutorService();
                }
                break;
            case R.id.app_iv_5:
                paramInt = 4;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.
                if (hasFocus) {
                    currentIndex = 4;
                    // 优化：使用线程池替代直接创建Thread
                    ThreadPoolManager.getInstance().addTask(mScrollTask);
                    shutdownScheduledExecutorService();
                } else {
                    startScheduledExecutorService();
                }
                break;
            case R.id.app_iv_6:
                paramInt = 5;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.
                if (hasFocus) {
                    currentIndex = 5;
                    // 优化：使用线程池替代直接创建Thread
                    ThreadPoolManager.getInstance().addTask(mScrollTask);
                    shutdownScheduledExecutorService();
                } else {
                    startScheduledExecutorService();
                }
                break;
            case R.id.app_iv_7:
                paramInt = 6;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.

                break;
            case R.id.app_iv_8:
                paramInt = 7;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.

                break;
            case R.id.app_iv_9:
                paramInt = 8;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.

                break;
            case R.id.app_iv_10:
                paramInt = 9;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.

                break;
            case R.id.app_iv_12:
                paramInt = 11;
                // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.

                break;

        }
        if (hasFocus) {
            showOnFocusAnimation(paramInt);
            if (null != home.whiteBorder) {
                home.whiteBorder.setVisibility(View.VISIBLE);
            }
        } else {
            showLoseFocusAinimation(paramInt);
            // 将白框隐藏
        }
    }

    /**
     * joychang 设置获取焦点时icon放大凸起
     *
     * @param paramInt
     */
    private void showOnFocusAnimation(final int paramInt) {
        app_fls[paramInt].bringToFront();// 将当前FrameLayout置为顶层
        float f1 = 1.0F;
        float f2 = 1.1F;
        this.animEffect.setAttributs(1.0F, 1.1F, f1, f2, 200L);
        Animation mAnimation = this.animEffect.createAnimation();
        mAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // settingbgs[paramInt].startAnimation(animEffect.alphaAnimation(0.0F,
                // 1.0F, 150L, 0L));
                appbgs[paramInt].setVisibility(View.VISIBLE);
                // settingbgs[paramInt].bringToFront();
            }
        });
        app_typeLogs[paramInt].startAnimation(mAnimation);
    }

    /**
     * 失去焦点缩小
     *
     * @param paramInt
     */
    private void showLoseFocusAinimation(final int paramInt) {
        float f1 = 1.1F;
        float f2 = 1.0F;
        animEffect.setAttributs(1.1F, 1.0F, f1, f2, 200L);
        Animation mAnimation = this.animEffect.createAnimation();
        appbgs[paramInt].setVisibility(View.GONE);
        // mAnimation.setAnimationListener(new AnimationListener() {
        // @Override
        // public void onAnimationStart(Animation animation) {}
        // @Override
        // public void onAnimationRepeat(Animation animation) {}
        // @Override
        // public void onAnimationEnd(Animation animation) {
        // settingbgs[paramInt].setVisibility(View.GONE);
        // }
        // });
        app_typeLogs[paramInt].startAnimation(mAnimation);
    }

    private void shutdownScheduledExecutorService() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void startScheduledExecutorService() {
        if (executorService.isShutdown()) {
            initscheduledExecutorService();
        }
    }

    private void initscheduledExecutorService() {
        executorService = Executors.newSingleThreadScheduledExecutor();
        // 当Activity显示出来后，每两秒钟切换一次图片显示
        executorService.scheduleAtFixedRate(mScrollTask, 0, 10,
                TimeUnit.SECONDS);
    }
    // {
}
