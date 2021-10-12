package com.bbot.copydata.xender.Interfaces;


import com.bbot.copydata.xender.Services.HotSpotService;

public interface SendReceiveListPresenter {

    void onCreateView();

    void onResume();


    void onUserVisibleHint();


    void onDestroy();


    void onStop();

    void onSendButtonClicked();


    void onServiceConnected(HotSpotService.MyBinder service);

    void onAllItemRemoved();
}
