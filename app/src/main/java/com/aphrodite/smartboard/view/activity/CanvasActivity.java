package com.aphrodite.smartboard.view.activity;

import android.view.View;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.utils.LogUtils;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;

/**
 * Created by Aphrodite on 20-4-22
 * 畫板
 */
public class CanvasActivity extends BaseActivity {
    @Override
    protected int getViewId() {
        return R.layout.activity_canvas;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK);
        setLeftBtnRes(R.drawable.back);
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    public void clickRightBtn(View view) {
        LogUtils.d(view.toString());
    }

}
