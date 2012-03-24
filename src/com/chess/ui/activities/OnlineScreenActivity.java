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
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListElement;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.adapters.OnlineGamesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.chess.utilities.Web;
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
public class OnlineScreenActivity extends CoreActivityActionBar implements View.OnClickListener {
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

	private GameListElement gameListElement;

	private AcceptDrawDialogListener acceptDrawDialogListener;
	private GameTypesSelectedListener gameTypesSelectedListener;
	private GameListItemClickListener gameListItemClickListener;
	private GameListItemLongClickListener gameListItemLongClickListener;
	//	private NewGamesButtonsAdapter newGamesButtonsAdapter;
	private GameListItemDialogListener gameListItemDialogListener;
	private ChallengeDialogListener challengeDialogListener;
	private IsDirectDialogChallengeListener isDirectDialogChallengeListener;
	private IsIndirectDialogListener indirectDialogListener;
	private NonLiveDialogListener nonLiveDialogListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_screen);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);
		if (MopubHelper.isShowAds(mainApp)) {
			MopubHelper.showBannerAd(upgradeBtn, (MoPubView) findViewById(R.id.mopub_adview), mainApp);
		}

		init();
		queries = new String[]{
				"http://www." + LccHolder.HOST + "/api/v2/get_echess_current_games?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&all=1",
				"http://www." + LccHolder.HOST + "/api/echess_challenges?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, ""),
				"http://www." + LccHolder.HOST + "/api/v2/get_echess_finished_games?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")};

		gamesTypeSpinner = (Spinner) findViewById(R.id.gamestypes);
		gamesTypeSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.onlineSpinner));

		mainApp.setLiveChess(extras.getBoolean(AppConstants.LIVE_CHESS));

		gamesTypeSpinner.post(new Runnable() {
			@Override
			public void run() {
				gamesTypeSpinner.setSelection(mainApp.getSharedData().getInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1));
//				gamesTypeSpinner.setSelection(currentListType);
			}
		});
		gamesTypeSpinner.setOnItemSelectedListener(gameTypesSelectedListener);

		gamesList = (ListView) findViewById(R.id.GamesList);
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
										+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
										+ AppConstants.CHESSID_PARAMETER + gameListElement.values.get(GameListElement.GAME_ID)
										+ "&command=ACCEPTDRAW&timestamp="
										+ gameListElement.values.get(GameListElement.TIMESTAMP),
								null/*progressDialog = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
						);
					}
				}
				break;
				case DialogInterface.BUTTON_NEUTRAL: {
					if (appService != null) {
						appService.RunSingleTask(4,
								"http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + AppConstants.CHESSID_PARAMETER + gameListElement.values.get(GameListElement.GAME_ID) + "&command=DECLINEDRAW&timestamp=" + gameListElement.values.get(GameListElement.TIMESTAMP),
								null/*progressDialog = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
						);
					}
				}
				break;
				case DialogInterface.BUTTON_NEGATIVE: {
					startActivity(new Intent(coreContext, GameOnlineScreenActivity.class).
							putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS).
							putExtra(GameListElement.GAME_ID, gameListElement.values.get(GameListElement.GAME_ID)));

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
//					final GameListElement el = mainApp.getGameListItems().get(temp_pos);
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

	protected void onResume() {
		/*if (isShowAds() && (!mainApp.isLiveChess() || (mainApp.isLiveChess() && lccHolder.isConnected()))) {
			  MobclixHelper.showBannerAd(getBannerAdviewWrapper(), removeAds, this, mainApp);
			}*/
		/*if (!mainApp.isNetworkChangedNotification())
			{*/
		//}

		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));

		// if connected
		//System.out.println("MARKER++++++++++++++++++++++++++++++++++++++++++++++++++++ LOGOUT");
		lccHolder.logout();
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

		new Handler().post(new Runnable() {
			public void run() {
				disableScreenLock();
			}
		});
	}

	@Override
	protected void onPause() {
		gamesList.setVisibility(View.GONE);
		unregisterReceiver(this.lccLoggingInInfoReceiver);
		if (mainApp.isLiveChess()) {
			/*// if connected
				  System.out.println("MARKER++++++++++++++++++++++++++++++++++++++++++++++++++++ LOGOUT");
				  lccHolder.logout();*/
			unregisterReceiver(challengesListUpdateReceiver);
		}
		super.onPause();
		enableScreenLock();
	}

	private void init() {
		acceptDrawDialogListener = new AcceptDrawDialogListener();
		gameTypesSelectedListener = new GameTypesSelectedListener();
		gameListItemClickListener = new GameListItemClickListener();
		gameListItemLongClickListener = new GameListItemLongClickListener();
//		newGamesButtonsAdapter = new NewGamesButtonsAdapter();
		gameListItemDialogListener = new GameListItemDialogListener();
		challengeDialogListener = new ChallengeDialogListener();
		isDirectDialogChallengeListener = new IsDirectDialogChallengeListener();
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
//			final GameListElement el = mainApp.getGameListItems().get(pos);

			if (pos == 0) {
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get(GameListElement.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, ChatActivity.class);
				intent.putExtra(GameListElement.GAME_ID, gameListElement.values.get(GameListElement.GAME_ID));
				intent.putExtra(GameListElement.TIMESTAMP, gameListElement.values.get(GameListElement.TIMESTAMP));
				startActivity(intent);
			} else if (pos == 1) {
				String Draw = AppConstants.OFFERDRAW;
				if (gameListElement.values.get(GameListElement.IS_DRAW_OFFER_PENDING).equals("p"))
					Draw = AppConstants.ACCEPTDRAW;

				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.CHESSID_PARAMETER + gameListElement.values.get(GameListElement.GAME_ID)
						+ AppConstants.COMMAND_PARAMETER + Draw + AppConstants.TIMESTAMP_PARAMETER
						+ gameListElement.values.get(GameListElement.TIMESTAMP), "GET", null, null);

				if (result.contains(AppConstants.SUCCESS)) {
					mainApp.ShowMessage(getString(R.string.accepted));
					update(1);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Online.this, "Error", result);
				}
			} else if (pos == 2) {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.CHESSID_PARAMETER + gameListElement.values.get(GameListElement.GAME_ID)
						+ AppConstants.COMMAND_RESIGN__AND_TIMESTAMP_PARAMETER
						+ gameListElement.values.get(GameListElement.TIMESTAMP), "GET", null, null);

				if (result.contains(AppConstants.SUCCESS)) {
					update(1);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Online.this, "Error", result);
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
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get(GameListElement.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, ChatActivity.class);
				intent.putExtra(GameListElement.GAME_ID, gameListElement.values.get(GameListElement.GAME_ID));
				intent.putExtra(GameListElement.TIMESTAMP, gameListElement.values.get(GameListElement.TIMESTAMP));
				startActivity(intent);
			}
			return true;
		}
	}

	private class ChallengeDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface d, int pos) {

//			final GameListElement el = mainApp.getGameListItems().get(pos);

			if (pos == 0) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(GameListElement.GAME_ID));
				LccHolder.LOG.info("Accept challenge: " + challenge);
				lccHolder.getAndroid().runAcceptChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.values.get(GameListElement.GAME_ID));
				update(2);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(GameListElement.GAME_ID));
				LccHolder.LOG.info("Decline challenge: " + challenge);
				lccHolder.getAndroid().runRejectChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.values.get(GameListElement.GAME_ID));
				update(3);
			}
		}
	}

	private class IsDirectDialogChallengeListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListElement el = mainApp.getGameListItems().get(pos);
			if (pos == 0) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(GameListElement.GAME_ID));
				LccHolder.LOG.info("Cancel my challenge: " + challenge);
				lccHolder.getAndroid().runCancelChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.values.get(GameListElement.GAME_ID));
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.values.get(GameListElement.GAME_ID));
				LccHolder.LOG.info("Just keep my challenge: " + challenge);
			}
		}
	}

	private class IsIndirectDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListElement el = mainApp.getGameListItems().get(pos);
			if (pos == 0) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.values.get(GameListElement.GAME_ID));
				LccHolder.LOG.info("Cancel my seek: " + challenge);
				lccHolder.getAndroid().runCancelChallengeTask(challenge);
				lccHolder.removeSeek(gameListElement.values.get(GameListElement.GAME_ID));
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getSeek(gameListElement.values.get(GameListElement.GAME_ID));
				LccHolder.LOG.info("Just keep my seek: " + challenge);
			}
		}
	}

	private class NonLiveDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListElement el = mainApp.getGameListItems().get(pos);

			if (pos == 0) {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ "/api/echess_open_invites?id="
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ "&acceptinviteid=" + gameListElement.values.get(GameListElement.GAME_ID), "GET", null, null);
				if (result.contains(AppConstants.SUCCESS)) {
					update(2);
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
					mainApp.ShowDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Online.this, "Error", result);
				}
			} else if (pos == 1) {

				String result = Web.Request("http://www." + LccHolder.HOST
						+ "/api/echess_open_invites?id="
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ "&declineinviteid=" + gameListElement.values.get(GameListElement.GAME_ID), "GET", null, null);
				if (result.contains(AppConstants.SUCCESS)) {
					update(3);
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
					mainApp.ShowDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Online.this, "Error", result);
				}
			}
		}
	}

	private class GameListItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
			gameListElement = mainApp.getGameListItems().get(pos);
			if (gameListElement.type == GameListElement.LIST_TYPE_CHALLENGES) {
				final String title = mainApp.isLiveChess() ?
						gameListElement.values.get(GameListElement.OPPONENT_CHESS_TITLE) :
						"Win: " + gameListElement.values.get(GameListElement.OPPONENT_WIN_COUNT)
								+ " Loss: " + gameListElement.values.get(GameListElement.OPPONENT_LOSS_COUNT)
								+ " Draw: " + gameListElement.values.get(GameListElement.OPPONENT_DRAW_COUNT);

				if (mainApp.isLiveChess()) {
					if (gameListElement.values.get(GameListElement.IS_DIRECT_CHALLENGE).equals("1") && gameListElement.values.get(GameListElement.IS_RELEASED_BY_ME).equals("0")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{
										getString(R.string.accept),
										getString(R.string.decline)},
										challengeDialogListener)
								.create().show();
					} else if (gameListElement.values.get(GameListElement.IS_DIRECT_CHALLENGE).equals("1") && gameListElement.values.get(GameListElement.IS_RELEASED_BY_ME).equals("1")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{"Cancel", "Keep"}, isDirectDialogChallengeListener)
								.create().show();
					} else if (gameListElement.values.get(GameListElement.IS_DIRECT_CHALLENGE).equals("0") && gameListElement.values.get(GameListElement.IS_RELEASED_BY_ME).equals("0")) {
						final Challenge challenge = lccHolder.getSeek(gameListElement.values.get(GameListElement.GAME_ID));
						LccHolder.LOG.info("Accept seek: " + challenge);
						lccHolder.getAndroid().runAcceptChallengeTask(challenge);
						lccHolder.removeSeek(gameListElement.values.get(GameListElement.GAME_ID));
						update(2);
					} else if (gameListElement.values.get(GameListElement.IS_DIRECT_CHALLENGE).equals("0") && gameListElement.values.get(GameListElement.IS_RELEASED_BY_ME).equals("1")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{"Cancel", "Keep"}, indirectDialogListener)
								.create().show();
					}
				} // echess
				else {
					new AlertDialog.Builder(coreContext)
							.setTitle(title)
							.setItems(new String[]{
									getString(R.string.accept),
									getString(R.string.decline)}, nonLiveDialogListener
							)
							.create().show();
				}

			} else if (gameListElement.type == GameListElement.LIST_TYPE_CURRENT) {
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get(GameListElement.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				if (gameListElement.values.get(GameListElement.IS_DRAW_OFFER_PENDING).equals("p")) {
					mainApp.acceptdraw = true;
					temp_pos = pos;
					showDialog(0);
				} else {
					mainApp.acceptdraw = false;

					Intent intent = new Intent(coreContext, GameOnlineScreenActivity.class);
					intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
					intent.putExtra(GameListElement.GAME_ID, gameListElement.values.get(GameListElement.GAME_ID));
					startActivity(intent);
				}
			} else if (gameListElement.type == 2) {
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get(GameListElement.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, GameOnlineScreenActivity.class);
				intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);
				intent.putExtra(GameListElement.GAME_ID, gameListElement.values.get(GameListElement.GAME_ID));
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
				} else {
					/*appService.RunRepeatble(ONLINE_CALLBACK_CODE, 0, 2000,
													  progressDialog = MyProgressDialog
														.show(Online.this, null, getString(R.string.updatinggameslist), true));*/
					update(ONLINE_CALLBACK_CODE);
				}
			}
		} else if (code == ONLINE_CALLBACK_CODE) {
			//int t = mainApp.sharedData.getInt("gamestype", 1);
			currentListType = mainApp.getSharedData().getInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1);
			ArrayList<GameListElement> tmp = new ArrayList<GameListElement>();
			gamesList.setVisibility(View.GONE);
			mainApp.getGameListItems().clear();
			if (gamesAdapter != null) {
				gamesAdapter.notifyDataSetChanged();
			}
			//gamesList.setVisibility(View.VISIBLE);
			if (mainApp.isLiveChess()) {
				tmp.addAll(lccHolder.getChallengesAndSeeksData());
			} else {
				if (currentListType == GameListElement.LIST_TYPE_CURRENT) {
//					tmp.addAll(ChessComApiParser.ViewChallengeParse(responseRepeatable));
					tmp.addAll(ChessComApiParser.GetCurrentOnlineGamesParse(responseRepeatable));
				}
				if (currentListType == GameListElement.LIST_TYPE_CHALLENGES) {
					tmp.addAll(ChessComApiParser.ViewChallengeParse(responseRepeatable));
//					tmp.addAll(ChessComApiParser.GetCurrentOnlineGamesParse(responseRepeatable));
				}
				if (currentListType == GameListElement.LIST_TYPE_FINISHED) {
					tmp.addAll(ChessComApiParser.GetFinishedOnlineGamesParse(responseRepeatable));
				}


//				if (currentListType == GameListElementOrig.LIST_TYPE_GAMES)
//				{
//					tmp.addAll(ChessComApiParser.GetCurrentOnlineGamesParse(responseRepeatable));
//				}
//				if (currentListType == GameListElement.LIST_TYPE_CHALLENGES)
//				{
//					tmp.addAll(ChessComApiParser.ViewChallengeParse(responseRepeatable));
//				}

			}

			//gamesList.setVisibility(View.GONE);
			mainApp.getGameListItems().addAll(tmp);
			for (GameListElement gameListItem : mainApp.getGameListItems()) {
				Log.d("GameLists", "game received" + gameListItem.toString());
			}
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
			mainApp.ShowMessage(getString(R.string.challengeaccepted));
		} else if (code == 3) {
			onPause();
			onResume();
			mainApp.ShowMessage(getString(R.string.challengedeclined));
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
			String GOTO = "http://www." + LccHolder.HOST + "/tournaments";
			try {
				GOTO = URLEncoder.encode(GOTO, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www."
					+ LccHolder.HOST + "/login.html?als="
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
					+ "&goto=" + GOTO)));
		} else if (view.getId() == R.id.stats) {
			// TODO hide to RestHelper
			String GOTO = "http://www." + LccHolder.HOST + "/echess/mobile-stats/"
					+ mainApp.getSharedData().getString(AppConstants.USERNAME, "");
			try {
				GOTO = URLEncoder.encode(GOTO, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST + "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&goto=" + GOTO)));
		} else if (view.getId() == R.id.currentGame) {
			/*try
						{*/
			if (lccHolder.getCurrentGameId() != null && lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
				lccHolder.processFullGame(lccHolder.getGame(lccHolder.getCurrentGameId()));
			}
			/*}
						catch(Exception e)
						{
						  e.printStackTrace();
						  System.out.println("!!!!!!!! mainApp.getGameId() " + mainApp.getGameId());
						  System.out.println("!!!!!!!! lccHolder.getGame(mainApp.getGameId()) " + lccHolder.getGame(mainApp.getGameId()));
						}*/
		} else if (view.getId() == R.id.start) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
//			finish();
//			LoadNext(0);
		}
	}

	private enum StartNewGameButtonsEnum {
		/*BUTTON_10_0(10, 0, "10 min"),
			BUTTON_5_0(5, 0, "5 min"),
			BUTTON_3_0(3, 0, "3 min"),
			BUTTON_30_0(30, 0, "30 min"),
			BUTTON_2_12(2, 12, "2 | 12"),
			BUTTON_1_5(1, 5, "1 | 5");*/

		BUTTON_10_0(10, 0, "10 min"),
		BUTTON_5_2(5, 2, "5 | 2"),
		BUTTON_15_10(15, 10, "15 | 10"),
		BUTTON_30_0(30, 0, "30 min"),
		BUTTON_5_0(5, 0, "5 min"),
		BUTTON_3_0(3, 0, "3 min"),
		BUTTON_2_1(2, 1, "2 | 1"),
		BUTTON_1_0(1, 0, "1 min");

		private int min;
		private int sec;
		private String text;

		private StartNewGameButtonsEnum(int min, int sec, String text) {
			this.min = min;
			this.sec = sec;
			this.text = text;
		}

		public int getMin() {
			return min;
		}

		public int getSec() {
			return sec;
		}

		public String getText() {
			return text;
		}
	}

	private BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			new Handler().post(new Runnable() {
				public void run() {

				}
			});
		}
	};
}