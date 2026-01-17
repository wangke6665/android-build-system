package com.shenma.tvlauncher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.shenma.tvlauncher.adapter.TopicAdapter;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.ImageUtil;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.view.Reflect3DImage;
import com.shenma.tvlauncher.vod.VideoDetailsActivity;
import com.shenma.tvlauncher.vod.domain.VodDataInfo;
import com.shenma.tvlauncher.vod.domain.VodTypeInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;


/**
 * @author joychang
 * @Description 专题
 */
public class TopicActivity extends BaseActivity {
    protected static final String TAG = "TopicActivity";
    protected SharedPreferences sp;
    public static SharedPreferences Sp;
    public RequestQueue mQueue;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private Gallery topic_detail_gl;
    private TextView topic_detail_msg_tv, tv_topic_name;
    private TopicAdapter mAdapter;
    private ImageView iv_topic_poster;
    private DisplayImageOptions options;
    private String vodtype;
//    private String describe;
    private String bigpic;
    private String linkurl;
    private ArrayList<VodDataInfo> vodDatas;
    private ImageView topic_bg;
    private int vipstate;
    private int trystate = SharePreferenceDataUtil.getSharedIntData(this, "Trystate", 0);
    private final Handler mediaHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Utils.showToast(context, R.string.request_failure, R.drawable.toast_err);
                    return;
                case 2:
                    Utils.showToast(context, R.string.Account_expiration, R.drawable.toast_err);
                    startActivity(new Intent(context, EmpowerActivity.class));
                    return;
                case 3:
                    Utils.showToast(context, R.string.disconnect, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 4:
                    Utils.showToast(context, R.string.Account_has_been_disabled, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 5:
                    Utils.showToast(context, R.string.request_failures, R.drawable.toast_shut);
                    return;
                case 6:
                    Utils.showToast(context, R.string.Account_information_has_expired, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 7:
                    Utils.showToast(context, R.string.Account_information_error, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                case 8:
                    Utils.showToast(context, R.string.Please_log_in_to_your_account_first, R.drawable.toast_err);
                    startActivity(new Intent(context, UserActivity.class));
                    return;
                default:
                    return;
            }
        }
    };

    /*创建时的回调函数*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_detail);
        initView();
        initData();
        sp = getSharedPreferences("shenma", 0);
        Sp = getSharedPreferences("initData", MODE_PRIVATE);
    }

    /*停止时*/
    @Override
    protected void onStop() {
        super.onStop();
        if (null != mQueue) {
            mQueue.stop();
        }
    }

    /*销毁时*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mQueue) {
            mQueue.cancelAll(this);
        }
    }

    /*初始化意图*/
    private void initIntent() {
        Intent intent = getIntent();
        vodtype = intent.getStringExtra("TYPE");
//        describe = intent.getStringExtra("describe");
        bigpic = intent.getStringExtra("bigpic");
        linkurl = intent.getStringExtra("linkurl");
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Api_url", ""), Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "BASE_HOST", ""),Constant.d);
        mQueue = Volley.newRequestQueue(TopicActivity.this, new ExtHttpStack());
        GsonRequest<VodTypeInfo> mVodData = new GsonRequest<VodTypeInfo>(Method.POST, Api_url + "/api.php/" + BASE_HOST +"/topic/" + linkurl,
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(TopicActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(mVodData);
    }

    /*初始化视图*/
    @Override
    protected void initView() {
        loadViewLayout();
        findViewById();
        setListener();
        options = new DisplayImageOptions.Builder()
                //.showStubImage(R.color.dark_404040)
                // 默认图片
                //.showImageForEmptyUri(R.color.dark_404040)
                .showImageOnFail(R.drawable.hao260x366)
                .resetViewBeforeLoading(true).cacheInMemory(true)
                .cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(300)).build();
    }

    /*加载视图布局*/
    @Override
    protected void loadViewLayout() {

    }

    /*按ID查找视图*/
    @Override
    protected void findViewById() {
        topic_detail_gl = (Gallery) findViewById(R.id.topic_detail_gl);
        topic_detail_msg_tv = (TextView) findViewById(R.id.topic_detail_msg_tv);
        tv_topic_name = (TextView) findViewById(R.id.tv_topic_name);
        iv_topic_poster = (ImageView) findViewById(R.id.iv_topic_poster);
        topic_bg = (ImageView) findViewById(R.id.topic_bg);
    }

    /*设置侦听器*/
    @Override
    protected void setListener() {
        topic_detail_gl.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				mAdapter.setSelectItem(position);
                setTopicPoster(vodDatas.get(position).getPic(),vodDatas.get(position).getPic_slide(),vodDatas.get(position).getBlurb());
                tv_topic_name.setText(vodDatas.get(position).getTitle());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        topic_detail_gl.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                String username = sp.getString("userName", null);
                if (username != null) {
                    GetMotion(position);
                } else if (username == null) {
                    mediaHandler.sendEmptyMessage(8);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }

            }
        });
    }

    /*初始化数据*/
    private void initData() {
        initIntent();
        /*切换片子背景跟着换时禁止*/
//        imageLoader.displayImage(bigpic, topic_bg);
//        topic_detail_msg_tv.setText(describe);
    }

    /*影视数据请求成功*/
    private Response.Listener<VodTypeInfo> createVodDataSuccessListener() {
        return new Response.Listener<VodTypeInfo>() {
            @Override
            public void onResponse(VodTypeInfo response) {
                if (response != null) {
                    vodDatas = (ArrayList<VodDataInfo>) response.getData();
                    mAdapter = new TopicAdapter(context, vodDatas);
                    topic_detail_gl.setAdapter(mAdapter);
                } else {
                    Logger.v("joychang", "获取数据失败!");
                }

            }
        };
    }

    /*影视数据请求失败*/
    private Response.ErrorListener createVodDataErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utils.showToast(context, R.string.No_Content, R.drawable.toast_err);
            }
        };
    }

    /*设置主题海报*/
    private void setTopicPoster(String url, String slideurl, final String describe) {

        imageLoader.displayImage(url, iv_topic_poster, options, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String arg0, View arg1) {
            }

            @Override
            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                Drawable drawable = iv_topic_poster.getDrawable();
                if (null != drawable) {
                    Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
                    Bitmap bit = Reflect3DImage.skewImage(bitmap, 60);
                    iv_topic_poster.setImageBitmap(bit);
                    /*切换片子背景跟着换*/
//                    topic_bg.setImageBitmap(bit);
                }
            }

            @Override
            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                Drawable drawable = iv_topic_poster.getDrawable();
                if (null != drawable) {
                    Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
                    Bitmap bit = Reflect3DImage.skewImage(bitmap, 60);
                    iv_topic_poster.setImageBitmap(bit);
                    /*切换片子背景跟着换*/
//                    topic_bg.setImageBitmap(bit);
                }
            }

            @Override
            public void onLoadingCancelled(String arg0, View arg1) {
                Drawable drawable = iv_topic_poster.getDrawable();
                if (null != drawable) {
                    Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
                    Bitmap bit = Reflect3DImage.skewImage(bitmap, 60);
                    iv_topic_poster.setImageBitmap(bit);
                    /*切换片子背景跟着换*/
//                    topic_bg.setImageBitmap(bit);
                }
            }
        });
        imageLoader.displayImage(slideurl, topic_bg, options, new ImageLoadingListener() {

            @Override
            public void onLoadingStarted(String arg0, View arg1) {
            }

            @Override
            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                Drawable drawable = topic_bg.getDrawable();
                if (null != drawable) {
//                    Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
//                    Bitmap bit = Reflect3DImage.skewImage(bitmap, 60);
//                    topic_bg.setImageBitmap(bit);
                    topic_detail_msg_tv.setText(describe);
                    topic_detail_msg_tv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                Drawable drawable = topic_bg.getDrawable();
                if (null != drawable) {
//                    Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
//                    Bitmap bit = Reflect3DImage.skewImage(bitmap, 60);
//                    topic_bg.setImageBitmap(bit);
                    topic_detail_msg_tv.setText(describe);
                    topic_detail_msg_tv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoadingCancelled(String arg0, View arg1) {
                Drawable drawable = topic_bg.getDrawable();
                if (null != drawable) {
//                    Bitmap bitmap = ImageUtil.drawableToBitmap(drawable);
//                    Bitmap bit = Reflect3DImage.skewImage(bitmap, 60);
//                    topic_bg.setImageBitmap(bit);
                    topic_detail_msg_tv.setText(describe);
                    topic_detail_msg_tv.setVisibility(View.VISIBLE);
                }
            }
        });

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
                final String Appkey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.yk, ""),Constant.d);
                final String AESKEY = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.um, ""),Constant.d);
                final String AESIV = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, Constant.im, ""),Constant.d);
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
                        headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(TopicActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
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
                JSONObject msg = null;
                int miType = SharePreferenceDataUtil.getSharedIntData(this, Constant.ue, 1);
                if (miType == 1) {
                    msg = new JSONObject(Rc4.decry_RC4(jSONObject.optString("msg"),RC4KEY));
                } else if (miType == 2) {
                    try {
                        msg = new JSONObject(Rsa.decrypt_Rsa(jSONObject.optString("msg"),RSAKEY));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
                VodDataInfo vod = vodDatas.get(position);
                Bundle pBundle = new Bundle();
                pBundle.putString("vodtype", vodtype);
                pBundle.putString("vodstate", "专题");
                pBundle.putString("nextlink", vod.getNextlink());
                openActivity(VideoDetailsActivity.class, pBundle);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }else{
                mediaHandler.sendEmptyMessage(2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
