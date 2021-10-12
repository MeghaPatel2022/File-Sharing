package com.bbot.copydata.xender;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public abstract class BaseFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
    public static final String IS_NEED_FOLDER_LIST = "isNeedFolderList";
    private static final int RC_READ_EXTERNAL_STORAGE = 123;
    private static String TAG = BaseFragment.class.getName();
    protected FolderListHelper mFolderHelper;
    protected boolean isNeedFolderList;

    abstract void permissionGranted();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isNeedFolderList = getActivity().getIntent().getBooleanExtra(IS_NEED_FOLDER_LIST, true);
        if (isNeedFolderList) {
            mFolderHelper = new FolderListHelper();
            mFolderHelper.initFolderListView(getContext());
        }
        readExternalStorage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Read external storage file
     */
    @AfterPermissionGranted(RC_READ_EXTERNAL_STORAGE)
    private void readExternalStorage() {
        boolean isGranted = EasyPermissions.hasPermissions(getContext(), "android.permission.READ_EXTERNAL_STORAGE");
        if (isGranted) {
            permissionGranted();
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.vw_rationale_storage),
                    RC_READ_EXTERNAL_STORAGE, "android.permission.READ_EXTERNAL_STORAGE");
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        permissionGranted();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        // If Permission permanently denied, ask user again
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            if (EasyPermissions.hasPermissions(getContext(), "android.permission.READ_EXTERNAL_STORAGE")) {
                permissionGranted();
            } else {
                getActivity().finish();
            }
        }
    }

}
