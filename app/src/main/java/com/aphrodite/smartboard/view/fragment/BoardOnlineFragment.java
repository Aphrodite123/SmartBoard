package com.aphrodite.smartboard.view.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.IntentAction;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWPage;
import com.aphrodite.smartboard.model.bean.ScreenRecordEntity;
import com.aphrodite.smartboard.model.bean.WorkBriefBean;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.FileUtils;
import com.aphrodite.smartboard.view.fragment.base.BaseFragment;
import com.aphrodite.smartboard.view.inter.BoardStatusListener;
import com.aphrodite.smartboard.view.widget.dialog.DeleteDialog;
import com.aphrodite.smartboard.view.widget.dialog.ShareDialog;
import com.aphrodite.smartboard.view.widget.popupwindow.ListPopupWindow;
import com.aphrodite.smartboard.view.widget.view.SimpleDoodleView;

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

    private String mCurrentFilePath;
    private List<ScreenRecordEntity> mEntities;
    private CW mCw;

    private BoardStatusListener mStatusListener;
    private ShareDialog mShareDialog = null;
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
        setTitleText("2020.5.8");
        setTitleColor(getResources().getColor(R.color.color_626262));
    }

    @Override
    protected void initListener() {
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
            mCurrentFilePath = bundle.getString(IntentAction.CanvasAction.PATH_TRACK_FILE);
        }

        getPaths();

        mBeans = new ArrayList<>();
        WorkBriefBean bean0 = new WorkBriefBean("创建时间", "2020.5.12 12：30");
        WorkBriefBean bean1 = new WorkBriefBean("修改时间", "2020.5.12 12：30");
        WorkBriefBean bean2 = new WorkBriefBean("创建时间", "2020.5.12 12：30");
        WorkBriefBean bean3 = new WorkBriefBean("创建时间", "2020.5.12 12：30");
        mBeans.add(bean0);
        mBeans.add(bean1);
        mBeans.add(bean2);
        mBeans.add(bean3);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (null != mShareDialog) {
            mShareDialog.dismiss();
        }
    }

    private void getPaths() {
        if (TextUtils.isEmpty(mCurrentFilePath)) {
            return;
        }

        mCw = CWFileUtils.read(mCurrentFilePath);
        if (null == mCw) {
            return;
        }

        List<CWPage> cwPages = mCw.getPAGES();
        if (ObjectUtils.isEmpty(cwPages)) {
            return;
        }

        mEntities = new ArrayList<>();
        for (CWPage cwPage : cwPages) {
            if (null == cwPage) {
                continue;
            }
            ScreenRecordEntity recordEntity = new ScreenRecordEntity();
            recordEntity.setCanDraw(false);
            recordEntity.setType("0");
            mEntities.add(recordEntity);
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
        if (null == mListPopupWindow) {
            mListPopupWindow = new ListPopupWindow(getContext());
        }
        mListPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setWindowBackground(1f);
            }
        });
        mListPopupWindow.setTitle("作品信息");
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
        public void onPositive() {
            FileUtils.deleteFile(new File(mCurrentFilePath), false);
        }
    };

    private ShareDialog.OnListener mShareListener = new ShareDialog.OnListener() {
        @Override
        public void onConfirm(int type, int id) {
        }
    };

}
