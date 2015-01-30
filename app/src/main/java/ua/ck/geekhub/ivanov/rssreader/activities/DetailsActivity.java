package ua.ck.geekhub.ivanov.rssreader.activities;

import android.content.Intent;
import android.content.res.Configuration;
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

    private ArrayList<Feed> mFeeds;
    private int mCurrentFeed;

    private static final String FEED_SELECTED = "feed_selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        int intExtra = getIntent().getIntExtra(Constants.EXTRA_POSITION, 0);
        if (savedInstanceState != null) {
            mCurrentFeed = savedInstanceState.getInt(FEED_SELECTED, intExtra);
        } else {
            mCurrentFeed = intExtra;
        }
        mFeeds = (ArrayList<Feed>) getIntent().getSerializableExtra(Constants.EXTRA_FEEDS);
        if (mFeeds == null) {
            mFeeds = new ArrayList<Feed>();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = new ViewPager(this);
        viewPager.setId(R.id.viewPager);
//        setContentView(R.layout.activity_details);
        setContentView(viewPager);


        FragmentManager fm = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int i) {
                return DetailsFragment.newInstance(mFeeds.get(i));
            }

            @Override
            public int getCount() {
                return mFeeds.size();
            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentFeed = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(mCurrentFeed);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                && getResources().getBoolean(R.bool.tablet)) {
            Intent data = new Intent();
            data.putExtra(Constants.EXTRA_FEED, mFeeds.get(mCurrentFeed));
            setResult(Constants.REQUEST_FEED, data);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FEED_SELECTED, mCurrentFeed);
    }
}