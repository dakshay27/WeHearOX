package com.wehear.ox;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;

public class AnimationActivity extends AppCompatActivity {


    int i=0;
    String TAG = "Animation Activity";
    private int[] images = {R.drawable.bluetooth_screen_1, R.drawable.bluetooth_screen_2, R.drawable.bluetooth_screen_3, R.drawable.bluetooth_screen_4};
    private String[] top = {"Personalize your Music experience", "Loaded with Amazing \"OX Features\"", "Your Personal Hearing Intelligence (PHI)", "Keep your headset updated with the Latest fireware"};
    String address;

    private ImageView img_bluetooth_screen;
    private TextView tv_top,tv_bottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        SharedPreferences devicePreference = PreferenceManager.getDefaultSharedPreferences(this);
        int mac = devicePreference.getInt("device_mac",-1);
        if(mac==-1)
        {
            Intent intent = new Intent(this,RegisteredDeviceActivity.class);
            startActivity(intent);
        }
        new CountDownTimer(2000,8000) {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                img_bluetooth_screen.setImageDrawable(getResources().getDrawable(images[i]));
                tv_top.setText(top[i]);
                i++;
                if(i== images.length) i=0;
            }
        }.start();

    }
    private BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            for (BluetoothDevice device : proxy.getConnectedDevices()) {
                address = device.getAddress();
                Log.d(TAG, "onServiceConnected: " + address);
            }
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy);
        }
    };

    private void findViewById(){
        img_bluetooth_screen = findViewById(R.id.img_bluetooth_screen);
        tv_top = findViewById(R.id.tv_top);
        tv_bottom = findViewById(R.id.tv_bottom);
    }

}