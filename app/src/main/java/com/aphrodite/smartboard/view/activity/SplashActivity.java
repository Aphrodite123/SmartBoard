package com.aphrodite.smartboard.view.activity;

import android.content.Intent;
import android.widget.ImageView;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;

import butterknife.BindView;

/**
 * Created by Aphrodite on 20-4-22
 */
public class SplashActivity extends BaseActivity {
    @BindView(R.id.splash_logo)
    ImageView mSplashLogo;

    @Override
    protected int getViewId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        mSplashLogo.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(IntentAction.MainAction.ACTION);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        }, 1000);
    }

}
