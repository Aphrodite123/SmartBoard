package com.aphrodite.smartboard.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

/**
 * Created by zhangjingming on 2017/8/3.
 * 微信分享
 */

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainApplication.getWXApi().handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        MainApplication.getWXApi().handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                ToastUtils.showMessage(getResources().getString(R.string.prompt_share_success));
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                ToastUtils.showMessage(getResources().getString(R.string.prompt_share_cancel));
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                ToastUtils.showMessage(getResources().getString(R.string.prompt_share_failed));
                break;
            default:
                break;
        }
        finish();
    }
}
