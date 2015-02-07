package ua.ck.geekhub.ivanov.rssreader.heplers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import ua.ck.geekhub.ivanov.rssreader.dummy.Feed;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper mInstance = null;

    public final static int DATABASE_VERSION = 1;
    public final static String TABLE_NAME = "feed";
    public final static String COLUMN_ID = "_id";
    public final static String COLUMN_TITLE = "title";
    public final static String COLUMN_LINK = "link";
    public final static String COLUMN_IMAGE = "image";
    public final static String COLUMN_DESCRIPTION = "description";
    public final static String COLUMN_AUTHOR_NAME = "authorName";
    public final static String COLUMN_AUTHOR_LINK = "authorLink";
    public final static String COLUMN_DATE = "pubDate";

    private DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        super(context, name, factory, version);
    }

    public static DatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DatabaseHelper(context.getApplicationContext(), TABLE_NAME, null, 1);
        }
        return mInstance;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFeedTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT NOT NULL, " +
                COLUMN_LINK + " TEXT NOT NULL, " +
                COLUMN_IMAGE + " TEXT NOT NULL, " +
                COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                COLUMN_AUTHOR_NAME + " TEXT NOT NULL, " +
                COLUMN_AUTHOR_LINK + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT NOT NULL)";
        db.execSQL(createFeedTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DATABASE_VERSION < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public boolean addFeed(Feed feed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_TITLE, feed.getTitle());
        cv.put(COLUMN_LINK, feed.getLink());
        cv.put(COLUMN_IMAGE, feed.getImage());
        cv.put(COLUMN_DESCRIPTION, feed.getDescription());
        cv.put(COLUMN_AUTHOR_NAME, feed.getAuthorName());
        cv.put(COLUMN_AUTHOR_LINK, feed.getAuthorLink());
        cv.put(COLUMN_DATE, feed.getPubDate());
        long result = db.insert(TABLE_NAME, null, cv);
        db.close();
        return result != 0;
    }

    public ArrayList<Feed> getAllFeed() {
        ArrayList<Feed> feedList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Feed feed = new Feed();
                feed
                        .setTitle(cursor.getString(1))
                        .setLink(cursor.getString(2))
                        .setImage(cursor.getString(3))
                        .setDescription(cursor.getString(4))
                        .setAuthorName(cursor.getString(5))
                        .setAuthorLink(cursor.getString(6))
                        .setPubDate(cursor.getString(7));
                feedList.add(feed);
            } while (cursor.moveToNext());
        }
        cursor.close();
        ArrayList<Feed> feeds = new ArrayList<>();
        for (int i = feedList.size() - 1; i >= 0; i--) {
            feeds.add(feedList.get(i));
        }
        return feeds;
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public boolean deleteFeed(Feed feed) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_LINK + "=?" , new String[] { feed.getLink() }) == 1;
    }

    public boolean isFeed(Feed feed) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " +
                COLUMN_LINK + " = \"" + feed.getLink() + "\"", null);
        cursor.moveToFirst();
        cursor.close();
        return !(cursor.getInt(0) == 0);
    }
}
