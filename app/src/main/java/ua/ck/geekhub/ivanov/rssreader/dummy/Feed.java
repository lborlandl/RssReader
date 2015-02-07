package ua.ck.geekhub.ivanov.rssreader.dummy;

import android.os.Parcel;
import android.os.Parcelable;

public class Feed implements Parcelable {
    private String mTitle, mLink, mImage, mDescription, mAuthorName, mAuthorLink, mPubDate;

    public Feed() {

    }

    public Feed(Parcel in) {
        String[] data = new String[7];
        in.readStringArray(data);
        mTitle = data[0];
        mLink = data[1];
        mImage = data[2];
        mDescription = data[3];
        mAuthorName = data[4];
        mAuthorLink = data[5];
        mPubDate = data[6];
    }

    public String getTitle() {
        return mTitle;
    }

    public Feed setTitle(String title) {
        mTitle = title;
        return this;
    }

    public String getLink() {
        return mLink;
    }

    public Feed setLink(String link) {
        mLink = link;
        return this;
    }

    public String getImage() {
        return mImage;
    }

    public Feed setImage(String image) {
        mImage = image;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Feed setDescription(String description) {
        mDescription = description;
        return this;
    }

    public String getAuthorName() {
        return mAuthorName;
    }

    public Feed setAuthorName(String authorName) {
        mAuthorName = authorName;
        return this;
    }

    public String getAuthorLink() {
        return mAuthorLink;
    }

    public Feed setAuthorLink(String authorLink) {
        mAuthorLink = authorLink;
        return this;
    }

    public String getPubDate() {
        return mPubDate;
    }

    public Feed setPubDate(String pubDate) {
        mPubDate = pubDate;
        return this;
    }

    @Override
    public String toString() {
        return mTitle;
    }

    @Override
    public boolean equals(Object o) {
        Feed feed;
        if (o instanceof Feed) {
            feed = (Feed) o;
        } else {
            return false;
        }
        return feed.getLink().equals(getLink());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{
                mTitle, mLink, mImage, mDescription, mAuthorName, mAuthorLink, mPubDate
        });
    }

    public static final Parcelable.Creator<Feed> CREATOR = new Parcelable.Creator<Feed>() {

        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }

        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }
    };
}