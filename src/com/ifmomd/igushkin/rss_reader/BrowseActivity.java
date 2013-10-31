package com.ifmomd.igushkin.rss_reader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class BrowseActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_activity_layout);
        WebView wv = (WebView)findViewById(R.id.wvContent);

        wv.getSettings().setBuiltInZoomControls(true);

        wv.getSettings().setDefaultTextEncodingName("utf-8");
        wv.loadDataWithBaseURL(null, getIntent().getStringExtra("data"), "text/html", "en_US", null);
    }
}