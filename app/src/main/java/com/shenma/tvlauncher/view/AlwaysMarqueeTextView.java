package com.shenma.tvlauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 一直滚动的TextView[跑马灯效果]
 */
public class AlwaysMarqueeTextView extends TextView implements Runnable {
    private int currentScrollX;
    private boolean isMeasure = false;
    private boolean isStop = false;
    private int textWidth;

    public AlwaysMarqueeTextView(Context context) {
        super(context);
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public boolean isFocused() {
        return true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.isMeasure) {
            getTextWidth();
            this.isMeasure = true;
        }
    }

    private void getTextWidth() {
        this.textWidth = (int) getPaint().measureText(getText().toString());
    }

    public void setText(CharSequence charSequence, BufferType bufferType) {
        super.setText(charSequence, bufferType);
        this.isMeasure = false;
    }

    public void run() {
        double d = (double) this.currentScrollX;
        Double.isNaN(d);
        this.currentScrollX = (int) (d + 0.5d);
        scrollTo(this.currentScrollX, 0);
        if (!this.isStop) {
            if (getScrollX() >= this.textWidth) {
                scrollTo(-getWidth(), 0);
                this.currentScrollX = -getWidth();
            }
            postDelayed(this, 10);
        }
    }

    public void startScroll() {
        this.isStop = false;
        removeCallbacks(this);
        post(this);
    }

    public void stopScroll() {
        this.currentScrollX = 0;
        this.isStop = true;
    }

    public void startFor0() {
        this.currentScrollX = 0;
        startScroll();
    }
}
