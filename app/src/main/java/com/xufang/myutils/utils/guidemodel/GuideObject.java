package com.xufang.myutils.utils.guidemodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by xufang on 2017/9/23.
 */

public class GuideObject {
    private static final String TAG = "GuideObject";

    private static final String KEY_EXTRA_CAN_TRIGGER_SHOW = "CAN_SHOW";
    private static final String KEY_EXTRA_LAST_SHOW_GUIDE_TIME = "LAST_SHOW_TIME";

    String mGuideTag;
    GuideCallback mGuideCallback;
    boolean mHasShown, mUseFileCacheOfShowTrigger, mUseFileCacheOfNoneShowTrigger, mCacheLastShowTimeInFile;
    private Map<String, GuideTriggerEvent> mTriggerEventMap;
    private CacheManager mCacheManager;
    private boolean mCanTriggerShow;
    int mExpireTime;
    TimeUnit mExpireTimeUnit;
    private long mLastShowGuideMillis;
    private boolean mLastShowExpire;
    private boolean mHasResetTriggerTimes;

    private StringBuffer mTmpBuffer;

    public GuideObject() {
        mHasShown = false;
        mUseFileCacheOfShowTrigger = false;
        mCacheLastShowTimeInFile = true;
        mUseFileCacheOfNoneShowTrigger = true;
        mExpireTime = 1;
        mExpireTimeUnit = TimeUnit.DAYS;
        mLastShowGuideMillis = 0;
        mHasResetTriggerTimes = false;
        mCanTriggerShow = true;
        mTriggerEventMap = new HashMap<>();
        mTmpBuffer = new StringBuffer();
    }

    public void setCacheManager(CacheManager cacheManager) {
        mCacheManager = cacheManager;
        mLastShowGuideMillis = getLastShowGuideMillis();
        mCanTriggerShow = getCanTriggerShow();
    }

    public void reset(String eventTag) {
        mHasShown = false;
        GuideTriggerEvent guideTriggerEvent = mTriggerEventMap.get(eventTag);
        if (guideTriggerEvent == null) {
            return;
        }

        guideTriggerEvent.setHasTriggerTimes(0);
        if (mUseFileCacheOfShowTrigger && guideTriggerEvent.getTriggerEventType() == GuideTriggerEvent.Type.TYPE_SHOW_TRIGGER_EVENT
                || mUseFileCacheOfNoneShowTrigger && guideTriggerEvent.getTriggerEventType() == GuideTriggerEvent.Type.TYPE_NONE_SHOW_TRIGGER_EVENT) {
            String key = getCacheKey(guideTriggerEvent);
            if (key != null && mCacheManager != null) {
                mCacheManager.cacheInteger(key, 0);
            }
        }
    }

    public void setNoneShowExpireTime(int expireTime, TimeUnit expireTimeUnit) {
        mExpireTime = expireTime;
        mExpireTimeUnit = expireTimeUnit;
    }

    public GuideTriggerEvent getTriggerEventByTag(String eventTag) {
        GuideTriggerEvent triggerEvent = mTriggerEventMap.get(eventTag);
        if (triggerEvent == null) {
            triggerEvent = new GuideTriggerEvent();
            triggerEvent.setEventTag(eventTag);
            mTriggerEventMap.put(eventTag, triggerEvent);
        }
        return triggerEvent;
    }

    public void writeToFileCache() {
        if (mCacheManager == null) {
            return;
        }

        Set<Map.Entry<String, GuideTriggerEvent>> entrySet = mTriggerEventMap.entrySet();
        for (Map.Entry<String, GuideTriggerEvent> entry : entrySet) {
            GuideTriggerEvent triggerEvent = entry.getValue();
            Logger.info(TAG, "writeToFileCache GuideTriggerEvent---->%s", triggerEvent);
            GuideTriggerEvent.Type type = triggerEvent.getTriggerEventType();
            if ((mUseFileCacheOfShowTrigger && GuideTriggerEvent.Type.TYPE_SHOW_TRIGGER_EVENT == type)
                    || (mUseFileCacheOfNoneShowTrigger && GuideTriggerEvent.Type.TYPE_NONE_SHOW_TRIGGER_EVENT == type)) {
                String key = getCacheKey(triggerEvent);
                Logger.debug(TAG, "writeToFileCache cache key---->%s", key);
                if (key != null) {
                    mCacheManager.cacheInteger(key, triggerEvent.getHasTriggerTimes());
                }
            }
        }
    }

    public boolean triggerShow(String eventTag) {
        GuideTriggerEvent showTrigger = getTriggerEventByTag(eventTag);
        showTrigger.setTriggerEventType(GuideTriggerEvent.Type.TYPE_SHOW_TRIGGER_EVENT);
        if (mUseFileCacheOfShowTrigger && !showTrigger.hasSync()) {
            String key = getCacheKey(showTrigger);
            Logger.debug(TAG, "triggerShow cache key---->%s", key);
            if (key != null && mCacheManager != null) {
                showTrigger.setHasTriggerTimes(mCacheManager.getInteger(key, 0));
                showTrigger.flagSycn();
            }
        }

        resetTriggerTimesIfExpire(showTrigger);
        if (!mCanTriggerShow) {
            return false;
        }

        boolean willShow = showTrigger.trigger();
        Logger.info(TAG, "triggerShow mGuideTag:%s, willShow:%b", mGuideTag, willShow);
        if (willShow) {
            mHasResetTriggerTimes = false;
            setLastShowGuideMillis();
        }
        return willShow;
    }

    private void resetTriggerTimesIfExpire(GuideTriggerEvent triggerEvent) {
        if (mHasResetTriggerTimes || !mCacheLastShowTimeInFile) {
            return;
        }

        if (hasLastShowExpire()) {
            Logger.info(TAG, "triggerShow hasLastShowExpire");
            mLastShowExpire = true;
            mHasResetTriggerTimes = true;
            triggerEvent.setHasTriggerTimes(0);
            setCanTriggerShow(true);
        }
    }

    public void triggerNoneShow(String eventTag) {
        Logger.info(TAG, "triggerNoneShow guideTag---->%s, eventTag---->%s, mCanTriggerShow---->%b", mGuideTag, eventTag, mCanTriggerShow);
        GuideTriggerEvent noneShowTrigger = getTriggerEventByTag(eventTag);
        noneShowTrigger.setTriggerEventType(GuideTriggerEvent.Type.TYPE_NONE_SHOW_TRIGGER_EVENT);
        if (mUseFileCacheOfNoneShowTrigger && !noneShowTrigger.hasSync()) {
            String key = getCacheKey(noneShowTrigger);
            Logger.debug(TAG, "triggerNoneShow cache key---->%s", key);
            if (key != null && mCacheManager != null) {
                noneShowTrigger.setHasTriggerTimes(mCacheManager.getInteger(key, 0));
                noneShowTrigger.flagSycn();
            }
        }

        if (mLastShowExpire) {
            Logger.info(TAG, "triggerNoneShow hasLastShowExpire");
            mLastShowExpire = false;
            noneShowTrigger.setHasTriggerTimes(0);
        }

        setCanTriggerShow(!noneShowTrigger.trigger());
        Logger.info(TAG, "triggerNoneShow mCanTriggerShow---->%b", mCanTriggerShow);
    }

    private String getCacheKey(GuideTriggerEvent triggerEvent) {
        if (triggerEvent != null) {
            mTmpBuffer.delete(0, mTmpBuffer.length());
            return mTmpBuffer.append(mGuideTag).append(triggerEvent.getTriggerEventType().ordinal()).append(triggerEvent.getEventTag()).toString();
        }
        return null;
    }

    private void setLastShowGuideMillis() {
        mLastShowGuideMillis = System.currentTimeMillis();
        if (mCacheManager != null && mCacheLastShowTimeInFile) {
            Logger.debug(TAG, "setLastShowGuideMillis guideTag:%s, mLastShowGuideMillis:%d", mGuideTag, mLastShowGuideMillis);
            mCacheManager.cacheLong(getLastShowGuideTimeKey(), mLastShowGuideMillis);
        }
    }

    private long getLastShowGuideMillis() {
        long lastShowGuideMills = 0;
        if (mCacheManager != null) {
            lastShowGuideMills = mCacheManager.getLong(getLastShowGuideTimeKey(), 0);
            Logger.debug(TAG, "getLastShowGuideMillis guideTag:%s, lastShowGuideMills:%d, mLastShowGuideMillis:%d", mGuideTag, lastShowGuideMills, mLastShowGuideMillis);
        }
        return lastShowGuideMills;
    }

    private String getLastShowGuideTimeKey() {
        return mGuideTag + KEY_EXTRA_LAST_SHOW_GUIDE_TIME;
    }

    private void setCanTriggerShow(boolean canTriggerShow) {
        mCanTriggerShow = canTriggerShow;
        if (mCacheManager != null && mUseFileCacheOfNoneShowTrigger) {
            mCacheManager.cacheBoolean(getCanTriggerShowKey(), canTriggerShow);
        }
    }

    private boolean getCanTriggerShow() {
        boolean canTriggerShow = true;
        if (mCacheManager != null) {
            canTriggerShow = mCacheManager.getBoolean(getCanTriggerShowKey(), true);
        }

        return canTriggerShow;
    }

    private String getCanTriggerShowKey() {
        return mGuideTag + KEY_EXTRA_CAN_TRIGGER_SHOW;
    }

    private boolean hasLastShowExpire() {
        return GuideTimeUtils.hasExpire(mLastShowGuideMillis, mExpireTime, mExpireTimeUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GuideObject that = (GuideObject) o;

        return mGuideTag != null ? mGuideTag.equals(that.mGuideTag) : that.mGuideTag == null;

    }

    @Override
    public int hashCode() {
        return mGuideTag != null ? mGuideTag.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "GuideObject{" +
                "mGuideTag=" + mGuideTag +
                ", mHasShown='" + mHasShown + '\'' +
                ", mUseFileCacheOfShowTrigger='" + mUseFileCacheOfShowTrigger + '\'' +
                ", mUseFileCacheOfNoneShowTrigger='" + mUseFileCacheOfNoneShowTrigger + '\'' +
                ", mTriggerEventMap='" + mTriggerEventMap + '\'' +
                '}';
    }
}
