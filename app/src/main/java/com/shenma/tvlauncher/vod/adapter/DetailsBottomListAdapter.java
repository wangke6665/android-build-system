package com.shenma.tvlauncher.vod.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shenma.tvlauncher.R;
import com.shenma.tvlauncher.vod.domain.VodUrlList;

import java.util.List;

public class DetailsBottomListAdapter extends BaseAdapter {
    private Context context;
    public List<VodUrlList> list;
    private LayoutInflater mInflater;
    private int type;

    public DetailsBottomListAdapter(Context paramContext, List<VodUrlList> paramList,int type) {
        this.context = paramContext;
        this.list = paramList;
        this.type = type;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void changData(List<VodUrlList> paramArrayList) {
        list = paramArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (type == 0) {
            convertView = mInflater.inflate(R.layout.mv_details_key_list_item, null);
        }else {
            convertView = mInflater.inflate(R.layout.mv_details_key_list_items, null);
        }
        TextView lv_text = (TextView) convertView.findViewById(R.id.lv_text);
        lv_text.setText(list.get(position).getTitle());
        return convertView;
    }



}