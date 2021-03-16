package com.wehear.ox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import me.itangqi.waveloadingview.WaveLoadingView;
import me.tankery.lib.circularseekbar.CircularSeekBar;

public class phiActivity extends AppCompatActivity {

    private static final String TAG = "phiActivity";

    ImageView badgeImage;
    List<Integer> PHIList = new ArrayList<>();
    List<String> Dates = new ArrayList<>();
    int sum = 0;
    Map<String, Object> phiData, year, month, day;
    TreeMap<String, Object> DATA;
    TreeMap<String, Integer> finalData = new TreeMap<>();
    String dateData;
    int phiNum;
    TextView earAge, hearingLossAge;
    int earAgeNum, hearingLossAgeNum;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    int curr_lvl = 1, prev_lvl = 1;
    private EditText mW, mO, length, volume;
    private DocumentReference mDataRef;
    String birthday;
    Date parseDate;
    Integer ageInt;
    TextView phiscoretv,scoretv;
    TextView earAge_textsub,earLossAge_textsub,dailyHearingPattern_Text,knowmore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phi);

        badgeImage = findViewById(R.id.img_badge);

        earAge = findViewById(R.id.tv_earAge_text);
        hearingLossAge = findViewById(R.id.tv_earLossAge_text);

        phiscoretv = findViewById(R.id.tv_score);
        scoretv = findViewById(R.id.tv_score);

        earAge_textsub = findViewById(R.id.tv_earAge_textsub);
        earLossAge_textsub = findViewById(R.id.tv_earLossAge_textsub);
        dailyHearingPattern_Text = findViewById(R.id.tv_dailyHearingPattern_Text);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setSelectedItemId(R.id.phi);
        bottomNavigationView.setItemIconTintList(null);
        int x=5;
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch (menuItem.getItemId()){

                    case R.id.features :
                        startActivity(new Intent(getApplicationContext(),FeaturesActivity.class));
                        overridePendingTransition(0,0);

                    case R.id.ox :
                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        overridePendingTransition(0,0);

                    case R.id.phi :
                        return true;

                    case R.id.profile :
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        overridePendingTransition(0,0);
                }

                return false;
            }
        });

        mDataRef = db.collection("Users").document(mUser.getUid());

        mDataRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    birthday = task.getResult().get("bday").toString();
                    Log.d(TAG, "onComplete: Birthday "+ birthday);
                } catch (NullPointerException e) {
                    Log.e(TAG, "onComplete: ",e);
                }
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    parseDate = sdf.parse(birthday);
                    Log.d(TAG, "onCreate: Parse Date"+ parseDate);
                } catch (Exception z){
                    Log.e(TAG, "onComplete: ",z);
                }

                getAge(parseDate);
                getPhiData();
            }
        }, 1000);
    }

    private void getPhiData() {
        DocumentReference PHIData = db.collection("minutes-data").document(mUser.getUid());
        PHIData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        phiData = document.getData();
                        DATA = new TreeMap<>(phiData);

                        for (String key : DATA.keySet()){
                            day = (Map) DATA.get(key);
                            phiNum = Integer.parseInt(day.get("PHI").toString());
                            PHIList.add(phiNum);
                            Dates.add(key);
                            finalData.put(key, phiNum);
                        }

                        Log.d(TAG, "onComplete: Date list- " + Dates);
                        Log.d(TAG, "onComplete: Phi list - " + PHIList);
                        Log.d(TAG, "onComplete: phi Data - " + finalData);

                        Log.d(TAG, "Size: " + PHIList.size());
                        if (PHIList.size() < 7) {
                            for (int i = 0; i < PHIList.size(); i++) {
                                sum += PHIList.get(i);
                            }
                        } else {
                            for (int i = (PHIList.size() - 7); i < PHIList.size(); i++) {
                                sum += PHIList.get(i);
                            }
                        }


                        Log.d(TAG, "PHI 7 days: " + sum);

                        //Ear age algorithms
                        int temp = 0;
                        int avg = 0;

                        if (PHIList.size() < 30) {
                            for (int i = 0; i < PHIList.size(); i++) {
                                temp += PHIList.get(i);
                            }
                        } else {
                            for (int i = (PHIList.size() - 30); i < PHIList.size(); i++) {
                                temp += PHIList.get(i);
                            }
                        }

                        avg = (temp/PHIList.size());
                        Log.d(TAG, "onComplete: AVG PHI - "+ avg);

                        scoretv.setText(Integer.toString(sum));

                        earAgeNum = ageInt + (30 - avg);
                        hearingLossAgeNum = 64 - (30 - avg);

                        earAge.setText(""+earAgeNum);
                        hearingLossAge.setText(""+hearingLossAgeNum);


                        if (sum >= 300) {
                            curr_lvl = 4;

                        } else if (sum >= 200) {
                            curr_lvl = 3;

                        } else if (sum >= 100) {
                            curr_lvl = 2;

                        } else {
                            curr_lvl = 1;

                        }

                        if (curr_lvl > prev_lvl) {
                            Toast.makeText(getApplicationContext(), "You have been promoted", Toast.LENGTH_LONG).show();
                            prev_lvl = curr_lvl;
                            switch (curr_lvl){
                                case 1:
                                    badgeImage.setImageResource(R.drawable.ic_bronze_badge);
                                    break;
                                case 2:
                                    badgeImage.setImageResource(R.drawable.ic_silver_badge);
                                    break;
                                case 3:
                                    badgeImage.setImageResource(R.drawable.ic_gold_badge);
                                    break;
                                case 4:
                                    badgeImage.setImageResource(R.drawable.ic_diamond_badge);
                                    break;
                            }
                        } else if (curr_lvl < prev_lvl) {
                            Toast.makeText(getApplicationContext(), "You have been demoted", Toast.LENGTH_LONG).show();
                            prev_lvl = curr_lvl;
                        }

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

    }

    private String getAge(Date date){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        //dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        ageInt = new Integer(age);
        String ageS = ageInt.toString();

        Log.d(TAG, "getAge: AGE - "+ ageS);

        return ageS;
    }

}