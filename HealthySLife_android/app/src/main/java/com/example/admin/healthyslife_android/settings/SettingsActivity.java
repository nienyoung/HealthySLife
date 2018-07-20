package com.example.admin.healthyslife_android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.example.admin.healthyslife_android.R;
import com.example.admin.healthyslife_android.fragment.SettingsFragment;

public class SettingsActivity extends Activity {

    public static SettingsActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        instance = this;

    }

    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.leftin, R.anim.leftout);
    }
}