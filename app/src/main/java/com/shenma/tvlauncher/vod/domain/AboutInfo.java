package com.shenma.tvlauncher.vod.domain;

import java.io.Serializable;
import java.util.List;

public class AboutInfo implements Serializable {
    private List<VodDataInfo> actor;
    private List<VodDataInfo> similary;

    public List<VodDataInfo> getSimilary() {
        return this.similary;
    }

    public void setSimilary(List<VodDataInfo> similary) {
        this.similary = similary;
    }

    public List<VodDataInfo> getActor() {
        return this.actor;
    }

    public void setActor(List<VodDataInfo> actor) {
        this.actor = actor;
    }

    public String toString() {
        return "AboutInfo [similary=" + this.similary + ", actor=" + this.actor + "]";
    }
}
