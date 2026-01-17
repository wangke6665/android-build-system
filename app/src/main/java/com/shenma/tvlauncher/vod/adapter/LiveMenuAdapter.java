package com.shenma.tvlauncher.vod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.vod.LivePlayerActivity;
import com.shenma.tvlauncher.vod.domain.VideoInfo;

import java.util.List;

/***
 * 播放菜单设配器
 *
 * @author joychang
 *
 */
public class LiveMenuAdapter<T> extends BaseAdapter {
    private final Context context;
    private final LayoutInflater mInflater;
    private final List<T> medialist;
    private final int type;
    private boolean isMenuItemShow = false;
    private boolean isMenuItemShows = false;
    private boolean isMenuItemShowss = false;

    public LiveMenuAdapter(Context context, List<T> medialist, int type, Boolean isMenuItemShow) {
        this.context = context;
        this.medialist = medialist;
        this.type = type;
        this.isMenuItemShow = isMenuItemShow.booleanValue();
        this.isMenuItemShows = isMenuItemShow.booleanValue();
        this.isMenuItemShowss = isMenuItemShow.booleanValue();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public LiveMenuAdapter(Context context, List<T> medialist, int type,Boolean isMenuItemShow, Boolean isMenuItemShows, Boolean isMenuItemShowss) {
        this.context = context;
        this.medialist = medialist;
        this.type = type;
        this.isMenuItemShow = isMenuItemShow;
        this.isMenuItemShows = isMenuItemShows;
        this.isMenuItemShowss = isMenuItemShowss;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return this.medialist.size();
    }

    public Object getItem(int position) {
        return this.medialist.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolders ViewHolders;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.mv_controler_menu_item, null);
            ViewHolders = new ViewHolders();
            ViewHolders.textView = convertView.findViewById(R.id.tv_menu_item);
            convertView.setTag(ViewHolders);
        } else {
            ViewHolders = (ViewHolders) convertView.getTag();
        }
        if (this.type == 0) {
            ViewHolders.textView.setText(((VideoInfo) this.medialist.get(position)).title);
        } else {
            ViewHolders.textView.setText((String) this.medialist.get(position));
        }
        int mPosition = 0;
        ViewHolders.textView.setTextColor(this.context.getResources().getColor(R.color.white));
        if (this.isMenuItemShow) {
            switch (this.type) {
                case 0:
                    mPosition = LivePlayerActivity.xjposition;
                    break;
                case 1:
                    mPosition = LivePlayerActivity.jmposition;
                    break;
                case 2:
                    mPosition = LivePlayerActivity.hmblposition;
                    break;
                case 3:
                    mPosition = LivePlayerActivity.phszposition;
                    break;
                case 4:
                    mPosition = LivePlayerActivity.nhposition;
                    break;
                case 5:
                    mPosition = LivePlayerActivity.qlposition;
                    break;
            }
            if (mPosition == position) {
                ViewHolders.textView.setTextColor(context.getResources().getColor(R.color.text_focus));
                convertView.setBackgroundResource(android.R.color.transparent);
            }else{
                convertView.setBackgroundResource(android.R.color.transparent);
            }
        }
        if (this.isMenuItemShows) {
            switch (this.type) {
                case 0:
                    mPosition = LivePlayerActivity.gsposition;
                    break;
            }
            if (mPosition == position) {
                ViewHolders.textView.setTextColor(context.getResources().getColor(R.color.text_focus));
                convertView.setBackgroundResource(android.R.color.transparent);
            }else{
//                ViewHolders.textView.setTextColor(context.getResources().getColor(R.color.bg_input));
                convertView.setBackgroundResource(android.R.color.transparent);
            }
        }

        if (this.isMenuItemShowss) {
            switch (this.type) {
                case 0:
                    mPosition = LivePlayerActivity.xjposition;
                    break;
            }
            if (mPosition == position) {
                ViewHolders.textView.setTextColor(context.getResources().getColor(R.color.text_focus));
                convertView.setBackgroundResource(android.R.color.transparent);
            }else{
//                ViewHolders.textView.setTextColor(context.getResources().getColor(R.color.purple));
                convertView.setBackgroundResource(android.R.color.transparent);
            }
        }
        return convertView;
    }


}

class ViewHolders {
    public TextView textView;
}


