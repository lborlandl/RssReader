package ua.ck.geekhub.ivanov.rssreader.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;

import java.io.InputStream;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.dummy.Feed;
import ua.ck.geekhub.ivanov.rssreader.heplers.Constants;
import ua.ck.geekhub.ivanov.rssreader.heplers.DatabaseHelper;
import ua.ck.geekhub.ivanov.rssreader.task.MyTagHandler;

public class DetailsFragment extends Fragment {

    private Feed mFeed;
    private boolean mIsTable, mIsFavourite;
    private UiLifecycleHelper mUiHelper;
    private Activity mActivity;
    private DatabaseHelper mDb;
    private View mImageProgressBar;
    private ImageView mImageViewFeed;

    private SessionStatusCallback statusCallback = new SessionStatusCallback();

    public static DetailsFragment newInstance(Feed feed) {
        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_FEED, feed);
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        mUiHelper = new UiLifecycleHelper(mActivity, null);
        mUiHelper.onCreate(savedInstanceState);
        mIsTable = (mActivity.findViewById(R.id.table_content_container) != null);
        mFeed = (Feed) getArguments().getSerializable(Constants.EXTRA_FEED);
        mDb = new DatabaseHelper(mActivity, DatabaseHelper.DATABASE_NAME, null, 1);
        mIsFavourite = mDb.isFeed(mFeed);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mImageProgressBar = view.findViewById(R.id.image_progress_bar);

        Drawable mActionBarBackgroundDrawable = getResources().getDrawable(
                R.drawable.ab_solid_toolbarstyle);
//        mActionBarBackgroundDrawable.setAlpha(0);

        ActionBar actionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        actionBar.setBackgroundDrawable(mActionBarBackgroundDrawable);
        if (!mIsTable) {
            mActionBarBackgroundDrawable.setAlpha(200);
        }
//        ((NotifyingScrollView) view.findViewById(R.id.scroll_view)).setOnScrollChangedListener(
//                new NotifyingScrollView.OnScrollChangedListener() {
//                    @Override
//                    public void onScrollChanged(ScrollView who, int l, int t, int oldl, int oldt) {
//                        final int headerHeight = 500 - mActionBar.getHeight();
//                        final float ratio = (float)
//                                Math.min(Math.max(t, 0), headerHeight) / headerHeight;
//                        final int newAlpha = (int) (ratio * 255);
//                        mActionBarBackgroundDrawable.setAlpha(newAlpha);
//                    }
//                });

        mImageViewFeed = (ImageView) view.findViewById(R.id.image_view_feed);
        mImageViewFeed.setVisibility(View.GONE);
        mImageProgressBar.setVisibility(View.VISIBLE);
        new DownloadImageTask(mImageViewFeed).execute(mFeed.getImage());

        TextView textViewTitle = (TextView) view.findViewById(R.id.text_view_title);
        textViewTitle.setText(Html.fromHtml(mFeed.getTitle()));

        TextView textViewDescription = (TextView) view.findViewById(R.id.text_view_description);
        textViewDescription.setText(Html.fromHtml(mFeed.getDescription(), null, new MyTagHandler()));
//        URLImageParser p = new URLImageParser(textViewDescription, getActivity());
//        Spanned htmlSpan = Html.fromHtml(mFeed.getDescription(), p, new MyTagHandler());
//        textViewDescription.setText(htmlSpan);
        //Html.fromHtml(mFeed.getDescription(), parserDescription, new MyTagHandler()));

//        TextView textViewDate = (TextView) view.findViewById(R.id.text_view_date);
//        textViewDate.setText(Html.fromHtml(mFeed.getPubDate()));

        view.findViewById(R.id.button_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mFeed.getLink()));
                startActivity(browserIntent);
            }
        });

        if (!mIsTable) {
            getActivity().setTitle(getResources().getString(R.string.news));
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            mImageProgressBar.setVisibility(View.GONE);
            mImageViewFeed.setVisibility(View.VISIBLE);
            bmImage.setImageBitmap(result);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.details, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemFavourite = menu.findItem(R.id.menu_action_favourite);
        if (mIsFavourite) {
            itemFavourite.setIcon(android.R.drawable.btn_star_big_on);
            itemFavourite.setTitle(R.string.remove_from_favourite);
        } else {
            itemFavourite.setIcon(android.R.drawable.btn_star_big_off);
            itemFavourite.setTitle(R.string.add_to_favourite);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_favourite:
                if (mIsFavourite) {
                    item.setIcon(android.R.drawable.btn_star_big_off);
                    item.setTitle(R.string.add_to_favourite);
                    mDb.deleteFeed(mFeed);
                } else {
                    item.setIcon(android.R.drawable.btn_star_big_on);
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
}
