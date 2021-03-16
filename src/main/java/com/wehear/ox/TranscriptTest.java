package com.wehear.ox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class TranscriptTest extends AppCompatActivity implements RecognitionListener {
    CardView button1;
    CardView button2;
    Intent speechIntent;
    SpeechRecognizer speech;
    TextToSpeech tts;
    TextView outText;
    String apikey="AIzaSyAXXBifGRhRLnYq72SfgHYcCPWpCTNV_lM";
    String TAG = "Transcript Test";
    String outputString="";

    static int speakerId=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transcript_test);

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        outText = findViewById(R.id.outText);

        button1 = findViewById(R.id.mic_button_test1);
        button2 = findViewById(R.id.mic_button_test2);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,TranscriptTest.this.getPackageName());
                button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(TranscriptTest.this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED)
                {
                    speakerId=1;
                    speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
                    speech.startListening(speechIntent);

                    Toast.makeText(TranscriptTest.this, "Listening...", Toast.LENGTH_LONG).show();

                }
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(TranscriptTest.this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED)
                {
                    speakerId=2;
                    speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
                    speech.startListening(speechIntent);

                    Toast.makeText(TranscriptTest.this, "Listening...", Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> output = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Log.d(TAG,output.toString());
        Date currentTime = Calendar.getInstance().getTime();

        if(speakerId==1)
        {
            outputString+="\n Speaker 1: "+ output.get(0);
        }
        else
        {
            outputString+="\n Speaker 2:"+output.get(0);
        }


       outText.setText(outputString);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
