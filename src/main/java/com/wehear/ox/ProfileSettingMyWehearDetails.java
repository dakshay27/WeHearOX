package com.wehear.ox;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ProfileSettingMyWehearDetails extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPreferences;
    String deviceMac, deviceName, deviceRegisteredTimeStamp, deviceColor;
    TextView txtDeviceName, txtDeviceAddress, txtDeviceColor, txtDeviceRegistedTime;
    ImageView imgDevice;
    private static final String TAG = "ProfileSettingMyWehearD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setting_my_wehear_details);
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        deviceMac = sharedPreferences.getString("isUsedWeHearOxMac", "");
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        deviceName = sharedPreferences.getString("isUsedWeHearOxName", "");
        imgDevice = findViewById(R.id.img_device_color);
        txtDeviceName = findViewById(R.id.txt_device_name);
        txtDeviceAddress = findViewById(R.id.txt_device_address);
        txtDeviceColor = findViewById(R.id.txt_device_color);
        txtDeviceRegistedTime = findViewById(R.id.txt_device_registed_time);
        if (db != null) {
            db = FirebaseFirestore.getInstance();
        }
        db.collection("Users").document(mUser.getUid())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String name = documentSnapshot.getString("fname");
                txtDeviceName.setText(name + "'s WeHear OX ");

            }
        });

        txtDeviceAddress.setText("Bluetooth Address - " + deviceMac);
        Log.d(TAG, "Device Mac" + deviceMac);
        txtDeviceColor.setVisibility(View.GONE);
        db.collection("Devices").document(deviceMac).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                deviceRegisteredTimeStamp = documentSnapshot.get("registeredTime").toString();
                if (documentSnapshot.get("deviceColor") != null) {
                    txtDeviceColor.setVisibility(View.VISIBLE);
                    deviceColor = documentSnapshot.get("deviceColor").toString();
                    switch (deviceColor) {
                        case "1":
                        case "4":
                            imgDevice.setImageResource(R.drawable.wehear_ox_gray);
                            txtDeviceColor.setText("Device Color : - Gray");
                            break;
                        case "2":
                            imgDevice.setImageResource(R.drawable.wehear_ox_blue);
                            txtDeviceColor.setText("Device Color : - Blue");
                            break;
                        case "3":
                            imgDevice.setImageResource(R.drawable.wehear_ox_green);
                            txtDeviceColor.setText("Device Color : - Green");
                            break;
                        case "5":
                            imgDevice.setImageResource(R.drawable.product);
                            txtDeviceColor.setText("Device Color : - Pink");
                            break;
                    }
                }
                Calendar cal = Calendar.getInstance();
                TimeZone tz = cal.getTimeZone();//get your local time zone.
                DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                sdf.setTimeZone(tz);//set time zone.
                String localTime = sdf.format(new Date(Long.parseLong(deviceRegisteredTimeStamp) * 1000));
                Date date = new Date();
                try {
                    date = sdf.parse(localTime);//get local date
                    String strDate = sdf.format(date);
                    txtDeviceRegistedTime.setText("Registered on - " + strDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                    txtDeviceRegistedTime.setText("Registered on - Unable to fetch try after some time");
                }

            }
        });
    }
}