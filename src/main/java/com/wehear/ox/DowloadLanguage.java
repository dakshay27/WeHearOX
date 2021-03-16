package com.wehear.ox;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.wehear.ox.AppModel.DowloadLanguageModel;
import com.wehear.ox.Database.AppDatabase;
import com.wehear.ox.Database.DatabaseLanguageModel;
import com.wehear.ox.Interfaces.ClickLanguages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class DowloadLanguage extends AppCompatActivity implements ClickLanguages {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    RecyclerView recyclerDowloadLan, recyclerAvailLan;
    List<DowloadLanguageModel> listDowloadLang, listAvailLang;
    String[] inputLanguages, inputLanguageCodesTrans;
    boolean procee = true;
    DownloadAvailLanguageAdapter downloadAvailLanguageAdapter;
    DownloadLanguageAdapter downloadLanguageAdapter;
    HashMap<String, String> hashMapInputTrans, hashMapDowloadLanguage;
    AppDatabase appDatabase;
    List<DatabaseLanguageModel> languageModels;
    boolean deleteCall = false;
    boolean engFav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dowload_language);
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        engFav=sharedPreferences.getBoolean("isDowloadFirst",true);
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "FavLang").build();
        if(engFav){
            Log.d("Here","EngFav");
            insertLanguage("English");
            editor.putBoolean("isDowloadFirst",false);
            editor.apply();
        }
        recyclerAvailLan = findViewById(R.id.recycler_avail_lan);
        recyclerAvailLan.setLayoutManager(new LinearLayoutManager(this));
        recyclerAvailLan.setHasFixedSize(true);
        recyclerDowloadLan = findViewById(R.id.recycler_download_lan);
        recyclerDowloadLan.setLayoutManager(new LinearLayoutManager(this));
        recyclerDowloadLan.setHasFixedSize(true);
        inputLanguages = new String[]{"English", "Hindi", "French", "Malyalam", "Afrikaans", "Azerbaijan", "Indonesian",
                "Malay", "Javnese", "Sundanese", "Catalan", "Czech", "Danish", "German", "Estonian", "Spanish", "Basque", "Filipino",
                "Galician", "Croatian", "Zulu", "Icelandic", "Italian", "Swahili", "Latvian", "Lithuanian", "Hungarian", "Dutch", "Norwegian",
                "Uzbek", "Polish", "Portuguese", "Romanian", "Slovenian", "Slovak", "Finnish", "Swedish", "Vietnamese", "Turkish",
                "Greek", "Bulgarian", "Russian", "Serbian", "Ukrainian", "Georgian", "Armenian", "Hebrew", "Arabic", "Farsi", "Urdu", "Amharic", "Tamil",
                "Bengali", "Kambodia", "Kannada", "Marathi", "Gujarati", "Sinhala", "Telugu", "Nepali", "Lao", "Thai", "Burmese", "Korean", "Japanese", "Chinese","Chinese(Hong Kong)"};
        inputLanguageCodesTrans = new String[]{"en", "hi", "fr", "ml", "af", "az", "id", "ms", "jv", "su", "ca", "cs", "da", "de", "et",
                "es", "eu", "fil", "gl", "hr", "zu", "is", "it", "sw", "lv", "lt", "hu", "nl", "nb", "uz", "pl", "pt", "ro", "sl", "sk", "fi", "sv", "vi",
                "tr", "el", "bg", "ru", "sr", "uk", "ka", "hy", "he", "ar", "fa", "ur", "am", "ta", "bn", "km", "kn", "mr", "gu", "si",
                "te", "ne", "lo", "th", "my", "ko", "ja", "zh", "yue"};


        hashMapInputTrans = new HashMap<>();
        hashMapDowloadLanguage = new HashMap<>();

        listAvailLang = new ArrayList<>();
        listDowloadLang = new ArrayList<>();
        for (int i = 0; i < inputLanguages.length; i++) {
            DowloadLanguageModel dowloadLanguageModel = new DowloadLanguageModel();
            dowloadLanguageModel.setFav(false);
            dowloadLanguageModel.setProcess(false);
            dowloadLanguageModel.setLanguageName(inputLanguages[i]);
            dowloadLanguageModel.setLangaugeCode(inputLanguageCodesTrans[i]);
            listAvailLang.add(dowloadLanguageModel);
        }
        prepareOfflineList(0);
    }

    private void prepareOfflineList(final int pos) {
        final RemoteModelManager modelManager = RemoteModelManager.getInstance();

        TranslateRemoteModel model = new TranslateRemoteModel.Builder(inputLanguageCodesTrans[pos])
                .build();
        modelManager.isModelDownloaded(model).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean) {
                    DowloadLanguageModel dowloadLanguageModel = new DowloadLanguageModel();
                    dowloadLanguageModel.setFav(false);
                    dowloadLanguageModel.setProcess(false);
                    dowloadLanguageModel.setLanguageName(inputLanguages[pos]);
                    dowloadLanguageModel.setLangaugeCode(inputLanguageCodesTrans[pos]);
                    listDowloadLang.add(dowloadLanguageModel);
                    for (int i = 0; i < listAvailLang.size(); i++) {
                        DowloadLanguageModel dowloadLanguageModel1 = listAvailLang.get(i);
                        if (dowloadLanguageModel1.getLanguageName().equals(inputLanguages[pos])) {
                            listAvailLang.remove(i);
                        }
                    }

                }
                if ((pos + 1) < inputLanguageCodesTrans.length)
                    prepareOfflineList(pos + 1);
                else {
                    procee = false;
                    for (int i = 0; i < listAvailLang.size(); i++) {
                        hashMapInputTrans.put(listAvailLang.get(i).getLanguageName(), listAvailLang.get(i).getLangaugeCode());
                    }
                    for (int i = 0; i < listDowloadLang.size(); i++) {
                        hashMapDowloadLanguage.put(listDowloadLang.get(i).getLanguageName(), listDowloadLang.get(i).getLangaugeCode());
                    }
                    Collections.sort(listAvailLang, new Comparator<DowloadLanguageModel>() {
                        @Override
                        public int compare(DowloadLanguageModel dowloadLanguageModel, DowloadLanguageModel t1) {
                            return dowloadLanguageModel.getLanguageName().compareToIgnoreCase(t1.getLanguageName());
                        }
                    });
                    Collections.sort(listDowloadLang, new Comparator<DowloadLanguageModel>() {
                        @Override
                        public int compare(DowloadLanguageModel dowloadLanguageModel, DowloadLanguageModel t1) {
                            return dowloadLanguageModel.getLanguageName().compareToIgnoreCase(t1.getLanguageName());
                        }
                    });
                    getLanguage();
                    downloadAvailLanguageAdapter = new DownloadAvailLanguageAdapter(DowloadLanguage.this, listAvailLang);
                    recyclerAvailLan.setAdapter(downloadAvailLanguageAdapter);
                    downloadLanguageAdapter = new DownloadLanguageAdapter(DowloadLanguage.this, listDowloadLang);
                    recyclerDowloadLan.setAdapter(downloadLanguageAdapter);
                }
            }
        });
    }


    @Override
    public void clickDowloadLanguages(int pos) {
        hashMapDowloadLanguage.put(listAvailLang.get(pos).getLanguageName(), hashMapInputTrans.get(listAvailLang.get(pos).getLanguageName()));
        DowloadLanguageModel dowloadLanguageModel = listAvailLang.get(pos);
        dowloadLanguageModel.setProcess(true);
        listDowloadLang.add(dowloadLanguageModel);
        downloadLangaugeModelClass(listAvailLang.get(pos).getLangaugeCode(), (listDowloadLang.size() - 1));
        hashMapInputTrans.remove(listAvailLang.get(pos).getLanguageName());
        listAvailLang.remove(pos);
        downloadAvailLanguageAdapter.notifyDataSetChanged();
        downloadLanguageAdapter.notifyDataSetChanged();
    }

    @Override
    public void clickDeleteLanguages(int pos) {
        deleteModel(hashMapDowloadLanguage.get(listDowloadLang.get(pos).getLanguageName()));
        listAvailLang.add(listDowloadLang.get(pos));
        listDowloadLang.remove(pos);
        downloadAvailLanguageAdapter.notifyDataSetChanged();
        downloadLanguageAdapter.notifyDataSetChanged();
    }

    @Override
    public void clickToInsertFav(String languageName) {
        insertLanguage(languageName);
    }

    @Override
    public void clickToDeleteFav(String languageName) {
        deleteCall = true;
        for (int i = 0; i < languageModels.size(); i++) {
            if (languageName.equals(languageModels.get(i).languageName)) {
                deleteLanguage(languageModels.get(i));
            }
        }
    }

    void insertLanguage(final String languageName) {
        class Insert extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseLanguageModel databaseLanguageModel = new DatabaseLanguageModel();
                databaseLanguageModel.languageName = (languageName);
                appDatabase.langaugeDao().insert(databaseLanguageModel);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getLanguage();
            }
        }
        new Insert().execute();
    }

    void deleteLanguage(final DatabaseLanguageModel databaseLanguageModel) {
        class Insert extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                appDatabase.langaugeDao().delete(databaseLanguageModel);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getLanguage();
            }
        }
        new Insert().execute();
    }

    void getLanguage() {
        class GetLan extends AsyncTask<Void, Void, List<DatabaseLanguageModel>> {

            @Override
            protected List<DatabaseLanguageModel> doInBackground(Void... voids) {
                List<DatabaseLanguageModel> lan = appDatabase.langaugeDao().getAll();
                return lan;
            }

            @Override
            protected void onPostExecute(List<DatabaseLanguageModel> databaseLanguageModels) {
                super.onPostExecute(databaseLanguageModels);
                languageModels = databaseLanguageModels;
                int pos;
                for (int i = 0; i < languageModels.size(); i++) {
                    String languageName = languageModels.get(i).languageName;
                    for (int j = 0; j < listAvailLang.size(); j++) {
                        pos = 0;
                        DowloadLanguageModel dowloadLanguageModel = listAvailLang.get(j);
                        if (languageName.equals(dowloadLanguageModel.getLanguageName())) {
                            dowloadLanguageModel.setFav(true);
                            listAvailLang.remove(j);
                            listAvailLang.add(pos, dowloadLanguageModel);
                            pos++;
                        }
                    }
                    for (int j = 0; j < listDowloadLang.size(); j++) {
                        pos = 0;
                        DowloadLanguageModel dowloadLanguageModel = listDowloadLang.get(j);
                        if (languageName.equals(dowloadLanguageModel.getLanguageName())) {
                            dowloadLanguageModel.setFav(true);
                            listDowloadLang.remove(j);
                            listDowloadLang.add(pos, dowloadLanguageModel);
                            pos++;
                        }
                    }
                }
                if(downloadAvailLanguageAdapter!=null) {
                    downloadAvailLanguageAdapter.notifyDataSetChanged();
                    downloadLanguageAdapter.notifyDataSetChanged();
                }
            }
        }
        new GetLan().execute();
    }

    private void downloadLangaugeModelClass(String languageCode, final int pos) {
        final RemoteModelManager modelManager = RemoteModelManager.getInstance();
        DownloadConditions conditions = new DownloadConditions.Builder()
                .build();
        TranslateRemoteModel model =
                new TranslateRemoteModel.Builder(languageCode).build();

        modelManager.download(model, conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DowloadLanguageModel dowloadLanguageModel = listDowloadLang.get(pos);
                dowloadLanguageModel.setProcess(false);
                downloadLanguageAdapter.notifyDataSetChanged();
            }
        });
    }

    private void deleteModel(String languageCode) {
        final RemoteModelManager modelManager = RemoteModelManager.getInstance();
        TranslateRemoteModel model =
                new TranslateRemoteModel.Builder(languageCode).build();
        modelManager.deleteDownloadedModel(model).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(DowloadLanguage.this, "Deleted Successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }
}