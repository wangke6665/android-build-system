package com.shenma.tvlauncher.domain;

public class WallpaperInfo {
    private String id;//编号
    private String skinname;//名称
    private String skinpath;//下载地址
    private String status;//

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSkinname() {
        return skinname;
    }

    public void setSkinname(String skinname) {
        this.skinname = skinname;
    }

    public String getSkinpath() {
        return skinpath;
    }

    public void setSkinpath(String skinpath) {
        this.skinpath = skinpath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "WallpaperInfo [id=" + id + ", skinname=" + skinname
                + "skinpath=" + skinpath + ", state=" + status
                + "]";
    }

}
