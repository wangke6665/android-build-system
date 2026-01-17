package com.shenma.tvlauncher.vod;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.BaseActivity;
import com.shenma.tvlauncher.vod.VideoDetailsActivity;
import com.shenma.tvlauncher.EmpowerActivity;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.SettingActActvity;
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
import com.shenma.tvlauncher.view.WiFiDialog;
import com.shenma.tvlauncher.vod.adapter.SearchTypeAdapter;
import com.shenma.tvlauncher.vod.domain.RequestVo;
import com.shenma.tvlauncher.vod.domain.VodDataInfo;
import com.shenma.tvlauncher.vod.domain.VodTypeInfo;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

/**
 * @author joychang
 * @Description 搜索
 */
public class SearchActivity extends Activity implements OnClickListener {
    private final String TAG = "SearchActivity";
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    protected SharedPreferences sp;
    public static SharedPreferences Sp;
    private TextView SearchText;
    private String VOD_URL = null;
    private Context context;
    private GridView gv_search_result;
    private RequestQueue mQueue;
    private int pageindex = 1;
    private StringBuilder sb;
//    private LinearLayout search_keybord_full_layout;
    private EditText search_keybord_input;
    private SearchTypeAdapter searchtypeAdapter;
    private int totalpage;
    private TextView tv_search;
    private TextView tv_search_empty_text;
    private String type = null;
    private String name = null;
    private ArrayList<VodDataInfo> vodDatas;
    private int vodpageindex;
    private VodTypeInfo vodtypeinfo;
    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(context, "糟糕,请求没成功!", R.drawable.toast_err);
                    return;
                case 2:
                    Utils.showToast("该账户已过期!",context, R.drawable.toast_err);
                    startActivity(new Intent(context, EmpowerActivity.class));
                    return;

                case 6:
                    Utils.showToast("该账户已在其它设备登录!",context, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 7:
                    Utils.showToast("账户已被禁用!",context, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 8:
                    Utils.showToast("账户信息错误!",context, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 9:
                    Utils.showToast("账户信息已失效!",context, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 10:
                    Utils.showToast("服务器开了小差,请在试一次!",context, R.drawable.toast_shut);
                    return;
                case 11:
                    Bitmap empower = Utils.createQRCodeBitmap("http://" + Utils.localIPv4get() + ":" + sp.getInt("port", 9978), 200, 200,"UTF-8","H", "1", Color.BLACK, Color.WHITE);
                    users_empower.setImageBitmap(empower);
                    return;
                default:
                    return;
            }
        }
    };
    private int vipstate;
    private int trystate = SharePreferenceDataUtil.getSharedIntData(this, "Trystate", 0);
//    private static final int PAGESIZE = 20;
//    private static final int REFRESH_ADAPTER = 1;
//    private String author = "%e4%bd%9c%e8%80%85%51%51%ef%bc%9a%31%32%31%39%31%33%38%30%32%32";
//    private int lastIndex = -1;
//    private LinearLayout menulayout;
//    private long start;
//    private TextView tv_filter_year;
//    private TextView tv_type_details_sum;
//    private List<String> types;
//    private List<VodFilterInfo> vodFilter;
//    private VodtypeAdapter vodtypeAdapter;
//    private List<String> years;
//    private String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
//    private String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
    private String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Api_url", ""), Constant.d);
    private String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "BASE_HOST", ""),Constant.d);
    private Dialog mDialog;
    private ImageView users_empower;
//    private String Voice_text = "第一行\n第二行";
    private String Voice_text;


    /*创建时的回调函数*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mv_search_new);
        context = this;
        initIntent();
        initView();
        sp = getSharedPreferences("shenma", 0);
        Sp = getSharedPreferences("initData", MODE_PRIVATE);
    }


    /*停止时*/
    protected void onStop() {
        super.onStop();
        // //发送广播
        // Intent localIntent = new Intent("com.example.MY_ACTION_RESUME_VIDEO");
        // LocalBroadcastManager.getInstance(SearchActivity.this).sendBroadcast(localIntent);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /*初始化视图*/
    private void initView() {
        findViewById();
        loadViewLayout();
        setListener();
        SearchText();
        getVoice();
    }



    /*请求搜索文字*/
    private void SearchText() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Api_url + "/api.php/" + BASE_HOST +"/SearchText",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        SearchTextResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                //Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("time", GetTimeStamp.timeStamp());
                params.put("key", encry_RC4_string(GetTimeStamp.timeStamp(),GetTimeStamp.timeStamp()));
                params.put("os",  Integer.toString(android.os.Build.VERSION.SDK_INT));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SearchActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*搜索文字响应*/
    public void SearchTextResponse(String response) {
        //Log.i(TAG, "SearchTextResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            if (code == 200){
                String msg = jSONObject.optString("msg");/*消息*/
                SearchText.setText(msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*按ID查找视图*/
    private void findViewById() {
        sb = new StringBuilder();
        SearchText = (TextView) findViewById(R.id.SearchText);//搜索文字
        search_keybord_input = (EditText) findViewById(R.id.search_keybord_input);
        tv_search = (TextView) findViewById(R.id.search_keybord_hint);
        tv_search_empty_text = (TextView) findViewById(R.id.search_empty_text);
//        search_keybord_full_layout = (LinearLayout) findViewById(R.id.search_keybord_full_layout);
        View ScrollView = findViewById(R.id.search_keybord_full_layout);
        gv_search_result = (GridView) findViewById(R.id.search_result);
        gv_search_result.setSelector(new ColorDrawable(Color.TRANSPARENT));
        /*获取焦点*/
//        ScrollView.requestFocus();
        EditText phoneEd= (EditText) findViewById(R.id.search_keybord_input);
        /*搜索框监听*/
        phoneEd.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                // TODO Auto-generated method stub
                if(arg1 == EditorInfo.IME_ACTION_SEARCH)
                {
                    type = "MOVIE";
                    Editable text = search_keybord_input.getText();
                    if (null != text && text.length() > 0) {
                        String search = text.toString();
                        sb = new StringBuilder();
                        sb.append(search);
                    }
                    readyToSearch();
                }
                return false;
            }
        });
    }

    /*加载视图布局*/
    private void loadViewLayout() {
    }

    /*设置侦听器*/
    private void setListener() {
        gv_search_result.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.d("SearchActivity", "onItemClick: position=" + position);
                String username = sp.getString("userName", null);
                Log.d("SearchActivity", "username=" + username);
                if (username != null) {
                    GetMotion(position);
                } else if (username == null) {
                    Utils.showToast("请先登录账号!",context, R.drawable.toast_err);
                    startActivity(new Intent(SearchActivity.this, UserActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
        gv_search_result.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int i = totalItemCount - visibleItemCount;
                if (firstVisibleItem < i) {
                    Logger.v("joychang", "<<<firstVisibleItem=" + firstVisibleItem + ".....i=" + i);
                } else {
                    /*分页加载数据*/
                    pageDown();
                }
            }
        });


        gv_search_result.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    /*初始化意图*/
    private void initIntent() {
        //VOD_TYPE = intent.getStringExtra("VOD_TYPE");
        type = getIntent().getStringExtra("TYPE");

        name = getIntent().getStringExtra("NAME");
        if (name != null && !name.equals("")) {
            int lastColonIndex = Utils.findLastIndexOfSeparators(name, '&', ':', '：', ';', '；');
            if (lastColonIndex != -1) {
                name = name.substring(0, lastColonIndex);
            }
            readyToSearch(name);
        }
    }

    /*单击*/
    public void onClick(View v) {
    }

    /*键盘点击*/
    public void doClick(View target) {
        int tag = target.getId();
        //清空
        if (tag == R.id.search_keybord_full_clear) {
            sb = new StringBuilder();
            readyToSearch();
            //删除
        } else if (tag == R.id.search_keybord_full_del) {
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            readyToSearch();
            //搜片
        } else if (tag == R.id.search_keybord_sp) {
            type = "MOVIE";
            Editable text = search_keybord_input.getText();
            if (null != text && text.length() > 0) {
                String search = text.toString();
                sb = new StringBuilder();
                sb.append(search);
            }
            readyToSearch();
        } else if (tag == R.id.search_keybord_sj2||tag == R.id.search_keybord_sj){
            boolean portAvailable = Utils.isPortAvailable(sp.getInt("port", 9978));
            if (portAvailable) {
                Utils.showToast(context, getString(R.string.Not_yet_activated), R.drawable.toast_err);
//                System.out.println("解码模式：硬解 服务未开启" + sp.getInt("port", 9978));
            } else {
//                System.out.println("解码模式：硬解 服务已开启" + sp.getInt("port", 9978));
                showUserDialogempower();
            }
        }else if(tag == R.id.search_keybord_full_voice){
            boolean portAvailable = Utils.isPortAvailable(sp.getInt("port", 9978));
            if (!portAvailable) {
                if (Voice_text != null){
                    showvoice();
                }
            }
        }else {
            sb.append(target.getTag());
            readyToSearch();
        }
    }

    /*准备搜索*/
    private void readyToSearch() {
        String str = sb.toString();
        search_keybord_input.setText(str);
        Logger.v("joychang", "搜索====" + str);
        SearchDatas(str);
    }

    /*准备搜索(引入)*/
    private void readyToSearch(String name) {
        EditText searchInput = (EditText) findViewById(R.id.search_keybord_input);
        if (searchInput != null) {
            searchInput.setText(name);
        }
        SearchDatas(name);
    }

    /**
     * 向下翻页
     */
    private void pageDown() {
        Logger.v("joychang", "pageindex=" + pageindex + "....vodpageindex="
                + vodpageindex);
        if (pageindex >= totalpage || pageindex > vodpageindex)
            return;
        pageindex = pageindex + 1;
        Logger.v("joychang", "请求页数===" + pageindex);
        processLogic();
    }

    /**
     * 获取视频列表
     */
    protected void processLogic() {
        RequestVo vo = new RequestVo();
        vo.context = context;
        vo.requestUrl = VOD_URL + "&page=" + pageindex;
        Logger.v("joychang", "访问:::" + VOD_URL);
        getDataFromServer(vo);
    }

    /**
     * 搜索视频
     */
    protected void SearchDatas(String filter) {
        RequestVo vo = new RequestVo();
        vo.context = context;
        vodDatas = null;
        pageindex = 1;
        VOD_URL = Api_url + "/api.php/" + BASE_HOST + "/vod/?ac=list&zm=" + Utils.getEcodString(filter);
        vo.requestUrl = VOD_URL + "&page=" + pageindex;
        Logger.d("joychang", "搜索VOD_URL=" + VOD_URL);
        getDataFromServer(vo);
    }

    /**
     * 从服务器上获取数据，并回调处理
     *
     * @param reqVo
     * @return call Back
     */
    protected void getDataFromServer(RequestVo reqVo) {
        showProgressDialog();
        mQueue = Volley.newRequestQueue(context, new ExtHttpStack());
        if (Utils.hasNetwork(context)) {
            mQueue.add(new GsonRequest<VodTypeInfo>(Request.Method.POST, reqVo.requestUrl, VodTypeInfo.class, createVodDataSuccessListener(), createVodDataErrorListener()) {
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
                    headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SearchActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                    return headers;
                }
            });
        }
    }

    /*请求数据成功*/
    private Listener<VodTypeInfo> createVodDataSuccessListener() {
        return new Listener<VodTypeInfo>() {
            public void onResponse(VodTypeInfo paramObject) {
                if (paramObject == null || paramObject.getData() == null || paramObject.getData().size() <= 0) {
                    Utils.showToast("没有搜索到相关内容!",context, R.drawable.toast_err);
                    vodDatas = new ArrayList<VodDataInfo>();
                    searchtypeAdapter = new SearchTypeAdapter(context, vodDatas, imageLoader);
                    gv_search_result.setAdapter(searchtypeAdapter);
                    gv_search_result.setVisibility(View.GONE);
                } else if (vodDatas == null || vodDatas.size() <= 0) {
                    gv_search_result.setVisibility(View.VISIBLE);
                    vodpageindex = 1;
                    vodtypeinfo = paramObject;
                    Logger.v("joychang", "vodtypeinfo" + vodtypeinfo.getPageindex() + "...." + vodtypeinfo.getVideonum());
                    totalpage = vodtypeinfo.getTotalpage();
                    ArrayList<VodDataInfo> vodDatalist = (ArrayList<VodDataInfo>) paramObject
                            .getData();
                    if (vodDatalist != null && vodDatalist.size() > 0) {
                        vodDatas = vodDatalist;
                        searchtypeAdapter = new SearchTypeAdapter(context, vodDatas, imageLoader);
                        gv_search_result.setAdapter(searchtypeAdapter);
                    }
                } else {
                    vodtypeinfo = paramObject;
                    ArrayList<VodDataInfo> vodDatalist = (ArrayList<VodDataInfo>) paramObject
                            .getData();
                    if (vodDatalist != null && vodDatalist.size() > 0) {
                        vodDatas.addAll(vodDatalist);
                        vodpageindex = vodDatas.size() / 20;
                        searchtypeAdapter.changData(vodDatas);
                        //System.out.println(vodDatalist);
                    }
                }
                closeProgressDialog();
            }
        };
    }

    /*据请求失败*/
    private ErrorListener createVodDataErrorListener() {
        return new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Logger.e("joychang", "请求超时");
//                    Toast.makeText(context, getString(R.string.str_data_loading_error), Toast.LENGTH_SHORT).show();
                    Utils.showToast(context, getString(R.string.str_data_loading_error) , R.drawable.toast_err);
                    if (vodDatas == null || vodDatas.size() <= 0) {
                        pageindex = 0;
                    } else {
                        vodpageindex = vodDatas.size() / 20;
                        pageindex = vodpageindex;
                    }
                } else if (error instanceof ParseError) {
                    pageindex = 2;
                    SearchTypeAdapter.vodDatas.clear();
                    searchtypeAdapter.notifyDataSetChanged();
                    Utils.showToast(context, "亲,没有搜索到相关内容!" , R.drawable.toast_err);
                    Logger.e("joychang", "ParseError=" + error.toString());
                } else if (error instanceof AuthFailureError) {
                    Logger.e("joychang", "AuthFailureError=" + error.toString());
                }
                closeProgressDialog();
            }
        };
    }

    /**
     * 显示提示框
     */
    protected void showProgressDialog() {
        Utils.loadingShow_tv(SearchActivity.this, R.string.str_data_loading);
    }

    /**
     * 关闭提示框
     */
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
                mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
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
                        headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SearchActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
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
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
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

            Log.d("SearchActivity", "vipstate=" + vipstate + ", trystate=" + trystate);
            if (vipstate == 1 || trystate == 1) {
                Log.d("SearchActivity", "跳转到VideoDetailsActivity");
                Intent intent = new Intent(SearchActivity.this, VideoDetailsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if ("ALL".equals(type)) {
                    intent.putExtra("vodtype", vodDatas.get(position).getType());
                } else {
                    intent.putExtra("vodtype", type != null ? type : vodDatas.get(position).getType());
                }
                intent.putExtra("nextlink", vodDatas.get(position).getNextlink());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else{
                Log.d("SearchActivity", "vipstate或trystate不满足，不跳转");
                mediaHandler.sendEmptyMessage(2);
            }

        } catch (Exception e) {
            Log.e("SearchActivity", "GetMotionResponse异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

  /*  private class WindowMessageID {
        public static final int NET_FAILED = 2;
        public static final int SUCCESS = 1;
        private WindowMessageID() {
        }
    }*/


    private void showUserDialogempower() {
        WiFiDialog.Builder builder = new WiFiDialog.Builder(context);
        View mView = View.inflate(context, R.layout.forget_search_empower, null);
        users_empower = (ImageView) mView.findViewById(R.id.users_empower);
        builder.setContentView(mView);
        mediaHandler.sendEmptyMessage(11);
        users_empower.setOnClickListener(new OnClickListener() {
            /*二维码框被单击*/
            public void onClick(View arg0) {
                String url = "http://" + Utils.localIPv4get() + ":" + sp.getInt("port", 9978) + "/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
        mDialog = builder.creates();
        mDialog.show();
    }


    private void showvoice() {
        WiFiDialog.Builder builder = new WiFiDialog.Builder(context);
        View mView = View.inflate(context, R.layout.forget_search_voice, null);
        TextView voice_text = (TextView) mView.findViewById(R.id.voice_text);
        voice_text.setText(Voice_text);
        builder.setContentView(mView);
        mDialog = builder.creates();
        mDialog.show();
    }

    /*获取语音提示文字*/
    private void getVoice() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=voice_text",
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SearchActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*获取成功*/
    public void VodGongGaoResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            String msg = jSONObject.optString("msg");/*状态信息*/
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    Voice_text = URLDecoder.decode(Rc4.decry_RC4(msg,RC4KEY), "UTF-8");
                } else if (miType == 2) {
                    Voice_text = URLDecoder.decode(Rsa.decrypt_Rsa(msg,RSAKEY), "UTF-8");
                } else if (miType == 3) {
                    Voice_text = URLDecoder.decode(AES.decrypt_Aes(AESKEY,msg, AESIV), "UTF-8");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*获取成失败*/
    public void VodGongGaoError(VolleyError volleyError) {
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
}
