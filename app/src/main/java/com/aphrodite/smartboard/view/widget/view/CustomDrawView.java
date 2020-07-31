package com.aphrodite.smartboard.view.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.apeman.sdk.bean.DevicePoint;
import com.aphrodite.smartboard.model.ffmpeg.TouchGestureDetector;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Created by Aphrodite on 2020/7/30.
 * 自定义画板适配智能手写板,支持涂鸦&过程回放 https://www.jianshu.com/p/548d2799fd6e
 */
public class CustomDrawView extends View {
    private Context mContext;

    private static int POINT_COUNT = 5;
    private int mLineWidth;
    private int mLineColor;
    private int mEraserWidth;
    private int mEraserColor;
    private Paint mPaint;
    private Path mPath;
    private int mode;

    //是否开启涂鸦
    private boolean mCanDraw;

    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;

    private List<List<DevicePoint>> mLines;
    private List<DevicePoint> mPoints;
    private int mLastX;
    private int mLastY;
    private int mPressure;

    public CustomDrawView(Context context) {
        super(context);
    }

    public CustomDrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        this.mLines = new ArrayList<>();
        this.mPressure = 1023;
        this.mLineWidth = 10;
        this.mLineColor = Color.BLACK;
        this.mEraserWidth = 20;
        this.mEraserColor = Color.WHITE;
        this.mode = Mode.DRAW;

        initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != mBufferBitmap) {
            canvas.drawBitmap(mBufferBitmap, 0, 0, null);
        }
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

    private void initBuffer() {
        mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);
    }

    private void initPaint() {
        if (null == mPaint) {
            mPaint = new Paint();
        }

        mPaint.setColor(mLineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        switch (mode) {
            case Mode.DRAW:
                mPaint.setXfermode(null);
                mPaint.setStrokeWidth(mLineWidth);
                mPaint.setColor(mLineColor);
                break;
            case Mode.ERASER:
                mPaint.setStrokeWidth(mEraserWidth);
                mPaint.setColor(mEraserColor);
                break;
        }
    }

    private void addPoint(int x, int y, int p) {
        DevicePoint point = new DevicePoint();
        point.setX(x);
        point.setY(y);
        point.setPressure(p);
        mPoints.add(point);
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private TouchGestureDetector mTouchGestureDetector = new TouchGestureDetector(mContext, new TouchGestureDetector.OnTouchGestureListener() {
        @Override
        public void onScrollBegin(MotionEvent e) {
            if (null == mPath) {
                mPath = new Path();
            }

            mPoints = new ArrayList<>();
            mLastX = (int) e.getX();
            mLastY = (int) e.getY();
            addPoint(mLastX, mLastY, mPressure);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mLastX = (int) e2.getX();
            mLastY = (int) e2.getY();
            addPoint(mLastX, mLastY, mPressure);
            invalidate();
            return true;
        }

        @Override
        public void onScrollEnd(MotionEvent e) {
            mLastX = (int) e.getX();
            mLastY = (int) e.getY();
            addPoint(mLastX, mLastY, mPressure);
            invalidate();
        }
    });

    public interface Mode {
        int BASE = 0x00;

        //绘制
        int DRAW = BASE + 1;

        //擦除
        int ERASER = BASE + 2;
    }

}
