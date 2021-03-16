package com.wehear.ox;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.ChipDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.triggertrap.seekarc.SeekArc;

import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainFragment extends Fragment {

        private static final String TAG = "MainActivity";



    public static BluetoothDevice mBluetoothDev;

    private TextView outdoortv,equalizertv;

    private ImageView outdoorimg,equalizerimg,wehearox;
    private SeekArc volumeBar;
    private ImageView bonephones;
    private AudioManager audioManager = null;
    int outdoorState;
    LinearLayout card_outdoor,card_equilizer;


    private ChipDrawable outdoorButton;
    TextView deviceName;
    CustomAlertDialogPermission dialogPermission;

    private static final int REQUEST_CODE_WRITE_DATA = 2;

    public static final String ACTION_BATTERY_LEVEL_CHANGED =
            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";
    public static final String EXTRA_BATTERY_LEVEL =
            "android.bluetooth.device.extra.BATTERY_LEVEL";
    public static final int BATTERY_LEVEL_UNKNOWN = -1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       try {
           View view = inflater.inflate(R.layout.main_fragment, container, false);
           Bugfender.init(getContext(), "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
           Bugfender.enableCrashReporting();


           volumeBar = view.findViewById(R.id.seek_volume);
           bonephones = view.findViewById(R.id.img_bonephones);
           outdoortv = view.findViewById(R.id.tv_outdoor);
           equalizertv = view.findViewById(R.id.tv_equalizer);

           outdoorimg = view.findViewById(R.id.img_outdoor);
           equalizerimg = view.findViewById(R.id.img_equalizer);
           wehearox = view.findViewById(R.id.img_wehearox);
           audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
           card_equilizer = view.findViewById(R.id.card_eq);
           card_outdoor = view.findViewById(R.id.card_outdoor);

           final SharedPreferences outDoorPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
           final  double volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

           outdoorState = outDoorPreference.getInt("outdoor_status", 0);
           if (outdoorState == 1) {
               outdoorimg.setImageResource(R.drawable.ic_outdoor);

           } else if (outdoorState == 0) {
               outdoorimg.setImageResource(R.drawable.ic_outdoor_grey);
               audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(int)(0.8*volume), 0);

           }


           card_outdoor.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   outdoorState = outDoorPreference.getInt("outdoor_status", 0);
                   SharedPreferences.Editor editor = outDoorPreference.edit();
                   if (outdoorState == 1) {
                       outdoorimg.setImageResource(R.drawable.ic_outdoor_grey);
                       editor.putInt("outdoor_status", 0);
                       editor.apply();


                       audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(int)(0.8*volume), 0);

                   } else if (outdoorState == 0) {
                       outdoorimg.setImageResource(R.drawable.ic_outdoor);
                       editor.putInt("outdoor_status", 1);
                       editor.apply();
                       audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,(int) volume, 0);
                   }


               }
           });


           volumeBar.setTop(audioManager
                   .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
           volumeBar.setProgress(audioManager
                   .getStreamVolume(AudioManager.STREAM_MUSIC));

           volumeBar.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
               @Override
               public void onProgressChanged(SeekArc seekArc, int i, boolean b) {
                   Log.d(TAG, "onProgressChanged: " + i);
                   audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
               }

               @Override
               public void onStartTrackingTouch(SeekArc seekArc) {

               }

               @Override
               public void onStopTrackingTouch(SeekArc seekArc) {

               }
           });

           bonephones.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if (audioManager.isMusicActive()) {

                       Intent i = new Intent("com.android.music.musicservicecommand");

                       i.putExtra("command", "pause");
                       getContext().sendBroadcast(i);
                   } else {
                       Intent i = new Intent("com.android.music.musicservicecommand");

                       i.putExtra("command", "play");
                       getContext().sendBroadcast(i);
                   }
               }
           });

           card_equilizer.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   startActivity(new Intent(getContext(), EqActivity.class));
               }
           });
//
           return view;
       }catch (Exception e)
       {
           Log.e(TAG, "onCreateView", e);
           Bugfender.d(TAG,e.toString());
           throw e;

       }
    }
}
