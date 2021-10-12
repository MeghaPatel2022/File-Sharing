package com.bbot.copydata.xender.Interfaces;

import android.graphics.Bitmap;

public interface CallBackListener {
    void QRCodeGenerated(String SSID, Bitmap bitmap);

    void onWarning();

    void onItemClick(String SSID);

    void showQRCodeProgressbar();

    void hideProgress();
}
