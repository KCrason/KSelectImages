package com.kcrason.kselectimages.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.event.TakePhotoEvent;
import com.kcrason.kselectimages.interfaces.Callback;
import com.kcrason.kselectimages.utils.ActivityManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by KCrason on 2016/6/7.
 */
public class KSelectImagesActivity extends FragmentActivity implements Callback {
    /**
     * 最大图片选择次数，int类型，默认9
     */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /**
     * 图片选择模式，默认多选
     */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /**
     * 是否显示相机，默认显示
     */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";
    /**
     * 默认选择集
     */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";
    /**
     * 当前预览的position
     */
    public static final String EXTRA_CURRENT_IMG_POSITION = "current_image_position";
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;

    private ArrayList<String> resultList = new ArrayList<>();

    private Button mSubmitButton;
    private int mDefaultCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);
        EventBus.getDefault().register(this);
        ActivityManager.getInstance().addActivity(this);
        Intent intent = getIntent();
        mDefaultCount = intent.getIntExtra(EXTRA_SELECT_COUNT, 9);
        int mode = intent.getIntExtra(EXTRA_SELECT_MODE, MODE_MULTI);
        boolean isShow = intent.getBooleanExtra(EXTRA_SHOW_CAMERA, true);

        if (mode == MODE_MULTI && intent.hasExtra(EXTRA_DEFAULT_SELECTED_LIST)) {
            if (intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST) != null) {
                resultList = intent.getStringArrayListExtra(EXTRA_DEFAULT_SELECTED_LIST);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putInt(KSelectImagesFragment.EXTRA_SELECT_COUNT, mDefaultCount);
        bundle.putInt(KSelectImagesFragment.EXTRA_SELECT_MODE, mode);
        bundle.putBoolean(KSelectImagesFragment.EXTRA_SHOW_CAMERA, isShow);
        bundle.putStringArrayList(KSelectImagesFragment.EXTRA_DEFAULT_SELECTED_LIST, resultList);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.image_grid, Fragment.instantiate(this, KSelectImagesFragment.class.getName(), bundle))
                .commit();

        // 完成按钮
        mSubmitButton = (Button) findViewById(R.id.commit);
        if (resultList == null || resultList.size() <= 0) {
            mSubmitButton.setText(getString(R.string.finish));
            mSubmitButton.setEnabled(false);
        } else {
            setSelectNumber();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (resultList != null && resultList.size() > 0) {
                    // 返回已选择的图片数据
                    Intent data = new Intent(KSelectImagesActivity.this, ReleaseImageActivity.class);
                    data.putStringArrayListExtra(EXTRA_RESULT, resultList);
                    startActivity(data);
                    overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
                    ActivityManager.getInstance().finishActivitys();
                }
            }
        });
    }


    @Override
    public void onSingleImageSelected(String path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putStringArrayListExtra(EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onBack(View view) {
        ActivityManager.getInstance().removeTopActivity();
        finish();
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }

    @Subscribe
    public void onEventMainThread(TakePhotoEvent event) {
        /**
         * 从拍照预览页传过来的index应当小于当前的结果集，避免出现不一致时的数组越界
         */
        if (event.getIndex() < resultList.size()) {
            resultList.remove(event.getIndex());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onImageSelected(String path) {
        if (!resultList.contains(path)) {
            resultList.add(path);
        }
        // 有图片之后，改变按钮状态
        if (resultList.size() > 0) {
            setSelectNumber();
            if (!mSubmitButton.isEnabled()) {
                mSubmitButton.setEnabled(true);
            }
        }
    }

    private void setSelectNumber() {
        mSubmitButton.setText(getString(R.string.finish) + "(" + resultList.size() + "/" + mDefaultCount + ")");
    }

    @Override
    public void onBackPressed() {
        ActivityManager.getInstance().removeTopActivity();
        finish();
        overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
    }

    @Override
    public void onImageUnselected(String path) {
        if (resultList.contains(path)) {
            resultList.remove(path);
            setSelectNumber();
        } else {
            setSelectNumber();
        }
        // 当为选择图片时候的状态
        if (resultList.size() == 0) {
            mSubmitButton.setText(getString(R.string.finish));
            mSubmitButton.setEnabled(false);
        }
    }

    @Override
    public void onCameraShot(File imageFile) {
        if (imageFile != null) {
            Intent data = new Intent(KSelectImagesActivity.this, TakePhotoPreview.class);
            resultList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(EXTRA_RESULT, resultList);
            startActivity(data);
            overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
        }
    }
}
