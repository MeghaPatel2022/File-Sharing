package com.bbot.copydata.xender.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.R;
import com.bbot.copydata.xender.filter.entity.ImageFile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Vincent Woo
 * Date: 2016/10/13
 * Time: 16:07
 */

public class ImagePickAdapter extends BaseAdapter<ImageFile, ImagePickAdapter.ImagePickViewHolder> {
    public String mImagePath;
    public Uri mImageUri;
    private boolean isNeedImagePager;
    private boolean isNeedCamera;
    private int mMaxNumber;
    private int mCurrentNumber = 0;

    public ImagePickAdapter(Context ctx, boolean needCamera, boolean isNeedImagePager, int max) {
        this(ctx, new ArrayList<ImageFile>(), needCamera, isNeedImagePager, max);
    }

    public ImagePickAdapter(Context ctx, ArrayList<ImageFile> list, boolean needCamera, boolean needImagePager, int max) {
        super(ctx, list);
        isNeedCamera = needCamera;
        mMaxNumber = max;
        isNeedImagePager = needImagePager;
    }

    @Override
    public ImagePickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_layout_item_image_pick, parent, false);
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        if (params != null) {
            WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            int width = wm.getDefaultDisplay().getWidth();
            params.height = width / Constant.COLUMN_NUMBER;
        }
        return new ImagePickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ImagePickViewHolder holder, int position) {

        holder.mShadow.setVisibility(View.INVISIBLE);
        holder.mCbx.setSelected(false);
        mCurrentNumber = 0;
        mList.get(position).setSelected(false);

        mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

        holder.mIvCamera.setVisibility(View.INVISIBLE);
        holder.mIvThumbnail.setVisibility(View.VISIBLE);
        holder.mCbx.setVisibility(View.VISIBLE);

        ImageFile file;
        if (isNeedCamera) {
            file = mList.get(position - 1);
        } else {
            file = mList.get(position);
        }

        RequestOptions options = new RequestOptions();

        Glide.with(mContext)
                .load(Uri.fromFile(new File(file.getPath())))
                .error(R.drawable.ic_circle_gray)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.IMMEDIATE)
                .into(holder.mIvThumbnail);

        if (file.isSelected()) {
            holder.mCbx.setSelected(true);
            holder.mShadow.setVisibility(View.VISIBLE);
        } else {
            holder.mCbx.setSelected(false);
            holder.mShadow.setVisibility(View.INVISIBLE);
        }

        holder.mCbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int index = isNeedCamera ? holder.getAdapterPosition() - 1 : holder.getAdapterPosition();
                if (v.isSelected()) {
                    holder.mShadow.setVisibility(View.INVISIBLE);
                    holder.mCbx.setSelected(false);
                    mCurrentNumber--;
                    mList.get(index).setSelected(false);
                } else {
                    holder.mShadow.setVisibility(View.VISIBLE);
                    holder.mCbx.setSelected(true);
                    mCurrentNumber++;
                    mList.get(index).setSelected(true);
                }

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(index));
                }
            }
        });

        if (!isNeedImagePager) {
            holder.mIvThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int index = isNeedCamera ? holder.getAdapterPosition() - 1 : holder.getAdapterPosition();
                    if (holder.mCbx.isSelected()) {
                        holder.mShadow.setVisibility(View.INVISIBLE);
                        holder.mCbx.setSelected(false);
                        mCurrentNumber--;
                        mList.get(index).setSelected(false);
                    } else {
                        holder.mShadow.setVisibility(View.VISIBLE);
                        holder.mCbx.setSelected(true);
                        mCurrentNumber++;
                        mList.get(index).setSelected(true);
                    }

                    if (mListener != null) {
                        mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(index));
                    }
                }
            });
        }
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

    class ImagePickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvCamera;
        private ImageView mIvThumbnail;
        private View mShadow;
        private ImageView mCbx;

        public ImagePickViewHolder(View itemView) {
            super(itemView);
            mIvCamera = (ImageView) itemView.findViewById(R.id.iv_camera);
            mIvThumbnail = (ImageView) itemView.findViewById(R.id.iv_thumbnail);
            mShadow = itemView.findViewById(R.id.shadow);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
        }
    }
}
