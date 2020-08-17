package com.aphrodite.smartboard.view.widget.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.apeman.sdk.bean.DevicePoint;
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.model.bean.CW;
import com.aphrodite.smartboard.model.bean.CWACT;
import com.aphrodite.smartboard.model.bean.CWLine;
import com.aphrodite.smartboard.utils.CWFileUtils;
import com.aphrodite.smartboard.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Aphrodite on 2020/7/30.
 * 自定义画板适配智能手写板,支持涂鸦&过程回放 https://www.jianshu.com/p/548d2799fd6e
 */
public class CustomDrawView extends View {
    private Context mContext;

    private static int POINT_COUNT = 5;
    private List<PathDraw> mCachePaths;
    private List<PathDraw> mRemovePaths;
    private Paint mPaint;
    private Path mPath;
    private int mode;
    private int mLineWidth;
    private int mLineColor;
    private int mEraserWidth;
    private int mEraserColor;

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
        if (!mCanDraw) {
            return super.onTouchEvent(event);
        }

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

                if (null == mPoints) {
                    mPoints = new ArrayList<>();
                } else {
                    mPoints.clear();
                }
                addPoint(mLastX, mLastY, mPressure);
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(mLastX, mLastY, (mLastX + x) / 2, (mLastY + y) / 2);
                if (null == mBufferBitmap) {
                    initBuffer();
                }
                mBufferCanvas.drawPath(mPath, mPaint);
                invalidate();
                mLastX = x;
                mLastY = y;
                addPoint(mLastX, mLastY, mPressure);
                break;
            case MotionEvent.ACTION_UP:
                mLastX = x;
                mLastY = y;
                if (null == mCachePaths) {
                    mCachePaths = new ArrayList<>();
                }
                PathDraw pathDraw = new PathDraw();
                pathDraw.paint = mPaint;
                pathDraw.path = mPath;
                mCachePaths.add(pathDraw);

                addPoint(mLastX, mLastY, mPressure);
                savePath();
                mPath.reset();
                break;
        }
        return true;
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

    private void initBuffer() {
        mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mBufferCanvas = new Canvas(mBufferBitmap);
    }

    private void addPoint(float x, float y, float p) {
        DevicePoint point = new DevicePoint();
        point.setX((int) x);
        point.setY((int) y);
        point.setPressure((int) p);
        mPoints.add(point);
    }

    private void savePath() {
        if (ObjectUtils.isEmpty(mPoints)) {
            return;
        }
        int red = (mPaint.getColor() & 0xff0000) >> 16;
        int green = (mPaint.getColor() & 0x00ff00) >> 8;
        int blue = (mPaint.getColor() & 0x0000ff);
        CWFileUtils.writeLine(mPoints, (int) mPaint.getStrokeWidth(), red + "," + green + "," + blue + ",1");
    }

    //重新绘制
    private void reDraw() {
        if (ObjectUtils.isEmpty(mCachePaths)) {
            return;
        }

        mBufferBitmap.eraseColor(Color.TRANSPARENT);
        for (PathDraw pathDraw : mCachePaths) {
            if (null == pathDraw) {
                continue;
            }
            pathDraw.draw(mBufferCanvas);
        }
        invalidate();
    }

    //撤销
    public void undo() {
        if (ObjectUtils.isEmpty(mCachePaths)) {
            return;
        }

        PathDraw pathDraw = mCachePaths.remove(mCachePaths.size() - 1);
        if (null == mRemovePaths) {
            mRemovePaths = new ArrayList<>();
        }
        mRemovePaths.add(pathDraw);
        reDraw();
    }

    //反撤销
    public void redo() {
        if (ObjectUtils.isEmpty(mRemovePaths)) {
            return;
        }
        PathDraw pathDraw = mRemovePaths.remove(mRemovePaths.size() - 1);
        if (null == mCachePaths) {
            mCachePaths = new ArrayList<>();
        }
        mCachePaths.add(pathDraw);
        reDraw();
    }

    //清空
    public void clear() {
        if (null != mCachePaths) {
            mCachePaths.clear();
        }

        if (null != mRemovePaths) {
            mRemovePaths.clear();
        }

        if (null != mBufferBitmap) {
            mBufferBitmap.eraseColor(Color.TRANSPARENT);
        }
        invalidate();
    }

    //销毁
    public void destory() {
        if (null != mBufferBitmap) {
            mBufferBitmap.recycle();
            mBufferBitmap = null;
        }

        if (null != mBufferCanvas) {
            mBufferCanvas = null;
        }

        if (null != mCachePaths) {
            mCachePaths.clear();
            mCachePaths = null;
        }

        if (null != mRemovePaths) {
            mRemovePaths.clear();
            mRemovePaths = null;
        }

        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

    }

    //回放
    public void replay(CW cw, int delay, double xScale, double yScale) {
        if (null == cw) {
            return;
        }
        List<CWACT> cwacts = cw.getACT();
        if (ObjectUtils.isEmpty(cwacts)) {
            return;
        }

        for (CWACT cwact : cwacts) {
            if (null == cwact) {
                continue;
            }
            createPaths(cwact.getLine(), delay, xScale, yScale);
        }
    }

    private void createPaths(CWLine line, int delay, double xScale, double yScale) {
        if (null == line) {
            return;
        }
        if (null == mBufferBitmap) {
            initBuffer();
        }
        List<List<Integer>> points = line.getPoints();
        if (ObjectUtils.isEmpty(points)) {
            return;
        }
        LogUtils.d("Enter to createPaths. " + points.size());
        String[] split = line.getColor().split("\\,");
        int color = Color.rgb(Integer.valueOf(split[0]), Integer.valueOf(split[1]), Integer.valueOf(split[2]));
        int width = line.getWidth();
        setLineColor(color);
        setLineWidth(width);
        initPaint();

        List<Integer> xyPoints;
        for (int i = 0; i < points.size(); i++) {
            xyPoints = points.get(i);
            if (ObjectUtils.isOutOfBounds(xyPoints, 2) || xyPoints.get(2) <= 0) {
                continue;
            }
            if (0 == i) {
                mBufferCanvas.drawPoint((float) (xyPoints.get(0) * xScale), (float) (xyPoints.get(1) * yScale), mPaint);
                invalidate();
            } else {
                drawPath(mLastX, mLastY, (float) (xyPoints.get(0) * xScale), (float) (xyPoints.get(1) * yScale));
            }
            mLastX = (float) (xyPoints.get(0) * xScale);
            mLastY = (float) (xyPoints.get(1) * yScale);
        }
    }

    private void drawPath(float startX, float startY, float endX, float endY) {
        float x = endX - startX;
        float y = endY - startY;
        //10倍插点
        int insertCount = (int) (Math.max(Math.abs(x), Math.abs(y)) + 2);
        //S.i("补点：$insertCount")
        float dx = x / insertCount;
        float dy = y / insertCount;
        for (int i = 0; i < insertCount; i++) {
            float insertX = startX + i * dx;
            float insertY = startY + i * dy;
            mBufferCanvas.drawPoint(insertX, insertY, mPaint);
            invalidate();
        }
    }

    public void setMode(int mode) {
        this.mode = mode;
        initPaint();
    }

    public void setCanDraw(boolean canDraw) {
        this.mCanDraw = canDraw;
    }

    public void setLineWidth(int lineWidth) {
        this.mLineWidth = lineWidth;
        initPaint();
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        initPaint();
    }

    public void setEraserWidth(int eraserWidth) {
        this.mEraserWidth = eraserWidth;
        initPaint();
    }

    public void setEraserColor(int eraserColor) {
        this.mEraserColor = eraserColor;
        initPaint();
    }

    public interface Mode {
        int BASE = 0x00;

        //绘制
        int DRAW = BASE + 1;

        //擦除
        int ERASER = BASE + 2;
    }

    private interface WhatType {
        int BASE = 0x00;
        int WHAT_01 = BASE + 1;
        int WHAT_02 = BASE + 1;
    }

    private static class PathDraw {
        public Paint paint;
        public Path path;

        public void draw(Canvas canvas) {
            if (null != canvas) {
                canvas.drawPath(path, paint);
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle = msg.getData();
            if (null == bundle) {
                return;
            }
            float x = bundle.getFloat(String.valueOf(WhatType.WHAT_01));
            float y = bundle.getFloat(String.valueOf(WhatType.WHAT_02));
            LogUtils.d("Enter to handleMessage. (" + x + " , " + y + ")");
            if (null != mBufferCanvas) {
                mBufferCanvas.drawPoint(x, y, mPaint);
                invalidate();
            }
        }
    };
}
