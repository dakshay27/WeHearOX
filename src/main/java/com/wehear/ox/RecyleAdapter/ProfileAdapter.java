package com.wehear.ox.RecyleAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bugfender.sdk.Bugfender;
import com.wehear.ox.AvailableDevicesActivity;
import com.wehear.ox.ManagePermissionBackground;
import com.wehear.ox.ProfileSettingMyWehearDetails;
import com.wehear.ox.R;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileAdapterViewHolder> {
    private Activity activity;
    private List<ProfileAdapterModel> profileAdapterModels;
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean deviceConnect;
    private static final String TAG = "ProfileAdapter";
    public String address;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private int usedWeHearOx;

    public ProfileAdapter(Activity activity, List<ProfileAdapterModel> profileAdapterModels) {
        sharedPreferences = activity.getSharedPreferences("UserDetails", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        usedWeHearOx = sharedPreferences.getInt("isUsedWeHearOx", 0);
        this.activity = activity;
        this.profileAdapterModels = profileAdapterModels;
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED) {
            mBluetoothAdapter.getProfileProxy(activity, serviceListener, BluetoothProfile.HEADSET);
        } else {
            Bugfender.d(TAG, "No headset Connected");
            deviceConnect = false;
        }
    }

    @NonNull
    @Override
    public ProfileAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_layout_profile_setting, parent, false);
        return new ProfileAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProfileAdapterViewHolder holder, int position) {
        final Uri[] uri = new Uri[1];
        final Intent[] intent = new Intent[1];
        ProfileAdapterModel profileAdapterModel = profileAdapterModels.get(position);
        holder.txtHeading.setText(profileAdapterModel.getTxtHeading());
        holder.imgProfilePic.setImageResource(profileAdapterModel.getImgIcon());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (holder.txtHeading.getText().toString()) {
                    case "Register new Wehear OX":
                        activity.startActivity(new Intent(activity, AvailableDevicesActivity.class));
                        break;
                    case "Run in background":
                        activity.startActivity(new Intent(activity, ManagePermissionBackground.class));
                        break;
                    case "Privacy Policy":
                        uri[0] = Uri.parse("http://www.wehear.in/privacy-policy/");
                        intent[0] = new Intent(Intent.ACTION_VIEW, uri[0]);
                        activity.startActivity(intent[0]);
                        break;
                    case "Product Warranty":
                        uri[0] = Uri.parse("http://www.wehear.in/claim-your-warranty/");
                        intent[0] = new Intent(Intent.ACTION_VIEW, uri[0]);
                        activity.startActivity(intent[0]);
                        break;
                    case "Help":
                        uri[0] = Uri.parse("https://www.wehear.in/contacts/");
                        intent[0] = new Intent(Intent.ACTION_VIEW, uri[0]);
                        activity.startActivity(intent[0]);
                        break;
                    case "My Wehear OX":

                        if (usedWeHearOx == 1) {
//                            if (deviceConnect)
                                activity.startActivity(new Intent(activity, ProfileSettingMyWehearDetails.class));
//                            else {
//                                final Dialog dialog = new Dialog(activity);
//                                dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
//                                dialog.setTitle("Custom Dialog");
//                                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//                                TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
//                                TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
//                                TextView txtIHaveYour = dialog.findViewById(R.id.txt_alread_have_device);
//                                txtIHaveYour.setVisibility(View.GONE);
//                                txtGetYour.setText("Connect Device");
//                                TextView txtDec = dialog.findViewById(R.id.tv_desc2);
//                                txtDec.setText("Connect Your Wehear OX device to use this feature");
//                                txtGetYour.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        Intent intentOpenBluetoothSettings = new Intent();
//                                        intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
//                                        activity.startActivity(intentOpenBluetoothSettings);
//                                    }
//                                });
//                                cancelbtn2.setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        dialog.dismiss();
//                                    }
//                                });
//                                dialog.show();
//                            }
                        } else {
                            final Dialog dialog = new Dialog(activity);
                            dialog.setContentView(R.layout.custom_no_we_hear_device_dialog);
                            dialog.setTitle("Custom Dialog");
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            TextView cancelbtn2 = dialog.findViewById(R.id.btn_cancel_dialog);
                            TextView txtGetYour = dialog.findViewById(R.id.txt_get_your_ox);
                            TextView txtIHaveYour = dialog.findViewById(R.id.txt_alread_have_device);
                            txtIHaveYour.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    activity.startActivity(new Intent(activity, AvailableDevicesActivity.class));
                                }
                            });
                            txtGetYour.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Uri uri = Uri.parse("https://www.wehear.in/");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    activity.startActivity(intent);
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
                        break;
                    case "About":
                        Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.setData(Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivity(i);
                        break;
                    case "Check For Update":
                        uri[0] = Uri.parse("https://play.google.com/store/apps/details?id=com.wehear.ox");
                        intent[0] = new Intent(Intent.ACTION_VIEW, uri[0]);
                        activity.startActivity(intent[0]);
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return profileAdapterModels.size();
    }

    class ProfileAdapterViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfilePic, imgToNewScreen;
        private TextView txtHeading;

        public ProfileAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfilePic = itemView.findViewById(R.id.imageView);
            imgToNewScreen = itemView.findViewById(R.id.imageView_forward);
            txtHeading = itemView.findViewById(R.id.tv_name);
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
                deviceConnect = address.contains("11:11:22") || address.contains("08:A3:0B") || address.contains("04:AA:00");
                Log.d("Called","Called");
                if (deviceConnect) {
                    editor.putString("isUsedWeHearOxMac", address);
                    editor.putString("isUsedWeHearOxName", device.getName());
                    editor.apply();
                }
            }
            BluetoothAdapter.getDefaultAdapter().closeProfileProxy(profile, proxy);
        }
    };

}
