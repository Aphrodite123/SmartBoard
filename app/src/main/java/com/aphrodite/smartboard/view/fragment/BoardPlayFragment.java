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
import android.widget.TextView;

import com.apeman.sdk.bean.BoardType;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWPage;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.service.RecordPlayerService;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.view.CustomDrawView;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;

import java.text.DecimalFormat;
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
    CustomDrawView mCustomDrawView;
    @BindView(R.id.board_play_seekbar)
    SeekBar mSeekBar;
    @BindView(R.id.play_speed)
    TextView mPlaySpeed;
    @BindView(R.id.board_play_status)
    ImageView mPlayStatusBtn;

    //按照设备比例缩放后的画布宽度
    private int mCanvasWidth;
    //按照设备比例缩放后的画布高度
    private int mCanvasHeight;
    //将设备坐标点转换为画布坐标点的缩放比例
    private Double mXScale;
    private Double mYScale;

    private BoardStatusListener mStatusListener;
    private String mRootPath;
    private String mCurrentDataPath;
    private String mCurrentAudioPath;
    private RecordPlayerService mRecordPlayerService;
    private ServiceConnection mServiceConnection;

    private List<ScreenRecordEntity> mEntities;
    private CW mCw;
    private int mDuration;
    //播放速度，默认为1s
    private float mPlaySpeedOffset = 1.0f;

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
            mRootPath = bundle.getString(IntentAction.CanvasAction.PATH_ROOT);
        }
        if (!TextUtils.isEmpty(mRootPath)) {
            mCurrentDataPath = mRootPath + AppConfig.DATA_FILE_NAME;
            mCurrentAudioPath = mRootPath + AppConfig.AUDIO_FILE_NAME;
        }
        getDeviceInfo();
        getPaths();

        mCustomDrawView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCustomDrawView.replay(mCw, 100, mXScale, mYScale);
            }
        }, 200);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mServiceConnection) {
            getActivity().unbindService(mServiceConnection);
        }
        if (null != mEntities) {
            mEntities.clear();
            mEntities = null;
        }
    }

    private void getDeviceInfo() {
        BoardType boardType = BoardType.NoteMaker;
        float deviceScale = (float) (boardType.getMaxX() / boardType.getMaxY());
        int viewWidth = UIUtils.getDisplayWidthPixels(getContext());
        int viewHeight = UIUtils.getDisplayHeightPixels(getContext());
        float screenScale = (float) (viewWidth) / (float) (viewHeight);
        if (screenScale > deviceScale) {
            //设备更宽，以View的高为基准进行缩放
            mCanvasHeight = viewHeight;
            mCanvasWidth = (int) (viewHeight * deviceScale);
        } else {
            //以View的宽为基准进行缩放
            mCanvasWidth = viewWidth;
            mCanvasHeight = (int) (viewWidth / deviceScale);
        }

        mXScale = mCanvasWidth / boardType.getMaxX();
        mYScale = mCanvasHeight / boardType.getMaxY();
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
        if (TextUtils.isEmpty(mCurrentDataPath)) {
            return;
        }

        mCw = CWFileUtils.read(mCurrentDataPath);
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
            recordEntity.setType("data/0");
            mEntities.add(recordEntity);
        }
    }

    private void drawPath(CW cw, int seconds, boolean isSeek) {
        if (null == cw) {
            return;
        }

        List<CWACT> cwacts = cw.getACT();
        if (ObjectUtils.isEmpty(cwacts)) {
            return;
        }

        for (CWACT cwact : cwacts) {
            if (null == cwact) {
                continue;
            }
            if (null != mCustomDrawView) {
            }
        }
    }

    @OnClick(R.id.board_play_fast_back)
    public void onPlayFastBack() {
        if (mPlaySpeedOffset <= 0.5) {
            return;
        }

        DecimalFormat format = new DecimalFormat("0.0");
        String speed = format.format(mPlaySpeedOffset / 2.0);
        mPlaySpeedOffset = Float.parseFloat(speed);
        mRecordPlayerService.setPlaySpeed(mPlaySpeedOffset);
        mPlaySpeed.setText(String.format(getString(R.string.play_rate), speed));
    }

    @OnClick(R.id.board_play_status)
    public void onPlay() {
        if (null == mPlayStatusBtn) {
            return;
        }
        if (mPlayStatusBtn.isSelected()) {
            mPlayStatusBtn.setSelected(false);
            mRecordPlayerService.onPause();
        } else {
            mPlayStatusBtn.setSelected(true);
            mRecordPlayerService.onResume();
        }
    }

    @OnClick(R.id.board_play_fast_forward)
    public void onPlayFastForward() {
        if (mPlaySpeedOffset >= 4) {
            return;
        }
        DecimalFormat format = new DecimalFormat("0.0");
        String speed = format.format(mPlaySpeedOffset * 2.0);
        mPlaySpeedOffset = Float.parseFloat(speed);
        mRecordPlayerService.setPlaySpeed(mPlaySpeedOffset);
        mPlaySpeed.setText(String.format(getString(R.string.play_rate), speed));
    }

    private RecordPlayerService.MediaPlayerListener mediaPlayerListener = new RecordPlayerService.MediaPlayerListener() {
        @Override
        public void prepare(int seconds) {
            mDuration = seconds / 1000;
            if (null != mPlayStatusBtn) {
                mPlayStatusBtn.setSelected(true);
            }
        }

        @Override
        public void playing(int seconds) {
            mSeekBar.setProgress((int) (1.0 * mPlaySpeedOffset * seconds / mDuration * 100));
            drawPath(mCw, seconds, false);
        }

        @Override
        public void completion() {
            if (null != mStatusListener) {
                mStatusListener.onPreview();
            }
        }
    };

}
