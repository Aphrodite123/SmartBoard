package com.aphrodite.smartboard.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.adapter.WorkListAdapter;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.recycleview.PullToRefreshRecyclerView;

import butterknife.BindView;

/**
 * Created by Aphrodite on 20-4-22
 */
public class MainFragment extends BaseFragment {
    @BindView(R.id.root)
    LinearLayout mRoot;
    @BindView(R.id.main_list)
    PullToRefreshRecyclerView mRefreshRecyclerView;

    private WorkListAdapter mListAdapter;

    @Override
    protected int getViewId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_RIGHT_BTN);
        setTitleText(R.string.main_page);
        setTitleColor(getResources().getColor(R.color.color_626262));

        mRefreshRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mListAdapter = new WorkListAdapter(getContext());
        mRefreshRecyclerView.setAdapter(mListAdapter);
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
