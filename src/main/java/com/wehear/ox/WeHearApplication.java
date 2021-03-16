package com.wehear.ox;

import android.app.Application;

import com.zello.sdk.Zello;

public class WeHearApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Zello.getInstance().configure(this);
    }
}
