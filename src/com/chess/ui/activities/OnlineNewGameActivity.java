package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListChallengeItem;
import com.chess.ui.adapters.OnlineChallengesGamesAdapter;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;

public class OnlineNewGameActivity extends LiveBaseActivity implements OnItemClickListener {

	private static final int UPDATE_DELAY = 120000;
	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";

	private ListView openChallengesListView;
	private ArrayList<GameListChallengeItem> gameListItems = new ArrayList<GameListChallengeItem>();
	private OnlineChallengesGamesAdapter gamesAdapter = null;
	private GameListChallengeItem gameListElement;
	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private int successToastMsgId;

	private LoadItem listLoadItem;
	private ListUpdateListener listUpdateListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_new_game);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (AppUtils.isNeedToUpgrade(this)) {
			MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
		}

		init();

		openChallengesListView = (ListView) this.findViewById(R.id.openChallenges);
		openChallengesListView.setAdapter(gamesAdapter);
		openChallengesListView.setOnItemClickListener(this);

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		findViewById(R.id.challengecreate).setOnClickListener(this);
	}

	private void init() {
		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();

		listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

		listUpdateListener = new ListUpdateListener();

		showActionRefresh = true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		updateList();
		handler.postDelayed(updateListOrder, UPDATE_DELAY);
	}

	private void updateList(){
		new GetStringObjTask(listUpdateListener).executeTask(listLoadItem);
	}

	private Runnable updateListOrder = new Runnable() {
		@Override
		public void run() {
			updateList();
			handler.postDelayed(this, UPDATE_DELAY);
		}
	};

	private class ListUpdateListener extends ChessUpdateListener {
		public ListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			getActionBarHelper().setRefreshActionItemState(show);
		}

		@Override
		public void updateData(String returnedObj) {
//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				gameListItems.clear();
				gameListItems.addAll(ChessComApiParser.getChallengesGames(returnedObj));

				if (gamesAdapter == null) {
					gamesAdapter = new OnlineChallengesGamesAdapter(getContext(),  gameListItems);
					openChallengesListView.setAdapter(gamesAdapter);
				}

				gamesAdapter.notifyDataSetChanged();
//			} else  if (returnedObj.contains(RestHelper.R_ERROR)) {
//				showSinglePopupDialog(R.string.error, returnedObj.substring(RestHelper.R_ERROR.length()));
//			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));

		} else if (view.getId() == R.id.friendchallenge) {
			startActivity(new Intent(this, OnlineFriendChallengeActivity.class));

		} else if (view.getId() == R.id.challengecreate) {
			startActivity(new Intent(this, OnlineOpenChallengeActivity.class));
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateList();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if(fragment.getTag().equals(CHALLENGE_ACCEPT_TAG)){
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, String.valueOf(gameListElement.getGameId()));
			successToastMsgId = R.string.challengeaccepted;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if(fragment.getTag().equals(CHALLENGE_ACCEPT_TAG)){
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, String.valueOf(gameListElement.getGameId()));
			successToastMsgId = R.string.challengedeclined;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
	}


	@Override
	public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
		gameListElement = gameListItems.get(pos);
		String title = gameListElement.getOpponentUsername() + StaticData.SYMBOL_NEW_STR
				+ getString(R.string.win_) + StaticData.SYMBOL_SPACE + gameListElement.getOpponentWinCount()
				+ StaticData.SYMBOL_NEW_STR
				+ getString(R.string.loss_) + StaticData.SYMBOL_SPACE + gameListElement.getOpponentLossCount()
				+ StaticData.SYMBOL_NEW_STR
				+ getString(R.string.draw_) + StaticData.SYMBOL_SPACE + gameListElement.getOpponentDrawCount();

		popupItem.setPositiveBtnId(R.string.accept);
		popupItem.setNegativeBtnId(R.string.decline);
		showPopupDialog(title, CHALLENGE_ACCEPT_TAG);
	}

	private class ChallengeInviteUpdateListener extends ChessUpdateListener {
		public ChallengeInviteUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if(isPaused)
				return;

//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				showToast(successToastMsgId);
//			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
//				showSinglePopupDialog(R.string.error, returnedObj.substring(RestHelper.R_ERROR.length()));
//			}
		}
	}
}
