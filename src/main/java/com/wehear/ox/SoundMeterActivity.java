package com.wehear.ox;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bugfender.sdk.Bugfender;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.BuildConfig;

import java.util.ArrayList;
import java.util.List;

import in.unicodelabs.kdgaugeview.KdGaugeView;

import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SoundMeterActivity extends AppCompatActivity {
    int hasDataPermission;
    private static final int REQUEST_CODE_WRITE_DATA = 2;
    private static final String TAG = "SoundMeterActivity";

    TextView mStatusView, mRange, soundmetertv, textView18;
    ImageView backbtn4, sound_info;
    Switch monitoringAccess;
    boolean isAllowed;
    MediaRecorder mRecorder;
    SeekBar soundBar;
    Thread runner;
    KdGaugeView dbMeter;
    int ampl;
    int amp, i = 0;
    int db;
    private static double mEMA = 0.0;
    static final private double EMA_FILTER = 0.9;
    LineChart mChart;
    List<Integer> dbList = new ArrayList<>();


    final Runnable updater = new Runnable() {

        public void run() {
            updateTv();
        }

        ;
    };
    final Handler mHandler = new Handler();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sound_meter);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        checkSelfPermission();

        mStatusView = (TextView) findViewById(R.id.tv_db_status);
        mRange = (TextView) findViewById(R.id.tv_db_title);
        monitoringAccess = findViewById(R.id.switch_btn_monitoringAccess);
        dbMeter = findViewById(R.id.db_meter);
        soundmetertv = findViewById(R.id.tv_soundmeter);
        textView18 = findViewById(R.id.tv18);
        sound_info = findViewById(R.id.sound_info);


        backbtn4 = findViewById(R.id.btn_back4);
        backbtn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sound_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(SoundMeterActivity.this);
                dialog.setContentView(R.layout.custom_dialog);
                dialog.setTitle("Custom Dialog");
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                Window window = dialog.getWindow();
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
                ImageView img = (ImageView) dialog.findViewById(R.id.img);
                TextView title = (TextView) dialog.findViewById(R.id.tv_title);
                TextView desc = (TextView) dialog.findViewById(R.id.tv_desc);
                ImageView cancelbtn = dialog.findViewById(R.id.btn_cancel);

                cancelbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

            }
        });

        final SharedPreferences permission = getSharedPreferences("permission", 0);
        isAllowed = permission.getBoolean("constantMonitoring", true);
        if (!isAllowed) {
            monitoringAccess.setChecked(false);
        }else{
            monitoringAccess.setChecked(true);
        }
        monitoringAccess.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor mEditor = permission.edit();
                mEditor.putBoolean("constantMonitoring", isChecked);
                mEditor.apply();
            }
        });

        if (runner == null) {
            runner = new Thread() {
                public void run() {
                    while (runner != null) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                        ;
                        mHandler.post(updater);
                    }
                }
            };
            runner.start();
            Log.d("Noise", "start runner()");
        }
    }

    public void onResume() {
        super.onResume();
        startRecorder();
    }

    public void onPause() {
        super.onPause();
        stopRecorder();
    }

    public void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                android.util.Log.e("[Monkey]", "IOException: " + android.util.Log.getStackTraceString(ioe));

            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }
            try {
                mRecorder.start();
            } catch (java.lang.SecurityException e) {
                android.util.Log.e("[Monkey]", "SecurityException: " + android.util.Log.getStackTraceString(e));
            }

            //mEMA = 0.0;
        }

    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double soundDb(double ampl)
    {
        return 20 * Math.log10(getAmplitudeEMA() / ampl);
    }

    public int getAmplitude() {
        if (mRecorder != null) {
            ampl = mRecorder.getMaxAmplitude();
            return (ampl);
        } else
            return 0;
    }

    public int getAmplitudeEMA() {
        amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        db = (int) (20 * Math.log10(mEMA));
        return db;
    }

    public void updateTv() {
        getAmplitudeEMA();
        i++;
        dbMeter.setSpeed(db);
        dbList.add(db);

        if (db < 70) {
            mStatusView.setText("Long-term exposure to sound at this level should not affect your hearing");
            mStatusView.setTextColor(Color.BLACK);
            mRange.setTextColor(Color.BLACK);
            mRange.setText("<70dB - OK");
        } else if (db < 80) {
            mStatusView.setText("Around 9 hours and 45 minutes a day at this level can cause temporary hearing loss. Weekly limit at this level is 75 hours.");
            mStatusView.setTextColor(Color.BLACK);
            mRange.setTextColor(Color.BLACK);
            mRange.setText("80dB - LOUD");
        } else if (db < 85) {
            mRange.setText("85dB - LOUD");
            mStatusView.setTextColor(Color.BLACK);
            mRange.setTextColor(Color.BLACK);
            mStatusView.setText("Around 5 hours and 30 minutes a day at this level can cause temporary hearing loss. Weekly limit at this level is 40 hours.");
        } else if (db < 90) {
            mRange.setText("90dB - TOO LOUD");
            mStatusView.setTextColor(Color.BLACK);
            mRange.setTextColor(Color.BLACK);
            mStatusView.setText("Around 1 hour and 45 minutes a day at this level can cause temporary hearing loss. Weekly limit at this level is 12 hours and 30 minutes.");
        } else if (db < 95) {
            mRange.setText("95dB - TOO LOUD");
            mStatusView.setTextColor(Color.BLACK);
            mRange.setTextColor(Color.BLACK);
            mStatusView.setText("Around 30 minutes a day at this level can cause temporary hearing loss. Weekly limit at this level is 4 hours.");
        } else if (db < 100) {
            mRange.setText("100dB - DANGER");
            mStatusView.setTextColor(Color.BLACK);
            mRange.setTextColor(Color.BLACK);
            mStatusView.setText("Just 10 minutes a day at this level can cause temporary hearing loss. Weekly limit at this level is 1 hour and 15 minutes.");
        } else {
            mRange.setText("100dB+ - YOU ARE AT RISK");
            mStatusView.setTextColor(Color.BLACK);
            mRange.setTextColor(Color.BLACK);
            mStatusView.setText("Even a few minutes a day at this level can cause temporary hearing loss. Weekly limit at this level is 40 hours.");
        }
    }

//    public void getGraph() {
//        mChart.setScaleEnabled(false);
//        mChart.getAxisLeft().setDrawGridLines(false);
//        mChart.getXAxis().setDrawGridLines(false);
//        mChart.getAxisRight().setDrawGridLines(false);
//        mChart.getAxisRight().setEnabled(false);
//        mChart.getAxisLeft().setEnabled(false);
//        mChart.getXAxis().setEnabled(false);
//        //mChart.getXAxis().setAxisMaximum(10);
//        mChart.getLegend().setEnabled(false);
//        mChart.getDescription().setEnabled(false);
//        mChart.getAxisLeft().setStartAtZero(true);
//
//        mChart.setScaleMinima(5f,1f);
//        mChart.setTouchEnabled(false);
//        mChart.setPinchZoom(true);
//        mChart.setScaleEnabled(true);
//        mChart.setDoubleTapToZoomEnabled(false);
//        mChart.setDrawGridBackground(false);
//        mChart.setDragEnabled(true);
//
//        float minXRange = 10;
//        float maxXRange = 10;
//        mChart.setVisibleXRange(minXRange, maxXRange);
//
//
//        mChart.getXAxis().setEnabled(false);
//        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//
//
//
//        //mChart.moveViewToX(Dates.size()+1);
//
//        //mChart.animateY(1000);
//        //mChart.animate();
//
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_WRITE_DATA:
                if (grantResults.length > 0) {

                    boolean readContacts = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readPhoneState = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readCallLogs = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    if (readContacts && readPhoneState && readCallLogs) {
                        Toast.makeText(this, "Thank you for your support.....", Toast.LENGTH_SHORT).show();
                    } else {
                        checkSelfPermission();
                    }
                }
                break;
        }
    }

    void checkSelfPermission() {
        hasDataPermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, MODIFY_AUDIO_SETTINGS);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, REQUEST_CODE_WRITE_DATA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, REQUEST_CODE_WRITE_DATA);
            }
        } else {
        }

    }
}
