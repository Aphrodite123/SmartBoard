package com.aphrodite.smartboard.view.activity;

import android.text.InputFilter;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

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
    @BindView(R.id.auth_code)
    TextView mAuthCode;

    private boolean mPasswordInvisiable = true;

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

        mInputPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        hideKeyBoard(this, mInputPhoneNumber);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @OnClick(R.id.login_auth)
    public void onSwitchLogin() {
        if (null == mLoginAuth) {
            return;
        }

        if (getString(R.string.login_by_auth).equals(mLoginAuth.getText())) {
            mLoginAuth.setText(getString(R.string.login_by_password));
            mInput.setHint(getString(R.string.hint_input_auth));
            mForgotPassword.setVisibility(View.GONE);
            mAuthCode.setVisibility(View.VISIBLE);
        } else {
            mLoginAuth.setText(getString(R.string.login_by_auth));
            mInput.setHint(getString(R.string.hint_input_password));
            mForgotPassword.setVisibility(View.VISIBLE);
            mAuthCode.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.password_format_icon)
    public void onSwitchPassword() {
        if (null == mInput) {
            return;
        }

        hideKeyBoard(this, mInput);

        mPasswordFormatIcon.requestFocus();
        if (mPasswordInvisiable) {
            mInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            mPasswordFormatIcon.setSelected(true);
            mPasswordInvisiable = false;
        } else {
            mInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
            mPasswordFormatIcon.setSelected(false);
            mPasswordInvisiable = true;
        }
        mInput.setSelection(mInput.length());
    }

}
