package com.wehear.ox;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TempActivity extends AppCompatActivity {

    private static final String TAG = "TempActivity";

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }
}
