package com.aphrodite.smartboard.view.activity;

import android.view.View;
import android.widget.LinearLayout;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.utils.LogUtils;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;
import com.aphrodite.smartboard.view.widget.popupwindow.PaletePopupWindow;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-4-22
 * 畫板
 */
public class CanvasActivity extends BaseActivity {
    @BindView(R.id.canvas_bottom_tab)
    LinearLayout mCanvasBottomTab;

    private PaletePopupWindow mPaletePopupWindow;
    private List<Integer> mColorIds;

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
    }

    @Override
    public void clickRightBtn(View view) {
        LogUtils.d(view.toString());
    }

    @OnClick(R.id.switch_color)
    public void onSwitchColor() {
        if (null == mPaletePopupWindow) {
            mPaletePopupWindow = new PaletePopupWindow(this, mColorIds);
        }
        if (!mPaletePopupWindow.isShowing()) {
            mPaletePopupWindow.showAsDropDown(mCanvasBottomTab);
        }
    }

}
