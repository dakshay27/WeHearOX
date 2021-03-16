package com.wehear.ox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FeaturesActivity extends AppCompatActivity {

    TextView featurestv,translatortv,hearingaidtv,soundmetertv,stoptimertv,paralleltv,schedulartv;
    ImageView translator_button,hearingaid_button,soundmeter_button,stoptimer_button,parallel_streaming_button,schedular_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_features);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        |View.SYSTEM_UI_FLAG_FULLSCREEN
                        |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        findViewById();

        translator_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TranslateTestActivity.class));
            }
        });

        hearingaid_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), HearingAidActivity.class));
            }
        });

        soundmeter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SoundMeterActivity.class));
            }
        });

        stoptimer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), TimerActivity.class));
            }
        });



    }

    private void findViewById(){
        translatortv = findViewById(R.id.tv_translator);
        hearingaidtv = findViewById(R.id.tv_hearingaid);
        soundmetertv = findViewById(R.id.tv_soundmeter);
        stoptimertv = findViewById(R.id.tv_stoptimer);
        paralleltv = findViewById(R.id.tv_parallel);
        schedulartv = findViewById(R.id.tv_schedular);

        translator_button = findViewById(R.id.btn_translator);
        hearingaid_button = findViewById(R.id.btn_hearingaid);
        soundmeter_button = findViewById(R.id.btn_soundmeter);
        stoptimer_button = findViewById(R.id.btn_stoptimer);
        parallel_streaming_button = findViewById(R.id.btn_parallel_streaming);
        schedular_button = findViewById(R.id.btn_schedular);
    }
}