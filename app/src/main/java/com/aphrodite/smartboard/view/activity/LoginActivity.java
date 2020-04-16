package com.aphrodite.smartboard.view.activity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;

import butterknife.BindView;

/**
 * Created by Aphrodite on 20-4-15
 */
public class LoginActivity extends BaseActivity {
    @BindView(R.id.login_root)
    LinearLayout mLoginRoot;
    @BindView(R.id.logo)
    ImageView mLogo;
    @BindView(R.id.input_phone_number)
    EditText mInputPhoneNumber;
    @BindView(R.id.login_btn)
    Button mLoginBtn;
    @BindView(R.id.login_auth)
    TextView mLoginAuth;
    @BindView(R.id.register)
    TextView mRegister;

    @BindView(R.id.login_input_root)
    RelativeLayout mLoginInputRoot;
    @BindView(R.id.password_format_icon)
    ImageView mPasswordFormatIcon;
    @BindView(R.id.input_et)
    EditText mInput;
    @BindView(R.id.forgot_password)
    TextView mForgotPassword;

    @Override
    protected int getViewId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK);
        setLeftBtnRes(R.drawable.back);
        setTitleColor(getResources().getColor(R.color.color_626262));
        showKeyBoard(mInputPhoneNumber);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyBoard();
    }
}
