package com.kcrason.kselectimages.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.utils.Constants;

import java.io.File;
import java.util.List;

public class ImagePublishAdapter extends BaseAdapter {
	private List<String> mDataList;
	private Context mContext;

	public ImagePublishAdapter(Context context, List<String> dataList) {
		this.mContext = context;
		this.mDataList = dataList;
	}

	/**
	 * 多返回一个用于展示添加图标
	 */
	public int getCount() {

		if (mDataList == null) {
			return 1;
		} else if (mDataList.size() == Constants.MAX_IMAGE_SIZE) {
			return Constants.MAX_IMAGE_SIZE;
		} else {
			return mDataList.size() + 1;
		}
	}

	public List<String> getItems() {
		return mDataList;
	}

	public void refreshData(List<String> listData) {
		mDataList = listData;
		notifyDataSetChanged();
	}

	public Object getItem(int position) {
		if (mDataList != null && mDataList.size() == Constants.MAX_IMAGE_SIZE) {
			return mDataList.get(position);
		} else if (mDataList == null || position - 1 < 0 || position > mDataList.size()) {
			return null;
		} else {
			return mDataList.get(position - 1);
		}
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// 所有Item展示不满一页，就不进行ViewHolder重用了，避免了一个拍照以后添加图片按钮被覆盖的奇怪问题
		convertView = View.inflate(mContext, R.layout.item_publish, null);
		ImageView imageIv = (ImageView) convertView.findViewById(R.id.item_grid_image);
		if (isShowAddItem(position)) {
			imageIv.setImageResource(R.drawable.selector_btn_add_pic);
		} else {
			final String imagePath = mDataList.get(position);

			Glide.with(mContext).load(new File(imagePath))
					.asBitmap()
					.placeholder(R.drawable.empty_picture)
					.override(100,100)
					.centerCrop()
					.into(imageIv);


		}
		return convertView;
	}

	private boolean isShowAddItem(int position) {
		int size = mDataList == null ? 0 : mDataList.size();
		return position == size;
	}

}
