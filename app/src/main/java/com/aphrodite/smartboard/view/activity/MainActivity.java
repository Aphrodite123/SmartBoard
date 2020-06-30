package com.aphrodite.smartboard.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apeman.sdk.bean.BoardType;
import com.apeman.sdk.service.ConnectStatus;
import com.apeman.sdk.service.PenService;
import com.apeman.sdk.service.command.Command;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.SPUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWLine;
import com.aphrodite.smartboard.model.event.SyncEvent;
import com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler;
import com.aphrodite.smartboard.model.handler.UsbHandler;
import com.aphrodite.smartboard.utils.BitmapUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FFmpegUtils;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.utils.ParseUtils;
import com.aphrodite.smartboard.view.activity.base.BaseDeviceActivity;
import com.aphrodite.smartboard.view.adapter.HomeViewPagerAdapter;
import com.aphrodite.smartboard.view.fragment.MainFragment;
import com.aphrodite.smartboard.view.fragment.MineFragment;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.viewpager.ConfigureSlideViewPager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_BEGIN;
import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_FINISH;

public class MainActivity extends BaseDeviceActivity {
    @BindViews({R.id.tab_home_ic, R.id.tab_mine_ic})
    List<ImageView> mTabIcons;
    @BindViews({R.id.tab_home_text, R.id.tab_mine_text})
    List<TextView> mTabTexts;
    @BindView(R.id.tab_viewpager)
    ConfigureSlideViewPager mViewPager;


    private MainFragment mainFragment;
    private MineFragment mineFragment;

    private long mExitTime;

    private FFmpegHandler mFfmpegHandler;

    private List<BaseFragment> mFragments;
    private HomeViewPagerAdapter mPagerAdapter;

    private static final String ASSETS_FILE_PATH = "data";
    private boolean mIsCopied;

    private CopyAssetsToSDcardTask mCopyAssetsToSDcardTask;
    private LoadSDcardTask mLoadSDcardTask;
    private String[] mWorkFolders;
    private List<Path> mPaths;
    private Paint mPaint;
    private float mLastX;
    private float mLastY;

    //按照设备比例缩放后的画布宽度
    private int mCanvasWidth;
    //按照设备比例缩放后的画布高度
    private int mCanvasHeight;

    //将设备坐标点转换为画布坐标点的缩放比例
    private Double mXScale;
    private Double mYScale;

    private Bitmap mBitmap;
    private Canvas mCanvas;

    @Override
    protected int getViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        showLoadingDialog();

        getDeviceInfo();
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {
        UsbHandler.registerUsbDeviceReceiver(this);
        UsbHandler.registerUsbPermissionReceiver(this);

        mCopyAssetsToSDcardTask = new CopyAssetsToSDcardTask();
        mPaths = new ArrayList<>();
        mLoadSDcardTask = new LoadSDcardTask();
        mIsCopied = (boolean) SPUtils.get(AppConfig.SharePreferenceKey.COPY_ASSETS_DATA_TO_SDCARD, false);
        if (hasStoragePermission()) {
            if (mIsCopied) {
                mLoadSDcardTask.execute();
            } else {
                mCopyAssetsToSDcardTask.execute();
                SPUtils.put(AppConfig.SharePreferenceKey.COPY_ASSETS_DATA_TO_SDCARD, true);
            }
        } else {
            requestStoragePermission();
        }

        initMainPage();

        //默认进入首页
        switchTab(0);
        mViewPager.setCurrentItem(0);

        PenService.Companion.connectUsbService(this, this);
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

    private void initMainPage() {
        mainFragment = new MainFragment();
        mineFragment = new MineFragment();
        mFragments = new ArrayList<>();
        mFragments.add(mainFragment);
        mFragments.add(mineFragment);

        mPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setFragments(mFragments);

        mFfmpegHandler = new FFmpegHandler(mHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        UsbHandler.unregisterReceiver(this);

        PenService.Companion.disconnectUsbService(this, this);

        if (null != mUsbPenService) {
            mUsbPenService.observeDevicePoint(null);
            mUsbPenService.getCommandLiveData().removeObserver(getCmdObserver());
        }
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - mExitTime) > 1000) {
            //双击退出
            ToastUtils.showMessage(R.string.press_exit_again);
            mExitTime = System.currentTimeMillis();
        } else {
            // 退出
            MainApplication.getApplication().exit();
        }
    }

    @TargetApi(23)
    private boolean hasStoragePermission() {
        return Build.VERSION.SDK_INT < 23
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(23)
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }

        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, permissions, AppConfig.PermissionType.STORAGE_PERMISSION);
    }

    @TargetApi(23)
    private boolean hasPermission() {
        return Build.VERSION.SDK_INT < 23
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(23)
    private void requestPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }

        String[] permissions = new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, permissions, AppConfig.PermissionType.RECORD_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConfig.PermissionType.STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mIsCopied) {
                        if (null != mLoadSDcardTask) {
                            mLoadSDcardTask.execute();
                        }
                    } else {
                        if (null != mCopyAssetsToSDcardTask) {
                            mCopyAssetsToSDcardTask.execute();
                        }
                        SPUtils.put(AppConfig.SharePreferenceKey.COPY_ASSETS_DATA_TO_SDCARD, true);
                    }
                } else {
                    ToastUtils.showMessage(R.string.permission_denied);
                    finish();
                }
                break;
            case AppConfig.PermissionType.RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(IntentAction.CanvasAction.ACTION);
                    startActivity(intent);
                } else {
                    ToastUtils.showMessage(R.string.permission_denied);
                }
                break;
        }
    }

    @Override
    protected Observer<ConnectStatus> getConnObserver() {
        return new Observer<ConnectStatus>() {
            @Override
            public void onChanged(ConnectStatus connectStatus) {
                if (connectStatus.getResult()) {
                    Intent intent = new Intent(IntentAction.DeviceOnLineAction.ACTION);
                    startActivity(intent);
                } else {
                    ToastUtils.showMessage(connectStatus.getMsg());
                }
            }
        };
    }

    @Override
    protected Observer<Command> getCmdObserver() {
        return new Observer<Command>() {
            @Override
            public void onChanged(Command command) {

            }
        };
    }

    @OnClick(R.id.tab_home_ll)
    public void onGoHomeClick() {
        switchTab(0);
        mViewPager.setCurrentItem(0);
    }

    @OnClick(R.id.tab_mine_ll)
    public void onGoMineClick() {
        switchTab(1);
        mViewPager.setCurrentItem(2);
    }

    @OnClick(R.id.tab_middle_btn)
    public void onMiddleClick() {
        if (!mDeviceConnected) {
            ToastUtils.showMessage(R.string.check_connected_device);
            return;
        }

        Intent intent = new Intent(IntentAction.DeviceOnLineAction.ACTION);
        startActivity(intent);
    }

    //    @OnClick(R.id.create_video_btn)
    public void onCreateVideoClick() {
        ParseUtils.getAssetsJson(this, "phone_region_code.json");

//        if (hasPermission()) {
//            pictureToGif();
//        } else {
//            requestPermission();
//        }
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

    private void pictureToGif() {
        if (!FileUtils.isExist(AppConfig.FFMPEG_PATH)) {
            return;
        }

        String srcFile = AppConfig.FFMPEG_PATH + "video.mp4";
        String Video2Gif = AppConfig.FFMPEG_PATH + "pituretogif.gif";
        int gifStart = 0;
        int gifDuration = 5;
        String resolution = "720x1280";//240x320、480x640、1080x1920
        int frameRate = 10;
        String[] commandLine = FFmpegUtils.generateGif(srcFile, gifStart, gifDuration, resolution, frameRate, Video2Gif);
        if (mFfmpegHandler != null) {
            mFfmpegHandler.executeFFmpegCmd(commandLine);
        }
    }

    private void switchTab(int position) {
        if (!ObjectUtils.isOutOfBounds(mTabIcons, position)) {
            mTabIcons.get(position).setSelected(true);
        }

        if (!ObjectUtils.isOutOfBounds(mTabTexts, position)) {
            mTabTexts.get(position).setSelected(true);
        }

        checkTabStatus(position, false);
    }

    private void checkTabStatus(int position, boolean isSelected) {
        if (null != mTabIcons) {
            ImageView imageView;
            for (int i = 0; i < mTabIcons.size(); i++) {
                imageView = mTabIcons.get(i);
                if (null == imageView || position == i) {
                    continue;
                }
                imageView.setSelected(isSelected);
            }
        }

        if (null != mTabTexts) {
            TextView textView;
            for (int i = 0; i < mTabTexts.size(); i++) {
                textView = mTabTexts.get(i);
                if (null == textView || position == i) {
                    continue;
                }
                textView.setSelected(isSelected);
            }
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

        for (String fold : mWorkFolders) {
            mPaths.clear();
            parsePath(AppConfig.DATA_PATH + fold, AppConfig.DATA_FILE_NAME);
        }
    }

    private void parsePath(String fileDir, String name) {
        String path = fileDir + File.separator + name;
        if (!com.aphrodite.smartboard.utils.FileUtils.isExist(path)) {
            return;
        }

        createBitmapForFile(mCanvasWidth, mCanvasHeight, fileDir, Bitmap.CompressFormat.JPEG, 100);

        CW cw = CWFileUtils.read(path);
        if (null == cw) {
            return;
        }

        List<CWACT> cwacts = cw.getACT();
        if (null == cwacts || cwacts.size() <= 0) {
            return;
        }

        for (CWACT cwact : cwacts) {
            if (null == cwact) {
                continue;
            }
            createPaths(cwact.getLine());
        }
//        drawPathToBitmap(mCanvasWidth, mCanvasHeight, fileDir, Bitmap.CompressFormat.JPEG, 100);
        try {
            BitmapUtils.saveBitmap(mBitmap, fileDir, AppConfig.COVER_IMAGE_NAME, Bitmap.CompressFormat.JPEG, 100);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createBitmapForFile(int width, int height, String fileDir, Bitmap.CompressFormat format, int quality) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBitmap.setHasAlpha(true);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(Color.WHITE);
    }

    //    private void createPaths(CWLine line) {
//        if (null == line) {
//            return;
//        }
//
//        List<List<Integer>> points = line.getPoints();
//        if (ObjectUtils.isEmpty(points)) {
//            return;
//        }
//        String[] split = line.getColor().split("\\,");
//        int color = Color.rgb(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
//        int width = line.getWidth();
//        initPaint(color, width);
//
//        Path path = new Path();
//        List<Integer> xyPoints;
//        for (int i = 0; i < points.size(); i++) {
//            xyPoints = points.get(i);
//            if (ObjectUtils.isOutOfBounds(xyPoints, 1)) {
//                continue;
//            }
//            if (0 == i) {
//                path.moveTo((float) (xyPoints.get(0) * mXScale), (float) (xyPoints.get(1) * mYScale));
//            } else {
//                path.quadTo(mLastX, mLastY, (float) (xyPoints.get(0) * mXScale), (float) (xyPoints.get(1) * mYScale));
//            }
//            mLastX = (float) (xyPoints.get(0) * mXScale);
//            mLastY = (float) (xyPoints.get(1) * mYScale);
//        }
//        mPaths.add(path);
//    }
    private void createPaths(CWLine line) {
        if (null == line) {
            return;
        }

        List<List<Integer>> points = line.getPoints();
        if (ObjectUtils.isEmpty(points)) {
            return;
        }
        String[] split = line.getColor().split("\\,");
        int color = Color.rgb(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
        int width = line.getWidth();
        initPaint(color, width);

        List<Integer> xyPoints;
        for (int i = 0; i < points.size(); i++) {
            xyPoints = points.get(i);
            if (ObjectUtils.isOutOfBounds(xyPoints, 1)) {
                continue;
            }
            if (0 == i) {
                mCanvas.drawPoint((float) (xyPoints.get(0) * mXScale), (float) (xyPoints.get(1) * mYScale), mPaint);
            } else {
                drawPath(mLastX, mLastY, (float) (xyPoints.get(0) * mXScale), (float) (xyPoints.get(1) * mYScale));
            }
            mLastX = (float) (xyPoints.get(0) * mXScale);
            mLastY = (float) (xyPoints.get(1) * mYScale);
        }
    }

    private void drawPath(float startX, float startY, float endX, float endY) {
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
            mCanvas.drawPoint(insertX, insertY, mPaint);
        }
    }

    private void drawPathToBitmap(int width, int height, String fileDir, Bitmap.CompressFormat format, int quality) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(true);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        if (ObjectUtils.isEmpty(mPaths)) {
            return;
        }
        for (int i = 0; i < mPaths.size(); i++) {
            canvas.drawPath(mPaths.get(i), mPaint);
        }

        try {
            BitmapUtils.saveBitmap(bitmap, fileDir, AppConfig.COVER_IMAGE_NAME, format, quality);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BEGIN:
                    break;
                case MSG_FINISH:
                    Toast.makeText(MainActivity.this, "保存成功，路径为：" + AppConfig.FFMPEG_PATH, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 拷贝Assets文件到SDcard路径
     */
    private class CopyAssetsToSDcardTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... objects) {
            return FileUtils.copyAssetsToSDcard(MainActivity.this, ASSETS_FILE_PATH, AppConfig.DATA_PATH);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            boolean result = (boolean) o;
            if (result && null != mLoadSDcardTask) {
                mLoadSDcardTask.execute();
            } else {
                dismissLoadingDialog();
            }
        }
    }

    private class LoadSDcardTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Object... objects) {
            loadSDcardData();
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            dismissLoadingDialog();
            EventBus.getDefault().post(SyncEvent.REFRESH_WORK_LIST);
        }
    }

}
