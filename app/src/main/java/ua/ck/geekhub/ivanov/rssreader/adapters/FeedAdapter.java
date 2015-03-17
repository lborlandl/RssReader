package ua.ck.geekhub.ivanov.rssreader.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import ua.ck.geekhub.ivanov.rssreader.R;
import ua.ck.geekhub.ivanov.rssreader.models.Feed;

public class FeedAdapter extends BaseAdapter {
    private int lastPosition = -1;

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private ArrayList<Feed> mFeedList;
    private boolean mIsAnimations = true;

    public FeedAdapter(Context context) {
        this(context, new ArrayList<Feed>());
    }

    public FeedAdapter(Context context, ArrayList<Feed> feedList) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        mFeedList = feedList;
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

    public void clear() {
        mFeedList.clear();
    }

    public void addAll(Collection<? extends Feed> items) {
        clear();
        mFeedList.addAll(items);
    }

    public int indexOf(Feed feed) {
        return mFeedList.indexOf(feed);
    }

    public void updateAnimation(boolean isAnimations) {
        mIsAnimations = isAnimations;
    }

    public ArrayList<Feed> getList() {
        return mFeedList;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        lastPosition = -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.feed_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.mTextViewDate = (TextView) convertView.findViewById(R.id.txt_item_date);
            viewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.txt_item_title);
            viewHolder.mTextViewAuthor = (TextView) convertView.findViewById(R.id.txt_item_author);

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

        if (mIsAnimations) {
            int id = (position > lastPosition) ?
                    R.anim.abc_slide_in_bottom : R.anim.abc_slide_in_top;
            Animation animation = AnimationUtils.loadAnimation(mContext, id);
            convertView.startAnimation(animation);
        }
        lastPosition = position;

        return convertView;
    }

    static class ViewHolder {
        TextView mTextViewTitle, mTextViewDate, mTextViewAuthor;
    }
}
