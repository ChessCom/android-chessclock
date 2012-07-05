package com.chess.ui.activities;

import actionbarcompat.ActionBarActivityHome;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.fragments.PopupProgressFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreActivityHome extends ActionBarActivityHome implements PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String TAG = "CoreActivityHome";
	private static final String CHECK_UPDATE_TAG = "check update";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	public static final String OBSOLETE_VERSION_TAG = "obsolete version";

	private static final String INFO_POPUP_TAG = "information popup";
	private static final String PROGRESS_TAG = "progress dialog popup";

	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	protected String response = StaticData.SYMBOL_EMPTY;
	protected String responseRepeatable = StaticData.SYMBOL_EMPTY;

	protected Context context;
	protected boolean isPaused;

	protected PopupDialogFragment popupDialogFragment;
	protected PopupItem popupItem;
	protected PopupItem popupProgressItem;
	protected PopupProgressFragment popupProgressDialogFragment;
	protected List<PopupDialogFragment> popupManager;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean(StaticData.SAVED_STATE)) {
				checkUserTokenAndStartActivity();
			}
		}

		context = this;

		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		popupItem = new PopupItem();
		popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
		popupProgressItem = new PopupItem();
		popupProgressDialogFragment = PopupProgressFragment.newInstance(popupProgressItem);

		popupManager = new ArrayList<PopupDialogFragment>();

		LccHolder.getInstance(this).setLiveChessClientEventListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(StaticData.SAVED_STATE, true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPaused = false;

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

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;

		unRegisterReceivers();

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		//mainApp.setForceBannerAdOnFailedLoad(false);

		if (progressDialog != null)
			progressDialog.dismiss();
	}

	private void registerReceivers() {
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));
	}

	private void unRegisterReceivers() {
		unregisterReceiver(infoMessageReceiver);
	}

	private void checkUserTokenAndStartActivity() {
		if (!AppData.getUserName(this).equals(StaticData.SYMBOL_EMPTY)) {
			Intent intent = new Intent(this, HomeScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			startActivity(new Intent(this, LoginScreenActivity.class));
		}
	}

	protected LccHolder getLccHolder() {
		return LccHolder.getInstance(this);
	}

	private final BroadcastReceiver infoMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			final TextView messageView = new TextView(context);
			messageView.setMovementMethod(LinkMovementMethod.getInstance());
			messageView.setText(Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE)));
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			messageView.setGravity(Gravity.CENTER);

			new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setCancelable(true)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE))
					.setView(messageView)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, int whichButton) {
							final Handler handler = new Handler();
							handler.post(new Runnable() {
								@Override
								public void run() {
									dialog.dismiss();
								}
							});
						}
					}).create().show();
		}
	};

	public void dismissAllPopups() {
		for (PopupDialogFragment fragment : popupManager) {
			fragment.getDialog().dismiss();
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		if (fragment.getTag().equals(CHECK_UPDATE_TAG)) {
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
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FlurryData.API_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
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
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
			}
		});

		showPopupDialog(R.string.warning, message, CONNECT_FAILED_TAG);
		popupDialogFragment.setButtons(1);
	}

    @Override
    public void onConnectionBlocked() {
        // TODO To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
	public void onObsoleteProtocolVersion() {
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update,
				OBSOLETE_VERSION_TAG);
		popupDialogFragment.setButtons(1);
		popupDialogFragment.getDialog().setCancelable(false);
	}

	// -----------------------------------------------------


	private void checkUpdate() {
		new CheckUpdateTask(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
	}

	private boolean forceFlag;


	private class CheckUpdateListener extends AbstractUpdateListener<Boolean> {
		public CheckUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
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

	protected void backToLoginActivity() {
		Intent intent = new Intent(this, LoginScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
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