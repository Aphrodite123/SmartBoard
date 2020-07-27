package com.aphrodite.smartboard.view.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.apeman.sdk.bean.BoardType;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWLine;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.bean.WorkBriefBean;
import com.aphrodite.smartboard.model.event.ActionEvent;
import com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FFmpegUtils;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.utils.TimeUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;
import com.aphrodite.smartboard.view.widget.dialog.ShareDialog;
import com.aphrodite.smartboard.view.widget.popupwindow.ListPopupWindow;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_BEGIN;
import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_FINISH;

/**
 * Created by Aphrodite on 20-5-8
 */
public class BoardDetailFragment extends BaseFragment {
    @BindView(R.id.palette_online_root)
    RelativeLayout mPaletteOnlineRoot;
    @BindView(R.id.palette_online_bg)
    ImageView mPaletteOnlineBg;
    @BindView(R.id.palette_online_canvas)
    SimpleDoodleView mPaletteOnlineCanvas;

    private String mCurrentDataPath;
    private String mCurrentAudioPath;
    private String mCurrentImagePath;

    private List<ScreenRecordEntity> mEntities;
    private CW mCw;

    private BoardStatusListener mStatusListener;
    private ShareDialog mShareDialog;
    private ListPopupWindow mListPopupWindow;
    private List<WorkBriefBean> mBeans;

    private DeleteDialog mDeleteDialog;

    //按照设备比例缩放后的画布宽度
    private int mCanvasWidth;
    //按照设备比例缩放后的画布高度
    private int mCanvasHeight;

    //将设备坐标点转换为画布坐标点的缩放比例
    private Double mXScale;
    private Double mYScale;

    private FFmpegHandler mFfmpegHandler;
    private static int LINE_PARTS = 5;
    private Paint mPaint;

    public BoardDetailFragment(BoardStatusListener statusListener) {
        this.mStatusListener = statusListener;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_board_detail;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK | TITLE_FLAG_SHOW_RIGHT_BTN);
        setLeftBtnRes(R.drawable.back);
        setRightBtnRes(R.drawable.share_toolbar_icon);
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {
        EventBus.getDefault().register(mEventListener);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    @Override
    protected void initData() {
        getDeviceInfo();
        mFfmpegHandler = new FFmpegHandler(mHandler);

        Bundle bundle = getArguments();
        if (null != bundle) {
            mCurrentDataPath = bundle.getString(IntentAction.CanvasAction.PATH_TRACK_FILE);
            mCurrentAudioPath = bundle.getString(IntentAction.CanvasAction.PATH_AUDIO_FILE);
            mCurrentImagePath = bundle.getString(IntentAction.CanvasAction.PATH_COVER_IMAGE);
        }

        if (TextUtils.isEmpty(mCurrentDataPath)) {
            return;
        }

        File file = new File(mCurrentImagePath);
        Glide.with(getContext()).load(file).into(mPaletteOnlineBg);

        mCw = CWFileUtils.read(mCurrentDataPath);
        if (null != mCw) {
            String createTime = TimeUtils.msToDateFormat(mCw.getTime(), TimeUtils.FORMAT_SPECIAL_SYMBOL_ONE);
            setTitleText(createTime);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(mEventListener);
        if (null != mShareDialog) {
            mShareDialog.dismiss();
        }
        //移除Handler
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
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

    private void setWindowBackground(Float alpha) {
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = alpha;
        getActivity().getWindow().setAttributes(layoutParams);
    }

    private void pathsCut() {
        if (null == mCw) {
            return;
        }

        List<CWACT> cwacts = mCw.getACT();
        if (null == cwacts || cwacts.size() <= 0) {
            return;
        }

        for (CWACT cwact : cwacts) {
            if (null == cwact) {
                continue;
            }

            splitLine(cwact.getLine());
        }

        handlePhoto();
    }

    private void splitLine(CWLine cwLine) {
        if (null == cwLine) {
            return;
        }

        String[] split = cwLine.getColor().split("\\,");
        int color = Color.rgb(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
        int width = cwLine.getWidth();

        List<List<Integer>> points = cwLine.getPoints();
        if (null == points || points.size() <= 0) {
            return;
        }

        //将线条进行分割
        int consult = points.size() / LINE_PARTS;
        if (consult > 1) {
            List<List<Integer>> splitPoints = new ArrayList<>();
            for (int i = 1; i < consult + 1; i++) {
                splitPoints.clear();

                if (1 == i) {
                    splitPoints.addAll(points.subList(0, LINE_PARTS));
                } else if (consult == i) {
                    splitPoints.addAll(points.subList(LINE_PARTS * (i - 1) - 2, points.size()));
                } else {
                    splitPoints.addAll(points.subList(LINE_PARTS * (i - 1) - 2, LINE_PARTS * i));
                }
                drawPathToBitmap(color, width, splitPoints);
            }
        } else {
            drawPathToBitmap(color, width, points);
        }
    }

    private void drawPathToBitmap(int color, int width, List<List<Integer>> points) {
        if (null == points || points.size() <= 0) {
            return;
        }

        Bitmap bitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        initPaint(color, width);

        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            List<Integer> xyPoints = points.get(i);
            if (null == xyPoints || xyPoints.size() < 2) {
                continue;
            }

            if (0 == i) {
                path.moveTo(xyPoints.get(0), xyPoints.get(1));
            } else {
                path.quadTo(mLastX,
                        mLastY,
                        (xyPoints.get(0) + mLastX) / 2,
                        (xyPoints.get(1) + mLastY) / 2);
            }

            mLastX = xyPoints.get(0);
            mLastY = xyPoints.get(1);
        }
        mPathList.add(path);

        for (int i = 0; i < mPathList.size(); i++) {
            canvas.drawPath(mPathList.get(i), mPaint);
        }

        StringBuffer imageName = new StringBuffer();
        imageName.append("img").append(mImageNum).append(".jpg");

        StringBuffer srcFile = new StringBuffer();
        srcFile.append(AppConfig.videoPath).append("_").append(mCurrentTime).append(File.separator);

        try {
            BitmapUtil.saveBitmap(bitmap, srcFile.toString(), imageName.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageNum++;
    }

    private void initPaint(int color, float size) {
        if (null == mPaint) {
            mPaint = new Paint();
        }

        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(size);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 图片合成视频
     */
    private void handlePhoto() {
        // 图片所在路径，图片命名格式img+number.jpg
        // 这里指定目录为根目录下img文件夹

        if (!FileUtils.isExist(AppConfig.FFMPEG_PATH)) {
            return;
        }

        String combineVideo = AppConfig.FFMPEG_PATH + "video.mp4";
        int frameRate = 10;// 合成视频帧率建议:1-10  普通视频帧率一般为25
        String[] commandLine = FFmpegUtils.pictureToVideo(AppConfig.FFMPEG_PATH, frameRate, combineVideo);
        if (mFfmpegHandler != null) {
            mFfmpegHandler.executeFFmpegCmd(commandLine);
        }
    }

    @OnClick(R.id.switch_player)
    public void onSwitchPlay() {
        if (null != mStatusListener) {
            mStatusListener.onPlay();
        }
    }

    @OnClick(R.id.switch_detail)
    public void onSwitchDetail() {
        if (null == mCw) {
            return;
        }

        mBeans = new ArrayList<>();
        if (null != mCw) {
            WorkBriefBean bean0 = new WorkBriefBean(getString(R.string.author), mCw.getAuthor());
            WorkBriefBean bean1 = new WorkBriefBean(getString(R.string.create_time), TimeUtils.msToDateFormat(1000 * mCw.getTime(), TimeUtils.FORMAT_CHINESE_ONE));
            WorkBriefBean bean2 = new WorkBriefBean(getString(R.string.edit_time), TimeUtils.msToDateFormat(1000 * mCw.getEditTime(), TimeUtils.FORMAT_CHINESE_ONE));
            mBeans.add(bean0);
            mBeans.add(bean1);
            mBeans.add(bean2);
        }

        if (null == mListPopupWindow) {
            mListPopupWindow = new ListPopupWindow(getContext());
        }
        mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowBackground(1f);
            }
        });
        mListPopupWindow.setTitle(getString(R.string.work_info));
        mListPopupWindow.setList(mBeans);
        if (!mListPopupWindow.isShowing()) {
            mListPopupWindow.showAtLocation(mPaletteOnlineRoot, Gravity.BOTTOM, 0, 0);
            setWindowBackground(0.8f);
        }
    }

    @OnClick(R.id.switch_editor)
    public void onSwitchEditor() {
        if (null != mStatusListener) {
            mStatusListener.onEditor();
        }
    }

    @OnClick(R.id.switch_delete)
    public void onDelete() {
        if (null == mDeleteDialog) {
            mDeleteDialog = new DeleteDialog(getContext(), mClickListener);
        }
        if (!mDeleteDialog.isShowing()) {
            mDeleteDialog.show();
        }
    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
        if (null == mShareDialog) {
            mShareDialog = new ShareDialog(getContext(), mShareListener);
        }

        if (!mShareDialog.isShowing()) {
            mShareDialog.show();
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BEGIN:
                    break;
                case MSG_FINISH:
                    Toast.makeText(getContext(), "保存成功，路径为：" + AppConfig.FFMPEG_PATH, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    private DeleteDialog.OnClickListener mClickListener = new DeleteDialog.OnClickListener() {
        @Override
        public void onNegative() {

        }

        @Override
        public void onPositive() {
            String dir = mCurrentDataPath.substring(0, mCurrentDataPath.lastIndexOf(AppConfig.SLASH));
            FileUtils.deleteDir(new File(dir), true);
            getActivity().finish();
        }
    };

    private ShareDialog.OnListener mShareListener = new ShareDialog.OnListener() {
        @Override
        public void onConfirm(int type, int id) {
        }
    };

    private Object mEventListener = new Object() {
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(ActionEvent event) {
            getActivity().finish();
        }
    };

}
