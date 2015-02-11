package ua.ck.geekhub.ivanov.rssreader.activities;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import ua.ck.geekhub.ivanov.rssreader.R;

public class SettingsActivity extends ActionBarActivity {
    private ListFragment mSettingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setBackgroundDrawable(getResources().
                getDrawable(R.drawable.ab_solid_toolbarstyle_list));
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintColor(getResources().getColor(R.color.action_bar_blue));

        mSettingsFragment = new ListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.list_main_container, mSettingsFragment)
                .commit();
    }
}
