package ua.ck.geekhub.ivanov.rssreader.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.readystatesoftware.systembartint.SystemBarTintManager;

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
    private Drawable mActionBarBackgroundDrawable;
    private ActionBar mActionBar;
    private int[] mAlpha;
    private SystemBarTintManager mTintManager;

    public void setAlpha(int alpha) {
        setAlpha(alpha, -1);
    }

    public void setAlpha(int alpha, int position) {
        if (position != -1) {
            mAlpha[position] = alpha;
        }
        mActionBarBackgroundDrawable.setAlpha(alpha);
        float alphaForStatusBar = ((float) alpha) / 255f;
        mTintManager.setStatusBarAlpha(alphaForStatusBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTintManager = new SystemBarTintManager(this);
        mTintManager.setStatusBarTintEnabled(true);
        mTintManager.setStatusBarTintColor(getResources().getColor(R.color.action_bar_blue));

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

        mActionBarBackgroundDrawable =
                getResources().getDrawable(R.drawable.ab_solid_toolbarstyle);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        if (!isTableLand) {
            mActionBar.setBackgroundDrawable(mActionBarBackgroundDrawable);
            setAlpha(0);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FEED_SELECTED, mCurrentFeed);
        outState.putIntArray(INT_ARRAY, mAlpha);
    }
}