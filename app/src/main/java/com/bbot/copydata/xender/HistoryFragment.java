package com.bbot.copydata.xender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.addisonelliott.segmentedbutton.SegmentedButtonGroup;
import com.bbot.copydata.xender.Adapter.ReceiveHistoryAdapter;
import com.bbot.copydata.xender.Adapter.SentHistoryAdapter;
import com.bbot.copydata.xender.Database.DBHelper;
import com.bbot.copydata.xender.Model.History;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.segmented)
    SegmentedButtonGroup segmented;
    @BindView(R.id.rv_received)
    RecyclerView rv_received;
    @BindView(R.id.rv_sent)
    RecyclerView rv_sent;
    @BindView(R.id.scrollView)
    NestedScrollView scrollView;
    @BindView(R.id.rl_no_received)
    RelativeLayout rl_no_received;
    @BindView(R.id.rl_no_sent)
    RelativeLayout rl_no_sent;

    ArrayList<History> receivedPathList = new ArrayList<>();
    ArrayList<History> sentPathList = new ArrayList<>();
    DBHelper dbHelper;
    ReceiveHistoryAdapter receiveHistoryAdapter;
    SentHistoryAdapter sentHistoryAdapter;
    MyReceiver r;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);

        dbHelper = new DBHelper(getContext());

        // Inflate the layout for this fragment
        segmented.setOnPositionChangedListener(position -> {
            // Handle stuff here
            if (segmented.getPosition() == 0) {
                rv_sent.setVisibility(View.GONE);
                scrollView.setVisibility(View.VISIBLE);
                if (receivedPathList.size() == 0) {
                    scrollView.setVisibility(View.GONE);
                    rl_no_received.setVisibility(View.VISIBLE);
                } else {
                    scrollView.setVisibility(View.VISIBLE);
                    rl_no_received.setVisibility(View.GONE);
                }
                rl_no_sent.setVisibility(View.GONE);
            } else {
                scrollView.setVisibility(View.GONE);
                rv_sent.setVisibility(View.VISIBLE);

                if (sentPathList.size() == 0) {
                    rv_sent.setVisibility(View.GONE);
                    rl_no_sent.setVisibility(View.VISIBLE);
                } else {
                    rv_sent.setVisibility(View.VISIBLE);
                    rl_no_sent.setVisibility(View.GONE);
                }
                rl_no_received.setVisibility(View.GONE);
            }
        });

        // Receive Adapter
        receivedPathList = dbHelper.getSendReceived(String.valueOf(1));
        Collections.reverse(receivedPathList);
        rv_received.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        receiveHistoryAdapter = new ReceiveHistoryAdapter(receivedPathList, getActivity());
        rv_received.setAdapter(receiveHistoryAdapter);

        if (receivedPathList.size() == 0) {
            scrollView.setVisibility(View.GONE);
            rl_no_received.setVisibility(View.VISIBLE);
        } else {
            scrollView.setVisibility(View.VISIBLE);
            rl_no_received.setVisibility(View.GONE);
        }
        rl_no_sent.setVisibility(View.GONE);

        // Sent Adapter
        sentPathList = dbHelper.getSendReceived(String.valueOf(0));
        Collections.reverse(sentPathList);
        rv_sent.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        sentHistoryAdapter = new SentHistoryAdapter(sentPathList, getActivity());
        rv_sent.setAdapter(sentHistoryAdapter);

        r = new MyReceiver();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(r,
                new IntentFilter("TAG_REFRESH"));

        return view;
    }

    public void notifyData() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Sent Adapter
                sentPathList = dbHelper.getSendReceived(String.valueOf(0));
                Collections.reverse(sentPathList);
                sentHistoryAdapter = new SentHistoryAdapter(sentPathList, getActivity());
                rv_sent.setAdapter(sentHistoryAdapter);

                // Receive Adapter
                receivedPathList = dbHelper.getSendReceived(String.valueOf(1));
                Collections.reverse(receivedPathList);
                receiveHistoryAdapter = new ReceiveHistoryAdapter(receivedPathList, getActivity());
                rv_received.setAdapter(receiveHistoryAdapter);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(r);
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            HistoryFragment.this.notifyData();
        }
    }

}