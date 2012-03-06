package com.chess.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.chess.R;
import com.chess.activities.tabs.Online;
import com.chess.adapters.OnlineGamesAdapter;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivityActionBar;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListElement;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MobclixHelper;
import com.chess.utilities.Web;
import com.chess.views.BackgroundChessDrawable;

import java.util.ArrayList;

public class LiveNewGameActivity extends CoreActivityActionBar implements OnClickListener, OnItemClickListener {
	private ListView openChallengesLictView;
	private ArrayList<GameListElement> gameListItems = new ArrayList<GameListElement>();
	private OnlineGamesAdapter gamesAdapter = null;
	private int UPDATE_DELAY = 120000;
	private Button challengecreate;
	private Button currentGame;
	private Button upgradeBtn;
	private GameListElement gameListElement;
	private ChallengeDialogListener challengeDialogListener;
	private DirectChallengeDialogListener directChallengeDialogListener;
	private ReleasedByMeDialogListener releasedByMeDialogListener;
	private EchessDialogListener echessDialogListener;

	private void init(){
		challengeDialogListener = new ChallengeDialogListener();
		directChallengeDialogListener = new DirectChallengeDialogListener();
		releasedByMeDialogListener = new ReleasedByMeDialogListener();
		echessDialogListener = new EchessDialogListener();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_new_game);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		if (MobclixHelper.isShowAds(mainApp)) {
			if (MobclixHelper.getBannerAdviewWrapper(mainApp) == null || MobclixHelper.getBannerAdview(mainApp) == null) {
				MobclixHelper.initializeBannerAdView(this, mainApp);
			}
		}

		init();

		upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		openChallengesLictView = (ListView) this.findViewById(R.id.openChallenges);
		openChallengesLictView.setAdapter(gamesAdapter);
		openChallengesLictView.setOnItemClickListener(this);

		findViewById(R.id.friendchallenge).setOnClickListener(this);
		challengecreate = (Button) findViewById(R.id.challengecreate);
		challengecreate.setOnClickListener(this);

		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(this);
	}

	protected void onResume() {
		if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.showBannerAd(MobclixHelper.getBannerAdviewWrapper(mainApp), upgradeBtn, this, mainApp);
		}
		registerReceiver(challengesListUpdateReceiver, new IntentFilter("com.chess.lcc.android-challenges-list-update"));
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
					appService.RunRepeatbleTask(Online.ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
							"http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" +
									mainApp.getSharedData().getString(AppConstants.USER_TOKEN, ""),
							null/*progressDialog = MyProgressDialog
                                        .show(OnlineNewGame.this, null, getString(R.string.loadinggames), true)*/);
				} else {
					/*appService.RunRepeatble(Online.ONLINE_CALLBACK_CODE, 0, 2000,
													  progressDialog = MyProgressDialog
														.show(OnlineNewGame.this, null, getString(R.string.updatinggameslist), true));*/
					update(Online.ONLINE_CALLBACK_CODE);
				}
			}
		} else if (code == Online.ONLINE_CALLBACK_CODE) {
			openChallengesLictView.setVisibility(View.GONE);
			gameListItems.clear();
			if (mainApp.isLiveChess()) {
				gameListItems.addAll(lccHolder.getChallengesAndSeeksData());
			} else {
				gameListItems.addAll(ChessComApiParser.ViewOpenChallengeParse(responseRepeatable));
			}
			if (gamesAdapter == null) {
				gamesAdapter = new OnlineGamesAdapter(this, R.layout.gamelistelement, gameListItems);
				openChallengesLictView.setAdapter(gamesAdapter);
			} /*else{*/
			gamesAdapter.notifyDataSetChanged();
			openChallengesLictView.setVisibility(View.VISIBLE);
			/*}*/
		} else if (code == 2) {
			mainApp.ShowMessage(getString(R.string.challengeaccepted));
			onPause();
			onResume();
		} else if (code == 3) {
			mainApp.ShowMessage(getString(R.string.challengedeclined));
			onPause();
			onResume();
		} else if (code == 4) {
			onPause();
			onResume();
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		System.out.println("LCCLOG: onWindowFocusChanged hasFocus " + hasFocus);
		if (hasFocus && MobclixHelper.isShowAds(mainApp) && mainApp.isForceBannerAdOnFailedLoad()) {
			MobclixHelper.showBannerAd(MobclixHelper.getBannerAdviewWrapper(mainApp), upgradeBtn, this, mainApp);
		}
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.upgradeBtn){
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
					"http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") +
							"&goto=http%3A%2F%2Fwww." + LccHolder.HOST + "%2Fmembership.html?c=androidads")));

		}else if(view.getId() == R.id.friendchallenge){
			startActivity(new Intent(this, LiveFriendChallengeActivity.class));
		}else if(view.getId() == R.id.challengecreate){
			startActivity(new Intent(this, LiveCreateChallengeActivity.class));
		}else if(view.getId() == R.id.currentGame){
			if (lccHolder.getCurrentGameId() != null && lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
				lccHolder.processFullGame(lccHolder.getGame(lccHolder.getCurrentGameId()));
			}
		}
	}

	private class ChallengeDialogListener implements  DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(AppConstants.GAME_ID));
				LccHolder.LOG.info("Accept challenge: " + challenge);
				lccHolder.getAndroid().runAcceptChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.values.get(AppConstants.GAME_ID));
				update(2);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(AppConstants.GAME_ID));
				LccHolder.LOG.info("Decline challenge: " + challenge);
				lccHolder.getAndroid().runRejectChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.values.get(AppConstants.GAME_ID));
				update(3);
			}
		}
	}

	private class DirectChallengeDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(AppConstants.GAME_ID));
				LccHolder.LOG.info("Cancel my challenge: " + challenge);
				lccHolder.getAndroid().runCancelChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.values.get(AppConstants.GAME_ID));
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(AppConstants.GAME_ID));
				LccHolder.LOG.info("Just keep my challenge: " + challenge);
			}
		}
	}

	private class ReleasedByMeDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.values.get(AppConstants.GAME_ID));
				LccHolder.LOG.info("Cancel my seek: " + challenge);
				lccHolder.getAndroid().runCancelChallengeTask(challenge);
				lccHolder.removeSeek(gameListElement.values.get(AppConstants.GAME_ID));
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.values.get(AppConstants.GAME_ID));
				LccHolder.LOG.info("Just keep my seek: " + challenge);
			}
		}
	}

	private class EchessDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				String result = Web.Request("http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&acceptinviteid=" + gameListElement.values.get(AppConstants.GAME_ID), "GET", null, null);
				if (result.contains("Success")) {
					update(2);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(LiveNewGameActivity.this, "Error", result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(OnlineNewGame.this, "Error", result);
				}
			} else if (pos == 1) {

				String result = Web.Request("http://www." + LccHolder.HOST + "/api/echess_open_invites?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&declineinviteid=" + gameListElement.values.get(AppConstants.GAME_ID), "GET", null, null);
				if (result.contains("Success")) {
					update(3);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(LiveNewGameActivity.this, "Error", result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(OnlineNewGame.this, "Error", result);
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?>  a, View v, int pos, long id) {
		gameListElement = gameListItems.get(pos);
		if (gameListElement.type == 0) {
			final String title = gameListElement.values.get("opponent_chess_title") ;

			if (gameListElement.values.get("is_direct_challenge").equals("1") && gameListElement.values.get("is_released_by_me").equals("0")) {
				new AlertDialog.Builder(LiveNewGameActivity.this)
						.setTitle(title)
						.setItems(new String[]{getString(R.string.accept),
								getString(R.string.decline)}, challengeDialogListener)
						.create().show();
			} else if (gameListElement.values.get("is_direct_challenge").equals("1") && gameListElement.values.get("is_released_by_me").equals("1")) {
				new AlertDialog.Builder(LiveNewGameActivity.this)
						.setTitle(title)
						.setItems(new String[]{"Cancel", "Keep"}, directChallengeDialogListener)
						.create().show();
			} else if (gameListElement.values.get("is_direct_challenge").equals("0")
					&& gameListElement.values.get("is_released_by_me").equals("0")) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.values.get(AppConstants.GAME_ID));
				LccHolder.LOG.info("Accept seek: " + challenge);
				lccHolder.getAndroid().runAcceptChallengeTask(challenge);
				lccHolder.removeSeek(gameListElement.values.get(AppConstants.GAME_ID));
				update(2);
			} else if (gameListElement.values.get("is_direct_challenge").equals("0")
					&& gameListElement.values.get("is_released_by_me").equals("1")) {
				new AlertDialog.Builder(LiveNewGameActivity.this)
						.setTitle(title)
						.setItems(new String[]{"Cancel", "Keep"}, releasedByMeDialogListener)
						.create().show();
			}

		}
	}
}
