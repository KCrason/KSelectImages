package com.kcrason.kselectimages.utils;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KCrason on 2016/6/7.
 */
public class ActivityManager {

    private static ActivityManager activityManage;
    private List<Activity> activities = new ArrayList<>();

    public static synchronized ActivityManager getInstance() {
        if (activityManage == null) {
            activityManage = new ActivityManager();
        }
        return activityManage;
    }

    public int getActivityCount() {
        return activities.size();
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    public void removeTopActivity() {
        if (getActivityCount() >= 1) {
            activities.remove(getActivityCount() - 1);
        }
    }

    public void finishActivitys() {
        for (int i = 0; i < getActivityCount(); i++) {
            activities.get(i).finish();
        }
        activities.clear();
    }
}
