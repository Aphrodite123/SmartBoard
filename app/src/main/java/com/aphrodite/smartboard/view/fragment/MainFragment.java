package com.aphrodite.smartboard.view.fragment;

import android.content.Intent;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.widget.dialog.ShareDialog;

import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-4-22
 */
public class MainFragment extends BaseFragment {
    private ShareDialog mShareDialog = null;

    @Override
    protected int getViewId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_RIGHT_BTN);
        setRightBtnRes(R.drawable.share_toolbar_icon);
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mShareDialog) {
            mShareDialog.dismiss();
        }
    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
        if (null == mShareDialog) {
            mShareDialog = new ShareDialog(getContext(), mShareListener);
        }

        if (!mShareDialog.isShowing()) {
            mShareDialog.show();
        }
    }

    private ShareDialog.OnListener mShareListener = new ShareDialog.OnListener() {
        @Override
        public void onConfirm(int type, int id) {

        }
    };

}
