package com.aphrodite.smartboard.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.apeman.sdk.bean.BoardType;
import com.apeman.sdk.bean.DevicePoint;
import com.apeman.sdk.bean.NoteDescription;
import com.apeman.sdk.service.ConnectStatus;
import com.apeman.sdk.service.PenService;
import com.apeman.sdk.service.command.Command;
import com.apeman.sdk.widget.BoardView;
import com.apeman.sdk.widget.BoardViewCallback;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.activity.base.BaseDeviceActivity;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;

import butterknife.BindView;
import butterknife.OnClick;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

/**
 * Created by Aphrodite on 20-6-19
 */
public class DeviceOnLineActivity extends BaseDeviceActivity {
    @BindView(R.id.board_view)
    public BoardView mBoardView;
    @BindView(R.id.msg)
    public TextView msg;

    private ProgressDialog mCleanProgressDialog;
    private DeleteDialog mPromptDialog;

    @Override
    protected int getViewId() {
        return R.layout.activity_device_online;
    }

    @Override
    protected void initView() {
        setStatusBarColor(this);
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK | TITLE_FLAG_SHOW_RIGHT_BTN);
        setLeftBtnRes(R.drawable.back);
        setRightBtnRes(R.drawable.share_toolbar_icon);
        setTitleColor(getResources().getColor(R.color.color_626262));
        initBoardView();
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initData() {
        PenService.Companion.connectUsbService(this, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PenService.Companion.disconnectUsbService(this, this);

        if (null != mUsbPenService) {
            mUsbPenService.observeDevicePoint(null);
            mUsbPenService.getCommandLiveData().removeObserver(getCmdObserver());
        }
    }

    @Override
    protected Observer<ConnectStatus> getConnObserver() {
        return new Observer<ConnectStatus>() {
            @Override
            public void onChanged(ConnectStatus connectStatus) {
                if (!connectStatus.getResult()) {
                    finish();
                }
            }
        };
    }

    @Override
    protected Observer<Command> getCmdObserver() {
        return new Observer<Command>() {
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
                        if (null != msg) {
                            msg.setText("onLoadFinished");
                        }

                        //这里需要注意一定要在完成画板的初始化后再监听硬件的报点
                        mUsbPenService.observeDevicePoint(new Function1<DevicePoint, Unit>() {
                            @Override
                            public Unit invoke(DevicePoint devicePoint) {
                                mBoardView.onPointReceived(devicePoint);
                                return null;
                            }
                        });

                    }
                });
            }
        });
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

    private void showDialog() {
        if (null == mPromptDialog) {
            mPromptDialog = new DeleteDialog(this, mClickListener);
        }
        mPromptDialog.setTitle(R.string.prompt);
        mPromptDialog.setMessage(R.string.message_save_image);

        if (!mPromptDialog.isShowing()) {
            mPromptDialog.show();
        }
    }

    private void dismissDialog() {
        if (null != mPromptDialog) {
            mPromptDialog.dismiss();
            mPromptDialog = null;
        }
    }

    private void savePicture() {
    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
        showDialog();
    }

    private DeleteDialog.OnClickListener mClickListener = new DeleteDialog.OnClickListener() {
        @Override
        public void onNegative() {
            dismissDialog();
        }

        @Override
        public void onPositive() {
            savePicture();
        }
    };

}
