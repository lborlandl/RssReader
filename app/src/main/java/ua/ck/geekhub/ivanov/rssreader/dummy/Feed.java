package ua.ck.geekhub.ivanov.rssreader.dummy;

import android.os.Parcel;
import android.os.Parcelable;

public class Feed implements Parcelable {
    private String title, link, image, description, authorName, authorLink, pubDate;

    public Feed() {

    }

    public Feed(Parcel in) {
        String[] data = new String[7];
        in.readStringArray(data);
        title = data[0];
        link = data[1];
        image = data[2];
        description = data[3];
        authorName = data[4];
        authorLink = data[5];
        pubDate = data[6];
    }

    public String getTitle() {
        return title;
    }

    public Feed setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getLink() {
        return link;
    }

    public Feed setLink(String link) {
        this.link = link;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Feed setImage(String image) {
        this.image = image;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Feed setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Feed setAuthorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public String getAuthorLink() {
        return authorLink;
    }

    public Feed setAuthorLink(String authorLink) {
        this.authorLink = authorLink;
        return this;
    }

    public String getPubDate() {
        return pubDate;
    }

    public Feed setPubDate(String pubDate) {
        this.pubDate = pubDate;
        return this;
    }

    @Override
    public String toString() {
        return title;
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
                title, link, image, description, authorName, authorLink, pubDate
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