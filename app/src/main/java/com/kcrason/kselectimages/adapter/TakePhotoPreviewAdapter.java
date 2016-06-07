package com.kcrason.kselectimages.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.utils.DisPlayUtils;
import com.kcrason.kselectimages.utils.ImageDisplayer;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class TakePhotoPreviewAdapter extends PagerAdapter {

	private int barHeight;
	private Context context;
	private ArrayList<String> mDataList;
	private RelativeLayout rl_title_bar;

	public TakePhotoPreviewAdapter(Context context, ArrayList<String> mDataList, RelativeLayout rl_title_bar) {
		this.context = context;
		this.mDataList = mDataList;
		this.rl_title_bar = rl_title_bar;
		this.barHeight = DisPlayUtils.dip2px(48);
	}

	@Override
	public int getCount() {
		return 1;
	}

	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	/**
	 * 显示标题栏
	 */
	private void titleBarVisiable() {
		TranslateAnimation titleBarEnterAnimation = new TranslateAnimation(0, 0, -barHeight, 0);
		rl_title_bar.setVisibility(View.VISIBLE);
		titleBarEnterAnimation.setDuration(400);
		rl_title_bar.startAnimation(titleBarEnterAnimation);
	}

	/**
	 * 隐藏标题栏
	 */
	private void titleBarGone() {
		TranslateAnimation titleBarExitAnimation = new TranslateAnimation(0, 0, 0, -barHeight);
		titleBarExitAnimation.setDuration(400);
		rl_title_bar.startAnimation(titleBarExitAnimation);
		rl_title_bar.setVisibility(View.GONE);
	}

	public Object instantiateItem(ViewGroup arg0, int position) {
		View image = LayoutInflater.from(context).inflate(R.layout.item_image_preview, null);
		image.setId(position);
		assert image != null;

		PhotoView imageView = (PhotoView) image.findViewById(R.id.image_preview);
		if (mDataList != null && !TextUtils.isEmpty(mDataList.get(mDataList.size() - 1))) {
			ImageDisplayer.getInstance().displayBmp(imageView, null, mDataList.get(mDataList.size() - 1), false);
		}
		imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
			@Override
			public void onPhotoTap(View view, float v, float v1) {
				if (rl_title_bar.getVisibility() == View.VISIBLE) {
					titleBarGone();
				} else {
					titleBarVisiable();
				}
			}
			@Override
			public void onOutsidePhotoTap() {
			}
		});

		arg0.addView(image, 0);
		return image;
	}

	public void destroyItem(ViewGroup container, int arg1, Object object) {
		container.removeView((View) object);
	}

	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

}
