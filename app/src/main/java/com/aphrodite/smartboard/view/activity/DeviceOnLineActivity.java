package com.aphrodite.smartboard.view.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.apeman.sdk.bean.BoardType;
import com.apeman.sdk.bean.DevicePoint;
import com.apeman.sdk.bean.NoteDescription;
import com.apeman.sdk.service.ConnectStatus;
import com.apeman.sdk.service.PenService;
import com.apeman.sdk.service.command.Command;
import com.apeman.sdk.widget.BoardView;
import com.apeman.sdk.widget.BoardViewCallback;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWLine;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.bean.ShareContentType;
import com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler;
import com.aphrodite.smartboard.utils.BitmapUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FFmpegUtils;
import com.aphrodite.smartboard.utils.FileUtil;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.utils.LogUtils;
import com.aphrodite.smartboard.utils.Share2;
import com.aphrodite.smartboard.view.activity.base.BaseDeviceActivity;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.Observer;
import butterknife.BindView;
import butterknife.OnClick;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_BEGIN;
import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_FINISH;

/**
 * Created by Aphrodite on 20-6-19
 */
public class DeviceOnLineActivity extends BaseDeviceActivity {
    @BindView(R.id.board_view)
    BoardView mBoardView;

    private ProgressDialog mCleanProgressDialog;
    private DeleteDialog mPromptDialog;

    private int mStrokeWidth = 10;
    private int mDrawColor = Color.BLACK;
    private long mTimestamp;

    //按照设备比例缩放后的画布宽度
    private int mCanvasWidth;
    //按照设备比例缩放后的画布高度
    private int mCanvasHeight;

    //将设备坐标点转换为画布坐标点的缩放比例
    private Double mXScale;
    private Double mYScale;

    private Paint mPaint;
    private List<DevicePoint> mDevicePoints;
    private List<ScreenRecordEntity> data = new ArrayList<>();

    private static int LINE_PARTS = 5;
    private CW mCw;
    private List<List<Integer>> mPoints;
    private float mLastX;
    private float mLastY;
    private int mImageNum;
    private FFmpegHandler mFfmpegHandler;

    @Override
    protected int getViewId() {
        return R.layout.activity_device_online;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK | TITLE_FLAG_SHOW_RIGHT_BTN);
        setLeftBtnRes(R.drawable.back);
        setRightBtnRes(R.drawable.share_toolbar_icon);
        setTitleColor(getResources().getColor(R.color.color_626262));
        getDeviceInfo();
        initBoardView();
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {
        mFfmpegHandler = new FFmpegHandler(mHandler);
        mPoints = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PenService.Companion.disconnectUsbService(this, this);

        if (null != mUsbPenService) {
            mUsbPenService.observeDevicePoint(null);
            mUsbPenService.getCommandLiveData().removeObserver(getCmdObserver());
        }
    }

    @Override
    protected Observer<ConnectStatus> getConnObserver() {
        return new Observer<ConnectStatus>() {
            @Override
            public void onChanged(ConnectStatus connectStatus) {
                if (connectStatus.getResult()) {
                    //这里需要注意一定要在完成画板的初始化后再监听硬件的报点
                    mUsbPenService.observeDevicePoint(new Function1<DevicePoint, Unit>() {
                        @Override
                        public Unit invoke(DevicePoint devicePoint) {
                            mBoardView.onPointReceived(devicePoint);

                            parseDevicePoint(devicePoint);
                            return Unit.INSTANCE;
                        }
                    });
                } else {
                    finish();
                }
            }
        };
    }

    @Override
    protected Observer<Command> getCmdObserver() {
        return new Observer<Command>() {
            @Override
            public void onChanged(Command command) {
                if (null == command) {
                    return;
                }

                switch (command.getExtra()) {
                    case 0x00:
                        cleanBoardView();
                        break;
                    case 0x01:
                        if (null != mBoardView) {
                            mBoardView.newPage();
                        }
                        break;
                }
            }
        };
    }

    private void initBoardView() {
        mBoardView.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                NoteDescription description = null;
                if (null != intent) {
                    description = intent.getParcelableExtra("versionInfo");
                }
                mBoardView.setup(BoardType.NoteMaker, description);
                mBoardView.setLoadFinishCallback(new BoardViewCallback() {
                    @Override
                    public void onLoadFinished() {
                        PenService.Companion.connectUsbService(DeviceOnLineActivity.this, DeviceOnLineActivity.this);
                    }
                });
            }
        });
    }

    public void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(mDrawColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void getDeviceInfo() {
        BoardType boardType = BoardType.NoteMaker;
        float deviceScale = (float) (boardType.getMaxX() / boardType.getMaxY());
        int viewWidth = UIUtils.getDisplayWidthPixels(this);
        int viewHeight = UIUtils.getDisplayHeightPixels(this);
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

    private void parseDevicePoint(DevicePoint devicePoint) {
        if (null == devicePoint) {
            return;
        }

        switch (devicePoint.getState()) {
            case (byte) 0x00:
                LogUtils.d("Enter to parseDevicePoint. 离开");
                //离开
                mDevicePoints = null;
                break;
            case (byte) 0x10:
                LogUtils.d("Enter to parseDevicePoint. 悬空");
                //悬空
                if (ObjectUtils.isEmpty(mDevicePoints)) {
                    break;
                }

                initPaint();
                int red = (mPaint.getColor() & 0xff0000) >> 16;
                int green = (mPaint.getColor() & 0x00ff00) >> 8;
                int blue = (mPaint.getColor() & 0x0000ff);
                CWFileUtils.writeLine(mDevicePoints, (int) mPaint.getStrokeWidth(), red + "," + green + "," + blue + ",1");
                mDevicePoints.clear();
                break;
            case (byte) 0x11:
                LogUtils.d("Enter to parseDevicePoint. 压下");
                //压下
                if (null == mDevicePoints) {
                    mDevicePoints = new ArrayList<>();
                }

                if (devicePoint.getPressure() > 0) {
                    mDevicePoints.add(devicePoint);
                }
                break;
        }
    }

    private void saveData() {
        ScreenRecordEntity screenRecordEntity = new ScreenRecordEntity();
        screenRecordEntity.setType("data/0");
        data.add(screenRecordEntity);

        mTimestamp = System.currentTimeMillis();
        CWFileUtils.write(data, AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA, mCanvasWidth, mCanvasHeight, mTimestamp);
    }

    private void pathsCut() {
        mCw = CWFileUtils.read(AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA + AppConfig.DATA_FILE_NAME);
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
        initPaint();

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
            BitmapUtils.saveBitmap(bitmap, AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA, imageName.toString(), Bitmap.CompressFormat.JPEG, 100);
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
        if (!FileUtils.isExist(AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA)) {
            return;
        }

        String combineVideo = AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA + "video.mp4";
        int frameRate = 10;// 合成视频帧率建议:1-10  普通视频帧率一般为25
        String[] commandLine = FFmpegUtils.pictureToVideo(AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA, frameRate, combineVideo);
        if (mFfmpegHandler != null) {
            mFfmpegHandler.executeFFmpegCmd(commandLine);
        }
    }

    private void cleanBoardView() {
        if (null == mCleanProgressDialog) {
            mCleanProgressDialog = new ProgressDialog(this);
            mCleanProgressDialog.setCancelable(false);
        }
        if (!mCleanProgressDialog.isShowing()) {
            mCleanProgressDialog.show();
        }

        mBoardView.clearBoardAndFile(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (null != mCleanProgressDialog) {
                    mCleanProgressDialog.dismiss();
                }
                return Unit.INSTANCE;
            }
        });
    }

    private void showDialog() {
        if (null == mPromptDialog) {
            mPromptDialog = new DeleteDialog(this, mClickListener);
        }
        mPromptDialog.setTitle(R.string.prompt);
        mPromptDialog.setMessage(R.string.message_save_image);

        if (!mPromptDialog.isShowing()) {
            mPromptDialog.show();
        }
    }

    private void dismissDialog() {
        if (null != mPromptDialog) {
            mPromptDialog.dismiss();
            mPromptDialog = null;
        }
    }

    private void savePicture() {
        String dir = AppConfig.DATA_PATH + mTimestamp;
        String imageName = "shot.jpg";

        try {
            BitmapUtils.saveBitmap(BitmapUtils.shotToView(mBoardView), dir, imageName, Bitmap.CompressFormat.JPEG, 100);
            File file = new File(dir + File.separator + imageName);
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), imageName, null);

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
            sendBroadcast(intent);

            ToastUtils.showMessage(R.string.toast_save_to_gallery_successed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
        LoadSDcardTask loadSDcardTask = new LoadSDcardTask();
        loadSDcardTask.execute();
    }

    private DeleteDialog.OnClickListener mClickListener = new DeleteDialog.OnClickListener() {
        @Override
        public void onNegative() {
            dismissDialog();
        }

        @Override
        public void onPositive() {
            savePicture();
        }
    };

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

                    String combineVideo = AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA + "video.mp4";
                    Uri shareFileUrl = FileUtil.getFileUri(DeviceOnLineActivity.this, ShareContentType.VIDEO, new File(combineVideo));
                    if (null == shareFileUrl) {
                        return;
                    }
                    new Share2.Builder(DeviceOnLineActivity.this)
                            .setContentType(ShareContentType.VIDEO)
                            .setShareFileUri(shareFileUrl)
                            .setTitle(getString(R.string.app_name))
                            .build()
                            .shareBySystem();

                    break;
                default:
                    break;
            }
        }
    };

    private class LoadSDcardTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog();

            String path = AppConfig.DATA_PATH + AppConfig.PATH_ONLINE_DATA;
            File file = new File(path);
            if (file.exists()) {
                FileUtils.deleteDir(file, false);
            }
        }

        @Override
        protected Object doInBackground(Object... objects) {
            saveData();
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            dismissLoadingDialog();

            pathsCut();
        }
    }

}
