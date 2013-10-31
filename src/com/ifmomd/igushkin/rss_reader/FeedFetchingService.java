package com.ifmomd.igushkin.rss_reader;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Sergey on 10/24/13.
 */
public class FeedFetchingService extends IntentService {

    private long channelId;

    public FeedFetchingService() {
        super("FeedFetchingService");
    }


    public IBinder onBind(Intent intent) {
        return null;
    }

    List<RSSItem> items = new ArrayList<RSSItem>();

    @Override

    protected void onHandleIntent(Intent intent) {
        channelId = intent.getLongExtra("channelId", -1);
        if (channelId == -1)
            onFetchFailed();
        else
            new RSSFetcher(items).execute(intent.getStringExtra("url"));
    }

    void onFetchFailed() {
        Log.e(getResources().getString(R.string.app_name), "Fetching feed in background have failed");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("com.ifmomd.igushkin.rss_reader.UPDATE_FAILED");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra("channelId", channelId);
        sendBroadcast(broadcastIntent);
    }

    class RSSFetcher extends RSSGetter {
        FeedsDBAdapter mDbHelper;

        List<RSSItem> workingList;

        RSSFetcher(List<RSSItem> workingList) {
            mDbHelper = new FeedsDBAdapter(FeedFetchingService.this);
            mDbHelper.open();
            this.workingList = workingList;
        }

        @Override
        protected void onPostExecute(List<RSSItem> rssItems) {
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    String dateTimeFormat =
                            currentFormat == Format.Atom? "yyyy-MM-dd'T'HH:mm:ssZ" : "EEE, dd MMM yyyy HH:mm:ss Z";
                    SimpleDateFormat format = new SimpleDateFormat(dateTimeFormat);

                    super.onPostExecute(null);
                    workingList.clear();
                    if (items == null || items.size() == 0) {
                        onFetchFailed();
                    } else {
                        workingList.addAll(items);
                    }
                    for (RSSItem item : workingList) {
                        long time;
                        try {
                            time = item.dateTime != null ? format.parse(item.dateTime.replaceAll("Z$", "+0000")).getTime() : System.currentTimeMillis();
                        } catch (ParseException ex) {
                            time = System.currentTimeMillis();}
                        mDbHelper.createItem(item.title, item.description, item.link, time/1000, channelId);
                    }
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("com.ifmomd.igushkin.rss_reader.CHANNEL_UPDATED");
                    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
                    broadcastIntent.putExtra("channelId", channelId);
                    sendBroadcast(broadcastIntent);
                    return null;
                }
            }.execute(null);
        }
    }
}
