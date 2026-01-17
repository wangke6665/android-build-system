package com.shenma.tvlauncher.vod.domain;

import java.util.Arrays;
import java.util.List;

public class VideoDetailInfo {
    private AboutInfo about;
    private String[] actor;
    private String[] area;
    private String cur_episode;
    private String[] director;
    private String foreign_ip;
    private String id;
    private String img_url;
    private String intro;
    private String is_finish;
    private String max_episode;
    private String play_filter;
    private String pubtime;
    private String raing;
    private String season_num;
    private String title;
    private String trunk;
    private String[] type;
//    private VideoList videolist;
    private List<VodUrl> video_list;
    private String top_type;

    public AboutInfo getAbout() {
        return this.about;
    }

    public void setAbout(AboutInfo about) {
        this.about = about;
    }

    public String[] getActor() {
        return this.actor;
    }

    public void setActor(String[] actor) {
        this.actor = actor;
    }

    public String[] getDirector() {
        return this.director;
    }

    public void setDirector(String[] director) {
        this.director = director;
    }

    public String[] getArea() {
        return this.area;
    }

    public void setArea(String[] area) {
        this.area = area;
    }

    public String[] getType() {
        return this.type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public String getTop_type() {
        return this.top_type;
    }

    public void setTop_type(String top_type) {
        this.top_type = top_type;
    }

//    public VideoList getVideolist() {
//        return this.videolist;
//    }

//    public void setVideolist(VideoList videolist) {
//        this.videolist = videolist;
//    }

    public List<VodUrl> getVideo_list() {
        return this.video_list;
    }

    public void setVideo_list(List<VodUrl> list) {
        this.video_list = list;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTrunk() {
        return this.trunk;
    }

    public void setTrunk(String trunk) {
        this.trunk = trunk;
    }

    public String getImg_url() {
        return this.img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getIntro() {
        return this.intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getIs_finish() {
        return this.is_finish;
    }

    public void setIs_finish(String is_finish) {
        this.is_finish = is_finish;
    }

    public String getPubtime() {
        return this.pubtime;
    }

    public void setPubtime(String pubtime) {
        this.pubtime = pubtime;
    }

    public String getCur_episode() {
        return this.cur_episode;
    }

    public void setCur_episode(String cur_episode) {
        this.cur_episode = cur_episode;
    }

    public String getMax_episode() {
        return this.max_episode;
    }

    public void setMax_episode(String max_episode) {
        this.max_episode = max_episode;
    }

    public String getSeason_num() {
        return this.season_num;
    }

    public void setSeason_num(String season_num) {
        this.season_num = season_num;
    }

    public String getRaing() {
        return this.raing;
    }

    public void setRaing(String raing) {
        this.raing = raing;
    }

    public String getPlay_filter() {
        return this.play_filter;
    }

    public void setPlay_filter(String play_filter) {
        this.play_filter = play_filter;
    }

    public String getForeign_ip() {
        return this.foreign_ip;
    }

    public void setForeign_ip(String foreign_ip) {
        this.foreign_ip = foreign_ip;
    }


    public String toString() {
        return "VideoDetailInfo [id=" + this.id
                + ", title=" + this.title
                + ", top_type=" + this.top_type
                + ", trunk=" + this.trunk
                + ", img_url=" + this.img_url
                + ", intro=" + this.intro
                + ", is_finish=" + this.is_finish
                + ", pubtime=" + this.pubtime
                + ", cur_episode=" + this.cur_episode
                + ", max_episode=" + this.max_episode
                + ", season_num=" + this.season_num
                + ", raing=" + this.raing
                + ", play_filter=" + this.play_filter
                + ", foreign_ip=" + this.foreign_ip
                + ", actor=" + Arrays.toString(this.actor)
                + ", director=" + Arrays.toString(this.director)
                + ", area=" + Arrays.toString(this.area)
                + ", type=" + Arrays.toString(this.type)
                + ", about=" + this.about
//                + ", videolist=" + this.videolist
                + ", video_list=" + this.video_list
                + "]";
    }
}
