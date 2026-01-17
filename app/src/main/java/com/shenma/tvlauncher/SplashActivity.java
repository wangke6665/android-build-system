package com.shenma.tvlauncher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.bumptech.glide.Glide;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.EncryptionConfig;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.Rsa;
import com.shenma.tvlauncher.utils.IO;
import com.shenma.tvlauncher.utils.Kodi;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import static com.shenma.tvlauncher.utils.GZUtils.A;
import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

/**
 * 应用开启启动界面
 *
 * @author joychang
 */

public class SplashActivity extends BaseActivity {
    protected static final String TAG = "SplashActivity";
    private static final int DETECTION_NET = 18;
    private static final int GET_INFO_SUCCESS = 10;
    private static final int GET_CATEGORY_FAIL = 9;
    private static final int EXIT = 8;
    private static final int FAIL = 7;
    public RequestQueue mQueue;
    public static SharedPreferences sp;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0: {
                    Utils.showToast(SplashActivity.this,R.string.request_failure, R.drawable.toast_err);
                    // Android 4.4 上缩短重试间隔，更快响应网络恢复
                    int retryDelay = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) ? 5000 : 15000;
                    handler.sendEmptyMessageDelayed(DETECTION_NET, retryDelay);
                    break;
                }
                case 1:
                    /*显示广告*/
                    if(imgurl != null && imgurl != ""){
                        Glide.with(SplashActivity.this).load(imgurl).into(splash);
                    }
                    /*执行广告倒计时*/
                    handler.sendEmptyMessage(2);
                    break;
                case 2:
                    if (tv_ad_time > 0){
                        tv_jump_time.setText(String.valueOf(tv_ad_time));
                        tv_ad_time = tv_ad_time - 1;
                        handler.sendEmptyMessageDelayed(3, 1000);
                        return;
                    }
                    Category();
                    break;
                case 3:
                    if (tv_ad_time > 0){
                        tv_jump_time.setText(String.valueOf(tv_ad_time));
                        tv_ad_time = tv_ad_time - 1;
                        handler.sendEmptyMessageDelayed(2, 1000);
                        return;
                    }
                    Category();
                    break;
                case GET_INFO_SUCCESS:
                    Category();
                    break;
                case DETECTION_NET: {
                    if (Utils.hasNetwork(context)) {
                        initData();
                    } else {
                        // Android 4上缩短重试间隔，更快响应网络恢复
                        int retryDelay = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) ? 5000 : 15000;
                        handler.sendEmptyMessageDelayed(DETECTION_NET, retryDelay);
                    }
                    break;
                    }
                case GET_CATEGORY_FAIL:
                    Utils.showToast(SplashActivity.this, R.string.Category_failure, R.drawable.toast_err);
                    //handler.sendEmptyMessageDelayed(EXIT, 2000);
                    break;
                case EXIT:
                    System.exit(0);//闪退APP
                    return;
                case FAIL:
                    Utils.showToast(SplashActivity.this, getString(R.string.fail) + Msg , R.drawable.toast_err);
                    handler.sendEmptyMessageDelayed(EXIT, 2000);
                    return;
                default:
                    return;
            }
        }
    };
    private TextView tv_splash_version;/*版本号*/
    private ImageView splash;/*广告图片*/
    private String imgurl;/*广告地址*/
    private TextView tv_jump_time;/*广告时间*/
    private TextView tv_jump;/*跳过按钮*/
    private LinearLayout tv_jump_id;/*打卡按钮*/
    private int tv_ad_time;/*倒计时*/
    private int mishwdecode;
    private int interface_style;
    private int allow_changing_styles;
    private String category = null;
    private String Msg;



    /*创建时的回调函数*/
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        // 打印包名和签名信息
        try {
            String packageName = getPackageName();
            android.util.Log.d("SplashActivity", "包名: " + packageName);
            
            android.content.pm.PackageInfo packageInfo = getPackageManager()
                .getPackageInfo(packageName, android.content.pm.PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : packageInfo.signatures) {
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String signatureHash = android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
                android.util.Log.d("SplashActivity", "签名(SHA1 Base64): " + signatureHash);
                
                // MD5格式
                java.security.MessageDigest md5 = java.security.MessageDigest.getInstance("MD5");
                md5.update(signature.toByteArray());
                byte[] digest = md5.digest();
                StringBuilder sb = new StringBuilder();
                StringBuilder sb32 = new StringBuilder();
                for (byte b : digest) {
                    sb.append(String.format("%02X:", b & 0xff));
                    sb32.append(String.format("%02X", b & 0xff));
                }
                if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
                android.util.Log.d("SplashActivity", "签名(MD5): " + sb.toString());
                android.util.Log.d("SplashActivity", "签名(MD5 32位): " + sb32.toString());
                android.util.Log.d("SplashActivity", "签名+包名: " + sb32.toString() + packageName);
            }
        } catch (Exception e) {
            android.util.Log.e("SplashActivity", "获取签名失败: " + e.getMessage());
        }
        
        findViewById();
        sp = getSharedPreferences("initData", MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_CONTACT = 1;
            String[] permissions = {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            //验证是否许可权限
            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    //申请权限
                    //	Toast.makeText(getApplicationContext(), "请授予我权限，一起愉快的玩耍吧！", REQUEST_CODE_CONTACT).show();
                    requestPermissions(permissions, REQUEST_CODE_CONTACT);
                }
            }
        }

    }

    /*启动时*/
    protected void onStart() {
        super.onStart();
        isNetWork();
    }

    /*加载主界面*/
    private void loadMainUI() {
        Intent intent = null;
        intent = new Intent(SplashActivity.this, HomeActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
//        openActivity(HomeActivity.class);
        finish();// 把当前的activity从任务栈里面移除
    }

    /*停止时*/
    @Override
    protected void onStop() {
        super.onStop();
        if (mQueue != null) {
            mQueue.stop();
        }
    }

    /*销毁时*/
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (null != mQueue) {
            mQueue.cancelAll(this);
        }
    }

    /*按下返回键时*/
    @Override
    public void onBackPressed() {
        System.exit(0);
        finish();
    }

    /*检测网络*/
    private void isNetWork() {
        if (Utils.hasNetwork(context)) {
            initData();
            return;
        }
        // Android 4上延迟检测，给网络更多时间初始化
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Utils.hasNetwork(context)) {
                        initData();
                    } else {
        showNetDialog(context);
                        handler.sendEmptyMessageDelayed(DETECTION_NET, 2000);
                    }
                }
            }, 500);
        } else {
            showNetDialog(context);
            handler.sendEmptyMessageDelayed(DETECTION_NET, 1000);
        }
    }

    /*初始化数据*/
    private void initData() {
        if (Api.COS_MIAN_URL.equals("")){
            Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
        }else {
            CosMainurl();
        }
    }

    /*请求云Cos*/
    private void CosMainurl(){
        try {
            String cosUrl = new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.COS_MIAN_URL, 1))), 1));
            Log.d(TAG, "开始请求云COS地址: " + cosUrl);
            Log.d(TAG, "Android 版本: " + Build.VERSION.SDK_INT);

        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        /*超时时间：Android 4.4 使用更长超时时间，其他版本使用5秒*/
            int socketTimeout = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) ? 30000 : 5000;
            Log.d(TAG, "超时时间设置: " + socketTimeout + "ms");
            
        /*请求云COS地址*/
            StringRequest stringRequest = new StringRequest(Request.Method.GET, cosUrl,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                            Log.d(TAG, "云COS请求成功，响应长度: " + (response != null ? response.length() : 0));
                        CosResponse(response);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "云COS请求失败: " + (error != null ? error.getMessage() : "null"));
                    if (error != null && error.networkResponse != null) {
                        Log.e(TAG, "网络响应状态码: " + error.networkResponse.statusCode);
                    }
                    if (error != null && error.getCause() != null) {
                        Log.e(TAG, "错误原因: " + error.getCause().getMessage());
                        error.getCause().printStackTrace();
                    }
                CosError(error);
            }
        });
            // Android 4.4 上增加重试次数，提高成功率
            int maxRetries = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) ? 2 : 0;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(socketTimeout,//请求的超时时间（以毫秒为单位），即请求在等待响应的最长时间
                    maxRetries,//最大重试次数。如果请求失败，将会重试的次数。
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//重试的时间间隔倍数。每次重试的等待时间将是前一个重试时间乘以该倍数。
        mQueue.add(stringRequest);
            Log.d(TAG, "云COS请求已添加到队列，重试次数: " + maxRetries);
        } catch (Exception e) {
            Log.e(TAG, "CosMainurl 异常: " + e.getMessage(), e);
            // 异常时直接回退到主控地址
            Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
        }
    }

    /*云Cos成功*/
    public void CosResponse(String response) {
        //Log.i(TAG, "CosResponse: " + response);
        // 如果响应为空或null，直接回退到主控地址
        if (response == null || response.trim().isEmpty()) {
            Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
            return;
        }
        
        try {
            /*解析结果不加密*/
            JSONObject jSONObject = new JSONObject(response);
            String url = jSONObject.optString("url");/*状态信息*/
            String type = jSONObject.optString("type");/*状态信息*/
            if (!url.equals("")){
                if (!type.equals("")){
                    Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(url, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(type), 1))));
                }else{
                    Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(url, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
                }
            }else{
                Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
            }
//            System.out.println("解码模式：硬解" + Utils.strRot13(new String(Base64.decode(Utils.strRot13(type), 1)))  );
           // System.out.println("解码模式：硬解" + new String( Base64.decode(Utils.strRot13(new String(Base64.decode(url, 1))), 1)) );
        } catch (JSONException e) {
            // JSON解析失败，回退到主控地址
            e.printStackTrace();
            Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
        } catch (Exception e) {
            // 其他异常（如Base64解码失败），也回退到主控地址
            e.printStackTrace();
            Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
        }

    }

    /*云Cos错误*/
    public void CosError(VolleyError volleyError) {
        Log.e(TAG, "CosError: " + (volleyError != null ? volleyError.getMessage() : "null"));
        if (volleyError != null && volleyError.getCause() != null) {
            Log.e(TAG, "CosError cause: " + volleyError.getCause().getMessage());
            volleyError.getCause().printStackTrace();
        }
        
        // 无论什么错误，都回退到主控地址
        // Android 4上立即回退，不延迟，因为已经重试过了
        Mainurl(new String( Base64.decode(Utils.strRot13(new String(Base64.decode(Api.MIAN_URL, 1))), 1)),Utils.strRot13(new String(Base64.decode(Utils.strRot13(Constant.e), 1))));
    }

    /*主控地址*/
    private void Mainurl(String url, final String Authorization){
        try {
            String requestUrl = url + "?app=" + Api.APPID;
            Log.d(TAG, "开始请求主控地址: " + requestUrl);
            Log.d(TAG, "Android 版本: " + Build.VERSION.SDK_INT);
            
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        /*超时时间：Android 4.4 使用更长超时时间，其他版本使用5秒*/
            int socketTimeout = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) ? 30000 : 5000;
            Log.d(TAG, "超时时间设置: " + socketTimeout + "ms");
            
        /*请求主控地址*/
//        String url = Api.MIAN_URL + "?app=" + Api.APPID;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, requestUrl,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        RequestResponse(response,Authorization);
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "主控地址请求失败: " + (error != null ? error.getMessage() : "null"));
                if (error != null && error.networkResponse != null) {
                    Log.e(TAG, "网络响应状态码: " + error.networkResponse.statusCode);
                }
                if (error != null && error.getCause() != null) {
                    Log.e(TAG, "错误原因: " + error.getCause().getMessage());
                    error.getCause().printStackTrace();
                }
                RequestError(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                params.put("key", AES.encrypt_Aes(Md5Encoder.encode(Constant.d), Md5Encoder.encode(Constant.b),Constant.c));
                params.put("sign", Base64.encodeToString(Utils.strRot13(Constant.c).getBytes(), Base64.DEFAULT));
                params.put("t", GetTimeStamp.timeStamp());
                params.put("ios", "new2");
                
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Authorization); // 设置其他请求头
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); // 确保Content-Type正确
                return headers;
            }
            
            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };
            // Android 4.4 上增加重试次数，提高成功率
            int maxRetries = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) ? 2 : 0;
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(socketTimeout,//请求的超时时间（以毫秒为单位），即请求在等待响应的最长时间
                    maxRetries,//最大重试次数。如果请求失败，将会重试的次数。
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//重试的时间间隔倍数。每次重试的等待时间将是前一个重试时间乘以该倍数。
        mQueue.add(stringRequest);
            Log.d(TAG, "主控地址请求已添加到队列，重试次数: " + maxRetries);
        } catch (Exception e) {
            Log.e(TAG, "Mainurl 异常: " + e.getMessage(), e);
            // 异常时重试
            handler.sendEmptyMessageDelayed(0, 2000);
        }
    }

    /*键盘监听*/
    @SuppressLint("RestrictedApi")
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER://按下遥控器ok键跳过广告
                tv_ad_time = 0;
                break;
            case KeyEvent.KEYCODE_ENTER://按下回车跳过广告
                tv_ad_time = 0;
                break;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    /*获取成功*/
    public void RequestResponse(String response,String Authorization) {
        Log.d("SplashActivity", "RequestResponse 开始处理响应，响应长度: " + (response != null ? response.length() : 0));
        try {
            /*解析结果不加密*/
//            JSONObject jSONObject = new JSONObject(response);
            /*解析结果加密*/
//            JSONObject jSONObject = new JSONObject(Rsa.decrypt_Rsa(response, Constant.a));
            /*解析结果加密+偏移+rc4*/
//            JSONObject jSONObject = new JSONObject(IO.a(Kodi.a(Rsa.decrypt_Rsa(response, Constant.a)),Constant.rq + Constant.dp));
            /*解析结果加密 - 支持新方式（AES-256-GCM）和旧方式（ROT13+RC4）*/
            JSONObject jSONObject;
            try {
                // 统一使用 legacy 模式（ROT13+RC4），兼容所有 Android 版本
                Log.d("SplashActivity", "统一使用 legacy 解密方式（ROT13+RC4）");
                
                String jsonString;
                // 统一使用 legacy 模式，不再使用 GCM
                if (false) { // 禁用 GCM 模式，统一使用 legacy
                    // 新方式：AES-256-GCM解密
                    // 流程：AES-256-GCM解密（内部处理Base64） → Gzip解压 → JSON字符串
                    Log.d("SplashActivity", "使用AES-256-GCM解密方式");
                    
                    // 步骤1: AES-256-GCM解密（内部会处理Base64解码）
                    String aesKey = EncryptionConfig.getAESKey();
                    Log.d("SplashActivity", "AES密钥长度: " + (aesKey != null ? aesKey.length() : 0));
                    // 直接获取字节数组，避免字符串转换导致的数据损坏
                    byte[] aesDecrypted = AES.decryptAES256GCMToBytes(aesKey, response);
                    if (aesDecrypted == null || aesDecrypted.length == 0) {
                        throw new IllegalArgumentException("AES解密结果为空");
                    }
                    Log.d("SplashActivity", "AES解密成功，长度: " + aesDecrypted.length);
                    
                    // 步骤2: Gzip解压（直接使用字节数组）
                    byte[] gzipDecompressed = A(aesDecrypted);
                    if (gzipDecompressed == null) {
                        throw new IllegalArgumentException("Gzip解压失败，返回null");
                    }
                    Log.d("SplashActivity", "Gzip解压成功，长度: " + gzipDecompressed.length);
                    
                    // 步骤3: 转换为字符串
                    jsonString = new String(gzipDecompressed, "UTF-8");
                    if (jsonString == null || jsonString.isEmpty()) {
                        throw new IllegalArgumentException("Gzip解压后的字符串为空");
                    }
                    Log.d("SplashActivity", "最终JSON字符串长度: " + jsonString.length());
                } else {
                    // 旧方式：ROT13+RC4+Base64+Gzip（legacy模式，保持兼容）
                    Log.d("SplashActivity", "使用Legacy解密方式（ROT13+RC4）");
                    
                    // 步骤1: ROT13解码
                    String rot13Decoded = Kodi.a(response);
                    Log.d("SplashActivity", "ROT13解码后长度: " + (rot13Decoded != null ? rot13Decoded.length() : 0));
                    Log.d("SplashActivity", "ROT13解码后前100字符: " + (rot13Decoded != null && rot13Decoded.length() > 100 ? rot13Decoded.substring(0, 100) : rot13Decoded));
                    
                    // 步骤2: RC4解密
                    String rc4Key = EncryptionConfig.getRC4Key();
                    Log.d("SplashActivity", "RC4密钥长度: " + (rc4Key != null ? rc4Key.length() : 0));
                    Log.d("SplashActivity", "RC4密钥前50字符: " + (rc4Key != null && rc4Key.length() > 50 ? rc4Key.substring(0, 50) : rc4Key));
                    String rc4Decrypted = IO.a(rot13Decoded, rc4Key);
                    Log.d("SplashActivity", "RC4解密后: " + (rc4Decrypted != null ? "非空, 长度=" + rc4Decrypted.length() : "null"));
                    if (rc4Decrypted != null) {
                        Log.d("SplashActivity", "RC4解密后前100字符: " + (rc4Decrypted.length() > 100 ? rc4Decrypted.substring(0, 100) : rc4Decrypted));
                        // 检查是否是有效的Base64
                        try {
                            Base64.decode(rc4Decrypted, Base64.DEFAULT);
                            Log.d("SplashActivity", "RC4解密后的字符串是有效的Base64");
                        } catch (IllegalArgumentException e) {
                            Log.e("SplashActivity", "RC4解密后的字符串不是有效的Base64: " + e.getMessage());
                            String hexStr = "";
                            int len = Math.min(200, rc4Decrypted.length());
                            for (int i = 0; i < len; i++) {
                                hexStr += String.format("%02x", (byte)rc4Decrypted.charAt(i));
                            }
                            Log.e("SplashActivity", "RC4解密后的字符串前200字符(hex): " + hexStr);
                            throw e; // 重新抛出异常
                        }
                    }
                    
                    // 步骤3: Base64解码
                    if (rc4Decrypted == null || rc4Decrypted.isEmpty()) {
                        throw new IllegalArgumentException("RC4解密结果为空，无法进行Base64解码");
                    }
                    byte[] base64Decoded = Base64.decode(rc4Decrypted, Base64.DEFAULT);
                    if (base64Decoded == null) {
                        throw new IllegalArgumentException("Base64解码失败，返回null");
                    }
                    Log.d("SplashActivity", "Base64解码成功，长度: " + base64Decoded.length);
                    
                    // 步骤4: Gzip解压
                    byte[] gzipDecompressed = A(base64Decoded);
                    if (gzipDecompressed == null) {
                        throw new IllegalArgumentException("Gzip解压失败，返回null");
                    }
                    Log.d("SplashActivity", "Gzip解压成功，长度: " + gzipDecompressed.length);
                    
                    // 步骤5: 转换为字符串
                    jsonString = new String(gzipDecompressed, "UTF-8");
                    if (jsonString == null || jsonString.isEmpty()) {
                        throw new IllegalArgumentException("Gzip解压后的字符串为空");
                    }
                    Log.d("SplashActivity", "最终JSON字符串长度: " + jsonString.length());
                }
                
                jSONObject = new JSONObject(jsonString);
                Log.d("SplashActivity", "JSON解析成功");
            } catch (IllegalArgumentException e) {
                Log.e("SplashActivity", "解密/解码失败: " + e.getMessage());
                e.printStackTrace();
                // 不再抛出异常，而是记录错误并尝试继续处理
                // 如果响应看起来像JSON，尝试直接解析
                try {
                    Log.d("SplashActivity", "尝试直接解析响应为JSON");
                    jSONObject = new JSONObject(response);
                    Log.d("SplashActivity", "直接JSON解析成功");
                } catch (Exception e2) {
                    Log.e("SplashActivity", "直接JSON解析也失败: " + e2.getMessage());
                    // 如果都失败了，调用错误处理
                    handler.sendEmptyMessage(0);
                    return;
                }
            } catch (Exception e) {
                Log.e("SplashActivity", "解码过程出错: " + e.getMessage());
                e.printStackTrace();
                // 不再抛出异常，而是记录错误并尝试继续处理
                try {
                    Log.d("SplashActivity", "尝试直接解析响应为JSON");
                    jSONObject = new JSONObject(response);
                    Log.d("SplashActivity", "直接JSON解析成功");
                } catch (Exception e2) {
                    Log.e("SplashActivity", "直接JSON解析也失败: " + e2.getMessage());
                    // 如果都失败了，调用错误处理
                    handler.sendEmptyMessage(0);
                    return;
                }
            }


            int code = jSONObject.optInt("code");/*状态码*/
            String msg = jSONObject.optString("msg");/*状态信息*/
            if (code == 200){
//                String ad_url = AES.decrypt_Aes(Constant.b,jSONObject.optString("Ad_url"),Constant.c);/*广告地址*/
//                String user_url = AES.decrypt_Aes(Constant.b,jSONObject.optString("User_url"),Constant.c);/*易如意地址*/
//                String appkey = AES.decrypt_Aes(Constant.b,jSONObject.optString("Appkey"),Constant.c);/*APP密钥*/
//                String rc4key = AES.decrypt_Aes(Constant.b,jSONObject.optString("Rc4key"),Constant.c);/*rc4密钥*/
//                String mi_rsa_public_key = AES.decrypt_Aes(Constant.b,jSONObject.optString("mi_rsa_public_key"),Constant.c);/*rsa公钥*/
//                //String mi_rsa_private_key = AES.decrypt_Aes(Constant.b,jSONObject.optString("mi_rsa_private_key"),Constant.c);/*rsa私钥*/
//                String mi_aes_key = AES.decrypt_Aes(Constant.b,jSONObject.optString("mi_aes_key"),Constant.c);/*aes密钥*/
//                String mi_aes_iv = AES.decrypt_Aes(Constant.b,jSONObject.optString("mi_aes_iv"),Constant.c);/*aesiv*/
//                String api_url = AES.decrypt_Aes(Constant.b,jSONObject.optString("Api_url"),Constant.c);/*苹果地址*/
//                String apikey = AES.decrypt_Aes(Constant.b,jSONObject.optString("ApiKey"),Constant.c);/*苹果密钥*/
//                String fb_url = AES.decrypt_Aes(Constant.b,jSONObject.optString("Fb_url"),Constant.c);/*反馈地址*/
//                JSONObject config = jSONObject.getJSONObject("Config");/*配置信息*/
//                String play_core = config.optString("play_core");/*播放器内核*/
//                String live_core = config.optString("live_core");/*直播播放器内核*/
//                String play_decode = config.optString("play_decode");/*显示初装播放器解码*/
//                String play_ratio = config.optString("play_ratio");/*初装画面比例*/
//                int mi_type = config.optInt("mi_type");/*易如意加密类型*/
//                int packet_buffering = config.optInt("packet_buffering");/*启用缓冲进度条*/
//                int videotimeout = config.optInt("videotimeout");/*视频连接超时跳帧*/
//                int http_detect_range_support = config.optInt("http_detect_range_support");/*播放资源是否支持续传*/
//                int reconnect = config.optInt("reconnect");/*资源是否断线重连*/
//                int resources_reconnect = config.optInt("resources_reconnect");/*资源断线重连次数*/
//                int live_streaming = config.optInt("live_streaming");/*直播流媒体优化*/
//                int skip_loop_filter = config.optInt("skip_loop_filter");/*画面质量*/
//                int home_text_shadow = config.optInt("Home_text_shadow");/*热门推荐文字及阴影*/
//                int ad_time = config.optInt("Ad_time");/*开屏广告时间*/
//                int ad_jump = config.optInt("Ad_jump");/*跳过广告*/
//                int trytime = config.optInt("Trytime");/*试看时间*/
//                int submission_method = config.optInt("Submission_method");/*解析读取提交方式*/
//                String play_jump_end = config.optString("play_jump_end");/*跳片尾时间*/
//                String play_jump = config.optString("play_jump");/*跳片头时间*/
//                int timeout = config.optInt("Timeout");/*解析超时时间*/
//                int login_type = config.optInt("login_type");/*登录类型*/
//                String exit_Message = config.optString("Exit_Message");/*退出消息*/
//                String client = config.optString("Client");/*解析客户端地址*/
//                String base_host = config.optString("Base_host");/*苹果文件名*/
//                int ijk_log_debug = config.optInt("Ijk_log_debug");/*播放器debug日志*/
//                int fb_type = config.optInt("Fb_type");/*反馈类型*/
//                int vod_notice_starting_time = config.optInt("Vod_Notice_starting_time");/*视频跑马公告启动时间*/
//                int vod_notice_end_time = config.optInt("Vod_Notice_end_time");/*视频跑马公告停留时间*/
//                String logo_url = config.optString("Logo_url");/*远程logo地址*/
//                int episodesnumber = config.optInt("EpisodesNumber");/*剧集数量*/
//                int vod_logo = config.optInt("vod_Logo");/*视频logo*/
//                int trystate = config.optInt("Trystate");/*试看状态*/
//                int topic = config.optInt("Topic");/*专题状态*/
//                int auto_source = config.optInt("Auto_Source");/*自动换源*/
//                int sniff_debug_mode = config.optInt("Sniff_debug_mode");/*嗅探调试模式*/
//                int navigation_mode = config.optInt("Navigation_mode");/*导航模式*/
//                int play_timeout_debug = config.optInt("Play_timeout_debug");/*播放时间调试模式*/
//                int cornerLabelView = config.optInt("CornerLabelView");/*角标模式*/
//                interface_style = config.optInt("Interface_Style");/*界面样式*/
//                allow_changing_styles = config.optInt("Allow_changing_styles");/*允许用户更改主题*/
//                int force_Style = config.optInt("Force_Style");/*强制使用主题*/
//                if (force_Style == 1){
//                    interface_style = config.optInt("Interface_Style");/*界面样式*/
//                    allow_changing_styles = 0;/*不允许更改主题*/
//                }else{
//                    if (SharePreferenceDataUtil.getSharedStringData(SplashActivity.this, "User_Style", null) != null){
//                        interface_style = Integer.parseInt(SharePreferenceDataUtil.getSharedStringData(SplashActivity.this, "User_Style", null));/*界面样式*/
//                    }
//                }
//                String pwd_text = config.optString("Pwd_text");/*类目提示语*/
//                int login_control = config.optInt("Login_control");/*登录控制*/
//                int vpn_check = config.optInt("Vpn_check");/*Vpn及抓包检测*/
//                int xp_check = config.optInt("Xp_check");/*Xp环境检测*/
//                String verifysign = config.optString("Verifysign");/*签名md5*/
//                int verifysign_check = config.optInt("Verifysign_check");/*Xp环境检测*/
//                String name = config.optString("Name");/*应用名称*/
//                int name_check = config.optInt("Name_check");/*名称检测*/
//                int settings_page = config.optInt("Settings_page");/*四格配置*/
//                int updatenumber = config.optInt("UpdateNumber");/*更新集数*/
//                int caton_check = config.optInt("caton_check");/*卡顿校验*/
//                int check_time = config.optInt("check_time");/*卡顿校验时间*/
//                int Tackle_mode = config.optInt("Tackle_mode");/*卡顿解决方式*/
//                int networkspeed = config.optInt("networkspeed");/*显示网速*/
//                int epg = config.optInt("epg");/*EPG风格*/
//                int switchs = config.optInt("switchs");/*换台方式*/
//                int memory_source = config.optInt("memory_source");/*记忆播放源*/
//                int memory_channel = config.optInt("memory_channel");/*记忆频道*/
//                int framedrop = config.optInt("framedrop");/*高负载丢帧*/
//                int start_on_prepared = config.optInt("start_on_prepared");/*自动播放片源*/
//                int async_init_decoder = config.optInt("async_init_decoder");/*异步解码*/
//                int no_time_adjust = config.optInt("no_time_adjust");/*真实视频时间*/
//                int infbuf = config.optInt("infbuf");/*无限缓冲*/
//                int vod_caton_check = config.optInt("vod_caton_check");/*卡顿换源校验*/
//                int seizing_time = config.optInt("seizing_time");/*卡顿换源校验时间*/
//                int live_no_source = config.optInt("live_no_source");/*源用尽处理方式*/
//                int live = config.optInt("live");/*直播功能*/
//                int core_mode = config.optInt("core_mode");/*指定内核功能*/
//                int Adblock = config.optInt("Adblock");/*广告遮挡*/
//                int xuanjitype = config.optInt("xuanjitype");/*选集排序方式*/
//                int xuanjinumber = config.optInt("xuanjinumber");/*每组多少集*/
//                int reverse = config.optInt("reverse");/*换台键反转*/
//                int remember_source = config.optInt("remember_source");/*播放线路记忆*/
//                int Same_source_search = config.optInt("Same_source_search");/*同源搜索*/
//                int search = config.optInt("search");/*远程搜索*/
//                int searchport = config.optInt("searchport");/*远程搜索端口*/


//                String ad_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.Ad_url),Constant.c);/*广告地址*/
//                String user_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.s),Constant.c);/*易如意地址*/
//                String appkey = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.Appkey),Constant.c);/*APP密钥*/
//                String rc4key = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.Rc4key),Constant.c);/*rc4密钥*/
//                String mi_rsa_public_key = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.mi_rsa_public_key),Constant.c);/*rsa公钥*/
//                //String mi_rsa_private_key = AES.decrypt_Aes(Constant.b,jSONObject.optString("mi_rsa_private_key"),Constant.c);/*rsa私钥*/
//                String mi_aes_key = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.mi_aes_key),Constant.c);/*aes密钥*/
//                String mi_aes_iv = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.mi_aes_iv),Constant.c);/*aesiv*/
//                String api_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.bs),Constant.c);/*苹果地址*/
//                String apikey = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.ApiKey),Constant.c);/*苹果密钥*/
//                String fb_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.Fb_url),Constant.c);/*反馈地址*/
//                JSONObject config = jSONObject.getJSONObject(Constant.Config);/*配置信息*/
//                String play_core = config.optString(Constant.play_core);/*播放器内核*/
//                String live_core = config.optString(Constant.live_core);/*直播播放器内核*/
//                String play_decode = config.optString(Constant.play_decode);/*显示初装播放器解码*/
//                String play_ratio = config.optString(Constant.play_ratio);/*初装画面比例*/
//                int mi_type = config.optInt(Constant.mi_type);/*易如意加密类型*/
//                int packet_buffering = config.optInt(Constant.packet_buffering);/*启用缓冲进度条*/
//                int videotimeout = config.optInt(Constant.videotimeout);/*视频连接超时跳帧*/
//                int http_detect_range_support = config.optInt(Constant.http_detect_range_support);/*播放资源是否支持续传*/
//                int reconnect = config.optInt(Constant.reconnect);/*资源是否断线重连*/
//                int resources_reconnect = config.optInt(Constant.resources_reconnect);/*资源断线重连次数*/
//                int live_streaming = config.optInt(Constant.live_streaming);/*直播流媒体优化*/
//                int skip_loop_filter = config.optInt(Constant.skip_loop_filter);/*画面质量*/
//                int home_text_shadow = config.optInt(Constant.Home_text_shadow);/*热门推荐文字及阴影*/
//                int ad_time = config.optInt(Constant.Ad_time);/*开屏广告时间*/
//                int ad_jump = config.optInt(Constant.Ad_jump);/*跳过广告*/
//                int trytime = config.optInt(Constant.Trytime);/*试看时间*/
//                int submission_method = config.optInt(Constant.Submission_method);/*解析读取提交方式*/
//                String play_jump_end = config.optString(Constant.play_jump_end);/*跳片尾时间*/
//                String play_jump = config.optString(Constant.play_jump);/*跳片头时间*/
//                int timeout = config.optInt(Constant.Timeout);/*解析超时时间*/
//                int login_type = config.optInt(Constant.login_type);/*登录类型*/
//                String exit_Message = config.optString(Constant.Exit_Message);/*退出消息*/
//                String client = config.optString(Constant.Client);/*解析客户端地址*/
//                String base_host = config.optString(Constant.Base_host);/*苹果文件名*/
//                int ijk_log_debug = config.optInt(Constant.Ijk_log_debug);/*播放器debug日志*/
//                int fb_type = config.optInt(Constant.Fb_type);/*反馈类型*/
//                int vod_notice_starting_time = config.optInt(Constant.Vod_Notice_starting_time);/*视频跑马公告启动时间*/
//                int vod_notice_end_time = config.optInt(Constant.Vod_Notice_end_time);/*视频跑马公告停留时间*/
//                String logo_url = config.optString(Constant.Logo_url);/*远程logo地址*/
//                int episodesnumber = config.optInt(Constant.EpisodesNumber);/*剧集数量*/
//                int vod_logo = config.optInt(Constant.vod_Logo);/*视频logo*/
//                int trystate = config.optInt(Constant.Trystate);/*试看状态*/
//                int topic = config.optInt(Constant.Topic);/*专题状态*/
//                int auto_source = config.optInt(Constant.Auto_Source);/*自动换源*/
//                int sniff_debug_mode = config.optInt(Constant.Sniff_debug_mode);/*嗅探调试模式*/
//                int navigation_mode = config.optInt(Constant.Navigation_mode);/*导航模式*/
//                int play_timeout_debug = config.optInt(Constant.Play_timeout_debug);/*播放时间调试模式*/
//                int cornerLabelView = config.optInt(Constant.CornerLabelView);/*角标模式*/
//                interface_style = config.optInt(Constant.Interface_Style);/*界面样式*/
//                allow_changing_styles = config.optInt(Constant.Allow_changing_styles);/*允许用户更改主题*/
//                int force_Style = config.optInt(Constant.Force_Style);/*强制使用主题*/
//                if (force_Style == 1){
//                    interface_style = config.optInt(Constant.Interface_Style);/*界面样式*/
//                    allow_changing_styles = 0;/*不允许更改主题*/
//                }else{
//                    if (SharePreferenceDataUtil.getSharedStringData(SplashActivity.this, Constant.User_Style, null) != null){
//                        interface_style = Integer.parseInt(SharePreferenceDataUtil.getSharedStringData(SplashActivity.this, "User_Style", null));/*界面样式*/
//                    }
//                }
//                String pwd_text = config.optString(Constant.Pwd_text);/*类目提示语*/
//                int login_control = config.optInt(Constant.Login_control);/*登录控制*/
//                int vpn_check = config.optInt(Constant.Vpn_check);/*Vpn及抓包检测*/
//                int xp_check = config.optInt(Constant.Xp_check);/*Xp环境检测*/
//                String verifysign = config.optString(Constant.Verifysign);/*签名md5*/
//                int verifysign_check = config.optInt(Constant.Verifysign_check);/*Xp环境检测*/
//                String name = config.optString(Constant.Name);/*应用名称*/
//                int name_check = config.optInt(Constant.Name_check);/*名称检测*/
//                int settings_page = config.optInt(Constant.Settings_page);/*四格配置*/
//                int updatenumber = config.optInt(Constant.UpdateNumber);/*更新集数*/
//                int caton_check = config.optInt(Constant.caton_check);/*卡顿校验*/
//                int check_time = config.optInt(Constant.check_time);/*卡顿校验时间*/
//                int Tackle_mode = config.optInt(Constant.Tackle_mode);/*卡顿解决方式*/
//                int networkspeed = config.optInt(Constant.networkspeed);/*显示网速*/
//                int epg = config.optInt(Constant.epg);/*EPG风格*/
//                int switchs = config.optInt(Constant.switchs);/*换台方式*/
//                int memory_source = config.optInt(Constant.memory_source);/*记忆播放源*/
//                int memory_channel = config.optInt(Constant.memory_channel);/*记忆频道*/
//                int framedrop = config.optInt(Constant.framedrop);/*高负载丢帧*/
//                int start_on_prepared = config.optInt(Constant.start_on_prepared);/*自动播放片源*/
//                int async_init_decoder = config.optInt(Constant.async_init_decoder);/*异步解码*/
//                int no_time_adjust = config.optInt(Constant.no_time_adjust);/*真实视频时间*/
//                int infbuf = config.optInt(Constant.infbuf);/*无限缓冲*/
//                int vod_caton_check = config.optInt(Constant.vod_caton_check);/*卡顿换源校验*/
//                int seizing_time = config.optInt(Constant.seizing_time);/*卡顿换源校验时间*/
//                int live_no_source = config.optInt(Constant.live_no_source);/*源用尽处理方式*/
//                int live = config.optInt(Constant.live);/*直播功能*/
//                int core_mode = config.optInt(Constant.core_mode);/*指定内核功能*/
//                int Adblock = config.optInt(Constant.Adblock);/*广告遮挡*/
//                int xuanjitype = config.optInt(Constant.xuanjitype);/*选集排序方式*/
//                int xuanjinumber = config.optInt(Constant.xuanjinumber);/*每组多少集*/
//                int reverse = config.optInt(Constant.reverse);/*换台键反转*/
//                int remember_source = config.optInt(Constant.remember_source);/*播放线路记忆*/
//                int Same_source_search = config.optInt(Constant.Same_source_search);/*同源搜索*/
//                int search = config.optInt(Constant.search);/*远程搜索*/
//                int searchport = config.optInt(Constant.searchport);/*远程搜索端口*/



                String ad_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.kg),Constant.c);/*广告地址*/
                String user_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.s),Constant.c);/*易如意地址*/
                String appkey = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.yk),Constant.c);/*APP密钥*/
                String rc4key = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.kd),Constant.c);/*rc4密钥*/
                String mi_rsa_public_key = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.tb),Constant.c);/*rsa公钥*/
                //String mi_rsa_private_key = AES.decrypt_Aes(Constant.b,jSONObject.optString("mi_rsa_private_key"),Constant.c);/*rsa私钥*/
                String mi_aes_key = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.um),Constant.c);/*aes密钥*/
                String mi_aes_iv = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.im),Constant.c);/*aesiv*/
                String api_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.bs),Constant.c);/*苹果地址*/
                String apikey = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.pd),Constant.c);/*苹果密钥*/
                String fb_url = AES.decrypt_Aes(Constant.b,jSONObject.optString(Constant.ot),Constant.c);/*反馈地址*/
                JSONObject config = jSONObject.getJSONObject(Constant.cz);/*配置信息*/
                String play_core = config.optString(Constant.hd);/*播放器内核*/
                String live_core = config.optString(Constant.vy);/*直播播放器内核*/
                String play_decode = config.optString(Constant.mg);/*显示初装播放器解码*/
                String play_ratio = config.optString(Constant.oh);/*初装画面比例*/
                int mi_type = config.optInt(Constant.ue);/*易如意加密类型*/
                int packet_buffering = config.optInt(Constant.mt);/*启用缓冲进度条*/
                int videotimeout = config.optInt(Constant.mJ);/*视频连接超时跳帧*/
                int http_detect_range_support = config.optInt(Constant.YK);/*播放资源是否支持续传*/
                int reconnect = config.optInt(Constant.kV);/*资源是否断线重连*/
                int resources_reconnect = config.optInt(Constant.Me);/*资源断线重连次数*/
                int live_streaming = config.optInt(Constant.Hs);/*直播流媒体优化*/
                int skip_loop_filter = config.optInt(Constant.nR);/*画面质量*/
                int home_text_shadow = config.optInt(Constant.Hu);/*热门推荐文字及阴影*/
                int ad_time = config.optInt(Constant.ke);/*开屏广告时间*/
                int ad_jump = config.optInt(Constant.od);/*跳过广告*/
                int trytime = config.optInt(Constant.gD);/*试看时间*/
                int submission_method = config.optInt(Constant.JE);/*解析读取提交方式*/
                String play_jump_end = config.optString(Constant.LS);/*跳片尾时间*/
                String play_jump = config.optString(Constant.mT);/*跳片头时间*/
                int timeout = config.optInt(Constant.WM);/*解析超时时间*/
                int login_type = config.optInt(Constant.MR);/*登录类型*/
                String exit_Message = config.optString(Constant.MU);/*退出消息*/
                String client = config.optString(Constant.Nr);/*解析客户端地址*/
                String base_host = config.optString(Constant.eg);/*苹果文件名*/
                int ijk_log_debug = config.optInt(Constant.of);/*播放器debug日志*/
                int fb_type = config.optInt(Constant.fb);/*反馈类型*/
                int vod_notice_starting_time = config.optInt(Constant.lf);/*视频跑马公告启动时间*/
                int vod_notice_end_time = config.optInt(Constant.Fi);/*视频跑马公告停留时间*/
                String logo_url = config.optString(Constant.yv);/*远程logo地址*/
                int episodesnumber = config.optInt(Constant.sd);/*剧集数量*/
                int vod_logo = config.optInt(Constant.kF);/*视频logo*/
                int trystate = config.optInt(Constant.wj);/*试看状态*/
                int topic = config.optInt(Constant.nF);/*专题状态*/
                int auto_source = config.optInt(Constant.Jv);/*自动换源*/
                int sniff_debug_mode = config.optInt(Constant.mr);/*嗅探调试模式*/
                int navigation_mode = config.optInt(Constant.bw);/*导航模式*/
                int play_timeout_debug = config.optInt(Constant.Fg);/*播放时间调试模式*/
                int cornerLabelView = config.optInt(Constant.fw);/*角标模式*/
                interface_style = config.optInt(Constant.hw, 4);/*界面样式，默认无界UI*/
                allow_changing_styles = config.optInt(Constant.aF);/*允许用户更改主题*/
                int force_Style = config.optInt(Constant.un);/*强制使用主题*/
                if (force_Style == 1){
                    interface_style = config.optInt(Constant.hw, 4);/*界面样式*/
                    allow_changing_styles = 0;/*不允许更改主题*/
                }else{
                    // 用户设置优先，否则使用无界UI(4)作为默认
                    String userStyle = SharePreferenceDataUtil.getSharedStringData(SplashActivity.this, "User_Style", null);
                    if (userStyle != null){
                        interface_style = Integer.parseInt(userStyle);/*用户选择的界面样式*/
                    } else {
                        interface_style = 4;/*默认无界UI*/
                    }
                }
                String pwd_text = config.optString(Constant.sb);/*类目提示语*/
                int login_control = config.optInt(Constant.jw);/*登录控制*/
                int vpn_check = config.optInt(Constant.vu);/*Vpn及抓包检测*/
                int xp_check = config.optInt(Constant.ft);/*Xp环境检测*/
                String verifysign = config.optString(Constant.gw);/*签名md5*/
                int verifysign_check = config.optInt(Constant.gq);/*Xp环境检测*/
                String name = config.optString(Constant.uv);/*应用名称*/
                int name_check = config.optInt(Constant.ac);/*名称检测*/
                int settings_page = config.optInt(Constant.fq);/*四格配置*/
                int updatenumber = config.optInt(Constant.fy);/*更新集数*/
                int caton_check = config.optInt(Constant.th);/*卡顿校验*/
                int check_time = config.optInt(Constant.jd);/*卡顿校验时间*/
                int Tackle_mode = config.optInt(Constant.yx);/*卡顿解决方式*/
                int networkspeed = config.optInt(Constant.id);/*显示网速*/
                int epg = config.optInt(Constant.user);/*EPG风格*/
                int switchs = config.optInt(Constant.gid);/*换台方式*/
                int memory_source = config.optInt(Constant.yms);/*记忆播放源*/
                int memory_channel = config.optInt(Constant.oi);/*记忆频道*/
                int framedrop = config.optInt(Constant.vgs);/*高负载丢帧*/
                int start_on_prepared = config.optInt(Constant.gt);/*自动播放片源*/
                int async_init_decoder = config.optInt(Constant.ufe);/*异步解码*/
                int no_time_adjust = config.optInt(Constant.jda);/*真实视频时间*/
                int infbuf = config.optInt(Constant.oge);/*无限缓冲*/
                int vod_caton_check = config.optInt(Constant.guda);/*卡顿换源校验*/
                int seizing_time = config.optInt(Constant.oftn);/*卡顿换源校验时间*/
                int live_no_source = config.optInt(Constant.ykf);/*源用尽处理方式*/
                int live = config.optInt(Constant.ogy);/*直播功能*/
                int core_mode = config.optInt(Constant.ldb);/*指定内核功能*/
                int Adblock = config.optInt(Constant.ykfa);/*广告遮挡*/
                int xuanjitype = config.optInt(Constant.gag);/*选集排序方式*/
                int xuanjinumber = config.optInt(Constant.vys);/*每组多少集*/
                int reverse = config.optInt(Constant.gida);/*换台键反转*/
                int remember_source = config.optInt(Constant.ogsv);/*播放线路记忆*/
                int Same_source_search = config.optInt(Constant.otcb);/*同源搜索*/
                int search = config.optInt(Constant.hym);/*远程搜索*/
                int searchport = config.optInt(Constant.vtk);/*远程搜索端口*/





                /*设置广告时间*/
                tv_ad_time = ad_time;/*设置时间*/
                tv_jump_time.setText(String.valueOf(ad_time));/*显示倒计时*/
                /*设置广告地址*/
                imgurl = ad_url;//广告地址
                /*设置跳过广告可见*/
                if (ad_jump == 0){
                    tv_jump_id.setVisibility(View.VISIBLE);
                }
                /*初装解码*/
                if (play_decode.equals("硬解码")) {
                    mishwdecode = 1;
                } else {
                    mishwdecode = 0;
                }
                sp.edit()
//                        .putString("User_url", Rc4.encry_RC4_string(user_url,Constant.d))/*Rc4加密 易如意地址 ---1*/
//                        .putString("Appkey", Rc4.encry_RC4_string(appkey,Constant.d))/*Rc4加密 易如意APP密钥*/
//                        .putString("Rc4key", Rc4.encry_RC4_string(rc4key,Constant.d))/*Rc4加密 易如意Rc4密钥 ---1*/
//                        .putString("mi_rsa_public_key", Rc4.encry_RC4_string(mi_rsa_public_key,Constant.d))/*rsa加密 易如意rsa公钥 ---1*/
//                        //.putString("mi_rsa_private_key", Rc4.encry_RC4_string(mi_rsa_private_key,Constant.d))/*rsa加密 易如意rsa私钥 ---1*/
//                        .putString("mi_aes_key", Rc4.encry_RC4_string(mi_aes_key,Constant.d))/*Rc4加密 易如意aes密钥 ---1*/
//                        .putString("mi_aes_iv", Rc4.encry_RC4_string(mi_aes_iv,Constant.d))/*Rc4加密 易如意aesic ---1*/
//                        .putString("Api_url", Rc4.encry_RC4_string(api_url,Constant.d))/*Rc4加密 苹果地址 ---1*/
//                        .putString("ApiKey", Rc4.encry_RC4_string(apikey,Constant.d))/*Rc4加密 苹果密钥 ---1*/
//                        .putString("Fb_url", Rc4.encry_RC4_string(fb_url,Constant.d))/*Rc4加密 反馈地址 ---1*/
//                        .putString("play_core", Rc4.encry_RC4_string(play_core,Constant.d))/*Rc4加密 播放器内核 ---1*/
//                        .putString("live_core", Rc4.encry_RC4_string(live_core,Constant.d))/*Rc4加密 播放器内核 ---1*/
//                        .putString("play_decode", Rc4.encry_RC4_string(play_decode,Constant.d))/*Rc4加密 初装显示解码 ---1*/
//                        .putInt("mIsHwDecode", mishwdecode)/*使用初装解码 ---1*/
//                        .putString("play_ratio", Rc4.encry_RC4_string(play_ratio,Constant.d))/*Rc4加密 初装画面比例 ---1*/
//                        .putInt("mi_type", mi_type)/*易如意加密类型 ---1*/
//                        .putInt("packet_buffering", packet_buffering)/*显示缓冲进度条 ---1*/
//                        .putInt("videotimeout", videotimeout)/*视频资源超时跳帧 ---1*/
//                        .putInt("http_detect_range_support", http_detect_range_support)/*检查视频资源断点续传 ---1*/
//                        .putInt("reconnect", reconnect)/*资源重试 ---1*/
//                        .putInt("resources_reconnect", resources_reconnect)/*资源重试次数 ---1*/
//                        .putInt("live_streaming", live_streaming)/*直播流媒体优化 ---1*/
//                        .putInt("skip_loop_filter", skip_loop_filter)/*画面质量 ---1*/
//                        .putInt("Home_text_shadow", home_text_shadow)/*热门推荐文字及阴影 ---1*/
//                        .putInt("Trytime", trytime)/*试看时间 ---1*/
//                        .putInt("Submission_method", submission_method)/*Rc4加密解析提交方式 ---1*/
//                        .putString("play_jump_end", Rc4.encry_RC4_string(play_jump_end,Constant.d))/*Rc4加密 跳片尾时间 ---1*/
//                        .putString("play_jump", Rc4.encry_RC4_string(play_jump,Constant.d))/*Rc4加密 跳头尾时间 ---1*/
//                        .putInt("Timeout", timeout)/*解析超时时间 ---1*/
//                        .putInt("login_type", login_type)/*登录类型 ---1*/
//                        .putString("Exit_Message", Rc4.encry_RC4_string(exit_Message,Constant.d) )/*Rc4加密 远程退出消息 ---1*/
//                        .putString("Client", Rc4.encry_RC4_string(client,Constant.d) )/*Rc4加密 解析客户端地址 ---1*/
//                        .putString("BASE_HOST", Rc4.encry_RC4_string(base_host,Constant.d) )/*苹果文件名 ---1*/
//                        .putInt("Ijk_log_debug", ijk_log_debug)/*播放器debug日志 ---1*/
//                        .putInt("Fb_type", fb_type)/*反馈类型 ---1*/
//                        .putInt("Vod_Notice_starting_time", vod_notice_starting_time)/*视频跑马公告启动时间 ---1*/
//                        .putInt("Vod_Notice_end_time", vod_notice_end_time)/*视频跑马公告停留时间 ---1*/
//                        .putString("Logo_url", Rc4.encry_RC4_string(logo_url,Constant.d))/*远程logo地址 ---1*/
//                        .putInt("EpisodesNumber", episodesnumber)/*剧集数量显示 ---1*/
//                        .putInt("vod_Logo", vod_logo)/*视频logo ---1*/
//                        .putInt("Trystate", trystate)/*试看状态 ---1*/
//                        .putInt("Topic", topic)/*专题状态 ---1*/
//                        .putInt("Auto_Source", auto_source)/*自动换源 ---1*/
//                        .putInt("Sniff_debug_mode", sniff_debug_mode)/*嗅探调试模式 ---1*/
//                        .putInt("Navigation_mode", navigation_mode)/*导航模式 ---1*/
//                        .putInt("Play_timeout_debug", play_timeout_debug)/*播放超时调试模式 ---1*/
//                        .putInt("CornerLabelView", cornerLabelView)/*角标模式 ---1*/
//                        .putInt("Interface_Style", interface_style)/*界面样式 ---1*/
//                        .putInt("Allow_changing_styles", allow_changing_styles)/*允许用户更改主题 ---1*/
//                        .putString("Pwd_text", Rc4.encry_RC4_string(pwd_text,Constant.d))/*类目提示语 ---1*/
//                        .putInt("Login_control", login_control)/*登录控制 ---1*/
//                        .putInt("Vpn_check", vpn_check)/*Vpn及抓包检测 ---1*/
//                        .putInt("Xp_check", xp_check)/*Xp环境检测 ---1*/
//                        .putString("Verifysign", Rc4.encry_RC4_string(verifysign,Constant.d))/*Rc4加密 签名md5 ---1*/
//                        .putInt("Verifysign_check", verifysign_check)/*签名检测 ---1*/
//                        .putString("Name", Rc4.encry_RC4_string(name,Constant.d))/*应用名称---1*/
//                        .putInt("Name_check", name_check)/*检查应用名称 ---1*/
//                        .putString("Authorization", Rc4.encry_RC4_string(Authorization,Constant.d))/*应用名称---1*/
//                        .putInt("Settings_page", settings_page)/*设置页四格 ---1*/
//                        .putInt("UpdateNumber", updatenumber)/*更新集数 ---1*/
//                        .putInt("caton_check", caton_check)/*卡顿校验 ---1*/
//                        .putInt("check_time", check_time)/*卡顿校验时间 ---1*/
//                        .putInt("Tackle_mode", Tackle_mode)/*卡顿解决方式 ---1*/
//                        .putInt("networkspeed", networkspeed)/*显示网速 ---1*/
//                        .putInt("epg", epg)/*EPG风格 ---1*/
//                        .putInt("switchs", switchs)/*换台方式 ---1*/
//                        .putInt("memory_source", memory_source)/*记忆播放源 ---1*/
//                        .putInt("memory_channel", memory_channel)/*记忆频道 ---1*/
//                        .putInt("framedrop", framedrop)/*高负载丢帧 ---1*/
//                        .putInt("start_on_prepared", start_on_prepared)/*自动播放片源 ---1*/
//                        .putInt("async_init_decoder", async_init_decoder)/*异步解码 ---1*/
//                        .putInt("no_time_adjust", no_time_adjust)/*真实视频时间 ---1*/
//                        .putInt("infbuf", infbuf)/*无限缓冲 ---1*/
//                        .putInt("vod_caton_check", vod_caton_check)/*卡顿换源校验 ---1*/
//                        .putInt("seizing_time", seizing_time)/*卡顿换源校验时间 ---1*/
//                        .putInt("live_no_source", live_no_source)/*源用尽处理方式 ---1*/
//                        .putInt("live", live)/*直播功能 ---1*/
//                        .putInt("core_mode", core_mode)/*指定内核功能 ---1*/
//                        .putInt("Adblock", Adblock)/*广告遮挡 ---1*/
//                        .putInt("xuanjitype", xuanjitype)/*选集排序方式 ---1*/
//                        .putInt("xuanjinumber", xuanjinumber)/*每组多少集 ---1*/
//                        .putInt("reverse", reverse)/*换台键反转 ---1*/
//                        .putInt("remember_source", remember_source)/*播放线路记忆 ---1*/
//                        .putInt("Same_source_search", Same_source_search)/*同源搜索 ---1*/
//                        .putInt("search", search)/*远程搜索 ---1*/
//                        .putInt("searchport", searchport)/*远程搜索端口 ---1*/



//                        .putString(Constant.s, Rc4.encry_RC4_string(user_url,Constant.d))/*Rc4加密 易如意地址 ---1*/
//                        .putString(Constant.Appkey, Rc4.encry_RC4_string(appkey,Constant.d))/*Rc4加密 易如意APP密钥*/
//                        .putString(Constant.Rc4key, Rc4.encry_RC4_string(rc4key,Constant.d))/*Rc4加密 易如意Rc4密钥 ---1*/
//                        .putString(Constant.mi_rsa_public_key, Rc4.encry_RC4_string(mi_rsa_public_key,Constant.d))/*rsa加密 易如意rsa公钥 ---1*/
//                        //.putString("mi_rsa_private_key", Rc4.encry_RC4_string(mi_rsa_private_key,Constant.d))/*rsa加密 易如意rsa私钥 ---1*/
//                        .putString(Constant.mi_aes_key, Rc4.encry_RC4_string(mi_aes_key,Constant.d))/*Rc4加密 易如意aes密钥 ---1*/
//                        .putString(Constant.mi_aes_iv, Rc4.encry_RC4_string(mi_aes_iv,Constant.d))/*Rc4加密 易如意aesic ---1*/
//                        .putString(Constant.bs, Rc4.encry_RC4_string(api_url,Constant.d))/*Rc4加密 苹果地址 ---1*/
//                        .putString(Constant.ApiKey, Rc4.encry_RC4_string(apikey,Constant.d))/*Rc4加密 苹果密钥 ---1*/
//                        .putString(Constant.Fb_url, Rc4.encry_RC4_string(fb_url,Constant.d))/*Rc4加密 反馈地址 ---1*/
//                        .putString(Constant.play_core, Rc4.encry_RC4_string(play_core,Constant.d))/*Rc4加密 播放器内核 ---1*/
//                        .putString(Constant.live_core, Rc4.encry_RC4_string(live_core,Constant.d))/*Rc4加密 直播播放器内核 ---1*/
//                        .putString(Constant.play_decode, Rc4.encry_RC4_string(play_decode,Constant.d))/*Rc4加密 初装显示解码 ---1*/
//                        .putInt(Constant.mIsHwDecode, mishwdecode)/*使用初装解码 ---1*/
//                        .putString(Constant.play_ratio, Rc4.encry_RC4_string(play_ratio,Constant.d))/*Rc4加密 初装画面比例 ---1*/
//                        .putInt(Constant.mi_type, mi_type)/*易如意加密类型 ---1*/
//                        .putInt(Constant.packet_buffering, packet_buffering)/*显示缓冲进度条 ---1*/
//                        .putInt(Constant.videotimeout, videotimeout)/*视频资源超时跳帧 ---1*/
//                        .putInt(Constant.http_detect_range_support, http_detect_range_support)/*检查视频资源断点续传 ---1*/
//                        .putInt(Constant.reconnect, reconnect)/*资源重试 ---1*/
//                        .putInt(Constant.resources_reconnect, resources_reconnect)/*资源重试次数 ---1*/
//                        .putInt(Constant.live_streaming, live_streaming)/*直播流媒体优化 ---1*/
//                        .putInt(Constant.skip_loop_filter, skip_loop_filter)/*画面质量 ---1*/
//                        .putInt(Constant.Home_text_shadow, home_text_shadow)/*热门推荐文字及阴影 ---1*/
//                        .putInt(Constant.Trytime, trytime)/*试看时间 ---1*/
//                        .putInt(Constant.Submission_method, submission_method)/*Rc4加密解析提交方式 ---1*/
//                        .putString(Constant.play_jump_end, Rc4.encry_RC4_string(play_jump_end,Constant.d))/*Rc4加密 跳片尾时间 ---1*/
//                        .putString(Constant.play_jump, Rc4.encry_RC4_string(play_jump,Constant.d))/*Rc4加密 跳头尾时间 ---1*/
//                        .putInt(Constant.Timeout, timeout)/*解析超时时间 ---1*/
//                        .putInt(Constant.login_type, login_type)/*登录类型 ---1*/
//                        .putString(Constant.Exit_Message, Rc4.encry_RC4_string(exit_Message,Constant.d) )/*Rc4加密 远程退出消息 ---1*/
//                        .putString(Constant.Client, Rc4.encry_RC4_string(client,Constant.d) )/*Rc4加密 解析客户端地址 ---1*/
//                        .putString(Constant.BASE_HOST, Rc4.encry_RC4_string(base_host,Constant.d) )/*苹果文件名 ---1*/
//                        .putInt(Constant.Ijk_log_debug, ijk_log_debug)/*播放器debug日志 ---1*/
//                        .putInt(Constant.Fb_type, fb_type)/*反馈类型 ---1*/
//                        .putInt(Constant.Vod_Notice_starting_time, vod_notice_starting_time)/*视频跑马公告启动时间 ---1*/
//                        .putInt(Constant.Vod_Notice_end_time, vod_notice_end_time)/*视频跑马公告停留时间 ---1*/
//                        .putString(Constant.Logo_url, Rc4.encry_RC4_string(logo_url,Constant.d))/*远程logo地址 ---1*/
//                        .putInt(Constant.EpisodesNumber, episodesnumber)/*剧集数量显示 ---1*/
//                        .putInt(Constant.vod_Logo, vod_logo)/*视频logo ---1*/
//                        .putInt(Constant.Trystate, trystate)/*试看状态 ---1*/
//                        .putInt(Constant.Topic, topic)/*专题状态 ---1*/
//                        .putInt(Constant.Auto_Source, auto_source)/*自动换源 ---1*/
//                        .putInt(Constant.Sniff_debug_mode, sniff_debug_mode)/*嗅探调试模式 ---1*/
//                        .putInt(Constant.Navigation_mode, navigation_mode)/*导航模式 ---1*/
//                        .putInt(Constant.Play_timeout_debug, play_timeout_debug)/*播放超时调试模式 ---1*/
//                        .putInt(Constant.CornerLabelView, cornerLabelView)/*角标模式 ---1*/
//                        .putInt(Constant.Interface_Style, interface_style)/*界面样式 ---1*/
//                        .putInt(Constant.Allow_changing_styles, allow_changing_styles)/*允许用户更改主题 ---1*/
//                        .putString(Constant.Pwd_text, Rc4.encry_RC4_string(pwd_text,Constant.d))/*类目提示语 ---1*/
//                        .putInt(Constant.Login_control, login_control)/*登录控制 ---1*/
//                        .putInt(Constant.Vpn_check, vpn_check)/*Vpn及抓包检测 ---1*/
//                        .putInt(Constant.Xp_check, xp_check)/*Xp环境检测 ---1*/
//                        .putString(Constant.Verifysign, Rc4.encry_RC4_string(verifysign,Constant.d))/*Rc4加密 签名md5 ---1*/
//                        .putInt(Constant.Verifysign_check, verifysign_check)/*签名检测 ---1*/
//                        .putString(Constant.Name, Rc4.encry_RC4_string(name,Constant.d))/*应用名称---1*/
//                        .putInt(Constant.Name_check, name_check)/*检查应用名称 ---1*/
//                        .putString(Constant.Y, Rc4.encry_RC4_string(Authorization,Constant.d))/*应用名称---1*/
//                        .putInt(Constant.Settings_page, settings_page)/*设置页四格 ---1*/
//                        .putInt(Constant.UpdateNumber, updatenumber)/*更新集数 ---1*/
//                        .putInt(Constant.caton_check, caton_check)/*卡顿校验 ---1*/
//                        .putInt(Constant.check_time, check_time)/*卡顿校验时间 ---1*/
//                        .putInt(Constant.Tackle_mode, Tackle_mode)/*卡顿解决方式 ---1*/
//                        .putInt(Constant.networkspeed, networkspeed)/*显示网速 ---1*/
//                        .putInt(Constant.epg, epg)/*EPG风格 ---1*/
//                        .putInt(Constant.switchs, switchs)/*换台方式 ---1*/
//                        .putInt(Constant.memory_source, memory_source)/*记忆播放源 ---1*/
//                        .putInt(Constant.memory_channel, memory_channel)/*记忆频道 ---1*/
//                        .putInt(Constant.framedrop, framedrop)/*高负载丢帧 ---1*/
//                        .putInt(Constant.start_on_prepared, start_on_prepared)/*自动播放片源 ---1*/
//                        .putInt(Constant.async_init_decoder, async_init_decoder)/*异步解码 ---1*/
//                        .putInt(Constant.no_time_adjust, no_time_adjust)/*真实视频时间 ---1*/
//                        .putInt(Constant.infbuf, infbuf)/*无限缓冲 ---1*/
//                        .putInt(Constant.vod_caton_check, vod_caton_check)/*卡顿换源校验 ---1*/
//                        .putInt(Constant.infbuf, seizing_time)/*卡顿换源校验时间 ---1*/
//                        .putInt(Constant.live_no_source, live_no_source)/*源用尽处理方式 ---1*/
//                        .putInt(Constant.live, live)/*直播功能 ---1*/
//                        .putInt(Constant.core_mode, core_mode)/*指定内核功能 ---1*/
//                        .putInt(Constant.Adblock, Adblock)/*广告遮挡 ---1*/
//                        .putInt(Constant.xuanjitype, xuanjitype)/*选集排序方式 ---1*/
//                        .putInt(Constant.xuanjinumber, xuanjinumber)/*每组多少集 ---1*/
//                        .putInt(Constant.reverse, reverse)/*换台键反转 ---1*/
//                        .putInt(Constant.remember_source, remember_source)/*播放线路记忆 ---1*/
//                        .putInt(Constant.Same_source_search, Same_source_search)/*同源搜索 ---1*/
//                        .putInt(Constant.search, search)/*远程搜索 ---1*/
//                        .putInt(Constant.searchport, searchport)/*远程搜索端口 ---1*/


                        .putString(Constant.s, Rc4.encry_RC4_string(user_url,Constant.d))/*Rc4加密 易如意地址 ---1*/
                        .putString(Constant.yk, Rc4.encry_RC4_string(appkey,Constant.d))/*Rc4加密 易如意APP密钥*/
                        .putString(Constant.kd, Rc4.encry_RC4_string(rc4key,Constant.d))/*Rc4加密 易如意Rc4密钥 ---1*/
                        .putString(Constant.tb, Rc4.encry_RC4_string(mi_rsa_public_key,Constant.d))/*rsa加密 易如意rsa公钥 ---1*/
                        //.putString("mi_rsa_private_key", Rc4.encry_RC4_string(mi_rsa_private_key,Constant.d))/*rsa加密 易如意rsa私钥 ---1*/
                        .putString(Constant.um, Rc4.encry_RC4_string(mi_aes_key,Constant.d))/*Rc4加密 易如意aes密钥 ---1*/
                        .putString(Constant.im, Rc4.encry_RC4_string(mi_aes_iv,Constant.d))/*Rc4加密 易如意aesic ---1*/
                        .putString(Constant.bs, Rc4.encry_RC4_string(api_url,Constant.d))/*Rc4加密 苹果地址 ---1*/
                        .putString(Constant.pd, Rc4.encry_RC4_string(apikey,Constant.d))/*Rc4加密 苹果密钥 ---1*/
                        .putString(Constant.ot, Rc4.encry_RC4_string(fb_url,Constant.d))/*Rc4加密 反馈地址 ---1*/
                        .putString(Constant.hd, Rc4.encry_RC4_string(play_core,Constant.d))/*Rc4加密 播放器内核 ---1*/
                        .putString(Constant.vy, Rc4.encry_RC4_string(live_core,Constant.d))/*Rc4加密 直播播放器内核 ---1*/
                        .putString(Constant.mg, Rc4.encry_RC4_string(play_decode,Constant.d))/*Rc4加密 初装显示解码 ---1*/
                        .putInt(Constant.dj, mishwdecode)/*使用初装解码 ---1*/
                        .putString(Constant.oh, Rc4.encry_RC4_string(play_ratio,Constant.d))/*Rc4加密 初装画面比例 ---1*/
                        .putInt(Constant.ue, mi_type)/*易如意加密类型 ---1*/
                        .putInt(Constant.mt, packet_buffering)/*显示缓冲进度条 ---1*/
                        .putInt(Constant.mJ, videotimeout)/*视频资源超时跳帧 ---1*/
                        .putInt(Constant.YK, http_detect_range_support)/*检查视频资源断点续传 ---1*/
                        .putInt(Constant.kV, reconnect)/*资源重试 ---1*/
                        .putInt(Constant.Me, resources_reconnect)/*资源重试次数 ---1*/
                        .putInt(Constant.Hs, live_streaming)/*直播流媒体优化 ---1*/
                        .putInt(Constant.nR, skip_loop_filter)/*画面质量 ---1*/
                        .putInt(Constant.Hu, home_text_shadow)/*热门推荐文字及阴影 ---1*/
                        .putInt(Constant.gD, trytime)/*试看时间 ---1*/
                        .putInt(Constant.JE, submission_method)/*Rc4加密解析提交方式 ---1*/
                        .putString(Constant.LS, Rc4.encry_RC4_string(play_jump_end,Constant.d))/*Rc4加密 跳片尾时间 ---1*/
                        .putString(Constant.mT, Rc4.encry_RC4_string(play_jump,Constant.d))/*Rc4加密 跳头尾时间 ---1*/
                        .putInt(Constant.WM, timeout)/*解析超时时间 ---1*/
                        .putInt(Constant.MR, login_type)/*登录类型 ---1*/
                        .putString(Constant.MU, Rc4.encry_RC4_string(exit_Message,Constant.d) )/*Rc4加密 远程退出消息 ---1*/
                        .putString(Constant.Nr, Rc4.encry_RC4_string(client,Constant.d) )/*Rc4加密 解析客户端地址 ---1*/
                        .putString(Constant.fn, Rc4.encry_RC4_string(base_host,Constant.d) )/*苹果文件名 ---1*/
                        .putInt(Constant.of, ijk_log_debug)/*播放器debug日志 ---1*/
                        .putInt(Constant.fb, fb_type)/*反馈类型 ---1*/
                        .putInt(Constant.lf, vod_notice_starting_time)/*视频跑马公告启动时间 ---1*/
                        .putInt(Constant.Fi, vod_notice_end_time)/*视频跑马公告停留时间 ---1*/
                        .putString(Constant.yv, Rc4.encry_RC4_string(logo_url,Constant.d))/*远程logo地址 ---1*/
                        .putInt(Constant.sd, episodesnumber)/*剧集数量显示 ---1*/
                        .putInt(Constant.kF, vod_logo)/*视频logo ---1*/
                        .putInt(Constant.wj, trystate)/*试看状态 ---1*/
                        .putInt(Constant.nF, topic)/*专题状态 ---1*/
                        .putInt(Constant.Jv, auto_source)/*自动换源 ---1*/
                        .putInt(Constant.mr, sniff_debug_mode)/*嗅探调试模式 ---1*/
                        .putInt(Constant.bw, navigation_mode)/*导航模式 ---1*/
                        .putInt(Constant.Fg, play_timeout_debug)/*播放超时调试模式 ---1*/
                        .putInt(Constant.fw, cornerLabelView)/*角标模式 ---1*/
                        .putInt(Constant.hw, interface_style)/*界面样式 ---1*/
                        .putInt(Constant.aF, allow_changing_styles)/*允许用户更改主题 ---1*/
                        .putString(Constant.sb, Rc4.encry_RC4_string(pwd_text,Constant.d))/*类目提示语 ---1*/
                        .putInt(Constant.jw, login_control)/*登录控制 ---1*/
                        .putInt(Constant.vu, vpn_check)/*Vpn及抓包检测 ---1*/
                        .putInt(Constant.ft, xp_check)/*Xp环境检测 ---1*/
                        .putString(Constant.gw, Rc4.encry_RC4_string(verifysign,Constant.d))/*Rc4加密 签名md5 ---1*/
                        .putInt(Constant.gq, verifysign_check)/*签名检测 ---1*/
                        .putString(Constant.uv, Rc4.encry_RC4_string(name,Constant.d))/*应用名称---1*/
                        .putInt(Constant.ac, name_check)/*检查应用名称 ---1*/
                        .putString(Constant.Y, Rc4.encry_RC4_string(Authorization,Constant.d))/*应用名称---1*/
                        .putInt(Constant.fq, settings_page)/*设置页四格 ---1*/
                        .putInt(Constant.fy, updatenumber)/*更新集数 ---1*/
                        .putInt(Constant.th, caton_check)/*卡顿校验 ---1*/
                        .putInt(Constant.jd, check_time)/*卡顿校验时间 ---1*/
                        .putInt(Constant.yx, Tackle_mode)/*卡顿解决方式 ---1*/
                        .putInt(Constant.id, networkspeed)/*显示网速 ---1*/
                        .putInt(Constant.user, epg)/*EPG风格 ---1*/
                        .putInt(Constant.gid, switchs)/*换台方式 ---1*/
                        .putInt(Constant.yms, memory_source)/*记忆播放源 ---1*/
                        .putInt(Constant.oi, memory_channel)/*记忆频道 ---1*/
                        .putInt(Constant.vgs, framedrop)/*高负载丢帧 ---1*/
                        .putInt(Constant.gt, start_on_prepared)/*自动播放片源 ---1*/
                        .putInt(Constant.ufe, async_init_decoder)/*异步解码 ---1*/
                        .putInt(Constant.jda, no_time_adjust)/*真实视频时间 ---1*/
                        .putInt(Constant.oge, infbuf)/*无限缓冲 ---1*/
                        .putInt(Constant.guda, vod_caton_check)/*卡顿换源校验 ---1*/
                        .putInt(Constant.oftn, seizing_time)/*卡顿换源校验时间 ---1*/
                        .putInt(Constant.ykf, live_no_source)/*源用尽处理方式 ---1*/
                        .putInt(Constant.ogy, live)/*直播功能 ---1*/
                        .putInt(Constant.ldb, core_mode)/*指定内核功能 ---1*/
                        .putInt(Constant.ykfa, Adblock)/*广告遮挡 ---1*/
                        .putInt(Constant.gag, xuanjitype)/*选集排序方式 ---1*/
                        .putInt(Constant.vys, xuanjinumber)/*每组多少集 ---1*/
                        .putInt(Constant.gida, reverse)/*换台键反转 ---1*/
                        .putInt(Constant.ogsv, remember_source)/*播放线路记忆 ---1*/
                        .putInt(Constant.otcb, Same_source_search)/*同源搜索 ---1*/
                        .putInt(Constant.hym, search)/*远程搜索 ---1*/
                        .putInt(Constant.vtk, searchport)/*远程搜索端口 ---1*/
                        .commit();//存入文件
                /*执行签名校验后显示广告*/
                verifySignatureAndContinue(user_url);
            }else{
                Msg = msg;
                handler.sendEmptyMessage(FAIL);//未授权/授权到期/应用不存在等等
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*获取失败*/
    public void RequestError(VolleyError volleyError) {
        Log.e(TAG, "RequestError: " + (volleyError != null ? volleyError.getMessage() : "null"));
        if (volleyError != null && volleyError.getCause() != null) {
            Log.e(TAG, "RequestError cause: " + volleyError.getCause().getMessage());
            volleyError.getCause().printStackTrace();
        }
        
        if (volleyError instanceof TimeoutError) {
            Log.e(TAG, "请求超时");
            // Android 4上网络可能较慢，缩短重试间隔
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                handler.sendEmptyMessageDelayed(0, 2000);
            } else {
                handler.sendEmptyMessage(0);
            }
            return;
        }
        if (volleyError instanceof AuthFailureError) {
            Log.e(TAG, "身份验证失败错误");
            handler.sendEmptyMessage(0);
            return;
        }
        if(volleyError instanceof NetworkError) {
            Log.e(TAG, "网络错误，请检查网络连接");
            // Android 4上网络错误时，缩短重试间隔
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                handler.sendEmptyMessageDelayed(DETECTION_NET, 2000);
            } else {
                handler.sendEmptyMessage(0);
            }
            return;
        }
        if(volleyError instanceof ServerError) {
            Log.e(TAG, "服务器错误");
            handler.sendEmptyMessage(0);
            return;
        }
        // 其他错误
        handler.sendEmptyMessage(0);
    }

    /*加载视图布局*/
    protected void loadViewLayout() {
    }

    /**
     * 初始化控件
     */
    @Override
    protected void findViewById() {
        splash = (ImageView) findViewById(R.id.splash);
        tv_splash_version = (TextView) findViewById(R.id.tv_splash_version);/*版本号*/
        tv_splash_version.setText("Version: " + Utils.getVersion(this));/*重新设置版本号*/
        tv_jump_time = (TextView) findViewById(R.id.tv_jump_time);/*倒计时时间*/
        tv_jump = (TextView) findViewById(R.id.tv_jump);/*跳过按钮*/
        tv_jump_id = (LinearLayout) findViewById(R.id.tv_jump_id);/*是否可见跳过和倒计时按钮*/

        /*点击跳过*/
        tv_jump.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                tv_ad_time = 0;

            }
        });
        tv_jump_time.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                tv_ad_time = 0;
            }
        });
    }

    /*设置侦听器*/
    protected void setListener() {
    }

    /*初始化视图*/
    protected void initView() {
    }

    private void Category() {
        mQueue = Volley.newRequestQueue(this, new ExtHttpStack());
        /*超时时间5秒 可声明公共*/
        int socketTimeout = 8000;
        /*请求主控地址*/
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Api_url", ""),Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "BASE_HOST", ""),Constant.d);
        String url = Api_url + "/api.php/" + BASE_HOST + "/Category";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    public void onResponse(String response) {
                        category = response;
                        if (!category.equals("") && !category.equals("null") && category != null){
                            loadMainUI();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                handler.sendEmptyMessage(GET_CATEGORY_FAIL);//类目请求失败
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                // 添加您的请求参数
                //params.put("key", "key");
                params.put("time", GetTimeStamp.timeStamp());
                params.put("key", encry_RC4_string(GetTimeStamp.timeStamp(),GetTimeStamp.timeStamp()));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SplashActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(socketTimeout,//请求的超时时间（以毫秒为单位），即请求在等待响应的最长时间
                1,//最大重试次数。如果请求失败，将会重试的次数。
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));//重试的时间间隔倍数。每次重试的等待时间将是前一个重试时间乘以该倍数。
        mQueue.add(stringRequest);
    }
    
    /**
     * 验证应用签名后继续
     * @param userUrl 用户后端地址
     */
    private void verifySignatureAndContinue(String userUrl) {
        if (userUrl == null || userUrl.isEmpty()) {
            // 没有用户后端地址，直接继续
            handler.sendEmptyMessage(1);
            return;
        }
        
        String verifyUrl = userUrl + "/api?app=" + Api.APPID + "&act=verify_signature";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, verifyUrl,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            org.json.JSONObject json = new org.json.JSONObject(response);
                            int code = json.optInt("code", -1);
                            if (code == 0) {
                                // 验证通过，继续显示广告
                                handler.sendEmptyMessage(1);
                            } else {
                                // 验证失败，显示非法客户端提示
                                showIllegalClientDialog(json);
                            }
                        } catch (Exception e) {
                            // 解析失败，继续（兼容旧版本后端）
                            handler.sendEmptyMessage(1);
                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error != null && error.networkResponse != null && error.networkResponse.statusCode == 403) {
                            // 403错误，显示非法客户端提示
                            try {
                                String responseBody = new String(error.networkResponse.data, "UTF-8");
                                android.util.Log.d("SignatureVerify", "Server response: " + responseBody);
                                org.json.JSONObject json = new org.json.JSONObject(responseBody);
                                showIllegalClientDialog(json);
                            } catch (Exception e) {
                                android.util.Log.e("SignatureVerify", "Parse error: " + e.getMessage());
                                showIllegalClientDialog(null);
                            }
                        } else {
                            // 其他错误，继续（兼容网络问题）
                            android.util.Log.e("SignatureVerify", "Network error: " + (error != null ? error.getMessage() : "null"));
                            handler.sendEmptyMessage(1);
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("app_sign", com.shenma.tvlauncher.utils.AppSignature.getSignatureMD5(SplashActivity.this));
                params.put("app_name", com.shenma.tvlauncher.utils.AppSignature.getPackageName(SplashActivity.this));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mQueue.add(stringRequest);
    }
    
    /**
     * 显示非法客户端提示框
     */
    private void showIllegalClientDialog(org.json.JSONObject json) {
        String title = "温馨提示";
        String message = "检测到您使用的是非官方版本，请下载正版应用";
        String downloadUrl = "";
        String downloadBtn = "立即下载";
        String exitBtn = "退出应用";
        
        if (json != null && json.has("config")) {
            try {
                org.json.JSONObject config = json.getJSONObject("config");
                title = config.optString("title", title);
                message = config.optString("message", message);
                downloadUrl = config.optString("download_url", downloadUrl);
                downloadBtn = config.optString("download_btn", downloadBtn);
                exitBtn = config.optString("exit_btn", exitBtn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        com.shenma.tvlauncher.utils.AppSignature.showIllegalClientDialog(
            SplashActivity.this, title, message, downloadUrl, downloadBtn, exitBtn);
    }
    
}
