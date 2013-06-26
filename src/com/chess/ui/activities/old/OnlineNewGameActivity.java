package com.chess.ui.activities.old;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailyChallengeItem;
import com.chess.backend.entity.new_api.DailyGameAcceptItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.adapters.DailyChallengesGamesAdapter;

import java.util.ArrayList;

public class OnlineNewGameActivity extends LiveBaseActivity implements OnItemClickListener {

	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";

	private ListView openChallengesListView;
	private ArrayList<DailyChallengeItem.Data> gameListItems = new ArrayList<DailyChallengeItem.Data>();
	private DailyChallengesGamesAdapter gamesAdapter = null;
	private DailyChallengeItem.Data gameListElement;
	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private int successToastMsgId;

	private LoadItem listLoadItem;
	private ListUpdateListener listUpdateListener;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_new_game);

		openChallengesListView = (ListView) this.findViewById(R.id.openChallenges);
		openChallengesListView.setAdapter(gamesAdapter);
		openChallengesListView.setOnItemClickListener(this);

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		findViewById(R.id.challengecreate).setOnClickListener(this);

		initUpgradeAndAdWidgets();
		/*moPubView = (MoPubView) findViewById(R.id.mopub_adview);
        MopubHelper.showBannerAd(upgradeBtn, moPubView, this);*/
	}

	private void init() {
		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();

		listLoadItem = new LoadItem();
//		listLoadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
		listLoadItem.setLoadPath(RestHelper.CMD_GAMES_CHALLENGES);
		listLoadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getAppData().getUserToken());

		listUpdateListener = new ListUpdateListener();

	}

	@Override
	protected void onStart() {
		init();
		super.onStart();
		updateList();
	}

	private void updateList(){
//		new GetStringObjTask(listUpdateListener).executeTask(listLoadItem);
		new RequestJsonTask<DailyChallengeItem>(listUpdateListener).executeTask(listLoadItem);
	}

//	private class ListUpdateListener extends ChessUpdateListener {
	private class ListUpdateListener extends ActionBarUpdateListener<DailyChallengeItem> {

	public ListUpdateListener() {
		super(getInstance(), DailyChallengeItem.class);
	}

	@Override
		public void showProgress(boolean show) {
			getActionBarHelper().setRefreshActionItemState(show);
		}

		@Override
		public void updateData(DailyChallengeItem returnedObj) {
			gameListItems.clear();
//			gameListItems.addAll(ChessComApiParser.getChallengesGames(returnedObj));
			gameListItems.addAll(returnedObj.getData());

//			if (gamesAdapter == null) {                 // TODO implement listener face to unlock
//				gamesAdapter = new DailyChallengesGamesAdapter(getContext(),  gameListItems);
//				openChallengesListView.setAdapter(gamesAdapter);
//			}

			gamesAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(getAppData().getMembershipAndroidIntent());

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
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if(tag.equals(CHALLENGE_ACCEPT_TAG)){
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ANSWER_GAME_SEEK(gameListElement.getGameId()));
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getAppData().getUserToken());
			successToastMsgId = R.string.challenge_accepted;

			new RequestJsonTask<DailyGameAcceptItem>(challengeInviteUpdateListener).executeTask(loadItem);
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

		if(tag.equals(CHALLENGE_ACCEPT_TAG)){
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ANSWER_GAME_SEEK(gameListElement.getGameId()));
			loadItem.setRequestMethod(RestHelper.DELETE);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getAppData().getUserToken());
			successToastMsgId = R.string.challenge_declined;

			new RequestJsonTask<DailyGameAcceptItem>(challengeInviteUpdateListener).executeTask(loadItem);
		}
		super.onNegativeBtnClick(fragment);
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

	private class ChallengeInviteUpdateListener extends ActionBarUpdateListener<DailyGameAcceptItem> {

		public ChallengeInviteUpdateListener() {
			super(getInstance(), DailyGameAcceptItem.class);
		}

		@Override
		public void updateData(DailyGameAcceptItem returnedObj) {
			if(isPaused)
				return;

			showToast(successToastMsgId);
		}
	}
}
