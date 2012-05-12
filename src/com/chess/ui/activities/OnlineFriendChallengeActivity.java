package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MyProgressDialog;

public class OnlineFriendChallengeActivity extends LiveBaseActivity implements OnClickListener {
	private Spinner iPlayAsSpinner;
	private Spinner daysPerMoveSpinner;
	private Spinner friendsSpinner;
	private CheckBox isRated;
	private RadioButton chess960;

	private int[] daysArr = new int[]{
			1,
			2,
			3,
			5,
			7,
			14
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.online_challenge_friend);
//		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		daysPerMoveSpinner = (Spinner) findViewById(R.id.dayspermove);
		daysPerMoveSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.dayspermove));

		chess960 = (RadioButton) findViewById(R.id.chess960);

		iPlayAsSpinner = (Spinner) findViewById(R.id.iplayas);
		iPlayAsSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.playas));

		friendsSpinner = (Spinner) findViewById(R.id.friend);
		friendsSpinner.setAdapter(new ChessSpinnerAdapter(this, new String[]{StaticData.SYMBOL_EMPTY}));

		isRated = (CheckBox) findViewById(R.id.ratedGame);
		findViewById(R.id.createchallenge).setOnClickListener(this);
	}


	@Override
	public void update(int code) {
		if (code == ERROR_SERVER_RESPONSE) {
			finish();
		} else if (code == INIT_ACTIVITY && !mainApp.isLiveChess()) {
			if (appService != null) {
				appService.RunSingleTask(0,
						"http://www." + LccHolder.HOST + "/api/get_friends?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY),
						progressDialog = new MyProgressDialog(ProgressDialog.show(OnlineFriendChallengeActivity.this, null, getString(R.string.gettingfriends), true))
				);
			}
		} else if (code == 0 || (code == INIT_ACTIVITY && mainApp.isLiveChess())) {
			String[] FRIENDS;
			if (mainApp.isLiveChess()) {
				FRIENDS = lccHolder.getOnlineFriends();
			} else {
				FRIENDS = ChessComApiParser.GetFriendsParse(response);
			}

//			ArrayAdapter<String> adapterF = new ArrayAdapter<String>(OnlineFriendChallengeActivity.this,
//					android.R.layout.simple_spinner_item,
//					FRIENDS);
//			adapterF.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			ArrayAdapter<String> friendsAdapter = new ChessSpinnerAdapter(this, FRIENDS);
			friendsSpinner.setAdapter(friendsAdapter);
			if (friendsSpinner.getSelectedItem().equals(StaticData.SYMBOL_EMPTY)) {
				new AlertDialog.Builder(OnlineFriendChallengeActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(getString(R.string.sorry))
						.setMessage(getString(R.string.nofriends))
						.setPositiveButton(getString(R.string.invitetitle), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.chess.com")));
							}
						})
						.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
//								finish();
							}
						}).setCancelable(false)
						.create().show();
			}
		} else if (code == 1) {
			mainApp.showDialog(this, getString(R.string.congratulations), getString(R.string.onlinegamecreated));
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.createchallenge) {
			if (friendsSpinner.getCount() == 0) {
				return;
			}

			int color = iPlayAsSpinner.getSelectedItemPosition();
			int days;
			days = daysArr[daysPerMoveSpinner.getSelectedItemPosition()];
			int israted;
			int gametype = 0;

			if (isRated.isChecked()) {
				israted = 1;
			} else {
				israted = 0;
			}
			if (chess960.isChecked()) {
				gametype = 2;
			}
			String query = "http://www." + LccHolder.HOST + "/api/echess_new_game?id="
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY) +
					"&timepermove=" + days +
					"&iplayas=" + color +
					"&israted=" + israted +
					"&game_type=" + gametype +
					"&opponent=" + friendsSpinner.getSelectedItem().toString().trim();
			if (appService != null) {
				appService.RunSingleTask(1,
						query,
						progressDialog = new MyProgressDialog(ProgressDialog
								.show(OnlineFriendChallengeActivity.this, null, getString(R.string.creating), true))
				);
			}
		}
	}

}
