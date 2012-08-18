package com.mopub.mobileads;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.*;
import android.widget.ImageButton;
import android.widget.Toast;
import com.chess.R;

public class MraidBrowser extends Activity {
    
    public static final String URL_EXTRA = "extra_url";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        
        setContentView(R.layout.mraid_browser);
        
        Intent intent = getIntent();
        initializeWebView(intent);
        initializeButtons(intent);
        enableCookies();
    }
    
    private void initializeWebView(Intent intent) {
        WebView webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        
        webSettings.setJavaScriptEnabled(true);
        
        /* Pinch to zoom is apparently not enabled by default on all devices, so
         * declare zoom support explicitly.
         * http://stackoverflow.com/questions/5125851/enable-disable-zoom-in-android-webview
         */
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webView.loadUrl(intent.getStringExtra(URL_EXTRA));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, 
                    String failingUrl) {
                Activity a = (Activity) view.getContext();
                Toast.makeText(a, "MRAID error: " + description, Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url == null) return false;
                
                if (url.startsWith("market:") || url.startsWith("tel:") || 
                        url.startsWith("voicemail:") || url.startsWith("sms:") || 
                        url.startsWith("mailto:") || url.startsWith("geo:") || 
                        url.startsWith("google.streetview:")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					finish();
                    return true;
                }
                return false;
            }
            
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                ImageButton forwardButton = (ImageButton) findViewById(R.id.browserForwardButton);
                forwardButton.setImageResource(R.drawable.unrightarrow);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                ImageButton backButton = (ImageButton) findViewById(R.id.browserBackButton);
                int backImageResource = (view.canGoBack()) ? 
                        R.drawable.leftarrow : R.drawable.unleftarrow;
                backButton.setImageResource(backImageResource);

                ImageButton forwardButton = (ImageButton) findViewById(R.id.browserForwardButton);
                int fwdImageResource = (view.canGoForward()) ? 
                        R.drawable.rightarrow : R.drawable.unrightarrow;
                forwardButton.setImageResource(fwdImageResource);
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                Activity a = (Activity) view.getContext();
                a.setTitle(getString(R.string.loading));
                a.setProgress(progress * 100);
                if (progress == 100) a.setTitle(view.getUrl());
            }
        });
    }
    
    private void initializeButtons(Intent intent) {
        ImageButton backButton = (ImageButton) findViewById(R.id.browserBackButton);
        backButton.setBackgroundColor(Color.TRANSPARENT);
        backButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WebView webView = (WebView) findViewById(R.id.webView);
                if (webView.canGoBack()) webView.goBack();
            }
        });
        
        ImageButton forwardButton = (ImageButton) findViewById(R.id.browserForwardButton);
        forwardButton.setBackgroundColor(Color.TRANSPARENT);
        forwardButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WebView webView = (WebView) findViewById(R.id.webView);
                if (webView.canGoForward()) webView.goForward();
            }
        });
        
        ImageButton refreshButton = (ImageButton) findViewById(R.id.browserRefreshButton);
        refreshButton.setBackgroundColor(Color.TRANSPARENT);
        refreshButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WebView webView = (WebView) findViewById(R.id.webView);
                webView.reload();
            }
        });
        
        ImageButton closeButton = (ImageButton) findViewById(R.id.browserCloseButton);
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        closeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MraidBrowser.this.finish();
            }
        });
    }
    
    private void enableCookies() {
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        CookieSyncManager.getInstance().stopSync();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        CookieSyncManager.getInstance().startSync();
    }
}
