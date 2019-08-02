package com.easyar.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseTargetActivity extends Activity {

    public static final String TARGET_KEY = "target-key";
    public static final String TARGET_PATH = "target-path";

    @NonNull
    protected abstract ViewGroup getDisplayView();

    @NonNull
    protected abstract String getEasyARKey();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }
}
