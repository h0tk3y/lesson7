package com.ifmomd.igushkin.rss_reader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sergey on 10/24/13.
 */
public class FeedsActivity extends Activity implements AdapterView.OnItemClickListener {

    FeedsDBAdapter mDbHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new FeedsDBAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.feeds_activity_layout);
        lstFeeds = (ListView) findViewById(R.id.lstFeeds);
        lstFeeds.setOnItemClickListener(this);
        setListContent();
    }

    ListView lstFeeds;

    private void setListContent() {
        Cursor c = mDbHelper.fetchAllChannels();
        if (c.getCount() == 0)
            Toast.makeText(this, "Add a channel to get started :)", Toast.LENGTH_LONG);
        startManagingCursor(c);
        String[] from = new String[]{FeedsDBAdapter.KEY_NAME, FeedsDBAdapter.KEY_LINK};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, c, from, to);
        lstFeeds.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuAddFeed) {
            final EditText inputName = new EditText(this);
            final EditText inputUrl = new EditText(this);
            inputUrl.setText("http://");
            inputUrl.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(inputName);
            linearLayout.addView(inputUrl);
            inputName.setHint(getString(R.string.feedName));
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dlgInput_title)
                    .setView(linearLayout)
                    .setPositiveButton(getString(R.string.dlgChangeFeed_Ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (inputName.getText().length() > 0 && inputUrl.length() > 0)
                                mDbHelper.createChannel(inputName.getText().toString(), inputUrl.getText().toString(), 0);
                            setListContent();
                        }
                    }).setNegativeButton(getString(R.string.dlgChangeFeed_Cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    ArrayList<RSSFeed> feeds = new ArrayList<RSSFeed>();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent result = new Intent();
        Cursor item = (Cursor)((SQLiteCursor)parent.getAdapter().getItem(position));
        String name = item.getString(item.getColumnIndex(FeedsDBAdapter.KEY_NAME));
        String link = item.getString(item.getColumnIndex(FeedsDBAdapter.KEY_LINK));
        long lastUpdate = item.getLong(item.getColumnIndex(FeedsDBAdapter.KEY_LAST_UPDATE));
        result.putExtra("id", id);
        result.putExtra("name", name);
        result.putExtra("link", link);
        result.putExtra("lastUpdated", lastUpdate);
        setResult(RESULT_OK, result);
        finish();
    }
}

class RSSFeed implements Serializable {
    String name;
    String url;

    RSSFeed(String name, String url) {
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        return name;
    }
}