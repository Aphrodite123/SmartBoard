package com.aphrodite.smartboard.view.widget.viewpager;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Aphrodite on 2019/2/15.
 * 可配置viewpager是否左右滑动
 */
public class ConfigureSlideViewPager extends ViewPager {
    private boolean mIsSlide = false;

    public ConfigureSlideViewPager(@NonNull Context context) {
        super(context);
    }

    public ConfigureSlideViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * return false; 可行,不拦截事件,
     * return true; 不行,孩子无法处理事件
     * return super.onInterceptTouchEvent(ev); 不行,会有细微移动
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mIsSlide) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }

    /**
     * 是否消费事件
     * 消费:事件就结束
     * 不消费:往父控件传
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsSlide) {
            return super.onTouchEvent(ev);
        } else {
            return true;
        }
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        //false:去除页面切换时的滑动翻页效果
        super.setCurrentItem(item, false);
    }

    public void setIsSlide(boolean isSlide) {
        this.mIsSlide = isSlide;
    }

}
