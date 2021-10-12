package com.bbot.copydata.xender.Application;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.bbot.copydata.xender.AdsManager.AppOpenManager;
import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.Model.SendFileModel;
import com.google.android.gms.ads.MobileAds;
import com.onesignal.OneSignal;

import java.util.ArrayList;


public class MyApplicationClass extends MultiDexApplication {
    private static final String ONESIGNAL_APP_ID = "927d8a43-3c51-40d2-8676-e7926c5111e8";
    private static AppOpenManager appOpenManager;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    ArrayList<SendFileModel> filesList = new ArrayList<>();
    private Context context;

    @SuppressWarnings("EmptyMethod")
    @Override
    public void onCreate() {
        super.onCreate();

        // Admob Ads.
        MobileAds.initialize(
                this,
                initializationStatus -> {});

        appOpenManager = new AppOpenManager(this);

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(MyApplicationClass.this);
    }

    public void setMap(ArrayList<SendFileModel> filesList){
        this.filesList = filesList;
    }

    public void reset(){
        this.filesList.clear();
        filesList = new ArrayList<>();
        Constant.filePaths.clear();
        Constant.FileName.clear();
        Constant.filePaths = new ArrayList<>();
        Constant.FileName = new ArrayList<>();

    }
    public ArrayList<SendFileModel> getFilesList(){
        return filesList;
    }
}
