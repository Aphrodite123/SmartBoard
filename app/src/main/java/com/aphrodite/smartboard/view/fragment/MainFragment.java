package com.aphrodite.smartboard.view.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.LinearLayout;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.WorkInfoBean;
import com.aphrodite.smartboard.model.bean.WorksBean;
import com.aphrodite.smartboard.model.event.SyncEvent;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.TimeUtils;
import com.aphrodite.smartboard.view.adapter.WorkListAdapter;
import com.aphrodite.smartboard.view.adapter.WorkListGridViewAdapter;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.recycleview.PullToRefreshRecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import androidx.recyclerview.widget.GridLayoutManager;
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

        mRefreshRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        mListAdapter = new WorkListAdapter(getContext(), mClickListener);
        mRefreshRecyclerView.setAdapter(mListAdapter);
    }

    @Override
    protected void initListener() {
        EventBus.getDefault().register(mEventListener);
    }

    @Override
    protected void initData() {
        mWorksBeans = new ArrayList<>();
        mCws = new ArrayList<>();
        loadSDcardData();
        parseData();

        if (ObjectUtils.isEmpty(mWorksBeans)) {
            return;
        }

        List<WorkInfoBean> mInfoBeans = new ArrayList<>();
        WorkInfoBean infoBean = null;
        for (int i = 0; i < mWorksBeans.size(); i++) {
            infoBean = new WorkInfoBean();
            infoBean.setDate(mWorksBeans.get(i).getDate());
            infoBean.setType(WorkListAdapter.VIEW_TYPE_DATE);
            mInfoBeans.add(infoBean);

            mInfoBeans.addAll(mWorksBeans.get(i).getData());
        }
        mListAdapter.setItems(mInfoBeans);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(mEventListener);
        if (null != mWorksBeans) {
            mWorksBeans.clear();
            mWorksBeans = null;
        }
        if (null != mCws) {
            mCws.clear();
            mCws = null;
        }
    }

    private void loadSDcardData() {
        File file = new File(AppConfig.DATA_PATH);
        if (!file.exists()) {
            return;
        }

        mWorkFolders = file.list();
        if (ObjectUtils.isEmpty(mWorkFolders)) {
            return;
        }

        CW cw = null;
        for (String fold : mWorkFolders) {
            if (TextUtils.isEmpty(fold)) {
                continue;
            }

            cw = CWFileUtils.read(AppConfig.DATA_PATH + fold + File.separator + AppConfig.DATA_FILE_NAME);
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
        bean.setDate(TimeUtils.msToDateFormat(time, TimeUtils.FORMAT_CHINESE_TWO, TimeUtils.FORMAT_CHINESE_THREE));
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
                infoBean.setTime(TimeUtils.msToDateFormat(cw.getTime(), TimeUtils.FORMAT_CLOCK_ONE));
                infoBean.setPath(AppConfig.DATA_PATH + cw.getTime() + File.separator);
                infoBean.setPicture(AppConfig.DATA_PATH + cw.getTime() + File.separator + AppConfig.COVER_IMAGE_NAME);
                infoBean.setDataPath(AppConfig.DATA_PATH + cw.getTime() + File.separator + AppConfig.DATA_FILE_NAME);
                infoBean.setAudioPath(AppConfig.DATA_PATH + cw.getTime() + File.separator + AppConfig.AUDIO_FILE_NAME);
                infoBean.setType(WorkListAdapter.VIEW_TYPE_ITEM);

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
        public void onClick(String path) {
            Intent intent = new Intent(IntentAction.CanvasAction.ACTION);
            intent.putExtra(IntentAction.CanvasAction.PATH_ROOT, path);
            getActivity().startActivity(intent);
        }
    };

    private Object mEventListener = new Object() {
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(SyncEvent event) {
            if (SyncEvent.REFRESH_WORK_LIST == event) {
                if (null != mWorksBeans) {
                    mWorksBeans.clear();
                }
                if (null != mCws) {
                    mCws.clear();
                }
                loadSDcardData();
                parseData();

                List<WorkInfoBean> mInfoBeans = new ArrayList<>();
                WorkInfoBean infoBean = null;
                for (int i = 0; i < mWorksBeans.size(); i++) {
                    infoBean = new WorkInfoBean();
                    infoBean.setDate(mWorksBeans.get(i).getDate());
                    infoBean.setType(WorkListAdapter.VIEW_TYPE_DATE);
                    mInfoBeans.add(infoBean);

                    mInfoBeans.addAll(mWorksBeans.get(i).getData());
                }

                if (null != mListAdapter) {
                    mListAdapter.setItems(mInfoBeans);
                }
            }
        }
    };

}
