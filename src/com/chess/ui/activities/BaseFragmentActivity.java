package com.chess.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.popup_fragments.PopupDialogFragment;
import com.chess.ui.fragments.popup_fragments.PopupProgressFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.slidingmenu.lib.app.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BaseFragmentActivity class
 *
 * @author alien_roger
 * @created at: 07.07.12 6:42
 */
//public abstract class BaseFragmentActivity extends FragmentActivity implements PopupDialogFace {
public abstract class BaseFragmentActivity extends BaseActivity implements PopupDialogFace {

	protected static final boolean HONEYCOMB_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

	private static final String INFO_POPUP_TAG = "information popup";
	private static final String PROGRESS_TAG = "progress dialog popup";
	protected static final String NETWORK_CHECK_TAG = "network check popup";
	protected static final int NETWORK_REQUEST = 3456;
	protected static final String RE_LOGIN_TAG = "re-login popup";
	protected static final String CHESS_NO_ACCOUNT_TAG = "chess no account popup";
	protected static final String CHECK_UPDATE_TAG = "check update";
	private static final boolean DEVELOPER_MODE = false;


	private Context context;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;

	protected PopupItem popupItem;
	protected PopupItem popupProgressItem;
	protected List<PopupDialogFragment> popupManager;
	protected List<PopupProgressFragment> popupProgressManager;

	protected boolean isPaused;
	private boolean changingConfiguration;

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	public BaseFragmentActivity() {
		super(R.string.chess_com);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (DEVELOPER_MODE) {
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
		super.onCreate(savedInstanceState);

//		if (!BuildConfig.DEBUG) {
			try {
				BugSenseHandler.initAndStartSession(this, AppConstants.BUGSENSE_API_KEY);
			} catch (Exception e) {
				e.printStackTrace();
				String stackTrace = Log.getStackTraceString(e).replaceAll("\n", " ");
				Map<String, String> params = new HashMap<String, String>();
				params.put(AppConstants.EXCEPTION, Build.MODEL + " " + stackTrace);
				FlurryAgent.logEvent(FlurryData.BUGSENSE_INIT_EXCEPTION, params);
			}
//		}

		context = this;

		popupItem = new PopupItem();
		popupProgressItem = new PopupItem();

		popupManager = new ArrayList<PopupDialogFragment>();
		popupProgressManager = new ArrayList<PopupProgressFragment>();

		preferences = AppData.getPreferences(this); // TODO rework shared pref usage to unique get method
		preferencesEditor = preferences.edit();
	}

	@Override
	protected void onResume() {
		super.onResume();
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
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//first saving my state, so the bundle wont be empty.
		//http://code.google.com/p/android/issues/detail?id=19917
		outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
		super.onSaveInstanceState(outState);
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

	private void dismissFragmentDialog(DialogFragment fragment){
		popupItem.setPositiveBtnId(R.string.ok);
		popupItem.setNegativeBtnId(R.string.cancel);
		fragment.setCancelable(true);
		fragment.dismiss();
		popupManager.remove(fragment);
	}

	protected Fragment findFragmentByTag(String tag) {
		return getSupportFragmentManager().findFragmentByTag(tag);
	}

	protected Fragment findFragmentById (int id) {
		 return getSupportFragmentManager().findFragmentById(id);
	}

	public void showKeyBoard(EditText editText){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
	}

	public void hideKeyBoard(View editText){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public void hideKeyBoard(){
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(findViewById(R.id.mainView).getWindowToken(), 0);
	}

	protected void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int msgId) {
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}

	// Single button no callback dialogs
	protected void showSinglePopupDialog(int titleId, int messageId) {
		popupItem.setButtons(1);
		showPopupDialog(titleId, messageId, INFO_POPUP_TAG);
	}

	protected void showSinglePopupDialog(String title, String message) {
		popupItem.setButtons(1);
		showPopupDialog(title, message, INFO_POPUP_TAG);
	}

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

	protected void showPopupDialog(int titleId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		updatePopupAndShow(tag);
	}

	protected void showPopupDialog(String title, String tag) {  // TODO handle popups overlays - set default button values
		popupItem.setTitle(title);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		updatePopupAndShow(tag);
	}

	private synchronized void updatePopupAndShow(String tag){
		popupManager.add(PopupDialogFragment.newInstance(popupItem));
		getLastPopupFragment().show(getSupportFragmentManager(), tag);
	}

	// Progress Dialogs
	protected void showPopupProgressDialog(String title) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(StaticData.SYMBOL_EMPTY);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.newInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupProgressDialog(String title, String message) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(message);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.newInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupProgressDialog(int titleId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(StaticData.SYMBOL_EMPTY);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.newInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupHardProgressDialog(int titleId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(StaticData.SYMBOL_EMPTY);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.newInstance(popupItem);
		popupProgressDialogFragment.setNotCancelable();
		updateProgressAndShow(popupProgressDialogFragment);
	}

	protected void showPopupProgressDialog(int titleId, int messageId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(messageId);
		PopupProgressFragment popupProgressDialogFragment = PopupProgressFragment.newInstance(popupItem);
		updateProgressAndShow(popupProgressDialogFragment);
	}

	private void updateProgressAndShow(PopupProgressFragment popupProgressDialogFragment){
		popupProgressDialogFragment.updatePopupItem(popupProgressItem);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
		popupProgressManager.add(popupProgressDialogFragment);
	}

	protected void dismissFragmentDialog() {
		if (getLastPopupFragment() == null)
			return;

		getLastPopupFragment().dismiss();
		popupManager.remove(popupManager.size() - 1);
	}

	protected PopupDialogFragment getLastPopupFragment(){
		if (popupManager.size() == 0){
			return null;
		} else {
			return popupManager.get(popupManager.size()-1);
		}
	}

	protected void dismissProgressDialog() {
		if(popupProgressManager.size() == 0)
			return;

		popupProgressManager.get(popupProgressManager.size()-1).dismiss();
	}

	public void dismissAllPopups() {
		for (PopupDialogFragment fragment : popupManager) {
			fragment.dismiss();
		}
	}

	protected String getTextFromField(EditText editText) {
		return editText.getText().toString().trim();
	}

	protected Context getContext() {
		return context;
	}
}
