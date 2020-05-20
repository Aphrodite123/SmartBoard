package com.aphrodite.smartboard.view.activity;

import android.widget.TextView;

import com.aphrodite.framework.config.base.BaseConfig;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;
import com.aphrodite.smartboard.config.RuntimeConfig;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;
import com.aphrodite.smartboard.view.widget.dialog.CustomMessageDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-4-22
 */
public class SettingActivity extends BaseActivity {
    @BindView(R.id.app_version_name)
    TextView mAppVersionName;

    private CustomMessageDialog messageDialog = null;

    @Override
    protected int getViewId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK);
        setLeftBtnRes(R.drawable.back);
        setTitleText(R.string.setting_for_app);
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        StringBuffer versionName = new StringBuffer();
        versionName.append(getString(R.string.app_name)).append(BaseConfig.PREFIX_VERSION).append(RuntimeConfig.APP_VERSION);
        mAppVersionName.setText(versionName.toString());
    }

    @OnClick(R.id.logout)
    public void onLogout() {
        if (null == messageDialog) {
            messageDialog = new CustomMessageDialog(this, mDialogClickListener);
        }
        messageDialog.setTitle(R.string.logout);
        messageDialog.setMessage(R.string.dialog_logout_message);
        messageDialog.setPositiveText(R.string.logout);
        messageDialog.setNegativeText(R.string.cancel);
        if (!messageDialog.isShowing()) {
            messageDialog.show();
        }
    }

    private CustomMessageDialog.OnDialogClickListener mDialogClickListener = new CustomMessageDialog.OnDialogClickListener() {
        @Override
        public void onNegative() {

        }

        @Override
        public void onPositive() {
            MainApplication.logout();
            finish();
        }
    };

}
