package com.aphrodite.smartboard.view.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.aphrodite.framework.utils.UIUtils;
import com.aphrodite.smartboard.R;

/**
 * Description:
 * Name:         CustomMessageDialog
 * Author:       zhangjingming
 * Date:         2016-09-20
 */

public class CustomMessageDialog extends Dialog implements View.OnClickListener {
    private Context context;
    private TextView tvMessage;
    private Button btnNegative;
    private Button btnPositive;
    private View divideLine;
    private OnDialogClickListener listener;
    private TextView tvTitle;

    public interface OnDialogClickListener {
        void onNegative();

        void onPositive();
    }

    public CustomMessageDialog(Context context, OnDialogClickListener listener) {
        super(context, R.style.custom_progress_dialog);
        init(context);
        this.context = context;
        this.listener = listener;
    }

    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_custom_message, null);
        tvTitle = root.findViewById(R.id.tv_title);
        btnNegative = root.findViewById(R.id.btn_negative);
        btnPositive = root.findViewById(R.id.btn_positive);
        btnNegative.setOnClickListener(this);
        btnPositive.setOnClickListener(this);
        divideLine = root.findViewById(R.id.divide_line);
        tvMessage = root.findViewById(R.id.tv_message);
        setContentView(root);

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = UIUtils.getScreenSize(context)[0] - context.getResources().getDimensionPixelSize(R.dimen.dip16);
        lp.y = context.getResources().getDimensionPixelSize(R.dimen.dip8);
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(0x55000000));
        dialogWindow.setAttributes(lp);
        dialogWindow.setWindowAnimations(android.R.style.Animation_Dialog);
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (null != listener)
                    listener.onNegative();
            }
        });
        setCanceledOnTouchOutside(false);
        setCancelable(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_negative:
                cancel();
                if (null != listener)
                    listener.onNegative();
                break;

            case R.id.btn_positive:
                dismiss();
                if (null != listener)
                    listener.onPositive();
                break;

            default:
                break;
        }
    }

    @Override
    public void show() {
        if (TextUtils.isEmpty(btnPositive.getText())) {
            btnPositive.setVisibility(View.GONE);
            divideLine.setVisibility(View.GONE);
        }
        if (TextUtils.isEmpty(btnNegative.getText())) {
            btnNegative.setVisibility(View.GONE);
            divideLine.setVisibility(View.GONE);
        }
        super.show();
    }

    public void setGravity(int gravity) {
        getWindow().setGravity(gravity);
    }

    public void setTitle(String title) {
        if (null != tvTitle && !TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    public void setTitle(int titleId) {
        if (null != tvTitle) {
            tvTitle.setText(titleId);
            tvTitle.setVisibility(View.VISIBLE);
        }
    }

    public void setMessage(String message) {
        if (null != tvMessage)
            tvMessage.setText(message);
    }

    public void setMessage(int messageId) {
        if (null != tvMessage)
            tvMessage.setText(messageId);
    }

    public void setPositiveText(String positiveText) {
        if (null != btnPositive)
            btnPositive.setText(positiveText);
    }

    public void setPositiveText(int positiveTextId) {
        if (null != btnPositive)
            btnPositive.setText(positiveTextId);
    }

    public void setNegativeText(String negativeText) {
        if (null != btnNegative)
            btnNegative.setText(negativeText);
    }

    public void setNegativeText(int negativeTextId) {
        if (null != btnNegative)
            btnNegative.setText(negativeTextId);
    }

    public void setPositiveTextColor(int color) {
        if (null != btnPositive)
            btnPositive.setTextColor(color);
    }
}
