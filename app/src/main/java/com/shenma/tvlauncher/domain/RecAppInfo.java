package com.shenma.tvlauncher.domain;

public class RecAppInfo {
    //	tjinfo ---- 推荐应用名
    //	tjapk ---- 推荐的apk路径。
    //	tjtype ---- 推荐位图展示位置，1---大图，2--小图
    //	tjpicur ---- 推荐图片地址。
    private String tjinfo;
    private String tjapk;
    private String tjtype;
    private String tjpicur;
    private String packname;

    public String getPackname() {
        return packname;
    }

    public void setPackname(String packname) {
        this.packname = packname;
    }

    public String getTjinfo() {
        return tjinfo;
    }

    public void setTjinfo(String tjinfo) {
        this.tjinfo = tjinfo;
    }

    public String getTjapk() {
        return tjapk;
    }

    public void setTjapk(String tjapk) {
        this.tjapk = tjapk;
    }

    public String getTjtype() {
        return tjtype;
    }

    public void setTjtype(String tjtype) {
        this.tjtype = tjtype;
    }

    public String getTjpicur() {
        return tjpicur;
    }

    public void setTjpicur(String tjpicur) {
        this.tjpicur = tjpicur;
    }

}
