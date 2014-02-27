package com.chess.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.model.PopupItem;
import com.chess.statics.AppConstants;
import com.chess.statics.FlurryData;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.popup_fragments.PopupDialogFragment;
import com.chess.ui.fragments.popup_fragments.PopupProgressFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.interfaces.PopupShowFace;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.newrelic.agent.android.NewRelic;
import com.slidingmenu.lib.app.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseFragmentPopupsActivity class
 *
 * @author alien_roger
 * @created at: 07.07.12 6:42
 */
public abstract class BaseFragmentPopupsActivity extends BaseActivity implements PopupDialogFace, PopupShowFace {

	protected static final boolean HONEYCOMB_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	public static final boolean JELLYBEAN_1_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

	private static final String INFO_POPUP_TAG = "information popup";
	private static final String PROGRESS_TAG = "progress dialog popup";
	protected static final String NETWORK_CHECK_TAG = "network check popup";
	protected static final int NETWORK_REQUEST = 3456;
	public static final String CHESS_NO_ACCOUNT_TAG = "chess no account popup";
	protected static final String CHECK_UPDATE_TAG = "check update";
	private static final boolean DEVELOPER_MODE = false;

	private Context context;
	protected PopupItem popupItem;
	protected PopupItem popupProgressItem;
	protected List<PopupDialogFragment> popupManager;
	protected List<PopupProgressFragment> popupProgressManager;

	protected boolean isPaused;
	private boolean changingConfiguration;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	public BaseFragmentPopupsActivity() {
		super(R.string.chess_com);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (DEVELOPER_MODE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
						.detectAll()   // or .detectAll() for all detectable problems
						.penaltyLog()
						.build());
				StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
						.detectAll()
						.penaltyLog()
						.penaltyDeath()
						.build());
			}
		}
		super.onCreate(savedInstanceState);

		// Bugsense integration
		try {
			BugSenseHandler.initAndStartSession(this, AppConstants.BUGSENSE_API_KEY);
		} catch (Exception e) {
			e.printStackTrace();
			String stackTrace = Log.getStackTraceString(e).replaceAll("\n", " ");
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.EXCEPTION, Build.MODEL + " " + stackTrace);
			FlurryAgent.logEvent(FlurryData.BUGSENSE_INIT_EXCEPTION, params);
		}

		// New Relic integration
		NewRelic.withApplicationToken(AppConstants.NEW_RELIC_API_KEY).start(this.getApplication());

		context = this;

		popupItem = new PopupItem();
		popupProgressItem = new PopupItem();

		popupManager = new ArrayList<PopupDialogFragment>();
		popupProgressManager = new ArrayList<PopupProgressFragment>();
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPaused = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		isPaused = false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		changingConfiguration = true;
	}

	public boolean isChangingConfiguration() {
		return changingConfiguration;
	}

	protected void unRegisterMyReceiver(BroadcastReceiver broadcastReceiver) {
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		dismissFragmentDialog(fragment);
	}

	@Override
	public void onNeutralBtnCLick(DialogFragment fragment) {
		dismissFragmentDialog(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		dismissFragmentDialog(fragment);
	}

	private void dismissFragmentDialog(DialogFragment fragment) {
		popupItem.setPositiveBtnId(R.string.ic_check);
		popupItem.setNegativeBtnId(R.string.ic_close);
		fragment.setCancelable(true);
		fragment.dismiss();
		popupManager.remove(fragment);
	}

	protected Fragment findFragmentByTag(String tag) {
		return getSupportFragmentManager().findFragmentByTag(tag);
	}

	protected Fragment findFragmentById(int id) {
		return getSupportFragmentManager().findFragmentById(id);
	}

	public void showKeyBoard(EditText editText) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
	}

	public void hideKeyBoard(View editText) {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.content_frame).getWindowToken(), 0);
	}

	protected void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int msgId) {
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}

	// Single button no callback dialogs
	@Override
	public void showSinglePopupDialog(int titleId, int messageId) {
		popupItem.setButtons(1);
		showPopupDialog(titleId, messageId, INFO_POPUP_TAG);
	}

	protected void showSinglePopupDialog(String title, String message) {
		popupItem.setButtons(1);
		showPopupDialog(title, message, INFO_POPUP_TAG);
	}

	@Override
	public void safeShowSinglePopupDialog(int titleId, String message) {
		if (isPaused)
			return;

		// temporary handling i18n manually
		final String messageI18n = AppUtils.getI18nStringForAPIError(context, message);
		showPopupDialog(titleId, messageI18n, INFO_POPUP_TAG, 1);
	}


	protected void showSinglePopupDialog(int titleId, String message) {
		// temporary handling i18n manually
		final String messageI18n = AppUtils.getI18nStringForAPIError(context, message);
		showPopupDialog(titleId, messageI18n, INFO_POPUP_TAG, 1);
	}

	protected void showSinglePopupDialog(String message) {
		popupItem.setButtons(1);
		showPopupDialog(message, INFO_POPUP_TAG);
	}

	protected void showSinglePopupDialog(int messageId) {
		popupItem.setButtons(1);
		showPopupDialog(messageId, INFO_POPUP_TAG);
	}

	// Default Dialogs
	protected void showPopupDialog(int titleId, int messageId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(messageId);
		updatePopupAndShow(tag);
	}

	protected void showPopupDialog(int titleId, String messageId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(messageId);
		updatePopupAndShow(tag);
	}

	protected void showPopupDialog(int titleId, String messageId, String tag, int buttons) {
		popupItem.setButtons(buttons);
		showPopupDialog(titleId, messageId, tag);
	}

	protected void showPopupDialog(String title, String message, String tag) {
		popupItem.setTitle(title);
		popupItem.setMessage(message);
		updatePopupAndShow(tag);
	}

	@Override
	public void showPopupDialog(int titleId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(Symbol.EMPTY);
		updatePopupAndShow(tag);
	}

	protected void showPopupDialog(String title, String tag) {  // TODO handle popups overlays - set default button values
		popupItem.setTitle(title);
		popupItem.setMessage(Symbol.EMPTY);
		updatePopupAndShow(tag);
	}

	private synchronized void updatePopupAndShow(String tag) {
		popupManager.add(PopupDialogFragment.createInstance(popupItem));
		getLastPopupFragment().show(getSupportFragmentManager(), tag);
	}

	// Progress Dialogs
	protected void showPopupProgressDialog(String title) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(Symbol.EMPTY);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.createInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupProgressDialog(String title, String message) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(message);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.createInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupProgressDialog(int titleId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(Symbol.EMPTY);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.createInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupHardProgressDialog() {
		popupProgressItem.setTitle(Symbol.EMPTY);
		popupProgressItem.setMessage(Symbol.EMPTY);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.createInstance(popupItem);
		popupProgressDialogFragment.setNotCancelable();
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupProgressDialog(int titleId, int messageId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(messageId);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.createInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	private void updateProgressAndShow(PopupProgressFragment popupProgressDialogFragment) {
		popupProgressDialogFragment.updatePopupItem(popupProgressItem);

		if (getSupportFragmentManager() == null) {
			return;
		}

		if (getSupportFragmentManager().getFragments() != null) {
			for (Fragment fragment : getSupportFragmentManager().getFragments()) { // transmit to all fragments? is it safe..? // TODO check logic
				if (fragment != null && fragment instanceof BasePopupsFragment) {
					List<PopupProgressFragment> progressFragments = ((BasePopupsFragment) fragment).getPopupFragmentManager();
					if (progressFragments != null && progressFragments.size() > 0) {
						popupProgressManager.add(popupProgressDialogFragment);
						return;
					}
				}
			}
		}

		if (popupProgressManager.size() > 0) { // if we already showing, then just add but not show
			popupProgressManager.add(popupProgressDialogFragment);
			return;
		}

		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
		popupProgressManager.add(popupProgressDialogFragment);
	}

	protected void dismissFragmentDialog() {
		if (getLastPopupFragment() == null)
			return;

		getLastPopupFragment().dismiss();
		popupManager.remove(popupManager.size() - 1);
	}

	public void dismissFragmentDialogByTag(String tag) {
		for (PopupDialogFragment fragment : popupManager) {
			if (fragment.getTag() != null && fragment.getTag().equals(tag)) {
				fragment.dismiss();
				popupManager.remove(fragment);
			}
		}
	}

	protected PopupDialogFragment getLastPopupFragment() {
		if (popupManager.size() == 0) {
			return null;
		} else {
			return popupManager.get(popupManager.size() - 1);
		}
	}

	protected void dismissProgressDialog() {
		if (getSupportFragmentManager() == null) {
			return;
		}

		if (getSupportFragmentManager().getFragments() != null) {
			for (Fragment fragment : getSupportFragmentManager().getFragments()) { // transmit to all fragments? is it safe..? // TODO check logic
				if (fragment != null && fragment instanceof BasePopupsFragment) {
					List<PopupProgressFragment> progressFragments = ((BasePopupsFragment) fragment).getPopupFragmentManager();
					if (progressFragments != null && progressFragments.size() > 0) { // if there are progresses to show, then don' do anything
						return;
					}
				}
			}
		}

		if (popupProgressManager.size() == 0) {
			return;
		}

		// we get first and dismiss it, then show next, and remove first
		popupProgressManager.get(0).dismiss();
		if (popupProgressManager.size() > 1) {
			popupProgressManager.get(1).show(getSupportFragmentManager(), PROGRESS_TAG);
		}
		popupProgressManager.remove(0);
	}

	public void dismissAllPopups() {
		for (PopupDialogFragment fragment : popupManager) {
			fragment.dismiss();
		}
	}

	protected String getTextFromField(EditText editText) {
		if (editText != null && editText.getText() != null) {
			return editText.getText().toString().trim();
		} else {
			return Symbol.EMPTY;
		}
	}

	protected Context getContext() {
		return context;
	}

	protected EasyTracker provideGATracker() {
		return EasyTracker.getInstance(this);
	}

	@Override
	public void setPositiveBtnId(int leftBtnId) {
		popupItem.setPositiveBtnId(R.string.ic_check);
	}
}
