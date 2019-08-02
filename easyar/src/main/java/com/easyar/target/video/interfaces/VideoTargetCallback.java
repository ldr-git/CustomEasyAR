package com.easyar.target.video.interfaces;

public interface VideoTargetCallback {

    int getStorageType();

    String getTargetPath();

    String getTargetKey();

    String getTargetVideoOverlay();


    //Override @ Source Activity
    void showLoading();

    void hideLoading();

    void onMatch();

}
