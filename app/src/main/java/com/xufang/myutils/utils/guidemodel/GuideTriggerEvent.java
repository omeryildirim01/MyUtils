package com.xufang.myutils.utils.guidemodel;

/**
 * Created by xufang on 2017/9/23.
 */

public class GuideTriggerEvent {
    public static final String DEFAULT_TRIGGER_SHOW_EVENT_TAG = "DF_TG_SW_TAG";
    public static final String DEFAULT_TRIGGER_NONE_SHOW_EVENT_TAG = "DF_TG_NONE_SW_TAG";

    private String mEventTag;
    private int mTriggerTimes;
    private int mHasTriggerTimes;
    private boolean mHasSyncFromFileCache;
    private Type mType;

    enum Type {
        TYPE_SHOW_TRIGGER_EVENT,
        TYPE_NONE_SHOW_TRIGGER_EVENT
    }

    public GuideTriggerEvent() {
        mHasTriggerTimes = 0;
        mTriggerTimes = 1;
        mHasSyncFromFileCache = false;
        mType = Type.TYPE_SHOW_TRIGGER_EVENT;
    }

    public void setTriggerTimes(int triggerTimes) {
        mTriggerTimes = triggerTimes;
    }

    public Type getTriggerEventType() {
        return mType;
    }

    public void setTriggerEventType(Type type) {
        mType = type;
    }

    public void setEventTag(String eventTag) {
        mEventTag = eventTag;
    }

    public String getEventTag() {
        return mEventTag;
    }

    public void flagSycn() {
        mHasSyncFromFileCache = true;
    }

    public boolean hasSync() {
        return mHasSyncFromFileCache;
    }

    public boolean trigger() {
        mHasTriggerTimes++;
        return mHasTriggerTimes == mTriggerTimes;
    }

    public int getHasTriggerTimes() {
        if (mHasTriggerTimes > mTriggerTimes) {
            mHasTriggerTimes = mTriggerTimes;
        }
        return mHasTriggerTimes;
    }

    public void setHasTriggerTimes(int hasTriggerTimes) {
        if (hasTriggerTimes > mTriggerTimes) {
            mHasTriggerTimes = 0;
            return;
        }
        mHasTriggerTimes = hasTriggerTimes;
    }

    public int getTriggerTimes() {
        return mTriggerTimes;
    }

    @Override
    public String toString() {
        return "GuideTriggerEvent{" +
                "mEventTag=" + mEventTag +
                ", mTriggerTimes=" + mTriggerTimes +
                ", mHasTriggerTimes=" + mHasTriggerTimes +
                ", mHasSyncFromFileCache=" + mHasSyncFromFileCache +
                ", mType='" + mType.name() + '\'' +
                '}';
    }
}
