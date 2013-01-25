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
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.entity.new_api.FriendsItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.adapters.FriendsSpinnerAdapter;

import java.util.List;

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

		daysArr = getResources().getIntArray(R.array.days_per_move_array);
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
		LoadItem loadItem = new LoadItem();   // TODO cache results or pre-load
		loadItem.setLoadPath(RestHelper.CMD_FRIENDS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(this));

		new RequestJsonTask<FriendsItem>(initUpdateListener).executeTask(loadItem);
	}

	private class InitUpdateListener extends ActionBarUpdateListener<FriendsItem> {

		public InitUpdateListener() {
			super(getInstance(), FriendsItem.class);
		}

		@Override
		public void updateData(FriendsItem returnedObj) {
			List<FriendsItem.Data> friends = returnedObj.getData();

			friendsSpnr.setAdapter(new FriendsSpinnerAdapter(getContext(), friends));
			if (friendsSpnr.getSelectedItem().equals(StaticData.SYMBOL_EMPTY)) {

				popupItem.setPositiveBtnId(R.string.invite);
				popupItem.setNegativeBtnId(R.string.cancel);
				showPopupDialog(R.string.sorry, R.string.no_friends_invite, NO_INVITED_FRIENDS_TAG);
			}
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
		int gameType = chess960.isChecked() ? RestHelper.V_GAME_CHESS_960 : RestHelper.V_GAME_CHESS;
		int isRated = this.isRatedChkBx.isChecked() ? 1 : 0;
		String opponentName = ((FriendsItem.Data)  friendsSpnr.getSelectedItem()).getUsername();

		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.ECHESS_NEW_GAME);
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);

		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ActionBarUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(getInstance(), DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.online_game_created);
		}

		@Override
		public void errorHandle(String resultMessage) {
			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if(tag.equals(ERROR_TAG)){
			backToLoginActivity();
		} else if(tag.equals(NO_INVITED_FRIENDS_TAG)){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.BASE_URL)));
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

		if(tag.equals(NO_INVITED_FRIENDS_TAG)){
            finish();
        }
		super.onNegativeBtnClick(fragment);
    }
}
