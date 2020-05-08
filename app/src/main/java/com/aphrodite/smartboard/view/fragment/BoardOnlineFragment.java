package com.aphrodite.smartboard.view.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-5-8
 */
public class BoardOnlineFragment extends BaseFragment {
    @BindView(R.id.palette_online_root)
    RelativeLayout mPaletteOnlineRoot;
    @BindView(R.id.palette_online_bg)
    ImageView mPaletteOnlineBg;
    @BindView(R.id.palette_online_canvas)
    SimpleDoodleView mPaletteOnlineCanvas;

    private BoardStatusListener mStatusListener;

    public BoardOnlineFragment(BoardStatusListener statusListener) {
        this.mStatusListener = statusListener;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_board_online;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK | TITLE_FLAG_SHOW_RIGHT_BTN);
        setLeftBtnRes(R.drawable.back);
        setRightBtnRes(R.drawable.icon_share);
        setTitleText("2020.5.8");
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.switch_editor)
    public void onSwitchEditor() {
        if (null != mStatusListener) {
            mStatusListener.onEditor();
        }
    }

}
