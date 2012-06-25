package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener2;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.utilities.ChessComApiParser;

public class OnlineFriendChallengeActivityDone extends LiveBaseActivity2 implements OnClickListener {

	private static final String CONGRATULATIONS_TAG = "congratulations popup";
	private static final String ERROR_TAG = "send request failed popup";

	private Spinner iPlayAsSpinner;
	private Spinner daysPerMoveSpinner;
	private Spinner friendsSpinner;
	private CheckBox isRated;
	private RadioButton chess960;
	                                   // TODO move to resources
	private int[] daysArr = new int[]{
			1,
			2,
			3,
			5,
			7,
			14
	};
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private InitUpdateListener initUpdateListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_challenge_friend);

		daysPerMoveSpinner = (Spinner) findViewById(R.id.dayspermove);
		daysPerMoveSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.dayspermove));

		chess960 = (RadioButton) findViewById(R.id.chess960);

		iPlayAsSpinner = (Spinner) findViewById(R.id.iplayas);
		iPlayAsSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.playas));

		friendsSpinner = (Spinner) findViewById(R.id.friend);
		friendsSpinner.setAdapter(new ChessSpinnerAdapter(this, new String[]{StaticData.SYMBOL_EMPTY}));

		isRated = (CheckBox) findViewById(R.id.ratedGame);
		findViewById(R.id.createchallenge).setOnClickListener(this);

		createChallengeUpdateListener = new CreateChallengeUpdateListener();
		initUpdateListener = new InitUpdateListener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initActivity();
	}

	private void initActivity(){
//		"http://www." + LccHolder.HOST
//				+ "/api/get_friends?id="
//				+ preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY),

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_FRIENDS);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));

		new GetStringObjTask(initUpdateListener).executeTask(loadItem);
	}

	private class InitUpdateListener extends ChessUpdateListener2 {
		public InitUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			onRequestSent(returnedObj);
		}
	}

	private void onRequestSent(String response){
		String[] friends;

		friends = ChessComApiParser.GetFriendsParse(response);

//			ArrayAdapter<String> adapterF = new ArrayAdapter<String>(OnlineFriendChallengeActivity.this,
//					android.R.layout.simple_spinner_item,
//					FRIENDS);
//			adapterF.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ArrayAdapter<String> friendsAdapter = new ChessSpinnerAdapter(this, friends);
		friendsSpinner.setAdapter(friendsAdapter);
		if (friendsSpinner.getSelectedItem().equals(StaticData.SYMBOL_EMPTY)) {

			new AlertDialog.Builder(OnlineFriendChallengeActivityDone.this) // TODO change popup
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(getString(R.string.sorry))
					.setMessage(getString(R.string.nofriends))
					.setPositiveButton(getString(R.string.invitetitle), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.BASE_URL)));
						}
					})
					.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
								finish();
						}
					}).setCancelable(false)
					.create().show();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.createchallenge) {
			createChallenge();
		}
	}

	private void createChallenge(){
		if (friendsSpinner.getCount() == 0) {  // TODO
			return;
		}

		int color = iPlayAsSpinner.getSelectedItemPosition();
		int days = daysArr[daysPerMoveSpinner.getSelectedItemPosition()];
		int gametype = chess960.isChecked()? 2 :0;
		int israted = isRated.isChecked() ? 1 :0;

//		String query = "http://www." + LccHolder.HOST + "/api/echess_new_game?id="
//				+ preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY) +
//				"&timepermove=" + days +
//				"&iplayas=" + color +
//				"&israted=" + israted +
//				"&game_type=" + gametype +
//				"&opponent=" + friendsSpinner.getSelectedItem().toString().trim();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_NEW_GAME);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_TIMEPERMOVE, String.valueOf(days));
		loadItem.addRequestParams(RestHelper.P_IPLAYAS, String.valueOf(color));
		loadItem.addRequestParams(RestHelper.P_ISRATED, String.valueOf(israted));
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, String.valueOf(gametype));
		loadItem.addRequestParams(RestHelper.P_OPPONENT, friendsSpinner.getSelectedItem().toString().trim());

		new GetStringObjTask(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener2 {
		public CreateChallengeUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if(returnedObj.contains(RestHelper.R_SUCCESS_)){
				showPopupDialog(R.string.congratulations, R.string.onlinegamecreated,
						CONGRATULATIONS_TAG);
//				AppUtils.showDialog(getContext(), getString(R.string.congratulations)
//						, getString(R.string.onlinegamecreated));
			}else if(returnedObj.contains(RestHelper.R_ERROR)){
				showPopupDialog(getString(R.string.error), returnedObj.substring(RestHelper.R_ERROR.length()),
						ERROR_TAG);
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if(fragment.getTag().equals(ERROR_TAG)){
			backToLoginActivity();
		}
	}
}
