package com.kcrason.kselectimages.utils;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.kcrason.kselectimages.KApplication;

/**
 * Created by KCrason on 2016/6/7.
 */
public class KUtils {

    /**
     * 收起软键盘
     */
    public static void putAwaySoftKeyboard(Context context, View view) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * 判断是否有网络连接
     */
    public static boolean isNetworkAvailable() {
        Context context = KApplication.getContext();
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        }
        return false;
    }

    public static void actionStart(Context context, Class zClass, Bundle bundle) {
        Intent intent = new Intent(context, zClass);
        if (bundle != null) {
            intent.putExtra(Constants.KEY, bundle);
        }
        context.startActivity(intent);
    }
}
