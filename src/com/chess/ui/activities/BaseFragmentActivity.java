package com.chess.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.fragments.PopupProgressFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * BaseFragmentActivity class
 *
 * @author alien_roger
 * @created at: 07.07.12 6:42
 */
public abstract class BaseFragmentActivity extends FragmentActivity implements PopupDialogFace {

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
	protected List<PopupDialogFragment> popupManager;

	protected boolean isPaused;
    private String currentLocale;

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
		backgroundChessDrawable = new BackgroundChessDrawable(this);


		popupItem = new PopupItem();
		popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
		popupProgressItem = new PopupItem();
		popupProgressDialogFragment = PopupProgressFragment.newInstance(popupProgressItem);

		popupManager = new ArrayList<PopupDialogFragment>();

		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

        currentLocale = preferences.getString(AppConstants.CURRENT_LOCALE, StaticData.LOCALE_EN);
        setLocale();
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

		if(!currentLocale.equals(getResources().getConfiguration().locale.getLanguage())){
			restartActivity();
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

    protected void setLocale(){
        String prevLang = getResources().getConfiguration().locale.getLanguage();
        String[] languageCodes = getResources().getStringArray(R.array.languages_codes);

        String setLocale = languageCodes[AppData.getLanguageCode(context)];
        if(!prevLang.equals(setLocale)) {
            Locale locale = new Locale(setLocale);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

            preferencesEditor.putString(AppConstants.CURRENT_LOCALE, setLocale);
            preferencesEditor.commit();

            currentLocale = setLocale;

            restartActivity();
        }
    }

    protected void restartActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
		Log.d("TEST", "___restartActivity___");
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
		popupDialogFragment.setButtons(2);

		popupItem.setPositiveBtnId(R.string.ok);
		popupItem.setNegativeBtnId(R.string.cancel);
		fragment.setCancelable(true);
		fragment.dismiss();
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
		updatePopupAndShow(tag);
	}

	protected void showPopupDialog(int titleId, String messageId, String tag) {
		popupItem.setTitle(titleId);
		popupItem.setMessage(messageId);
		updatePopupAndShow(tag);
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

	protected void showPopupDialog(String title, String tag) {
		popupItem.setTitle(title);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		updatePopupAndShow(tag);
	}

	private void updatePopupAndShow(String tag){
		popupDialogFragment.updatePopupItem(popupItem);
		popupDialogFragment.show(getSupportFragmentManager(), tag);
	}

	// Progress Dialogs
	protected void showPopupProgressDialog(String title) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(StaticData.SYMBOL_EMPTY);
		updateProgressAndShow();
	}

	protected void showPopupProgressDialog(String title, String message) {
		popupProgressItem.setTitle(title);
		popupProgressItem.setMessage(message);
		updateProgressAndShow();
	}

	protected void showPopupProgressDialog(int titleId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(StaticData.SYMBOL_EMPTY);
		updateProgressAndShow();
	}

	protected void showPopupHardProgressDialog(int titleId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(StaticData.SYMBOL_EMPTY);
		popupProgressDialogFragment.setNotCancelable();
		updateProgressAndShow();
	}

	protected void showPopupProgressDialog(int titleId, int messageId) {
		popupProgressItem.setTitle(titleId);
		popupProgressItem.setMessage(messageId);
		updateProgressAndShow();
	}

	private void updateProgressAndShow(){
		popupProgressDialogFragment.updatePopupItem(popupProgressItem);
		popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
	}

	protected void dismissFragmentDialog() {
//		if (popupDialogFragment != null && popupDialogFragment.getDialog() != null)
//			popupDialogFragment.getDialog().dismiss();
		popupDialogFragment.dismiss();
	}

	protected void dismissProgressDialog() {
//		if (popupProgressDialogFragment != null && popupProgressDialogFragment.getDialog() != null)
//			popupProgressDialogFragment.getDialog().dismiss();
		popupProgressDialogFragment.dismiss();
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
