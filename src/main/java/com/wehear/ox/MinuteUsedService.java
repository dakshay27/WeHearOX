package com.wehear.ox;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MinuteUsedService extends Service implements TextToSpeech.OnInitListener {
    TextToSpeech tts;
    SharedPreferences phiMinutes,permission;
    SharedPreferences.Editor editor;
    public static final String CHANNEL_ID = "minuteUsedChannel";
    private static final String TAG = "MyTimeUsedService";
    static final private double EMA_FILTER = 0.6;
    public static Runnable runnable = null;
    public static int userPhi = 0;
    public static int minutesUsed = 0;
    public static int minuteUsedwithoutOx = 0;
    public static int averageVolume = 0;
    public static int mAverageVol = 0;
    public static int session_len = 0;
    public static int i = 0;
    public static int k = 0;
    public static int j = 0;
    public static int sum = 0;
    public static int avgsession = 0;
    public static int avgDecible = 0;
    public static boolean wasInternetConnected = true;
    public static int pminutesUsed = 0;
    public static int pminuteUsedwithoutOx = 0;
    public static int pmAverageVol = 0;
    public static int pavgsession = 0;
    public static int pPHI = 0;
    public static int pDB = 0;
    public static String pDate;
    public static String status;
    public static String address;
    private static double mEMA = 0.0;
    private double PHI0,PHI1,PHI2,PHI3,PHI4,PHI5,PHI6;


    final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
    public Context context = this;
    public Handler handler = null;
    public List<Integer> SessionLength = new ArrayList<>();
    boolean isRunning = false;
    CollectionReference minDataRef;
    DocumentReference mDocRef;
    String saveDate;
    double mulW, mulO, paiFinal, mulH;
    boolean isAllowed;
    List<Integer> PHIList = new ArrayList<>();
    int PHI, z = 0;
    double sumPHI = 0;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    MediaRecorder mRecorder;
    Thread runner;
    int ampl;
    int amp;
    int dB, sumDB = 0, avgDB;
    int minUsed70 = 0, minUsed80 = 0, minUsed85 = 0, minUsed90 = 0, minUsed90p = 0, minMicError = 0;
    int pMinUsed70 = 0, pMinUsed80 = 0, pMinUsed85 = 0, pMinUsed90 = 0, pMinUsed90p = 0;
    boolean updateSuccess = false;
    ArrayList<String> updateTime = new ArrayList<>();
    ArrayList<String> checkUpdateTime = new ArrayList<String>();
    ArrayList<String> checkUpdateTimeFlag = new ArrayList<String>();
    private BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            for (BluetoothDevice device : proxy.getConnectedDevices()) {
                address = device.getAddress();
                Log.d(TAG, "onServiceConnected: " + address);
            }
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy);
        }
    };

    public MinuteUsedService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        permission = getSharedPreferences("permission", 0);
        editor = permission.edit();

        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.RECORD_AUDIO,
                context.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            // do stuff
            editor.putBoolean("constantMonitoring", false);
        }else {
            editor.putBoolean("constantMonitoring", true);
        }

        createNotificationChannel();
        checkConnected();
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
//        String input = intent.getStringExtra("inputExtra");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        phiMinutes = getSharedPreferences("PHIData", 0);
        final SharedPreferences minutes = getSharedPreferences("minutes", 0);


        minutesUsed = minutes.getInt("mWithOx", 0);
        minuteUsedwithoutOx = minutes.getInt("mWithoutOx", 0);
        mAverageVol = minutes.getInt("mAverageVol", 0);
        avgsession = minutes.getInt("mAverageSess", 0);
        avgDecible = minutes.getInt("mAverageDb", 0);
//        minUsed70 = minutes.getInt("m70", 0);
//        minUsed80 = minutes.getInt("m80", 0);
//        minUsed85 = minutes.getInt("m85", 0);
//        minUsed90 = minutes.getInt("m90", 0);
//        minUsed90p = minutes.getInt("m90p", 0);
        userPhi = phiMinutes.getInt("PHI", 0);
        minMicError = minutes.getInt("mMicError", 0);


        final SharedPreferences previousMins = getSharedPreferences("previousMins", 0);
        final SharedPreferences updateStatus = getSharedPreferences("updateStatus", 0);
        final SharedPreferences.Editor updateEditor = updateStatus.edit();
        final SharedPreferences rankPreference = PreferenceManager.getDefaultSharedPreferences(MinuteUsedService.this);

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            status = "is connected";
        } else {
            status = "isn't connected";
        }

        final RemoteViews collaseView = new RemoteViews(getPackageName(),
                R.layout.custom_notification_collapase);
        final RemoteViews expandedView = new RemoteViews(getPackageName(),
                R.layout.custom_notification_expandable);


        mBuilder.setSmallIcon(R.drawable.ic_ox_icon);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setPriority(NotificationCompat.PRIORITY_MIN);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);
        mBuilder.setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(collaseView)
                .setCustomBigContentView(expandedView)
                .setColorized(true);
        collaseView.setTextViewText(R.id.txt_total_today_use_collapse, "Today's use is " + returnHourToMinutes(minutesUsed + minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minutesUsed + minuteUsedwithoutOx) + "m " + " minutes");
        collaseView.setTextViewText(R.id.text_connect_or_not, "WeHear OX " + status);
        expandedView.setTextViewText(R.id.txt_total_today_use_expanable, "Today's use is " + returnHourToMinutes(minutesUsed + minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minutesUsed + minuteUsedwithoutOx) + "m " + " minutes");
        expandedView.setTextViewText(R.id.text_connect_or_not, "WeHear OX " + status);
        expandedView.setTextViewText(R.id.txt_total_use_with_ox, returnHourToMinutes(minutesUsed) + "h " + returnMinutesToRemains(minutesUsed) + "m");
        expandedView.setTextViewText(R.id.txt_total_use_with_out_ox, returnHourToMinutes(minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minuteUsedwithoutOx) + "m");
        expandedView.setTextViewText(R.id.txt_current_phi, String.valueOf(userPhi));
        expandedView.setTextViewText(R.id.txt_avg_volume, String.valueOf(mAverageVol));
        expandedView.setTextViewText(R.id.txt_avg_session_time, String.valueOf(avgsession) + "m");
        if (dB < 0) {
            expandedView.setTextViewText(R.id.txt_surround_time, "0" + "db");
        } else {
            expandedView.setTextViewText(R.id.txt_surround_time, String.valueOf(dB) + "db");
        }
        if (dB > 80) {
            expandedView.setTextColor(R.id.txt_surround_time, Color.parseColor("#f0382b"));
        } else {
            expandedView.setTextColor(R.id.txt_surround_time, ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setLightColor(Color.BLUE);
            serviceChannel.setImportance(NotificationManager.IMPORTANCE_UNSPECIFIED);
            serviceChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
            Log.d(TAG, "createNotificationChannel: Service Notification Channel Created" + serviceChannel);
            Log.d(TAG, "createNotificationChannel: Service Notification Channel Created" + serviceChannel);
        }
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Example Service")
//                .setContentText(input)
//                .setSmallIcon(R.drawable.transparent_app_logo_splash)
//                .setContentIntent(pendingIntent)
//                .build();
        final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.notify(11, mBuilder.build());
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            startForeground(11, mBuilder.build(),FOREGROUND_SERVICE_TYPE_MICROPHONE);
//        }
//        else{
            startForeground(11, mBuilder.build());
//        }

        //startRecorder();
        //getAmplitudeEMA();

        Bugfender.init(MinuteUsedService.this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                try {

                    Bugfender.e(TAG, "Runnable started");
                    checkUpdateTime.add("00:15");
                    checkUpdateTime.add("02:00");
                    checkUpdateTime.add("04:00");
                    checkUpdateTime.add("08:00");
                    checkUpdateTime.add("12:00");
                    checkUpdateTime.add("16:00");
                    checkUpdateTime.add("20:00");
                    checkUpdateTimeFlag.add("16:50");
                    checkUpdateTimeFlag.add("16:51");
                    checkUpdateTimeFlag.add("16:52");
                    checkUpdateTimeFlag.add("16:53");
                    checkUpdateTimeFlag.add("16:54");
                    updateTime.add("23:55");




                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mBuilder.setCustomContentView(collaseView)
                            .setCustomBigContentView(expandedView)
                            .setStyle(new NotificationCompat.DecoratedCustomViewStyle());


                    collaseView.setTextViewText(R.id.txt_total_today_use_collapse, "Today's use is " + returnHourToMinutes(minutesUsed + minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minutesUsed + minuteUsedwithoutOx) + "m " + " minutes");
                    collaseView.setTextViewText(R.id.text_connect_or_not, "WeHear OX " + status);
                    expandedView.setTextViewText(R.id.txt_total_today_use_expanable, "Today's use is " + returnHourToMinutes(minutesUsed + minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minutesUsed + minuteUsedwithoutOx) + "m " + " minutes");
                    expandedView.setTextViewText(R.id.text_connect_or_not, "WeHear OX " + status);
                    expandedView.setTextViewText(R.id.txt_total_use_with_ox, returnHourToMinutes(minutesUsed) + "h " + returnMinutesToRemains(minutesUsed) + "m");
                    expandedView.setTextViewText(R.id.txt_total_use_with_out_ox, returnHourToMinutes(minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minuteUsedwithoutOx) + "m");
                    expandedView.setTextViewText(R.id.txt_current_phi, String.valueOf(userPhi));
                    expandedView.setTextViewText(R.id.txt_avg_volume, String.valueOf(mAverageVol));
                    expandedView.setTextViewText(R.id.txt_avg_session_time, String.valueOf(avgsession) + "m");
                    if (dB < 0) {
                        expandedView.setTextViewText(R.id.txt_surround_time, "0" + "db");
                    } else {
                        expandedView.setTextViewText(R.id.txt_surround_time, String.valueOf(dB) + "db");
                    }
                    if (dB > 80) {
                        expandedView.setTextColor(R.id.txt_surround_time, Color.parseColor("#f0382b"));
                    } else {
                        expandedView.setTextColor(R.id.txt_surround_time, ResourcesCompat.getColor(getResources(), R.color.colorSecondary, null));
                    }
                    AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    assert manager != null;
                    if (manager.isMusicActive() || (manager.getMode() == AudioManager.MODE_IN_CALL)) {
                        int currentVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        int maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                        int currentVolumePercentage = 100 * currentVolume / maxVolume;
                        i++;
                        k++;
                        j = 0;
                        if (currentVolumePercentage < 40)
                            currentVolumePercentage = 40;
                        averageVolume += currentVolumePercentage;
                        if (k > 9) {
                            session_len = k;
                        }

                        Log.d(TAG, "run: " + k);
                        mAverageVol = averageVolume / i;

                        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
                            checkConnected();
                            if (address.contains("11:11:22") || address.contains("08:A3:0B") || address.contains("04:AA:00")) {
                                minutesUsed++;
                                Log.d(TAG, "Using OX" + minutesUsed);
//                                SharedPreferences.Editor mEditor = minutes.edit();
//                                mEditor.putInt("minUs", minutesUsed);
//                                mEditor.apply();
//                                mBuilder.setContentTitle("Today's use is " + minutesUsed + " minutes");
//                                mBuilder.setContentText("WeHear OX is connected");
                            } else {
                                minuteUsedwithoutOx++;
                                Log.d(TAG, "Using OX" + minutesUsed);
                                Log.d(TAG, "Not Using OX" + minuteUsedwithoutOx);

                            }
                            mNotificationManager.notify(11, mBuilder.build());
                        } else {
                            minuteUsedwithoutOx++;
                            mNotificationManager.notify(11, mBuilder.build());
                            Log.d(TAG, "Not Using OX" + minuteUsedwithoutOx);
                        }
                        SharedPreferences.Editor mEditor = minutes.edit();
                        mEditor.putInt("mWithOx", minutesUsed);
                        mEditor.putInt("mWithoutOx", minuteUsedwithoutOx);
                        mEditor.putInt("mAverageVol", mAverageVol);

                        mEditor.apply();

                        // TODO - Solve Service Restart Issue

                    } else {
                        if (j < 5) {
                            j++;
                            Log.d(TAG, "j: " + j);
                        }
                        if (j >= 5 && k > 10) {
                            SessionLength.add(session_len);
                            Log.d(TAG, "Sesion Length array: " + SessionLength);
                            Log.d(TAG, "Sesion Length" + session_len);
                            sum += SessionLength.get(SessionLength.size() - 1);
                            avgsession = sum / SessionLength.size();

                            Log.d(TAG, "sum: " + sum);
                            Log.d(TAG, "Average: " + avgsession);

                            SharedPreferences.Editor mEditor = minutes.edit();
                            mEditor.putInt("mAverageSess", avgsession);
                            mEditor.apply();

                            k = 0;
                            session_len = 0;
                            j = 0;
                        } else if (j > 4 && k < 10) {
                            k = 0;
                            j = 0;

                        }
                    }
                } catch (Exception ignored) {

                }

                SharedPreferences permission = getSharedPreferences("permission", 0);
                isAllowed = permission.getBoolean("constantMonitoring", true);
                Log.d(TAG, "onStartCommand: IS ALLOWED - " + isAllowed);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                Date currentTime = new Date(System.currentTimeMillis());
                String time = sdf.format(currentTime);
                Log.d(TAG, "currentTime: " + time);
                if(updateTime.contains(time))
                {

                    final Map<String, Object> minData = new HashMap<>();
                    minData.put("minUsedOX", minutesUsed);
                    minData.put("minUsedWOX", minuteUsedwithoutOx);
                    minData.put("avgSession", avgsession);
                    minData.put("avgVolume", mAverageVol);
                    minData.put("averageDb", avgDB);
                    minData.put("m70", minUsed70);
                    minData.put("m80", minUsed80);
                    minData.put("m85", minUsed85);
                    minData.put("m90", minUsed90);
                    minData.put("m90p", minUsed90p);
                    minData.put("mMicError", minMicError);
                    Calendar calendar = Calendar.getInstance();
                    int currentDay = calendar.get(Calendar.DAY_OF_WEEK);

                    Date todayDate = new Date(System.currentTimeMillis());
                    SimpleDateFormat day = new SimpleDateFormat("yyyy MM dd");
                    saveDate = day.format(todayDate);
                    SharedPreferences.Editor previousEditor = previousMins.edit();
                    previousEditor.putInt("pminOX", minutesUsed);
                    previousEditor.putInt("pminWOX", minuteUsedwithoutOx);
                    previousEditor.putInt("pAvgSession", avgsession);
                    previousEditor.putInt("pAvgVolume", mAverageVol);
                    previousEditor.putInt("pAverageDb", avgDB);
                    previousEditor.putInt("pM70", minUsed70);
                    previousEditor.putInt("pM80", minUsed80);
                    previousEditor.putInt("pM85", minUsed85);
                    previousEditor.putInt("pM90", minUsed90);
                    previousEditor.putInt("pM90p", minUsed90p);
                    previousEditor.putString("pDate", saveDate);
                    previousEditor.putInt("pDay",currentDay);


                    previousEditor.apply();




                    getPhiData(calculatePHI(avgsession,minutesUsed,minuteUsedwithoutOx,mAverageVol),minData,saveDate,currentDay);
                }

                if(checkUpdateTime.contains(time))
                {
                    int status = updateStatus.getInt("updateStatus", 0);
                    if(status==0)
                    {
                        pminutesUsed = previousMins.getInt("pminOX", 0);
                        pminuteUsedwithoutOx = previousMins.getInt("pminWOX", 0);
                        pavgsession = previousMins.getInt("pAvgSession", 0);
                        pmAverageVol = previousMins.getInt("pAvgVolume", 0);
                        pDate = previousMins.getString("pDate", "");
                        pDB = previousMins.getInt("pAverageDb", 0);
                        pMinUsed70 = previousMins.getInt("pM70", 0);
                        pMinUsed80 = previousMins.getInt("pM80", 0);
                        pMinUsed85 = previousMins.getInt("pM85", 0);
                        pMinUsed90 = previousMins.getInt("pM90", 0);
                        pMinUsed90p = previousMins.getInt("pM90p", 0);
                        int pDay = previousMins.getInt("pDay",0);
                        Map<String, Object> minData = new HashMap<>();
                        minData.put("minUsedOX", pminutesUsed);
                        minData.put("minUsedWOX", pminuteUsedwithoutOx);
                        minData.put("avgSession", pavgsession);
                        minData.put("avgVolume", pmAverageVol);
                        minData.put("averageDb", pDB);
                        minData.put("m70", pMinUsed70);
                        minData.put("m80", pMinUsed80);
                        minData.put("m85", pMinUsed85);
                        minData.put("m90", pMinUsed90);
                        minData.put("m90p", pMinUsed90p);


                        getPhiData(calculatePHI(pavgsession,pminutesUsed,pminuteUsedwithoutOx,pmAverageVol),minData,pDate, pDay);


                    }


                }


                minMicError++;
                Log.d(TAG, "run: Service running");
                handler.postDelayed(runnable, 60000);
            }
        };
        handler.postDelayed(runnable, 65000);

        return START_STICKY;
    }

    private double calculatePHI(int avgsessionL,int minutesUsedL,int minuteUsedwithoutOxL,int mAverageVolL) {

        if (avgsessionL > 90) {
            mulW = 2;
            mulO = 2;
        } else if (avgsessionL > 70) {
            mulW = 1.5;
            mulO = 1.5;
        } else if (avgsessionL > 25) {
            mulW = 1;
            mulO = 1.15;
        } else {
            mulW = 1;
            mulO = 1;
        }

        if (minutesUsedL > 480) {
            mulH = 0.2;
        } else if (minutesUsedL > 420) {
            mulH = 0.3;
        } else if (minutesUsedL > 360) {
            mulH = 0.4;
        } else if (minutesUsedL > 300) {
            mulH = 0.5;
        } else if (minutesUsedL > 240) {
            mulH = 0.65;
        } else if (minutesUsedL > 180) {
            mulH = 0.8;
        } else if (minutesUsedL > 120) {
            mulH = 0.9;
        } else {
            mulH = 1;
        }


        Log.d(TAG, "onClick: mulW-" + mulW + "mulO-" + mulO);

        if (mAverageVolL < 40) {
            mAverageVolL = 40;
        }

        paiFinal = (((minutesUsedL / mulW) * mulH) - (minuteUsedwithoutOxL * mulO) + ((100 - mAverageVolL) * 5)) / 5;

        z++;
        return paiFinal;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Created");
        //super.startForeground(11, mBuilder.build());
        FirebaseApp.initializeApp(context);

        Bugfender.d(TAG, "Created");
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        isRunning = false;
        Log.d(TAG, "onDestroy: App Destroy " + isRunning);
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        //Send broadcast to the Activity to kill this service and restart it.
        super.onLowMemory();
        Log.d(TAG, "onDestroy: ram clearrrrrr");
        Intent serviceIntent = new Intent(context, MinuteUsedService.class);
        serviceIntent.putExtra("inputExtra", "Value");
        ContextCompat.startForegroundService(context, serviceIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //Send broadcast to the Activity to restart the service
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onDestroy: destroyyyyyyy");

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void checkConnected() {

        BluetoothAdapter.getDefaultAdapter().getProfileProxy(this, serviceListener, BluetoothProfile.HEADSET);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Minute Used Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

//    public void onResume() {
//        super.onResume();
//        startRecorder();
//    }
//
//    public void onPause() {
//        super.onPause();
//        stopRecorder();
//    }

    public void startRecorder() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");

            try {
                mRecorder.prepare();
            } catch (java.io.IOException ioe) {
                Log.e("[Monkey]", "IOException: " + Log.getStackTraceString(ioe));

            } catch (SecurityException e) {
                Log.e("[Monkey]", "SecurityException: " + Log.getStackTraceString(e));
            }
            try {
                mRecorder.start();
            } catch (SecurityException e) {
                Log.e("[Monkey]", "SecurityException: " + Log.getStackTraceString(e));
            } catch (Exception e) {
                e.printStackTrace();
            }

            //mEMA = 0.0;
        }

    }

    public void stopRecorder() {
        if (mRecorder != null) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public double soundDb(double ampl) {
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
        dB = (int) ((20 * Math.log10(amp)) - 5);


        return dB;
    }

    private void getPhiData(final double finalPhi, Map<String, Object> minData, String saveDate, final int currentDay) {
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();


        minData.put("PHI", Math.round(finalPhi));
        final HashMap<String, Object> Date = new HashMap<>();
        Date.put(saveDate, minData);
        DocumentReference minDataDocRef = db.collection("minutes-data").document(mUser.getUid());
        minDataDocRef.update(Date).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                final SharedPreferences updateStatus = getSharedPreferences("updateStatus", 0);
                final SharedPreferences minutes = getSharedPreferences("minutes", 0);

                minutesUsed = 0;
                minuteUsedwithoutOx = 0;
                avgsession = 0;
                mAverageVol = 0;
                avgDB = 0;
                minUsed70 = 0;
                minUsed80 = 0;
                minUsed85 = 0;
                minUsed90 = 0;
                minUsed90p = 0;
                minMicError = 0;
                SharedPreferences.Editor mEditor = minutes.edit();
                mEditor.putInt("mWithOx", minutesUsed);
                mEditor.putInt("mWithoutOx", minuteUsedwithoutOx);
                mEditor.putInt("mAverageVol", mAverageVol);
                mEditor.putInt("mAverageSess", avgsession);
                mEditor.putInt("mAverageDb", avgDB);
                mEditor.putInt("m70", minUsed70);
                mEditor.putInt("m80", minUsed80);
                mEditor.putInt("m85", minUsed85);
                mEditor.putInt("m90", minUsed90);
                mEditor.putInt("m90p", minUsed90p);
                mEditor.putInt("mMicError", minMicError);
                mEditor.apply();
                final SharedPreferences.Editor updateEditor = updateStatus.edit();
                updateEditor.putInt("updateStatus", 1);
                Bugfender.d(TAG, "Kalp" + "- Update Successful");
                updateEditor.apply();
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                final RemoteViews collaseView = new RemoteViews(getPackageName(),
                        R.layout.custom_notification_collapase);
                final RemoteViews expandedView = new RemoteViews(getPackageName(),
                        R.layout.custom_notification_expandable);

                mBuilder.setCustomContentView(collaseView)
                        .setCustomBigContentView(expandedView)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
                collaseView.setTextViewText(R.id.txt_total_today_use_collapse, "Today's use is " + returnHourToMinutes(minutesUsed + minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minutesUsed + minuteUsedwithoutOx) + "m " + " minutes");
                collaseView.setTextViewText(R.id.text_connect_or_not, "WeHear OX " + status);
                expandedView.setTextViewText(R.id.txt_total_today_use_expanable, "Today's use is " + returnHourToMinutes(minutesUsed + minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minutesUsed + minuteUsedwithoutOx) + "m " + " minutes");
                expandedView.setTextViewText(R.id.text_connect_or_not, "WeHear OX " + status);
                expandedView.setTextViewText(R.id.txt_total_use_with_ox, returnHourToMinutes(minutesUsed) + "h " + returnMinutesToRemains(minutesUsed) + "m");
                expandedView.setTextViewText(R.id.txt_total_use_with_out_ox, returnHourToMinutes(minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minuteUsedwithoutOx) + "m");
                expandedView.setTextViewText(R.id.txt_current_phi, String.valueOf(userPhi));
                expandedView.setTextViewText(R.id.txt_avg_volume, String.valueOf(mAverageVol));
                expandedView.setTextViewText(R.id.txt_avg_session_time, String.valueOf(avgsession) + "m");
                final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(11, mBuilder.build());
                final DocumentReference userData = db.collection("Users").document(mUser.getUid());

                db.runTransaction(new Transaction.Function<Double>() {
                    @Nullable
                    @Override
                    public Double apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        DocumentSnapshot documentSnapshot = transaction.get(userData);
                        PHI0=documentSnapshot.getDouble("PHI0");
                        PHI1=documentSnapshot.getDouble("PHI1");
                        PHI2=documentSnapshot.getDouble("PHI2");
                        PHI3=documentSnapshot.getDouble("PHI3");
                        PHI4=documentSnapshot.getDouble("PHI4");
                        PHI5=documentSnapshot.getDouble("PHI5");
                        PHI6=documentSnapshot.getDouble("PHI6");



                        switch (currentDay) {
                            case Calendar.SUNDAY:
                                PHI0=finalPhi;
                                break;
                            case Calendar.MONDAY:
                                PHI1=  finalPhi;
                                break;
                            case Calendar.TUESDAY:
                                PHI2= finalPhi;
                                break;
                            case Calendar.WEDNESDAY:
                                PHI3=  finalPhi;
                                break;
                            case Calendar.THURSDAY:
                                PHI4=  finalPhi;
                                break;
                            case Calendar.FRIDAY:
                                PHI5=  finalPhi;
                                break;
                            case Calendar.SATURDAY:
                                PHI6=  finalPhi;
                                break;
                        }

                        Log.d(TAG,"PHI0-"+Double.toString(PHI0));
                        sumPHI=  (PHI0+PHI1+PHI2+PHI3+PHI3+PHI4+PHI5+PHI6);
                        Log.d(TAG,"sumPHI-"+Double.toString(Math.round(sumPHI)));




                        transaction.update(userData, "phi",(int) Math.round(sumPHI), "PHI0", PHI0, "PHI1", PHI1, "PHI2", PHI2, "PHI3", PHI3, "PHI4", PHI4, "PHI5", PHI5, "PHI6", PHI6);








                        return null;
                    }
                }).addOnSuccessListener(new OnSuccessListener<Double>() {
                    @Override
                    public void onSuccess(Double result) {
                        db.terminate();
                        Log.d(TAG, "Transaction success: " + result);
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Transaction failure.", e);
                            }
                        });


            }
        });
    }
    private int returnHourToMinutes(int minutes) {
        return minutes / 60;
    }

    private int returnMinutesToRemains(int minutes) {
        return minutes % 60;
    }

    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "Incoming call from";

        ContentResolver contentResolver = getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, null, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));

            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
}