package ua.ck.geekhub.ivanov.rssreader.services;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ua.ck.geekhub.ivanov.rssreader.tools.Constants;

public class FeedLoader extends AsyncTaskLoader<String> {

    public static final String URL_EXTRA = "url_for_loader";

    public String mUrl;

    public FeedLoader(Context context, Bundle args) {
        super(context);
        if (args != null) {
            mUrl = args.getString(URL_EXTRA);
        } else {
            mUrl = Constants.URL_NEWS;
        }
    }

    @Override
    public String loadInBackground() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            java.net.URL url = new java.net.URL(mUrl);
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
}
