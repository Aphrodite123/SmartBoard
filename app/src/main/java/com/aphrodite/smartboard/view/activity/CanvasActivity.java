package com.aphrodite.smartboard.view.activity;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import butterknife.BindView;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import com.apeman.sdk.bean.BoardType;
import com.apeman.sdk.bean.DevicePoint;
import com.apeman.sdk.bean.NoteDescription;
import com.apeman.sdk.bean.UsbBoardInfo;
import com.apeman.sdk.service.ConnectStatus;
import com.apeman.sdk.service.PenService;
import com.apeman.sdk.service.UsbPenService;
import com.apeman.sdk.service.command.Command;
import com.apeman.sdk.service.usb.USBPenServiceImpl;
import com.apeman.sdk.widget.BoardView;
import com.apeman.sdk.widget.BoardViewCallback;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.event.ActionEvent;
import com.aphrodite.smartboard.utils.LogUtils;
import com.aphrodite.smartboard.view.activity.base.BaseActivity;
import com.aphrodite.smartboard.view.fragment.BoardEditorFragment;
import com.aphrodite.smartboard.view.fragment.BoardOnlineFragment;
import com.aphrodite.smartboard.view.fragment.BoardPlayFragment;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aphrodite on 20-4-22
 * 画板
 */
public class CanvasActivity extends BaseActivity implements ServiceConnection {
    @BindView(R.id.board_view)
    BoardView mBoardView;
    @BindView(R.id.msg)
    TextView mInfo;

    private List<BaseFragment> mFragments;

    //设备是否在线
    private boolean mDeviceOnline = true;
    private String mCurrentDataPath;
    private String mCurrentAudioPath;
    private String mCurrentImagePath;

    private UsbPenService<UsbBoardInfo, UsbDevice> mUsbPenService;
    private ProgressDialog mCleanProgressDialog;

    @Override
    protected int getViewId() {
        return R.layout.activity_canvas;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
    }

    @Override
    protected void initListener() {
    }

    @Override
    protected void initData() {
//        Intent intent = getIntent();
//        if (null != intent) {
//            mCurrentDataPath = intent.getStringExtra(IntentAction.CanvasAction.PATH_TRACK_FILE);
//            mCurrentAudioPath = intent.getStringExtra(IntentAction.CanvasAction.PATH_AUDIO_FILE);
//            mCurrentImagePath = intent.getStringExtra(IntentAction.CanvasAction.PATH_COVER_IMAGE);
//        }
//
//        mFragments = new ArrayList<>();
//        BoardOnlineFragment onlineFragment = new BoardOnlineFragment(mStatusListener);
//        BoardPlayFragment playFragment = new BoardPlayFragment(mStatusListener);
//        BoardEditorFragment editorFragment = new BoardEditorFragment(mStatusListener);
//
//        Bundle bundle = new Bundle();
//        bundle.putString(IntentAction.CanvasAction.PATH_TRACK_FILE, mCurrentDataPath);
//        bundle.putString(IntentAction.CanvasAction.PATH_AUDIO_FILE, mCurrentAudioPath);
//        bundle.putString(IntentAction.CanvasAction.PATH_COVER_IMAGE, mCurrentImagePath);
//        onlineFragment.setArguments(bundle);
//        playFragment.setArguments(bundle);
//        editorFragment.setArguments(bundle);
//
//        mFragments.add(onlineFragment);
//        mFragments.add(playFragment);
//        mFragments.add(editorFragment);
//
//        switchFragment(mDeviceOnline ? 0 : 2);

        initBoardView();
    }

    private void initBoardView() {
        mBoardView.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                NoteDescription description = null;
                if (null != intent) {
                    description = intent.getParcelableExtra("versionInfo");
                }
                mBoardView.setup(BoardType.NoteMaker, description);

                mBoardView.setLoadFinishCallback(new BoardViewCallback() {
                    @Override
                    public void onLoadFinished() {
                        PenService.Companion.connectUsbService(CanvasActivity.this, CanvasActivity.this);
                        if (null != mInfo) {
                            mInfo.setText("onLoadFinished");
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void clickBack(View view) {
    }

//    @Override
//    public void onBackPressed() {
//        EventBus.getDefault().post(ActionEvent.BACK_PRESSED_BOARD);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCleanProgressDialog) {
            mCleanProgressDialog.dismiss();
        }

        PenService.Companion.disconnectUsbService(this, this);

        if (null != mUsbPenService) {
            mUsbPenService.observeDevicePoint(null);
            mUsbPenService.getCommandLiveData().removeObserver(mCmdObserver);
        }
    }

    private void switchFragment(int index) {
        if (ObjectUtils.isOutOfBounds(mFragments, index)) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root, mFragments.get(index));
        fragmentTransaction.commit();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        USBPenServiceImpl.ConnBinder connBinder = (USBPenServiceImpl.ConnBinder) service;
        mUsbPenService = connBinder.getService();
        mUsbPenService.getConnLiveData().observe(this, mConnObserver);
        mUsbPenService.getCommandLiveData().observe(this, mCmdObserver);
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
        if(null!=mInfo){
            mInfo.setText("Finish on onServiceDisconnected");
        }
        ToastUtils.showMessage(R.string.device_disconnected);
        finish();
    }

    private void cleanBoardView() {
        if (null == mCleanProgressDialog) {
            mCleanProgressDialog = new ProgressDialog(this);
            mCleanProgressDialog.setCancelable(false);
        }
        if (!mCleanProgressDialog.isShowing()) {
            mCleanProgressDialog.show();
        }

        mBoardView.clearBoardAndFile(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (null != mCleanProgressDialog) {
                    mCleanProgressDialog.dismiss();
                }
                return null;
            }
        });
    }

    private Observer mConnObserver = new Observer<ConnectStatus>() {
        private ConnectStatus lastValue;

        @Override
        public void onChanged(ConnectStatus connectStatus) {
            if (lastValue == connectStatus) {
                return;
            }
            lastValue = connectStatus;
            if (null != mInfo) {
                mInfo.setText("连接状态：" + connectStatus.getResult() + " , " + connectStatus.getMsg());
            }
            if (connectStatus.getResult()) {
                //这里需要注意一定要在完成画板的初始化后再监听硬件的报点
                mUsbPenService.observeDevicePoint(new Function1<DevicePoint, Unit>() {
                    @Override
                    public Unit invoke(DevicePoint devicePoint) {
                        mBoardView.onPointReceived(devicePoint);
                        mInfo.setText(devicePoint.toString());
                        LogUtils.d("DevicePoint: " + devicePoint.toString());
                        return null;
                    }
                });
            } else {
                if(null!=mInfo){
                    mInfo.setText("Finish on onChanged");
                }

                ToastUtils.showMessage(connectStatus.getMsg());
                finish();
            }
        }
    };

    private Observer mCmdObserver = new Observer<Command>() {
        @Override
        public void onChanged(Command command) {
            if (null == command) {
                return;
            }

            Byte bytes;
            while ((bytes = command.getExtra()) != null) {
                if (0x00 == bytes) {
                    cleanBoardView();
                } else if (0x01 == bytes) {
                    mBoardView.newPage();
                }
            }

        }
    };

    private BoardStatusListener mStatusListener = new BoardStatusListener() {
        @Override
        public void onPreview() {
            switchFragment(0);
        }

        @Override
        public void onPlay() {
            switchFragment(1);
        }

        @Override
        public void onEditor() {
            switchFragment(2);
        }
    };

}
