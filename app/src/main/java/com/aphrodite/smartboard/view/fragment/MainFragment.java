package com.aphrodite.smartboard.view.fragment;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

/**
 * Created by Aphrodite on 20-4-22
 */
public class MainFragment extends BaseFragment {
    @Override
    protected int getViewId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_RIGHT_BTN);
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
    }

}
