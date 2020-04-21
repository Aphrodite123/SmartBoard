package com.aphrodite.smartboard.view.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.SPUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.network.WebServiceUtils;
import com.aphrodite.smartboard.model.network.inter.IResponseListener;
import com.aphrodite.smartboard.model.network.task.NetworkAsyncTask;
import com.aphrodite.smartboard.utils.LogUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-4-15
 */
public class MineFragment extends BaseFragment implements IResponseListener {
    @BindView(R.id.login_btn)
    Button mLoginBtn;

    private String mPhoneNumber;
    private String mAuthCode;
    private NetworkAsyncTask mNetworkAsyncTask = null;

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
        mPhoneNumber = (String) SPUtils.get(AppConfig.SharePreferenceKey.PHONE_NUMBER, "");
        mAuthCode = (String) SPUtils.get(AppConfig.SharePreferenceKey.AUTH_CODE, "");
        if (TextUtils.isEmpty((mPhoneNumber)) || TextUtils.isEmpty(mAuthCode)) {
            mLoginBtn.setVisibility(View.VISIBLE);
        } else {
            mLoginBtn.setVisibility(View.GONE);

            getUserSet();
        }
    }

    private void getUserSet() {
        List<String> reqValue = new ArrayList<>();
        reqValue.add(mPhoneNumber);
        reqValue.add(mAuthCode);
        reqValue.add("");
        reqValue.add("");
        reqValue.add("");
        reqValue.add("");
        reqValue.add("");
        reqValue.add("");

        mNetworkAsyncTask = new NetworkAsyncTask(WebServiceUtils.NET_URL_GET_USER_SET, WebServiceUtils.NET_URL_GET_USER_SET_KEY, reqValue, null, getContext(), this);
        mNetworkAsyncTask.execute();
    }

    @Override
    public void result(String method, List<String> list, Object object) {
        switch (method) {
            case WebServiceUtils.NET_URL_GET_USER_SET:
                LogUtils.d("Enter to result." + method + list.toString() + object);
                dismissLoadingDialog();
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.login_btn)
    public void onLoginClick() {
        Intent intent = new Intent(IntentAction.LoginAction.ACTION);
        getContext().startActivity(intent);
    }

}
