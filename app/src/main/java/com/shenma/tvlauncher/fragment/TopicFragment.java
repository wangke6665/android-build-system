package com.shenma.tvlauncher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.shenma.tvlauncher.network.ExtHttpStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.shenma.tvlauncher.Api;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.SettingWallpaperActivity;
import com.shenma.tvlauncher.TopicActivity;
import com.shenma.tvlauncher.application.MyVolley;
import com.shenma.tvlauncher.domain.Topic;
import com.shenma.tvlauncher.domain.TopicInfo;
import com.shenma.tvlauncher.network.GsonRequest;
import com.shenma.tvlauncher.utils.AES;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.GetTimeStamp;
import com.shenma.tvlauncher.utils.ImageUtil;
import com.shenma.tvlauncher.utils.Logger;
import com.shenma.tvlauncher.utils.Md5Encoder;
import com.shenma.tvlauncher.utils.Rc4;
import com.shenma.tvlauncher.utils.ScaleAnimEffect;
import com.shenma.tvlauncher.utils.SharePreferenceDataUtil;
import com.shenma.tvlauncher.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.shenma.tvlauncher.utils.Rc4.encry_RC4_string;

/**
 * @author joychang
 * @Description 影视分類
 */
public class TopicFragment extends BaseFragment implements OnFocusChangeListener, OnClickListener {
    private FrameLayout[] mv_fls;
    public FrameLayout[] mv_typeLogs;
    private ImageView[] mvLogs;
    private ImageView[] mvLogsa;
    private int[] mvbgs;
    private TextView[] tvs;
    ScaleAnimEffect animEffect;
    private View view;
    public RequestQueue mQueue;
    public ImageLoader imageLoader;
    private List<TopicInfo> data = null;
    private String TAG = "topicFragment";
    private int id1 = 0;
    private int id2 = 0;
    private int id3 = 0;
    private int id4 = 0;
    private int id5 = 0;
    private int id6 = 0;


    /*创建时的回调函数*/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "topicFragment...onCreate");
    }

    /*在创建视图时*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Logger.d(TAG, "topicFragment...onCreateView");
        mQueue = Volley.newRequestQueue(getActivity(), new ExtHttpStack());
        if (container == null) {
            return null;
        }
        if (view == null) {

            int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
            if (Interface_Style == 0){
                /*旧UI*/
                view = inflater.inflate(R.layout.layout_topic, container, false);
            }else if(Interface_Style == 1){
                /*新UI*/
                view = inflater.inflate(R.layout.layout_topicss, container, false);
            }else if(Interface_Style == 2||Interface_Style == 3){
                /*新UI圆角*/
                view = inflater.inflate(R.layout.layout_topics, container, false);
            }
//            view = inflater.inflate(R.layout.layout_topic, container, false);

            init();
        } else {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null)
                viewGroup.removeView(view);
        }
        if(data == null){
            initData();
        }
        return view;
    }

    /*启动*/
    @Override
    public void onStart() {
        super.onStart();
    }

    /*初始化*/
    private void init() {
        loadViewLayout();
        findViewById();
        setListener();
        // mv_fls[0].requestFocus();
    }

    /*初始化数据*/
    private void initData() {
        String Api_url = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Api_url", ""), Constant.d);
        String BASE_HOST = Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "BASE_HOST", ""), Constant.d);
        imageLoader = MyVolley.getImageLoader();
        GsonRequest<Topic> mtopics = new GsonRequest<Topic>(Method.POST, Api_url + "/api.php/" + BASE_HOST +"/topic", Topic.class, createMyReqSuccessListener(), createMyReqErrorListener()) {
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
                headers.put("Authorization", Rc4.decry_RC4(SharePreferenceDataUtil.getSharedStringData(getActivity(), "Authorization", ""),Constant.d));  /*设置其他请求头*/
                return headers;
            }
        };

        mQueue.add(mtopics); // 执行
    }

    /*请求成功*/
    private Response.Listener<Topic> createMyReqSuccessListener() {
        return new Response.Listener<Topic>() {
            @Override
            public void onResponse(Topic response) {
                data = response.getData();
                //count = new int[6];
                int paramInt = 0;
                String paramUrl;
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getTjwei().equals("1")) {
                        paramInt = 0;
                        id1 = i;
                        tvs[0].setText(data.get(i).getZtname());
                        tvs[0].setVisibility(View.VISIBLE);//启用后首页推荐显示文字
                    } else if (data.get(i).getTjwei().equals("2")) {
                        paramInt = 1;
                        id2 = i;
                        tvs[1].setText(data.get(i).getZtname());
                        tvs[1].setVisibility(View.VISIBLE);//启用后首页推荐显示文字
                    } else if (data.get(i).getTjwei().equals("3")) {
                        paramInt = 2;
                        id3 = i;
                        tvs[2].setText(data.get(i).getZtname());
                        tvs[2].setVisibility(View.VISIBLE);//启用后首页推荐显示文字
                    } else if (data.get(i).getTjwei().equals("4")) {
                        id4 = i;
                        paramInt = 3;
                        tvs[3].setText(data.get(i).getZtname());
                        tvs[3].setVisibility(View.VISIBLE);//启用后首页推荐显示文字
                    } else if (data.get(i).getTjwei().equals("5")) {
                        id5 = i;
                        paramInt = 4;
                        tvs[4].setText(data.get(i).getZtname());
                        tvs[4].setVisibility(View.VISIBLE);//启用后首页推荐显示文字
                    } else if (data.get(i).getTjwei().equals("6")) {
                        id6 = i;
                        paramInt = 5;
                        tvs[5].setText(data.get(i).getZtname());
                        tvs[5].setVisibility(View.VISIBLE);//启用后首页推荐显示文字
                    }
                    paramUrl =  data.get(i).getSmallpic();
                    Logger.v("joychang", "paramUrl=" + paramUrl);

                    /*第六个专题不显示封面*/
//                    if (null != paramUrl && !paramUrl.contains("null") && paramInt != 5) {
//                        setTypeImage(paramInt, paramUrl);
//                    }

                    if (null != paramUrl && !paramUrl.contains("null")) {
                        setTypeImage(paramInt, paramUrl);
                    }
                }
            }
        };
    }

    /*设置图片*/
    private void setTypeImage(int paramInt, String paramUrl) {
        Logger.d(TAG, "paramUrl=" + paramUrl);
        // imageLoader.get(paramUrl,
        // ImageLoader.getImageListener(typeLog_bgs[paramInt],mvbgs[paramInt],mvbgs[paramInt]));
//        imageLoader.get(paramUrl, ImageUtil.getmImageListener(
//                mv_typeLogs[paramInt], mvbgs[paramInt], mvbgs[paramInt]));

        imageLoader.get(paramUrl,
                ImageLoader.getImageListener(mvLogsa[paramInt],
                        mvbgs[paramInt],
                        mvbgs[paramInt]));
    }

//    private void setTypeImage(int paramInt, String paramUrl) {
//        imageLoader.get(paramUrl, new ImageLoader.ImageListener() {
//            @Override
//            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
//                Bitmap originalBitmap = response.getBitmap();
//                Bitmap roundedBitmap = ImageUtil.getRoundedCornerBitmap(originalBitmap, 20);
//                Drawable roundedDrawable = new BitmapDrawable(getResources(), roundedBitmap);
//                mv_typeLogs[paramInt].setBackground(roundedDrawable);
//            }
//
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                // Handle error
//            }
//        });
//    }


    /*请求失败*/
    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    Logger.e("joychang", "请求超时");
                } else if (error instanceof AuthFailureError) {
                    Logger.e("joychang", "AuthFailureError=" + error.toString());
                }
            }
        };
    }

    /*加载视图布局*/
    protected void loadViewLayout() {
        mv_fls = new FrameLayout[6];
        mv_typeLogs = new FrameLayout[6];
        // typeLog_bgs = new ImageView[7];
        mvLogs = new ImageView[6];
        mvLogsa = new ImageView[6];
        mvbgs = new int[6];
        animEffect = new ScaleAnimEffect();
        tvs = new TextView[6];
    }

    /*按ID查找视图*/
    protected void findViewById() {
        mv_fls[0] = (FrameLayout) view.findViewById(R.id.topic_fl_0);
        mv_fls[1] = (FrameLayout) view.findViewById(R.id.topic_fl_1);
        mv_fls[2] = (FrameLayout) view.findViewById(R.id.topic_fl_2);
        mv_fls[3] = (FrameLayout) view.findViewById(R.id.topic_fl_3);
        mv_fls[4] = (FrameLayout) view.findViewById(R.id.topic_fl_4);
        mv_fls[5] = (FrameLayout) view.findViewById(R.id.topic_fl_5);

        mv_typeLogs[0] = (FrameLayout) view.findViewById(R.id.topic_iv_0);
        mv_typeLogs[1] = (FrameLayout) view.findViewById(R.id.topic_iv_1);
        mv_typeLogs[2] = (FrameLayout) view.findViewById(R.id.topic_iv_2);
        mv_typeLogs[3] = (FrameLayout) view.findViewById(R.id.topic_iv_3);
        mv_typeLogs[4] = (FrameLayout) view.findViewById(R.id.topic_iv_4);
        mv_typeLogs[5] = (FrameLayout) view.findViewById(R.id.topic_iv_5);

        mvLogs[0] = (ImageView) view.findViewById(R.id.topic_bg_0);
        mvLogs[1] = (ImageView) view.findViewById(R.id.topic_bg_1);
        mvLogs[2] = (ImageView) view.findViewById(R.id.topic_bg_2);
        mvLogs[3] = (ImageView) view.findViewById(R.id.topic_bg_3);
        mvLogs[4] = (ImageView) view.findViewById(R.id.topic_bg_4);
        mvLogs[5] = (ImageView) view.findViewById(R.id.topic_bg_5);


        mvLogsa[0] = (ImageView) view.findViewById(R.id.topic_i_0);
        mvLogsa[1] = (ImageView) view.findViewById(R.id.topic_i_1);
        mvLogsa[2] = (ImageView) view.findViewById(R.id.topic_i_2);
        mvLogsa[3] = (ImageView) view.findViewById(R.id.topic_i_3);
        mvLogsa[4] = (ImageView) view.findViewById(R.id.topic_i_4);
        mvLogsa[5] = (ImageView) view.findViewById(R.id.topic_i_5);


        tvs[0] = (TextView) view.findViewById(R.id.top_re_0);
        tvs[1] = (TextView) view.findViewById(R.id.top_re_1);
        tvs[2] = (TextView) view.findViewById(R.id.top_re_2);
        tvs[3] = (TextView) view.findViewById(R.id.top_re_3);
        tvs[4] = (TextView) view.findViewById(R.id.top_re_4);
        tvs[5] = (TextView) view.findViewById(R.id.top_re_5);

        for (int i = 0; i < mv_typeLogs.length; i++) {
            mvLogs[i].setVisibility(View.GONE);
            mv_typeLogs[i].setOnClickListener(this);
            // if(ISTV){
            mv_typeLogs[i].setOnFocusChangeListener(this);
            // }
        }
    }

    /*设置侦听器*/
    protected void setListener() {

    }

    /*单击*/
    @Override
    public void onClick(View v) {
        //TODO 跳转二级界面 传参：专题类型、专题二级接口地址、专题大海报地址
        Intent i = new Intent(home,TopicActivity.class);
        switch (v.getId()) {
            case R.id.topic_iv_0:

                if(null != data && data.size() > 0){
                    i.putExtra("describe", data.get(id1).getZtdescribe());
                    i.putExtra("bigpic", data.get(id1).getBigpic());
                    i.putExtra("linkurl", data.get(id1).getLinkurl());
                    i.putExtra("TYPE", data.get(id1).getVideotype());
                    startActivity(i);
                }
                break;
            case R.id.topic_iv_1:
                if(null != data && data.size() > 1){
                    i.putExtra("describe", data.get(id2).getZtdescribe());
                    i.putExtra("bigpic", data.get(id2).getBigpic());
                    i.putExtra("linkurl", data.get(id2).getLinkurl());
                    i.putExtra("TYPE", data.get(id2).getVideotype());
                    startActivity(i);
                }
                break;
            case R.id.topic_iv_2:
                if(null != data && data.size() >2){
                    i.putExtra("describe", data.get(id3).getZtdescribe());
                    i.putExtra("bigpic", data.get(id3).getBigpic());
                    i.putExtra("linkurl", data.get(id3).getLinkurl());
                    i.putExtra("TYPE", data.get(id3).getVideotype());
                    startActivity(i);
                }
                break;
            case R.id.topic_iv_3:
                if(null != data && data.size() > 3){
                    i.putExtra("describe", data.get(id4).getZtdescribe());
                    i.putExtra("bigpic", data.get(id4).getBigpic());
                    i.putExtra("linkurl", data.get(id4).getLinkurl());
                    i.putExtra("TYPE", data.get(id4).getVideotype());
                    startActivity(i);
                }
                break;
            case R.id.topic_iv_4:
                if(null != data && data.size() > 4){
                    i.putExtra("describe", data.get(id5).getZtdescribe());
                    i.putExtra("bigpic", data.get(id5).getBigpic());
                    i.putExtra("linkurl", data.get(id5).getLinkurl());
                    i.putExtra("TYPE", data.get(id5).getVideotype());
                    startActivity(i);
                }
                break;
            case R.id.topic_iv_5:
                if(null != data && data.size() > 5){
                    i.putExtra("describe", data.get(id6).getZtdescribe());
                    i.putExtra("bigpic", data.get(id6).getBigpic());
                    i.putExtra("linkurl", data.get(id6).getLinkurl());
                    i.putExtra("TYPE", data.get(id6).getVideotype());
                    startActivity(i);
                }
                break;
        }
    }

    /*聚焦变化*/
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int paramInt = 0;
        switch (v.getId()) {
            case R.id.topic_iv_0:
                paramInt = 0;
                break;
            case R.id.topic_iv_1:
                paramInt = 1;
                break;
            case R.id.topic_iv_2:
                paramInt = 2;
                break;
            case R.id.topic_iv_3:
                paramInt = 3;
                break;
            case R.id.topic_iv_4:
                paramInt = 4;
                break;
            case R.id.topic_iv_5:
                paramInt = 5;
                break;
        }
        if (hasFocus) {
            showOnFocusAnimation(paramInt);
            if (null != home.whiteBorder) {
                home.whiteBorder.setVisibility(View.VISIBLE);
            }
            flyAnimation(paramInt);
        } else {
            showLoseFocusAinimation(paramInt);
            // 将白框隐藏
        }


        int Interface_Style = SharePreferenceDataUtil.getSharedIntData(getActivity(), "Interface_Style", 4);
        if (Interface_Style == 0||Interface_Style == 1){
            for (TextView tv : tvs) {
                if(tv.getVisibility()!=View.GONE) {
                    //tv.setVisibility(View.GONE);
                }
            }
        }else if(Interface_Style == 2||Interface_Style == 3){
//            for (TextView tv : tvs) {
//                if (tv.getVisibility() != View.GONE) {
//                    tv.setVisibility(View.GONE);
//                }
//            }
            for (int i = 0; i < tvs.length; i++) {
                TextView tv = tvs[i];
                if (i != 5 && tv.getVisibility() != View.GONE) {
                    tv.setVisibility(View.GONE);
                }
            }


        }




    }

    /**
     * 飞框焦点动画
     *
     * @param paramInt
     */
    private void flyAnimation(int paramInt) {
        int[] location = new int[2];
        mv_typeLogs[paramInt].getLocationOnScreen(location);
        int width = mv_typeLogs[paramInt].getWidth();
        int height = mv_typeLogs[paramInt].getHeight();
        float x = (float) location[0];
        float y = (float) location[1];
        Logger.v("joychang", "paramInt=" + paramInt + "..x=" + x + "...y=" + y);
        if (mHeight > 1000 && mWidth > 1000) {
            switch (paramInt) {
                case 0:
                    width = width + 46;
                    height = height + 65;
                    x = 285;
                    y = 490;
                    break;
                case 1:
                    width = width + 65;
                    height = height + 39;
                    x = 781;
                    y = 377;
                    break;
                case 2:
                    width = width + 30;
                    height = height + 23;
                    x = 630;
                    y = 679;
                    break;
                case 3:
                    width = width + 30;
                    height = height + 23;
                    x = 926;
                    y = 679;
                    break;
                case 4:
                    width = width + 40;
                    height = height + 60;
                    x = 1272;
                    y = 490;
                    break;
                case 5:
                    width = width + 30;
                    height = height + 60;
                    x = 1620;
                    y = 339 + 150;
            }
        } else {
            switch (paramInt) {
                case 0:
                    width = width + 26;
                    height = height + 40;
                    x = 102 + 66;
                    y = 189 + 116;
                    break;
                case 1:
                    width = width + 43;
                    height = height + 27;
                    x = 365 + 132;
                    y = 189 + 41;
                    break;
                case 2:
                    width = width + 21;
                    height = height + 14;
                    x = 365 + 33;
                    y = 422 + 9;
                    break;
                case 3:
                    width = width + 21;
                    height = height + 14;
                    x = 561 + 35;
                    y = 422 + 9;
                    break;
                case 4:
                    width = width + 26;
                    height = height + 42;
                    x = 760 + 67;
                    y = 189 + 115;
                    break;
                case 5:
                    width = width + 20;
                    height = height + 40;
                    x = 1023 + 35;
                    y = 289 + 15;
                    break;
            }
        }
        home.flyWhiteBorder(width, height, x, y);
    }

    /**
     * joychang 设置获取焦点时icon放大凸起
     *
     * @param paramInt
     */
    private void showOnFocusAnimation(final int paramInt) {
        mv_fls[paramInt].bringToFront();// 将当前FrameLayout置为顶层
        float f1 = 1.0F;
        float f2 = 1.1F;
        this.animEffect.setAttributs(1.0F, 1.1F, f1, f2, 200L);
        Animation mAnimation = this.animEffect.createAnimation();
        mAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // settingbgs[paramInt].startAnimation(animEffect.alphaAnimation(0.0F,
                // 1.0F, 150L, 0L));
                mvLogs[paramInt].setVisibility(View.VISIBLE);
                // settingbgs[paramInt].bringToFront();
                tvs[paramInt].setVisibility(View.VISIBLE);
            }
        });
        mv_typeLogs[paramInt].startAnimation(mAnimation);
        // typeLog_bgs[paramInt].startAnimation(mAnimation);
    }

    /**
     * 失去焦点缩小
     *
     * @param paramInt
     */
    private void showLoseFocusAinimation(final int paramInt) {
        float f1 = 1.1F;
        float f2 = 1.0F;
        animEffect.setAttributs(1.1F, 1.0F, f1, f2, 200L);
        Animation mAnimation = this.animEffect.createAnimation();
        mvLogs[paramInt].setVisibility(View.GONE);
        // mAnimation.setAnimationListener(new AnimationListener() {
        // @Override
        // public void onAnimationStart(Animation animation) {}
        // @Override
        // public void onAnimationRepeat(Animation animation) {}
        // @Override
        // public void onAnimationEnd(Animation animation) {
        // settingbgs[paramInt].setVisibility(View.GONE);
        // }
        // });
        // typeLog_bgs[paramInt].startAnimation(mAnimation);
        mv_typeLogs[paramInt].startAnimation(mAnimation);

    }

    /*停止*/
    @Override
    public void onStop() {
        super.onStop();
        if (null != mQueue) {
            mQueue.stop();
        }
    }

    /*销毁时*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mQueue) {
            mQueue.cancelAll(this);
        }
    }

}
