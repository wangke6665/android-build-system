package com.shenma.tvlauncher.utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 屏幕适配工具类
 * 基于设计稿尺寸（1280x720）进行等比例缩放
 * 解决不同分辨率屏幕下UI显示不一致的问题
 */
public class ScreenAdapterUtil {

    // 设计稿基准尺寸（横屏模式：宽x高）
    private static final float DESIGN_WIDTH = 1280f;
    private static final float DESIGN_HEIGHT = 720f;

    private static ScreenAdapterUtil instance;
    private Context appContext;
    
    // 屏幕实际尺寸
    private int screenWidth;
    private int screenHeight;
    
    // 缩放比例
    private float scaleX;
    private float scaleY;
    private float scale; // 取较小值保持比例

    private ScreenAdapterUtil() {}

    public static ScreenAdapterUtil getInstance() {
        if (instance == null) {
            synchronized (ScreenAdapterUtil.class) {
                if (instance == null) {
                    instance = new ScreenAdapterUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化（在Application或Activity中调用）
     */
    public void init(Context context) {
        this.appContext = context.getApplicationContext();
        updateScreenSize(context);
    }

    /**
     * 更新屏幕尺寸（在Activity onResume或配置变化时调用）
     */
    public void updateScreenSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        
        // 确保横屏模式下宽度大于高度
        screenWidth = Math.max(dm.widthPixels, dm.heightPixels);
        screenHeight = Math.min(dm.widthPixels, dm.heightPixels);
        
        // 计算缩放比例
        scaleX = screenWidth / DESIGN_WIDTH;
        scaleY = screenHeight / DESIGN_HEIGHT;
        scale = Math.min(scaleX, scaleY); // 取较小值保持宽高比
    }

    /**
     * 获取屏幕宽度
     */
    public int getScreenWidth() {
        return screenWidth;
    }

    /**
     * 获取屏幕高度
     */
    public int getScreenHeight() {
        return screenHeight;
    }

    /**
     * 获取水平缩放比例
     */
    public float getScaleX() {
        return scaleX;
    }

    /**
     * 获取垂直缩放比例
     */
    public float getScaleY() {
        return scaleY;
    }

    /**
     * 获取统一缩放比例（保持宽高比）
     */
    public float getScale() {
        return scale;
    }

    /**
     * 根据设计稿尺寸获取实际像素值（基于宽度缩放）
     * @param designPx 设计稿中的像素值
     * @return 实际屏幕像素值
     */
    public int getScaledWidth(float designPx) {
        return Math.round(designPx * scaleX);
    }

    /**
     * 根据设计稿尺寸获取实际像素值（基于高度缩放）
     * @param designPx 设计稿中的像素值
     * @return 实际屏幕像素值
     */
    public int getScaledHeight(float designPx) {
        return Math.round(designPx * scaleY);
    }

    /**
     * 根据设计稿尺寸获取实际像素值（统一缩放，保持比例）
     * @param designPx 设计稿中的像素值
     * @return 实际屏幕像素值
     */
    public int getScaledSize(float designPx) {
        return Math.round(designPx * scale);
    }

    /**
     * 获取按屏幕宽度百分比计算的尺寸
     * @param percent 百分比（0-100）
     * @return 实际像素值
     */
    public int getWidthPercent(float percent) {
        return Math.round(screenWidth * percent / 100f);
    }

    /**
     * 获取按屏幕高度百分比计算的尺寸
     * @param percent 百分比（0-100）
     * @return 实际像素值
     */
    public int getHeightPercent(float percent) {
        return Math.round(screenHeight * percent / 100f);
    }

    /**
     * 设置View的尺寸（基于设计稿尺寸自动缩放）
     * @param view 目标View
     * @param designWidth 设计稿宽度（-1表示wrap_content，-2表示match_parent）
     * @param designHeight 设计稿高度（-1表示wrap_content，-2表示match_parent）
     */
    public void setViewSize(View view, float designWidth, float designHeight) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        
        if (designWidth == -1) {
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else if (designWidth == -2) {
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            params.width = getScaledWidth(designWidth);
        }
        
        if (designHeight == -1) {
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else if (designHeight == -2) {
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else {
            params.height = getScaledHeight(designHeight);
        }
        
        view.setLayoutParams(params);
    }

    /**
     * 设置View的margin（基于设计稿尺寸自动缩放）
     */
    public void setViewMargin(View view, float left, float top, float right, float bottom) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) params;
            marginParams.leftMargin = getScaledWidth(left);
            marginParams.topMargin = getScaledHeight(top);
            marginParams.rightMargin = getScaledWidth(right);
            marginParams.bottomMargin = getScaledHeight(bottom);
            view.setLayoutParams(marginParams);
        }
    }

    /**
     * 设置View的padding（基于设计稿尺寸自动缩放）
     */
    public void setViewPadding(View view, float left, float top, float right, float bottom) {
        view.setPadding(
            getScaledWidth(left),
            getScaledHeight(top),
            getScaledWidth(right),
            getScaledHeight(bottom)
        );
    }

    /**
     * 设置TextView的字体大小（基于设计稿尺寸自动缩放）
     * @param textView 目标TextView
     * @param designSp 设计稿中的sp值
     */
    public void setTextSize(TextView textView, float designSp) {
        // 使用scale来缩放字体，保持与UI元素的比例一致
        float scaledSp = designSp * scale;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSp);
    }

    /**
     * 设置TextView的字体大小（使用像素值）
     * @param textView 目标TextView
     * @param designPx 设计稿中的像素值
     */
    public void setTextSizePx(TextView textView, float designPx) {
        float scaledPx = designPx * scale;
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, scaledPx);
    }

    /**
     * 获取缩放后的字体大小（sp）
     * @param designSp 设计稿中的sp值
     * @return 缩放后的sp值
     */
    public float getScaledTextSize(float designSp) {
        return designSp * scale;
    }

    /**
     * 适配卡片尺寸 - 用于无界UI底部推荐卡片
     * 根据屏幕宽度动态计算卡片宽度，确保显示合适数量的卡片
     * @param cardCount 期望在屏幕中显示的卡片数量
     * @param spacing 卡片之间的间距（设计稿尺寸）
     * @param padding 容器左右padding（设计稿尺寸）
     * @return 单个卡片的宽度
     */
    public int getCardWidth(int cardCount, float spacing, float padding) {
        int totalPadding = getScaledWidth(padding * 2);
        int totalSpacing = getScaledWidth(spacing * (cardCount - 1));
        int availableWidth = screenWidth - totalPadding - totalSpacing;
        return availableWidth / cardCount;
    }

    /**
     * 适配卡片高度 - 保持宽高比
     * @param cardWidth 卡片宽度
     * @param aspectRatio 宽高比（宽/高），如16:9则传入16f/9f
     * @return 卡片高度
     */
    public int getCardHeight(int cardWidth, float aspectRatio) {
        return Math.round(cardWidth / aspectRatio);
    }

    /**
     * 获取底部推荐区域高度（基于屏幕高度百分比）
     * @param percent 占屏幕高度的百分比
     * @return 实际像素值
     */
    public int getBottomAreaHeight(float percent) {
        return getHeightPercent(percent);
    }

    /**
     * dp转px
     */
    public int dp2px(float dp) {
        if (appContext == null) return (int) dp;
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, 
            appContext.getResources().getDisplayMetrics()
        );
    }

    /**
     * sp转px
     */
    public int sp2px(float sp) {
        if (appContext == null) return (int) sp;
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp,
            appContext.getResources().getDisplayMetrics()
        );
    }

    /**
     * 打印当前屏幕适配信息（调试用）
     */
    public String getDebugInfo() {
        return String.format(
            "Screen: %dx%d, Scale: %.2f (X:%.2f, Y:%.2f), Design: %.0fx%.0f",
            screenWidth, screenHeight, scale, scaleX, scaleY, DESIGN_WIDTH, DESIGN_HEIGHT
        );
    }
}
