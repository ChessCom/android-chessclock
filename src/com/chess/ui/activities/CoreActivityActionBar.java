package com.chess.ui.activities;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.backend.tasks.ConnectLiveChessTask;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.utilities.AppUtils;
import com.mopub.mobileads.MoPubView;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, ActiveFragmentInterface, PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String TAG = "CoreActivityActionBar";
	private static final String CHECK_UPDATE_TAG = "check update";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	protected static final String OBSOLETE_VERSION_TAG = "obsolete version";
	private static final String INFO_MSG_TAG = "info message popup";


	protected Bundle extras;
	protected Handler handler;

	// we may have this add on every screen, so control it on the lowest level
	protected MoPubView moPubView;
	private Boolean forceFlag;
	private LccConnectUpdateListener lccConnectUpdateListener;

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();

		extras = getIntent().getExtras();



		lccConnectUpdateListener = new LccConnectUpdateListener();

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
	protected void onResume() {
		super.onResume();

		if (DataHolder.getInstance().isLiveChess() &&
				!LccHolder.getInstance(this).isConnected() && !LccHolder.getInstance(this).isConnectingInProgress()) {
			new ConnectLiveChessTask(lccConnectUpdateListener).executeTask();
		}

		long startDay = preferences.getLong(AppConstants.START_DAY, 0);
		if (startDay == 0 || !DateUtils.isToday(startDay)) {
			checkUpdate();
		}
	}

	private class LccConnectUpdateListener extends AbstractUpdateListener<Void> {
		public LccConnectUpdateListener() {
			super(getContext());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// try to destroy ad here as Mopub team suggested
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
			final Handler handler = new Handler();
			handler.post(new Runnable() {
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

		} else if (fragment.getTag().equals(CHECK_UPDATE_TAG)) {
			if (forceFlag) {
				// drop start day
				preferencesEditor.putLong(AppConstants.START_DAY, 0);
				preferencesEditor.commit();

				backToLoginActivity();
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.GOOGLE_PLAY_URI));
			startActivity(intent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected(), menu);
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

		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG);
		popupDialogFragment.setButtons(1);
	}

    @Override
    public void onConnectionBlocked() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(true);
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

	private void checkUpdate() {
		new CheckUpdateTask(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
	}

	private class CheckUpdateListener extends AbstractUpdateListener<Boolean> {
		public CheckUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(Boolean returnedObj) {
			forceFlag = returnedObj;
			if (isPaused)
				return;

			showPopupDialog(R.string.update_check, R.string.update_available_please_update,
					CHECK_UPDATE_TAG);
			popupDialogFragment.setButtons(1);
		}
	}

}

