package com.littlekai.heneikenobjdetection.dao;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;


import com.littlekai.heneikenobjdetection.fragments.CameraFragment;
import com.littlekai.heneikenobjdetection.utils.BitmapUtilsHelper;

public class HenikenObjDetectionApplication extends Application {
    Bitmap capturedBm;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    CameraFragment camerafragment;
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }
    public static Context getAppContext() {
        return mContext;
    }
    public Bitmap getCapturedBm() {

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("resize_input", true)) {
            return BitmapUtilsHelper.Companion.resize640Bitmap(capturedBm, Integer.parseInt(sharedPreferences.getString("crop_size", "640")));

        }

            return    capturedBm;
    }

    public void setCapturedBm(Bitmap capturedBm) {
        this.capturedBm = capturedBm;
    }

    public CameraFragment getCamerafragment() {
        return camerafragment;
    }

    public void setCamerafragment(CameraFragment camerafragment) {
        this.camerafragment = camerafragment;
    }
}
