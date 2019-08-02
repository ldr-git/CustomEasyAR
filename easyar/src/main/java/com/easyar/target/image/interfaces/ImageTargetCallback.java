package com.easyar.target.image.interfaces;

public interface ImageTargetCallback {

    int getStorageType();

    String getTargetPath();

    String getTargetKey();


    //Override @ Source Activity
    void onMatch();

}
