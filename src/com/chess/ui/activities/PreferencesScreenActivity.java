package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.chess.R;
import com.chess.backend.YourMoveUpdateService;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.adapters.SelectionAdapter2;
import com.chess.ui.core.AppConstants;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * PreferencesScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:18
 */
public class PreferencesScreenActivity extends LiveBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
	private Spinner actionAfterMyMove;
	private Spinner strength;
	private CheckBox showSubmitButton;
	private CheckBox enableNotifications;
	private CheckBox vacationCheckBox;
	private CheckBox showCoordinates;
	private CheckBox showHighlights;
	private CheckBox enableSounds;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		FlurryAgent.onEvent("Settings Accessed", null);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences_screen);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		context = this;

		Spinner boardsSpinner = (Spinner) findViewById(R.id.boardsSpinner);
		Spinner piecesSpinner = (Spinner) findViewById(R.id.piecesSpinner);
		Button prefInvite = (Button) findViewById(R.id.PrefInvite);
		Button prefContactUs = (Button) findViewById(R.id.prefContactUs);

		actionAfterMyMove = (Spinner) findViewById(R.id.PrefAIM);
		actionAfterMyMove.setAdapter(new ChessSpinnerAdapter(this, R.array.AIM));
		//Notif =  (Spinner)findViewById(R.id.PrefNotif);
		strength = (Spinner) findViewById(R.id.PrefStrength);
		strength.setAdapter(new ChessSpinnerAdapter(this, R.array.strength));

		enableSounds = (CheckBox) findViewById(R.id.enableSounds);
		showSubmitButton = (CheckBox) findViewById(R.id.PrefSSB);
		enableNotifications = (CheckBox) findViewById(R.id.PrefNEnable);
		vacationCheckBox = (CheckBox) findViewById(R.id.PrefVacation);
		showCoordinates = (CheckBox) findViewById(R.id.PrefCoords);
		showHighlights = (CheckBox) findViewById(R.id.PrefHighlights);

		TextView onlineTitle = (TextView) findViewById(R.id.onlineTitle);
		LinearLayout afterIMoveLayout = (LinearLayout) findViewById(R.id.afterIMoveLayout);
		TextView computerTitle = (TextView) findViewById(R.id.computerTitle);
		LinearLayout prefStrengthLayout = (LinearLayout) findViewById(R.id.prefStrengthLayout);

		TextView preferencesUpgrade = (TextView) findViewById(R.id.upgradeBtn);
//		boolean liveMembershipLevel =
//				lccHolder.getUser() != null ? mainApp.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 50) : false;
		boolean liveMembershipLevel =
				lccHolder.getUser() != null && mainApp.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 50);
		if (liveMembershipLevel
				|| (!mainApp.isLiveChess() && Integer.parseInt(mainApp.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 3)) {
			preferencesUpgrade.setVisibility(View.VISIBLE);
		} else {
			preferencesUpgrade.setVisibility(View.GONE);
		}

		if (mainApp.isLiveChess()) {
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
		preferencesUpgrade.setOnClickListener(this);


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
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_blue), getString(R.string.blue)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_brown), getString(R.string.brown)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_green), getString(R.string.green)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_grey), getString(R.string.grey)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_marble), getString(R.string.marble)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_red), getString(R.string.red)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_tan), getString(R.string.tan)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_wood_light), getString(R.string.woodlight)));
		boardsList.add(new SelectionItem(getResources().getDrawable(R.drawable.board_wood_dark), getString(R.string.wooddark)));

		//spinners
		boardsSpinner.setAdapter(new SelectionAdapter2(this, boardsList));
		boardsSpinner.setOnItemSelectedListener(new BoardSpinnerListener());
		int boardsPosition = mainApp.getSharedData().getInt(mainApp.getSharedData()
				.getString(AppConstants.USERNAME, AppConstants.SYMBOL_EMPTY) + AppConstants.PREF_BOARD_TYPE, 0);
		boardsSpinner.setSelection(boardsPosition);

		piecesSpinner.setAdapter(new SelectionAdapter2(this, piecesList));
		piecesSpinner.setOnItemSelectedListener(new PiecesSpinnerListener());
		int piecesPosition = mainApp.getSharedData().getInt(mainApp.getSharedData()
				.getString(AppConstants.USERNAME, AppConstants.SYMBOL_EMPTY) + AppConstants.PREF_PIECES_SET, 0);
		piecesSpinner.setSelection(piecesPosition);


		actionAfterMyMove.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getUserName() + AppConstants.PREF_ACTION_AFTER_MY_MOVE, pos);
				mainApp.getSharedDataEditor().commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});
		/*Notif.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getUserName()+"notif", pos);
				mainApp.getSharedDataEditor().commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});*/
		strength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getUserName() + AppConstants.PREF_COMPUTER_STRENGTH, pos);
				mainApp.getSharedDataEditor().commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});

		//checkboxes
		enableSounds.setOnCheckedChangeListener(this);
		showSubmitButton.setOnCheckedChangeListener(this);
		enableNotifications.setOnCheckedChangeListener(this);
		showCoordinates.setOnCheckedChangeListener(this);
		showHighlights.setOnCheckedChangeListener(this);

		if (mainApp.guest) {
			vacationCheckBox.setVisibility(View.GONE);
			findViewById(R.id.prefLogout).setVisibility(View.GONE);
		} else {
			vacationCheckBox.setOnClickListener(this);
			findViewById(R.id.prefLogout).setVisibility(View.VISIBLE);
			findViewById(R.id.prefLogout).setOnClickListener(this);
		}

		prefInvite.setOnClickListener(this);
		prefContactUs.setOnClickListener(this);




	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.prefLogout) { // DO NOT turn to switch!
			if (!mainApp.guest) {
//				if (mainApp.isLiveChess()/* && lccHolder.isConnected() */) {
					lccHolder.logout();
//				}
				mainApp.guest = true;
				mainApp.getSharedDataEditor().putString(AppConstants.PASSWORD, AppConstants.SYMBOL_EMPTY);
				mainApp.getSharedDataEditor().putString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY);
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(this, HomeScreenActivity.class);
				Log.d("TEST", "launching home activity");
				intent.putExtra(StaticData.NAVIGATION_CMD, StaticData.NAV_FINISH_2_LOGIN);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				stopService(new Intent(coreContext, YourMoveUpdateService.class));
				finish();
			}
		} else if (view.getId() == R.id.upgradeBtn) {
			startActivity(mainApp.getMembershipAndroidIntent());
		}else if (view.getId() == R.id.PrefInvite) {
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType(AppConstants.MIME_TYPE_TEXT_PLAIN);
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.invite_subject));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.invite_text) + mainApp.getUserName() + "\". \n \n Sent from my Android");
			FlurryAgent.onEvent("Invite A Friend", null);
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
		} else if (view.getId() == R.id.prefContactUs) {
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType(AppConstants.MIME_TYPE_TEXT_PLAIN);
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{AppConstants.EMAIL_MOBILE_CHESS_COM});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Support");
			//emailIntent.setData(Uri.parse("mailto:mobile@chess.com?subject=Android Support".replace(" ", "%20")));
			startActivity(Intent.createChooser(emailIntent, getString(R.string.send_mail)));
		}
	}

	private class BoardSpinnerListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			mainApp.getSharedDataEditor().putInt(mainApp.getUserName() + AppConstants.PREF_BOARD_TYPE, pos);
			mainApp.getSharedDataEditor().commit();
			mainApp.loadBoard(mainApp.res_boards[pos]);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	}

	private class PiecesSpinnerListener implements AdapterView.OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
			mainApp.getSharedDataEditor().putInt(mainApp.getSharedData()
					.getString(AppConstants.USERNAME, AppConstants.SYMBOL_EMPTY) + AppConstants.PREF_PIECES_SET, pos);
			mainApp.getSharedDataEditor().commit();
			mainApp.loadPieces(pos);
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		actionAfterMyMove.setSelection(mainApp.getSharedData().getInt(mainApp.getUserName() + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0));
		strength.setSelection(mainApp.getSharedData().getInt(mainApp.getUserName() + AppConstants.PREF_COMPUTER_STRENGTH, 0));

		if (mainApp.isLiveChess()) {
			showSubmitButton.setChecked(mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false));
		} else {
			showSubmitButton.setChecked(mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_SHOW_SUBMIT_MOVE, true));
		}
		enableSounds.setChecked(mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_SOUNDS, true));
		enableNotifications.setChecked(mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_NOTIFICATION, true));
		showCoordinates.setChecked(mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_BOARD_COORDINATES, true));
		showHighlights.setChecked(mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true));
	}


	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (!mainApp.guest && !mainApp.isLiveChess()) {
				if (appService != null) {
					appService.RunSingleTask(0,
							"http://www." + LccHolder.HOST + "/api/get_vacation_status?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY),
							progressDialog = new MyProgressDialog(ProgressDialog.show(context, null, getString(R.string.loading), true))
					);
				}
			}
		} else if (code == 0) {
			if (!mainApp.guest && response.trim().split("[+]")[1].equals("1")) {
				vacationCheckBox.setChecked(true);
				vacationCheckBox.setText(getString(R.string.vacationOn));
			}
		} else if (code == 1) {
			if (vacationCheckBox.isChecked())
				vacationCheckBox.setText(getString(R.string.vacationOn));
			else
				vacationCheckBox.setText(getString(R.string.vacationOff));
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
		if (compoundButton.getId() == R.id.PrefSSB) {
			String sharedKey = mainApp.isLiveChess()?AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE : AppConstants.PREF_SHOW_SUBMIT_MOVE;
			mainApp.getSharedDataEditor().putBoolean(mainApp.getUserName() + sharedKey, checked);
			mainApp.getSharedDataEditor().commit();
		} else if (compoundButton.getId() == R.id.enableSounds) {
			mainApp.getSharedDataEditor().putBoolean(mainApp.getUserName() + AppConstants.PREF_SOUNDS, checked);
			mainApp.getSharedDataEditor().commit();
		} else if (compoundButton.getId() == R.id.PrefNEnable) {
			mainApp.getSharedDataEditor().putBoolean(mainApp.getUserName() + AppConstants.PREF_NOTIFICATION, checked);
			mainApp.getSharedDataEditor().commit();
			if (checked)
				startService(new Intent(this, YourMoveUpdateService.class));
			else
				stopService(new Intent(context, YourMoveUpdateService.class));
		} else if (compoundButton.getId() == R.id.PrefVacation) {

			String query;
			if (vacationCheckBox.isChecked()) {
				query = "http://www." + LccHolder.HOST + "/api/vacation_leave?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY);
			} else {
				query = "http://www." + LccHolder.HOST + "/api/vacation_return?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY);
			}
			if (appService != null) {    // TODO change to rest helper
				appService.RunSingleTask(1,
						query,
						progressDialog = new MyProgressDialog(ProgressDialog.show(context, null, getString(R.string.loading), true)));
			}
		} else if (compoundButton.getId() == R.id.PrefCoords) {
			mainApp.getSharedDataEditor().putBoolean(mainApp.getUserName() + AppConstants.PREF_BOARD_COORDINATES, checked);
			mainApp.getSharedDataEditor().commit();
		} else if (compoundButton.getId() == R.id.PrefHighlights) {
			mainApp.getSharedDataEditor().putBoolean(mainApp.getUserName() + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, checked);
			mainApp.getSharedDataEditor().commit();
		}
	}
}