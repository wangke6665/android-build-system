package com.shenma.tvlauncher.vod.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.vod.domain.VodDataInfo;
import com.shenma.tvlauncher.view.shapeimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaCodecInfo;

/***
 * 影视类型适配器
 *
 * @author joychang
 *
 */
public class VodtypeAdapter extends BaseAdapter {
    public static List<VodDataInfo> vodDatas;
    private LayoutInflater mInflater;
    private ImageLoader imageLoader;
    private Context context;
    private ViewHolder holder;
    private DisplayImageOptions options;

    public VodtypeAdapter(Context context, ArrayList<VodDataInfo> datas, ImageLoader imageLoader) {
        this.context = context;
        this.vodDatas = datas;
        this.imageLoader = imageLoader;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.default_film_img)//默认图片
                .showImageForEmptyUri(R.drawable.default_film_img)
                .showImageOnFail(R.drawable.default_film_img)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new FadeInBitmapDisplayer(IjkMediaCodecInfo.RANK_SECURE))
                .build();

    }

    public void changData(ArrayList<VodDataInfo> paramArrayList) {
        setVideos(paramArrayList);
        notifyDataSetChanged();
    }

    private void setVideos(ArrayList<VodDataInfo> paramArrayList) {
        if (paramArrayList != null) {
            this.vodDatas = paramArrayList;
            return;
        }
        ArrayList<VodDataInfo> localArrayList = new ArrayList<VodDataInfo>();
        this.vodDatas = localArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.mv_type_details_item, null);
            holder = new ViewHolder();
            holder.poster = (RoundedImageView) convertView
                    .findViewById(R.id.video_poster);
            holder.state = (TextView) convertView
                    .findViewById(R.id.video_state);
            holder.score = (TextView) convertView
                    .findViewById(R.id.video_item_Score);
            holder.videoName = (TextView) convertView
                    .findViewById(R.id.video_name);
            // 设置圆角并启用剪裁
            if (holder.poster != null) {
                int radius = (int) context.getResources().getDimension(R.dimen.sm_20);
                holder.poster.setRadius(radius);
                // 启用剪裁，去除圆角外的虚影（仅API 21+）
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        holder.poster.setClipToOutline(true);
                    }
                } catch (Exception e) {
                    // 忽略低版本API不支持的异常
                }
                // 获取父容器并启用剪裁
                View parentView = (View) holder.poster.getParent();
                if (parentView != null) {
                    if (parentView instanceof android.widget.FrameLayout) {
                        ((android.widget.FrameLayout) parentView).setClipChildren(true);
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                ((android.widget.FrameLayout) parentView).setClipToOutline(true);
                            }
                        } catch (Exception e) {
                            // 忽略低版本API不支持的异常
                        }
                    }
                }
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        VodDataInfo vd = vodDatas.get(position);
        imageLoader.displayImage(vd.getPic(), holder.poster, options);
        // 确保圆角在图片加载后正确应用
        if (holder.poster != null) {
            final RoundedImageView finalPoster = holder.poster;
            finalPoster.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (finalPoster != null) {
                        int radius = (int) context.getResources().getDimension(R.dimen.sm_20);
                        finalPoster.setRadius(radius);
                        finalPoster.invalidate();
                    }
                }
            }, 100);
        }
        holder.videoName.setText(vd.getTitle());
        holder.state.setText(vd.getState());
        if (vd.getScore() != null && vd.getScore() != ""){
            holder.score.setVisibility(View.VISIBLE);
            holder.score.setText(vd.getScore());
        }else{
            holder.score.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return vodDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return vodDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        private TextView score;
        private TextView state;
        private RoundedImageView poster;
        private ImageView spuerHd;
        private TextView videoName;
    }
}
