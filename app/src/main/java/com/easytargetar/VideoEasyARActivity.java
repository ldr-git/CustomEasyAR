package com.easytargetar;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.easyar.target.video.VideoTargetActivity;
import com.squareup.picasso.Picasso;

import cn.easyar.StorageType;

public class VideoEasyARActivity extends VideoTargetActivity {

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
        setContentView(R.layout.activity_video_easy_ar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("EasyAR Sample");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Picasso.get().load("file://" + getTargetPath()).into(((ImageView) findViewById(R.id.imageViewTarget)));

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
    public int getStorageType() {
        return StorageType.Absolute;
    }

    @Override
    public void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onMatch() {
        super.onMatch();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Match!", Toast.LENGTH_SHORT).show();
            }
        });

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
