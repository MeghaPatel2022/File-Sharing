package com.bbot.copydata.xender.AdsManager;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.OnLifecycleEvent;

import com.bbot.copydata.xender.Activity.MainActivity;
import com.bbot.copydata.xender.Application.MyApplicationClass;
import com.bbot.copydata.xender.Const.SharedPreference;
import com.bbot.copydata.xender.Welcome.IntroActivity;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;
import java.util.Random;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

public class AppOpenManager implements Application.ActivityLifecycleCallbacks {

    private final FirebaseAnalytics mFirebaseAnalytics;
    private static final String LOG_TAG = "AppOpenManager";
    private static final String AD_UNIT_ID = "ca-app-pub-1599168469857155/5761792445";
    private static boolean isShowingAd = false;
    private final MyApplicationClass myApplication;
    private final Context mContext;
    private AppOpenAd appOpenAd = null;
    private AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private Activity currentActivity = null;

    private long loadTime = 0;

    /**
     * Constructor
     */
    public AppOpenManager(MyApplicationClass myApplication) {
        this.myApplication = myApplication;
        mContext = myApplication.getApplicationContext();
        this.myApplication.registerActivityLifecycleCallbacks(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {
                    /**
                     * Called when an app open ad has loaded.
                     *
                     * @param ad the loaded app open ad.
                     */
                    @Override
                    public void onAppOpenAdLoaded(AppOpenAd ad) {
                        fireAnalyticsAds("admob_openApp", "loaded");
                        AppOpenManager.this.appOpenAd = ad;
                        AppOpenManager.this.loadTime = (new Date()).getTime();
                        showAdIfAvailable();
                        currentActivity = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        super.onAdLoaded(appOpenAd);
                        fireAnalyticsAds("admob_openApp", "loaded");
                        AppOpenManager.this.appOpenAd = appOpenAd;
                        AppOpenManager.this.loadTime = (new Date()).getTime();
                        showAdIfAvailable();
                        currentActivity = null;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        AppOpenManager.this.appOpenAd = null;
                        isShowingAd = false;
                        if (loadAdError.getMessage() != null)
                            fireAnalyticsAds("admob_openApp_Error", loadAdError.getMessage());
                        if (SharedPreference.getLogin(currentActivity)) {
                            Intent intent = new Intent(currentActivity, MainActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                        } else {
                            Intent intent = new Intent(currentActivity, IntroActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                        }
                    }

                    /**
                     * Called when an app open ad has failed to load.
                     *
                     * @param loadAdError the error.
                     */
                    @Override
                    public void onAppOpenAdFailedToLoad(LoadAdError loadAdError) {
                        // Handle the error.
                        AppOpenManager.this.appOpenAd = null;
                        isShowingAd = false;
                        if (loadAdError.getMessage() != null)
                            fireAnalyticsAds("admob_openApp_Error", loadAdError.getMessage());
                        if (SharedPreference.getLogin(currentActivity)) {
                            Intent intent = new Intent(currentActivity, MainActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                        } else {
                            Intent intent = new Intent(currentActivity, IntroActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                        }
                    }
                };

        AdRequest request = getAdRequest();
        Random rand = new Random();
        int number = rand.nextInt(5) + 1;
        Log.e("LLL_randome: ", String.valueOf(number));
        if (number == 1) {
            fireAnalyticsAds("admob_openApp", "Ad Request send");
            AppOpenAd.load(
                    myApplication, AD_UNIT_ID, request,
                    AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);
        } else {
            if (SharedPreference.getLogin(myApplication)) {
                Intent intent = new Intent(myApplication, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myApplication.startActivity(intent);
            } else {
                Intent intent = new Intent(myApplication, IntroActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                myApplication.startActivity(intent);
            }
        }
    }

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(ON_START)
    public void onStart() {
//        showAdIfAvailable();
        Log.d(LOG_TAG, "onStart");
    }


    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    /**
     * ActivityLifecycleCallback methods
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;
        showAdIfAvailable();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;

    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            if (SharedPreference.getLogin(currentActivity)) {
                                Intent intent = new Intent(currentActivity, MainActivity.class);
                                currentActivity.startActivity(intent);
                                currentActivity.finish();
                            } else {
                                Intent intent = new Intent(currentActivity, IntroActivity.class);
                                currentActivity.startActivity(intent);
                                currentActivity.finish();
                            }
//                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            if (SharedPreference.getLogin(currentActivity)) {
                                Intent intent = new Intent(currentActivity, MainActivity.class);
                                currentActivity.startActivity(intent);
                                currentActivity.finish();
                            } else {
                                Intent intent = new Intent(currentActivity, IntroActivity.class);
                                currentActivity.startActivity(intent);
                                currentActivity.finish();
                            }
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appOpenAd.show(currentActivity, fullScreenContentCallback);

        }
//        else {
//            Log.d(LOG_TAG, "Can not show ad.");
//            fetchAd();
//        }
    }

}
