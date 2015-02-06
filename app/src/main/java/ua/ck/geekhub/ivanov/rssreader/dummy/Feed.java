package ua.ck.geekhub.ivanov.rssreader.dummy;

import java.io.Serializable;

public class Feed implements Serializable {
    private String title, link, image, description, authorName, authorLink, pubDate;

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
}