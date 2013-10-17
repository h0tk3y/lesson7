package com.ifmomd.igushkin.rss_reader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Sergey on 10/17/13.
 */
public class BrowseActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_activity_layout);
        WebView wv = (WebView)findViewById(R.id.wvContent);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        wv.getSettings().setBuiltInZoomControls(true);

        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl(getIntent().getStringExtra("uri"));
    }
}