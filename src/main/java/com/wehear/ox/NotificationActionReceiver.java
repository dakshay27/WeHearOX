package com.wehear.ox;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import static com.wehear.ox.TimerService.CANCEL;
import static com.wehear.ox.HearingAidService.CANCELL;
import static com.wehear.ox.TimerService.timeLeft;

public class NotificationActionReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationActionRecei";
    public static final String CHANNEL_ID = "timerServiceChannel";
    public static final String CHANNEL_ID2 = "hearingAidChannel";


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if(CANCEL.equals(action)) {
            Log.d(TAG, "onReceive: ");
            TimerCountDownActivity.stopTimer();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.deleteNotificationChannel(CHANNEL_ID);

            Intent service = new Intent(context, TimerService.class);
            context.stopService(service);

//            String ns = Context.NOTIFICATION_SERVICE;

//            NotificationManager nMgr = (NotificationManager) context.getSystemService(ns);
//            nMgr.cancel(1);
        }

        if (CANCELL.equals(action)) {
            HearingAidActivity.endAudio();

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.deleteNotificationChannel(CHANNEL_ID2);

            Intent service2 = new Intent(context, HearingAidService.class);
            context.stopService(service2);
        }
    }
}
