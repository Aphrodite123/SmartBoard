package com.aphrodite.smartboard.model.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;

/**
 * Created by Aphrodite on 20-6-29
 */
public class UsbDeviceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                break;
            case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                break;
            default:
                break;
        }
    }
}
