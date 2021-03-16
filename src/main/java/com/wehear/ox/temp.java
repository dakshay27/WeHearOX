package com.wehear.ox;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MicrophoneInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class temp extends AppCompatActivity {
    AudioManager mAudioManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    BluetoothHeadset mBluetoothHeadset;
    BluetoothProfile.ServiceListener mProfileListener;
    boolean isBluetoothConnected;
    List<BluetoothDevice> devices;
    ArrayList<BluetoothDevice> arrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},99);
        }
        /*
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        try {
            List<MicrophoneInfo> l=am.getMicrophones();
            ArrayList<MicrophoneInfo> m=new ArrayList<>(l);
            for (int i=0;i<m.size();i++)
            Toast.makeText(this,String.valueOf(m.get(i).getType()), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "No Microphone", Toast.LENGTH_SHORT).show();
        }


        Log.e("INFO", "starting bluetooth");
        am.startBluetoothSco();

        registerReceiver(new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.e("INFO", "Audio SCO state: " + state);
                Log.e("INFO", "Audio SCO state req: " + AudioManager.SCO_AUDIO_STATE_CONNECTED);

                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {

         */
        /*
         * Now the connection has been established to the bluetooth device.
         * Record audio or whatever (on another thread).With AudioRecord you can          record   with an object created like this:
         * new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
         * AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
         *
         * After finishing, don't forget to unregister this receiver and
         * to stop the bluetooth connection with am.stopBluetoothSco();
         */
                    /*
                    Toast.makeText(context, "CONNECTED", Toast.LENGTH_SHORT).show();

                    unregisterReceiver(this);
                }

            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
        */


//        pairedDevices = btAdapter.getBondedDevices();
        mAudioManager=(AudioManager) getSystemService(AUDIO_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mProfileListener != null) {
                Log.e("Message","Bluetooth Headset profile was already opened, let's close it");
                mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            }

            mProfileListener = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        Log.e("Message","Found paired Bluetooth Headset");
                        Toast.makeText(temp.this, "Found paired Bluetooth Headset", Toast.LENGTH_SHORT).show();
                        mBluetoothHeadset = (BluetoothHeadset) proxy;
                        devices = mBluetoothHeadset.getConnectedDevices();
                        arrayList=new ArrayList<>(devices);
                        if(arrayList.size()>0)
                        {
                            isBluetoothConnected = true;
                            Log.e("Message","Found connected Bluetooth Headset");
                            Toast.makeText(temp.this, "Found connected Bluetooth Headset", Toast.LENGTH_SHORT).show();
                            Log.e("devices",arrayList.get(0).getName());
                            Toast.makeText(temp.this, arrayList.get(0).getName(), Toast.LENGTH_SHORT).show();
                            boolean b =mBluetoothHeadset.startVoiceRecognition(arrayList.get(0));
                            Log.e("voice recog",String.valueOf(b));
                        }
                        start();
                    }
                }
                public void onServiceDisconnected(int profile) {
                    Log.e("FUNCTION","onserviceDisconnected");
                    if (profile == BluetoothProfile.HEADSET) {
                        mBluetoothHeadset = null;
                        isBluetoothConnected = false;
                        Log.e("Message","No paired Bluetooth Headset");

                    }
                }
            };
            boolean success = mBluetoothAdapter.getProfileProxy(getApplicationContext(), mProfileListener, BluetoothProfile.HEADSET);
            if (!success) {
                Log.e("message","[Bluetooth] getProfileProxy failed !");
            }
        } else {
            Log.e("Message","Bluetooth disabled on device");
            Toast.makeText(this, "Bluetooth  disabled on device", Toast.LENGTH_SHORT).show();
        }


        Log.e("SEARCHING CATEGORy", String.valueOf(BluetoothProfile.HEADSET));

    }
    public  void start(){

        final Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,temp.this.getPackageName());
        SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.e("SPEECH","onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {

                Log.e("SPEECH","onBeginningOfSpeech");
            }

            @Override
            public void onRmsChanged(float rmsdB) {

                Log.e("SPEECH","onRmsChanged");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {

                Log.e("SPEECH","onBufferReceived");
            }

            @Override
            public void onEndOfSpeech() {

                Log.e("SPEECH","onEndOfSpeech");
            }

            @Override
            public void onError(int error) {

                Log.e("SPEECH","onError");
                Log.e("speech error",String.valueOf(error));
            }

            @Override
            public void onResults(Bundle results) {

                Log.e("SPEECH","onResults");
                ArrayList<String> output = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Toast.makeText(temp.this, output.toString(), Toast.LENGTH_SHORT).show();
                mBluetoothHeadset.startVoiceRecognition(arrayList.get(0));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

                Log.e("SPEECH","onPartialResults");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

                Log.e("SPEECH","onEvent");
            }
        });
        if (isBluetoothConnected)
        {
            speech.startListening(speechIntent);
            Toast.makeText(this, "Listening...", Toast.LENGTH_LONG).show();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==99){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "RECORD AUDIO PERMISSION GRANTED", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "RECORD AUDIO PERMISSION DENIED, RE-REQUESTING PERMISSION", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},99);
            }
        }
    }
}