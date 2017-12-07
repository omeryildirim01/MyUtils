package com.xufang.myutils.utils;

import android.app.Activity;
import android.os.Build;

/**
 * Created by xufang on 2017/9/16.
 */

public class ActivityCheckUtils {

    public static boolean checkActivityValid(Activity activity) {
        if (activity == null) {
            //MLog.warn(this, "Fragment " + this + " not attached to Activity");
            return false;
        }

        if (activity.isFinishing()) {
            //MLog.warn(this, "activity is finishing");
            return false;
        }

        if (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed()) {
            //MLog.warn(this, "activity is isDestroyed");
            return false;
        }

        return true;
    }
}
