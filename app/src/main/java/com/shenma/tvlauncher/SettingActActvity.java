package com.shenma.tvlauncher;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author joychang
 * @Description 活动专区
 */
public class SettingActActvity extends BaseActivity {
    private final String TAG = "SettingActActvity";
    private TextView account;
    private TextView empower_fens;
    private TextView vipTime;
    private TextView actishiwenzi;
//    public ImageLoader imageLoader;
    private ImageView empower_iv_pay_ecode;
    private String empower_url;
    public RequestQueue mQueue;
    private LinearLayout vip_root_lin_1;
    private RelativeLayout vip_goods_r_1;
    private TextView vip_goods_1_title;
    private TextView vip_goods_1_price;
    private TextView vip_goods_1_message;
    private TextView vip_goods_gid_1;
    private LinearLayout vip_root_lin_2;
    private RelativeLayout vip_goods_r_2;
    private TextView vip_goods_2_title;
    private TextView vip_goods_2_price;
    private TextView vip_goods_2_message;
    private TextView vip_goods_gid_2;
    private LinearLayout vip_root_lin_3;
    private RelativeLayout vip_goods_r_3;
    private TextView vip_goods_3_title;
    private TextView vip_goods_3_price;
    private TextView vip_goods_3_message;
    private TextView vip_goods_gid_3;
    private LinearLayout vip_root_lin_4;
    private RelativeLayout vip_goods_r_4;
    private TextView vip_goods_4_title;
    private TextView vip_goods_4_price;
    private TextView vip_goods_4_message;
    private TextView vip_goods_gid_4;
    private RelativeLayout vip_root_clock;
    private LinearLayout vip_clock;
    private RelativeLayout vip_root_inv;
    private LinearLayout vip_inv;
    private LinearLayout vip_ac;
    private TextView vip_acno;
    private String fen;
    private String Msg;
    private String clock_state = "1";
    private String inv_state = "1";
    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(context, R.string.request_failure, R.drawable.toast_err);
                    return;
                case 2:
                    Utils.showToast(SettingActActvity.this, getString(R.string.Check_in_successful) + Msg + "!", R.drawable.toast_smile);
                    GetInfo();
                    return;
                case 4:
                    FenList();
                    return;
                case 5:
                    Utils.showToast(context, R.string.Redemption_successful, R.drawable.toast_smile);
                    GetInfo();
                    return;
                case 6:
                    Utils.showToast(SettingActActvity.this, getString(R.string.fail) + Msg , R.drawable.toast_err);
                    return;
                case 7:
                    startActivity(new Intent(SettingActActvity.this, UserActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    Utils.showToast(SettingActActvity.this, getString(R.string.fail) + Msg , R.drawable.toast_err);
                    finish();
                    return;
                case 8:
                    loadImg();
                    return;
                default:
                    return;
            }
        }
    };

    /*创建时的回调函数*/
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting_act);
        findViewById();
        initView();
        initData();
    }

    /*按ID查找视图*/
    protected void findViewById() {
        findViewById(R.id.empowers).setBackgroundResource(R.drawable.video_details_bg);
        account = (TextView) findViewById(R.id.empower_codestrs);
        empower_fens = (TextView) findViewById(R.id.empower_fens);
        vipTime = (TextView) findViewById(R.id.empower_times);
        empower_iv_pay_ecode = (ImageView) findViewById(R.id.empower_iv_pay_ecode);
        vip_goods_1_message = (TextView) findViewById(R.id.vip_goods_1_message);
        vip_goods_1_title = (TextView) findViewById(R.id.vip_goods_1_title);
        vip_goods_1_price = (TextView) findViewById(R.id.vip_goods_1_price);
        vip_root_lin_1 = (LinearLayout) findViewById(R.id.vip_root_lin_1);
        vip_goods_r_1 = (RelativeLayout) findViewById(R.id.vip_goods_r_1);
        vip_goods_gid_1 = (TextView) findViewById(R.id.vip_goods_gid_1);
        vip_goods_2_message = (TextView) findViewById(R.id.vip_goods_2_message);
        vip_goods_2_title = (TextView) findViewById(R.id.vip_goods_2_title);
        vip_goods_2_price = (TextView) findViewById(R.id.vip_goods_2_price);
        vip_root_lin_2 = (LinearLayout) findViewById(R.id.vip_root_lin_2);
        vip_goods_r_2 = (RelativeLayout) findViewById(R.id.vip_goods_r_2);
        vip_goods_gid_2 = (TextView) findViewById(R.id.vip_goods_gid_2);
        vip_goods_3_message = (TextView) findViewById(R.id.vip_goods_3_message);
        vip_goods_3_title = (TextView) findViewById(R.id.vip_goods_3_title);
        vip_goods_3_price = (TextView) findViewById(R.id.vip_goods_3_price);
        vip_root_lin_3 = (LinearLayout) findViewById(R.id.vip_root_lin_3);
        vip_goods_r_3 = (RelativeLayout) findViewById(R.id.vip_goods_r_3);
        vip_goods_gid_3 = (TextView) findViewById(R.id.vip_goods_gid_3);
        vip_goods_4_message = (TextView) findViewById(R.id.vip_goods_4_message);
        vip_goods_4_title = (TextView) findViewById(R.id.vip_goods_4_title);
        vip_goods_4_price = (TextView) findViewById(R.id.vip_goods_4_price);
        vip_root_lin_4 = (LinearLayout) findViewById(R.id.vip_root_lin_4);
        vip_goods_r_4 = (RelativeLayout) findViewById(R.id.vip_goods_r_4);
        vip_goods_gid_4 = (TextView) findViewById(R.id.vip_goods_gid_4);

        vip_root_lin_1.setVisibility(View.GONE);
        vip_goods_r_1.setVisibility(View.GONE);
        vip_root_lin_2.setVisibility(View.GONE);
        vip_goods_r_2.setVisibility(View.GONE);
        vip_root_lin_3.setVisibility(View.GONE);
        vip_goods_r_3.setVisibility(View.GONE);
        vip_root_lin_4.setVisibility(View.GONE);
        vip_goods_r_4.setVisibility(View.GONE);

        vip_root_clock = (RelativeLayout) findViewById(R.id.vip_root_clock);
        vip_clock = (LinearLayout) findViewById(R.id.vip_clock);
        vip_root_inv = (RelativeLayout) findViewById(R.id.vip_root_inv);
        vip_inv = (LinearLayout) findViewById(R.id.vip_inv);
        vip_ac = (LinearLayout) findViewById(R.id.vip_ac);
        vip_acno = (TextView) findViewById(R.id.vip_acno);
        actishiwenzi = (TextView) findViewById(R.id.actishiwenzi);
        vip_goods_r_1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_1();
            }
        });
        vip_goods_r_2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_2();
            }
        });
        vip_goods_r_3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_3();
            }
        });
        vip_goods_r_4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                vip_goods_r_4();
            }
        });
        vip_root_clock.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                vip_root_clock();
            }
        });
        vip_root_inv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                vip_root_inv();
            }
        });
    }

    /*商品1被单击*/
    private void vip_goods_r_1(){
        String gid = vip_goods_gid_1.getText().toString();
        Getfen(gid);
    }

    /*商品2被单击*/
    private void vip_goods_r_2(){
        String gid = vip_goods_gid_2.getText().toString();
        Getfen(gid);
    }

    /*商品3被单击*/
    private void vip_goods_r_3(){
        String gid = vip_goods_gid_3.getText().toString();
        Getfen(gid);
    }

    /*商品4被单击*/
    private void vip_goods_r_4(){
        String gid = vip_goods_gid_4.getText().toString();
        Getfen(gid);
    }

    /*打卡被单击*/
    private void vip_root_clock(){
        GetClock();
    }

    /*邀请奖励被单击*/
    private void vip_root_inv(){
        int login_type = SharePreferenceDataUtil.getSharedIntData(this, "login_type", 0);
        if (login_type == 2){
            startActivity(new Intent(SettingActActvity.this, SettingInvActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }else{
            Utils.showToast(context, R.string.Not_yet_activated, R.drawable.toast_shut);
        }
    }

    /*发起支付订单*/
    private void Getfen(final String gid) {
        Utils.loadingShow_tv(context, R.string.loading);/*转圈*/
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID  + "&act=get_fen",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        GetfenResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
//                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String codedata = "token=" + sp.getString("ckinfo", null) + "&fid=" + gid + "&t=" + GetTimeStamp.timeStamp();
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingActActvity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*请求支付响应*/
    public void GetfenResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "GetfenResponse: " + response);
        Utils.loadingClose_Tv();
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");/*状态码*/
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
                mediaHandler.sendEmptyMessage(5);
            }else if (code == 125){
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(7);
            }else if (code == 127){
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(7);
            }else if (code == 114){
                Msg = URLDecoder.decode(msg, "UTF-8");
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                mediaHandler.sendEmptyMessage(7);
            }else{
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(6);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*发起打卡*/
    private void GetClock() {
        Utils.loadingShow_tv(context, R.string.loading);/*转圈*/
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        final String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID +"&act=clock",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        ClockResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
//                Error(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String codedata = "token=" + sp.getString("ckinfo", null)  + "&t=" + GetTimeStamp.timeStamp();
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingActActvity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);
    }

    /*打卡响应*/
    public void ClockResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "ClockResponse: " + response);
        Utils.loadingClose_Tv();
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
            String msg = null;
            if (miType == 1) {
                msg = Rc4.decry_RC4(jSONObject.optString("msg"), RC4KEY);
            } else if (miType == 2) {
                msg = Rsa.decrypt_Rsa(jSONObject.optString("msg"), RSAKEY);
            } else if (miType == 3) {
                msg = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
            }
            if (code == 200){
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(2);
            }else if (code == 125){
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(7);
            }else if (code == 127){
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(7);
            }else if (code == 114){
                Msg = URLDecoder.decode(msg, "UTF-8");
                sp.edit().putString("userName", null).putString("passWord", null).putString("vip", null).putString("fen", null).putString("ckinfo", null).commit();
                mediaHandler.sendEmptyMessage(7);
            }else{
                Msg = URLDecoder.decode(msg, "UTF-8");
                mediaHandler.sendEmptyMessage(6);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*初始化视图*/
    protected void initView() {
        account.setVisibility(View.VISIBLE);
        account.setText(sp.getString("userName", ""));
        empower_fens.setVisibility(View.VISIBLE);
        empower_fens.setText(sp.getString("fen", ""));
        vipTime.setVisibility(View.VISIBLE);

        if (sp.getString("userName", "").equals("")){
            account.setText(R.string.no_login);
            empower_fens.setText(R.string.no_login);
            vipTime.setText(R.string.no_login);
            return;
        }

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
        } else if (vipstate == 0) {
            vipTime.setText(R.string.Expired);
        } else if (vipstate == 1) {
            vipTime.setText(GetTimeStamp.timeStamp2Date(time, ""));
        }
    }

    /*初始化数据*/
    private void initData() {
//        imageLoader = MyVolley.getImageLoader();
        Getfen();
        GetAcText();
        GetInfo();
    }

    /*请求积分*/
    private void Getfen() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=fen",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        FenResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                mediaHandler.sendEmptyMessage(1);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingActActvity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*积分响应*/
    public void FenResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "FenResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    fen = Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY);
                } else if (miType == 2) {
                    fen = Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY);
                } else if (miType == 3) {
                    fen = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
                }
                mediaHandler.sendEmptyMessage(4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*积分列表*/
    private void FenList(){
        try {
            Object object = new JSONTokener(fen).nextValue();
            if (object instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) object;
                TextView[] gidViews = {vip_goods_gid_1, vip_goods_gid_2, vip_goods_gid_3, vip_goods_gid_4};
                TextView[] titleViews = {vip_goods_1_title, vip_goods_2_title, vip_goods_3_title, vip_goods_4_title};
                TextView[] priceViews = {vip_goods_1_price, vip_goods_2_price, vip_goods_3_price, vip_goods_4_price};
                TextView[] messageViews = {vip_goods_1_message, vip_goods_2_message, vip_goods_3_message, vip_goods_4_message};
                LinearLayout[] rootLinViews = {vip_root_lin_1, vip_root_lin_2, vip_root_lin_3, vip_root_lin_4};
                RelativeLayout[] goodsRViews = {vip_goods_r_1, vip_goods_r_2, vip_goods_r_3, vip_goods_r_4};
                int index = 0;
                for (int i = 0; i < jsonArray.length()&& index < 5; i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.has("clock_state")) {
                        clock_state = jsonObject.getString("clock_state");
                        if (clock_state.equals("0")){
                            vip_clock.setVisibility(View.GONE);
                        }
                    }
                    if (jsonObject.has("inv_state")) {
                        inv_state = jsonObject.getString("inv_state");
                        if (inv_state.equals("0")){
                            vip_inv.setVisibility(View.GONE);
                        }
                        int login_type = SharePreferenceDataUtil.getSharedIntData(this, "login_type", 0);
                        if (login_type != 2){
                            vip_inv.setVisibility(View.GONE);
                        }
                    }
                    if(clock_state.equals("0") && inv_state.equals("0")){
                        vip_ac.setVisibility(View.GONE);
                        vip_acno.setVisibility(View.VISIBLE);
                    }
                    if (jsonObject.has("gname")) {
                        String gid = jsonObject.getString("gid");
                        String gname = jsonObject.getString("gname");
                        String fen_num = jsonObject.getString("fen_num");
                        String vip_num = jsonObject.getString("vip_num");
                        if (!TextUtils.isEmpty(gname) && index < gidViews.length) {
                            gidViews[index].setText(gid);
                            titleViews[index].setText(gname);
                            priceViews[index].setText(fen_num + getString(R.string.integral));
                            messageViews[index].setText(getString(R.string.After_successful_redemption) + fen_num + getString(R.string.Obtaining_membership) + vip_num + getString(R.string.hour));
                            rootLinViews[index].setVisibility(View.VISIBLE);
                            goodsRViews[index].setVisibility(View.VISIBLE);
                            index++;
                        }
                    }



                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void GetAcText() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=ac_notice",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        AcTextResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
//                Error(error);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingActActvity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*扫码提示响应*/
    public void AcTextResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "AcTextResponse: " + response);
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
                String content = msg.getString("content");
                String qrCodeUrl = msg.getString("qr_code_url");
                empower_url = qrCodeUrl;
                actishiwenzi.setText(content);
            }
            mediaHandler.sendEmptyMessage(8);
        } catch (Exception e) {
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
//                Error(error);
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingActActvity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*刷新帐号信息响应*/
    public void InfoResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "InfoResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            String msg = jSONObject.optString("msg");
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
                empower_fens.setText(fen);
                sp.edit()
                        .putString("vip", vip)
                        .putString("fen", fen)
                        .commit();

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
                } else if (vipstate == 0) {
                    vipTime.setText(R.string.Expired);
                } else if (vipstate == 1) {
                    vipTime.setText(GetTimeStamp.timeStamp2Date(time, ""));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*加载二维码*/
    private void loadImg(){
        Glide.with(this).load(empower_url).into(empower_iv_pay_ecode);
    }

    /*设置侦听器*/
    protected void setListener() {
    }

    /*启动时*/
    protected void onStart() {
        super.onStart();
        Logger.i("SettingActActvity", "SettingActActvity....onStart");
    }

    /*停止时*/
    protected void onStop() {
        super.onStop();
        if (this.mQueue != null) {
            this.mQueue.stop();
        }
        Logger.i("SettingActActvity", "SettingActActvity....onStop");
    }

    /*暂停时*/
    protected void onPause() {
        super.onPause();
        Logger.i("SettingActActvity", "SettingActActvity....onPause");
    }

    /*按下返回键时*/
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*加载视图布局*/
    protected void loadViewLayout() {
    }

}
