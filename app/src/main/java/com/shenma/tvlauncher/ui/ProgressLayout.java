package com.shenma.tvlauncher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.shenma.tvlauncher.R;

import java.util.ArrayList;
import java.util.List;

public class ProgressLayout extends RelativeLayout {

    private static final String TAG_PROGRESS = "ProgressLayout.TAG_PROGRESS";

    public enum State {
        CONTENT, PROGRESS, EMPTY
    }

    private List<View> mContentViews;
    private View mProgressView;
    private View mEmptyView;
    private TextView mSpeedTextView;
    private ImageView mLoadingGifView;
    private State mState;

    public ProgressLayout(Context context) {
        super(context);
    }

    public ProgressLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mState = State.CONTENT;
        mContentViews = new ArrayList<>();
        initView();
    }

    private void initView() {
        mEmptyView = LayoutInflater.from(getContext()).inflate(R.layout.view_empty, null);
        mEmptyView.setTag(TAG_PROGRESS);
        mEmptyView.setVisibility(GONE);
        mProgressView = LayoutInflater.from(getContext()).inflate(R.layout.view_progress, null);
        mProgressView.setTag(TAG_PROGRESS);
        mProgressView.setVisibility(GONE);
        
        mSpeedTextView = mProgressView.findViewById(R.id.tv_speed_info);
        mLoadingGifView = mProgressView.findViewById(R.id.iv_loading_gif);
        
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
        addView(mProgressView, params);
        addView(mEmptyView, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child.getTag() == null || !child.getTag().equals(TAG_PROGRESS)) {
            mContentViews.add(child);
        }
    }

    public void showProgress() {
        if (mLoadingGifView != null) {
            mLoadingGifView.setVisibility(VISIBLE);
        }
        if (mSpeedTextView != null) {
            mSpeedTextView.setVisibility(VISIBLE);
        }
        switchState(State.PROGRESS);
        
        if (mProgressView != null) {
            mProgressView.bringToFront();
            bringChildToFront(mProgressView);
        }
        
        if (getParent() instanceof ViewGroup) {
            final ViewGroup parent = (ViewGroup) getParent();
            parent.bringChildToFront(this);
            
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child != null && child.getId() != 0) {
                    try {
                        String resourceName = getContext().getResources().getResourceName(child.getId());
                        if (resourceName != null && resourceName.contains("tv_progress_time")) {
                            child.setVisibility(View.GONE);
                            int progressIndex = parent.indexOfChild(this);
                            int timeIndex = parent.indexOfChild(child);
                            if (progressIndex >= 0 && timeIndex >= 0 && timeIndex < progressIndex) {
                                parent.removeViewAt(timeIndex);
                                parent.addView(child, progressIndex + 1);
                            }
                            break;
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        }
        
        post(new Runnable() {
            @Override
            public void run() {
                if (mProgressView != null) {
                    mProgressView.bringToFront();
                    bringChildToFront(mProgressView);
                }
                if (getParent() instanceof ViewGroup) {
                    ViewGroup parent = (ViewGroup) getParent();
                    parent.bringChildToFront(ProgressLayout.this);
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        View child = parent.getChildAt(i);
                        if (child != null && child.getId() != 0) {
                            try {
                                String resourceName = getContext().getResources().getResourceName(child.getId());
                                if (resourceName != null && resourceName.contains("tv_progress_time")) {
                                    child.setVisibility(View.GONE);
                                    int progressIndex = parent.indexOfChild(ProgressLayout.this);
                                    int timeIndex = parent.indexOfChild(child);
                                    if (progressIndex >= 0 && timeIndex >= 0 && timeIndex < progressIndex) {
                                        parent.removeViewAt(timeIndex);
                                        parent.addView(child, progressIndex + 1);
                                    }
                                    break;
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                    }
                    parent.invalidate();
                    ProgressLayout.this.invalidate();
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if (getParent() instanceof ViewGroup) {
                                ViewGroup p = (ViewGroup) getParent();
                                p.bringChildToFront(ProgressLayout.this);
                                if (mProgressView != null) {
                                    mProgressView.bringToFront();
                                    bringChildToFront(mProgressView);
                                }
                                p.invalidate();
                                ProgressLayout.this.invalidate();
                            }
                        }
                    });
                }
            }
        });
    }

    public void showEmpty() {
        switchState(State.EMPTY);
    }

    public void showContent() {
        switchState(State.CONTENT);
    }

    public void showContent(boolean flag, int size) {
        if (flag && size == 0) showEmpty();
        else showContent();
    }

    public boolean isProgress() {
        return mState == State.PROGRESS;
    }

    public boolean isContent() {
        return mState == State.CONTENT;
    }

    public boolean isEmpty() {
        return mState == State.EMPTY;
    }

    public void switchState(State state) {
        if (mState == state) return;
        mState = state;
        switch (state) {
            case CONTENT:
                mEmptyView.setVisibility(GONE);
                mProgressView.setVisibility(GONE);
                setContentVisibility(true);
                break;
            case PROGRESS:
                mEmptyView.setVisibility(GONE);
                mProgressView.setVisibility(VISIBLE);
                if (mSpeedTextView != null) {
                    mSpeedTextView.setVisibility(VISIBLE);
                }
                setContentVisibility(false);
                mProgressView.bringToFront();
                bringChildToFront(mProgressView);
                break;
            case EMPTY:
                mEmptyView.setVisibility(VISIBLE);
                mProgressView.setVisibility(GONE);
                setContentVisibility(false);
                break;
        }
    }

    private void setContentVisibility(boolean visible) {
        for (View view : mContentViews) {
            if (visible) showView(view);
            else hideView(view);
        }
    }

    private void showView(View view) {
        view.setAlpha(0f);
        view.setVisibility(VISIBLE);
        view.animate().alpha(1f).setDuration(100);
    }

    private void hideView(View view) {
        view.setVisibility(GONE);
    }

    public void updateSpeedInfo(String speedText) {
        if (mSpeedTextView != null) {
            if (speedText != null && !speedText.isEmpty()) {
                mSpeedTextView.setText(speedText);
                mSpeedTextView.setVisibility(VISIBLE);
            } else {
                mSpeedTextView.setVisibility(GONE);
            }
        }
    }

    public void hideSpeedInfo() {
        if (mSpeedTextView != null) {
            mSpeedTextView.setVisibility(GONE);
        }
    }

    public void setLoadingGif(int gifResId) {
        if (mLoadingGifView != null && gifResId != 0) {
            try {
                Context context = getContext();
                if (context != null) {
                    Glide.with(context).load(gifResId).into(mLoadingGifView);
                } else {
                    mLoadingGifView.setImageResource(gifResId);
                }
            } catch (Exception e) {
                try {
                    mLoadingGifView.setImageResource(gifResId);
                } catch (Exception ex) {
                    // Ignore
                }
            }
        }
    }

    public void setLoadingGif(String gifPath) {
        if (mLoadingGifView != null && gifPath != null && !gifPath.isEmpty()) {
            try {
                Context context = getContext();
                if (context != null) {
                    Glide.with(context).load(gifPath).into(mLoadingGifView);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}