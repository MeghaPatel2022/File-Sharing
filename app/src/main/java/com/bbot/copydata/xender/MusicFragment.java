package com.bbot.copydata.xender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.Adapter.AudioPickAdapter;
import com.bbot.copydata.xender.Adapter.OnSelectStateListener;
import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.filter.FileFilter;
import com.bbot.copydata.xender.filter.callback.FilterResultCallback;
import com.bbot.copydata.xender.filter.entity.AudioFile;
import com.bbot.copydata.xender.filter.entity.Directory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MusicFragment#newInstance} factory method to
 * create an instance of getContext() fragment.
 */
public class MusicFragment extends BaseFragment {

    public static final String IS_NEED_RECORDER = "IsNeedRecorder";
    public static final String IS_TAKEN_AUTO_SELECTED = "IsTakenAutoSelected";

    public static final int DEFAULT_MAX_NUMBER = 9;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    protected boolean isNeedFolderList;
    LinearLayoutManager layoutManager;
    List<AudioFile> imgDownloadList = new ArrayList<>();
    List<AudioFile> imgMainDownloadList = new ArrayList<>();
    List<AudioFile> imgMain1DownloadList = new ArrayList<>();
    private int mMaxNumber;
    private int mCurrentNumber = 0;
    private RecyclerView mRecyclerView;
    private NestedScrollView scrollView;
    private AudioPickAdapter mAdapter;
    private boolean isNeedRecorder;
    private boolean isTakenAutoSelected;
    private ArrayList<AudioFile> mSelectedList = new ArrayList<>();
    private List<Directory<AudioFile>> mAll;
    private String mAudioPath;
    private ImageView img_no_data;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private TextView tv_folder;
    private LinearLayout ll_folder;
    private RelativeLayout rl_rec_aud;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MyReceiver r;

    public MusicFragment() {
        // Required empty public constructor
    }

    /**
     * Use getContext() factory method to create a new instance of
     * getContext() fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MusicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MusicFragment newInstance(String param1, String param2) {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void permissionGranted() {
        loadData();
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
        mMaxNumber = getActivity().getIntent().getIntExtra(Constant.MAX_NUMBER, DEFAULT_MAX_NUMBER);
        isNeedRecorder = getActivity().getIntent().getBooleanExtra(IS_NEED_RECORDER, false);
        isTakenAutoSelected = getActivity().getIntent().getBooleanExtra(IS_TAKEN_AUTO_SELECTED, true);
        isNeedFolderList = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for getContext() fragment
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        scrollView = view.findViewById(R.id.scrollView);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.rv_audio_pick);
        layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new AudioPickAdapter(getContext(), mMaxNumber);
        mRecyclerView.setAdapter(mAdapter);

        img_no_data = view.findViewById(R.id.img_no_data);

        mAdapter.setOnSelectStateListener(new OnSelectStateListener<AudioFile>() {
            @Override
            public void OnSelectStateChanged(boolean state, AudioFile file) {
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

        ll_folder = (LinearLayout) view.findViewById(R.id.ll_folder);

    }

    private void loadData() {
        FileFilter.getAudios(getActivity(), new FilterResultCallback<AudioFile>() {
            @Override
            public void onResult(List<Directory<AudioFile>> directories) {
                // Refresh folder list
                if (isNeedFolderList) {
                    ArrayList<Directory> list = new ArrayList<>();
                    Directory all = new Directory();
                    all.setName(getResources().getString(R.string.vw_all));
                    list.add(all);
                    list.addAll(directories);
                    mFolderHelper.fillData(list);
                }

                mAll = directories;

                if (directories.size() == 0)
                    img_no_data.setVisibility(View.VISIBLE);

                refreshData(directories);

                scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                        if (v.getChildAt(v.getChildCount() - 1) != null) {
                            if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                                    scrollY > oldScrollY) {

                                visibleItemCount = layoutManager.getChildCount();
                                totalItemCount = layoutManager.getItemCount();
                                pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                                if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                                    onScrolledToBottom();
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    private void refreshData(List<Directory<AudioFile>> directories) {
        boolean tryToFindTaken = isTakenAutoSelected;

        // if auto-select taken file is enabled, make sure requirements are met
        if (tryToFindTaken && !TextUtils.isEmpty(mAudioPath)) {
            File takenFile = new File(mAudioPath);
            tryToFindTaken = !mAdapter.isUpToMax() && takenFile.exists(); // try to select taken file only if max isn't reached and the file exists
        }

        List<AudioFile> list = new ArrayList<>();
        for (Directory<AudioFile> directory : directories) {
            list.addAll(directory.getFiles());

            // auto-select taken file?
            if (tryToFindTaken) {
                tryToFindTaken = findAndAddTaken(directory.getFiles());   // if taken file was found, we're done
            }
        }

        for (AudioFile file : mSelectedList) {
            int index = list.indexOf(file);
            if (index != -1) {
                list.get(index).setSelected(true);
            }
        }
        imgDownloadList.clear();
        imgDownloadList = list;
        onScrolledToBottom();
    }

    private void onScrolledToBottom() {

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
                imgMainDownloadList.clear();
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

    private boolean findAndAddTaken(List<AudioFile> list) {
        for (AudioFile audioFile : list) {
            if (audioFile.getPath().equals(mAudioPath)) {
                mSelectedList.add(audioFile);
                Constant.filePaths.add(audioFile.getPath());
                Constant.FileName.add(audioFile.getName());
                mCurrentNumber++;
                mAdapter.setCurrentNumber(mCurrentNumber);

                return true;   // taken file was found and added
            }
        }
        return false;    // taken file wasn't found
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_TAKE_AUDIO:
                if (resultCode == RESULT_OK) {
                    if (data.getData() != null) {
                        mAudioPath = data.getData().getPath();
                    }
                    loadData();
                }
                break;
        }
    }

    public void notifyData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
            MusicFragment.this.notifyData();
        }
    }


}