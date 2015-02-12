package ua.ck.geekhub.ivanov.rssreader.activities;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import ua.ck.geekhub.ivanov.rssreader.R;

public class SettingsActivity extends ActionBarActivity {
    private ListFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);

        mSettingsFragment = new ListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.list_main_container, mSettingsFragment)
                .commit();
    }
}
