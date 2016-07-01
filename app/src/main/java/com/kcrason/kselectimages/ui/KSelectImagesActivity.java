package com.kcrason.kselectimages.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.event.TakePhotoEvent;
import com.kcrason.kselectimages.interfaces.Callback;
import com.kcrason.kselectimages.utils.ActivityManager;
import com.kcrason.kselectimages.utils.Constants;
import com.kcrason.kselectimages.utils.KUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Created by KCrason on 2016/6/7.
 */
public class KSelectImagesActivity extends FragmentActivity implements Callback {

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
        mDefaultCount = intent.getIntExtra(Constants.EXTRA_SELECT_COUNT, 9);
        int mode = intent.getIntExtra(Constants.EXTRA_SELECT_MODE, MODE_MULTI);
        boolean isShow = intent.getBooleanExtra(Constants.EXTRA_SHOW_CAMERA, true);

        if (mode == MODE_MULTI && intent.hasExtra(Constants.EXTRA_DEFAULT_SELECTED_LIST)) {
            if (intent.getStringArrayListExtra(Constants.EXTRA_DEFAULT_SELECTED_LIST) != null) {
                resultList = intent.getStringArrayListExtra(Constants.EXTRA_DEFAULT_SELECTED_LIST);
            }
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Constants.EXTRA_SELECT_COUNT, mDefaultCount);
        bundle.putInt(Constants.EXTRA_SELECT_MODE, mode);
        bundle.putBoolean(Constants.EXTRA_SHOW_CAMERA, isShow);
        bundle.putStringArrayList(Constants.EXTRA_DEFAULT_SELECTED_LIST, resultList);

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
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList(Constants.EXTRA_RESULT, resultList);
                    KUtils.actionStart(KSelectImagesActivity.this, ReleaseImageActivity.class, bundle);
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
        data.putStringArrayListExtra(Constants.EXTRA_RESULT, resultList);
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
            /**
             * 插入到系统图库
             */
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), imageFile.getAbsolutePath(), imageFile.getName(), null);
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.parse("file://" + imageFile.getAbsolutePath()));
                sendBroadcast(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Intent data = new Intent(KSelectImagesActivity.this, TakePhotoPreview.class);
            resultList.add(imageFile.getAbsolutePath());
            data.putStringArrayListExtra(Constants.EXTRA_RESULT, resultList);
            startActivity(data);
            overridePendingTransition(R.anim.selecter_image_alpha_enter, R.anim.selecter_image_alpha_exit);
        }
    }
}
