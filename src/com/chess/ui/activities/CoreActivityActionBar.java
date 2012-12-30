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
import android.view.*;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.mopub.mobileads.MoPubView;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String CONNECT_FAILED_TAG = "connect_failed";
	private static final String OBSOLETE_VERSION_TAG = "obsolete version";


	protected Bundle extras;
	protected Handler handler;
	protected boolean showActionSearch;
	protected boolean showActionSettings;
	protected boolean showActionNewGame;
	protected boolean showActionRefresh;

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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && getActionBar() != null) {
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
		if (mainView != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
				mainView.setBackground(backgroundChessDrawable);
			} else {
				mainView.setBackgroundDrawable(backgroundChessDrawable);
			}
		}
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
		backgroundChessDrawable.updateConfig();
		super.onConfigurationChanged(newConfig);
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

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(CONNECT_FAILED_TAG)) {
			if (AppData.isLiveChess(this)) {
				getLccHolder().logout();
			}
			backToHomeActivity();
		} else if (tag.equals(OBSOLETE_VERSION_TAG)) {
			// Show site and
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AppData.setLiveChessMode(getContext(), false);
					LccHolder.getInstance(getContext()).setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(RestHelper.PLAY_ANDROID_HTML)));
				}
			});

			backToHomeActivity();
		}
		super.onPositiveBtnClick(fragment);
	}

	private void adjustActionBar() {
		getActionBarHelper().showMenuItemById(R.id.menu_settings, showActionSettings);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, showActionNewGame);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, showActionRefresh);
		getActionBarHelper().showMenuItemById(R.id.menu_search, showActionSearch);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected(), menu);
		getActionBarHelper().showMenuItemById(R.id.menu_search, showActionSearch, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_settings, showActionSettings, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_new_game, showActionNewGame, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_refresh, showActionRefresh, menu);

		if(HONEYCOMB_PLUS_API){
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
			case R.id.menu_settings:
				startActivity(new Intent(this, PreferencesScreenActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

//	protected abstract class ChessUpdateListener extends ActionBarUpdateListener<String> {
//		public ChessUpdateListener() {
//			super(CoreActivityActionBar.this);
//		}
//	}


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
	public void onSessionExpired(final String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				final LinearLayout customView = (LinearLayout) inflater.inflate(R.layout.popup_relogin_frame, null, false);

				PopupItem popupItem = new PopupItem();
				popupItem.setCustomView(customView);

				PopupCustomViewFragment reLoginFragment = PopupCustomViewFragment.newInstance(popupItem);
				reLoginFragment.show(getSupportFragmentManager(), RE_LOGIN_TAG);

				getLccHolder().logout();

				((TextView) customView.findViewById(R.id.titleTxt)).setText(message);

				EditText usernameEdt = (EditText) customView.findViewById(R.id.usernameEdt);
				EditText passwordEdt = (EditText) customView.findViewById(R.id.passwordEdt);
				setLoginFields(usernameEdt, passwordEdt);

				customView.findViewById(R.id.re_signin).setOnClickListener(CoreActivityActionBar.this);

				LoginButton facebookLoginButton = (LoginButton) customView.findViewById(R.id.re_fb_connect);
				facebookInit(facebookLoginButton);
				facebookLoginButton.logout();

				usernameEdt.setText(AppData.getUserName(CoreActivityActionBar.this));
			}
		});
	}

	@Override
	public void onConnectionFailure(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
			}
		});

		if (isPaused)
			return;

		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG);
		getLastPopupFragment().setButtons(1);
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
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update, OBSOLETE_VERSION_TAG);
		getLastPopupFragment().setButtons(1);
		getLastPopupFragment().setCancelable(false);
	}

	@Override
	public void onFriendsStatusChanged(){

	}

	@Override
	public void onAdminAnnounce(String message) {
		showSinglePopupDialog(message);
		getLastPopupFragment().setButtons(1);
	}

	// -----------------------------------------------------


	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.re_signin){
			signInUser();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == Facebook.DEFAULT_AUTH_ACTIVITY_CODE && facebook != null){
			facebook.authorizeCallback(requestCode, resultCode, data);
		}
	}

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

	@Override
	protected void afterLogin() {
//		restartActivity();
	}
}

