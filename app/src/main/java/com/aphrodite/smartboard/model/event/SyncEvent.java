package com.aphrodite.smartboard.model.event;

/**
 * Created by Aphrodite on 2019/6/11.
 */
public enum SyncEvent {
    //刷新首页作品列表
    REFRESH_WORK_LIST,
    //同步离线笔记
    SYNC_OFFLINE_DATA,
    //结束同步离线笔记
    END_SYNC_OFFLINE;
}