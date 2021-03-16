package com.wehear.ox;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.shawnlin.numberpicker.NumberPicker;

public class TimerActivity extends AppCompatActivity {
    TimePicker timer;
    TextView stoptv, timerinfo, textView13, textView12, textView14;
    Button setTimerBtn;
    private NumberPicker hour, minute, second;
    private Button startButton;
    ImageView backbtn2;
    private static final String TAG = "TimerActivity";
    //    public int hour,minute;
    static String timeLeft;
    int h, m, s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer);
        if (isMyServiceRunning(TimerService.class)) {
            startActivity(new Intent(this, TimerCountDownActivity.class));
            finish();
        }
//        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TimerActivity.this);
//        int state = sharedPreferences.getInt("timer_state",0);
//        if(state==1)
//        {
//            Intent intent = new Intent(TimerActivity.this, TimerCountDownActivity.class);
//
//        }
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();


        hour = findViewById(R.id.number_picker_hour);
        minute = findViewById(R.id.number_picker_min);
        second = findViewById(R.id.number_picker_sec);
        stoptv = findViewById(R.id.tv_stop);
        timerinfo = findViewById(R.id.tv_timerinfo);
        textView13 = findViewById(R.id.tv13);
        textView12 = findViewById(R.id.tv12);
        textView14 = findViewById(R.id.tv14);
        backbtn2 = findViewById(R.id.btn_back2);

        backbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });


        startButton = findViewById(R.id.btn_start);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                h = hour.getValue();
                m = minute.getValue();
                s = second.getValue();
                if (h == 0 && m == 0 && s == 0) {
                    Toast.makeText(getApplicationContext(), "Set the time first!", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(TimerActivity.this, TimerCountDownActivity.class);
                    intent.putExtra("Hour", h);
                    intent.putExtra("Minute", m);
                    intent.putExtra("Second", s);
                    startActivity(intent);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    finish();

                }
            }


        });
        //setTimerBtn = findViewById(R.id.setTimer);
        //timer = findViewById(R.id.timePicker);
        //timer.setIs24HourView(true);

//        setTimerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (Build.VERSION.SDK_INT >= 23) {
//                    hour = timer.getHour();
//                    minute = timer.getMinute();
//                } else {
//                    hour = timer.getCurrentHour();
//                    minute = timer.getCurrentMinute();
//                }
//                Log.d(TAG, "onClick: " + hour + " : " + minute);
//
//                Intent intent = new Intent(TimerActivity.this, TimerCountDownActivity.class);
//                intent.putExtra("Hour",hour);
//                intent.putExtra("Minute",minute);
//                startActivity(intent);
//
//
//            }
//    });

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
