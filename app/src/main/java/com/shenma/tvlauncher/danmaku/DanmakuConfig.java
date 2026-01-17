package com.shenma.tvlauncher.danmaku;

/**
 * 弹幕配置类
 */
public class DanmakuConfig {
    private int switchOn;       // 弹幕开关 0=关 1=开
    private String api;         // 弹幕解析接口
    private int defaultShow;    // 默认显示 0=关 1=开
    private int maxCount;       // 同屏最大弹幕数
    private int opacity;        // 透明度 0-100
    private int fontSize;       // 字体大小
    private int speed;          // 速度 100为正常

    public DanmakuConfig() {
        this.switchOn = 0;
        this.api = "";
        this.defaultShow = 1;
        this.maxCount = 100;
        this.opacity = 80;
        this.fontSize = 25;
        this.speed = 100;
    }

    public boolean isEnabled() {
        return switchOn == 1 && api != null && !api.isEmpty();
    }

    public boolean isDefaultShow() {
        return defaultShow == 1;
    }

    public int getSwitchOn() {
        return switchOn;
    }

    public void setSwitchOn(int switchOn) {
        this.switchOn = switchOn;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public int getDefaultShow() {
        return defaultShow;
    }

    public void setDefaultShow(int defaultShow) {
        this.defaultShow = defaultShow;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public int getOpacity() {
        return opacity;
    }

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getOpacityFloat() {
        return opacity / 100f;
    }

    public float getSpeedFactor() {
        return speed / 100f;
    }
}
