package com.kcrason.kselectimages.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件操作类
 */
public class FileUtils {

	public static File createTmpFile(Context context) {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			// 已挂载
			File pic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			/**
			 * 注意此处必须进行文件夹的判断，不进行判断在某些国产手机上会出现bug
			 */
			if (!pic.exists()) {
				pic.mkdirs();
			}
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
			String fileName = "select_image_" + timeStamp + "";

			File tmpFile = new File(pic, fileName + ".jpg");

			return tmpFile;
		} else {
			File cacheDir = context.getCacheDir();
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
			String fileName = "select_image_" + timeStamp + "";
			File tmpFile = new File(cacheDir, fileName + ".jpg");

			return tmpFile;
		}
	}
}
