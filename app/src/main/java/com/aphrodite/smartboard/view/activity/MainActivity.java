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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aphrodite.framework.utils.FileUtils;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWLine;
import com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler;
import com.aphrodite.smartboard.utils.BitmapUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FFmpegUtils;
import com.aphrodite.smartboard.utils.ParseUtils;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;
import com.aphrodite.smartboard.view.adapter.HomeViewPagerAdapter;
import com.aphrodite.smartboard.view.fragment.MainFragment;
import com.aphrodite.smartboard.view.fragment.MineFragment;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.viewpager.ConfigureSlideViewPager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_BEGIN;
import static com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler.MSG_FINISH;

public class MainActivity extends BaseActivity {
    @BindViews({R.id.tab_home_ic, R.id.tab_found_ic, R.id.tab_message_ic, R.id.tab_mine_ic})
    List<ImageView> mTabIcons;
    @BindViews({R.id.tab_home_text, R.id.tab_found_text, R.id.tab_message_text, R.id.tab_mine_text})
    List<TextView> mTabTexts;
    @BindView(R.id.tab_viewpager)
    ConfigureSlideViewPager mViewPager;

    private long mExitTime;

    private FFmpegHandler mFfmpegHandler;

    private List<BaseFragment> mFragments;
    private HomeViewPagerAdapter mPagerAdapter;

    private LoadSDcardTask mLoadSDcardTask;
    private String[] mWorkFolders;
    private List<Path> mPaths;
    private Paint mPaint;
    private float mLastX;
    private float mLastY;

    @Override
    protected int getViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {
        mPaths = new ArrayList<>();
        mLoadSDcardTask = new LoadSDcardTask();
        if (hasStoragePermission()) {
            mLoadSDcardTask.execute();
        } else {
            requestStoragePermission();
        }

        MainFragment mainFragment = new MainFragment();
        MineFragment fragment2 = new MineFragment();
        MineFragment fragment3 = new MineFragment();
        MineFragment mineFragment = new MineFragment();
        mFragments = new ArrayList<>();
        mFragments.add(mainFragment);
        mFragments.add(fragment2);
        mFragments.add(fragment3);
        mFragments.add(mineFragment);

        mPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setFragments(mFragments);

        mFfmpegHandler = new FFmpegHandler(mHandler);

        //默认进入首页
        switchTab(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    if (null != mLoadSDcardTask) {
                        mLoadSDcardTask.execute();
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

    @OnClick(R.id.tab_home_ll)
    public void onGoHomeClick() {
        switchTab(0);
    }

    @OnClick(R.id.tab_found_ll)
    public void onGoFoundClick() {
        switchTab(1);
    }

    @OnClick(R.id.tab_message_ll)
    public void onGoMessageClick() {
        switchTab(2);
    }

    @OnClick(R.id.tab_mine_ll)
    public void onGoMineClick() {
        switchTab(3);
    }

    @OnClick(R.id.tab_middle_btn)
    public void onMiddleClick() {
        if (hasPermission()) {
            Intent intent = new Intent(IntentAction.CanvasAction.ACTION);
            startActivity(intent);
        } else {
            requestPermission();
        }
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

        if (!FileUtils.isFolderExist(AppConfig.FFMPEG_PATH)) {
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
        if (!FileUtils.isFolderExist(AppConfig.FFMPEG_PATH)) {
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

        mViewPager.setCurrentItem(position);

        ImageView imageView;
        for (int i = 0; i < mTabIcons.size(); i++) {
            imageView = mTabIcons.get(i);
            if (null == imageView || position == i) {
                continue;
            }
            imageView.setSelected(false);
        }

        TextView textView;
        for (int i = 0; i < mTabTexts.size(); i++) {
            textView = mTabTexts.get(i);
            if (null == textView || position == i) {
                continue;
            }
            textView.setSelected(false);
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
            parsePath(AppConfig.DATA_PATH + fold, "data.cw");
        }
    }

    private void parsePath(String fileDir, String name) {
        String path = fileDir + File.separator + name;
        if (!com.aphrodite.smartboard.utils.FileUtils.isExist(path)) {
            return;
        }

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
        drawPathToBitmap(UIUtils.getDisplayHeightPixels(this), UIUtils.getDisplayWidthPixels(this), fileDir, Bitmap.CompressFormat.JPEG, 100);
    }

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

        Path path = new Path();
        List<Integer> xyPoints;
        for (int i = 0; i < points.size(); i++) {
            xyPoints = points.get(i);
            if (ObjectUtils.isOutOfBounds(xyPoints, 1)) {
                continue;
            }
            if (0 == i) {
                path.moveTo(xyPoints.get(0), xyPoints.get(1));
            } else {
                path.quadTo(mLastX, mLastY, xyPoints.get(0), xyPoints.get(1));
            }
            mLastX = xyPoints.get(0);
            mLastY = xyPoints.get(1);
        }
        mPaths.add(path);
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

        StringBuilder name = new StringBuilder();
        name.append(AppConfig.COVER_IMAGE_NAME).append(AppConfig.IMAGE_SUFFIX);

        try {
            BitmapUtils.saveBitmap(bitmap, fileDir, name.toString(), format, quality);
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

    private class LoadSDcardTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog();
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
        }
    }

}
