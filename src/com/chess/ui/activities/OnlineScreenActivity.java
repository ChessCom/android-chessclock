package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.Web;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListItem;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.adapters.OnlineGamesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * OnlineScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:12
 */
public class OnlineScreenActivity extends LiveBaseActivity implements View.OnClickListener {
	private ListView gamesList;
	private Spinner gamesTypeSpinner;
	private OnlineGamesAdapter gamesAdapter = null;
	private Button upgradeBtn;

	private String[] queries;
	private boolean compleated = false;
	private int UPDATE_DELAY = 120000;
	private int temp_pos = -1;
	private int currentListType = 0;

	public static int ONLINE_CALLBACK_CODE = 32;

	private GameListItem gameListElement;

	private AcceptDrawDialogListener acceptDrawDialogListener;
	private GameTypesSelectedListener gameTypesSelectedListener;
	private GameListItemClickListener gameListItemClickListener;
	private GameListItemLongClickListener gameListItemLongClickListener;
	//	private NewGamesButtonsAdapter newGamesButtonsAdapter;
	private GameListItemDialogListener gameListItemDialogListener;
//	private ChallengeDialogListener challengeDialogListener;
//	private IsDirectDialogChallengeListener isDirectDialogChallengeListener;
	private IsIndirectDialogListener indirectDialogListener;
	private NonLiveDialogListener nonLiveDialogListener;



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_screen);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		if (MopubHelper.isShowAds(mainApp)) {
            moPubView = (MoPubView) findViewById(R.id.mopub_adview);
			MopubHelper.showBannerAd(upgradeBtn, moPubView, mainApp);
		}

		init();
		queries = new String[]{
				"http://www." + LccHolder.HOST + AppConstants.API_V2_GET_ECHESS_CURRENT_GAMES_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY) + "&all=1",
				"http://www." + LccHolder.HOST + AppConstants.API_ECHESS_CHALLENGES_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY),
				"http://www." + LccHolder.HOST + AppConstants.API_V2_GET_ECHESS_FINISHED_GAMES_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)};

		gamesTypeSpinner = (Spinner) findViewById(R.id.gamestypes);
		gamesTypeSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.onlineSpinner));

		mainApp.setLiveChess(false);

		gamesTypeSpinner.post(new Runnable() {
			@Override
			public void run() {
				gamesTypeSpinner.setSelection(mainApp.getSharedData().getInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1));
//				gamesTypeSpinner.setSelection(currentListType);
			}
		});
		gamesTypeSpinner.setOnItemSelectedListener(gameTypesSelectedListener);

		gamesList = (ListView) findViewById(R.id.onlineGamesList);
		gamesList.setOnItemClickListener(gameListItemClickListener);
		gamesList.setOnItemLongClickListener(gameListItemLongClickListener);

		findViewById(R.id.tournaments).setOnClickListener(this);
		findViewById(R.id.stats).setOnClickListener(this);
		findViewById(R.id.start).setOnClickListener(this);
	}


	private class AcceptDrawDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			gameListElement = mainApp.getGameListItems().get(temp_pos);

			switch (whichButton) {
				case DialogInterface.BUTTON_POSITIVE: {
					if (appService != null) {
						appService.RunSingleTask(4,
								"http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID
										+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
										+ AppConstants.CHESSID_PARAMETER + gameListElement.getGameId()
										+ "&command=ACCEPTDRAW&timestamp="
										+ gameListElement.values.get(GameListItem.TIMESTAMP),
								null/*progressDialog = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
						);
					}
				}
				break;
				case DialogInterface.BUTTON_NEUTRAL: {
					if (appService != null) {
						appService.RunSingleTask(4,
								"http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY) + AppConstants.CHESSID_PARAMETER + gameListElement.getGameId() + "&command=DECLINEDRAW&timestamp=" + gameListElement.values.get(GameListItem.TIMESTAMP),
								null/*progressDialog = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
						);
					}
				}
				break;
				case DialogInterface.BUTTON_NEGATIVE: {
					startActivity(new Intent(coreContext, GameOnlineScreenActivity.class).
							putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS).
							putExtra(GameListItem.GAME_ID, gameListElement.getGameId()));

				}
				break;
				default:
					break;
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0: {
				if (temp_pos > -1) {
//					final GameListItem el = mainApp.getGameListItems().get(temp_pos);
					return new AlertDialog.Builder(this)
							.setTitle(getString(R.string.accept_draw_q))
							.setPositiveButton(getString(R.string.accept), acceptDrawDialogListener)
							.setNeutralButton(getString(R.string.decline), acceptDrawDialogListener)
							.setNegativeButton(getString(R.string.game), acceptDrawDialogListener).create();
				}
			}
			default:
				break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onStop() {
		gamesList.setVisibility(View.GONE);
		super.onStop();
	}

	@Override
	protected void onRestart() {
		gamesList.setVisibility(View.VISIBLE);
		super.onRestart();
	}

	@Override
	protected void onResume() {
		/*if (isShowAds() && (!mainApp.isLiveChess() || (mainApp.isLiveChess() && lccHolder.isConnected()))) {
			  MobclixHelper.showBannerAd(getBannerAdviewWrapper(), removeAds, this, mainApp);
			}*/
		/*if (!mainApp.isNetworkChangedNotification())
			{*/
		//}

		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));

		super.onResume();

		gamesList.setVisibility(View.VISIBLE);

		/*if (gamesAdapter != null)
			{
			  gamesAdapter.clear();
			  gamesAdapter = null;
			  mainApp.getGameListItems().clear();
			  gamesAdapter.notifyDataSetChanged();
			}
			else
			{
			  mainApp.getGameListItems().clear();
			}*/

//		enableScreenLockTimer();
	}

	@Override
	protected void onPause() {
		gamesList.setVisibility(View.GONE);
		unregisterReceiver(this.lccLoggingInInfoReceiver);
		/*if (mainApp.isLiveChess()) {
			unregisterReceiver(challengesListUpdateReceiver);
		}*/
		super.onPause();
//		enableScreenLock();
	}

	private void init() {
		acceptDrawDialogListener = new AcceptDrawDialogListener();
		gameTypesSelectedListener = new GameTypesSelectedListener();
		gameListItemClickListener = new GameListItemClickListener();
		gameListItemLongClickListener = new GameListItemLongClickListener();
		gameListItemDialogListener = new GameListItemDialogListener();
//		challengeDialogListener = new ChallengeDialogListener();
//		isDirectDialogChallengeListener = new IsDirectDialogChallengeListener();
		indirectDialogListener = new IsIndirectDialogListener();
		nonLiveDialogListener = new NonLiveDialogListener();
	}

	private class GameTypesSelectedListener implements AdapterView.OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
			gamesAdapter = null;
			mainApp.getSharedDataEditor().putInt(AppConstants.ONLINE_GAME_LIST_TYPE, pos);
//			currentListType = pos;
			mainApp.getSharedDataEditor().commit();
			if (compleated && appService != null && appService.getRepeatableTimer() != null) {
				onPause();
				onResume();
			}
			compleated = true;
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	}

	private class GameListItemDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, ChatActivity.class);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
				intent.putExtra(GameListItem.TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));
				startActivity(intent);
			} else if (pos == 1) {
				String Draw = AppConstants.OFFERDRAW;
				if (gameListElement.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p"))
					Draw = AppConstants.ACCEPTDRAW;

				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
						+ AppConstants.CHESSID_PARAMETER + gameListElement.getGameId()
						+ AppConstants.COMMAND_PARAMETER + Draw + AppConstants.TIMESTAMP_PARAMETER
						+ gameListElement.values.get(GameListItem.TIMESTAMP), "GET", null, null);

				if (result.contains(RestHelper.R_SUCCESS)) {
					mainApp.showToast(getString(R.string.accepted));
					update(1);
				} else if (result.contains("Error+")) {
					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(Online.this, "Error", result);
				}
			} else if (pos == 2) {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
						+ AppConstants.CHESSID_PARAMETER + gameListElement.getGameId()
						+ AppConstants.COMMAND_RESIGN__AND_TIMESTAMP_PARAMETER
						+ gameListElement.values.get(GameListItem.TIMESTAMP), "GET", null, null);

				if (result.contains(RestHelper.R_SUCCESS)) {
					update(1);
				} else if (result.contains("Error+")) {
					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(Online.this, "Error", result);
				}
			}
		}
	}


	private class GameListItemLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> a, View v, int pos, long id) {
			gameListElement = mainApp.getGameListItems().get(pos);
			if (gameListElement.type == 1) {
				new AlertDialog.Builder(coreContext)
						.setItems(new String[]{
								getString(R.string.chat),
								getString(R.string.drawoffer),
								getString(R.string.resignorabort)},
								gameListItemDialogListener)
						.create().show();
			} else if (gameListElement.type == 2) {
				mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, ChatActivity.class);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
				intent.putExtra(GameListItem.TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));
				startActivity(intent);
			}
			return true;
		}
	}

//	private class ChallengeDialogListener implements DialogInterface.OnClickListener {
//
//		@Override
//		public void onClick(DialogInterface d, int pos) {
//
////			final GameListItem el = mainApp.getGameListItems().get(pos);
//
//			if (pos == 0) {
//				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
//				LccHolder.LOG.info("Accept challenge: " + challenge);
//				lccHolder.getAndroid().runAcceptChallengeTask(challenge);
//				lccHolder.removeChallenge(gameListElement.getGameId());
//				update(2);
//			} else if (pos == 1) {
//				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
//				LccHolder.LOG.info("Decline challenge: " + challenge);
//				lccHolder.getAndroid().runRejectChallengeTask(challenge);
//				lccHolder.removeChallenge(gameListElement.getGameId());
//				update(3);
//			}
//		}
//	}
//
//	private class IsDirectDialogChallengeListener implements DialogInterface.OnClickListener {
//		@Override
//		public void onClick(DialogInterface d, int pos) {
////			final GameListItem el = mainApp.getGameListItems().get(pos);
//			if (pos == 0) {
//				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
//				LccHolder.LOG.info(AppConstants.CANCEL_MY_CHALLENGE + challenge);
//				lccHolder.getAndroid().runCancelChallengeTask(challenge);
//				lccHolder.removeChallenge(gameListElement.getGameId());
//				update(4);
//			} else if (pos == 1) {
//				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
//				LccHolder.LOG.info(AppConstants.JUST_KEEP_MY_CHALLENGE + challenge);
//			}
//		}
//	}

	private class IsIndirectDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListItem el = mainApp.getGameListItems().get(pos);
			if (pos == 0) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.getGameId());
				LccHolder.LOG.info("Cancel my seek: " + challenge);
				lccHolder.getAndroid().runCancelChallengeTask(challenge);
				lccHolder.removeSeek(gameListElement.getGameId());
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.getGameId());
				LccHolder.LOG.info("Just keep my seek: " + challenge);
			}
		}
	}

	private class NonLiveDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListItem el = mainApp.getGameListItems().get(pos);

			if (pos == 0) {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_ECHESS_OPEN_INVITES_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
						+ AppConstants.ACCEPT_INVITEID_PARAMETER + gameListElement.getGameId(), "GET", null, null);
				if (result.contains(RestHelper.R_SUCCESS)) {
					update(2);
				} else if (result.contains(RestHelper.R_ERROR)) {
					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(Online.this, "Error", result);
				}
			} else if (pos == 1) {

				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_ECHESS_OPEN_INVITES_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
						+ AppConstants.DECLINE_INVITEID_PARAMETER + gameListElement.getGameId(), "GET", null, null);
				if (result.contains(RestHelper.R_SUCCESS)) {
					update(3);
				} else if (result.contains(RestHelper.R_ERROR)) {
					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(Online.this, "Error", result);
				}
			}
		}
	}

	private class GameListItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
			gameListElement = mainApp.getGameListItems().get(pos);
			if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
				final String title = /*mainApp.isLiveChess() ?
						gameListElement.values.get(GameListItem.OPPONENT_CHESS_TITLE) :*/
						"Win: " + gameListElement.values.get(GameListItem.OPPONENT_WIN_COUNT)
								+ " Loss: " + gameListElement.values.get(GameListItem.OPPONENT_LOSS_COUNT)
								+ " Draw: " + gameListElement.values.get(GameListItem.OPPONENT_DRAW_COUNT);

				/*if (mainApp.isLiveChess()) {
					if (gameListElement.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("1") && gameListElement.values.get(GameListItem.IS_RELEASED_BY_ME).equals("0")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{
										getString(R.string.accept),
										getString(R.string.decline)},
										challengeDialogListener)
								.create().show();
					} else if (gameListElement.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("1") && gameListElement.values.get(GameListItem.IS_RELEASED_BY_ME).equals("1")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{"Cancel", "Keep"}, isDirectDialogChallengeListener)
								.create().show();
					} else if (gameListElement.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("0") && gameListElement.values.get(GameListItem.IS_RELEASED_BY_ME).equals("0")) {
						final Challenge challenge = lccHolder.getSeek(gameListElement.getGameId());
						LccHolder.LOG.info("Accept seek: " + challenge);
						lccHolder.getAndroid().runAcceptChallengeTask(challenge);
						lccHolder.removeSeek(gameListElement.getGameId());
						update(2);
					} else if (gameListElement.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("0") && gameListElement.values.get(GameListItem.IS_RELEASED_BY_ME).equals("1")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{"Cancel", "Keep"}, indirectDialogListener)
								.create().show();
					}
				} // echess
				else { */
					new AlertDialog.Builder(coreContext)
							.setTitle(title)
							.setItems(new String[]{
									getString(R.string.accept),
									getString(R.string.decline)}, nonLiveDialogListener
							)
							.create().show();
//				}

			} else if (gameListElement.type == GameListItem.LIST_TYPE_CURRENT) {
				mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				if (gameListElement.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p")) {
					mainApp.acceptdraw = true;
					temp_pos = pos;
					showDialog(0);
				} else {
					mainApp.acceptdraw = false;

					Intent intent = new Intent(coreContext, GameOnlineScreenActivity.class);
					intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
					intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
					startActivity(intent);
				}
			} else if (gameListElement.type == GameListItem.LIST_TYPE_FINISHED) {
				mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, GameOnlineScreenActivity.class);
				intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId()); // TODO eliminate strings parameters
				startActivity(intent);
			}
		}
	}

	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (appService != null) {
				if (!mainApp.isLiveChess()) {
					Log.d("web", "RunRepeatableTask(ONLINE_CALLBACK_CODE");
					appService.RunRepeatableTask(ONLINE_CALLBACK_CODE, 0, UPDATE_DELAY,
							queries[mainApp.getSharedData().getInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1)],
							null/*progressDialog = MyProgressDialog
									.show(Online.this, null, getString(R.string.updatinggameslist), true)*/);
				}/* else {
					*//*appService.RunRepeatble(ONLINE_CALLBACK_CODE, 0, 2000,
													  progressDialog = MyProgressDialog
														.show(Online.this, null, getString(R.string.updatinggameslist), true));*//*
					update(ONLINE_CALLBACK_CODE);
				}*/
			}
		} else if (code == ONLINE_CALLBACK_CODE) {
			//int t = mainApp.sharedData.getInt("gamestype", 1);
			currentListType = mainApp.getSharedData().getInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1);
			ArrayList<GameListItem> tmp = new ArrayList<GameListItem>();
			gamesList.setVisibility(View.GONE);
			mainApp.getGameListItems().clear();
			if (gamesAdapter != null) {
				gamesAdapter.notifyDataSetChanged();
			}
			//gamesList.setVisibility(View.VISIBLE);
			/*if (mainApp.isLiveChess()) {
				tmp.addAll(lccHolder.getChallengesAndSeeksData());
			} else {*/
				if (currentListType == GameListItem.LIST_TYPE_CURRENT) {
//					tmp.addAll(ChessComApiParser.ViewChallengeParse(responseRepeatable));
					tmp.addAll(ChessComApiParser.GetCurrentOnlineGamesParse(responseRepeatable));
				}
				if (currentListType == GameListItem.LIST_TYPE_CHALLENGES) {
					tmp.addAll(ChessComApiParser.ViewChallengeParse(responseRepeatable));
//					tmp.addAll(ChessComApiParser.GetCurrentOnlineGamesParse(responseRepeatable));
				}
				if (currentListType == GameListItem.LIST_TYPE_FINISHED) {
					tmp.addAll(ChessComApiParser.GetFinishedOnlineGamesParse(responseRepeatable));
				}

//			}

			//gamesList.setVisibility(View.GONE);
			mainApp.getGameListItems().addAll(tmp);
//			for (GameListItem gameListItem : mainApp.getGameListItems()) {
//				Log.d("GameLists", "game received" + gameListItem.toString());
//			}
			if (gamesAdapter != null) {
				gamesAdapter.notifyDataSetChanged();
			}
			gamesList.setVisibility(View.VISIBLE);
			if (gamesAdapter == null) {
				gamesAdapter = new OnlineGamesAdapter(OnlineScreenActivity.this, R.layout.gamelistelement, mainApp.getGameListItems());
				gamesList.setAdapter(gamesAdapter);
			} /*else {*/
			gamesAdapter.notifyDataSetChanged();
			//gamesList.setVisibility(View.VISIBLE);
			/*}*/
		} else if (code == 1) {
			onPause();
			onResume();
		} else if (code == 2) {
			onPause();
			onResume();
			mainApp.showToast(getString(R.string.challengeaccepted));
		} else if (code == 3) {
			onPause();
			onResume();
			mainApp.showToast(getString(R.string.challengedeclined));
		} else if (code == 4) {
			onPause();
			onResume();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(mainApp.getMembershipAndroidIntent());
		} else if (view.getId() == R.id.tournaments) {// !_Important_! Use instead of switch due issue of ADT14
			// TODO hide to RestHelper
			String GOTO = "http://www." + LccHolder.HOST + AppConstants.TOURNAMENTS;
			try {
				GOTO = URLEncoder.encode(GOTO, "UTF-8");
			} catch (UnsupportedEncodingException ignored) {
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www."
					+ LccHolder.HOST + AppConstants.LOGIN_HTML_ALS
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY)
					+ "&goto=" + GOTO)));
		} else if (view.getId() == R.id.stats) {
			// TODO hide to RestHelper
			String GOTO = "http://www." + LccHolder.HOST + AppConstants.ECHESS_MOBILE_STATS
					+ mainApp.getUserName();
			try {
				GOTO = URLEncoder.encode(GOTO, "UTF-8");
			} catch (UnsupportedEncodingException ignored) {
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + AppConstants.LOGIN_HTML_ALS + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY) + "&goto=" + GOTO)));
		} else if (view.getId() == R.id.start) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
		}
	}

	private BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			new Handler().post(new Runnable() {
				@Override
				public void run() {

				}
			});
		}
	};
}
