package com.easytargetar;

import android.app.Application;
import android.content.Context;

import com.easyar.helper.Preferences;

public class SampleApp extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        Preferences.init(this, "SampleApp");

    }

    public static Context getContext() {
        return context;
    }

}
