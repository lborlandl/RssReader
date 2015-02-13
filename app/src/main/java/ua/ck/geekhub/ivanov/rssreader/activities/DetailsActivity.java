package ua.ck.geekhub.ivanov.rssreader.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

    public static final String INT_ARRAY = "INT_ARRAY";
    private ArrayList<Feed> mFeeds;
    private int mCurrentFeed;

    private static final String FEED_SELECTED = "feed_selected";
    private Drawable mActionBarBackground;
    private int[] mAlpha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAlpha = new int[getIntent().getIntExtra(Constants.EXTRA_FEEDS_COUNT, 10)];

        int intExtra = getIntent().getIntExtra(Constants.EXTRA_POSITION, 0);
        if (savedInstanceState != null) {
            mAlpha = savedInstanceState.getIntArray(INT_ARRAY);
            mCurrentFeed = savedInstanceState.getInt(FEED_SELECTED, intExtra);
        } else {
            mCurrentFeed = intExtra;
        }
        mFeeds = getIntent().getParcelableArrayListExtra(Constants.EXTRA_FEEDS);
        if (mFeeds == null) {
            mFeeds = new ArrayList<>();
        }

        boolean isTable = getResources().getBoolean(R.bool.tablet);
        boolean isTableLand = getResources().getBoolean(R.bool.tablet_land);

        if (isTable && isTableLand) {
            Intent data = new Intent();
            data.putExtra(Constants.EXTRA_FEED, mFeeds.get(mCurrentFeed));
            setResult(Constants.REQUEST_FEED, data);
            finish();
        }

        mActionBarBackground = new ColorDrawable(getResources().getColor(R.color.color_primary));

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (!isTableLand) {
            actionBar.setBackgroundDrawable(mActionBarBackground);
            setAlpha(mAlpha[mCurrentFeed]);
        }

        final ViewPager viewPager = new ViewPager(this);
        viewPager.setId(R.id.viewPager);
        setContentView(viewPager);


        final FragmentManager fm = getSupportFragmentManager();
        viewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int i) {
                return DetailsFragment.newInstance(mFeeds.get(i), i);
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
                int left = mAlpha[position];
                int right = 0;
                if (mAlpha.length - 1 != position) {
                    right = mAlpha[position + 1];
                }

                if (positionOffset != 0.0f) {
                    int alpha = (int) (right * positionOffset + left * (1 - positionOffset));
                    setAlpha(alpha);
                }
            }

            @Override
            public void onPageSelected(int position) {
                setAlpha(mAlpha[position]);
                mCurrentFeed = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setCurrentItem(mCurrentFeed);
    }


    public void setAlpha(int alpha) {
        setAlpha(alpha, -1);
    }

    public void setAlpha(int alpha, int position) {
        if (position != -1) {
            mAlpha[position] = alpha;
        }
        mActionBarBackground.setAlpha(alpha);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FEED_SELECTED, mCurrentFeed);
        outState.putIntArray(INT_ARRAY, mAlpha);
    }
}