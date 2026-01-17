package com.shenma.tvlauncher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.umeng.analytics.MobclickAgent;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * @author joychang
 * @Description 授权中心
 */
@SuppressLint({"SimpleDateFormat"})
public class EmpowerActivity extends BaseActivity {
    private final String TAG = "EmpowerActivity";
    public RequestQueue mQueue;
    private TextView account;/*帐号*/
    private TextView jifen;/*积分*/
    private TextView vipTime;/*VIP时间*/
    private String goods;/*商品列表*/

    private EditText editCode;
    private ImageView empower_iv_pay_ecode;
    private Button sendCode;
    private String viptime;
    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    vipTime.setText(R.string.Lifetime_VIP);
                    payviewlayout.setVisibility(View.GONE);/*电视付款布局隐藏*/
                    kmviewlayout.setVisibility(View.GONE);/*卡密布局隐藏*/
                    return;
                case 2:
                    long time = System.currentTimeMillis() / 1000;
                    long vip = Long.parseLong(sp.getString("vip", ""));
                    if (time > vip){
                        vipTime.setText(R.string.Expired);
                    }else{
                        vipTime.setText(GetTimeStamp.timeStamp2Date(viptime, ""));
                    }
                    return;
                case 3:
                    loadImg(empower_type);
                    return;
                case 4:
                    GoodsList();
                    return;
                case 5:
                    Utils.showToast(EmpowerActivity.this, getString(R.string.fail) + Msg , R.drawable.toast_err);
                    return;
                case 6:
                    if (stoppay == 0){
                        Querypay();
                    }
                    return;
                case 7:
                    Utils.showToast(EmpowerActivity.this, R.string.Payment_successful_recharge_failed, R.drawable.toast_err);
                    return;
                case 8:
                    GetInfo();
                    return;
                default:
                    return;
            }
        }
    };
    private RelativeLayout vip_root_card;/*卡密兑换*/
    private RelativeLayout vip_root_fen;/*积分兑换*/
    private RelativeLayout vip_goods_r_1;/*商品1布局*/
    private RelativeLayout vip_goods_r_2;/*商品2布局*/
    private RelativeLayout vip_goods_r_3;/*商品3布局*/
    private RelativeLayout vip_goods_r_4;/*商品4布局*/
    private TextView vip_goods_1_message;/*商品1描述*/
    private TextView vip_goods_1_title;/*商品1名称*/
    private TextView vip_goods_1_price;/*商品1金额*/
    private TextView vip_goods_gid_1;/*商品1GID*/
    private LinearLayout vip_root_lin_1;/*商品1布局*/
    private TextView vip_goods_2_title;/*商品2名称*/
    private TextView vip_goods_2_price;/*商品2金额*/
    private TextView vip_goods_2_message;/*商品2描述*/
    private TextView vip_goods_gid_2;/*商品2GID*/
    private LinearLayout vip_root_lin_2;/*商品2布局*/
    private TextView vip_goods_3_title;/*商品3名称*/
    private TextView vip_goods_3_price;/*商品3金额*/
    private TextView vip_goods_3_message;/*商品3描述*/
    private TextView vip_goods_gid_3;/*商品3GID*/
    private LinearLayout vip_root_lin_3;/*商品3布局*/
    private TextView vip_goods_4_title;/*商品4名称*/
    private TextView vip_goods_4_price;/*商品4金额*/
    private TextView vip_goods_4_message;/*商品4描述*/
    private TextView vip_goods_gid_4;/*商品1GID*/
    private LinearLayout vip_root_lin_4;/*商品4布局*/
    private LinearLayout payviewlayout;/*电视付款布局*/
    private LinearLayout kmviewlayout;/*卡密布局*/
    private LinearLayout vip_convert;/*积分兑换按钮*/
    private LinearLayout vip_card;/*卡密兑换按钮*/
    private String goodsmoney;/*商品金额*/
    private String goodsname;/*商品名*/
    private String[] Pay_Name;/*支付类型*/
    private String[] Pay_Type;/*支付类型*/
    private AlertDialog alertDialog; /*支付信息框*/
    private ImageView mQrLineView;/*蓝色扫描框*/
    private String empower_url;/*支付二维码地址*/
    private int empower_type = 1;/*二维码类型*/
    private String orders;/*订单号*/
    private String way;/*支付方式*/
    private String Msg;/*错误消息*/
    private int stoppay = 1;/*停止查询支付*/
    private TextView qr_code_text;/*二维码提文字示*/
    private String Qr_code_text;/*二维码提文字示*/
    private String Qr_code_Url;/*二维码地址*/
    private String Long_Qr_code_Url = "";/*永久二维码地址*/
    private TextView empower_pay_text;


    /*创建时的回调函数*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_empower);
        findViewById();
        initView();
        initData();
    }

    /*初始化数据*/
    private void initData() {
        Getgoods();
        GetText();
        GetInfo();
    }

    /*请求商品*/
    private void Getgoods() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=goods",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        GoodsResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(EmpowerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*商品响应*/
    public void GoodsResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "GoodsResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    goods = Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY);
                } else if (miType == 2) {
                    try {
                        goods = Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    goods = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
                }
                mediaHandler.sendEmptyMessage(4);
            }else{
                vip_root_card();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*商品列表*/
    private void GoodsList() {
        try {
            Object object = new JSONTokener(goods).nextValue();
            if (object instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) object;
                int[] vip_goods_gids = {R.id.vip_goods_gid_1, R.id.vip_goods_gid_2, R.id.vip_goods_gid_3, R.id.vip_goods_gid_4};
                int[] vip_goods_titles = {R.id.vip_goods_1_title, R.id.vip_goods_2_title, R.id.vip_goods_3_title, R.id.vip_goods_4_title};
                int[] vip_goods_prices = {R.id.vip_goods_1_price, R.id.vip_goods_2_price, R.id.vip_goods_3_price, R.id.vip_goods_4_price};
                int[] vip_goods_messages = {R.id.vip_goods_1_message, R.id.vip_goods_2_message, R.id.vip_goods_3_message, R.id.vip_goods_4_message};
                int[] vip_root_lins = {R.id.vip_root_lin_1, R.id.vip_root_lin_2, R.id.vip_root_lin_3, R.id.vip_root_lin_4};
                int[] vip_goods_rs = {R.id.vip_goods_r_1, R.id.vip_goods_r_2, R.id.vip_goods_r_3, R.id.vip_goods_r_4};
                int index = 0;
                for (int i = 0; i < jsonArray.length() && index < 4; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.has("gname")) {
                        String gid = jsonObject.getString("gid");
                        String gname = jsonObject.getString("gname");
                        String gmoney = jsonObject.getString("gmoney");
                        String cv = jsonObject.getString("cv");
                        String pay_ali_state = jsonObject.getString("pay_ali_state");
                        String pay_ali_name = jsonObject.getString("pay_ali_name");
                        String pay_ali_type = jsonObject.getString("pay_ali_type");
                        String pay_wx_state = jsonObject.getString("pay_wx_state");
                        String pay_wx_name = jsonObject.getString("pay_wx_name");
                        String pay_wx_type = jsonObject.getString("pay_wx_type");
                        String pay_qq_state = jsonObject.getString("pay_qq_state");
                        String pay_qq_name = jsonObject.getString("pay_qq_name");
                        String pay_qq_type = jsonObject.getString("pay_qq_type");
                        String pay_other_state = jsonObject.getString("pay_other_state");
                        String pay_other_name = jsonObject.getString("pay_other_name");
                        String pay_other_type = jsonObject.getString("pay_other_type");
                        String clock_state = jsonObject.getString("clock_state");
                        String card = jsonObject.getString("vip_card");
                        if (!TextUtils.isEmpty(gname)) {
                            TextView vip_goods_gid = findViewById(vip_goods_gids[index]);
                            TextView vip_goods_title = findViewById(vip_goods_titles[index]);
                            TextView vip_goods_price = findViewById(vip_goods_prices[index]);
                            TextView vip_goods_message = findViewById(vip_goods_messages[index]);
                            LinearLayout vip_root_lin = findViewById(vip_root_lins[index]);
                            RelativeLayout vip_goods_r = findViewById(vip_goods_rs[index]);
                            vip_goods_gid.setText(gid);
                            vip_goods_title.setText(gname);
                            vip_goods_price.setText("￥" + gmoney + "元");
                            vip_goods_message.setText(cv);
                            vip_root_lin.setVisibility(View.VISIBLE);
                            vip_goods_r.setVisibility(View.VISIBLE);
                            index++;
                            List<String> PayList = new ArrayList<>();
                            if (pay_ali_state.equals("y")){
                                PayList.add(pay_ali_name);
                            }
                            if (pay_wx_state.equals("y")){
                                PayList.add(pay_wx_name);
                            }
                            if (pay_qq_state.equals("y")){
                                PayList.add(pay_qq_name);
                            }
                            if (pay_other_state.equals("y")){
                                PayList.add(pay_other_name);
                            }
                            if (!clock_state.equals("0")){
                                vip_convert.setVisibility(View.VISIBLE);
                            }
                            if (card.equals("0")){
                                vip_card.setVisibility(View.GONE);
                            }

                            Pay_Name = PayList.toArray(new String[0]);
                            List<String> PayLists = new ArrayList<>();
                            if (pay_ali_state.equals("y")){
                                PayLists.add(pay_ali_type);
                            }
                            if (pay_wx_state.equals("y")){
                                PayLists.add(pay_wx_type);
                            }
                            if (pay_qq_state.equals("y")){
                                PayLists.add(pay_qq_type);
                            }
                            if (pay_other_state.equals("y")){
                                PayLists.add(pay_other_type);
                            }
                            Pay_Type = PayLists.toArray(new String[0]);
                        }
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*二维码和文字获取扫码提示*/
    private void GetText() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=text_notice",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        TextResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(EmpowerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*扫码提示响应*/
    public void TextResponse(String response) {
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "TextResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            JSONObject msg = null;
            if (miType == 1) {
                msg = new JSONObject(URLDecoder.decode(Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY), "UTF-8"));
            } else if (miType == 2) {
                msg = new JSONObject(URLDecoder.decode(Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY), "UTF-8"));
            } else if (miType == 3) {
                msg = new JSONObject(URLDecoder.decode(AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV), "UTF-8"));
            }
            String content = msg.getString("content");
            String qrCodeUrl = msg.getString("qr_code_url");
            String LongQrCodeUrl = msg.getString("long_qr_code_url");
            String pay_text = msg.getString("pay_text");
            if (code == 200){
                if (!pay_text.equals("null") && !pay_text.equals("")){
                    empower_pay_text.setText(pay_text);
                }
                qr_code_text.setText(content);
                Qr_code_text = content;
                if (!LongQrCodeUrl.equals("")){
                    Long_Qr_code_Url = LongQrCodeUrl;
                }
                if (!qrCodeUrl.equals("")){
                    Qr_code_Url = qrCodeUrl;
                    empower_type = 1;
                }else{
                    way = "icon";
                    empower_type = 0;
                    empower_url = User_url + "/Webpage/?app=" + Api.APPID;
                }
            }
            mediaHandler.sendEmptyMessage(8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*获取失败*/
    public void Error(VolleyError volleyError) {
        //Log.i(TAG, "Error: " + volleyError);
        if (volleyError instanceof TimeoutError) {
            //System.out.println("请求超时");
        }
        if (volleyError instanceof AuthFailureError) {
            //System.out.println("身份验证失败错误");
        }
        if(volleyError instanceof NetworkError) {
            //System.out.println("请检查网络");
        }
        if(volleyError instanceof ServerError) {
            //System.out.println("错误404");
        }

    }

    /*初始化视图*/
    protected void initView() {
        vip_goods_r_1.setVisibility(View.GONE);
        vip_goods_r_2.setVisibility(View.GONE);
        vip_goods_r_3.setVisibility(View.GONE);
        vip_goods_r_4.setVisibility(View.GONE);
        vip_root_lin_1.setVisibility(View.GONE);
        vip_root_lin_2.setVisibility(View.GONE);
        vip_root_lin_3.setVisibility(View.GONE);
        vip_root_lin_4.setVisibility(View.GONE);
        vip_convert.setVisibility(View.GONE);
        vip_card.setVisibility(View.VISIBLE);
        account.setVisibility(View.VISIBLE);
        account.setText(sp.getString("userName", ""));
        jifen.setVisibility(View.VISIBLE);
        jifen.setText(sp.getString("fen", ""));
        vipTime.setVisibility(View.VISIBLE);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = sp.getString("vip", "");
        int vipstate = 0;
        try {
            if (new Date(System.currentTimeMillis()).getTime() < format.parse(GetTimeStamp.timeStamp2Date(time, "")).getTime()) {
                vipstate = 1;
            } else {
                vipstate = 0;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (time.equals("999999999")) {
            vipTime.setText(R.string.Lifetime_VIP);
            qr_code_text.setText("");
        } else if (vipstate == 0) {
            vipTime.setText(R.string.Expired);
        } else if (vipstate == 1) {
            vipTime.setText(GetTimeStamp.timeStamp2Date(time, ""));
        }
    }

    /*加载视图布局*/
    protected void loadViewLayout() {
    }

    /*按ID查找视图*/
    protected void findViewById() {
        findViewById(R.id.empower).setBackgroundResource(R.drawable.video_details_bg);
        mQrLineView = (ImageView) findViewById(R.id.scan_line);
        findViewById(R.id.empower_code);
        sendCode = (Button) findViewById(R.id.empower_btn_sendCode);
        account = (TextView) findViewById(R.id.empower_codestr);
        jifen = (TextView) findViewById(R.id.empower_fen);
        jifen.setVisibility(View.GONE);
        vipTime = (TextView) findViewById(R.id.empower_time);
        editCode = (EditText) findViewById(R.id.empower_search_keybord_input);
        empower_pay_text = (TextView) findViewById(R.id.empower_pay_text);
        empower_iv_pay_ecode = (ImageView) findViewById(R.id.empower_iv_pay_ecode);
        vip_goods_r_1 = (RelativeLayout) findViewById(R.id.vip_goods_r_1);/*设置布局*/
        vip_goods_r_2 = (RelativeLayout) findViewById(R.id.vip_goods_r_2);/*设置布局*/
        vip_goods_r_3 = (RelativeLayout) findViewById(R.id.vip_goods_r_3);/*设置布局*/
        vip_goods_r_4 = (RelativeLayout) findViewById(R.id.vip_goods_r_4);/*设置布局*/
        vip_goods_1_message = (TextView) findViewById(R.id.vip_goods_1_message);/*商品描述*/
        vip_goods_1_title = (TextView) findViewById(R.id.vip_goods_1_title);/*商品名称*/
        vip_goods_1_price = (TextView) findViewById(R.id.vip_goods_1_price);/*商品金额*/
        vip_goods_gid_1 = (TextView) findViewById(R.id.vip_goods_gid_1);/*商品1金额*/
        vip_root_lin_1 = (LinearLayout) findViewById(R.id.vip_root_lin_1);/*设置布局*/
        vip_goods_2_message = (TextView) findViewById(R.id.vip_goods_2_message);/*商品描述*/
        vip_goods_2_title = (TextView) findViewById(R.id.vip_goods_2_title);/*商品名称*/
        vip_goods_2_price = (TextView) findViewById(R.id.vip_goods_2_price);/*商品金额*/
        vip_root_lin_2 = (LinearLayout) findViewById(R.id.vip_root_lin_2);/*设置布局*/
        vip_goods_r_2 = (RelativeLayout) findViewById(R.id.vip_goods_r_2);/*设置布局*/
        vip_goods_gid_2 = (TextView) findViewById(R.id.vip_goods_gid_2);/*商品2金额*/
        vip_goods_3_message = (TextView) findViewById(R.id.vip_goods_3_message);/*商品描述*/
        vip_goods_3_title = (TextView) findViewById(R.id.vip_goods_3_title);/*商品名称*/
        vip_goods_3_price = (TextView) findViewById(R.id.vip_goods_3_price);/*商品金额*/
        vip_root_lin_3 = (LinearLayout) findViewById(R.id.vip_root_lin_3);/*设置布局*/
        vip_goods_r_3 = (RelativeLayout) findViewById(R.id.vip_goods_r_3);/*设置布局*/
        vip_goods_gid_3 = (TextView) findViewById(R.id.vip_goods_gid_3);/*商品3金额*/
        vip_goods_4_message = (TextView) findViewById(R.id.vip_goods_4_message);/*商品描述*/
        vip_goods_4_title = (TextView) findViewById(R.id.vip_goods_4_title);/*商品名称*/
        vip_goods_4_price = (TextView) findViewById(R.id.vip_goods_4_price);/*商品金额*/
        vip_root_lin_4 = (LinearLayout) findViewById(R.id.vip_root_lin_4);/*设置布局*/
        vip_goods_r_4 = (RelativeLayout) findViewById(R.id.vip_goods_r_4);/*设置布局*/
        vip_goods_gid_4 = (TextView) findViewById(R.id.vip_goods_gid_4);/*商品4金额*/
        kmviewlayout = (LinearLayout) findViewById(R.id.kmviewlayout);/*卡密充值布局*/
        payviewlayout = (LinearLayout) findViewById(R.id.payviewlayout);/*电视付款布局*/
        qr_code_text = (TextView) findViewById(R.id.qr_code_text);
        vip_goods_r_1.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_1();
            }
        });
        vip_goods_r_2.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_2();
            }
        });
        vip_goods_r_3.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_3();
            }
        });
        vip_goods_r_4.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_4();
            }
        });
        vip_card = (LinearLayout) findViewById(R.id.vip_card);/*卡密兑换布局*/
        vip_root_card = (RelativeLayout) findViewById(R.id.vip_root_card);/*卡密兑换*/
        vip_root_card.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vip_root_card();
            }
        });
        vip_convert = (LinearLayout) findViewById(R.id.vip_convert);/*积分兑换布局*/
        vip_root_fen = (RelativeLayout) findViewById(R.id.vip_root_fen);/*积分兑换按钮*/
        vip_root_fen.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                vip_root_fen();
            }
        });

        /*卡密充值*/
        sendCode.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String code = editCode.getText().toString();
                String username = sp.getString("userName", null);
                //EmpowerRecharge(username, code);
                getCard(username, code);
            }
        });

    }

    /*跳转到支付页面*/
    private void showPayActivity(String payUrl) {
        Intent intent = new Intent(this, PayActivity.class);
        intent.putExtra(PayActivity.EXTRA_PAY_URL, payUrl);
        startActivity(intent);
    }

    /*商品1被单击*/
    private void vip_goods_r_1(){
        stoppay = 1;
        final String open_ring = vip_goods_gid_1.getText().toString();/*Gid1*/
        goodsmoney =  vip_goods_1_price.getText().toString();/*商品金额*/
        goodsname = vip_goods_1_title.getText().toString();/*商品名称*/
        final String username = sp.getString("userName", null);/*用户名*/
        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        final long timeMillis = System.currentTimeMillis();/*时间戳*/
        final String time = sdfTwo.format(timeMillis);/*系统时间*/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EmpowerActivity.this);
        alertBuilder.setTitle(R.string.Please_select);
        alertBuilder.setItems(Pay_Name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                way = Pay_Type[i];
                Getpay(time + timeMillis, username, way, open_ring,Pay_Name[i]);
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /*商品2被单击*/
    private void vip_goods_r_2(){
        stoppay = 1;
        final String open_ring = vip_goods_gid_2.getText().toString();/*Gid1*/
        goodsmoney =  vip_goods_2_price.getText().toString();/*商品金额*/
        goodsname = vip_goods_2_title.getText().toString();
        final String username = sp.getString("userName", null);/*用户名*/
        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        final long timeMillis = System.currentTimeMillis();/*时间戳*/
        final String time = sdfTwo.format(timeMillis);/*系统时间*/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EmpowerActivity.this);
        alertBuilder.setTitle(R.string.Please_select);
        alertBuilder.setItems(Pay_Name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                way = Pay_Type[i];
                Getpay(time + timeMillis, username, way, open_ring,Pay_Name[i]);
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /*商品3被单击*/
    private void vip_goods_r_3(){
        stoppay = 1;
        final String open_ring = vip_goods_gid_3.getText().toString();/*Gid1*/
        goodsmoney =  vip_goods_3_price.getText().toString();/*商品金额*/
        goodsname = vip_goods_3_title.getText().toString();
        final String username = sp.getString("userName", null);/*用户名*/
        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        final long timeMillis = System.currentTimeMillis();/*时间戳*/
        final String time = sdfTwo.format(timeMillis);/*系统时间*/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EmpowerActivity.this);
        alertBuilder.setTitle(R.string.Please_select);
        alertBuilder.setItems(Pay_Name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                way = Pay_Type[i];
                Getpay(time + timeMillis, username, way, open_ring,Pay_Name[i]);
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /*商品4被单击*/
    private void vip_goods_r_4(){
        stoppay = 1;
        final String open_ring = vip_goods_gid_4.getText().toString();/*Gid1*/
        goodsmoney =  vip_goods_4_price.getText().toString();/*商品金额*/
        goodsname = vip_goods_4_title.getText().toString();
        final String username = sp.getString("userName", null);/*用户名*/
        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        final long timeMillis = System.currentTimeMillis();/*时间戳*/
        final String time = sdfTwo.format(timeMillis);/*系统时间*/
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EmpowerActivity.this);
        alertBuilder.setTitle(R.string.Please_select);
        alertBuilder.setItems(Pay_Name, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                way = Pay_Type[i];
                Getpay(time + timeMillis, username, way, open_ring,Pay_Name[i]);
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    /*卡密被单击*/
    private void vip_root_card(){
        stoppay = 1;
        GetText();
        qr_code_text.setText(Qr_code_text);
        payviewlayout.setVisibility(View.GONE);/*电视付款布局隐藏*/
        kmviewlayout.setVisibility(View.VISIBLE);/*卡密布局可见*/
        findViewById(R.id.box_layout).setBackgroundResource(R.drawable.frame_no);/*删除二维码框*/
    }

    /*积分被单击*/
    private void vip_root_fen(){
        stoppay = 1;
        startActivity(new Intent(EmpowerActivity.this, SettingActActvity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /*发起支付订单*/
    private void Getpay(final String orderrc4, final String userrc4, final String way, final String gidrc4,final String PayType) {
        Utils.loadingShow_tv(context, R.string.loading);/*转圈*/
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app="+ Api.APPID +"&act=pay",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        GetpayResponse(response,PayType);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String codedata = "order=" + orderrc4 +"&account=" + userrc4 + "&way=" + way + "&gid=" + gidrc4 + "&t=" + GetTimeStamp.timeStamp();
                String data = null;
                if (miType == 1) {
                    data = Rc4.encry_RC4_string(codedata, RC4KEY);
                } else if (miType == 2) {
                    try {
                        data = Rsa.encrypt_Rsa(codedata, RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    data = AES.encrypt_Aes(AESKEY,codedata, AESIV);
                }

                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(codedata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(EmpowerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(15 * 1000,//请求的超时时间（以毫秒为单位），即请求在等待响应的最长时间
                0,//最大重试次数。如果请求失败，将会重试的次数。
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//重试的时间间隔倍数。每次重试的等待时间将是前一个重试时间乘以该倍数。
        mQueue.add(stringRequest);
    }

    /*请求支付响应*/
    public void GetpayResponse(String response,String PayType) {
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "GetpayResponse: " + response);
        Utils.loadingClose_Tv();
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            String msg = jSONObject.optString("msg");/*状态码*/
            if (code == 200){
                String order = jSONObject.optString("order");/*订单号*/
                String qr_url = jSONObject.optString("qr_url");/*收款码地址*/
                String qr_content = jSONObject.optString("qr_content");/*二维码内容*/
                String amount = jSONObject.optString("amount");/*金额*/
                String pay_type = jSONObject.optString("pay_type");/*支付方式*/
                if (!order.isEmpty()){
                    empower_url = qr_url;
                    orders = order;
                    /*跳转到支付页面（WebView加载支付链接）*/
                    if (qr_url != null && !qr_url.isEmpty()) {
                        showPayActivity(qr_url);
                    } else {
                        GetText();
                        Utils.showToast(EmpowerActivity.this, R.string.Order_exception, R.drawable.toast_shut);
                        return;
                    }
                    stoppay = 0;
                    mediaHandler.sendEmptyMessageDelayed(6, 4000);
                    Utils.showToast(EmpowerActivity.this, R.string.Scan_QR_code, R.drawable.toast_smile);
                }else{
                    /*关闭请求*/
                    GetText();
                    Utils.showToast(EmpowerActivity.this, R.string.Order_exception, R.drawable.toast_shut);
                }
            }else{
                stoppay = 1;
                qr_code_text.setText(Qr_code_text);
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    Msg = URLDecoder.decode(Rc4.decry_RC4(msg,RC4KEY), "UTF-8");
                } else if (miType == 2) {
                    Msg = URLDecoder.decode(Rsa.decrypt_Rsa(msg,RSAKEY), "UTF-8");
                } else if (miType == 3) {
                    Msg = URLDecoder.decode(AES.decrypt_Aes(AESKEY,msg, AESIV), "UTF-8");
                }
                GetText();
                mediaHandler.sendEmptyMessage(5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*查询支付*/
    public void Querypay() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app="+ Api.APPID +"&act=pay_res",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        QuerypayResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String codedata = "oid=" + orders  + "&t=" + GetTimeStamp.timeStamp();
                String data = null;
                if (miType == 1) {
                    data = Rc4.encry_RC4_string(codedata, RC4KEY);
                } else if (miType == 2) {
                    try {
                        data = Rsa.encrypt_Rsa(codedata, RSAKEY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (miType == 3) {
                    data = AES.encrypt_Aes(AESKEY,codedata, AESIV);
                }

                String sign = Md5Encoder.encode(new StringBuilder(String.valueOf(codedata)).append("&").append(Appkey).toString());
                Map<String, String> params = new HashMap<>();
                params.put("data", data);
                params.put("sign", sign);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(EmpowerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*查询订单响应*/
    public void QuerypayResponse(String response) {
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "QuerypayResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
            if (code == 200){
                stoppay = 1;
                GetText();
                qr_code_text.setText(Qr_code_text);
                GetInfo();
                Utils.showToast(EmpowerActivity.this, R.string.Recharged_successfully, R.drawable.toast_smile);
            }else if (code == 154){
                stoppay = 0;
                mediaHandler.sendEmptyMessageDelayed(6, 4000);
            }else if (code == 201){
                /*支付成功但充值失败*/
                stoppay = 1;
                /*联系客服二维码*/

                qr_code_text.setText(Qr_code_text);
                mediaHandler.sendEmptyMessage(7);
            }else if (code == 155){
                /*未知订单*/
                GetText();
                qr_code_text.setText(Qr_code_text);
                Utils.showToast(EmpowerActivity.this, R.string.unknown_error, R.drawable.toast_err);
            }else if (code == 153){
                /*订单不存在*/
                GetText();
                qr_code_text.setText(Qr_code_text);
                Utils.showToast(EmpowerActivity.this, R.string.Order_does_not_exist, R.drawable.toast_err);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*刷新帐号信息*/
    private void GetInfo() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=get_info",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        InfoResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(EmpowerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*刷新帐号信息响应*/
    public void InfoResponse(String response) {
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        //Log.i(TAG, "InfoResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            String msg = jSONObject.optString("msg");/*状态信息*/
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            JSONObject jSON = null;
            if (miType == 1) {
                jSON = new JSONObject(Rc4.decry_RC4(msg, RC4KEY));
            } else if (miType == 2) {
                jSON = new JSONObject(Rsa.decrypt_Rsa(msg, RSAKEY));
            } else if (miType == 3) {
                jSON = new JSONObject(AES.decrypt_Aes(AESKEY,msg, AESIV));
            }
            String vip = jSON.optString("vip");
            String fen = jSON.optString("fen");
            if (code == 200){
                viptime = vip;
                jifen.setText(fen);
                sp.edit()
                        .putString("vip", vip)
                        .putString("fen", fen)
                        .commit();
                if (viptime.equals("999999999")) {
                    if (!Long_Qr_code_Url.equals("")){
                        findViewById(R.id.box_layout).setBackgroundResource(R.drawable.frame_no);/*删除二维码框*/
                        Qr_code_Url = Long_Qr_code_Url;
                        empower_type = 1;
                        qr_code_text.setText("");
                    }
                    mediaHandler.sendEmptyMessage(1);
                } else {
                    mediaHandler.sendEmptyMessage(2);
                }
                mediaHandler.sendEmptyMessage(3);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*卡密充值*/
    private void getCard(final String username, final String code) {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=card",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        CardResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String codedata = "account=" + username + "&kami=" + code + "&t=" + GetTimeStamp.timeStamp();
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(EmpowerActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*卡密充值响应*/
    public void CardResponse(String response) {
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        //Log.i(TAG, "CardResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            String msg = null;
            if (miType == 1) {
                msg = Rc4.decry_RC4(jSONObject.optString("msg"), RC4KEY) ;
            } else if (miType == 2) {
                msg = Rsa.decrypt_Rsa(jSONObject.optString("msg"), RSAKEY) ;
            } else if (miType == 3) {
                msg = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
            }
            if (code == 200){
                Utils.showToast(EmpowerActivity.this, URLDecoder.decode(msg, "UTF-8"), R.drawable.toast_smile);
                GetInfo();
            }else{
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*启动时*/
    protected void onStart() {
        super.onStart();
        //Logger.i("EmpowerActivity", "nn?....onStart");
    }

    /*停止时*/
    protected void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.stop();
        }
        //Logger.i("EmpowerActivity", "EmpowerActivity....onStop");
    }

    /*销毁时*/
    protected void onDestroy() {
        super.onDestroy();
        stoppay = 1;
        if (mQueue != null) {
            mQueue.stop();
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        //Logger.d(TAG, "onDestroy");
    }

    /*暂停时*/
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd("EmpowerActivity");
        MobclickAgent.onPause(this);
        //Logger.i("EmpowerActivity", "EmpowerActivity....onPause");
    }

    /*按下返回键时*/
    public void onBackPressed() {
        super.onBackPressed();
        if (mQueue != null) {
            mQueue.stop();
        }
        stoppay = 1;
    }

    /*设置侦听器*/
    protected void setListener() {
    }

    /*显示二维码*/
    private void loadImg(int type) {
        /*扫描动画*/
        TranslateAnimation mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE,0f, TranslateAnimation.RELATIVE_TO_PARENT, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
        mAnimation.setDuration(3000);
        if (type == 1){
            mAnimation.setRepeatCount(0);
        }else{
            mAnimation.setRepeatCount(1);
        }
        mQrLineView.setVisibility(View.VISIBLE);
        mAnimation.setInterpolator(new LinearInterpolator());
        mQrLineView.setAnimation(mAnimation);
        mAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                mQrLineView.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (type == 1){
            Glide.with(this).load(Qr_code_Url).into(empower_iv_pay_ecode);
            return;
        }

        /*生成二维码*/
        Bitmap logoBitmap;
        if (way.equals("wx")) {
            logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wx);
        } else if (way.equals("ali")) {
            logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ali);
        } else if (way.equals("qq")) {
            logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qq);
        }else if (way.equals("other")) {
            logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.other);
        }else if (way.equals("icon")) {
            logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
        }  else {
            logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.other);
        }
        Bitmap empower = Utils.createQRCodeBitmap(empower_url, 400, 400, "UTF-8", "Q", "1", Color.BLACK, Color.WHITE, logoBitmap, 2);
        empower_iv_pay_ecode.setImageBitmap(empower);
    }

    /*
    private class WindowMessageID {
        public static final int ERWEIMA_FAIL = 7;
        public static final int LOAD_IMG = 8;
        public static final int RECHARGE_FAIL_DIS = 4;
        public static final int RECHARGE_FAIL_USED = 3;
        public static final int RECHARGE_SUCCESS = 2;
        public static final int RESPONSE_NO_SUCCESS = 1;
        public static final int VIP_TIME = 6;
        public static final int VIP_TIME_999 = 5;

        private WindowMessageID() {
        }
    }
    */
}
