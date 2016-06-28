package com.kcrason.kselectimages.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.interfaces.SelectImagesCallBack;
import com.kcrason.kselectimages.model.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 图片Adapter
 */
public class ImageGridAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;

    private LayoutInflater mInflater;
    private boolean showCamera = true;
    private boolean showSelectIndicator = true;

    private List<Image> mImages = new ArrayList<>();
    private List<Image> mSelectedImages = new ArrayList<>();

    private int mItemSize;
    private GridView.LayoutParams mItemLayoutParams;

    private SelectImagesCallBack imagesCallBack;

    public ImageGridAdapter(Context context, boolean showCamera, SelectImagesCallBack imagesCallBack) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showCamera = showCamera;
        this.imagesCallBack = imagesCallBack;
        mItemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT,
                GridView.LayoutParams.MATCH_PARENT);
    }

    /**
     * 显示选择指示器
     *
     * @param b
     */
    public void showSelectIndicator(boolean b) {
        showSelectIndicator = b;
    }

    public void setShowCamera(boolean b) {
        if (showCamera == b)
            return;

        showCamera = b;
        notifyDataSetChanged();
    }

    public boolean getShowCamera() {
        return showCamera;
    }

    public boolean isShowCamera() {
        return showCamera;
    }

    /**
     * 选择某个图片，改变选择状态
     *
     * @param image
     */
    public void select(Image image) {
        if (mSelectedImages.contains(image)) {
            mSelectedImages.remove(image);
        } else {
            mSelectedImages.add(image);
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     *
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<String> resultList) {
        for (String path : resultList) {
            Image image = getImageByPath(path);
            if (image != null) {
                mSelectedImages.add(image);
            }
        }
        if (mSelectedImages.size() > 0) {
            notifyDataSetChanged();
        }
    }

    private Image getImageByPath(String path) {
        if (mImages != null && mImages.size() > 0) {
            for (Image image : mImages) {
                if (image.path.equalsIgnoreCase(path)) {
                    return image;
                }
            }
        }
        return null;
    }

    /**
     * 设置数据集
     *
     * @param images
     */
    public void setData(List<Image> images) {
        mSelectedImages.clear();

        if (images != null && images.size() > 0) {
            mImages = images;
        } else {
            mImages.clear();
        }
        notifyDataSetChanged();
    }

    /**
     * 重置每个Column的Size
     *
     * @param columnWidth
     */
    public void setItemSize(int columnWidth) {
        if (mItemSize == columnWidth) {
            return;
        }
        mItemSize = columnWidth;
        mItemLayoutParams = new GridView.LayoutParams(mItemSize, mItemSize);
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera) {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return showCamera ? mImages.size() + 1 : mImages.size();
    }

    public List<Image> getImageList() {
        return mImages;
    }

    @Override
    public Image getItem(int i) {
        if (showCamera) {
            if (i == 0) {
                return null;
            }
            return mImages.get(i - 1);
        } else {
            return mImages.get(i);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        int type = getItemViewType(i);
        if (type == TYPE_CAMERA) {
            view = mInflater.inflate(R.layout.list_item_camera, viewGroup, false);
            view.findViewById(R.id.takePhoto).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    imagesCallBack.onSelectImages(i);
                }
            });
            view.setTag(null);
        } else if (type == TYPE_NORMAL) {
            ViewHolde holde;
            if (view == null) {
                view = mInflater.inflate(R.layout.list_item_image, viewGroup, false);
                holde = new ViewHolde(view);
            } else {
                holde = (ViewHolde) view.getTag();
                if (holde == null) {
                    view = mInflater.inflate(R.layout.list_item_image, viewGroup, false);
                    holde = new ViewHolde(view);
                }
            }
            if (holde != null) {
                holde.bindData(getItem(i), i);
            }
        }

        /** Fixed View Size */
        GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
        if (lp.height != mItemSize) {
            view.setLayoutParams(mItemLayoutParams);
        }
        return view;
    }


    class ViewHolde implements OnClickListener {
        @BindView(R.id.image)
        ImageView image;

        @BindView(R.id.checkmark)
        CheckBox indicator;

        @BindView(R.id.mask)
        View mask;

        int i;

        ViewHolde(View view) {
            ButterKnife.bind(this, view);
            view.setTag(this);
        }

        void bindData(final Image data, int i) {
            if (data == null)
                return;
            // 处理单选和多选状态
            this.i = i;
            if (showSelectIndicator) {
                indicator.setVisibility(View.VISIBLE);
                if (mSelectedImages.contains(data)) {
                    // 设置选中状态

                    indicator.setChecked(true);
                    mask.setVisibility(View.VISIBLE);
                } else {
                    // 未选择
                    indicator.setChecked(false);
                    mask.setVisibility(View.GONE);
                }
            } else {
                indicator.setVisibility(View.GONE);
            }
            File imageFile = new File(data.path);

            if (mItemSize > 0) {
                // 显示图片
                Glide.with(mContext).load(imageFile).asBitmap().placeholder(R.drawable.default_error)
                        .error(R.drawable.default_error)
                        .override(mItemSize, mItemSize)
                        .centerCrop()
                        .into(image);
            }
            indicator.setOnClickListener(this);
        }

        @Override
        public void onClick(View arg0) {
            imagesCallBack.onSelectImages(i);
        }
    }
}
