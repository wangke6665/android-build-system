package com.shenma.tvlauncher.vod.domain;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/***
 * @Description 媒体数据info
 * @author joychang
 *
 */

//public class VideoInfo implements Parcelable {
//    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {
//        @Override
//        public VideoInfo createFromParcel(Parcel parcel) {
//            Bundle readBundle = parcel.readBundle();
//            VideoInfo videoInfo = new VideoInfo();
//            videoInfo.title = readBundle.getString("title");
//            videoInfo.url = readBundle.getString(IjkMediaPlayer.OnNativeInvokeListener.ARG_URL);
//            return videoInfo;
//        }
//
//        @Override
//        public VideoInfo[] newArray(int i) {
//            return new VideoInfo[i];
//        }
//    };
//    public String title;
//    public String url;
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel parcel, int i) {
//        Bundle bundle = new Bundle();
//        bundle.putString("title", title);
//        bundle.putString(IjkMediaPlayer.OnNativeInvokeListener.ARG_URL, url);
//        parcel.writeBundle(bundle);
//    }
//}

public class VideoInfo implements Parcelable {
    public static final Parcelable.Creator<VideoInfo> CREATOR = new Parcelable.Creator<VideoInfo>() {

        @Override
        public VideoInfo createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();
            VideoInfo data = new VideoInfo();
            data.title = bundle.getString("title");
            data.url = bundle.getString("url");
//            data.url = bundle.getString(IjkMediaPlayer.OnNativeInvokeListener.ARG_URL);
            return data;
        }

        @Override
        public VideoInfo[] newArray(int size) {
            return new VideoInfo[size];
        }

    };
    public String title;
    public String url;

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
//        bundle.putString("url", url);
        bundle.putString(IjkMediaPlayer.OnNativeInvokeListener.ARG_URL, url);
        dest.writeBundle(bundle);
    }

}
