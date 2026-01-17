package com.shenma.tvlauncher.tvlive.parsexml;

import java.util.ArrayList;

public class NetMedia {

    public int level;
    private String channleClass = "";
    private String channlename = "";
    private String link = "";
    private String source = "";
    private int totlePosition = 0;
    ;
    private ArrayList<NetMedia> channlesList = new ArrayList<NetMedia>();
    private String epg = "";

    public String getEpg() {
        return epg;
    }

    public void setEpg(String epg) {
        this.epg = epg;
    }

    public ArrayList<NetMedia> getChannlesArrayList() {
        return channlesList;
    }

    public void setChannlesArrayList(ArrayList<NetMedia> channlesArrayList) {
        this.channlesList = channlesArrayList;
    }

    public String getChannleClass() {
        return channleClass;
    }

    public void setChannleClass(String channleClass) {
        this.channleClass = channleClass;
    }

    public String getChannlename() {
        return channlename;
    }

    public void setChannlename(String channlename) {
        this.channlename = channlename;
    }

    public int getTotlePosition() {
        return totlePosition;
    }

    public void setTotlePosition(int totlePosition) {
        this.totlePosition = totlePosition;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "NetMedia [channleClass=" + channleClass + ", channlename="
                + channlename + ", link=" + link + ", source=" + source + "]";
    }

}
