package com.shenma.tvlauncher.vod.domain;

import java.io.Serializable;
import java.util.List;


public class VodFilter implements Serializable {
    private List<VodFilterInfo> flitter;

    public List<VodFilterInfo> getFlitter() {
        return this.flitter;
    }

    public String toString() {
        return "VodFilter [flitter=" + this.flitter
                + "]";
    }
}


/*public class VodFilter implements Serializable {
    private List<VodFilterInfo> tvplay;
    private List<VodFilterInfo> movie;
    private List<VodFilterInfo> tvshow;
    private List<VodFilterInfo> comic;
    private List<VodFilterInfo> hanguoju;
    private List<VodFilterInfo> oumeiju;
    private List<VodFilterInfo> movie_4k;
    private List<VodFilterInfo> movie_zb;
    private List<VodFilterInfo> flitter;




    public List<VodFilterInfo> getFlitter() {
        return this.flitter;
    }

    public void setFlitter(List<VodFilterInfo> flitter) {
        this.flitter = flitter;
    }

    public List<VodFilterInfo> getTvplay() {
        return this.tvplay;
    }

    public void setTvplay(List<VodFilterInfo> tvplay) {
        this.tvplay = tvplay;
    }

    public List<VodFilterInfo> getMovie() {
        return this.movie;
    }

    public void setMovie(List<VodFilterInfo> movie) {
        this.movie = movie;
    }

    public List<VodFilterInfo> getTvshow() {
        return this.tvshow;
    }

    public void setTvshow(List<VodFilterInfo> tvshow) {
        this.tvshow = tvshow;
    }

    public List<VodFilterInfo> getComic() {
        return this.comic;
    }

    public void setComic(List<VodFilterInfo> comic) {
        this.comic = comic;
    }

    public List<VodFilterInfo> getOumeiju()
    {
        return this.oumeiju;
    }

    public void setoumeiju(List<VodFilterInfo> oumeiju) {
        this.oumeiju = oumeiju;
    }

    public List<VodFilterInfo> getHanguoju()
    {
        return this.hanguoju;
    }

    public void setHanguoju(List<VodFilterInfo> hanguoju) {
        this.hanguoju = hanguoju;
    }

    public List<VodFilterInfo> getMovie_4k() {
        return this.movie_4k;
    }

    public void setMovie_4k(List<VodFilterInfo> movie_4k) {
        this.movie_4k = movie_4k;
    }

    public List<VodFilterInfo> getMovie_zb() {
        return this.movie_zb;
    }

    public void setMovie_ZB(List<VodFilterInfo> movie_zb) {
        this.movie_zb = movie_zb;
    }

    public String toString() {
        return "VodFilter [tvplay=" + this.tvplay
                + ", movie=" + this.movie
                + ", tvshow=" + this.tvshow
                + ", comic=" + this.comic
                + ", hanguoju=" + this.hanguoju
                + ", oumeiju=" + this.oumeiju
                + ", movie_4k=" + this.movie_4k
                + ", movie_zb=" + this.movie_zb
                + ", flitter=" + this.flitter
                + "]";
    }

}
*/