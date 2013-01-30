package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.VacationItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ChessWhiteSpinnerAdapter;
import com.chess.ui.adapters.SelectionAdapter;
import com.chess.utilities.AppUtils;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.Facebook;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * PreferencesScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:18
 */
public class PreferencesScreenActivity extends LiveBaseActivity implements CompoundButton.OnCheckedChangeListener {

	private static final String VACATION_TAG = "confirm vacation popup";
	private static final String LOCALE_CHANGE_TAG = "locale change popup";

	private Spinner afterMyMoveSpinner;
	private Spinner strengthSpinner;
	private CheckBox showOnlineSubmitChckBx;
	private CheckBox showLiveSubmitChckBx;
	private CheckBox enableNotifications;
	private CheckBox vacationCheckBox;
	private CheckBox showCoordinates;
	private CheckBox showHighlights;
	private CheckBox enableSounds;
	private VacationUpdateListener vacationStatusGetUpdateListener;
	private VacationUpdateListener vacationStatusPostUpdateListener;
	private VacationUpdateListener vacationStatusDeleteUpdateListener;
	private Spinner maxRatingSpinner;
	private Spinner minRatingSpinner;

	private List<SelectionItem> boardsList;
	private List<SelectionItem> piecesList;
	private int localeSelectedId;
	private Spinner langSpinner;
	private Spinner boardsSpinner;
	private Spinner piecesSpinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_screen);

		FlurryAgent.logEvent(FlurryData.SETTINGS_ACCESSED);

		widgetsInit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		vacationStatusGetUpdateListener = new VacationUpdateListener(VacationUpdateListener.GET);
		vacationStatusPostUpdateListener = new VacationUpdateListener(VacationUpdateListener.POST);
		vacationStatusDeleteUpdateListener = new VacationUpdateListener(VacationUpdateListener.DELETE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setParameters();

		if (!AppData.isGuest(this) && !AppData.isLiveChess(this)) { // TODO why not in live?
			updateVacationStatus();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		vacationStatusGetUpdateListener = null;
		vacationStatusPostUpdateListener = null;
		vacationStatusDeleteUpdateListener = null;
	}

	private void setParameters() {

		Button logoutBtn = (Button) findViewById(R.id.prefLogout);
		logoutBtn.setOnClickListener(this);

		if (AppData.isGuest(this)) {
			vacationCheckBox.setVisibility(View.GONE);
			logoutBtn.setText(R.string.login);
		} else {
			vacationCheckBox.setOnClickListener(this);
			logoutBtn.setText(R.string.logout);
		}

		String userName = AppData.getUserName(this);
		afterMyMoveSpinner.setSelection(preferences.getInt(userName + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0));
		strengthSpinner.setSelection(preferences.getInt(userName + AppConstants.PREF_COMPUTER_STRENGTH, 0));

		showLiveSubmitChckBx.setChecked(preferences.getBoolean(userName + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false));
		showOnlineSubmitChckBx.setChecked(preferences.getBoolean(userName + AppConstants.PREF_SHOW_SUBMIT_MOVE, true));

		enableSounds.setChecked(preferences.getBoolean(userName + AppConstants.PREF_SOUNDS, true));
		enableNotifications.setChecked(preferences.getBoolean(userName + AppConstants.PREF_NOTIFICATION, true));
		showCoordinates.setChecked(preferences.getBoolean(userName + AppConstants.PREF_BOARD_COORDINATES, true));
		showHighlights.setChecked(preferences.getBoolean(userName + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true));


		minRatingSpinner.setAdapter(new ChessWhiteSpinnerAdapter(this, getItemsFromEntries(R.array.minRating)));
		minRatingSpinner.setSelection(preferences.getInt(userName + AppConstants.CHALLENGE_MIN_RATING, 0));

		maxRatingSpinner.setAdapter(new ChessWhiteSpinnerAdapter(this, getItemsFromEntries(R.array.maxRating)));
		maxRatingSpinner.setSelection(preferences.getInt(userName + AppConstants.CHALLENGE_MAX_RATING, 0));

		langSpinner.setAdapter(new ChessWhiteSpinnerAdapter(this, getItemsFromEntries(R.array.languages)));
		langSpinner.setSelection(AppData.getLanguageCode(this));

		afterMyMoveSpinner.setAdapter(new ChessWhiteSpinnerAdapter(this, getItemsFromEntries(R.array.AIM)));
		afterMyMoveSpinner.setSelection(AppData.getAfterMoveAction(this));

		strengthSpinner.setAdapter(new ChessWhiteSpinnerAdapter(this, getItemsFromEntries(R.array.strength)));

		// Piece and board bitmaps list init
		piecesList = new ArrayList<SelectionItem>(9);
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_alpha), getString(R.string.piece_alpha)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_book), getString(R.string.piece_book)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_cases), getString(R.string.piece_cases)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_classic), getString(R.string.piece_classic)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_club), getString(R.string.piece_club)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_condal), getString(R.string.piece_condal)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_maya), getString(R.string.piece_maya)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_modern), getString(R.string.piece_modern)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_vintage), getString(R.string.piece_vintage)));

		boardsList = new ArrayList<SelectionItem>(9);
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_wood_dark), getString(R.string.board_wooddark)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_wood_light), getString(R.string.board_woodlight)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_blue), getString(R.string.board_blue)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_brown), getString(R.string.board_brown)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_green), getString(R.string.board_green)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_grey), getString(R.string.board_grey)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_marble), getString(R.string.board_marble)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_red), getString(R.string.board_red)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_tan), getString(R.string.board_tan)));

		//spinners
		int boardsPosition = preferences.getInt(AppData.getUserName(this) + AppConstants.PREF_BOARD_TYPE, 0);
		boardsSpinner.setSelection(boardsPosition);
		boardsList.get(boardsPosition).setChecked(true);
		boardsSpinner.setAdapter(new SelectionAdapter(this, boardsList));

		int piecesPosition = preferences.getInt(AppData.getUserName(this) + AppConstants.PREF_PIECES_SET, 0);
		piecesSpinner.setSelection(piecesPosition);
		piecesList.get(piecesPosition).setChecked(true);
		piecesSpinner.setAdapter(new SelectionAdapter(this, piecesList));
	}


	private void widgetsInit() {
		findViewById(R.id.prefInvite).setOnClickListener(this);
		findViewById(R.id.prefContactUs).setOnClickListener(this);

		showOnlineSubmitChckBx = (CheckBox) findViewById(R.id.showOnlineSubmitChckBx);
		showLiveSubmitChckBx = (CheckBox) findViewById(R.id.showLiveSubmitChckBx);
		vacationCheckBox = (CheckBox) findViewById(R.id.prefVacation);

		minRatingSpinner = (Spinner) findViewById(R.id.minRatingSpinner);
		minRatingSpinner.setOnItemSelectedListener(ratingSelectedListener);

		maxRatingSpinner = (Spinner) findViewById(R.id.maxRatingSpinner);
		maxRatingSpinner.setOnItemSelectedListener(ratingSelectedListener);

		enableSounds = (CheckBox) findViewById(R.id.enableSoundsChkBx);
		enableNotifications = (CheckBox) findViewById(R.id.notificationsChckBx);
		showCoordinates = (CheckBox) findViewById(R.id.prefCoords);
		showHighlights = (CheckBox) findViewById(R.id.prefHighlights);


		Button preferencesUpgrade = (Button) findViewById(R.id.upgradeBtn);
		preferencesUpgrade.setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(this)) {
			preferencesUpgrade.setVisibility(View.VISIBLE);
		} else {
			preferencesUpgrade.setVisibility(View.GONE);
		}

		langSpinner = (Spinner) findViewById(R.id.langSpinner);
		boardsSpinner = (Spinner) findViewById(R.id.boardsSpinner);
		piecesSpinner = (Spinner) findViewById(R.id.piecesSpinner);
		afterMyMoveSpinner = (Spinner) findViewById(R.id.afterMoveSpinner);
		strengthSpinner = (Spinner) findViewById(R.id.prefStrength);

		langSpinner.setOnItemSelectedListener(langSelectedListener);
		afterMyMoveSpinner.setOnItemSelectedListener(afterMyMoveSelectedListener);
		strengthSpinner.setOnItemSelectedListener(strengthSelectedListener);
		boardsSpinner.setOnItemSelectedListener(boardSpinnerListener);
		piecesSpinner.setOnItemSelectedListener(piecesSpinnerListener);

		//checkboxes
		enableSounds.setOnCheckedChangeListener(this);
		showOnlineSubmitChckBx.setOnCheckedChangeListener(this);
		showLiveSubmitChckBx.setOnCheckedChangeListener(this);
		enableNotifications.setOnCheckedChangeListener(this);
		showCoordinates.setOnCheckedChangeListener(this);
		showHighlights.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.prefLogout) {
			if (!AppData.isGuest(this)) {
				getLccHolder().logout();

				// un-register from GCM
				unRegisterGcmService();

				AppData.setGuest(this, true);

				Facebook facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
				SessionStore.restore(facebook, this);
				facebook.logoutMe(this, new LogoutRequestListener());

				preferencesEditor.putString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
				preferencesEditor.putString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
				preferencesEditor.commit();

				AppUtils.cancelNotifications(this);
			}

			openStartScreen(StaticData.NAV_FINISH_2_LOGIN);
		} else if (id == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));
		} else if (id == R.id.prefInvite) {
			String userName = AppData.getUserName(this);
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType(AppConstants.MIME_TYPE_TEXT_PLAIN);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invite_subject));
			emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_text)
					+ userName + "\". \n \n Sent from my Android");
			FlurryAgent.logEvent(FlurryData.INVITE_A_FRIEND, null);
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail_)));
		} else if (id == R.id.prefContactUs) {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType(AppConstants.MIME_TYPE_MESSAGE_RFC822);
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstants.EMAIL_MOBILE_CHESS_COM});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Support");
			emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackBodyCompose());
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail_)));
		} else if (id == R.id.prefVacation) {
			changeVacationStatus();
		}
	}

	private void openStartScreen(int code) {
		Intent intent = new Intent(this, HomeScreenActivity.class);
		intent.putExtra(StaticData.NAVIGATION_CMD, code);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	private String feedbackBodyCompose() {
		AppUtils.DeviceInfo deviceInfo = new AppUtils.DeviceInfo().getDeviceInfo(this);
		return getResources().getString(R.string.feedback_mailbody) + ": " + AppConstants.VERSION_CODE
				+ deviceInfo.APP_VERSION_CODE + ", " + AppConstants.VERSION_NAME + deviceInfo.APP_VERSION_NAME
				+ ", " + deviceInfo.MODEL + ", " + AppConstants.SDK_API + deviceInfo.SDK_API + ", ";
	}

	private AdapterView.OnItemSelectedListener ratingSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.CHALLENGE_MIN_RATING, minRatingSpinner.getSelectedItemPosition());
			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.CHALLENGE_MAX_RATING, maxRatingSpinner.getSelectedItemPosition());
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	};

	private AdapterView.OnItemSelectedListener langSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			int prevCode = AppData.getLanguageCode(getContext());
			if (prevCode != pos) {
				localeSelectedId = pos;

				popupItem.setPositiveBtnId(R.string.restart);
				showPopupDialog(R.string.locale_change, R.string.need_app_restart_to_apply, LOCALE_CHANGE_TAG);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	};

	private AdapterView.OnItemSelectedListener strengthSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.PREF_COMPUTER_STRENGTH, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	};

	private AdapterView.OnItemSelectedListener afterMyMoveSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.PREF_ACTION_AFTER_MY_MOVE, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> a) {
		}
	};

	private AdapterView.OnItemSelectedListener boardSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			for (SelectionItem item : boardsList) {
				item.setChecked(false);
			}

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);

			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.PREF_BOARD_TYPE, pos);
			preferencesEditor.commit();

			((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};

	private AdapterView.OnItemSelectedListener piecesSpinnerListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			for (SelectionItem item : piecesList) {
				item.setChecked(false);
			}

			SelectionItem selectionItem = (SelectionItem) adapterView.getItemAtPosition(pos);
			selectionItem.setChecked(true);

			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.PREF_PIECES_SET, pos);
			preferencesEditor.commit();

			((BaseAdapter) adapterView.getAdapter()).notifyDataSetChanged();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
		if (compoundButton.getId() == R.id.showOnlineSubmitChckBx) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_SHOW_SUBMIT_MOVE, checked);
		} else if (compoundButton.getId() == R.id.showLiveSubmitChckBx) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, checked);
		} else if (compoundButton.getId() == R.id.enableSoundsChkBx) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_SOUNDS, checked);
		} else if (compoundButton.getId() == R.id.notificationsChckBx) {
			// don't check move if pref didn't changed
			boolean notificationsWasEnabled = AppData.isNotificationsEnabled(this);
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_NOTIFICATION, checked);
			preferencesEditor.commit();

			if (!notificationsWasEnabled && checked) {
				registerGcmService();
				checkMove();
			} else if (!checked) {
				unRegisterGcmService();
			}

		} else if (compoundButton.getId() == R.id.prefCoords) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_BOARD_COORDINATES, checked);
		} else if (compoundButton.getId() == R.id.prefHighlights) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, checked);
		}
		preferencesEditor.commit();
	}

	private void changeVacationStatus() {
		if (vacationCheckBox.isChecked()) {
			showPopupDialog(R.string.confirm_vacation_title, R.string.confirm_vacation_msg, VACATION_TAG);
		} else {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_VACATIONS);
			loadItem.setRequestMethod(RestHelper.DELETE);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));

//			new GetStringObjTask(vacationStatusUpdateListener).executeTask(loadItem);
			new RequestJsonTask<VacationItem>(vacationStatusDeleteUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(VACATION_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_VACATIONS);
			loadItem.setRequestMethod(RestHelper.POST);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));

//			new GetStringObjTask(vacationLeaveStatusUpdateListener).executeTask(loadItem);
			new RequestJsonTask<VacationItem>(vacationStatusPostUpdateListener).executeTask(loadItem);
		} else if (tag.equals(LOCALE_CHANGE_TAG)) {
			preferencesEditor.putInt(AppData.getUserName(getContext()) + StaticData.SHP_LANGUAGE, localeSelectedId);
			preferencesEditor.commit();

			setLocale();
			openStartScreen(StaticData.NAV_FINISH_2_SPLASH);
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if (tag.equals(VACATION_TAG)) {
			vacationCheckBox.setChecked(false);
		} else if (tag.equals(LOCALE_CHANGE_TAG)) {
			langSpinner.setSelection(AppData.getLanguageCode(getContext()));
		}
		super.onNegativeBtnClick(fragment);
	}

	private void updateVacationStatus() {
		LoadItem listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.CMD_VACATIONS);
		listLoadItem.setRequestMethod(RestHelper.GET);
		listLoadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));

//		new GetStringObjTask(vacationStatusUpdateListener).execute(listLoadItem);
		new RequestJsonTask<VacationItem>(vacationStatusPostUpdateListener).executeTask(listLoadItem);
	}

	private class VacationUpdateListener extends ActionBarUpdateListener<VacationItem> {

		static final int GET = 0;
		static final int POST = 1;
		static final int DELETE = 2;
		private int listenerCode;

		public VacationUpdateListener(int listenerCode) {
			super(getInstance(), VacationItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			if (!AppData.isGuest(getContext())) {
				switch (listenerCode) {
					case GET:
						vacationCheckBox.setChecked(returnedObj.getData().isOnVacation());
						break;
					case POST:
						vacationCheckBox.setChecked(true);
						break;
					case DELETE:
						vacationCheckBox.setChecked(false);
						break;
				}
			}
		}
	}

	private class LogoutRequestListener extends BaseRequestListener {
		public void onComplete(String response, final Object state) {
			// callback should be run in the original thread,
			// not the background thread
			handler.post(new Runnable() {
				public void run() {
					SessionEvents.onLogoutFinish();
				}
			});
		}
	}
}