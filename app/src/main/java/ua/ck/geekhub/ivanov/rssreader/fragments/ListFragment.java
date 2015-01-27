package ua.ck.geekhub.ivanov.rssreader.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import ua.ck.geekhub.ivanov.rssreader.services.UpdateFeedService;
import ua.ck.geekhub.ivanov.rssreader.heplers.Utils;

public class ListFragment extends Fragment {

    private ListView mListView;
    private SwipeRefreshLayout mSwipeLayout;
    private View mProgressBar;
    private ActionBar mActionBar;

    private ArrayList<Feed> mFeedList = new ArrayList<Feed>();

    private String mDownloadData;

    private SharedPreferences mSharedPreferences;

    private boolean mIsTable, mAllowNotification;
    private int mSpinnerSelected = 0;
    private FeedAdapter mFeedAdapter;

    private Context mContext;

//    public static ListFragment newInstance() {
//        Bundle args = new Bundle();
//        ListFragment fragment = new ListFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }


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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {
            mSpinnerSelected = savedInstanceState.getInt(Constants.EXTRA_SPINNER);
        }
        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        mAllowNotification = mSharedPreferences.getBoolean(Constants.EXTRA_ALLOW_NOTIFICATION, false);
        if (mAllowNotification) {
            Intent updateServiceIntent = new Intent(getActivity(), UpdateFeedService.class);
            getActivity().startService(updateServiceIntent);
        }

        mContext = getActivity().getApplicationContext();

        mActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

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
                switch (position) {
                    case 2:
                        mSwipeLayout.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        updateFavourite();
                        break;
                    default:
                        mSwipeLayout.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.VISIBLE);
                        startDownloadData(getSelectedLink(mSpinnerSelected));
                }
                return true;
            }
        });
        //TODO not working:
        mActionBar.setSelectedNavigationItem(mSpinnerSelected);
        mProgressBar = view.findViewById(R.id.loading_indicator);


        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (mSpinnerSelected) {
                    case 2:
                        updateFavourite();
                        break;
                    default:
                        startDownloadData(getSelectedLink(mSpinnerSelected));
                }
            }
        });
        mSwipeLayout.setColorSchemeResources(
                R.color.action_bar_blue,
                android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_red_light);

        mIsTable = (view.findViewById(R.id.table_content_container) != null);

        mListView = (ListView) view.findViewById(R.id.list_feeds);
//        if (mSpinnerSelected == 2) {
//            mListView.setEmptyView(view.findViewById(R.id.view_empty_favourite));
//        } else {
//            mListView.setEmptyView(view.findViewById(R.id.view_empty_list));
//        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsTable) {
                    Feed feed = mFeedList.get(position);
                    DetailsFragment detailsFragment = DetailsFragment.newInstance(feed);
                    getFragmentManager()
                            .beginTransaction()
                            .replace(R.id.table_content_container, detailsFragment)
                            .commit();
                } else {
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra(Constants.EXTRA_FEEDS, mFeedList);
                    intent.putExtra(Constants.EXTRA_POSITION, position);
                    intent.putExtra(Constants.EXTRA_STATE, mSpinnerSelected);
                    startActivityForResult(intent, Constants.REQUEST_FEED);
                }
            }
        });
        mFeedAdapter = new FeedAdapter(getActivity(), mFeedList);
        mListView.setAdapter(mFeedAdapter);
        mSwipeLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        startDownloadData(getSelectedLink(mSpinnerSelected));
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        mSpinnerSelected = mSharedPreferences.getInt(Constants.EXTRA_SPINNER_POSITION, 0);
//        mActionBar.setSelectedNavigationItem(mSpinnerSelected);
//        if (requestCode == Constants.REQUEST_FEED) {
//            Feed feed = (Feed) data.getSerializableExtra(Constants.EXTRA_FEED);
//            int selected = mFeedList.indexOf(feed);
//            if (selected != -1) {
//                mListView.setSelection(selected);
//            }
//            DetailsFragment detailsFragment = DetailsFragment.newInstance(feed);
//            getFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.table_content_container, detailsFragment)
//                    .commit();
//        }
//    }

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
                DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                db.deleteAll();
                updateFavourite();
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

    private void updateFavourite() {
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        mFeedList = db.getAllFeed();
        mFeedAdapter = new FeedAdapter(getActivity(), mFeedList);
        mListView.setAdapter(mFeedAdapter);
//        mListView.setAdapter(mFeedAdapter);
        mSwipeLayout.setRefreshing(false);
        if (!mFeedList.isEmpty() && mIsTable) {
            Feed feed = mFeedList.get(0);
            DetailsFragment detailsFragment = DetailsFragment.newInstance(feed);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.table_content_container, detailsFragment)
                    .commit();
        }
    }

    private String getSelectedLink(int pos) {
        switch (pos) {
            case 0:
                return Constants.URL_NEWS;
            case 1:
                return Constants.URL_TEXT;
            default:
                return null;
        }
    }

    private void startDownloadData(String url) {
        if (Utils.isOnline(getActivity())) {
            mSwipeLayout.setRefreshing(true);
            new DownloadStringTask().execute(url);
        } else {
            Toast.makeText(getActivity(), R.string.not_network, Toast.LENGTH_SHORT).show();
            mSwipeLayout.setRefreshing(false);
        }
    }

    private void updateList() {
        try {

            JSONObject rssJSONObject = XML.toJSONObject(mDownloadData);
            JSONObject rss = rssJSONObject.getJSONObject("rss");
            JSONObject channel = rss.getJSONObject("channel");
            JSONArray items = channel.getJSONArray("item");

            ArrayList<Feed> newList = new ArrayList<Feed>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                JSONObject author = item.getJSONObject("atom:author");
                Object enclosureObject = item.get("enclosure");
                JSONObject enclosure;
                if (enclosureObject instanceof JSONArray) {
                    enclosure = ((JSONArray) enclosureObject).getJSONObject(0);
                } else {
                    enclosure = (JSONObject) enclosureObject;
                }

                Feed rssItem = new Feed();
                rssItem
                        .setTitle(item.optString("title"))
                        .setLink(item.optString("link"))
                        .setDescription(item.optString("description"))
                        .setImage(enclosure.optString("url"))
                        .setAuthorName(author.optString("name"))
                        .setAuthorLink(author.optString("uri"))
                        .setPubDate(item.optString("pubDate"));

                newList.add(rssItem);
            }
            mFeedList = newList;
        } catch (JSONException e) {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), R.string.error_download, Toast.LENGTH_LONG).show();
            }
            e.printStackTrace();
        }
        mFeedAdapter = new FeedAdapter(mContext, mFeedList);
        mListView.setAdapter(mFeedAdapter);
        mSwipeLayout.setRefreshing(false);
        if (mIsTable) {
            mListView.setSelection(0);
            DetailsFragment detailsFragment = DetailsFragment.newInstance(mFeedList.get(0));
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.table_content_container, detailsFragment)
                    .commit();
        }
        mSwipeLayout.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    class DownloadStringTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
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
            mDownloadData = stringBuilder.toString();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateList();
        }
    }
}
