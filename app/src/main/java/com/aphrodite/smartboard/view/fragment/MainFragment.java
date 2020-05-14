package com.aphrodite.smartboard.view.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.WorkInfoBean;
import com.aphrodite.smartboard.model.bean.WorksBean;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.LogUtils;
import com.aphrodite.smartboard.utils.TimeUtils;
import com.aphrodite.smartboard.view.adapter.WorkListAdapter;
import com.aphrodite.smartboard.view.adapter.WorkListGridViewAdapter;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.recycleview.PullToRefreshRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

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
        mListAdapter = new WorkListAdapter(getContext(), mClickListener);
        mRefreshRecyclerView.setAdapter(mListAdapter);
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {
        mWorksBeans = new ArrayList<>();

        loadSDcardData();
        parseData();
        mListAdapter.setItems(mWorksBeans);
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

            cw = CWFileUtils.read(AppConfig.TEMP_PATH + fold + File.separator + "data.cw");
            mCws.add(cw);
        }
    }

    private void parseData() {
        if (ObjectUtils.isEmpty(mCws)) {
            return;
        }

        List<Long> mTimes = new ArrayList<>();
        for (CW cw : mCws) {
            if (null == cw) {
                continue;
            }

            mTimes.add(cw.getTime());
        }
        if (ObjectUtils.isEmpty(mTimes)) {
            return;
        }
        Collections.sort(mTimes);

        collectDataToDay(mTimes);
    }

    /**
     * 按天聚合数据
     *
     * @param mTimes
     */
    private void collectDataToDay(List<Long> mTimes) {
        if (ObjectUtils.isEmpty(mTimes)) {
            return;
        }

        long time;
        WorksBean bean = new WorksBean();
        List<WorkInfoBean> workInfoBeans = new ArrayList<>();
        for (int i = 0; i < mTimes.size(); i++) {
            time = mTimes.get(0);
            if (TimeUtils.isSameDay(time, mTimes.get(i), TimeZone.getDefault())) {
                createWorkBeans(bean, workInfoBeans, mTimes.get(i));
            } else {
                collectDataToDay(mTimes.subList(i, mTimes.size()));
                break;
            }
        }
    }

    private void createWorkBeans(WorksBean bean, List<WorkInfoBean> workInfoBeans, Long time) {
        bean.setDate(TimeUtils.msToDateFormat(1000 * time, TimeUtils.FORMAT_CHINESE_ONE, TimeUtils.FORMAT_CHINESE_TWO));
        if (ObjectUtils.isEmpty(mCws)) {
            return;
        }

        CW cw = null;
        for (int i = 0; i < mCws.size(); i++) {
            cw = mCws.get(i);
            if (null == cw) {
                continue;
            }

            if (time == cw.getTime()) {
                WorkInfoBean infoBean = new WorkInfoBean();
                infoBean.setAuthor(cw.getAuthor());
                infoBean.setTime(TimeUtils.msToDateFormat(1000 * cw.getTime(), TimeUtils.FORMAT_CLOCK_ONE));
                infoBean.setPicture(AppConfig.TEMP_PATH + cw.getTime() + File.separator + "cover_image.jpg");
                infoBean.setDataPath(AppConfig.TEMP_PATH + cw.getTime() + File.separator + "data.cw");
                infoBean.setAudioPath(AppConfig.TEMP_PATH + cw.getTime() + File.separator + "audio.mp3");

                workInfoBeans.add(infoBean);
            }
        }
        bean.setData(workInfoBeans);

        if (!mWorksBeans.contains(bean)) {
            mWorksBeans.add(bean);
        }
    }

    private WorkListGridViewAdapter.OnClickListener mClickListener = new WorkListGridViewAdapter.OnClickListener() {
        @Override
        public void onClick(String dataPath, String audioPath) {
            Intent intent = new Intent(IntentAction.CanvasAction.ACTION);
            intent.putExtra(IntentAction.CanvasAction.PATH_TRACK_FILE, dataPath);
            intent.putExtra(IntentAction.CanvasAction.PATH_AUDIO_FILE, audioPath);
            getActivity().startActivity(intent);
        }
    };

}
