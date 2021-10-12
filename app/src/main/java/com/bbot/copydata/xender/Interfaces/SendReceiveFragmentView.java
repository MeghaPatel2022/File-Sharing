package com.bbot.copydata.xender.Interfaces;

import android.content.Context;

import com.bbot.copydata.xender.Services.HotSpotService;

import java.util.ArrayList;

public interface SendReceiveFragmentView {
    Context getContext();

    @SuppressWarnings("unused")
    void setFontProperty();

    void initView();

    void showMessage(String string);

    void onBackPressed();

    ArrayList<String> checkAndRequestPermissions();

    boolean isLocationEnabled();

    void requestLocationPermissionDialog();

    boolean hasWritePermission();

    void requestWritePermission();

    void bindService();

    void requestPermission(ArrayList<String> permissionRequires);

    void initConnectionAndSocket(HotSpotService.MyBinder service);

    void onAllItemRemoved();

    void setViewPager();
}
