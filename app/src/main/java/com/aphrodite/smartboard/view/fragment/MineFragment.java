package com.aphrodite.smartboard.view.fragment;

import android.content.Intent;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-4-15
 */
public class MineFragment extends BaseFragment {
    @Override
    protected int getViewId() {
        return R.layout.fragment_mine;
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

    @OnClick(R.id.login_btn)
    public void onLoginClick() {
        Intent intent = new Intent(IntentAction.LoginAction.ACTION);
        getContext().startActivity(intent);
    }

}
