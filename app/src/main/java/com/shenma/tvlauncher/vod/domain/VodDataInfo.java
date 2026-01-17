package com.shenma.tvlauncher.vod.domain;

public class VodDataInfo {
    private String nextlink;
    private String pic;
    private String state;
    private String title;
    private String type;
    private String score;
    private String blurb;
    private String pic_slide;


    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNextlink() {
        return this.nextlink;
    }

    public void setNextlink(String nextlink) {
        this.nextlink = nextlink;
    }

    public String getPic() {
        return this.pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getBlurb() {
        return this.blurb;
    }

    public String getPic_slide() {
        return this.pic_slide;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getScore() {
        return this.score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String toString() {
        return "VodDataInfo [title=" + this.title
                + ", nextlink=" + this.nextlink
                + ", pic=" + this.pic
                + ", blurb=" + this.blurb
                + ", pic_slide=" + this.pic_slide
                + ", state=" + this.state
                + ", type=" + this.type
                + ", score=" + this.score + "]";
    }
}
