package com.aphrodite.smartboard.view.widget.dialog;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;

public class DeleteDialog extends Dialog implements View.OnClickListener {
    private Context mContext;
    private TextView mTitle;
    private TextView message;
    private TextView mCancelBtn;
    private TextView mConfirmBtn;
    private OnClickListener mOnClickListener;

    public interface OnClickListener {
        void onPositive();
    }

    public DeleteDialog(@NonNull Context context, OnClickListener listener) {
        super(context);
        this.mContext = context;
        this.mOnClickListener = listener;
        init(context);
    }

    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_delete, null);
        setContentView(root);

        mTitle = root.findViewById(R.id.title);
        message = root.findViewById(R.id.message);
        mCancelBtn = root.findViewById(R.id.cancel_btn);
        mConfirmBtn = root.findViewById(R.id.confirm_btn);

        mCancelBtn.setOnClickListener(this);
        mConfirmBtn.setOnClickListener(this);

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = UIUtils.dip2px(context, 280);
        lp.height = UIUtils.dip2px(context, 250);
        lp.y = context.getResources().getDimensionPixelSize(R.dimen.dip8);
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        dialogWindow.setAttributes(lp);
        dialogWindow.setWindowAnimations(android.R.style.Animation_Dialog);
        setCanceledOnTouchOutside(true);
        setCancelable(true);
    }

    public void setTitle(int title) {
        if (null != mTitle) {
            mTitle.setText(title);
        }
    }

    public void setTitle(String title) {
        if (null != mTitle) {
            mTitle.setText(title);
        }
    }

    public void setMessage(int message) {
        if (null != mTitle) {
            mTitle.setText(message);
        }
    }

    public void setMessage(String message) {
        if (null != mTitle) {
            mTitle.setText(message);
        }
    }

    public void setMessage(TextView message) {
        this.message = message;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_btn:
                dismiss();
                break;
            case R.id.confirm_btn:
                if (null != mOnClickListener) {
                    mOnClickListener.onPositive();
                    dismiss();
                }
                break;
        }

    }
}
