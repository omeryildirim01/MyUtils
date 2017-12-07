package com.xufang.myutils.utils.guidemodel;

/**
 * Created by xufang on 2017/9/23.
 */

public class SharePrefCacheManager implements CacheManager {
    @Override
    public void cacheBoolean(String key, boolean value) {

    }

    @Override
    public void cacheInteger(String key, int value) {

    }

    @Override
    public void cacheLong(String key, long value) {

    }

    @Override
    public boolean getBoolean(String key, boolean defaultVal) {
        return true;
    }

    @Override
    public int getInteger(String key, int defaultVal) {
        return 0;
    }

    @Override
    public long getLong(String key, long defaultVal) {
        return 0;
    }
}
