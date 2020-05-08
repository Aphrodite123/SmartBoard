package com.aphrodite.smartboard.view.activity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;
import com.aphrodite.smartboard.view.fragment.BoardEditorFragment;
import com.aphrodite.smartboard.view.fragment.BoardOnlineFragment;
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
        mFragments = new ArrayList<>();
        mFragments.add(new BoardOnlineFragment(mStatusListener));
        mFragments.add(new BoardEditorFragment(mStatusListener));

        switchFragment(mDeviceOnline ? 0 : 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        public void onPlay() {
            switchFragment(0);
        }

        @Override
        public void onEditor() {
            switchFragment(1);
        }
    };

}
