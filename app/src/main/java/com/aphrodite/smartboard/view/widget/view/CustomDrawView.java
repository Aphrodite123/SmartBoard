package com.aphrodite.smartboard.view.widget.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.apeman.sdk.bean.DevicePoint;
import com.aphrodite.smartboard.model.ffmpeg.TouchGestureDetector;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by Aphrodite on 2020/7/30.
 * 自定义画板适配智能手写板,支持涂鸦&过程回放
 */
public class CustomDrawView extends View {
    private Context mContext;

    //是否开启涂鸦
    private boolean mCanDraw;

    private List<List<DevicePoint>> mLines;
    private List<DevicePoint> mPoints;

    public CustomDrawView(Context context) {
        super(context);
    }

    public CustomDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (!mCanDraw) {
            return true;
        }
        boolean consumed = mTouchGestureDetector.onTouchEvent(event); // 由手势识别器处理手势
        if (!consumed) {
            return super.dispatchTouchEvent(event);
        }
        return true;
    }

    private TouchGestureDetector mTouchGestureDetector = new TouchGestureDetector(mContext, new TouchGestureDetector.OnTouchGestureListener() {
        @Override
        public void onScrollBegin(MotionEvent e) {
            super.onScrollBegin(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public void onScrollEnd(MotionEvent e) {
            super.onScrollEnd(e);
        }
    });

}
