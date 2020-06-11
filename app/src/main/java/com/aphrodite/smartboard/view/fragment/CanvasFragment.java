package com.aphrodite.smartboard.view.fragment;

import android.widget.TextView;

import com.apeman.sdk.widget.BoardView;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

import java.io.File;

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
        loadOnLineData();
    }

    private void loadOnLineData() {
        File file = new File(AppConfig.BOARD_ONLINE_PATH);
        if (!file.exists()) {
            return;
        }

        String[] workFolders = file.list();
        if (ObjectUtils.isEmpty(workFolders)) {
            return;
        }

//        CW cw = null;
//        for (String fold : workFolders) {
//            if (TextUtils.isEmpty(fold)) {
//                continue;
//            }
//
//            cw = CWFileUtils.read(AppConfig.DATA_PATH + fold + File.separator + AppConfig.DATA_FILE_NAME);
//            mCws.add(cw);
//        }
    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
    }

}
