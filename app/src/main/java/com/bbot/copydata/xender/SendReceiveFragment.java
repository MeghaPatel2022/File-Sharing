package com.bbot.copydata.xender;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bbot.copydata.xender.Adapter.CircularItemAdapter;
import com.bbot.copydata.xender.Application.MyApplicationClass;
import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.Database.DBHelper;
import com.bbot.copydata.xender.Interfaces.ApplicationKillListener;
import com.bbot.copydata.xender.Interfaces.CallBackListener;
import com.bbot.copydata.xender.Interfaces.SendReceiveFragmentView;
import com.bbot.copydata.xender.Interfaces.SendReceiveListPresenter;
import com.bbot.copydata.xender.Interfaces.TCPListener;
import com.bbot.copydata.xender.JSONHelper.JsonHelper;
import com.bbot.copydata.xender.Model.SendFileModel;
import com.bbot.copydata.xender.Services.AppKillDetectionService;
import com.bbot.copydata.xender.Services.HotSpotService;
import com.bbot.copydata.xender.Services.SendReceiveListPresenterImpl;
import com.bbot.copydata.xender.Services.TCPCommunicator;
import com.bbot.copydata.xender.Services.WiFiAddress;
import com.bbot.copydata.xender.Services.WifiHotSpots;
import com.bbot.copydata.xender.SharedPrefrance.PreferenceHelper;
import com.bbot.copydata.xender.databinding.FragmentSendReceiveBinding;
import com.blikoon.qrcodescanner.QrCodeActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;
import static android.net.wifi.WifiManager.WIFI_STATE_DISABLED;
import static com.bbot.copydata.xender.JSONHelper.JsonHelper.FILE;

public class SendReceiveFragment extends Fragment implements
        View.OnClickListener,
        SendReceiveFragmentView, CallBackListener, TCPListener, ApplicationKillListener {
    private FirebaseAnalytics mFirebaseAnalytics;

    private static final int ACTION_LOCATION_SOURCE_SETTINGS = 3;
    private static final int REQUEST_CODE_QR_SCAN = 5;
    public static boolean isSender, isReceiver;
    public static FragmentSendReceiveBinding fragmentPlayListBinding;
    public static TabsPagerAdapter tabsPagerAdapter;
    private final IntentFilter intentFilter = new IntentFilter();
    private final int PERMISSION_CAMERA_REQUEST_CODE = 4;
    private final int PERMISSION_REQUEST_CODE = 1;
    private final int PERMISSION_REQUEST_CODE_WRITE = 2;
    private final int PERMISSION_REQUEST_CODE_HOTSPOT = 7;
    private final int PERMISSION_REQUEST_TURN_OFF_HOTSPOT = 8;
    private final Handler pingPongHandler = new Handler();
    private final int[] icon = new int[]{R.drawable.ic_history, R.drawable.ic_apk, R.drawable.ic_image, R.drawable.ic_video, R.drawable.ic_music, R.drawable.ic_folder};
    private final int[] selectIcon = new int[]{R.drawable.ic_select_history, R.drawable.ic_select_apk, R.drawable.ic_select_image, R.drawable.ic_select_video, R.drawable.ic_select_music,
            R.drawable.ic_select_folder};
    ArrayList<SendFileModel> filesList = new ArrayList<>();
    boolean mBound = false;
    ServerSocket server;
    WifiManager mWifiManager;
    boolean isSocketConnected = false;
    BufferedWriter out = null;
    HotSpotService mService = null;
    Handler handler = new Handler();
    ConnectionDetector cd;
    boolean isInternetPresent = false;
    private AppCompatActivity mainActivity;
    private TCPCommunicator tcpClient;
    private TextView txtTitle;
    private SharedPreferences prefs;
    private boolean mShouldStartScan = false;
    private Socket socket;
    private Timer timer = new Timer();
    private String selectedItem, deviceName;
    private WifiHotSpots hotutil;
    private boolean isDeviceConnected = false;
    BroadcastReceiver wifiStatusChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Called now WiFiStatus", mWifiManager.getWifiState() + "");
            if (mShouldStartScan && mWifiManager.isWifiEnabled()) {
                scanHotSpots();
            } else {
                if (mWifiManager.getWifiState() == WIFI_STATE_DISABLED) {
                    if (isDeviceConnected) {
                        resetEverything("Wifi disabled");
                        handler.removeCallbacksAndMessages(null);
                    }
                }
            }
        }
    };
    private final ServiceConnection appCloseConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            setAppKillListener((AppKillDetectionService.MyBinder) service);
        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            resetEverything("Service close connection");
        }

    };
    /**
     * Following broadcast receiver is to listen the Location button toggle state in Android.
     */
    private final BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction() != null && intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                // Make an action or refresh an already managed state.

                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                if (locationManager != null) {
                    boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    if (isGpsEnabled || isNetworkEnabled) {
                        if (checkAndRequestPermissions().size() == 0) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(mainActivity.getApplicationContext())) {
                                requestWritePermission();
                            } else {
                                if (isSender)
                                    createHotSpot();
                                else if (isReceiver) {
                                    scanHotSpots();
                                }
                            }
                        }
                    } else {
                        resetEverything("Location off");
                    }

                }
            }
        }
    };
    private CircularItemAdapter adapter;
    BroadcastReceiver wifiScanner = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!mShouldStartScan) return;

            boolean success = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
            } else {
                success = true;
            }
            if (success && !isSocketConnected) {
                List<ScanResult> scanResults = filterResults(mWifiManager.getScanResults());

                if (scanResults.size() > 0) {
                    setAdapter(scanResults);
                    fragmentPlayListBinding.myCircularList.setAdapter(adapter);
                    handler.removeCallbacksAndMessages(null);
                    fragmentPlayListBinding.pulsator.stop();
                    fragmentPlayListBinding.txtNearPanda.setVisibility(View.GONE);
                    fragmentPlayListBinding.laySendReceive.setVisibility(View.GONE);
                    fragmentPlayListBinding.pulsator.setVisibility(View.GONE);
                    fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
                    fragmentPlayListBinding.layFilesView.setVisibility(View.GONE);
                    fragmentPlayListBinding.bntStop.setVisibility(View.VISIBLE);
                } else {
                    if (!isDeviceConnected)
                        scanFailure();
                }

            } else {
                if (!isDeviceConnected)
                    // scan failure handling
                    scanFailure();
            }
        }


    };
    private DBHelper dbHelper;
    private Dialog dialog1;
    private boolean isConnected = false;
    private WifiAPReceiver wifiAPReceiver;
    private String SSID;
    private Bitmap bitmap;
    private SendReceiveListPresenter playListPresenter;
    private final ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            playListPresenter.onServiceConnected((HotSpotService.MyBinder) service);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            if (fragmentPlayListBinding.layProgress.getVisibility() == View.VISIBLE) {
                fragmentPlayListBinding.layProgress.setVisibility(View.VISIBLE);
            }
            resetEverything("Service disconnected");
        }
    };
    private long mLastClickTime = 0, mLastConnectTime = 0;
    private AdView adView;

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    /**
     * Is location is on
     *
     * @return boolean
     */

    public static boolean locationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            return isGpsEnabled || isNetworkEnabled;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public Context getContext() {
        if (mainActivity != null)
            return mainActivity.getApplicationContext();
        else
            return getActivity().getApplicationContext();
    }

    void startAnim() {
        fragmentPlayListBinding.avi.smoothToShow();
    }

    void stopAnim() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (fragmentPlayListBinding != null)
                    fragmentPlayListBinding.avi.smoothToHide();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (AppCompatActivity) context;

    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (AppCompatActivity) activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentPlayListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_send_receive, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        dbHelper = new DBHelper(getContext());

        cd = new ConnectionDetector(getContext());
        isInternetPresent = cd.isConnectingToInternet();

        hotutil = new WifiHotSpots(mainActivity);
        mWifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        tcpClient = TCPCommunicator.getInstance();
        tcpClient.setContext(mainActivity);
        TCPCommunicator.addListener(this);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        IntentFilter apFilter = new IntentFilter();
        apFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");

        wifiAPReceiver = new WifiAPReceiver();
        mainActivity.registerReceiver(wifiAPReceiver, apFilter);
        playListPresenter = new SendReceiveListPresenterImpl(this);
        playListPresenter.onCreateView();

        return fragmentPlayListBinding.getRoot();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

    }

    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);


        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getContext(), adWidth);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_invite:
                fireAnalytics("Invite Friends", "Recommended");
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
            case R.id.tv_sendFile:
                fireAnalytics("Send File", "File");
                onSendFileCallBack();
                break;
            case R.id.txt_send:
                fireAnalytics("Receive Connection", "Request");
                isReceiver = true;
                isSender = false;
                mShouldStartScan = true;
                resetDeviceListView();
                if (checkAndRequestPermissions().size() == 0) {
                    if (!locationEnabled(mainActivity)) {
                        requestLocationPermissionDialog();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(getActivity().getApplicationContext())) {
                            requestWritePermission();
                        } else {
                            scanHotSpots();
                        }
                    }
                } else {
                    requestPermission(checkAndRequestPermissions());
                }
                break;
            case R.id.tv_disconnect:
            case R.id.txt_disconnect:
                fireAnalytics("Connection", "Disconnect");
                sendConnectionStatus();
                resetEverything("Disconnect button");
                break;
            case R.id.bnt_stop: {
                fireAnalytics("Connection", "Disconnect");
                sendConnectionStatus();
                resetEverything("Stop button");
            }
            break;
            case R.id.txt_send_files:
                break;
            case R.id.bnt_stop_receive:
                fireAnalytics("Connection", "Disconnect");
                resetEverything("Stop receive button");
                break;
            case R.id.txt_receive: {
                isSender = true;
                isReceiver = false;
                fireAnalytics("Send Connection", "Request");
                playListPresenter.onSendButtonClicked();
            }
            break;

        }
    }

    /**
     * Send Files to receiver from sender
     *
     * @param list files
     */
    @SuppressLint("NewApi")
    public void sendFiles(ArrayList<SendFileModel> list) {
        if (list.size() == 0) return;
        try {
            Runnable runnable = () -> {
                try {
                    showProgressDialog();
                    try {
                        updatePercentage("Sending...");
                        for (int i = 0; i < list.size(); i++) {

                            if (list.get(i).type == null || list.get(i).type.equals(FILE)) {
                                try {

                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(FILE, true);
                                    try {
                                        String outMsg = jsonObject.toString() + System.getProperty("line.separator");
                                        timer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                try {
                                                    out.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, 5000);
                                        out.write(outMsg);
                                        cancelTimer();
                                        out.flush();
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                SendFileModel sendFileModel = list.get(i);
                                ArrayList<File> files = new ArrayList<>(list.get(i).files);
                                ArrayList<String> filesName = list.get(i).fileName;

                                BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
                                DataOutputStream dos = new DataOutputStream(bos);

                                dos.writeInt(files.size());

                                for (int j = 0; j < files.size(); j++) {
                                    File file = files.get(j);
                                    long length = file.length();
                                    dos.writeLong(length);

                                    String name = filesName.get(j);
                                    dos.writeUTF(name);

                                    InputStream in = new FileInputStream(file);

                                    byte[] buf = new byte[1024];
                                    int len;

                                    while (length > 0 && (len = in.read(buf, 0, (int) Math.min(buf.length, length))) != -1) {
                                        dos.write(buf, 0, len);
                                        length -= len;
                                        System.out.println("Percentace: " + (int) (len / file.length() * 100) + "%");
                                    }
                                    dbHelper.insertHistoryData(file.getAbsolutePath(), filesName.get(j), 0);
                                    dos.flush();

                                }

                                Constant.filePaths.clear();
                                Constant.FileName.clear();
                                mainActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentPlayListBinding.tvSelect.setText("SELECTED");
                                    }
                                });
                                sendFileModel.isSend = true;
                                ArrayList<SendFileModel> sendFileModels = ((MyApplicationClass) (mainActivity.getApplicationContext())).getFilesList();
                                sendFileModels.set(0, sendFileModel);

                                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
                                Intent localIn = new Intent("TAG_REFRESH");
                                lbm.sendBroadcast(localIn);
                            }
                        }
                        ((MyApplicationClass) (mainActivity.getApplicationContext())).reset();
                        mainActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                                fragmentPlayListBinding.tvSelect.setText("SELECTED");
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                        mainActivity.runOnUiThread(() -> {
                            showMessage(mainActivity.getResources().getString(R.string.str_device_disconnected));
                            resetEverything("Send files");
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isLocationEnabled() {
        return locationEnabled(mainActivity);
    }

    private void scanHotSpots() {
        if (isWifiApEnabled()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestHotSpotDisable();
            } else {
                startHotSpot(false);
                setWifiEnabled();
            }
        } else {
            setWifiEnabled();
            Log.d("OnScanHotspot", "true");
            fragmentPlayListBinding.layReceiver.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.pulsator.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.pulsator.start();
            fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
            getHotSpotsList();
        }
    }

    private void requestHotSpotDisable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.AlertDialogTheme);
        builder.setCancelable(false);
        builder.setTitle(mainActivity.getResources().getString(R.string.str_turn_off_hotspot));
        builder.setMessage(mainActivity.getResources().getString(R.string.str_turn_off_wifi));
        builder.setPositiveButton("Yes", (dialog, which) -> {
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.TetherSettings");
            intent.setComponent(cn);
            startActivityForResult(intent, PERMISSION_REQUEST_CODE_HOTSPOT);
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            resetEverything("No button");
            dialog.cancel();
        });

        builder.show();
    }

    public void getHotSpotsList() {
        handler.post(() -> {
            setWifiEnabled();
            Log.d("OnGetHotspot", "true");
            fragmentPlayListBinding.laySender.setVisibility(View.GONE);
            fragmentPlayListBinding.layReceiver.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.laySendReceive.setVisibility(View.GONE);
            fragmentPlayListBinding.layFilesView.setVisibility(View.GONE);
            fragmentPlayListBinding.myCircularList.removeAllViews();
            mWifiManager.startScan();
        });

    }

    public void setWifiEnabled() {
        if (!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);
    }

    /**
     * Change Mobile Data status
     *
     * @param mobileDataEnabled boolean
     */
    public void setMobileDataState(boolean mobileDataEnabled) {
        try {
            TelephonyManager telephonyService = (TelephonyManager) mainActivity.getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Method to turn ON/OFF a  Access Point
     *
     * @param enable Put true if you want to start  Access Point
     */
    public void startHotSpot(boolean enable) {
        Method[] mMethods = mWifiManager.getClass().getDeclaredMethods();
        for (Method mMethod : mMethods) {
            if (mMethod.getName().equals("setWifiApEnabled")) {
                try {
                    mMethod.invoke(mWifiManager, null, enable);
                    return;
                } catch (Exception ignored) {
                }
                break;
            }
        }
    }

    /**
     * @return true if Wifi Access Point Enabled
     */
    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApState");
            return ((int) method.invoke(mWifiManager) == 13 || (int) method.invoke(mWifiManager) == 12);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void resetDeviceListView() {
        fragmentPlayListBinding.myCircularList.removeAllViews();
        adapter = new CircularItemAdapter(this, new ArrayList<>());
        fragmentPlayListBinding.myCircularList.setAdapter(adapter);
    }

    /**
     * Send Connection Status to receiver
     */
    private void sendConnectionStatus() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JsonHelper.connected, false);
            Runnable runnable = () -> {
                try {
                    String outMsg = jsonObject.toString() + System.getProperty("line.separator");
                    scheduleTimer();
                    out.write(outMsg);
                    cancelTimer();
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            this.isDeviceConnected = false;
            server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAppKillListener(AppKillDetectionService.MyBinder service) {
        service.addListener(this);
    }

    /**
     * Init Connection and Socket
     *
     * @param service
     */
    @Override
    public void initConnectionAndSocket(HotSpotService.MyBinder service) {
        HotSpotService.MyBinder binder = service;
        mService = binder.getService();
        mService.addListener(this);
        createHotSpot();
        new InitTCPClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void createHotSpot() {
        if (mService != null && isSender) {
            mService.createHotSpot();
        }
    }

    @Override
    public void onTCPMessageReceived(String message) {
        if (mainActivity == null) return;
        mainActivity.runOnUiThread(() -> {
            try {
                Log.d("onTCPMessageReceived", message);
                JSONObject jsonObject = new JSONObject(message);
                if (jsonObject.has(JsonHelper.connected)) {
                    isDeviceConnected = jsonObject.getBoolean(JsonHelper.connected);


                    if (!isDeviceConnected) {
                    } else {

                        fragmentPlayListBinding.layEmptyView.setVisibility(View.GONE);

                    }

                } else if (jsonObject.has(JsonHelper.PingPong)) {

                    dismissDialog();
                    isDeviceConnected = true;

                    fragmentPlayListBinding.layConnected.setVisibility(View.GONE);
                    fragmentPlayListBinding.layHeader.setVisibility(View.GONE);


                    fragmentPlayListBinding.layEmptyView.setVisibility(View.GONE);

                    JSONObject jsonStatus = new JSONObject();
                    jsonStatus.put(JsonHelper.connected, true);
                    TCPCommunicator.writeToSocket(jsonStatus.toString(), new Handler());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

    }

    private void showLongTapDialog() {

        dialog1 = new Dialog(mainActivity);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog1.setContentView(R.layout.dialog_detail_med);
        dialog1.getWindow().setLayout(mainActivity.getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.setCancelable(true);
        dialog1.setCanceledOnTouchOutside(false);

        ImageView img_done = dialog1.findViewById(R.id.img_done);

        img_done.setOnClickListener(v -> {
            dialog1.dismiss();

            mainActivity.runOnUiThread(() -> {
                fragmentPlayListBinding.layProgress.setVisibility(View.VISIBLE);
                startAnim();
                fragmentPlayListBinding.txtMesssage.setVisibility(View.GONE);
            });

            fragmentPlayListBinding.rlConnected.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
            fragmentPlayListBinding.layFilesView.setVisibility(View.GONE);
            fragmentPlayListBinding.layReceiver.setVisibility(View.GONE);
            fragmentPlayListBinding.laySender.setVisibility(View.GONE);
            fragmentPlayListBinding.layConnected.setVisibility(View.GONE);
            fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
            fragmentPlayListBinding.longTap.setVisibility(View.GONE);
            fragmentPlayListBinding.layEmptyView.setVisibility(View.GONE);
            new Handler().postDelayed(() -> mainActivity.runOnUiThread(() -> {
                fragmentPlayListBinding.txtMesssage.setVisibility(View.VISIBLE);
                stopAnim();
                setViewPager();
                fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
            }), 3000);


//                Intent intent = new Intent(getActivity(),SendFileActivity.class);
//                startActivity(intent);
        });

        dialog1.show();

//        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.Theme_AppCompat_Light_Dialog_Alert);
//        builder.setCancelable(false);
//        builder.setTitle(mainActivity.getResources().getString(R.string.str_note));
//        builder.setMessage("Device connected");
//        builder.setPositiveButton("OK", (dialog, which) -> {
//            dialog.cancel();
//        });
//        builder.show();
    }


    @Override
    public void onTCPConnectionStatusChanged(boolean isConnectedNow) {
        isSocketConnected = isConnectedNow;
        if (isConnectedNow) {
            mainActivity.runOnUiThread(() -> {
                try {
                    isDeviceConnected = true;
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(JsonHelper.connected, true);
                    jsonObject.put(JsonHelper.DeviceName, android.os.Build.MODEL);
                    TCPCommunicator.writeToSocket(jsonObject.toString() + "\n", new Handler());
                    handler.removeCallbacksAndMessages(null);
                    adapter = new CircularItemAdapter(this, new ArrayList<>());
                    fragmentPlayListBinding.myCircularList.setAdapter(adapter);
                    fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
                    fragmentPlayListBinding.layFilesView.setVisibility(View.VISIBLE);
                    fragmentPlayListBinding.layReceiver.setVisibility(View.GONE);
                    fragmentPlayListBinding.laySender.setVisibility(View.GONE);
                    fragmentPlayListBinding.layConnected.setVisibility(View.GONE);
                    fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
                    showLongTapDialog();
                    fragmentPlayListBinding.longTap.setVisibility(View.VISIBLE);
                    fragmentPlayListBinding.layEmptyView.setVisibility(View.GONE);

                    dismissDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        }
    }

    @Override
    public void onErrorMessage(String message) {
        if (mainActivity == null) return;
        mainActivity.runOnUiThread(() -> {
            if ((Build.MANUFACTURER.toUpperCase().contains("XIAOMI") || Build.MANUFACTURER.toUpperCase().contains("REDMI")) && isDeviceConnected && message != null && message.equalsIgnoreCase(mainActivity.getString(R.string.str_disconnection_message))) {
                showWiFiAssistDialog();
            }
            resetEverything("On error message");
            fragmentPlayListBinding.bntStop.setVisibility(View.GONE);
            hotutil.disconnect();
            fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
            fragmentPlayListBinding.laySendReceive.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.layFilesView.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.layReceiver.setVisibility(View.GONE);
            handler.removeCallbacksAndMessages(null);
            try {
                isSocketConnected = false;
                TCPCommunicator.closeStreams();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        Log.d("MainActivity", "error" + message + "");

    }

    /**
     * Show WiFi Assistance Dialog
     */
    private void showWiFiAssistDialog() {

        final Dialog dialog = new Dialog(mainActivity);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return;
        @SuppressLint("InflateParams") View child = inflater.inflate(R.layout.dialog_wifi_assistance_warning, null);

        TextView txtWarning = child.findViewById(R.id.txt_warning);
        TextView txtUser = child.findViewById(R.id.txt_user);
        TextView txtOff = child.findViewById(R.id.txt_off);
        TextView txtAssistantOn = child.findViewById(R.id.txt_assistant_on);
        TextView txtAssistantOff = child.findViewById(R.id.txt_assistant_off);
        Button btnOff = child.findViewById(R.id.btn_off);


        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str1 = new SpannableString(mainActivity.getString(R.string.str_redmi_xiomi_users));
        str1.setSpan(new ForegroundColorSpan(Color.RED), 0, str1.length(), 0);
        builder.append(str1);


        SpannableString str2 = new SpannableString(mainActivity.getString(R.string.str_turn_off_assistant));
        str2.setSpan((android.graphics.Typeface.BOLD), 12, 15, 0);
        str2.setSpan(new UnderlineSpan(), 35, str2.length(), 0);
        builder.append(str2);

        SpannableString str3 = new SpannableString(mainActivity.getString(R.string.str_connection_failure));
        str3.setSpan((android.graphics.Typeface.BOLD), 22, 24, 0);
        builder.append(str3);


        txtUser.setText(builder, TextView.BufferType.SPANNABLE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // Do something for lollipop and above versions
            txtWarning.setLetterSpacing(0.05f);
            txtUser.setLetterSpacing(0.05f);
            txtOff.setLetterSpacing(0.05f);
            txtAssistantOn.setLetterSpacing(0.05f);
            txtAssistantOff.setLetterSpacing(0.05f);
            btnOff.setLetterSpacing(0.05f);
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(child);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        btnOff.setOnClickListener(view -> {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_IP_SETTINGS), PERMISSION_REQUEST_TURN_OFF_HOTSPOT);
            dialog.dismiss();
        });

        dialog.show();

    }

    /**
     * Show WiFi Assistance Dialog
     */
    private void showAgainWiFiAssistDialog() {
        final Dialog dialog = new Dialog(mainActivity);
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater == null) return;
        @SuppressLint("InflateParams") View child = inflater.inflate(R.layout.dialog_wifi_assistance_again_warning, null);

        TextView txtWarning = child.findViewById(R.id.txt_warning);
        TextView txtUser = child.findViewById(R.id.txt_user);
        TextView txtOff = child.findViewById(R.id.txt_off);
        TextView txtOffVerify = child.findViewById(R.id.txt_off_verify);
        TextView txtYes = child.findViewById(R.id.txt_yes);
        TextView txtContinue = child.findViewById(R.id.txt_continue);


        SpannableStringBuilder builder = new SpannableStringBuilder();
        SpannableString str1 = new SpannableString(mainActivity.getString(R.string.str_redmi_xiomi_users));
        str1.setSpan(new ForegroundColorSpan(Color.RED), 0, str1.length(), 0);
        builder.append(str1);


        SpannableString str2 = new SpannableString(mainActivity.getString(R.string.str_turn_off_assistant));
        str2.setSpan((android.graphics.Typeface.BOLD), 12, 15, 0);
        str2.setSpan(new UnderlineSpan(), 35, str2.length(), 0);
        builder.append(str2);

        SpannableString str3 = new SpannableString(mainActivity.getString(R.string.str_connection_failure));
        str3.setSpan((android.graphics.Typeface.BOLD), 22, 24, 0);
        builder.append(str3);


        txtUser.setText(builder, TextView.BufferType.SPANNABLE);


        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(child);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        txtContinue.setOnClickListener(view -> {
            startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_IP_SETTINGS), PERMISSION_REQUEST_TURN_OFF_HOTSPOT);
            dialog.dismiss();
        });

        txtYes.setOnClickListener(v -> {
            dialog.dismiss();
        });
        dialog.show();


    }

    @Override
    public void showProgressDialog() {
        mainActivity.runOnUiThread(this::CreateProgressDialog);
    }

    @Override
    public void dismissDialog() {
        if (mainActivity != null) {
            mainActivity.runOnUiThread(() -> {
                stopAnim();
                if (fragmentPlayListBinding != null)
                    fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
            });
        }
    }

    @Override
    public void updatePercentage(String progress) {
        mainActivity.runOnUiThread(() -> {
            fragmentPlayListBinding.layProgress.setVisibility(View.VISIBLE);
            startAnim();
            fragmentPlayListBinding.txtMesssage.setText(progress);
        });

    }

    private void cancelTimer() {
        timer.cancel();
        timer = new Timer();
    }

    @Override
    public void showReceivedMessage(int totalFile) {
        mainActivity.runOnUiThread(() -> {
            Toast.makeText(mainActivity, totalFile + " Files saved in Files folder", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void fileReceived(long id, File file1) {
        mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity, "File received in DataSharing folder", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void CreateProgressDialog() {

        mainActivity.runOnUiThread(() -> {

            fragmentPlayListBinding.layProgress.setVisibility(View.VISIBLE);
            startAnim();

        });


    }

    private void scheduleTimer() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 1500);
    }

    /**
     * Stop UI,Socket and Service
     */
    private void resetEverything(String fromWho) {
        if (mService != null) {
            mService.closeConnection();
        }

        if (mainActivity != null)
            new WifiHotSpots(mainActivity).removeNetwork();
        try {
            if (server != null)
                server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fragmentPlayListBinding != null) {
            stopAnim();
            fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
        }
        try {
            getActivity().unbindService(connection);
            unBindNetwork();
        } catch (Exception e) {

        }

        isSender = false;
        isReceiver = false;
        isDeviceConnected = false;
        mShouldStartScan = false;
        Constant.filePaths = new ArrayList<>();
        Constant.FileName = new ArrayList<>();
        if (fragmentPlayListBinding != null) {
            fragmentPlayListBinding.tvSelect.setText("Selected");
            fragmentPlayListBinding.rlConnected.setVisibility(View.GONE);
            fragmentPlayListBinding.myCircularList.removeAllViews();
            fragmentPlayListBinding.bntStop.setVisibility(View.GONE);
            fragmentPlayListBinding.pulsator.stop();
            fragmentPlayListBinding.layConnected.setVisibility(View.GONE);
            fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
            fragmentPlayListBinding.layReceiver.setVisibility(View.GONE);
            fragmentPlayListBinding.laySender.setVisibility(View.GONE);
            fragmentPlayListBinding.layFilesView.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.laySendReceive.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.layEmptyView.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.longTap.setVisibility(View.GONE);

            fragmentPlayListBinding.imgQrcode.setImageBitmap(null);
        }

        cancelTimer();


        handler.removeCallbacksAndMessages(null);
        unBindNetwork();

        pingPongHandler.removeCallbacksAndMessages(null);

        if (isSocketConnected) {

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonHelper.connected, false);
                TCPCommunicator.writeToSocket(jsonObject.toString(), new Handler());
                handler.postDelayed(() -> hotutil.disconnect(), 1000);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TCPCommunicator.closeStreams();
                    }
                }, 1000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onAllItemRemoved() {
        if (isDeviceConnected) {
            fragmentPlayListBinding.longTap.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.layEmptyView.setVisibility(View.GONE);
        } else {
            fragmentPlayListBinding.longTap.setVisibility(View.GONE);
            fragmentPlayListBinding.layEmptyView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void setViewPager() {
        tabsPagerAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager(), getActivity());
        fragmentPlayListBinding.viewPager.setAdapter(tabsPagerAdapter);

        fragmentPlayListBinding.tabDownload.setupWithViewPager(fragmentPlayListBinding.viewPager);

        for (int i = 0; i < fragmentPlayListBinding.tabDownload.getTabCount(); i++) {
            TabLayout.Tab tab = fragmentPlayListBinding.tabDownload.getTabAt(i);
            Objects.requireNonNull(tab).setCustomView(tabsPagerAdapter.getTabView(i));
        }

        Objects.requireNonNull(fragmentPlayListBinding.tabDownload.getTabAt(0)).select();
        fragmentPlayListBinding.tabDownload.setTabIndicatorFullWidth(false);

        fragmentPlayListBinding.tabDownload.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == null) {
                    sendConnectionStatus();
                    resetEverything("Disconnect button");
                } else {
                    ImageView imageView = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.img_tab);
                    imageView.setImageDrawable(getContext().getResources().getDrawable(selectIcon[tab.getPosition()]));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab == null) {
                    sendConnectionStatus();
                    resetEverything("Disconnect button");
                } else {
                    ImageView imageView = Objects.requireNonNull(tab.getCustomView()).findViewById(R.id.img_tab);
                    imageView.setImageDrawable(getContext().getResources().getDrawable(icon[tab.getPosition()]));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        fragmentPlayListBinding.viewPager.setOffscreenPageLimit(5);
    }

    public void unBindNetwork() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity() != null) {
                ConnectivityManager manager = (ConnectivityManager) getActivity().getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                manager.bindProcessToNetwork(null);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ConnectivityManager.setProcessDefaultNetwork(null);
        }
    }

    private void resetImageAndButtons() {
        fragmentPlayListBinding.bntStop.setVisibility(View.VISIBLE);
        fragmentPlayListBinding.imgQrcode.setImageBitmap(null);
    }

    /**
     * Request write setting permission
     */
    @Override
    public void requestWritePermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        builder.setCancelable(false);
        builder.setTitle(getActivity().getResources().getString(R.string.str_require_permission));
        builder.setMessage(getActivity().getResources().getString(R.string.str_enable_write));
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + mainActivity.getPackageName()));
            startActivityForResult(intent, PERMISSION_REQUEST_CODE_WRITE);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            resetEverything("Write permission no button");
            dialog.cancel();
        });
        builder.show();

    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission(ArrayList<String> listPermissionsNeeded) {
        String[] array = new String[listPermissionsNeeded.size()];

        for (int i = 0; i < listPermissionsNeeded.size(); i++) {
            array[i] = listPermissionsNeeded.get(i);
        }
        if (listPermissionsNeeded.size() > 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    mainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) || ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(mainActivity, Manifest.permission.CAMERA)
            ) {
                prefs.edit().putBoolean(PreferenceHelper.PermissionAsked, true).apply();
                requestPermissions(
                        array,
                        PERMISSION_REQUEST_CODE
                );
            } else {
                if (isReceiver) {
                    if (prefs.getBoolean(PreferenceHelper.PermissionCameraAsked, false)) {
                        requestPermissionDialog();
                    } else {
                        prefs.edit().putBoolean(PreferenceHelper.PermissionCameraAsked, true).apply();
                        requestPermissions(
                                array,
                                PERMISSION_REQUEST_CODE
                        );
                    }
                } else {
                    if (prefs.getBoolean(PreferenceHelper.PermissionAsked, false)) {
                        requestPermissionDialog();
                    } else {
                        prefs.edit().putBoolean(PreferenceHelper.PermissionAsked, true).apply();
                        requestPermissions(
                                array,
                                PERMISSION_REQUEST_CODE
                        );
                    }
                }
            }
        }
    }

    /**
     * Permission dialog
     */
    private void requestPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.AlertDialogTheme);
        builder.setCancelable(false);
        builder.setTitle("Required Permission");
        builder.setMessage("Please enable the permissions in order to connect to device");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, PERMISSION_REQUEST_CODE);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            resetEverything("QR code no button");
            dialog.cancel();
        });
        builder.show();
    }

    /**
     * Show Location dialog
     */
    @Override
    public void requestLocationPermissionDialog() {
        new AlertDialog.Builder(mainActivity, R.style.AlertDialogTheme)
                .setTitle(R.string.title_location_services)
                .setCancelable(false)
                .setMessage(R.string.message_location_services)
                .setNegativeButton(R.string.action_cancel, (dialog, which) -> {
                    resetEverything("location cancel");
                })
                .setPositiveButton(R.string.action_enable, (dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, ACTION_LOCATION_SOURCE_SETTINGS);
                })
                .show();
    }

    @Override
    public boolean hasWritePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.System.canWrite(mainActivity.getApplicationContext());
        }
        return true;
    }

    @Override
    public ArrayList<String> checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT <= 21) {
            return new ArrayList<>();
        }
        int permissionCamera = ContextCompat.checkSelfPermission(
                mainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
        );
        int storage = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readStorage = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        int camera = ContextCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA);

        ArrayList<String> listPermissionsNeeded = new ArrayList<>();


        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (readStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (isReceiver) {

            if (camera != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }
        }


        return listPermissionsNeeded;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        playListPresenter.onStop();

    }

    @Override
    public void setFontProperty() {
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
    }

    @Override
    public void initView() {
        fragmentPlayListBinding.txtSend.setOnClickListener(this);
        fragmentPlayListBinding.txtReceive.setOnClickListener(this);
        fragmentPlayListBinding.bntStop.setOnClickListener(this);
        fragmentPlayListBinding.tvSendFile.setOnClickListener(this);
        fragmentPlayListBinding.tvDisconnect.setOnClickListener(this);
        fragmentPlayListBinding.bntStopReceive.setOnClickListener(this);
        fragmentPlayListBinding.txtDisconnect.setOnClickListener(this);
        fragmentPlayListBinding.txtSendFiles.setOnClickListener(this);
        fragmentPlayListBinding.imgInvite.setOnClickListener(this);
        mainActivity.registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        mainActivity.registerReceiver(wifiStatusChange, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        mainActivity.registerReceiver(wifiScanner, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        fragmentPlayListBinding.myCircularList.setLayoutManager(new GridLayoutManager(getContext(), 2));
        bindToService();

        if (isInternetPresent) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Step 1 - Create an AdView and set the ad unit ID on it.
                    if (mainActivity != null) {
                        adView = new AdView(mainActivity.getApplicationContext());
                        fireAnalyticsAds("admob_banner", "Ad Request send");
                        adView.setAdUnitId(getString(R.string.banner_ad));
                        adView.setAdListener(new AdListener() {
                            @Override
                            public void onAdFailedToLoad(LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                if (loadAdError.getMessage() != null)
                                    fireAnalyticsAds("admob_banner_Error", loadAdError.getMessage());
                            }

                            @Override
                            public void onAdLoaded() {
                                super.onAdLoaded();
                                fireAnalyticsAds("admob_banner", "loaded");
                            }
                        });
                        fragmentPlayListBinding.bannerContainer.addView(adView);
                        loadBanner();
                    }
                }
            }, 2000);
        }

        try {
            mainActivity.startService(new Intent(mainActivity, AppKillDetectionService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        sendConnectionStatus();
        resetEverything("On back pressed");
        if (fragmentPlayListBinding.pulsator.isStarted()) {
            fragmentPlayListBinding.laySendReceive.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.layFilesView.setVisibility(View.VISIBLE);
            fragmentPlayListBinding.layReceiver.setVisibility(View.GONE);
            fragmentPlayListBinding.pulsator.stop();
            handler.removeCallbacksAndMessages(null);
        }

    }

    /**
     * @param SSID ssid
     */
    private void onDeviceItemClick(String SSID) {
        if (adapter != null) {
            selectedItem = SSID;
            fragmentPlayListBinding.connectedDevice.setText(selectedItem);
            if (Build.VERSION.SDK_INT <= 21) {
                openQRCodeScan();
                return;
            }
            int permissionCamera = ContextCompat.checkSelfPermission(
                    mainActivity,
                    Manifest.permission.CAMERA
            );

            if (permissionCamera != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        mainActivity,
                        Manifest.permission.CAMERA
                )) {
                    prefs.edit().putBoolean(PreferenceHelper.PermissionCameraAsked, true).apply();
                    String[] array = new String[1];
                    array[0] = (Manifest.permission.CAMERA);
                    requestPermissions(array, PERMISSION_CAMERA_REQUEST_CODE);
                } else {
                    if (prefs.getBoolean(PreferenceHelper.PermissionCameraAsked, false)) {
                        showPermissionDialog();
                    } else {
                        prefs.edit().putBoolean(PreferenceHelper.PermissionCameraAsked, true).apply();
                        String[] array = new String[1];
                        array[0] = (Manifest.permission.CAMERA);
                        requestPermissions(array, PERMISSION_CAMERA_REQUEST_CODE);

                    }
                }
            } else {
                openQRCodeScan();
            }

        }
    }

    /**
     * Show Permission Dialog
     */
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, R.style.AlertDialogTheme);
        builder.setCancelable(false);
        builder.setTitle("Required Permission");
        builder.setMessage("Please enable the permissions in order to Scan the device");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
            intent.setData(uri);
            startActivityForResult(intent, PERMISSION_CAMERA_REQUEST_CODE);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            resetEverything("Scan permission");
            dialog.cancel();
        });
        builder.show();
    }

    /**
     * Open QR code Scanner
     */
    private void openQRCodeScan() {
        Intent i = new Intent(mainActivity, QrCodeActivity.class);
        startActivityForResult(i, REQUEST_CODE_QR_SCAN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE || requestCode == PERMISSION_REQUEST_CODE_WRITE || requestCode == ACTION_LOCATION_SOURCE_SETTINGS || requestCode == PERMISSION_REQUEST_CODE_HOTSPOT) {
            if (checkAndRequestPermissions().size() == 0) {
                if (!locationEnabled(mainActivity)) {
                    requestLocationPermissionDialog();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(mainActivity.getApplicationContext())) {
                        requestWritePermission();
                    } else {
                        if (isSender) {
                            bindService();
                        } else if (isReceiver) {
                            scanHotSpots();
                        }
                    }
                }
            }
        } else if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            int permissionCamera = ContextCompat.checkSelfPermission(
                    mainActivity,
                    Manifest.permission.CAMERA
            );

            if (permissionCamera == PackageManager.PERMISSION_GRANTED) {
                scanHotSpots();
            }
        } else if (requestCode == PERMISSION_REQUEST_TURN_OFF_HOTSPOT) {
            showAgainWiFiAssistDialog();
        } else {
            if (resultCode != RESULT_OK) {
                if (data == null)
                    return;
                //Getting the passed result
                String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
                if (result != null) {
                    AlertDialog alertDialog = new AlertDialog.Builder(mainActivity, R.style.AlertDialogTheme).create();
                    alertDialog.setTitle("Scan Error");
                    alertDialog.setMessage("QR Code could not be scanned");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                }
                return;

            }
            if (requestCode == REQUEST_CODE_QR_SCAN) {
                if (data == null)
                    return;
                //Getting the passed result
                String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String SSID = jsonObject.getString(JsonHelper.SSID);
                    String Password = jsonObject.getString(JsonHelper.Password);
                    if (selectedItem.equals(SSID)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                            boolean isConnected = hotutil.connectToHotspot(SSID, Password);
                            if (isConnected)
                                bindNetworkProcess(SSID, Password);
                            else {
                                stopAnim();
                                fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
                                Toast.makeText(mainActivity, "Failed to connect, Please try again...", Toast.LENGTH_SHORT).show();
                                resetEverything("QR code failed");
                            }
                        }

                    } else {
                        Toast.makeText(mainActivity.getApplicationContext(), "Scanned wrong QR code", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void setAdapter(List<ScanResult> scanResults) {
        adapter = new CircularItemAdapter(this, scanResults);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mainActivity == null) return;
        if (requestCode == PERMISSION_CAMERA_REQUEST_CODE) {
            int permissionCamera = ContextCompat.checkSelfPermission(
                    mainActivity,
                    Manifest.permission.CAMERA
            );

            if (permissionCamera == PackageManager.PERMISSION_GRANTED) {
                scanHotSpots();
            } else {
                resetEverything("Camera permission denied");
            }
        } else if (checkAndRequestPermissions().size() == 0) {
            if (!locationEnabled(mainActivity)) {
                requestLocationPermissionDialog();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(mainActivity.getApplicationContext())) {
                    requestWritePermission();
                } else {
                    if (isSender) {
                        bindService();
                    } else if (isReceiver) {
                        scanHotSpots();
                    }
                }
            }
        } else {
            resetEverything("Permission denied");
        }
    }

    public void binAppKillService() {
        try {
            mainActivity.unbindService(appCloseConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(mainActivity, AppKillDetectionService.class);
        mainActivity.bindService(intent, appCloseConnection, Context.BIND_AUTO_CREATE);
    }

    public void bindToService() {
        try {
            mainActivity.unbindService(connection);
        } catch (Exception ignored) {
        }
        Intent intent = new Intent(mainActivity, HotSpotService.class);
        mainActivity.bindService(intent, connection, Context.BIND_WAIVE_PRIORITY | Context.BIND_AUTO_CREATE);
    }

    @Override
    public void hideProgress() {
        stopAnim();
        fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
    }

    @Override
    public void showQRCodeProgressbar() {
        Log.d("Show QR code", true + "");
        fragmentPlayListBinding.layProgress.setVisibility(View.VISIBLE);
        startAnim();
        fragmentPlayListBinding.txtMesssage.setText(getString(R.string.txt_generating));

    }

    private void scanFailure() {
        handler.postDelayed(() -> {
            if (mainActivity == null) return;
            mWifiManager = (WifiManager) mainActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            setWifiEnabled();
            getHotSpotsList();
        }, 7000);
    }

    public List<ScanResult> filterResults(List<ScanResult> scanResults) {
        List<ScanResult> scanResults1 = new ArrayList<>();
        if (scanResults == null) return null;
        for (int i = 0; i < scanResults.size(); i++) {
            ScanResult scanResult = scanResults.get(i);
            if (scanResult.SSID.startsWith("AndroidShare")) {
                scanResults1.add(scanResult);
            }
        }
        return scanResults1;
    }

    private void ConnectToServer(String host, String port) {
        if (SystemClock.elapsedRealtime() - mLastConnectTime < 1000) {
            return;
        }
        mLastConnectTime = SystemClock.elapsedRealtime();
        tcpClient.init(host,
                Integer.parseInt(port));
    }

    public void bindNetworkProcess(String netId, String netPass) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().runOnUiThread(() -> {

                fragmentPlayListBinding.layProgress.setVisibility(View.VISIBLE);
                startAnim();
                fragmentPlayListBinding.txtMesssage.setText(getString(R.string.txt_connecting));
//                WifiNetworkSpecifier wifiNetworkSpecifier = null;
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
//                    wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
//                            .setSsid(netId)
//                            .setWpa2Passphrase(netPass)
//                            .build();
//
//                    NetworkRequest networkRequest = new NetworkRequest.Builder()
//                            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
//                            .setNetworkSpecifier(wifiNetworkSpecifier)
//                            .build();
//                    ConnectivityManager connectivityManager = (ConnectivityManager) mainActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//                    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
//                        @Override
//                        public void onAvailable(@NotNull Network network) {
//                            super.onAvailable(network);
//
//                            isConnected = connectivityManager.bindProcessToNetwork(network);
//                            stopAnim();
//                            Log.e("LLLL_Wifi: ", "onAvailable");
//                        }
//
//                        @Override
//                        public void onLosing(@NonNull Network network, int maxMsToLive) {
//                            super.onLosing(network, maxMsToLive);
//                            Log.e("LLLL_Wifi: ", "onLosing");
//                            stopAnim();
//                        }
//
//                        @Override
//                        public void onLost(@NotNull Network network) {
//                            super.onLost(network);
//                            Log.e("LLLL_Wifi: ", "losing active connection");
//                            stopAnim();
//                        }
//
//                        @Override
//                        public void onUnavailable() {
//                            super.onUnavailable();
//                            Log.e("LLLL_Wifi: ", "onUnavailable");
//                            stopAnim();
//                        }
//
//                        @Override
//                        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
//                            super.onCapabilitiesChanged(network, networkCapabilities);
//                        }
//                    };
//                    connectivityManager.requestNetwork(networkRequest, networkCallback);
//
//                }


//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        getActivity().runOnUiThread(() -> {
//                            Toast.makeText(getActivity(), "Failed to connect", Toast.LENGTH_SHORT).show();
//                            resetEverything("Bind time out");
//                        });
//                    }
//                }, 20000);
                final ConnectivityManager manager = (ConnectivityManager) mainActivity.getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkRequest.Builder builder;
                builder = new NetworkRequest.Builder();
                //set the transport type do WIFI
                builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                manager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(Network network) {

                        cancelTimer();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            isConnected = manager.bindProcessToNetwork(network);
                            try {
                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(() -> {

                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 3000) {
                                        return;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();
                                    WiFiAddress address = new WiFiAddress(mainActivity);
                                    ConnectToServer(WiFiAddress.getGatewayIPAddress(), String.valueOf(6678));
                                }, 10000);
                                //do a callback or something else to alert your code that it's ok to send the message through socket now
                            } catch (Exception e) {
                                e.printStackTrace();
                                stopAnim();
                                fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
                            }
                        } else {
                            Log.d("Called multipel times", "Yes");
                            //This method was deprecated in API level 23
                            isConnected = ConnectivityManager.setProcessDefaultNetwork(network);
                            try {
                                final Handler handler = new Handler(Looper.getMainLooper());
                                handler.postDelayed(() -> {

                                    if (SystemClock.elapsedRealtime() - mLastClickTime < 3000) {
                                        return;
                                    }
                                    mLastClickTime = SystemClock.elapsedRealtime();
                                    WiFiAddress address = new WiFiAddress(mainActivity);
                                    ConnectToServer(WiFiAddress.getGatewayIPAddress(), String.valueOf(6678));
                                }, 5000);
                                //do a callback or something else to alert your code that it's ok to send the message through socket now
                            } catch (Exception e) {
                                e.printStackTrace();
                                stopAnim();
                                fragmentPlayListBinding.layProgress.setVisibility(View.GONE);
                            }

                        }
                        manager.unregisterNetworkCallback(this);


                    }
                });
            });
        }
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isSender)
            sendConnectionStatus();
        else {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JsonHelper.connected, false);
                TCPCommunicator.writeToSocket(jsonObject.toString(), new Handler());
                handler.postDelayed(() -> hotutil.disconnect(), 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        resetEverything("Activity destroyed");
        mainActivity.unregisterReceiver(mGpsSwitchStateReceiver);
        mainActivity.unregisterReceiver(wifiStatusChange);
        mainActivity.unregisterReceiver(wifiScanner);
        mainActivity = null;
        if (playListPresenter != null)
            playListPresenter.onDestroy();
        fragmentPlayListBinding = null;
        txtTitle = null;
        playListPresenter = null;
    }

    @Override
    public void onWarning() {
        requestHotSpotDisable();
    }

    @Override
    public void onItemClick(String SSID) {
        onDeviceItemClick(SSID);
    }

    @Override
    public void bindService() {
        bindToService();
    }

    @Override
    public void QRCodeGenerated(String SSID, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            this.SSID = SSID;
            this.bitmap = bitmap;
        } else {
            if (isWifiApEnabled()) {
                showQRCode(SSID, bitmap);
            } else {
                showMessage("Failed to create hotspot, please try again...");
            }

        }
    }

    private void showQRCode(String SSID, Bitmap bitmap) {
        fragmentPlayListBinding.laySender.setVisibility(View.VISIBLE);
        fragmentPlayListBinding.layReceiver.setVisibility(View.GONE);
        fragmentPlayListBinding.layFilesView.setVisibility(View.GONE);
        fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
        fragmentPlayListBinding.laySendReceive.setVisibility(View.GONE);
        fragmentPlayListBinding.bntStop.setVisibility(View.VISIBLE);
        if (isWifiApEnabled()) {
            fragmentPlayListBinding.imgQrcode.setImageBitmap(bitmap);
            fragmentPlayListBinding.txtDeviceId.setText(SSID);
        } else {
            showMessage("Failed to start hotspot, please try again...");
        }
        hideProgress();
    }

    @Override
    public void updateList() {

    }

    @Override
    public void onAppKilled() {
        Log.d("ApplicationKilled", "Killed");
    }


    public void onSendFileCallBack() {

        SendFileModel sendFileModel = new SendFileModel();
        Set<File> files = new HashSet<>();
        Set<String> filesname = new HashSet<>();
        for (int i = 0; i < Constant.filePaths.size(); i++) {
            files.add(new File(Constant.filePaths.get(i)));
            filesname.add(Constant.FileName.get(i));
            fireDetailAnalytics("Send", Constant.filePaths.get(i));
        }
        sendFileModel.files = new ArrayList<>(files);
        sendFileModel.fileName = new ArrayList<>(filesname);
        sendFileModel.type = FILE;
        filesList.add(sendFileModel);

        ArrayList<SendFileModel> map = (ArrayList<SendFileModel>) ((MyApplicationClass) (getActivity().getApplicationContext())).getFilesList().clone();
        map.add(sendFileModel);
        ((MyApplicationClass) (getActivity().getApplicationContext())).setMap(map);

        if (isSender) {
            sendFiles(filesList);
        } else if (isReceiver) {
            ArrayList<SendFileModel> sendFileModels = ((MyApplicationClass) (getActivity().getApplicationContext())).getFilesList();
            tcpClient.sendFiles(sendFileModels);
        }
    }

    private void fireAnalytics(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "SendReceiveFragment");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg1);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    private void fireDetailAnalytics(String arg1, String arg0) {
        Bundle params = new Bundle();
        params.putString("from", arg1);
        params.putString("image_path", arg0);
        mFirebaseAnalytics.logEvent("select_image", params);
    }

    @SuppressLint("StaticFieldLeak")
    public class InitTCPClientTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                server = new ServerSocket(6678);
                socket = server.accept();
                socket.setKeepAlive(true);

                if (mainActivity != null) {
                    mainActivity.runOnUiThread(() -> {
                        fragmentPlayListBinding.bntStop.setVisibility(View.VISIBLE);
                        fragmentPlayListBinding.imgQrcode.setImageBitmap(null);
                    });
                }
                String inMsg = "";

                InputStream in = socket.getInputStream();

                out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                BufferedReader receiveRead = new BufferedReader(new InputStreamReader(in));

                while ((receiveRead != null && (inMsg = receiveRead.readLine()) != null)) {
                    try {
                        Log.d("On Received", inMsg);
                        final JSONObject jsonObject = new JSONObject(inMsg);

                        if (jsonObject.has(JsonHelper.connected)) {
                            cancelTimer();
                            isDeviceConnected = false;
                            try {
                                isDeviceConnected = jsonObject.getBoolean(JsonHelper.connected);
                                if (jsonObject.has(JsonHelper.DeviceName)) {
                                    deviceName = jsonObject.getString(JsonHelper.DeviceName);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            mainActivity.runOnUiThread(() -> {
                                if (isDeviceConnected) {
                                    dismissDialog();
                                    showLongTapDialog();
                                    fragmentPlayListBinding.layReceiver.setVisibility(View.GONE);
                                    fragmentPlayListBinding.layFilesView.setVisibility(View.VISIBLE);
                                    fragmentPlayListBinding.layConnected.setVisibility(View.GONE);
                                    fragmentPlayListBinding.connectedDevice.setText(deviceName);

                                    fragmentPlayListBinding.layHeader.setVisibility(View.GONE);
                                    fragmentPlayListBinding.laySendReceive.setVisibility(View.GONE);
                                    fragmentPlayListBinding.laySender.setVisibility(View.GONE);
                                    fragmentPlayListBinding.longTap.setVisibility(View.VISIBLE);
                                    fragmentPlayListBinding.layEmptyView.setVisibility(View.GONE);

                                    resetImageAndButtons();
                                } else {
                                    resetEverything("Init tcp");
                                }

                            });

                        } else if (jsonObject.has(JsonHelper.FILE)) {
                            try {
                                File path1 = new File(Environment.getExternalStorageDirectory().toString() + "/DataSharing");
                                File file = new File(path1.getAbsolutePath());

                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                tcpClient.receiveFiles(new BufferedInputStream(socket.getInputStream()), file.getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Log.d("It's over", true + "");
            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dismissDialog();
        }

        private void schedulePingPong() {
//            pingPongHandler.postDelayed(() -> {
//                if (fragmentPlayListBinding != null && fragmentPlayListBinding.layProgress.getVisibility() == View.VISIBLE)
//                    sendPingPong();
//            }, 5000);
        }

    }

    public class WifiAPReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.net.wifi.WIFI_AP_STATE_CHANGED")) {
                int apState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

                Log.d("WifiStatus", apState + "");
                if (10 == apState) {
                    Log.d("Disconnecting device", true + "");
                    resetEverything("wifi status");
                    handler.removeCallbacksAndMessages(null);
                } else if (SSID != null) {
                    if (isWifiApEnabled()) {
                        showQRCode(SSID, bitmap);
                        SSID = null;
                        bitmap = null;
                    } else {
                        hideProgress();
                        showMessage("Failed to create hotspot, please try again...");
                    }
                }

            }
        }
    }

}
