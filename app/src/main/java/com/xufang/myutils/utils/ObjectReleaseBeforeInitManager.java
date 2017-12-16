package com.xufang.myutils.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xufang on 2017/9/18.
 * 当创建新对象时，保证新对象的onInit在旧对象的onRelease之后调用。onInit,onRelease由业务实现
 * 注：不支持多线程！！！
 * <p>
 * 栗子：当Activity持有一个单例时，如果新Activity的onCreate在旧Activity的onDestroy之前调用，
 * 而这个单例在onCreate中初始化并已开始使用，那么旧Activity调用onDestroy时会reset这个单例，就可能会出问题。
 */

public final class ObjectReleaseBeforeInitManager {
    private static final String TAG = "ObjectReleaseBeforeInitManager";

    private Map<String, ReleaseBeforeInitObject> mObjectMap;

    private ObjectReleaseBeforeInitManager() {
        mObjectMap = new HashMap<>();
    }

    public static ObjectReleaseBeforeInitManager getInstance() {
        return Holder.INSTANCE;
    }

    public void callInit(ReleaseBeforeInitObject target) {
        if (target == null || target.getType() == null) {
            return;
        }

        String type = target.getType();

        ReleaseBeforeInitObject releaseBeforeInitObject = mObjectMap.get(type);
        if (releaseBeforeInitObject != null) {
            releaseBeforeInitObject.onRelease();
        }
        mObjectMap.put(type, target);
        target.onInit();
    }

    public void callRelease(ReleaseBeforeInitObject target) {
        if (target == null || target.getType() == null) {
            return;
        }

        String type = target.getType();

        ReleaseBeforeInitObject releaseBeforeInitObject = mObjectMap.get(type);
        if (releaseBeforeInitObject != null) {
            releaseBeforeInitObject.onRelease();
            mObjectMap.remove(type);
        }
    }

    public interface ReleaseBeforeInitObject {
        void onInit();

        void onRelease();

        String getType();
    }

    private static class Holder {
        private static final ObjectReleaseBeforeInitManager INSTANCE = new ObjectReleaseBeforeInitManager();
    }
}
