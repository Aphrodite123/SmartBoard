package com.aphrodite.smartboard.config;

/**
 * Created by Aphrodite on 2018/7/26.
 */
public interface IntentAction {

    String ACTION_SUFFIX = "com.aphrodite.smartboard.view.";

    interface MainAction {
        String ACTION = ACTION_SUFFIX + "MAIN";
    }

    interface LoginAction {
        String ACTION = ACTION_SUFFIX + "LOGIN";
    }

    interface SearchRegionAction {
        String ACTION = ACTION_SUFFIX + "SEARCHREGION";
    }

    interface RegisterAction {
        String ACTION = ACTION_SUFFIX + "REGISTER";
    }

    interface CanvasAction {
        String ACTION = ACTION_SUFFIX + "CANVAS";
    }

    interface SettingAction {
        String ACTION = ACTION_SUFFIX + "SETTING";
    }

}
