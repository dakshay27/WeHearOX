package com.wehear.ox;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;

public class TranslateTestActivity2 extends AppCompatActivity implements RecognitionListener {
    ImageView imgInfo;
    private static final String TAG = "TranslateTestActivity2";
    int hasDataPermission;
    private static final int REQUEST_CODE_WRITE_DATA = 2;
    String apikey = "AIzaSyAXXBifGRhRLnYq72SfgHYcCPWpCTNV_lM";
    private static final String TAG_MEDIA = "YourBroadcastReceiver";
    String message = "";

    CardView button;
    static int flag;
    ImageView imgSpeakes;
    static String ILC = "en";
    String OLC = "hi";
    String ILCT = "en";
    String OLCT = "hi";
    boolean isOffline;
    static Intent speechIntent;

    static SpeechRecognizer speech;

    TextToSpeech tts;

    TextView outText, tvTransaltorLanguages, tvPressButtonSpeak;
    static int res;
    private static AudioManager manager;
    private static ComponentName componentName;
    static boolean useHeadset;
    static BluetoothAdapter mBluetoothAdapter;
    static BluetoothHeadset mBluetoothHeadset;
    static BluetoothProfile.ServiceListener mProfileListener;
    static boolean isBluetoothConnected;
    static List<BluetoothDevice> devices;
    static ArrayList<BluetoothDevice> arrayList;
    static boolean connected;
    String finalFirstLanguage, finalSecondLanguage;
    ImageView imgBack;
    String previewMessage = "Translated text output here.";
    String previewCurrentLanguage = "en";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_test2);
        checkSelfPermission();
        imgInfo = findViewById(R.id.img_hand_info);
        imgBack = findViewById(R.id.backbtn6);
        tvPressButtonSpeak = findViewById(R.id.tv_press_button_speak);
        tvTransaltorLanguages = findViewById(R.id.tv_translator_languages);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        imgSpeakes = findViewById(R.id.img_speaks_one_more);
        Intent intent = getIntent();
        ILC = intent.getStringExtra("ILC");
        ILCT = intent.getStringExtra("ILCT");
        OLC = intent.getStringExtra("OLC");
        OLCT = intent.getStringExtra("OLCT");
        finalFirstLanguage = intent.getStringExtra("finalFirstLanguage");
        finalSecondLanguage = intent.getStringExtra("finalSecondLanguage");
        isOffline = intent.getBooleanExtra("isOffline", false);
        if (!checkInternetConnection(getApplicationContext()) && (!isOffline)) {
            Toast.makeText(this, "Please Interner On", Toast.LENGTH_SHORT).show();
            finish();
        }
        tvTransaltorLanguages.setText("Translation of " + finalFirstLanguage + " to " + finalSecondLanguage);
        previewMessageTranslate();

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);

        Log.d(TAG, ILC + "/" + ILCT + "/" + OLC + "/" + OLCT);

        outText = findViewById(R.id.outText);


        button = findViewById(R.id.mic_button_test);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, TranslateTestActivity2.this.getPackageName());
        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        componentName = new ComponentName(this, ButtonCheck.class);
        manager.registerMediaButtonEventReceiver(componentName);

//
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM);
        at.play();

        // a little sleep
        at.stop();
        at.release();
        if (checkInternetConnection(getApplicationContext())) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    flag = 101;
                    speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, OLC);
                    res = 0;
                    speech.startListening(speechIntent);
                    Toast.makeText(TranslateTestActivity2.this, "Listening...", Toast.LENGTH_LONG).show();
                    final Animation myAnim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein_fadeout);
                    button.startAnimation(myAnim2);
                }
            });
        }

        imgSpeakes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!message.equals("")) {
                    manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    manager.setBluetoothScoOn(false);
                    manager.setSpeakerphoneOn(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tts = new TextToSpeech(getApplicationContext(),
                                    new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    //Log.e("Languages",tts.getAvailableLanguages().toString());
                                    tts.setLanguage(Locale.forLanguageTag(OLCT));
                                    tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            });
                        }
                    }, 1000);
                } else {

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
        Toast.makeText(this, "Some Error!!\nPlease speak again", Toast.LENGTH_SHORT).show();
        button.clearAnimation();

    }

    @Override
    public void onResults(Bundle results) {
        if (res == 0) {
            res = 1;
            if (mBluetoothHeadset != null && mBluetoothAdapter != null) {
                boolean b = mBluetoothHeadset.stopVoiceRecognition(arrayList.get(0));
                mBluetoothAdapter = null;
                mBluetoothHeadset = null;
                Log.e("onresult", "Stopped blt voice recog");
                Log.e("stopped", String.valueOf(b));
            }
            if (flag == 101) {
                ArrayList<String> output = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d(TAG, output.toString());
                String query = output.get(0);
                message = query;
                outText.setText(query);
                button.clearAnimation();
                if (isOffline) {
                    TranslatorOptions translatorOptions = new TranslatorOptions.Builder()
                                .setSourceLanguage(OLCT)
                            .setTargetLanguage(ILCT)
                            .build();
                    final Translator sourceToTarget = Translation.getClient(translatorOptions);

                    sourceToTarget.translate(query).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(final String s) {
                            Log.e("Translated Text", s);
                            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                @Override
                                public void onInit(int status) {
                                    Log.e(TAG, tts.getAvailableLanguages().toString());
                                    tts.setLanguage(Locale.forLanguageTag(ILCT));
                                    manager.setMode(AudioManager.MODE_NORMAL);
                                    manager.setBluetoothScoOn(true);
                                    manager.setSpeakerphoneOn(false);
                                    tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(TranslateTestActivity2.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String url = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + query + "&target=" + ILCT + "&source=" + OLCT + "&format=text";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e("Response: ", response.toString());
                                    try {
                                        final String translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                        Log.e(TAG, translatedString);

                                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                            @Override
                                            public void onInit(int status) {
                                                Log.e(TAG, tts.getAvailableLanguages().toString());
                                                tts.setLanguage(Locale.forLanguageTag(ILCT));
                                                manager.setMode(AudioManager.MODE_NORMAL);
                                                manager.setBluetoothScoOn(true);
                                                manager.setSpeakerphoneOn(false);
                                                tts.speak(translatedString, TextToSpeech.QUEUE_FLUSH, null);
                                            }
                                        });

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO: Handle error

                                }
                            });
                    MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
                }
            }
            if (flag == 100) {
                ArrayList<String> output = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                Log.d(TAG, output.toString());
                String query = output.get(0);
                button.clearAnimation();
                if (isOffline) {
                    TranslatorOptions translatorOptions = new TranslatorOptions.Builder()
                            .setSourceLanguage(ILCT)
                            .setTargetLanguage(OLCT)
                            .build();
                    final Translator sourceToTarget = Translation.getClient(translatorOptions);
                    sourceToTarget.translate(query).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(final String s) {
                            Log.e("Translated Text", s);
                            outText.setText("\n" + s);
                            message = s;
                            manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                            manager.setBluetoothScoOn(false);
                            manager.setSpeakerphoneOn(true);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        @Override
                                        public void onInit(int status) {
                                            //Log.e("Languages",tts.getAvailableLanguages().toString());
                                            tts.setLanguage(Locale.forLanguageTag(OLCT));
                                            tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                                        }
                                    });
                                }
                            }, 1000);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(TranslateTestActivity2.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    String url = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + query + "&target=" + OLCT + "&source=" + ILCT + "&format=text";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e("Response: ", response.toString());
                                    try {
                                        final String translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                        Log.e("Translated Text", translatedString);
                                        outText.setText(translatedString);
                                        message = translatedString;
                                        manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                                        manager.setBluetoothScoOn(false);
                                        manager.setSpeakerphoneOn(true);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                                    @Override
                                                    public void onInit(int status) {
                                                        //Log.e("Languages",tts.getAvailableLanguages().toString());
                                                        tts.setLanguage(Locale.forLanguageTag(OLCT));
                                                        tts.speak(translatedString, TextToSpeech.QUEUE_FLUSH, null);
                                                    }
                                                });
                                            }
                                        }, 1000);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO: Handle error

                                }
                            });

                    MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
                }
            }
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public static void usingHeadset(final Context context) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mProfileListener != null) {
                Log.e("Message", "Bluetooth Headset profile was already opened, let's close it on found");
                mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            }

            mProfileListener = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        Log.e("Message", "Found paired Bluetooth Headset");
                        //Toast.makeText(MainActivity.this, "Found paired Bluetooth Headset", Toast.LENGTH_SHORT).show();
                        mBluetoothHeadset = (BluetoothHeadset) proxy;
                        devices = mBluetoothHeadset.getConnectedDevices();
                        arrayList = new ArrayList<>(devices);
                        if (arrayList.size() > 0) {
                            isBluetoothConnected = true;
                            Log.e("Message", "Found connected Bluetooth Headset");
                            //Toast.makeText(MainActivity.this, "Found connected Bluetooth Headset", Toast.LENGTH_SHORT).show();
                            Log.e("devices", arrayList.get(0).getName());
                            //Toast.makeText(MainActivity.this, arrayList.get(0).getName(), Toast.LENGTH_SHORT).show();
                            boolean b = mBluetoothHeadset.startVoiceRecognition(arrayList.get(0));
                            Log.e("voice recog", String.valueOf(b));
                            if (b) {
                                Log.e("message", "Listening...");
                                Toast.makeText(context, "Listening...", Toast.LENGTH_LONG).show();
                                res = 0;
                                speech.startListening(speechIntent);
                            }

                        }

                    }
                }

                public void onServiceDisconnected(int profile) {
                    Log.e("FUNCTION", "onserviceDisconnected");
                    if (profile == BluetoothProfile.HEADSET) {
                        speech.stopListening();
                        mBluetoothHeadset = null;
                        isBluetoothConnected = false;
                        Log.e("Message", "No paired Bluetooth Headset");

                    }
                }
            };
            boolean success = mBluetoothAdapter.getProfileProxy(context, mProfileListener, BluetoothProfile.HEADSET);
            if (!success) {
                Log.e("message", "[Bluetooth] getProfileProxy failed !");
            }
        } else {
            Log.e("Message", "Bluetooth disabled on device");
            Toast.makeText(context, "Bluetooth  disabled on device", Toast.LENGTH_SHORT).show();
        }

    }

    public static boolean checkInternetConnection(Context context) {

        //Check internet connection:
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Means that we are connected to a network (mobile or wi-fi)
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
    }

    public static class ButtonCheck extends BroadcastReceiver {


        public ButtonCheck() {
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Here");
            String intentAction = intent.getAction();
            if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                    return;
                switch (keyEvent.getKeyCode()) {

                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        if (checkInternetConnection(context)) {
                            flag = 100;
                            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ILC);
                            if (useHeadset) {
                                usingHeadset(context);
                            } else {
                                res = 0;
                                speech.startListening(speechIntent);
                                Toast.makeText(context, "Listening...", Toast.LENGTH_LONG).show();

                            }
                        } else {
                            Toast.makeText(context, "INTERNET UNAVAILABLE", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }

            if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                Log.i(TAG, "no media button information");
                Toast.makeText(context, "no media button information", Toast.LENGTH_SHORT).show();
                return;
            }
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                Log.i(TAG, "No key press");
                Toast.makeText(context, "No key press", Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        manager.unregisterMediaButtonEventReceiver(componentName);
        manager.setMode(AudioManager.MODE_NORMAL);
        speech.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterMediaButtonEventReceiver(componentName);
        manager.setMode(AudioManager.MODE_NORMAL);
        speech.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.setMode(AudioManager.MODE_NORMAL);
    }

    private void previewMessageTranslate() {
        final Dialog dialog = new Dialog(TranslateTestActivity2.this);
        dialog.setContentView(R.layout.custom_translate_test_2_info_dialog);
        dialog.setTitle("Custom Dialog");
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        final TextView txtIntro = dialog.findViewById(R.id.txt_text_info_custom_dialog);
        final Button btnStart = dialog.findViewById(R.id.btn_start);
        if (isOffline) {

            TranslatorOptions translatorOptions = new TranslatorOptions.Builder()
                    .setSourceLanguage(previewCurrentLanguage)
                    .setTargetLanguage(OLCT)
                    .build();
            final Translator sourceToTarget = Translation.getClient(translatorOptions);
            sourceToTarget.translate(previewMessage).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    outText.setText(s);
                }
            });
            sourceToTarget.translate(tvTransaltorLanguages.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    tvTransaltorLanguages.setText(s);
                }
            });
            sourceToTarget.translate(tvPressButtonSpeak.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    tvPressButtonSpeak.setText(s);
                }
            });
            sourceToTarget.translate(txtIntro.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    txtIntro.setText(s);
                    sourceToTarget.translate(btnStart.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            btnStart.setText(s);
                            dialog.show();
                        }
                    });
                }
            });

        } else {
            final String[] url = {"https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + previewMessage + "&target=" + OLCT + "&source=" + previewCurrentLanguage + "&format=text"};
            final JsonObjectRequest[] jsonObjectRequest = {new JsonObjectRequest
                    (Request.Method.POST, url[0], null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response: ", response.toString());
                            try {
                                final String translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                outText.setText(translatedString);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error

                        }
                    })};
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest[0]);
            url[0] = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + tvTransaltorLanguages.getText().toString() + "&target=" + OLCT + "&source=" + previewCurrentLanguage + "&format=text";
            jsonObjectRequest[0] = new JsonObjectRequest
                    (Request.Method.POST, url[0], null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response: ", response.toString());
                            try {
                                final String translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                tvTransaltorLanguages.setText(translatedString);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error

                        }
                    });
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest[0]);
            url[0] = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + tvPressButtonSpeak.getText().toString() + "&target=" + OLCT + "&source=" + previewCurrentLanguage + "&format=text";
            jsonObjectRequest[0] = new JsonObjectRequest
                    (Request.Method.POST, url[0], null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response: ", response.toString());
                            try {
                                final String translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                tvPressButtonSpeak.setText(translatedString);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error

                        }
                    });
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest[0]);
            url[0] = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + txtIntro.getText().toString() + "&target=" + OLCT + "&source=" + previewCurrentLanguage + "&format=text";
            jsonObjectRequest[0] = new JsonObjectRequest
                    (Request.Method.POST, url[0], null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e("Response: ", response.toString());
                            try {
                                final String translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                txtIntro.setText(translatedString);
                                url[0] = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + btnStart.getText().toString() + "&target=" + OLCT + "&source=" + previewCurrentLanguage + "&format=text";
                                jsonObjectRequest[0] = new JsonObjectRequest
                                        (Request.Method.POST, url[0], null, new Response.Listener<JSONObject>() {
                                            @Override
                                            public void onResponse(JSONObject response) {
                                                Log.e("Response: ", response.toString());
                                                try {
                                                    final String translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                                    btnStart.setText(translatedString);
                                                    dialog.show();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                // TODO: Handle error

                                            }
                                        });
                                MySingleton.getInstance(TranslateTestActivity2.this).addToRequestQueue(jsonObjectRequest[0]);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error

                        }
                    });
            MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest[0]);

        }
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        imgInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CODE_WRITE_DATA:
                if(grantResults.length > 0){
                    boolean writeExternal = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAudio = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean bluetooth = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean audioSetting = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if(writeExternal && recordAudio && bluetooth && audioSetting){
                         
                    }else{
                        Toast.makeText(TranslateTestActivity2.this, "Please accepet all condition..", Toast.LENGTH_SHORT).show();
                        checkSelfPermission();
                    }
                }
                break;
        }
    }
    private void checkSelfPermission(){
        hasDataPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(this, BLUETOOTH_ADMIN)
                + ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, MODIFY_AUDIO_SETTINGS);

        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, BLUETOOTH_ADMIN}, REQUEST_CODE_WRITE_DATA);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, BLUETOOTH_ADMIN}, REQUEST_CODE_WRITE_DATA);
            }
        }

    }
}
