package com.aphrodite.smartboard.view.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.framework.utils.ToastUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.event.ActionEvent;
import com.aphrodite.smartboard.utils.BitmapUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;
import com.aphrodite.smartboard.view.widget.dialog.ShareDialog;
import com.aphrodite.smartboard.view.widget.popupwindow.PaletePopupWindow;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-5-8
 */
public class BoardEditorFragment extends BaseFragment {
    @BindView(R.id.board_editor_root)
    RelativeLayout mRoot;
    @BindView(R.id.custom_canvas)
    SimpleDoodleView mCanvas;
    @BindView(R.id.canvas_bottom_tab)
    LinearLayout mCanvasBottomTab;
    @BindView(R.id.switch_color)
    TextView mSwitchColorBtn;

    private String mCurrentDataPath;
    private String mCurrentAudioPath;
    private String mCurrentImagePath;

    private List<ScreenRecordEntity> mEntities;
    private CW mCw;

    private BoardStatusListener mStatusListener;

    private List<ScreenRecordEntity> mRecordEntities = new ArrayList<>();

    private PaletePopupWindow mPaletePopupWindow;
    private List<Integer> mColorIds;

    //是否正在对作品编辑，默认进入则为正在编辑
    private boolean mIsEditing = true;
    private ShareDialog mShareDialog;
    private DeleteDialog mPromptDialog;

    public BoardEditorFragment(BoardStatusListener statusListener) {
        this.mStatusListener = statusListener;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_board_editor;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK | TITLE_FLAG_SHOW_RIGHT_BTN);
        setLeftBtnRes(R.drawable.back);
        setRightBtnRes(R.drawable.icon_done);
        setTitleText(R.string.editor);
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {
        EventBus.getDefault().register(mEventListener);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mStatusListener) {
                    mStatusListener.onPreview();
                }
            }
        });

        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsEditing) {
                    savePicture();
                    mIsEditing = false;
                    setRightBtnRes(R.drawable.share_toolbar_icon);
                } else {
                    shareDialog();
                }
            }
        });
    }

    @Override
    protected void initData() {
        Bundle bundle = getArguments();
        if (null != bundle) {
            mCurrentDataPath = bundle.getString(IntentAction.CanvasAction.PATH_TRACK_FILE);
            mCurrentAudioPath = bundle.getString(IntentAction.CanvasAction.PATH_AUDIO_FILE);
            mCurrentImagePath = bundle.getString(IntentAction.CanvasAction.PATH_COVER_IMAGE);
        }

        onBottomTab(0);

        getPaths();
        drawPath(mCw);

        mColorIds = new ArrayList<>();
        mColorIds.add(getResources().getColor(R.color.color_b71919));
        mColorIds.add(getResources().getColor(R.color.color_ffe82a));
        mColorIds.add(getResources().getColor(R.color.color_2e48ff));
        mColorIds.add(getResources().getColor(R.color.color_7bff16));
        mColorIds.add(getResources().getColor(R.color.color_f836ff));
        mColorIds.add(getResources().getColor(R.color.color_32397e));
        mColorIds.add(getResources().getColor(R.color.color_bac290));
        mColorIds.add(getResources().getColor(R.color.color_492c4f));
        mColorIds.add(getResources().getColor(R.color.color_ffaaaa));
        mColorIds.add(getResources().getColor(R.color.color_ffbe79));
        mColorIds.add(getResources().getColor(R.color.color_ff0000));
        mColorIds.add(getResources().getColor(R.color.color_000000));

        //默认为红色
        mCanvas.setDrawColor(getResources().getColor(R.color.color_7bff16));
        ScreenRecordEntity recordEntity = new ScreenRecordEntity();
        recordEntity.setType("0");
        mRecordEntities.add(recordEntity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(mEventListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setWindowBackground(Float alpha) {
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = alpha;
        getActivity().getWindow().setAttributes(layoutParams);
    }

    private void getPaths() {
        if (TextUtils.isEmpty(mCurrentDataPath)) {
            return;
        }

        mCw = CWFileUtils.read(mCurrentDataPath);
    }

    private void drawPath(CW cw) {
        if (null == cw) {
            return;
        }

        List<CWACT> cwacts = cw.getACT();
        if (ObjectUtils.isEmpty(cwacts)) {
            return;
        }

        for (CWACT cwact : cwacts) {
            if (null == cwact) {
                continue;
            }
            if (null != mCanvas) {
                mCanvas.splitLine(cwact.getLine());
            }
        }
    }

    private void savePicture() {
        String dir = null;
        if (!TextUtils.isEmpty(mCurrentDataPath)) {
            dir = mCurrentDataPath.substring(0, mCurrentDataPath.lastIndexOf(AppConfig.SLASH));
        }
        String imageName = "shot.jpg";

        try {
            BitmapUtils.saveBitmap(BitmapUtils.shotToView(mCanvas), dir, imageName, Bitmap.CompressFormat.JPEG, 100);
            File file = new File(dir + File.separator + imageName);
            MediaStore.Images.Media.insertImage(getContext().getContentResolver(), file.getAbsolutePath(), imageName, null);

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
            getContext().sendBroadcast(intent);

            ToastUtils.showMessage(R.string.toast_save_to_gallery_successed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shareDialog() {
        if (null == mShareDialog) {
            mShareDialog = new ShareDialog(getContext(), mShareListener);
        }

        if (!mShareDialog.isShowing()) {
            mShareDialog.show();
        }
    }

    private void onBottomTab(int index) {
        switch (index) {
            //畫筆
            case 0:
                if (null != mCanvas) {
                    mCanvas.setCanDraw(true);
                }
                break;
            //橡皮
            case 1:
                mCanvas.setIsEraser(true);
                break;
            //色板
            case 2:
                if (null == mPaletePopupWindow) {
                    mPaletePopupWindow = new PaletePopupWindow(getContext(), mColorIds);
                }
                mPaletePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        setWindowBackground(1f);
                    }
                });
                if (!mPaletePopupWindow.isShowing()) {
                    mPaletePopupWindow.showAtLocation(mRoot, Gravity.BOTTOM, 0, 0);
                    setWindowBackground(0.8f);
                }
                break;
            //清空
            case 3:
                mCanvas.clear();
                break;
        }
    }

    @OnClick(R.id.switch_paint)
    public void onPainting() {
        onBottomTab(0);
    }

    @OnClick(R.id.switch_eraser)
    public void onEraser() {
        onBottomTab(1);
    }

    @OnClick(R.id.switch_color)
    public void onColor() {
        onBottomTab(2);
    }

    @OnClick(R.id.switch_clear)
    public void onClear() {
        onBottomTab(3);
    }

    private DeleteDialog.OnClickListener mClickListener = new DeleteDialog.OnClickListener() {
        @Override
        public void onNegative() {
            if (null != mStatusListener) {
                mStatusListener.onPreview();
            }
        }

        @Override
        public void onPositive() {
            savePicture();
            if (null != mStatusListener) {
                mStatusListener.onPreview();
            }
        }
    };

    private ShareDialog.OnListener mShareListener = new ShareDialog.OnListener() {
        @Override
        public void onConfirm(int type, int id) {
        }
    };

    private Object mEventListener = new Object() {
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onEventMainThread(ActionEvent event) {
            if (mIsEditing) {
                if (null == mPromptDialog) {
                    mPromptDialog = new DeleteDialog(getContext(), mClickListener);
                }
                mPromptDialog.setTitle(R.string.prompt);
                mPromptDialog.setMessage(R.string.message_edit_exit);

                if (!mPromptDialog.isShowing()) {
                    mPromptDialog.show();
                }
            } else {
                if (null != mStatusListener) {
                    mStatusListener.onPreview();
                }
            }
        }
    };

}
