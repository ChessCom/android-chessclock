package com.chess.ui.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import com.chess.R;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.13
 * Time: 8:37
 */
public class WebViewFragment extends CommonLogicFragment {

	private static final String URL = "url";
	private static final String TITLE = "title";

	private ProgressBar horizontalProgress;
	private String url;
	private String title;
	private WebView webView;

	public WebViewFragment() {
	}

	public static WebViewFragment createInstance(String url, String title) {
		WebViewFragment fragment = new WebViewFragment();

		Bundle arguments = new Bundle();
		arguments.putString(URL, url);
		arguments.putString(TITLE, title);
		fragment.setArguments(arguments);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			url = getArguments().getString(URL);
			title = getArguments().getString(TITLE);
		} else {
			url = savedInstanceState.getString(URL);
			title = savedInstanceState.getString(TITLE);
		}

		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.web_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(AppUtils.upCaseFirst(title));

		widgetsInit(view);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(URL, url);
		outState.putString(TITLE, title);
	}

	private void widgetsInit(View view) {
		webView = (WebView) view.findViewById(R.id.webView);

//		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new MyWebChromeClient());

		horizontalProgress = (ProgressBar) view.findViewById(R.id.webProgressbar);

		getActivityFace().setPullToRefreshView(webView, this);

	}

	@Override
	public void onResume() {
		super.onResume();
		webView.loadUrl(url);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
			showLoadingProgress(true);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			showLoadingProgress(false);
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
