//================================================================================================================================
//
// Copyright (c) 2015-2019 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.easyar.target.video;

import android.opengl.GLES20;
import android.util.Log;

import com.easyar.helper.Preferences;
import com.easyar.target.BGRenderer;
import com.easyar.target.video.interfaces.VideoTargetCallback;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import cn.easyar.Buffer;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDevicePreference;
import cn.easyar.CameraDeviceSelector;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraParameters;
import cn.easyar.DelayedCallbackScheduler;
import cn.easyar.FeedbackFrameFork;
import cn.easyar.FrameFilterResult;
import cn.easyar.FunctorOfVoidFromTargetAndBool;
import cn.easyar.Image;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.ImageTrackerResult;
import cn.easyar.InputFrame;
import cn.easyar.InputFrameFork;
import cn.easyar.InputFrameThrottler;
import cn.easyar.InputFrameToFeedbackFrameAdapter;
import cn.easyar.InputFrameToOutputFrameAdapter;
import cn.easyar.Matrix44F;
import cn.easyar.OutputFrame;
import cn.easyar.OutputFrameBuffer;
import cn.easyar.OutputFrameFork;
import cn.easyar.OutputFrameJoin;
import cn.easyar.StorageType;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2F;
import cn.easyar.Vec2I;

public class EasyARVideoInitializer {

    private static final String TAG = EasyARVideoInitializer.class.getSimpleName();

    private DelayedCallbackScheduler scheduler;
    private CameraDevice camera;
    private ArrayList<ImageTracker> trackers;
    private BGRenderer bgRenderer;
    private VideoRenderer videoRenderer;
    private int tracked_target = 0;
    private int active_target = 0;
    private ARVideo video = null;

    private InputFrameThrottler throttler;
    private FeedbackFrameFork feedbackFrameFork;
    private InputFrameToOutputFrameAdapter i2OAdapter;
    private InputFrameFork inputFrameFork;
    private OutputFrameJoin join;
    private OutputFrameBuffer oFrameBuffer;
    private InputFrameToFeedbackFrameAdapter i2FAdapter;
    private OutputFrameFork outputFrameFork;
    private int previousInputFrameIndex = -1;
    private byte[] imageBuffer;

    private VideoTargetCallback targetCallback;
    private boolean match = false;

    private int cameraType = Preferences.getInt("cameraType", CameraDeviceType.Back);

    public EasyARVideoInitializer(VideoTargetCallback targetCallback) {
        this.targetCallback = targetCallback;
        scheduler = new DelayedCallbackScheduler();
        trackers = new ArrayList<ImageTracker>();
    }

    private void loadFromImageFromAssetsPath(ImageTracker tracker, String path, String name) {
        ImageTarget target = ImageTarget.createFromImageFile(path, StorageType.Assets, name, "", "", 1.0f);
        if (target == null) {
            Log.e(TAG, "target create failed or key is not correct");
            return;
        }
        tracker.loadTarget(target, scheduler, new FunctorOfVoidFromTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadFromImageFromAppPath(ImageTracker tracker, String path, String name) {
        ImageTarget target = ImageTarget.createFromImageFile(path, StorageType.App, name, "", "", 1.0f);
        if (target == null) {
            Log.e(TAG, "target create failed or key is not correct");
            return;
        }
        tracker.loadTarget(target, scheduler, new FunctorOfVoidFromTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    private void loadFromImageFromAbsolutePath(ImageTracker tracker, String path, String name) {
        ImageTarget target = ImageTarget.createFromImageFile(path, StorageType.Absolute, name, "", "", 1.0f);
        if (target == null) {
            Log.e(TAG, "target create failed or key is not correct");
            return;
        }
        tracker.loadTarget(target, scheduler, new FunctorOfVoidFromTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i(TAG, String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    public void recreate_context() {
        if (active_target != 0) {
            video.onLost();
            video.dispose();
            video = null;
            tracked_target = 0;
            active_target = 0;
        }
        if (bgRenderer != null) {
            bgRenderer.dispose();
            bgRenderer = null;
        }
        if (videoRenderer != null) {
            videoRenderer.dispose();
        }
        videoRenderer = null;
        previousInputFrameIndex = -1;
        bgRenderer = new BGRenderer();
        videoRenderer = new VideoRenderer();
    }

    public void toggleCamera() {
        Log.d(TAG, "BEFORE => toggleCamera: " + (cameraType == CameraDeviceType.Back ? "BACK" : "FRONT"));
        if (cameraType == CameraDeviceType.Back) {
            cameraType = CameraDeviceType.Front;
        } else {
            cameraType = CameraDeviceType.Back;
        }
        Log.d(TAG, "AFTER => toggleCamera: " + (cameraType == CameraDeviceType.Back ? "BACK" : "FRONT"));
        Preferences.setInt("cameraType", cameraType);
        if (camera != null) {
            camera.stop();
        }
    }

    public void initialize() {
        recreate_context();

        camera = CameraDeviceSelector.createCameraDevice(CameraDevicePreference.PreferObjectSensing);
        throttler = InputFrameThrottler.create();
        inputFrameFork = InputFrameFork.create(2);
        join = OutputFrameJoin.create(2);
        oFrameBuffer = OutputFrameBuffer.create();
        i2OAdapter = InputFrameToOutputFrameAdapter.create();
        i2FAdapter = InputFrameToFeedbackFrameAdapter.create();
        outputFrameFork = OutputFrameFork.create(2);

        boolean status = true;
        status &= camera.openWithType(CameraDeviceType.Default);
        camera.setSize(new Vec2I(1280, 720));
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        if (!status) {
            return;
        }
        ImageTracker tracker = ImageTracker.create();
        if (targetCallback != null) {
            switch (targetCallback.getStorageType()) {
                case StorageType.Absolute:
                    loadFromImageFromAbsolutePath(tracker, targetCallback.getTargetPath(), targetCallback.getTargetKey());
                    break;
                case StorageType.Assets:
                    loadFromImageFromAssetsPath(tracker, targetCallback.getTargetPath(), targetCallback.getTargetKey());
                    break;
                case StorageType.App:
                    loadFromImageFromAppPath(tracker, targetCallback.getTargetPath(), targetCallback.getTargetKey());
                    break;
                default:
                    loadFromImageFromAbsolutePath(tracker, targetCallback.getTargetPath(), targetCallback.getTargetKey());
                    break;
            }
        }
        trackers.add(tracker);
        feedbackFrameFork = FeedbackFrameFork.create(trackers.size());

        camera.inputFrameSource().connect(throttler.input());
        throttler.output().connect(inputFrameFork.input());
        inputFrameFork.output(0).connect(i2OAdapter.input());
        i2OAdapter.output().connect(join.input(0));

        inputFrameFork.output(1).connect(i2FAdapter.input());
        i2FAdapter.output().connect(feedbackFrameFork.input());
        int k = 0;
        for (ImageTracker _tracker : trackers) {
            feedbackFrameFork.output(k).connect(_tracker.feedbackFrameSink());
            _tracker.outputFrameSource().connect(join.input(k + 1));
            k++;
        }

        join.output().connect(outputFrameFork.input());
        outputFrameFork.output(0).connect(oFrameBuffer.input());
        outputFrameFork.output(1).connect(i2FAdapter.sideInput());
        oFrameBuffer.signalOutput().connect(throttler.signalInput());
    }

    public void dispose() {
        if (video != null) {
            video.dispose();
            video = null;
        }
        tracked_target = 0;
        active_target = 0;

        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        if (videoRenderer != null) {
            videoRenderer.dispose();
        }
        videoRenderer = null;
        if (bgRenderer != null) {
            bgRenderer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
        if (scheduler != null) {
            scheduler.dispose();
            scheduler = null;
        }
    }

    public boolean start() {
        boolean status = true;
        if (camera != null) {
            status &= camera.start();
        } else {
            status = false;
        }
        for (ImageTracker tracker : trackers) {
            status &= tracker.start();
        }
        return status;
    }

    public void stop() {
        if (camera != null) {
            camera.stop();
        }
        for (ImageTracker tracker : trackers) {
            tracker.stop();
        }
    }

    public void render(int width, int height, int screenRotation) {
        while (scheduler.runOne()) {
        }

        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        OutputFrame oframe = oFrameBuffer.peek();
        if (oframe == null) {
            return;
        }
        InputFrame iframe = oframe.inputFrame();
        if (iframe == null) {
            oframe.dispose();
            return;
        }
        CameraParameters cameraParameters = iframe.cameraParameters();
        if (cameraParameters == null) {
            oframe.dispose();
            iframe.dispose();
            return;
        }
        float viewport_aspect_ratio = (float) width / (float) height;
        Matrix44F imageProjection = cameraParameters.imageProjection(viewport_aspect_ratio, screenRotation, true, false);
        Image image = iframe.image();

        try {
            if (iframe.index() != previousInputFrameIndex) {
                Buffer buffer = image.buffer();
                try {
                    if ((imageBuffer == null) || (imageBuffer.length != buffer.size())) {
                        imageBuffer = new byte[buffer.size()];
                    }
                    buffer.copyToByteArray(imageBuffer);
                    bgRenderer.upload(image.format(), image.width(), image.height(), ByteBuffer.wrap(imageBuffer));
                } finally {
                    buffer.dispose();
                }
                previousInputFrameIndex = iframe.index();
            }
            bgRenderer.render(imageProjection);

            Matrix44F projectionMatrix = cameraParameters.projection(0.01f, 1000.f, viewport_aspect_ratio, screenRotation, true, false);
            for (FrameFilterResult oResult : oframe.results()) {
                if (oResult instanceof ImageTrackerResult) {
                    ImageTrackerResult result = (ImageTrackerResult) oResult;
                    ArrayList<TargetInstance> targetInstances = result.targetInstances();
                    for (TargetInstance targetInstance : targetInstances) {
                        if (targetInstance.status() == TargetStatus.Tracked) {
                            Target target = targetInstance.target();
                            int id = target.runtimeID();
                            if (active_target != 0 && active_target != id) {
                                video.onLost();
                                video.dispose();
                                video = null;
                                tracked_target = 0;
                                active_target = 0;
                            }
                            if (tracked_target == 0) {
                                if (video == null && videoRenderer != null) {
                                    if (targetCallback != null) {
                                        video = new ARVideo(targetCallback);
                                        if (targetCallback != null) {
                                            switch (targetCallback.getStorageType()) {
                                                case StorageType.Absolute:
                                                    video.openVideoFileFromAbsolutePath(targetCallback.getTargetVideoOverlay(), videoRenderer.texId(), scheduler);
                                                    break;
                                                case StorageType.Assets:
                                                    video.openVideoFileFromAssetsPath(targetCallback.getTargetVideoOverlay(), videoRenderer.texId(), scheduler);
                                                    break;
                                                default:
                                                    video.openVideoFileFromAbsolutePath(targetCallback.getTargetVideoOverlay(), videoRenderer.texId(), scheduler);
                                                    break;
                                            }
                                        }
                                    }
                                }
                                if (video != null) {
                                    video.onFound();
                                    tracked_target = id;
                                    active_target = id;
                                }
                            }
                            ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                            if (imagetarget != null) {
                                if (video != null && videoRenderer != null) {
                                    video.update();
                                    ArrayList<Image> images = ((ImageTarget) target).images();
                                    Image targetImg = images.get(0);
                                    float targetScale = imagetarget.scale();
                                    Vec2F scale = new Vec2F(targetScale, targetScale * targetImg.height() / targetImg.width());
                                    if (video.isRenderTextureAvailable()) {
                                        videoRenderer.render(projectionMatrix, targetInstance.pose(), scale);
                                    }
                                }
                            }
                            target.dispose();
                            if (targetCallback != null && !match) {
                                match = true;
                                targetCallback.onMatch();
                            }
                        }
                        targetInstance.dispose();
                    }
                    if (targetInstances.size() == 0) {
                        if (tracked_target != 0) {
                            video.onLost();
                            tracked_target = 0;
                        }
                    }
                }
                if (oResult != null) {
                    oResult.dispose();
                }
            }
        } finally {
            iframe.dispose();
            oframe.dispose();
            if (cameraParameters != null) {
                cameraParameters.dispose();
            }
            image.dispose();
        }
    }
}
