package com.aphrodite.smartboard.model.ffmpeg;

import android.os.Handler;

import com.aphrodite.smartboard.utils.LogUtil;

/**
 * Handler消息处理器
 * Created by frank on 2019/11/11.
 */
public class FFmpegHandler {

    private final static String TAG = FFmpegHandler.class.getSimpleName();

    public final static int MSG_BEGIN = 9012;

    public final static int MSG_FINISH = 1112;

    public final static int MSG_CONTINUE = 2012;

    public final static int MSG_TOAST = 4562;

    private Handler mHandler;

    private boolean isContinue = false;

    public FFmpegHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void isContinue(boolean isContinue) {
        this.isContinue = isContinue;
    }

    /**
     * 执行ffmpeg命令行
     *
     * @param commandLine commandLine
     */
    public void executeFFmpegCmd(final String[] commandLine) {
        if (commandLine == null) {
            return;
        }
        FFmpegTask.execute(commandLine, new OnHandleListener() {
            @Override
            public void onBegin() {
                LogUtil.i(TAG, "handle onBegin...");
                mHandler.obtainMessage(MSG_BEGIN).sendToTarget();
            }

            @Override
            public void onEnd(int resultCode, String resultMsg) {
                LogUtil.i(TAG, "handle onEnd...");
                if (isContinue) {
                    mHandler.obtainMessage(MSG_CONTINUE).sendToTarget();
                } else {
                    mHandler.obtainMessage(MSG_FINISH).sendToTarget();
                }
            }
        });
    }

}
