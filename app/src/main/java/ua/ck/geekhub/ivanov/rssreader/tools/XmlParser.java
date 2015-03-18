package ua.ck.geekhub.ivanov.rssreader.tools;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import ua.ck.geekhub.ivanov.rssreader.models.Feed;

public class XmlParser {

    private static final String ITEM = "item";
    private static final String TITLE = "title";
    private static final String LINK = "link";
    private static final String DESCRIPTION = "description";
    private static final String ENCLOSURE = "enclosure";
    private static final String NAME = "name";
    private static final String URI = "uri";
    private static final String URL = "url";
    private static final String PUB_DATE = "pubDate";

    public ArrayList<Feed> getList(String data) {
        ArrayList<Feed> list = new ArrayList<>();
        Feed feed = new Feed();

        try {
            XmlPullParser xpp = prepareXpp(data);
            String tagName = "";

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        tagName = xpp.getName();
                        if (tagName.equals(ITEM)) {
                            feed = new Feed();
                            break;
                        }
                        if (tagName.equals(ENCLOSURE)) {
                            feed.setImage(xpp.getAttributeValue(null, URL));
                        }
                        break;
                    case XmlPullParser.TEXT:
                        String text = xpp.getText();
                        switch (tagName) {
                            case TITLE:
                                feed.setTitle(text);
                                break;
                            case LINK:
                                feed.setLink(text);
                                break;
                            case DESCRIPTION:
                                feed.setDescription(text);
                                break;
                            case NAME:
                                feed.setAuthorName(text);
                                break;
                            case URI:
                                feed.setAuthorLink(text);
                                break;
                            case PUB_DATE:
                                feed.setPubDate(text);
                                break;
                            default:
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        tagName = "";
                        if (xpp.getName().equals(ITEM)) {
                            list.add(feed);
                        }
                        break;
                    default:
                        break;
                }
                xpp.next();
            }

        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    public String getLastLink(String data) {
        try {
            XmlPullParser xpp = prepareXpp(data);
            String tagName = "";
            boolean isReturn = false;

            while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                switch (xpp.getEventType()) {
                    case XmlPullParser.START_TAG:
                        tagName = xpp.getName();
                        if (tagName.equals(ITEM)) {
                            isReturn = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        String text = xpp.getText();
                        if (isReturn && tagName.equals(LINK)) {
                            return text;
                        }
                        break;
                    default:
                        break;
                }
                xpp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private XmlPullParser prepareXpp(String data) throws XmlPullParserException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(data));
        return xpp;
    }
}
