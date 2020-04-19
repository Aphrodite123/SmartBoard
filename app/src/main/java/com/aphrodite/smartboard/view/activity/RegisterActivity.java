package com.aphrodite.smartboard.view.activity;

import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.model.network.WebServiceUtils;
import com.aphrodite.smartboard.model.network.inter.IResponseListener;
import com.aphrodite.smartboard.model.network.task.NetworkAsyncTask;
import com.aphrodite.smartboard.model.timer.CustomCountDownTimer;
import com.aphrodite.smartboard.utils.LogUtils;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class RegisterActivity extends BaseActivity implements IResponseListener {
    @BindView(R.id.input_phone_number)
    EditText mInputPhoneNumber;
    @BindView(R.id.register_btn)
    Button mRegisterBtn;

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

    private CustomCountDownTimer mDownTimer;

    private NetworkAsyncTask mNetworkAsyncTask = null;

    @Override
    protected int getViewId() {
        return R.layout.activity_register;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK);
        setLeftBtnRes(R.drawable.back);
        setTitleColor(getResources().getColor(R.color.color_626262));
        setTitleText(R.string.register_quickly);

        mInputPhoneNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mPasswordFormatIcon.setVisibility(View.GONE);
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
    protected void onDestroy() {
        super.onDestroy();
        if (null != mDownTimer) {
            mDownTimer.cancel();
            mDownTimer = null;
        }
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

    @OnClick(R.id.register_btn)
    public void onRegister() {
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
            case WebServiceUtils.NET_URL_REGUSER:
                LogUtils.d("Enter to result. " + method + list.toString() + object);
                break;
            default:
                break;
        }
    }

    private CustomCountDownTimer.CountDownListener mCountDownListener = new CustomCountDownTimer.CountDownListener() {

        @Override
        public void onTick(long time) {
            mAuthCode.setClickable(false);
            mAuthCode.setText(String.format(getResources().getString(R.string.format_auth_code_regain), time / 1000));
        }

        @Override
        public void onFinish() {
            mAuthCode.setClickable(true);
            mAuthCode.setText(getResources().getString(R.string.get_auth_code));
        }
    };

}
