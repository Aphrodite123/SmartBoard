package com.aphrodite.smartboard.model.ffmpeg;

/**
 * 流程执行监听器
 * Created by frank on 2019/11/11.
 */
public interface OnHandleListener {
    void onBegin();

    void onEnd(int resultCode, String resultMsg);
}
