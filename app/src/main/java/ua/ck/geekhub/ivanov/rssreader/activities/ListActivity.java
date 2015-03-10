package ua.ck.geekhub.ivanov.rssreader.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.fragments.ListFragment;

public class ListActivity extends ActionBarActivity {

    private ListFragment mFeedFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.table_content_container);
        boolean tabletLand = getResources().getBoolean(R.bool.tablet_land);
        if (fragment != null && !tabletLand) {
            fragmentManager
                    .beginTransaction()
                    .remove(fragment)
                    .commit();
        }

        if (savedInstanceState == null && mFeedFragment == null) {
            mFeedFragment = new ListFragment();
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.list_main_container, mFeedFragment)
                    .commit();
        }
    }
}
