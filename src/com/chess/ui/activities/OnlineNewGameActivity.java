package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListItem;
import com.chess.ui.adapters.OnlineChallengesGamesAdapter;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;

public class OnlineNewGameActivity extends LiveBaseActivity implements OnClickListener, OnItemClickListener {

	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";

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
	private LoadItem listLoadItem;
	private ListUpdateListener listUpdateListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_new_game);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (MopubHelper.isShowAds(this)) {
			MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
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

		listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

		listUpdateListener = new ListUpdateListener();
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
			gameListItems.clear();
			gameListItems.addAll(ChessComApiParser.ViewOpenChallengeParse(returnedObj));

			if (gamesAdapter == null) {
				gamesAdapter = new OnlineChallengesGamesAdapter(getContext(),  gameListItems);
				openChallengesListView.setAdapter(gamesAdapter);
			}

			gamesAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void update(int code) {
//		if (code == INIT_ACTIVITY) {
//			if (appService != null) {
//				appService.RunRepeatableTask(OnlineScreenActivity.ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
//						"http://www." + LccHolder.HOST + AppConstants.API_ECHESS_OPEN_INVITES_ID +
//								preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY),
//						null);
//			}
//		} else if (code == OnlineScreenActivity.ONLINE_CALLBACK_CODE) {
//			openChallengesListView.setVisibility(View.GONE);
//			gameListItems.clear();
//
//			gameListItems.addAll(ChessComApiParser.ViewOpenChallengeParse(responseRepeatable));
//
//			if (gamesAdapter == null) {
//				gamesAdapter = new OnlineChallengesGamesAdapter(this, gameListItems);
//				openChallengesListView.setAdapter(gamesAdapter);
//			}
//
//			gamesAdapter.notifyDataSetChanged();
//			openChallengesListView.setVisibility(View.VISIBLE);
//
//		} else if (code == CHALLENGE_RESULT_SENT) {
//			showToast(successToastMsgId);
//			onPause();
//			onResume();
//		} else if (code == 4) {
//			onPause();
//			onResume();
//		}
	}


	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));

		} else if (view.getId() == R.id.friendchallenge) {
			startActivity(new Intent(this, OnlineFriendChallengeActivity.class));

		} else if (view.getId() == R.id.challengecreate) {
			startActivity(new Intent(this, OnlineCreateChallengeActivity.class));
		}
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

//	private DialogInterface.OnClickListener echessDialogListener = new DialogInterface.OnClickListener() {// TODO change to PopupDialog
//		@Override
//		public void onClick(DialogInterface d, int pos) {
//			LoadItem loadItem = new LoadItem();
//			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
//			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
//
//			if (pos == ACCEPT_DRAW) {
//				loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, String.valueOf(gameListElement.getGameId()));
//				successToastMsgId = R.string.challengeaccepted;
//			} else if (pos == DECLINE_DRAW) {
//				loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, String.valueOf(gameListElement.getGameId()));
//				successToastMsgId = R.string.challengedeclined;
//			}
//
//			new GetStringObjTask(challengeInviteUpdateListener).execute(loadItem);
//		}
//	};

	@Override
	public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
		gameListElement = gameListItems.get(pos);
		if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
			String title = "Win: " + gameListElement.values.get(GameListItem.OPPONENT_WIN_COUNT)
					+ " Loss: " + gameListElement.values.get(GameListItem.OPPONENT_LOSS_COUNT)
					+ " Draw: " + gameListElement.values.get(GameListItem.OPPONENT_DRAW_COUNT);

			showPopupDialog(title, CHALLENGE_ACCEPT_TAG);
			popupItem.setPositiveBtnId(R.string.accept);
			popupItem.setNegativeBtnId(R.string.decline);

//			new AlertDialog.Builder(OnlineNewGameActivity.this)
//					.setTitle(title)
//					.setItems(new String[]{getString(R.string.accept),
//							getString(R.string.decline)}, echessDialogListener)
//					.create().show();
		}
	}

	private class ChallengeInviteUpdateListener extends ChessUpdateListener {
		public ChallengeInviteUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if(isPaused)
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
//				update(CHALLENGE_RESULT_SENT);
				showToast(successToastMsgId);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
//				mainApp.showDialog(OnlineNewGameActivity.this, AppConstants.ERROR, returnedObj.split("[+]")[1]);
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}
}
