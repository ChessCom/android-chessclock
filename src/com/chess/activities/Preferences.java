package com.chess.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.chess.R;
import com.chess.core.CoreActivity;
import com.chess.lcc.android.LccHolder;
import com.chess.model.Selection;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.Notifications;
import com.chess.views.SelectionAdapter;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;

public class Preferences extends CoreActivity implements OnClickListener {
	private Button PrefBoard, PrefPices, PrefInvite;
	private Button prefContactUs;
	private Spinner AIM, /*Notif, */
			Strength;
	private CheckBox PrefSSB, PrefNEnable, PrefVacation, PrefShowCoords, PrefShowHighlights;
	private CheckBox enableSounds;
	private SelectionAdapter Boards, Pieces;
	private TextView onlineTitle;
	private LinearLayout afterIMoveLayout;
	private TextView computerTitle;
	private LinearLayout prefStrengthLayout;
	private TextView preferencesUpgrade;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		FlurryAgent.onEvent("Settings Accessed", null);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		PrefBoard = (Button) findViewById(R.id.PrefBoard);
		PrefPices = (Button) findViewById(R.id.PrefPices);
		PrefInvite = (Button) findViewById(R.id.PrefInvite);
		prefContactUs = (Button) findViewById(R.id.prefContactUs);

		AIM = (Spinner) findViewById(R.id.PrefAIM);
		//Notif =  (Spinner)findViewById(R.id.PrefNotif);
		Strength = (Spinner) findViewById(R.id.PrefStrength);

		enableSounds = (CheckBox) findViewById(R.id.enableSounds);
		PrefSSB = (CheckBox) findViewById(R.id.PrefSSB);
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
				|| (!mainApp.isLiveChess() && Integer.parseInt(mainApp.getSharedData().getString("premium_status", "0")) < 3)) {
			preferencesUpgrade.setVisibility(View.VISIBLE);
		} else {
			preferencesUpgrade.setVisibility(View.GONE);
		}

		if (mainApp.isLiveChess()) {
			onlineTitle.setText("Live Game");
			afterIMoveLayout.setVisibility(View.GONE);
			PrefNEnable.setVisibility(View.GONE);
			PrefVacation.setVisibility(View.GONE);
			computerTitle.setVisibility(View.GONE);
			prefStrengthLayout.setVisibility(View.GONE);
		} else {
			onlineTitle.setText("Online Game");
			afterIMoveLayout.setVisibility(View.VISIBLE);
			PrefNEnable.setVisibility(View.VISIBLE);
			PrefVacation.setVisibility(View.VISIBLE);
			computerTitle.setVisibility(View.VISIBLE);
			prefStrengthLayout.setVisibility(View.VISIBLE);
		}
		preferencesUpgrade.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
						"http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString("user_token", "") +
								"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidads")));
			}
		});

		//spiners
		AIM.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString("username", "") + "aim", pos);
				mainApp.getSharedDataEditor().commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});
		/*Notif.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString("username", "")+"notif", pos);
				mainApp.getSharedDataEditor().commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> a) {}
		});*/
		Strength.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString("username", "") + "strength", pos);
				mainApp.getSharedDataEditor().commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});

		//checkboxes

		enableSounds.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString("username", "") + "enableSounds", res);
				mainApp.getSharedDataEditor().commit();
			}
		});

		PrefSSB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				if (mainApp.isLiveChess()) {
					mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString("username", "") + "ssblive", res);
				} else {
					mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString("username", "") + "ssb", res);
				}
				mainApp.getSharedDataEditor().commit();
			}
		});
		PrefNEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString("username", "") + "notifE", res);
				mainApp.getSharedDataEditor().commit();
				if (res)
					startService(new Intent(Preferences.this, Notifications.class));
				else
					stopService(new Intent(Preferences.this, Notifications.class));
			}
		});
		PrefShowCoords.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString("username", "") + "coords", res);
				mainApp.getSharedDataEditor().commit();
			}
		});
		PrefShowHighlights.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton b, boolean res) {
				mainApp.getSharedDataEditor().putBoolean(mainApp.getSharedData().getString("username", "") + "highlights", res);
				mainApp.getSharedDataEditor().commit();
			}
		});
		if (mainApp.guest) {
			PrefVacation.setVisibility(View.GONE);
		} else {
			PrefVacation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String query = "";
					if (PrefVacation.isChecked()) {
						query = "http://www." + LccHolder.HOST + "/api/vacation_leave?id=" + mainApp.getSharedData().getString("user_token", "");
					} else {
						query = "http://www." + LccHolder.HOST + "/api/vacation_return?id=" + mainApp.getSharedData().getString("user_token", "");
					}
					if (appService != null) {
						appService.RunSingleTask(1,
								query,
								progressDialog = new MyProgressDialog(ProgressDialog.show(Preferences.this, null, getString(R.string.loading), true)));
					}
				}
			});
		}
		//buttons
		PrefBoard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(Preferences.this)
						.setTitle("Boards:")
						.setAdapter(Boards, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface d, int pos) {
								mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString("username", "") + "board", pos);
								mainApp.getSharedDataEditor().commit();
								PrefBoard.setCompoundDrawables(Boards.items.get(pos).image, null, null, null);
								mainApp.LoadBoard(mainApp.res_boards[pos]);
							}
						}).create().show();
			}
		});
		PrefPices.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(Preferences.this)
						.setTitle("Pieces:")
						.setAdapter(Pieces, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface d, int pos) {
								mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString("username", "") + "pieces", pos);
								mainApp.getSharedDataEditor().commit();
								PrefPices.setCompoundDrawables(Pieces.items.get(pos).image, null, null, null);
								mainApp.LoadPieces(mainApp.res_pieces[pos]);
							}
						}).create().show();
			}
		});
		PrefInvite.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("text/plain");
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Play Chess with me");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I just signed up to play chess at Chess.com on my Android device. Download the free app here: \n http://chess.com/android or signup at http://www.chess.com/register.html . Then find me - my username is \"" + mainApp.getSharedData().getString("username", "") + "\". \n \n Sent from my Android");
				FlurryAgent.onEvent("Invite A Friend", null);
				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});
		prefContactUs.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"mobile@chess.com", ""});
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Android Support");
				//emailIntent.setData(Uri.parse("mailto:mobile@chess.com?subject=Android Support".replace(" ", "%20")));
				startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			}
		});

		findViewById(R.id.prefLogout).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.prefLogout) { // DO NOT turn to switch!
			if (!mainApp.guest) {
				if (mainApp.isLiveChess()/* && lccHolder.isConnected() */) {
					lccHolder.logout();
				}
				mainApp.getSharedDataEditor().putString("password", "");
				mainApp.getSharedDataEditor().putString("user_token", "");
				mainApp.getSharedDataEditor().commit();
			}
			startActivity(new Intent(this, Singin.class));
			finish();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		AIM.post(new Runnable() {
			@Override
			public void run() {
				AIM.setSelection(mainApp.getSharedData().getInt(mainApp.getSharedData().getString("username", "") + "aim", 0));
			}
		});
		/*Notif.post(new Runnable() {
			@Override
			public void run() {
				Notif.setSelection(mainApp.getSharedData().getInt(mainApp.getSharedData().getString("username", "")+"notif", 0));
			}
		});*/
		Strength.post(new Runnable() {
			@Override
			public void run() {
				Strength.setSelection(mainApp.getSharedData().getInt(mainApp.getSharedData().getString("username", "") + "strength", 0));
			}
		});
		Strength.post(new Runnable() {
			@Override
			public void run() {
				if (mainApp.isLiveChess()) {
					PrefSSB.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "ssblive", false));
				} else {
					PrefSSB.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "ssb", true));
				}
				enableSounds.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "enableSounds", true));
			}
		});
		PrefNEnable.post(new Runnable() {
			@Override
			public void run() {
				PrefNEnable.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "notifE", true));
			}
		});
		PrefShowCoords.post(new Runnable() {
			@Override
			public void run() {
				PrefShowCoords.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "coords", true));
			}
		});
		PrefShowHighlights.post(new Runnable() {
			@Override
			public void run() {
				PrefShowHighlights.setChecked(mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString("username", "") + "highlights", true));
			}
		});

		//buttons defaults
		PrefBoard.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(getResources().getIdentifier("board_" + mainApp.res_boards[mainApp.getSharedData().getInt(mainApp.getSharedData().getString("username", "") + "board", 0)], "drawable", "com.chess")), null, null, null);
		PrefPices.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(getResources().getIdentifier("pieces_" + mainApp.res_pieces[mainApp.getSharedData().getInt(mainApp.getSharedData().getString("username", "") + "pieces", 0)], "drawable", "com.chess")), null, null, null);

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

		Pieces = new SelectionAdapter(Preferences.this, R.layout.selection_item, pieces);
		Boards = new SelectionAdapter(Preferences.this, R.layout.selection_item, boards);
	}

	@Override
	public void LoadNext(int code) {
	}

	@Override
	public void LoadPrev(int code) {
		finish();
	}

	@Override
	public void Update(int code) {
		if (code == -1) {
			if (!mainApp.guest && !mainApp.isLiveChess()) {
				if (appService != null) {
					appService.RunSingleTask(0,
							"http://www." + LccHolder.HOST + "/api/get_vacation_status?id=" + mainApp.getSharedData().getString("user_token", ""),
							progressDialog = new MyProgressDialog(ProgressDialog.show(Preferences.this, null, getString(R.string.loading), true))
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

}
