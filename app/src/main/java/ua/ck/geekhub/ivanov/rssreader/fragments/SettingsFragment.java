package ua.ck.geekhub.ivanov.rssreader.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.services.UpdateFeedService;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_NOTIFICATION_ON = "pref_key_notification_on";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_NOTIFICATION_ON)) {
            CheckBoxPreference notificationOn = (CheckBoxPreference) findPreference(key);
            Intent updateServiceIntent = new Intent(getActivity(), UpdateFeedService.class);
            if (notificationOn.isChecked()) {
                getActivity().startService(updateServiceIntent);
            } else {
                getActivity().stopService(updateServiceIntent);
            }
        }
    }
}