package com.ifmomd.igushkin.rss_reader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sergey on 10/17/13.
 */
public class FeedActivity extends Activity implements AdapterView.OnItemClickListener {
    ListView lstItems;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_activity_layout);
        lstItems = (ListView)findViewById(R.id.lstItems);
        List<RSSItem> rssItems = new ArrayList<RSSItem>();
        lstItems.setAdapter(new ArrayAdapter<RSSItem>(this,android.R.layout.simple_list_item_1,android.R.id.text1,rssItems));
        lstItems.setOnItemClickListener(this);
        RSSFetcher f = new RSSFetcher(rssItems);
        f.execute("http://habrahabr.ru/rss");
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        RSSItem r = (RSSItem)parent.getAdapter().getItem(position);
        Intent i = new Intent(this, BrowseActivity.class);
        i.putExtra("uri",r.link);
        startActivity(i);
    }


    class RSSFetcher extends RSSGetter {
        List<RSSItem> workingList;

        RSSFetcher(List<RSSItem> workingList) {
            this.workingList = workingList;
        }

        @Override
        protected void onPostExecute(List<RSSItem> rssItems) {
            super.onPostExecute(rssItems);
            workingList.clear();
            workingList.addAll(rssItems);
            ((ArrayAdapter<RSSItem>) lstItems.getAdapter()).notifyDataSetChanged();
        }
    }
}