package com.aphrodite.smartboard.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;
import com.aphrodite.smartboard.view.fragment.BoardEditorFragment;
import com.aphrodite.smartboard.view.fragment.BoardOnlineFragment;
import com.aphrodite.smartboard.view.fragment.BoardPlayFragment;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aphrodite on 20-4-22
 * 画板
 */
public class CanvasActivity extends BaseActivity {
    private List<BaseFragment> mFragments;

    //设备是否在线
    private boolean mDeviceOnline = true;
    private String mCurrentDataPath;
    private String mCurrentAudioPath;
    private String mCurrentImagePath;

    @Override
    protected int getViewId() {
        return R.layout.activity_canvas;
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
        Intent intent = getIntent();
        if (null != intent) {
            mCurrentDataPath = intent.getStringExtra(IntentAction.CanvasAction.PATH_TRACK_FILE);
            mCurrentAudioPath = intent.getStringExtra(IntentAction.CanvasAction.PATH_AUDIO_FILE);
            mCurrentImagePath = intent.getStringExtra(IntentAction.CanvasAction.PATH_COVER_IMAGE);
        }

        mFragments = new ArrayList<>();
        BoardOnlineFragment onlineFragment = new BoardOnlineFragment(mStatusListener);
        BoardPlayFragment playFragment = new BoardPlayFragment(mStatusListener);
        BoardEditorFragment editorFragment = new BoardEditorFragment(mStatusListener);

        Bundle bundle = new Bundle();
        bundle.putString(IntentAction.CanvasAction.PATH_TRACK_FILE, mCurrentDataPath);
        bundle.putString(IntentAction.CanvasAction.PATH_AUDIO_FILE, mCurrentAudioPath);
        bundle.putString(IntentAction.CanvasAction.PATH_COVER_IMAGE, mCurrentImagePath);
        onlineFragment.setArguments(bundle);
        playFragment.setArguments(bundle);
        editorFragment.setArguments(bundle);

        mFragments.add(onlineFragment);
        mFragments.add(playFragment);
        mFragments.add(editorFragment);

        switchFragment(mDeviceOnline ? 0 : 2);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void clickBack(View view) {
    }

    private void switchFragment(int index) {
        if (ObjectUtils.isOutOfBounds(mFragments, index)) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root, mFragments.get(index));
        fragmentTransaction.commit();
    }

    private BoardStatusListener mStatusListener = new BoardStatusListener() {
        @Override
        public void onPreview() {
            switchFragment(0);
        }

        @Override
        public void onPlay() {
            switchFragment(1);
        }

        @Override
        public void onEditor() {
            switchFragment(2);
        }
    };

}
