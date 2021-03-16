package com.wehear.ox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class HeadsetActionButtonReceiver extends BroadcastReceiver {


    private static final String TAG = "HeadsetActionButtonRece";
    public HeadsetActionButtonReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"Here");
        String intentAction = intent.getAction();
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    Toast.makeText(context, intentAction.toString() + " Play_pause", Toast.LENGTH_SHORT).show();

                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    Toast.makeText(context, intentAction.toString() + " Play", Toast.LENGTH_SHORT).show();

                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    Toast.makeText(context, intentAction.toString() + " Pause", Toast.LENGTH_SHORT).show();

                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    Toast.makeText(context, intentAction.toString() + " Stop", Toast.LENGTH_SHORT).show();

                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    Toast.makeText(context, intentAction.toString() + " Next", Toast.LENGTH_SHORT).show();

                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    Toast.makeText(context, intentAction.toString() + " previous", Toast.LENGTH_SHORT).show();

                    break;
            }
        }

        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            Log.i(TAG,"no media button information");
            Toast.makeText(context, "no media button information", Toast.LENGTH_SHORT).show();
            return;
        }
        KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            Log.i(TAG,"No key press");
            Toast.makeText(context, "No key press", Toast.LENGTH_SHORT).show();
            return;
        }
    }


}

