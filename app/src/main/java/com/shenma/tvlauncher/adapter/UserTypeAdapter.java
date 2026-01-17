package com.shenma.tvlauncher.adapter;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.dao.bean.AppInfo;
import com.shenma.tvlauncher.vod.db.Album;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaCodecInfo;

public class UserTypeAdapter<T> extends BaseAdapter {
    private Boolean ISAPP;
    private Context context;
    private ViewHolder holder;
    private ImageLoader imageLoader;
    private LayoutInflater mInflater;
    private DisplayImageOptions options;
    private List<T> vodDatas = new ArrayList();

    public UserTypeAdapter(Context context, List<T> datas, ImageLoader imageLoader, Boolean ISAPP) {
        this.context = context;
        this.vodDatas.addAll(datas);
        this.imageLoader = imageLoader;
        this.ISAPP = ISAPP;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.options = new Builder().showStubImage(R.drawable.default_film_img).showImageForEmptyUri(R.drawable.default_film_img).showImageOnFail(R.drawable.default_film_img).resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Config.RGB_565).displayer(new FadeInBitmapDisplayer(IjkMediaCodecInfo.RANK_SECURE)).build();
    }

    public void changData(ArrayList<T> paramArrayList) {
        setVideos(paramArrayList);
    }

    public void setAppData(List applist) {
        if (applist != null) {
            this.vodDatas = applist;
        }
    }

    private void setVideos(ArrayList<T> paramArrayList) {
        if (paramArrayList != null) {
            this.vodDatas = paramArrayList;
        } else {
            this.vodDatas = new ArrayList();
        }
    }

    public void clearDatas() {
        if (this.vodDatas != null && this.vodDatas.size() > 0) {
            this.vodDatas.clear();
        }
    }

    public void remove(int index) {
        if (this.vodDatas != null && this.vodDatas.size() > index) {
            this.vodDatas.remove(index);
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (this.ISAPP.booleanValue()) {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.my_app_item, null);
                this.holder = new ViewHolder();
                this.holder.app_icon = (ImageView) convertView.findViewById(R.id.app_icon);
                this.holder.app_title = (TextView) convertView.findViewById(R.id.app_title);
                this.holder.packflag = (TextView) convertView.findViewById(R.id.packflag);
                convertView.setTag(this.holder);
            } else {
                this.holder = (ViewHolder) convertView.getTag();
            }
            AppInfo appinfo = (AppInfo) this.vodDatas.get(position);
            this.holder.app_icon.setImageDrawable(appinfo.getAppicon());
            this.holder.app_title.setText(appinfo.getAppname());
            this.holder.packflag.setText(appinfo.getApppack());
        } else {
            if (convertView == null) {
                convertView = this.mInflater.inflate(R.layout.user_type_details_item, null);
                this.holder = new ViewHolder();
                this.holder.poster = (ImageView) convertView.findViewById(R.id.user_video_poster);
                this.holder.checked = (ImageView) convertView.findViewById(R.id.user_video_checked);
                this.holder.state = (TextView) convertView.findViewById(R.id.user_video_state);
                this.holder.videoName = (TextView) convertView.findViewById(R.id.user_video_name);
                convertView.setTag(this.holder);
            } else {
                this.holder = (ViewHolder) convertView.getTag();
            }
            Album vd = (Album) this.vodDatas.get(position);
            this.imageLoader.displayImage(vd.getAlbumPic(), this.holder.poster, this.options);
            this.holder.videoName.setText(vd.getAlbumTitle());
            this.holder.state.setText(vd.getAlbumState());
        }
        return convertView;
    }

    public int getCount() {
        return this.vodDatas.size();
    }

    public Object getItem(int position) {
        return this.vodDatas.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    class ViewHolder {
        private ImageView app_icon;
        private TextView app_title;
        private ImageView checked;
        private TextView packflag;
        private ImageView poster;
        private TextView state;
        private TextView videoName;

        ViewHolder() {
        }
    }
}
