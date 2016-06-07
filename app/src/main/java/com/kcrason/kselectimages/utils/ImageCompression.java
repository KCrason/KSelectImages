package com.kcrason.kselectimages.utils;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageCompression {

	/**
	 * 获取图片的角度
	 * 
	 * @param path
	 * @return
	 */
	public static int readPicDegree(String path) {
		int degree = 0;
		// 读取图片文件信息的类ExifInterface
		ExifInterface exif = null;
		try {
			exif = new ExifInterface(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (exif != null) {
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		}
		return degree;
	}

	/**
	 * 将图片纠正到正确方向，并返回纠正后的图片
	 * 
	 * @return 纠向后的图片
	 */
	public static Bitmap rotateBitmap(int degree, Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		if (bitmap != null) {
			return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		} else {
			return null;
		}
	}

	/**
	 * 改变图片压缩比例，减少图片读取的时内存的占用问题
	 * 
	 * @param srcPath
	 * @return
	 */
	public static ByteArrayOutputStream getImage(String srcPath) {
		if (TextUtils.isEmpty(srcPath)) {
			return null;
		}
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds设置为true,那么其在读取file类型的图片时不会占用内存，但通过设置这一属性
		// 可以获得相应的图片的宽高
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		float hh = 1280f;
		float ww = 720f;
		// 缩放比,由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;
		if (w > h && w > ww) {
			// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {
			// 如果高度高的话根据高度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0) {
			be = 1;
		}
		newOpts.inSampleSize = be;
		newOpts.inPreferredConfig = Config.RGB_565;// 默认8888
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(rotateBitmap(readPicDegree(srcPath), bitmap));// 压缩好比例大小后再进行质量压缩
	}

	/**
	 * 此方法的调用必须在图片进行比例压缩之后进行调用，否则无法达到内存优化的目的。
	 * 实际上在进行质量压缩之前，已经将图片读取到内存了，那么在进行比例压缩时设置的参数也就没有意义了。
	 * 
	 * @param image
	 * @return
	 */
	private static ByteArrayOutputStream compressImage(Bitmap image) {
		if (image == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		image.compress(Bitmap.CompressFormat.PNG, 100, baos);
		int options = 90;
		// 循环判断如果压缩后图片是否大于256kb,大于继续压缩
		while (baos.toByteArray().length / 1024 > 256) {
			baos.reset();
			if (options >= 0 && options <= 100) {
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);
				options -= 10;
			} else {
				break;
			}
		}
		if (image != null && !image.isRecycled()) {
			image.recycle();
			image = null;
		}
		return baos;
	}
}
