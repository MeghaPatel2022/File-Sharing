package com.bbot.copydata.xender.Welcome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.bbot.copydata.xender.Activity.MainActivity;
import com.bbot.copydata.xender.Const.SharedPreference;
import com.bbot.copydata.xender.Fragment.Intro.IntroFirstFragment;
import com.bbot.copydata.xender.Fragment.Intro.IntroSecondFragment;
import com.bbot.copydata.xender.Fragment.Intro.IntroThirdFragment;
import com.bbot.copydata.xender.Fragment.Intro.PagerAdapter.IntroViewerPagersAdapter;
import com.bbot.copydata.xender.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.ll_first_introPage)
    LinearLayout ll_first_introPage;
    @BindView(R.id.ll_second_introPage)
    LinearLayout ll_second_introPage;
    @BindView(R.id.ll_third_introPage)
    LinearLayout ll_third_introPage;
    @BindView(R.id.img_first_next)
    ImageView img_first_next;
    @BindView(R.id.img_second_next)
    ImageView img_second_next;
    @BindView(R.id.img_third_next)
    ImageView img_third_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (SharedPreference.getLogin(IntroActivity.this)) {
            Intent intent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_intro);

        ButterKnife.bind(IntroActivity.this);

        img_first_next.setOnClickListener(this);
        img_second_next.setOnClickListener(this);
        img_third_next.setOnClickListener(this);

        setViewPager();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_first_next:
                viewPager.setCurrentItem(1);
                break;
            case R.id.img_second_next:
                viewPager.setCurrentItem(2);
                break;
            case R.id.img_third_next:
                Intent intent = new Intent(IntroActivity.this, StartActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void setViewPager() {
        IntroViewerPagersAdapter adapter = new IntroViewerPagersAdapter(getSupportFragmentManager());
        adapter.addFragment(new IntroFirstFragment(), "FIRST_FRAGMENT");
        adapter.addFragment(new IntroSecondFragment(), "SECOND_FRAGMENT");
        adapter.addFragment(new IntroThirdFragment(), "THIRD_FRAGMENT");
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    ll_first_introPage.setVisibility(View.VISIBLE);
                    ll_second_introPage.setVisibility(View.GONE);
                    ll_third_introPage.setVisibility(View.GONE);
                } else if (position == 1) {
                    ll_first_introPage.setVisibility(View.GONE);
                    ll_second_introPage.setVisibility(View.VISIBLE);
                    ll_third_introPage.setVisibility(View.GONE);
                } else if (position == 2) {
                    ll_first_introPage.setVisibility(View.GONE);
                    ll_second_introPage.setVisibility(View.GONE);
                    ll_third_introPage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 2) {
            viewPager.setCurrentItem(1);
        } else if (viewPager.getCurrentItem() == 1) {
            viewPager.setCurrentItem(0);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }
}