package com.aphrodite.smartboard.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.framework.view.adapter.BaseRecyclerAdapter;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.model.bean.WorkBriefBean;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListPopupWindowAdapter<T> extends BaseRecyclerAdapter<T, ListPopupWindowAdapter.ViewHolder> {
    private Context mContext;

    public ListPopupWindowAdapter(Context context) {
        super(context);
        this.mContext = context;
    }

    @NonNull
    @Override
    public ListPopupWindowAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.popup_window_list_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListPopupWindowAdapter.ViewHolder holder, int position) {
        WorkBriefBean bean = (WorkBriefBean) getItem(position);
        if (null == bean) {
            return;
        }
        holder.mName.setText(bean.getName());
        holder.mValue.setText(bean.getValue());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.item_root)
        LinearLayout mRoot;
        @BindView(R.id.item_key)
        TextView mName;
        @BindView(R.id.item_value)
        TextView mValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2px(mContext, 40));
            mRoot.setLayoutParams(params);
        }
    }

}
