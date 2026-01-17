package com.shenma.tvlauncher.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HurlStack;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class NetUtils extends HurlStack {

    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        // TODO Auto-generated method stub
        return super.createConnection(url);
    }

    @Override
    public HttpResponse performRequest(Request<?> request,
                                       Map<String, String> additionalHeaders) throws IOException,
            AuthFailureError {
        // TODO Auto-generated method stub
        return super.performRequest(request, additionalHeaders);
    }

}
