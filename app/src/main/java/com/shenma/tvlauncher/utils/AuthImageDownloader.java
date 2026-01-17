package com.shenma.tvlauncher.utils;


import android.content.Context;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * HTTPS的图片加载
 */
public class AuthImageDownloader extends BaseImageDownloader {

    private SSLSocketFactory mSSLSocketFactory;
    public AuthImageDownloader(Context context) {
        super(context);
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
        SSLContext sslContext = sslContextForTrustedCertificates();
        //mSSLSocketFactory = sslContext.getSocketFactory();
        mSSLSocketFactory =new SSLSocketFactoryCompat(trustAllCert);
    }
    public AuthImageDownloader(Context context, int connectTimeout, int readTimeout) {
        super(context, connectTimeout, readTimeout);
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
        SSLContext sslContext = sslContextForTrustedCertificates();
        //mSSLSocketFactory = sslContext.getSocketFactory();
        mSSLSocketFactory =new SSLSocketFactoryCompat(trustAllCert);
    }
    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException
    {
        URL url = null;
        try {
            url = new URL(imageUri);
        } catch (MalformedURLException e) {
        }
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection)conn).setSSLSocketFactory(mSSLSocketFactory);
            //((HttpsURLConnection)conn).setHostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
            ((HttpsURLConnection)conn).setHostnameVerifier(SSLSocketClient.getHostnameVerifier());
        }
        return new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);
    }
    // always verify the host - dont check for certificate
    final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public SSLContext sslContextForTrustedCertificates() {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }catch (KeyManagementException e) {
            e.printStackTrace();
        }finally {
            return sc;
        }
    }

    class miTM implements javax.net.ssl.TrustManager, X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public boolean isServerTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        public boolean isClientTrusted(
                java.security.cert.X509Certificate[] certs) {
            return true;
        }
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }
}