package com.aphrodite.smartboard.config;

import com.aphrodite.framework.config.base.BaseConfig;
import com.aphrodite.smartboard.application.MainApplication;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Created by Aphrodite on 2019/5/28.
 */
public class AppConfig extends BaseConfig {
    public static final String ROOT_PATH = SDCARD_PATH + "com.aphrodite.smartboard/";

    /**
     * 智能手写板数据文件存储路径，默认指定到data
     */
    public static final String DATA_PATH = MainApplication.getApplication().getExternalFilesDir("data").getAbsolutePath() + File.separator;

    /**
     * User缓存目录
     */
    public static final String CACHE_PATH = MainApplication.getApplication().getCacheDir().getAbsolutePath();

    /**
     * 在线模式板子数据保存路径
     */
    public static final String BOARD_ONLINE_PATH = CACHE_PATH + File.separator + "Notes/";

    /**
     * 正则*从文件名中匹配出timestamp
     */
    public static final Pattern BOARD_ONLINE_FILE_PATTERN = Pattern.compile("Note-(\\d{13})\\.note");

    /**
     * 智能手写板ffmpeg文件存储路径，默认指定到ffmpeg
     */
    public static final String FFMPEG_PATH = MainApplication.getApplication().getExternalFilesDir("ffmpeg").getAbsolutePath() + File.separator;

    /**
     * assets 数据路径
     */
    public static final String ASSETS_FILE_PATH = "data";

    public static final String DATA_FILE_NAME = "data.cw";

    public static final String AUDIO_FILE_NAME = "audio.mp3";

    /**
     * 封面图片名称
     */
    public static final String COVER_IMAGE_NAME = "cover_image.jpg";

    /**
     * 微信APP ID
     */
    public static final String WX_APP_ID = "wx87889e866c6936fe";

    public interface SharePreferenceKey {
        //用户手机号
        String PHONE_NUMBER = "phone_number";

        //手机验证码
        String AUTH_CODE = "auth_code";

        //首次进入应用将Assets中数据文件拷贝到指定SDcard路径下
        String COPY_ASSETS_DATA_TO_SDCARD = "copy_assets_data_to_sdcard";
    }

    public interface PermissionType {
        int BASE = 0x1000;

        int STORAGE_PERMISSION = BASE + 1;

        int RECORD_PERMISSION = BASE + 2;
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

    public interface FileType {
        String MP3 = ".mp3";
        String CW = ".cw";
    }

    public interface DeviceCmds {
        int USB_PID = 0x8670;

        int USB_VID = 0x0805;
    }

    public interface ByteCommand {
        byte BASE = 0x00;

        byte CMD_01 = BASE + 1;

        byte CMD_02 = BASE + 2;

        byte CMD_03 = BASE + 3;

        byte CMD_04 = BASE + 4;

        byte CMD_05 = BASE + 5;
    }

    public interface UsbHandler {
        byte BASE = 0x00;

        byte WHAT_01 = BASE + 1;

        byte WHAT_02 = BASE + 2;

        byte WHAT_03 = BASE + 3;

        byte WHAT_04 = BASE + 4;

        byte WHAT_05 = BASE + 5;

        byte WHAT_06 = BASE + 6;
    }

}
