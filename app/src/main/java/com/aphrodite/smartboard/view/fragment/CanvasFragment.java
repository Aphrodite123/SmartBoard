package com.aphrodite.smartboard.view.fragment;

import android.widget.TextView;

import com.apeman.sdk.widget.BoardView;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

import butterknife.BindView;

public class CanvasFragment extends BaseFragment {
    @BindView(R.id.board_view)
    BoardView mBoardView;
    @BindView(R.id.msg)
    TextView msg;

    @Override
    protected int getViewId() {
        return R.layout.fragment_canvas;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

}
