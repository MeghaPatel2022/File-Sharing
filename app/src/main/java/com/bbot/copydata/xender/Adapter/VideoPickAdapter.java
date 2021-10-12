package com.bbot.copydata.xender.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.R;
import com.bbot.copydata.xender.Util;
import com.bbot.copydata.xender.filter.entity.VideoFile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;


/**
 * Created by Vincent Woo
 * Date: 2016/10/21
 * Time: 14:13
 */

public class VideoPickAdapter extends BaseAdapter<VideoFile, VideoPickAdapter.VideoPickViewHolder> {
    public String mVideoPath;
    private boolean isNeedCamera;
    private int mMaxNumber;
    private int mCurrentNumber = 0;

    public VideoPickAdapter(Context ctx, boolean needCamera, int max) {
        this(ctx, new ArrayList<VideoFile>(), needCamera, max);
    }

    public VideoPickAdapter(Context ctx, ArrayList<VideoFile> list, boolean needCamera, int max) {
        super(ctx, list);
        isNeedCamera = needCamera;
        mMaxNumber = max;
    }

    @Override
    public VideoPickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_layout_item_video_pick, parent, false);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Constant.COLUMN_NUMBER;
        }
        return new VideoPickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final VideoPickViewHolder holder, int position) {

        holder.mShadow.setVisibility(View.INVISIBLE);
        holder.mCbx.setSelected(false);
        mCurrentNumber = 0;
        mList.get(position).setSelected(holder.mCbx.isSelected());

        holder.mIvCamera.setVisibility(View.INVISIBLE);
        holder.mIvThumbnail.setVisibility(View.VISIBLE);
        holder.mCbx.setVisibility(View.VISIBLE);
        holder.mDurationLayout.setVisibility(View.VISIBLE);

        final VideoFile file;
        if (isNeedCamera) {
            file = mList.get(position - 1);
        } else {
            file = mList.get(position);
        }

        RequestOptions options = new RequestOptions();
        Glide.with(mContext)
                .load(file.getPath())
                .apply(options.centerCrop())
                .transition(withCrossFade())
//                    .transition(new DrawableTransitionOptions().crossFade(500))
                .into(holder.mIvThumbnail);

        if (file.isSelected()) {
            holder.mCbx.setSelected(true);
            holder.mShadow.setVisibility(View.VISIBLE);
        } else {
            holder.mCbx.setSelected(false);
            holder.mShadow.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.isSelected()) {
                    holder.mShadow.setVisibility(View.INVISIBLE);
                    holder.mCbx.setSelected(false);
                    mCurrentNumber--;
                } else {
                    holder.mShadow.setVisibility(View.VISIBLE);
                    holder.mCbx.setSelected(true);
                    mCurrentNumber++;
                }

                int index = isNeedCamera ? holder.getAdapterPosition() - 1 : holder.getAdapterPosition();
                mList.get(index).setSelected(holder.mCbx.isSelected());

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(index));
                }
            }
        });

        holder.mDuration.setText(Util.getDurationString(file.getDuration()));

    }

    @Override
    public int getItemCount() {
        return isNeedCamera ? mList.size() + 1 : mList.size();
    }

    public boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

    public void setCurrentNumber(int number) {
        mCurrentNumber = number;
    }

    class VideoPickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvCamera;
        private ImageView mIvThumbnail;
        private View mShadow;
        private ImageView mCbx;
        private TextView mDuration;
        private RelativeLayout mDurationLayout;

        public VideoPickViewHolder(View itemView) {
            super(itemView);
            mIvCamera = (ImageView) itemView.findViewById(R.id.iv_camera);
            mIvThumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            mShadow = itemView.findViewById(R.id.shadow);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
            mDuration = (TextView) itemView.findViewById(R.id.txt_duration);
            mDurationLayout = (RelativeLayout) itemView.findViewById(R.id.layout_duration);
        }
    }
}
