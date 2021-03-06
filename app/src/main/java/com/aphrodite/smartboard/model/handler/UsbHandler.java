package com.aphrodite.smartboard.model.handler;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.apeman.sdk.bean.BoardType;
import com.apeman.sdk.bean.DevicePoint;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.event.SyncEvent;
import com.aphrodite.smartboard.utils.ByteUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.LogUtils;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public class UsbHandler {
    private static UsbHandler mUsbHandler = null;
    public static final String ACTION_USB_PERMISSION = "com.aphrodite.smartboard.USB_PERMISSION";
    public static final String ACTION_USB_SINGLE_PERMISSION = "com.aphrodite.smartboard.USB_SINGLE_PERMISSION";
    public static final String ACTION_USB_COMPLETE = "com.aphrodite.smartboard.USB_COMPLETE";

    private Context mContext;
    private UsbDeviceReceiver mUsbDeviceReceiver;
    private UsbPermissionReceiver mUsbPermissionReceiver;
    private UsbDevice mCurUsbDevice;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mOutUsbEndpoint;
    private UsbEndpoint mInUsbEndpoint;
    private DataHandler mHandler;
    private ExecutorService mSingleThreadExecutor;

    public static UsbHandler getInstance() {
        if (null == mUsbHandler) {
            synchronized (UsbHandler.class) {
                if (null == mUsbHandler) {
                    mUsbHandler = new UsbHandler(MainApplication.getApplication());
                }
            }
        }
        return mUsbHandler;
    }

    private UsbHandler(Context context) {
        this.mContext = context;
        this.mHandler = new DataHandler(context);
        this.mSingleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    public void registerUsbDeviceReceiver() {
        if (null == mUsbDeviceReceiver) {
            mUsbDeviceReceiver = new UsbDeviceReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        mContext.registerReceiver(mUsbDeviceReceiver, filter);
    }

    public void registerUsbPermissionReceiver() {
        if (null == mUsbPermissionReceiver) {
            mUsbPermissionReceiver = new UsbPermissionReceiver();
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        mContext.registerReceiver(mUsbPermissionReceiver, filter);
    }

    public void unregisterReceiver() {
        if (null != mUsbDeviceReceiver) {
            mContext.unregisterReceiver(mUsbDeviceReceiver);
        }

        if (null != mUsbPermissionReceiver) {
            mContext.unregisterReceiver(mUsbPermissionReceiver);
        }
    }

    public void detectUsb(UsbHandler handler1, UsbHandler handler2) {
        boolean isfind = findDevice(AppConfig.DeviceCmds.USB_VID, AppConfig.DeviceCmds.USB_PID, 0, 0, 0, 1, handler1, handler2);
        if (!isfind) {
            LogUtils.i("Device not found.");
        }
    }

    private boolean findDevice(int vendorId, int productId, int deviceClass, int deviceProtocol, int deviceSubclass, int interfaceCount, UsbHandler handler1, UsbHandler handler2) {
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
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
                if (usbManager.hasPermission(device)) {
                    this.mCurUsbDevice = device;
                    open();
                } else {
                    PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, permissionIntent);
                }
                return found;
            }
        }
        Intent intent = new Intent(ACTION_USB_COMPLETE);
        mContext.sendBroadcast(intent);
        return found;
    }

    @SuppressLint({"NewApi"})
    public void open() {
        if (null == mCurUsbDevice) {
            return;
        }

        try {
            UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
            mUsbDeviceConnection = usbManager.openDevice(mCurUsbDevice);
            if (mUsbDeviceConnection == null)
                return;

            mUsbInterface = mCurUsbDevice.getInterface(0);
            if (mUsbInterface == null)
                return;

            mOutUsbEndpoint = getOutEndpoint(mUsbInterface);
            mInUsbEndpoint = getInEndpoint(mUsbInterface);

            if (!mUsbDeviceConnection.claimInterface(mUsbInterface, true)) {
                mUsbDeviceConnection.close();
                return;
            }
//            setDeviceStatus(AppConfig.ByteCommand.CMD_02);
            queryOffLineInfo();
            //通知Activity显示loading
            EventBus.getDefault().post(SyncEvent.SYNC_OFFLINE_DATA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取输入端口
     */
    private UsbEndpoint getInEndpoint(UsbInterface usbInterface) {
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
    private UsbEndpoint getOutEndpoint(UsbInterface usbInterface) {
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

    private int setFeature(byte[] buf) {
        int res = 0;
        if (null == mUsbDeviceConnection || null == mOutUsbEndpoint) {
            return res;
        }
        //检测buf长度
        if (buf.length > 16 * 1024) {
            int pack = buf.length / (16 * 1024);
            if (pack % (16 * 1024) > 0) {
                pack++;
            }

            for (int i = 0; i < pack; i++) {
                byte[] newBuffer = Arrays.copyOfRange(buf, 16 * 1024 * i, 16 * 1024 * (i + 1));
                res = mUsbDeviceConnection.bulkTransfer(mOutUsbEndpoint, newBuffer, newBuffer.length, 0);
            }
        } else {
            res = mUsbDeviceConnection.bulkTransfer(mOutUsbEndpoint, buf, buf.length, 8192);
        }
        return res;
    }

    private int getFeature(byte[] buf) {
        if (null == mUsbDeviceConnection || null == mInUsbEndpoint) {
            return 0;
        }
        int res = mUsbDeviceConnection.bulkTransfer(mInUsbEndpoint, buf, buf.length, 8192);
        return res;
    }

    /**
     * 3.2	查询当前状态
     */
    public void queryDeviceStatus() {
        byte[] pageTrans = {AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.CMD_01, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int res = setFeature(pageTrans);
                if (res < 0) {
                    return;
                }

                int bufferLength = 8;
                ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
                buffer.order(ByteOrder.nativeOrder());
                res = getFeature(buffer.array());
                if (res < 0)
                    return;
                if (null != mHandler) {
                    Message message = new Message();
                    message.what = AppConfig.UsbHandler.WHAT_05;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_05), buffer.array());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        };
        if (null != mSingleThreadExecutor) {
            mSingleThreadExecutor.execute(runnable);
        }
    }

    /**
     * 3.3	设置产品状态
     *
     * @param targetStatus Payload[0]说明：Target status
     *                     0x00：离线模式（STATUS_OFFLINE）
     *                     0x01：在线模式（STATUS_ONLINE）
     *                     0x02：SYNC模式（STATUS_SYNC）
     *                     0x03：固件升级模式（STATUS_DFU）
     */
    public void setDeviceStatus(byte targetStatus) {
        byte[] pageTrans = {AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.CMD_02, AppConfig.ByteCommand.CMD_01, targetStatus, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int res = setFeature(pageTrans);
                if (res < 0) {
                    return;
                }

                int bufferLength = 8;
                ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
                buffer.order(ByteOrder.nativeOrder());
                res = getFeature(buffer.array());
                if (res < 0)
                    return;
                if (null != mHandler) {
                    Message message = new Message();
                    message.what = AppConfig.UsbHandler.WHAT_06;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_06), buffer.array());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        };
        if (null != mSingleThreadExecutor) {
            mSingleThreadExecutor.execute(runnable);
        }
    }

    /**
     * 5.3.2	离线存储信息查询
     */
    public void queryOffLineInfo() {
        byte[] buf = {AppConfig.ByteCommand.CMD_04, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE, AppConfig.ByteCommand.BASE};
        Gson gson = new Gson();
        LogUtils.d("Enter to queryOffLineInfo: " + gson.toJson(buf));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int res = setFeature(buf);
                if (res < 0) {
                    return;
                }

                int bufferLength = 8;
                ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
                buffer.order(ByteOrder.nativeOrder());
                res = getFeature(buffer.array());
                if (res < 0)
                    return;

                if (null != mHandler) {
                    Message message = new Message();
                    message.what = AppConfig.UsbHandler.WHAT_01;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_01), buffer.array());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        };
        if (null != mSingleThreadExecutor) {
            mSingleThreadExecutor.execute(runnable);
        }
    }

    /**
     * 5.3.3	页传输
     *
     * @param payload
     */
    public void queryPages(byte payload) {
        byte[] pageTrans = {AppConfig.ByteCommand.CMD_04, AppConfig.ByteCommand.CMD_01, AppConfig.ByteCommand.CMD_01, payload, payload, payload};
        Gson gson = new Gson();
        LogUtils.d("Enter to queryPages: " + gson.toJson(pageTrans));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int res = setFeature(pageTrans);
                if (res < 0) {
                    return;
                }

                int bufferLength = 8;
                ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
                buffer.order(ByteOrder.nativeOrder());
                res = getFeature(buffer.array());
                if (res < 0)
                    return;
                if (null != mHandler) {
                    Message message = new Message();
                    message.what = AppConfig.UsbHandler.WHAT_02;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_02), buffer.array());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        };
        if (null != mSingleThreadExecutor) {
            mSingleThreadExecutor.execute(runnable);
        }
    }

    /**
     * 5.3.4	坐标传输
     *
     * @param payload
     */
    public void queryCoordinates(byte[] payload) {
        byte[] pageTrans = {AppConfig.ByteCommand.CMD_04, AppConfig.ByteCommand.CMD_02, AppConfig.ByteCommand.CMD_02, payload[4], payload[5], (byte) (payload[3] + payload[4]), (byte) (payload[3] + payload[5])};
        Gson gson = new Gson();
        LogUtils.d("Enter to queryCoordinates: " + gson.toJson(pageTrans));
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int res = setFeature(pageTrans);
                if (res < 0) {
                    return;
                }

                int bufferLength = 16;
                ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
                buffer.order(ByteOrder.nativeOrder());
                res = getFeature(buffer.array());
                if (res < 0)
                    return;
                if (null != mHandler) {
                    synchronized (mHandler) {
                        Message message = new Message();
                        message.what = AppConfig.UsbHandler.WHAT_03;
                        Bundle bundle = new Bundle();
                        bundle.putByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_03), buffer.array());
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
            }
        };
        if (null != mSingleThreadExecutor) {
            mSingleThreadExecutor.execute(runnable);
        }
    }

    /**
     * 5.3.5	页删除
     *
     * @param payload
     */
    public void deletePage(byte payload) {
        byte[] pageTrans = {AppConfig.ByteCommand.CMD_04, AppConfig.ByteCommand.CMD_03, AppConfig.ByteCommand.CMD_01, payload, payload, payload};
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int res = setFeature(pageTrans);
                if (res < 0) {
                    return;
                }

                int bufferLength = 8;
                ByteBuffer buffer = ByteBuffer.allocate(bufferLength);
                buffer.order(ByteOrder.nativeOrder());
                res = getFeature(buffer.array());
                if (res < 0)
                    return;
                if (null != mHandler) {
                    Message message = new Message();
                    message.what = AppConfig.UsbHandler.WHAT_04;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_04), buffer.array());
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        };
        if (null != mSingleThreadExecutor) {
            mSingleThreadExecutor.execute(runnable);
        }
    }

    public void close() {
        unregisterReceiver();
        mUsbHandler = null;
        mContext = null;
        mUsbDeviceReceiver = null;
        mUsbPermissionReceiver = null;
        mCurUsbDevice = null;
        if (null != mUsbDeviceConnection) {
            mUsbDeviceConnection.releaseInterface(mUsbInterface);
            mUsbDeviceConnection.close();
        }
        mUsbDeviceConnection = null;
        mUsbInterface = null;
        mOutUsbEndpoint = null;
        mInUsbEndpoint = null;
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (null != mSingleThreadExecutor) {
            mSingleThreadExecutor.shutdownNow();
            mSingleThreadExecutor = null;
        }
    }

    private void handleError(byte code) {
        switch (code) {
            case AppConfig.ErrorId.BASE:
                LogUtils.d("Request failed." + mContext.getString(R.string.illegal_instruction));
                break;
            case AppConfig.ErrorId.ERROR_01:
                LogUtils.d("Request failed." + mContext.getString(R.string.check_code_failed));
                break;
            case AppConfig.ErrorId.ERROR_02:
                LogUtils.d("Request failed." + mContext.getString(R.string.length_instruction));
                break;
            case AppConfig.ErrorId.ERROR_03:
                LogUtils.d("Request failed." + mContext.getString(R.string.device_busy_status));
                break;
            case AppConfig.ErrorId.ERROR_04:
                LogUtils.d("Request failed." + mContext.getString(R.string.index_instruction));
                break;
            case AppConfig.ErrorId.ERROR_05:
                LogUtils.d("Request failed." + mContext.getString(R.string.coordinate_index_instruction));
                break;
            default:
                break;
        }
    }

    private class UsbPermissionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    synchronized (this) {
                        UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        mCurUsbDevice = usbDevice;
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            open();
                        }
                    }
                    break;
            }
        }
    }

    private class UsbDeviceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    detectUsb(null, null);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                    break;
                default:
                    break;
            }
        }
    }

    private class DataHandler extends Handler {
        private WeakReference mReference;
        private int mPageCount;
        private int mCurPageIndex;

        private long mTimestamp;
        //按照设备比例缩放后的画布宽度
        private int mCanvasWidth;
        //按照设备比例缩放后的画布高度
        private int mCanvasHeight;
        //将设备坐标点转换为画布坐标点的缩放比例
        private Double mXScale;
        private Double mYScale;

        private List<ScreenRecordEntity> mData = new ArrayList<>();
        private Paint mPaint;
        private int mStrokeWidth = 10;
        private int mDrawColor = Color.BLACK;

        private int mCoordinateCount;
        private List<DevicePoint> mDevicePoints;

        public DataHandler(Context context) {
            mReference = new WeakReference<>(context);
            mDevicePoints = new ArrayList<>();
            this.queryDeviceInfo();
            this.initPaint();
        }

        private void queryDeviceInfo() {
            BoardType boardType = BoardType.NoteMaker;
            float deviceScale = (float) (boardType.getMaxX() / boardType.getMaxY());
            int viewWidth = UIUtils.getDisplayWidthPixels(mContext);
            int viewHeight = UIUtils.getDisplayHeightPixels(mContext);
            float screenScale = (float) (viewWidth) / (float) (viewHeight);
            if (screenScale > deviceScale) {
                //设备更宽，以View的高为基准进行缩放
                mCanvasHeight = viewHeight;
                mCanvasWidth = (int) (viewHeight * deviceScale);
            } else {
                //以View的宽为基准进行缩放
                mCanvasWidth = viewWidth;
                mCanvasHeight = (int) (viewWidth / deviceScale);
            }

            mXScale = mCanvasWidth / boardType.getMaxX();
            mYScale = mCanvasHeight / boardType.getMaxY();
        }

        private void initPaint() {
            mPaint = new Paint();
            mPaint.setColor(mDrawColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mStrokeWidth);
            mPaint.setAntiAlias(true);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        private void saveData() {
            ScreenRecordEntity screenRecordEntity = new ScreenRecordEntity();
            screenRecordEntity.setType("data/0");
            if (null != mData) {
                mData.clear();
            }
            mData.add(screenRecordEntity);

            mTimestamp = System.currentTimeMillis();
            CWFileUtils.write(mData, AppConfig.DATA_PATH + mTimestamp + File.separator, mCanvasWidth, mCanvasHeight, mTimestamp);
        }

        private void handleCoordinate(byte[] buffer) {
            if (ObjectUtils.isEmpty(buffer)) {
                return;
            }

            int pressure = ByteUtils.byteToInteger(new byte[]{buffer[10], buffer[9]});
            if (pressure <= 0) {
                int red = (mPaint.getColor() & 0xff0000) >> 16;
                int green = (mPaint.getColor() & 0x00ff00) >> 8;
                int blue = (mPaint.getColor() & 0x0000ff);
                CWFileUtils.writeLine(mDevicePoints, (int) mPaint.getStrokeWidth(), red + "," + green + "," + blue + ",1");

                //保存完后将mDevicePoints置为空
                mDevicePoints.clear();
            } else {
                DevicePoint devicePoint = new DevicePoint();
                devicePoint.setX(ByteUtils.byteToInteger(new byte[]{buffer[6], buffer[5]}));
                devicePoint.setY(ByteUtils.byteToInteger(new byte[]{buffer[8], buffer[7]}));
                devicePoint.setPressure(pressure);
                mDevicePoints.add(devicePoint);
            }
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            Gson gson = new Gson();
            switch (msg.what) {
                //离线存储信息查询
                case AppConfig.UsbHandler.WHAT_01:
                    if (null != bundle) {
                        byte[] buffer = bundle.getByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_01));
                        LogUtils.d("Enter to handleMessage: " + gson.toJson(buffer));
                        if (AppConfig.ByteCommand.CMD_03 == buffer[0]) {
                            handleError(buffer[1]);
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        //Slave:0x05 0x00 0x02
                        if (AppConfig.ByteCommand.CMD_05 != buffer[0] || AppConfig.ByteCommand.BASE != buffer[1] || AppConfig.ByteCommand.CMD_02 != buffer[2]) {
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        mCurPageIndex = buffer[3];
                        mPageCount = buffer[4];
                        queryPages((byte) mCurPageIndex);
                    }
                    break;
                //页传输
                case AppConfig.UsbHandler.WHAT_02:
                    if (null != bundle) {
                        byte[] buffer = bundle.getByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_02));
                        LogUtils.d("Enter to handleMessage: " + gson.toJson(buffer));
                        if (AppConfig.ByteCommand.CMD_03 == buffer[0]) {
                            handleError(buffer[1]);
                            //离线笔记传输完成切换成在线模式
                            setDeviceStatus(AppConfig.ByteCommand.CMD_01);
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        //Slave:0x05 0x01 0x03
                        if (AppConfig.ByteCommand.CMD_05 != buffer[0] || AppConfig.ByteCommand.CMD_01 != buffer[1] || AppConfig.ByteCommand.CMD_03 != buffer[2]) {
                            //离线笔记传输完成切换成在线模式
                            setDeviceStatus(AppConfig.ByteCommand.CMD_01);
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        mCoordinateCount = ByteUtils.byteToInteger(new byte[]{buffer[4], buffer[5]});
                        int[] positionCount;
                        //坐标传输，默认从第1个开始
                        positionCount = ByteUtils.integerToArray(0);
                        if (ObjectUtils.isOutOfBounds(positionCount, 2)) {
                            return;
                        }
                        buffer[4] = (byte) positionCount[1];
                        buffer[5] = (byte) positionCount[0];
                        queryCoordinates(buffer);
                    }
                    break;
                //坐标传输
                case AppConfig.UsbHandler.WHAT_03:
                    if (null != bundle) {
                        byte[] buffer = bundle.getByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_03));
                        LogUtils.d("Enter to handleMessage: " + gson.toJson(buffer));
                        if (AppConfig.ByteCommand.CMD_03 == buffer[0]) {
                            handleError(buffer[1]);
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        //Slave:0x05 0x02 0x08
                        if (AppConfig.ByteCommand.CMD_05 != buffer[0] || AppConfig.ByteCommand.CMD_02 != buffer[1] || AppConfig.ByteCommand.CMD_08 != buffer[2]) {
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        int curIndex = ByteUtils.byteToInteger(new byte[]{buffer[4], buffer[3]});
                        if (curIndex > 0) {
                            handleCoordinate(buffer);
                        }
                        mCoordinateCount--;
                        if (mCoordinateCount <= 0) {
                            //页坐标传输完成后将数据写入文件
                            saveData();

                            //当前页坐标传输完成后将进行下一页的传输
                            mCurPageIndex++;
                            if (mCurPageIndex < mPageCount) {
                                queryPages((byte) mCurPageIndex);
                            } else {
                                deletePage((byte) 0);
                                //离线笔记传输完成切换成在线模式
                                setDeviceStatus(AppConfig.ByteCommand.CMD_01);
                                EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            }
                        }

                        int[] positions = ByteUtils.integerToArray(mCoordinateCount);
                        if (ObjectUtils.isOutOfBounds(positions, 2)) {
                            return;
                        }
                        buffer[4] = (byte) positions[1];
                        buffer[5] = (byte) positions[0];
                        queryCoordinates(buffer);
                    }
                    break;
                //页面删除回调
                case AppConfig.UsbHandler.WHAT_04:
                    if (null != bundle) {
                        byte[] buffer = bundle.getByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_04));
                        LogUtils.d("Enter to handleMessage: " + gson.toJson(buffer));
                        if (AppConfig.ByteCommand.CMD_03 == buffer[0]) {
                            handleError(buffer[1]);
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        //Slave:0x05 0x03 0x02
                        if (AppConfig.ByteCommand.CMD_05 != buffer[0] || AppConfig.ByteCommand.CMD_03 != buffer[1] || AppConfig.ByteCommand.CMD_02 != buffer[2]) {
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        if (AppConfig.ByteCommand.BASE == buffer[4]) {
                            LogUtils.d("Delete page success.");
                        } else {
                            LogUtils.d("Delete page failed.");
                        }
                    }
                    break;
                //查询设备状态
                case AppConfig.UsbHandler.WHAT_05:
                    if (null != bundle) {
                        byte[] buffer = bundle.getByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_05));
                        LogUtils.d("Enter to handleMessage: " + gson.toJson(buffer));
                        if (AppConfig.ByteCommand.CMD_03 == buffer[0]) {
                            handleError(buffer[1]);
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        //Slave:0x01 0x01 0x01
                        if (AppConfig.ByteCommand.CMD_01 != buffer[0] || AppConfig.ByteCommand.CMD_01 != buffer[1] || AppConfig.ByteCommand.CMD_01 != buffer[1]) {
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        switch (buffer[3]) {
                            case AppConfig.ByteCommand.BASE:
                                LogUtils.d("Query device status: " + "离线模式");
                                break;
                            case AppConfig.ByteCommand.CMD_01:
                                LogUtils.d("Query device status: " + "在线模式");
                                break;
                            case AppConfig.ByteCommand.CMD_02:
                                LogUtils.d("Query device status: " + "SYNC模式");
                                break;
                            case AppConfig.ByteCommand.CMD_03:
                                LogUtils.d("Query device status: " + "固件升级模式");
                                break;
                        }
                    }
                    break;
                //设置设备状态
                case AppConfig.UsbHandler.WHAT_06:
                    if (null != bundle) {
                        byte[] buffer = bundle.getByteArray(String.valueOf(AppConfig.UsbHandler.WHAT_06));
                        LogUtils.d("Enter to handleMessage: " + gson.toJson(buffer));
                        if (AppConfig.ByteCommand.CMD_03 == buffer[0]) {
                            handleError(buffer[1]);
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        //Slave:0x01 0x01 0x01
                        if (AppConfig.ByteCommand.CMD_01 != buffer[0] || AppConfig.ByteCommand.CMD_01 != buffer[1] || AppConfig.ByteCommand.CMD_01 != buffer[1]) {
                            EventBus.getDefault().post(SyncEvent.END_SYNC_OFFLINE);
                            break;
                        }

                        switch (buffer[3]) {
                            case AppConfig.ByteCommand.BASE:
                                LogUtils.d("Query device status: " + "离线模式");
                                break;
                            case AppConfig.ByteCommand.CMD_01:
                                LogUtils.d("Query device status: " + "在线模式");
                                break;
                            case AppConfig.ByteCommand.CMD_02:
                                queryOffLineInfo();
                                LogUtils.d("Query device status: " + "SYNC模式");
                                break;
                            case AppConfig.ByteCommand.CMD_03:
                                LogUtils.d("Query device status: " + "固件升级模式");
                                break;
                        }
                    }
                    break;
            }
        }
    }

}
