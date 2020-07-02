package com.aphrodite.smartboard.model.handler;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.AsyncTask;

import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.utils.LogUtils;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;

public class UsbHandler {
    private static UsbDeviceReceiver mUsbDeviceReceiver;
    private static UsbPermissionReceiver mUsbPermissionReceiver;

    public static final String ACTION_USB_PERMISSION = "com.aphrodite.smartboard.USB_PERMISSION";

    public static final String ACTION_USB_SINGLE_PERMISSION = "com.aphrodite.smartboard.USB_SINGLE_PERMISSION";

    public static final String ACTION_USB_COMPLETE = "com.aphrodite.smartboard.USB_COMPLETE";

    private static final String TAG = "HidDevice";

    private static final String INTERNAL_NAME_DOCK = "PosMate";

    private static final String INTERNAL_NAME_FRAME = "PMFrame";

    public static final int DEV_UNKOWN = -1;

    public static final int DEV_DOCK = 0;

    public static final int DEV_TRAY = 1;

    private static final int USB_REQUEST_TYPE_INTERFACE = 1;

    private static final int CMD_TIMEOUT_MS = 8192;

    private int mDevType;

    public UsbDevice mDevice;

    UsbDeviceConnection mDevConn;

    UsbInterface mInterface;

    UsbRequest mRequest;

    UsbInterface mInterfaceEvent;

    UsbRequest mRequestEvent;

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public UsbHandler(int type, UsbDevice device, UsbDeviceConnection conn, UsbInterface interFace, UsbRequest request, UsbInterface interFaceEvent, UsbRequest requestIn) {
        this.mDevice = device;
        this.mDevType = type;
        this.mDevConn = conn;
        this.mInterface = interFace;
        this.mRequest = request;
        this.mInterfaceEvent = interFace;
        this.mRequestEvent = requestIn;
    }

    public static void registerUsbDeviceReceiver(Context context) {
        if (null == mUsbDeviceReceiver) {
            mUsbDeviceReceiver = new UsbDeviceReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        context.registerReceiver(mUsbDeviceReceiver, filter);
    }

    public static void registerUsbPermissionReceiver(Context context) {
        if (null == mUsbPermissionReceiver) {
            mUsbPermissionReceiver = new UsbPermissionReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(mUsbPermissionReceiver, filter);
    }

    public static void unregisterReceiver(Context context) {
        if (null != mUsbDeviceReceiver) {
            context.unregisterReceiver(mUsbDeviceReceiver);
        }

        if (null != mUsbPermissionReceiver) {
            context.unregisterReceiver(mUsbPermissionReceiver);
        }
    }

    public int getDevType() {
        return this.mDevType;
    }

    public static void detectUsb(Context context, UsbHandler handler1, UsbHandler handler2) {
        boolean isfind = findDevice(context, AppConfig.DeviceCmds.USB_VID, AppConfig.DeviceCmds.USB_PID, 0, 0, 0, 1, handler1, handler2);
        if (!isfind) {
            LogUtils.i("HidDevice", "Device not found");
        }
    }

    private static boolean findDevice(Context context, int vendorId, int productId, int deviceClass, int deviceProtocol, int deviceSubclass, int interfaceCount, UsbHandler handler1, UsbHandler handler2) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        int devVendorId = 0;
        boolean found = false;
        for (UsbDevice device : deviceList.values()) {
            if (device.getProductId() == productId)
                devVendorId = device.getVendorId();
            int devProductID = device.getProductId();
            int devClass = device.getDeviceClass();
            int devProtocol = device.getDeviceProtocol();
            int devSubClass = device.getDeviceSubclass();
            int devIFCount = device.getInterfaceCount();
            if (devVendorId == vendorId && devProductID == productId && devClass == deviceClass && devSubClass == deviceSubclass && devProtocol == deviceProtocol && devIFCount == interfaceCount) {
                found = true;
                if ((handler1 != null && device.equals(handler1.mDevice)) || (handler2 != null && device.equals(handler2.mDevice)))
                    continue;
                if (usbManager.hasPermission(device)) {
                    open(context, device);
                } else {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, permissionIntent);
                }
                return found;
            }
        }
        Intent intent = new Intent(ACTION_USB_COMPLETE);
        context.sendBroadcast(intent);
        return found;
    }

    public static boolean requestDevicePermission(Context context, UsbDevice device, int vendorId, int productId, int deviceClass, int deviceProtocol, int deviceSubclass, int interfaceCount) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        int devVendorId = device.getVendorId();
        int devProductID = device.getProductId();
        int devClass = device.getDeviceClass();
        int devProtocol = device.getDeviceProtocol();
        int devSubClass = device.getDeviceSubclass();
        int devIFCount = device.getInterfaceCount();
        if (devVendorId == vendorId && devProductID == productId && devClass == deviceClass && devSubClass == deviceSubclass && devProtocol == deviceProtocol && devIFCount == interfaceCount) {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent("com.cynoware.posmate.USB_SINGLE_PERMISSION"), 0);
            usbManager.requestPermission(device, permissionIntent);
            return true;
        }
        return false;
    }

    private static String getDeviceName(UsbDeviceConnection conn) {
        String str = "";
        byte[] buf = new byte[32];
        buf[0] = 2;
        buf[1] = 9;
        int res = setFeature(conn, buf);
        if (res < 0)
            return str;
        res = getFeature(conn, (byte) 2, buf);
        if (res < 0)
            return str;
        try {
            int i;
            for (i = 8; i < buf.length && buf[i] != 0; i++) ;
            str = new String(buf, 8, i - 8, "UTF-8");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
        }
        return str;
    }

    @SuppressLint({"NewApi"})
    public static void open(Context context, UsbDevice usbDevice) {
        if (null == usbDevice) {
            return;
        }

        try {
            UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection conn = usbManager.openDevice(usbDevice);
            if (conn == null)
                return;

            UsbInterface usbInterface = usbDevice.getInterface(0);
            if (usbInterface == null)
                return;

            byte[] host = {0x04, 0x00, 0x00, 0x00, 0x00, 0x00};
            setFeature(conn, getOutEndpoint(usbInterface), host);

            if (!conn.claimInterface(usbInterface, true)) {
                conn.close();
                return;
            }
            UsbInterface usbInterfaceEvent = usbDevice.getInterface(1);
            if (usbInterfaceEvent == null)
                return;
            if (!conn.claimInterface(usbInterfaceEvent, true)) {
                conn.close();
                return ;
            }
            UsbRequest request = new UsbRequest();
            UsbEndpoint endPoint = usbInterface.getEndpoint(0);
            request.initialize(conn, endPoint);
            UsbRequest requestEvent = new UsbRequest();
            UsbEndpoint endPointEvent = usbInterface.getEndpoint(0);
            requestEvent.initialize(conn, endPointEvent);
            String name = getDeviceName(conn);
            int type = -1;
            if (name.equals("PosMate")) {
                type = 0;
            } else if (name.equals("PMFrame")) {
                type = 1;
            } else {
                conn.close();
                return null;
            }
            return new UsbHandler(type, usbDevice, conn, usbInterface, request, usbInterfaceEvent, requestEvent);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取输入端口
     */
    private static UsbEndpoint getInEndpoint(UsbInterface usbInterface) {
        if (null == usbInterface) {
            return null;
        }

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            if (null == endpoint) {
                continue;
            }
            if (129 == endpoint.getAddress()) {
                return endpoint;
            }
        }
        return null;
    }

    /**
     * 获取输出端口
     */
    private static UsbEndpoint getOutEndpoint(UsbInterface usbInterface) {
        if (null == usbInterface) {
            return null;
        }

        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            if (null == endpoint) {
                continue;
            }
            if (1 == endpoint.getAddress()) {
                return endpoint;
            }
        }
        return null;
    }

    static int getFeature(UsbDeviceConnection conn, byte reportID, byte[] buf) {
        buf[0] = reportID;
        int res = conn.controlTransfer(161, 1, 770, 0, buf, buf.length, 8192);
        return res;
    }

    private static int setFeature(UsbDeviceConnection connection, byte[] buf) {
        int res = connection.controlTransfer(33, 9, 770, 0, buf, buf.length, 8192);
        return res;
    }

    public int setFeature(byte[] buf) {
        return setFeature(this.mDevConn, buf);
    }

    private static int setFeature(UsbDeviceConnection connection, UsbEndpoint endpoint, byte[] buf) {
        int res = 0;
        //检测buf长度
        if (buf.length > 16 * 1024) {
            int pack = buf.length / (16 * 1024);
            if (pack % (16 * 1024) > 0) {
                pack++;
            }

            for (int i = 0; i < pack; i++) {
                byte[] newBuffer = Arrays.copyOfRange(buf, 16 * 1024 * i, 16 * 1024 * (i + 1));
                res = connection.bulkTransfer(endpoint, newBuffer, newBuffer.length, 0);
            }
        } else {
            res = connection.bulkTransfer(endpoint, buf, buf.length, 0);
        }
        return res;
    }

    public int getFeature(byte reportID, byte[] buf) {
        return getFeature(this.mDevConn, reportID, buf);
    }

    private static int getFeature(UsbDeviceConnection connection, UsbEndpoint endpoint, byte[] buf) {
        int res = connection.bulkTransfer(endpoint, buf, buf.length, 0);
        return res;
    }

    private String syncOffLineNotes(UsbDeviceConnection conn, UsbEndpoint outUsbEndpoint, UsbEndpoint inUsbEndpoint, byte[] buf) {
        String str = "";

        int res = setFeature(conn, outUsbEndpoint, buf);
        if (res < 0) {
            return str;
        }

        res = getFeature(conn, inUsbEndpoint, buf);
        if (res < 0)
            return str;
        try {
            str = new String(buf, "UTF-8");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
        }
        return str;
    }

    public void close() {
        if (this.mDevConn != null) {
            if (this.mRequest != null)
                this.mRequest.cancel();
            if (this.mRequestEvent != null)
                this.mRequestEvent.cancel();
            if (this.mInterface != null)
                this.mDevConn.releaseInterface(this.mInterface);
            if (this.mInterfaceEvent != null)
                this.mDevConn.releaseInterface(this.mInterfaceEvent);
            this.mDevConn.close();
            this.mDevConn = null;
        }
    }

    private static class UsbPermissionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            open(context, usbDevice);
                        }
                    }
                    break;
            }
        }
    }

    private static class UsbDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d("onReceive");
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    detectUsb(context, null, null);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    break;
                default:
                    break;
            }
        }
    }

    private class SyncOffLineDataTask extends AsyncTask<Object, Object, Object> {
        private UsbDeviceConnection conn;
        private UsbEndpoint outUsbEndpoint;
        private UsbEndpoint inUsbEndpoint;
        private byte[] buf;

        public SyncOffLineDataTask(UsbDeviceConnection conn, UsbEndpoint outUsbEndpoint, UsbEndpoint inUsbEndpoint, byte[] buf) {
            this.conn = conn;
            this.outUsbEndpoint = outUsbEndpoint;
            this.inUsbEndpoint = inUsbEndpoint;
            this.buf = buf;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            return syncOffLineNotes(conn, outUsbEndpoint, inUsbEndpoint, buf);
            ;
        }
    }

}
