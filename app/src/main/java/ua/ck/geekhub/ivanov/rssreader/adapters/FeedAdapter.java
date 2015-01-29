package ua.ck.geekhub.ivanov.rssreader.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.dummy.Feed;

public class FeedAdapter extends BaseAdapter {

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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.feed_item, parent, false);

            mViewHolder = new ViewHolder();
            mViewHolder.mTextViewDate = (TextView) convertView.findViewById(R.id.feed_date);
            mViewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.feed_title);
            mViewHolder.mTextViewAuthor = (TextView) convertView.findViewById(R.id.feed_author);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
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

        mViewHolder.mTextViewDate.setText(convertView.getResources()
                .getString(R.string.published) + " " + dateFormat.format(date));
        mViewHolder.mTextViewTitle.setText(Html.fromHtml(feed.getTitle()));
        mViewHolder.mTextViewAuthor.setText(", " + feed.getAuthorName());

//        if (position == 0) {
//            convertView
//                    .setBackgroundColor(convertView.getResources().getColor(R.color.list_selected));
//        }
        return convertView;
    }

    static class ViewHolder {
        TextView mTextViewTitle, mTextViewDate, mTextViewAuthor;
    }
}
