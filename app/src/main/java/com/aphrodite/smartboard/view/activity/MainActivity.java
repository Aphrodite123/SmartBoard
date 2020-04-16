package com.aphrodite.smartboard.view.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aphrodite.framework.utils.FileUtils;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.model.ffmpeg.FFmpegHandler;
import com.aphrodite.smartboard.utils.FFmpegUtil;
import com.aphrodite.smartboard.utils.ParseUtil;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;
import com.aphrodite.smartboard.view.adapter.HomeViewPagerAdapter;
import com.aphrodite.smartboard.view.fragment.MineFragment;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.viewpager.ConfigureSlideViewPager;

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

    @Override
    protected int getViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        //默认进入首页
        switchTab(0);

        MineFragment fragment1 = new MineFragment();
        MineFragment fragment2 = new MineFragment();
        MineFragment fragment3 = new MineFragment();
        MineFragment mineFragment = new MineFragment();
        mFragments = new ArrayList<>();
        mFragments.add(fragment1);
        mFragments.add(fragment2);
        mFragments.add(fragment3);
        mFragments.add(mineFragment);

        mPagerAdapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.setFragments(mFragments);

    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {
        mFfmpegHandler = new FFmpegHandler(mHandler);
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
    private boolean hasPermission() {
        return Build.VERSION.SDK_INT < 23
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @TargetApi(23)
    private void requestPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return;
        }
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(this, permissions, AppConfig.PermissionType.RECORD_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AppConfig.PermissionType.RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    handlePhoto();
                } else {
                    Toast.makeText(this, "没有权限", Toast.LENGTH_LONG).show();
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

    }


    //    @OnClick(R.id.create_video_btn)
    public void onCreateVideoClick() {
        ParseUtil.getAssetsJson(this, "phone_region_code.json");

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

        if (!FileUtils.isFolderExist(AppConfig.VIDEO_PATH)) {
            return;
        }

        String combineVideo = AppConfig.VIDEO_PATH + "video.mp4";
        int frameRate = 10;// 合成视频帧率建议:1-10  普通视频帧率一般为25
        String[] commandLine = FFmpegUtil.pictureToVideo(AppConfig.VIDEO_PATH, frameRate, combineVideo);
        if (mFfmpegHandler != null) {
            mFfmpegHandler.executeFFmpegCmd(commandLine);
        }
    }

    private void pictureToGif() {
        if (!FileUtils.isFolderExist(AppConfig.VIDEO_PATH)) {
            return;
        }

        String srcFile = AppConfig.VIDEO_PATH + "video.mp4";
        String Video2Gif = AppConfig.VIDEO_PATH + "pituretogif.gif";
        int gifStart = 0;
        int gifDuration = 5;
        String resolution = "720x1280";//240x320、480x640、1080x1920
        int frameRate = 10;
        String[] commandLine = FFmpegUtil.generateGif(srcFile, gifStart, gifDuration, resolution, frameRate, Video2Gif);
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_BEGIN:
                    break;
                case MSG_FINISH:
                    Toast.makeText(MainActivity.this, "保存成功，路径为：" + AppConfig.VIDEO_PATH, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };


}
