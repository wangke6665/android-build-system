package com.shenma.tvlauncher;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.adapter.WallpaperAdapter;
import com.shenma.tvlauncher.domain.Wallpaper;
import com.shenma.tvlauncher.domain.WallpaperInfo;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;
import com.shenma.tvlauncher.tvlive.network.ThreadPoolManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.shenma.tvlauncher.utils.SSLSocketFactoryCompat;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

/**
 * @author joychang
 * @Description 更换壁纸
 */

public class SettingWallpaperActivity extends BaseActivity {

    private RequestQueue mQueue;
    private GridView wallpaper_gv;
    private int position = -1;
    private int pageindex = 1;
    private int vodpageindex;
    private int totalpage;
    private List<WallpaperInfo> data;
    private Listener<Wallpaper> listener = new Listener<Wallpaper>() {

        @Override
        public void onResponse(Wallpaper response) {
            if (response != null && "200".equals(response.getCode())) {
                data = response.getData();
                wallpaper_gv.setAdapter(new WallpaperAdapter(context, data));
            }
        }
    };
    private ErrorListener errorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof TimeoutError) {
                //Logger.e("zhouchuan", "请求超时");
            } else if (error instanceof AuthFailureError) {
                //Logger.e("zhouchuan","AuthFailureError=" + error.toString());
            }
        }
    };
    private String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "Api_url", ""),Constant.d);
    private String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(this, "BASE_HOST", ""),Constant.d);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_setting_wallpaper);
        initView();
    }

    @Override
    protected void initView() {
        findViewById();
        setListener();
        initData();
    }

    private void initData() {
        mQueue = Volley.newRequestQueue(context, new ExtHttpStack());
        GsonRequest<Wallpaper> mWallpaper = new GsonRequest<Wallpaper>(Method.POST, Api_url +"/api.php/" + BASE_HOST + "/wallpaper/" + "?page=" + pageindex, Wallpaper.class, listener, errorListener) {
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
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(SettingWallpaperActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(mWallpaper);
    }

    @Override
    protected void loadViewLayout() {

    }

    @Override
    protected void findViewById() {
        wallpaper_gv = (GridView) findViewById(R.id.wallpaper_gv);
    }

    @Override
    protected void setListener() {
        LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.setbig2));
        lac.setOrder(LayoutAnimationController.ORDER_RANDOM);
        lac.setDelay(0.5f);
        wallpaper_gv.setLayoutAnimation(lac);
        wallpaper_gv.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int i = totalItemCount - visibleItemCount;
                if (firstVisibleItem < i) {
                    //Logger.v("joychang", "<<<firstVisibleItem=" + firstVisibleItem + ".....i=" + i);
                } else {
                    // 分页加载数据
                    pageDown();
                }
            }
        });

        wallpaper_gv.setOnItemSelectedListener(new OnItemSelectedListener() {
            private int lastIndex;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > lastIndex) {//向下按
                    if ((position - parent.getFirstVisiblePosition()) > 14 && (position - lastIndex) == 5 && position < (parent.getCount() % 5 == 0 ? parent.getCount() - 5 : parent.getCount() - (parent.getCount() % 5))) {
                        wallpaper_gv.post(new Runnable() {
                            @Override
                            public void run() {
                                wallpaper_gv.smoothScrollBy(152, 500);
                            }
                        });
                    }
                } else {//向上按
                    if ((position - parent.getFirstVisiblePosition()) < 5 && (lastIndex - position) == 5 && parent.getFirstVisiblePosition() != 0) {
                        wallpaper_gv.post(new Runnable() {
                            @Override
                            public void run() {
                                wallpaper_gv.smoothScrollBy(-152, 500);
                            }
                        });
                    }
                }
                lastIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        wallpaper_gv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SettingWallpaperActivity.this.position = position;
                Utils.showToast(context,R.string.Set_successfully, R.drawable.toast_smile);
            }
        });
    }

    /**
     * 向下翻页
     */
    private void pageDown() {
        //Logger.v("joychang", "pageindex=" + pageindex + "....vodpageindex=" + vodpageindex);
        if (pageindex >= totalpage || pageindex > vodpageindex)
            return;
        pageindex = pageindex + 1;
        //Logger.v("joychang", "请求页数===" + pageindex);
        initData();
    }

    @Override
    protected void onDestroy() {
        if (position != -1) {
            String wallpaperFileName = data.get(position).getSkinpath().substring(data.get(position).getSkinpath().lastIndexOf("/") + 1);
            String wallpaperPath = data.get(position).getSkinpath();
            if (!startCheckLoaclFile(wallpaperFileName)) {
                startDownload(Constant.HEARD_URL + wallpaperPath);
            }
        }
        super.onDestroy();
    }

    /**
     * 检测本地文件
     *
     * @param fileName 壁纸图片文件名
     * @author drowtram
     */
    private boolean startCheckLoaclFile(String fileName) {
        File file = context.getFilesDir();
        try {
            if (file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fName = f.getName();
                    if (fName.equals(fileName)) {
                        //检测到本地有对应的资源图片，则拷贝到程序资源文件夹中替换主页背景
                        sendBroadcast(fileName);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 下载文件
     *
     * @param url
     */

    private void startDownload(final String url) {
        // 优化：使用线程池替代直接创建Thread
        ThreadPoolManager.getInstance().addTask(new Runnable() {
            public void run() {
                File file = context.getFilesDir();
                String fileName = url.substring(url.lastIndexOf("/") + 1);
                //Log.d("zhouchuan", "文件路径" + fileName);
                if (!file.exists()) {
                    file.mkdirs();
                }
                try {
                    // 使用支持 TLS 1.2 的 OkHttpClient（兼容 Android 4.4）
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
                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .sslSocketFactory(new SSLSocketFactoryCompat(trustAllCert), trustAllCert)
                            .build();
                    //2.创建请求对象Request
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    //3.执行请求
                    Call call = client.newCall(request);
                    //同步请求
                    Response response = call.execute();
                    if (response.body().contentLength() > 0) {
                        InputStream is = response.body().byteStream();
                        //FileOutputStream fos = context.openFileOutput(fileName, 3);
                        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
                        byte[] buffer = new byte[8192];
                        while (true) {
                            int count = is.read(buffer);
                            if (count == -1) {
                                fos.close();
                                is.close();
                                sendBroadcast(fileName);
                                return;
                            }
                            fos.write(buffer, 0, count);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    private void sendBroadcast(String fileName) {
        Intent mIntent = new Intent();
        mIntent.setAction("com.hd.changewallpaper");
        mIntent.putExtra("wallpaperFileName", fileName);
        sendBroadcast(mIntent);
    }
}
