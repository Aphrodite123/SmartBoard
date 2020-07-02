package com.aphrodite.smartboard.application;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.multidex.MultiDex;

import com.aphrodite.framework.model.network.api.RetrofitInitial;
import com.aphrodite.framework.model.network.interceptor.BaseCommonParamInterceptor;
import com.aphrodite.framework.utils.SPUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.application.base.BaseApplication;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.RuntimeConfig;
import com.aphrodite.smartboard.model.database.migration.GlobalRealmMigration;
import com.aphrodite.smartboard.utils.LogUtils;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.FileNotFoundException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;

import static com.aphrodite.smartboard.config.RuntimeConfig.DATABASE_NAME;
import static com.aphrodite.smartboard.config.RuntimeConfig.DATABASE_VERSION;

/**
 * Created by Aphrodite on 2018/7/26.
 */
public class MainApplication extends BaseApplication {
    private static MainApplication mIpenApplication;

    private int mActivityCount;

    private RealmConfiguration mRealmConfiguration;

    private static IWXAPI mWXApi;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    @Override
    protected void initSystem() {
        this.mIpenApplication = this;

        registerActivityLifecycleCallbacks(lifecycleCallbacks);

        Logger.addLogAdapter(new AndroidLogAdapter());

        initRealm();

        initStetho();

        initToast();

        SPUtils.init(this, RuntimeConfig.PACKAGE_NAME);

        registerWX(AppConfig.WX_APP_ID);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initRealm() {
        Realm.init(this);
        createGlobalRealm();
    }

    public boolean isAppBackground() {
        return 0 == mActivityCount;
    }

    public RetrofitInitial getRetrofitInit(boolean isJson, String baseUrl, BaseCommonParamInterceptor paramInterceptor) {
        RetrofitInitial retrofitInitial = new RetrofitInitial
                .Builder()
                .with(getApplication())
                .isJson(isJson)
                .baseUrl(baseUrl)
                .commonParamInterceptor(paramInterceptor)
                .build();
        return retrofitInitial;
    }

    private void createGlobalRealm() {
        mRealmConfiguration = new RealmConfiguration.Builder()
                .name(DATABASE_NAME)
                .schemaVersion(DATABASE_VERSION)
                //开发阶段，删除旧版本数据
//                .deleteRealmIfMigrationNeeded()
                .migration(new GlobalRealmMigration())
                .build();
        Realm.setDefaultConfiguration(mRealmConfiguration);
    }

    public Realm getGlobalRealm() throws FileNotFoundException {
        Realm realm;
        if (null == mRealmConfiguration) {
            mRealmConfiguration = new RealmConfiguration.Builder()
                    .name(DATABASE_NAME)
                    .schemaVersion(DATABASE_VERSION)
                    .migration(new GlobalRealmMigration())
                    .build();
        }

        try {
            realm = Realm.getInstance(mRealmConfiguration);
        } catch (RealmMigrationNeededException e) {
            LogUtils.e("Enter getGlobalRealm method.RealmMigrationNeededException: " + e);
            Realm.migrateRealm(mRealmConfiguration, new GlobalRealmMigration());
            realm = Realm.getInstance(mRealmConfiguration);
        }

        return realm;
    }

    /**
     * 退出程序
     */
    public void exit() {
        System.exit(0);
    }

    /**
     * 初始化Stetho调试工具
     */
    private void initStetho() {
//        Stetho.initialize(Stetho.newInitializerBuilder(this)
//                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                .enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
//                .build());
    }

    private void initToast() {
        ToastUtils.init(this);
    }

    private void registerWX(String appId) {
        mWXApi = WXAPIFactory.createWXAPI(this, appId, true);
        mWXApi.registerApp(appId);
    }

    public static void logout() {
        SPUtils.remove(AppConfig.SharePreferenceKey.PHONE_NUMBER);
        SPUtils.remove(AppConfig.SharePreferenceKey.AUTH_CODE);
    }

    public static MainApplication getApplication() {
        return mIpenApplication;
    }

    public static IWXAPI getWXApi() {
        return mWXApi;
    }

    private ActivityLifecycleCallbacks lifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            mActivityCount++;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            mActivityCount--;
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

}
