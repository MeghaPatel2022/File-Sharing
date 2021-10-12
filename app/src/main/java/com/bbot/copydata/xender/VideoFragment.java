package com.bbot.copydata.xender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.Adapter.OnSelectStateListener;
import com.bbot.copydata.xender.Adapter.VideoPickAdapter;
import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.filter.FileFilter;
import com.bbot.copydata.xender.filter.callback.FilterResultCallback;
import com.bbot.copydata.xender.filter.entity.Directory;
import com.bbot.copydata.xender.filter.entity.VideoFile;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VideoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoFragment extends BaseFragment {

    public static final String THUMBNAIL_PATH = "FilePick";
    public static final String IS_NEED_CAMERA = "IsNeedCamera";
    public static final String IS_TAKEN_AUTO_SELECTED = "IsTakenAutoSelected";

    public static final int DEFAULT_MAX_NUMBER = 9;
    public static final int COLUMN_NUMBER = 3;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    List<VideoFile> imgDownloadList = new ArrayList<>();
    List<VideoFile> imgMainDownloadList = new ArrayList<>();
    List<VideoFile> imgMain1DownloadList = new ArrayList<>();
    GridLayoutManager layoutManager;
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private RecyclerView mRecyclerView;
    private VideoPickAdapter mAdapter;
    private RelativeLayout rl_progress;
    private AVLoadingIndicatorView avi;
    private boolean isNeedCamera;
    private boolean isTakenAutoSelected;
    private ArrayList<VideoFile> mSelectedList = new ArrayList<>();
    private List<Directory<VideoFile>> mAll;
    private ProgressBar mProgressBar;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
//    private TextView tv_count;
    private TextView tv_folder;
//    private RelativeLayout tb_pick;
    private ImageView img_no_data;
    private LinearLayout ll_folder;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MyReceiver r;

    public VideoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment VideoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoFragment newInstance(String param1, String param2) {
        VideoFragment fragment = new VideoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    void startAnim() {
        rl_progress.setVisibility(View.VISIBLE);
        avi.show();
    }

    void stopAnim() {
        rl_progress.setVisibility(View.GONE);
        avi.hide();
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
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        initView(view);
        loadData();

        // Inflate the layout for this fragment
        return view;
    }

    private void initView(View view) {
//        tv_count = (TextView) view.findViewById(R.id.tv_count);
//        tv_count.setText(mCurrentNumber + "/" + mMaxNumber);

        rl_progress = view.findViewById(R.id.rl_progress);
        avi = view.findViewById(R.id.avi);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_video_pick);
        layoutManager = new GridLayoutManager(getContext(), COLUMN_NUMBER);
        mRecyclerView.setLayoutManager(layoutManager);

        mAdapter = new VideoPickAdapter(getContext(), isNeedCamera, mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        img_no_data = view.findViewById(R.id.img_no_data);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<VideoFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, VideoFile file) {
                if (state) {
                    mSelectedList.add(file);
                    Constant.filePaths.add(file.getPath());
                    Constant.FileName.add(file.getName());
                    mCurrentNumber++;
                } else {
                    mSelectedList.remove(file);
                    Constant.filePaths.remove(file.getPath());
                    Constant.FileName.remove(file.getName());
                    mCurrentNumber--;
                }
                SendReceiveFragment.fragmentPlayListBinding.tvSelect.setText(Constant.filePaths.size()+" Selected ");
            }
        });

        File folder = new File(getActivity().getExternalCacheDir().getAbsolutePath() + File.separator + THUMBNAIL_PATH);
        if (!folder.exists()) {
            startAnim();
        } else {
            stopAnim();
        }

        ll_folder = (LinearLayout) view.findViewById(R.id.ll_folder);
//        if (isNeedFolderList) {
//            ll_folder.setVisibility(View.GONE);
//            ll_folder.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    mFolderHelper.toggle(tb_pick);
//                }
//            });
//            tv_folder = (TextView) view.findViewById(R.id.tv_folder);
//            tv_folder.setText(getResources().getString(R.string.vw_all));
//            refreshData(mAll);
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_TAKE_VIDEO:
                if (resultCode == RESULT_OK) {
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File file = new File(mAdapter.mVideoPath);
                    Uri contentUri = Uri.fromFile(file);
                    mediaScanIntent.setData(contentUri);
                    getActivity().sendBroadcast(mediaScanIntent);

//                    loadData();
                }
                break;
        }
    }

    private void loadData() {
        FileFilter.getVideos(getActivity(), new FilterResultCallback<VideoFile>() {
            @Override
            public void onResult(List<Directory<VideoFile>> directories) {
//                mProgressBar.setVisibility(View.GONE);
                stopAnim();
                // Refresh folder list
                mAll = directories;
                if (directories.size() == 0)
                    img_no_data.setVisibility(View.VISIBLE);

                refreshData(directories);

            }
        });
    }

    private void refreshData(List<Directory<VideoFile>> directories) {
        boolean tryToFindTaken = isTakenAutoSelected;

        // if auto-select taken file is enabled, make sure requirements are met
        if (tryToFindTaken && !TextUtils.isEmpty(mAdapter.mVideoPath)) {
            File takenFile = new File(mAdapter.mVideoPath);
            tryToFindTaken = !mAdapter.isUpToMax() && takenFile.exists(); // try to select taken file only if max isn't reached and the file exists
        }

        List<VideoFile> list = new ArrayList<>();
        for (Directory<VideoFile> directory : directories) {
            list.addAll(directory.getFiles());

            // auto-select taken file?
            if (tryToFindTaken) {
                tryToFindTaken = findAndAddTaken(directory.getFiles());   // if taken file was found, we're done
            }
        }

        for (VideoFile file : mSelectedList) {
            int index = list.indexOf(file);
            if (index != -1) {
                list.get(index).setSelected(true);
            }
        }
        imgDownloadList.clear();
        imgDownloadList = list;
//        new LongOperation().execute();
        getActivity().runOnUiThread(() -> {
            mAdapter.refresh(imgDownloadList);
        });
    }

    private boolean findAndAddTaken(List<VideoFile> list) {
        for (VideoFile videoFile : list) {
            if (videoFile.getPath().equals(mAdapter.mVideoPath)) {
                mSelectedList.add(videoFile);
                Constant.filePaths.add(videoFile.getPath());
                Constant.FileName.add(videoFile.getName());
                mCurrentNumber++;
                mAdapter.setCurrentNumber(mCurrentNumber);
//                tv_count.setText(mCurrentNumber + "/" + mMaxNumber);

                return true;   // taken file was found and added
            }
        }
        return false;    // taken file wasn't found
    }


    // Bug: Repeat image done
    public void notifyData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAll = new ArrayList<>();
                imgDownloadList.clear();
                mAdapter.refresh(new ArrayList<>());
                mSelectedList.clear();
                Constant.filePaths.clear();
                Constant.FileName.clear();
                loadData();
                Log.e("LLLL_VideoNotify: ","Done");
                mAdapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            VideoFragment.this.notifyData();
        }
    }


}