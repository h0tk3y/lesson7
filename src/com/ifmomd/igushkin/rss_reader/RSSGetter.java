package com.ifmomd.igushkin.rss_reader;

import android.os.AsyncTask;
import android.os.Parcelable;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RSSGetter extends AsyncTask<String, Void, List<RSSItem>> implements ChannelParsedHandler, ItemParsedHandler {

    List<RSSItem>    items    = new ArrayList<RSSItem>();
    List<RSSChannel> channels = new ArrayList<RSSChannel>();

    @Override
    public void onChannelParsed(RSSChannel result) {
        channels.add(result);
    }

    @Override
    public void onItemParsed(RSSItem result) {
        items.add(result);
    }

    private enum Tag {channel, feed, item, entry, title, link, description, summary, date}

    ;

    class TagHandler extends DefaultHandler {
        ItemParsedHandler    itemParsedHandler;
        ChannelParsedHandler channelParsedHandler;


        public TagHandler(ItemParsedHandler itemParsedHandler, ChannelParsedHandler channelParsedHandler) {
            super();
            this.itemParsedHandler = itemParsedHandler;
            this.channelParsedHandler = channelParsedHandler;
        }

        RSSItem    currentItem;
        RSSChannel currentChannel;
        boolean    isItem; //otherwise parsing channel/feed

        private Stack<Tag> tags = new Stack<Tag>();

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            Tag foundTag = null;
            try { foundTag = Tag.valueOf(qName.toLowerCase());} catch (IllegalArgumentException ex) {/*it's alright*/}
            if (foundTag != null) tags.push(foundTag);
            if (foundTag == Tag.channel || foundTag == Tag.feed) {
                currentChannel = new RSSChannel();
            }
            if (foundTag == Tag.item || foundTag == Tag.entry) {
                isItem = true;
                currentItem = new RSSItem();
                currentItem.channel = currentChannel;
            }
            if (foundTag == Tag.link) {
                String link = attributes.getValue("href");
                if (link != null)
                    (isItem ? currentItem : currentChannel).link = link;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            Tag foundTag = null;
            try { foundTag = Tag.valueOf(qName.toLowerCase());} catch (IllegalArgumentException ex) {/*it's alright*/}
            if (foundTag != null)
                if (tags.size() > 0 && tags.peek() == foundTag) tags.pop(); //else TODO something's clearly wrong
            if (foundTag == Tag.channel || foundTag == Tag.feed) {
                channelParsedHandler.onChannelParsed(currentChannel);
                currentChannel = null;
            }
            if (foundTag == Tag.item || foundTag == Tag.entry) {
                itemParsedHandler.onItemParsed(currentItem);
                isItem = false;
                currentItem = null;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (tags.size() > 0) {
                Tag currentTag = tags.peek();
                RSSSomething target = isItem ? currentItem : currentChannel;
                if (target != null) {
                    if (currentTag == Tag.title)
                        target.title += new String(ch, start, length);
                    if (currentTag == Tag.description || currentTag == Tag.summary)
                        target.description += new String(ch, start, length);
                    if (currentTag == Tag.link)
                        target.link += new String(ch, start, length);
                }
            }
        }
    }

    @Override
    protected List<RSSItem> doInBackground(String... params) {
        if (params.length > 0) {
            String url = params[0];
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URI.create(url));
            HttpResponse response = null;
            boolean winEncoding = url.toLowerCase().contains("bash"); //horrible peace of shit
            InputStream content = null;
            try {
                response = client.execute(request);
                if (response != null) {
                    HttpEntity e = response.getEntity();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    content = e.getContent();
                    Reader r = new InputStreamReader(content, winEncoding ? "Windows-1251" : "UTF-8");
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    TagHandler tagHandler = new TagHandler(this, this) {
                    };
                    parser.parse(new InputSource(r), tagHandler);
                }
            } catch (ParserConfigurationException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (content != null) try {
                    content.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return items;
    }
}

class RSSSomething {
    String title       = "";
    String link        = "";
    String description = "";

    long id = -1;
}

class RSSItem extends RSSSomething implements Serializable {

    RSSChannel channel;

    @Override
    public String toString() {
        return title;
    }
}

class RSSChannel extends RSSSomething implements Serializable {
    long lasUpdated;
}

interface ItemParsedHandler {
    void onItemParsed(RSSItem result);
}

interface ChannelParsedHandler {
    void onChannelParsed(RSSChannel result);
}