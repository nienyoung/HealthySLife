package com.example.admin.healthyslife_android.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.example.admin.healthyslife_android.R;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends PreferenceFragment {

    private EditTextPreference userHeight;
    private EditTextPreference userWeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.pref_settings);
        getPreferenceManager().setSharedPreferencesName("mySettings");

        userHeight = (EditTextPreference) findPreference("pref_key_user_height");
        userWeight = (EditTextPreference) findPreference("pref_key_user_weight");

        //若当前数据不为零则直接显示
        SharedPreferences settings = getActivity().getSharedPreferences("mySettings", MODE_PRIVATE);
        String nowHeight = settings.getString("pref_key_user_height", "0");
        String nowWeight = settings.getString("pref_key_user_weight", "0");
        if(!nowHeight.equals("0")){
            userHeight.setSummary(nowHeight);
        }
        if(!nowWeight.equals("0")){
            userWeight.setSummary(nowWeight);
        }

        //显示用户输入
        userHeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (preference.getKey().equals("pref_key_user_height") && (o.toString()!=null)) {
                    userHeight.setSummary(o.toString());
                } else if (preference.getKey().equals("pref_key_user_weight") && (o.toString()!=null)) {
                    userWeight.setSummary(o.toString());
                }else{
                    return false;
                }
                return true;
            }
        });
        userWeight.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (preference.getKey().equals("pref_key_user_height")) {
                    userHeight.setSummary(o.toString());
                } else if (preference.getKey().equals("pref_key_user_weight")) {
                    userWeight.setSummary(o.toString());
                }else{
                    return false;
                }
                return true;
            }
        });
    }

}