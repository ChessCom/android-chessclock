package com.chess.ui.activities;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.SearchView;
import com.chess.R;
import com.chess.ui.interfaces.PopupDialogFace;
import com.inneractive.api.ads.InneractiveAd;

import java.util.HashMap;
import java.util.Map;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, PopupDialogFace {

	protected Bundle extras;
	protected Handler handler;

	private HashMap<Integer, Boolean> actionMenuMap;

	// we may have this add on every screen, so control it on the lowest level
	//protected MoPubView moPubView;
	protected InneractiveAd inneractiveBannerAd;
	protected InneractiveAd inneractiveRectangleAd;

	public void setFullScreen() {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);  // TODO solve problem for QVGA screens
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && getActionBar() != null) {
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_SHOW_CUSTOM);
		}

		handler = new Handler();
		extras = getIntent().getExtras();
		actionMenuMap = new HashMap<Integer, Boolean>();
	}

	protected void initUpgradeAndAdWidgets() {
//		if (!AppUtils.isNeedToUpgrade(this)) {
//			findViewById(R.id.bannerUpgradeView).setVisibility(View.GONE);
//		} else {
//			findViewById(R.id.bannerUpgradeView).setVisibility(View.VISIBLE);
//		}
//
//		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
//		upgradeBtn.setOnClickListener(this);
//
//		inneractiveBannerAd = (InneractiveAd) findViewById(R.id.inneractiveBannerAd);
//		InneractiveAdHelper.showBannerAd(upgradeBtn, inneractiveBannerAd, this);
	}


	@Override
	protected void onStart() {
		if (HONEYCOMB_PLUS_API) {
			adjustActionBar();
		}
		super.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!HONEYCOMB_PLUS_API) {
			adjustActionBar();
		}
	}



	@Override
	public void onConfigurationChanged(Configuration newConfig) {
//		backgroundChessDrawable.updateConfig();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPause() {
		super.onPause();

	    /* // try to destroy ad here as MoPub team suggested
		if (moPubView != null) {
		moPubView.destroy();
    	}*/
		/*preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();*/
	}

	@Override
	protected void onDestroy() {
//		if (inneractiveBannerAd != null) {
//			inneractiveBannerAd.cleanUp();
//		}
//		if (inneractiveRectangleAd != null) {
//			inneractiveRectangleAd.cleanUp();
//		}
		super.onDestroy();
	}

	public void adjustActionBar() {
		for (Map.Entry<Integer, Boolean> entry : actionMenuMap.entrySet()) {
			getActionBarHelper().showMenuItemById(entry.getKey(), entry.getValue());
		}
	}

	protected void enableActionMenu(int menuId, boolean show) {
		actionMenuMap.put(menuId, show);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				getSlidingMenu().toggle();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.new_action_menu, menu);

		for (Map.Entry<Integer, Boolean> entry : actionMenuMap.entrySet()) {
			getActionBarHelper().showMenuItemById(entry.getKey(), entry.getValue(), menu);
		}

		if(HONEYCOMB_PLUS_API){
			// Get the SearchView and set the searchable configuration
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			MenuItem menuItem = menu.findItem(R.id.menu_search);
			if (menuItem != null) {
				SearchView searchView = (SearchView) menuItem.getActionView();
				if (searchView != null)
					searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			}
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Because this activity has set launchMode="singleTop", the system calls this method
		// to deliver the intent if this activity is currently the foreground activity when
		// invoked again (when the user executes a search from this activity, we don't create
		// a new instance of this activity, so the system delivers the search intent here)
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			onSearchQuery(query);
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			onSearchQuery(query);
		}
	}

	@Override
	protected void onSearchAutoCompleteQuery(String query) {

	}

	@Override
	protected void onSearchQuery(String query) {
//		Intent intent = new Intent(this, VideoListActivity.class);
//		intent.putExtra(RestHelper.P_KEYWORD, query);
//		startActivity(intent);
	}



	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.re_signin){
			signInUser();
		}
	}

	public ActionBarHelper provideActionBarHelper() {
		return getActionBarHelper();
	}

	protected CoreActivityActionBar getInstance() {
		return this;
	}

	@Override
	protected void afterLogin() {

	}
}
