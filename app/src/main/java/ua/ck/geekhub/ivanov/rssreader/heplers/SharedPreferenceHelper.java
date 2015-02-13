package ua.ck.geekhub.ivanov.rssreader.heplers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceHelper {

    private static SharedPreferenceHelper mInstance;

    private SharedPreferences mSharedPreferences;

    public static final String ALLOW_NOTIFICATION = "allow_notification";
    public static final String SPINNER_POSITION = "spinner_position";
    public static final String LAST_NEWS_LINK = "last_news_link";
    public static final String UPDATE_TIME = "time_for_update";
    public static final String RUNNING_LIST_FRAGMENT = "running_list_fragment";
    public static final String ATTEMPT_TO_UPDATE = "attempt_to_update";


    private SharedPreferenceHelper(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferenceHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPreferenceHelper(context);
        }
        return mInstance;
    }

    public boolean getAllowNotification() {
        return mSharedPreferences.getBoolean(ALLOW_NOTIFICATION, false);
    }

    public void putAllowNotification(boolean isAllow) {
        mSharedPreferences.edit()
                .putBoolean(ALLOW_NOTIFICATION, isAllow)
                .apply();
    }

    public int getSpinnerPosition() {
        return mSharedPreferences.getInt(SPINNER_POSITION, 0);
    }

    public void putSpinnerPosition(int position) {
        mSharedPreferences.edit()
                .putInt(SPINNER_POSITION, position)
                .apply();
    }

    public String getLastNewsLink() {
        return mSharedPreferences.getString(LAST_NEWS_LINK, null);
    }

    public void putLastNewsLink(String link) {
        mSharedPreferences.edit()
                .putString(LAST_NEWS_LINK, link)
                .apply();
    }

    public int getUpdateTime() {
        return mSharedPreferences.getInt(UPDATE_TIME, (30 * 60));
    }

    public void putTimeSecond(int second) {
        mSharedPreferences.edit()
                .putInt(UPDATE_TIME, second)
                .apply();
    }

    public void putTimeMinute(int minute) {
        mSharedPreferences.edit()
                .putInt(UPDATE_TIME, (minute / 60))
                .apply();
    }

    public boolean getListRunning() {
        return mSharedPreferences.getBoolean(RUNNING_LIST_FRAGMENT, false);
    }

    public void putListRunning(boolean isRunning) {
        mSharedPreferences.edit()
                .putBoolean(RUNNING_LIST_FRAGMENT, isRunning)
                .apply();
    }

    public int getCountAttempt() {
        return mSharedPreferences.getInt(ATTEMPT_TO_UPDATE, (3));
    }

    public void putCountAttempt(int count) {
        mSharedPreferences.edit()
                .putInt(ATTEMPT_TO_UPDATE, count)
                .apply();
    }
}
