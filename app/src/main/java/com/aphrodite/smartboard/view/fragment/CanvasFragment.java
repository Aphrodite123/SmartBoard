package com.aphrodite.smartboard.view.fragment;

import android.widget.TextView;

import com.apeman.sdk.widget.BoardView;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class CanvasFragment extends BaseFragment {
    @BindView(R.id.board_view)
    public BoardView mBoardView;
    @BindView(R.id.msg)
    public TextView msg;

    @Override
    protected int getViewId() {
        return R.layout.fragment_canvas;
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

    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
    }

}
