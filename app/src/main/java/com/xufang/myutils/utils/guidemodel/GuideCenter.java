package com.xufang.myutils.utils.guidemodel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by xufang on 2017/9/23.
 * 一个管理引导的工具类，适用于多次触发事件显示引导的情景。可自定义GuideTag(引导标志),EventTag(触发引导的事件标识).
 * 若达到触发次数，则回调GuideCallback的void showGuide(String guideTag)方法
 */

public class GuideCenter {
    private static final String TAG = "GuideCenter";

    private static final int DEFAULT_TRIGGER_TIMES = 1;

    private Map<String, GuideObject> mGuideObjectMap;

    private static class Holder {
        private static final GuideCenter INSTANCE = new GuideCenter();
    }

    private GuideCenter() {
        mGuideObjectMap = new HashMap<>();
    }

    public static GuideCenter getInstance() {
        return Holder.INSTANCE;
    }

    public void setNeedFileCache(String guideTag, boolean useFileCacheOfShowTrigger, boolean useFileCacheOfNoneShowTrigger, boolean cacheLastShowTimeInFile) {
        GuideObject guideObject = mGuideObjectMap.get(guideTag);
        if (guideObject == null) {
            guideObject = initGuideObject(guideTag);
        }
        guideObject.mUseFileCacheOfShowTrigger = useFileCacheOfShowTrigger;
        guideObject.mUseFileCacheOfNoneShowTrigger = useFileCacheOfNoneShowTrigger;
        guideObject.mCacheLastShowTimeInFile = cacheLastShowTimeInFile;
    }

    public void setNoneShowExpireTime(String guideTag, int expireTime, TimeUnit expireTimeUnit) {
        GuideObject guideObject = mGuideObjectMap.get(guideTag);
        if (guideObject == null) {
            guideObject = initGuideObject(guideTag);
        }
        guideObject.mExpireTime = expireTime;
        guideObject.mExpireTimeUnit = expireTimeUnit;
    }

    public void registerGuideCallback(String guideTag, GuideCallback callback) {
        Logger.info(TAG, "registerGuideCallback guideTag---->%s", guideTag);
        GuideObject guideObject = mGuideObjectMap.get(guideTag);
        if (guideObject == null) {
            guideObject = initGuideObject(guideTag);
        }
        guideObject.mGuideCallback = callback;
    }

    public void unRegisterGuideCallback(String guideTag) {
        Logger.info(TAG, "unRegisterGuideCallback guideTag---->%s", guideTag);
        GuideObject guideObject = mGuideObjectMap.get(guideTag);
        if (guideObject != null) {
            guideObject.mGuideCallback = null;
            guideObject.writeToFileCache();
        }
    }

    public void triggerShow(String guideTag) {
        triggerShow(guideTag, GuideTriggerEvent.DEFAULT_TRIGGER_SHOW_EVENT_TAG);
    }

    public void triggerShow(String guideTag, int triggerTimes) {
        triggerShow(guideTag, GuideTriggerEvent.DEFAULT_TRIGGER_SHOW_EVENT_TAG, triggerTimes);
    }

    public void triggerShow(String guideTag, String eventTag) {
        triggerShow(guideTag, eventTag, DEFAULT_TRIGGER_TIMES);
    }

    public void triggerShow(String guideTag, String eventTag, int triggerTimes) {
        GuideObject guideObject = mGuideObjectMap.get(guideTag);
        if (guideObject == null) {
            guideObject = initGuideObject(guideTag);
        }
        GuideTriggerEvent guideTriggerEvent = guideObject.getTriggerEventByTag(eventTag);
        guideTriggerEvent.setTriggerTimes(triggerTimes);
        Logger.info(TAG, "triggerShow guideTag---->%s, eventTag---->%s, triggerTimes---->%d, hasTriggerTimes---->%d", guideTag, eventTag, triggerTimes, guideTriggerEvent.getHasTriggerTimes());

        if (guideObject.triggerShow(eventTag) && guideObject.mGuideCallback != null) {
            guideObject.mGuideCallback.showGuide(guideObject.mGuideTag);
            guideObject.mHasShown = true;
        }
    }

    public void interceptTriggerSequenceEvent(String guideTag) {
        interceptTriggerSequenceEvent(guideTag, GuideTriggerEvent.DEFAULT_TRIGGER_SHOW_EVENT_TAG);
    }

    public void interceptTriggerSequenceEvent(String guideTag, String eventTag) {
        interceptTriggerSequenceEvent(guideTag, eventTag, false);
    }

    public void interceptTriggerSequenceEvent(String guideTag, boolean clearTriggerTimes) {
        interceptTriggerSequenceEvent(guideTag, GuideTriggerEvent.DEFAULT_TRIGGER_SHOW_EVENT_TAG, clearTriggerTimes);
    }

    public void interceptTriggerSequenceEvent(String guideTag, String eventTag, boolean clearTriggerTimes) {
        GuideObject guideObject = mGuideObjectMap.get(guideTag);
        Logger.info(TAG, "clearTriggerTimes:%b, interceptTriggerSequenceEvent guideObject---->%s",
                clearTriggerTimes, guideObject);
        if (guideObject == null) {
            return;
        }

        if (clearTriggerTimes) {
            guideObject.reset(eventTag);
        } else {
            GuideTriggerEvent triggerEvent = guideObject.getTriggerEventByTag(eventTag);
            int triggerTimes = triggerEvent.getTriggerTimes();
            triggerEvent.setHasTriggerTimes(triggerTimes);
        }
    }

    public void triggerNoneShow(String guideTag) {
        triggerNoneShow(guideTag, GuideTriggerEvent.DEFAULT_TRIGGER_NONE_SHOW_EVENT_TAG);
    }

    public void triggerNoneShow(String guideTag, int triggerTimes) {
        triggerNoneShow(guideTag, GuideTriggerEvent.DEFAULT_TRIGGER_NONE_SHOW_EVENT_TAG, triggerTimes);
    }

    public void triggerNoneShow(String guideTag, String eventTag) {
        triggerNoneShow(guideTag, eventTag, DEFAULT_TRIGGER_TIMES);
    }

    public void triggerNoneShow(String guideTag, String eventTag, int triggerTimes) {
        Logger.info(TAG, "triggerNoneShow guideTag---->%s, eventTag---->%s, triggerTimes---->%d", guideTag, eventTag, triggerTimes);
        GuideObject guideObject = mGuideObjectMap.get(guideTag);
        if (guideObject == null) {
            return;
        }

        guideObject.getTriggerEventByTag(eventTag).setTriggerTimes(triggerTimes);
        guideObject.triggerNoneShow(eventTag);
    }

    private GuideObject initGuideObject(String guideTag) {
        GuideObject guideObject = new GuideObject();
        guideObject.mGuideTag = guideTag;
        guideObject.mHasShown = false;
        guideObject.setCacheManager(new SharePrefCacheManager());
        mGuideObjectMap.put(guideTag, guideObject);
        return guideObject;
    }
}
