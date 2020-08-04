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
import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.utils.CWFileUtils;

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

    private static class PathDraw {
        public Paint paint;
        public Path path;

        public void draw(Canvas canvas) {
            if (null != canvas) {
                canvas.drawPath(path, paint);
            }
        }
    }
}
