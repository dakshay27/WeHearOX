package com.wehear.ox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class HearingAidService extends Service {

    public static final String CHANNEL_ID = "hearingAidChannel";
    private static final String TAG = "HearingAidService";
    public static Runnable runnable = null;
    public static final String CANCELL = "STOP";


    public Context context = this;


    public HearingAidService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();

        Intent notificationIntent = new Intent(this, HearingAidActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);


        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.ic_ox_icon);

        mBuilder.setContentTitle("Hearing Aid Mode is ON");
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setContentText("Your device is in hearing aid mode currently");
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);
        Intent hintent = new Intent(getBaseContext(),NotificationActionReceiver.class);
        hintent.setAction(CANCELL);
        hintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getBroadcast(this, 22, hintent, 0);
        mBuilder.addAction(R.drawable.ic_close_black_24dp,"STOP",pIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(serviceChannel);
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
        mNotificationManager.notify(22, mBuilder.build());

        startForeground(22, mBuilder.build());

        (new Thread() {
            @Override
            public void run() {

                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                HearingAidActivity.startAudio();
            }
        }).start();


//        runnable = new Runnable() {
//            public void run() {
//                HearingAidActivity.startAudio();
//                Toast.makeText(context,"SERVICE RUNNING", Toast.LENGTH_LONG).show();
//            }
//        };


        return START_STICKY;
    }


    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Created");
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        /* IF YOU WANT THIS SERVICE KILLED WITH THE APP THEN UNCOMMENT THE FOLLOWING LINE */
        //handler.removeCallbacks(runnable);
        HearingAidActivity.endAudio();
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Hearing aid Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}