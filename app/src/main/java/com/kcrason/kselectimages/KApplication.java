package com.kcrason.kselectimages;

import android.app.Application;
import android.content.Context;

/**
 * Created by KCrason on 2016/6/7.
 */
public class KApplication extends Application {


    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
