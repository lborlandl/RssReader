package ua.ck.geekhub.ivanov.rssreader.fragments;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.activities.DetailsActivity;
import ua.ck.geekhub.ivanov.rssreader.activities.LoginActivity;
import ua.ck.geekhub.ivanov.rssreader.activities.SettingsActivity;
import ua.ck.geekhub.ivanov.rssreader.adapters.FeedAdapter;
import ua.ck.geekhub.ivanov.rssreader.heplers.DatabaseHelper;
import ua.ck.geekhub.ivanov.rssreader.heplers.NotificationHelper;
import ua.ck.geekhub.ivanov.rssreader.heplers.PreferenceHelper;
import ua.ck.geekhub.ivanov.rssreader.models.Feed;
import ua.ck.geekhub.ivanov.rssreader.services.UpdateFeedService;
import ua.ck.geekhub.ivanov.rssreader.tools.Constants;
import ua.ck.geekhub.ivanov.rssreader.tools.Utils;

public class ListFragment extends android.support.v4.app.ListFragment {

    private ListView mList;
    private View mListContainer;
    private SwipeRefreshLayout mSwipeLayout;
    private View mProgressBar;

    private ActionBar mActionBar;
    //    private ArrayList<Feed> mFeedList = new ArrayList<>();
    private Feed mCurrentFeed;

    private int mCurrentFeedIndex;

    private PreferenceHelper mPreferenceHelper;
    private boolean mIsTableLand, mIsResult = false;
    private int mSpinnerSelected;
    private FeedAdapter mAdapter;

    private ActionBarActivity mActivity;

    private int mTask = 0;

    private TextView mTextViewEmpty;
    private View mButtonTryAgain;
    private Button mButtonGoToOther;

    private static final int NEWS = 0;
    private static final int FAVOURITE = 1;

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
        mActivity = (ActionBarActivity) getActivity();
        mPreferenceHelper.putListRunning(true);
        mAdapter.updateAnimation(mPreferenceHelper.isAnimation());
        updateNotification();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPreferenceHelper.putListRunning(false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mSpinnerSelected = savedInstanceState.getInt(Constants.EXTRA_SPINNER);
        }
        mActivity = (ActionBarActivity) getActivity();
        mPreferenceHelper = PreferenceHelper.getInstance(mActivity);

        if (mPreferenceHelper.isNotification()) {
            Intent updateServiceIntent = new Intent(mActivity, UpdateFeedService.class);
            mActivity.startService(updateServiceIntent);
        }

        mProgressBar = view.findViewById(R.id.progress_list);

        setActionBarSetting();

        mSwipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                switch (mSpinnerSelected) {
                    case NEWS:
                        startDownloadData(Constants.URL_NEWS);
                        break;
                    case FAVOURITE:
                        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                        mAdapter.addAll(db.getAllFeed());
                        updateList();
                        break;
                }
                if (mIsTableLand) {
                    mCurrentFeedIndex = 0;
                }
            }
        });
        mSwipeLayout.setColorSchemeResources(
                R.color.color_primary,
                android.R.color.holo_orange_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_red_light);

        setListViewSetting(view);
    }

    private void setActionBarSetting() {
        mActionBar = mActivity.getSupportActionBar();
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
                mPreferenceHelper.putSpinnerPosition(position);
                setEmptyViewChanges();
                switch (position) {
                    case NEWS:
                        if (Utils.isOnline(mActivity)) {
                            showProgressBar();
                            startDownloadData(Constants.URL_NEWS);
                        } else {
                            mAdapter.clear();
                            updateList();
                        }
                        break;
                    case FAVOURITE:
                        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                        mAdapter.addAll(db.getAllFeed());
                        updateList();
                        mActivity.supportInvalidateOptionsMenu();
                        break;
                }
                return true;
            }
        });
        mActionBar.setSelectedNavigationItem(mSpinnerSelected);
    }

    private void setListViewSetting(View view) {
        mList = (ListView) view.findViewById(android.R.id.list);
        if (mIsTableLand) {
            mList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        }
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mIsTableLand) {
                    if (mAdapter.indexOf(mCurrentFeed) != position) {
                        mCurrentFeed = mAdapter.getFeed(position);
                        setCurrentFeed();
                        mCurrentFeedIndex = position;
                    }
                } else {
                    Intent intent = new Intent(mActivity, DetailsActivity.class);
                    intent.putExtra(Constants.EXTRA_FEED_ARRAY, mAdapter.getList());
                    intent.putExtra(Constants.EXTRA_POSITION, position);
                    intent.putExtra(Constants.EXTRA_STATE, mSpinnerSelected);
                    startActivityForResult(intent, Constants.REQUEST_FEED);
                }
            }
        });
        mAdapter = new FeedAdapter(mActivity);
        mList.setAdapter(mAdapter);

        mListContainer = view.findViewById(R.id.list_container);

        mTextViewEmpty = (TextView) view.findViewById(R.id.txt_empty);
        mButtonTryAgain = view.findViewById(R.id.btn_empty_try_again);
        mButtonTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                startDownloadData(Constants.URL_NEWS);
            }
        });

        mButtonGoToOther = (Button) view.findViewById(R.id.btn_empty_go_to_other);
        mButtonGoToOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpinnerSelected == FAVOURITE) {
                    mActionBar.setSelectedNavigationItem(NEWS);
                } else {
                    mActionBar.setSelectedNavigationItem(FAVOURITE);
                }
            }
        });
    }

    private void setEmptyViewChanges() {
        boolean isFavourite = mSpinnerSelected == FAVOURITE;

        int text = isFavourite ? R.string.warning_favourite : R.string.warning_news;
        mTextViewEmpty.setText(text);

        int visibility = isFavourite ? View.GONE : View.VISIBLE;
        mButtonTryAgain.setVisibility(visibility);

        int textButton = isFavourite ? R.string.go_to_news : R.string.go_to_favourite;
        mButtonGoToOther.setText(textButton);
    }

    private void updateNotification() {
        boolean result = mActivity.getIntent().getBooleanExtra(Constants.EXTRA_NOTIFICATION, false);
        NotificationManager manager = (NotificationManager)
                mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(NotificationHelper.NOTIFY_ID);
        if (mPreferenceHelper.isNotification() && result && mPreferenceHelper.isForeground()) {
            NotificationHelper helper = NotificationHelper.getInstance(getActivity());
            helper.showNotification(NotificationHelper.FOREGROUND);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSpinnerSelected = mPreferenceHelper.getSpinnerPosition();
        mActionBar.setSelectedNavigationItem(mSpinnerSelected);
        if (resultCode == Constants.REQUEST_FEED && data != null) {
            mIsResult = true;
            mCurrentFeed = data.getParcelableExtra(Constants.EXTRA_FEED);
        } else {
            mIsResult = false;
        }
        if (resultCode == Constants.REQUEST_IS_CHANGED && mSpinnerSelected == FAVOURITE) {
            DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
            mAdapter.addAll(db.getAllFeed());
            updateList();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        boolean visibility = (mSpinnerSelected == FAVOURITE) && !mAdapter.isEmpty();
        menu.findItem(R.id.menu_delete_all_favourite).setVisible(visibility);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                Intent detailsActivityIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivityForResult(detailsActivityIntent, Constants.REQUEST_IS_CHANGED);
                return true;
            case R.id.action_login:
                startActivity(new Intent(getActivity(), LoginActivity.class));
                return true;
            case R.id.menu_delete_all_favourite:
                new MaterialDialog.Builder(mActivity)
                        .title(R.string.title_dialog)
                        .content(R.string.message_dialog)
                        .positiveText(R.string.button_yes)
                        .negativeText(R.string.button_no)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
                                db.deleteAll();
                                mAdapter.clear();
                                updateList();
                            }
                        })
                        .show();
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

    private XmlPullParser prepareXpp(String data) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(data));
        return xpp;
    }

    private ArrayList<Feed> parseXML(String data) {
        ArrayList<Feed> list = new ArrayList<>();
        Feed feed = new Feed();

        final String ITEM = "item";
        final String TITLE = "title";
        final String LINK = "link";
        final String DESCRIPTION = "description";
        final String ENCLOSURE = "enclosure";
        final String NAME = "name";
        final String URI = "uri";
        final String URL = "url";
        final String PUB_DATE = "pubDate";

        try {
            XmlPullParser xpp = prepareXpp(data);
            String tagName = "";

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        tagName = xpp.getName();
                        if (tagName.equals(ITEM)) {
                            feed = new Feed();
                            break;
                        }
                        if (tagName.equals(ENCLOSURE)) {
                            feed.setImage(xpp.getAttributeValue(null, URL));
                        }
                        break;
                    case XmlPullParser.TEXT:
                        String text = xpp.getText();
                        switch (tagName) {
                            case TITLE:
                                feed.setTitle(text);
                                break;
                            case LINK:
                                feed.setLink(text);
                                break;
                            case DESCRIPTION:
                                feed.setDescription(text);
                                break;
                            case NAME:
                                feed.setAuthorName(text);
                                break;
                            case URI:
                                feed.setAuthorLink(text);
                                break;
                            case PUB_DATE:
                                feed.setPubDate(text);
                                break;
                            default:
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = "";
                        if (xpp.getName().equals(ITEM)) {
                            list.add(feed);
                        }
                        break;
                    default:
                        break;
                }
                xpp.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void updateList() {
        mAdapter.notifyDataSetChanged();
        mSwipeLayout.setRefreshing(false);
        hideProgressBar();
        if (mIsTableLand) {
            if (mIsResult) {
                if (mTask++ == 1) {
                    mIsResult = false;
                    mTask = 0;
                }
                int selected = mAdapter.indexOf(mCurrentFeed);
                if (selected != -1) {
                    mCurrentFeedIndex = selected;
                }
            } else {
                if (!mAdapter.isEmpty()) {
                    mCurrentFeed = mAdapter.getFeed(0);
                    mCurrentFeedIndex = 0;
                }
            }
            mList.setItemChecked(mCurrentFeedIndex, true);
            setCurrentFeed();
        } else {
            mCurrentFeedIndex = 0;
        }
        mList.setSelectionFromTop(mCurrentFeedIndex, 0);
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

    class DownloadStringTask extends AsyncTask<String, Void, String> {
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
            mAdapter.addAll(parseXML(s));
            updateList();
        }
    }
}


//    private ArrayList<Feed> parseJSON(String data) {
//        try {
//            JSONObject rssJSONObject = XML.toJSONObject(data);
//            JSONObject rss = rssJSONObject.getJSONObject("rss");
//            JSONObject channel = rss.getJSONObject("channel");
//            JSONArray items = channel.getJSONArray("item");
//
//            ArrayList<Feed> list = new ArrayList<>();
//            for (int i = 0; i < items.length(); i++) {
//                JSONObject item = items.getJSONObject(i);
//                JSONObject author = item.getJSONObject("atom:author");
//                Object enclosureObject = item.opt("enclosure");
//                JSONObject enclosure = null;
//                if (enclosureObject != null) {
//                    if (enclosureObject instanceof JSONArray) {
//                        enclosure = ((JSONArray) enclosureObject).getJSONObject(0);
//                    } else {
//                        enclosure = (JSONObject) enclosureObject;
//                    }
//                }
//
//                Feed rssItem = new Feed();
//                rssItem
//                        .setTitle(item.optString("title"))
//                        .setLink(item.optString("link"))
//                        .setDescription(item.optString("description"))
//                        .setAuthorName(author.optString("name"))
//                        .setAuthorLink(author.optString("uri"))
//                        .setPubDate(item.optString("pubDate"));
//
//                if (enclosureObject != null) {
//                    rssItem.setImage(enclosure.optString("url"));
//                }
//
//                list.add(rssItem);
//            }
//            mPreferenceHelper.putLastNewsLink(list.get(0).getLink());
//            return list;
//        } catch (JSONException e) {
//            Toast.makeText(mActivity, R.string.error_download, Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }