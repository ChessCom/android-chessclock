package com.chess.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivityActionBar;
import com.chess.lcc.android.LccHolder;
import com.chess.model.Selection;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.Notifications;
import com.chess.views.BackgroundChessDrawable;
import com.chess.views.SelectionAdapter;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;

/**
 * PreferencesScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:18
 */
public class PreferencesScreenActivity extends CoreActivityActionBar implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
	private Button PrefBoard, PrefPices, PrefInvite;
	private Button prefContactUs;
	private Spinner actionAfterMyMove, /*Notif, */ strength;
	private CheckBox showSubmitButton, PrefNEnable, PrefVacation, PrefShowCoords, PrefShowHighlights;
	private CheckBox enableSounds;
	private SelectionAdapter Boards, Pieces;
	private TextView onlineTitle;
	private LinearLayout afterIMoveLayout;
	private TextView computerTitle;
	private LinearLayout prefStrengthLayout;
	private TextView preferencesUpgrade;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		FlurryAgent.onEvent("Settings Accessed", null);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		context = this;

		PrefBoard = (Button) findViewById(R.id.PrefBoard);
		PrefPices = (Button) findViewById(R.id.PrefPices);
		PrefInvite = (Button) findViewById(R.id.PrefInvite);
		prefContactUs = (Button) findViewById(R.id.prefContactUs);

		actionAfterMyMove = (Spinner) findViewById(R.id.PrefAIM);
		//Notif =  (Spinner)findViewById(R.id.PrefNotif);
		strength = (Spinner) findViewById(R.id.PrefStrength);

		enableSounds = (CheckBox) findViewById(R.id.enableSounds);
		showSubmitButton = (CheckBox) findViewById(R.id.PrefSSB);
		PrefNEnable = (CheckBox) findViewById(R.id.PrefNEnable);
		PrefVacation = (CheckBox) findViewById(R.id.PrefVacation);
		PrefShowCoords = (CheckBox) findViewById(R.id.PrefCoords);
		PrefShowHighlights = (CheckBox) findViewById(R.id.PrefHighlights);

		onlineTitle = (TextView) findViewById(R.id.onlineTitle);
		afterIMoveLayout = (LinearLayout) findViewById(R.id.afterIMoveLayout);
		computerTitle = (TextView) findViewById(R.id.computerTitle);
		prefStrengthLayout = (LinearLayout) findViewById(R.id.prefStrengthLayout);

		preferencesUpgrade = (TextView) findViewById(R.id.preferencesUpgrade);
		boolean liveMembershipLevel =
				lccHolder.getUser() != null ? mainApp.isLiveChess() && (lccHolder.getUser().getMembershipLevel() < 50) : false;
		if (liveMembershipLevel
				|| (!mainApp.isLiveChess() && Integer.parseInt(mainApp.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 3)) {
			preferencesUpgrade.setVisibility(View.VISIBLE);
		} else {
			preferencesUpgrade.setVisibility(View.GONE);
		}

		if (mainApp.isLiveChess()) {
			onlineTitle.setText(getString(R.string.live_game));
			afterIMoveLayout.setVisibility(View.GONE);
			PrefNEnable.setVisibility(View.GONE);
			PrefVacation.setVisibility(View.GONE);
			computerTitle.setVisibility(View.GONE);
			prefStrengthLayout.setVisibility(View.GONE);
		} else {
			onlineTitle.setText(getString(R.string.online_game));
			afterIMoveLayout.setVisibility(View.VISIBLE);
			PrefNEnable.setVisibility(View.VISIBLE);
			PrefVacation.setVisibility(View.VISIBLE);
			computerTitle.setVisibility(View.VISIBLE);
			prefStrengthLayout.setVisibility(View.VISIBLE);
		}
		preferencesUpgrade.setOnClickListener(this);

		//spiners
		actionAfterMyMove.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_ACTION_AFTER_MY_MOVE, pos);
				mainApp.getSharedDataEditor().commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});
		/*Notif.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")+"notif", pos);
				mainApp.getSharedDataEditor().commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});*/
		strength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, pos);
				mainApp.getSharedDataEditor().commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});

		//checkboxes
		enableSounds.setOnCheckedChangeListener(this);
		showSubmitButton.setOnCheckedChangeListener(this);
		PrefNEnable.setOnCheckedChangeListener(this);
		PrefShowCoords.setOnCheckedChangeListener(this);
		PrefShowHighlights.setOnCheckedChangeListener(this);

		if (mainApp.guest) {
			PrefVacation.setVisibility(View.GONE);
		} else {
			PrefVacation.setOnClickListener(this);
		}
		//buttons
		PrefBoard.setOnClickListener(this);
		PrefPices.setOnClickListener(this);
		PrefInvite.setOnClickListener(this);
		prefContactUs.setOnClickListener(this);

		findViewById(R.id.prefLogout).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.prefLogout) { // DO NOT turn to switch!
			if (!mainApp.guest) {
				if (mainApp.isLiveChess()/* && lccHolder.isConnected() */) {
					lccHolder.logout();
				}
				mainApp.getSharedDataEditor().putString(AppConstants.PASSWORD, "");
				mainApp.getSharedDataEditor().putString(AppConstants.USER_TOKEN, "");
				mainApp.getSharedDataEditor().commit();
			}
			startActivity(new Intent(this, Singin.class));
			finish();
		}else if(view.getId() == R.id.preferencesUpgrade){
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
					"http://www." + LccHolder.HOST + "/login.html?als="
							+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") +
							"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidads")));
		}else if(view.getId() == R.id.PrefBoard){
			new AlertDialog.Builder(context)
					.setTitle(getString(R.string.boards_s))
					.setAdapter(Boards, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface d, int pos) {
							mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_TYPE, pos);
							mainApp.getSharedDataEditor().commit();
							PrefBoard.setCompoundDrawables(Boards.items.get(pos).image, null, null, null);
							mainApp.LoadBoard(mainApp.res_boards[pos]);
						}
					}).create().show();
		}else if(view.getId() == R.id.PrefPices){
			new AlertDialog.Builder(context)
					.setTitle(getString(R.string.pieces_s))
					.setAdapter(Pieces, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface d, int pos) {
							mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_PIECES_SET, pos);
							mainApp.getSharedDataEditor().commit();
							PrefPices.setCompoundDrawables(Pieces.items.get(pos).image, null, null, null);
							mainApp.LoadPieces(mainApp.res_pieces[pos]);
						}
					}).create().show();
		}else if(view.getId() == R.id.PrefInvite){
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("text/plain");
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.invite_subject));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.invite_text) + mainApp.getSharedData().getString(AppConstants.USERNAME, "") + "\". \n \n Sent from my Android");
			FlurryAgent.onEvent("Invite A Friend", null);
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		}else if(view.getId() == R.id.prefContactUs){
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"mobile@chess.com"});
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Support");
			//emailIntent.setData(Uri.parse("mailto:mobile@chess.com?subject=Android Support".replace(" ", "%20")));
			startActivity(Intent.createChooser(emailIntent, "Send mail..."));
		}

//		PrefBoard = (Button) findViewById(R.id.PrefBoard);
//		PrefPices = (Button) findViewById(R.id.PrefPices);
//		PrefInvite = (Button) findViewById(R.id.PrefInvite);
//		prefContactUs = (Button) findViewById(R.id.prefContactUs);		
	}

	@Override
	protected void onResume() {
		super.onResume();
//		actionAfterMyMove.post(new Runnable() {
//			@Override
//			public void run() {
				actionAfterMyMove.setSelection(mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_ACTION_AFTER_MY_MOVE, 0));
//			}
//		});
		/*Notif.post(new Runnable() {
			@Override
			public void run() {
				Notif.setSelection(mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "")+"notif", 0));
			}
		});*/
//		strength.post(new Runnable() {
//			@Override
//			public void run() {
				strength.setSelection(mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0));
//			}
//		});
//		strength.post(new Runnable() {
//			@Override
//			public void run() {
				if (mainApp.isLiveChess()) {
					showSubmitButton.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false));
				} else {
					showSubmitButton.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE, true));
				}
				enableSounds.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SOUNDS, true));
//			}
//		});
//		PrefNEnable.post(new Runnable() {
//			@Override
//			public void run() {
				PrefNEnable.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_NOTIFICATION, true));
//			}
//		});
//		PrefShowCoords.post(new Runnable() {
//			@Override
//			public void run() {
				PrefShowCoords.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_COORDINATES, true));
//			}
//		});
//		PrefShowHighlights.post(new Runnable() {
//			@Override
//			public void run() {
				PrefShowHighlights.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true));
//			}
//		});

		//buttons defaults
		PrefBoard.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(getResources().getIdentifier("board_" + mainApp.res_boards[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_TYPE, 0)], "drawable", "com.chess")), null, null, null);
		PrefPices.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(getResources().getIdentifier("pieces_" + mainApp.res_pieces[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_PIECES_SET, 0)], "drawable", "com.chess")), null, null, null);

		ArrayList<Selection> pieces = new ArrayList<Selection>(9);
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_alpha), getString(R.string.alpha)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_book), getString(R.string.book)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_cases), getString(R.string.cases)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_classic), getString(R.string.classicP)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_club), getString(R.string.club)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_condal), getString(R.string.condal)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_maya), getString(R.string.maya)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_modern), getString(R.string.modern)));
		pieces.add(new Selection(getResources().getDrawable(R.drawable.pieces_vintage), getString(R.string.vintage)));

		ArrayList<Selection> boards = new ArrayList<Selection>(9);
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_blue), getString(R.string.blue)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_brown), getString(R.string.brown)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_green), getString(R.string.green)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_grey), getString(R.string.grey)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_marble), getString(R.string.marble)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_red), getString(R.string.red)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_tan), getString(R.string.tan)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_wood_light), getString(R.string.woodlight)));
		boards.add(new Selection(getResources().getDrawable(R.drawable.board_wood_dark), getString(R.string.wooddark)));

		Pieces = new SelectionAdapter(context, R.layout.selection_item, pieces);
		Boards = new SelectionAdapter(context, R.layout.selection_item, boards);
	}


	@Override
	public void Update(int code) {
		if (code == -1) {
			if (!mainApp.guest && !mainApp.isLiveChess()) {
				if (appService != null) {
					appService.RunSingleTask(0,
							"http://www." + LccHolder.HOST + "/api/get_vacation_status?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, ""),
							progressDialog = new MyProgressDialog(ProgressDialog.show(context, null, getString(R.string.loading), true))
					);
				}
			}
		} else if (code == 0) {
			if (!mainApp.guest && response.trim().split("[+]")[1].equals("1")) {
				PrefVacation.setChecked(true);
				PrefVacation.setText(getString(R.string.vacationOn));
			}
		} else if (code == 1) {
			if (PrefVacation.isChecked())
				PrefVacation.setText(getString(R.string.vacationOn));
			else
				PrefVacation.setText(getString(R.string.vacationOff));
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
//		enableSounds = (CheckBox) findViewById(R.id.enableSounds);
//		showSubmitButton = (CheckBox) findViewById(R.id.PrefSSB);
//		PrefNEnable = (CheckBox) findViewById(R.id.PrefNEnable);
//		PrefVacation = (CheckBox) findViewById(R.id.PrefVacation);
//		PrefShowCoords = (CheckBox) findViewById(R.id.PrefCoords);
//		PrefShowHighlights = (CheckBox) findViewById(R.id.PrefHighlights);
		if(compoundButton.getId() == R.id.PrefSSB){
			if (mainApp.isLiveChess()) {
				mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, checked);
			} else {
				mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE, checked);
			}
			mainApp.getSharedDataEditor().commit();
		}else if(compoundButton.getId() == R.id.enableSounds){
			mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SOUNDS, checked);
			mainApp.getSharedDataEditor().commit();			
		}else if(compoundButton.getId() == R.id.PrefNEnable){
			mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_NOTIFICATION, checked);
			mainApp.getSharedDataEditor().commit();
			if (checked)
				startService(new Intent(context, Notifications.class));
			else
				stopService(new Intent(context, Notifications.class));			
		}else if(compoundButton.getId() == R.id.PrefVacation){

			String query = "";
			if (PrefVacation.isChecked()) {
				query = "http://www." + LccHolder.HOST + "/api/vacation_leave?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "");
			} else {
				query = "http://www." + LccHolder.HOST + "/api/vacation_return?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "");
			}
			if (appService != null) {
				appService.RunSingleTask(1,
						query,
						progressDialog = new MyProgressDialog(ProgressDialog.show(context, null, getString(R.string.loading), true)));
			}
		}else if(compoundButton.getId() == R.id.PrefCoords){
			mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_COORDINATES, checked);
			mainApp.getSharedDataEditor().commit();			
		}else if(compoundButton.getId() == R.id.PrefHighlights){
			mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, checked);
			mainApp.getSharedDataEditor().commit();			
		}
		//To change body of implemented methods use File | Settings | File Templates.
	}
}