package com.chess.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.chess.R;
import com.chess.backend.statics.AppConstants;

/**
 * WebViewActivity class
 *
 * @author alien_roger
 * @created at: 12.08.12 10:10
 */
public class WebViewActivity extends LiveBaseActivity {

	private ProgressBar horizontalProgress;
	private String url;
	private WebView webView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.webview_screen);

		widgetsInit();
		url = getIntent().getStringExtra(AppConstants.EXTRA_WEB_URL);
		String title = getIntent().getStringExtra(AppConstants.EXTRA_TITLE);


		setTitle(title.toUpperCase());
		showActionRefresh = true;
	}

	private void widgetsInit(){
		webView = (WebView) findViewById(R.id.webView);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new MyWebChromeClient());

		horizontalProgress = (ProgressBar) findViewById(R.id.webProgressbar);
	}

	@Override
	protected void onResume() {
		super.onResume();
		webView.loadUrl(url);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_refresh:
				webView.loadUrl(url);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MyWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			getActionBarHelper().setRefreshActionItemState(true);
//			progressBar.setVisibility(View.VISIBLE);
//			showRightButton(false);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			getActionBarHelper().setRefreshActionItemState(false);
//			progressBar.setVisibility(View.GONE);
//			showRightButton(true);
		}

	}


	private class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onProgressChanged(WebView view, int progress) {
			if (progress < 100 && horizontalProgress.getVisibility() == ProgressBar.GONE) {
				horizontalProgress.setVisibility(ProgressBar.VISIBLE);
			}

			horizontalProgress.setProgress(progress);

			if (progress == 100) {
				horizontalProgress.setVisibility(ProgressBar.GONE);
			}
		}
	}

}