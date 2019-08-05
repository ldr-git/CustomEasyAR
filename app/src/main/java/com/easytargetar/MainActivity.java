package com.easytargetar;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("EasyAR Sample");

        findViewById(R.id.buttonLaunchARImage)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(ImageEasyARActivity.newIntent(MainActivity.this, ImageEasyARActivity.class, FileHelper.getPath(FileHelper.Path.RESOURCE, "target/ar-target.jpg"), "target"));
                    }
                });

        findViewById(R.id.buttonLaunchARVideo)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(VideoEasyARActivity.newIntent(MainActivity.this, VideoEasyARActivity.class, FileHelper.getPath(FileHelper.Path.RESOURCE, "target/ar-target.jpg"), "target", "https://sightpvideo-cdn.sightp.com/sdkvideo/EasyARSDKShow201520.mp4"));
                    }
                });

        findViewById(R.id.buttonLaunchARVideoLocal)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(VideoEasyARActivity.newIntent(MainActivity.this, VideoEasyARActivity.class, FileHelper.getPath(FileHelper.Path.RESOURCE, "target/ar-target.jpg"), "target", "file://" + FileHelper.getPath(MainActivity.this, FileHelper.Path.RESOURCE, "overlay", "target_overlay.mp4")));
                    }
                });

    }
}
