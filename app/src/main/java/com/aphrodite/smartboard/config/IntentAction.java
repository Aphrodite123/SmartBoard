package com.aphrodite.smartboard.config;

/**
 * Created by Aphrodite on 2018/7/26.
 */
public interface IntentAction {

    String ACTION_SUFFIX = "com.aphrodite.smartboard.view.";

    interface LoginAction {
        String ACTION = ACTION_SUFFIX + "LOGIN";
    }

    interface SearchRegionAction {
        String ACTION = ACTION_SUFFIX + "SEARCHREGION";
    }

}
