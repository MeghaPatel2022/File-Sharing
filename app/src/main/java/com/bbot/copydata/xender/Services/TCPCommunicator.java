package com.bbot.copydata.xender.Services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bbot.copydata.xender.Application.MyApplicationClass;
import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.Database.DBHelper;
import com.bbot.copydata.xender.Interfaces.TCPListener;
import com.bbot.copydata.xender.JSONHelper.JsonHelper;
import com.bbot.copydata.xender.Model.SendFileModel;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static com.bbot.copydata.xender.SendReceiveFragment.fragmentPlayListBinding;


public class TCPCommunicator {
    private static TCPCommunicator uniqInstance;
    private static String serverHost;
    private static int serverPort;
    private static Set<TCPListener> allListeners;
    private static BufferedWriter out;
    private static BufferedReader in;
    private static Socket s;
    private static Handler UIHandler;
    Handler handler = new Handler();
    DBHelper dbHelper;
    private Activity context;
    private FirebaseAnalytics mFirebaseAnalytics;

    private TCPCommunicator() {
        allListeners = new HashSet<>();
    }

    public static TCPCommunicator getInstance() {
        if (uniqInstance == null) {
            uniqInstance = new TCPCommunicator();
        }
        return uniqInstance;
    }

    public static void removeListener(TCPListener tcpListener) {
        if (allListeners != null)
            allListeners.remove(tcpListener);
    }

    public static void writeToSocket(final String obj, Handler handle) {
        UIHandler = handle;
        Runnable runnable = () -> {
            try {
                String outMsg = obj + System.getProperty("line.separator");
                out.write(outMsg);
                out.flush();
            } catch (Exception e) {
                UIHandler.post(e::printStackTrace);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    public static void addListener(TCPListener listener) {
        if (allListeners != null) {
            allListeners.clear();
            allListeners.add(listener);
        }
    }

    static void removeAllListeners() {
        if (allListeners != null)
            allListeners.clear();
    }

    public static void closeStreams() {
        try {
            s.close();
            in.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getServerHost() {
        return serverHost;
    }

    private static void setServerHost(String serverHost) {
        TCPCommunicator.serverHost = serverHost;
    }

    private static int getServerPort() {
        return serverPort;
    }

    private static void setServerPort(int serverPort) {
        TCPCommunicator.serverPort = serverPort;
    }

    /**
     * Send Files to sender from receiver
     *
     * @param list files
     */
    @SuppressLint("NewApi")
    public void sendFiles(ArrayList<SendFileModel> list) {
        if (list.size() == 0) return;
        for (TCPListener listener : allListeners)
            listener.CreateProgressDialog();
        try {
            Runnable runnable = () -> {
                try {
                    try {
                        for (TCPListener listener : allListeners)
                            listener.updatePercentage("Sending...");

                        for (int i = 0; i < list.size(); i++) {

                            if (list.get(i).type == null || list.get(i).type.equals(JsonHelper.FILE)) {
                                try {

                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(JsonHelper.FILE, true);
                                    try {
                                        String outMsg = jsonObject.toString() + System.getProperty("line.separator");
                                        out.write(outMsg);
                                        out.flush();
                                        Thread.sleep(1000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                SendFileModel sendFileModel = list.get(i);
                                ArrayList<File> files = new ArrayList<>();
                                files.addAll(list.get(i).files);

                                ArrayList<String> filesName = new ArrayList<>();
                                filesName.addAll(list.get(i).fileName);

                                BufferedOutputStream bos = new BufferedOutputStream(s.getOutputStream());
                                DataOutputStream dos = new DataOutputStream(bos);

                                dos.writeInt(files.size());
                                dos.flush();

                                for (int j = 0; j < files.size(); j++) {
                                    File file = files.get(j);

                                    long length = file.length();
                                    dos.writeLong(length);

                                    String name = filesName.get(j);
                                    dos.writeUTF(name);

                                    FileInputStream fis = new FileInputStream(file);
                                    BufferedInputStream bis = new BufferedInputStream(fis);

                                    byte buf[] = new byte[1024];
                                    int len;

                                    while (length > 0 && (len = bis.read(buf, 0, (int) Math.min(buf.length, length))) != -1) {
                                        dos.write(buf, 0, len);
                                        length -= len;
                                    }
                                    dbHelper.insertHistoryData(file.getAbsolutePath(), filesName.get(j), 0);
                                    dos.flush();
                                }

                                Constant.filePaths.clear();
                                Constant.FileName.clear();
                                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                                Intent localIn = new Intent("TAG_REFRESH");
                                lbm.sendBroadcast(localIn);
                                context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        fragmentPlayListBinding.tvSelect.setText("SELECTED");
                                    }
                                });

                                sendFileModel.isSend = true;
                                ArrayList<SendFileModel> sendFileModels = (ArrayList<SendFileModel>) ((MyApplicationClass) (context.getApplicationContext())).getFilesList().clone();
                                sendFileModels.set(0, sendFileModel);
                                ((MyApplicationClass) (context.getApplicationContext())).setMap(sendFileModels);

                                for (TCPListener listener : allListeners)
                                    listener.updateList();
                            }

                        }
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (TCPListener listener : allListeners)
                                    listener.dismissDialog();
                            }
                        });

                        ((MyApplicationClass) (context.getApplicationContext())).reset();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    public void init(String host, int port) {
        setServerHost(host);
        setServerPort(port);
        InitTCPClientTask task = new InitTCPClientTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void setContext(Activity context) {
        this.context = context;
        dbHelper = new DBHelper(context);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }
    
    public void receiveFiles(BufferedInputStream bis, String path1) throws Exception {

        for (TCPListener listener : allListeners)
            listener.updatePercentage("Receiving...");

        DataInputStream dis = new DataInputStream(bis);

        int filesCount = dis.readInt();

        for (int i = 0; i < filesCount; i++) {
            long fileSize = dis.readLong();
            String fileName = dis.readUTF();

            File f = new File(path1, fileName);

            f.createNewFile();

            Log.d("LL_Server: ", f.getAbsolutePath());

            byte buf[] = new byte[1024];
            int len;
            FileOutputStream outputStream = new FileOutputStream(f);
            while (fileSize > 0 && (len = dis.read(buf, 0, (int) Math.min(buf.length, fileSize))) != -1) {
                outputStream.write(buf, 0, len);
                fileSize -= len;
            }

            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
            MediaScannerConnection.scanFile(
                    context,
                    new String[]{f.getAbsolutePath()},
                    null,
                    (path, uri) -> {
                        long id = WifiHotSpots.getSongIdFromMediaStore(f.getPath(), context);
                        for (TCPListener listener : allListeners) {
                            listener.fileReceived(id, f);
                        }
                    });
            dbHelper.insertHistoryData(f.getAbsolutePath(), fileName, 1);

            outputStream.close();
        }

        Log.d("LL_Server: ", "Done");
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        Intent localIn = new Intent("TAG_REFRESH");
        lbm.sendBroadcast(localIn);

        fireAnalytics("Receive", "Files");

        for (TCPListener listener : allListeners)
            listener.dismissDialog();

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

                s = new Socket();
                s.setKeepAlive(true);
                s.setSoLinger(true, 1000);

                Log.e("LLLL_serverHost: ", getServerHost() + "   Port: " + getServerPort());
                SocketAddress remoteAddress = new InetSocketAddress(getServerHost(), getServerPort());
                Log.d("connect to server", "inside");
                s.connect(remoteAddress);
                Log.d("connect to server", "true");
                in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                Log.d("Server time out ", s.getSoTimeout() + "");
                DataInputStream dataOS = new DataInputStream(s.getInputStream());
                for (TCPListener listener : allListeners)
                    listener.onTCPConnectionStatusChanged(true);
                String inMsg;
                while (in != null && (inMsg = in.readLine()) != null) {
                    if (inMsg.contains(JsonHelper.FILE)) {
                        File path1 = new File(Environment.getExternalStorageDirectory().toString() + "/DataSharing");
                        File file = new File(path1.getAbsolutePath());

                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        receiveFiles(new BufferedInputStream(s.getInputStream()), file.getAbsolutePath());
                    } else {
                        for (TCPListener listener : allListeners)
                            listener.onTCPMessageReceived(inMsg);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                for (TCPListener listener : allListeners)
                    listener.onErrorMessage(e.getMessage());
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for (TCPListener listener : allListeners)
                listener.dismissDialog();
        }

    }


}
