package com.shenma.tvlauncher.network;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;

import java.io.UnsupportedEncodingException;

/**
 * @param <T>
 * @author joychang
 * @Description 解析json
 */
public class GsonRequest<T> extends Request<T> {
    private final Gson mGson;
    private final Class<T> mClazz;
    private final Listener<T> mListener;


    public GsonRequest(int method,
                       String url,
                       Class<T> clazz,
                       Listener<T> listener,
                       ErrorListener errorListener) {
        //super(Method.GET, url, errorListener);
        super(method, url, errorListener);
        this.mClazz = clazz;
        this.mListener = listener;
        mGson = new Gson();
    }


    public GsonRequest(int method,
                       String url,
                       Class<T> clazz,
                       Listener<T> listener,
                       ErrorListener errorListener,
                       Gson gson) {
        super(Method.GET, url, errorListener);
        this.mClazz = clazz;
        this.mListener = listener;
        mGson = gson;
    }


    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
    }


    @Override
    public Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            int result1 = json.indexOf("notice");//如果发现notice不用rc4解密
            int result2 = json.indexOf("code");//如果发现code不用rc4解密
            int result3 = json.indexOf("flitter");//如果发现flitter不用rc4解密

            if(result1 != -1){
                return Response.success(mGson.fromJson(json, mClazz),HttpHeaderParser.parseCacheHeaders(response));
            }
            if(result2 != -1){
                return Response.success(mGson.fromJson(json, mClazz),HttpHeaderParser.parseCacheHeaders(response));
            }
            if(result3 != -1){
                return Response.success(mGson.fromJson(json, mClazz),HttpHeaderParser.parseCacheHeaders(response));
            }else {
                /*固定Rc4解码Hex*/
                String ApiKey = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(null, Constant.pd, ""), Constant.d);
                //return Response.success(mGson.fromJson(Rc4.decry_RC4(json, ApiKey), mClazz), HttpHeaderParser.parseCacheHeaders(response));

                /*动态Rc4解码Hex*/
                //return Response.success(mGson.fromJson(Rc4.decry_RC4(json, Md5Encoder.encode(Constant.d)), mClazz), HttpHeaderParser.parseCacheHeaders(response));

                /*动态Rc4解码Base64*/
                return Response.success(mGson.fromJson(Rc4.decryptBase64(json, Md5Encoder.encode(Constant.d)), mClazz), HttpHeaderParser.parseCacheHeaders(response));
            }

        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e2) {
            return Response.error(new ParseError(e2));
        }
    }
}