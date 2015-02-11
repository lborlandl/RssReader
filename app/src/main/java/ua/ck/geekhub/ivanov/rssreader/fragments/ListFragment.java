package ua.ck.geekhub.ivanov.rssreader.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.activities.DetailsActivity;
import ua.ck.geekhub.ivanov.rssreader.activities.LoginActivity;
import ua.ck.geekhub.ivanov.rssreader.dummy.Feed;
import ua.ck.geekhub.ivanov.rssreader.heplers.Constants;
import ua.ck.geekhub.ivanov.rssreader.heplers.DatabaseHelper;
import ua.ck.geekhub.ivanov.rssreader.heplers.Utils;
import ua.ck.geekhub.ivanov.rssreader.services.UpdateFeedService;

public class ListFragment extends android.support.v4.app.ListFragment {

    private ListView mList;
    private View mListContainer;
    private SwipeRefreshLayout mSwipeLayout;
    private View mProgressBar;

    private ActionBar mActionBar;
    private ArrayList<Feed> mFeedList = new ArrayList<>();
    private Feed mCurrentFeed;

    private int mCurrentFeedIndex;

    private SharedPreferences mSharedPreferences;
    private boolean mIsTableLand, mAllowNotification, mIsResult = false;
    private int mSpinnerSelected;
    private FeedAdapter mAdapter;

    private Context mContext;
    private Activity mActivity;

    private int mTask = 0;

    private TextView mTextViewEmpty;
    private View mButtonTryAgain;
    private Button mButtonGoToOther;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mIsTableLand = getResources().getBoolean(R.bool.tablet_land);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity = getActivity();
        mContext = mActivity.getApplicationContext();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mSpinnerSelected = savedInstanceState.getInt(Constants.EXTRA_SPINNER);
        }
        mActivity = getActivity();
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mActivity);

        mAllowNotification = mSharedPreferences
                .getBoolean(Constants.EXTRA_ALLOW_NOTIFICATION, false);
        if (mAllowNotification) {
            Intent updateServiceIntent = new Intent(mActivity, UpdateFeedService.class);
            mActivity.startService(updateServiceIntent);
        }

        mContext = mActivity.getApplicationContext();
        mProgressBar = view.findViewById(R.id.loading_indicator);

        setActionBarSetting();

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (mSpinnerSelected) {
                    case Constants.NEWS:
                        startDownloadData(Constants.URL_NEWS);
                        break;
                    case Constants.FAVOURITE:
                        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                        mFeedList = db.getAllFeed();
                        updateList();
                        break;
                }
                if (mIsTableLand) {
                    mCurrentFeedIndex = 0;
                }
            }
        });
        mSwipeLayout.setColorSchemeResources(
                R.color.action_bar_blue,
                android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_red_light);

        setListViewSetting(view);
    }

    private void setActionBarSetting() {
        mActionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        final String[] dropdownValues = getResources().getStringArray(R.array.spinner_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mActionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, dropdownValues);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mActionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long id) {
                mSpinnerSelected = position;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(Constants.EXTRA_SPINNER, position);
                editor.apply();
                setEmptyViewChanges();
                switch (position) {
                    case Constants.NEWS:
                        if (Utils.isOnline(mActivity)) {
                            showProgressBar();
                            startDownloadData(Constants.URL_NEWS);
                        } else {
                            mFeedList = new ArrayList<>();
                            updateList();
                        }
                        break;
                    case Constants.FAVOURITE:
                        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                        mFeedList = db.getAllFeed();
                        updateList();
                        break;
                }
                return true;
            }
        });
        mActionBar.setSelectedNavigationItem(mSpinnerSelected);
    }

    private void setListViewSetting(View view) {
        mList = (ListView) view.findViewById(android.R.id.list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsTableLand) {
                    if (mFeedList.indexOf(mCurrentFeed) != position) {
                        mCurrentFeed = mFeedList.get(position);
                        setCurrentFeed();
                        mCurrentFeedIndex = position;
                    }
                } else {
                    Intent intent = new Intent(mActivity, DetailsActivity.class);
                    intent.putExtra(Constants.EXTRA_FEEDS, mFeedList);
                    intent.putExtra(Constants.EXTRA_POSITION, position);
                    intent.putExtra(Constants.EXTRA_STATE, mSpinnerSelected);
                    intent.putExtra(Constants.EXTRA_FEEDS_COUNT, mFeedList.size());
                    startActivityForResult(intent, Constants.REQUEST_FEED);
                }
            }
        });
        mAdapter = new FeedAdapter(mActivity, mFeedList);
        mList.setAdapter(mAdapter);

        mListContainer = view.findViewById(R.id.list_container);

        mTextViewEmpty = (TextView) view.findViewById(R.id.text_view_empty);
        mButtonTryAgain = view.findViewById(R.id.button_empty_try_again);
        mButtonTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                startDownloadData(Constants.URL_NEWS);
            }
        });

        mButtonGoToOther = (Button) view.findViewById(R.id.button_empty_go_to_other);
        mButtonGoToOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpinnerSelected == Constants.FAVOURITE) {
                    mActionBar.setSelectedNavigationItem(Constants.NEWS);
                } else {
                    mActionBar.setSelectedNavigationItem(Constants.FAVOURITE);
                }
            }
        });
    }

    private void setEmptyViewChanges() {
        boolean isFavourite = mSpinnerSelected == Constants.FAVOURITE;

        int text = isFavourite ? R.string.warning_favourite : R.string.warning_news;
        mTextViewEmpty.setText(text);

        int visibility = isFavourite ? View.GONE : View.VISIBLE;
        mButtonTryAgain.setVisibility(visibility);

        int textButton = isFavourite ? R.string.go_to_news : R.string.go_to_favourite;
        mButtonGoToOther.setText(textButton);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSpinnerSelected = mSharedPreferences.getInt(Constants.EXTRA_SPINNER, 0);
        mActionBar.setSelectedNavigationItem(mSpinnerSelected);
        if (requestCode == Constants.REQUEST_FEED && data != null) {
            mIsResult = true;
            mCurrentFeed = data.getParcelableExtra(Constants.EXTRA_FEED);
        } else {
            mIsResult = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemDeleteAllFavourite = menu.findItem(R.id.menu_delete_all_favourite);
        if (mSpinnerSelected == Constants.FAVOURITE) {
            itemDeleteAllFavourite.setVisible(true);
        } else {
            itemDeleteAllFavourite.setVisible(false);
        }
        MenuItem itemChangeNotification = menu.findItem(R.id.menu_change_notification);
        if (mAllowNotification) {
            itemChangeNotification.setTitle(R.string.off_notification);
        } else {
            itemChangeNotification.setTitle(R.string.on_notification);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_login:
                startActivity(new Intent(getActivity(), LoginActivity.class));
                return true;
            case R.id.menu_delete_all_favourite:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);
                alertDialog.setTitle(getString(R.string.title_dialog));
                alertDialog.setMessage(getString(R.string.message_dialog));
                alertDialog.setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                        db.deleteAll();
                        mFeedList = new ArrayList<>();
                        updateList();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {

                    }
                });
                alertDialog.setCancelable(true);
                alertDialog.show();
                return true;
            case R.id.menu_change_notification:
                Intent updateServiceIntent = new Intent(getActivity(), UpdateFeedService.class);
                if (mAllowNotification) {
                    item.setTitle(R.string.off_notification);
                    getActivity().stopService(updateServiceIntent);
                } else {
                    item.setTitle(R.string.on_notification);
                    getActivity().startService(updateServiceIntent);
                }
                mAllowNotification = !mAllowNotification;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(Constants.EXTRA_ALLOW_NOTIFICATION, mAllowNotification);
                editor.apply();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Constants.EXTRA_SPINNER, mSpinnerSelected);
    }


    private void startDownloadData(String url) {
        if (Utils.isOnline(mActivity)) {
            mSwipeLayout.setRefreshing(true);
            new DownloadStringTask().execute(url);
        } else {
            Toast.makeText(mActivity, R.string.warning_news, Toast.LENGTH_SHORT).show();
            mSwipeLayout.setRefreshing(false);
            hideProgressBar();
        }
    }

    private void showProgressBar() {
        mListContainer.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mListContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private ArrayList<Feed> parseJSON(String data) {
        try {
            JSONObject rssJSONObject = XML.toJSONObject(data);
            JSONObject rss = rssJSONObject.getJSONObject("rss");
            JSONObject channel = rss.getJSONObject("channel");
            JSONArray items = channel.getJSONArray("item");

            ArrayList<Feed> list = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject author = item.getJSONObject("atom:author");
                Object enclosureObject = item.opt("enclosure");
                JSONObject enclosure = null;
                if (enclosureObject != null) {
                    if (enclosureObject instanceof JSONArray) {
                        enclosure = ((JSONArray) enclosureObject).getJSONObject(0);
                    } else {
                        enclosure = (JSONObject) enclosureObject;
                    }
                }

                Feed rssItem = new Feed();
                rssItem
                        .setTitle(item.optString("title"))
                        .setLink(item.optString("link"))
                        .setDescription(item.optString("description"))
                        .setAuthorName(author.optString("name"))
                        .setAuthorLink(author.optString("uri"))
                        .setPubDate(item.optString("pubDate"));

                if (enclosureObject != null) {
                    rssItem.setImage(enclosure.optString("url"));
                }

                list.add(rssItem);
            }
            return list;
        } catch (JSONException e) {
//            Toast.makeText(mActivity, R.string.error_download, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void updateList() {
        mAdapter = new FeedAdapter(mContext, mFeedList);
        mList.setAdapter(mAdapter);
        mSwipeLayout.setRefreshing(false);
        hideProgressBar();
        if (mIsTableLand) {
            if (mIsResult) {
                if (mTask++ == 1) {
                    mIsResult = false;
                    mTask = 0;
                }
                int selected = mFeedList.indexOf(mCurrentFeed);
                if (selected != -1) {
                    mCurrentFeedIndex = selected;
                    mList.setSelectionFromTop(mCurrentFeedIndex, 0);
                }
            } else {
                if (!mFeedList.isEmpty()) {
                    mCurrentFeed = mFeedList.get(0);
                    mCurrentFeedIndex = 0;
                }
            }
            mList.setItemChecked(mCurrentFeedIndex, true);
            setCurrentFeed();
        }
    }

    private void setCurrentFeed() {
        if (mCurrentFeed == null) {
            return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            DetailsFragment detailsFragment = DetailsFragment.newInstance(mCurrentFeed);
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.table_content_container, detailsFragment)
                    .commit();
        }
    }

    class DownloadStringTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                java.net.URL url = new java.net.URL(params[0]);
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(url.openStream()));
                String buffString;
                while ((buffString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(buffString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mFeedList = parseJSON(s);
            updateList();
        }
    }

    class FeedAdapter extends BaseAdapter {

        private ArrayList<Feed> mFeedList;
        private LayoutInflater mLayoutInflater;

        public FeedAdapter(Context context, ArrayList<Feed> feedList) {
            mFeedList = feedList;
            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mFeedList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFeedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public Feed getFeed(int position) {
            return (Feed) getItem(position);
        }

        @Override
        public boolean isEmpty() {
            return mFeedList.isEmpty();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.feed_item, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.mTextViewDate = (TextView) convertView.findViewById(R.id.feed_date);
                viewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.feed_title);
                viewHolder.mTextViewAuthor = (TextView) convertView.findViewById(R.id.feed_author);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Feed feed = getFeed(position);

            Date date = new Date();
            try {
                SimpleDateFormat incomingDate =
                        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
                date = incomingDate.parse(feed.getPubDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm");

            viewHolder.mTextViewDate.setText(convertView.getResources()
                    .getString(R.string.published) + " " + dateFormat.format(date));
            viewHolder.mTextViewTitle.setText(Html.fromHtml(feed.getTitle()));
            viewHolder.mTextViewAuthor.setText(", " + feed.getAuthorName());

            return convertView;
        }
    }

    static class ViewHolder {
        TextView mTextViewTitle, mTextViewDate, mTextViewAuthor;
    }
}