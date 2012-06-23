package com.chess.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener2;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.lcc.android.LccHolder;
import com.chess.model.PopupItem;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.adapters.SelectionAdapter;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.utilities.AppUtils;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * PreferencesScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:18
 */
public class PreferencesScreenActivity2 extends LiveBaseActivity2 implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

	private static final String VACATION_TAG = "confirm vacation popup";

	private Spinner afterMyMoveSpinner;
	private Spinner strengthSpinner;
	private CheckBox showSubmitButton;
	private CheckBox enableNotifications;
	private CheckBox vacationCheckBox;
	private CheckBox showCoordinates;
	private CheckBox showHighlights;
	private CheckBox enableSounds;
	private Context context;
	private VacationStatusUpdateListener vacationStatusUpdateListener;
	private VacationLeaveStatusUpdateListener vacationLeaveStatusUpdateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_screen2);

		context = this;

		FlurryAgent.onEvent(FlurryData.SETTINGS_ACCESSED);

		widgetsInit();

		vacationStatusUpdateListener = new VacationStatusUpdateListener();
		vacationLeaveStatusUpdateListener = new VacationLeaveStatusUpdateListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		afterMyMoveSpinner.setSelection(preferences.getInt(AppData.getUserName(this) + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0));
		strengthSpinner.setSelection(preferences.getInt(AppData.getUserName(this) + AppConstants.PREF_COMPUTER_STRENGTH, 0));

		if (DataHolder.getInstance().isLiveChess()) {
			showSubmitButton.setChecked(preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false));
		} else {
			showSubmitButton.setChecked(preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_SHOW_SUBMIT_MOVE, true));
		}
		enableSounds.setChecked(preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_SOUNDS, true));
		enableNotifications.setChecked(preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_NOTIFICATION, true));
		showCoordinates.setChecked(preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_BOARD_COORDINATES, true));
		showHighlights.setChecked(preferences.getBoolean(AppData.getUserName(this) + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true));

		if(!DataHolder.getInstance().isGuest() && !DataHolder.getInstance().isLiveChess())
			updateVacationStatus();
	}

	private void updateVacationStatus(){
		LoadItem listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.GET_VACATION_STATUS);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

		new GetStringObjTask(vacationStatusUpdateListener).execute(listLoadItem);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.prefLogout) { // DO NOT turn to switch!
			if (!DataHolder.getInstance().isGuest()) {
				LccHolder.getInstance(this).logout();

				DataHolder.getInstance().setGuest(true);

				preferencesEditor.putString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
				preferencesEditor.putString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
				preferencesEditor.commit();

				Intent intent = new Intent(this, HomeScreenActivity.class);
				intent.putExtra(StaticData.NAVIGATION_CMD, StaticData.NAV_FINISH_2_LOGIN);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		} else if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));
		}else if (view.getId() == R.id.prefInvite) {
			String userName = AppData.getUserName(this);
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType(AppConstants.MIME_TYPE_TEXT_PLAIN);
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.invite_subject));
			emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_text)
					+ userName + "\". \n \n Sent from my Android");
			FlurryAgent.onEvent("Invite A Friend", null);
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
		} else if (view.getId() == R.id.prefContactUs) {
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType(AppConstants.MIME_TYPE_TEXT_PLAIN);
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{AppConstants.EMAIL_MOBILE_CHESS_COM});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Support");
			//emailIntent.setData(Uri.parse("mailto:mobile@chess.com?subject=Android Support".replace(" ", "%20")));
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
		} else if (view.getId() == R.id.prefVacation) {
			updateVacationLeaveStatus();
		}
	}

	private class VacationStatusUpdateListener extends ChessUpdateListener2 {
		public VacationStatusUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			getActionBarHelper().setRefreshActionItemState(show);
		}

		@Override
		public void updateData(String returnedObj) {
			if (!DataHolder.getInstance().isGuest() && returnedObj.trim().split("[+]")[1].equals("1")) {
				vacationCheckBox.setChecked(true);
				vacationCheckBox.setText(getString(R.string.vacationOn));
			}
		}
	}


	@Override
	protected void widgetsInit() {
		super.widgetsInit();

		findViewById(R.id.prefInvite).setOnClickListener(this);
		findViewById(R.id.prefContactUs).setOnClickListener(this);

		enableSounds = (CheckBox) findViewById(R.id.enableSounds);
		showSubmitButton = (CheckBox) findViewById(R.id.prefSSB);
		enableNotifications = (CheckBox) findViewById(R.id.prefNEnable);
		vacationCheckBox = (CheckBox) findViewById(R.id.prefVacation);
		showCoordinates = (CheckBox) findViewById(R.id.prefCoords);
		showHighlights = (CheckBox) findViewById(R.id.prefHighlights);

		Button logoutBtn = (Button) findViewById(R.id.prefLogout);
		logoutBtn.setOnClickListener(this);

		if (DataHolder.getInstance().isGuest()) {
			vacationCheckBox.setVisibility(View.GONE);
			logoutBtn.setVisibility(View.GONE);
		} else {
			vacationCheckBox.setOnClickListener(this);
			logoutBtn.setVisibility(View.VISIBLE);
		}

		Button preferencesUpgrade = (Button) findViewById(R.id.upgradeBtn);
		preferencesUpgrade.setOnClickListener(this);

		if (AppUtils.isNeedToUpgrade(this)) {
			preferencesUpgrade.setVisibility(View.VISIBLE);
		} else {
			preferencesUpgrade.setVisibility(View.GONE);
		}

		Spinner langSpinner = (Spinner) findViewById(R.id.langSpinner);
		Spinner boardsSpinner = (Spinner) findViewById(R.id.boardsSpinner);
		Spinner piecesSpinner = (Spinner) findViewById(R.id.piecesSpinner);

		langSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.languages));
		langSpinner.setOnItemSelectedListener(langSelectedListener);
		langSpinner.setSelection(AppData.getLanguageCode(this));

		afterMyMoveSpinner = (Spinner) findViewById(R.id.prefAIM);
		afterMyMoveSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.AIM));
		afterMyMoveSpinner.setOnItemSelectedListener(afterMyMoveSelectedListener);

		strengthSpinner = (Spinner) findViewById(R.id.prefStrength);
		strengthSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.strength));
		strengthSpinner.setOnItemSelectedListener(strengthSelectedListener);



		TextView onlineTitle = (TextView) findViewById(R.id.onlineTitle);
		LinearLayout afterIMoveLayout = (LinearLayout) findViewById(R.id.afterIMoveLayout);
		TextView computerTitle = (TextView) findViewById(R.id.computerTitle);
		LinearLayout prefStrengthLayout = (LinearLayout) findViewById(R.id.prefStrengthLayout);


		if (DataHolder.getInstance().isLiveChess()) {
			onlineTitle.setText(getString(R.string.liveTitle));
			afterIMoveLayout.setVisibility(View.GONE);
			enableNotifications.setVisibility(View.GONE);
			vacationCheckBox.setVisibility(View.GONE);
			computerTitle.setVisibility(View.GONE);
			prefStrengthLayout.setVisibility(View.GONE);
		} else {
			onlineTitle.setText(getString(R.string.onlineTitle));
			afterIMoveLayout.setVisibility(View.VISIBLE);
			enableNotifications.setVisibility(View.VISIBLE);
			vacationCheckBox.setVisibility(View.VISIBLE);
			computerTitle.setVisibility(View.VISIBLE);
			prefStrengthLayout.setVisibility(View.VISIBLE);
		}



		List<SelectionItem> piecesList = new ArrayList<SelectionItem>(9);
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_alpha), getString(R.string.alpha)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_book), getString(R.string.book)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_cases), getString(R.string.cases)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_classic), getString(R.string.classicP)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_club), getString(R.string.club)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_condal), getString(R.string.condal)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_maya), getString(R.string.maya)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_modern), getString(R.string.modern)));
		piecesList.add(new SelectionItem(getResources().getDrawable(R.drawable.pieces_vintage), getString(R.string.vintage)));

		List<SelectionItem> boardsList = new ArrayList<SelectionItem>(9);
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_wood_dark), getString(R.string.wooddark)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_wood_light), getString(R.string.woodlight)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_blue), getString(R.string.blue)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_brown), getString(R.string.brown)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_green), getString(R.string.green)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_grey), getString(R.string.grey)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_marble), getString(R.string.marble)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_red), getString(R.string.red)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_tan), getString(R.string.tan)));

		//spinners
		boardsSpinner.setAdapter(new SelectionAdapter(this, boardsList));
		boardsSpinner.setOnItemSelectedListener(boardSpinnerListener);
		int boardsPosition = preferences.getInt(AppData.getUserName(this)
				+ AppConstants.PREF_BOARD_TYPE, 0);
		boardsSpinner.setSelection(boardsPosition);

		piecesSpinner.setAdapter(new SelectionAdapter(this, piecesList));
		piecesSpinner.setOnItemSelectedListener(piecesSpinnerListener);
		int piecesPosition = preferences.getInt(AppData.getUserName(this) + AppConstants.PREF_PIECES_SET, 0);
		piecesSpinner.setSelection(piecesPosition);

		//checkboxes
		enableSounds.setOnCheckedChangeListener(this);
		showSubmitButton.setOnCheckedChangeListener(this);
		enableNotifications.setOnCheckedChangeListener(this);
		showCoordinates.setOnCheckedChangeListener(this);
		showHighlights.setOnCheckedChangeListener(this);
	}

	private AdapterView.OnItemSelectedListener boardSpinnerListener  = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.PREF_BOARD_TYPE, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};

	private AdapterView.OnItemSelectedListener piecesSpinnerListener = new  AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.PREF_PIECES_SET, pos);
			preferencesEditor.commit();
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};

	private AdapterView.OnItemSelectedListener langSelectedListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
			preferencesEditor.putInt(AppData.getUserName(getContext()) + StaticData.SHP_LANGUAGE, pos);
			preferencesEditor.commit();

			boolean  changed = AppUtils.changeLocale(getContext());
			if(changed){
				Intent intent = getIntent();
				finish();
				startActivity(intent);
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

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
		if (compoundButton.getId() == R.id.prefSSB) {
			String sharedKey = DataHolder.getInstance().isLiveChess()? AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE : AppConstants.PREF_SHOW_SUBMIT_MOVE;
			preferencesEditor.putBoolean(AppData.getUserName(this) + sharedKey, checked);
			preferencesEditor.commit();
		} else if (compoundButton.getId() == R.id.enableSounds) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_SOUNDS, checked);
			preferencesEditor.commit();
		} else if (compoundButton.getId() == R.id.prefNEnable) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_NOTIFICATION, checked);
			preferencesEditor.commit();
			if (checked)
				AppUtils.startNotificationsUpdate(this);
			else
				AppUtils.stopNotificationsUpdate(this);

		} else if (compoundButton.getId() == R.id.prefCoords) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_BOARD_COORDINATES, checked);
			preferencesEditor.commit();
		} else if (compoundButton.getId() == R.id.prefHighlights) {
			preferencesEditor.putBoolean(AppData.getUserName(this) + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, checked);
			preferencesEditor.commit();
		}
	}

	private void updateVacationLeaveStatus(){
		if(vacationCheckBox.isChecked()){
			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.confirm_vacation_title);
			popupItem.setMessage(R.string.confirm_vacation_msg);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
			popupDialogFragment.show(getSupportFragmentManager(), VACATION_TAG);
		}else{
			LoadItem listLoadItem = new LoadItem();
			listLoadItem.setLoadPath(RestHelper.VACATION_RETURN);

			listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			new GetStringObjTask(vacationLeaveStatusUpdateListener).executeTask(listLoadItem);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if(fragment.getTag().equals(VACATION_TAG)){
			LoadItem listLoadItem = new LoadItem();
			listLoadItem.setLoadPath(RestHelper.VACATION_LEAVE);

			listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			new GetStringObjTask(vacationLeaveStatusUpdateListener).executeTask(listLoadItem);
		}
	}

	private class VacationLeaveStatusUpdateListener extends ChessUpdateListener2 {
		public VacationLeaveStatusUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			getActionBarHelper().setRefreshActionItemState(show);
		}

		@Override
		public void updateData(String returnedObj) {
			if (vacationCheckBox.isChecked())
				vacationCheckBox.setText(getString(R.string.vacationOn));
			else
				vacationCheckBox.setText(getString(R.string.vacationOff));
		}
	}
}