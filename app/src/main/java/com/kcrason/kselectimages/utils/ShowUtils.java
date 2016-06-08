package com.kcrason.kselectimages.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import com.kcrason.kselectimages.R;

/**
 * Created by KCrason on 2016/6/7.
 */
public class ShowUtils {

    public static void showDialog(Activity activity, String msg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getString(R.string.prompt));
        builder.setMessage(msg);
        builder.setPositiveButton(activity.getString(android.R.string.yes), listener);
        builder.setNegativeButton(activity.getString(android.R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static ProgressDialog showProgressDialog(Activity activity, String msg) {
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(msg);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        return progressDialog;
    }
}
