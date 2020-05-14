package com.aphrodite.smartboard.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aphrodite.framework.view.adapter.BaseRecyclerAdapter;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.model.bean.WorksBean;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkListAdapter<T> extends BaseRecyclerAdapter<T, WorkListAdapter.ViewHolder> {
    private Context mContext;
    private WorkListGridViewAdapter mGridViewAdapter;
    private WorkListGridViewAdapter.OnClickListener mClickListener;

    public WorkListAdapter(Context context, WorkListGridViewAdapter.OnClickListener listener) {
        super(context);
        this.mContext = context;
        this.mClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_work_list_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkListAdapter.ViewHolder holder, int position) {
        WorksBean bean = (WorksBean) getItem(position);
        if (null == bean) {
            return;
        }
        holder.mDate.setText(bean.getDate());
        mGridViewAdapter = new WorkListGridViewAdapter(mContext, bean.getData(), mClickListener);
        holder.mGridView.setAdapter(mGridViewAdapter);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root)
        LinearLayout mRoot;
        @BindView(R.id.date)
        TextView mDate;
        @BindView(R.id.work_list_item_gv)
        GridView mGridView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) mContext.getResources().getDimension(R.dimen.dip25);
            params.rightMargin = (int) mContext.getResources().getDimension(R.dimen.dip25);
            mRoot.setLayoutParams(params);
        }
    }

}
