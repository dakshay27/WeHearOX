package com.wehear.ox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import jp.kshoji.audio.receiver.AudioRouter;

public class HearingAidTest extends AppCompatActivity {
    AudioRouter audioRouter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_aid_test);
        audioRouter=new AudioRouter(this);
        audioRouter.setRouteMode(AudioRouter.AudioRouteMode.BLUETOOTH_A2DP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}