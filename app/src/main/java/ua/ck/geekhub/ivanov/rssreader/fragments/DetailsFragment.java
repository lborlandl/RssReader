package ua.ck.geekhub.ivanov.rssreader.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.activities.DetailsActivity;
import ua.ck.geekhub.ivanov.rssreader.heplers.PreferenceHelper;
import ua.ck.geekhub.ivanov.rssreader.models.Feed;
import ua.ck.geekhub.ivanov.rssreader.tools.Constants;
import ua.ck.geekhub.ivanov.rssreader.heplers.DatabaseHelper;
import ua.ck.geekhub.ivanov.rssreader.tools.ListHandler;
import ua.ck.geekhub.ivanov.rssreader.views.NotifyingScrollView;
import ua.ck.geekhub.ivanov.rssreader.tools.UILImageGetter;

public class DetailsFragment extends Fragment {

    private Feed mFeed;
    private boolean mIsTableLand, mIsFavourite;
    private UiLifecycleHelper mUiHelper;
    private ActionBarActivity mActivity;
    private DatabaseHelper mDb;
    private int mPosition;
    private static final String EXTRA_POSITION = "EXTRA_POSITION";

    private SessionStatusCallback statusCallback = new SessionStatusCallback();

    public static DetailsFragment newInstance(Feed feed) {
        Bundle args = new Bundle();
        args.putParcelable(Constants.EXTRA_FEED, feed);
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DetailsFragment newInstance(Feed feed, int position) {
        Bundle args = new Bundle();
        args.putParcelable(Constants.EXTRA_FEED, feed);
        args.putInt(EXTRA_POSITION, position);
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (ActionBarActivity) getActivity();
        mIsTableLand = getResources().getBoolean(R.bool.tablet_land);
        mFeed = getArguments().getParcelable(Constants.EXTRA_FEED);
        mUiHelper = new UiLifecycleHelper(mActivity, null);
        mUiHelper.onCreate(savedInstanceState);
        mDb = DatabaseHelper.getInstance(mActivity);
        mIsFavourite = mDb.isFeed(mFeed);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!mIsTableLand) {
            mPosition = getArguments().getInt(EXTRA_POSITION, 0);

            final int actionBarHeight = mActivity.getSupportActionBar().getHeight();
            boolean isTable = getResources().getBoolean(R.bool.tablet);
            final int imageHeight = isTable ? 250 : 500;
            final int maxAlpha = 250;
            ((NotifyingScrollView) view.findViewById(R.id.scroll_view)).setOnScrollChangedListener(
                    new NotifyingScrollView.OnScrollChangedListener() {
                        @Override
                        public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
                            int headerHeight = imageHeight - actionBarHeight;
                            float ratio = (float)
                                    Math.min(Math.max(t, 0), headerHeight) / headerHeight;
                            int newAlpha = (int) (ratio * maxAlpha);
                            ((DetailsActivity) mActivity).setAlpha(newAlpha, mPosition);
                        }
                    });
        } else {
            view.findViewById(R.id.gradient).setVisibility(View.GONE);
        }

        PreferenceHelper helper = PreferenceHelper.getInstance(getActivity());
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageOnFail(R.drawable.no_image)
                .showImageForEmptyUri(R.drawable.no_image)
                .cacheOnDisk(helper.isCacheOnDisk())
                .cacheInMemory(helper.isCacheInMemory())
                .considerExifParams(true)
                .build();

        ImageView mImageViewFeed = (ImageView) view.findViewById(R.id.image_view_feed);

        final ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(mActivity.getBaseContext()));
        imageLoader.displayImage(mFeed.getImage(), mImageViewFeed, options,
                new AnimateFirstDisplayListener(view.findViewById(R.id.image_progress_bar)));

        TextView textViewTitle = (TextView) view.findViewById(R.id.text_view_title);
        textViewTitle.setText(Html.fromHtml(mFeed.getTitle()));

        TextView textViewDescription = (TextView) view.findViewById(R.id.text_view_description);
        Spanned spanned = Html.fromHtml(mFeed.getDescription(),
                new UILImageGetter(textViewDescription, mActivity), new ListHandler());
        textViewDescription.setText(spanned);

        view.findViewById(R.id.button_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mFeed.getLink()));
                startActivity(browserIntent);
            }
        });

        if (!mIsTableLand) {
            getActivity().setTitle(getResources().getString(R.string.news));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.details, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        shareActionProvider.setShareIntent(buildShareIntent());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemFavourite = menu.findItem(R.id.menu_action_favourite);
        if (mIsFavourite) {
            itemFavourite.setIcon(R.drawable.ic_star);
            itemFavourite.setTitle(R.string.remove_from_favourite);
        } else {
            itemFavourite.setIcon(R.drawable.ic_star_outline);
            itemFavourite.setTitle(R.string.add_to_favourite);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_favourite:
                if (mIsFavourite) {
                    item.setIcon(R.drawable.ic_star_outline);
                    item.setTitle(R.string.add_to_favourite);
                    mDb.deleteFeed(mFeed);
                } else {
                    item.setIcon(R.drawable.ic_star);
                    item.setTitle(R.string.remove_from_favourite);
                    mDb.addFeed(mFeed);
                }
                mIsFavourite = !mIsFavourite;
                return true;
            case R.id.menu_share_facebook:
                if (FacebookDialog.canPresentShareDialog(mActivity.getApplicationContext(),
                        FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
                    FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(mActivity)
                            .setName(Html.fromHtml(mFeed.getTitle()).toString())
                            .setLink(mFeed.getLink())
                            .setDescription(Html.fromHtml(mFeed.getDescription()).toString())
                            .setPicture(mFeed.getImage())
                            .setApplicationName(getString(R.string.app_name))
                            .build();
                    mUiHelper.trackPendingDialogCall(shareDialog.present());

                } else {
                    login();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mUiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error,
                                Bundle data) {
                Toast.makeText(mActivity,
                        String.format("Error: %s", error.toString()), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {

            }
        });
    }

    private Intent buildShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, Html.fromHtml(mFeed.getTitle()).toString());
        shareIntent.putExtra(Intent.EXTRA_TEXT, mFeed.getLink());
        return shareIntent;
    }

    private void publishFeedDialog() {
        Bundle params = new Bundle();
        params.putString("name", Html.fromHtml(mFeed.getTitle()).toString());
        params.putString("description", Html.fromHtml(mFeed.getDescription()).toString());
        params.putString("link", mFeed.getLink());
        params.putString("picture", mFeed.getImage());

        WebDialog feedDialog = new WebDialog.FeedDialogBuilder(getActivity(),
                Session.getActiveSession(), params)
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error == null) {
                            final String postId = values.getString("post_id");
                            if (postId != null) {
                                Toast.makeText(getActivity(),
                                        getString(R.string.published),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // User clicked the Cancel button
                                Toast.makeText(getActivity().getApplicationContext(),
                                        getString(R.string.cancel_published),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else if (error instanceof FacebookOperationCanceledException) {
                            // User clicked the "x" button
                            Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.cancel_published),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Generic, ex: network error
                            Toast.makeText(getActivity().getApplicationContext(),
                                    getString(R.string.error_published),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                })
                .build();
        feedDialog.show();
    }

    public void login() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(getActivity(), this, true, statusCallback);
        }
    }

    private void afterLogin() {
        publishFeedDialog();
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
//            if (exception != null) {
//                handleException(exception);
//            }
            if (state.isOpened()) {
                afterLogin();
            }
//            else if (state.isClosed()) {
//                afterLogout();
//            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mUiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mUiHelper.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mUiHelper.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUiHelper.onDestroy();
    }


    private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener {

        static final List<String> displayedImages =
                Collections.synchronizedList(new LinkedList<String>());
        View mImageProgressBar;

        AnimateFirstDisplayListener(View progressBar) {
            mImageProgressBar = progressBar;
        }

        @Override
        public void onLoadingStarted(String s, View view) {
            mImageProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String s, View view, FailReason failReason) {
            mImageProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            mImageProgressBar.setVisibility(View.GONE);
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }

        @Override
        public void onLoadingCancelled(String s, View view) {
            mImageProgressBar.setVisibility(View.GONE);
        }
    }
}
