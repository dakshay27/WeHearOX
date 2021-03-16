package com.wehear.ox;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.wehear.ox.Database.AppDatabase;
import com.wehear.ox.Database.DatabaseLanguageModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;

public class TranslateTestActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    int hasDataPermission;
    private static final int REQUEST_CODE_WRITE_DATA = 2;
    String TAG = "Translate Test Activity";
    List<String> languages;
    AppDatabase appDatabase;
    List<DatabaseLanguageModel> languageModels;
    static List<String> offlineLanguageCode;
    boolean procee;
    private String[] inputLanguages;
    String[] inputLanguageCodes;
    String[] outputLanguageCodes;
    String[] inputLanguageCodesTrans;
    String[] outputLanguageCodesTrans;
    private String[] outputLanguages;


    private LanguageSelectArrayAdapter inputArrayAdapter;
    private Spinner outputspinner;
    private Spinner inputspinner;
    private LanguageSelectArrayAdapter outputArrayAdapter;

    String ILC = "en";
    String OLC = "hi";
    String ILCT = "en";
    String OLCT = "hi";
    String finalFirstLanguage = "";
    String finalSecondLanguage = "";

    HashMap<String, String> hashMapInput;
    HashMap<String, String> hashMapOutput;
    HashMap<String, String> hashMapInputTrans;
    HashMap<String, String> hashMapOutputTrans;
    HashMap<String, Boolean> hashFav, hashDown;
    String address;
    static boolean isConnected = false;
    ImageView imgBack, imgDowload;
    Button startButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_test);

        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "FavLang").build();
        offlineLanguageCode = new ArrayList<>();
        hashMapInput = new HashMap<>();
        hashMapInputTrans = new HashMap<>();
        hashMapOutput = new HashMap<>();
        hashMapOutputTrans = new HashMap<>();
        // button
        imgBack = findViewById(R.id.btn_back6);
        imgDowload = findViewById(R.id.img_dowload);
        imgDowload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TranslateTestActivity.this, DowloadLanguage.class));
            }
        });
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        startButton = findViewById(R.id.translate_start_button);
        hashDown = new HashMap<>();
        hashFav = new HashMap<>();
        languages = new ArrayList<>();
        checkSelfPermission();
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkConnected();

//
                if (ILC.equals(OLC)) {
                    Toast.makeText(TranslateTestActivity.this, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(TranslateTestActivity.this, TranslateTestActivity2.class);
                    intent.putExtra("ILC", ILC);
                    intent.putExtra("ILCT", ILCT);
                    intent.putExtra("OLC", OLC);
                    intent.putExtra("OLCT", OLCT);
                    intent.putExtra("finalFirstLanguage", finalFirstLanguage);
                    intent.putExtra("finalSecondLanguage", finalSecondLanguage);
                    if (offlineLanguageCode.contains(ILCT)) {
                        if (offlineLanguageCode.contains(OLCT)) {

                            intent.putExtra("isOffline", true);
                        } else {
                            intent.putExtra("isOffline", false);
                        }
                    } else {
                        intent.putExtra("isOffline", false);
                    }

                    startActivity(intent);

                }
            }
        });

        // languages list
        inputLanguages = new String[]{"English", "Hindi", "French", "Malyalam", "Afrikaans", "Azerbaijan", "Indonesian",
                "Malay", "Javnese", "Sundanese", "Catalan", "Czech", "Danish", "German", "Estonian", "Spanish", "Basque", "Filipino",
                "Galician", "Croatian", "Zulu", "Icelandic", "Italian", "Swahili", "Latvian", "Lithuanian", "Hungarian", "Dutch", "Norwegian",
                "Uzbek", "Polish", "Portuguese", "Romanian", "Slovenian", "Slovak", "Finnish", "Swedish", "Vietnamese", "Turkish",
                "Greek", "Bulgarian", "Russian", "Serbian", "Ukrainian", "Georgian", "Armenian", "Hebrew", "Arabic", "Farsi", "Urdu", "Amharic", "Tamil",
                "Bengali", "Kambodia", "Kannada", "Marathi", "Gujarati", "Sinhala", "Telugu", "Nepali", "Lao", "Thai", "Burmese", "Korean", "Japanese", "Chinese", "Chinese(Hong Kong)"};

        //languages codes
        inputLanguageCodes = new String[]{"en", "hi-IN", "fr-FR", "ml-IN", "af-ZA", "az-AZ", "id-ID", "ms-MY", "jv-ID", "su-ID", "ca-ES", "cs-CZ", "da-DK", "de", "et-EE",
                "es", "eu-ES", "fil-PH", "gl-ES", "hr-HR", "zu-ZA", "is-IS", "it-IT", "sw", "lv_LV", "lt-LT", "hu-HU", "nl-NL", "nb-NO", "uz-UZ", "pl-PL", "pt-PT", "ro-RO", "sl-SI", "sk-SK", "fi-FI", "sv-SE", "vi-VN",
                "tr-TR", "el-GR", "bg-BG", "ru-RU", "sr-RS", "uk-UA", "ka-GE", "hy-AM", "he-IL", "ar", "fa-IR", "ur", "am-ET", "ta", "bn", "km-KH", "kn-IN", "mr-IN", "gu-IN", "si-LK",
                "te-IN", "ne-NP", "lo-LA", "th-TH", "my-MM", "ko-KR", "ja-JP", "zh", "yue"};

        //languages code for api;
        inputLanguageCodesTrans = new String[]{"en", "hi", "fr", "ml", "af", "az", "id", "ms", "jv", "su", "ca", "cs", "da", "de", "et",
                "es", "eu", "fil", "gl", "hr", "zu", "is", "it", "sw", "lv", "lt", "hu", "nl", "nb", "uz", "pl", "pt", "ro", "sl", "sk", "fi", "sv", "vi",
                "tr", "el", "bg", "ru", "sr", "uk", "ka", "hy", "he", "ar", "fa", "ur", "am", "ta", "bn", "km", "kn", "mr", "gu", "si",
                "te", "ne", "lo", "th", "my", "ko", "ja", "zh", "yue"};

        inputspinner = findViewById(R.id.spn_first_language);

        for (int i = 0; i < inputLanguages.length; i++) {
            hashMapInput.put(inputLanguages[i], inputLanguageCodes[i]);
            hashMapInputTrans.put(inputLanguages[i], inputLanguageCodesTrans[i]);
        }
        Arrays.sort(inputLanguages);
        finalFirstLanguage = inputLanguages[0];
        ILC = hashMapInput.get(inputLanguages[0]);
        ILCT = hashMapInputTrans.get(inputLanguages[0]);
        inputspinner.setOnItemSelectedListener(this);
        outputLanguages = new String[]{"Hindi", "English", "French", "Malyalam", "Afrikaans", "Azerbaijan", "Indonesian",
                "Malay", "Javnese", "Sundanese", "Catalan", "Czech", "Danish", "German", "Estonian", "Spanish", "Basque", "Filipino",
                "Galician", "Croatian", "Zulu", "Icelandic", "Italian", "Swahili", "Latvian", "Lithuanian", "Hungarian", "Dutch", "Norwegian",
                "Uzbek", "Polish", "Portuguese", "Romanian", "Slovenian", "Slovak", "Finnish", "Swedish", "Vietnamese", "Turkish",
                "Greek", "Bulgarian", "Russian", "Serbian", "Ukrainian", "Georgian", "Armenian", "Hebrew", "Arabic", "Farsi", "Urdu", "Amharic", "Tamil",
                "Bengali", "Kambodia", "Kannada", "Marathi", "Gujarati", "Sinhala", "Telugu", "Nepali", "Lao", "Thai", "Burmese", "Korean", "Chinese(Hong Kong)", "Japanese", "Chinese"};

        outputLanguageCodes = new String[]{"hi-IN", "en", "fr-FR", "ml-IN", "af-ZA", "az-AZ", "id-ID", "ms-MY", "jv-ID", "su-ID", "ca-ES", "cs-CZ", "da-DK", "de", "et-EE",
                "es", "eu-ES", "fil-PH", "gl-ES", "hr-HR", "zu-ZA", "is-IS", "it-IT", "sw", "lv_LV", "lt-LT", "hu-HU", "nl-NL", "nb-NO", "uz-UZ", "pl-PL", "pt-PT", "ro-RO", "sl-SI", "sk-SK", "fi-FI", "sv-SE", "vi-VN",
                "tr-TR", "el-GR", "bg-BG", "ru-RU", "sr-RS", "uk-UA", "ka-GE", "hy-AM", "he-IL", "ar", "fa-IR", "ur", "am-ET", "ta", "bn", "km-KH", "kn-IN", "mr-IN", "gu-IN", "si-LK",
                "te-IN", "ne-NP", "lo-LA", "th-TH", "my-MM", "ko-KR", "yue", "ja-JP", "zh"};
        outputLanguageCodesTrans = new String[]{"hi", "en", "fr", "ml", "af", "az", "id", "ms", "jv", "su", "ca", "cs", "da", "de", "et",
                "es", "eu", "fil", "gl", "hr", "zu", "is", "it", "sw", "lv", "lt", "hu", "nl", "nb", "uz", "pl", "pt", "ro", "sl", "sk", "fi", "sv", "vi",
                "tr", "el", "bg", "ru", "sr", "uk", "ka", "hy", "he", "ar", "fa", "ur", "am", "ta", "bn", "km", "kn", "mr", "gu", "si",
                "te", "ne", "lo", "th", "my", "ko", "yue", "ja", "zh"};
        //Toast.makeText(this, String.valueOf(outputLanguages.length), Toast.LENGTH_SHORT).show();
        outputspinner = findViewById(R.id.spn_second_language);

        for (int i = 0; i < outputLanguages.length; i++) {
            hashMapOutput.put(outputLanguages[i], outputLanguageCodes[i]);
            hashMapOutputTrans.put(outputLanguages[i], outputLanguageCodesTrans[i]);
        }
        Arrays.sort(outputLanguages);
        languages = new ArrayList<>(Arrays.asList(outputLanguages));
        prepareOfflineList(0);
        finalSecondLanguage = outputLanguages[0];
        OLC = hashMapOutput.get(outputLanguages[0]);
        OLCT = hashMapOutputTrans.get(outputLanguages[0]);

        outputspinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spn_first_language) {
            String temp = hashMapInput.get(languages.get(position));
            if (temp.equals(OLC)) {
                Toast.makeText(TranslateTestActivity.this, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                ILC = hashMapInput.get(inputLanguages[position]);
                ILCT = hashMapInputTrans.get(inputLanguages[position]);
            } else {
                //Toast.makeText(TranslateActivity.this, inputLanguages[position] + " input language", Toast.LENGTH_SHORT).show();
                ILC = hashMapInput.get(languages.get(position));
                ILCT = hashMapInputTrans.get(languages.get(position));
                finalFirstLanguage = languages.get(position);
                Log.d("Here", ILC + ILCT);
            }
        } else if (parent.getId() == R.id.spn_second_language) {
            String temp = hashMapOutput.get(languages.get(position));
            if (temp.equals(ILC)) {
                Toast.makeText(TranslateTestActivity.this, "Input language and Output language cannot be same", Toast.LENGTH_SHORT).show();
                OLC = hashMapOutput.get(languages.get(position));
                OLCT = hashMapOutputTrans.get(languages.get(position));
            } else {
                //Toast.makeText(TranslateActivity.this, outputLanguages[position] + " output language", Toast.LENGTH_SHORT).show();
                OLC = hashMapOutput.get(languages.get(position));
                OLCT = hashMapOutputTrans.get(languages.get(position));
                finalSecondLanguage = languages.get(position);
                Log.d("Here", ILC + " " + ILCT);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private BluetoothProfile.ServiceListener serviceListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceDisconnected(int profile) {
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            for (BluetoothDevice device : proxy.getConnectedDevices()) {
                address = device.getAddress();
                Log.d(TAG, "onServiceConnected: " + address);
                if (address.contains("11:11:22") || address.contains("08:A3:0B")) {
                    isConnected = true;
                } else {
                    Bugfender.d(TAG, "Not connected to weHear");
                    isConnected = false;
                }
            }
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy);
        }
    };


    void checkConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            mBluetoothAdapter.getProfileProxy(this, serviceListener, BluetoothProfile.HEADSET);

        } else {
            isConnected = false;
        }
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
                outputArrayAdapter = new LanguageSelectArrayAdapter(TranslateTestActivity.this, R.layout.custom_spinner_layout, languages, hashFav, hashDown);
                outputspinner.setAdapter(outputArrayAdapter);
                inputArrayAdapter = new LanguageSelectArrayAdapter(TranslateTestActivity.this, R.layout.custom_spinner_layout, languages, hashFav, hashDown);
                inputspinner.setAdapter(inputArrayAdapter);
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
                    boolean writeExternal = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAudio = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean bluetooth = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean audioSetting = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (writeExternal && recordAudio && bluetooth && audioSetting) {

                    } else {
                        Toast.makeText(TranslateTestActivity.this, "Please accepet all condition..", Toast.LENGTH_SHORT).show();
                        checkSelfPermission();
                    }
                }
                break;
        }
    }

    private void checkSelfPermission() {
        hasDataPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(this, BLUETOOTH_ADMIN)
                + ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, MODIFY_AUDIO_SETTINGS);

        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, BLUETOOTH_ADMIN}, REQUEST_CODE_WRITE_DATA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, BLUETOOTH_ADMIN}, REQUEST_CODE_WRITE_DATA);
            }
        }

    }
}
