package com.littlekai.heneikenobjdetection.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.littlekai.heneikenobjdetection.R;

import java.util.HashSet;
import java.util.Set;


public class SettingFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

    }
}
