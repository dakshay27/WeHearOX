package com.wehear.ox;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.util.zip.Inflater;

public class CustomProgressBar {
    Activity activity;
    Dialog dialog;

    public CustomProgressBar(Activity activity) {
        this.activity = activity;


    }

    public void show() {
         dialog = new Dialog(this.activity);
        dialog.setContentView(R.layout.custom_progress_dialog);

        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView imageView = dialog.findViewById(R.id.custom_progress_dialog_image);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this.activity, R.anim.fadein_fadeout);
        imageView.startAnimation(fadeInAnimation);



    }

    public void dismiss()
    {
        dialog.dismiss();
    }

}
