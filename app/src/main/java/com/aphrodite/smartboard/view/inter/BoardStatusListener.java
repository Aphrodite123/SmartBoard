package com.aphrodite.smartboard.view.inter;

/**
 * Created by Aphrodite on 20-5-8
 * 用于监听手写板显示态or编辑态
 */
public interface BoardStatusListener {
    void onPlay();

    void onEditor();
}
