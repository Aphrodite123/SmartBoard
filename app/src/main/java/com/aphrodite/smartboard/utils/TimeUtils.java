package com.aphrodite.smartboard.utils;

import android.text.TextUtils;

import com.aphrodite.framework.utils.ObjectUtils;
import com.aphrodite.smartboard.R;
import com.aphrodite.smartboard.application.MainApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @Author szh
 * @Date 2019-09-25
 * @Description
 */
public class TimeUtils {
    /**
     * 年月日格式
     */
    public static final String FORMAT_CHINESE_ONE = "yyyy年MM月dd日";
    public static final String FORMAT_CHINESE_TWO = "MM月dd日";
    /**
     * 时钟格式
     */
    public static final String FORMAT_CLOCK_ONE = "HH:mm";

    /**
     * 时间戳转日期格式
     *
     * @param milSecond 毫秒
     * @param pattern
     * @return
     */
    public static String msToDateFormat(long milSecond, String pattern) {
        if (milSecond < 0) {
            return "";
        }

        Date date = new Date(milSecond);
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * 时间的显示规则
     * 当时间为今天时，显示为『今天』
     * 当时间为昨天时，显示为『昨天』
     * 当时间超出了昨天，显示为『月日』，如『7月28日』
     * 当时间为非今年时，显示为『年月日 时:分』，如『17年12月28日』
     *
     * @param ms 毫秒
     * @return
     */
    public static String msToDateFormat(long ms, String yearFormat, String monthFormat) {
        if (ms <= 0 || TextUtils.isEmpty(yearFormat) || TextUtils.isEmpty(monthFormat)) {
            return "";
        }

        Date date = new Date(ms);
        if (ObjectUtils.isEmpty(date)) {
            return "";
        }

        Calendar curCalendar = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(yearFormat);
        SimpleDateFormat beforeYesDate = new SimpleDateFormat(monthFormat);
        StringBuffer buffer = new StringBuffer();
        if (curCalendar.get(Calendar.YEAR) != calendar.get(Calendar.YEAR)) {
            //跨年,显示为『年月日』，如『17年12月28日』
            return simpleDateFormat.format(date);
        }

        int poor = Math.abs(curCalendar.get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR));
        if (poor >= 2) {
            //昨天以前，显示为『月日』，如『7月28日』
            return beforeYesDate.format(date);
        }

        if (poor >= 1) {
            //昨天，显示为『昨天』
            buffer.append(MainApplication.getApplication().getResources().getString(R.string.yesterday));
            return buffer.toString();
        }

        if (poor >= 0) {
            //今天，显示为『今天 时:分』
            buffer.append(MainApplication.getApplication().getResources().getString(R.string.today));
            return buffer.toString();
        }
        return "";
    }

    /**
     * 秒->01:05
     *
     * @param mRecordSeconds
     * @return
     */
    public static String convertTime(int mRecordSeconds) {
        int minute = 0, second = 0;
        if (mRecordSeconds >= 60) {
            minute = mRecordSeconds / 60;
            second = mRecordSeconds % 60;
        } else {
            second = mRecordSeconds;
        }
        String timeTip = "0" + minute + ":" + (second < 10 ? "0" + second : second + "");
        return timeTip;
    }

    /**
     * 01:05->秒
     *
     * @param timeTip
     * @return
     */
    public static int convertTime(String timeTip) {
        String[] split = timeTip.split("\\:");
        int minutes = Integer.valueOf(split[0]);
        int seconds = Integer.valueOf(split[1].substring(0, 2));
        return minutes * 60 + seconds;
    }

    public static boolean isSameDay(long secOne, long secTwo, TimeZone timeZone) {
        return Math.abs(secOne - secTwo) < 24 * 60 * 60 && secToAbsoluteDays(secOne, timeZone) == secToAbsoluteDays(secTwo, timeZone);
    }

    private static long secToAbsoluteDays(long sec, TimeZone timeZone) {
        return (1000 * sec + timeZone.getOffset(1000 * sec)) / (24 * 60 * 60 * 1000);
    }

}
