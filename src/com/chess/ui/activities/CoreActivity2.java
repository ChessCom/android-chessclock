package com.chess.ui.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.fragments.PopupProgressFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.flurry.android.FlurryAgent;

public abstract class CoreActivity2 extends FragmentActivity implements PopupDialogFace {

	private static final String INFO_POPUP_TAG = "information popup";
	private static final String PROGRESS_TAG = "progress dialog popup";

	protected DisplayMetrics metrics;
	protected BackgroundChessDrawable backgroundChessDrawable;

	private Context context;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;
	protected PopupDialogFragment popupDialogFragment;
	protected PopupItem popupItem;
	protected PopupItem popupProgressItem;
	protected PopupProgressFragment popupProgressDialogFragment;
	protected boolean isPaused;


	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		backgroundChessDrawable =  new BackgroundChessDrawable(this);

		popupItem = new PopupItem();
		popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
		popupProgressItem = new PopupItem();
		popupProgressDialogFragment = PopupProgressFragment.newInstance(popupProgressItem);

		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isPaused = false;


		if (preferences.getLong(AppConstants.FIRST_TIME_START, 0) == 0) {

			preferencesEditor.putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;
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

	protected Context getContext(){
		return context;
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
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

	protected void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	protected void showPopupDialog(int titleId, String messageId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(messageId);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}


	protected void showPopupDialog(String title, String message, String tag) {
		popupItem.setTitle(title);
		popupItem.setMessage(message);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	protected void showPopupDialog(int titleId, String tag) {
		popupItem.setTitle(titleId);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	protected void showPopupDialog(String title, String tag) {
		popupItem.setTitle(title);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

    // Progress Dialogs
	protected void showPopupProgressDialog(String title) {
		popupProgressItem.setTitle(title);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void showPopupProgressDialog(String title, String message) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(message);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void showPopupProgressDialog(int title) {
		popupProgressItem.setTitle(title);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void showPopupProgressDialog(int titleId, int messageId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(messageId);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void dismissProgressDialog(){
		if (popupProgressDialogFragment != null && popupProgressDialogFragment.getDialog() != null)
			popupProgressDialogFragment.getDialog().dismiss();
	}

	protected String getTextFromField(EditText editText) {
		return editText.getText().toString().trim();
	}

	protected void showToast(int msgId){
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}
}