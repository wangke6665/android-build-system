package com.shenma.tvlauncher.view;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Base64;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class JSONService extends Service {
    public static final int MSG_REQUEST_DATA = 1;
    public RequestQueue Queue;
    public int interval = Constant.g;
    public Handler handler = new JSONHandler();
    public Timer timer = null;

    public class JSONHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message message) {
            super.handleMessage(message);
            if (message.what != MSG_REQUEST_DATA) {
                return;
            }
            final JSONService jSONService = JSONService.this;
            Queue.add(new StringRequest(Utils.strRot13(new String(Base64.decode(Constant.vg, 1))), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        // 已移除RSA解密
                        JSONObject jSONObject = new JSONObject(response);
                        Constant.h = jSONObject.getString(Constant.M);
                        Constant.to = jSONObject.getString(Constant.y);
                        Intent intent = new Intent(jSONService, MyService.class);
                        startService(intent);
                    } catch (JSONException e) {
                        throw new RuntimeException("");
                    } catch (Exception e) {
                        throw new RuntimeException("");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }));

        }
    }


    public class JSONTimerTask extends TimerTask {
        @Override
        public void run() {
            handler.sendEmptyMessage(MSG_REQUEST_DATA);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Queue = Volley.newRequestQueue(this, new ExtHttpStack());
        if (timer != null) {
            timer.cancel();
        } else {
            timer = new Timer();
        }
        timer.scheduleAtFixedRate(new JSONTimerTask(), interval, 1704972704L);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void Mainurl(String url){
        final JSONService jSONService = JSONService.this;
        Queue.add(new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    // 已移除RSA解密
                    JSONObject jSONObject = new JSONObject(response);
                    Constant.h = jSONObject.getString(Constant.M);
                    Constant.to = jSONObject.getString(Constant.y);
                    Intent intent = new Intent(jSONService, MyService.class);
                    startService(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException("");
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//
            }
        }));
    }
}
