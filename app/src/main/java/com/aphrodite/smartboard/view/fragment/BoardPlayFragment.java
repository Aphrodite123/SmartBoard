package com.aphrodite.smartboard.view.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWPage;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.service.RecordPlayerService;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Aphrodite on 20-5-9
 */
public class BoardPlayFragment extends BaseFragment {
    @BindView(R.id.board_play_canvas)
    SimpleDoodleView mSimpleDoodleView;
    @BindView(R.id.board_play_seekbar)
    SeekBar mSeekBar;
    @BindView(R.id.board_play_status)
    ImageView mPlayStatusBtn;

    private BoardStatusListener mStatusListener;

    private String mCurrentAudioPath;
    private RecordPlayerService mRecordPlayerService;
    private ServiceConnection mServiceConnection;


    private List<ScreenRecordEntity> mEntities;
    private CW mCw;

    public BoardPlayFragment(BoardStatusListener statusListener) {
        this.mStatusListener = statusListener;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_board_play;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK);
        setLeftBtnRes(R.drawable.back);
        setTitleText(R.string.work_playback);
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mStatusListener) {
                    mStatusListener.onPreview();
                }
            }
        });
    }

    @Override
    protected void initData() {
        Bundle bundle = getArguments();
        if (null != bundle) {
            mCurrentAudioPath = bundle.getString(IntentAction.CanvasAction.PATH_TRACK_FILE);
        }

        getPaths();
    }

    private void startAudioService() {
        if (null == mServiceConnection) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    RecordPlayerService.RecordPlayerBinder playerBinder = (RecordPlayerService.RecordPlayerBinder) service;
                    mRecordPlayerService = playerBinder.getRecordPlayerService();
                    mRecordPlayerService.setMediaPlayerListener(mediaPlayerListener);
                    mRecordPlayerService.start(mCurrentAudioPath);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }

        Intent intent = new Intent(getActivity(), RecordPlayerService.class);
        getActivity().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void getPaths() {
        if (TextUtils.isEmpty(mCurrentAudioPath)) {
            return;
        }

        mCw = CWFileUtils.read(mCurrentAudioPath);
        if (null == mCw) {
            return;
        }

        List<CWPage> cwPages = mCw.getPAGES();
        if (ObjectUtils.isEmpty(cwPages)) {
            return;
        }

        mEntities = new ArrayList<>();
        for (CWPage cwPage : cwPages) {
            if (null == cwPage) {
                continue;
            }
            ScreenRecordEntity recordEntity = new ScreenRecordEntity();
            recordEntity.setCanDraw(false);
            recordEntity.setType("0");
            mEntities.add(recordEntity);
        }
    }

    @OnClick(R.id.board_play_status)
    public void onPlay() {
        if (null == mPlayStatusBtn) {
            return;
        }
        if (mPlayStatusBtn.isSelected()) {
            mPlayStatusBtn.setSelected(false);
        } else {
            mPlayStatusBtn.setSelected(true);
        }
    }

    private RecordPlayerService.MediaPlayerListener mediaPlayerListener = new RecordPlayerService.MediaPlayerListener() {
        @Override
        public void prepare(int seconds) {

        }

        @Override
        public void playing(int seconds) {

        }

        @Override
        public void completion() {

        }
    };

}
