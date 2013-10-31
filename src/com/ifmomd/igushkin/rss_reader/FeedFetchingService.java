package com.ifmomd.igushkin.rss_reader;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
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
            super.onPostExecute(null);
            workingList.clear();
            if (items == null || items.size() == 0) {
                onFetchFailed();
            } else {
                workingList.addAll(items);
            }
            for (RSSItem item : workingList) {
                mDbHelper.createItem(item.title, item.description, item.link, System.currentTimeMillis()/1000, channelId);
            }
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("com.ifmomd.igushkin.rss_reader.CHANNEL_UPDATED");
            broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
            broadcastIntent.putExtra("channelId", channelId);
            sendBroadcast(broadcastIntent);
        }
    }
}
