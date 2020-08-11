package com.aphrodite.smartboard.model.bean;

/**
 * ShareContentType
 * Support Share Content Types.
 *
 * @author baishixian
 * @date 2018/3/29 11:41
 */
public @interface ShareContentType {
    /**
     * Share Text
     */
    final String TEXT = "text/plain";

    /**
     * Share Image
     */
    final String IMAGE = "image/*";

    /**
     * Share Audio
     */
    final String AUDIO = "audio/*";

    /**
     * Share Video
     */
    final String VIDEO = "video/*";

    /**
     * Share File
     */
    final String FILE = "*/*";
}
