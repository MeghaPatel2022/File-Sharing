package com.bbot.copydata.xender.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bbot.copydata.xender.Const.Constant;
import com.bbot.copydata.xender.Model.ApplicationModel;
import com.bbot.copydata.xender.R;
import com.bbot.copydata.xender.SendReceiveFragment;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ApplicationListAdapter extends RecyclerView.Adapter<ApplicationListAdapter.MyClassView> {

    ArrayList<ApplicationModel> applicationModels;
    Activity activity;

    public ApplicationListAdapter(ArrayList<ApplicationModel> applicationModels, Activity activity) {
        this.applicationModels = applicationModels;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.list_applications, null, false);
        return new MyClassView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyClassView holder, int position) {
        ApplicationModel applicationModel = applicationModels.get(position);

        Glide.with(activity)
                .load(applicationModel.getAppIcon())
                .into(holder.img_app_icon);

        holder.tv_app_name.setText(applicationModel.getAppName());

        holder.itemView.setOnClickListener(v -> {
            if (holder.img_select.getVisibility() == View.VISIBLE) {
                Constant.filePaths.remove(applicationModel.getAppPath());
                Constant.FileName.remove(applicationModel.getAppName());
                holder.img_select.setVisibility(View.GONE);
                holder.img_unselect.setVisibility(View.VISIBLE);
            } else {
                Constant.filePaths.add(applicationModel.getAppPath());
                Constant.FileName.add(applicationModel.getAppName());
                holder.img_select.setVisibility(View.VISIBLE);
                holder.img_unselect.setVisibility(View.GONE);
            }

            SendReceiveFragment.fragmentPlayListBinding.tvSelect.setText(Constant.filePaths.size()+" Selected ");
        });
    }

    @Override
    public int getItemCount() {
        return applicationModels.size();
    }

    public class MyClassView extends RecyclerView.ViewHolder {

        ImageView img_app_icon, img_unselect, img_select;
        TextView tv_app_name;

        public MyClassView(@NonNull View itemView) {
            super(itemView);

            img_app_icon = itemView.findViewById(R.id.img_app_icon);
            tv_app_name = itemView.findViewById(R.id.tv_app_name);
            img_unselect = itemView.findViewById(R.id.img_unselect);
            img_select = itemView.findViewById(R.id.img_select);
        }
    }
}
