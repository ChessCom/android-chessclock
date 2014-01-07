package com.chess.ui.activities;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.SearchView;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.interfaces.PopupDialogFace;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, PopupDialogFace {

	protected Bundle extras;
	protected Handler handler;

	private SparseBooleanArray actionMenuMap;

	public void setFullScreen() {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);  // TODO solve problem for QVGA screens
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adjustActionBarHomeIcon();


		handler = new Handler();
		extras = getIntent().getExtras();
		actionMenuMap = new SparseBooleanArray();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void adjustActionBarHomeIcon() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && getActionBar() != null) {
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_SHOW_CUSTOM);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && getActionBar() != null) {
			hideUpIcon(getActionBar());
		}
	}

	@TargetApi(18)
	private void hideUpIcon(ActionBar actionBar) {
		actionBar.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_ab_back_empty));
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
		for (int i = 0; i < actionMenuMap.size(); i++) {
			int key = actionMenuMap.keyAt(i);
			boolean value = actionMenuMap.valueAt(i);
			getActionBarHelper().showMenuItemById(key, value);
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
				hideKeyBoard();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.new_action_menu, menu);

		for (int i = 0; i < actionMenuMap.size(); i++) {
			int key = actionMenuMap.keyAt(i);
			boolean value = actionMenuMap.valueAt(i);
			getActionBarHelper().showMenuItemById(key, value, menu);
		}


		if (HONEYCOMB_PLUS_API) {
			adjustSearchView(menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void adjustSearchView(Menu menu) {
		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		MenuItem menuItem = menu.findItem(R.id.menu_search);
		if (menuItem != null) {
			SearchView searchView = (SearchView) menuItem.getActionView();
			if (searchView != null) {
				// change text color
				int searchTextViewId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
				TextView searchTextView = (TextView) searchView.findViewById(searchTextViewId);
				searchTextView.setTextColor(getResources().getColor(R.color.white));
				searchTextView.setHintTextColor(getResources().getColor(R.color.hint_text));
				searchView.setIconifiedByDefault(false);

				searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String query) {
						return false;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						onSearchAutoCompleteQuery(newText);
						return false;
					}
				});

				searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			}
		}
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
