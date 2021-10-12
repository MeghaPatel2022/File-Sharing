package com.bbot.copydata.xender;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.Adapter.FolderListAdapter;
import com.bbot.copydata.xender.filter.entity.Directory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent Woo
 * Date: 2018/2/27
 * Time: 13:43
 */

public class FolderListHelper {
    private PopupWindow mPopupWindow;
    private View mContentView;
    private RecyclerView rv_folder;
    private FolderListAdapter mAdapter;

    public void initFolderListView(Context ctx) {
        if (mPopupWindow == null) {
            mContentView = LayoutInflater.from(ctx)
                    .inflate(com.bbot.copydata.xender.R.layout.vw_layout_folder_list, null);
            rv_folder = (RecyclerView) mContentView.findViewById(com.bbot.copydata.xender.R.id.rv_folder);
            mAdapter = new FolderListAdapter(ctx, new ArrayList<Directory>());
            rv_folder.setAdapter(mAdapter);
            rv_folder.setLayoutManager(new LinearLayoutManager(ctx));
            mContentView.setFocusable(true);
            mContentView.setFocusableInTouchMode(true);

            mPopupWindow = new PopupWindow(mContentView);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.setTouchable(true);
        }
    }

    public void setFolderListListener(FolderListAdapter.FolderListListener listener) {
        mAdapter.setListener(listener);
    }

    public void fillData(List<Directory> list) {
        mAdapter.refresh(list);
    }

    public void toggle(View anchor) {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            int[] a = new int[2]; //getLocationInWindow required array of size 2
            anchor.getLocationInWindow(a);
//            mContentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
//            mPopupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, 0 , a[1]+anchor.getHeight());
//            mPopupWindow.showAsDropDown(anchor,
//                    (anchor.getMeasuredWidth() - mContentView.getMeasuredWidth()) / 2,
//                    0);
//            mPopupWindow.update(anchor, mContentView.getMeasuredWidth(),
//                    mContentView.getMeasuredHeight());

            PopupWindow pw = new PopupWindow(anchor, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            pw.showAtLocation(anchor, Gravity.TOP, 160, 150);
        }
    }
}
