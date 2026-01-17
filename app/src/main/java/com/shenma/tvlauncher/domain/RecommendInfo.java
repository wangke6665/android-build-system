package com.shenma.tvlauncher.domain;

import java.io.Serializable;

/**
 * @author joychang
 * @Description推荐数据
 */
public class RecommendInfo implements Serializable {
    private String id;
    private String tjinfo;//影片名称
    private String tjid;//影片id
    private String tjtype;//影片类型
    private String tjstarttime;//开始时间
    private String tjendtime;//结束时间
    private String tjpicur;//图片地址
    private String tjwei;//推荐位置编号
    private String tjurl;//影片地址
    private String state;//影片信息

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTjinfo() {
        //System.out.println("影片名称:" + this.tjinfo);
        return this.tjinfo;
    }

    public void setTjinfo(String tjinfo) {
        this.tjinfo = tjinfo;
    }

    public String getTjid() {
        return tjid;
    }

    public void setTjid(String tjid) {
        this.tjid = tjid;
    }

    public String getTjtype() {
        return tjtype;
    }

    public void setTjtype(String tjtype) {
        this.tjtype = tjtype;
    }

    public String getTjstarttime() {
        return tjstarttime;
    }

    public void setTjstarttime(String tjstarttime) {
        this.tjstarttime = tjstarttime;
    }

    public String getTjendtime() {
        return tjendtime;
    }

    public void setTjendtime(String tjendtime) {
        this.tjendtime = tjendtime;
    }

    public String getTjpicur() {
        return tjpicur;
    }

    public void setTjpicur(String tjpicur) {
        this.tjpicur = tjpicur;
    }

    public String getTjwei() {
        return tjwei;
    }

    public void setTjwei(String tjwei) {
        this.tjwei = tjwei;
    }

    public String getTjurl() {
        //System.out.println("影片地址:" + this.tjurl);
        return this.tjurl;
    }

    public void setTjurl(String tjurl) {
        this.tjurl = tjurl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "RecommendInfo [id=" + id + ", tjinfo=" + tjinfo + ", tjid="
                + tjid + ", tjtype=" + tjtype + ", tjstarttime=" + tjstarttime
                + ", tjendtime=" + tjendtime + ", tjpicur=" + tjpicur
                + ", tjwei=" + tjwei + ", tjurl=" + tjurl + ", state=" + state
                + "]";
    }
}
