package com.wehear.ox;

import android.annotation.SuppressLint;
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
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;

//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;
//import com.google.cloud.translate.Translation;

public class ConversationMode extends AppCompatActivity implements AdapterView.OnItemSelectedListener, RecognitionListener {
    int hasDataPermission;
    private static final int REQUEST_CODE_WRITE_DATA = 2;
    static ComponentName mReceiverComponent;
    private static final String TAG = "ConversationMode";
    private AudioManager audioManager;
    private Spinner inputspinner;
    private ArrayAdapter inputaa;
    private String[] inputLanguages;
    private Spinner outputspinner;
    private ArrayAdapter outputaa;
    private ImageView backbtn5;
    private String[] outputLanguages;
    private TextView oxtv, intv, outtv;
    ImageView micButton;
    private static ImageView micButton2;
    String[] inputLanguageCodes;
    String[] outputLanguageCodes;
    String[] inputLanguageCodesTrans;
    String[] outputLanguageCodesTrans;
    static String ILC = "en"
            ;
    static String OLC = "hi-IN";
    String ILCT = "en";
    String OLCT = "hi-IN";
    HashMap<String, String> hashMapInput;
    HashMap<String, String> hashMapOutput;
    HashMap<String, String> hashMapInputTrans;
    HashMap<String, String> hashMapOutputTrans;
    private ArrayList<String> supportedLanguages;
    private String languagePreference;
    TextView outText;
    TextView inText;
    static boolean connected;
    //Translate translate;
    TextToSpeech tts;
    static int flag;
    static SpeechRecognizer speech;
    //Switch aSwitch;
    static boolean useHeadset;
    static BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    static BluetoothHeadset mBluetoothHeadset;
    static BluetoothProfile.ServiceListener mProfileListener;
    static boolean isBluetoothConnected;
    static List<BluetoothDevice> devices;
    static ArrayList<BluetoothDevice> arrayList;
    static Intent speechIntent;
    static int res;
    String translatedString;
    String apikey = "AIzaSyAXXBifGRhRLnYq72SfgHYcCPWpCTNV_lM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_conversation_mode);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();

        findViewById();
        useHeadset = false;
        checkSelfPermission();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReceiverComponent = new ComponentName(this, CatchEvent.class);
        audioManager.registerMediaButtonEventReceiver(mReceiverComponent);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM);
        at.play();

        at.stop();
        at.release();
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e("Languages", tts.getAvailableLanguages().toString());
                Log.e("NUM ANGUAGES", String.valueOf(tts.getAvailableLanguages().size()));
            }
        });

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);


        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);


        backbtn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //aSwitch = findViewById(R.id.switch1);

        /*
        inputLanguages= new String[]{"English", "Hindi","French","Malyalam","Afrikaans","Azerbaijan","Indonesian",
                "Malay","Javnese","Sundanese","Catalan","Czech","Danish","German","Estonian","Spanish","Filipino",
                "Galician","Croatian","Zulu","Icelandic","Swahili","Latvian","Lithuanian","Hungarian","Dutch","Norwegian",
                "Uzbek","Polish","Portuguese","Romanian","Slovenian","Slovak","Finnish","Swedish","Vietnamese","Turkish",
                "Greek","Bulgarian","Russian","Serbian","Ukrainian","Georgian","Armenian","Hebrew","Arabic","Farsi","Urdu","Amharic","Tamil",
                "Bengali","Kambodia","Kannada","Marathi","Gujarati","Sinhala","Telugu","Nepali","Lao","Thai","Burmese","Korean",
                "Chinese","Chinese(Hong Kong)","Japanese","Chinese"};
        inputLanguageCodes= new String[]{"en","hi","fr","ml","af","az","id","ms","jv","su","ca","cz","da","de","et",
                "es","fil","gl","hr","zu","is","sw","lv","lt","hu","nl","nb","uz","pl","pt","ro","sl","sk","fi","sv","vi",
                "tr","el","bg","ru","sr","uk","ka","hy","he","ar","fa","ur","am","ta","bn","km","kn","mr","gu","si",
                "te","ne","lo","th","my","ko","cmn","yue","ja","zh"};
*/

        hashMapInput = new HashMap<>();
        hashMapInputTrans = new HashMap<>();
        inputLanguages = new String[]{"English", "Hindi", "French", "Malyalam", "Afrikaans", "Azerbaijan", "Indonesian",
                "Malay", "Javnese", "Sundanese", "Catalan", "Czech", "Danish", "German", "Estonian", "Spanish", "Basque", "Filipino",
                "Galician", "Croatian", "Zulu", "Icelandic", "Italian", "Swahili", "Latvian", "Lithuanian", "Hungarian", "Dutch", "Norwegian",
                "Uzbek", "Polish", "Portuguese", "Romanian", "Slovenian", "Slovak", "Finnish", "Swedish", "Vietnamese", "Turkish",
                "Greek", "Bulgarian", "Russian", "Serbian", "Ukrainian", "Georgian", "Armenian", "Hebrew", "Arabic", "Farsi", "Urdu", "Amharic", "Tamil",
                "Bengali", "Kambodia", "Kannada", "Marathi", "Gujarati", "Sinhala", "Telugu", "Nepali", "Lao", "Thai", "Burmese", "Korean", "Japanese", "Chinese"};
        inputLanguageCodes = new String[]{"en", "hi-IN", "fr-FR", "ml-IN", "af-ZA", "az-AZ", "id-ID", "ms-MY", "jv-ID", "su-ID", "ca-ES", "cs-CZ", "da-DK", "de", "et-EE",
                "es", "eu-ES", "fil-PH", "gl-ES", "hr-HR", "zu-ZA", "is-IS", "it-IT", "sw", "lv_LV", "lt-LT", "hu-HU", "nl-NL", "nb-NO", "uz-UZ", "pl-PL", "pt-PT", "ro-RO", "sl-SI", "sk-SK", "fi-FI", "sv-SE", "vi-VN",
                "tr-TR", "el-GR", "bg-BG", "ru-RU", "sr-RS", "uk-UA", "ka-GE", "hy-AM", "he-IL", "ar", "fa-IR", "ur", "am-ET", "ta", "bn", "km-KH", "kn-IN", "mr-IN", "gu-IN", "si-LK",
                "te-IN", "ne-NP", "lo-LA", "th-TH", "my-MM", "ko-KR", "ja-JP", "zh"};
        inputLanguageCodesTrans = new String[]{"en", "hi", "fr", "ml", "af", "az", "id", "ms", "jv", "su", "ca", "cs", "da", "de", "et",
                "es", "eu", "fil", "gl", "hr", "zu", "is", "it", "sw", "lv", "lt", "hu", "nl", "nb", "uz", "pl", "pt", "ro", "sl", "sk", "fi", "sv", "vi",
                "tr", "el", "bg", "ru", "sr", "uk", "ka", "hy", "he", "ar", "fa", "ur", "am", "ta", "bn", "km", "kn", "mr", "gu", "si",
                "te", "ne", "lo", "th", "my", "ko", "ja", "zh"};
        for (int i = 0; i < inputLanguages.length; i++) {
            hashMapInput.put(inputLanguages[i], inputLanguageCodes[i]);
            hashMapInputTrans.put(inputLanguages[i], inputLanguageCodesTrans[i]);
        }
        Arrays.sort(inputLanguages);

        inputaa = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, inputLanguages);
        inputaa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        inputspinner.setAdapter(inputaa);
        inputspinner.setOnItemSelectedListener(this);

/*
        outputLanguages= new String[]{"Hindi","English","French","Malyalam","Afrikaans","Azerbaijan","Indonesian",
                "Malay","Javnese","Sundanese","Catalan","Czech","Danish","German","Estonian","Spanish","Filipino",
                "Galician","Croatian","Zulu","Icelandic","Swahili","Latvian","Lithuanian","Hungarian","Dutch","Norwegian",
                "Uzbek","Polish","Portuguese","Romanian","Slovenian","Slovak","Finnish","Swedish","Vietnamese","Turkish",
                "Greek","Bulgarian","Russian","Serbian","Ukrainian","Georgian","Armenian","Hebrew","Arabic","Farsi","Urdu","Amharic","Tamil",
                "Bengali","Kambodia","Kannada","Marathi","Gujarati","Sinhala","Telugu","Nepali","Lao","Thai","Burmese","Korean",
                "Chinese","Chinese(Hong Kong)","Japanese","Chinese"};
        outputLanguageCodes= new String[]{"hi","en","fr","ml","af","az","id","ms","jv","su","ca","cz","da","de","et",
                "es","fil","gl","hr","zu","is","sw","lv","lt","hu","nl","nb","uz","pl","pt","ro","sl","sk","fi","sv","vi",
                "tr","el","bg","ru","sr","uk","ka","hy","he","ar","fa","ur","am","ta","bn","km","kn","mr","gu","si",
                "te","ne","lo","th","my","ko","cmn","yue","ja","zh"};
*/
        hashMapOutput = new HashMap<>();
        hashMapOutputTrans = new HashMap<>();
        outputLanguages = new String[]{"Hindi", "English", "French", "Malyalam", "Afrikaans", "Azerbaijan", "Indonesian",
                "Malay", "Javnese", "Sundanese", "Catalan", "Czech", "Danish", "German", "Estonian", "Spanish", "Basque", "Filipino",
                "Galician", "Croatian", "Zulu", "Icelandic", "Italian", "Swahili", "Latvian", "Lithuanian", "Hungarian", "Dutch", "Norwegian",
                "Uzbek", "Polish", "Portuguese", "Romanian", "Slovenian", "Slovak", "Finnish", "Swedish", "Vietnamese", "Turkish",
                "Greek", "Bulgarian", "Russian", "Serbian", "Ukrainian", "Georgian", "Armenian", "Hebrew", "Arabic", "Farsi", "Urdu", "Amharic", "Tamil",
                "Bengali", "Kambodia", "Kannada", "Marathi", "Gujarati", "Sinhala", "Telugu", "Nepali", "Lao", "Thai", "Burmese", "Korean", "Japanese", "Chinese"};
        outputLanguageCodes = new String[]{"hi-IN", "en", "fr-FR", "ml-IN", "af-ZA", "az-AZ", "id-ID", "ms-MY", "jv-ID", "su-ID", "ca-ES", "cs-CZ", "da-DK", "de", "et-EE",
                "es", "eu-ES", "fil-PH", "gl-ES", "hr-HR", "zu-ZA", "is-IS", "it-IT", "sw", "lv_LV", "lt-LT", "hu-HU", "nl-NL", "nb-NO", "uz-UZ", "pl-PL", "pt-PT", "ro-RO", "sl-SI", "sk-SK", "fi-FI", "sv-SE", "vi-VN",
                "tr-TR", "el-GR", "bg-BG", "ru-RU", "sr-RS", "uk-UA", "ka-GE", "hy-AM", "he-IL", "ar", "fa-IR", "ur", "am-ET", "ta", "bn", "km-KH", "kn-IN", "mr-IN", "gu-IN", "si-LK",
                "te-IN", "ne-NP", "lo-LA", "th-TH", "my-MM", "ko-KR", "ja-JP", "zh"};
        outputLanguageCodesTrans = new String[]{"hi", "en", "fr", "ml", "af", "az", "id", "ms", "jv", "su", "ca", "cs", "da", "de", "et",
                "es", "eu", "fil", "gl", "hr", "zu", "is", "it", "sw", "lv", "lt", "hu", "nl", "nb", "uz", "pl", "pt", "ro", "sl", "sk", "fi", "sv", "vi",
                "tr", "el", "bg", "ru", "sr", "uk", "ka", "hy", "he", "ar", "fa", "ur", "am", "ta", "bn", "km", "kn", "mr", "gu", "si",
                "te", "ne", "lo", "th", "my", "ko", "ja", "zh"};
        for (int i = 0; i < outputLanguages.length; i++) {
            hashMapOutput.put(outputLanguages[i], outputLanguageCodes[i]);
            hashMapOutputTrans.put(outputLanguages[i], outputLanguageCodesTrans[i]);
        }
        Arrays.sort(outputLanguages);

        outputaa = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, outputLanguages);
        outputaa.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        outputspinner.setAdapter(outputaa);
        outputspinner.setOnItemSelectedListener(this);

        if (checkInternetConnection(getApplicationContext())) {

            //If there is internet connection, get translate service and start translation:
            //getTranslateService();

            speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ILC);
            speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ConversationMode.this.getPackageName());

            micButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ILC.equals(OLC)) {
                        Toast.makeText(ConversationMode.this, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                    } else {
                        flag = 100;
                        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ILC);
                        if (useHeadset) {
                            usingHeadset(getApplicationContext());
                        } else {
                            res = 0;
                            speech.startListening(speechIntent);
                            Toast.makeText(ConversationMode.this, "Listening...", Toast.LENGTH_LONG).show();
                            final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein_fadeout);
                            micButton.startAnimation(myAnim);
                        }
                    }
                }
            });

            micButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ILC.equals(OLC)) {
                        Toast.makeText(ConversationMode.this, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                    } else {
                        flag = 101;
                        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, OLC);
                        if (useHeadset) {
                            usingHeadset(getApplicationContext());
                        } else {
                            res = 0;
                            speech.startListening(speechIntent);
                            Toast.makeText(ConversationMode.this, "Listening...", Toast.LENGTH_LONG).show();
                            final Animation myAnim2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein_fadeout);
                            micButton2.startAnimation(myAnim2);
                        }
                    }
                }
            });
        } else {
            Toast.makeText(this, "INTERNET UNAVAILABLE", Toast.LENGTH_SHORT).show();
        }
/*        useHeadset=false;
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    useHeadset=true;
                }
                else{
                    useHeadset=false;
                }
            }
        });*/
    }

    /*
    public void getTranslateService() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try (InputStream is = getResources().openRawResource(R.raw.credentials)) {

            //Get credentials:
            final GoogleCredentials myCredentials = GoogleCredentials.fromStream(is);

            //Set credentials and get translate service:
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(myCredentials).build();
            translate = translateOptions.getService();

        } catch (IOException ioe) {
            ioe.printStackTrace();

        }
    }
    */
    public static boolean checkInternetConnection(Context context) {

        //Check internet connection:
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Means that we are connected to a network (mobile or wi-fi)
        connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        return connected;
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

    public void convMode(View view) {
        Intent intent = new Intent(this, ConversationMode.class);
        startActivity(intent);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spn_spinner6) {
            String temp = hashMapInput.get(inputLanguages[position]);

            if (temp.equals(OLC)) {
                Toast.makeText(ConversationMode.this, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                ILC = hashMapInput.get(inputLanguages[position]);
                ILCT = hashMapInputTrans.get(inputLanguages[position]);
            } else {
                //Toast.makeText(ConversationMode.this, inputLanguages[position] + " input language", Toast.LENGTH_SHORT).show();
                ILC = hashMapInput.get(inputLanguages[position]);
                ILCT = hashMapInputTrans.get(inputLanguages[position]);
            }

        } else if (parent.getId() == R.id.spn_spinner7) {
            String temp = hashMapOutput.get(outputLanguages[position]);
            if (temp.equals(ILC)) {
                Toast.makeText(ConversationMode.this, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                OLC = hashMapOutput.get(outputLanguages[position]);
                OLCT = hashMapOutputTrans.get(outputLanguages[position]);
            } else {
                //Toast.makeText(ConversationMode.this, outputLanguages[position] + " output language", Toast.LENGTH_SHORT).show();
                OLC = hashMapOutput.get(outputLanguages[position]);
                OLCT = hashMapOutputTrans.get(outputLanguages[position]);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
        if (useHeadset) {
            boolean b = mBluetoothHeadset.stopVoiceRecognition(arrayList.get(0));
            Log.e("onerror", "Stopped blt voice recog");
            Log.e("stopped", String.valueOf(b));
        }
    }

    @Override
    public void onResults(Bundle results) {
        //Toast.makeText(this, "HELLO", Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "BYE", Toast.LENGTH_SHORT).show();
        if (res == 0) {
            res = 1;
            if (mBluetoothHeadset != null && mBluetoothAdapter != null) {
                boolean b = mBluetoothHeadset.stopVoiceRecognition(arrayList.get(0));
                mBluetoothAdapter = null;
                mBluetoothHeadset = null;
                Log.e("onresult", "Stopped blt voice recog");
                Log.e("stopped", String.valueOf(b));
            }

            if (flag == 100) {
                try {
                    ArrayList<String> output = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String query = output.get(0);
                    inText.setText("\n" + query);
                    String url = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + query + "&target=" + OLCT + "&source=" + ILCT;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e("Response: ", response.toString());
                                    try {
                                        translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                        Log.e("Translated Text", translatedString);
                                        outText.setText(translatedString);
                                        micButton2.setClickable(true);
                                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                            @Override
                                            public void onInit(int status) {
                                                //Log.e("Languages",tts.getAvailableLanguages().toString());
                                                tts.setLanguage(Locale.forLanguageTag(OLCT));
                                                micButton.clearAnimation();
                                                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                                                audioManager.setBluetoothScoOn(false);
                                                audioManager.setSpeakerphoneOn(true);

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

                    /*Translation translation = translate.translate(output.get(0).toLowerCase(), Translate.TranslateOption.sourceLanguage(ILCT), Translate.TranslateOption.targetLanguage(OLCT), Translate.TranslateOption.model("base"));
                    final String temp = translation.getTranslatedText();
                    */
/*
                    final String temp="hello";
                    outText.setText("\n" + temp);
                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            //Log.e("Languages", tts.getAvailableLanguages().toString());
                            tts.setLanguage(Locale.forLanguageTag(OLCT));
                            tts.speak(temp, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
*/
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
            } else if (flag == 101) {
                try {
                    ArrayList<String> output = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String query = output.get(0);
                    outText.setText("\n" + query);
                    String url = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + query + "&target=" + ILCT + "&source=" + OLCT;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.e("Response: ", response.toString());
                                    try {
                                        translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                        Log.e("Translated Text", translatedString);
                                        inText.setText(translatedString);
                                        micButton2.setClickable(true);
                                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                            @Override
                                            public void onInit(int status) {

                                                tts.setLanguage(Locale.forLanguageTag(ILCT));
                                                micButton2.clearAnimation();
                                                audioManager.setMode(AudioManager.MODE_NORMAL);
                                                audioManager.setBluetoothScoOn(false);
                                                audioManager.setSpeakerphoneOn(false);
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
                    /*Translation translation = translate.translate(output.get(0).toLowerCase(), Translate.TranslateOption.sourceLanguage(OLCT), Translate.TranslateOption.targetLanguage(ILCT), Translate.TranslateOption.model("base"));
                    final String temp = translation.getTranslatedText();
                    */
//                    final String temp="hello";
/*

                    outText.setText("\n" + temp);
                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            //Log.e("Languages", tts.getAvailableLanguages().toString());
                            tts.setLanguage(Locale.forLanguageTag(ILCT));
                            tts.speak(temp, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
*/
                } catch (Exception e) {
                    Log.e("error", e.toString());
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

    public void swap(View view) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.audio_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_switch: {
                if (useHeadset == false) {
                    useHeadset = true;
                    item.setIcon(R.drawable.ic_headset_mic_white_24dp);
                    return true;
                } else if (useHeadset == true) {
                    useHeadset = false;
                    item.setIcon(R.drawable.ic_mic_white_24dp);
                    return true;
                }
            }
            default:
                return true;
        }
    }

    private void findViewById() {
        outText = findViewById(R.id.tv_outText);
        inText = findViewById(R.id.tv_inText);
        backbtn5 = findViewById(R.id.btn_back5);
        oxtv = findViewById(R.id.tv_ox);
        intv = findViewById(R.id.tv_in);
        outtv = findViewById(R.id.tv_out);
        outputspinner = findViewById(R.id.spn_spinner7);
        micButton = findViewById(R.id.btn_mic);
        micButton2 = findViewById(R.id.btn_mic2);
        inputspinner = findViewById(R.id.spn_spinner6);
    }

    public static class CatchEvent extends BroadcastReceiver {

        public CatchEvent() {
            super();
        }

        @SuppressLint("LongLogTag")
        @Override
        public void onReceive(Context context, Intent intent) {

            String intentAction = intent.getAction();
            if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
                KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                    return;
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        if (checkInternetConnection(context)) {
                            micButton2.setClickable(false);
                            if (ILC.equals(OLC)) {
                                Toast.makeText(context, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                            } else {
                                flag = 100;
                                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ILC);
                                if (useHeadset) {
                                    usingHeadset(context);
                                } else {
                                    res = 0;
                                    speech.startListening(speechIntent);
                                    Toast.makeText(context, "Listening...", Toast.LENGTH_LONG).show();

                                }
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
                        Toast.makeText(ConversationMode.this, "Please accepet all condition..", Toast.LENGTH_SHORT).show();
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
/*

        inputLanguages= new String[]{"English", "Hindi","French","Malyalam","Afrikaans","Azerbaijan","Indonesian",
                "Malay","Javnese","Sundanese","Catalan","Czech","Danish","German","Estonian","Spanish","Filipino",
                "Galician","Croatian","Zulu","Icelandic","Swahili","Latvian","Lithuanian","Hungarian","Dutch","Norwegian",
                "Uzbek","Polish","Portuguese","Romanian","Slovenian","Slovak","Finnish","Swedish","Vietnamese","Turkish",
                "Greek","Bulgarian","Russian","Serbian","Ukrainian","Georgian","Armenian","Hebrew","Arabic","Farsi","Urdu","Amharic","Tamil",
                "Bengali","Kambodia","Kannada","Marathi","Gujarati","Sinhala","Telugu","Nepali","Lao","Thai","Burmese","Korean",
                "Chinese","Chinese(Hong Kong)","Japanese","Chinese"};
        inputLanguageCodes= new String[]{"en","hi","fr","ml","af","az","id","ms","jv","su","ca","cz","da","de","et",
                "es","fil","gl","hr","zu","is","sw","lv","lt","hu","nl","nb","uz","pl","pt","ro","sl","sk","fi","sv","vi",
                "tr","el","bg","ru","sr","uk","ka","hy","he","ar","fa","ur","am","ta","bn","km","kn","mr","gu","si",
                "te","ne","lo","th","my","ko","cmn","yue","ja","zh"};
*/

//TEXT TO Speech Languages: [ko_KR, mr_IN, ru_RU, zh_TW, hu_HU, th_TH, ur_PK, nb_NO, da_DK, tr_TR, et_EE, bs, sw, pt_PT, vi_VN, en_US, sv_SE, su_ID, bn_BD, gu_IN, kn_IN, el_GR, hi_IN, fi_FI, km_KH, bn_IN, fr_FR, uk_UA, en_AU, nl_NL, fr_CA, sr, pt_BR, ml_IN, si_LK, de_DE, ku, cs_CZ, pl_PL, sk_SK, fil_PH, it_IT, ne_NP, hr, en_NG, zh_CN, es_ES, cy, ta_IN, ja_JP, sq, yue_HK, en_IN, es_US, jv_ID, la, in_ID, te_IN, ro_RO, ca, en_GB]
