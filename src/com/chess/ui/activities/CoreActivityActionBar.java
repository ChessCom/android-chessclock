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
import android.util.Log;
import android.view.*;
import android.widget.SearchView;
import com.chess.R;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.inneractive.api.ads.InneractiveAd;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, PopupDialogFace {

	protected Bundle extras;
	protected Handler handler;
	protected boolean showActionSearch;
	protected boolean showActionSettings;
	protected boolean showActionNewGame;
	protected boolean showActionRefresh;

	// we may have this add on every screen, so control it on the lowest level
	//protected MoPubView moPubView;
	protected InneractiveAd inneractiveBannerAd;
	protected InneractiveAd inneractiveRectangleAd;

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && getActionBar() != null) {
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_USE_LOGO
					| ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_SHOW_TITLE);
		}

		handler = new Handler();
		extras = getIntent().getExtras();
	}

	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		View mainView = findViewById(R.id.mainView);
		if (mainView != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				mainView.setBackground(backgroundChessDrawable);
			} else {
				mainView.setBackgroundDrawable(backgroundChessDrawable);
			}
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		backgroundChessDrawable.updateConfig();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onResume() {
		super.onResume();
		adjustActionBar();
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

	protected void adjustActionBar() {
		getActionBarHelper().showMenuItemById(R.id.menu_settings, showActionSettings);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, showActionNewGame);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, showActionRefresh);
		getActionBarHelper().showMenuItemById(R.id.menu_search, showActionSearch);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				backToHomeActivity();
				break;
			case R.id.menu_settings:
				startActivity(new Intent(this, PreferencesScreenActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_search, showActionSearch, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, showActionSettings, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, showActionNewGame, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, showActionRefresh, menu);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			// Get the SearchView and set the searchable configuration
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		}
		return super.onCreateOptionsMenu(menu);
	}

	protected abstract class ChessUpdateListener extends ActionBarUpdateListener<String> {
		public ChessUpdateListener() {
			super(CoreActivityActionBar.this);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.re_signin) {
			signInUser();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK && requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE && facebook != null) {
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}

	public ActionBarHelper provideActionBarHelper() {
		return getActionBarHelper();
	}

	protected CoreActivityActionBar getInstance() {
		return this;
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(this);
	}

	@Override
	protected void afterLogin() {
//		restartActivity();
	}
}
