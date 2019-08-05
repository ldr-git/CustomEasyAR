//================================================================================================================================
//
// Copyright (c) 2015-2019 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package com.easyar.target.video;

import android.util.Log;

import com.easyar.target.video.interfaces.VideoTargetCallback;

import cn.easyar.DelayedCallbackScheduler;
import cn.easyar.FunctorOfVoidFromVideoStatus;
import cn.easyar.StorageType;
import cn.easyar.TextureId;
import cn.easyar.VideoPlayer;
import cn.easyar.VideoStatus;
import cn.easyar.VideoType;

public class ARVideo {

    private static final String TAG = ARVideo.class.getSimpleName();

    private VideoPlayer player;
    private boolean prepared;
    private boolean found;
    private String path;

    private VideoTargetCallback targetCallback;

    public ARVideo(VideoTargetCallback targetCallback) {
        this.targetCallback = targetCallback;
        player = new VideoPlayer();
        prepared = false;
        found = false;
    }

    public void dispose() {
        Log.d(TAG, "dispose()");
        player.close();
    }

    public void openVideoFileFromAssetsPath(String path, int texid, DelayedCallbackScheduler scheduler) {
        targetCallback.showLoading();
        this.path = path;
        player.setRenderTexture(TextureId.fromInt(texid));
        player.setVideoType(VideoType.Normal);
        player.open(path, StorageType.Assets, scheduler, new FunctorOfVoidFromVideoStatus() {
            @Override
            public void invoke(int status) {
                setVideoStatus(status);
            }
        });
    }

    public void openTransparentVideoFile(String path, int texid, DelayedCallbackScheduler scheduler) {
        targetCallback.showLoading();
        this.path = path;
        player.setRenderTexture(TextureId.fromInt(texid));
        player.setVideoType(VideoType.TransparentSideBySide);
        player.open(path, StorageType.Assets, scheduler, new FunctorOfVoidFromVideoStatus() {
            @Override
            public void invoke(int status) {
                setVideoStatus(status);
            }
        });
    }

    public void openVideoFileFromAbsolutePath(String url, int texid, DelayedCallbackScheduler scheduler) {
        Log.d(TAG, "openVideoFileFromAbsolutePath: " + url);
        targetCallback.showLoading();
        this.path = url;
        player.setRenderTexture(TextureId.fromInt(texid));
        player.setVideoType(VideoType.Normal);
        player.open(url, StorageType.Absolute, scheduler, new FunctorOfVoidFromVideoStatus() {
            @Override
            public void invoke(int status) {
                setVideoStatus(status);
            }
        });
    }

    public void setVideoStatus(int status) {
        Log.i("EAsyTargetAR", "video: " + path + " (" + Integer.toString(status) + ")");
        if (status == VideoStatus.Ready) {
            prepared = true;
            if (found) {
                targetCallback.hideLoading();
                player.play();
            }
        } else if (status == VideoStatus.Completed) {
            if (found) {
                player.play();
            }
        }
    }

    public void onFound() {
        Log.d(TAG, "onFound()");
        found = true;
        if (prepared) {
            targetCallback.hideLoading();
            player.play();
        }
    }

    public void onLost() {
        Log.d(TAG, "onLost()");
        found = false;
        if (prepared) {
            player.pause();
        }
    }

    public boolean isRenderTextureAvailable() {
        return player.isRenderTextureAvailable();
    }

    public void update() {
        Log.d(TAG, "update()");
        player.updateFrame();
    }
}
