package com.wehear.ox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class PhiSmartAlert extends AppCompatActivity {
    SwitchCompat switchCompatMain, swtichUsageAlert, switchSessionTime, switchVolumn, switchSurrondNoise, switchSurrondEnviroment;
    LinearLayout linearAudio, linearVolumn, linearSurround, linearSession, linearSessionVisibility, linearAudioVisibility, linearVolumnVisibility, linearSurround75Visibility, linearSurround80Visibility, linearSurround85Visibility, linearAudioWithOutVisibility;
    boolean isCheckedMainSwitch;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    boolean isMainEnable;
    Spinner spnUsage, spnWithOutUsage, spnVolumn, spnSession, spnNoise75, spnNoise80, spnNoise85;
    String[] hours = {"1 hour", "2 hour", "3 hour", "4 hour", "5 hour", "Smart"};
    String[] session = {"20 min", "45 min", "60 min", "90 min", "120 min", "Smart"};
    String[] volumn = {"70", "80", "90", "100", "Smart"};
    String[] noise = {"10", "20", "30", "50", "Smart"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phi_smart_alert);
        findViewById();
        sharedPreferences = getSharedPreferences("PhiSmartAlert", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        isMainEnable = sharedPreferences.getBoolean("isAutoEnable", false);


        if (!isMainEnable) {
            switchCompatMain.setChecked(true);
            linearSession.setEnabled(true);
            linearSurround.setEnabled(true);
            linearVolumn.setEnabled(true);
            linearAudio.setEnabled(true);

        } else {
            switchCompatMain.setChecked(false);
            linearSession.setEnabled(false);
            linearSurround.setEnabled(false);
            linearVolumn.setEnabled(false);
            linearAudio.setEnabled(false);
        }
        switchCompatMain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    linearSession.setAlpha(0.5f);
                    linearSurround.setAlpha(0.5f);
                    linearVolumn.setAlpha(0.5f);
                    linearAudio.setAlpha(0.5f);
                    linearAudio.setEnabled(false);
                    linearSurround.setEnabled(false);
                    linearSession.setEnabled(false);
                    linearVolumn.setEnabled(false);
                    editor.putBoolean("isSmart", true);
                } else {
                    linearSession.setAlpha(1.0f);
                    linearSurround.setAlpha(1.0f);
                    linearVolumn.setAlpha(1.0f);
                    linearAudio.setAlpha(1.0f);
                    linearAudio.setEnabled(true);
                    linearSurround.setEnabled(true);
                    linearSession.setEnabled(true);
                    linearVolumn.setEnabled(true);
                    editor.putBoolean("isSmart", false);
                }
                editor.apply();
            }
        });
        swtichUsageAlert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    linearAudioVisibility.setVisibility(View.VISIBLE);
                    linearAudioWithOutVisibility.setVisibility(View.VISIBLE);
                }else{
                    linearAudioVisibility.setVisibility(View.GONE);
                    linearAudioWithOutVisibility.setVisibility(View.GONE);
                }
            }
        });
        switchVolumn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    linearVolumnVisibility.setVisibility(View.VISIBLE);
                }else{
                    linearVolumnVisibility.setVisibility(View.GONE);
                }
            }
        });
        switchSurrondNoise.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    linearSurround75Visibility.setVisibility(View.VISIBLE);
                    linearSurround80Visibility.setVisibility(View.VISIBLE);
                    linearSurround85Visibility.setVisibility(View.VISIBLE);
                }else{
                    linearSurround75Visibility.setVisibility(View.GONE);
                    linearSurround80Visibility.setVisibility(View.GONE);
                    linearSurround85Visibility.setVisibility(View.GONE);
                }
            }
        });
        switchSessionTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    linearSessionVisibility.setVisibility(View.VISIBLE);
                }else{
                    linearSessionVisibility.setVisibility(View.GONE);
                }
            }
        });
    }

    void findViewById() {
        switchCompatMain = findViewById(R.id.switch_main_phi_alert);
        switchSessionTime = findViewById(R.id.switch_session_time);
        switchSurrondEnviroment = findViewById(R.id.switch_surrond_enviroment);
        switchSurrondNoise = findViewById(R.id.switch_surrond_noice);
        switchVolumn = findViewById(R.id.switch_volumn);
        swtichUsageAlert = findViewById(R.id.switch_usage_alert);
        linearAudio = findViewById(R.id.linear_audio);
        linearVolumn = findViewById(R.id.linear_volumn);
        linearSurround = findViewById(R.id.linear_surround_noise);
        linearSession = findViewById(R.id.linear_session);
        linearSessionVisibility = findViewById(R.id.linear_session_spn);
        spnUsage = findViewById(R.id.spn_usage_with_device);
        spnWithOutUsage = findViewById(R.id.spn_usage_with_out_device);
        spnNoise75 = findViewById(R.id.spn_75dp_noise_device);
        spnNoise80 = findViewById(R.id.spn_80dp_noise_device);
        spnNoise85 = findViewById(R.id.spn_85dp_noise_device);
        spnSession = findViewById(R.id.spn_session_time);
        spnVolumn = findViewById(R.id.spn_volumn_device);
        linearAudioVisibility = findViewById(R.id.layout_usage_with);
        linearAudioWithOutVisibility = findViewById(R.id.layout_usage_with_out);
        linearSurround75Visibility = findViewById(R.id.layout_env_75);
        linearSurround80Visibility = findViewById(R.id.layout_env_80);
        linearSurround85Visibility = findViewById(R.id.layout_env_85);
        linearVolumnVisibility = findViewById(R.id.layout_volumn);
        ArrayAdapter outputaa = new ArrayAdapter(PhiSmartAlert.this, R.layout.support_simple_spinner_dropdown_item, volumn);
        spnVolumn.setAdapter(outputaa);
        outputaa = new ArrayAdapter(PhiSmartAlert.this, R.layout.support_simple_spinner_dropdown_item, hours);
        spnUsage.setAdapter(outputaa);
        spnWithOutUsage.setAdapter(outputaa);
        outputaa = new ArrayAdapter(PhiSmartAlert.this, R.layout.support_simple_spinner_dropdown_item, session);
        spnSession.setAdapter(outputaa);
        spnNoise75.setAdapter(outputaa);
        spnNoise80.setAdapter(outputaa);
        spnNoise85.setAdapter(outputaa);
    }
}