package com.shenma.tvlauncher.vod.domain;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;


public class VodUrl implements Parcelable {
    public static final Parcelable.Creator<VodUrl> CREATOR = new Parcelable.Creator<VodUrl>() {
        @Override
        public VodUrl createFromParcel(Parcel parcel) {
            return new VodUrl(parcel);
        }

        @Override
        public VodUrl[] newArray(int i) {
            return new VodUrl[i];
        }
    };
    private String duration;
    private List<VodUrlList> list;
    private List<VodUrlList> lists;
    private String name;
    private String[] parse;
    private String type;

    public VodUrl(Parcel parcel) {
        this.type = parcel.readString();
        this.name = parcel.readString();
        this.list = parcel.createTypedArrayList(VodUrlList.CREATOR);
        this.lists = parcel.createTypedArrayList(VodUrlList.CREATOR);
        this.parse = parcel.createStringArray();
        this.duration = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getDuration() {
        return this.duration;
    }

    public List<VodUrlList> getList() {
        return this.list;
    }
    public List<VodUrlList> getLists() {
        return this.lists;
    }

    public String getName() {
        return this.name;
    }

    public String[] getParse() {
        return this.parse;
    }

    public String getType() {
        return this.type;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setList(List<VodUrlList> list) {
        this.list = list;
    }
    public void setLists(List<VodUrlList> lists) {
        this.lists = lists;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParse(String[] parse) {
        this.parse = parse;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(type);
        out.writeString(name);
        out.writeTypedList(list);
        out.writeTypedList(lists);
        out.writeStringArray(parse);
        out.writeString(duration);
    }
}
