package com.kcrason.kselectimages.event;

/**
 * Created by KCrason on 2016/6/7.
 */
public class TakePhotoEvent {

    private int index;

    public TakePhotoEvent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
