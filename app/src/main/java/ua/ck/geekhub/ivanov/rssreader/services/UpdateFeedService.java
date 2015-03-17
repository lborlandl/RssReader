package ua.ck.geekhub.ivanov.rssreader.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import ua.ck.geekhub.ivanov.rssreader.heplers.NotificationHelper;
import ua.ck.geekhub.ivanov.rssreader.heplers.PreferenceHelper;
import ua.ck.geekhub.ivanov.rssreader.tools.Constants;
import ua.ck.geekhub.ivanov.rssreader.tools.Utils;
import ua.ck.geekhub.ivanov.rssreader.tools.XmlParser;

public class UpdateFeedService extends Service {

    private PreferenceHelper mPreferenceHelper;
    private NotificationHelper mNotificationHelper;

    private static volatile boolean flag;

    @Override
    public void onCreate() {
        super.onCreate();
        flag = true;
        mPreferenceHelper = PreferenceHelper.getInstance(getApplicationContext());
        mNotificationHelper = NotificationHelper.getInstance(getApplicationContext());
        if (mPreferenceHelper.isForeground()) {
            startForeground(NotificationHelper.NOTIFY_ID,
                    mNotificationHelper.build(NotificationHelper.FOREGROUND_FIRST));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                main:
                while (flag) {
                    try {
                        for (int i = 0; i < mPreferenceHelper.getDelay(); i++) {
                            TimeUnit.MINUTES.sleep(1);
                            if (!flag) {
                                break main;
                            }
                        }
                    } catch (InterruptedException e) {
                        flag = false;
                        e.printStackTrace();
                    }
                    new CheckUpdateTask().execute(Constants.URL_NEWS);
                }
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flag = false;
    }

    private class CheckUpdateTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            if (!Utils.isOnline(getApplicationContext())) {
                return null;
            }
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
            String newLastLink = new XmlParser().getLastLink(s);
            String savedLastLink = mPreferenceHelper.getLastNewsLink();
            if (savedLastLink == null || (newLastLink != null && !savedLastLink.equals(newLastLink))) {
                if (!mPreferenceHelper.isListRunning() && flag) {
                    mNotificationHelper.showNotification(NotificationHelper.UPDATE);
                }
                mPreferenceHelper.putLastNewsLink(newLastLink);
            }
        }
    }
}
