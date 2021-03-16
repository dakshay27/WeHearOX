package com.wehear.ox;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

public class ManagePermissionBackground extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String[] phoneBrand, autostart, batterymanager, lockapp;
    Spinner spnPhoneBrand;
    TextView txtHeadAutoStart, txtMainAutoStart;
    TextView txtHeadBatteryManager, txtMainBatteryManager;
    TextView txtHeadLockScreen, txtMainLockScreen;
    String strOtherPhone;
    private final String BRAND_XIAOMI = "xiaomi";

    private final String BRAND_HONOR = "honor";
    private final String BRAND_OPPO = "oppo";

    private final String BRAND_VIVO = "vivo";
    private final String BRAND_SAMSUNG = "samsung";
    private final String BRAND_ONEPLUS = "oneplus";
    private ArrayAdapter inputArrayAdapter;
    HashMap<String, String> hashMapInput, hashBattery, hashLockApp;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_permission_background);

        backButton = findViewById(R.id.btn_back9);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        strOtherPhone = "1.\tLock WeHear app in Recents;\n" +
                "2.\tAllow WeHear to autostart in Security/Settings > AutoStarts;\n" +
                "3.\tAdd WeHear to Exceptions\n" +
                "4.\tIf you have installed another startup manager , remove WeHear from its blocklist\n";
        phoneBrand = new String[]{"Huawei EMUI", "Xiaomi MIUI", "VIVO Funtouch OS", "OPPO ColorOS", "Samsung", "OnePlus oxygen Os", "Other Phone Brand"};
        autostart = new String[]{"1.\tOpen \"Phone Manager\" app which comes pre-installed on Huawei device. If you are not able to find the \"Phone Manager\" app on your home screen, search for it.\n" +
                "2.\tSelect \"Battery\"\n" +
                "3.\tSelect \"Launch\" to manage app launches and background running\n" +
                "4.\tIf you have not selected \"Manage all automatically\", select \"Manage batch manually\"\n" +
                "5.\tEnable all three options for \"WeHear\" (Auto-launch, Secondary launch, Run in Background)\n",

                "1.\tOpen “Security” and tap “Permission”;\n" +
                        "2.\tSelect “Auto Start”;\n" +
                        "3.\tFind WeHear and turn on the Switch;\n",

                "1.\tGo to settings application.\n" +
                        "2.\tGo to more settings.\n" +
                        "3.\tTap on Applications or permission management.\n" +
                        "4.\tOpen Autostart.\n" +
                        "5.\tYou will now see list of applications.\n" +
                        "6.\tSelect WeHear and turn on the switch.\n",

                "1.\tOpen Phone Manager and find Privacy Permission;\n" +
                        "2.\tEnter StartUp Manager;\n" +
                        "3.\tFind WeHear and turn on the Switch;\n",

                "1.\tOpen Smart Manager;\n" +
                        "2.\tSlide right on the Screen and tap auto run apps;\n" +
                        "3.\tFind WeHear and turn on the Switch;\n",

                "1.\tOpen Smart Manager;\n" +
                        "2.\tSlide right on the Screen and tap auto run apps;\n" +
                        "3.\tFind WeHear and turn on the Switch;\n",

                "1.\tLock WeHear app in Recents;\n" +
                        "2.\tAllow WeHear to autostart in Security/Settings > AutoStarts;\n" +
                        "3.\tAdd WeHear to Exceptions\n" +
                        "4.\tIf you have installed another startup manager , remove WeHear from its blocklist\n",
        };
        batterymanager = new String[]{
                "1.\tOpen Settings;\n" +
                        "2.\tSelect Advance Setting;\n" +
                        "3.\tSelect Battery manger;\n" +
                        "4.\tSelect Protected App;\n" +
                        "5.\tFind WeHear and turn on the Switch.\n",

                "1.\tOpen Settings;\n" +
                        "2.\tSelect “Battery and Performance”;\n" +
                        "3.\tSelect “Choose Apps”\n" +
                        "4.\tFind WeHear and select “No Restrication” in background settings\n",

                "1.\tOpen I manager and Tap Power Manager;\n" +
                        "2.\tSelect BackStage Power;\n" +
                        "3.\tFind WeHear and turn on the Switch\n",

                "1.\tOpen setting and tap battery;\n" +
                        "2.\tTap Energy Saver;\n" +
                        "3.\tFind WeHear to turn off Background Freeze and Abnormal Apps Optimization;\n" +
                        "If you can’t set after you complete the above steps try to:\n" +
                        "1.\tOpen Phone Manager , and Find Clean Background;\n" +
                        "2.\tTap to add\n" +
                        "3.\tTick WeHear and tap OK;\n",
                "1.\tTap Smart Manager;\n" +
                        "2.\tSelect Battery and tap to enter settings page;\n" +
                        "3.\tTap unmonitored apps and find WeHear and tap to enter;\n" +
                        "4.\tTap battery and tap optimize battery usage;\n" +
                        "5.\tSelect all apps, find WeHear and turn off the Switch;\n",
                "1.\tTap Smart Manager;\n" +
                        "2.\tSelect Battery and tap to enter settings page;\n" +
                        "3.\tTap unmonitored apps and find WeHear and tap to enter;\n" +
                        "4.\tTap battery and tap optimize battery usage;\n" +
                        "5.\tSelect all apps, find WeHear and turn off the Switch;\n",
                "1.\tLock WeHear app in Recents;\n" +
                        "2.\tAllow WeHear to autostart in Security/Settings > AutoStarts;\n" +
                        "3.\tAdd WeHear to Exceptions\n" +
                        "4.\tIf you have installed another startup manager , remove WeHear from its blocklist\n",};
        lockapp = new String[]{
                "1.\tTap Menu button, enter Recents page. \n" +
                        "2.\tPull down WeHear Ox until a lock icon appears.\n",
                "1.\tTap Menu button, enter Recents page. \n" +
                        "2.\tSelect WeHear Ox and lock it.\n",
                "1.\tTap Menu button, enter Recents page. \n" +
                        "2.\tPull down WeHear Ox until a lock icon appears.\n",
                "1.\tTap Menu button, enter Recents page.\n" +
                        "2.\tPull down WeHear Ox until a lock icon appears.\n",
                "1.\tTap the menu button and enter the recently opened program pages;\n" +
                        "2.\tTap the more icon to select Lock apps;\n" +
                        "3.\tTap the lock icon of WeHear;\n" +
                        "4.\tTap done to complete action;\n",
                "1.\tTap the menu button and enter the recently opened program pages;\n" +
                        "2.\tTap the more icon to select Lock apps;\n" +
                        "3.\tTap the lock icon of WeHear;\n" +
                        "4.\tTap done to complete action;\n",
                "1.\tLock WeHear app in Recents;\n" +
                        "2.\tAllow WeHear to autostart in Security/Settings > AutoStarts;\n" +
                        "3.\tAdd WeHear to Exceptions\n" +
                        "4.\tIf you have installed another startup manager , remove WeHear from its blocklist\n",};
        hashMapInput = new HashMap<>();
        hashBattery = new HashMap<>();
        hashLockApp = new HashMap<>();
        spnPhoneBrand = findViewById(R.id.spn_phone_brand);
        inputArrayAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, phoneBrand);
        inputArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spnPhoneBrand.setAdapter(inputArrayAdapter);
        spnPhoneBrand.setOnItemSelectedListener(this);
        txtHeadAutoStart = findViewById(R.id.txt_head_autostart);
        txtMainAutoStart = findViewById(R.id.txt_main_autostart);
        txtHeadBatteryManager = findViewById(R.id.txt_head_bettery_manager);
        txtMainBatteryManager = findViewById(R.id.txt_main_bettery_manager);
        txtHeadLockScreen = findViewById(R.id.txt_head_lock_the_app);
        txtMainLockScreen = findViewById(R.id.txt_main_lock_the_app);
        for (int i = 0; i < phoneBrand.length; i++) {
            hashMapInput.put(phoneBrand[i], autostart[i]);
            hashBattery.put(phoneBrand[i], batterymanager[i]);
            hashLockApp.put(phoneBrand[i], lockapp[i]);
        }

        String build_info = Build.BRAND.toLowerCase();
        switch (build_info) {
            case BRAND_HONOR:
                itemSelect(0);
                break;
            case BRAND_XIAOMI:
                itemSelect(1);
                break;
            case BRAND_VIVO:
                itemSelect(2);
                break;
            case BRAND_OPPO:
                itemSelect(3);
                break;
            case BRAND_SAMSUNG:
                itemSelect(4);
                break;
            case BRAND_ONEPLUS:
                itemSelect(5);
                break;
            default:
                itemSelect(6);
                break;

        }

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        itemSelect(i);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    private void itemSelect(int i) {
        spnPhoneBrand.setSelection(i);
        String autostart = hashMapInput.get(phoneBrand[i]);
        String battery = hashBattery.get(phoneBrand[i]);
        String lock = hashLockApp.get(phoneBrand[i]);
        if (i == (phoneBrand.length - 1)) {
            txtMainLockScreen.setVisibility(View.INVISIBLE);
            txtHeadLockScreen.setVisibility(View.INVISIBLE);
            txtMainBatteryManager.setVisibility(View.INVISIBLE);
            txtHeadBatteryManager.setVisibility(View.INVISIBLE);
            txtHeadAutoStart.setVisibility(View.INVISIBLE);
            txtMainAutoStart.setText(autostart);
        } else {
            txtMainLockScreen.setVisibility(View.VISIBLE);
            txtHeadLockScreen.setVisibility(View.VISIBLE);
            txtMainBatteryManager.setVisibility(View.VISIBLE);
            txtHeadBatteryManager.setVisibility(View.VISIBLE);
            txtHeadAutoStart.setVisibility(View.VISIBLE);
            txtMainAutoStart.setText(autostart);
            txtMainBatteryManager.setText(battery);
            txtMainLockScreen.setText(lock);
        }
    }
}