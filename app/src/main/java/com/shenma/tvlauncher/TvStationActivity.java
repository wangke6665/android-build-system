package com.shenma.tvlauncher;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.adapter.TvStationAdapter;
import com.shenma.tvlauncher.application.MyVolley;
import com.shenma.tvlauncher.dao.TVStationDao;
import com.shenma.tvlauncher.dao.bean.TVSCollect;
import com.shenma.tvlauncher.domain.TVStation;
import com.shenma.tvlauncher.domain.TVStationInfo;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

public class TvStationActivity extends BaseActivity {

    private GridView tvstation_gv;
    private ErrorListener mErrorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof TimeoutError) {
                //Logger.e("zhouchuan", "请求超时");
            } else if (error instanceof AuthFailureError) {
                //Logger.e("zhouchuan","AuthFailureError=" + error.toString());
            }
        }
    };
    private RequestQueue mQueue;
    private ImageLoader imageLoader;
    private int lastIndex = -1;
    private List<TVSCollect> tvs;
    private List<TVStationInfo> data;
    private Listener<TVStation> mSuccessLinListener = new Listener<TVStation>() {

        @Override
        public void onResponse(TVStation response) {
            if (response != null && "200".equals(response.getCode())) {
                data = filtrationData(response.getData());
                tvstation_gv.setAdapter(new TvStationAdapter(context, data, imageLoader));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tv_station_grid);
        initView();
    }

    @Override
    protected void initView() {
        findViewById();
        setListener();
        queryDB();
        initData();
    }

    @Override
    protected void loadViewLayout() {

    }

    @Override
    protected void findViewById() {
        tvstation_gv = (GridView) findViewById(R.id.tvstation_gv);
        tvstation_gv.setSelector(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void setListener() {
        LayoutAnimationController lac = new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.setbig2));
        lac.setOrder(LayoutAnimationController.ORDER_RANDOM);
        lac.setDelay(0.5f);
        tvstation_gv.setLayoutAnimation(lac);
        tvstation_gv.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > lastIndex) {//向下按
                    if ((position - parent.getFirstVisiblePosition()) > 14 && (position - lastIndex) == 5 && position < (parent.getCount() % 5 == 0 ? parent.getCount() - 5 : parent.getCount() - (parent.getCount() % 5))) {
                        tvstation_gv.post(new Runnable() {
                            @Override
                            public void run() {
                                tvstation_gv.smoothScrollBy(152, 500);
                            }
                        });
                    }
                } else {//向上按
                    if ((position - parent.getFirstVisiblePosition()) < 5 && (lastIndex - position) == 5 && parent.getFirstVisiblePosition() != 0) {
                        tvstation_gv.post(new Runnable() {
                            @Override
                            public void run() {
                                tvstation_gv.smoothScrollBy(-152, 500);
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
        tvstation_gv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TVSCollect tvsc = new TVSCollect();
                tvsc.setChannelname(data.get(position).getChannelname());
                tvsc.setChannelpic(data.get(position).getChannelpic());
                tvsc.setTvindex(getIntent().getIntExtra("index", -1));
                boolean isUpdate = getIntent().getBooleanExtra("isUpdate", false);
                if (isUpdate) {
                    //替换电视台
                    TVStationDao.getInstance(context).updateTvsi(tvsc.getTvindex(), tvsc);
                    TvStationActivity.this.finish();
                } else {
                    //添加电视台
                    //存入数据库
                    TVStationDao.getInstance(context).addTvsi(tvsc);
                    TvStationActivity.this.finish();
                }
            }

        });
    }

    private void initData() {
        mQueue = Volley.newRequestQueue(context, new ExtHttpStack());
        imageLoader = MyVolley.getImageLoader();
        GsonRequest<TVStation> mTvs = new GsonRequest<TVStation>(Method.POST, Constant.TVSTATIONS, TVStation.class, mSuccessLinListener, mErrorListener) {
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(TvStationActivity.this, "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };
        mQueue.add(mTvs);
    }

    private void queryDB() {
        tvs = TVStationDao.getInstance(context).queryAllTvsi();
    }

    /**
     * 过滤掉相关数据
     *
     * @param data
     * @return
     */
    private List<TVStationInfo> filtrationData(List<TVStationInfo> data) {
        List<TVStationInfo> tvlist = new ArrayList<TVStationInfo>();
        for (TVStationInfo info : data) {
            boolean flag = false;
            for (TVSCollect tv : tvs) {
                if (info.getChannelname().equals(tv.getChannelname())) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                tvlist.add(info);
            }
        }
        return tvlist;
    }

}
