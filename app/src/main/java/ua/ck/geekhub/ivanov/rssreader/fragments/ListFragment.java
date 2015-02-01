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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.activities.DetailsActivity;
import ua.ck.geekhub.ivanov.rssreader.activities.LoginActivity;
import ua.ck.geekhub.ivanov.rssreader.adapters.FeedAdapter;
import ua.ck.geekhub.ivanov.rssreader.dummy.Feed;
import ua.ck.geekhub.ivanov.rssreader.heplers.Constants;
import ua.ck.geekhub.ivanov.rssreader.heplers.DatabaseHelper;
import ua.ck.geekhub.ivanov.rssreader.heplers.Utils;
import ua.ck.geekhub.ivanov.rssreader.services.UpdateFeedService;

public class ListFragment extends Fragment {

    private ListView mListView;
    private SwipeRefreshLayout mSwipeLayout;
    private View mProgressBar;
    private ActionBar mActionBar;

    private ArrayList<Feed> mFeedList = new ArrayList<Feed>();
    private Feed mCurrentFeed;

    private SharedPreferences mSharedPreferences;

    private boolean mIsTableLand, mAllowNotification, mIsResult = false;
    private int mSpinnerSelected = 0;
    private FeedAdapter mFeedAdapter;

    private Context mContext;
    private Activity mActivity;

    private int mTask = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        mIsTableLand = getResources().getBoolean(R.bool.tablet_land);
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

        mActionBar = ((ActionBarActivity) mActivity).getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mProgressBar = view.findViewById(R.id.loading_indicator);

        final String[] dropdownValues = getResources().getStringArray(R.array.spinner_list);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActionBar.getThemedContext(),
                android.R.layout.simple_spinner_item, android.R.id.text1, dropdownValues);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mActionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long id) {
                mSpinnerSelected = position;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(Constants.EXTRA_SPINNER_POSITION, position);
                editor.apply();
                //TODO
                switch (position) {
                    case 1:
                        mSwipeLayout.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                        mFeedList = db.getAllFeed();
                        updateList();
                        break;
                    default:
                        mSwipeLayout.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        startDownloadData(Constants.URL_NEWS);
                }
                return true;
            }
        });
        //TODO not working:
        mActionBar.setSelectedNavigationItem(mSpinnerSelected);

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (mSpinnerSelected) {
                    case 1:
                        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                        mFeedList = db.getAllFeed();
                        updateList();
                        break;
                    default:
                        startDownloadData(Constants.URL_NEWS);
                }
            }
        });
        mSwipeLayout.setColorSchemeResources(
                R.color.action_bar_blue,
                android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_red_light);

        mListView = (ListView) view.findViewById(R.id.list_feeds);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsTableLand) {
                    mCurrentFeed = mFeedList.get(position);
                    setCurrentFeed();
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
        mFeedAdapter = new FeedAdapter(mActivity, mFeedList);
        mListView.setAdapter(mFeedAdapter);
        mSwipeLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        startDownloadData(Constants.URL_NEWS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSpinnerSelected = mSharedPreferences.getInt(Constants.EXTRA_SPINNER_POSITION, 0);
        mActionBar.setSelectedNavigationItem(mSpinnerSelected);
        if (requestCode == Constants.REQUEST_FEED && data != null) {
            mIsResult = true;
            mCurrentFeed = (Feed) data.getSerializableExtra(Constants.EXTRA_FEED);
            int selected = mFeedList.indexOf(mCurrentFeed);
            if (selected != -1) {
                mListView.setSelection(selected);
            }
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
        if (mSpinnerSelected == 2) {
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
            Toast.makeText(mActivity, R.string.not_network, Toast.LENGTH_SHORT).show();
            mSwipeLayout.setRefreshing(false);
        }
    }

    private ArrayList<Feed> parseJSON(String data) {
        try {
            JSONObject rssJSONObject = XML.toJSONObject(data);
            JSONObject rss = rssJSONObject.getJSONObject("rss");
            JSONObject channel = rss.getJSONObject("channel");
            JSONArray items = channel.getJSONArray("item");

            ArrayList<Feed> list = new ArrayList<Feed>();
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
            Log.d("lalka", "ArrayList<>");
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void updateList() {
        mFeedAdapter = new FeedAdapter(mContext, mFeedList);
        mListView.setAdapter(mFeedAdapter);
        mSwipeLayout.setRefreshing(false);
        if (mIsTableLand) {
            if (mIsResult) {
                if (mTask++ == 1) {
                    mIsResult = false;
                    mTask = 0;
                }
            } else {
                if (!mFeedList.isEmpty()){
                    mCurrentFeed = mFeedList.get(0);
                }
            }
            setCurrentFeed();
        }
        mSwipeLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        Log.d("lalka", "updateList()");
    }

    private void setCurrentFeed() {
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
}