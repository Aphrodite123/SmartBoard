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
    private float mLastX;
    private float mLastY;
    private float mPressure;

    public CustomDrawView(Context context) {
        this(context, null);
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
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                if (null == mPath) {
                    mPath = new Path();
                }
                mPath.moveTo(mLastX, mLastY);
                break;
            case MotionEvent.ACTION_MOVE:
                if (Mode.ERASER == mode || !mCanDraw) {
                    break;
                }
                mPath.quadTo(mLastX, mLastY, (mLastX + x) / 2, (mLastY + y) / 2);
                if (null == mBufferBitmap) {
                    initBuffer();
                }
                mBufferCanvas.drawPath(mPath, mPaint);
                invalidate();
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (Mode.DRAW == Mode.DRAW && mCanDraw) {
                    mLastX = x;
                    mLastY = y;
                    addPoint(mLastX, mLastY, mPressure);
                }
                mPath.reset();
                break;
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

    private void addPoint(float x, float y, float p) {
        DevicePoint point = new DevicePoint();
        point.setX((int) x);
        point.setY((int) y);
        point.setPressure((int) p);
        if (null == mPoints) {
            mPoints = new ArrayList<>();
        }
        mPoints.add(point);
    }

    public void clear() {
        if (null != mBufferBitmap) {
            mBufferBitmap.eraseColor(Color.TRANSPARENT);
        }
        invalidate();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setCanDraw(boolean canDraw) {
        this.mCanDraw = canDraw;
    }

    public interface Mode {
        int BASE = 0x00;

        //绘制
        int DRAW = BASE + 1;

        //擦除
        int ERASER = BASE + 2;
    }

}
