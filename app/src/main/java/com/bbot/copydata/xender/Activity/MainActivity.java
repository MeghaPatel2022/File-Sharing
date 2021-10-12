package com.bbot.copydata.xender.Activity;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bbot.copydata.xender.Interfaces.ResponseListener;
import com.bbot.copydata.xender.R;
import com.bbot.copydata.xender.SendReceiveFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ResponseListener {

    Fragment fragment;
    FragmentManager fragmentManager;

    private static final int RC_APP_UPDATE = 101;
    private AppUpdateManager mAppUpdateManager;
    InstallStateUpdatedListener installStateUpdatedListener = new
            InstallStateUpdatedListener() {
                @Override
                public void onStateUpdate(InstallState state) {
                    if (state.installStatus() == InstallStatus.DOWNLOADED) {
                        //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                        popupSnackbarForCompleteUpdate();
                    } else if (state.installStatus() == InstallStatus.INSTALLED) {
                        if (mAppUpdateManager != null) {
                            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
                        }

                    } else {
                        Log.i("LLL_Update_App: ", "InstallStateUpdatedListener: state: " + state.installStatus());
                    }
                }
            };

    @Override
    protected void onStart() {
        super.onStart();
        mAppUpdateManager = AppUpdateManagerFactory.create(this);

        mAppUpdateManager.registerListener(installStateUpdatedListener);

        mAppUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/)) {

                try {
                    mAppUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, AppUpdateType.FLEXIBLE /*AppUpdateType.IMMEDIATE*/, MainActivity.this, RC_APP_UPDATE);
                    Log.e("LLL_Update_App: ", "Update available");
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    Log.e("LLL_Update_App: ", e.getMessage());
                }

            } else if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                //CHECK THIS if AppUpdateType.FLEXIBLE, otherwise you can skip
                popupSnackbarForCompleteUpdate();
            } else {
                Log.e("LLL_Update_App: ", "checkForAppUpdateAvailability: something else");
            }
        });
    }

    private void popupSnackbarForCompleteUpdate() {

        Snackbar snackbar =
                Snackbar.make(
                        findViewById(R.id.drawerLayout),
                        "New app is ready!",
                        Snackbar.LENGTH_INDEFINITE);

        snackbar.setAction("Install", view -> {
            if (mAppUpdateManager != null) {
                mAppUpdateManager.completeUpdate();
            }
        });


        snackbar.setActionTextColor(getResources().getColor(R.color.purple_200));
        snackbar.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAppUpdateManager != null) {
            mAppUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        fragment = new SendReceiveFragment();
        setFragment(fragment);

        Calendar calendar = Calendar.getInstance();
//        new AlarmReceiver().setRepeatAlarm(getApplicationContext(), 1001, calendar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    protected void setFragment(Fragment fragment) {
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onResponse() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
