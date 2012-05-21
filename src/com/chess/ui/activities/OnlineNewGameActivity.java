package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.lcc.android.LccHolder;
import com.chess.model.GameListItem;
import com.chess.ui.adapters.OnlineChallengesGamesAdapter;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;

public class OnlineNewGameActivity extends LiveBaseActivity implements OnClickListener, OnItemClickListener {

	private ListView openChallengesListView;
	private ArrayList<GameListItem> gameListItems = new ArrayList<GameListItem>();
	private OnlineChallengesGamesAdapter gamesAdapter = null;
	private static final int UPDATE_DELAY = 120000;
	private GameListItem gameListElement;
	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private static final int CHALLENGE_RESULT_SENT = 2;
	private int successToastMsgId;
	private static final int ACCEPT_DRAW = 0;
	private static final int DECLINE_DRAW = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_new_game);
//		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);
		if (MopubHelper.isShowAds(mainApp)) {
			moPubView = (MoPubView) findViewById(R.id.mopub_adview);
			MopubHelper.showBannerAd(upgradeBtn, moPubView, mainApp);
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
		findViewById(R.id.challengecreate).setOnClickListener(this);
	}

	private void init() {
		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();
	}

	@Override
	protected void onPause() {
		/*if (MobclixHelper.isShowAds(mainApp)) {
			MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
		}*/
		super.onPause();
	}

	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (appService != null) {
				appService.RunRepeatableTask(OnlineScreenActivity.ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
						"http://www." + LccHolder.HOST + AppConstants.API_ECHESS_OPEN_INVITES_ID +
								mainApp.getSharedData().getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY),
						null);
			}
		} else if (code == OnlineScreenActivity.ONLINE_CALLBACK_CODE) {
			openChallengesListView.setVisibility(View.GONE);
			gameListItems.clear();

			gameListItems.addAll(ChessComApiParser.ViewOpenChallengeParse(responseRepeatable));

			if (gamesAdapter == null) {
				gamesAdapter = new OnlineChallengesGamesAdapter(this, gameListItems);
				openChallengesListView.setAdapter(gamesAdapter);
			}

			gamesAdapter.notifyDataSetChanged();
			openChallengesListView.setVisibility(View.VISIBLE);

		} else if (code == CHALLENGE_RESULT_SENT) {
			showToast(successToastMsgId);
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
		}
	}

	private DialogInterface.OnClickListener echessDialogListener = new DialogInterface.OnClickListener() {// TODO change to PopupDialog
		@Override
		public void onClick(DialogInterface d, int pos) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			if (pos == ACCEPT_DRAW) {
				loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, String.valueOf(gameListElement.getGameId()));
				successToastMsgId = R.string.challengeaccepted;
			} else if (pos == DECLINE_DRAW) {
				loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, String.valueOf(gameListElement.getGameId()));
				successToastMsgId = R.string.challengedeclined;
			}

			new GetStringObjTask(challengeInviteUpdateListener).execute(loadItem);
		}
	};

	@Override
	public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
		gameListElement = gameListItems.get(pos);
		if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
			final String title = "Win: " + gameListElement.values.get(GameListItem.OPPONENT_WIN_COUNT)
					+ " Loss: " + gameListElement.values.get(GameListItem.OPPONENT_LOSS_COUNT)
					+ " Draw: " + gameListElement.values.get(GameListItem.OPPONENT_DRAW_COUNT);

			new AlertDialog.Builder(OnlineNewGameActivity.this)  // TODO change to PopupDialog
					.setTitle(title)
					.setItems(new String[]{getString(R.string.accept),
							getString(R.string.decline)}, echessDialogListener)
					.create().show();
		}
	}

	private class ChallengeInviteUpdateListener extends ChessUpdateListener {
		public ChallengeInviteUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if(isFinishing())
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				update(CHALLENGE_RESULT_SENT);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				mainApp.showDialog(OnlineNewGameActivity.this, AppConstants.ERROR, returnedObj.split("[+]")[1]);
			}
		}
	}
}
