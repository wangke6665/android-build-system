package com.shenma.tvlauncher.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.FrameLayout;
import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.widget.Button;
import android.text.TextPaint;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.AboutActivity;
import com.shenma.tvlauncher.vod.db.Album;
import com.shenma.tvlauncher.vod.dao.VodDao;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.ClearActivity;
import com.shenma.tvlauncher.EmpowerActivity;
import com.shenma.tvlauncher.HistoryActivity;
import com.shenma.tvlauncher.HomeActivity;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.SettingPlayActivity;
import com.shenma.tvlauncher.SettingWallpaperActivity;
import com.shenma.tvlauncher.UserActivity;
import com.shenma.tvlauncher.application.MyVolley;
import com.shenma.tvlauncher.domain.Recommend;
import com.shenma.tvlauncher.domain.RecommendInfo;
import com.shenma.tvlauncher.view.cornerlabelview.CornerLabelView;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.ResUtil;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.ScaleAnimEffect;
import com.shenma.tvlauncher.utils.ScreenAdapterUtil;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.view.cornerlabelview.CornerLabelView;
import com.shenma.tvlauncher.vod.LivePlayerActivity;
import com.shenma.tvlauncher.vod.SearchActivity;
import com.shenma.tvlauncher.vod.VideoDetailsActivity;
import android.text.TextUtils;
import com.shenma.tvlauncher.vod.domain.VideoDetailInfo;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

public class RecommendFragment extends BaseFragment implements OnFocusChangeListener, OnClickListener {
    private final String TAG = "RecommendFragment";
    public ImageLoader imageLoader;
    public RequestQueue mQueue;
    public ImageView[] re_typeLogs;
    protected SharedPreferences sp;
    public static SharedPreferences Sp;
    ScaleAnimEffect animEffect;
    private List<RecommendInfo> data = null;
    private Intent i;
    private final Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(context, R.string.request_failure, R.drawable.toast_err);
                    return;
                case 2:
                    Utils.showToast(context,R.string.Account_expiration, R.drawable.toast_err);
                    startActivity(new Intent(context, EmpowerActivity.class));
                    return;
                case 3:
                    Utils.showToast(context,R.string.disconnect, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 4:
                    Utils.showToast(context,R.string.Account_has_been_disabled, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 5:
                    Utils.showToast(context,R.string.request_failures, R.drawable.toast_shut);
                    return;
                case 6:
                    Utils.showToast(context,R.string.Account_information_has_expired, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 7:
                    Utils.showToast(context,R.string.Account_information_error, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 8:
                    Utils.showToast(context,R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                default:
                    return;
            }
        }
    };
    private FrameLayout[] re_fls;
    private int[] re_typebgs;
    private ImageView[] rebgs;
    private TextView tv_intro = null;
    private TextView[] tvs;
    private View view;
    private int vipstate;
    private int trystate;
    private CornerLabelView  jb_1,jb_2,jb_3,jb_4,jb_5,jb_6;
    private boolean isNavigating = false; // 防止重复跳转标志
    
    // 无界UI详情显示控件
    private TextView tv_title_infinity, tv_rating_infinity;
    private TextView tv_year_infinity, tv_type_infinity, tv_desc_infinity;
    private Button btn_play_infinity;
    private VideoDetailInfo currentDetailInfo;
    private int currentSelectedPosition = -1;
    
    // 详情缓存，启动时预加载所有推荐项的详情
    private java.util.HashMap<Integer, VideoDetailInfo> detailCache = new java.util.HashMap<>();
    private boolean isPreloadingDetails = false;
    private RelativeLayout infoContainerInfinity;
    private LinearLayout categoryContainerInfinity;
    private TextView tv_time_infinity;
    private ImageView iv_net_state_infinity;
    private Handler timeHandler;
    // 顶部功能按钮
    private LinearLayout btn_search_infinity, btn_history_infinity, btn_play_setting_infinity, btn_user_infinity;
    private com.shenma.tvlauncher.view.AlwaysMarqueeTextView tv_gonggao_infinity;
    private TextView tv_vip_expire_infinity, tv_account_infinity, tv_app_name_infinity;
    private LinearLayout btn_play_now_infinity;
    private LinearLayout btn_collect_infinity;
    private ImageView iv_collect_icon_infinity;
    private TextView tv_collect_text_infinity;
    private TextView tv_info_infinity;
    
    // 动态推荐卡片
    private LinearLayout llRecommendContainer;
    private HorizontalScrollView hsvRecommendInfinity;
    private java.util.ArrayList<FrameLayout> dynamicCardList = new java.util.ArrayList<>();
    private java.util.ArrayList<ImageView> dynamicImageList = new java.util.ArrayList<>();
    private java.util.ArrayList<TextView> dynamicTextList = new java.util.ArrayList<>();
    private int lastSelectedCardIndex = -1; // 记录上次选中的卡片索引

    /*创建时的回调函数*/
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("RecommendFragment", "onCreate()........");
        sp = getActivity().getSharedPreferences("shenma", 0);
        Sp = getActivity().getSharedPreferences("initData", MODE_PRIVATE);
    }

    /*在创建视图时查看*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "onCreateView()........");
        if (container == null) {
            return null;
        }
        // 初始化trystate，必须在getActivity()可用后
        trystate = SharePreferenceDataUtil.getSharedIntData(getActivity(), Constant.wj, 0);
        if (null == view) {
            int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
            if (Interface_Style == 0){
                /*旧UI*/
                view = inflater.inflate(R.layout.layout_recommend, container,false);
            }else if(Interface_Style == 1){
                /*新UI*/
                view = inflater.inflate(R.layout.layout_recommends, container,false);
            }else if(Interface_Style == 2){
                /*新UI圆角*/
                view = inflater.inflate(R.layout.layout_recommendss, container,false);
            }else if(Interface_Style == 3){
                /*旧UI圆角*/
                view = inflater.inflate(R.layout.layout_recommendsss, container,false);
            }else if(Interface_Style == 4){
                /*无界UI*/
                view = inflater.inflate(R.layout.layout_recommend_infinity, container,false);
                view.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
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

    /*停止时*/
    @Override
    public void onStop() {
        super.onStop();
        Logger.d(TAG, "onStop()........");
        if (null != mQueue) {
            mQueue.stop();
        }
    }

    /*销毁时*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(TAG, "onDestroy()........");
        if (null != mQueue) {
            mQueue.cancelAll(this);
        }
    }

    /*恢复时*/
    @Override
    public void onResume() {
        super.onResume();
        // 重置跳转标志，允许用户再次点击
        isNavigating = false;
        Logger.d(TAG, "onResume()........");
        // 刷新账号和VIP信息（从登录页返回时更新）
        initVipAndAppName();
    }

    /*分离时*/
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

    /*初始化*/
    private void init() {
        loadViewLayout();
        findViewById();
        setListener();
        //re_fls[0].requestFocus();
    }

    /*初始化数据*/
    private void initData() {
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Api_url", ""),Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "BASE_HOST", ""),Constant.d);
        mQueue = Volley.newRequestQueue(context, new ExtHttpStack());
        imageLoader = MyVolley.getImageLoader();
        GsonRequest<Recommend> mRecommend = new GsonRequest<Recommend>(Method.POST, Api_url + "/api.php/" + BASE_HOST +"/top",
                Recommend.class, createMyReqSuccessListener(), createMyReqErrorListener()) {
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
                params.put("os",  Integer.toString(android.os.Build.VERSION.SDK_INT));
                params.put("Style", String.valueOf(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4)));
                return params;
            }



            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };

        mQueue.add(mRecommend);     //     执行
    }

    /*推荐侦听器*/
    private Listener<Recommend> createMyReqSuccessListener() {
        final int Home_text_shadow = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Home_text_shadow", 0);
        return new Listener<Recommend>() {
            public void onResponse(Recommend response) {
                data = response.getData();
                int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
                
                // 无界UI使用动态卡片
                if (Interface_Style == 4) {
                    android.util.Log.d("RecommendFragment", "无界UI: 收到数据 size=" + (data != null ? data.size() : "null"));
                    createDynamicRecommendCards(data);
                    return;
                }
                
                // 其他UI使用固定卡片
                int paramInt = 0;
                String paramUrl;
                for (int i = 0; i < data.size() && i < tvs.length; i++) {
                    if (Interface_Style == 0||Interface_Style == 1||Interface_Style == 3){
                        paramInt = i + 3;
                    }else if(Interface_Style == 2){
                        paramInt = i;
                    }
                    tvs[i].setText(data.get(i).getTjinfo());
                    paramUrl = data.get(i).getTjpicur();
                    /*no0010*/
                    if (Home_text_shadow == 0){
                        tvs[i].setVisibility(View.VISIBLE);
                    }
                    Logger.v("joychang", "paramUrl=" + paramUrl);
                    setTypeImage(paramInt, paramUrl);
                }
            }
        };
    }
    
    /**
     * 动态创建无界UI推荐卡片
     */
    private void createDynamicRecommendCards(final java.util.List<RecommendInfo> dataList) {
        android.util.Log.d("RecommendFragment", "createDynamicRecommendCards: dataList size = " + (dataList != null ? dataList.size() : "null"));
        android.util.Log.d("RecommendFragment", "llRecommendContainer = " + (llRecommendContainer != null ? "OK" : "NULL"));
        if (llRecommendContainer == null || dataList == null || dataList.size() == 0) {
            android.util.Log.e("RecommendFragment", "createDynamicRecommendCards: container or data is null/empty");
            return;
        }
        
        // 清空旧卡片
        llRecommendContainer.removeAllViews();
        dynamicCardList.clear();
        dynamicImageList.clear();
        dynamicTextList.clear();
        
        // 获取屏幕适配参数 - 设计为屏幕可容纳4个卡片
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float density = getResources().getDisplayMetrics().density;
        int containerPadding = (int) (screenWidth * 0.03f); // 容器左右内边距3%
        int availableWidth = screenWidth - containerPadding * 2; // 可用宽度
        int cardMargin = (int) (screenWidth * 0.025f); // 卡片间距2.5%
        // 4个卡片 + 3个间距 = 可用宽度
        int cardWidth = (int) ((availableWidth - cardMargin * 3) / 4f);
        int cardHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        
        // 设置容器内边距，确保第一个和最后一个卡片不被裁剪
        llRecommendContainer.setPadding(containerPadding, 0, containerPadding, 0);
        llRecommendContainer.setClipChildren(false);
        llRecommendContainer.setClipToPadding(false);
        if (hsvRecommendInfinity != null) {
            hsvRecommendInfinity.setClipChildren(false);
            hsvRecommendInfinity.setClipToPadding(false);
        }
        
        for (int i = 0; i < dataList.size(); i++) {
            final int position = i;
            final RecommendInfo item = dataList.get(i);
            
            // 创建卡片容器
            FrameLayout cardFrame = new FrameLayout(getActivity());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(cardWidth, cardHeight);
            cardParams.setMarginEnd(cardMargin);
            cardFrame.setLayoutParams(cardParams);
            cardFrame.setFocusable(true);
            cardFrame.setFocusableInTouchMode(true);
            cardFrame.setClipChildren(false);
            cardFrame.setClipToPadding(false);
            // 禁用默认焦点边框
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                cardFrame.setDefaultFocusHighlightEnabled(false);
            }
            cardFrame.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            
            // 创建图片容器（使用FrameLayout替代CardView以兼容Android 4.x）
            FrameLayout imageContainer = new FrameLayout(getActivity());
            FrameLayout.LayoutParams cvParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            imageContainer.setLayoutParams(cvParams);
            imageContainer.setBackgroundColor(0x33000000);
            
            // 创建图片
            ImageView imageView = new ImageView(getActivity());
            FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(imgParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageContainer.addView(imageView);
            
            // 创建标题文字
            TextView textView = new TextView(getActivity());
            FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            tvParams.gravity = android.view.Gravity.BOTTOM;
            textView.setLayoutParams(tvParams);
            textView.setBackgroundResource(R.drawable.bg_card_text_gradient);
            textView.setTextColor(0xFFFFFFFF);
            textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            textView.setSingleLine(true);
            textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textView.setPadding((int)(6 * getResources().getDisplayMetrics().density), 
                               (int)(6 * getResources().getDisplayMetrics().density),
                               (int)(6 * getResources().getDisplayMetrics().density),
                               (int)(6 * getResources().getDisplayMetrics().density));
            textView.setText(item.getTjinfo());
            
            // 优化：添加热播标签（前6个推荐项显示），样式与其他UI一致（CornerLabelView 左上角三角）
            // 每个推荐项使用不同的颜色，跟其他UI保持一致
            CornerLabelView hotBadge = null;
            if (i < 6) { // 前6个推荐项显示热播标签
                hotBadge = new CornerLabelView(getActivity());
                // 尺寸对齐旧UI（sm_100）
                int badgeSize = (int) getResources().getDimension(R.dimen.sm_100);
                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(badgeSize, badgeSize);
                badgeParams.gravity = android.view.Gravity.TOP | android.view.Gravity.START;
                // 贴边展示：最小化内边距，仅保留1dp防止裁剪
                int margin = (int) (getResources().getDisplayMetrics().density); // 1dp
                badgeParams.setMargins(margin, margin, 0, 0);
                hotBadge.setLayoutParams(badgeParams);
                
                // 根据位置设置不同的颜色（跟其他UI保持一致）
                int[] badgeColors = {
                    R.color.red,        // 第1个：红色
                    R.color.blue_user,  // 第2个：蓝色
                    R.color.darkgreen,   // 第3个：深绿色
                    R.color.detail_point, // 第4个：detail_point颜色
                    R.color.brown,      // 第5个：棕色
                    R.color.purple      // 第6个：紫色
                };
                
                // 样式与旧UI保持一致
                hotBadge.setBgColorId(badgeColors[i])
                        .setTextColorId(R.color.white)
                        .setText(R.string.Hot_broadcast);
                // 通过反射调整位置与尺寸，贴近旧UI（左上角、边长sm_50、文字sm_25）
                try {
                    java.lang.reflect.Field positionField = CornerLabelView.class.getDeclaredField("position");
                    positionField.setAccessible(true);
                    positionField.setInt(hotBadge, 3); // 左上角
                    java.lang.reflect.Field sideLengthField = CornerLabelView.class.getDeclaredField("sideLength");
                    sideLengthField.setAccessible(true);
                    int sideLength = (int) getResources().getDimension(R.dimen.sm_50);
                    sideLengthField.setInt(hotBadge, sideLength);
                    java.lang.reflect.Field textSizeField = CornerLabelView.class.getDeclaredField("textSize");
                    textSizeField.setAccessible(true);
                    int textSize = (int) getResources().getDimension(R.dimen.sm_25);
                    textSizeField.setInt(hotBadge, textSize);
                    java.lang.reflect.Field textPaintField = CornerLabelView.class.getDeclaredField("mTextPaint");
                    textPaintField.setAccessible(true);
                    TextPaint tp = (TextPaint) textPaintField.get(hotBadge);
                    if (tp != null) {
                        tp.setTextSize(textSize);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // 添加到卡片
            cardFrame.addView(imageContainer);
            cardFrame.addView(textView);
            if (hotBadge != null) {
                cardFrame.addView(hotBadge);
            }
            
            // 设置焦点监听
            cardFrame.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    android.util.Log.d("RecommendFragment", "onFocusChange: position=" + position + ", hasFocus=" + hasFocus);
                    if (hasFocus) {
                        // 恢复上一个选中卡片的状态
                        if (lastSelectedCardIndex >= 0 && lastSelectedCardIndex < dynamicCardList.size() && lastSelectedCardIndex != position) {
                            View lastCard = dynamicCardList.get(lastSelectedCardIndex);
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                lastCard.setElevation(0f);
                            }
                            lastCard.animate().scaleX(1.0f).scaleY(1.0f).setDuration(150).start();
                        }
                        // 放大当前卡片
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            v.setElevation(10f);
                        }
                        v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).start();
                        lastSelectedCardIndex = position;
                        // 滚动到可见
                        if (hsvRecommendInfinity != null) {
                            hsvRecommendInfinity.smoothScrollTo((int) v.getX() - 100, 0);
                        }
                        // 更新背景和详情
                        android.util.Log.d("RecommendFragment", "onFocusChange: calling getMovieDetailsAndShow(" + position + ")");
                        try {
                            getMovieDetailsAndShow(position);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    // 失去焦点时不恢复，保持放大状态，直到另一个卡片获得焦点
                }
            });
            
            // 设置点击事件
            cardFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = sp.getString("userName", null);
                    if (username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        return;
                    }
                    GetMotion(position);
                }
            });
            
            // 加载图片
            String imgUrl = item.getTjpicur();
            if (imgUrl != null && !imgUrl.isEmpty() && imageLoader != null) {
                imageLoader.get(imgUrl, ImageLoader.getImageListener(imageView, 
                    R.drawable.fl_re_1, R.drawable.fl_re_1));
            }
            
            // 添加到容器
            llRecommendContainer.addView(cardFrame);
            dynamicCardList.add(cardFrame);
            dynamicImageList.add(imageView);
            dynamicTextList.add(textView);
        }
        
        android.util.Log.d("RecommendFragment", "createDynamicRecommendCards: created " + dynamicCardList.size() + " cards");
        
        // 预加载所有推荐项的详情
        preloadAllDetails(dataList);
        
        // 延迟让第一个卡片获取焦点
        if (dynamicCardList.size() > 0) {
            dynamicCardList.get(0).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dynamicCardList.size() > 0) {
                        dynamicCardList.get(0).requestFocus();
                    }
                }
            }, 500);
            // 加载第一个详情（优先显示）
            getMovieDetailsAndShow(0);
        }
    }
    
    /*预加载所有推荐项的详情*/
    private void preloadAllDetails(final java.util.List<RecommendInfo> dataList) {
        if (dataList == null || dataList.isEmpty() || getActivity() == null) return;
        
        isPreloadingDetails = true;
        detailCache.clear();
        
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Api_url", ""),Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "BASE_HOST", ""),Constant.d);
        
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getActivity(), new ExtHttpStack());
        }
        
        for (int i = 0; i < dataList.size(); i++) {
            final int position = i;
            final RecommendInfo item = dataList.get(i);
            
            GsonRequest<VideoDetailInfo> request = new GsonRequest<VideoDetailInfo>(Method.POST, 
                    Api_url + "/api.php/" + BASE_HOST + "/vod/" + item.getTjurl(),
                    VideoDetailInfo.class, new Response.Listener<VideoDetailInfo>() {
                        @Override
                        public void onResponse(VideoDetailInfo response) {
                            if (response != null) {
                                // 存入缓存
                                detailCache.put(position, response);
                                android.util.Log.d("RecommendFragment", "preload: cached position=" + position + ", title=" + response.getTitle());
                                
                                // 如果是当前选中的位置，立即更新UI
                                if (currentSelectedPosition == position) {
                                    currentDetailInfo = response;
                                    displayMovieDetails(response);
                                    // 更新背景图（使用推荐列表中的横版图）
                                    String bgUrl = item.getTjpicur();
                                    updateBackgroundImage(bgUrl);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            android.util.Log.e("RecommendFragment", "preload error: position=" + position);
                        }
                    }) {
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
                    params.put("os", Integer.toString(android.os.Build.VERSION.SDK_INT));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Authorization", ""),Constant.d));
                    return headers;
                }
            };
            
            mQueue.add(request);
        }
        
        android.util.Log.d("RecommendFragment", "preloadAllDetails: started preloading " + dataList.size() + " items");
    }

    /*设置类型图像*/
    private void setTypeImage(int paramInt, String paramUrl) {
        imageLoader.get(paramUrl,
                ImageLoader.getImageListener(re_typeLogs[paramInt],
                        re_typebgs[paramInt],
                        re_typebgs[paramInt]));
    }

    /*请求失败*/
    private ErrorListener createMyReqErrorListener() {
        return new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Logger.e("joychang", "请求超时");
                } else if (error instanceof AuthFailureError) {
                    Logger.e("joychang", "AuthFailureError=" + error.toString());
                }
            }
        };
    }

    /*加载视图布局*/
    protected void loadViewLayout() {
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
        if (Interface_Style == 4){
            /*无界UI - 使用动态卡片，不需要固定数组*/
            re_fls = new FrameLayout[3]; // 只保留隐藏的功能按钮位置
            re_typeLogs = new ImageView[3];
            re_typebgs = new int[3];
            rebgs = new ImageView[3];
        }else if (Interface_Style == 0 || Interface_Style == 3){
            /*旧UI*/
            re_fls = new FrameLayout[9];
            re_typeLogs = new ImageView[9];
            re_typebgs = new int[9];
            rebgs = new ImageView[9];
        }else if(Interface_Style == 1){
            /*新UI*/
            re_fls = new FrameLayout[11];
            re_typeLogs = new ImageView[11];
            re_typebgs = new int[11];
            rebgs = new ImageView[11];
        }else if(Interface_Style == 2){
            /*新UI圆角*/
            re_fls = new FrameLayout[6];
            re_typeLogs = new ImageView[6];
            re_typebgs = new int[6];
            rebgs = new ImageView[6];
        }
        tvs = new TextView[6];
        animEffect = new ScaleAnimEffect();
    }

    /*按ID查找视图*/
    protected void findViewById() {
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
        
        // 无界UI使用动态卡片
        if (Interface_Style == 4) {
            llRecommendContainer = (LinearLayout) view.findViewById(R.id.ll_recommend_container);
            hsvRecommendInfinity = (HorizontalScrollView) view.findViewById(R.id.hsv_recommend_infinity);
        } else {
            // 其他UI初始化固定卡片
            re_fls[0] = (FrameLayout) view.findViewById(R.id.fl_re_0);
            re_fls[1] = (FrameLayout) view.findViewById(R.id.fl_re_1);
            re_fls[2] = (FrameLayout) view.findViewById(R.id.fl_re_2);
            re_fls[3] = (FrameLayout) view.findViewById(R.id.fl_re_3);
            re_fls[4] = (FrameLayout) view.findViewById(R.id.fl_re_4);
            re_fls[5] = (FrameLayout) view.findViewById(R.id.fl_re_5);

            if(Interface_Style == 0 || Interface_Style == 1 ||Interface_Style == 3){
                re_fls[6] = (FrameLayout) view.findViewById(R.id.fl_re_6);
                re_fls[7] = (FrameLayout) view.findViewById(R.id.fl_re_7);
                re_fls[8] = (FrameLayout) view.findViewById(R.id.fl_re_8);
            }
            
            re_typeLogs[0] = (ImageView) view.findViewById(R.id.iv_re_0);
            re_typeLogs[1] = (ImageView) view.findViewById(R.id.iv_re_1);
            re_typeLogs[2] = (ImageView) view.findViewById(R.id.iv_re_2);
            re_typeLogs[3] = (ImageView) view.findViewById(R.id.iv_re_3);
            re_typeLogs[4] = (ImageView) view.findViewById(R.id.iv_re_4);
            re_typeLogs[5] = (ImageView) view.findViewById(R.id.iv_re_5);

            if(Interface_Style == 0 || Interface_Style == 1 ||Interface_Style == 3){
                re_typeLogs[6] = (ImageView) view.findViewById(R.id.iv_re_6);
                re_typeLogs[7] = (ImageView) view.findViewById(R.id.iv_re_7);
                re_typeLogs[8] = (ImageView) view.findViewById(R.id.iv_re_8);
            }

            jb_1 = (CornerLabelView) view.findViewById(R.id.jb_1);
            jb_2 = (CornerLabelView ) view.findViewById(R.id.jb_2);
            jb_3 = (CornerLabelView ) view.findViewById(R.id.jb_3);
            jb_4 = (CornerLabelView ) view.findViewById(R.id.jb_4);
            jb_5 = (CornerLabelView ) view.findViewById(R.id.jb_5);
            jb_6 = (CornerLabelView ) view.findViewById(R.id.jb_6);
            int CornerLabelView = SharePreferenceDataUtil.getSharedIntData(getActivity(), "CornerLabelView", 0);
            if (CornerLabelView == 1){
                jb_1.setVisibility(View.GONE);
                jb_2.setVisibility(View.GONE);
                jb_3.setVisibility(View.GONE);
                jb_4.setVisibility(View.GONE);
                jb_5.setVisibility(View.GONE);
                jb_6.setVisibility(View.GONE);
            }
            if(Interface_Style == 1){
                /*新UI*/
                re_fls[9] = (FrameLayout) view.findViewById(R.id.fl_re_9);
                re_fls[10] = (FrameLayout) view.findViewById(R.id.fl_re_10);
                re_typeLogs[9] = (ImageView) view.findViewById(R.id.iv_re_9);
                re_typeLogs[10] = (ImageView) view.findViewById(R.id.iv_re_10);
                rebgs[9] = (ImageView) view.findViewById(R.id.re_bg_9);
                rebgs[10] = (ImageView) view.findViewById(R.id.re_bg_9);
            }
            re_typebgs[0] = R.drawable.fl_re_1;
            re_typebgs[1] = R.drawable.fl_re_1;
            re_typebgs[2] = R.drawable.fl_re_1;
            re_typebgs[3] = R.drawable.fl_re_1;
            re_typebgs[4] = R.drawable.fl_re_1;
            re_typebgs[5] = R.drawable.fl_re_1;
            rebgs[0] = (ImageView) view.findViewById(R.id.re_bg_0);
            rebgs[1] = (ImageView) view.findViewById(R.id.re_bg_1);
            rebgs[2] = (ImageView) view.findViewById(R.id.re_bg_2);
            rebgs[3] = (ImageView) view.findViewById(R.id.re_bg_3);
            rebgs[4] = (ImageView) view.findViewById(R.id.re_bg_4);
            rebgs[5] = (ImageView) view.findViewById(R.id.re_bg_5);

            if(Interface_Style == 0 || Interface_Style == 1 ||Interface_Style == 3){
                rebgs[6] = (ImageView) view.findViewById(R.id.re_bg_6);
                rebgs[7] = (ImageView) view.findViewById(R.id.re_bg_7);
                rebgs[8] = (ImageView) view.findViewById(R.id.re_bg_8);
            }

            if(Interface_Style == 0 || Interface_Style == 1 ||Interface_Style == 3){
                tvs[0] = (TextView) view.findViewById(R.id.tv_re_3);
                tvs[1] = (TextView) view.findViewById(R.id.tv_re_4);
                tvs[2] = (TextView) view.findViewById(R.id.tv_re_5);
                tvs[3] = (TextView) view.findViewById(R.id.tv_re_6);
                tvs[4] = (TextView) view.findViewById(R.id.tv_re_7);
                tvs[5] = (TextView) view.findViewById(R.id.tv_re_8);
            }else if (Interface_Style == 2){
                tvs[0] = (TextView) view.findViewById(R.id.tv_re_0);
                tvs[1] = (TextView) view.findViewById(R.id.tv_re_1);
                tvs[2] = (TextView) view.findViewById(R.id.tv_re_2);
                tvs[3] = (TextView) view.findViewById(R.id.tv_re_3);
                tvs[4] = (TextView) view.findViewById(R.id.tv_re_4);
                tvs[5] = (TextView) view.findViewById(R.id.tv_re_5);
            }
        } // 结束其他UI的else块
        
        // 初始化无界UI详情显示控件
        if (Interface_Style == 4) {
            infoContainerInfinity = (RelativeLayout) view.findViewById(R.id.info_container_infinity);
            tv_title_infinity = (TextView) view.findViewById(R.id.tv_title_infinity);
            tv_rating_infinity = (TextView) view.findViewById(R.id.tv_rating_infinity);
            tv_year_infinity = (TextView) view.findViewById(R.id.tv_year_infinity);
            tv_type_infinity = (TextView) view.findViewById(R.id.tv_type_infinity);
            tv_desc_infinity = (TextView) view.findViewById(R.id.tv_desc_infinity);
            btn_play_infinity = (Button) view.findViewById(R.id.btn_play_infinity);
            categoryContainerInfinity = (LinearLayout) view.findViewById(R.id.category_container_infinity);
            tv_time_infinity = (TextView) view.findViewById(R.id.tv_time_infinity);
            iv_net_state_infinity = (ImageView) view.findViewById(R.id.iv_net_state_infinity);
            
            // 初始化顶部功能按钮
            btn_search_infinity = (LinearLayout) view.findViewById(R.id.btn_search_infinity);
            btn_history_infinity = (LinearLayout) view.findViewById(R.id.btn_history_infinity);
            btn_play_setting_infinity = (LinearLayout) view.findViewById(R.id.btn_play_setting_infinity);
            btn_user_infinity = (LinearLayout) view.findViewById(R.id.btn_user_infinity);
            tv_gonggao_infinity = (com.shenma.tvlauncher.view.AlwaysMarqueeTextView) view.findViewById(R.id.tv_gonggao_infinity);
            tv_vip_expire_infinity = (TextView) view.findViewById(R.id.tv_vip_expire_infinity);
            tv_account_infinity = (TextView) view.findViewById(R.id.tv_account_infinity);
            // tv_app_name_infinity已删除
            btn_play_now_infinity = (LinearLayout) view.findViewById(R.id.btn_play_now_infinity);
            btn_collect_infinity = (LinearLayout) view.findViewById(R.id.btn_collect_infinity);
            iv_collect_icon_infinity = (ImageView) view.findViewById(R.id.iv_collect_icon_infinity);
            tv_collect_text_infinity = (TextView) view.findViewById(R.id.tv_collect_text_infinity);
            tv_info_infinity = (TextView) view.findViewById(R.id.tv_info_infinity);
            
            // 初始化分类标签
            initCategoryButtons();
            
            // 初始化时间更新
            initTimeUpdate();
            
            // 初始化顶部功能按钮点击事件
            initTopButtons();
            
            // 初始化公告
            initGonggao();
            
            // 初始化会员信息和APP名字
            initVipAndAppName();
            
            // 加载缓存的背景图和详情，避免黑屏
            loadCachedData();
            
            // 无界UI：根据屏幕尺寸动态设置所有UI元素（必须在控件初始化之后调用）
            applyInfinityUIAdapter();
        }


    }
    
    
    /*初始化无界UI分类标签*/
    private void initCategoryButtons() {
        if (categoryContainerInfinity == null || getActivity() == null) return;
        
        // 从HomeActivity获取分类数据并添加按钮（不添加首页按钮）
        try {
            String category = ((HomeActivity) getActivity()).getCategory();
            if (category != null && !category.isEmpty()) {
                List<Map<String, Object>> dataList = new com.google.gson.Gson().fromJson(category, 
                    new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>(){}.getType());
                
                for (int i = 0; i < dataList.size(); i++) {
                    Map<String, Object> item = dataList.get(i);
                    final String typeEn = (String) item.get("type_en");
                    final String typeName = (String) item.get("type_name");
                    
                    // 使用屏幕适配计算分类按钮尺寸
                    ScreenAdapterUtil catAdapter = ScreenAdapterUtil.getInstance();
                    int catTextSize = catAdapter.getHeightPercent(5); // 字体大小5%
                    int catPaddingH = catAdapter.getWidthPercent(1);  // 水平padding
                    int catPaddingV = catAdapter.getHeightPercent(1); // 垂直padding
                    int catMargin = catAdapter.getWidthPercent(3);    // 间距增大
                    
                    TextView categoryButton = new TextView(getActivity());
                    categoryButton.setText(typeName);
                    categoryButton.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, catTextSize);
                    categoryButton.setTextColor(getResources().getColor(R.color.white));
                    categoryButton.setGravity(android.view.Gravity.CENTER);
                    categoryButton.setPadding(catPaddingH, catPaddingV, catPaddingH, catPaddingV);
                    categoryButton.setFocusable(true);
                    categoryButton.setFocusableInTouchMode(true);
                    categoryButton.setBackground(getResources().getDrawable(R.drawable.selector_category_focus));
                    
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, catMargin, 0);
                    
                    categoryButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 处理直播类型
                            if ("LIVE".equals(typeEn)) {
                                Log.d(TAG, "========== 无界UI点击直播标签 ==========");
                                String username = sp.getString("userName", null);
                                Log.d(TAG, "username = " + username);
                                if (TextUtils.isEmpty(username)) {
                                    Log.d(TAG, "用户未登录，跳转登录页");
                                    Intent mIntent = new Intent(getActivity(), UserActivity.class);
                                    startActivity(mIntent);
                                } else {
                                    long time = System.currentTimeMillis() / 1000;
                                    long vip = Long.parseLong(sp.getString("vip", "0"));
                                    Log.d(TAG, "当前时间戳 = " + time + ", VIP到期时间 = " + vip);
                                    if (time > vip && !"999999999".equals(sp.getString("vip", ""))) {
                                        Log.d(TAG, "VIP已过期，跳转续费页");
                                        Intent mIntent = new Intent(getActivity(), EmpowerActivity.class);
                                        startActivity(mIntent);
                                    } else {
                                        int live = SharePreferenceDataUtil.getSharedIntData(getActivity(), Constant.ogy, 0);
                                        Log.d(TAG, "VIP有效，检查live值: " + live);
                                        if (live == 1) {
                                            Log.d(TAG, "live=1，跳转直播页面");
                                            Intent mIntent = new Intent(getActivity(), LivePlayerActivity.class);
                                            startActivity(mIntent);
                                        } else {
                                            Log.d(TAG, "live!=1，显示未激活提示");
                                            Utils.showToast(context, R.string.Not_yet_activated, R.drawable.toast_shut);
                                        }
                                    }
                                }
                                return;
                            }
                            // 处理用户中心
                            if ("USER".equals(typeEn)) {
                                Intent mIntent = new Intent(getActivity(), UserActivity.class);
                                startActivity(mIntent);
                                return;
                            }
                            // 处理授权/续费
                            if ("EMPOWER".equals(typeEn)) {
                                String username = sp.getString("userName", null);
                                if (TextUtils.isEmpty(username)) {
                                    Intent mIntent = new Intent(getActivity(), UserActivity.class);
                                    startActivity(mIntent);
                                } else {
                                    Intent mIntent = new Intent(getActivity(), EmpowerActivity.class);
                                    startActivity(mIntent);
                                }
                                return;
                            }
                            // 其他分类跳转到分类页面
                            Bundle pBundle = new Bundle();
                            pBundle.putString("TYPE", typeEn);
                            pBundle.putString("TYPENAME", typeName);
                            Intent intent = new Intent(getActivity(), com.shenma.tvlauncher.vod.VodTypeActivity.class);
                            intent.putExtras(pBundle);
                            startActivity(intent);
                        }
                    });
                    
                    // 焦点时文字保持白色（因为背景已经是渐变色了）
                    
                    categoryContainerInfinity.addView(categoryButton, params);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /*初始化时间更新*/
    private void initTimeUpdate() {
        if (tv_time_infinity == null) return;
        
        // 立即更新一次时间
        updateTime();
        
        // 每分钟更新一次时间
        timeHandler = new Handler();
        final Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                if (timeHandler != null) {
                    timeHandler.postDelayed(this, 60000);
                }
            }
        };
        timeHandler.postDelayed(timeRunnable, 60000);
    }
    
    /*更新时间显示*/
    private void updateTime() {
        if (tv_time_infinity != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
            String time = sdf.format(new java.util.Date());
            tv_time_infinity.setText(time);
        }
    }
    
    /*初始化顶部功能按钮点击事件*/
    private void initTopButtons() {
        // 搜索按钮
        if (btn_search_infinity != null) {
            btn_search_infinity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), com.shenma.tvlauncher.vod.SearchActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        // 历史记录按钮
        if (btn_history_infinity != null) {
            btn_history_infinity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), HistoryActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        // 播放设置按钮
        if (btn_play_setting_infinity != null) {
            btn_play_setting_infinity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SettingPlayActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        // 个人中心按钮
        if (btn_user_infinity != null) {
            btn_user_infinity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), UserActivity.class);
                    startActivity(intent);
                }
            });
        }
        
        // 立即播放按钮
        if (btn_play_now_infinity != null) {
            btn_play_now_infinity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 防止重复跳转
                    if (isNavigating) {
                        return;
                    }
                    // 检查登录状态
                    if (!checkLoginStatus()) {
                        return;
                    }
                    // 播放当前选中的影片
                    if (currentDetailInfo != null && currentSelectedPosition >= 0 && data != null && data.size() > currentSelectedPosition) {
                        isNavigating = true;
                        Intent intent = new Intent(getActivity(), VideoDetailsActivity.class);
                        intent.putExtra("nextlink", data.get(currentSelectedPosition).getTjurl());
                        intent.putExtra("vodstate", data.get(currentSelectedPosition).getState());
                        intent.putExtra("vodtype", data.get(currentSelectedPosition).getTjtype().toUpperCase());
                        startActivity(intent);
                    }
                }
            });
        }
        
        // 收藏按钮
        if (btn_collect_infinity != null) {
            btn_collect_infinity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 检查登录状态
                    if (!checkLoginStatus()) {
                        return;
                    }
                    if (currentDetailInfo != null && currentSelectedPosition >= 0 && data != null && data.size() > currentSelectedPosition) {
                        VodDao dao = new VodDao(getActivity());
                        String videoId = currentDetailInfo.getId();
                        String vodtype = data.get(currentSelectedPosition).getTjtype().toUpperCase();
                        String vodstate = data.get(currentSelectedPosition).getState();
                        String nextlink = data.get(currentSelectedPosition).getTjurl();
                        
                        Boolean isCollected = dao.queryZJById(videoId, 1);
                        if (isCollected) {
                            // 已收藏，取消收藏
                            dao.deleteByWhere(videoId, vodtype, 1);
                            iv_collect_icon_infinity.setImageResource(R.drawable.ic_heart_white);
                            tv_collect_text_infinity.setText("收藏");
                            Utils.showToast(getActivity(), R.string.Cancel_collection_successful, R.drawable.toast_smile);
                        } else {
                            // 未收藏，添加收藏
                            Album al = new Album();
                            al.setAlbumId(videoId);
                            al.setAlbumType(vodtype);
                            al.setTypeId(1);
                            al.setAlbumState(vodstate);
                            al.setNextLink(nextlink);
                            al.setAlbumPic(currentDetailInfo.getImg_url());
                            al.setAlbumTitle(currentDetailInfo.getTitle());
                            dao.addAlbums(al);
                            iv_collect_icon_infinity.setImageResource(R.drawable.ic_heart_filled);
                            tv_collect_text_infinity.setText("已收藏");
                            Utils.showToast(getActivity(), R.string.Collection_successful, R.drawable.toast_smile);
                        }
                    }
                }
            });
        }
    }
    
    /**
     * 检查登录状态
     * @return true-已登录且有效，false-未登录或已过期
     */
    private boolean checkLoginStatus() {
        String userName = sp.getString("userName", null);
        String vipTime = sp.getString("vip", null);
        
        // 检查是否登录
        if (userName == null || userName.isEmpty() || vipTime == null || vipTime.isEmpty()) {
            Utils.showToast(getActivity(), R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
            startActivity(new Intent(getActivity(), UserActivity.class));
            return false;
        }
        
        // 检查VIP是否过期
        try {
            // 永久VIP
            if (vipTime.equals("999999999")) {
                return true;
            }
            
            long currentTime = System.currentTimeMillis() / 1000;
            long vipExpireTime = Long.parseLong(vipTime);
            
            if (currentTime >= vipExpireTime) {
                // VIP已过期，检查是否有试用权限
                int trystate = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Trystate", 0);
                if (trystate == 1) {
                    return true;
                }
                Utils.showToast(getActivity(), R.string.Account_expiration, R.drawable.toast_err);
                startActivity(new Intent(getActivity(), EmpowerActivity.class));
                return false;
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Utils.showToast(getActivity(), R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
            startActivity(new Intent(getActivity(), UserActivity.class));
            return false;
        }
    }
    
    /*初始化公告*/
    private void initGonggao() {
        // 从HomeActivity获取公告内容
        if (getActivity() instanceof HomeActivity) {
            String gonggao = ((HomeActivity) getActivity()).getGonggaoText();
            if (gonggao != null && !gonggao.isEmpty()) {
                if (tv_gonggao_infinity != null) {
                    tv_gonggao_infinity.setText(gonggao);
                    tv_gonggao_infinity.setSelected(true);
                }
            } else {
                // 无公告内容，显示空
                if (tv_gonggao_infinity != null) {
                    tv_gonggao_infinity.setText("");
                }
            }
        } else {
            if (tv_gonggao_infinity != null) {
                tv_gonggao_infinity.setText("");
            }
        }
    }
    
    /*更新公告内容（由HomeActivity调用）*/
    public void updateGonggao(String text) {
        if (tv_gonggao_infinity != null) {
            if (text != null && !text.isEmpty()) {
                tv_gonggao_infinity.setText(text);
                tv_gonggao_infinity.setSelected(true);
            } else {
                tv_gonggao_infinity.setText("");
            }
        }
    }
    
    /*初始化账号和到期时间*/
    private void initVipAndAppName() {
        // 设置APP名字（从AndroidManifest.xml获取）
        if (tv_app_name_infinity != null) {
            try {
                android.content.pm.ApplicationInfo appInfo = getActivity().getPackageManager()
                        .getApplicationInfo(getActivity().getPackageName(), 0);
                String appName = getActivity().getPackageManager().getApplicationLabel(appInfo).toString();
                tv_app_name_infinity.setText(appName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // 设置账号
        if (tv_account_infinity != null && sp != null) {
            String userName = sp.getString("userName", null);
            Log.d(TAG, "initVipAndAppName - userName: [" + userName + "]");
            if (!TextUtils.isEmpty(userName) && !"null".equals(userName)) {
                // 只显示前5位
                String displayName = userName.length() > 5 ? userName.substring(0, 5) + "..." : userName;
                tv_account_infinity.setText("账号：" + displayName);
            } else {
                tv_account_infinity.setText("账号：未登录");
            }
        }
        
        // 设置到期时间
        if (tv_vip_expire_infinity != null) {
            String time = sp.getString("vip", "");
            int vipstate = 0;
            try {
                long currentTime = System.currentTimeMillis() / 1000;
                long vipTime = Long.parseLong(time);
                if (currentTime < vipTime) {
                    vipstate = 1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if (time.equals("999999999")) {
                tv_vip_expire_infinity.setText("到期时间：永久");
            } else if (vipstate == 0) {
                tv_vip_expire_infinity.setText("到期时间：已过期");
                tv_vip_expire_infinity.setTextColor(getResources().getColor(R.color.white));
            } else if (vipstate == 1) {
                tv_vip_expire_infinity.setText("到期时间：" + GetTimeStamp.timeStamp2Date(time, ""));
            }
        }
    }
    
    /*加载缓存的背景图和详情*/
    private void loadCachedData() {
        if (getActivity() == null || sp == null) return;
        
        // 确保imageLoader已初始化
        if (imageLoader == null) {
            imageLoader = com.shenma.tvlauncher.application.MyVolley.getImageLoader();
        }
        
        // 加载缓存的背景图
        String cachedBgUrl = sp.getString("infinity_bg_cache", "");
        ImageView bg = (ImageView) getActivity().findViewById(R.id.iv_bg_infinity);
        if (bg != null) {
            if (!cachedBgUrl.isEmpty() && imageLoader != null) {
                imageLoader.get(cachedBgUrl, ImageLoader.getImageListener(bg, R.drawable.main_bg, R.drawable.main_bg));
            } else {
                // 没有缓存时显示默认背景
                bg.setImageResource(R.drawable.main_bg);
            }
        }
        
        // 加载缓存的标题
        String cachedTitle = sp.getString("infinity_title_cache", "");
        if (tv_title_infinity != null) {
            tv_title_infinity.setText(!cachedTitle.isEmpty() ? cachedTitle : "精彩推荐");
        }
        
        // 加载缓存的评分
        String cachedRating = sp.getString("infinity_rating_cache", "");
        if (tv_rating_infinity != null && !cachedRating.isEmpty()) {
            tv_rating_infinity.setText("豆瓣 " + cachedRating);
        }
        
        // 加载缓存的简介
        String cachedIntro = sp.getString("infinity_intro_cache", "");
        if (tv_desc_infinity != null) {
            tv_desc_infinity.setText(!cachedIntro.isEmpty() ? "简介：" + cachedIntro : "简介：精彩内容即将呈现...");
        }
        
        // 加载缓存的类型|年代|地区
        String cachedType = sp.getString("infinity_type_cache", "");
        String cachedYear = sp.getString("infinity_year_cache", "");
        String cachedArea = sp.getString("infinity_area_cache", "");
        if (tv_info_infinity != null) {
            StringBuilder infoBuilder = new StringBuilder();
            if (!cachedType.isEmpty()) {
                infoBuilder.append(cachedType.replace("[", "").replace("]", "").replace(",", "/"));
            }
            if (!cachedYear.isEmpty()) {
                if (infoBuilder.length() > 0) infoBuilder.append(" | ");
                infoBuilder.append(cachedYear);
            }
            if (!cachedArea.isEmpty()) {
                if (infoBuilder.length() > 0) infoBuilder.append(" | ");
                infoBuilder.append(cachedArea.replace("[", "").replace("]", "").replace(",", "/"));
            }
            if (infoBuilder.length() > 0) {
                tv_info_infinity.setText(infoBuilder.toString());
            } else {
                tv_info_infinity.setText("加载中...");
            }
        }
    }

    /*没用*/
//    private int getPX(int i) {
//        return getResources().getDimensionPixelSize(i);
//    }

    /*设置侦听器*/
    protected void setListener() {
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
        
        // 无界UI使用动态卡片，不需要设置固定监听器
        if (Interface_Style == 4) {
            return;
        }
        
        for (int i = 0; i < re_typeLogs.length; i++) {
            re_typeLogs[i].setOnClickListener(this);
            //if(ISTV){
//				re_typeLogs[i].setOnFocusChangeListener(this);
            //}
            re_typeLogs[i].setOnFocusChangeListener(this);
            rebgs[i].setVisibility(View.GONE);
        }
        
        // 为FrameLayout也设置焦点监听器（电视遥控器焦点在FrameLayout上）
        for (int i = 0; i < re_fls.length; i++) {
            if (re_fls[i] != null) {
                final int index = i;
                re_fls[i].setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        int style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
                        if (hasFocus) {
                            if (style == 4) {
                                // 无界UI：放大动画 + 切换背景和详情
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    v.setElevation(10f);
                                }
                                v.animate()
                                    .scaleX(1.08f)
                                    .scaleY(1.08f)
                                    .setDuration(150)
                                    .start();
                                try {
                                    if (data != null && data.size() > 0) {
                                        int dataIndex = index - 3;
                                        if (dataIndex >= 0 && dataIndex < data.size()) {
                                            getMovieDetailsAndShow(dataIndex);
                                        }
                                    }
                                } catch (Exception e) { e.printStackTrace(); }
                            } else {
                                // 其他UI：执行动画
                                showOnFocusTranslAnimation(index);
                                if (null != home.whiteBorder) {
                                    home.whiteBorder.setVisibility(View.VISIBLE);
                                }
                                flyAnimation(index);
                            }
                        } else {
                            if (Interface_Style == 4) {
                                // 无界UI：缩小动画
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                    v.setElevation(0f);
                                }
                                v.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setDuration(150)
                                    .start();
                            } else {
                                showLooseFocusTranslAinimation(index);
                            }
                        }
                    }
                });
            }
        }
    }

    /*聚焦变化时*/
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int paramInt = 0;
        switch (v.getId()) {
            case R.id.iv_re_0:
                paramInt = 0;
                break;
            case R.id.iv_re_1:
                paramInt = 1;
                break;
            case R.id.iv_re_2:
                paramInt = 2;
                break;
            case R.id.iv_re_3:
                paramInt = 3;
                break;
            case R.id.iv_re_4:
                paramInt = 4;
                break;
            case R.id.iv_re_5:
                paramInt = 5;
                break;
            case R.id.iv_re_6:
                paramInt = 6;
                break;
            case R.id.iv_re_7:
                paramInt = 7;
                break;
            case R.id.iv_re_8:
                paramInt = 8;
                break;
            case R.id.iv_re_9:
                paramInt = 9;
                break;
            case R.id.iv_re_10:
                paramInt = 10;
                break;
        }
        if (hasFocus) {
            int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
            if (Interface_Style == 4) {
                try {
                    if (data != null && data.size() > 0) {
                        int dataIndex = paramInt - 3;
                        if (dataIndex >= 0 && dataIndex < data.size()) {
                            // 调用getMovieDetailsAndShow获取横版背景图
                            getMovieDetailsAndShow(dataIndex);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
            showOnFocusTranslAnimation(paramInt);
            if (null != home.whiteBorder) {
                home.whiteBorder.setVisibility(View.VISIBLE);
            }
            flyAnimation(paramInt);
        } else {
            showLooseFocusTranslAinimation(paramInt);
        }
        for (TextView tv : tvs) {
            if (tv.getVisibility() != View.GONE) {
                tv.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 飞框焦点动画
     *
     * @param paramInt
     */
    private void flyAnimation(int paramInt) {
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
        if (Interface_Style == 4) return;

        int[] location = new int[2];
        re_typeLogs[paramInt].getLocationOnScreen(location);
        int width = re_typeLogs[paramInt].getWidth();
        int height = re_typeLogs[paramInt].getHeight();
        float x = (float) location[0];
        float y = (float) location[1];
        Logger.v("joychang", "paramInt=" + paramInt + "..x=" + x + "...y=" + y);
        switch (paramInt) {
            case 0:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    x = getResources().getDimensionPixelSize(R.dimen.sm_49);
                    y = getResources().getDimensionPixelSize(R.dimen.sm_190) - 3;
                } else {
                    x = getResources().getDimensionPixelSize(R.dimen.sm_21);
                    y = getResources().getDimensionPixelSize(R.dimen.sm_164);
                }
                break;
            case 1:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    y = getResources().getDimensionPixelSize(R.dimen.sm_310) + 14;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_49);
                } else {
                    y = 298;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_21);
                }
                //y = getResources().getDimensionPixelSize(R.dimen.sm_316);
                break;
            case 2:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    x = getResources().getDimensionPixelSize(R.dimen.sm_49);
                    y = getResources().getDimensionPixelSize(R.dimen.sm_450) - 1;
                } else {
                    x = 42 - 21;
                    y = 425 + 4;
                }
                break;
            case 3:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    width = width + 24 + 14;
                    height = height + 13 + 8;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_370) - 2;
                    y = getResources().getDimensionPixelSize(R.dimen.sm_252) + 1;
                } else {
                    width = width + 24;
                    height = height + 16;
                    x = (float) 188 + 154;
                    y = (float) 189 + 40;
                }
                break;
            case 4:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    width = width + 13 + 6;
                    height = height + 7 + 5;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_246) - 2;
                    y = getResources().getDimensionPixelSize(R.dimen.sm_456) + 12;
                } else {
                    width = width + 13;
                    height = height + 8;
                    x = (float) 188 + 28;
                    y = (float) 436 + 8;
                }
                break;
            case 5:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    width = width + 13 + 6;
                    height = height + 7 + 5;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_481) + 2;
                    y = getResources().getDimensionPixelSize(R.dimen.sm_456) + 12;
                } else {
                    width = width + 13;
                    height = height + 8;
                    x = (float) 420 + 38;
                    y = (float) 436 + 8;
                }
                break;
            case 6:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    width = width + 15 + 8;
                    height = height + 22 + 13;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_746) + 3;
                    y = getResources().getDimensionPixelSize(R.dimen.sm_320) + 9;
                } else {
                    width = width + 18;
                    height = height + 26;
                    x = (float) 654 + 75;
                    y = (float) 189 + 115;
                }
                break;
            case 7:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    width = width + 17 + 10;
                    height = height + 12 + 5;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_1000) + 73;
                    y = getResources().getDimensionPixelSize(R.dimen.sm_220) + 1;
                } else {
                    width = width + 17;
                    height = height + 14;
                    x = (float) 924 + 111;
                    y = (float) 189 + 8;
                }
                break;
            case 8:
                if (mHeight > 1000 && mWidth > 1000) {
                    //1080p
                    width = width + 17 + 10;
                    height = height + 12 + 5;
                    x = getResources().getDimensionPixelSize(R.dimen.sm_1000) + 73;
                    y = getResources().getDimensionPixelSize(R.dimen.sm_435) - 2;
                } else {
                    width = width + 17;
                    height = height + 14;
                    x = (float) 924 + 111;
                    y = (float) 394 + 18;
                }
                break;

        }
        Logger.d(TAG, "X=" + x + "---Y=" + y);
        home.flyWhiteBorder(width, height, x, y);
    }

    /**
     *显示焦点平移动画
     *
     * @param paramInt
     */
    private void showOnFocusTranslAnimation(int paramInt) {

        re_fls[paramInt].bringToFront();//将当前FrameLayout置为顶层
        Animation mtAnimation = null;
        Animation msAnimation = null;

        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
        if (Interface_Style == 0||Interface_Style == 3){
            /*旧UI*/
            switch (paramInt) {
                case 0:
                    mtAnimation = animEffect.translAnimation(0.0f, -20.0f, 0.0f, -5.0f);
                    break;
                case 1:
                    mtAnimation = animEffect.translAnimation(0.0f, -20.0f, 0.0f, 1.0f);
                    break;
                case 2:
                    mtAnimation = animEffect.translAnimation(0.0f, -20.0f, 0.0f, 5.0f);
                    break;
                case 3:
                    mtAnimation = animEffect.translAnimation(0.0f, -10.0f, 0.0f, -5.0f);
                    break;
                case 4:
                    mtAnimation = animEffect.translAnimation(0.0f, -20.0f, 0.0f, 5.0f);
                    break;
                case 5:
                    mtAnimation = animEffect.translAnimation(0.0f, -10.0f, 0.0f, 5.0f);
                    break;
                case 6:
                    mtAnimation = animEffect.translAnimation(0.0f, 10.0f, 0.0f, 0.0f);
                    break;
                case 7:
                    mtAnimation = animEffect.translAnimation(0.0f, 20.0f, 0.0f, -5.0f);
                    break;
                case 8:
                    mtAnimation = animEffect.translAnimation(0.0f, 20.0f, 0.0f, 5.0f);
                    break;
                default:
                    break;
            }
        }else if(Interface_Style == 1){
            /*新UI*/
            switch (paramInt) {
                case 0:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 1:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 2:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 4:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 5:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 6:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 7:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 8:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 9:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 10:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                default:
                    break;
            }
        }else if(Interface_Style == 2){
            /*新UI圆角*/
            switch (paramInt) {
                case 0:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 1:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 2:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 4:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 5:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 6:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 7:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 8:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 9:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 10:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                default:
                    break;
            }
        }

        msAnimation = animEffect.ScaleAnimation(1.0F, 1.05F, 1.0F, 1.05F);
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(msAnimation);
        set.addAnimation(mtAnimation);
        set.setFillAfter(true);
//		set.setFillEnabled(true);
        set.setAnimationListener(new MyOnFocusAnimListenter(paramInt));
//		ImageView iv = re_typeLogs[paramInt];
//		iv.setAnimation(set);
//		set.startNow(); TODO
        re_fls[paramInt].startAnimation(set);
        //re_fls[paramInt].startAnimation(set);

    }

    /**
     * 失去焦点缩小
     *
     * @param paramInt
     */
    private void showLooseFocusTranslAinimation(int paramInt) {
        Animation mAnimation = null;
        Animation mtAnimation = null;
        Animation msAnimation = null;
        AnimationSet set = null;
        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
        if (Interface_Style == 0||Interface_Style == 3){
            /*旧UI*/
            switch (paramInt) {
                case 0:
                    mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, -5.0f, 0.0f);
                    break;
                case 1:
                    mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 1.0f, 0.0f);
                    break;
                case 2:
                    mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 3:
                    mtAnimation = animEffect.translAnimation(-10.0f, 0.0f, -5.0f, 0.0f);
                    break;
                case 4:
                    mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 5:
                    mtAnimation = animEffect.translAnimation(-10.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 6:
                    mtAnimation = animEffect.translAnimation(10.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 7:
                    mtAnimation = animEffect.translAnimation(20.0f, 0.0f, -5.0f, 0.0f);
                    break;
                case 8:
                    mtAnimation = animEffect.translAnimation(20.0f, 0.0f, 5.0f, 0.0f);
                    break;

                default:
                    break;

            }
        }else if(Interface_Style == 1){
            /*新UI*/
            switch (paramInt) {
                case 0:
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, -5.0f, 0.0f);
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 1:
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 1.0f, 0.0f);
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 2:
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 5.0f, 0.0f);
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(-10.0f, 0.0f, -5.0f, 0.0f);
                    break;
                case 4:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 5:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(-10.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 6:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(10.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 7:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(20.0f, 0.0f, -5.0f, 0.0f);
                    break;
                case 8:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(20.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 9:///
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 10:///
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                default:
                    break;

            }
        }else if(Interface_Style == 2){
            /*新UI圆角*/
            switch (paramInt) {
                case 0:
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, -5.0f, 0.0f);
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 1:
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 1.0f, 0.0f);
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 2:
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 5.0f, 0.0f);
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 3:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(-10.0f, 0.0f, -5.0f, 0.0f);
                    break;
                case 4:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(-20.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 5:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(-10.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 6:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(10.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 7:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(20.0f, 0.0f, -5.0f, 0.0f);
                    break;
                case 8:
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    //mtAnimation = animEffect.translAnimation(20.0f, 0.0f, 5.0f, 0.0f);
                    break;
                case 9:///
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                case 10:///
                    mtAnimation = animEffect.translAnimation(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                default:
                    break;

            }
        }

        msAnimation = animEffect.ScaleAnimation(1.05F, 1.0F, 1.05F, 1.0F);
        set = new AnimationSet(true);
        set.addAnimation(msAnimation);
        set.addAnimation(mtAnimation);
        set.setFillAfter(true);
//		set.setFillEnabled(true);
        set.setAnimationListener(new MyLooseFocusAnimListenter(paramInt));
//		ImageView iv = re_typeLogs[paramInt];
//		iv.setAnimation(set);
//		set.startNow();
//		mAnimation.setAnimationListener(new MyLooseFocusAnimListenter(paramInt));
        rebgs[paramInt].setVisibility(View.GONE);
        re_fls[paramInt].startAnimation(set);
    }

    /**
     * 根据状态来下载或者打开app
     *
     * @param apkurl
     * @param packName
     * @author drowtram
     */
    private void startOpenOrDownload(String apkurl, String packName, String fileName) {
        //判断当前应用是否已经安装
        for (PackageInfo pack : home.packLst) {
            if (pack.packageName.equals(packName)) {
                //已安装了apk，则直接打开
                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(packName);
                startActivity(intent);
                return;
            }
        }
        //如果没有安装，则查询本地是否有安装包文件，有则直接安装
        if (!Utils.startCheckLoaclApk(home, fileName)) {
            //如果没有安装包  则进行下载安装
            Utils.startDownloadApk(home, apkurl, null);
        }
    }

    /*单击*/
    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.iv_re_0:
                String username = sp.getString("userName", null);
                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 0||SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 3){
                    /*旧UI搜索*/
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    i = new Intent();
                    i.setClass(home, SearchActivity.class);
                    i.putExtra("TYPE", "ALL");
                    startActivity(i);

                    break;
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 1){
                    /*新UI个人中心*/
                    i = new Intent();
                    i.setClass(home, UserActivity.class);
                    startActivity(i);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 2){
                    /*新UI圆角推荐1*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(0);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI - 隐藏按钮不处理*/
                    return;
                }
                break;
            case R.id.iv_re_1:

                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 0 || SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 3){
                    /*旧UI个人中心*/
                    i = new Intent();
                    i.setClass(home, UserActivity.class);
                    startActivity(i);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 1){
                    /*新UI开通会员-个性主题*/
//                    username = sp.getString("userName", null);
//                    if (username == null) {
//                        if (username == null) {
//                            Utils.showToast("请先登录账号!",context, R.drawable.toast_err);
//                            startActivity(new Intent(context, UserActivity.class));
//                            break;
//                        }
//                    }
                    i = new Intent();
//                    i.setClass(home, EmpowerActivity.class);
                    i.setClass(home, SettingWallpaperActivity.class);
                    startActivity(i);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 2){
                    /*新UI圆角推荐2*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(1);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI - 隐藏按钮不处理*/
                    return;
                }

                break;
            case R.id.iv_re_2:
                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 0 || SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 3){
                    /*旧UI历史记录*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    startActivity(new Intent(context, HistoryActivity.class));
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 1){
                    /*新UI播放设置*/
                    i = new Intent();
                    i.setClass(home, SettingPlayActivity.class);
                    startActivity(i);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 2){
                    /*新UI圆角推荐3*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(2);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI - 隐藏按钮不处理*/
                    return;
                }

                break;
            case R.id.iv_re_3:
//                username = sp.getString("userName", null);
//                if (username == null && username == null) {
//                    mediaHandler.sendEmptyMessage(8);
//                    break;
//                }
//                /*推荐一*/
//                GetMotion(0);

                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 0 || SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 3){
                    /*旧UI推荐1*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    /*推荐一*/
                    GetMotion(0);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 1){
                    /*新ui推荐1*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    /*推荐一*/
                    GetMotion(0);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 2){
                    /*新UI圆角推荐4*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(3);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI推荐1 - 直接跳转播放*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(0);
                }
                break;
            case R.id.iv_re_4:
//                username = sp.getString("userName", null);
//                if (username == null && username == null) {
//                    mediaHandler.sendEmptyMessage(8);
//                    break;
//                }
//                /*推荐二*/
//                GetMotion(1);
                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 0 || SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 3){
                    /*旧UI推荐2*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    /*推荐二*/
                    GetMotion(1);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 1){
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    /*推荐二*/
                    GetMotion(1);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 2){
                    /*新UI圆角推荐5*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(4);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI推荐2 - 直接跳转播放*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(1);
                }
                break;
            case R.id.iv_re_5:
//                username = sp.getString("userName", null);
//                if (username == null && username == null) {
//                    mediaHandler.sendEmptyMessage(8);
//                    break;
//                }
//                /*推荐三*/
//                GetMotion(2);
                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 0 || SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 3){
                    /*旧UI推荐3*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    /*推荐三*/
                    GetMotion(2);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 1){
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    /*推荐三*/
                    GetMotion(2);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 2){
                    /*新UI圆角推荐6*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(5);
                }else if(SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI推荐3 - 直接跳转播放*/
                    username = sp.getString("userName", null);
                    if (username == null && username == null) {
                        mediaHandler.sendEmptyMessage(8);
                        break;
                    }
                    GetMotion(2);
                }
                break;
            case R.id.iv_re_6:
                username = sp.getString("userName", null);
                if (username == null && username == null) {
                    mediaHandler.sendEmptyMessage(8);
                    break;
                }
                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI推荐4 - 直接跳转播放*/
                    GetMotion(3);
                }else{
                    /*推荐四*/
                    GetMotion(3);
                }
                break;
            case R.id.iv_re_7:
                username = sp.getString("userName", null);
                if (username == null && username == null) {
                    mediaHandler.sendEmptyMessage(8);
                    break;
                }
                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI推荐5 - 直接跳转播放*/
                    GetMotion(4);
                }else{
                    /*推荐五*/
                    GetMotion(4);
                }
                break;
            case R.id.iv_re_8:
                username = sp.getString("userName", null);
                if (username == null && username == null) {
                    mediaHandler.sendEmptyMessage(8);
                    break;
                }
                if (SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4) == 4){
                    /*无界UI推荐6 - 直接跳转播放*/
                    GetMotion(5);
                }else{
                    /*推荐六*/
                    GetMotion(5);
                }
                break;
            case R.id.iv_re_9:
                /*新ui清理记录*/
                i = new Intent();
                i.setClass(home, ClearActivity.class);
                startActivity(i);
                break;
            case R.id.iv_re_10:
                /*新ui关于我们*/
                i = new Intent();
                i.setClass(home, AboutActivity.class);
                startActivity(i);
                break;
        }
        home.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    private class WindowMessageID {
        public static final int RECOMMEND_EXPIRE = 2;
        public static final int RECOMMEND_OFFSITE = 3;
        public static final int RESPONSE_NO_SUCCESS = 1;

        private WindowMessageID() {
        }
    }

    /**
     * 获取焦点时动画监听
     *
     * @author joychang
     */
    public class MyOnFocusAnimListenter implements Animation.AnimationListener {

        private int paramInt;

        public MyOnFocusAnimListenter(int paramInt) {
            this.paramInt = paramInt;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Logger.v("joychang", "onAnimationEnd");
            rebgs[paramInt].setVisibility(View.VISIBLE);
//			Animation localAnimation =animEffect
//					.alphaAnimation(0.0F, 1.0F, 150L, 0L);
//			localImageView.startAnimation(localAnimation);

            int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
            if (Interface_Style == 0||Interface_Style == 1||Interface_Style == 3){
                if (paramInt >= 3 && paramInt <= 8) {
                    tvs[paramInt - 3].setVisibility(View.VISIBLE);
                }
            }else if(Interface_Style == 2){
                tvs[paramInt].setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

    }

    /**
     * 获取焦点时动画监听
     *
     * @author joychang
     */
    public class MyLooseFocusAnimListenter implements Animation.AnimationListener {

        private int paramInt;

        public MyLooseFocusAnimListenter(int paramInt) {
            this.paramInt = paramInt;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            Logger.v("joychang", "onAnimationEnd");
//			Animation localAnimation =animEffect
//					.alphaAnimation(0.0F, 1.0F, 150L, 0L);
//			localImageView.startAnimation(localAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

    }

    /*心跳*/
    private void GetMotion(final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (true) {
            try {
                if (new Date(System.currentTimeMillis()).getTime() < simpleDateFormat.parse(GetTimeStamp.timeStamp2Date(sp.getString("vip", null), "")).getTime() || sp.getString("vip", null).equals("999999999")) {
                    vipstate = 1;/*没到期*/
                } else {
                    vipstate = 0;/*已到期*/
                }
                mQueue = Volley.newRequestQueue(getActivity(), new ExtHttpStack());
                String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.s, ""),Constant.d);
                final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.kd, ""),Constant.d);
                final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.tb, ""),Constant.d);
                final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.um, ""),Constant.d);
                final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.im, ""),Constant.d);
                final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.yk, ""),Constant.d);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=motion",
                        new com.android.volley.Response.Listener<String>() {
                            public void onResponse(String response) {
                                GetMotionResponse(response,position);
                            }
                        }, new com.android.volley.Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        mediaHandler.sendEmptyMessage(1);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        String codedata = "token=" + sp.getString("ckinfo", null) + "&t=" + GetTimeStamp.timeStamp();
                        int miType = SharePreferenceDataUtil.getSharedIntData(getActivity(), Constant.ue, 1);
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
                        headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Authorization", ""),Constant.d));  /*设置其他请求头*/
                        return headers;
                    }
                };
                mQueue.add(stringRequest);
                return;
            } catch (ParseException ex) {
                ex.printStackTrace();
                continue;
            }
        }



    }

    /*心跳响应*/
    public void GetMotionResponse(String response,final int position) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.kd, ""), Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), Constant.im, ""),Constant.d);
        //Log.i(TAG, "GetMotionResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(getActivity(), Constant.ue, 1);
                JSONObject msg = null;
                if (miType == 1) {
                    msg = new JSONObject(Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY));
                } else if (miType == 2) {
                    msg = new JSONObject(Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY));
                } else if (miType == 3) {
                    msg = new JSONObject(AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV));
                }
                String vip = msg.optString("vip");
                int Try = msg.optInt("Try");
                int Clientmode = msg.optInt("Clientmode");
                trystate = Try;
                sp.edit().putString("vip", vip).commit();
                Sp.edit()
                        .putInt("Submission_method", Clientmode)
                        .putInt("Trystate", Try)
                        .commit();
            }else if (code == 127) {/*其他设备登录*/
                mediaHandler.sendEmptyMessage(3);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 114) {/*账户封禁*/
                mediaHandler.sendEmptyMessage(4);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 125) {/*账户信息错误*/
                mediaHandler.sendEmptyMessage(7);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 127) {/*账户信息失效*/
                mediaHandler.sendEmptyMessage(6);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 201){/*201心跳失败*/
                mediaHandler.sendEmptyMessage(5);
                return;
            }else if (code == 106){/*201心跳失败*/
                mediaHandler.sendEmptyMessage(5);
                return;
            }
            if (vipstate == 1 || trystate == 1) {
                // 防止重复跳转
                if (isNavigating) {
                    return;
                }
                isNavigating = true;
                i = new Intent();
                i.setClass(home, VideoDetailsActivity.class);
                i.putExtra("nextlink", data.get(position).getTjurl());
                i.putExtra("vodstate", data.get(position).getState());
                i.putExtra("vodtype", data.get(position).getTjtype().toUpperCase());
                startActivity(i);
            }else{
                mediaHandler.sendEmptyMessage(2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*获取影片详情并显示在首页*/
    private void getMovieDetailsAndShow(final int position) {
        if (data == null || position >= data.size() || getActivity() == null) {
            return;
        }
        
        // 保存当前选中的位置
        currentSelectedPosition = position;
        
        // 优先从缓存读取
        if (detailCache.containsKey(position)) {
            VideoDetailInfo cachedInfo = detailCache.get(position);
            android.util.Log.d("RecommendFragment", "getMovieDetailsAndShow: using cache for position=" + position + ", title=" + cachedInfo.getTitle());
            currentDetailInfo = cachedInfo;
            displayMovieDetails(cachedInfo);
            // 更新背景图（使用推荐列表中的横版图）
            String bgUrl = data.get(position).getTjpicur();
            updateBackgroundImage(bgUrl);
            return;
        }
        
        // 缓存中没有，发起网络请求
        android.util.Log.d("RecommendFragment", "getMovieDetailsAndShow: cache miss, requesting position=" + position);
        
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Api_url", ""),Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "BASE_HOST", ""),Constant.d);
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getActivity(), new ExtHttpStack());
        }
        
        GsonRequest<VideoDetailInfo> mVodData = new GsonRequest<VideoDetailInfo>(Method.POST, 
                Api_url + "/api.php/" + BASE_HOST + "/vod/" + data.get(position).getTjurl(),
                VideoDetailInfo.class, new Response.Listener<VideoDetailInfo>() {
                    @Override
                    public void onResponse(VideoDetailInfo response) {
                        if (response != null && getActivity() != null) {
                            // 存入缓存
                            detailCache.put(position, response);
                            android.util.Log.d("RecommendFragment", "onResponse: position=" + position + ", currentSelectedPosition=" + currentSelectedPosition + ", title=" + response.getTitle());
                            // 只有当前选中的位置还是请求时的位置才更新UI
                            if (currentSelectedPosition == position) {
                                currentDetailInfo = response;
                                displayMovieDetails(response);
                                // 更新背景图（使用推荐列表中的横版图）
                                String bgUrl = data.get(position).getTjpicur();
                                updateBackgroundImage(bgUrl);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        android.util.Log.e("RecommendFragment", "onErrorResponse: position=" + position + ", error=" + (error != null ? error.getMessage() : "null"));
                    }
                }) {
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
                params.put("os", Integer.toString(android.os.Build.VERSION.SDK_INT));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Authorization", ""),Constant.d));
                return headers;
            }
        };
        
        mQueue.add(mVodData);
    }

    /*更新背景图，保持旧图直到新图加载完成，避免黑屏*/
    private void updateBackgroundImage(final String bgUrl) {
        if (getActivity() == null || bgUrl == null || imageLoader == null) return;
        
        final ImageView bg = (ImageView) getActivity().findViewById(R.id.iv_bg_infinity);
        if (bg == null) return;
        
        // 使用自定义ImageListener，只在成功加载时才更新图片，失败或加载中保持原图
        imageLoader.get(bgUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    // 新图片加载成功，更新背景
                    bg.setImageBitmap(response.getBitmap());
                }
                // 如果bitmap为null（正在加载中），保持原图不变
            }
            
            @Override
            public void onErrorResponse(VolleyError error) {
                // 加载失败，保持原图不变，不显示默认图
                android.util.Log.e("RecommendFragment", "Background image load failed: " + bgUrl);
            }
        });
    }
    
    /*显示影片详情*/
    private void displayMovieDetails(VideoDetailInfo info) {
        if (info == null || getActivity() == null) return;
        
        // 缓存第一个推荐项的详情（用于下次进入时快速显示）
        if (currentSelectedPosition == 0 && data != null && currentSelectedPosition < data.size()) {
            String picUrl = data.get(currentSelectedPosition).getTjpicur();
            sp.edit().putString("infinity_bg_cache", picUrl != null ? picUrl : "")
                    .putString("infinity_title_cache", info.getTitle() != null ? info.getTitle() : "")
                    .putString("infinity_rating_cache", info.getRaing() != null ? info.getRaing() : "")
                    .putString("infinity_intro_cache", info.getIntro() != null ? info.getIntro() : "")
                    .putString("infinity_type_cache", info.getType() != null ? java.util.Arrays.toString(info.getType()) : "")
                    .putString("infinity_year_cache", info.getPubtime() != null ? info.getPubtime() : "")
                    .putString("infinity_area_cache", info.getArea() != null ? java.util.Arrays.toString(info.getArea()) : "")
                    .apply();
        }
        
        // 设置标题
        if (tv_title_infinity != null && info.getTitle() != null) {
            tv_title_infinity.setText(info.getTitle());
        }
        
        // 设置评分
        if (tv_rating_infinity != null && info.getRaing() != null) {
            tv_rating_infinity.setText("豆瓣 " + info.getRaing());
        }
        
        // 设置年份
        if (tv_year_infinity != null && info.getPubtime() != null) {
            tv_year_infinity.setText(info.getPubtime());
        }
        
        // 设置类型
        if (tv_type_infinity != null && info.getType() != null) {
            String typeText = java.util.Arrays.toString(info.getType()).replace("[", "").replace("]", "").replace(",", " / ");
            tv_type_infinity.setText(typeText);
        }
        
        // 设置影片信息（类型 | 年代 | 地区）
        if (tv_info_infinity != null) {
            StringBuilder infoBuilder = new StringBuilder();
            // 类型
            if (info.getType() != null && info.getType().length > 0) {
                String typeText = java.util.Arrays.toString(info.getType()).replace("[", "").replace("]", "").replace(",", "/");
                infoBuilder.append(typeText);
            }
            // 年代
            if (info.getPubtime() != null && !info.getPubtime().isEmpty()) {
                if (infoBuilder.length() > 0) infoBuilder.append(" | ");
                infoBuilder.append(info.getPubtime());
            }
            // 地区
            if (info.getArea() != null && info.getArea().length > 0) {
                if (infoBuilder.length() > 0) infoBuilder.append(" | ");
                String areaText = java.util.Arrays.toString(info.getArea()).replace("[", "").replace("]", "").replace(",", "/");
                infoBuilder.append(areaText);
            }
            tv_info_infinity.setText(infoBuilder.toString());
        }
        
        // 设置简介
        if (tv_desc_infinity != null && info.getIntro() != null) {
            tv_desc_infinity.setText("简介：" + info.getIntro());
        }
        
        // 设置播放按钮点击事件
        if (btn_play_infinity != null) {
            btn_play_infinity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到播放页面
                    if (currentDetailInfo != null && currentDetailInfo.getVideo_list() != null 
                            && currentDetailInfo.getVideo_list().size() > 0 && currentSelectedPosition >= 0) {
                        // 使用保存的当前选中位置
                        GetMotion(currentSelectedPosition);
                    }
                }
            });
        }
    }
    
    /**
     * 无界UI屏幕适配方法
     * 根据屏幕尺寸动态设置卡片大小、字体大小、间距等
     */
    private void applyInfinityUIAdapter() {
        if (view == null || getActivity() == null) return;
        
        // 初始化屏幕适配工具
        ScreenAdapterUtil adapter = ScreenAdapterUtil.getInstance();
        adapter.init(getActivity());
        
        int screenWidth = adapter.getScreenWidth();
        int screenHeight = adapter.getScreenHeight();
        float scale = adapter.getScale();
        
        // 调试日志
        Logger.d(TAG, "Screen: " + screenWidth + "x" + screenHeight + ", scale=" + scale);
        
        // ========== 1. 底部推荐区域高度适配 ==========
        LinearLayout progressLayout = (LinearLayout) view.findViewById(R.id.progressLayout);
        if (progressLayout != null) {
            // 底部区域占屏幕高度的25%
            int bottomHeight = adapter.getHeightPercent(25);
            ViewGroup.LayoutParams progressParams = progressLayout.getLayoutParams();
            progressParams.height = bottomHeight;
            progressLayout.setLayoutParams(progressParams);
        }
        
        // ========== 2. 卡片宽度适配 ==========
        // 无界UI使用动态卡片，不需要适配固定卡片
        // 动态卡片的尺寸在 createDynamicRecommendCards 中设置
        
        // ========== 3. 卡片标题字体适配 ==========
        // 无界UI使用动态卡片，tvs数组不再使用
        
        // ========== 4. 顶部功能按钮适配 ==========
        // 图标和字体大小基于屏幕高度百分比计算
        int iconSize = adapter.getHeightPercent(4); // 图标增大
        int buttonPadding = adapter.getHeightPercent(1);
        int buttonMargin = adapter.getWidthPercent(1);
        int buttonTextSizePx = adapter.getHeightPercent(3); // 按钮字体增大
        
        // 搜索按钮
        ImageView ivSearchIcon = (ImageView) view.findViewById(R.id.iv_search_icon);
        TextView tvSearchText = (TextView) view.findViewById(R.id.tv_search_text);
        LinearLayout btnSearch = (LinearLayout) view.findViewById(R.id.btn_search_infinity);
        if (ivSearchIcon != null) {
            ViewGroup.LayoutParams iconParams = ivSearchIcon.getLayoutParams();
            iconParams.width = iconSize;
            iconParams.height = iconSize;
            ivSearchIcon.setLayoutParams(iconParams);
        }
        if (tvSearchText != null) {
            tvSearchText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, buttonTextSizePx);
        }
        if (btnSearch != null) {
            btnSearch.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        }
        
        // 历史按钮
        ImageView ivHistoryIcon = (ImageView) view.findViewById(R.id.iv_history_icon);
        TextView tvHistoryText = (TextView) view.findViewById(R.id.tv_history_text);
        LinearLayout btnHistory = (LinearLayout) view.findViewById(R.id.btn_history_infinity);
        if (ivHistoryIcon != null) {
            ViewGroup.LayoutParams iconParams = ivHistoryIcon.getLayoutParams();
            iconParams.width = iconSize;
            iconParams.height = iconSize;
            ivHistoryIcon.setLayoutParams(iconParams);
        }
        if (tvHistoryText != null) {
            tvHistoryText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, buttonTextSizePx);
        }
        if (btnHistory != null) {
            btnHistory.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) btnHistory.getLayoutParams();
            mlp.setMarginStart(buttonMargin);
            btnHistory.setLayoutParams(mlp);
        }
        
        // 设置按钮
        ImageView ivSettingIcon = (ImageView) view.findViewById(R.id.iv_setting_icon);
        TextView tvSettingText = (TextView) view.findViewById(R.id.tv_setting_text);
        LinearLayout btnSetting = (LinearLayout) view.findViewById(R.id.btn_play_setting_infinity);
        if (ivSettingIcon != null) {
            ViewGroup.LayoutParams iconParams = ivSettingIcon.getLayoutParams();
            iconParams.width = iconSize;
            iconParams.height = iconSize;
            ivSettingIcon.setLayoutParams(iconParams);
        }
        if (tvSettingText != null) {
            tvSettingText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, buttonTextSizePx);
        }
        if (btnSetting != null) {
            btnSetting.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) btnSetting.getLayoutParams();
            mlp.setMarginStart(buttonMargin);
            btnSetting.setLayoutParams(mlp);
        }
        
        // 我的按钮
        ImageView ivUserIcon = (ImageView) view.findViewById(R.id.iv_user_icon);
        TextView tvUserText = (TextView) view.findViewById(R.id.tv_user_text);
        LinearLayout btnUser = (LinearLayout) view.findViewById(R.id.btn_user_infinity);
        if (ivUserIcon != null) {
            ViewGroup.LayoutParams iconParams = ivUserIcon.getLayoutParams();
            iconParams.width = iconSize;
            iconParams.height = iconSize;
            ivUserIcon.setLayoutParams(iconParams);
        }
        if (tvUserText != null) {
            tvUserText.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, buttonTextSizePx);
        }
        if (btnUser != null) {
            btnUser.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) btnUser.getLayoutParams();
            mlp.setMarginStart(buttonMargin);
            btnUser.setLayoutParams(mlp);
        }
        
        // ========== 5. 右上角时间和网络状态适配 ==========
        int timeTextSizePx = adapter.getHeightPercent(3); // 时间字体缩小
        if (tv_time_infinity != null) {
            tv_time_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, timeTextSizePx);
        }
        if (iv_net_state_infinity != null) {
            int netIconSize = adapter.getHeightPercent(3);
            ViewGroup.LayoutParams netParams = iv_net_state_infinity.getLayoutParams();
            netParams.width = netIconSize;
            netParams.height = netIconSize;
            iv_net_state_infinity.setLayoutParams(netParams);
        }
        
        // ========== 6. 影片信息区域字体和间距适配 ==========
        int titleSizePx = adapter.getHeightPercent(7); // 标题字体
        int infoSizePx = adapter.getHeightPercent(4);  // 信息字体
        int descSizePx = adapter.getHeightPercent(4);  // 简介字体
        int infoContainerTop = adapter.getHeightPercent(5); // 信息容器顶部边距
        int infoMarginTop = adapter.getHeightPercent(3);  // 信息间距（类型|年代|地区）
        int descMarginTop = adapter.getHeightPercent(4);  // 简介间距
        int btnMarginTop = adapter.getHeightPercent(5);   // 按钮间距
        
        // 设置影片信息容器顶部边距
        LinearLayout infoContainer = (LinearLayout) view.findViewById(R.id.bottom_info_container);
        if (infoContainer != null) {
            ViewGroup.MarginLayoutParams containerParams = (ViewGroup.MarginLayoutParams) infoContainer.getLayoutParams();
            containerParams.topMargin = infoContainerTop;
            infoContainer.setLayoutParams(containerParams);
        }
        
        if (tv_title_infinity != null) {
            tv_title_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, titleSizePx);
        }
        if (tv_rating_infinity != null) {
            tv_rating_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, infoSizePx);
        }
        if (tv_info_infinity != null) {
            tv_info_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, infoSizePx);
            // 设置间距
            ViewGroup.MarginLayoutParams infoParams = (ViewGroup.MarginLayoutParams) tv_info_infinity.getLayoutParams();
            infoParams.topMargin = infoMarginTop;
            tv_info_infinity.setLayoutParams(infoParams);
        }
        if (tv_desc_infinity != null) {
            tv_desc_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, descSizePx);
            // 设置间距
            ViewGroup.MarginLayoutParams descParams = (ViewGroup.MarginLayoutParams) tv_desc_infinity.getLayoutParams();
            descParams.topMargin = descMarginTop;
            tv_desc_infinity.setLayoutParams(descParams);
        }
        // 按钮容器间距
        LinearLayout btnContainer = (LinearLayout) view.findViewById(R.id.btn_container_infinity);
        if (btnContainer != null) {
            ViewGroup.MarginLayoutParams btnParams = (ViewGroup.MarginLayoutParams) btnContainer.getLayoutParams();
            btnParams.topMargin = btnMarginTop;
            btnContainer.setLayoutParams(btnParams);
        }
        
        // ========== 7. 公告和账号信息字体适配 ==========
        int smallTextSizePx = adapter.getHeightPercent(3); // 增大字体
        // 公告标签
        TextView tvGonggaoLabel = (TextView) view.findViewById(R.id.tv_gonggao_label);
        if (tvGonggaoLabel != null) {
            tvGonggaoLabel.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, smallTextSizePx);
        }
        if (tv_gonggao_infinity != null) {
            tv_gonggao_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, smallTextSizePx);
        }
        if (tv_account_infinity != null) {
            tv_account_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, smallTextSizePx);
        }
        if (tv_vip_expire_infinity != null) {
            tv_vip_expire_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, smallTextSizePx);
        }
        
        // ========== 8. 播放和收藏按钮适配 ==========
        int btnPaddingH = adapter.getWidthPercent(2);
        int btnPaddingV = adapter.getHeightPercent(2);
        int btnTextSizePx = adapter.getHeightPercent(3);
        int btnIconSize = adapter.getHeightPercent(3);
        
        if (btn_play_now_infinity != null) {
            btn_play_now_infinity.setPadding(btnPaddingH, btnPaddingV, btnPaddingH, btnPaddingV);
            
            // 播放按钮内的图标和文字
            if (btn_play_now_infinity.getChildCount() >= 2) {
                View playIcon = btn_play_now_infinity.getChildAt(0);
                View playText = btn_play_now_infinity.getChildAt(1);
                if (playIcon instanceof ImageView) {
                    ViewGroup.LayoutParams iconParams = playIcon.getLayoutParams();
                    iconParams.width = btnIconSize;
                    iconParams.height = btnIconSize;
                    playIcon.setLayoutParams(iconParams);
                }
                if (playText instanceof TextView) {
                    ((TextView) playText).setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, btnTextSizePx);
                }
            }
        }
        
        if (btn_collect_infinity != null) {
            btn_collect_infinity.setPadding(btnPaddingH, btnPaddingV, btnPaddingH, btnPaddingV);
        }
        if (iv_collect_icon_infinity != null) {
            ViewGroup.LayoutParams iconParams = iv_collect_icon_infinity.getLayoutParams();
            iconParams.width = btnIconSize;
            iconParams.height = btnIconSize;
            iv_collect_icon_infinity.setLayoutParams(iconParams);
        }
        if (tv_collect_text_infinity != null) {
            tv_collect_text_infinity.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, btnTextSizePx);
        }
        
        // ========== 9. 信息容器padding适配 ==========
        if (infoContainerInfinity != null) {
            int containerPadding = adapter.getWidthPercent(2);
            infoContainerInfinity.setPadding(containerPadding, 0, containerPadding, 0);
        }
        
        // ========== 10. HorizontalScrollView padding适配 ==========
        android.widget.HorizontalScrollView hsv = (android.widget.HorizontalScrollView) view.findViewById(R.id.hsv_recommend_infinity);
        if (hsv != null) {
            int hsvPaddingH = adapter.getWidthPercent(2);
            int hsvPaddingTop = adapter.getScaledSize(12);
            int hsvPaddingBottom = adapter.getScaledSize(24);
            hsv.setPadding(hsvPaddingH, hsvPaddingTop, hsvPaddingH, hsvPaddingBottom);
        }
        
        // ========== 11. 顶部功能栏padding适配 ==========
        LinearLayout topBar = (LinearLayout) view.findViewById(R.id.top_bar_infinity);
        if (topBar != null) {
            int topBarPaddingH = adapter.getWidthPercent(2);
            int topBarPaddingTop = adapter.getHeightPercent(1);
            topBar.setPadding(topBarPaddingH, topBarPaddingTop, topBarPaddingH, 0);
        }
        
        Logger.d(TAG, "Infinity UI Adapter applied: " + adapter.getDebugInfo());
    }
}
