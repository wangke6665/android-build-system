package com.shenma.tvlauncher.vod.domain;

import java.io.Serializable;

public class MediaInfo implements Serializable {
    private String mediaurl;
    private String name;
    private String type;

    public String getMediaurl() {
        return this.mediaurl;
    }

    public void setMediaurl(String mediaurl) {
        this.mediaurl = mediaurl;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return "MediaInfo [mediaurl=" + this.mediaurl + ", name=" + this.name + ", type=" + this.type + "]";
    }
}
