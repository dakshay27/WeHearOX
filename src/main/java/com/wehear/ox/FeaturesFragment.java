package com.wehear.ox;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
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
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.MODIFY_AUDIO_SETTINGS;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.MODE_PRIVATE;
import static com.wehear.ox.MainActivity.dialog;

public class FeaturesFragment extends Fragment {

    int hasDataPermission;
    private static final int REQUEST_CODE_WRITE_DATA = 2;

    TextView translatortv, hearingaidtv, soundmetertv, stoptimertv, paralleltv, schedulartv;
    ImageView translator_button, hearingaid_button, soundmeter_button, stoptimer_button, parallel_streaming_button, schedular_button, features_icon;
    boolean deviceConnect;
    public String address;
    String TAG = "FeaturesFragment";
    private int usedWeHearOx;
    SharedPreferences sharedPreferences, sharedPreferences1;
    SharedPreferences.Editor editor, editor1;

    private CustomViewPager viewPager;
    private TextView imgNext;
    ImageView imgcancel;
    MyViewPagerAdapter myViewPagerAdapter;
    private static final int MAX_STEP = 1;
    int userInt;

    private String[] text = {"Allow Microphone access."};
    private int[] image2 = {R.drawable.ic_permission_microphone};
    private String[] des = {"Wehear OX uses microphone to measure surrounding noise for PHI. It needs to use mic to take audio input in Translator and hearing aid mode."};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            sharedPreferences = getActivity().getSharedPreferences("UserDetails", MODE_PRIVATE);
            editor = sharedPreferences.edit();
            usedWeHearOx = sharedPreferences.getInt("isUsedWeHearOx", 0);
            View rootView = inflater.inflate(R.layout.activity_features, container, false);
            Bugfender.init(getContext(), "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
            Bugfender.enableCrashReporting();
            final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
                mBluetoothAdapter.getProfileProxy(getActivity(), serviceListener, BluetoothProfile.HEADSET);
            } else {
                Bugfender.d(TAG, "No headset Connected");
                deviceConnect = false;

            }
            translatortv = rootView.findViewById(R.id.tv_translator);
            hearingaidtv = rootView.findViewById(R.id.tv_hearing);
            soundmetertv = rootView.findViewById(R.id.tv_soundmeter);
            stoptimertv = rootView.findViewById(R.id.tv_stoptimer);
            paralleltv = rootView.findViewById(R.id.tv_parallel);
            schedulartv = rootView.findViewById(R.id.tv_schedular);
            features_icon = rootView.findViewById(R.id.ox_features_image_svg);

            translator_button = rootView.findViewById(R.id.btn_translator);
            hearingaid_button = rootView.findViewById(R.id.btn_hearingaid);
            soundmeter_button = rootView.findViewById(R.id.btn_soundmeter);
            stoptimer_button = rootView.findViewById(R.id.btn_stoptimer);
            parallel_streaming_button = rootView.findViewById(R.id.btn_parallel_streaming);
            schedular_button = rootView.findViewById(R.id.btn_schedular);

            sharedPreferences1 = getActivity().getSharedPreferences("UserTermscondition", MODE_PRIVATE);
            editor1 = sharedPreferences1.edit();
            userInt = sharedPreferences1.getInt("terms", 0);

            LayoutInflater inflaterAppBar = getLayoutInflater();
            View myView = inflaterAppBar.inflate(R.layout.activity_main, null);
            if (usedWeHearOx == -1) {
                translator_button.setImageResource(R.drawable.ic_translation_disable);
                hearingaid_button.setImageResource(R.drawable.ic_hearing_aid_disable);
            }
            translator_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
                        mBluetoothAdapter.getProfileProxy(getActivity(), serviceListener, BluetoothProfile.HEADSET);
                    } else {
                        Bugfender.d(TAG, "No headset Connected");
                        deviceConnect = false;
                    }
                    if (usedWeHearOx == 1) {
                        if (deviceConnect)
                            startActivity(new Intent(getContext(), TranslateActivity.class));
                        else {
                            final Dialog dialog = new Dialog(getContext());
                            dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                            dialog.setTitle("Custom Dialog");
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
                            TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                            TextView txtIHaveYour = dialog.findViewById(R.id.txt_alread_have_device);
                            txtIHaveYour.setVisibility(View.GONE);
                            txtGetYour.setText("Connect Device");
                            TextView txtDec = dialog.findViewById(R.id.tv_desc2);
                            txtDec.setText("Connect Your Wehear OX device to use this feature");
                            txtGetYour.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intentOpenBluetoothSettings = new Intent();
                                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                                    startActivity(intentOpenBluetoothSettings);
                                }
                            });
                            cancelbtn2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    } else {
                        final Dialog dialog = new Dialog(getContext());
                        dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                        dialog.setTitle("Custom Dialog");
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
                        TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                        TextView txtIHaveYour = dialog.findViewById(R.id.txt_alread_have_device);
                        txtIHaveYour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getActivity(), AvailableDevicesActivity.class));
                            }
                        });
                        txtGetYour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri uri = Uri.parse("https://www.wehear.in/");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                        cancelbtn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }
            });

            hearingaid_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
                        mBluetoothAdapter.getProfileProxy(getActivity(), serviceListener, BluetoothProfile.HEADSET);
                    } else {
                        Bugfender.d(TAG, "No headset Connected");
                        deviceConnect = false;
                    }
                    if (usedWeHearOx == 1) {
                        if (deviceConnect)
                            startActivity(new Intent(getContext(), HearingAidActivity.class));
                        else {
                            final Dialog dialog = new Dialog(getContext());
                            dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                            dialog.setTitle("Custom Dialog");
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
                            TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                            TextView txtIHaveYour = dialog.findViewById(R.id.txt_alread_have_device);
                            txtIHaveYour.setVisibility(View.GONE);
                            txtGetYour.setText("Connect Device");
                            TextView txtDec = dialog.findViewById(R.id.tv_desc2);
                            txtDec.setText("Connect Your Wehear OX device to use this feature");
                            txtGetYour.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intentOpenBluetoothSettings = new Intent();
                                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                                    startActivity(intentOpenBluetoothSettings);
                                }
                            });
                            cancelbtn2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    } else {
                        final Dialog dialog = new Dialog(getContext());
                        dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                        dialog.setTitle("Custom Dialog");
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
                        TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                        TextView txtIHaveYour = dialog.findViewById(R.id.txt_alread_have_device);
                        txtIHaveYour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(new Intent(getActivity(), AvailableDevicesActivity.class));
                            }
                        });
                        txtGetYour.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Uri uri = Uri.parse("https://www.wehear.in/");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);
                            }
                        });
                        cancelbtn2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    }
                }
            });

            soundmeter_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (checkSelfPermission()){
                        startActivity(new Intent(getContext(), SoundMeterActivity.class));
                    }else {
                        showCustomDialog();
                    }
                }
            });

            stoptimer_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), TimerActivity.class));
                }
            });
//            parallel_streaming_button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    startActivity(new Intent(getContext(), TranslateActivity.class));
//                }
//            });
            return rootView;
        } catch (Exception e) {
            Log.e(TAG, "onCreateView", e);
            throw e;
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
                deviceConnect = address.contains("11:11:22") || address.contains("08:A3:0B") || address.contains("04:AA:00") || address.contains("A2:F6:BB:A8:D2:DA");
                if (deviceConnect) {
                    editor.putString("isUsedWeHearOxMac", address);
                    editor.putString("isUsedWeHearOxName", device.getName());
                    editor.apply();
                }
            }
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy);
        }
    };

    private void showToast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    boolean checkSelfPermission() {

        hasDataPermission = ContextCompat.checkSelfPermission(getActivity(), WRITE_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(getActivity(), RECORD_AUDIO)
                + ContextCompat.checkSelfPermission(getActivity(), MODIFY_AUDIO_SETTINGS);
        Log.d(TAG, "onCreate: checkSelfPermission = " + hasDataPermission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onCreate: requesting permission");
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                requestPermissions(new String[]{ RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, REQUEST_CODE_WRITE_DATA);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{ RECORD_AUDIO, MODIFY_AUDIO_SETTINGS}, REQUEST_CODE_WRITE_DATA);
            }
        } else {
        }

         if(hasDataPermission == PackageManager.PERMISSION_GRANTED){
          return true;
        }
         else {
             return false;
         }
    }


    private void showCustomDialog() {
        dialog = new BottomSheetDialog(getActivity(), R.style.BottomSheetDialogTheme);
        View bottomView = LayoutInflater.from(getContext()).inflate(R.layout.custom_view_pager_permission, (LinearLayout) getActivity().findViewById(R.id.linear_bottom_sheet));
        viewPager = (CustomViewPager) bottomView.findViewById(R.id.view_pager_permission);
        viewPager.disableScroll(true);
        imgNext = (TextView) bottomView.findViewById(R.id.btn_next_permission);
        imgcancel = (ImageView) bottomView.findViewById(R.id.img_cancel);
        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
//        bottomProgressDots(0);
//        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
        imgNext.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                int now = viewPager.getCurrentItem();
                if (now == 0) {
                    checkPermissonSelf(RECORD_AUDIO);
                    dialog.dismiss();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkPermissonSelf(String permission) {
        hasDataPermission = ContextCompat.checkSelfPermission(getActivity(), permission);
        if (hasDataPermission != PackageManager.PERMISSION_GRANTED) {

            if(!getActivity().shouldShowRequestPermissionRationale(permission))
            {
               Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                intent.setData(uri);
               startActivity(intent);

            }else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{permission}, REQUEST_CODE_WRITE_DATA);
                } else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{permission}, REQUEST_CODE_WRITE_DATA);
                }
            }


        } else {
            int current = viewPager.getCurrentItem() + 1;
            if (current < MAX_STEP) {
                viewPager.setCurrentItem(current);
            } else {
                dialog.dismiss();
                if (userInt == 0) {
                    final Dialog dialog = new Dialog(getActivity());
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



    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

}

