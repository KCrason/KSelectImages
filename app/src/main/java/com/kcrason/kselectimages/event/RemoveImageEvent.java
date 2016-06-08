package com.kcrason.kselectimages.event;

public class RemoveImageEvent {

    private int index;
    private boolean isRevoke;
    private String path;

    public RemoveImageEvent(int index, boolean isRevoke) {
        this.index = index;
        this.isRevoke = isRevoke;
    }

    public RemoveImageEvent(int index, boolean isRevoke, String path) {
        this.index = index;
        this.isRevoke = isRevoke;
        this.path = path;
    }

    public boolean getIsRevoke() {
        return isRevoke;
    }

    public int getIndex() {
        return index;
    }

    public String getPath() {
        return path;
    }
}
