package com.aphrodite.smartboard.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;

import java.util.List;

public class PaletePopAdapter extends BaseAdapter {
    private Context mContext;
    private List<Integer> mColorIds;

    public PaletePopAdapter(Context context, List<Integer> colorIds) {
        this.mContext = context;
        this.mColorIds = colorIds;
    }

    @Override
    public int getCount() {
        return ObjectUtils.isEmpty(mColorIds) ? 0 : mColorIds.size();
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_window_palete_item, null);
        ImageView imageView;
        imageView = view.findViewById(R.id.palete_item_iv);
        imageView.setBackgroundColor(mContext.getResources().getColor(R.color.black));
        return view;
    }

}
