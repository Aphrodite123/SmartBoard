package com.aphrodite.smartboard.view.activity.base;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.os.IBinder;

import androidx.lifecycle.Observer;

import com.apeman.sdk.bean.UsbBoardInfo;
import com.apeman.sdk.service.ConnectStatus;
import com.apeman.sdk.service.UsbPenService;
import com.apeman.sdk.service.command.Command;
import com.apeman.sdk.service.usb.USBPenServiceImpl;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.activity.DeviceOnLineActivity;

import java.util.List;

/**
 * Created by Aphrodite on 20-6-19
 * 设备Base Activity
 */
public abstract class BaseDeviceActivity extends BaseActivity implements ServiceConnection {
    protected UsbPenService<UsbBoardInfo, UsbDevice> mUsbPenService;

    protected abstract Observer<ConnectStatus> getConnObserver();

    protected abstract Observer<Command> getCmdObserver();

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        USBPenServiceImpl.ConnBinder connBinder = (USBPenServiceImpl.ConnBinder) service;
        mUsbPenService = connBinder.getService();
        mUsbPenService.getConnLiveData().observe(this, getConnObserver());
        mUsbPenService.getCommandLiveData().observe(this, getCmdObserver());

        List<UsbDevice> usbDevices = mUsbPenService.listConnectedUsbDevice(this);
        if (ObjectUtils.isEmpty(usbDevices)) {
            return;
        }

        for (UsbDevice device : usbDevices) {
            if (null == device) {
                continue;
            }

            mUsbPenService.connectSmartBoard(device);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        ToastUtils.showMessage(R.string.device_disconnected);
        if (this instanceof DeviceOnLineActivity) {
            finish();
        }
    }
}
