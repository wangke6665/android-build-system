package com.shenma.tvlauncher.vod.db;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;

/**
 * @author joychang
 * @Descripton 用户记录数据库
 */

/*
@Table(name = "albums")
public class Album implements Parcelable {
    public static final Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();
            Album data = new Album();
            data.id = bundle.getInt("ID");
            data.playIndex = bundle.getInt("PlayIndex");
            data.collectionTime = bundle.getInt("CollectionTime");
            data.typeId = bundle.getInt("TypeId");
            data.albumId = bundle.getString("AlbumId");
            data.albumType = bundle.getString("AlbumType");
            data.albumSourceType = bundle.getString("AlbumSourceType");
            data.albumPic = bundle.getString("AlbumPic");
            data.albumTitle = bundle.getString("AlbumTitle");
            data.albumState = bundle.getString("AlbumState");
            data.nextLink = bundle.getString("NextLink");
            return data;
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    private static final long serialVersionUID = 3070028055291587236L;
    @Id(column = "id")// 数据库主键
    private int id;
    private String albumId;// 影片ID
    private int playIndex;// 剧集标
    private int collectionTime;// 上次播放时间点
    private int typeId;// typeId:0:追剧 1：收藏 2：记录
    private String albumType;// 影片类型
    private String albumSourceType;// 源类型
    private String albumTitle;// 影片名称
    private String albumState;// 影片更新
    private String albumPic;// 影片图片路径
    private String nextLink;// 影片路径


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public int getPlayIndex() {
        return this.playIndex;
    }

    public void setPlayIndex(int playIndex) {
        this.playIndex = playIndex;
    }

    public int getCollectionTime() {
        return this.collectionTime;
    }

    public void setCollectionTime(int collectionTime) {
        this.collectionTime = collectionTime;
    }

    public int getTypeId() {
        return this.typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getAlbumType() {
        return this.albumType;
    }

    public void setAlbumType(String albumType) {
        this.albumType = albumType;
    }

    public String getAlbumSourceType() {
        return this.albumSourceType;
    }

    public void setAlbumSourceType(String albumSourceType) {
        this.albumSourceType = albumSourceType;
    }

    public String getAlbumTitle() {
        return this.albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getAlbumState() {
        return this.albumState;
    }

    public void setAlbumState(String albumState) {
        this.albumState = albumState;
    }

    public String getAlbumPic() {
        return this.albumPic;
    }

    public void setAlbumPic(String albumPic) {
        this.albumPic = albumPic;
    }

    public String getNextLink() {
        return this.nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    @Override
    public String toString() {
        return "Album [id=" + this.id
                + ", albumId=" + this.albumId
                + ", playIndex=" + this.playIndex
                + ", collectionTime=" + this.collectionTime
                + ", typeId=" + this.typeId
                + ", albumType=" + this.albumType
                + ", albumSourceType=" + this.albumSourceType
                + ", albumTitle=" + this.albumTitle
                + ", albumState=" + this.albumState
                + ", albumPic=" + this.albumPic
                + ", nextLink=" + this.nextLink
                + "]";
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt("ID", this.id);
        bundle.putInt("PlayIndex", this.playIndex);
        bundle.putInt("CollectionTime", this.collectionTime);
        bundle.putInt("TypeId", this.typeId);
        bundle.putString("AlbumId", this.albumId);
        bundle.putString("AlbumType", this.albumType);
        bundle.putString("AlbumSourceType", this.albumSourceType);
        bundle.putString("AlbumPic", this.albumPic);
        bundle.putString("AlbumTitle", this.albumTitle);
        bundle.putString("AlbumState", this.albumState);
        bundle.putString("NextLink", this.nextLink);
        dest.writeBundle(bundle);
    }
}
*/

@Table(name = "albums")
public class Album implements Parcelable {
    public static final Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            Bundle bundle = source.readBundle();
            Album data = new Album();
            data.id = bundle.getInt("ID");
            data.playIndex = bundle.getInt("PlayIndex");
            data.collectionTime = bundle.getInt("CollectionTime");
            data.typeId = bundle.getInt("TypeId");
            data.albumId = bundle.getString("AlbumId");
            data.albumType = bundle.getString("AlbumType");
            data.albumSourceType = bundle.getString("AlbumSourceType");
            data.albumPic = bundle.getString("AlbumPic");
            data.albumTitle = bundle.getString("AlbumTitle");
            data.albumState = bundle.getString("AlbumState");
            data.nextLink = bundle.getString("NextLink");
            data.time = bundle.getString("Time");
            return data;
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    //private static final long serialVersionUID = 3070028055291587236L;//不知道是什么玩意启用就报错
    @Id(column = "id")// 数据库主键
    private int id;
    private String albumId;// 影片ID
    private int playIndex;// 剧集标
    private int collectionTime;// 上次播放时间点
    private int typeId;// typeId:0:追剧 1：收藏 2：记录
    private String albumType;// 影片类型
    private String albumSourceType;// 源类型
    private String albumTitle;// 影片名称
    private String albumState;// 影片更新
    private String albumPic;// 影片图片路径
    private String nextLink;// 影片路径
    private String time;// 最后观看时间


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public int getPlayIndex() {
        return this.playIndex;
    }

    public void setPlayIndex(int playIndex) {
        this.playIndex = playIndex;
    }

    public int getCollectionTime() {
        return this.collectionTime;
    }

    public void setCollectionTime(int collectionTime) {
        this.collectionTime = collectionTime;
    }

    public int getTypeId() {
        return this.typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getAlbumType() {
        return this.albumType;
    }

    public void setAlbumType(String albumType) {
        this.albumType = albumType;
    }

    public String getAlbumSourceType() {
        return this.albumSourceType;
    }

    public void setAlbumSourceType(String albumSourceType) {
        this.albumSourceType = albumSourceType;
    }

    public String getAlbumTitle() {
        return this.albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public String getAlbumState() {
        return this.albumState;
    }

    public void setAlbumState(String albumState) {
        this.albumState = albumState;
    }

    public String getAlbumPic() {
        return this.albumPic;
    }

    public void setAlbumPic(String albumPic) {
        this.albumPic = albumPic;
    }

    public String getNextLink() {
        return this.nextLink;
    }

    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Album [id=" + this.id
                + ", albumId=" + this.albumId
                + ", playIndex=" + this.playIndex
                + ", collectionTime=" + this.collectionTime
                + ", typeId=" + this.typeId
                + ", albumType=" + this.albumType
                + ", albumSourceType=" + this.albumSourceType
                + ", albumTitle=" + this.albumTitle
                + ", albumState=" + this.albumState
                + ", albumPic=" + this.albumPic
                + ", nextLink=" + this.nextLink
                + ", time=" + this.time
                + "]";
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putInt("ID", this.id);
        bundle.putInt("PlayIndex", this.playIndex);
        bundle.putInt("CollectionTime", this.collectionTime);
        bundle.putInt("TypeId", this.typeId);
        bundle.putString("AlbumId", this.albumId);
        bundle.putString("AlbumType", this.albumType);
        bundle.putString("AlbumSourceType", this.albumSourceType);
        bundle.putString("AlbumPic", this.albumPic);
        bundle.putString("AlbumTitle", this.albumTitle);
        bundle.putString("AlbumState", this.albumState);
        bundle.putString("NextLink", this.nextLink);
        bundle.putString("Time", this.time);
        dest.writeBundle(bundle);
    }
}
