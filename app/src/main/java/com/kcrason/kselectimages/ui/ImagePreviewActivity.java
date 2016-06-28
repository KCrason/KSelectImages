package com.kcrason.kselectimages.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.event.RefreshImageSelect;
import com.kcrason.kselectimages.model.Image;
import com.kcrason.kselectimages.utils.ActivityManager;
import com.kcrason.kselectimages.utils.Constants;
import com.kcrason.kselectimages.utils.DisPlayUtils;
import com.kcrason.kselectimages.utils.KUtils;
import com.kcrason.kselectimages.utils.SnackBarUtils;
import com.kcrason.kselectimages.widget.ViewPagerFixed;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ImagePreviewActivity extends Activity {

    private int currentPosition = 0;

    private ArrayList<String> mDataList;

    private boolean isPreviewAll;

    private ArrayList<Image> mAllImagesList;

    private int barHeight;

    private boolean isFlag = true;

    private Map<String, String> tempImagePath = new HashMap<>();
    private boolean mIsShowCamera = false;


    @BindView(R.id.rlayout_parent)
    RelativeLayout mParentView;

    @BindView(R.id.viewpager)
    ViewPagerFixed mViewPagerFixed;

    @BindView(R.id.preview_commit)
    Button mPreviewCommit;

    @BindView(R.id.preview_checkmark)
    CheckBox mPreviewCheckMark;

    @BindView(R.id.tv_image_number)
    TextView mImageNumber;

    @BindView(R.id.rl_title_bar)
    RelativeLayout mTitleBar;

    @BindView(R.id.rl_bottom_bar)
    RelativeLayout mBottomBar;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityManager.getInstance().addActivity(this);
        setContentView(R.layout.activity_preview);
        ButterKnife.bind(this);

        initData();

        if (!isPreviewAll) {
            resetNumber();
            mImageNumber.setText((currentPosition + 1) + "/" + mDataList.size());
        } else {
            if (tempImagePath.size() == 0) {
                mPreviewCommit.setText(getString(R.string.finish));
            } else {
                mPreviewCommit.setText(getString(R.string.finish) + "(" + tempImagePath.size() + "/" + 9 + ")");
            }
            mImageNumber.setText((currentPosition + 1) + "/" + mAllImagesList.size());
            if (tempImagePath.containsValue(mAllImagesList.get(0).path)) {
                mPreviewCheckMark.setChecked(true);
            } else {
                mPreviewCheckMark.setChecked(false);
            }
        }
        mViewPagerFixed.addOnPageChangeListener(pageChangeListener);
        mViewPagerFixed.setAdapter(new MyPageAdapter());

        if (mIsShowCamera) {
            mViewPagerFixed.setCurrentItem(currentPosition - 1);
        } else {
            mViewPagerFixed.setCurrentItem(currentPosition);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!tempImagePath.isEmpty() && tempImagePath.size() != 0) {
            tempImagePath.clear();
        }
    }

    private void resetNumber() {
        if (tempImagePath.size() == 0) {
            mPreviewCommit.setText(getString(R.string.finish));
        } else {
            mPreviewCommit.setText(getString(R.string.finish) + "(" + tempImagePath.size() + "/" + 9 + ")");
        }
    }

    private void initData() {
        barHeight = DisPlayUtils.dip2px(48);
        Bundle bundle = getIntent().getBundleExtra(Constants.KEY);
        if (bundle != null) {
            mDataList = bundle.getStringArrayList(Constants.EXTRA_RESULT);
            isPreviewAll = bundle.getBoolean(Constants.EXTRA_PRE_ALL, false);
            if (isPreviewAll) {
                currentPosition = bundle.getInt(Constants.EXTRA_CUR_POSITION, -1);
                mAllImagesList = (ArrayList<Image>) bundle.getSerializable(Constants.EXTRA_ALL_IMAGES);
                mIsShowCamera = bundle.getBoolean(Constants.EXTRA_SHOW_CAMERA, false);
                if (mIsShowCamera && currentPosition == 1) {
                    currentPosition = 0;
                }
            }
        }
        if (mDataList != null) {
            for (int i = 0; i < mDataList.size(); i++) {
                tempImagePath.put(mDataList.get(i), mDataList.get(i));
            }
        }
    }

    private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

        public void onPageSelected(int arg0) {
            isFlag = false;
            currentPosition = arg0;
            if (isPreviewAll) {
                mImageNumber.setText((currentPosition + 1) + "/" + mAllImagesList.size());
                if (tempImagePath.containsKey(mAllImagesList.get(arg0).path)) {
                    mPreviewCheckMark.setChecked(true);
                } else {
                    mPreviewCheckMark.setChecked(false);
                }
            } else {
                mImageNumber.setText((currentPosition + 1) + "/" + mDataList.size());
                if (tempImagePath.containsKey(mDataList.get(arg0))) {
                    mPreviewCheckMark.setChecked(true);
                } else {
                    mPreviewCheckMark.setChecked(false);
                }
            }
        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        public void onPageScrollStateChanged(int arg0) {

        }
    };

    public void onBack(View view) {
        ActivityManager.getInstance().removeTopActivity();
        finish();
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }

    private void titleBarVisiable() {
        TranslateAnimation titleBarEnterAnimation = new TranslateAnimation(0, 0, -barHeight, 0);
        TranslateAnimation bottomBarEnterAnimation = new TranslateAnimation(0, 0, barHeight, 0);

        mTitleBar.setVisibility(View.VISIBLE);
        mBottomBar.setVisibility(View.VISIBLE);

        titleBarEnterAnimation.setDuration(400);
        bottomBarEnterAnimation.setDuration(400);

        mTitleBar.startAnimation(titleBarEnterAnimation);
        mBottomBar.startAnimation(bottomBarEnterAnimation);

    }

    private void titleBarGone() {
        TranslateAnimation titleBarExitAnimation = new TranslateAnimation(0, 0, 0, -barHeight);
        TranslateAnimation bottomBarExitAnimation = new TranslateAnimation(0, 0, 0, barHeight);

        titleBarExitAnimation.setDuration(400);
        bottomBarExitAnimation.setDuration(400);

        mTitleBar.startAnimation(titleBarExitAnimation);
        mBottomBar.startAnimation(bottomBarExitAnimation);

        mTitleBar.setVisibility(View.GONE);
        mBottomBar.setVisibility(View.GONE);
    }

    class MyPageAdapter extends PagerAdapter {

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            // photoView的单击事件
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v1) {
                    if (mTitleBar.getVisibility() == View.VISIBLE) {
                        titleBarGone();
                    } else {
                        titleBarVisiable();
                    }
                }
            });

            if (isPreviewAll) {
                String allImagePath = mAllImagesList.get(position).path;
                if (mAllImagesList != null && !TextUtils.isEmpty(allImagePath)) {
                    Glide.with(ImagePreviewActivity.this).load(new File(allImagePath))
                            .asBitmap().placeholder(R.drawable.default_error)
                            .error(R.drawable.default_error)
                            .fitCenter().into(photoView);
                }
            } else {
                String imagePath = mDataList.get(position);
                if (mDataList != null && !TextUtils.isEmpty(imagePath)) {
                    Glide.with(ImagePreviewActivity.this).load(new File(imagePath))
                            .asBitmap()
                            .placeholder(R.drawable.default_error)
                            .error(R.drawable.default_error)
                            .fitCenter()
                            .into(photoView);
                }
            }
            container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            return photoView;
        }

        public void destroyItem(ViewGroup container, int arg1, Object object) {
            container.removeView((View) object);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public int getCount() {
            if (isPreviewAll) {
                if (mAllImagesList == null) {
                    return 0;
                }
                return mAllImagesList.size();
            } else {
                if (mDataList == null) {
                    return 0;
                }
                return mDataList.size();
            }
        }
    }

    private ArrayList<String> getResultList() {
        ArrayList<String> resultList = new ArrayList<>();
        if (tempImagePath.size() != 0) {
            resultList.addAll(tempImagePath.values());
        } else {
            if (isPreviewAll) {
                if (isFlag && currentPosition == 1) {
                    resultList.add(mAllImagesList.get(currentPosition - 1).path);
                } else {
                    resultList.add(mAllImagesList.get(currentPosition).path);
                }
            } else {
                resultList.add(mDataList.get(currentPosition));
            }
        }
        return resultList;
    }


    private void checkMarkImage() {
        String imagePath;
        if (isPreviewAll) {
            imagePath = mAllImagesList.get(currentPosition).path;
            if (tempImagePath.containsKey(imagePath)) {
                mPreviewCheckMark.setChecked(false);
                tempImagePath.remove(imagePath);
            } else {
                // 如果选择的图片大于9张则无法继续选择图片
                if (tempImagePath.size() >= 9) {
                    SnackBarUtils.showSnackBar(mParentView, getString(R.string.image_amount_limit));
                    return;
                }
                mPreviewCheckMark.setChecked(true);
                tempImagePath.put(imagePath, imagePath);
            }
        } else {
            imagePath = mDataList.get(currentPosition);
            if (tempImagePath.containsKey(imagePath)) {
                mPreviewCheckMark.setChecked(false);
                tempImagePath.remove(imagePath);
            } else {
                mPreviewCheckMark.setChecked(true);
                tempImagePath.put(imagePath, imagePath);
            }
        }
        Image image = new Image(imagePath);
        EventBus.getDefault().post(new RefreshImageSelect(image));
        resetNumber();
    }

    @Override
    public void onBackPressed() {
        ActivityManager.getInstance().removeTopActivity();
        finish();
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }

    @OnClick({R.id.preview_checkmark, R.id.preview_commit})
    void onClicks(View view) {
        switch (view.getId()) {
            case R.id.preview_checkmark:
                checkMarkImage();
                break;
            case R.id.preview_commit:
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(Constants.EXTRA_RESULT, getResultList());
                KUtils.actionStart(this, ReleaseImageActivity.class, bundle);
                ActivityManager.getInstance().finishActivitys();
                overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
                break;
        }
    }
}