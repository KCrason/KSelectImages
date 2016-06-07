package com.kcrason.kselectimages.interfaces;

import java.io.File;

/**
 * Created by KCrason on 2016/6/7.
 */
public interface Callback {
    void onSingleImageSelected(String path);

    void onImageSelected(String path);

    void onImageUnselected(String path);

    void onCameraShot(File imageFile);
}
