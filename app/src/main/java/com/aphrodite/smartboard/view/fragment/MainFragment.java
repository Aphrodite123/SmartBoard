package com.aphrodite.smartboard.view.fragment;

import android.text.TextUtils;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.WorkInfoBean;
import com.aphrodite.smartboard.model.bean.WorksBean;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.view.adapter.WorkListAdapter;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.recycleview.PullToRefreshRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private String[] mWorkFolders;
    private List<CW> mCws;
    private List<WorksBean> mWorksBeans;

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
        loadSDcardData();
        parseData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void loadSDcardData() {
        File file = new File(AppConfig.TEMP_PATH);
        if (!file.exists()) {
            return;
        }

        mWorkFolders = file.list();
        if (ObjectUtils.isEmpty(mWorkFolders)) {
            return;
        }

        mCws = new ArrayList<>();
        CW cw = null;
        for (String fold : mWorkFolders) {
            if (TextUtils.isEmpty(fold)) {
                continue;
            }

            cw = CWFileUtils.read(AppConfig.TEMP_PATH + fold + "data.cw");
            mCws.add(cw);
        }
    }

    private void parseData() {
        if (ObjectUtils.isEmpty(mCws)) {
            return;
        }

        long currentTime = 0;
        mWorksBeans = new ArrayList<>();
        WorksBean worksBean = null;
        List<WorkInfoBean> workInfoBeans = null;
        WorkInfoBean workInfoBean = null;

        for (int i = 0; i < mCws.size(); i++) {
            currentTime = mCws.get(i).getTime();

            for (int j = 0; j < mCws.size(); j++) {
                if (Math.abs(currentTime - mCws.get(j).getTime()) <= 24 * 60 * 60) {

                }

            }


        }


        List<Long> mTimes = new ArrayList<>();
        for (CW cw : mCws) {
            if (null == cw) {
                continue;
            }

            if (mTimes.contains(cw.getTime())) {
                continue;
            }

            mTimes.add(cw.getTime());
        }

        if (ObjectUtils.isEmpty(mTimes)) {
            return;
        }


        for (int i = 0; i < mTimes.size(); i++) {
            worksBean = new WorksBean();
            workInfoBeans = new ArrayList<>();
            for (int j = 0; j < mCws.size(); j++) {
                if (mTimes.get(i) == mCws.get(j).getTime()) {
                    workInfoBean = new WorkInfoBean();
                    workInfoBean.setPicture();
                }
            }
        }


    }

}
