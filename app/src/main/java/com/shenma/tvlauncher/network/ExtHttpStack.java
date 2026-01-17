package com.shenma.tvlauncher.network;

import android.os.Build;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.utils.SSLSocketFactoryCompat;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 支持 Android 5.0 以下版本 TLS 1.2 的 HttpStack
 * Android 5.0 以下版本不校验证书，解决 TLS 兼容性问题
 * Android 5.0 及以上版本使用系统默认的证书验证
 */
public class ExtHttpStack extends HurlStack {
    
    private static SSLSocketFactory sslSocketFactory;
    
    // 不校验证书的 TrustManager
    private static final X509TrustManager trustAllManager = new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // 不验证客户端证书
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // 不验证服务器证书
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                };
    
    // 不验证主机名的 HostnameVerifier
    private static final HostnameVerifier trustAllHostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // 接受所有主机名
            return true;
        }
    };
    
    private static final String TAG = "ExtHttpStack";
    
    static {
        try {
            // 只为 Android 5.0 以下版本创建不校验证书的 SSLSocketFactory
            // Android 5.0 及以上版本使用系统默认的证书验证
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "Android " + Build.VERSION.SDK_INT + " 检测到，初始化不校验证书的 SSL 配置");
                // Android 4.4 使用 SSLSocketFactoryCompat 以支持 TLS 1.2，且不校验证书
                sslSocketFactory = new SSLSocketFactoryCompat(trustAllManager);
                Log.d(TAG, "SSLSocketFactory 初始化成功");
            } else {
                Log.d(TAG, "Android " + Build.VERSION.SDK_INT + " 使用系统默认证书验证");
            }
            // Android 5.0 及以上版本不设置 sslSocketFactory，使用系统默认验证
        } catch (Exception e) {
            Log.e(TAG, "SSLSocketFactory 初始化失败: " + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = super.createConnection(url);
        
        // Android 4.4 上为所有连接设置超时
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            try {
                connection.setConnectTimeout(20000); // 20秒连接超时
                connection.setReadTimeout(20000); // 20秒读取超时
                Log.d(TAG, "设置连接超时: 20秒 (Android " + Build.VERSION.SDK_INT + ")");
            } catch (Exception e) {
                Log.e(TAG, "设置超时失败: " + e.getMessage(), e);
            }
        }
        
        // 如果是 HTTPS 连接且是 Android 5.0 以下版本，设置不校验证书的配置
        if (connection instanceof HttpsURLConnection && 
            Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
            
            Log.d(TAG, "配置 HTTPS 连接: " + url.toString() + " (Android " + Build.VERSION.SDK_INT + ")");
            
            // Android 5.0 以下设置不校验证书的 SSLSocketFactory
            if (sslSocketFactory != null) {
                try {
                    httpsConnection.setSSLSocketFactory(sslSocketFactory);
                    Log.d(TAG, "SSLSocketFactory 设置成功");
                } catch (Exception e) {
                    Log.e(TAG, "设置 SSLSocketFactory 失败: " + e.getMessage(), e);
                }
            } else {
                Log.w(TAG, "SSLSocketFactory 为 null，无法设置");
        }
            
            // Android 5.0 以下设置不验证主机名的 HostnameVerifier
            try {
                httpsConnection.setHostnameVerifier(trustAllHostnameVerifier);
                Log.d(TAG, "HostnameVerifier 设置成功（不验证主机名）");
            } catch (Exception e) {
                Log.e(TAG, "设置 HostnameVerifier 失败: " + e.getMessage(), e);
            }
        } else if (connection instanceof HttpsURLConnection) {
            Log.d(TAG, "Android " + Build.VERSION.SDK_INT + " 使用系统默认 HTTPS 配置: " + url.toString());
        }
        // Android 5.0 及以上版本使用系统默认的证书和主机名验证
        
        return connection;
    }

    @Override
    public HttpResponse performRequest(Request<?> request,
                                       Map<String, String> additionalHeaders) throws IOException,
            AuthFailureError {
        return super.performRequest(request, additionalHeaders);
    }

}
