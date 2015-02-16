package ua.ck.geekhub.ivanov.rssreader.heplers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.activities.ListActivity;
import ua.ck.geekhub.ivanov.rssreader.activities.SettingsActivity;
import ua.ck.geekhub.ivanov.rssreader.tools.Constants;

public class NotificationHelper {

    private static NotificationHelper mInstance;
    private Context mContext;

    public final static int NOTIFY_ID = 101;

    private PreferenceHelper mPreferenceHelper;
    private NotificationManager mNotificationManager;

    public final static int FOREGROUND_FIRST = 0;
    public final static int FOREGROUND = 1;
    public final static int UPDATE = 2;

    private final Params[] params = new Params[]{
            new Params(SettingsActivity.class,
                    R.string.notification_running_ticker,
                    R.string.notification_running_title,
                    R.string.notification_running_text),
            new Params(SettingsActivity.class, 0,
                    R.string.notification_running_title,
                    R.string.notification_running_text),
            new Params(ListActivity.class,
                    R.string.notification_ticker,
                    R.string.notification_title,
                    R.string.notification_text),
    };

    private NotificationHelper(Context context) {
        mContext = context;
        mPreferenceHelper = PreferenceHelper.getInstance(context);
        mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static NotificationHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NotificationHelper(context);
        }
        return mInstance;
    }

    public void showNotification(int mode) {
        Notification notification = build(mode);
        mNotificationManager.notify(NOTIFY_ID, notification);
    }

    public Notification build(int mode) {
        Params params = this.params[mode];
        Resources res = mContext.getResources();

        Intent resultIntent = new Intent(mContext, params.cls);
        boolean extra = mode == UPDATE;
        resultIntent.putExtra(Constants.EXTRA_NOTIFICATION, extra);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
        builder
                .setSmallIcon(R.drawable.logo_trash)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.logo_trash_large))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(res.getString(params.title))
                .setContentText(res.getString(params.text));
        if (params.ticker != 0) {
            builder.setTicker(res.getString(params.ticker));
        }

        Notification notification = builder.build();
        if (mode == UPDATE) {
            setDefault(notification);
        }
        return notification;
    }

    private void setDefault(Notification notification) {
        if (mPreferenceHelper.isVibration()) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (mPreferenceHelper.isLed()) {
            notification.defaults |= Notification.DEFAULT_LIGHTS;
        }
        if (mPreferenceHelper.isSound()) {
            notification.defaults |= Notification.DEFAULT_SOUND;
        }
    }

    private class Params {
        Class cls;
        int ticker, title, text;

        Params(Class cls, int ticker, int title, int text) {
            this.cls = cls;
            this.ticker = ticker;
            this.title = title;
            this.text = text;
        }
    }
}
