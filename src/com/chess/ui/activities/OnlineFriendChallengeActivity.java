package com.chess.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.utilities.ChessComApiParser;

public class OnlineFriendChallengeActivity extends LiveBaseActivity implements OnClickListener {

    private static final String NO_INVITED_FRIENDS_TAG = "no invited friends";
	private static final String ERROR_TAG = "send request failed popup";

	private Spinner iPlayAsSpnr;
	private Spinner daysPerMoveSpnr;
	private Spinner friendsSpnr;
	private CheckBox isRatedChkBx;
	private RadioButton chess960;

	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private InitUpdateListener initUpdateListener;
	private int[] daysArr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_challenge_friend);

		daysArr = getResources().getIntArray(R.array.daysArr);
		daysPerMoveSpnr = (Spinner) findViewById(R.id.dayspermove);
		daysPerMoveSpnr.setAdapter(new ChessSpinnerAdapter(this, getItemsFromEntries(R.array.dayspermove)));

		chess960 = (RadioButton) findViewById(R.id.chess960);

		iPlayAsSpnr = (Spinner) findViewById(R.id.iplayas);
		iPlayAsSpnr.setAdapter(new ChessSpinnerAdapter(this, getItemsFromEntries(R.array.playas)));

		friendsSpnr = (Spinner) findViewById(R.id.friendsSpinner);
		friendsSpnr.setAdapter(new ChessSpinnerAdapter(this, getItemsFromArray(new String[]{StaticData.SYMBOL_EMPTY})));

		isRatedChkBx = (CheckBox) findViewById(R.id.ratedGame);
		findViewById(R.id.createchallenge).setOnClickListener(this);

		createChallengeUpdateListener = new CreateChallengeUpdateListener();
		initUpdateListener = new InitUpdateListener();

		showActionSettings = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateData();
	}

	private void updateData(){
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GET_FRIENDS);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));

		new GetStringObjTask(initUpdateListener).executeTask(loadItem);
	}

	private class InitUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			onRequestSent(returnedObj);
		}
	}

	private void onRequestSent(String response){
		String[] friends;

		friends = ChessComApiParser.GetFriendsParse(response);

		friendsSpnr.setAdapter(new ChessSpinnerAdapter(this, getItemsFromArray(friends)));
		if (friendsSpnr.getSelectedItem().equals(StaticData.SYMBOL_EMPTY)) {

            popupItem.setPositiveBtnId(R.string.invite);
            popupItem.setNegativeBtnId(R.string.cancel);
            showPopupDialog(R.string.sorry, R.string.nofriends, NO_INVITED_FRIENDS_TAG);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.createchallenge) {
			createChallenge();
		}
	}

	private void createChallenge(){
		if (friendsSpnr.getCount() == 0) {
			return;
		}

		int color = iPlayAsSpnr.getSelectedItemPosition();
		int days = daysArr[daysPerMoveSpnr.getSelectedItemPosition()];
		int gameType = chess960.isChecked() ? 2 : 0;
		int isRated = this.isRatedChkBx.isChecked() ? 1 : 0;

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_NEW_GAME);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_TIMEPERMOVE, String.valueOf(days));
		loadItem.addRequestParams(RestHelper.P_IPLAYAS, String.valueOf(color));
		loadItem.addRequestParams(RestHelper.P_ISRATED, String.valueOf(isRated));
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, String.valueOf(gameType));
		loadItem.addRequestParams(RestHelper.P_OPPONENT, friendsSpnr.getSelectedItem().toString().trim());

		new GetStringObjTask(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
//			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
                showSinglePopupDialog(R.string.congratulations, R.string.onlinegamecreated);
//			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
//				showPopupDialog(getString(R.string.error), returnedObj.substring(RestHelper.R_ERROR.length()),
//						ERROR_TAG);
//			}
		}

		@Override
		public void errorHandle(String resultMessage) {
			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if(fragment.getTag().equals(ERROR_TAG)){
			backToLoginActivity();
		} else if(fragment.getTag().equals(NO_INVITED_FRIENDS_TAG)){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.BASE_URL)));
        }
	}

    @Override
    public void onNegativeBtnClick(DialogFragment fragment) {
        super.onNegativeBtnClick(fragment);
        if(fragment.getTag().equals(NO_INVITED_FRIENDS_TAG)){
            finish();
        }
    }
}
