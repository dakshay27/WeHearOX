package com.wehear.ox;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wehear.ox.AppModel.ViewPagerModel;
import com.wehear.ox.DailyData.DemoDailyDataActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

public class PhiFragment extends Fragment {
    private TextView txtInner, txtOuter;
    private LinearLayout linearViewPager, linearQA;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    List<ViewPagerModel> pagerModels;
    private static final int MAX_STEP = 3;
    ProgressIndicator progressIndicator;
    TextView txt100, txt200, txt300, txtNextTarget, txtNeedMore, txtDailyChartPHI;
    TextView txtMinuteUsedCurrent, txtMinuteUsedWithoutCurrent, txtAudioSessionCurrent, txtAudioAmptitudeCurrent, txtSurrondTimeCurrent, txtDailyPHICurrent;
    TextView txtMinuteUsedTarget, txtMinuteUsedWithoutTarget, txtAudioSessionTarget, txtAudioAmptitudeTarget, txtSurrondTimeTarget, txtDailyPHITarget;
    int minuteUsed, minuteWithOutUsed, avgSessionTime, avgAmpltitude, dailyPHI, avgSurrondNoise;
    int minuteUsedCurrent, minuteWithOutUsedCurrent, avgSessionTimeCurrent, avgAmpltitudeCurrent, dailyPHICurrent, avgSurrondNoiseCurrent;
    SharedPreferences phiMinutes;
    TreeMap<String, Integer> phiDataGraph = new TreeMap<>();
    FirebaseFirestore db;
    List<Integer> phiList = new ArrayList<>();
    BarChart barChart;
    private static final String TAG = "phiActivity";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ImageView badgeImage;
    List<Integer> PHIList = new ArrayList<>();
    List<String> Dates = new ArrayList<>();
    int sum = 0;
    ImageView phi_info;
    Map<String, Object> phiData, year, month, day;
    TreeMap<String, Object> DATA;
    TreeMap<String, Integer> finalData = new TreeMap<>();
    String dateData;
    int phiNum;
    TextView earAge, hearingLossAge;
    int sumPhi = 0;
    int earAgeNum, hearingLossAgeNum;
    FirebaseFirestore db1 = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();
    int curr_lvl = 1, prev_lvl = 1;
    private EditText mW, mO, length, volume;
    private DocumentReference mDataRef;
    String birthday;
    Date parseDate;
    Integer ageInt;
    TextView phiscoretv, scoretv;
    TextView earAge_textsub, earLossAge_textsub, txtphiLevel;
    LinearLayout card_dda;
    boolean phiFirst;
    SharedPreferences previousMins;
    View view;
    Animation animation, animation1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.activity_phi, container, false);
            Bugfender.init(getContext(), "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
            Bugfender.enableCrashReporting();
            sharedPreferences = getContext().getSharedPreferences("UserDetails", MODE_PRIVATE);
            animation = new AlphaAnimation(0.0f, 1.0f);
            animation1 = new AlphaAnimation(0.0f, 1.0f);
            txtInner = view.findViewById(R.id.txt_inner_phi);
            txtOuter = view.findViewById(R.id.txt_outer_phi);
            animation1.setDuration(5000);
            animation1.setStartOffset(1500);

            animation1.setRepeatMode(Animation.REVERSE);
            animation1.setRepeatCount(Animation.INFINITE);
            animation.setDuration(3000);
            animation.setStartOffset(1500);
            animation.setRepeatMode(Animation.REVERSE);
            animation.setRepeatCount(Animation.INFINITE);
            txtInner.startAnimation(animation);
            txtOuter.startAnimation(animation1);
            previousMins = getActivity().getSharedPreferences("previousMins", 0);
            minuteUsedCurrent = previousMins.getInt("pminOX", 0);
            minuteWithOutUsedCurrent = previousMins.getInt("pminWOX", 0);
            avgSessionTimeCurrent = previousMins.getInt("pAvgSession", 0);
            avgAmpltitudeCurrent = previousMins.getInt("pAvgVolume", 0);
            dailyPHICurrent = previousMins.getInt("pPHI", 0);
            avgSurrondNoiseCurrent = previousMins.getInt("pAverageDb", 0);
            phiMinutes = getContext().getSharedPreferences("PHIData", 0);
            editor = sharedPreferences.edit();
            pagerModels = new ArrayList<>();
            linearQA = view.findViewById(R.id.linear_q_a_card);
            viewPager = (ViewPager) view.findViewById(R.id.view_pager);
            tabLayout = (TabLayout) view.findViewById(R.id.tab_layout_phi);
            linearViewPager = view.findViewById(R.id.view_pager_linear);
            progressIndicator = view.findViewById(R.id.prgress_phi);
            txt100 = view.findViewById(R.id.txt_1);
            txt200 = view.findViewById(R.id.txt_2);
            txt300 = view.findViewById(R.id.txt_3);
//            txt300 = view.findViewById(R.id.txt_4);
            txtNextTarget = view.findViewById(R.id.txt_next_target_phi);
            txtNeedMore = view.findViewById(R.id.txt_need_more_phi);
            barChart = view.findViewById(R.id.br_chart1);
            phiFirst = sharedPreferences.getBoolean("phiFirst", true);
            badgeImage = view.findViewById(R.id.img_badge);
            txtphiLevel = view.findViewById(R.id.txt_level_phi);
            txtDailyChartPHI = view.findViewById(R.id.txt_daily_chart_phi);

            if (phiFirst) {
                Log.d("Here", "phiCall");
                editor.putBoolean("phiFirst", false);
                editor.apply();
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.custom_dialog_phi);
                dialog.setTitle("Custom Dialog");
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                ImageView cancelbtn2 = dialog.findViewById(R.id.btn_cancel2);
                TextView managePermission = dialog.findViewById(R.id.tv_manage_permission);

                managePermission.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ManagePermissionBackground.class);
                        startActivity(intent);
                    }
                });

                cancelbtn2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

            }
            txtAudioAmptitudeCurrent = view.findViewById(R.id.txt_avg_ampptitude_current);
            txtMinuteUsedCurrent = view.findViewById(R.id.txt_minute_used_current);
            txtMinuteUsedWithoutCurrent = view.findViewById(R.id.txt_minute_used_without_current);
            txtAudioSessionCurrent = view.findViewById(R.id.txt_avg_session_time_current);
            txtSurrondTimeCurrent = view.findViewById(R.id.txt_avg_surround_time_current);
            txtDailyPHICurrent = view.findViewById(R.id.txt_daily_phi_current);


            txtAudioAmptitudeTarget = view.findViewById(R.id.txt_avg_ampptitude_target);
            txtMinuteUsedTarget = view.findViewById(R.id.txt_minute_used_target);
            txtMinuteUsedWithoutTarget = view.findViewById(R.id.txt_minute_used_without_target);
            txtAudioSessionTarget = view.findViewById(R.id.txt_avg_session_time_target);
            txtSurrondTimeTarget = view.findViewById(R.id.txt_avg_surround_time_target);
            txtDailyPHITarget = view.findViewById(R.id.txt_daily_phi_target);
            earAge = view.findViewById(R.id.tv_earAge_text);
            hearingLossAge = view.findViewById(R.id.tv_earLossAge_text);

            scoretv = view.findViewById(R.id.tv_score);
            phi_info = view.findViewById(R.id.img_phi_info);
            card_dda = view.findViewById(R.id.card_dda);


            earAge_textsub = view.findViewById(R.id.tv_earAge_textsub);
            earLossAge_textsub = view.findViewById(R.id.tv_earLossAge_textsub);

            mDataRef = db1.collection("Users").document(mUser.getUid());
            card_dda.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), DemoDailyDataActivity.class));
                }
            });
            phi_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(getContext());
                    dialog.setContentView(R.layout.custom_dialog_phi);
                    dialog.setTitle("Custom Dialog");
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    ImageView cancelbtn2 = dialog.findViewById(R.id.btn_cancel2);
                    TextView managePermission = dialog.findViewById(R.id.tv_manage_permission);

                    managePermission.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getContext(), ManagePermissionBackground.class);
                            startActivity(intent);
                        }
                    });

                    cancelbtn2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                }
            });

            mDataRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    try {
                        birthday = task.getResult().get("bday").toString();
                        Log.d(TAG, "onComplete: Birthday " + birthday);
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        try {
                            parseDate = sdf.parse(birthday);
                            Log.d(TAG, "onCreate: Parse Date" + parseDate);
                        } catch (Exception z) {
                            Log.e(TAG, "onComplete: ", z);
                        }
                        getAge(parseDate);

                        getPhiData();
                    } catch (NullPointerException e) {
                        Log.e(TAG, "onComplete: ", e);
                    }
                }
            });
            getPhiDataList();
            linearQA.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = Uri.parse("https://www.wehear.in/phi/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
            return view;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView", e);
            throw e;
        }

    }

    private void init() {
        if (pagerModels.size() == 0) {
            addToPager("You're doing great job, Your ears are thanking you.!", R.drawable.ic_party);
        }
        ViewPagerModel[] viewPagerModels = getViewPagerModels();

        ArrayList<Fragment> fragments = new ArrayList<>();

        for (ViewPagerModel viewPagerModel : viewPagerModels) {
            PhiFragmentViewPager fragmentViewPager = PhiFragmentViewPager.getInstance(viewPagerModel);
            fragments.add(fragmentViewPager);
        }
        PhiMyPagerAdapter adapter = new PhiMyPagerAdapter(getActivity().getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager, true);

    }

    private void getPhiData() {
        DocumentReference PHIData = db1.collection("Users").document(mUser.getUid());
        PHIData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d(TAG, "Working!!!");
                    try {
                        Long phi = (Long) document.getLong("phi");
                        String gender = document.getString("gender");
                        if (phi == 0) {
                            Log.d(TAG, "Working!!! Here No" + ageInt);
                            earAge.setText(Integer.toString(ageInt));
                            // m or f
                            // 64 or 68
                            if (gender.equals("M")) {
                                hearingLossAge.setText("64");
                            } else {
                                hearingLossAge.setText("68");
                            }
                        }
                        Log.d(TAG, "Working!!! Here no" + ageInt);
                        int phiInt = phi.intValue();
                        Log.d(TAG, "Working!!! Here Also" + ageInt);
                        progressIndicator.setProgress(phiInt);
                        if (minuteUsedCurrent == 0) {
                            txtMinuteUsedCurrent.setText("--");
                        } else {
                            txtMinuteUsedCurrent.setText("" + minuteUsedCurrent);
                        }
                        if (minuteWithOutUsedCurrent == 0) {
                            txtMinuteUsedWithoutCurrent.setText("--");
                        } else {
                            txtMinuteUsedWithoutCurrent.setText("" + minuteWithOutUsedCurrent);
                        }
                        if (avgSurrondNoiseCurrent == 0) {
                            txtSurrondTimeCurrent.setText("--");
                        } else {
                            txtSurrondTimeCurrent.setText("" + avgSurrondNoiseCurrent);
                        }
                        if (avgAmpltitudeCurrent == 0) {
                            txtAudioAmptitudeCurrent.setText("--");
                        } else {
                            txtAudioAmptitudeCurrent.setText("" + avgAmpltitudeCurrent);
                        }
                        if (avgSessionTimeCurrent == 0) {
                            txtAudioSessionCurrent.setText("--");
                        } else {
                            txtAudioSessionCurrent.setText("" + avgSessionTimeCurrent);
                        }
                        if (dailyPHICurrent == 0) {
                            txtDailyPHICurrent.setText("--");
                        } else {
                            txtDailyPHICurrent.setText("" + dailyPHICurrent);
                        }


                        if (phiInt < 25) {
                            minuteUsed = 200;
                            minuteWithOutUsed = 120;
                            avgAmpltitude = 70;
                            avgSurrondNoise = 70;
                            avgSessionTime = 50;
                            dailyPHI = 15;
                            txt100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_balck));
                            txt200.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_gray));
                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_gray));
//                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_gray));
                            txtNextTarget.setText("Next Target - 25 PHI");
                            int needMore = 25 - phiInt;
                            txtNeedMore.setText("You need " + needMore + " more PHI to reach next target Consider following suggestions to improve your PHI and ear health");
                            txtMinuteUsedTarget.setText("" + minuteUsed);
                            txtMinuteUsedWithoutTarget.setText("" + minuteWithOutUsed);
                            txtAudioAmptitudeTarget.setText("" + avgAmpltitude);
                            txtAudioSessionTarget.setText("" + avgSessionTime);
                            txtSurrondTimeTarget.setText("" + avgSurrondNoise);
                            txtDailyPHITarget.setText("" + dailyPHI);
                            String minWithUsed = "We suggest you to keep your daily usage of Wehear OX to " + minuteUsed + " minutes in order to achieve your PHI target.";
                            String minWithOutUsed = "Try reducing use of in-ear earphones and headphones, to reach your target limit your use to " + minuteWithOutUsed + " minutes.";
                            String volumn = "Try keeping your average volume in between " + (avgAmpltitude - 5) + " to " + avgAmpltitude + " to achieve the PHI target.";
                            String sessionTime = "Average session time shows for how long you use your devices continuously on average, keep it below " + avgSessionTime + " minutes and take a break of 10 minutes in between.";
                            String surround = "Avoid staying in high noise environment, noise level below " + avgSurrondNoise + "db is good for your ear health.";
                            if ((minuteUsed + 20) <= minuteUsedCurrent) {
                                txtMinuteUsedCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((minuteWithOutUsed + 10) <= minuteWithOutUsedCurrent) {
                                txtMinuteUsedWithoutCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithOutUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((avgAmpltitude + 10) <= avgAmpltitudeCurrent) {
                                txtAudioAmptitudeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(volumn, R.drawable.ic_soundmeter);
                            }

                            if ((avgSessionTime + 10) <= avgSessionTimeCurrent) {
                                txtAudioSessionCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(sessionTime, R.drawable.time);
                            }
                            if ((avgSurrondNoise + 10) <= avgSurrondNoiseCurrent) {
                                txtSurrondTimeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(surround, R.drawable.ic_noise);
                            }
                            if ((dailyPHI - 5) >= dailyPHICurrent) {
                                txtDailyPHICurrent.setTextColor(getResources().getColor(R.color.pink));
                            }

                        } else if (phiInt >= 25 && phiInt < 50) {
                            minuteUsed = 180;
                            minuteWithOutUsed = 80;
                            avgAmpltitude = 60;
                            avgSessionTime = 35;
                            avgSurrondNoise = 64;
                            dailyPHI = 28;
                            txt100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target));
                            txt200.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_balck));
                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_gray));
//                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_gray));
                            txtNextTarget.setText("Next Target - 50 PHI");
                            int needMore = 50 - phiInt;
                            txtNeedMore.setText("You need " + needMore + " more PHI to reach next target Consider following suggestions to improve your PHI and ear health");
                            txtMinuteUsedTarget.setText("" + minuteUsed);
                            txtMinuteUsedWithoutTarget.setText("" + minuteWithOutUsed);
                            txtAudioAmptitudeTarget.setText("" + avgAmpltitude);
                            txtAudioSessionTarget.setText("" + avgSessionTime);
                            txtSurrondTimeTarget.setText("" + avgSurrondNoise);
                            txtDailyPHITarget.setText("" + dailyPHI);
                            String minWithUsed = "We suggest you to keep your daily usage of Wehear OX to " + minuteUsed + " minutes in order to achieve your PHI target.";
                            String minWithOutUsed = "Try reducing use of in-ear earphones and headphones, to reach your target limit your use to " + minuteWithOutUsed + " minutes.";
                            String volumn = "Try keeping your average volume in between " + (avgAmpltitude - 5) + " to " + avgAmpltitude + " to achieve the PHI target.";
                            String sessionTime = "Average session time shows for how long you use your devices continuously on average, keep it below " + avgSessionTime + " minutes and take a break of 10 minutes in between.";
                            String surround = "Avoid staying in high noise environment, noise level below " + avgSurrondNoise + "db is good for your ear health.";
                            if ((minuteUsed + 20) <= minuteUsedCurrent) {
                                txtMinuteUsedCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((minuteWithOutUsed + 10) <= minuteWithOutUsedCurrent) {
                                txtMinuteUsedWithoutCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithOutUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((avgAmpltitude + 10) <= avgAmpltitudeCurrent) {
                                txtAudioAmptitudeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(volumn, R.drawable.ic_soundmeter);
                            }

                            if ((avgSessionTime + 10) <= avgSessionTimeCurrent) {
                                txtAudioSessionCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(sessionTime, R.drawable.time);
                            }
                            if ((avgSurrondNoise + 10) <= avgSurrondNoiseCurrent) {
                                txtSurrondTimeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(surround, R.drawable.ic_noise);
                            }
                            if ((dailyPHI - 5) >= dailyPHICurrent) {
                                txtDailyPHICurrent.setTextColor(getResources().getColor(R.color.pink));
                            }
                        } else if (phiInt >= 50 && phiInt < 75) {
                            minuteUsed = 180;
                            minuteWithOutUsed = 40;
                            avgAmpltitude = 55;
                            avgSessionTime = 25;
                            avgSurrondNoise = 55;
                            dailyPHI = 43;
                            txt100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target));
                            txt200.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target));
                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_balck));
//                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_gray));
                            txtNextTarget.setText("Next Target - 75 PHI");
                            int needMore = 75 - phiInt;
                            txtNeedMore.setText("You need " + needMore + " more PHI to reach next target Consider following suggestions to improve your PHI and ear health");
                            txtMinuteUsedTarget.setText("" + minuteUsed);
                            txtMinuteUsedWithoutTarget.setText("" + minuteWithOutUsed);
                            txtAudioAmptitudeTarget.setText("" + avgAmpltitude);
                            txtAudioSessionTarget.setText("" + avgSessionTime);
                            txtSurrondTimeTarget.setText("" + avgSurrondNoise);
                            txtDailyPHITarget.setText("" + dailyPHI);
                            String minWithUsed = "We suggest you to keep your daily usage of Wehear OX to " + minuteUsed + " minutes in order to achieve your PHI target.";
                            String minWithOutUsed = "Try reducing use of in-ear earphones and headphones, to reach your target limit your use to " + minuteWithOutUsed + " minutes.";
                            String volumn = "Try keeping your average volume in between " + (avgAmpltitude - 5) + " to " + avgAmpltitude + " to achieve the PHI target.";
                            String sessionTime = "Average session time shows for how long you use your devices continuously on average, keep it below " + avgSessionTime + " minutes and take a break of 10 minutes in between.";
                            String surround = "Avoid staying in high noise environment, noise level below " + avgSurrondNoise + "db is good for your ear health.";
                            if ((minuteUsed + 20) <= minuteUsedCurrent) {
                                txtMinuteUsedCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((minuteWithOutUsed + 10) <= minuteWithOutUsedCurrent) {
                                txtMinuteUsedWithoutCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithOutUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((avgAmpltitude + 10) <= avgAmpltitudeCurrent) {
                                txtAudioAmptitudeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(volumn, R.drawable.ic_soundmeter);
                            }

                            if ((avgSessionTime + 10) <= avgSessionTimeCurrent) {
                                txtAudioSessionCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(sessionTime, R.drawable.time);
                            }
                            if ((avgSurrondNoise + 10) <= avgSurrondNoiseCurrent) {
                                txtSurrondTimeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(surround, R.drawable.ic_noise);
                            }
                            if ((dailyPHI - 5) >= dailyPHICurrent) {
                                txtDailyPHICurrent.setTextColor(getResources().getColor(R.color.pink));
                            }
                        } else if (phiInt >= 75) {
                            minuteUsed = 180;
                            minuteWithOutUsed = 20;
                            avgAmpltitude = 50;
                            avgSessionTime = 25;
                            avgSurrondNoise = 51;
                            dailyPHI = 50;
                            txt100.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target));
                            txt200.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target));
                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target));
//                            txt300.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ic_phi_target_balck));
                            txtNextTarget.setText("Next Target - 100 PHI");
                            int needMore = 100 - phiInt;
                            txtNeedMore.setText("You need " + needMore + " more PHI to reach next target Consider following suggestions to improve your PHI and ear health");
                            txtMinuteUsedTarget.setText("" + minuteUsed);
                            txtMinuteUsedWithoutTarget.setText("" + minuteWithOutUsed);
                            txtAudioAmptitudeTarget.setText("" + avgAmpltitude);
                            txtAudioSessionTarget.setText("" + avgSessionTime);
                            txtSurrondTimeTarget.setText("" + avgSurrondNoise);
                            txtDailyPHITarget.setText("" + dailyPHI);
                            String minWithUsed = "We suggest you to keep your daily usage of Wehear OX to " + minuteUsed + " minutes in order to achieve your PHI target.";
                            String minWithOutUsed = "Try reducing use of in-ear earphones and headphones, to reach your target limit your use to " + minuteWithOutUsed + " minutes.";
                            String volumn = "Try keeping your average volume in between " + (avgAmpltitude - 5) + " to " + avgAmpltitude + " to achieve the PHI target.";
                            String sessionTime = "Average session time shows for how long you use your devices continuously on average, keep it below " + avgSessionTime + " minutes and take a break of 10 minutes in between.";
                            String surround = "Avoid staying in high noise environment, noise level below " + avgSurrondNoise + "db is good for your ear health.";
                            if ((minuteUsed + 20) <= minuteUsedCurrent) {
                                txtMinuteUsedCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((minuteWithOutUsed + 10) <= minuteWithOutUsedCurrent) {
                                txtMinuteUsedWithoutCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(minWithOutUsed, R.drawable.ic_baseline_access_time_24);
                            }
                            if ((avgAmpltitude + 10) <= avgAmpltitudeCurrent) {
                                txtAudioAmptitudeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(volumn, R.drawable.ic_soundmeter);
                            }

                            if ((avgSessionTime + 10) <= avgSessionTimeCurrent) {
                                txtAudioSessionCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(sessionTime, R.drawable.time);
                            }
                            if ((avgSurrondNoise + 10) <= avgSurrondNoiseCurrent) {
                                txtSurrondTimeCurrent.setTextColor(getResources().getColor(R.color.pink));
                                addToPager(surround, R.drawable.ic_noise);
                            }
                            if ((dailyPHI - 5) >= dailyPHICurrent) {
                                txtDailyPHICurrent.setTextColor(getResources().getColor(R.color.pink));
                            }
                        }
                        ValueAnimator animator = new ValueAnimator();
                        animator.setObjectValues(0, phiInt);// here you set the range, from 0 to "count" value
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            public void onAnimationUpdate(ValueAnimator animation) {
                                scoretv.setText(String.valueOf(animation.getAnimatedValue()));

                            }
                        });

                        init();

                        animator.setDuration(1000); // here you set the duration of the anim
                        animator.start();

                        Log.d(TAG, "PHI-" + phi);
                        Log.d(TAG, "PHI-" + phiInt);
                        earAgeNum = ageInt + ((60 - phiInt)/2);
                        hearingLossAgeNum = 64 - ((60 - phiInt)/2);

                        earAge.setText("" + earAgeNum);
                        hearingLossAge.setText("" + hearingLossAgeNum);
                        if (phi >= 75) {
                            curr_lvl = 4;
                            txtphiLevel.setText("4");
                        } else if (phi >= 50) {
                            curr_lvl = 3;
                            txtphiLevel.setText("3");
                        } else if (phi >= 25) {
                            curr_lvl = 2;
                            txtphiLevel.setText("2");
                        } else {
                            curr_lvl = 1;
                            txtphiLevel.setText("1");
                        }
                        switch (curr_lvl) {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                        Bugfender.d("Error", e.toString());
                    }

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Bugfender.d("Error", e.toString());
            }
        });

    }

    private void addToPager(String text, int img) {
        pagerModels.add(new ViewPagerModel(text, img));
    }

    private ViewPagerModel[] getViewPagerModels() {
        ViewPagerModel[] viewPagerModels = new ViewPagerModel[pagerModels.size()];
        pagerModels.toArray(viewPagerModels);
        return viewPagerModels;
    }

//    private void getPhiData() {
//        DocumentReference PHIData = db1.collection("minutes-data").document(mUser.getUid());
//        PHIData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//
//                        phiData = document.getData();
//                        DATA = new TreeMap<>(phiData);
//
//                        for (String key : DATA.keySet()) {
//                            day = (Map) DATA.get(key);
//                            phiNum = Integer.parseInt(day.get("PHI").toString());
//                            PHIList.add(phiNum);
//                            Dates.add(key);
//                            finalData.put(key, phiNum);
//                        }
//                        if(PHIList.size()==1)
//                        {
//                            earAge.setText(getAge(parseDate));
//                            return;
//                        }
//                        Log.d(TAG, "onComplete: Date list- " + Dates);
//                        Log.d(TAG, "onComplete: Phi list - " + PHIList);
//                        Log.d(TAG, "onComplete: phi Data - " + finalData);
//
//                        Log.d(TAG, "Size: " + PHIList.size());
//                        if (PHIList.size() < 7) {
//                            for (int i = 0; i < PHIList.size(); i++) {
//                                sum += PHIList.get(i);
//                            }
//                        } else {
//                            for (int i = (PHIList.size() - 7); i < PHIList.size(); i++) {
//                                sum += PHIList.get(i);
//                            }
//                        }
//
//
//                        Log.d(TAG, "PHI 7 days: " + sum);
//
//                        //Ear age algorithms
//                        int temp = 0;
//                        int avg = 0;
//
//                        if (PHIList.size() < 30) {
//                            for (int i = 0; i < PHIList.size(); i++) {
//                                temp += PHIList.get(i);
//                            }
//                        } else {
//                            for (int i = (PHIList.size() - 30); i < PHIList.size(); i++) {
//                                temp += PHIList.get(i);
//                            }
//                        }
//
//                        avg = (temp / PHIList.size());
//                        Log.d(TAG, "onComplete: AVG PHI - " + avg);
//
//                        scoretv.setText(Integer.toString(sum));
//
//                        earAgeNum = ageInt + (30 - avg);
//                        hearingLossAgeNum = 64 - (30 - avg);
//
//                        earAge.setText("" + earAgeNum);
//                        hearingLossAge.setText("" + hearingLossAgeNum);
//
//
//                        if (sum >= 300) {
//                            curr_lvl = 4;
//
//                        } else if (sum >= 200) {
//                            curr_lvl = 3;
//
//                        } else if (sum >= 100) {
//                            curr_lvl = 2;
//
//                        } else {
//                            curr_lvl = 1;
//
//                        }
//
//                        if (curr_lvl > prev_lvl) {
////                            Toast.makeText(getContext(), "You have been promoted", Toast.LENGTH_LONG).show();
////                            prev_lvl = curr_lvl;
////                            switch (curr_lvl) {
////                                case 1:
////                                    badgeImage.setImageResource(R.drawable.ic_bronze_badge);
////                                    break;
////                                case 2:
////                                    badgeImage.setImageResource(R.drawable.ic_silver_badge);
////                                    break;
////                                case 3:
////                                    badgeImage.setImageResource(R.drawable.ic_gold_badge);
////                                    break;
////                                case 4:
////                                    badgeImage.setImageResource(R.drawable.ic_diamond_badge);
////                                    break;
////                            }
////                        } else if (curr_lvl < prev_lvl) {
////                            Toast.makeText(getContext(), "You have been demoted", Toast.LENGTH_LONG).show();
//                            prev_lvl = curr_lvl;
//                        }
//
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });
//
//    }

    private String getAge(Date date) {
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.setTime(date);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        ageInt = new Integer(age);
        String ageS = Integer.toString(ageInt);

        Log.d(TAG, "getAge: AGE - " + ageInt);

        return ageS;
    }

    private static boolean isIntentResolved(Context ctx, Intent intent) {
        return (intent != null && ctx.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null);
    }

    private void getPhiDataList() {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        DocumentReference PHIData = db.collection("minutes-data").document(mUser.getUid());
        PHIData.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

            }
        });
        PHIData.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot document) {
                if (document.exists()) {
                    phiData = document.getData();
                    DATA = new TreeMap<>(phiData);

                    for (String key : DATA.keySet()) {
                        day = (Map) DATA.get(key);
                        phiNum = Integer.parseInt(day.get("PHI").toString());
                        phiList.add(phiNum);
                        Dates.add(key);
                        finalData.put(key, phiNum);

                        phiDataGraph.put(key, phiNum);

                    }

                    // {30,16,7,30,16,7,25}

                    Log.d(TAG, "phiData: Date list- " + Dates);
                    Log.d(TAG, "phiData: Phi list - " + phiList);
                    Log.d(TAG, "phiData: phi Data - " + finalData);
                    getGraph(phiDataGraph);
                } else {
                    Log.d(TAG, "No such document");
                }
            }
        });
//        PHIData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isComplete()) {
//                    DocumentSnapshot document = task.getResult();
//                    if (document.exists()) {
//
//                        phiData = document.getData();
//                        DATA = new TreeMap<>(phiData);
//
//                        for (String key : DATA.keySet()) {
//                            day = (Map) DATA.get(key);
//                            phiNum = Integer.parseInt(day.get("PHI").toString());
//                            phiList.add(phiNum);
//                            Dates.add(key);
//                            finalData.put(key, phiNum);
//                            phiDataGraph.put(key, phiNum);
//                        }
//                        // {30,16,7,30,16,7,25}
//
//                        Log.d(TAG, "phiData: Date list- " + Dates);
//                        Log.d(TAG, "phiData: Phi list - " + phiList);
//                        Log.d(TAG, "phiData: phi Data - " + finalData);
//                        getGraph(phiDataGraph);
//                    } else {
//                        Log.d(TAG, "No such document");
//                    }
//                } else {
//                    Log.d(TAG, "get failed with ", task.getException());
//                }
//            }
//        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//
//                DocumentReference userPHI = db.collection("Users").document(mUser.getUid());
//                userPHI.update("phi", sumPhi).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "sumPhi afterupload" + sumPhi);
//                        phiList.clear();
//                        phiData.clear();
//
//                        db.terminate();
//                    }
//                });
//            }
//        }, 2000);
    }

    private void getGraph(final TreeMap<String, Integer> dataTreeMap) {
        Log.d(TAG, "In graph:data:" + dataTreeMap.size());
        if (dataTreeMap.size() > 1) {
            ArrayList<BarEntry> entries = new ArrayList<>();

            for (int i = 0; i < dataTreeMap.size(); i++) {
                entries.add(new BarEntry(i, (dataTreeMap.get(Dates.get(i)) + 50)));

            }
            //entries.add(new BarEntry(.size(), 0));

            ArrayList xVals = new ArrayList();
            for (int i = 0; i < phiDataGraph.size(); i++) {

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
                try {
                    Date parseDate = sdf.parse(Dates.get(i));
                    SimpleDateFormat day = new SimpleDateFormat("dd-MM");
                    String showDate = day.format(parseDate);
                    xVals.add(showDate);
                } catch (Exception z) {
                    Log.e(TAG, "onComplete: ", z);
                }
            }

            Log.d(TAG, "In graph:" + entries.size());
            final BarDataSet barDataSet = new BarDataSet(entries, "PHI");
            barDataSet.setDrawValues(false);
            barDataSet.setColors(R.color.black_overlay);
            barDataSet.setHighLightColor(Color.parseColor("#f72672"));
            barDataSet.setHighLightAlpha(255);
            barDataSet.setHighlightEnabled(true);
            BarData data;
            data = new BarData(barDataSet);
//            data.setBarWidth(0.2f);
            barChart.setData(data);
            barChart.setScaleEnabled(false);
            barChart.getAxisLeft().setDrawGridLines(false);
            barChart.getXAxis().setDrawGridLines(false);
            barChart.getAxisRight().setDrawGridLines(false);
            barChart.getAxisRight().setEnabled(false);
            barChart.getAxisLeft().setEnabled(false);
            barChart.getXAxis().setEnabled(false);
            barChart.getLegend().setEnabled(false);
            barChart.getDescription().setEnabled(false);
            barChart.getAxisLeft().setStartAtZero(true);
            //barChart.getAxisLeft().setAxisMaxValue(500);
            barChart.getBarData().setBarWidth(0.2f);
//            if(entries.size()>8) {
//                barChart.setScaleMinima(Dates.size() / 8f, 1f);
//            }
//            if(entries.size()>7) {
            barChart.setVisibleXRange(6, 8);
//            }
//            else{
//                barChart.setVisibleXRange(entries.size(),entries.size());
//            }
//        barChart.setPinchZoom(true);
            barChart.setScaleEnabled(true);
            barChart.setDoubleTapToZoomEnabled(false);
            barChart.setDrawGridBackground(false);
            barChart.setDragEnabled(true);
            barChart.setFitBars(true);
            barChart.getXAxis().setEnabled(true);
            barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xVals));


            barChart.animateY(1000);
//        barChart.getXAxis().setAxisMaximum(barDataSet.getXMax() + 0.25f);
//        barChart.getXAxis().setAxisMinimum(barDataSet.getXMin() - 0.25f);
            barChart.animate();

            barChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    int s = (int) e.getX();
                    // replace the color at the specified index
                    barChart.invalidate();
                    txtDailyChartPHI.setText("Day's PHI : " + (phiDataGraph.get(Dates.get(s))));
                }


                @Override
                public void onNothingSelected() {
                }
            });
            barChart.moveViewToX(entries.size() - 5);
            barChart.invalidate(); // refresh
            txtDailyChartPHI.setText("Day's PHI : " + (phiDataGraph.get(Dates.get(Dates.size() - 1))));
        }
    }


}
