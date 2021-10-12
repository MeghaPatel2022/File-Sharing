package com.bbot.copydata.xender;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

    Context context;
    private int[] icon = new int[]{R.drawable.ic_history, R.drawable.ic_apk,R.drawable.ic_image,R.drawable.ic_video,R.drawable.ic_music,R.drawable.ic_folder};
    private int[] selectIcon = new int[]{R.drawable.ic_select_history, R.drawable.ic_select_apk,R.drawable.ic_select_image,R.drawable.ic_select_video,R.drawable.ic_select_music,
            R.drawable.ic_select_folder};

    public TabsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public View getTabView(int position) {
        // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
        View v;
        v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
        ImageView img_tab = v.findViewById(R.id.img_tab);

        if (position == 0) {
            img_tab.setImageDrawable(context.getResources().getDrawable(selectIcon[position]));
        } else {
            img_tab.setImageDrawable(context.getResources().getDrawable(icon[position]));
        }

        return v;
    }


    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new HistoryFragment(); //ChildFragment1 at position 0
            case 1:
                return new ApplicationFragment();
            case 2:
                return new ImageFragment();
            case 3:
                return new VideoFragment();
            case 4:
                return new MusicFragment();
            case 5:
                return new FolderFragment();
        }

        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        // POSITION_NONE makes it possible to reload the PagerAdapter
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 6;
    }
}
