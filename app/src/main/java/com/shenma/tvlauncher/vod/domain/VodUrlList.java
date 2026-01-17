package com.shenma.tvlauncher.vod.domain;

import android.os.Parcel;
import android.os.Parcelable;

public class VodUrlList implements Parcelable {
    public static final Creator<VodUrlList> CREATOR = new Creator<VodUrlList>() {
        @Override
        public VodUrlList createFromParcel(Parcel parcel) {
            return new VodUrlList(parcel);
        }

        @Override
        public VodUrlList[] newArray(int i) {
            return new VodUrlList[i];
        }
    };
    private static final long serialVersionUID = -27350910593881993L;
    public String title;
    public String url;

    public VodUrlList() {
        url = "*";
    }

    public VodUrlList(Parcel parcel) {
        url = "*";
        title = parcel.readString();
        url = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }



    public String toString() {
        return "VodUrl [title=" + this.title + ", url=" + this.url + "]";
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.title);
        parcel.writeString(this.url);
    }
}
