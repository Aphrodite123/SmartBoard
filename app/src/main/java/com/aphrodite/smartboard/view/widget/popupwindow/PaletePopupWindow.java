package com.aphrodite.smartboard.view.widget.popupwindow;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.view.adapter.PaletePopAdapter;

import java.util.List;

public class PaletePopupWindow extends PopupWindow {
    private Context mContext;
    private List<Integer> mColorIds;
    private GridView mGridView;
    private PaletePopAdapter mPaletePopAdapter;

    public PaletePopupWindow(Context context, List<Integer> colorIds) {
        super(context);
        this.mContext = context;
        this.mColorIds = colorIds;
        initView();
        initData();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.popup_window_palete, null);
        setContentView(view);

        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.anim_popupwindow_zoom);

        mGridView = view.findViewById(R.id.palete_gv);
        mPaletePopAdapter = new PaletePopAdapter(mContext, mColorIds);
        mGridView.setAdapter(mPaletePopAdapter);
    }

    private void initData() {

    }

    @Override
    public void showAsDropDown(View anchor) {
        //解决Android7.0中PopupWindow会覆盖Toolbar 20180420
        if (Build.VERSION.SDK_INT >= 24) {
            Rect visibleFrame = new Rect();
            anchor.getGlobalVisibleRect(visibleFrame);
            int height = anchor.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom;
            setHeight(height);
        }

        super.showAsDropDown(anchor);
    }

}
