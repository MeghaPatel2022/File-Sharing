package com.bbot.copydata.xender.Interfaces;

import java.io.File;

public interface TCPListener {
	 void onTCPMessageReceived(String message);
	 void onTCPConnectionStatusChanged(boolean isConnectedNow);
	 void onErrorMessage(String message);

    void showProgressDialog();

	void updatePercentage(String progress);

	void dismissDialog();

    void showReceivedMessage(int totalFile);


	void fileReceived(long id, File file1);

    void CreateProgressDialog();



    void updateList();
}
