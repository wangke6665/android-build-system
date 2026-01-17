package com.shenma.tvlauncher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author joychang
 * @Description 邀请奖励
 */
public class SettingInvActivity extends BaseActivity{
    private TextView inv_user;
    private TextView inv_invcode;
    private ImageView inv_img;
    private TextView ivn_text;
    private String Inv_url;
    public RequestQueue mQueue;
    private final String TAG = "SettingInvActivity";
    private Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    loadImg();
                    return;
                default:
                    return;
            }
        }
    };


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting_inv);
        findViewById();
        initView();
        initData();
    }

    protected void initView() {
        inv_user.setVisibility(View.VISIBLE);
        inv_invcode.setVisibility(View.VISIBLE);
        String user = sp.getString("userName", "");
        if (user != ""){
            inv_user.setText(sp.getString("userName", ""));
        }else{
            inv_user.setText(R.string.no_login);
            startActivity(new Intent(SettingInvActivity.this, UserActivity.class));
            finish();
            Utils.showToast(SettingInvActivity.this, R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
        }
        inv_invcode.setText(R.string.Activity_not_opened);
    }

    protected void loadViewLayout() {

    }

    protected void findViewById() {
        findViewById(R.id.inv).setBackgroundResource(R.drawable.video_details_bg);
        inv_user = (TextView) findViewById(R.id.inv_user);//帐号数据
        inv_invcode = (TextView) findViewById(R.id.inv_invcode);//邀请码数据
        inv_img = (ImageView) findViewById(R.id.inv_img);
        ivn_text = (TextView) findViewById(R.id.ivn_text);
    }

    private void initData(){
        GetInfo();
        GetInv();
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingInvActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*刷新帐号信息响应*/
    public void InfoResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
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
            String inv = jSON.optString("inv");
            if (code == 200){
                inv_invcode.setText(inv);
                sp.edit().putString("vip", vip).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*请求邀请*/
    private void GetInv() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        String User_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.s, ""),Constant.d);
        final String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""),Constant.d);
        final String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
        int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, User_url + "/api?app=" + Api.APPID + "&act=inv",
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        InvResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingInvActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(stringRequest);

    }

    /*邀请响应*/
    public void InvResponse(String response) {
        String RC4KEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.kd, ""), Constant.d);
        String RSAKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.tb, ""),Constant.d);
        String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
        String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
        //Log.i(TAG, "InvResponse: " + response);
        try {
            JSONObject jSONObject = new JSONObject(response);
            int code = jSONObject.optInt("code");
            if (code == 200){
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                String msg = null;
                if (miType == 1) {
                    msg = Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY);
                } else if (miType == 2) {
                    msg = Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY);
                } else if (miType == 3) {
                    msg = AES.decrypt_Aes(AESKEY,jSONObject.optString("msg"), AESIV);
                }
                JSONObject MSGjSON = new JSONObject(msg);
                String inv_state = MSGjSON.optString("inv_state");
                String inv_text = MSGjSON.optString("inv_text");
                String inv_url = MSGjSON.optString("inv_url");
                if (!inv_state.equals("0")){
                    ivn_text.setText(URLDecoder.decode(inv_text, "UTF-8"));
                    Inv_url = inv_url;
                    mediaHandler.sendEmptyMessage(1);
                }else{
                    inv_invcode.setText("活动尚未开启");
                    inv_img.setVisibility(View.GONE);
                    ivn_text.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setListener() {
    }

    private void loadImg(){
        inv_img.setVisibility(View.VISIBLE);
        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon); // 读取Logo文件
        Bitmap empower = Utils.createQRCodeBitmap(Inv_url, 300, 300,"UTF-8","H", "1", Color.BLACK, Color.WHITE,logoBitmap,2);
        inv_img.setImageBitmap(empower);
    }

    /*启动时*/
    protected void onStart() {
        super.onStart();
        //Logger.i("SettingInvActivity", "SettingInvActivity....onStart");
    }

    /*停止时*/
    protected void onStop() {
        super.onStop();
        if (this.mQueue != null) {
            this.mQueue.stop();
        }
        //Logger.i("SettingInvActivity", "SettingInvActivity....onStop");
    }

    /*暂停时*/
    protected void onPause() {
        super.onPause();
        //Logger.i("SettingInvActivity", "SettingInvActivity....onPause");
    }

    /*按下返回键时*/
    public void onBackPressed() {
        super.onBackPressed();
        //Logger.i("SettingInvActivity", "SettingInvActivity....BackPressed");
    }

}
