package com.bbot.copydata.xender.Welcome;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bbot.copydata.xender.Activity.MainActivity;
import com.bbot.copydata.xender.Const.SharedPreference;
import com.bbot.copydata.xender.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {

    @BindView(R.id.img_start)
    ImageView img_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        ButterKnife.bind(StartActivity.this);

        img_start.setOnClickListener(v -> {
            SharedPreference.setLogin(StartActivity.this,true);
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}