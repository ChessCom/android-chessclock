package com.chess.ui.activities;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.*;
import android.widget.SearchView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.ui.interfaces.PopupDialogFace;
import com.mopub.mobileads.MoPubView;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, ActiveFragmentInterface, PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String CONNECT_FAILED_TAG = "connect_failed";
	protected static final String OBSOLETE_VERSION_TAG = "obsolete version";
	private static final String INFO_MSG_TAG = "info message popup";


	protected Bundle extras;
	protected Handler handler;
	protected boolean showSearch;

	// we may have this add on every screen, so control it on the lowest level
	protected MoPubView moPubView;

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= StaticData.SDK_ICE_CREAM_SANDWICH && getActionBar() != null) {
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_USE_LOGO
					| ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_SHOW_TITLE);
		}

		handler = new Handler();

		extras = getIntent().getExtras();
        LccHolder.getInstance(this).setLiveChessClientEventListener(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		View mainView = findViewById(R.id.mainView);
		if (mainView != null)
			mainView.setBackgroundDrawable(backgroundChessDrawable);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(StaticData.SAVED_STATE, true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		backgroundChessDrawable.updateConfig();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void switchFragment(Fragment fragment) {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		ft.commit();
	}

	public void showProgress(boolean show) {
		getActionBarHelper().setRefreshActionItemState(show);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// try to destroy ad here as MoPub team suggested
		if (moPubView != null) {
			moPubView.destroy();
		}

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();
	}

	protected void backToHomeActivity() {
		Intent intent = new Intent(this, HomeScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	protected void backToLoginActivity() {
		Intent intent = new Intent(this, LoginScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(CONNECT_FAILED_TAG)) {
			if (DataHolder.getInstance().isLiveChess()) {
				getLccHolder().logout();
			}
			backToHomeActivity();
		} else if (fragment.getTag().equals(OBSOLETE_VERSION_TAG)) {
			// Show site and
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DataHolder.getInstance().setLiveChess(false);
					LccHolder.getInstance(getContext()).setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(RestHelper.PLAY_ANDROID_HTML)));
				}
			});

			backToHomeActivity();
		} else if (fragment.getTag().equals(INFO_MSG_TAG)) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected(), menu);
		getActionBarHelper().showMenuItemById(R.id.menu_search, showSearch, menu);
		
		if(Build.VERSION.SDK_INT >= StaticData.SDK_HONEYCOMB){
			// Get the SearchView and set the searchable configuration
			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
			SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				backToHomeActivity();
				break;
		}
		return super.onOptionsItemSelected(item);
	}



	private void checkUserTokenAndStartActivity() { // TODO decide where to use
		if (!AppData.getUserToken(this).equals(StaticData.SYMBOL_EMPTY)) {
			Intent intent = new Intent(this, HomeScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		} else {
			startActivity(new Intent(this, LoginScreenActivity.class));
		}
	}

	// ---------- LiveChessClientEventListenerFace ----------------
	@Override
	public void onConnecting() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
				getActionBarHelper().setRefreshActionItemState(true);
			}
		});
	}

	@Override
	public void onConnectionEstablished() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, true);
			}
		});
	}

	@Override
	public void onConnectionFailure(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d("TEST", "onConnectionFailure/ another login");
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
			}
		});

		if(isPaused)
			return;

		popupDialogFragment.setButtons(1);
		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG);
	}

    @Override
    public void onConnectionBlocked(final boolean blocked) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(blocked);
			}
		});

    }

    @Override
	public void onObsoleteProtocolVersion() {
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update,
				OBSOLETE_VERSION_TAG);
		popupDialogFragment.setButtons(1);
		popupDialogFragment.setCancelable(false);
	}

	@Override
	public void onFriendsStatusChanged(){

	}

	// -----------------------------------------------------

	protected LccHolder getLccHolder() {
		return LccHolder.getInstance(this);
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

}

