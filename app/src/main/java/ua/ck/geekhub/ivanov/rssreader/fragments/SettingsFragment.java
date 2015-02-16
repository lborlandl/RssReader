package ua.ck.geekhub.ivanov.rssreader.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.services.UpdateFeedService;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_NOTIFICATION_ON = "pref_key_notification_on";
    public static final String KEY_PREF_CACHE_ON_DISK = "pref_key_cache_on_disk";
    public static final String KEY_PREF_CACHE_IN_MEMORY = "pref_key_cache_in_memory";

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
            return;
        }
        if (key.equals(KEY_PREF_CACHE_ON_DISK)) {
            CheckBoxPreference checkBox = (CheckBoxPreference) findPreference(key);
            if (!checkBox.isChecked()) {
                getLoader().clearDiskCache();
            }
            return;
        }
        if (key.equals(KEY_PREF_CACHE_IN_MEMORY)) {
            CheckBoxPreference checkBox = (CheckBoxPreference) findPreference(key);
            if (!checkBox.isChecked()) {
                getLoader().clearMemoryCache();
            }
        }
    }

    private ImageLoader getLoader() {
        ImageLoader loader = ImageLoader.getInstance();
        if (!loader.isInited()) {
            loader.init(ImageLoaderConfiguration.createDefault(getActivity().getBaseContext()));
        }
        return loader;
    }
}