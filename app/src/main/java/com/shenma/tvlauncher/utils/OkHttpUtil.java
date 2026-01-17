package com.shenma.tvlauncher.utils;

import java.io.File;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.X509TrustManager;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Callback;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * OkHttp工具类
 * 优化：实现OkHttpClient单例复用，避免每次创建新实例造成资源浪费
 */
public class OkHttpUtil {
    private final static int CACHE_SIZE_BYTES = 1024 * 1024 * 2;
    
    // 优化：使用单例模式复用OkHttpClient实例
    private static volatile OkHttpClient defaultClient;
    private static volatile OkHttpClient sslClient;
    private static volatile OkHttpClient cacheClient;
    
    /**
     * 获取默认的OkHttpClient实例（单例）
     */
    public static OkHttpClient getDefaultClient() {
        if (defaultClient == null) {
            synchronized (OkHttpUtil.class) {
                if (defaultClient == null) {
                    defaultClient = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        return defaultClient;
    }
    
    /**
     * 获取支持SSL的OkHttpClient实例（单例）
     */
    private static OkHttpClient getSslClient() {
        if (sslClient == null) {
            synchronized (OkHttpUtil.class) {
                if (sslClient == null) {
                    //定义一个信任所有证书的TrustManager
                    final X509TrustManager trustAllCert = new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    };
                    sslClient = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                            .sslSocketFactory(new SSLSocketFactoryCompat(trustAllCert), trustAllCert)
                            .build();
                }
            }
        }
        return sslClient;
    }
    
    /**
     * 获取带缓存的OkHttpClient实例（单例）
     */
    private static OkHttpClient getCacheClient(File cacheFile) {
        if (cacheClient == null) {
            synchronized (OkHttpUtil.class) {
                if (cacheClient == null) {
                    //定义一个信任所有证书的TrustManager
                    final X509TrustManager trustAllCert = new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    };
                    //缓存大小为10M
                    int cacheSize = 10 * 1024 * 1024;
                    final Cache cache = new Cache(cacheFile, cacheSize);
                    cacheClient = new OkHttpClient.Builder()
                            .cache(cache)
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .hostnameVerifier(SSLSocketClient.getHostnameVerifier())
                            .sslSocketFactory(new SSLSocketFactoryCompat(trustAllCert), trustAllCert)
                            .build();
                }
            }
        }
        return cacheClient;
    }
    
    /**
     * 优化：使用单例OkHttpClient实例
     */
    public static void post(String address, Callback callback, Map<String, String> map) {
        OkHttpClient client = getDefaultClient();
        Builder builder = new Builder();
        if (map != null) {
            for (Entry<String, String> entry : map.entrySet()) {
                builder.add((String) entry.getKey(), (String) entry.getValue());
            }
        }
        client.newCall(new Request.Builder().url(address).post(builder.build()).build()).enqueue(callback);
    }
    
    /**
     * 优化：使用单例OkHttpClient实例
     */
    public static void get(String address, Callback callback) {
        OkHttpClient client = getDefaultClient();
        client.newCall(new Request.Builder().url(address).build()).enqueue(callback);
    }


    /**
     * 优化：使用单例OkHttpClient实例，避免重复创建
     * @deprecated 使用 cacheGet 替代
     */
    @Deprecated
    public static void Cacheget(File cacheFile, String address, Callback callback) {
        cacheGet(cacheFile, address, callback);
    }
    
    /**
     * 优化：使用单例OkHttpClient实例，避免重复创建
     */
    public static void cacheGet(File cacheFile, String address, Callback callback) {
        OkHttpClient client = getCacheClient(cacheFile);
        CacheControl cacheControl = new CacheControl.Builder()
                //有缓存直接用缓存
                //.noStore()
                //缓存60秒
                .maxAge(0, TimeUnit.SECONDS)
                //强制使用缓存
                //.maxStale(Integer.MAX_VALUE, TimeUnit.SECONDS)
                .build();
        client.newCall(new Request.Builder()
                .url(address)
                .cacheControl(cacheControl)
                .removeHeader("User-Agent")
                .addHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36")
                .build())
                .enqueue(callback);
    }

    /**
     * 优化：使用单例OkHttpClient实例，避免重复创建
     */
    public static void okhttpget(String url, Callback callback) {
        OkHttpClient client = getSslClient();
        client.newCall(new Request.Builder().url(url).build()).enqueue(callback);
    }
}
