package com.kcrason.kselectimages.model;

import java.util.List;

/**
 * Created by KCrason on 2016/6/7.
 * 图片文件夹实体
 */
public class Folder {

    public String name;
    public String path;
    public Image cover;
    public List<Image> images;

    @Override
    public boolean equals(Object o) {
        try {
            Folder other = (Folder) o;
            return this.path.equalsIgnoreCase(other.path);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
