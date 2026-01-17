package com.shenma.tvlauncher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.UserActivity;
import com.shenma.tvlauncher.dao.TVStationDao;
import com.shenma.tvlauncher.dao.bean.TVSCollect;
import com.shenma.tvlauncher.utils.Constant;
import com.shenma.tvlauncher.utils.ScaleAnimEffect;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author joychang
 * @Description 网络电视
 */
public class TVFragment extends BaseFragment {

    public ImageView[] tv_typeLogs;
    ScaleAnimEffect animEffect;
    private FrameLayout[] tv_fls;
    private ImageView[] tvbgs;
    private View view;
    private TVStationDao dao;
    private List<TVSCollect> tvs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dao = TVStationDao.getInstance(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        if (null == view) {
            view = inflater.inflate(R.layout.layout_tv, container, false);
            init();
        } else {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            if (viewGroup != null)
                viewGroup.removeView(view);
        }
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() {
        loadViewLayout();
        findViewById();
        setListener();
    }

    protected void loadViewLayout() {
        tv_fls = new FrameLayout[1];
        tv_typeLogs = new ImageView[1];
        tvbgs = new ImageView[1];
        animEffect = new ScaleAnimEffect();
    }

    protected void findViewById() {
        tv_fls[0] = (FrameLayout) view.findViewById(R.id.tv_fl_re_0);
        tv_typeLogs[0] = (ImageView) view.findViewById(R.id.tv_iv_livetv);
        tvbgs[0] = (ImageView) view.findViewById(R.id.tv_bg_0);
    }

    protected void setListener() {
        initClickListener();
    }

    private void initClickListener() {
        for (int i = 0; i < tv_typeLogs.length; i++) {
            tvbgs[i].setVisibility(View.GONE);
            tv_typeLogs[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent();
                    switch (v.getId()) {
                        case R.id.tv_iv_livetv:
                            //直播
                            i.setClass(home, UserActivity.class);
                            i.putExtra("TVTYPE", Constant.TVLIVE);
                            startActivity(i);
                            break;
                    }
                    home.overridePendingTransition(android.R.anim.fade_in,
                            android.R.anim.fade_out);
                }
            });
            //if(ISTV){
            tv_typeLogs[i].setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    int paramInt = 0;
                    switch (v.getId()) {
                        case R.id.tv_iv_livetv:
                            paramInt = 0;
                            // 此处设置不同大小的item的长宽 ,以及框飞入的X和Y轴.
                            break;
                    }
                    if (hasFocus) {
                        showOnFocusAnimation(paramInt);
                        if (null != home.whiteBorder) {
                            home.whiteBorder.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showLoseFocusAinimation(paramInt);
                        //将白框隐藏
                    }
                }
            });
        }
        //}
    }

    /**
     * joychang 设置获取焦点时icon放大凸起
     *
     * @param paramInt
     */
    private void showOnFocusAnimation(final int paramInt) {
        tv_fls[paramInt].bringToFront();//将当前FrameLayout置为顶层
        float f1 = 1.0F;
        float f2 = 1.1F;
        animEffect.setAttributs(1.0F, 1.1F, f1, f2, 200L);
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
                tvbgs[paramInt].setVisibility(View.VISIBLE);
            }
        });
        tv_typeLogs[paramInt].startAnimation(mAnimation);
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
        tv_typeLogs[paramInt].startAnimation(mAnimation);
        tvbgs[paramInt].setVisibility(View.GONE);
//		mAnimation.setAnimationListener(new AnimationListener() {
//			@Override
//			public void onAnimationStart(Animation animation) {}
//			@Override
//			public void onAnimationRepeat(Animation animation) {}
//			@Override
//			public void onAnimationEnd(Animation animation) {
//				tvbgs[paramInt].setVisibility(View.GONE);
//			}
//		});


    }

    @Override
    public void onResume() {
        tvs = dao.queryAllTvsi();
        //每次onResume时，更新服务端返回的数据
        super.onResume();
    }


}

