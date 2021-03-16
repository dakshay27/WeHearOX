package com.wehear.ox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bugfender.android.BuildConfig;
import com.bugfender.sdk.Bugfender;

public class CustomAlertDialogPermission extends AppCompatDialogFragment {
    Activity activity;
    Dialog dialog;

    public CustomAlertDialogPermission(Activity activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.layout_dialog_permission);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Bugfender.init(activity.getApplicationContext(), "dZLz4SooWjZlWmh6fi2LPOLD4ILKtKV7", BuildConfig.DEBUG);
        Bugfender.enableCrashReporting();


        dialog.show();
        Button button_auto_start =  dialog.findViewById(R.id.auto_permission_button);
        button_auto_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(Settings.ACTION_APPLICATION_SETTINGS));
            }
        });
        ImageButton imageButton = dialog.findViewById(R.id.close_button_permission);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button button_battery_manager = dialog.findViewById(R.id.batter_manager_button);
        button_battery_manager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS));
            }
        });
        dialog.setCancelable(false);



        return dialog;
    }
}
