package com.bbot.copydata.xender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.Adapter.FolderListAdapter;
import com.bbot.copydata.xender.Adapter.ImagePickAdapter;
import com.bbot.copydata.xender.Adapter.OnSelectStateListener;
import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.filter.FileFilter;
import com.bbot.copydata.xender.filter.callback.FilterResultCallback;
import com.bbot.copydata.xender.filter.entity.Directory;
import com.bbot.copydata.xender.filter.entity.ImageFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends BaseFragment {


    public static final String IS_NEED_CAMERA = "IsNeedCamera";
    public static final String IS_NEED_IMAGE_PAGER = "IsNeedImagePager";
    public static final String IS_TAKEN_AUTO_SELECTED = "IsTakenAutoSelected";

    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final int COLUMN_NUMBER = 3;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public ArrayList<ImageFile> mSelectedList = new ArrayList<>();

    List<ImageFile> imgDownloadList = new ArrayList<>();
    List<ImageFile> imgMainDownloadList = new ArrayList<>();
    List<ImageFile> imgMain1DownloadList = new ArrayList<>();
    GridLayoutManager layoutManager;
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private NestedScrollView scrollView;
    private RecyclerView mRecyclerView;
    private ImagePickAdapter mAdapter;
    private boolean isNeedCamera;
    private boolean isNeedImagePager;
    private boolean isTakenAutoSelected;
    private RelativeLayout rl_main;
    private List<Directory<ImageFile>> mAll;
    //    private TextView tv_count;
    private TextView tv_folder;
    private ImageView img_no_data;
    private LinearLayout ll_folder;
    private CardView mhl_folder;
    //    private RelativeLayout rl_done;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView rv_folder;
    private FolderListAdapter mbAdapter;
    private ImageView iv_folder;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private Handler mHandler;
    private MyReceiver r;


    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(String param1, String param2) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void permissionGranted() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        r = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        mMaxNumber = getActivity().getIntent().getIntExtra(Constant.MAX_NUMBER, DEFAULT_MAX_NUMBER);
        isNeedCamera = getActivity().getIntent().getBooleanExtra(IS_NEED_CAMERA, false);
        isNeedImagePager = getActivity().getIntent().getBooleanExtra(IS_NEED_IMAGE_PAGER, false);
        isTakenAutoSelected = getActivity().getIntent().getBooleanExtra(IS_TAKEN_AUTO_SELECTED, true);

        initView(view);
        loadData();

        return view;
    }

    private void initView(View view) {
//        tv_count = (TextView) view.findViewById(R.id.tv_count);
//        tv_count.setText(mCurrentNumber + "/" + mMaxNumber);
        iv_folder = view.findViewById(R.id.iv_folder);
        mhl_folder = view.findViewById(R.id.mhl_folder);
        rl_main = view.findViewById(R.id.rl_main);
        scrollView = view.findViewById(R.id.scrollView);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_image_pick);
        layoutManager = new GridLayoutManager(getContext(), COLUMN_NUMBER);
        mRecyclerView.setLayoutManager(layoutManager);
//        mRecyclerView.setNestedScrollingEnabled(false);
        mAdapter = new ImagePickAdapter(getContext(), isNeedCamera, isNeedImagePager, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        rv_folder = (RecyclerView) view.findViewById(com.bbot.copydata.xender.R.id.rv_folder);
        mbAdapter = new FolderListAdapter(getContext(), new ArrayList<Directory>());
        rv_folder.setLayoutManager(new LinearLayoutManager(getContext(),RecyclerView.VERTICAL,false));
        rv_folder.setAdapter(mbAdapter);
        mbAdapter.setListener(directory -> {
            if (mhl_folder.getVisibility() == View.GONE) {
                Constant.expand(mhl_folder);
            } else {
                Constant.collapse(mhl_folder);
            }
            iv_folder.setRotation(180);

            tv_folder.setText(directory.getName());

            mAdapter.clear();
            imgMainDownloadList.clear();
            for (Directory<ImageFile> dir : mAll) {
                if (dir.getPath().equals(directory.getPath())) {
                    List<Directory<ImageFile>> list = new ArrayList<>();
                    list.add(dir);
                    new LongOperation(list, directory.getId()).execute();
                    break;
                }
            }

//            scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
//
//                if (v.getChildAt(v.getChildCount() - 1) != null) {
//                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
//                            scrollY > oldScrollY) {
//
//                        visibleItemCount = layoutManager.getChildCount();
//                        totalItemCount = layoutManager.getItemCount();
//                        pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
//
//                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
//                            onScrolledToBottom();
//                        }
//                    }
//                }
//            });
        });
        img_no_data = view.findViewById(R.id.img_no_data);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<ImageFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, ImageFile file) {
                if (state) {
                    Constant.filePaths.add(file.getPath());
                    Constant.FileName.add(file.getName());
                    mSelectedList.add(file);
                    mCurrentNumber++;
                } else {
                    Constant.filePaths.remove(file.getPath());
                    Constant.FileName.remove(file.getName());
                    mSelectedList.remove(file);
                    mCurrentNumber--;
                }
                SendReceiveFragment.fragmentPlayListBinding.tvSelect.setText(Constant.filePaths.size() + " Selected ");
//                tv_count.setText(mCurrentNumber + "/" + mMaxNumber);
            }
        });

        ll_folder = (LinearLayout) view.findViewById(R.id.ll_folder);

        Log.e("LLLLL_Is: ", String.valueOf(isNeedImagePager));

        rl_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mhl_folder.getVisibility() == View.GONE) {
                    Constant.expand(mhl_folder);
                } else {
                    Constant.collapse(mhl_folder);
                }
                iv_folder.setRotation(180);
            }
        });
        tv_folder = (TextView) view.findViewById(R.id.tv_folder);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_TAKE_IMAGE:
                if (resultCode == RESULT_OK) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(mAdapter.mImagePath);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    getActivity().sendBroadcast(mediaScanIntent);

                    loadData();
                } else {
                    //Delete the record in Media DB, when user select "Cancel" during take picture
                    getContext().getContentResolver().delete(mAdapter.mImageUri, null, null);
                }
                break;
            case Constant.REQUEST_CODE_BROWSER_IMAGE:
                if (resultCode == RESULT_OK) {
                    ArrayList<ImageFile> list = data.getParcelableArrayListExtra(Constant.RESULT_BROWSER_IMAGE);
                    mCurrentNumber = list.size();
                    mAdapter.setCurrentNumber(mCurrentNumber);
//                    tv_count.setText(mCurrentNumber + "/" + mMaxNumber);
                    Constant.filePaths.clear();
                    Constant.FileName.clear();
                    mSelectedList.clear();
                    for (int i = 0; i < list.size(); i++) {
                        Constant.filePaths.add(list.get(i).getPath());
                        Constant.FileName.add(list.get(i).getName());
                    }
                    mSelectedList.addAll(list);

                    for (ImageFile file : mAdapter.getDataSet()) {
                        if (mSelectedList.contains(file)) {
                            file.setSelected(true);
                        } else {
                            file.setSelected(false);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    public void fillData(List<Directory> list) {
        mbAdapter.refresh(list);
    }

    private void loadData() {
        FileFilter.getImages(getActivity(), new FilterResultCallback<ImageFile>() {
            @Override
            public void onResult(List<Directory<ImageFile>> directories) {
                // Refresh folder list
                ArrayList<Directory> list = new ArrayList<>();
                list.addAll(directories);
                fillData(list);

                mAll = directories;
                new LongOperation(directories, directories.get(0).getId()).execute();

            }
        });
    }


    private void refreshData(List<Directory<ImageFile>> directories, String id) {
        boolean tryToFindTakenImage = isTakenAutoSelected;

        // if auto-select taken image is enabled, make sure requirements are met
        if (tryToFindTakenImage && !TextUtils.isEmpty(mAdapter.mImagePath)) {
            File takenImageFile = new File(mAdapter.mImagePath);
            tryToFindTakenImage = !mAdapter.isUpToMax() && takenImageFile.exists(); // try to select taken image only if max isn't reached and the file exists
        }

        List<ImageFile> list = new ArrayList<>();
        for (Directory<ImageFile> directory : directories) {
            if (directory.getId().equals(id)) {
                list.addAll(directory.getFiles());

                // auto-select taken images?
                if (tryToFindTakenImage) {
                    findAndAddTakenImage(directory.getFiles());   // if taken image was found, we're done
                }
                break;
            }
        }

        for (ImageFile file : mSelectedList) {
            int index = list.indexOf(file);
            if (index != -1) {
                list.get(index).setSelected(true);
            }
        }

        tv_folder.setText(directories.get(0).getName());

        Log.e("LLLL_size: ", String.valueOf(list.size()));
        imgDownloadList.clear();
        imgDownloadList = list;
        onScrolledToBottom();
    }

    private void onScrolledToBottom() {

        Log.e("LLL_DownSize: ", String.valueOf(imgDownloadList.size()));
        if (imgDownloadList.size() == 0) {
            getActivity().runOnUiThread(() -> {
                mRecyclerView.setVisibility(View.GONE);
            });
        } else {
            if (imgMain1DownloadList.size() < imgDownloadList.size()) {
                int x, y;
                if ((imgDownloadList.size() - imgMain1DownloadList.size()) >= 60) {
                    x = imgMain1DownloadList.size();
                    y = x + 60;
                } else {
                    x = imgMain1DownloadList.size();
                    y = x + imgDownloadList.size() - imgMain1DownloadList.size();
                }
                imgMainDownloadList = new ArrayList<>();
                for (int i = x; i < y; i++) {
                    imgMainDownloadList.add(imgDownloadList.get(i));
                    imgMain1DownloadList.add(imgDownloadList.get(i));
                }

                getActivity().runOnUiThread(() -> {
                    mAdapter.refresh(imgMainDownloadList);
                });
            }
        }
    }

    private boolean findAndAddTakenImage(List<ImageFile> list) {
        for (ImageFile imageFile : list) {
            if (imageFile.getPath().equals(mAdapter.mImagePath)) {
                mSelectedList.add(imageFile);
                Constant.filePaths.add(imageFile.getPath());
                Constant.FileName.add(imageFile.getName());
                mCurrentNumber++;
                mAdapter.setCurrentNumber(mCurrentNumber);
//                tv_count.setText(mCurrentNumber + "/" + mMaxNumber);

                return true;   // taken image was found and added
            }
        }
        return false;    // taken image wasn't found
    }

    private void refreshSelectedList(List<ImageFile> list) {
        for (ImageFile file : list) {
            if (file.isSelected() && !mSelectedList.contains(file)) {
                mSelectedList.add(file);
                Constant.filePaths.add(file.getPath());
                Constant.FileName.add(file.getName());
            }
        }
    }

    public void notifyData() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mbAdapter.refresh(new ArrayList<>());
                mAdapter.refresh(new ArrayList<>());
                mbAdapter.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
                mSelectedList.clear();
                Constant.filePaths.clear();
                Constant.FileName.clear();
                loadData();
                Log.e("LLLL_ImageNotify: ","Done");
                mbAdapter.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }

    private final class LongOperation extends AsyncTask<Void, Void, List<ImageFile>> {

        List<Directory<ImageFile>> directories;
        String id;

        public LongOperation(List<Directory<ImageFile>> directories, String id) {
            this.directories = directories;
            this.id = id;
        }

        @Override
        protected List<ImageFile> doInBackground(Void... params) {
            boolean tryToFindTakenImage = isTakenAutoSelected;

            // if auto-select taken image is enabled, make sure requirements are met
            if (tryToFindTakenImage && !TextUtils.isEmpty(mAdapter.mImagePath)) {
                File takenImageFile = new File(mAdapter.mImagePath);
                tryToFindTakenImage = !mAdapter.isUpToMax() && takenImageFile.exists(); // try to select taken image only if max isn't reached and the file exists
            }

            List<ImageFile> list = new ArrayList<>();
            for (Directory<ImageFile> directory : directories) {
                if (directory.getId().equals(id)) {
                    list.addAll(directory.getFiles());

                    // auto-select taken images?
                    if (tryToFindTakenImage) {
                        findAndAddTakenImage(directory.getFiles());   // if taken image was found, we're done
                    }
                    break;
                }
            }

            for (ImageFile file : mSelectedList) {
                int index = list.indexOf(file);
                if (index != -1) {
                    list.get(index).setSelected(true);
                }
            }
            getActivity().runOnUiThread(() -> tv_folder.setText(directories.get(0).getName()));

            return list;
        }

        @Override
        protected void onPostExecute(List<ImageFile> result) {
            if (result.size() == 0)
                img_no_data.setVisibility(View.VISIBLE);
            imgDownloadList.clear();
            imgDownloadList = result;
            getActivity().runOnUiThread(() -> {
                mAdapter.refresh(imgDownloadList);
            });
//            onScrolledToBottom();
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            notifyData();
        }
    }

}