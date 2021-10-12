package com.bbot.copydata.xender.Adapter;

import android.content.Context;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vincent Woo
 * Date: 2016/10/14
 * Time: 15:42
 */

public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    protected Context mContext;
    protected ArrayList<T> mList;
    protected OnSelectStateListener<T> mListener;

    public BaseAdapter(Context ctx, ArrayList<T> list) {
        mContext = ctx;
        mList = list;
    }

    public void add(List<T> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void add(T file) {
        mList.add(file);
        notifyDataSetChanged();
    }

    public void add(int index, T file) {
        mList.add(index, file);
        notifyDataSetChanged();
    }

    public void refresh(List<T> list) {
        Log.e("LL_Img_SizeBefore: ", String.valueOf(mList.size()));
        mList.addAll(list);
        Log.e("LL_Img_SizeAfter: ", String.valueOf(mList.size()));
        if (list.isEmpty())
            notifyDataSetChanged();
        if (mList.size() >= 60)
            notifyItemRangeChanged(mList.size() - 60,list.size());
        else
            notifyDataSetChanged();
    }

    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    public void refresh(T file) {
        mList.clear();
        mList.add(file);
        notifyDataSetChanged();
    }

    public List<T> getDataSet() {
        return mList;
    }

    public void setOnSelectStateListener(OnSelectStateListener<T> listener) {
        mListener = listener;
    }
}
