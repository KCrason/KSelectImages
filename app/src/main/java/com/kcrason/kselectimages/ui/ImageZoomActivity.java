package com.kcrason.kselectimages.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.event.RemoveImageEvent;
import com.kcrason.kselectimages.utils.DisPlayUtils;
import com.kcrason.kselectimages.utils.ImageDisplayer;
import com.kcrason.kselectimages.utils.ShowUtils;
import com.kcrason.kselectimages.utils.SnackBarUtils;
import com.kcrason.kselectimages.widget.ViewPagerFixed;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageZoomActivity extends Activity implements DialogInterface.OnClickListener {


    private MyPageAdapter adapter;
    private int currentPosition;
    private List<String> mDataList = new ArrayList<>();
    private int mBarHeight;

    @BindView(R.id.viewpager)
    ViewPagerFixed mViewPagerFixed;

    @BindView(R.id.rl_title_bar)
    RelativeLayout mTitleBar;

    @BindView(R.id.tv_image_number)
    TextView mImageNumber;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom);
        ButterKnife.bind(this);
        initData();
    }

    @OnClick(R.id.imgv_delete)
    void onClicks(View v) {
        switch (v.getId()) {
            case R.id.imgv_delete:
                if (mDataList.size() == 1) {
                    ShowUtils.showDialog(this, getString(R.string.delele_image), this);
                } else {
                    removeImg(currentPosition);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * 显示标题栏
     */
    private void titleBarVisiable() {
        TranslateAnimation enterAnimation = new TranslateAnimation(0, 0, -mBarHeight, 0);
        mTitleBar.setVisibility(View.VISIBLE);
        enterAnimation.setDuration(400);
        mTitleBar.startAnimation(enterAnimation);
    }

    /**
     * 隐藏标题栏
     */
    private void titleBarGone() {
        TranslateAnimation exitAnimation = new TranslateAnimation(0, 0, 0, -mBarHeight);
        exitAnimation.setDuration(400);
        mTitleBar.startAnimation(exitAnimation);
        mTitleBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        mDataList.clear();
        super.onDestroy();
    }

    private void initData() {
        mBarHeight = DisPlayUtils.dip2px(48);
        Intent intent = getIntent();
        mDataList = intent.getStringArrayListExtra(KSelectImagesActivity.EXTRA_RESULT);
        currentPosition = intent.getIntExtra(KSelectImagesActivity.EXTRA_CURRENT_IMG_POSITION, 0);
        mViewPagerFixed.addOnPageChangeListener(pageChangeListener);
        adapter = new MyPageAdapter(mDataList);
        mViewPagerFixed.setAdapter(adapter);
        mViewPagerFixed.setCurrentItem(currentPosition);
        resetImageNumber();
    }

    private void removeImg(int location) {
        startRemoveImage(location);
    }

    private void startRemoveImage(int location) {
        if (location + 1 <= mDataList.size()) {
            mDataList.remove(location);
            SnackBarUtils.showSnackBar(ButterKnife.findById(this, R.id.rlayout_parent), getString(R.string.delete_sucess));
            EventBus.getDefault().post(new RemoveImageEvent(location));
            // 判断是否是最后一张图片，如果是，则不进行图片数量的重置显示
            if (location != 0) {
                resetImageNumber();
            } else {
                if (mDataList.size() != 0) {
                    resetImageNumber();
                }
            }
        }
    }


    /**
     * 当用户在删除图片时，重置当前图片的位置和当前图片的总数
     */
    private void resetImageNumber() {
        if (mDataList != null) {
            mImageNumber.setText((currentPosition + 1) + "/" + (mDataList.size()));
        }
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {
            currentPosition = arg0;
            resetImageNumber();
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public void onBack(View view) {
        finish();
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        startRemoveImage(0);
        finish();
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }

    class MyPageAdapter extends PagerAdapter {
        private List<String> dataList = new ArrayList<String>();

        public MyPageAdapter(List<String> dataList) {
            this.dataList = dataList;
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public Object instantiateItem(ViewGroup arg0, int position) {
            View image = LayoutInflater.from(ImageZoomActivity.this).inflate(R.layout.item_image_preview, null);
            image.setId(position);
            assert image != null;
            PhotoView imageView = (PhotoView) image.findViewById(R.id.image_preview);
            if (!TextUtils.isEmpty(dataList.get(position))) {
                ImageDisplayer.getInstance().displayBmp(imageView, null, dataList.get(position));
            }
            imageView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v1) {
                    if (mTitleBar.getVisibility() == View.VISIBLE) {
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

        @Override
        public int getCount() {
            if (dataList == null) {
                return 0;
            } else {
                return dataList.size();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }
}