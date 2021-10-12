package com.bbot.copydata.xender.Adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.R;
import com.bbot.copydata.xender.Util;
import com.bbot.copydata.xender.filter.entity.NormalFile;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Vincent Woo
 * Date: 2016/10/26
 * Time: 10:23
 */

public class NormalFilePickAdapter extends BaseAdapter<NormalFile, NormalFilePickAdapter.NormalFilePickViewHolder> {
    private int mMaxNumber;
    private int mCurrentNumber = 0;

    public NormalFilePickAdapter(Context ctx, int max) {
        this(ctx, new ArrayList<NormalFile>(), max);
    }

    public NormalFilePickAdapter(Context ctx, ArrayList<NormalFile> list, int max) {
        super(ctx, list);
        mMaxNumber = max;
    }

    @Override
    public NormalFilePickViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_layout_item_normal_file_pick, parent, false);
        return new NormalFilePickViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final NormalFilePickViewHolder holder, final int position) {
        final NormalFile file = mList.get(position);

        holder.mTvTitle.setText(Util.extractFileNameWithSuffix(file.getPath()));
        holder.mTvTitle.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        if (holder.mTvTitle.getMeasuredWidth() >
                Util.getScreenWidth(mContext) - Util.dip2px(mContext, 10 + 32 + 10 + 48 + 10 * 2)) {
            holder.mTvTitle.setLines(2);
        } else {
            holder.mTvTitle.setLines(1);
        }

        if (file.isSelected()) {
            holder.mCbx.setSelected(true);
        } else {
            holder.mCbx.setSelected(false);
        }

        if (file.getPath().endsWith("xls") || file.getPath().endsWith("xlsx")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_xls);
        } else if (file.getPath().endsWith("doc") || file.getPath().endsWith("docx")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_word);
        } else if (file.getPath().endsWith("ppt") || file.getPath().endsWith("pptx")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_power_point);
        } else if (file.getPath().endsWith("pdf")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_pdf);
        } else if (file.getPath().endsWith("txt")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_txt);
        } else if (file.getPath().endsWith("apk")) {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(file.getPath(), 0);

            // you need to set this variables manually for some reason to get icon from APK file that has not been installed
            packageInfo.applicationInfo.sourceDir = file.getPath();
            packageInfo.applicationInfo.publicSourceDir = file.getPath();

            Drawable icon = packageInfo.applicationInfo.loadIcon(packageManager);
            Glide.with(mContext)
                    .load(icon)
                    .error(R.drawable.ic_circle_gray)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .priority(Priority.IMMEDIATE)
                    .into(holder.mIvIcon);
        } else if (file.getPath().endsWith("mp3") || file.getPath().endsWith("wmv") || file.getPath().endsWith("ogg") || file.getPath().endsWith("pcm") || file.getPath().endsWith("aiff")) {
            holder.mIvIcon.setImageResource(R.drawable.ic_h_music);
        } else if (file.getPath().endsWith(".jpg") || file.getPath().endsWith(".JPG")
                || file.getPath().endsWith(".jpeg") || file.getPath().endsWith(".JPEG")
                || file.getPath().endsWith(".png") || file.getPath().endsWith(".PNG")
                || file.getPath().endsWith(".gif") || file.getPath().endsWith(".GIF")) {
            Glide.with(mContext)
                    .load(Uri.fromFile(new File(file.getPath())))
                    .error(R.drawable.ic_circle_gray)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .centerCrop()
                    .priority(Priority.IMMEDIATE)
                    .into(holder.mIvIcon);
        } else if (file.getPath().endsWith("mp4") || file.getPath().endsWith("mpeg") || file.getPath().endsWith("wav") || file.getPath().endsWith("flv")) {
            RequestOptions options = new RequestOptions();
            Glide.with(mContext)
                    .load(file.getPath())
                    .apply(options.centerCrop())
                    .centerCrop()
                    .transition(withCrossFade())
                    .into(holder.mIvIcon);
        } else {
            holder.mIvIcon.setImageResource(R.drawable.ic_txt);
        }

        holder.mCbx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.isSelected()) {
                    holder.mCbx.setSelected(false);
                    mCurrentNumber--;
                } else {
                    holder.mCbx.setSelected(true);
                    mCurrentNumber++;
                }

                mList.get(holder.getAdapterPosition()).setSelected(holder.mCbx.isSelected());

                if (mListener != null) {
                    mListener.OnSelectStateChanged(holder.mCbx.isSelected(), mList.get(holder.getAdapterPosition()));
                }
            }
        });

//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = Uri.parse("file://" + file.getPath());
//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setDataAndType(uri, "audio/mp3");
//                if (Util.detectIntent(mContext, intent)) {
//                    mContext.startActivity(intent);
//                } else {
//                    Toast.makeText(mContext, "No Application exists for audio!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private boolean isUpToMax() {
        return mCurrentNumber >= mMaxNumber;
    }

    class NormalFilePickViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvIcon;
        private TextView mTvTitle;
        private ImageView mCbx;

        public NormalFilePickViewHolder(View itemView) {
            super(itemView);
            mIvIcon = (ImageView) itemView.findViewById(R.id.ic_file);
            mTvTitle = (TextView) itemView.findViewById(R.id.tv_file_title);
            mCbx = (ImageView) itemView.findViewById(R.id.cbx);
        }
    }

}
