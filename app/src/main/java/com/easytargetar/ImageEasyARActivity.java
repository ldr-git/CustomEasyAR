package com.easytargetar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.easyar.helper.StringHelper;
import com.easyar.interfaces.SnapshotCallback;
import com.easyar.target.image.ImageTargetActivity;
import com.squareup.picasso.Picasso;

import java.io.File;

import cn.easyar.StorageType;

public class ImageEasyARActivity extends ImageTargetActivity implements SnapshotCallback {

    @Override
    protected ViewGroup getDisplayView() {
        return findViewById(R.id.preview);
    }

    @Override
    protected String getEasyARKey() {
        return BuildConfig.EASY_AR_KEY;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_easy_ar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("EasyAR Sample");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (!StringHelper.isNullOrBlank(getTargetPath())) {
            Picasso.get().load("file://" + getTargetPath()).into(((ImageView) findViewById(R.id.imageViewTarget)));
        }

        initialized();

        findViewById(R.id.buttonSnapshot)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snapshot();
                    }
                });

    }

    @Override
    public void onMatch() {
        super.onMatch();

        startActivity(new Intent(this, DetailActivity.class));
        finish();

    }

    @Override
    public int getStorageType() {
        return StorageType.Absolute;
    }

    @Override
    public void snapshot(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ImageView) findViewById(R.id.imageViewTarget)).setImageBitmap(bitmap);
            }
        });
    }
}
