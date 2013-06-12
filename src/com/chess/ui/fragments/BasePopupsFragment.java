package com.chess.ui.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.PopupItem;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.fragments.popup_fragments.PopupDialogFragment;
import com.chess.ui.fragments.popup_fragments.PopupProgressFragment;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 15:22
 */
public abstract class BasePopupsFragment extends Fragment implements PopupDialogFace {

	protected static final boolean HONEYCOMB_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	protected static final boolean JELLY_BEAN_PLUS_API = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

	private static final String INFO_POPUP_TAG = "information popup";
	private static final String PROGRESS_TAG = "progress dialog popup";
	protected static final String NETWORK_CHECK_TAG = "network check popup";
	protected static final int NETWORK_REQUEST = 3456;
	protected static final String RE_LOGIN_TAG = "re-login popup";
	protected static final String CHESS_NO_ACCOUNT_TAG = "chess no account popup";
	protected static final String CHECK_UPDATE_TAG = "check update";

	protected PopupItem popupItem;
	protected PopupItem popupProgressItem;
	protected List<PopupDialogFragment> popupManager;
	protected List<PopupProgressFragment> popupProgressManager;

	protected boolean isPaused;
	private boolean changingConfiguration;



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		popupItem = new PopupItem();
		popupProgressItem = new PopupItem();

		popupManager = new ArrayList<PopupDialogFragment>();
		popupProgressManager = new ArrayList<PopupProgressFragment>();
	}

	protected Context getContext() {
		return getActivity();
	}

	@Override
	public void onResume() {
		super.onResume();
		isPaused = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		isPaused = true;
	}

	protected ContentResolver getContentResolver(){
		return getActivity().getContentResolver();
	}

	/**
	 * Make verification of tag
	 * @param fragment to check
	 * @return true if tag is not null
	 */
	protected boolean isTagEmpty(DialogFragment fragment){
		String tag = fragment.getTag();
		if (tag == null) {
			onPositiveBtnClick(fragment);
			return true;
		}
		return false;
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

	// Single button no callback dialogs
	protected void showSinglePopupDialog(int titleId, int messageId) {
		showPopupDialog(titleId, messageId, INFO_POPUP_TAG);
		getLastPopupFragment().setButtons(1);
	}

	protected void showSinglePopupDialog(String title, String message) {
		showPopupDialog(title, message, INFO_POPUP_TAG);
		getLastPopupFragment().setButtons(1);
	}

	public void safeShowSinglePopupDialog(int titleId, String message) {
		if (isPaused)
			return;

		// temporary handling i18n manually
		final String messageI18n = AppUtils.getI18nStringForAPIError(getActivity(), message);
		showPopupDialog(titleId, messageI18n, INFO_POPUP_TAG);
		getLastPopupFragment().setButtons(1);
	}


	protected void showSinglePopupDialog(int titleId, String message) {
		// temporary handling i18n manually
		final String messageI18n = AppUtils.getI18nStringForAPIError(getActivity(), message);
		showPopupDialog(titleId, messageI18n, INFO_POPUP_TAG);
		getLastPopupFragment().setButtons(1);
	}

	protected void showSinglePopupDialog(String message) {
		showPopupDialog(message, INFO_POPUP_TAG);
		getLastPopupFragment().setButtons(1);
	}

	protected void showSinglePopupDialog(int messageId) {
		showPopupDialog(messageId, INFO_POPUP_TAG);
		getLastPopupFragment().setButtons(1);
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

	protected void showPopupDialog(String title, String tag) {  // TODO handle popups overlays - set default button values
		popupItem.setTitle(title);
		popupItem.setMessage(StaticData.SYMBOL_EMPTY);
		updatePopupAndShow(tag);
	}

	private synchronized void updatePopupAndShow(String tag){
		popupManager.add(PopupDialogFragment.newInstance(popupItem, this));
		getLastPopupFragment().show(getFragmentManager(), tag);
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

	private void updateProgressAndShow(PopupProgressFragment popupProgressDialogFragment){
		popupProgressDialogFragment.updatePopupItem(popupProgressItem);
		popupProgressDialogFragment.show(getFragmentManager(), PROGRESS_TAG);
		popupProgressManager.add(popupProgressDialogFragment);
	}

	protected void dismissFragmentDialog() {
		if (getLastPopupFragment() == null)
			return;

		getLastPopupFragment().dismiss();
		popupManager.remove(popupManager.size()-1);
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
		if (TextUtils.isEmpty(editText.getText())){
			return StaticData.SYMBOL_EMPTY;
		} else {
			return editText.getText().toString().trim();
		}
	}

	protected void showToast(String msg) {
		Context context = getActivity();
		if (context != null) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
	}

	protected void showToast(int msgId) {
		Context context = getActivity();
		if (context != null) {
			Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
		}
	}

	public void showKeyBoard(EditText editText){
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
	}

	public void hideKeyBoard(View editText){
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	public void hideKeyBoard(){
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
	}

	@Override
	public void setInitialSavedState(SavedState state) {
		super.setInitialSavedState(state);
		// TODO -> File | Settings | File Templates.
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("smth", "value");
		super.onSaveInstanceState(outState);
	}
}
