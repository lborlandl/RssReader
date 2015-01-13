package ua.ck.geekhub.ivanov.rssreader.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.dummy.Feed;
import ua.ck.geekhub.ivanov.rssreader.fragments.DetailsFragment;
import ua.ck.geekhub.ivanov.rssreader.heplers.Constants;

public class DetailsActivity extends ActionBarActivity {

    private ArrayList<Feed> feeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = new ViewPager(this);
        viewPager.setId(R.id.viewPager);
//        setContentView(R.layout.activity_details);
        setContentView(viewPager);

        feeds = (ArrayList<Feed>) getIntent().getSerializableExtra(Constants.EXTRA_FEEDS);
        if (feeds == null) {
            feeds = new ArrayList<Feed>();
        }

        FragmentManager fm = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int i) {
                return DetailsFragment.newInstance(feeds.get(i));
            }

            @Override
            public int getCount() {
                return feeds.size();
            }
        });

        int mCurrentFeed = getIntent().getIntExtra(Constants.EXTRA_POSITION, 0);
        viewPager.setCurrentItem(mCurrentFeed);
    }
}