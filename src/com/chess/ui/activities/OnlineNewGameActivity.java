package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListItem;
import com.chess.ui.adapters.OnlineGamesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MobclixHelper;
import com.chess.utilities.MopubHelper;
import com.chess.utilities.Web;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;

public class OnlineNewGameActivity extends CoreActivityActionBar implements OnClickListener, OnItemClickListener {

	private ListView openChallengesListView;
	private ArrayList<GameListItem> gameListItems = new ArrayList<GameListItem>();
	private OnlineGamesAdapter gamesAdapter = null;
	private int UPDATE_DELAY = 120000;
	private Button challengecreate;
	private Button currentGame;
	private Button upgradeBtn;
	private GameListItem gameListElement;
	private EchessDialogListener echessDialogListener;

	private void init() {
		echessDialogListener = new EchessDialogListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_new_game);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);
		if (MopubHelper.isShowAds(mainApp)) {
			MopubHelper.showBannerAd(upgradeBtn, (MoPubView) findViewById(R.id.mopub_adview), mainApp);
		}

		/*if (MobclixHelper.isShowAds(mainApp)) {
			if (MobclixHelper.getBannerAdviewWrapper(mainApp) == null || MobclixHelper.getBannerAdview(mainApp) == null) {
				MobclixHelper.initializeBannerAdView(this, mainApp);
			}
		}*/

		init();

		openChallengesListView = (ListView) this.findViewById(R.id.openChallenges);
		openChallengesListView.setAdapter(gamesAdapter);
		openChallengesListView.setOnItemClickListener(this);

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		challengecreate = (Button) findViewById(R.id.challengecreate);
		challengecreate.setOnClickListener(this);

		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		registerReceiver(challengesListUpdateReceiver, new IntentFilter(IntentConstants.CHALLENGES_LIST_UPDATE));
		super.onResume();
		if (lccHolder.getCurrentGameId() == null) {
			currentGame.setVisibility(View.GONE);
		} else if (mainApp.isLiveChess()) {
			currentGame.setVisibility(View.VISIBLE);
		}
		disableScreenLock();
	}

	@Override
	protected void onPause() {
		if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
		}
		unregisterReceiver(challengesListUpdateReceiver);
		super.onPause();
		enableScreenLock();
	}

	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (appService != null) {
				if (!mainApp.isLiveChess()) {
					appService.RunRepeatableTask(OnlineScreenActivity.ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
							"http://www." + LccHolder.HOST + AppConstants.API_ECHESS_OPEN_INVITES_ID +
									mainApp.getSharedData().getString(AppConstants.USER_TOKEN, ""),
							null/*progressDialog = MyProgressDialog
                                        .show(OnlineNewGame.this, null, getString(R.string.loadinggames), true)*/);
				} else {
					/*appService.RunRepeatble(Online.ONLINE_CALLBACK_CODE, 0, 2000,
													  progressDialog = MyProgressDialog
														.show(OnlineNewGame.this, null, getString(R.string.updatinggameslist), true));*/
					update(OnlineScreenActivity.ONLINE_CALLBACK_CODE);
				}
			}
		} else if (code == OnlineScreenActivity.ONLINE_CALLBACK_CODE) {
			openChallengesListView.setVisibility(View.GONE);
			gameListItems.clear();
			if (mainApp.isLiveChess()) {
				gameListItems.addAll(lccHolder.getChallengesAndSeeksData());
			} else {
				gameListItems.addAll(ChessComApiParser.ViewOpenChallengeParse(responseRepeatable));
			}
			for (GameListItem gameListItem : gameListItems) {
				Log.d("GameLists", "game received" + gameListItem.toString());
			}


			if (gamesAdapter == null) {
				gamesAdapter = new OnlineGamesAdapter(this, R.layout.gamelistelement, gameListItems);
				openChallengesListView.setAdapter(gamesAdapter);
			} /*else{*/
			gamesAdapter.notifyDataSetChanged();
			openChallengesListView.setVisibility(View.VISIBLE);
			/*}*/
		} else if (code == 2) {
			mainApp.showToast(getString(R.string.challengeaccepted));
			onPause();
			onResume();
		} else if (code == 3) {
			mainApp.showToast(getString(R.string.challengedeclined));
			onPause();
			onResume();
		} else if (code == 4) {
			onPause();
			onResume();
		}
	}

	/*@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		System.out.println("LCCLOG: onWindowFocusChanged hasFocus " + hasFocus);
		if (hasFocus && MobclixHelper.isShowAds(mainApp) && mainApp.isForceBannerAdOnFailedLoad()) {
			MobclixHelper.showBannerAd( upgradeBtn, this, mainApp);
		}
	}*/

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(mainApp.getMembershipAndroidIntent());

		} else if (view.getId() == R.id.friendchallenge) {
			startActivity(new Intent(this, OnlineFriendChallengeActivity.class));
		} else if (view.getId() == R.id.challengecreate) {
			startActivity(new Intent(this, OnlineCreateChallengeActivity.class));
		} else if (view.getId() == R.id.currentGame) {
			if (lccHolder.getCurrentGameId() != null && lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
				lccHolder.processFullGame(lccHolder.getGame(lccHolder.getCurrentGameId()));
			}
		}
	}

	private class EchessDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				String result = Web.Request("http://www." + LccHolder.HOST + AppConstants.API_ECHESS_OPEN_INVITES_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.ACCEPT_INVITEID_PARAMETER + gameListElement.values.get(GameListItem.GAME_ID),
						"GET", null, null);
				if (result.contains(AppConstants.SUCCESS)) {
					update(GameBaseActivity.CALLBACK_COMP_MOVE);
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
					mainApp.showDialog(OnlineNewGameActivity.this, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(OnlineNewGame.this, "Error", result);
				}
			} else if (pos == 1) {

				String result = Web.Request("http://www." + LccHolder.HOST + AppConstants.API_ECHESS_OPEN_INVITES_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.DECLINE_INVITEID_PARAMETER + gameListElement.values.get(GameListItem.GAME_ID),
						"GET", null, null);
				if (result.contains(AppConstants.SUCCESS)) {
					update(3);
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
					mainApp.showDialog(OnlineNewGameActivity.this, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(OnlineNewGame.this, "Error", result);
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
		gameListElement = gameListItems.get(pos);
//		if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
		if (gameListElement.type == GameListItem.LIST_TYPE_CURRENT) {
			final String title = mainApp.isLiveChess() ?
					gameListElement.values.get(GameListItem.OPPONENT_CHESS_TITLE) :
					"Win: " + gameListElement.values.get(GameListItem.OPPONENT_WIN_COUNT)
							+ " Loss: " + gameListElement.values.get(GameListItem.OPPONENT_LOSS_COUNT)
							+ " Draw: " + gameListElement.values.get(GameListItem.OPPONENT_DRAW_COUNT);

			new AlertDialog.Builder(OnlineNewGameActivity.this)
					.setTitle(title)
					.setItems(new String[]{getString(R.string.accept),
							getString(R.string.decline)}, echessDialogListener)
					.create().show();
		}
	}
}
