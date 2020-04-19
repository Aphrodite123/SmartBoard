package com.aphrodite.smartboard.model.timer;

import android.os.CountDownTimer;

public class CustomCountDownTimer extends CountDownTimer {
    private CountDownListener mListener;

    public CustomCountDownTimer(long millisInFuture, long countDownInterval, CountDownListener listener) {
        super(millisInFuture, countDownInterval);
        this.mListener = listener;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (null != mListener) {
            mListener.onTick(millisUntilFinished);
        }
    }

    @Override
    public void onFinish() {
        if (null != mListener) {
            mListener.onFinish();
        }
    }

    public interface CountDownListener {
        void onTick(long time);

        void onFinish();
    }

}
