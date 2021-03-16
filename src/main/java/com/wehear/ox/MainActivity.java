package com.wehear.ox;

import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.wehear.ox.AppModel.ViewPagerPermissionModel;
import com.zello.sdk.BluetoothAccessoryState;
import com.zello.sdk.BluetoothAccessoryType;
import com.zello.sdk.Events;
import com.zello.sdk.Tab;

import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements Events, TextToSpeech.OnInitListener {
    TextToSpeech tts;
    boolean isWantToShow = false;
    private TextView imgNext;
    ImageView imgcancel;
    private static final int MAX_STEP = 3;
    private static final String TAG = "MainActivity";
    private String currentTab = "OX";
    private CustomViewPager viewPager;
    public static BluetoothDevice mBluetoothDev;
    private LinearLayout linearFirstHeader, linearSecondHeader;
    private ImageView imgFirstHeader, imgSecondHeader;
    private TextView username, status;
    BluetoothAdapter mBluetoothAdapter;
    MainActivity.MyViewPagerAdapter myViewPagerAdapter;
    List<ViewPagerPermissionModel> pagerModels;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    private int usedWeHearOx;
    static BottomSheetDialog dialog;
    TextView txtTotalUsed, txtTotalUsedWithOX, txtTotalUsedWithOutOX, txtStatus, txtPhi;
    private String[] text = {"Allow Microphone access.", "Allow location access.", "Allow storage access."};
    private int[] image2 = {R.drawable.ic_permission_microphone, R.drawable.ic_permission_location, R.drawable.ic_permission_file};
    private String[] des = {"Wehear OX uses microphone to measure surrounding noise for PHI. It needs to use mic to take audio input in Translator and hearing aid mode.",
            "Wehear OX needs to use your location to search your available Wehear OX device around you to connect using bluetooth.",
            "Wehear OX needs access to your device storage to save your hearing patterns and PHI data."};
    String userName;
    TextView deviceName;
    CustomAlertDialogPermission dialogPermission;
    SharedPreferences sharedPreferences, sharedPreferences1;
    SharedPreferences.Editor editor, editor1;
    private static final int REQUEST_CODE_WRITE_DATA = 2;

    public static final String ACTION_BATTERY_LEVEL_CHANGED =
            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED";
    public static final String EXTRA_BATTERY_LEVEL =
            "android.bluetooth.device.extra.BATTERY_LEVEL";
    public static final int BATTERY_LEVEL_UNKNOWN = -1;
    int userInt;

    public BottomNavigationView bottomNavigationView;
    public String address;
    private static AudioManager manager;
    int hasDataPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        usedWeHearOx = sharedPreferences.getInt("isUsedWeHearOx", 0);

        if (!isMyServiceRunning(MinuteUsedService.class)) {
            Intent minuteIntent = new Intent(this, MinuteUsedService.class);
            minuteIntent.putExtra("inputExtra", "Value");
            ContextCompat.startForegroundService(this, minuteIntent);
        }
        final SharedPreferences minutes = getSharedPreferences("minutes", 0);
        final int usedMinute = minutes.getInt("mWithOx", 0);
        final int mWithoutOx = minutes.getInt("mWithoutOx", 0);
        final int mAverageVol = minutes.getInt("mAverageVol", 0);
        final int mAverageSess = minutes.getInt("mAverageSess", 0);

        username = findViewById(R.id.appbar_username);
        status = findViewById(R.id.appbar_status);
        linearFirstHeader = findViewById(R.id.linear_first_layout);
        linearSecondHeader = findViewById(R.id.linear_second_layout);
        imgFirstHeader = findViewById(R.id.img_first_layout);
        imgSecondHeader = findViewById(R.id.img_second_layout);
        txtStatus = findViewById(R.id.txt_status_toolbar);
        txtTotalUsedWithOutOX = findViewById(R.id.txt_with_out_usage_toolbar);
        txtTotalUsed = findViewById(R.id.txt_total_use_toolbar);
        txtTotalUsedWithOX = findViewById(R.id.txt_withusage_toolbar);
        txtPhi = findViewById(R.id.txt_phi_toolbar);
        sharedPreferences1 = getSharedPreferences("UserTermscondition", MODE_PRIVATE);
        editor1 = sharedPreferences1.edit();
        userInt = sharedPreferences1.getInt("terms", 0);
        int userPermission = sharedPreferences1.getInt("permission", 0);

        if (userPermission == 0) {
            editor1.putInt("permission", 1);
            editor1.apply();
            showCustomDialog();
        }
        imgFirstHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animFadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up);

                linearSecondHeader.setVisibility(View.VISIBLE);
                linearFirstHeader.setVisibility(View.GONE);
                linearSecondHeader.setAnimation(animFadeIn);
                isWantToShow = true;
                getMinuteData();

            }
        });
        imgSecondHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isWantToShow = false;
                Animation animFadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up_test);
                linearSecondHeader.setVisibility(View.GONE);
                linearFirstHeader.setVisibility(View.VISIBLE);
                linearFirstHeader.setAnimation(animFadeIn);
                updateAppBar();

            }
        });

        updateAppBar();

        if (db != null) {
            db = FirebaseFirestore.getInstance();
        }
        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {

            Task<DocumentSnapshot> doc = db.collection("Users").document(mUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (!documentSnapshot.exists()) {
                        Intent intent = new Intent(MainActivity.this, UserInfoActivity.class);
                        startActivity(intent);
                    } else {
                        if (usedWeHearOx == 0) {
                            startActivity(new Intent(getApplicationContext(), RegisteredDeviceActivity.class));
                            finish();
                        }
                    }
                }
            });


            db.collection("Users").document(mUser.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String name = documentSnapshot.getString("fname");
                    userName = name;
                    username.setText(name + "'s WeHear OX |");

                }
            });
            DocumentReference minuteRef = db.collection("Users").document(mUser.getUid());
            DocumentReference mWithoutOxRef = db.collection("Users").document(mUser.getUid());
            DocumentReference mAverageVolRef = db.collection("Users").document(mUser.getUid());
            DocumentReference mAverageSessRef = db.collection("Users").document(mUser.getUid());
            minuteRef.update("minuteUsed", usedMinute)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                        }
                    });
            mWithoutOxRef.update("mWithoutOx", mWithoutOx)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
            mAverageVolRef.update("mAverageVol", mAverageVol)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
            mAverageSessRef.update("mAverageSess", mAverageSess)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    });
            minuteRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    String Name = documentSnapshot.getString("fname");
                    Log.d(TAG, "onSuccess: " + Name);
                    db.terminate();
                }
            });
            Log.d(TAG, mUser.getUid());
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.ox);
        loadFragment(new MainFragment());
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.features: {
                        updateAppBar();
                        loadFragment(new FeaturesFragment());
                        currentTab = "FEATURES";
                        return true;
                    }

                    case R.id.ox: {
                        updateAppBar();
                        loadFragment(new MainFragment());
                        currentTab = "OX";
                        return true;

                    }


                    case R.id.phi: {
                        updateAppBar();
                        loadFragment(new PhiFragment());
                        currentTab = "PHI";
                        return true;
                    }
                    case R.id.profile: {
                        updateAppBar();
                        loadFragment(new ProfileFragment());
                        currentTab = "PROFILE";
                        return true;
                    }
                }
                return false;
            }
        });

    }


    public void loadFragment(Fragment fragment) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        Log.d(TAG, Integer.toString(manager.getBackStackEntryCount()));
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

    @Override
    public void onBackPressed() {
        BottomNavigationView mBottomNavigationView = findViewById(R.id.bottom_navigation);
        if (currentTab.equals("OX")) {
            super.onBackPressed();
            finish();
        } else {
            mBottomNavigationView.setSelectedItemId(R.id.ox);

        }

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
                if (usedWeHearOx == 0 || usedWeHearOx == -1) {
                    status.setText("Not Registered Yet");
                    txtStatus.setText(userName + " WeHear OX is\nNot Registered Yet");
                } else {
                    if (address.contains("11:11:22") || address.contains("08:A3:0B") || address.contains("04:AA:00")) {
                        txtStatus.setText(userName + " WeHear OX is\nConnected");
                        status.setText("Connected");
                    } else {
                        Bugfender.d(TAG, "Not connected to weHear");
                        txtStatus.setText(userName + " WeHear OX is\nDisconnected");
                        status.setText("Disconnected");
                    }
                }
            }
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy);
        }
    };


    void updateAppBar() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            mBluetoothAdapter.getProfileProxy(this, serviceListener, BluetoothProfile.HEADSET);
        } else {
            if (usedWeHearOx == 1) {
                Bugfender.d(TAG, "No headset Connected");
                txtStatus.setText(userName + " WeHear OX is\nDisconnected");
                status.setText("Disconnected");
            } else {
                txtStatus.setText(userName + " WeHear OX is\nNot Registered Yet");
                status.setText("Not Registered Yet");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("DestroyS", "destroy");
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
//        unregisterReceiver(mReceiver);
    }

    @Override
    public void onSelectedContactChanged() {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageStateChanged() {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAppStateChanged() {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLastContactsTabChanged(@NonNull Tab tab) {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onContactsChanged() {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAudioStateChanged() {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMicrophonePermissionNotGranted() {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBluetoothAccessoryStateChanged(@NonNull BluetoothAccessoryType bluetoothAccessoryType, @NonNull BluetoothAccessoryState bluetoothAccessoryState, @Nullable String s, @Nullable String s1) {
        Toast.makeText(this, "Called", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInit(int status) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_DATA) {
            int current = viewPager.getCurrentItem() + 1;
            if (current < MAX_STEP) {
                // move to next screen
                viewPager.setCurrentItem(current);

            } else {

                dialog.dismiss();
                if (userInt == 0) {
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                    dialog.setTitle("Custom Dialog");
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
                    cancelbtn2.setVisibility(View.GONE);
                    TextView txtIHaveYour = dialog.findViewById(R.id.txt_alread_have_device);
                    txtIHaveYour.setVisibility(View.GONE);
                    dialog.setCancelable(false);
                    TextView txtDes = dialog.findViewById(R.id.tv_desc2);
                    txtDes.setText(Html.fromHtml("I accept <font color=#ef135b>Terms & Condition!!</font> of using Wehear OX"));
                    txtDes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri = Uri.parse("http://www.wehear.in/privacy-policy/");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                    TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                    txtGetYour.setText("I Accept");
                    txtGetYour.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editor1.putInt("terms", 1);
                            editor1.apply();
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        }
//        switch (requestCode) {
//            case REQUEST_CODE_WRITE_DATA:
//                if (grantResults.length > 0) {
//                    boolean writeExternal = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                    boolean recordAudio = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//                    boolean modifyAudioSetting = grantResults[2] == PackageManager.PERMISSION_GRANTED;
//                    boolean readContacts = grantResults[3] == PackageManager.PERMISSION_GRANTED;
//                    boolean readPhoneState = grantResults[4] == PackageManager.PERMISSION_GRANTED;
//                    boolean readCallLogs = grantResults[5] == PackageManager.PERMISSION_GRANTED;
//                    if (writeExternal && recordAudio && modifyAudioSetting && readContacts && readPhoneState && readCallLogs) {
//                         
//                    } else {
//                        checkSelfPermission();
//                    }
//                }
//                break;
//        }
    }

    public String getContactDisplayNameByNumber(String number) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        String name = "Incoming call from";

        ContentResolver contentResolver = getContentResolver();
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

    private void checkSelfPermission() {
        hasDataPermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(this, RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(this, MODIFY_AUDIO_SETTINGS)
                + ContextCompat.checkSelfPermission(this, READ_CONTACTS)
                + ContextCompat.checkSelfPermission(this, READ_PHONE_STATE)
                + ContextCompat.checkSelfPermission(this, READ_CALL_LOG);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS, READ_CONTACTS, READ_PHONE_STATE, READ_CALL_LOG}, REQUEST_CODE_WRITE_DATA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO, MODIFY_AUDIO_SETTINGS, READ_CONTACTS, READ_PHONE_STATE, READ_CALL_LOG}, REQUEST_CODE_WRITE_DATA);
            }
        }

    }

    private void checkPermissonSelf(String permission) {
        hasDataPermission = ContextCompat.checkSelfPermission(this, permission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{permission}, REQUEST_CODE_WRITE_DATA);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE_WRITE_DATA);
            }
        } else {
            int current = viewPager.getCurrentItem() + 1;
            if (current < MAX_STEP) {
                viewPager.setCurrentItem(current);
            } else {
                dialog.dismiss();
                if (userInt == 0) {
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                    dialog.setTitle("Custom Dialog");
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
                    cancelbtn2.setVisibility(View.GONE);
                    dialog.setCancelable(false);
                    TextView txtDes = dialog.findViewById(R.id.tv_desc2);
                    txtDes.setText(Html.fromHtml("I accept WeHearOx <font color=#ef135b>Terms & Condition!!</font>"));
                    txtDes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Uri uri = Uri.parse("http://www.wehear.in/privacy-policy/");
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                        }
                    });
                    TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                    txtGetYour.setText("I Accept");
                    txtGetYour.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editor1.putInt("terms", 1);
                            editor1.apply();
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        }
    }


    private void showCustomDialog() {
        dialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
        View bottomView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_view_pager_permission, (LinearLayout) findViewById(R.id.linear_bottom_sheet));
        viewPager = (CustomViewPager) bottomView.findViewById(R.id.view_pager_permission);
        viewPager.disableScroll(true);
        imgNext = (TextView) bottomView.findViewById(R.id.btn_next_permission);
        imgcancel = (ImageView) bottomView.findViewById(R.id.img_cancel);
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
//        bottomProgressDots(0);
//        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        imgNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int now = viewPager.getCurrentItem();
                if (now == 0) {
                    checkPermissonSelf(RECORD_AUDIO);
                    if(ContextCompat.checkSelfPermission(MainActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED )
                    {
                        final SharedPreferences permission = getSharedPreferences("permission", 0);

                    }
                } else if (now == 1) {
                    checkPermissonSelf(ACCESS_FINE_LOCATION);
                } else if (now == 2) {
                    checkPermissonSelf(WRITE_EXTERNAL_STORAGE);
                }
            }
        });
        imgcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < MAX_STEP) {
                    viewPager.setCurrentItem(current);
                } else {
                    dialog.dismiss();
                }
            }
        });
        dialog.setContentView(bottomView);
        dialog.setCancelable(false);
        dialog.show();
    }

//    @Override
//    public void onPageScrolled(int now, float positionOffset, int positionOffsetPixels) {
//
//
//    }
//
//    @Override
//    public void onPageSelected(int now) {
//
//        if (now == 1) {
//            checkPermissonSelf(RECORD_AUDIO);
//        } else if (now == 2) {
//            checkPermissonSelf(ACCESS_FINE_LOCATION);
//        } else if (now == 3) {
//            checkPermissonSelf(READ_CONTACTS);
//        }
////        } else if (now == 3) {
////            checkPermissonSelf(WRITE_EXTERNAL_STORAGE);
////        }
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int state) {
//
//    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.custom_view_pager_permission_intrusction, container, false);
            ((TextView) view.findViewById(R.id.txt_permission_name)).setText(text[position]);
            ((ImageView) view.findViewById(R.id.img_permission_pic)).setImageResource(image2[position]);
            ((TextView) view.findViewById(R.id.txt_permission_dis)).setText(des[position]);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return text.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    //    private void bottomProgressDots(int current_index) {
//        LinearLayout dotsLayout = (LinearLayout) dialog.findViewById(R.id.layoutDots);
//        ImageView[] dots = new ImageView[MAX_STEP];
//
//        dotsLayout.removeAllViews();
//        for (int i = 0; i < dots.length; i++) {
//            dots[i] = new ImageView(this);
//            int width_height = 20;
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width_height, width_height);
//            //params.setMargins(5, 5, 5, 5);
//            params.setMargins(5, 0, 5, 0);
//            dots[i].setLayoutParams(params);
//            dots[i].setImageResource(R.drawable.unselected);
//            dotsLayout.addView(dots[i]);
//
//        }
//
//        if (dots.length > 0) {
//            dots[current_index].setImageResource(R.drawable.selected);
//        }
//    }
    public void getMinuteData() {
        final SharedPreferences minutes = getSharedPreferences("minutes", 0);
        SharedPreferences phiMinutes = getSharedPreferences("PHIData", 0);
        int minutesUsed = minutes.getInt("mWithOx", 0);
        int minuteUsedwithoutOx = minutes.getInt("mWithoutOx", 0);
        int userPhi = phiMinutes.getInt("PHI", 0);
        txtTotalUsed.setText("Today's use is " + returnHourToMinutes(minutesUsed + minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minutesUsed + minuteUsedwithoutOx) + "m");
        txtTotalUsedWithOX.setText(returnHourToMinutes(minutesUsed) + "h " + returnMinutesToRemains(minutesUsed) + "m");
        txtTotalUsedWithOutOX.setText(returnHourToMinutes(minuteUsedwithoutOx) + "h " + returnMinutesToRemains(minuteUsedwithoutOx) + "m");
        txtPhi.setText(String.valueOf(userPhi));
        updateAppBar();
    }

    private int returnHourToMinutes(int minutes) {
        return minutes / 60;
    }

    private int returnMinutesToRemains(int minutes) {
        return minutes % 60;
    }
}
