package com.kcrason.kselectimages.utils;

import android.content.Context;

import com.kcrason.kselectimages.KApplication;

import java.lang.reflect.Field;

/**
 * Created by KCrason on 2016/6/7.
 */
public class DisPlayUtils {

    private static Context context = KApplication.getContext();

    /**
     * 获取屏幕宽度
     */
    public static int getScrrenWidthPixels() {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     *
     */
    public static float getDisplayDensity() {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 获取屏幕高度
     */
    public static int getScrrenHeightPixel() {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * dip转px
     *
     * @param dipValue
     * @return
     */

    public static int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * px转dip
     *
     * @param pxValue
     * @return
     */

    public static int px2dip(float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int sp2px(float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */

    public static int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
}
