package com.yus.videorecording.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by yufs on 2017/7/6.
 */

public class CommonMethods {
    /**
     * 获取屏幕宽
     * @param context
     * @return
     */
    public static int getWindowSizeW(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }
}
