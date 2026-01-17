package com.shenma.tvlauncher.vod.adapter;

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
import com.shenma.tvlauncher.vod.domain.VodDataInfo;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaCodecInfo;

public class SearchTypeAdapter extends BaseAdapter {
    public static List<VodDataInfo> vodDatas;
    private Context context;
    private ViewHolder holder;
    private ImageLoader imageLoader;
    private LayoutInflater mInflater;
    private DisplayImageOptions options = new Builder().showStubImage(R.drawable.default_film_img).showImageForEmptyUri(R.drawable.default_film_img).showImageOnFail(R.drawable.default_film_img).resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Config.RGB_565).displayer(new FadeInBitmapDisplayer(IjkMediaCodecInfo.RANK_SECURE)).build();

    public SearchTypeAdapter(Context context, ArrayList<VodDataInfo> datas, ImageLoader imageLoader) {
        this.context = context;
        vodDatas = datas;
        this.imageLoader = imageLoader;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public void changData(ArrayList<VodDataInfo> paramArrayList) {
        setVideos(paramArrayList);
        notifyDataSetChanged();
    }

    private void setVideos(ArrayList<VodDataInfo> paramArrayList) {
        if (paramArrayList != null) {
            vodDatas = paramArrayList;
        } else {
            vodDatas = new ArrayList();
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.mv_type_details_item, null);
            this.holder = new ViewHolder();
            this.holder.poster = (ImageView) convertView.findViewById(R.id.video_poster);
            this.holder.state = (TextView) convertView.findViewById(R.id.video_state);
            this.holder.score = (TextView) convertView.findViewById(R.id.video_item_Score);
            this.holder.videoName = (TextView) convertView.findViewById(R.id.video_name);
            convertView.setTag(this.holder);
        } else {
            this.holder = (ViewHolder) convertView.getTag();
        }
        VodDataInfo vd = (VodDataInfo) vodDatas.get(position);
        this.imageLoader.displayImage(vd.getPic(), this.holder.poster, this.options);
        this.holder.videoName.setText(vd.getTitle());
        this.holder.state.setText(vd.getState());
        if (vd.getScore() != null && vd.getScore() != ""){
            this.holder.score.setVisibility(View.VISIBLE);
            this.holder.score.setText(vd.getScore());
        }else{
            this.holder.score.setVisibility(View.GONE);
        }
        return convertView;
    }

    public int getCount() {
        return vodDatas.size();
    }

    public Object getItem(int position) {
        return vodDatas.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    class ViewHolder {
        private ImageView poster;
        private ImageView spuerHd;
        private TextView state;
        private TextView score;
        private TextView videoName;

        ViewHolder() {
        }
    }
}
