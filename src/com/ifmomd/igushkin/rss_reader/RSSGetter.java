package com.ifmomd.igushkin.rss_reader;

import android.content.Entity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sergey on 10/16/13.
 */
public class RSSGetter extends AsyncTask<String, Void, List<RSSItem>> {

    private enum RSSFormat {Common, StackOverflow}

    @Override
    protected List<RSSItem> doInBackground(String... params) {
        List<RSSItem> result = null;
        if (params.length > 0) {
            String url = params[0];
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(URI.create(url));
            HttpResponse response = null;
            try {
                response = client.execute(request);
                if (response != null) {
                    HttpEntity e = response.getEntity();
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    DocumentBuilder db = dbf.newDocumentBuilder();
                    Document d = db.parse(e.getContent());
                    Element n = (Element) d.getDocumentElement();

                    RSSFormat format = RSSFormat.Common;
                    result = new ArrayList<RSSItem>();
                    NodeList items = d.getElementsByTagName("item");
                    if (items.getLength() == 0) {
                        items = d.getElementsByTagName("entry");
                        format = RSSFormat.StackOverflow;
                    }
                    for (int i = 0; i < items.getLength(); i++) {
                        RSSItem r = new RSSItem();
                        Node item = items.item(i);
                        NodeList properties = item.getChildNodes();
                        for (int j = 0; j < properties.getLength(); j++) {
                            Node property = properties.item(j);
                            String name = property.getNodeName();
                            if (name.equalsIgnoreCase("title")) {
                                r.title = property.getFirstChild().getNodeValue();
                            } else if (name.equalsIgnoreCase("link")) {
                                if (format == RSSFormat.StackOverflow)
                                    r.link = property.getAttributes().getNamedItem("href").getTextContent();
                                else
                                    r.link = property.getFirstChild().getNodeValue();
                            } else if (name.equalsIgnoreCase("description") || name.equalsIgnoreCase("summary")) {
                                StringBuilder text = new StringBuilder();
                                NodeList chars = property.getChildNodes();
                                for (int k = 0; k < chars.getLength(); k++) {
                                    text.append(chars.item(k).getNodeValue());
                                }
                                r.description = text.toString();
                            } else if (name.equalsIgnoreCase("date")) {
                                r.pubDate = property.getFirstChild().getNodeValue();
                            }
                        }
                        result.add(r);
                    }
                    // }
                }
            } catch (ParserConfigurationException ex) {
                ex.printStackTrace();
            } catch (SAXException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}

class RSSItem {
    String title, link, description, guid, pubDate;

    @Override
    public String toString() {
        return title;
    }
}

class RSSChannel {
    String title, link, description;
}
