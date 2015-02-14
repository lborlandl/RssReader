package ua.ck.geekhub.ivanov.rssreader.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.services.UpdateFeedService;

public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.setting_container, new SettingFragment())
                    .commit();
        }
    }

    public static class SettingFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        public static final String KEY_PREF_NOTIFICATION_ON = "pref_key_notification_on";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(KEY_PREF_NOTIFICATION_ON)) {
                CheckBoxPreference notificationOn = (CheckBoxPreference) findPreference(key);
                Intent updateServiceIntent = new Intent(getActivity(), UpdateFeedService.class);
                if (notificationOn.isChecked()) {
                    getActivity().stopService(updateServiceIntent);
                } else {
                    getActivity().startService(updateServiceIntent);
                }
            }
        }
    }
}
