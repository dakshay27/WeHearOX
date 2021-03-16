package com.wehear.ox;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Locale;

public class PhoneCallReceiver extends BroadcastReceiver {
    static TextToSpeech tts;
    Context context = null;
    String message="";
    private static final String TAG = "Phone call";
    private ITelephony telephonyService;


    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        try {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Toast.makeText(context, "Phone is Ringing:" + incomingNumber + "Name:" + getContactDisplayNameByNumber(incomingNumber), Toast.LENGTH_SHORT).show();
                message="Incoming call from " + getContactDisplayNameByNumber(incomingNumber);
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

            }

        } catch (Exception e) {
            Toast.makeText(context, "CalledEx"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            Log.d("ErrorSome",e.getLocalizedMessage());
        }
    }

    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "Incoming call from";

        ContentResolver contentResolver = context.getContentResolver();
        Cursor contactLookup = contentResolver.query(uri, null, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                // this.id =
                // contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                // String contactId =
                // contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        return name;
    }

    public static class Receiver extends Activity {
        private static AudioManager manager;
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            Bundle extras = getIntent().getExtras();
            tts = new TextToSpeech(this,ttsInitListener);
            Receiver.speakSMS(extras.getString("Message"));
            finish();

        }
        private TextToSpeech.OnInitListener ttsInitListener=new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int version) {


            }
        };
        public static void speakSMS(String sms)
        {
            tts.setLanguage(Locale.US);
            manager.setMode(AudioManager.MODE_NORMAL);
            manager.setBluetoothScoOn(true);
            manager.setSpeakerphoneOn(false);
            Log.d("ErrorSome",sms);
            tts.speak(sms, TextToSpeech.QUEUE_FLUSH, null);
        }

    }
}
