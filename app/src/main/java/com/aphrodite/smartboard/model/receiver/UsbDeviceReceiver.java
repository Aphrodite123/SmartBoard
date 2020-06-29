package com.aphrodite.smartboard.model.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

import com.aphrodite.smartboard.model.handler.UsbHandler;
import com.aphrodite.smartboard.utils.LogUtils;

/**
 * Created by Aphrodite on 20-6-29
 */
public class UsbDeviceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.d("onReceive");
        switch (intent.getAction()) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                UsbHandler.detectUsb(context, null, null);
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                break;
            default:
                break;
        }
    }
}
