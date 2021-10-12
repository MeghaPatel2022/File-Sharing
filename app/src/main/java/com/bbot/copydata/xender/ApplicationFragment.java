package com.bbot.copydata.xender;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.bbot.copydata.xender.Adapter.ApplicationListAdapter;
import com.bbot.copydata.xender.Adapter.PackageAdapter;
import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.Model.ApplicationModel;
import com.bbot.copydata.xender.filter.entity.Directory;
import com.bbot.copydata.xender.filter.entity.NormalFile;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.provider.BaseColumns._ID;
import static android.provider.MediaStore.Files.FileColumns.MIME_TYPE;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DATE_ADDED;
import static android.provider.MediaStore.MediaColumns.SIZE;
import static android.provider.MediaStore.MediaColumns.TITLE;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ApplicationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicationFragment extends BaseFragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<ApplicationModel> applicationModels = new ArrayList<>();
    ArrayList<ApplicationModel> systemApplicationModels = new ArrayList<>();

    @BindView(R.id.segmented)
    SegmentedButtonGroup segmented;
    @BindView(R.id.rv_installed_package)
    RecyclerView rv_installed_package;
    @BindView(R.id.rv_package)
    RecyclerView rv_package;
    @BindView(R.id.rv_system_installed_package)
    RecyclerView rv_system_installed_package;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.tv_app)
    TextView tv_app;
    @BindView(R.id.tv_sys_app)
    TextView tv_sys_app;
    @BindView(R.id.rl_progress)
    RelativeLayout rl_progress;
    @BindView(R.id.avi)
    AVLoadingIndicatorView avi;
    @BindView(R.id.img_no_data)
    ImageView img_no_data;
    List<NormalFile> selectedList = new ArrayList<>();
    List<Directory<NormalFile>> mAll = new ArrayList<>();
    private int mCurrentNumber = 0;
    private String[] mSuffix;
    private ArrayList<NormalFile> mSelectedList = new ArrayList<>();

    private ApplicationListAdapter applicationListAdapter;
    private ApplicationListAdapter sysApplicationListAdapter;
    private PackageAdapter mAdapter;

    private ContentResolver mContentResolver;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MyReceiver r;

    public ApplicationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ApplicationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ApplicationFragment newInstance(String param1, String param2) {
        ApplicationFragment fragment = new ApplicationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    static private Bitmap getBitmapFromDrawable(@NonNull Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
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
//        loadData();
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

        View view = inflater.inflate(R.layout.fragment_application, container, false);
        ButterKnife.bind(this, view);
        mContentResolver = getContext().getContentResolver();

        mSuffix = new String[]{"apk"};
        new LongOperation().execute();

        // Inflate the layout for this fragment
        segmented.setOnPositionChangedListener(position -> {
            // Handle stuff here

            if (segmented.getPosition() == 0) {
                rv_package.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                img_no_data.setVisibility(View.GONE);
            } else {
                if (selectedList.isEmpty())
                    img_no_data.setVisibility(View.VISIBLE);
                scrollView.setVisibility(View.GONE);
                rv_package.setVisibility(View.VISIBLE);
            }
        });

        // Installed Application Packages.
        rv_installed_package.setLayoutManager(new GridLayoutManager(getContext(), 4));
        applicationListAdapter = new ApplicationListAdapter(applicationModels, getActivity());
        rv_installed_package.setAdapter(applicationListAdapter);

        // System Application Packages.
        rv_system_installed_package.setLayoutManager(new GridLayoutManager(getContext(), 4));
        //sysApplicationListAdapter = new ApplicationListAdapter(systemApplicationModels, getActivity());
        //rv_system_installed_package.setAdapter(sysApplicationListAdapter);

        // Application Packages From Storage.
        rv_package.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mAdapter = new PackageAdapter(getContext(), 50);
        rv_package.setAdapter(mAdapter);

        loadData();

        mAdapter.setOnSelectStateListener((state, file) -> {
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

        });

        return view;

    }


    private void getApplications() {

        applicationModels.clear();
        systemApplicationModels.clear();
        // Get installed APK List and it's logo.
        final PackageManager pm = getActivity().getPackageManager();
        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {

            if ((packageInfo.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0) {
            } else {
                // It is installed by the user
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                }

                try {
                    Drawable icon = getActivity().getPackageManager().getApplicationIcon(packageInfo.packageName);
                    Bitmap bitmap = getBitmapFromDrawable(icon);
                    String appName = "";
                    PackageManager packageManagers= getContext().getApplicationContext().getPackageManager();
                    try {
                        appName = (String) packageManagers.getApplicationLabel(packageManagers.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA));
                        Log.e("LLL_PackageName: ",appName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    ApplicationModel applicationModel = new ApplicationModel();
                    applicationModel.setAppIcon(bitmap);
                    applicationModel.setAppName(appName+".apk");
                    applicationModel.setAppPath(packageInfo.sourceDir);

                    applicationModels.add(applicationModel);

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<ApplicationModel> getAppFolder(String filePath, ArrayList<ApplicationModel> docDataList) {

        File dir = new File(filePath);
        boolean success = true;
        if (success && dir.isDirectory()) {
            File[] listFile = dir.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isFile()) {
                    if (listFile[i].getAbsolutePath().contains(".apk") ||
                            listFile[i].getAbsolutePath().contains(".aab")) {

                        ApplicationModel applicationModel = new ApplicationModel();
                        PackageManager pm = getActivity().getPackageManager();
                        PackageInfo pi = pm.getPackageArchiveInfo(listFile[i].getParentFile().getAbsolutePath(), 0);

                        // the secret are these two lines....
                        pi.applicationInfo.sourceDir = listFile[i].getParentFile().getAbsolutePath();
                        pi.applicationInfo.publicSourceDir = listFile[i].getParentFile().getAbsolutePath();
                        //

                        Drawable APKicon = pi.applicationInfo.loadIcon(pm);
                        Bitmap bitmap = getBitmapFromDrawable(APKicon);

                        String AppName = (String) pi.applicationInfo.loadLabel(pm);

                        applicationModel.setAppPath(listFile[i].getParentFile().getAbsolutePath());
                        applicationModel.setAppName(AppName);
                        applicationModel.setAppIcon(bitmap);

                        docDataList.add(applicationModel);

                    }
                } else if (listFile[i].isDirectory()) {
                    getAppFolder(listFile[i].getAbsolutePath(), docDataList);
                }
            }
        }

        Collections.reverse(docDataList);
        return docDataList;
    }

    private void loadData() {
        List<Directory<NormalFile>> directories = new ArrayList<>();
        String[] FILE_PROJECTION = {
                //Base File
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,

                //Normal File
                MediaStore.Files.FileColumns.MIME_TYPE};

        Cursor data = mContentResolver.query(MediaStore.Files.getContentUri("external"), FILE_PROJECTION, null, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        while (data.moveToNext()) {
            String path = data.getString(data.getColumnIndexOrThrow(DATA));
            if (path != null && contains(path)) {
                //Create a File instance
                NormalFile file = new NormalFile();
                file.setId(data.getLong(data.getColumnIndexOrThrow(_ID)));
                file.setName(data.getString(data.getColumnIndexOrThrow(TITLE)));
                file.setPath(data.getString(data.getColumnIndexOrThrow(DATA)));
                file.setSize(data.getLong(data.getColumnIndexOrThrow(SIZE)));
                file.setDate(data.getLong(data.getColumnIndexOrThrow(DATE_ADDED)));

                file.setMimeType(data.getString(data.getColumnIndexOrThrow(MIME_TYPE)));

                //Create a Directory
                Directory<NormalFile> directory = new Directory<>();
                directory.setName(Util.extractFileNameWithSuffix(Util.extractPathWithoutSeparator(file.getPath())));
                directory.setPath(Util.extractPathWithoutSeparator(file.getPath()));

                if (!directories.contains(directory)) {
                    directory.addFile(file);
                    directories.add(directory);
                } else {
                    directories.get(directories.indexOf(directory)).addFile(file);
                }

            }
        }

        // Refresh folder list
        Log.e("LLLL_ApkData: ", String.valueOf(directories.size()));
        mAll = directories;
        List<NormalFile> list = new ArrayList<>();
        for (Directory<NormalFile> directory1 : directories) {
            list.addAll(directory1.getFiles());
        }

        for (NormalFile file1 : mSelectedList) {
            int index = list.indexOf(file1);
            if (index != -1) {
                list.get(index).setSelected(true);
            }
        }
        Log.e("LLLL_ApkData: ", String.valueOf(directories.size()));
        selectedList = list;
        Log.wtf("LLL_PackageSize: ", String.valueOf(selectedList.size()));
        if (!list.isEmpty())
            img_no_data.setVisibility(View.GONE);

        getActivity().runOnUiThread(() -> {
            mAdapter.refresh(selectedList);
        });

    }

    private boolean contains(String path) {
        String mSuffixRegex;
        mSuffixRegex = obtainSuffixRegex(mSuffix);
        String name = Util.extractFileNameWithSuffix(path);
        Pattern pattern = Pattern.compile(mSuffixRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    private String obtainSuffixRegex(String[] suffixes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < suffixes.length ; i++) {
            if (i ==0) {
                builder.append(suffixes[i].replace(".", ""));
            } else {
                builder.append("|\\.");
                builder.append(suffixes[i].replace(".", ""));
            }
        }
        return ".+(\\." + builder.toString() + ")$";
    }

    public void notifyData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.refresh(new ArrayList<>());
                mSelectedList.clear();
                Constant.filePaths.clear();
                Constant.FileName.clear();
                loadData();
                Log.e("LLLL_VideoNotify: ","Done");
                mAdapter.notifyDataSetChanged();

                applicationListAdapter.notifyDataSetChanged();
            }
        });
    }

    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }

    public void onResume() {
        super.onResume();

    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startAnim();
        }

        @Override
        protected String doInBackground(Void... params) {
            getApplications();
            return "Execute";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("Execute")) {
                getActivity().runOnUiThread(() -> {

                    if (!applicationModels.isEmpty())
                        img_no_data.setVisibility(View.GONE);

                    applicationListAdapter.notifyDataSetChanged();
//                    tv_sys_app.setText("System Apps (" + systemApplicationModels.size() + ")");
                    tv_app.setText("Apps (" + applicationModels.size() + ")");
                    stopAnim();
                });
            }
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ApplicationFragment.this.notifyData();
        }
    }


}