package com.aphrodite.smartboard.view.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
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

import com.apeman.sdk.bean.BoardType;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWLine;
import com.aphrodite.smartboard.model.bean.ShareContentType;
import com.aphrodite.smartboard.model.bean.WorkBriefBean;
import com.aphrodite.smartboard.model.event.ActionEvent;
import com.aphrodite.smartboard.model.event.SyncEvent;
import com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler;
import com.aphrodite.smartboard.utils.BitmapUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FFmpegUtils;
import com.aphrodite.smartboard.utils.FileUtil;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.utils.Share2;
import com.aphrodite.smartboard.utils.TimeUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;
import com.aphrodite.smartboard.view.widget.dialog.ShareDialog;
import com.aphrodite.smartboard.view.widget.popupwindow.ListPopupWindow;
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

    private String mRootPath;
    private String mCurrentDataPath;
    private String mCurrentImagePath;

    private BoardStatusListener mStatusListener;
    private ShareDialog mShareDialog;
    private DeleteDialog mDeleteDialog;
    private ListPopupWindow mListPopupWindow;
    private List<WorkBriefBean> mBeans;

    //按照设备比例缩放后的画布宽度
    private int mCanvasWidth;
    //按照设备比例缩放后的画布高度
    private int mCanvasHeight;
    //将设备坐标点转换为画布坐标点的缩放比例
    private Double mXScale;
    private Double mYScale;

    private static int LINE_PARTS = 5;
    private CW mCw;
    private FFmpegHandler mFfmpegHandler;
    private Paint mPaint;
    private float mLastX;
    private float mLastY;
    private int mImageNum;
    private List<List<Integer>> mPoints;

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
        mPoints = new ArrayList<>();

        Bundle bundle = getArguments();
        if (null != bundle) {
            mRootPath = bundle.getString(IntentAction.CanvasAction.PATH_ROOT);
        }
        if (!TextUtils.isEmpty(mRootPath)) {
            mCurrentDataPath = mRootPath + AppConfig.DATA_FILE_NAME;
            mCurrentImagePath = mRootPath + AppConfig.COVER_IMAGE_NAME;
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

        mPoints.clear();
        mPoints = null;
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

        mPoints.addAll(points);

        Bitmap bitmap = Bitmap.createBitmap(mCanvasWidth, mCanvasHeight, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        initPaint(color, width);

        List<Integer> xyPoints;
        for (int i = 0; i < mPoints.size(); i++) {
            xyPoints = mPoints.get(i);
            if (ObjectUtils.isOutOfBounds(xyPoints, 2) || xyPoints.get(2) <= 0) {
                continue;
            }
            if (0 == i) {
                canvas.drawPoint((float) (xyPoints.get(0) * mXScale), (float) (xyPoints.get(1) * mYScale), mPaint);
            } else {
                drawPath(canvas, mLastX, mLastY, (float) (xyPoints.get(0) * mXScale), (float) (xyPoints.get(1) * mYScale));
            }
            mLastX = (float) (xyPoints.get(0) * mXScale);
            mLastY = (float) (xyPoints.get(1) * mYScale);
        }

        StringBuffer imageName = new StringBuffer();
        imageName.append("img").append(mImageNum).append(".jpg");
        try {
            BitmapUtils.saveBitmap(bitmap, mCurrentDataPath.substring(0, mCurrentDataPath.lastIndexOf("/")), imageName.toString(), Bitmap.CompressFormat.JPEG, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mImageNum++;
    }

    private void drawPath(Canvas canvas, float startX, float startY, float endX, float endY) {
        float x = endX - startX;
        float y = endY - startY;
        //10倍插点
        int insertCount = (int) (Math.max(Math.abs(x), Math.abs(y)) + 2);
        //S.i("补点：$insertCount")
        float dx = x / insertCount;
        float dy = y / insertCount;
        for (int i = 0; i < insertCount; i++) {
            float insertX = startX + i * dx;
            float insertY = startY + i * dy;
            canvas.drawPoint(insertX, insertY, mPaint);
        }
    }

    /**
     * 图片合成视频
     */
    private void handlePhoto() {
        // 图片所在路径，图片命名格式img+number.jpg
        // 这里指定目录为根目录下img文件夹
        if (!FileUtils.isExist(mRootPath)) {
            return;
        }

        String combineVideo = mRootPath + "video.mp4";
        int frameRate = 10;// 合成视频帧率建议:1-10  普通视频帧率一般为25
        String[] commandLine = FFmpegUtils.pictureToVideo(mRootPath, frameRate, combineVideo);
        if (mFfmpegHandler != null) {
            mFfmpegHandler.executeFFmpegCmd(commandLine);
        }
    }

    /**
     * 生成GIF文件
     */
    private void pictureToGif() {
        if (!FileUtils.isExist(mRootPath)) {
            return;
        }

        String srcFile = mRootPath + "video.mp4";
        String Video2Gif = mRootPath + "pituretogif.gif";
        int gifStart = 0;
        int gifDuration = 5;
        String resolution = "720x1280";//240x320、480x640、1080x1920
        int frameRate = 10;
        String[] commandLine = FFmpegUtils.generateGif(srcFile, gifStart, gifDuration, resolution, frameRate, Video2Gif);
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
            WorkBriefBean bean1 = new WorkBriefBean(getString(R.string.create_time), TimeUtils.msToDateFormat(mCw.getTime(), TimeUtils.FORMAT_CHINESE_ONE));
            WorkBriefBean bean2 = new WorkBriefBean(getString(R.string.edit_time), TimeUtils.msToDateFormat(mCw.getEditTime(), TimeUtils.FORMAT_CHINESE_ONE));
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
//        if (null == mShareDialog) {
//            mShareDialog = new ShareDialog(getContext(), mShareListener);
//        }
//
//        if (!mShareDialog.isShowing()) {
//            mShareDialog.show();
//        }

        String combineVideo = mRootPath + "video.mp4";
        Uri shareFileUrl = FileUtil.getFileUri(getContext(), ShareContentType.VIDEO, new File(combineVideo));
        if (null == shareFileUrl) {
            return;
        }
        new Share2.Builder(getActivity())
                .setContentType(ShareContentType.VIDEO)
                .setShareFileUri(shareFileUrl)
                .setTitle(getString(R.string.app_name))
                .build()
                .shareBySystem();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BEGIN:
                    showLoadingDialog();
                    break;
                case MSG_FINISH:
                    dismissLoadingDialog();
                    if (null != mPoints) {
                        mPoints.clear();
                    }
                    ToastUtils.showMessage(String.format(getResources().getString(R.string.prompt_output_path), mRootPath));
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
            FileUtils.deleteDir(new File(mRootPath), true);
            EventBus.getDefault().post(SyncEvent.REFRESH_WORK_LIST);
            getActivity().finish();
        }
    };

    private ShareDialog.OnListener mShareListener = new ShareDialog.OnListener() {
        @Override
        public void onConfirm(int type, int id) {
            pathsCut();
        }
    };

    private Object mEventListener = new Object() {
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(ActionEvent event) {
            getActivity().finish();
        }
    };

}
