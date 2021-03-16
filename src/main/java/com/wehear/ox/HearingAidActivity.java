package com.wehear.ox;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.BassBoost;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.bugfender.sdk.Bugfender;
import com.google.firebase.BuildConfig;
import com.triggertrap.seekarc.SeekArc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import jp.kshoji.audio.receiver.AudioRouter;

import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class HearingAidActivity extends AppCompatActivity {
    static LinearLayout linearPlayPause;
    static LinearLayout linearMicChoose;
    LinearLayout linearWeHearMic;
    LinearLayout linearMobileMic;
    static AudioRouter audioRouter;
    static ImageView imgMic;
    SeekArc seekBar;
    boolean suppress = false;
    LoudnessEnhancer loudnessEnhancer;
    private static final String TAG = "HearingAidActivity";
    private static final int REQUEST_CODE_RECORD_AUDIO = 1;
    private static final int REQUEST_CODE_WRITE_DATA = 2;

    private static ImageView micButton;
    private ImageView backbtn;
    private TextView textView7, textView9, hearingtv;

    private static boolean isRecording;
    private static AudioRecord record;
    private static AudioTrack player;
    private static AudioManager manager;

    private static int recordState, playerState;
    private static int minBuffer;
    //Audio Settings
    private int source = MediaRecorder.AudioSource.MIC;
    private final int channel_in = AudioFormat.CHANNEL_IN_MONO;
    private final int channel_out = AudioFormat.CHANNEL_OUT_MONO;
    private final int format = AudioFormat.ENCODING_PCM_16BIT;

    public final String CHANNEL_ID = "12";

    private final static int REQUEST_ENABLE_BT = 1;
    private boolean IS_HEADPHONE_AVAILBLE = false;

    Context context = this;
    boolean bluetoothTalk = true;
    static boolean pressCount = true;
    boolean pressCount2 = true;
    boolean flag = true;
    static ImageButton btnNoiseSupress;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mBluetoothDevice;
    BluetoothHeadset mBluetoothHeadset;
    BluetoothProfile.ServiceListener mProfileListener;
    boolean isBluetoothConnected;
    List<BluetoothDevice> devices;
    ArrayList<BluetoothDevice> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hearing_aid);
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        btnNoiseSupress = findViewById(R.id.btn_noise_supress);
//        btnNoiseSupress.setEnabled(false);
        seekBar = findViewById(R.id.seek_loud_enhance);
        linearMicChoose = findViewById(R.id.linear_mic_choose);
        linearWeHearMic = findViewById(R.id.linear_wehear_mic_choose);
        linearMobileMic = findViewById(R.id.linear_mobile_mic_choose);
        linearPlayPause = findViewById(R.id.linear_play_pause);
        imgMic = findViewById(R.id.img_mic_hearing);
        linearPlayPause.setVisibility(View.GONE);
        linearMicChoose.setVisibility(View.VISIBLE);
        int hasDataPermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, MODIFY_AUDIO_SETTINGS);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);

        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, REQUEST_CODE_WRITE_DATA);
        }


        textView7 = findViewById(R.id.textView7);
        textView9 = findViewById(R.id.textView9);
        hearingtv = findViewById(R.id.tv_hearing);
        backbtn = findViewById(R.id.btn_back);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        micButton = findViewById(R.id.btn_mic);
        if (isMyServiceRunning(HearingAidService.class)) {
            manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            linearMicChoose.setVisibility(View.GONE);
            linearPlayPause.setVisibility(View.VISIBLE);
            micButton.setImageResource(R.drawable.ic_stop);
            pressCount = false;
            btnNoiseSupress.setEnabled(true);
            Toast.makeText(HearingAidActivity.this, "We are still Listening.", Toast.LENGTH_LONG).show();
        }
        final Intent hearingAidIntent = new Intent(context, HearingAidService.class);
        hearingAidIntent.putExtra("inputExtra", "Value");
        btnNoiseSupress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (record != null) {
                    int id = record.getAudioSessionId();
                    NoiseSuppressor noise = NoiseSuppressor.create(id);
                    noise.setEnabled(true);
                    Log.d("Noise", "Off");
                }
                if (!suppress) {
                    btnNoiseSupress.setImageResource(R.drawable.ic_noise_suppress_on);
                    suppress = true;
                } else {
                    btnNoiseSupress.setImageResource(R.drawable.ic_noise_suppress_off);
                    suppress = false;
                }
            }
        });
        micButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag = true;
                if (pressCount) {
                    Toast.makeText(HearingAidActivity.this, "Listening...", Toast.LENGTH_LONG).show();
                    micButton.setImageResource(R.drawable.ic_stop);
                    ContextCompat.startForegroundService(context, hearingAidIntent);
                    btnNoiseSupress.setEnabled(true);
                    pressCount = false;
                } else {
                    endAudio();
                    stopService(hearingAidIntent);
                    Toast.makeText(HearingAidActivity.this, "Stopped Listening", Toast.LENGTH_LONG).show();
                }
            }
        });
        seekBar.setProgress(1000);
        seekBar.setOnSeekArcChangeListener(new SeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(SeekArc seekArc, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekArc seekArc) {

            }

            @Override
            public void onStopTrackingTouch(SeekArc seekArc) {
                if (loudnessEnhancer != null) {
                    loudnessEnhancer.setTargetGain(seekArc.getProgress());
                    loudnessEnhancer.setEnabled(true);
                }
            }
        });

//        imgMic.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (bluetoothTalk) {
//                    bluetoothTalk = false;
//                    imgMic.setImageResource(R.drawable.mic);
//                } else {
//                    bluetoothTalk = true;
//                    imgMic.setImageResource(R.drawable.ic_headset_mic_white_24dp);
//                }
//
//            }
//        });
        imgMic.setVisibility(View.INVISIBLE);
        linearMobileMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothTalk = false;
                linearMicChoose.setVisibility(View.GONE);
                linearPlayPause.setVisibility(View.VISIBLE);
                imgMic.setVisibility(View.VISIBLE);
                imgMic.setImageResource(R.drawable.mic);
                initSpeakerAudio();
            }
        });
        linearWeHearMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothTalk = true;
                linearMicChoose.setVisibility(View.GONE);
                linearPlayPause.setVisibility(View.VISIBLE);
                imgMic.setVisibility(View.VISIBLE);
                imgMic.setImageResource(R.drawable.ic_headset_mic_white_24dp);
                initBluetoothAudio();
            }
        });
    }

    public void initBluetoothAudio() {
        setVolumeControlStream(AudioManager.MODE_IN_COMMUNICATION);
        Toast.makeText(context, "Bluetooth", Toast.LENGTH_SHORT).show();
        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioRouter == null)
            audioRouter = new AudioRouter(this);
        int sample_rate;//= getSampleRate();
        try {
            Method getProperty = AudioManager.class.getMethod("getProperty", String.class);
            Field bufferSizeField = AudioManager.class.getField("PROPERTY_OUTPUT_FRAMES_PER_BUFFER");
            Field sampleRateField = AudioManager.class.getField("PROPERTY_OUTPUT_SAMPLE_RATE");
            minBuffer = Integer.valueOf((String) getProperty.invoke(manager, (String) bufferSizeField.get(manager)));
            sample_rate = Integer.valueOf((String) getProperty.invoke(manager, (String) sampleRateField.get(manager)));
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | InvocationTargetException e) {
            sample_rate = getSampleRate();
            minBuffer = AudioRecord.getMinBufferSize(sample_rate, channel_in, format);
        }
        audioRouter.setRouteMode(AudioRouter.AudioRouteMode.BLUETOOTH_A2DP);
//        minBuffer = AudioRecord.getMinBufferSize(sample_rate, channel_in, format);
        manager.setBluetoothScoOn(true);
        manager.startBluetoothSco();

//        manager.setSpeakerphoneOn(false);
        manager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        manager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION, manager.getStreamMaxVolume(AudioManager.MODE_IN_COMMUNICATION), 0);
        record = new AudioRecord(source, sample_rate, channel_in, format, minBuffer);
        recordState = record.getState();
        int id = record.getAudioSessionId();
        Log.d("Record", "ID: " + id);
        playerState = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player = new AudioTrack(
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                    new AudioFormat.Builder().setEncoding(format).setSampleRate(sample_rate).setChannelMask(channel_out).build(),
                    minBuffer,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
            playerState = player.getState();
            if (AcousticEchoCanceler.isAvailable()) {
                AcousticEchoCanceler echo = AcousticEchoCanceler.create(id);
                echo.setEnabled(true);
                Log.d("Echo", "Off");
            }
            if (NoiseSuppressor.isAvailable()) {
                NoiseSuppressor noise = NoiseSuppressor.create(id);
                noise.setEnabled(true);
                Log.d("Noise", "Off");
            }
            if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl gain = AutomaticGainControl.create(id);
                gain.setEnabled(false);

                Log.d("Gain", "Off");
            }
//            BassBoost base = new BassBoost(1, player.getAudioSessionId());
//            base.setStrength((short) 1000);
            loudnessEnhancer = new LoudnessEnhancer(player.getAudioSessionId());
            loudnessEnhancer.setTargetGain(1000);
            loudnessEnhancer.setEnabled(true);
        }
        //Tests all sample rates before selecting one that works
//        int sample_rate = getSampleRate();
//        Log.d(TAG, "initAudio: "+ sample_rate);
//        minBuffer = AudioRecord.getMinBufferSize(sample_rate, channel_in, format);
//
//        Log.d(TAG, "Initializing BT");
//
//        //IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
//        //registerReceiver(mBluetoothScoReceiver, intentFilter);
//
//        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//
//        manager.setBluetoothScoOn(true);
//        //manager.startBluetoothSco();
//        manager.setSpeakerphoneOn(false);
//
//        manager.setMode(AudioManager.MODE_NORMAL);
//        manager.setStreamVolume(AudioManager.MODE_NORMAL, manager.getStreamMaxVolume(AudioManager.MODE_NORMAL),0);
//
//        //useBluetoothMic();
//        Log.d(TAG, "Can BT record from mic? " + manager.isBluetoothScoAvailableOffCall());
//
//        record = new AudioRecord(source, sample_rate, channel_in, format, minBuffer);
//        recordState = record.getState();
//        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.P) {
//            try {
//
//                Log.d(TAG, "initAudio: " + record.getActiveMicrophones());
//            } catch (Exception IOException){
//                Log.e(TAG, "initAudio: ", IOException );
//            }
//        }
//        int id = record.getAudioSessionId();
//
//        Log.d("Record", "ID: " + id);
//        playerState = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            player = new AudioTrack(
//                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
//                    new AudioFormat.Builder().setEncoding(format).setSampleRate(sample_rate).setChannelMask(channel_out).build(),
//                    minBuffer,
//                    AudioTrack.PERFORMANCE_MODE_LOW_LATENCY,
//                    AudioManager.AUDIO_SESSION_ID_GENERATE);
//            playerState = player.getState();
//
//            //MIC Gain
//            double gain = 5;
//
//            ByteBuffer data = ByteBuffer.allocateDirect(getSampleRate()).order(ByteOrder.nativeOrder());
//
//            int audioInputLengthBytes = record.read(data, getSampleRate());
//            ShortBuffer shortBuffer = data.asShortBuffer();
//            for (int i = 0; i < audioInputLengthBytes / 2; i++) { // /2 because we need the length in shorts
//                short s = shortBuffer.get(i);
//                int increased = (int) (s * gain);
//                s = (short) Math.min(Math.max(increased, Short.MIN_VALUE), Short.MAX_VALUE);
//                Log.d(TAG, "onProgressChanged: i= "+ increased);
//                shortBuffer.put(i, s);
//            }
//
//
//            // Formatting Audio
//            if (AcousticEchoCanceler.isAvailable()) {
//                AcousticEchoCanceler echo = AcousticEchoCanceler.create(id);
//                echo.setEnabled(true);
//                Log.d("Echo", "Off");
//            }
//            if (NoiseSuppressor.isAvailable()) {
//                NoiseSuppressor noise = NoiseSuppressor.create(id);
//                noise.setEnabled(true);
//                Log.d("Noise", "Off");
//            }
//            if (AutomaticGainControl.isAvailable()) {
//                AutomaticGainControl gain1 = AutomaticGainControl.create(id);
//                gain1.setEnabled(true);
//                Log.d("Gain", "Off");
//            }
//            LoudnessEnhancer loudnessEnhancer = new LoudnessEnhancer(player.getAudioSessionId());
//            loudnessEnhancer.setTargetGain(2000);
//            loudnessEnhancer.setEnabled(true);
////            BassBoost base = new BassBoost(1, player.getAudioSessionId());
////            base.setStrength((short) 1000);
//
//        }
    }

    public void initSpeakerAudio() {
        setVolumeControlStream(AudioManager.MODE_NORMAL);
        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        manager.setSpeakerphoneOn(true);
        Toast.makeText(context, "Speaker", Toast.LENGTH_SHORT).show();
        int sample_rate = getSampleRate();
        minBuffer = AudioRecord.getMinBufferSize(sample_rate, channel_in, format);
        record = new AudioRecord(source, sample_rate, channel_in, format, minBuffer);
        recordState = record.getState();
        int id = record.getAudioSessionId();
        Log.d("Record", "ID: " + id);
        playerState = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player = new AudioTrack(
                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
                    new AudioFormat.Builder().setEncoding(format).setSampleRate(sample_rate).setChannelMask(channel_out).build(),
                    minBuffer,
                    AudioTrack.MODE_STREAM,
                    AudioManager.AUDIO_SESSION_ID_GENERATE);
            playerState = player.getState();
            // Formatting Audio
            if (AcousticEchoCanceler.isAvailable()) {
                AcousticEchoCanceler echo = AcousticEchoCanceler.create(id);
                echo.setEnabled(true);
                Log.d("Echo", "Off");
            }
            if (NoiseSuppressor.isAvailable()) {
                NoiseSuppressor noise = NoiseSuppressor.create(id);
                noise.setEnabled(true);
                Log.d("Noise", "Off");
            }
            if (AutomaticGainControl.isAvailable()) {
                AutomaticGainControl gain = AutomaticGainControl.create(id);
                gain.setEnabled(false);
                Log.d("Gain", "Off");
            }
            BassBoost base = new BassBoost(1, player.getAudioSessionId());
            base.setStrength((short) 1000);
        }
//        //Tests all sample rates before selecting one that works
//        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        String sampleRateStr = manager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
//        int sample_rate = Integer.parseInt(sampleRateStr);
//        if (sample_rate == 0) sample_rate = 48000;
//        Log.d(TAG, "initAudio: " + sample_rate);
//        minBuffer = AudioRecord.getMinBufferSize(sample_rate, channel_in, format);
//
//        Log.d(TAG, "Initializing BT");
//
//        //IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
//        //registerReceiver(mBluetoothScoReceiver, intentFilter);
//
//
//        manager.setBluetoothScoOn(false);
//        //manager.startBluetoothSco();
//        manager.setSpeakerphoneOn(false);
//
//        manager.setMode(AudioManager.MODE_NORMAL);
//        manager.setStreamVolume(AudioManager.MODE_IN_COMMUNICATION, manager.getStreamMaxVolume(AudioManager.MODE_NORMAL), 0);
//
//        //useBluetoothMic();
//        Log.d(TAG, "Can BT record from mic? " + manager.isBluetoothScoAvailableOffCall());
//
//        record = new AudioRecord(source, sample_rate, channel_in, format, minBuffer);
//        recordState = record.getState();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            try {
//
//                Log.d(TAG, "initAudio: " + record.getActiveMicrophones());
//            } catch (Exception IOException) {
//                Log.e(TAG, "initAudio: ", IOException);
//            }
//        }
//        int id = record.getAudioSessionId();
//
//        Log.d("Record", "ID: " + id);
//        playerState = 0;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            player = new AudioTrack(
//                    new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
//                    new AudioFormat.Builder().setEncoding(format).setSampleRate(sample_rate).setChannelMask(channel_out).build(),
//                    minBuffer,
//                    AudioTrack.PERFORMANCE_MODE_LOW_LATENCY,
//                    AudioManager.AUDIO_SESSION_ID_GENERATE);
//            playerState = player.getState();
//
//            //MIC Gain
//            double gain = 10;
//
//            ByteBuffer data = ByteBuffer.allocateDirect(256).order(ByteOrder.nativeOrder());
//
//            int audioInputLengthBytes = record.read(data, 256);
//            ShortBuffer shortBuffer = data.asShortBuffer();
//            for (int i = 0; i < audioInputLengthBytes / 2; i++) { // /2 because we need the length in shorts
//                short s = shortBuffer.get(i);
//                int increased = (int) (s * gain);
//                s = (short) Math.min(Math.max(increased, Short.MIN_VALUE), Short.MAX_VALUE);
//                Log.d(TAG, "onProgressChanged: i= " + increased);
//                shortBuffer.put(i, s);
//            }
//
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                player = new AudioTrack(
//                        new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
//                        new AudioFormat.Builder().setEncoding(format).setSampleRate(sample_rate).setChannelMask(channel_out).build(),
//                        minBuffer,
//                        AudioTrack.MODE_STREAM,
//                        AudioManager.AUDIO_SESSION_ID_GENERATE);
//                playerState = player.getState();
//                // Formatting Audio
//                if (AcousticEchoCanceler.isAvailable()) {
//                    AcousticEchoCanceler echo = AcousticEchoCanceler.create(id);
//                    echo.setEnabled(true);
//                    Log.d("Echo", "Off");
//                }
//                if (NoiseSuppressor.isAvailable()) {
//                    NoiseSuppressor noise = NoiseSuppressor.create(id);
//                    noise.setEnabled(true);
//                    Log.d("Noise", "Off");
//                }
//                if (AutomaticGainControl.isAvailable()) {
//                    AutomaticGainControl gain1 = AutomaticGainControl.create(id);
//                    gain1.setEnabled(false);
//                    Log.d("Gain", "Off");
//                }
//                BassBoost base = new BassBoost(1, player.getAudioSessionId());
//                base.setStrength((short) 1000);
//            }
//            LoudnessEnhancer loudnessEnhancer = new LoudnessEnhancer(player.getAudioSessionId());
//            loudnessEnhancer.setTargetGain(1000);
//            loudnessEnhancer.setEnabled(true);
//            BassBoost base = new BassBoost(1, player.getAudioSessionId());
//            base.setStrength((short) 1000);
//
//        }
    }

    public static void startAudio() {
//        int read = 0, write = 0;
//        if (recordState == AudioRecord.STATE_INITIALIZED && playerState == AudioTrack.STATE_INITIALIZED) {
//            record.startRecording();
//            player.play();
//            isRecording = true;
//            Log.d("Record", "Recording...");
//        }
//        while (isRecording) {
//            short[] audioData = new short[minBuffer];
//            if (record != null) {
//                float gain = 1.5f;
//                read = record.read(audioData, 0, minBuffer);
//                if (read > 0) {
//                    for (int i = 0; i < read; ++i) {
//                        audioData[i] = (short) Math.min((int) (audioData[i] * gain), (int) Short.MAX_VALUE);
//                    }
//                }
//
//            } else
//                break;
//            Log.d("Record", "Read: " + read);
//            if (player != null)
//                write = player.write(audioData, 0, read);
//            else
//                break;
//            Log.d("Record", "Write: " + write);
//        }
        int read = 0, write = 0;
        if (recordState == AudioRecord.STATE_INITIALIZED && playerState == AudioTrack.STATE_INITIALIZED) {
            record.startRecording();
            player.play();
            isRecording = true;
            Log.d("Record", "Recording...");
        }
        while (isRecording) {
            short[] audioData = new short[minBuffer];
            if (record != null)
                read = record.read(audioData, 0, minBuffer);
            else
                break;
            Log.d("Record", "Read: " + read);
            if (player != null)
                write = player.write(audioData, 0, read);
            else
                break;
            Log.d("Record", "Write: " + write);
        }
    }

    private BroadcastReceiver mBluetoothScoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
            System.out.println("ANDROID Audio SCO state: " + state);
            if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                /*
                 * Now the connection has been established to the bluetooth device.
                 * Record audio or whatever (on another thread).With AudioRecord you can record with an object created like this:
                 * new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                 * AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
                 *
                 * After finishing, don't forget to unregister this receiver and
                 * to stop the bluetooth connection with am.stopBluetoothSco();
                 */
                unregisterReceiver(this);
            }
        }
    };

    public static void endAudio() {
        imgMic.setVisibility(View.INVISIBLE);
        micButton.setImageResource(R.drawable.ic_microphone);
        btnNoiseSupress.setEnabled(false);
        pressCount = true;
        linearPlayPause.setVisibility(View.GONE);
        linearMicChoose.setVisibility(View.VISIBLE);
        manager.setMode(AudioManager.MODE_NORMAL);
        manager.setStreamVolume(AudioManager.MODE_NORMAL, manager.getStreamMaxVolume(AudioManager.MODE_NORMAL), 0);
        if (record != null) {
            if (record.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
                record.stop();
            record.release();
            isRecording = false;
            Log.d("Record", "Stopping...");
        }
        if (player != null) {
            if (player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
                player.stop();
            player.release();
            isRecording = false;
            Log.d("Player", "Stopping...");
        }
        if (audioRouter != null) {
            audioRouter.terminate();
            audioRouter = null;
        }
    }


    public int getSampleRate() {
        //Find a sample rate that works with the device
        for (int rate : new int[]{8000, 11025, 16000, 22050, 44100, 48000}) {
            int buffer = AudioRecord.getMinBufferSize(rate, channel_in, format);
            if (buffer > 0)
                return rate;
            Log.d(TAG, "getSampleRate: " + rate);
        }
        return -1;
    }

    public void setNotification() {
        Log.d(TAG, "setNotification: Generated");

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);

        Intent stopIntent = new Intent(this, MainActivity.class);

        mBuilder.setSmallIcon(R.mipmap.ic_launcher_foreground);
        mBuilder.setContentTitle("Hearing Aid Mode On");
        mBuilder.setContentText("Your device is in the hearing aid mode and listening to your surroundings right now.");
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setOngoing(true);
        mBuilder.addAction(R.drawable.ic_stop_black_24dp, "Stop Hearing Aid Mode", pendingIntent);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Hearing aid On";
            String description = "Your device is in the hearing aid mode listening to your surroundings";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void cancleNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Hearing aid On";
            String description = "Your device is in the hearing aid mode listening to your surroundings";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.cancel(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // unregisterReceiver(mBluetoothScoReceiver);
        // Stop Bluetooth SCO.
//        if (manager != null) {

//
//        }
    }

    public void useBluetoothMic() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (mProfileListener != null) {
                Log.e("Message", "Bluetooth Headset profile was already opened, let's close it");
                mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            }

            mProfileListener = new BluetoothProfile.ServiceListener() {
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    if (profile == BluetoothProfile.HEADSET) {
                        Log.e("Message", "Found paired Bluetooth Headset");
                        Toast.makeText(HearingAidActivity.this, "Found paired Bluetooth Headset", Toast.LENGTH_SHORT).show();
                        mBluetoothHeadset = (BluetoothHeadset) proxy;
                        devices = mBluetoothHeadset.getConnectedDevices();
                        arrayList = new ArrayList<>(devices);
                        if (arrayList.size() > 0) {
                            isBluetoothConnected = true;
                            Log.e("Message", "Found connected Bluetooth Headset");
                            Toast.makeText(HearingAidActivity.this, "Found connected Bluetooth Headset", Toast.LENGTH_SHORT).show();
                            Log.e("devices", arrayList.get(0).getName());
                            Toast.makeText(HearingAidActivity.this, arrayList.get(0).getName(), Toast.LENGTH_SHORT).show();
                            boolean b = mBluetoothHeadset.startVoiceRecognition(arrayList.get(0));
                            Log.e("voice recog", String.valueOf(b));
                        }
                        //start();
                    }
                }

                public void onServiceDisconnected(int profile) {
                    Log.e("FUNCTION", "onserviceDisconnected");
                    if (profile == BluetoothProfile.HEADSET) {
                        mBluetoothHeadset = null;
                        isBluetoothConnected = false;
                        Log.e("Message", "No paired Bluetooth Headset");

                    }
                }
            };
            boolean success = mBluetoothAdapter.getProfileProxy(getApplicationContext(), mProfileListener, BluetoothProfile.HEADSET);
            if (!success) {
                Log.e("message", "[Bluetooth] getProfileProxy failed !");
            }
        } else {
            Log.e("Message", "Bluetooth disabled on device");
            Toast.makeText(this, "Bluetooth  disabled on device", Toast.LENGTH_SHORT).show();
        }


        Log.e("SEARCHING CATEGORy", String.valueOf(BluetoothProfile.HEADSET));

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}