package com.aphrodite.smartboard.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.model.bean.WorkInfoBean;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

/**
 * Created by Aphrodite on 20-5-12
 */
public class WorkListGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<WorkInfoBean> mData;

    public WorkListGridViewAdapter(Context mContext, List<WorkInfoBean> data) {
        this.mContext = mContext;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return ObjectUtils.isEmpty(mData) ? 0 : mData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_work_list_gv_item, null);
        ImageView imageView;
        TextView time;
        imageView = view.findViewById(R.id.image);
        time = view.findViewById(R.id.image);

        WorkInfoBean bean = mData.get(position);
        if (null != bean) {
            File file = new File(bean.getPicture());
            Glide.with(mContext).load(file).into(imageView);
            time.setText(bean.getTime());
        }
        return view;
    }
}
