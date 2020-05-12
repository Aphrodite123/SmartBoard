package com.aphrodite.smartboard.view.widget.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.model.bean.WorkBriefBean;
import com.aphrodite.smartboard.view.adapter.ListPopupWindowAdapter;
import com.aphrodite.smartboard.view.widget.recycleview.PullToRefreshRecyclerView;

import java.util.List;

public class ListPopupWindow extends PopupWindow {
    private TextView mName;
    private PullToRefreshRecyclerView mRefreshRecyclerView;

    private Context mContext;
    private ListPopupWindowAdapter mAdapter;

    public ListPopupWindow(Context context) {
        super(context);
        this.mContext = context;
        initView();
        initData();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_window_list, null);
        setContentView(view);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(UIUtils.dip2px(mContext, 250));
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.anim_popupwindow_zoom);

        mName = view.findViewById(R.id.title);
        mRefreshRecyclerView = view.findViewById(R.id.list);
        mRefreshRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ListPopupWindowAdapter(mContext);
        mRefreshRecyclerView.setAdapter(mAdapter);
    }

    private void initData() {

    }

    public void setTitle(String title) {
        if (null != mName) {
            mName.setText(title);
        }
    }

    public void setList(List<WorkBriefBean> beans) {
        if (null != mAdapter) {
            mAdapter.setItems(beans);
        }
    }

}
