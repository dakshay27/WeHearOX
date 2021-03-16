package com.wehear.ox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.wehear.ox.TimerCountDownActivity.timeLeftInMilliseconds;

public class TimerService extends Service {
    public static final String CHANNEL_ID = "timerServiceChannel";
    private static final String TAG = "TimerService";
    public static final String CANCEL = "CANCEL";
    static long total, timeLeft = 0;
    NotificationManager mNotificationManager;
    Timer timer;
    NotificationCompat.Builder mBuilder;
    NotificationChannel serviceChannel;
    String time;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        total = timeLeftInMilliseconds + System.currentTimeMillis();
        Log.d(TAG, "onStartCommand: " + timeLeftInMilliseconds);
        Log.d(TAG, "onStartCommand: " + System.currentTimeMillis());
        Log.d(TAG, "onStartCommand: " + total);

        //Date currentTime = Calendar.getInstance().getTime();
        String i = intent.getStringExtra("inputExtra");
        Intent notificationIntent = new Intent(this, TimerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Log.d(TAG, "onCreate: Service Created");
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
        Date resultdate = new Date(total);
        Log.d(TAG, "onStartCommand: " + sdf.format(resultdate));
//        System.out.println(sdf.format(resultdate));
//        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.ic_ox_icon);
        mBuilder.setContentTitle("Stop Timer");
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setContentText("Your device will be disconnected at " + sdf.format(resultdate));
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);

        Intent nintent = new Intent(getBaseContext(), NotificationActionReceiver.class);
        nintent.setAction(CANCEL);
        nintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 1, nintent, 0);
        mBuilder.addAction(R.drawable.ic_close_black_24dp, "Cancel", pIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
            Log.d(TAG, "createNotificationChannel: Service Notification Channel Created" + serviceChannel);
        }

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

        startForeground(1, mBuilder.build());

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() < total) {
                    timeLeft = total - System.currentTimeMillis();
                    time = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(timeLeft),
                            TimeUnit.MILLISECONDS.toMinutes(timeLeft) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeLeft)), // The change is in this line
                            TimeUnit.MILLISECONDS.toSeconds(timeLeft) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeLeft)));
                    mBuilder.setContentTitle("Time Left : " + time);
                    mNotificationManager.notify(1, mBuilder.build());
                    Log.d(TAG, "run: " + time);
                    Log.d(TAG, "run: " + System.currentTimeMillis() + "  " + total);
                }
            }
        }, 0, 1000);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNotificationManager.cancel(1);
        timer.cancel();
        Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
    }
}
