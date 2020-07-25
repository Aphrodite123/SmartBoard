package com.aphrodite.smartboard.view.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.bean.WorkBriefBean;
import com.aphrodite.smartboard.model.event.ActionEvent;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.utils.TimeUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;
import com.aphrodite.smartboard.view.widget.dialog.ShareDialog;
import com.aphrodite.smartboard.view.widget.popupwindow.ListPopupWindow;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Aphrodite on 20-5-8
 */
public class BoardOnlineFragment extends BaseFragment {
    @BindView(R.id.palette_online_root)
    RelativeLayout mPaletteOnlineRoot;
    @BindView(R.id.palette_online_bg)
    ImageView mPaletteOnlineBg;
    @BindView(R.id.palette_online_canvas)
    SimpleDoodleView mPaletteOnlineCanvas;

    private String mCurrentDataPath;
    private String mCurrentAudioPath;
    private String mCurrentImagePath;

    private List<ScreenRecordEntity> mEntities;
    private CW mCw;

    private BoardStatusListener mStatusListener;
    private ShareDialog mShareDialog;
    private ListPopupWindow mListPopupWindow;
    private List<WorkBriefBean> mBeans;

    private DeleteDialog mDeleteDialog;

    public BoardOnlineFragment(BoardStatusListener statusListener) {
        this.mStatusListener = statusListener;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_board_online;
    }

    @Override
    protected void initView() {
        setToolbarFlag(TITLE_FLAG_SHOW_LEFT_BACK | TITLE_FLAG_SHOW_RIGHT_BTN);
        setLeftBtnRes(R.drawable.back);
        setRightBtnRes(R.drawable.share_toolbar_icon);
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {
        EventBus.getDefault().register(mEventListener);
        mLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
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

        if (TextUtils.isEmpty(mCurrentDataPath)) {
            return;
        }

        File file = new File(mCurrentImagePath);
        Glide.with(getContext()).load(file).into(mPaletteOnlineBg);

        mCw = CWFileUtils.read(mCurrentDataPath);
        if (null != mCw) {
            String createTime = TimeUtils.msToDateFormat(mCw.getTime(), TimeUtils.FORMAT_SPECIAL_SYMBOL_ONE);
            setTitleText(createTime);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(mEventListener);
        if (null != mShareDialog) {
            mShareDialog.dismiss();
        }
    }

    private void setWindowBackground(Float alpha) {
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = alpha;
        getActivity().getWindow().setAttributes(layoutParams);
    }

    @OnClick(R.id.switch_player)
    public void onSwitchPlay() {
        if (null != mStatusListener) {
            mStatusListener.onPlay();
        }
    }

    @OnClick(R.id.switch_detail)
    public void onSwitchDetail() {
        if (null == mCw) {
            return;
        }

        mBeans = new ArrayList<>();
        if (null != mCw) {
            WorkBriefBean bean0 = new WorkBriefBean(getString(R.string.author), mCw.getAuthor());
            WorkBriefBean bean1 = new WorkBriefBean(getString(R.string.create_time), TimeUtils.msToDateFormat(mCw.getTime(), TimeUtils.FORMAT_CHINESE_ONE));
            WorkBriefBean bean2 = new WorkBriefBean(getString(R.string.edit_time), TimeUtils.msToDateFormat(mCw.getEditTime(), TimeUtils.FORMAT_CHINESE_ONE));
            mBeans.add(bean0);
            mBeans.add(bean1);
            mBeans.add(bean2);
        }

        if (null == mListPopupWindow) {
            mListPopupWindow = new ListPopupWindow(getContext());
        }
        mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowBackground(1f);
            }
        });
        mListPopupWindow.setTitle(getString(R.string.work_info));
        mListPopupWindow.setList(mBeans);
        if (!mListPopupWindow.isShowing()) {
            mListPopupWindow.showAtLocation(mPaletteOnlineRoot, Gravity.BOTTOM, 0, 0);
            setWindowBackground(0.8f);
        }
    }

    @OnClick(R.id.switch_editor)
    public void onSwitchEditor() {
        if (null != mStatusListener) {
            mStatusListener.onEditor();
        }
    }

    @OnClick(R.id.switch_delete)
    public void onDelete() {
        if (null == mDeleteDialog) {
            mDeleteDialog = new DeleteDialog(getContext(), mClickListener);
        }
        if (!mDeleteDialog.isShowing()) {
            mDeleteDialog.show();
        }
    }

    @OnClick(R.id.iv_right_btn)
    public void onToolbarRightBtn() {
        if (null == mShareDialog) {
            mShareDialog = new ShareDialog(getContext(), mShareListener);
        }

        if (!mShareDialog.isShowing()) {
            mShareDialog.show();
        }
    }

    private DeleteDialog.OnClickListener mClickListener = new DeleteDialog.OnClickListener() {
        @Override
        public void onNegative() {

        }

        @Override
        public void onPositive() {
            String dir = mCurrentDataPath.substring(0, mCurrentDataPath.lastIndexOf(AppConfig.SLASH));
            FileUtils.deleteDir(new File(dir), true);
            getActivity().finish();
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
            getActivity().finish();
        }
    };

}
