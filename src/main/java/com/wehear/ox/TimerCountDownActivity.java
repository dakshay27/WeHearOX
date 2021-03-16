package com.wehear.ox;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;

import java.util.concurrent.TimeUnit;

import me.tankery.lib.circularseekbar.CircularSeekBar;

public class TimerCountDownActivity extends AppCompatActivity {
    public static final String STARTFOREGROUND_ACTION = "1";
    public static final String STOPFOREGROUND_ACTION = "0";
    static Intent serviceIntent;
    Button cancelTimer;
    ImageView backbtn3;
    public static CountDownTimer countDownTimer;
    public static long timeLeftInMilliseconds;
    CircularSeekBar circularSeekBar;
    static int max;
    private Context context = this;
    TextView textView, stoptv2, stopinfo;
    private static final String TAG = "TimerCountDownActivity";
    public static String hms;
    static String cuurentTime;
    static String allTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer_count_down);
        serviceIntent = new Intent(context, TimerService.class);
        textView = findViewById(R.id.tv1);
        cancelTimer = findViewById(R.id.btn_cancelTimer);
        circularSeekBar = findViewById(R.id.seekbar_circular);
        backbtn3 = findViewById(R.id.btn_back3);
        stoptv2 = findViewById(R.id.tv_stop2);
        stopinfo = findViewById(R.id.tv_stopinfo);
        backbtn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        cancelTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stopService(serviceIntent)) {
                    stopTimer();
                    Intent i = new Intent(context, TimerActivity.class);
                    startActivity(i);
                    finish();
                }

            }
        });
        if (isMyServiceRunning(TimerService.class)) {
            circularSeekBar.setEnabled(false);
            circularSeekBar.setMax(max);
            circularSeekBar.setProgress(timeLeftInMilliseconds);
            init();
        } else {
            Intent rIntent = getIntent();
            int hour = rIntent.getIntExtra("Hour", 0);
            int minute = rIntent.getIntExtra("Minute", 0);
            int second = rIntent.getIntExtra("Second", 0);
            Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
            Bugfender.enableCrashReporting();

            String i = hour + " : " + minute + " : " + second;
            serviceIntent.putExtra("inputExtra", i);
            ContextCompat.startForegroundService(context, serviceIntent);

            timeLeftInMilliseconds = ((hour * 3600) + (minute * 60) + second) * 1000;
            Log.d(TAG, "onCreate: " + timeLeftInMilliseconds);
            max = (int) timeLeftInMilliseconds;
            circularSeekBar.setEnabled(false);
            circularSeekBar.setMax(max);
            circularSeekBar.setProgress(max);
            init();

        }
    }

    public static void stopTimer() {
        if (countDownTimer != null)
            countDownTimer.cancel();

    }


    private void init() {
        countDownTimer = new CountDownTimer(timeLeftInMilliseconds, 1000) {
            @SuppressLint("DefaultLocale")
            @Override
            public void onTick(long l) {
                timeLeftInMilliseconds = l;
                int progress = (int) l;
                circularSeekBar.setProgress(progress);
                hms = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(timeLeftInMilliseconds),
                        TimeUnit.MILLISECONDS.toMinutes(timeLeftInMilliseconds) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeftInMilliseconds)), // The change is in this line
                        TimeUnit.MILLISECONDS.toSeconds(timeLeftInMilliseconds) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeftInMilliseconds)));
                textView.setText(hms);
            }

            @Override
            public void onFinish() {
                stopService(serviceIntent);
//                Intent i = new Intent(TimerCountDownActivity.this, TimerActivity.class);
//                startActivity(i);
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }
            }
        }.start();

//        int timeLeft = (int) timeLeftInMilliseconds;

    }

    public void cancelNotification() {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(ns);
        nMgr.cancel(1);
    }


    private void startService(String in, boolean start) {
        Log.d(TAG, "startService: Service Created");
        if (!start) {

        } else {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}

