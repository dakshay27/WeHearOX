package com.wehear.ox.DailyData;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wehear.ox.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DemoDailyDataActivity extends AppCompatActivity implements OnChartValueSelectedListener{


    private static final String TAG = "DailyDataAnalysis";
    private BarChart chart;
    List<Entry> BARENTRY;
    List<String> BarEntryLabels;
    BarDataSet Bardataset;
    BarData BARDATA;
    ImageView backbtn8;
    private Button nextButton;
    public TextView mMinuteUsedOx, mMinuteUsedOther, mAverageVolume, mAverageSession;
    Map<String, Object> mapData;
    Map day;
    TreeMap<String, Object> DATA;
    List<String> Dates = new ArrayList<>();
    TextView dateText;
    TextView mAveragedb, mPhivalue;
    LinearLayout card_MOX, card_MWOX, card_AVGST, card_AVGVOL, card_AVGSURNOICE, card_DPHI;

    int month = 01, year = 2021;


    int minUsedOx, minUsedWOx, avgSession, avgVolume, avgDB, phi;
    TreeMap<String, Integer> mOX = new TreeMap<>();
    TreeMap<String, Integer> mWOX = new TreeMap<>();
    TreeMap<String, Integer> aSL = new TreeMap<>();
    TreeMap<String, Integer> aV = new TreeMap<>();
    TreeMap<String, Integer> aD = new TreeMap<>();
    TreeMap<String, Integer> PHI = new TreeMap<>();

    int i = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser mUser = mAuth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_daily_data);

        findViewById();

        card_MOX.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {


                card_MOX.setBackgroundResource(R.drawable.rounded_corner_current_user);
                card_MWOX.setBackgroundResource(R.color.white);
                card_AVGST.setBackgroundResource(R.color.white);
                card_AVGVOL.setBackgroundResource(R.color.white);
                card_AVGSURNOICE.setBackgroundResource(R.color.white);
                card_DPHI.setBackgroundResource(R.color.white);
                getGraph(mOX);
            }
        });
        card_MWOX.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {


                card_MOX.setBackgroundResource(R.color.white);
                card_MWOX.setBackgroundResource(R.drawable.rounded_corner_current_user);
                card_AVGST.setBackgroundResource(R.color.white);
                card_AVGVOL.setBackgroundResource(R.color.white);
                card_AVGSURNOICE.setBackgroundResource(R.color.white);
                card_DPHI.setBackgroundResource(R.color.white);
                getGraph(mWOX);
            }
        });
        card_AVGST.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {


                card_MOX.setBackgroundResource(R.color.white);
                card_MWOX.setBackgroundResource(R.color.white);
                card_AVGST.setBackgroundResource(R.drawable.rounded_corner_current_user);
                card_AVGVOL.setBackgroundResource(R.color.white);
                card_AVGSURNOICE.setBackgroundResource(R.color.white);
                card_DPHI.setBackgroundResource(R.color.white);
                getGraph(aSL);
            }
        });
        card_AVGVOL.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {


                card_MOX.setBackgroundResource(R.color.white);
                card_MWOX.setBackgroundResource(R.color.white);
                card_AVGST.setBackgroundResource(R.color.white);
                card_AVGVOL.setBackgroundResource(R.drawable.rounded_corner_current_user);
                card_AVGSURNOICE.setBackgroundResource(R.color.white);
                card_DPHI.setBackgroundResource(R.color.white);
                getGraph(aV);
            }
        });

        card_AVGSURNOICE.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {


                card_MOX.setBackgroundResource(R.color.white);
                card_MWOX.setBackgroundResource(R.color.white);
                card_AVGST.setBackgroundResource(R.color.white);
                card_AVGVOL.setBackgroundResource(R.color.white);
                card_AVGSURNOICE.setBackgroundResource(R.drawable.rounded_corner_current_user);
                card_DPHI.setBackgroundResource(R.color.white);
                getGraph(aD);
            }
        });
//        card_DPHI.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("ResourceAsColor")
//            @Override
//            public void onClick(View v) {
//
//
//                card_MOX.setBackgroundResource(R.color.white);
//                card_MWOX.setBackgroundResource(R.color.white);
//                card_AVGST.setBackgroundResource(R.color.white);
//                card_AVGVOL.setBackgroundResource(R.color.white);
//                card_AVGSURNOICE.setBackgroundResource(R.color.white);
//                card_DPHI.setBackgroundResource(R.drawable.rounded_corner_current_user);
//                getGraph(PHI);
//            }
//        });


        demoData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                card_MOX.setBackgroundResource(R.drawable.rounded_corner_current_user);
                card_MWOX.setBackgroundResource(R.color.white);
                card_AVGST.setBackgroundResource(R.color.white);
                card_AVGVOL.setBackgroundResource(R.color.white);
                card_AVGSURNOICE.setBackgroundResource(R.color.white);
                card_DPHI.setBackgroundResource(R.color.white);
                getGraph(mOX);
            }
        }, 1000);
    }

    private void findViewById() {
        chart = findViewById(R.id.br_chart1);
        mMinuteUsedOx = findViewById(R.id.tv_minUsedOX);
        mMinuteUsedOther = findViewById(R.id.tv_minuteUsedOther);
        mAverageVolume = findViewById(R.id.tv_avgVolume);
        mAverageSession = findViewById(R.id.tv_avgSessionLength);
        mAveragedb = findViewById(R.id.tv_averagedb);
        mPhivalue = findViewById(R.id.tv_phivalue);
        card_MOX = findViewById(R.id.card_mOX);
        card_MWOX = findViewById(R.id.card_MWOX);
        card_AVGST = findViewById(R.id.card_AVGST);
        card_AVGVOL = findViewById(R.id.card_AVGVOL);
        card_AVGSURNOICE = findViewById(R.id.card_AVGSURNOICE);
        card_DPHI = findViewById(R.id.card_DPHI);
        //dateText = findViewById(R.id.tv_Date);
        backbtn8 = findViewById(R.id.btn_back8);
        backbtn8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void getGraph(TreeMap<String, Integer> dataTreeMap) {

        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < dataTreeMap.size(); i++) {
            entries.add(new BarEntry(i, dataTreeMap.get(Dates.get(i))));
        }
        //entries.add(new BarEntry(.size(), 0));

        ArrayList xVals = new ArrayList();
        for (int i = 0; i < mOX.size(); i++) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
            try {
                Date parseDate = sdf.parse(Dates.get(i));
                SimpleDateFormat day = new SimpleDateFormat("dd/MM");
                String showDate = day.format(parseDate);
                xVals.add(showDate);
            } catch (Exception z) {
                Log.e(TAG, "onComplete: ", z);
            }

        }


        final BarDataSet barDataSet = new BarDataSet(entries, "PHI");
        barDataSet.setDrawValues(false);
        barDataSet.setColors(ContextCompat.getColor(this, R.color.black_overlay));
        barDataSet.setHighLightColor(ContextCompat.getColor(this, R.color.pink_hearing_ring));
        barDataSet.setHighLightAlpha(255);
        barDataSet.setHighlightEnabled(true);

        ArrayList<String> Date = new ArrayList<>();
        for (int i = 0; i < Dates.size(); i++) {
            Date.add(Dates.get(i));
        }
        Log.d(TAG, "getGraph: " + Date);

        BarData data;
        data = new BarData(barDataSet);

        chart.setData(data);

        chart.setScaleEnabled(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisLeft().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.getAxisLeft().setStartAtZero(true);
        //chart.getAxisLeft().setAxisMaxValue(500);

        chart.setScaleMinima(Dates.size() / 8f, 1f);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDragEnabled(true);

        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xVals));


        chart.moveViewToX(Dates.size() + 1);

        chart.animateY(1000);
        chart.animate();

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {


                int s = (int) e.getX();
                // replace the color at the specified index
                chart.invalidate();

                mMinuteUsedOx.setText(Integer.toString(mOX.get(Dates.get(s))));
                mMinuteUsedOther.setText(Integer.toString(mWOX.get(Dates.get(s))));
                mAverageSession.setText(Integer.toString(aSL.get(Dates.get(s))));
                mAverageVolume.setText(Integer.toString(aV.get(Dates.get(s))));
                mAveragedb.setText(Integer.toString(aD.get(Dates.get(s))));
                mPhivalue.setText(Integer.toString(PHI.get(Dates.get(s))));

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
                try {
                    Date parseDate = sdf.parse(Dates.get(s));
                    SimpleDateFormat day = new SimpleDateFormat("dd/MM/yyyy");
                    String showDate = day.format(parseDate);
                    dateText.setText(showDate);
                } catch (Exception z) {
                    Log.e(TAG, "onComplete: ", z);
                }
            }


            @Override
            public void onNothingSelected() {
            }
        });
        chart.invalidate(); // refresh


    }

    private  void demoData(){
        DocumentReference mData = db.collection("minutes-data").document(mUser.getUid());
        DocumentReference Ddata = mData.collection("yr").document("abc");

        Ddata.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                Log.d(TAG, "onComplete: "+task.getResult());



            }
        });
    }

    private void getData() {
        DocumentReference mData = db.collection("minutes-data").document(mUser.getUid());





        mData.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Log.d("idddddad", "getData: "+task.getResult());
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        mapData = document.getData();

                        Log.d(TAG, "onComplete: "+mapData.size());
                        if (mapData != null) {
                            DATA = new TreeMap<>(mapData);
                            Log.d("xxxix", "Map Data : " + DATA);
                        }

                        for (String key : DATA.keySet()) {
                            day = (Map) DATA.get(key);

                            Dates.add(key+ " "+month+" "+year);
                            Log.d(TAG, "date: "+key+ " "+month+" "+year);
                            minUsedOx = Integer.parseInt(day.get("minUsedOX").toString());
                            minUsedWOx = Integer.parseInt(day.get("minUsedWOX").toString());
                            avgSession = Integer.parseInt(day.get("avgSession").toString());
                            avgVolume = Integer.parseInt(day.get("avgVolume").toString());
                            try {
                                avgDB = Integer.parseInt(day.get("averageDb").toString());
                            } catch (Exception e) {
                                avgDB = 0;
                            }
                            phi = Integer.parseInt(day.get("PHI").toString());


                            mOX.put(key, minUsedOx);
                            mWOX.put(key, minUsedWOx);
                            aSL.put(key, avgSession);
                            aV.put(key, avgVolume);
                            aD.put(key, avgDB);
                            PHI.put(key, phi);
                        }

                        Log.d(TAG, "MOX - " + mOX);
                        Log.d(TAG, "MWOX - " + mWOX);
                        Log.d(TAG, "ASL - " + aSL);
                        Log.d(TAG, "AV - " + aV);
                        Log.d(TAG, "AD -" + aD);
                        Log.d(TAG, "phi -" + PHI);

                        mMinuteUsedOx.setText(Integer.toString(mOX.get(Dates.get(Dates.size() - 1))));
                        mMinuteUsedOther.setText(Integer.toString(mWOX.get(Dates.get(Dates.size() - 1))));
                        mAverageSession.setText(Integer.toString(aSL.get(Dates.get(Dates.size() - 1))));
                        mAverageVolume.setText(Integer.toString(aV.get(Dates.get(Dates.size() - 1))));
                        mAveragedb.setText(Integer.toString(aD.get(Dates.get(Dates.size() - 1))));
                        mPhivalue.setText(Integer.toString(PHI.get(Dates.get(Dates.size() - 1))));

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd");
                        try {
                            Date parseDate = sdf.parse(Dates.get(Dates.size() - 1));
                            SimpleDateFormat day = new SimpleDateFormat("dd/MM/yyyy");
                            String showDate = day.format(parseDate);
                            dateText.setText(showDate);
                        } catch (Exception e) {
                            Log.e(TAG, "onComplete: ", e);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

}