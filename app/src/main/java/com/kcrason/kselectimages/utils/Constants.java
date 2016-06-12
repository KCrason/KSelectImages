package com.kcrason.kselectimages.utils;

/**
 * Created by KCrason on 2016/6/7.
 */
public class Constants {

    public static final int MAX_IMAGE_SIZE = 9;// 单次最多发送图片数
    public static final String KEY = "key_bundle";
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;

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
     * 所有图片
     */
    public static final String EXTRA_ALL_IMAGES = "all_images";

    /**
     * 当前位置
     */
    public static final String EXTRA_CUR_POSITION ="current_position";

    /**
     * 是否预览全部
     */
    public static final String EXTRA_PRE_ALL = "preview_all";
}
