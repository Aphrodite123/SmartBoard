package com.aphrodite.smartboard.view.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
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
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.utils.BitmapUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.view.activity.base.BaseDeviceActivity;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private int mStrokeWidth = 10;
    private int mDrawColor = Color.BLACK;
    private long mTimestamp;

    //按照设备比例缩放后的画布宽度
    private int mCanvasWidth;
    //按照设备比例缩放后的画布高度
    private int mCanvasHeight;

    //将设备坐标点转换为画布坐标点的缩放比例
    private Double mXScale;
    private Double mYScale;

    private Paint mPaint;
    private List<DevicePoint> mDevicePoints;
    private List<ScreenRecordEntity> data = new ArrayList<>();

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
        getDeviceInfo();
        mTimestamp = System.currentTimeMillis();
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
                    if ((byte) 0x00 == bytes) {
                        cleanBoardView();
                    } else if ((byte) 0x01 == bytes) {
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
                                msg.setText(devicePoint.toString());

                                parseDevicePoint(devicePoint);
                                return null;
                            }
                        });

                    }
                });
            }
        });
    }

    public void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(mDrawColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void getDeviceInfo() {
        BoardType boardType = BoardType.NoteMaker;
        float deviceScale = (float) (boardType.getMaxX() / boardType.getMaxY());
        int viewWidth = UIUtils.getDisplayWidthPixels(this);
        int viewHeight = UIUtils.getDisplayHeightPixels(this);
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

    private void parseDevicePoint(DevicePoint devicePoint) {
        if (null == devicePoint) {
            return;
        }

        switch (devicePoint.getState()) {
            case (byte) 0x00:
                //离开
                mDevicePoints = null;
                break;
            case (byte) 0x10:
                initPaint();
                //悬空
                if (null == mDevicePoints) {
                    mDevicePoints = new ArrayList<>();
                } else {
                    int red = (mPaint.getColor() & 0xff0000) >> 16;
                    int green = (mPaint.getColor() & 0x00ff00) >> 8;
                    int blue = (mPaint.getColor() & 0x0000ff);
                    CWFileUtils.writeLine(mDevicePoints, (int) mPaint.getStrokeWidth(), red + "," + green + "," + blue + ",1");
                    mDevicePoints.clear();
                }
                break;
            case (byte) 0x11:
                //压下
                if (null != mDevicePoints) {
                    mDevicePoints.add(devicePoint);
                }
                break;
        }
    }

    private void saveData() {
        ScreenRecordEntity screenRecordEntity = new ScreenRecordEntity();
        screenRecordEntity.setType("data/0");
        data.add(screenRecordEntity);

        CWFileUtils.write(data, AppConfig.DATA_PATH + mTimestamp + File.separator, mCanvasWidth, mCanvasHeight, mTimestamp);
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
                return Unit.INSTANCE;
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
        String dir = AppConfig.DATA_PATH + mTimestamp + File.separator;
        String imageName = "shot.jpg";

        try {
            BitmapUtils.saveBitmap(BitmapUtils.shotToView(mBoardView), dir, imageName, Bitmap.CompressFormat.JPEG, 100);
            File file = new File(dir + File.separator + imageName);
            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), imageName, null);

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
            sendBroadcast(intent);

            ToastUtils.showMessage(R.string.toast_save_to_gallery_successed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
        saveData();
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
