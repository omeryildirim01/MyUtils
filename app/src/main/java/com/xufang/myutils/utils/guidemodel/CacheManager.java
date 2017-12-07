package com.xufang.myutils.utils.guidemodel;

/**
 * Created by xufang on 2017/9/23.
 */

public interface CacheManager {
    void cacheBoolean(String key, boolean value);

    void cacheInteger(String key, int value);

    void cacheLong(String key, long value);

    boolean getBoolean(String key, boolean defaultVal);

    int getInteger(String key, int defaultVal);

    long getLong(String key, long defaultVal);
}
