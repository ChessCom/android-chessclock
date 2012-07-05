package com.chess.ui.activities;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.backend.tasks.ConnectLiveChessTask;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.fragments.PopupProgressFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreActivityActionBar extends ActionBarActivity implements View.OnClickListener
		, ActiveFragmentInterface, PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String TAG = "CoreActivityActionBar";
	private static final String CHECK_UPDATE_TAG = "check update";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	protected static final String OBSOLETE_VERSION_TAG = "obsolete version";
	//	protected static final String ERROR_TAG = "error happend";
	private static final String INFO_POPUP_TAG = "information popup";
	private static final String INFO_MSG_TAG = "info message popup";
	private static final String PROGRESS_TAG = "progress dialog popup";


	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	protected BackgroundChessDrawable backgroundChessDrawable;
	protected PopupItem popupItem;
	protected PopupDialogFragment popupDialogFragment;
	protected PopupItem popupProgressItem;
	protected PopupProgressFragment popupProgressDialogFragment;
	protected List<PopupDialogFragment> popupManager;

	protected Handler handler;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;


	// we may have this add on every screen, so control it on the lowest level
	protected MoPubView moPubView;
	private Boolean forceFlag;
	protected boolean isPaused;
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
		backgroundChessDrawable = new BackgroundChessDrawable(this);

		popupItem = new PopupItem();
		popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
		popupProgressItem = new PopupItem();
		popupProgressDialogFragment = PopupProgressFragment.newInstance(popupProgressItem);

		popupManager = new ArrayList<PopupDialogFragment>();

		extras = getIntent().getExtras();

		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();

		AppUtils.changeLocale(this);

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

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

	protected void widgetsInit() {

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
		isPaused = false;

		if (LccHolder.getInstance(this).isNotConnectedToLive()) {
			new ConnectLiveChessTask(lccConnectUpdateListener).executeTask();
		}

		registerReceivers();

		if (preferences.getLong(AppConstants.FIRST_TIME_START, 0) == 0) {
			preferencesEditor.putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
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

		@Override
		public void showProgress(boolean show) {
		}

	}

	protected void onLccConnectFail(Integer resultCode) {
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;

		// try to destroy ad here as Mopub team suggested
		if (moPubView != null) {
			moPubView.destroy();
		}

		unRegisterReceivers();

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

	}

	private void registerReceivers() {
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));
	}

	private void unRegisterReceivers() {
		unregisterReceiver(infoMessageReceiver);
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

	public void dismissAllPopups() {
		for (PopupDialogFragment fragment : popupManager) {
			fragment.getDialog().dismiss();
		}
		popupManager.clear();
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
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
		fragment.getDialog().dismiss();
	}

	@Override
	public void onNeutralBtnCLick(DialogFragment fragment) {
		fragment.getDialog().dismiss();
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		fragment.getDialog().dismiss();
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

	private void checkUserTokenAndStartActivity() {
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
				Log.d("TEST", "show progress from CoreAcb");

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
				Log.d("TEST", "HIDE progress from CoreAcb");

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
	public void onObsoleteProtocolVersion() {
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update,
				OBSOLETE_VERSION_TAG);
		popupDialogFragment.setButtons(1);
		popupDialogFragment.getDialog().setCancelable(false);
	}

	// -----------------------------------------------------

	protected LccHolder getLccHolder() {
		return LccHolder.getInstance(this);
	}

	private final BroadcastReceiver infoMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());

			String message = intent.getExtras().getString(AppConstants.MESSAGE);
			String title = intent.getExtras().getString(AppConstants.TITLE);

			showPopupDialog(title, message);
			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(title);
			popupItem.setMessage(message);

			PopupDialogFragment popupFragment = PopupDialogFragment.newInstance(popupItem, CoreActivityActionBar.this);
			popupFragment.show(getSupportFragmentManager(), INFO_MSG_TAG);
		}
	};


	public ActionBarHelper provideActionBarHelper() {
		return getActionBarHelper();
	}

	protected CoreActivityActionBar getInstance() {
		return this;
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {// TODO don't do any hacks
		try {
			super.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// hack for Android's IllegalArgumentException: Receiver not registered
		}
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(this);
	}

	protected String getTextFromField(EditText editText) {
		return editText.getText().toString().trim();
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FlurryData.API_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
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

	protected void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int msgId) {
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}

	// Single button no callback dialogs
	protected void showSinglePopupDialog(int titleId, int messageId) {
		showPopupDialog(titleId, messageId, INFO_POPUP_TAG);
		popupDialogFragment.setButtons(1);
	}

	protected void showSinglePopupDialog(String title, String message) {
		showPopupDialog(title, message, INFO_POPUP_TAG);
		popupDialogFragment.setButtons(1);
	}

	protected void showSinglePopupDialog(int titleId, String message) {
		showPopupDialog(titleId, message, INFO_POPUP_TAG);
		popupDialogFragment.setButtons(1);
	}

	protected void showSinglePopupDialog(String message) {
		showPopupDialog(message, INFO_POPUP_TAG);
		popupDialogFragment.setButtons(1);
	}

	protected void showSinglePopupDialog(int messageId) {
		showPopupDialog(messageId, INFO_POPUP_TAG);
		popupDialogFragment.setButtons(1);
	}

	// Default Dialogs
	protected void showPopupDialog(int titleId, int messageId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(messageId);
		popupDialogFragment.updatePopupItem(popupItem);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	protected void showPopupDialog(int titleId, String messageId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(messageId);
		popupDialogFragment.updatePopupItem(popupItem);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}


	protected void showPopupDialog(String title, String message, String tag) {
		popupItem.setTitle(title);
		popupItem.setMessage(message);
		popupDialogFragment.updatePopupItem(popupItem);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	protected void showPopupDialog(int titleId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		popupDialogFragment.updatePopupItem(popupItem);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	protected void showPopupDialog(String title, String tag) {
		popupItem.setTitle(title);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		popupDialogFragment.updatePopupItem(popupItem);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	// Progress Dialogs
	protected void showPopupProgressDialog(String title) {
		popupProgressItem.setTitle(title);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		popupDialogFragment.updatePopupItem(popupItem);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void showPopupProgressDialog(String title, String message) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(message);
		popupDialogFragment.updatePopupItem(popupItem);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void showPopupProgressDialog(int titleId) {
		popupProgressItem.setTitle(titleId);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		popupDialogFragment.updatePopupItem(popupItem);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void showPopupHardProgressDialog(int titleId) {
		popupProgressItem.setTitle(titleId);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		popupDialogFragment.updatePopupItem(popupItem);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
		popupProgressDialogFragment.setNotCancelable();
	}

	protected void showPopupProgressDialog(int titleId, int messageId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(messageId);
		popupDialogFragment.updatePopupItem(popupItem);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void dismissProgressDialog() {
		if (popupProgressDialogFragment != null && popupProgressDialogFragment.getDialog() != null)
			popupProgressDialogFragment.getDialog().dismiss();
	}

	protected Context getContext() {
		return this;
	}

}

