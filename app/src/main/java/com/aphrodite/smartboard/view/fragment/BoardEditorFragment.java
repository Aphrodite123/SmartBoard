package com.aphrodite.smartboard.view.fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.service.AudioService;
import com.aphrodite.smartboard.utils.AudioUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.popupwindow.PaletePopupWindow;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Aphrodite on 20-5-8
 */
public class BoardEditorFragment extends BaseFragment {
    @BindView(R.id.board_editor_root)
    RelativeLayout mRoot;
    @BindView(R.id.board_path_bg)
    ImageView mBoardPathBg;
    @BindView(R.id.custom_canvas)
    SimpleDoodleView mCanvas;
    @BindView(R.id.canvas_bottom_tab)
    LinearLayout mCanvasBottomTab;
    @BindView(R.id.switch_color)
    TextView mSwitchColorBtn;

    private BoardStatusListener mStatusListener;

    private AudioService mAudioService;
    private ServiceConnection mServiceConnection;
    private List<ScreenRecordEntity> mRecordEntities = new ArrayList<>();

    private PaletePopupWindow mPaletePopupWindow;
    private List<Integer> mColorIds;

    public BoardEditorFragment(BoardStatusListener statusListener) {
        this.mStatusListener = statusListener;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_board_editor;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK | TITLE_FLAG_SHOW_RIGHT_BTN);
        setLeftBtnRes(R.drawable.back);
        setRightBtnRes(R.drawable.icon_done);
        setTitleText(R.string.editor);
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

        AudioUtils.addRecordListener(mRecordListener);
    }

    @Override
    protected void initData() {
        mColorIds = new ArrayList<>();
        mColorIds.add(getResources().getColor(R.color.color_b71919));
        mColorIds.add(getResources().getColor(R.color.color_ffe82a));
        mColorIds.add(getResources().getColor(R.color.color_2e48ff));
        mColorIds.add(getResources().getColor(R.color.color_7bff16));
        mColorIds.add(getResources().getColor(R.color.color_f836ff));
        mColorIds.add(getResources().getColor(R.color.color_32397e));
        mColorIds.add(getResources().getColor(R.color.color_bac290));
        mColorIds.add(getResources().getColor(R.color.color_492c4f));
        mColorIds.add(getResources().getColor(R.color.color_ffaaaa));
        mColorIds.add(getResources().getColor(R.color.color_ffbe79));
        mColorIds.add(getResources().getColor(R.color.color_ff0000));
        mColorIds.add(getResources().getColor(R.color.color_000000));

        //默认为红色
        mCanvas.setDrawColor(getResources().getColor(R.color.color_7bff16));
        ScreenRecordEntity recordEntity = new ScreenRecordEntity();
        recordEntity.setType("0");
        mRecordEntities.add(recordEntity);
        startAudioService();
    }

    @Override
    public void onDestroy() {
        getActivity().unbindService(mServiceConnection);
        AudioUtils.clearAllListener();
        if (null != mAudioService) {
            mAudioService.stopAudio(null);
        }
        super.onDestroy();
    }

    private void setWindowBackground(Float alpha) {
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = alpha;
        getActivity().getWindow().setAttributes(layoutParams);
    }

    private void onBottomTab(int index) {
        switch (index) {
            //畫筆
            case 0:
                if (null != mCanvas) {
                    mCanvas.setCanDraw(true);
                }

                if (null != mAudioService) {
                    if (mAudioService.isFinish()) {
                        FileUtils.deleteFile(new File(AppConfig.TEMP_PATH), false);
                        mAudioService.startAudio();
                    } else {
                        if (mAudioService.isRunning()) {
                            mAudioService.pauseAuido();
                        } else {
                            mAudioService.resumeAudio();
                        }
                    }
                }
                break;
            //橡皮
            case 1:
                if (null != mAudioService) {
                    if (mAudioService.isFinish()) {
                        FileUtils.deleteFile(new File(AppConfig.TEMP_PATH), false);
                        mAudioService.startAudio();
                    } else {
                        if (mAudioService.isRunning()) {
                            mAudioService.pauseAuido();
                        } else {
                            mAudioService.resumeAudio();
                        }
                    }
                }
                mCanvas.setIsEraser(true);
                break;
            //色板
            case 2:
                if (null == mPaletePopupWindow) {
                    mPaletePopupWindow = new PaletePopupWindow(getContext(), mColorIds);
                }
                mPaletePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        setWindowBackground(1f);
                    }
                });
                if (!mPaletePopupWindow.isShowing()) {
                    mPaletePopupWindow.showAtLocation(mRoot, Gravity.BOTTOM, 0, 0);
                    setWindowBackground(0.8f);
                }
                break;
            //清空
            case 3:
                mCanvas.clear();
                break;
        }
    }

    private void startAudioService() {
        if (null == mServiceConnection) {
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    AudioService.AudioBinder audioBinder = (AudioService.AudioBinder) service;
                    mAudioService = audioBinder.getAudioService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }

        Intent intent = new Intent(getActivity(), AudioService.class);
        getActivity().bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void clickRightBtn(View view) {
        if (null != mAudioService) {
            mAudioService.stopAudio(null);
        }

        if (null != mCanvas) {
            mCanvas.setCanDraw(false);
            mCanvas.clear();
        }
    }

    @OnClick(R.id.switch_paint)
    public void onPainting() {
        onBottomTab(0);
    }

    @OnClick(R.id.switch_eraser)
    public void onEraser() {
        onBottomTab(1);
    }

    @OnClick(R.id.switch_color)
    public void onColor() {
        onBottomTab(2);
    }

    @OnClick(R.id.switch_clear)
    public void onClear() {
        onBottomTab(3);
    }

    private AudioUtils.RecordListener mRecordListener = new AudioUtils.RecordListener() {
        @Override
        public void onStartRecord() {

        }

        @Override
        public void onPauseRecord() {

        }

        @Override
        public void onResumeRecord() {

        }

        @Override
        public void onStopRecord(String stopTip) {
            CWFileUtils.write(mRecordEntities, AppConfig.TEMP_PATH, UIUtils.getDisplayWidthPixels(getContext()), UIUtils.getDisplayHeightPixels(getContext()));
        }

        @Override
        public void onRecording(int time) {
            CWFileUtils.setSeconds(time);
        }
    };

}
