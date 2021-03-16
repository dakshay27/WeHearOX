package com.wehear.ox;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;

import com.bugfender.sdk.Bugfender;
import com.bullhead.equalizer.EqualizerFragment;
import com.google.firebase.BuildConfig;

public class EqActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eq);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();


        EqualizerFragment equalizerFragment = EqualizerFragment.newBuilder()
                .setAccentColor(Color.parseColor("#ef135b"))
                .setAudioSessionId(0)
                .build();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_eq,equalizerFragment)
                .commit();
    }
}

