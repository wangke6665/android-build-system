package com.shenma.tvlauncher.vod;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.EmpowerActivity;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.SettingWallpaperActivity;
import com.shenma.tvlauncher.UserActivity;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.vod.adapter.VodtypeAdapter;
import com.shenma.tvlauncher.vod.domain.RequestVo;
import com.shenma.tvlauncher.vod.domain.VodDataInfo;
import com.shenma.tvlauncher.vod.domain.VodFilter;
import com.shenma.tvlauncher.vod.domain.VodFilterInfo;
import com.shenma.tvlauncher.vod.domain.VodTypeInfo;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

/**
 * @author joychang
 * @Description 片源选择
 */
public class VodTypeActivity extends Activity implements OnItemClickListener {
    private static final int PAGESIZE = 30;
    private String VOD_TYPE;  // 改为实例变量，避免不同类目共享同一个URL
    private final String TAG = "VodTypeActivity";
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    protected SharedPreferences sp;
    public static SharedPreferences Sp;
    private String VOD_DATA = "VOD_DATA";
    private String VOD_FILTER = "VOD_FILTER";
    private List<String> areas;
    private String author = "";
    private Context context = this;
    private LinearLayout filter_type_container;
    private LinearLayout filter_year_container;
    private LinearLayout filter_area_container;
    private LinearLayout filter_sort_container;
    private int selected_type_index = -1; // -1表示"全部"
    private int selected_year_index = -1; // -1表示"全部"
    private int selected_area_index = -1; // -1表示"全部"
    private int selected_sort_index = -1; // -1表示"全部"
    private String[] sort = new String[]{"Hotdesc", "scoredesc", "updatedesc"};
    private int gHeight;
    private GridView gv_type_details_grid;
    private TextView iv_type_details_type;
    private TextView type_details_text;
    private int lastIndex = -1;
    private RequestQueue mQueue;
    private int vip;
    private boolean isNavigating = false; // 防止重复跳转标志
    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(context, R.string.request_failure, R.drawable.toast_err);
                    return;
                case 2:
                    Utils.showToast(context,R.string.Account_expiration, R.drawable.toast_err);
                    startActivity(new Intent(context, EmpowerActivity.class));
                    return;
                case 4:
                    tv_type_details_sum.setVisibility(View.VISIBLE);//显示多少部
                    return;
                case 5:
                    tv_type_details_sum.setVisibility(View.GONE);//显示多少部
                    return;
                case 6:
                    Utils.showToast(context,R.string.disconnect, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 7:
                    Utils.showToast(context,R.string.Account_has_been_disabled, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 8:
                    Utils.showToast(context,R.string.Account_information_error, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 9:
                    Utils.showToast(context,R.string.Account_information_has_expired,R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 10:
                    Utils.showToast(context,R.string.request_failures, R.drawable.toast_shut);
                    return;
                default:
                    return;
            }
        }
    };
    private int pageindex = 1;
    private long start;
    private int totalpage;
    private TextView tv_type_details_sum;
    private String type = null;
    private String typename = null;
    private List<String> types;
    private int vipstate;
    private int trystate = SharePreferenceDataUtil.getSharedIntData(this, "Trystate", 0);
    private ArrayList<VodDataInfo> vodDatas;
    private List<VodFilterInfo> vodFilter;
    private int vodpageindex;
    private VodtypeAdapter vodtypeAdapter;
    private VodTypeInfo vodtypeinfo;
    private List<String> years;
    private String filterString;

    /*创建时的回调函数*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);
        findViewById(R.id.vod).setBackgroundResource(R.drawable.video_details_bg);
        initIntent();
        initView();
        initData();
        initMenuData();
        sp = getSharedPreferences("shenma", 0);
        Sp = getSharedPreferences("initData", MODE_PRIVATE);
    }

    /*停止时*/
    protected void onStop() {
        super.onStop();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    /*恢复时*/
    @Override
    protected void onResume() {
        super.onResume();
        // 重置跳转标志，允许用户再次点击
        isNavigating = false;
    }

    /**
     * 获取影视类型
     */
    private void initIntent() {
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Api_url", ""),Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "BASE_HOST", ""),Constant.d);
        type = getIntent().getStringExtra("TYPE");
        if (type != null) {
            VOD_TYPE = Api_url + "/api.php/"+ BASE_HOST +"/vod/?ac=list&class="+ type.toLowerCase();
            typename = getIntent().getStringExtra("TYPENAME");
        }
    }

    /**
     * 初始化
     */
    private void initView() {
        try {
            mQueue = Volley.newRequestQueue(context, new com.shenma.tvlauncher.network.ExtHttpStack());
        } catch (Exception e) {
        mQueue = Volley.newRequestQueue(context, new ExtHttpStack());
        }
        findViewById();
        loadViewLayout();
        setListener();
        processLogic("");
        gHeight = gv_type_details_grid.getHeight();
        //Logger.i("VodTypeActivity", "gHeight=" + gHeight);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        iv_type_details_type.setText(typename);
        getFilterDataFromServer();
        getVodcategory();
    }

    /*取类目公告*/
    private void getVodcategory() {
        try {
            mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        } catch (Exception e) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        }
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=category_notice",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        VodcategoryResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
//                VodGongGaoError(error);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VodTypeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*获取成功*/
    public void VodcategoryResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "RequestResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            String msg = jSONObject.optString("msg");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    type_details_text.setText(URLDecoder.decode(Rc4.decry_RC4(msg,RC4KEY), "UTF-8"));
                } else if (miType == 2) {
                    type_details_text.setText(URLDecoder.decode(Rsa.decrypt_Rsa(msg,RSAKEY), "UTF-8"));
                } else if (miType == 3) {
                    type_details_text.setText(URLDecoder.decode(AES.decrypt_Aes(AESKEY,msg, AESIV), "UTF-8"));
                }
                type_details_text.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化菜单数据
     */
    private void initMenuData() {
    }

    /*按ID查找视图*/
    protected void findViewById() {
        iv_type_details_type = (TextView) findViewById(R.id.type_details_type);
        type_details_text = (TextView) findViewById(R.id.type_details_text);
        tv_type_details_sum = (TextView) findViewById(R.id.type_details_sum);
        gv_type_details_grid = (GridView) findViewById(R.id.type_details_grid);
        gv_type_details_grid.setSelector(new ColorDrawable(0));
        filter_type_container = (LinearLayout) findViewById(R.id.filter_type_container);
        filter_year_container = (LinearLayout) findViewById(R.id.filter_year_container);
        filter_area_container = (LinearLayout) findViewById(R.id.filter_area_container);
        filter_sort_container = (LinearLayout) findViewById(R.id.filter_sort_container);
    }

    /*键盘点击事件*/
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 此方法已不再使用，筛选选项改为在头部直接展示
    }
    
    /**
     * 创建筛选选项TextView
     */
    private TextView createFilterTextView(String text, final int category, final int index) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        // 设置左右间距为8dp，增大间距
            int marginPx = (int) (14 * getResources().getDisplayMetrics().density);
            params.setMargins(marginPx, 0, marginPx, 0);
        textView.setLayoutParams(params);
        textView.setText(text);
        try {
            int textColor;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                textColor = getResources().getColor(R.color.white, null);
            } else {
                textColor = getResources().getColor(R.color.white);
            }
            textView.setTextColor(textColor);
        } catch (Exception e) {
            textView.setTextColor(0xFFFFFFFF);
        }
        // 使用更大的字体大小
        textView.setTextSize(getResources().getDimensionPixelSize(R.dimen.sm_24) / getResources().getDisplayMetrics().scaledDensity);
        textView.setGravity(android.view.Gravity.CENTER);
        // 设置padding左右各6dp，给文字更多空间
        int paddingHorizontal = (int) (6 * getResources().getDisplayMetrics().density);
        int paddingVertical = (int) (3 * getResources().getDisplayMetrics().density);
        textView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
        // 移除字体内边距，让文字更紧凑
        textView.setIncludeFontPadding(false);
        // 设置行高为单行
        textView.setSingleLine(true);
        // 设置ellipsize，避免文字过长
        textView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        // 计算固定高度，确保选中和未选中时高度一致
        int textHeight = (int) (textView.getTextSize() + paddingVertical * 2);
        // 设置最小和最大高度相同，确保点击时高度不变
        textView.setMinHeight(textHeight);
        textView.setMaxHeight(textHeight);
        // 设置最小宽度为0，让宽度自适应文字
        textView.setMinWidth(0);
        textView.setBackgroundResource(R.drawable.filter_bg_selector_rounded);
        textView.setFocusable(true);
        textView.setClickable(true);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                handleFilterClick(category, index);
            }
        });
        textView.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // 保存当前尺寸，确保焦点变化时尺寸不变
                int currentWidth = v.getWidth();
                int currentHeight = v.getHeight();
                if (hasFocus) {
                    v.setBackgroundResource(R.drawable.filter_focus_rounded);
                } else {
                    // 检查是否是选中项（index为-1表示"全部"）
                    boolean isSelected = false;
                    if (category == 0 && selected_type_index == index) isSelected = true;
                    else if (category == 1 && selected_year_index == index) isSelected = true;
                    else if (category == 2 && selected_area_index == index) isSelected = true;
                    else if (category == 3 && selected_sort_index == index) isSelected = true;
                    v.setBackgroundResource(isSelected ? R.drawable.filter_sleted_rounded_compact : R.drawable.filter_bg_selector_rounded);
                }
                // 强制保持尺寸不变，避免点击时间距变大
                if (currentWidth > 0 && currentHeight > 0 && v.getLayoutParams() != null) {
                    v.getLayoutParams().width = currentWidth;
                    v.getLayoutParams().height = currentHeight;
                    v.requestLayout();
                }
            }
        });
        return textView;
    }
    
    /**
     * 对齐所有筛选选项，确保每列对齐
     * 每行的第一个选项对齐第一个，第二个对齐第二个，以此类推
     */
    private void alignFilterOptions() {
        // 获取所有容器的最大子视图数量
        int maxCount = 0;
        if (filter_type_container != null) {
            maxCount = Math.max(maxCount, filter_type_container.getChildCount());
        }
        if (filter_year_container != null) {
            maxCount = Math.max(maxCount, filter_year_container.getChildCount());
        }
        if (filter_area_container != null) {
            maxCount = Math.max(maxCount, filter_area_container.getChildCount());
        }
        if (filter_sort_container != null) {
            maxCount = Math.max(maxCount, filter_sort_container.getChildCount());
        }
        
        if (maxCount == 0) {
            return;
        }
        
        // 为每个位置计算最大宽度
        int[] maxWidths = new int[maxCount];
        
        // 遍历所有容器，找到每个位置的最大宽度
        LinearLayout[] containers = {filter_type_container, filter_year_container, filter_area_container, filter_sort_container};
        for (LinearLayout container : containers) {
            if (container != null) {
                for (int i = 0; i < container.getChildCount() && i < maxCount; i++) {
                    View child = container.getChildAt(i);
                    if (child != null) {
                        // 测量子视图的宽度
                        child.measure(
                            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED),
                            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
                        );
                        int width = child.getMeasuredWidth();
                        maxWidths[i] = Math.max(maxWidths[i], width);
        }
                }
            }
        }
        
        // 统一设置每个位置的宽度
        for (LinearLayout container : containers) {
            if (container != null) {
                for (int i = 0; i < container.getChildCount() && i < maxCount; i++) {
                    View child = container.getChildAt(i);
                    if (child != null && child.getLayoutParams() != null) {
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) child.getLayoutParams();
                        params.width = maxWidths[i];
                        child.setLayoutParams(params);
                    }
                }
            }
        }
    }
    
    /**
     * 处理筛选选项点击
     */
    private void handleFilterClick(int category, int index) {
        // index为-1表示"全部"选项
        if (category == 0) {
            // 类型
            selected_type_index = index;
            updateFilterViews(0);
        } else if (category == 1) {
            // 年份
            selected_year_index = index;
            updateFilterViews(1);
        } else if (category == 2) {
            // 地区
            selected_area_index = index;
            updateFilterViews(2);
        } else if (category == 3) {
            // 排序
            selected_sort_index = index;
            updateFilterViews(3);
        }
            setFilterString();
    }
    
    /**
     * 更新筛选选项视图状态
     */
    private void updateFilterViews(int category) {
        if (category == 0 && filter_type_container != null) {
            for (int i = 0; i < filter_type_container.getChildCount(); i++) {
                View child = filter_type_container.getChildAt(i);
                // 第0个是"全部"选项（index -1），第1个开始是数据项（index 0, 1, 2...）
                int dataIndex = i == 0 ? -1 : i - 1;
                child.setBackgroundResource(selected_type_index == dataIndex ? R.drawable.filter_sleted_rounded_compact : R.drawable.filter_bg_selector_rounded);
            }
        } else if (category == 1 && filter_year_container != null) {
            for (int i = 0; i < filter_year_container.getChildCount(); i++) {
                View child = filter_year_container.getChildAt(i);
                int dataIndex = i == 0 ? -1 : i - 1;
                child.setBackgroundResource(selected_year_index == dataIndex ? R.drawable.filter_sleted_rounded_compact : R.drawable.filter_bg_selector_rounded);
            }
        } else if (category == 2 && filter_area_container != null) {
            for (int i = 0; i < filter_area_container.getChildCount(); i++) {
                View child = filter_area_container.getChildAt(i);
                int dataIndex = i == 0 ? -1 : i - 1;
                child.setBackgroundResource(selected_area_index == dataIndex ? R.drawable.filter_sleted_rounded_compact : R.drawable.filter_bg_selector_rounded);
            }
        } else if (category == 3 && filter_sort_container != null) {
            for (int i = 0; i < filter_sort_container.getChildCount(); i++) {
                View child = filter_sort_container.getChildAt(i);
                int dataIndex = i == 0 ? -1 : i - 1;
                child.setBackgroundResource(selected_sort_index == dataIndex ? R.drawable.filter_sleted_rounded_compact : R.drawable.filter_bg_selector_rounded);
            }
        }
    }

    /*加载视图布局*/
    protected void loadViewLayout() {
    }

    /**
     * 获取视频筛选信息
     */
    protected void getFilterDataFromServer() {
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Api_url", ""),Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "BASE_HOST", ""),Constant.d);
        RequestVo vo = new RequestVo();
        vo.context = context;
        vo.requestUrl = Api_url + "/api.php/"+ BASE_HOST + "/vod/?&ac=flitter" + "&class=" + type.toLowerCase();
        vo.type = VOD_FILTER;
        getDataFromServer(vo);
    }

    /*筛选栏*/
    private void setFilterString() {
        filterString = "";
        vodDatas = null;
        pageindex = 1;
        // index为-1表示"全部"，不添加筛选条件
        if (selected_sort_index >= 0 && selected_sort_index < sort.length) {
            filterString = new StringBuilder(String.valueOf(filterString)).append("&sort=") + sort[selected_sort_index];
        }
        if (selected_area_index >= 0 && areas != null && selected_area_index < areas.size()) {
            filterString = new StringBuilder(String.valueOf(filterString)).append("&area=").append(Utils.getEcodString(areas.get(selected_area_index))).toString();
        }
        if (selected_type_index >= 0 && types != null && selected_type_index < types.size()) {
            filterString = new StringBuilder(String.valueOf(filterString)).append("&type=").append(Utils.getEcodString(types.get(selected_type_index))).toString();
        }
        if (selected_year_index >= 0 && years != null && selected_year_index < years.size()) {
            filterString = new StringBuilder(String.valueOf(filterString)).append("&year=").append(Utils.getEcodString(years.get(selected_year_index))).toString();
        }
        processLogic(filterString);
    }

    /*清除过滤器*/
    private void clearFilter() {
        // 设置为-1表示选中"全部"
        selected_sort_index = -1;
        selected_area_index = -1;
        selected_type_index = -1;
        selected_year_index = -1;
        updateFilterViews(0);
        updateFilterViews(1);
        updateFilterViews(2);
        updateFilterViews(3);
        vodDatas = null;
        pageindex = 1;
        filterString = "";//清除搜索
        processLogic("");
    }

    /**
     * 获取视频列表
     */
    protected void processLogic(String filter) {
        RequestVo vo = new RequestVo();
        vo.context = context;
        vo.type = VOD_DATA;
        vo.requestUrl = VOD_TYPE + "&page=" + pageindex + filter;
        Log.d("joychang", "vo.requestUrl=" + vo.requestUrl);
        start = System.currentTimeMillis();
        getDataFromServer(vo);
    }

    /**
     * 从服务器上获取数据，并回调处理
     *
     * @param reqVo
     */
    protected void getDataFromServer(RequestVo reqVo) {
        showProgressDialog();
        if (Utils.hasNetwork(context)) {
            if (reqVo.type == VOD_DATA) {
                GsonRequest<VodTypeInfo> mVodData = new GsonRequest<VodTypeInfo>(Method.POST, reqVo.requestUrl,
                        VodTypeInfo.class, createVodDataSuccessListener(), createVodDataErrorListener()){
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
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VodTypeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                        return headers;
                    }
                };
                mQueue.add(mVodData);     //     执行
            } else if (reqVo.type == VOD_FILTER) {
                GsonRequest<VodFilter> mVodData = new GsonRequest<VodFilter>(Method.POST, reqVo.requestUrl,
                        VodFilter.class, createVodFilterSuccessListener(), createVodFilterErrorListener()){
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
                        return params;
                    }
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VodTypeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                        return headers;
                    }
                };
                mQueue.add(mVodData);     //     执行
            }
        } else {}
    }

    //数据筛选请求成功
    private Listener<VodFilter> createVodFilterSuccessListener() {
        return new Listener<VodFilter>() {
            public void onResponse(VodFilter response) {
                String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(context, "Api_url", ""),Constant.d);
                String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(context, "BASE_HOST", ""),Constant.d);
                if (response != null) {
                    vodFilter = response.getFlitter();
                    ArrayList<String> seachs = new ArrayList<String>();
                    seachs.add("搜索");
                    seachs.add("清空筛选");
                    ArrayList<String> sorts = new ArrayList<String>();
                    sorts.add("热度优先");
                    sorts.add("评分最高");
                    sorts.add("最近更新");
                    {
                        String name;
                        if (vodFilter.size() > 0) {
                            name = vodFilter.get(0).getField();
                            if (name.equals("type")) {
                                types = Arrays.asList(vodFilter.get(0).getValues());
                            } else if (name.equals("year")) {
                                years = Arrays.asList(vodFilter.get(0).getValues());
                            } else if (name.equals("area")) {
                                areas = Arrays.asList(vodFilter.get(0).getValues());
                            }
                        }
                        if (vodFilter.size() > 1) {
                            name = vodFilter.get(1).getField();
                            if (name.equals("type")) {
                                types = Arrays.asList(vodFilter.get(1).getValues());
                            } else if (name.equals("year")) {
                                years = Arrays.asList(vodFilter.get(1).getValues());
                            } else if (name.equals("area")) {
                                areas = Arrays.asList(vodFilter.get(1).getValues());
                            }
                        }
                        if (vodFilter.size() > 2) {
                            name = vodFilter.get(2).getField();
                            if (name.equals("type")) {
                                types = Arrays.asList(vodFilter.get(2).getValues());
                            } else if (name.equals("year")) {
                                years = Arrays.asList(vodFilter.get(2).getValues());
                            } else if (name.equals("area")) {
                                areas = Arrays.asList(vodFilter.get(2).getValues());
                            }
                        }
                    }

                    // 动态创建筛选选项，每个分类前面添加"全部"选项
                    if (types != null && types.size() > 0 && filter_type_container != null) {
                        filter_type_container.removeAllViews();
                        // 先添加"全部"选项，index为-1
                        TextView allTextView = createFilterTextView("全部", 0, -1);
                        filter_type_container.addView(allTextView);
                        // 再添加其他选项
                        for (int i = 0; i < types.size(); i++) {
                            TextView textView = createFilterTextView(types.get(i), 0, i);
                            filter_type_container.addView(textView);
                        }
                    }
                    if (years != null && years.size() > 0 && filter_year_container != null) {
                        filter_year_container.removeAllViews();
                        // 先添加"全部"选项，index为-1
                        TextView allTextView = createFilterTextView("全部", 1, -1);
                        filter_year_container.addView(allTextView);
                        // 再添加其他选项
                        for (int i = 0; i < years.size(); i++) {
                            TextView textView = createFilterTextView(years.get(i), 1, i);
                            filter_year_container.addView(textView);
                        }
                    }
                    if (areas != null && areas.size() > 0 && filter_area_container != null) {
                        filter_area_container.removeAllViews();
                        // 先添加"全部"选项，index为-1
                        TextView allTextView = createFilterTextView("全部", 2, -1);
                        filter_area_container.addView(allTextView);
                        // 再添加其他选项
                        for (int i = 0; i < areas.size(); i++) {
                            TextView textView = createFilterTextView(areas.get(i), 2, i);
                            filter_area_container.addView(textView);
                        }
                    }
                    if (sorts != null && sorts.size() > 0 && filter_sort_container != null) {
                        filter_sort_container.removeAllViews();
                        // 先添加"全部"选项，index为-1
                        TextView allTextView = createFilterTextView("全部", 3, -1);
                        filter_sort_container.addView(allTextView);
                        // 再添加其他选项
                        for (int i = 0; i < sorts.size(); i++) {
                            TextView textView = createFilterTextView(sorts.get(i), 3, i);
                            filter_sort_container.addView(textView);
                        }
                    }
                    // 对齐所有筛选选项，确保每列对齐
                    alignFilterOptions();
                    
                    // 更新视图状态，确保默认选中"全部"
                    updateFilterViews(0);
                    updateFilterViews(1);
                    updateFilterViews(2);
                    updateFilterViews(3);
                }
            }
        };
    }

    private ErrorListener createVodFilterErrorListener() {
        return new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
            }
        };
    }

    //影视数据请求成功
    private Listener<VodTypeInfo> createVodDataSuccessListener() {
        return new Listener<VodTypeInfo>() {
            public void onResponse(VodTypeInfo response) {
                closeProgressDialog();
                if (response != null) {
                    //Log.v("joychang", "Vod用时==" + (System.currentTimeMillis() - start));
                    //Logger.v("joychang", "获取数据成功!pageindex=" + pageindex);
                    if (vodDatas == null || vodDatas.size() <= 0) {
                        vodpageindex = 1;
                        vodtypeinfo = response;
                        //Logger.v("joychang", "vodtypeinfo" + vodtypeinfo.getPageindex() + "...." + vodtypeinfo.getVideonum());
                        tv_type_details_sum.setText("共" + vodtypeinfo.getVideonum() + "部");

                        int EpisodesNumber = SharePreferenceDataUtil.getSharedIntData(context, "EpisodesNumber", 0);
                        mediaHandler.sendEmptyMessage(EpisodesNumber == 1 ? 4 : 5);

                        totalpage = vodtypeinfo.getTotalpage();
                        ArrayList<VodDataInfo> vodDatalist = (ArrayList<VodDataInfo>) response
                                .getData();
                        if (vodDatalist == null || vodDatalist.size() <= 0) {
//                            pageindex = 2;
                            /*类目下空数据不闪退*/
//                            VodtypeAdapter.vodDatas.clear();
//                            vodtypeAdapter.notifyDataSetChanged();
//                            return;
                        }
                        vodDatas = vodDatalist;
                        vodtypeAdapter = new VodtypeAdapter(context, vodDatas, imageLoader);
                        gv_type_details_grid.setAdapter(vodtypeAdapter);
                        return;
                    }
                    vodtypeinfo = response;
                    ArrayList<VodDataInfo> vodDatalist = (ArrayList<VodDataInfo>) response
                            .getData();
                    if (vodDatalist != null && vodDatalist.size() > 0) {
                        vodDatas.addAll(vodDatalist);
                        VodTypeActivity vodTypeActivity = VodTypeActivity.this;
                        VodTypeActivity vodTypeActivity2 = VodTypeActivity.this;
                        int access$20 = vodTypeActivity2.vodpageindex + 1;
                        vodTypeActivity2.vodpageindex = access$20;
                        vodTypeActivity.vodpageindex = access$20;
                        vodtypeAdapter.changData(vodDatas);
                        return;
                    }
                    return;
                }
                if (vodDatas == null || vodDatas.size() <= 0) {
                    vodDatas = new ArrayList<VodDataInfo>();
                    vodtypeAdapter = new VodtypeAdapter(context, vodDatas, imageLoader);
                    gv_type_details_grid.setAdapter(vodtypeAdapter);
                    pageindex = 0;
                } else {
                    pageindex = vodpageindex;
                }
                //Logger.v("joychang", "获取数据失败!dataCallBack...pageindex=" + pageindex);
            }
        };
    }

    //影视数据请求失败
    private ErrorListener createVodDataErrorListener() {
        return new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    //Logger.e("joychang", "请求超时");
                    Utils.showToast(context, getString(R.string.str_data_loading_error), (int) R.drawable.toast_err);
                    if (vodDatas == null || vodDatas.size() <= 0) {
                        pageindex = 0;
                    } else {
                        pageindex = vodpageindex;
                    }
                } else if (error instanceof ParseError) {
                    tv_type_details_sum.setText("共0部");
                    pageindex = 2;
                    /*类目下空数据不闪退*/
//                    VodtypeAdapter.vodDatas.clear();
//                    vodtypeAdapter.notifyDataSetChanged();
                    Utils.showToast(context, R.string.No_Content, (int) R.drawable.toast_err);
                    //Logger.e("joychang", "ParseError=" + error.toString());
                } else if (error instanceof AuthFailureError) {
                    //Logger.e("joychang", "AuthFailureError=" + error.toString());
                }
                closeProgressDialog();
            }
        };
    }

    /*集合列表*/
    protected void setListener() {
        // 筛选选项的点击事件已在createFilterTextView中设置
        gv_type_details_grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // 防止重复点击
                if (isNavigating) {
                    return;
                }
                String username = sp.getString("userName", null);
                if (username != null) {
                    GetMotion(position);
                } else if (username == null) {
                    Utils.showToast(context, R.string.Please_log_in_to_your_account_first , R.drawable.toast_err);
                    startActivity(new Intent(VodTypeActivity.this, UserActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
        gv_type_details_grid.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int i = totalItemCount - visibleItemCount;
                //Logger.v("joychang", "<<<firstVisibleItem=" + firstVisibleItem + ".....i=" + i);
                if (i != 0 && firstVisibleItem >= i) {
                    pageDown();
                }
            }
        });
        gv_type_details_grid.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /**
     * 向下翻页
     */
    private void pageDown() {
        //Logger.v("joychang", "pageindex=" + pageindex + "....vodpageindex=" + vodpageindex);
        if (pageindex < totalpage && pageindex <= vodpageindex) {
            pageindex++;
            //Logger.v("joychang", "请求页数===" + pageindex);
            if (filterString == null){
                processLogic("");
            }else {
                processLogic(filterString);
            }
//            processLogic("");
        }
    }

    /*按下键时*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Logger.i(TAG, "KeyEvent.KEYCODE_BACK");
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WindowMessageID {
        public static final int RECOMMEND_EXPIRE = 2;
        public static final int RECOMMEND_OFFSITE = 3;
        public static final int RESPONSE_NO_SUCCESS = 1;

        private WindowMessageID() {
        }
    }

    //显示提示框
    protected void showProgressDialog() {
        Utils.loadingShow_tv(this, R.string.str_data_loading);
    }

    //关闭提示框
    protected void closeProgressDialog() {
        Utils.loadingClose_Tv();
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
                try {
            mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        } catch (Exception e) {
                mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        }
                String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
                final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
                final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
                final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
                final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
                final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
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
                        headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(VodTypeActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
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
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "GetMotionResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
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
                mediaHandler.sendEmptyMessage(6);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 114) {/*账户封禁*/
                mediaHandler.sendEmptyMessage(7);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 125) {/*账户信息错误*/
                mediaHandler.sendEmptyMessage(8);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 127) {/*账户信息失效*/
                mediaHandler.sendEmptyMessage(9);
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                return;
            }else if (code == 201){/*201心跳失败*/
                mediaHandler.sendEmptyMessage(10);
                return;
            }else if (code == 106){/*201心跳失败*/
                mediaHandler.sendEmptyMessage(10);
                return;
            }
            if (vipstate == 1 || trystate == 1) {
                // 防止重复跳转
                if (isNavigating) {
                    return;
                }
                isNavigating = true;
                Intent intent = new Intent(VodTypeActivity.this, VideoDetailsActivity.class);
                intent.putExtra("vodtype", type);
                intent.putExtra("vodstate", ((VodDataInfo) vodDatas.get(position)).getState());
                intent.putExtra("nextlink", ((VodDataInfo) vodDatas.get(position)).getNextlink());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else{
                mediaHandler.sendEmptyMessage(2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
