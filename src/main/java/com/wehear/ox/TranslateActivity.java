package com.wehear.ox;

import android.Manifest;
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
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.BuildConfig;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.wehear.ox.Database.AppDatabase;
import com.wehear.ox.Database.DatabaseLanguageModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;


public class TranslateActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, RecognitionListener {
    int hasDataPermission;
    private static final int REQUEST_CODE_WRITE_DATA = 2;
    ImageView imgArowSwap, imgSpeakerOneMore;
    List<DatabaseLanguageModel> languageModels;
    AppDatabase appDatabase;
    boolean flag = false;
    ImageView imgDowload;
    static boolean offlineavail, procee = true;
    private static final String TAG = "TranslateActivity";
    static AudioManager audioManager;
    static ComponentName mReceiverComponent;
    private Spinner inputspinner;
    private LanguageSelectArrayAdapter inputaa;
    private String[] inputLanguages;
    private Spinner outputspinner;
    private LanguageSelectArrayAdapter outputaa;
    private ImageView mic_image;
    private ImageView btnback6;
    private TextView oxtvv, textView, textView2;
    private String[] outputLanguages;
    static ImageView micButton;
    String[] inputLanguageCodes;
    String[] outputLanguageCodes;
    String[] inputLanguageCodesTrans;
    String[] outputLanguageCodesTrans;
    static List<String> offlineLanguageCode, languages;
    static String ILC = "hi-IN";
    static String OLC = "hi";
    static String ILCT = "";
    static String OLCT = "";
    HashMap<String, String> hashMapInput;
    HashMap<String, String> hashMapOutput;
    HashMap<String, String> hashMapInputTrans;
    HashMap<String, String> hashMapOutputTrans;
    private ArrayList<String> supportedLanguages;
    private String languagePreference;
    TextView outText;
    static boolean connected;
    //Translate translate;
    TextToSpeech tts;
    static SpeechRecognizer speech;
    //Switch aSwitch;
    static boolean useHeadset;
    AudioManager mAudioManager;
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
    HashMap<String, Boolean> hashFav, hashDown;
    int pos = 0, totalDown = 0;
    String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_translate);
        imgSpeakerOneMore = findViewById(R.id.img_speaks_one_more);
        useHeadset = false;
        imgArowSwap = findViewById(R.id.img_arrow_swap);
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "FavLang").build();

        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        checkSelfPermission();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mReceiverComponent = new ComponentName(this, CatchEvent.class);
        audioManager.registerMediaButtonEventReceiver(mReceiverComponent);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT), AudioTrack.MODE_STREAM);
        at.play();

        at.stop();
        at.release();
        imgDowload = findViewById(R.id.img_dowload);
        imgDowload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TranslateActivity.this, DowloadLanguage.class));
            }
        });
        outText = findViewById(R.id.tv_outText);
        micButton = findViewById(R.id.btn_mic);
        //mic_image=findViewById(R.id.mic_image);
        //aSwitch=findViewById(R.id.headset_switch);

        btnback6 = findViewById(R.id.btn_back6);
        btnback6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        oxtvv = findViewById(R.id.tv_ox);
        textView = findViewById(R.id.tv);
        textView2 = findViewById(R.id.tv2);


        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);

        hashMapInput = new HashMap<>();
        hashMapInputTrans = new HashMap<>();
        inputLanguages = new String[]{"English", "Hindi", "French", "Malyalam", "Afrikaans", "Azerbaijan", "Indonesian",
                "Malay", "Javnese", "Sundanese", "Catalan", "Czech", "Danish", "German", "Estonian", "Spanish", "Basque", "Filipino",
                "Galician", "Croatian", "Zulu", "Icelandic", "Italian", "Swahili", "Latvian", "Lithuanian", "Hungarian", "Dutch", "Norwegian",
                "Uzbek", "Polish", "Portuguese", "Romanian", "Slovenian", "Slovak", "Finnish", "Swedish", "Vietnamese", "Turkish",
                "Greek", "Bulgarian", "Russian", "Serbian", "Ukrainian", "Georgian", "Armenian", "Hebrew", "Arabic", "Farsi", "Urdu", "Amharic", "Tamil",
                "Bengali", "Kambodia", "Kannada", "Marathi", "Gujarati", "Sinhala", "Telugu", "Nepali", "Lao", "Thai", "Burmese", "Korean", "Japanese", "Chinese", "Chinese(Hong Kong)"};

        inputLanguageCodes = new String[]{"en", "hi-IN", "fr-FR", "ml-IN", "af-ZA", "az-AZ", "id-ID", "ms-MY", "jv-ID", "su-ID", "ca-ES", "cs-CZ", "da-DK", "de", "et-EE",
                "es", "eu-ES", "fil-PH", "gl-ES", "hr-HR", "zu-ZA", "is-IS", "it-IT", "sw", "lv_LV", "lt-LT", "hu-HU", "nl-NL", "nb-NO", "uz-UZ", "pl-PL", "pt-PT", "ro-RO", "sl-SI", "sk-SK", "fi-FI", "sv-SE", "vi-VN",
                "tr-TR", "el-GR", "bg-BG", "ru-RU", "sr-RS", "uk-UA", "ka-GE", "hy-AM", "he-IL", "ar", "fa-IR", "ur", "am-ET", "ta", "bn", "km-KH", "kn-IN", "mr-IN", "gu-IN", "si-LK",
                "te-IN", "ne-NP", "lo-LA", "th-TH", "my-MM", "ko-KR", "ja-JP", "zh", "yue"};
        inputLanguageCodesTrans = new String[]{"en", "hi", "fr", "ml", "af", "az", "id", "ms", "jv", "su", "ca", "cs", "da", "de", "et",
                "es", "eu", "fil", "gl", "hr", "zu", "is", "it", "sw", "lv", "lt", "hu", "nl", "nb", "uz", "pl", "pt", "ro", "sl", "sk", "fi", "sv", "vi",
                "tr", "el", "bg", "ru", "sr", "uk", "ka", "hy", "he", "ar", "fa", "ur", "am", "ta", "bn", "km", "kn", "mr", "gu", "si",
                "te", "ne", "lo", "th", "my", "ko", "ja", "zh", "yue"};
        hashDown = new HashMap<>();
        hashFav = new HashMap<>();
        for (int i = 0; i < inputLanguages.length; i++) {
            hashMapInput.put(inputLanguages[i], inputLanguageCodes[i]);
            hashMapInputTrans.put(inputLanguages[i], inputLanguageCodesTrans[i]);
        }
        Arrays.sort(inputLanguages);
        ILCT = hashMapInputTrans.get(inputLanguages[0]);
        inputspinner = findViewById(R.id.spn_spinner6);

        inputspinner.setOnItemSelectedListener(this);

        hashMapOutput = new HashMap<>();
        hashMapOutputTrans = new HashMap<>();
        outputLanguages = new String[]{"Hindi", "English", "French", "Malyalam", "Afrikaans", "Azerbaijan", "Indonesian",
                "Malay", "Javnese", "Sundanese", "Catalan", "Czech", "Danish", "German", "Estonian", "Spanish", "Basque", "Filipino",
                "Galician", "Croatian", "Zulu", "Icelandic", "Italian", "Swahili", "Latvian", "Lithuanian", "Hungarian", "Dutch", "Norwegian",
                "Uzbek", "Polish", "Portuguese", "Romanian", "Slovenian", "Slovak", "Finnish", "Swedish", "Vietnamese", "Turkish",
                "Greek", "Bulgarian", "Russian", "Serbian", "Ukrainian", "Georgian", "Armenian", "Hebrew", "Arabic", "Farsi", "Urdu", "Amharic", "Tamil",
                "Bengali", "Kambodia", "Kannada", "Marathi", "Gujarati", "Sinhala", "Telugu", "Nepali", "Lao", "Thai", "Burmese", "Korean",
                "Chinese(Hong Kong)", "Japanese", "Chinese"};
        outputLanguageCodes = new String[]{"hi-IN", "en", "fr-FR", "ml-IN", "af-ZA", "az-AZ", "id-ID", "ms-MY", "jv-ID", "su-ID", "ca-ES", "cs-CZ", "da-DK", "de", "et-EE",
                "es", "eu-ES", "fil-PH", "gl-ES", "hr-HR", "zu-ZA", "is-IS", "it-IT", "sw", "lv_LV", "lt-LT", "hu-HU", "nl-NL", "nb-NO", "uz-UZ", "pl-PL", "pt-PT", "ro-RO", "sl-SI", "sk-SK", "fi-FI", "sv-SE", "vi-VN",
                "tr-TR", "el-GR", "bg-BG", "ru-RU", "sr-RS", "uk-UA", "ka-GE", "hy-AM", "he-IL", "ar", "fa-IR", "ur", "am-ET", "ta", "bn", "km-KH", "kn-IN", "mr-IN", "gu-IN", "si-LK",
                "te-IN", "ne-NP", "lo-LA", "th-TH", "my-MM", "ko-KR", "yue", "ja-JP", "zh"};
        outputLanguageCodesTrans = new String[]{"hi", "en", "fr", "ml", "af", "az", "id", "ms", "jv", "su", "ca", "cs", "da", "de", "et",
                "es", "eu", "fil", "gl", "hr", "zu", "is", "it", "sw", "lv", "lt", "hu", "nl", "nb", "uz", "pl", "pt", "ro", "sl", "sk", "fi", "sv", "vi",
                "tr", "el", "bg", "ru", "sr", "uk", "ka", "hy", "he", "ar", "fa", "ur", "am", "ta", "bn", "km", "kn", "mr", "gu", "si",
                "te", "ne", "lo", "th", "my", "ko", "yue", "ja", "zh"};

        offlineLanguageCode = new ArrayList<>();
        //Toast.makeText(this, String.valueOf(outputLanguages.length), Toast.LENGTH_SHORT).show();
        for (int i = 0; i < outputLanguages.length; i++) {
            hashMapOutput.put(outputLanguages[i], outputLanguageCodes[i]);
            hashMapOutputTrans.put(outputLanguages[i], outputLanguageCodesTrans[i]);
        }
        Arrays.sort(outputLanguages);
        languages = new ArrayList<>(Arrays.asList(outputLanguages));
        OLCT = hashMapOutputTrans.get(outputLanguages[0]);
        prepareOfflineList(0);
        outputspinner = findViewById(R.id.spn_spinner7);

        outputspinner.setOnItemSelectedListener(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, TranslateActivity.this.getPackageName());

        //If there is internet connection, get translate service and start translation:
        //getTranslateService();
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!procee) {
                    if (checkInternetConnection(getApplicationContext())) {
                        if (ILC.equals(OLC)) {
                            showToast("Input language and Output language cannot be same", getApplicationContext());

                        } else {
                            if (ContextCompat.checkSelfPermission(TranslateActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ILC);
                                if (offlineLanguageCode.contains(ILCT)) {
                                    if (offlineLanguageCode.contains(OLCT)) {
                                        offlineavail = true;
                                    } else {
                                        offlineavail = false;
                                    }
                                } else {
                                    offlineavail = false;
                                }
                                if (useHeadset) {
                                    usingHeadset(getApplicationContext());
                                } else {
                                    res = 0;
                                    speech.startListening(speechIntent);
                                    showToast("Listening...", getApplicationContext());
                                    final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein_fadeout);
                                    micButton.startAnimation(myAnim);
                                }
                            } else {
                                ActivityCompat.requestPermissions(TranslateActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 99);
                            }
                        }
                    } else {
                        if (ILC.equals(OLC)) {
                            showToast("Input language and Output language cannot be same", getApplicationContext());
                        } else {
                            if (offlineLanguageCode.contains(ILCT)) {
                                if (offlineLanguageCode.contains(OLCT)) {
                                    offlineavail = true;
                                    if (ContextCompat.checkSelfPermission(TranslateActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                                        if (useHeadset) {
                                            usingHeadset(getApplicationContext());
                                        } else {
                                            res = 0;
                                            speech.startListening(speechIntent);
                                            showToast("Listening", getApplicationContext());

                                            final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadein_fadeout);
                                            micButton.startAnimation(myAnim);
                                        }
                                    } else {
                                        ActivityCompat.requestPermissions(TranslateActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 99);
                                    }
                                } else {
                                    offlineavail = false;
                                    showToast("Traget not in offline mode", getApplicationContext());
                                }
                            } else {
                                offlineavail = false;
                                showToast("Source not in offline mode", getApplicationContext());
                            }
                        }
                    }
                } else {
                    showToast("Some inner process are running!!\nPlease wait a minutes!!", getApplicationContext());
                }
            }
        });


        /*aSwitch.setChecked(false);
        useHeadset=false;
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    useHeadset=true;
                }
                else{
                    useHeadset=false;
                }
            }
        });*/
        imgArowSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ic = ILC;
                ILC = OLC;
                OLC = ic;
                int a = inputspinner.getSelectedItemPosition();
                inputspinner.setSelection(outputspinner.getSelectedItemPosition());
                outputspinner.setSelection(a);
            }
        });
        imgSpeakerOneMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!message.equals("")) {
                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            //Log.e("Languages",tts.getAvailableLanguages().toString());
                            tts.setLanguage(Locale.forLanguageTag(OLCT));
                            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    });
                }
            }
        });
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

                        mBluetoothHeadset = (BluetoothHeadset) proxy;
                        devices = mBluetoothHeadset.getConnectedDevices();
                        arrayList = new ArrayList<>(devices);
                        if (arrayList.size() > 0) {
                            isBluetoothConnected = true;
                            Log.e("Message", "Found connected Bluetooth Headset");

                            Log.e("devices", arrayList.get(0).getName());

                            boolean b = mBluetoothHeadset.startVoiceRecognition(arrayList.get(0));
                            Log.e("voice recog", String.valueOf(b));
                            if (b) {
                                Log.e("message", "Listening...");
                                showToast("Listening", context);
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
            showToast("Bluetooth disabled on device", context);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (parent.getId() == R.id.spn_spinner6) {
            String temp = hashMapInput.get(languages.get(position));

            if (temp.equals(OLC)) {
                showToast("Input language and Output language cannot be same", getApplicationContext());
                ILC = hashMapInput.get(languages.get(position));
                ILCT = hashMapInputTrans.get(languages.get(position));
            } else {
                ILC = hashMapInput.get(languages.get(position));
                ILCT = hashMapInputTrans.get(languages.get(position));
                Log.d("Here", ILC + " " + ILCT);
            }

        } else if (parent.getId() == R.id.spn_spinner7) {
            String temp = hashMapOutput.get(languages.get(position));
            if (temp.equals(ILC)) {
                showToast("Input language and Output language cannot be same", getApplicationContext());
                OLC = hashMapOutput.get(languages.get(position));
                OLCT = hashMapOutputTrans.get(languages.get(position));
            } else {
                OLC = hashMapOutput.get(languages.get(position));
                OLCT = hashMapOutputTrans.get(languages.get(position));
                Log.d("Here O", OLC + " " + OLCT);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        micButton.clearAnimation();
    }

    @Override
    public void onBeginningOfSpeech() {
        micButton.clearAnimation();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        micButton.clearAnimation();
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        micButton.clearAnimation();
    }

    @Override
    public void onEndOfSpeech() {
        micButton.clearAnimation();
    }

    @Override
    public void onError(int error) {

        micButton.clearAnimation();
        if (useHeadset) {
            boolean b = mBluetoothHeadset.stopVoiceRecognition(arrayList.get(0));
            Log.e("onerror", "Stopped blt voice recog");
            Log.e("stopped", String.valueOf(b));

        } else {

        }
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
            ArrayList<String> output = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (output.size() == 0) {
                showToast("Unidentified Speech", getApplicationContext());
            }

            /*
            db.collection("translations")
                    .document("sKQQqmi80fDdfPgJpvgS")
                    .update("input",output.get(0))
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.e("INPUT","SUCCESS");
                            db.collection("translations")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    HashMap<String,Object> hashMap = new HashMap<>(document.getData());
                                                    HashMap<String,Object> hashMap1 = new HashMap<String, Object>((HashMap<String, Object>)hashMap.get("translated"));
                                                    translatedString=hashMap1.get(OLCT).toString();
                                                    outText.setText("\n" + translatedString);
                                                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                                        @Override
                                                        public void onInit(int status) {
                                                            //Log.e("Languages",tts.getAvailableLanguages().toString());
                                                            tts.setLanguage(Locale.forLanguageTag(OLCT));
                                                            tts.speak(translatedString, TextToSpeech.QUEUE_FLUSH, null);
                                                        }
                                                    });
                                                }
                                            } else {
                                                Log.w("TRANSLATED", "Error getting documents.", task.getException());

                                            }
                                        }
                                    });


                        }
                    });
            */
            //Translation translation = translate.translate(output.get(0).toLowerCase(), Translate.TranslateOption.sourceLanguage(ILCT), Translate.TranslateOption.targetLanguage(OLCT), Translate.TranslateOption.model("base"));
            //outText.setText(outText.getText()+"\n"+"-> "+translation.getTranslatedText());
            //final String string = translation.getTranslatedText();
            final String query = output.get(0);

            if (offlineavail) {

                TranslatorOptions translatorOptions = new TranslatorOptions.Builder()
                        .setSourceLanguage(ILCT)
                        .setTargetLanguage(OLCT)
                        .build();
                final Translator sourceToTarget = Translation.getClient(translatorOptions);
                micButton.clearAnimation();
                sourceToTarget.translate(query).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(final String s) {
                        Log.e("Translated Text", s);
                        outText.setText(s);
                        message = s;
                        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                            @Override
                            public void onInit(int status) {
                                //Log.e("Languages",tts.getAvailableLanguages().toString());
                                tts.setLanguage(Locale.forLanguageTag(OLCT));
                                tts.speak(s, TextToSpeech.QUEUE_FLUSH, null);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        showToast(e.getLocalizedMessage(), getApplicationContext());

                    }
                });
            } else {

                String url = "https://translation.googleapis.com/language/translate/v2?key=" + apikey + "&q=" + query + "&target=" + OLCT + "&source=" + ILCT;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.e("Response: ", response.toString());
                                try {
                                    translatedString = (String) ((JSONObject) ((JSONObject) response.get("data")).getJSONArray("translations").get(0)).get("translatedText");
                                    Log.e("Translated Text", translatedString);
                                    micButton.clearAnimation();
                                    outText.setText(translatedString);
                                    message = translatedString;
                                    tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        @Override
                                        public void onInit(int status) {
                                            //Log.e("Languages",tts.getAvailableLanguages().toString());
                                            tts.setLanguage(Locale.forLanguageTag(OLCT));
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
/*            outText.setText("\n" + translatedString);
            tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    //Log.e("Languages",tts.getAvailableLanguages().toString());
                    tts.setLanguage(Locale.forLanguageTag(OLCT));
                    tts.speak(translatedString, TextToSpeech.QUEUE_FLUSH, null);
                }
            });
            */
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        micButton.clearAnimation();
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        micButton.clearAnimation();
    }

    public void convMode(View view) {
        Intent intent = new Intent(this, TranslateTestActivity.class);
        startActivity(intent);
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
                        if (!procee) {
                            if (checkInternetConnection(context)) {
                                if (ILC.equals(OLC)) {
                                    showToast("Input language and Output language cannot be same", context);

                                } else {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ILC);
                                        if (offlineLanguageCode.contains(ILCT)) {
                                            if (offlineLanguageCode.contains(OLCT)) {
                                                offlineavail = true;
                                            } else {
                                                offlineavail = false;
                                            }
                                        } else {
                                            offlineavail = false;
                                        }
                                        if (useHeadset) {
                                            usingHeadset(context);
                                        } else {
                                            res = 0;
                                            speech.startListening(speechIntent);
                                            showToast("Listening...", context);
                                            final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.fadein_fadeout);
                                            micButton.startAnimation(myAnim);
                                        }
                                    }
                                }
                            } else {

                                if (ILC.equals(OLC)) {
                                    showToast("Input language and Output language cannot be same", context);
                                } else {
                                    if (offlineLanguageCode.contains(ILCT)) {
                                        if (offlineLanguageCode.contains(OLCT)) {
                                            offlineavail = true;
                                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                                speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, ILC);
                                                if (useHeadset) {
                                                    usingHeadset(context);
                                                } else {
                                                    res = 0;
                                                    speech.startListening(speechIntent);
                                                    showToast("Listening...", context);
                                                    final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.fadein_fadeout);
                                                    micButton.startAnimation(myAnim);
                                                }
                                            }
                                        } else {
                                            offlineavail = false;
                                            showToast("Traget not in offline mode", context);
                                        }
                                    } else {
                                        offlineavail = false;
                                        showToast("Source not in offline mode", context);
                                    }
                                }
                            }
                        } else {
                            showToast("Some inner process are running!!\nPlease wait a minutes!!", context);
                        }
                        break;

                }
            }

            if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                Log.i(TAG, "no media button information");
                showToast("no media button information", context);

                return;
            }
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null) {
                Log.i(TAG, "No key press");
                showToast("No key press", context);
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioManager.unregisterMediaButtonEventReceiver(mReceiverComponent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        audioManager.registerMediaButtonEventReceiver(mReceiverComponent);
    }


    private static void showToast(String s, Context context) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    private void prepareOfflineList(final int pos) {
        final int[] pos1 = {0};
        String langaugeCode = hashMapOutputTrans.get(outputLanguages[pos]);
        final RemoteModelManager modelManager = RemoteModelManager.getInstance();
        TranslateRemoteModel model = new TranslateRemoteModel.Builder(langaugeCode)
                .build();
        modelManager.isModelDownloaded(model).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    offlineLanguageCode.add(outputLanguages[pos]);
                    hashDown.put(outputLanguages[pos], true);
                    languages.remove(outputLanguages[pos]);
                    languages.add(pos1[0], outputLanguages[pos]);
                    pos1[0]++;
                    totalDown++;
                } else {
                    hashDown.put(outputLanguages[pos], false);
                }
                if ((pos + 1) < outputLanguageCodesTrans.length)
                    prepareOfflineList(pos + 1);
                else {
                    procee = false;
                    getLanguage();
                }
            }
        });
    }

    void getLanguage() {
        final int[] pos1 = {0};
        for (int i = 0; i < outputLanguages.length; i++) {
            hashFav.put(outputLanguages[i], false);
        }
        class GetLan extends AsyncTask<Void, Void, List<DatabaseLanguageModel>> {

            @Override
            protected List<DatabaseLanguageModel> doInBackground(Void... voids) {
                List<DatabaseLanguageModel> lan = appDatabase.langaugeDao().getAll();
                return lan;
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            protected void onPostExecute(List<DatabaseLanguageModel> databaseLanguageModels) {
                super.onPostExecute(databaseLanguageModels);
                languageModels = databaseLanguageModels;
                for (int i = 0; i < languageModels.size(); i++) {
                    String languageName = languageModels.get(i).languageName;
                    for (int j = 0; j < outputLanguages.length; j++) {
                        if (outputLanguages[j].equals(languageName) && (!hashFav.get(languageName))) {

                            hashFav.replace(languageName, true);
                            Log.d("Here", "Lan:" + languageName + "Pos:" + pos1[0]);
                            languages.remove(languageName);
                            languages.add(pos1[0], languageName);
                            pos1[0]++;

                            break;
                        }
                    }
                }
                outputaa = new LanguageSelectArrayAdapter(TranslateActivity.this, R.layout.custom_spinner_layout, languages, hashFav, hashDown);
                outputspinner.setAdapter(outputaa);
                inputaa = new LanguageSelectArrayAdapter(TranslateActivity.this, R.layout.custom_spinner_layout, languages, hashFav, hashDown);
                inputspinner.setAdapter(inputaa);
            }
        }
        new GetLan().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_WRITE_DATA:
                if (grantResults.length > 0) {
                    boolean bluetothh = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAudio = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (bluetothh && recordAudio) {
                        
                    } else {
                        Toast.makeText(TranslateActivity.this, "Please accepet all condition for use this feature..", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                break;
        }
    }

    private void checkSelfPermission() {
        hasDataPermission = ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, MODIFY_AUDIO_SETTINGS);

        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, REQUEST_CODE_WRITE_DATA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, REQUEST_CODE_WRITE_DATA);
            }
        }

    }
}
