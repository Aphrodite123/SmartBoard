package com.aphrodite.smartboard.config;

import com.aphrodite.framework.config.base.BaseConfig;

import java.io.File;

/**
 * Created by Aphrodite on 2019/5/28.
 */
public class AppConfig extends BaseConfig {
    public static final String ROOT_PATH = SDCARD_PATH + "com.aphrodite.smartboard/";

    public static final String VIDEO_PATH = ROOT_PATH + "video/";

    public static final String TEMP_PATH = ROOT_PATH + "temp/";

    /**
     * 微信APP ID
     */
    public static final String WX_APP_ID = "wx87889e866c6936fe";

    public interface SharePreferenceKey {
        //用户手机号
        String PHONE_NUMBER = "phone_number";

        //手机验证码
        String AUTH_CODE = "auth_code";
    }

    public interface PermissionType {
        int BASE = 0x1000;

        int RECORD_PERMISSION = BASE + 1;
    }

    public interface RegularModel {
        /**
         * 11位手机号码
         */
        String PHONE_PATTERN = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
    }

    public interface ShareType {
        int BASE = 0x1000;
        int WECHAT_FRIEND = BASE + 1;

        int WECHAT_MOMENTS = BASE + 2;
    }

}
