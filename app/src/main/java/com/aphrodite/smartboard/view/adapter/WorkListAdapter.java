package com.aphrodite.smartboard.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aphrodite.framework.view.adapter.BaseRecyclerAdapter;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.model.bean.WorkInfoBean;
import com.bumptech.glide.Glide;

import java.io.File;

public class WorkListAdapter<T> extends BaseRecyclerAdapter<T, WorkListAdapter.ViewHolder> {
    private Context mContext;
    private WorkListGridViewAdapter mGridViewAdapter;
    private WorkListGridViewAdapter.OnClickListener mClickListener;

    public static final int VIEW_TYPE_DATE = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    public WorkListAdapter(Context context, WorkListGridViewAdapter.OnClickListener listener) {
        super(context);
        this.mContext = context;
        this.mClickListener = listener;
    }

    @NonNull
    @Override
    public WorkListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType) {
            case VIEW_TYPE_DATE:
                view = mInflater.inflate(R.layout.layout_devices_item_one, null);
                return new ViewHolderOne(view);
            case VIEW_TYPE_ITEM:
                view = mInflater.inflate(R.layout.layout_devices_item_two, null);
                return new ViewHolderTwo(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull WorkListAdapter.ViewHolder holder, int position) {
        WorkInfoBean bean = (WorkInfoBean) getItem(position);
        if (null == bean) {
            return;
        }
        switch (bean.getType()) {
            case VIEW_TYPE_DATE:
                holder.mDate.setText(bean.getDate());
                break;
            case VIEW_TYPE_ITEM:
                File file = new File(bean.getPicture());
                Glide.with(mContext).load(file).into(holder.mImageView);
                holder.mTime.setText(bean.getTime());

                holder.mItemTwoRoot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (null != mClickListener) {
                            mClickListener.onClick(bean.getDataPath(), bean.getAudioPath(), bean.getPicture());
                        }
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        WorkInfoBean bean = (WorkInfoBean) getItem(position);
        if (null == bean) {
            return -1;
        }
        return bean.getType();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        if (null == manager) {
            return;
        }
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int spanSize = 0;
                WorkInfoBean bean = (WorkInfoBean) getItem(position);
                if (null == bean) {
                    return spanSize;
                }
                switch (bean.getType()) {
                    case VIEW_TYPE_DATE:
                        spanSize = 1;
                        break;
                    case VIEW_TYPE_ITEM:
                        spanSize = 2;
                        break;
                }
                return spanSize;
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout mItemOneRoot;
        public TextView mDate;

        public RelativeLayout mItemTwoRoot;
        public ImageView mImageView;
        public TextView mTime;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ViewHolderOne extends ViewHolder {
        public ViewHolderOne(@NonNull View itemView) {
            super(itemView);
            mItemOneRoot = itemView.findViewById(R.id.item_one_root);
            mDate = itemView.findViewById(R.id.date);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) mContext.getResources().getDimension(R.dimen.dip25);
            params.rightMargin = (int) mContext.getResources().getDimension(R.dimen.dip25);
            mItemOneRoot.setLayoutParams(params);
        }
    }

    public class ViewHolderTwo extends ViewHolder {
        public ViewHolderTwo(@NonNull View itemView) {
            super(itemView);
            mItemTwoRoot = itemView.findViewById(R.id.item_two_root);
            mImageView = itemView.findViewById(R.id.image);
            mTime = itemView.findViewById(R.id.time);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) mContext.getResources().getDimension(R.dimen.dip25);
            params.rightMargin = (int) mContext.getResources().getDimension(R.dimen.dip25);
            mItemTwoRoot.setLayoutParams(params);
        }
    }

}
