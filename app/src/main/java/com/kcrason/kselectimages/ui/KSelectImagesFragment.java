package com.kcrason.kselectimages.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kcrason.kselectimages.R;
import com.kcrason.kselectimages.adapter.FolderAdapter;
import com.kcrason.kselectimages.adapter.ImageGridAdapter;
import com.kcrason.kselectimages.event.RefreshImageSelect;
import com.kcrason.kselectimages.interfaces.Callback;
import com.kcrason.kselectimages.interfaces.SelectImagesCallBack;
import com.kcrason.kselectimages.model.Folder;
import com.kcrason.kselectimages.model.Image;
import com.kcrason.kselectimages.utils.Constants;
import com.kcrason.kselectimages.utils.FileUtils;
import com.kcrason.kselectimages.utils.KUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by KCrason on 2016/6/7.
 */
public class KSelectImagesFragment extends Fragment implements View.OnClickListener, SelectImagesCallBack {

    @BindView(R.id.rlayout_parent)
    RelativeLayout mRelativeLayout;

    @BindView(R.id.grid)
    GridView mGridView;

    @BindView(R.id.category_btn)
    TextView mCategoryText;

    // 预览按钮
    @BindView(R.id.preview)
    Button mPreviewBtn;

    // 底部View
    @BindView(R.id.footer)
    View mPopupAnchorView;

    @BindView(R.id.timeline_area)
    TextView mTimeLineText;

    // 不同loader定义
    private static final int LOADER_ALL = 0;
    private static final int LOADER_CATEGORY = 1;
    // 请求加载系统照相机
    private static final int REQUEST_CAMERA = 100;
    // 结果数据
    private ArrayList<String> resultList = new ArrayList<>();
    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();
    private ArrayList<Image> mAllImageList;

    // 图片Grid

    private Callback mCallback;
    private ImageGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;
    private PopupWindow mFolderPopupWindow;
    private ListView folderList;

    private int mDesireImageCount;
    private boolean hasFolderGened = false;
    private boolean mIsShowCamera = false;
    private int mGridWidth, mGridHeight;
    private File mTmpFile;
    private int mode;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
        try {
            mCallback = (Callback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(
                    "The Activity must implement KSelectImagesFragment.Callback interface...");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_image, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 选择图片数量
        mDesireImageCount = getArguments().getInt(Constants.EXTRA_SELECT_COUNT);
        // 图片选择模式
        mode = getArguments().getInt(Constants.EXTRA_SELECT_MODE);
        // 默认选择
        if (mode == Constants.MODE_MULTI) {
            ArrayList<String> tmp = getArguments().getStringArrayList(Constants.EXTRA_DEFAULT_SELECTED_LIST);
            if (tmp != null && tmp.size() > 0) {
                resultList = tmp;
            }
        }

        // 是否显示照相机
        mIsShowCamera = getArguments().getBoolean(Constants.EXTRA_SHOW_CAMERA, true);
        mImageAdapter = new ImageGridAdapter(getContext(), mIsShowCamera, this);
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(mode == Constants.MODE_MULTI);

        // 初始化，先隐藏当前timeline
        mTimeLineText.setVisibility(View.GONE);

        // 初始化，加载所有图片
        mCategoryText.setText(getString(R.string.all_image));
        mCategoryText.setOnClickListener(this);

        // 初始化，按钮状态初始化,
        if (resultList == null || resultList.size() <= 0) {
            mPreviewBtn.setText(getString(R.string.preview));
            // 如果没有选择照片，则预览按钮不可点击
            mPreviewBtn.setEnabled(false);
        } else {
            mPreviewBtn.setText(getString(R.string.preview) + "(" + resultList.size() + ")");
            mPreviewBtn.setEnabled(true);
        }
        mPreviewBtn.setOnClickListener(this);

        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int state) {
                if (state == SCROLL_STATE_IDLE) {
                    // 停止滑动，日期指示器消失
                    mTimeLineText.setVisibility(View.GONE);
                } else if (state == SCROLL_STATE_FLING) {
                    mTimeLineText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (mTimeLineText.getVisibility() == View.VISIBLE) {
                    int index = firstVisibleItem + 1 == view.getAdapter().getCount() ? view.getAdapter().getCount() - 1
                            : firstVisibleItem + 1;
                    Image image = (Image) view.getAdapter().getItem(index);
                    if (image != null) {
                        mTimeLineText.setText(formatPhotoDate(image.path));
                    }
                }
            }
        });
        mGridView.setAdapter(mImageAdapter);
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int width = mGridView.getWidth();
                final int height = mGridView.getHeight();

                mGridWidth = width;
                mGridHeight = height;

                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                final int numCount = width / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (width - columnSpace * (numCount - 1)) / numCount;
                mImageAdapter.setItemSize(columnWidth);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(Constants.EXTRA_RESULT, resultList);
                bundle.putSerializable(Constants.EXTRA_ALL_IMAGES, mAllImageList);
                bundle.putBoolean(Constants.EXTRA_SHOW_CAMERA, mImageAdapter.getShowCamera());
                bundle.putInt(Constants.EXTRA_CUR_POSITION, i);
                bundle.putBoolean(Constants.EXTRA_PRE_ALL, true);
                KUtils.actionStart(getActivity(), ImagePreviewActivity.class, bundle);
                getActivity().overridePendingTransition(R.anim.selecter_image_alpha_enter,
                        R.anim.selecter_image_alpha_exit);
            }
        });

        mFolderAdapter = new FolderAdapter(getActivity());
    }

    public String formatPhotoDate(long time) {
        return timeFormat(time, "yyyy年MM月dd日");
    }

    public String timeFormat(long timeMillis, String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.CHINA);
        return format.format(new Date(timeMillis));
    }

    public String formatPhotoDate(String path) {
        File file = new File(path);
        if (file.exists()) {
            long time = file.lastModified();
            return formatPhotoDate(time);
        }
        return "1970-01-01";
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList(int width, int height) {
        mFolderPopupWindow = new PopupWindow(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.item_image_folder, null);
        folderList = (ListView) view.findViewById(R.id.folder_list);
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(0xb0000000));
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height * 5 / 6);
        mFolderPopupWindow.setContentView(view);
        mFolderPopupWindow.setFocusable(true);
        folderList.setAdapter(mFolderAdapter);

        folderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View arg1, int i, long l) {
                mFolderAdapter.setSelectIndex(i);

                final int index = i;
                final AdapterView v = adapterView;

                mFolderPopupWindow.dismiss();
                if (index == 0) {
                    getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                    mCategoryText.setText(getString(R.string.all_image));
                    if (mIsShowCamera) {
                        mImageAdapter.setShowCamera(true);
                    } else {
                        mImageAdapter.setShowCamera(false);
                    }
                } else {
                    Folder folder = (Folder) v.getAdapter().getItem(index);
                    if (null != folder) {
                        mImageAdapter.setData(folder.images);
                        // 当切换图片文件夹时，需要更新相应的所有图片预览数据源
                        mAllImageList.clear();
                        mAllImageList.addAll(folder.images);
                        mCategoryText.setText(folder.name);
                        // 设定默认选择
                        if (resultList != null && resultList.size() > 0) {
                            mImageAdapter.setDefaultSelected(resultList);
                        }
                    }
                    mImageAdapter.setShowCamera(false);
                }
                // 滑动到最初始位置
                mGridView.smoothScrollToPosition(0);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 首次加载所有图片
        getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == getActivity().RESULT_OK) {
                if (mTmpFile != null) {
                    if (mCallback != null) {
                        mCallback.onCameraShot(mTmpFile);
                    }
                }
            } else {
                if (mTmpFile != null && mTmpFile.exists()) {
                    mTmpFile.delete();
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }
        }

        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            public void onGlobalLayout() {

                final int height = mGridView.getHeight();

                final int desireSize = getResources().getDimensionPixelOffset(R.dimen.image_size);
                final int numCount = mGridView.getWidth() / desireSize;
                final int columnSpace = getResources().getDimensionPixelOffset(R.dimen.space_size);
                int columnWidth = (mGridView.getWidth() - columnSpace * (numCount - 1)) / numCount;
                mImageAdapter.setItemSize(columnWidth);

                if (mFolderPopupWindow != null) {
                    mFolderPopupWindow.setHeight(height * 5 / 7);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        super.onConfigurationChanged(newConfig);

    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            mTmpFile = FileUtils.createTmpFile(getActivity());
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        } else {
            Toast.makeText(getContext(), getString(R.string.no_image), Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onEventMainThread(RefreshImageSelect imageSelect) {
        selectImageFromGrid(imageSelect.getImage(), mode);
    }

    /**
     * 选择图片操作
     *
     * @param image
     */
    private void selectImageFromGrid(Image image, int mode) {
        if (image != null) {
            // 多选模式
            if (mode == Constants.MODE_MULTI) {
                if (resultList.contains(image.path)) {
                    resultList.remove(image.path);
                    if (resultList.size() != 0) {
                        mPreviewBtn.setEnabled(true);
                        mPreviewBtn.setText(getString(R.string.preview) + "(" + resultList.size() + ")");
                    } else {
                        mPreviewBtn.setEnabled(false);
                        mPreviewBtn.setText(getString(R.string.preview));
                    }
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.path);
                    }
                } else {
                    // 判断选择数量问题
                    if (mDesireImageCount == resultList.size()) {
                        Toast.makeText(getContext(), getString(R.string.image_amount_limit), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    resultList.add(image.path);
                    mPreviewBtn.setEnabled(true);
                    mPreviewBtn.setText(getString(R.string.preview) + "(" + resultList.size() + ")");
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.path);
                    }
                }
                mImageAdapter.select(image);
            }
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'", null,
                        IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data != null) {
                mAllImageList = new ArrayList<>();
                int count = data.getCount();
                if (count > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                        long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                        Image image = new Image(path, name, dateTime);
                        mAllImageList.add(image);
                        if (!hasFolderGened) {
                            // 获取文件夹名称
                            File imageFile = new File(path);
                            File folderFile = imageFile.getParentFile();
                            Folder folder = new Folder();
                            folder.name = folderFile.getName();
                            folder.path = folderFile.getAbsolutePath();
                            folder.cover = image;
                            if (!mResultFolder.contains(folder)) {
                                List<Image> imageList = new ArrayList<>();
                                imageList.add(image);
                                folder.images = imageList;
                                mResultFolder.add(folder);
                            } else {
                                // 更新
                                Folder f = mResultFolder.get(mResultFolder.indexOf(folder));
                                f.images.add(image);
                            }
                        }
                    } while (data.moveToNext());

                    mImageAdapter.setData(mAllImageList);

                    // 设定默认选择
                    if (resultList != null && resultList.size() > 0) {
                        mImageAdapter.setDefaultSelected(resultList);
                    }

                    mFolderAdapter.setData(mResultFolder);
                    hasFolderGened = true;

                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };


    @Override
    public void onSelectImages(int i) {
        if (mImageAdapter.isShowCamera()) {
            // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
            if (i == 0) {
                // 如果图片大于等于9张，则不能再进行拍照操作
                if (resultList.size() < 9) {
                    showCameraAction();
                } else {
                    Toast.makeText(getContext(), getString(R.string.image_amount_limit), Toast.LENGTH_SHORT).show();
                }
            } else {
                // 正常操作
                Image image = mImageAdapter.getImageList().get(i - 1);
                selectImageFromGrid(image, mode);
            }
        } else {
            // 正常操作
            Image image = mImageAdapter.getImageList().get(i);
            selectImageFromGrid(image, mode);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.category_btn:
                if (mFolderPopupWindow == null) {
                    createPopupFolderList(mGridWidth, mGridHeight);
                }
                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    //mFolderPopupWindow.showAtLocation(mRelativeLayout, Gravity.BOTTOM, 0, mPopupAnchorView.getHeight());
                    mFolderPopupWindow.showAsDropDown(mPopupAnchorView);
                    int index = mFolderAdapter.getSelectIndex();
                    index = index == 0 ? index : index - 1;
                    folderList.setSelection(index);
                }
                break;
            case R.id.preview:
                Bundle bundle = new Bundle();
                bundle.putStringArrayList(Constants.EXTRA_RESULT, resultList);
                KUtils.actionStart(getActivity(), ImagePreviewActivity.class, bundle);
                getActivity().overridePendingTransition(R.anim.selecter_image_alpha_enter,
                        R.anim.selecter_image_alpha_exit);
                break;
            default:
                break;
        }
    }
}
