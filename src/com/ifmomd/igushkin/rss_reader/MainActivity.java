package com.ifmomd.igushkin.rss_reader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {
    FeedsDBAdapter mDbHelper;

    RSSChannel currentChannel;

    ListView           lstItems;
    ArrayList<RSSItem> rssItems;
    ArrayList<RSSChannel> rssChannels = new ArrayList<RSSChannel>();

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentChannel != null && intent.hasExtra("channelId") && intent.getLongExtra("channelId", -1) == currentChannel.id) {
                hideProgress();
                fillData();
            }
        }
    };

    private BroadcastReceiver failReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentChannel != null && intent.hasExtra("channelId") && intent.getLongExtra("channelId", -1) == currentChannel.id) {
                onFetchFailed();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter f = new IntentFilter();
        f.addAction("com.ifmomd.igushkin.rss_reader.CHANNEL_UPDATED");
        f.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(updateReceiver, f);
        f = new IntentFilter();
        f.addAction("com.ifmomd.igushkin.rss_reader.UPDATE_FAILED");
        f.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(failReceiver, f);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
        unregisterReceiver(failReceiver);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new FeedsDBAdapter(this);
        mDbHelper.open();

        onResume();
        setContentView(R.layout.feed_activity_layout);
        lstItems = (ListView) findViewById(R.id.lstItems);
        lstItems.setItemsCanFocus(false);
        lstItems.setOnItemClickListener(this);
        updateItemsList();

        if (mDbHelper.fetchAllChannels().getCount() == 0) {
            selectFeed();
        }
    }

    private void fillData() {
        Cursor itemsCursor;
        if (currentChannel == null)
            itemsCursor = mDbHelper.fetchAllItems();
        else
            itemsCursor = mDbHelper.fetchItemsByChannel(currentChannel.id);
        startManagingCursor(itemsCursor);
        String[] source = new String[]{FeedsDBAdapter.KEY_TITLE};
        int[] target = new int[]{android.R.id.text1};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, itemsCursor, source, target);
        lstItems.setAdapter(adapter);
        hideProgress();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("channel", currentChannel);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        currentChannel = (RSSChannel)savedInstanceState.getSerializable("channel");
    }

    private void updateCurrentFeed() {
        Intent i = new Intent(this, FeedFetchingService.class);
        i.putExtra("channelId", currentChannel != null ? currentChannel.id : -1);
        i.putExtra("url", currentChannel!= null? currentChannel.link : "");
        startService(i);
    }

    private void updateItemsList() {
        updateCurrentFeed();
        showProgress();
        fillData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fetch_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            currentChannel = (RSSChannel)data.getSerializableExtra("channel");
            long id = data.getLongExtra("id",-1);
            String name = data.getStringExtra("name");
            String link = data.getStringExtra("link");
            long lastUpdated = data.getLongExtra("lastUpdated", 0);
            currentChannel = new RSSChannel();
            currentChannel.id = id;
            currentChannel.title = name;
            currentChannel.link = link;
            currentChannel.lasUpdated = lastUpdated;
            updateItemsList();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuChangeSource) {
            selectFeed();
        }
        if (item.getItemId() == R.id.mnuRefresh) {
            updateItemsList();
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectFeed() {Intent i = new Intent(this, FeedsActivity.class);
        startActivityForResult(i, 0);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RSSItem r = (RSSItem) parent.getAdapter().getItem(position);
        Intent i = new Intent(this, BrowseActivity.class);
        i.putExtra("data", r.description);
        i.putExtra("use UTF-8", r.link.contains("bash")); //horrible piece of shit
        startActivity(i);
    }

    private void showProgress() {
        findViewById(R.id.pbProgress).setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        findViewById(R.id.pbProgress).setVisibility(View.INVISIBLE);
    }

    private void onFetchFailed() {
        Toast.makeText(this, R.string.toaFetchFailed, Toast.LENGTH_SHORT).show();
    }

}