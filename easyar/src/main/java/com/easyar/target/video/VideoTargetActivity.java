package com.easyar.target.video;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.easyar.base.BaseTargetActivity;
import com.easyar.helper.BundleHelper;
import com.easyar.target.video.interfaces.VideoTargetCallback;

import java.util.HashMap;

import cn.easyar.Engine;

public abstract class VideoTargetActivity extends BaseTargetActivity implements VideoTargetCallback {

    public static final String TARGET_VIDEO_OVERLAY = "target-video-overlay";

    private static final String TAG = VideoTargetActivity.class.getSimpleName();

    private VideoGLView glView;

    public static <C extends VideoTargetActivity> Intent newIntent(Context context, Class<C> cls, String targetPath, String targetKey, String targetVideoOverlay) {
        Intent newIntent = new Intent(context, cls);
        newIntent.putExtras(new BundleHelper.Builder()
                .putExtra(TARGET_PATH, targetPath)
                .putExtra(TARGET_KEY, targetKey)
                .putExtra(TARGET_VIDEO_OVERLAY, targetVideoOverlay)
                .get());
        return newIntent;
    }

    public static <C extends VideoTargetActivity> Intent newIntent(Context context, Class<C> cls, Bundle extras) {
        Intent newIntent = new Intent(context, cls);
        newIntent.putExtras(extras);
        return newIntent;
    }

    protected void initialized() {
        if (getDisplayView() == null) {
            throw new IllegalStateException("Display view cannot be null!");
        }
        if (getEasyARKey() == null) {
            throw new IllegalStateException("EasyARKey cannot be null!");
        }

        if (!Engine.initialize(this, getEasyARKey())) {
            Log.e("EasyTargetAR", "Initialization Failed.");
            Toast.makeText(this, Engine.errorMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        glView = new VideoGLView(this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                getDisplayView().addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void onFailure() {

            }
        });
    }

    private interface PermissionCallback {
        void onSuccess();

        void onFailure();
    }

    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;

    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int requestCode = permissionRequestCodeSerial;
                permissionRequestCodeSerial += 1;
                permissionCallbacks.put(requestCode, callback);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                callback.onSuccess();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissionCallbacks.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbacks.get(requestCode);
            permissionCallbacks.remove(requestCode);
            boolean executed = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true;
                    callback.onFailure();
                }
            }
            if (!executed) {
                callback.onSuccess();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glView != null) {
            glView.onResume();
        }
    }

    @Override
    protected void onPause() {
        if (glView != null) {
            glView.onPause();
        }
        super.onPause();
    }

    @Override
    public String getTargetPath() {
        return getIntent().getStringExtra(TARGET_PATH);
    }

    @Override
    public String getTargetKey() {
        return getIntent().getStringExtra(TARGET_KEY);
    }

    @Override
    public String getTargetVideoOverlay() {
        return getIntent().getStringExtra(TARGET_VIDEO_OVERLAY);
    }

    @Override
    public void onMatch() {
        Log.d(TAG, "onMatch()");
    }

}
