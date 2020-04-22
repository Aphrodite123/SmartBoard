package com.aphrodite.smartboard.view.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.config.AppConfig;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;

/**
 * Created by Aphrodite on 2019/3/7.
 */
public class ShareDialog extends Dialog implements View.OnClickListener {
    private Context mContext;

    private ImageView mCloseBtn;
    private TextView mWechatFriend;
    private TextView mWechatMoments;

    private WXMediaMessage mediaMessage;
    private int mThumbSize;

    private OnListener mListener;
    private int mId;

    public interface OnListener {
        void onConfirm(int type, int id);
    }

    public ShareDialog(@NonNull Context context, OnListener listener) {
        super(context);
        this.mContext = context;
        this.mListener = listener;
        this.mThumbSize = UIUtils.dip2px(context, 20);

        init(context);
    }

    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_share, null);

        mCloseBtn = root.findViewById(R.id.dialog_close_iv);
        mWechatFriend = root.findViewById(R.id.wechat_friends_btn);
        mWechatMoments = root.findViewById(R.id.wechat_moments_btn);

        mCloseBtn.setOnClickListener(this);
        mWechatFriend.setOnClickListener(this);
        mWechatMoments.setOnClickListener(this);

        setContentView(root);

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = UIUtils.dip2px(context, 280);
        lp.height = UIUtils.dip2px(context, 300);
        lp.y = context.getResources().getDimensionPixelSize(R.dimen.dip8);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialogWindow.setAttributes(lp);
        dialogWindow.setWindowAnimations(android.R.style.Animation_Dialog);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_close_iv:
                cancel();
                break;
            case R.id.wechat_friends_btn:
                if (null != mListener) {
                    mListener.onConfirm(AppConfig.ShareType.WECHAT_FRIEND, mId);
                }
                dismiss();
                break;
            case R.id.wechat_moments_btn:
                if (null != mListener) {
                    mListener.onConfirm(AppConfig.ShareType.WECHAT_MOMENTS, mId);
                }
                dismiss();
                break;
        }
    }

}
