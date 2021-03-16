package com.wehear.ox;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bugfender.sdk.Bugfender;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.BLUETOOTH_ADMIN;

public class AvailableDevicesActivity extends AppCompatActivity {
    int hasDataPermission;

    private static final int REQUEST_CODE_WRITE_DATA = 2;
    LinearLayout linearDeviceRegister, linearChooseDevice;
    ImageView imgDeviceConnect, imgDeviceRegister, imgDeviceRegisterCompleted;
    private static final String TAG = "AvailableDeviceActivity";
    private static final int MAX_STEP = 3;
    AvailableDevicesActivity.MyViewPagerAdapter myViewPagerAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ListView mListView;
    private int usedWeHearOx;
    Button btnOxisnotinthelist;
    Button btnNext;
    SwipeRefreshLayout.OnRefreshListener refreshListener;
    CountDownTimer countDownTimer;
    private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    public static BluetoothDevice device;

    private final static int REQUEST_ENABLE_BT = 1;
    List<BluetoothDevice> bluetoothDevices;

    ListAdapter adapter;
    public static final UUID SPP_UUID = UUID.fromString("00001105-0000-1000-8000-00805F9B34FB");
    public static String deviceName, macAddress, name, mac;

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private Context context = this;

    private ListView scanList;
    private ScanListAdapter mAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFirebaseFirestore;
    FirebaseFirestore db;
    private DocumentReference mDataRef, mDeviceTable;
    ViewPager viewPager;
    ArrayList<bluetoothList> arrayOfDevices;
    static Dialog dialog;
    private String[] text = {"Press power button for 3 sec", "Red and Blue light starts blinking rapidly", "Tap on your device to connect"};
    private int[] image2 = {R.drawable.on_image, R.drawable.power_image, R.drawable.third_image};
    private int[] image3 = {R.drawable.ic_sec3, R.drawable.ic_tick, R.drawable.white_partition};
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_devices);
        animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        animation.setStartOffset(20);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        assert mUser != null;
        if (mUser.getUid() != null) {
            Log.d(TAG, "onCreate: " + mUser.getUid());
        } else {
            Log.d(TAG, "onCreate: null");
        }
        mFirebaseFirestore = FirebaseFirestore.getInstance();
        Bugfender.init(this, "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();
        findViewById();
        mDataRef = mFirebaseFirestore.collection("Users").document(mUser.getUid());

        arrayOfDevices = new ArrayList<bluetoothList>();
        mAdapter = new ScanListAdapter(AvailableDevicesActivity.this, arrayOfDevices);
        adapter = new ListAdapter(this, arrayOfDevices);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mBluetoothAdapter.startDiscovery();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        };
        mSwipeRefreshLayout.setOnRefreshListener(refreshListener);
        btnOxisnotinthelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCustomDialog();
            }
        });


        if (!bAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


        if (bAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth Not Supported", Toast.LENGTH_SHORT).show();
        } else {
            Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
            bluetoothDevices = new ArrayList<>();
            int index = 0;
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    bluetoothDevices.add(device);
                    index++;
                    deviceName = device.getName();
                    macAddress = device.getAddress();
                    if (device.getAddress().startsWith(("11:11:22:")) || device.getAddress().contains("08:A3:0B") || device.getAddress().contains("04:AA:00")) {
                        bluetoothList newDevice = new bluetoothList(deviceName, macAddress);
                        Log.d(TAG, "onCreate: " + deviceName + " " + macAddress);

                        mAdapter.add(newDevice);
                        mAdapter.notifyDataSetChanged();
                    }
                    //Log.d(TAG, "onCreate: Name:  " + deviceName + "   Mac: " + macAddress);
                }

//        aAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.two_line_list_item, list);

                mListView.setAdapter(adapter);

            }
        }


        checkSelfPermission();

        bluetoothScanning();

        scanList.setAdapter(mAdapter);

        scanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                // if (!(isBluetoothHeadsetConnected())) {
                linearChooseDevice.setVisibility(View.GONE);
                linearDeviceRegister.setVisibility(View.VISIBLE);
                final BluetoothDevice device = bluetoothDevices.get(i);
                bluetoothList selectedItem = mAdapter.getItem(i);
                final String mac1 = selectedItem.macAddress;
                final String name = selectedItem.deviceName;

                imgDeviceConnect.startAnimation(animation);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        imgDeviceConnect.clearAnimation();
                        dotColorToPink(1);
                        imgDeviceRegister.setImageResource(R.drawable.circle_colored);
                        imgDeviceRegister.startAnimation(animation);
                        try {
                            device.createBond();
                            if (createBond(device))
                                showToast("We have pair device successfully");

                            else
                                showToast("This device is already paired otherwise some error to pairing");
                        } catch (Exception e) {
                            showToast(e.getLocalizedMessage());
                        }
                        connect(device);
                        bluetoothDevices.get(i).createBond();
                        mDataRef = mFirebaseFirestore.collection("Users").document(mUser.getUid());

                        db.collection("Devices").document(mac1).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        usedWeHearOx = 1;
                                        editor.putInt("isUsedWeHearOx", usedWeHearOx);
                                        editor.apply();
                                        final Map<String, Object> data = new HashMap<>();
                                        data.put("userOxId", mac1);
                                        mDeviceTable = mFirebaseFirestore.collection("Devices").document(mac1.trim());
                                        mDataRef
                                                .set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                data.clear();
                                                data.put("registered", true);
                                                data.put("registeredTime", ((System.currentTimeMillis()) / 1000));
                                                mDeviceTable.set(data, SetOptions.merge()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        showToast("Register Successfully");
                                                        editor.putString("isUsedWeHearOxMac", mac1);
                                                        editor.putString("isUsedWeHearOxName", device.getName());
                                                        editor.apply();
                                                        imgDeviceRegister.clearAnimation();
                                                        imgDeviceRegisterCompleted.setImageResource(R.drawable.circle_colored);
                                                        dotColorToPink(2);
                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }, 2000);

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        showToast(e.getLocalizedMessage());
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                showToast(e.getLocalizedMessage());
                                            }
                                        });
                                    } else {
                                        usedWeHearOx = -1;
                                        editor.putInt("isUsedWeHearOx", usedWeHearOx);
                                        editor.apply();
                                        final Dialog dialog = new Dialog(AvailableDevicesActivity.this);
                                        dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                                        dialog.setTitle("Custom Dialog");
                                        dialog.setCancelable(false);
                                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                                        dialog.findViewById(R.id.btn_cancel_dialog).setVisibility(View.GONE);
                                        TextView txtDec = dialog.findViewById(R.id.tv_desc2);
                                        txtDec.setText("Your Device not Authentic Device...\nPlease Contact with our techanical team!!");
                                        TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                                        txtGetYour.setText("Ok");
                                        txtGetYour.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                        dialog.show();
                                    }
                                }
                            }
                        });
                    }
                }, 2000);


//                }


            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        countDownTimer.cancel();

    }

    private void showCustomDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_share_stepper);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        viewPager = (ViewPager) dialog.findViewById(R.id.view_pager);
        btnNext = dialog.findViewById(R.id.btn_next);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;


        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);

        bottomProgressDots(0);

        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < MAX_STEP) {
                    // move to next screen
                    viewPager.setCurrentItem(current);
                } else {
                    //finish();
                    dialog.dismiss();
                }
            }
        });


        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void bottomProgressDots(int current_index) {
        LinearLayout dotsLayout = (LinearLayout) dialog.findViewById(R.id.layoutDots);
        ImageView[] dots = new ImageView[MAX_STEP];

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(this);
            //int width_height = 40;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //params.setMargins(5, 5, 5, 5);
            params.setMargins(5, 0, 5, 0);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.unselected);
            //dots[i].setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
            dotsLayout.addView(dots[i]);
            //dotsLayout.bringToFront();
        }

        if (dots.length > 0) {
            dots[current_index].setImageResource(R.drawable.selected);
            //dots[current_index].setColorFilter(getResources().getColor(R.color.pink), PorterDuff.Mode.SRC_IN);
        }
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            bottomProgressDots(position);

            if (position == text.length - 1) {
                btnNext.setText(getString(R.string.GOT_IT));
                btnNext.setBackgroundResource(R.drawable.buttongradient);
                btnNext.setTextColor(Color.WHITE);

            } else {
                btnNext.setText(getString(R.string.NEXT));
                btnNext.setBackgroundResource(R.drawable.buttongradient);
                btnNext.setTextColor(Color.WHITE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    private void showToast(String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public static class bluetoothList {
        String deviceName, macAddress;

        public bluetoothList(String deviceName, String macAddress) {
            this.deviceName = deviceName;
            this.macAddress = macAddress;
        }
    }


    void bluetoothScanning() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            //check BT permissions in manifest
            checkSelfPermission();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()) {

            //check BT permissions in manifest
            checkSelfPermission();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver2, discoverDevicesIntent);
        }
//        BroadcastReceiver mReceiver = new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent) {
//                String action = intent.getAction();
//                //if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                // Discovery has found a device. Get the BluetoothDevice
//                // object and its info from the Intent.
//
//                bluetoothDevices.add(device);
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
//
//                bluetoothList newDevice = new bluetoothList(deviceName, deviceHardwareAddress);
//
//                boolean flag = true;
//                for (int i = 0; i < mAdapter.getCount(); i++) {
//
//                    bluetoothList bluetoothList = mAdapter.getItem(i);
//                    if (bluetoothList.macAddress.equals(deviceHardwareAddress))
//                        flag = false;
//
//                }
////                if (device.getAddress().startsWith(("11:11:22:")) || device.getAddress().contains("08:A3:0B") || device.getAddress().contains("04:AA:00")) {
//                if (flag)
//                    mAdapter.add(newDevice);
////                }
//                mAdapter.notifyDataSetChanged();
//                Log.d(TAG, "Device : " + deviceName + "  MAC : " + deviceHardwareAddress);
//            }
//            //}
//        };
        countDownTimer = new CountDownTimer(5000, 50000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mSwipeRefreshLayout.setRefreshing(true);

                refreshListener.onRefresh();
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

    public boolean isBluetoothHeadsetConnected() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {

            return true;
        } else {
            return false;
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.share_stepper_2, container, false);
            ((TextView) view.findViewById(R.id.description2)).setText(text[position]);
            ((ImageView) view.findViewById(R.id.image2)).setImageResource(image2[position]);
            ((ImageView) view.findViewById(R.id.image3)).setImageResource(image3[position]);
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

    private void findViewById() {
        mListView = findViewById(R.id.list_available_devices);
        scanList = findViewById(R.id.list_available_devices);
        btnOxisnotinthelist = findViewById(R.id.btn_oxisnotinthelist);
        btnNext = findViewById(R.id.btn_next);
        linearChooseDevice = findViewById(R.id.linear_choose_device);
        linearDeviceRegister = findViewById(R.id.register_device);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeToRefresh);
        imgDeviceConnect = findViewById(R.id.img_connect_start);
        imgDeviceRegister = findViewById(R.id.img_registration_start);
        imgDeviceRegisterCompleted = findViewById(R.id.img_registration_completed);
    }

    private void dotColorToPink(int i) {
        ImageView img1 = null, img2 = null, img3 = null, img4 = null;
        if (i == 1) {
            img1 = findViewById(R.id.img_dot_1);
            img2 = findViewById(R.id.img_dot_2);
            img3 = findViewById(R.id.img_dot_3);
            img4 = findViewById(R.id.img_dot_4);
        } else if (i == 2) {
            img1 = findViewById(R.id.img_dot_5);
            img2 = findViewById(R.id.img_dot_6);
            img3 = findViewById(R.id.img_dot_7);
            img4 = findViewById(R.id.img_dot_8);
        }
        img1.setImageResource(R.drawable.circle_colored);
        img1.startAnimation(animation);
        img1.clearAnimation();
        img2.setImageResource(R.drawable.circle_colored);
        img2.startAnimation(animation);
        img2.clearAnimation();
        img3.setImageResource(R.drawable.circle_colored);
        img3.startAnimation(animation);
        img3.clearAnimation();
        img4.setImageResource(R.drawable.circle_colored);
        img4.startAnimation(animation);
        img4.clearAnimation();
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();
    }

    private Boolean connect(BluetoothDevice bdDevice) {
        Boolean bool = false;
        try {
            Log.i("Log", "service method is called ");
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(bdDevice);//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool.booleanValue();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_WRITE_DATA:
                if (grantResults.length > 0) {
                    boolean writeExternal = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean recordAudio = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (writeExternal && recordAudio) {

                    } else {
                        Toast.makeText(context, "Please accepet all condition..", Toast.LENGTH_SHORT).show();
                        checkSelfPermission();
                    }
                }
                break;
        }
    }

    private void checkSelfPermission() {
        hasDataPermission = ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                + ContextCompat.checkSelfPermission(this, BLUETOOTH_ADMIN);
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

    private BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getAddress().startsWith(("11:11:22:")) || device.getAddress().contains("08:A3:0B") || device.getAddress().contains("04:AA:00")) {
                    if (!bluetoothDevices.contains(device)) {
                        bluetoothDevices.add(device);
                        String deviceName = device.getName();
                        String deviceHardwareAddress = device.getAddress(); // MAC address
                        bluetoothList newDevice = new bluetoothList(deviceName, deviceHardwareAddress);
                        mAdapter.add(newDevice);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());

            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }
}

