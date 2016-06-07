package com.kcrason.kselectimages.utils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by KCrason on 2016/6/7.
 */
public class SnackBarUtils {

    //默认short
    public static void showSnackBar(View parentView, String msg) {
        Snackbar.make(parentView, msg, Snackbar.LENGTH_SHORT).show();
    }
}
