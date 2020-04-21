package com.aphrodite.smartboard.view.activity;

import android.content.Intent;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.SPUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.network.WebServiceUtils;
import com.aphrodite.smartboard.model.network.inter.IResponseListener;
import com.aphrodite.smartboard.model.network.task.NetworkAsyncTask;
import com.aphrodite.smartboard.model.timer.CustomCountDownTimer;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-4-15
 */
public class LoginActivity extends BaseActivity implements IResponseListener {
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
    private CustomCountDownTimer mDownTimer;

    private NetworkAsyncTask mNetworkAsyncTask = null;

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
//        mInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mLoginAuth.setText(getString(R.string.login_by_password));
        mInput.setHint(getString(R.string.hint_input_auth));
        mInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        mForgotPassword.setVisibility(View.GONE);
        mAuthCode.setVisibility(View.VISIBLE);
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

    @OnClick(R.id.login_btn)
    public void onLogin() {
        showLoadingDialog();
        login();
    }

    @OnClick(R.id.register)
    public void onRegister() {
        Intent intent = new Intent(IntentAction.RegisterAction.ACTION);
        startActivity(intent);
    }

    @OnClick(R.id.auth_code)
    public void onAuthCode() {
        if (TextUtils.isEmpty(mInputPhoneNumber.getText().toString())) {
            ToastUtils.showMessage(R.string.prompt_phone_not_empty);
            return;
        }

        if (mInputPhoneNumber.getText().toString().length() != 11) {
            ToastUtils.showMessage(R.string.prompt_account_wrong);
            return;
        }

        mDownTimer = new CustomCountDownTimer(60 * 1000, 1000, mCountDownListener);
        mDownTimer.start();

        sendAuthCode();
    }

    private void login() {
        if (TextUtils.isEmpty(mInputPhoneNumber.getText().toString())) {
            ToastUtils.showMessage(R.string.prompt_phone_not_empty);
            return;
        }

        if (TextUtils.isEmpty(mInput.getText().toString())) {
            ToastUtils.showMessage(R.string.prompt_password_not_empty);
            return;
        }

        List<String> reqValue = new ArrayList<>();

        reqValue.add(mInputPhoneNumber.getText().toString());
        reqValue.add(mInput.getText().toString());
        reqValue.add("");

        mNetworkAsyncTask = new NetworkAsyncTask(WebServiceUtils.NET_URL_LOGIN, WebServiceUtils.NET_URL_LOGIN_KEY, reqValue, null, this, this);
        mNetworkAsyncTask.execute();
    }

    private void sendAuthCode() {
        if (TextUtils.isEmpty(mInputPhoneNumber.getText().toString())) {
            ToastUtils.showMessage(R.string.prompt_phone_not_empty);
            return;
        }

        if (mInputPhoneNumber.getText().toString().length() != 11) {
            ToastUtils.showMessage(R.string.prompt_account_wrong);
            return;
        }

        List<String> reqValue = new ArrayList<>();

        reqValue.add(mInputPhoneNumber.getText().toString());
        reqValue.add("");

        mNetworkAsyncTask = new NetworkAsyncTask(WebServiceUtils.NET_URL_REGUSER, WebServiceUtils.NET_URL_REGUSER_KEY, reqValue, null, this, this);
        mNetworkAsyncTask.execute();
    }

    @Override
    public void result(String method, List<String> list, Object object) {
        switch (method) {
            case WebServiceUtils.NET_URL_LOGIN:
                if (ObjectUtils.isOutOfBounds(list, 2)) {
                    if ("true".equals(list.get(0))) {
                        SPUtils.put(AppConfig.SharePreferenceKey.PHONE_NUMBER, mInputPhoneNumber.getText().toString());
                        SPUtils.put(AppConfig.SharePreferenceKey.AUTH_CODE, mInput.getText().toString());

                        finish();
                    } else {
                        ToastUtils.showMessage(list.get(1));
                    }
                }
                dismissLoadingDialog();
                break;
            default:
                break;
        }
    }

    private CustomCountDownTimer.CountDownListener mCountDownListener = new CustomCountDownTimer.CountDownListener() {

        @Override
        public void onTick(long time) {
            if (null == mAuthCode) {
                return;
            }
            mAuthCode.setClickable(false);
            mAuthCode.setText(String.format(getResources().getString(R.string.format_auth_code_regain), time / 1000));
        }

        @Override
        public void onFinish() {
            if (null == mAuthCode) {
                return;
            }
            mAuthCode.setClickable(true);
            mAuthCode.setText(getResources().getString(R.string.get_auth_code));
        }
    };

}
