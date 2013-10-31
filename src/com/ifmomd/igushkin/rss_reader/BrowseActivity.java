package com.ifmomd.igushkin.rss_reader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowseActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_activity_layout);
        WebView wv = (WebView) findViewById(R.id.wvContent);

        wv.getSettings().setBuiltInZoomControls(true);

        wv.getSettings().setDefaultTextEncodingName("utf-8");
        wv.loadDataWithBaseURL(null, getIntent().getStringExtra("data"), "text/html", "en_US", null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().hasExtra("link")) {
            getMenuInflater().inflate(R.menu.browse_activity_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuGoTo) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getIntent().getStringExtra("link")));
            startActivity(browserIntent);
        }
        return super.onOptionsItemSelected(item);
    }
}