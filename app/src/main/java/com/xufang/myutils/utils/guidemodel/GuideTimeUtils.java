package com.xufang.myutils.utils.guidemodel;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by xufang on 2017/10/9.
 */

public class GuideTimeUtils {
    private static final String TAG = "GuideTimeUtils";

    public static boolean hasExpire(long lastTimeMillis, int expireTime, TimeUnit expireTimeUnit) {
        boolean ret = false;
        switch (expireTimeUnit) {
            case DAYS:
                ret = exceedTheIntervalDay(lastTimeMillis, expireTime);
                break;
            case HOURS:
                break;
            default:
                break;
        }

        return ret;
    }

    private static boolean exceedTheIntervalDay(long lastTimeMillis, int intervalDays) {
        //day of lastTimeMillis
        Calendar calendarYesterday = Calendar.getInstance();
        calendarYesterday.setTimeInMillis(lastTimeMillis);
        calendarYesterday.add(Calendar.DAY_OF_YEAR, intervalDays);
        int yearLastDay = calendarYesterday.get(Calendar.YEAR);
        int dayLastDay = calendarYesterday.get(Calendar.DAY_OF_YEAR);

        //today
        Calendar calendarToday = Calendar.getInstance();
        long nowMillis = System.currentTimeMillis();
        calendarToday.setTimeInMillis(nowMillis);
        int yearToday = calendarToday.get(Calendar.YEAR);
        int dayToday = calendarToday.get(Calendar.DAY_OF_YEAR);

        Logger.info(TAG, "exceedTheIntervalDay, lastTimeMillis:%d, nowMillis:%d, yearLastDay:%d, yearToday:%d, dayLastDay:%d, dayToday:%d",
                lastTimeMillis, nowMillis, yearLastDay, yearToday, dayLastDay, dayToday);

        return (yearLastDay == yearToday && dayLastDay <= dayToday) || yearLastDay < yearToday;
    }
}
