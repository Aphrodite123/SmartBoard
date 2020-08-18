package com.aphrodite.smartboard.config;

import com.aphrodite.framework.config.base.BaseConfig;
import com.aphrodite.smartboard.application.MainApplication;

import java.io.File;

/**
 * Created by Aphrodite on 2019/5/28.
 */
public class AppConfig extends BaseConfig {
    /**
     * 智能手写板数据文件存储路径，默认指定到data
     */
    public static final String DATA_PATH = MainApplication.getApplication().getExternalFilesDir("data").getAbsolutePath() + File.separator;

    /**
     * assets 数据路径
     */
    public static final String ASSETS_FILE_PATH = "data";

    //在线数据保存路径
    public static final String PATH_ONLINE_DATA = "online/";

    public static final String DATA_FILE_NAME = "data.cw";

    public static final String AUDIO_FILE_NAME = "audio.mp3";

    //封面图片名称
    public static final String COVER_IMAGE_NAME = "cover_image.jpg";

    //微信APP ID
    public static final String WX_APP_ID = "wxec7f76bcab4613b2";

    //微信APP KEY
    public static final String WX_APP_KEY = "d6448bffb5bb516121efc9573a3d035c";

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

        byte CMD_06 = BASE + 6;

        byte CMD_07 = BASE + 7;

        byte CMD_08 = BASE + 8;
    }

    public interface UsbHandler {
        byte BASE = 0x00;

        byte WHAT_01 = BASE + 1;

        byte WHAT_02 = BASE + 2;

        byte WHAT_03 = BASE + 3;

        byte WHAT_04 = BASE + 4;

        byte WHAT_05 = BASE + 5;

        byte WHAT_06 = BASE + 6;

        byte WHAT_07 = BASE + 7;
    }

    /**
     * 错误码解释
     */
    public interface ErrorId {
        //指令ID非法，HOST发送的cmd_id不在预设指令列表中
        byte BASE = 0x00;

        //校验和失败
        byte ERROR_01 = BASE + 1;

        //指令Payload长度非法，长度与协议定义不一致
        byte ERROR_02 = BASE + 2;

        //系统忙状态，当前设备状态无法执行目标指令
        byte ERROR_03 = BASE + 3;

        //笔记索引值错误
        byte ERROR_04 = BASE + 4;

        //请求坐标索引值错误
        byte ERROR_05 = BASE + 5;
    }

}
