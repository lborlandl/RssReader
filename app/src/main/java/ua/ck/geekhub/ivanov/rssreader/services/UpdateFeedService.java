package ua.ck.geekhub.ivanov.rssreader.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.activities.ListActivity;
import ua.ck.geekhub.ivanov.rssreader.heplers.Constants;
import ua.ck.geekhub.ivanov.rssreader.heplers.SharedPreferenceHelper;
import ua.ck.geekhub.ivanov.rssreader.heplers.Utils;

public class UpdateFeedService extends Service {

    private final static int ID = 1;
    private final static int DAY = 24 * 60 * 60;

    private SharedPreferenceHelper mSPHelper;
    private int mCounter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        mSPHelper = SharedPreferenceHelper.getInstance(getApplicationContext());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    new CheckUpdateTask().execute(Constants.URL_NEWS);
                    try {
                        int updateTime;
                        if (mCounter > mSPHelper.getCountAttempt()) {
                            mCounter = 0;
                            updateTime = DAY;
                        } else {
                            updateTime = mSPHelper.getUpdateTime();
                        }
                        TimeUnit.SECONDS.sleep(updateTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
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

    private void buildNotification() {
        Context context = getApplicationContext();

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent(context, ListActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder
                .setSmallIcon(R.drawable.logo_trash)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.logo_trash_large))
                .setTicker(res.getString(R.string.notification_ticker))
                .setContentTitle(res.getString(R.string.notification_title))
                .setContentText(res.getString(R.string.notification_text))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent);

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_ALL;

        nm.notify(ID, notification);
//        startForeground(ID, builder.build());
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
            try {
                JSONArray items = XML
                        .toJSONObject(s)
                        .getJSONObject("rss")
                        .getJSONObject("channel")
                        .getJSONArray("item");
                s = items.getJSONObject(0).optString("link");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                s = null;
            }
            String link = mSPHelper.getLastNewsLink();
            if (link == null || (s != null && !link.equals(s))) {
                if (!mSPHelper.getListRunning()) {
                    buildNotification();
                    mCounter++;
                }
                mSPHelper.putLastNewsLink(s);
            }
        }
    }
}
