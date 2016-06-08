package com.kcrason.kselectimages.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.ImageView;

import com.kcrason.kselectimages.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;

public class ImageDisplayer {
    private static ImageDisplayer instance;
    private static final int THUMB_WIDTH = 256;
    private static final int THUMB_HEIGHT = 256;
    private int mScreenWidth;
    private int mScreenHeight;

    public static ImageDisplayer getInstance() {
        if (instance == null) {
            synchronized (ImageDisplayer.class) {
                instance = new ImageDisplayer();
            }
        }
        return instance;
    }

    public ImageDisplayer() {
        this.mScreenWidth = DisPlayUtils.getScrrenWidthPixels();
        this.mScreenHeight = DisPlayUtils.getScrrenHeightPixel();
    }

    public Handler h = new Handler();
    public final String TAG = getClass().getSimpleName();
    private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<>();

    public void put(String key, Bitmap bmp) {
        if (!TextUtils.isEmpty(key) && bmp != null) {
            imageCache.put(key, new SoftReference<>(bmp));
        }
    }

    public void displayBmp(final ImageView iv, final String thumbPath, final String sourcePath) {
        displayBmp(iv, thumbPath, sourcePath, false);
    }

    public void displayBmp(final ImageView iv, final String thumbPath, final String sourcePath, final boolean showThumb) {
        final int degree = ImageCompression.readPicDegree(sourcePath);
        if (TextUtils.isEmpty(thumbPath) && TextUtils.isEmpty(sourcePath)) {
            return;
        }

        if (iv.getTag() != null && iv.getTag().equals(sourcePath)) {
            return;
        }


        final String path;
        if (!TextUtils.isEmpty(thumbPath) && showThumb) {
            path = thumbPath;
        } else if (!TextUtils.isEmpty(sourcePath)) {
            path = sourcePath;
        } else {
            return;
        }

        iv.setTag(path);

        if (imageCache.containsKey(showThumb ? path + THUMB_WIDTH + THUMB_HEIGHT : path)) {
            SoftReference<Bitmap> reference = imageCache.get(showThumb ? path + THUMB_WIDTH + THUMB_HEIGHT : path);
            // 可以用LruCahche会好些
            Bitmap imgInCache = reference.get();
            if (imgInCache != null) {
                refreshView(iv, imgInCache, path, degree);
                return;
            }
        }
        iv.setImageBitmap(null);

        // 不在缓存则加载图片
        new Thread() {
            Bitmap img;

            public void run() {
                try {
                    if (path != null && path.equals(thumbPath)) {
                        img = BitmapFactory.decodeFile(path);
                    }
                    if (img == null) {
                        img = compressImg(sourcePath, showThumb);
                    }
                } catch (Exception e) {

                }

                if (img != null) {
                    put(showThumb ? path + THUMB_WIDTH + THUMB_HEIGHT : path, img);
                }
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        refreshView(iv, img, path, degree);
                    }
                });
            }
        }.start();

    }

    private void refreshView(ImageView imageView, Bitmap bitmap, String path, int degree) {
        if (imageView != null && bitmap != null) {
            if (path != null) {
                imageView.setImageBitmap(ImageCompression.rotateBitmap(degree, bitmap));
                imageView.setTag(path);
            }
        } else {
            imageView.setImageResource(R.drawable.default_error);
        }
    }

    public Bitmap compressImg(String path, boolean showThumb) throws IOException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, opt);
        in.close();
        int i = 0;
        Bitmap bitmap = null;
        if (showThumb) {
            while (true) {
                if ((opt.outWidth >> i <= THUMB_WIDTH) && (opt.outHeight >> i <= THUMB_HEIGHT)) {
                    in = new BufferedInputStream(new FileInputStream(new File(path)));
                    opt.inSampleSize = (int) Math.pow(2.0D, i);
                    opt.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(in, null, opt);
                    break;
                }
                i += 1;
            }
        } else {
            while (true) {
                if ((opt.outWidth >> i <= mScreenWidth) && (opt.outHeight >> i <= mScreenHeight)) {
                    in = new BufferedInputStream(new FileInputStream(new File(path)));
                    opt.inSampleSize = (int) Math.pow(2.0D, i);
                    opt.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeStream(in, null, opt);
                    break;
                }
                i += 1;
            }
        }
        return bitmap;
    }
}
