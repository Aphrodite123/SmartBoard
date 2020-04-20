package com.aphrodite.smartboard.view.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.aphrodite.framework.utils.SPUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-4-15
 */
public class MineFragment extends BaseFragment {
    @BindView(R.id.login_btn)
    Button mLoginBtn;

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
        if (TextUtils.isEmpty((CharSequence) SPUtils.get(AppConfig.SharePreferenceKey.AUTH_CODE, ""))) {
            mLoginBtn.setVisibility(View.VISIBLE);
        } else {
            mLoginBtn.setVisibility(View.GONE);
        }

    }

    @OnClick(R.id.login_btn)
    public void onLoginClick() {
        Intent intent = new Intent(IntentAction.LoginAction.ACTION);
        getContext().startActivity(intent);
    }

}
