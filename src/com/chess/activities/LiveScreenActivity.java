package com.chess.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.adapters.OnlineGamesAdapter;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivityActionBar;
import com.chess.core.IntentConstants;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListElement;
import com.chess.utilities.Web;
import com.chess.views.BackgroundChessDrawable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * LiveScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class LiveScreenActivity extends CoreActivityActionBar implements View.OnClickListener {
	private ListView gamesList;
	private OnlineGamesAdapter gamesAdapter = null;
	private TextView challengesListTitle;
	private TextView startNewGameTitle;

	private Button currentGame;
	private Button start;
	private GridView gridview;

	private String[] queries;
	private boolean compleated = false;
	private int UPDATE_DELAY = 120000;
	private int temp_pos = -1;

	public static int ONLINE_CALLBACK_CODE = 32;

	private GameListElement gameListElement;

	private AcceptDrawDialogListener acceptDrawDialogListener;
	private GameListItemClickListener gameListItemClickListener;
	private GameListItemLongClickListener gameListItemLongClickListener;
	private GameListItemDialogListener gameListItemDialogListener;
	private ChallengeDialogListener challengeDialogListener;
	private IsDirectDialogChallengeListener isDirectDialogChallengeListener;
	private IsIndirencetDialogListener isIndirencetDialogListener;
	private NonLiveDialogListener nonLiveDialogListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_screen);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		init();
		queries = new String[]{
				"http://www." + LccHolder.HOST + "/api/echess_challenges?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, ""),
				"http://www." + LccHolder.HOST + "/api/v2/get_echess_current_games?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&all=1",
				"http://www." + LccHolder.HOST + "/api/v2/get_echess_finished_games?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")};

		challengesListTitle = (TextView) findViewById(R.id.challengesListTitle);
		startNewGameTitle = (TextView) findViewById(R.id.startNewGameTitle);

		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(this);

		/*if (!mainApp.isNetworkChangedNotification())
			{*/
		mainApp.setLiveChess(extras.getBoolean(AppConstants.LIVE_CHESS));
		//}
		gridview = (GridView) findViewById(R.id.gridview);


		if (lccHolder.isConnected()) {
			start.setVisibility(View.VISIBLE);
			gridview.setVisibility(View.VISIBLE);
			challengesListTitle.setVisibility(View.VISIBLE);
			startNewGameTitle.setVisibility(View.VISIBLE);
		}

		gamesList = (ListView) findViewById(R.id.GamesList);
		gamesList.setOnItemClickListener(gameListItemClickListener);
		gamesList.setOnItemLongClickListener(gameListItemLongClickListener);
		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(this);

		gridview.setAdapter(new NewGamesButtonsAdapter());
	}


	private class AcceptDrawDialogListener implements DialogInterface.OnClickListener {

		public void onClick(DialogInterface dialog, int whichButton) {
			gameListElement = mainApp.getGameListItems().get(temp_pos);

			switch (whichButton) {
				case DialogInterface.BUTTON_POSITIVE: {
					if (appService != null) {
						appService.RunSingleTask(4,
								"http://www." + LccHolder.HOST + "/api/submit_echess_action?id="
										+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
										+ "&chessid=" + gameListElement.values.get(AppConstants.GAME_ID)
										+ "&command=ACCEPTDRAW&timestamp="
										+ gameListElement.values.get(AppConstants.TIMESTAMP),
								null/*progressDialog = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
						);
					}
				}
				break;
				case DialogInterface.BUTTON_NEUTRAL: {
					if (appService != null) {
						appService.RunSingleTask(4,
								"http://www." + LccHolder.HOST + "/api/submit_echess_action?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + "&chessid=" + gameListElement.values.get(AppConstants.GAME_ID) + "&command=DECLINEDRAW&timestamp=" + gameListElement.values.get(AppConstants.TIMESTAMP),
								null/*progressDialog = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
						);
					}
				}
				break;
				case DialogInterface.BUTTON_NEGATIVE: {
					startActivity(new Intent(coreContext, GameLiveScreenActivity.class).
							putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS).
							putExtra(AppConstants.GAME_ID, gameListElement.values.get(AppConstants.GAME_ID)));

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
		mainApp.setLiveChess(extras.getBoolean(AppConstants.LIVE_CHESS));
		//}
		if (mainApp.isLiveChess() && !lccHolder.isConnected()) {
			new Handler().post(new Runnable() {
				public void run() {
					start.setVisibility(View.GONE);
					gridview.setVisibility(View.GONE);
					challengesListTitle.setVisibility(View.GONE);
					startNewGameTitle.setVisibility(View.GONE);
				}
			});
		}
		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
		registerReceiver(challengesListUpdateReceiver, new IntentFilter(IntentConstants.CHALLENGES_LIST_UPDATE));


		super.onResume();
		currentGame.post(new Runnable() {
			public void run() {
				if (mainApp.isLiveChess() && lccHolder.getCurrentGameId() != null &&
						lccHolder.getGame(lccHolder.getCurrentGameId()) != null) {
					currentGame.setVisibility(View.VISIBLE);
				} else {
					currentGame.setVisibility(View.GONE);
				}
			}
		});
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
		gameListItemClickListener = new GameListItemClickListener();
		gameListItemLongClickListener = new GameListItemLongClickListener();
		gameListItemDialogListener = new GameListItemDialogListener();
		challengeDialogListener = new ChallengeDialogListener();
		isDirectDialogChallengeListener = new IsDirectDialogChallengeListener();
		isIndirencetDialogListener = new IsIndirencetDialogListener();
		nonLiveDialogListener = new NonLiveDialogListener();
	}


	private class GameListItemDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListElement el = mainApp.getGameListItems().get(pos);

			if (pos == 0) {
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get("opponent_username"));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, ChatActivity.class);
				intent.putExtra(AppConstants.GAME_ID, gameListElement.values.get(AppConstants.GAME_ID));
				intent.putExtra(AppConstants.TIMESTAMP, gameListElement.values.get(AppConstants.TIMESTAMP));
				startActivity(intent);
			} else if (pos == 1) {
				String Draw = "OFFERDRAW";
				if (gameListElement.values.get("is_draw_offer_pending").equals("p"))
					Draw = "ACCEPTDRAW";

				String result = Web.Request("http://www." + LccHolder.HOST
						+ "/api/submit_echess_action?id="
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ "&chessid=" + gameListElement.values.get(AppConstants.GAME_ID)
						+ "&command=" + Draw + "&timestamp="
						+ gameListElement.values.get(AppConstants.TIMESTAMP), "GET", null, null);

				if (result.contains("Success")) {
					mainApp.ShowMessage(getString(R.string.accepted));
					update(1);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Online.this, "Error", result);
				}
			} else if (pos == 2) {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ "/api/submit_echess_action?id="
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ "&chessid=" + gameListElement.values.get(AppConstants.GAME_ID)
						+ "&command=RESIGN&timestamp="
						+ gameListElement.values.get(AppConstants.TIMESTAMP), "GET", null, null);

				if (result.contains("Success")) {
					update(1);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
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
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get("opponent_username"));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, ChatActivity.class);
				intent.putExtra(AppConstants.GAME_ID, gameListElement.values.get(AppConstants.GAME_ID));
				intent.putExtra(AppConstants.TIMESTAMP, gameListElement.values.get(AppConstants.TIMESTAMP));
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

	private class IsDirectDialogChallengeListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListElement el = mainApp.getGameListItems().get(pos);
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

	private class IsIndirencetDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListElement el = mainApp.getGameListItems().get(pos);
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

	private class NonLiveDialogListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListElement el = mainApp.getGameListItems().get(pos);

			if (pos == 0) {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ "/api/echess_open_invites?id="
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ "&acceptinviteid=" + gameListElement.values.get(AppConstants.GAME_ID), "GET", null, null);
				if (result.contains("Success")) {
					update(2);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
				} else {
					//mainApp.ShowDialog(Online.this, "Error", result);
				}
			} else if (pos == 1) {

				String result = Web.Request("http://www." + LccHolder.HOST
						+ "/api/echess_open_invites?id="
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ "&declineinviteid=" + gameListElement.values.get(AppConstants.GAME_ID), "GET", null, null);
				if (result.contains("Success")) {
					update(3);
				} else if (result.contains("Error+")) {
					mainApp.ShowDialog(coreContext, "Error", result.split("[+]")[1]);
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
			if (gameListElement.type == 0) {
				final String title = mainApp.isLiveChess() ?
						gameListElement.values.get("opponent_chess_title") :
						"Win: " + gameListElement.values.get("opponent_win_count")
								+ " Loss: " + gameListElement.values.get("opponent_loss_count")
								+ " Draw: " + gameListElement.values.get("opponent_draw_count");

				if (mainApp.isLiveChess()) {
					if (gameListElement.values.get("is_direct_challenge").equals("1") && gameListElement.values.get("is_released_by_me").equals("0")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{
										getString(R.string.accept),
										getString(R.string.decline)},
										challengeDialogListener)
								.create().show();
					} else if (gameListElement.values.get("is_direct_challenge").equals("1") && gameListElement.values.get("is_released_by_me").equals("1")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{"Cancel", "Keep"}, isDirectDialogChallengeListener)
								.create().show();
					} else if (gameListElement.values.get("is_direct_challenge").equals("0") && gameListElement.values.get("is_released_by_me").equals("0")) {
						final Challenge challenge = lccHolder.getSeek(gameListElement.values.get(AppConstants.GAME_ID));
						LccHolder.LOG.info("Accept seek: " + challenge);
						lccHolder.getAndroid().runAcceptChallengeTask(challenge);
						lccHolder.removeSeek(gameListElement.values.get(AppConstants.GAME_ID));
						update(2);
					} else if (gameListElement.values.get("is_direct_challenge").equals("0") && gameListElement.values.get("is_released_by_me").equals("1")) {
						new AlertDialog.Builder(coreContext)
								.setTitle(title)
								.setItems(new String[]{"Cancel", "Keep"}, isIndirencetDialogListener)
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

			} else if (gameListElement.type == 1) {
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get("opponent_username"));
				mainApp.getSharedDataEditor().commit();

				if (gameListElement.values.get("is_draw_offer_pending").equals("p")) {
					mainApp.acceptdraw = true;
					temp_pos = pos;
					showDialog(0);
				} else {
					mainApp.acceptdraw = false;

					Intent intent = new Intent(coreContext, GameLiveScreenActivity.class);
					intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
					intent.putExtra(AppConstants.GAME_ID, gameListElement.values.get(AppConstants.GAME_ID));
					startActivity(intent);
				}
			} else if (gameListElement.type == 2) {
				mainApp.getSharedDataEditor().putString("opponent", gameListElement.values.get("opponent_username"));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, GameLiveScreenActivity.class);
				intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);
				intent.putExtra(AppConstants.GAME_ID, gameListElement.values.get(AppConstants.GAME_ID));
				startActivity(intent);
			}
		}
	}

	private class NewGamesButtonsAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		private NewGamesButtonsAdapter() {
			this.inflater = LayoutInflater.from(coreContext);
		}

		public int getCount() {
			return StartNewGameButtonsEnum.values().length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final Button button;
			if (convertView == null) {
				button = (Button) inflater.inflate(R.layout.default_button_grey, null, false);
			} else {
				button = (Button) convertView;
			}
			StartNewGameButtonsEnum.values();
			final StartNewGameButtonsEnum startNewGameButton = StartNewGameButtonsEnum.values()[position];
			button.setText(startNewGameButton.getText());
			button.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					mainApp.getSharedDataEditor().putString(AppConstants.CHALLENGE_INITIAL_TIME, "" + startNewGameButton.getMin());
					mainApp.getSharedDataEditor().putString(AppConstants.CHALLENGE_BONUS_TIME, "" + startNewGameButton.getSec());
					mainApp.getSharedDataEditor().commit();
					startActivity(new Intent(coreContext, LiveCreateChallengeActivity.class));
				}
			});
			return button;
		}

	}


	@Override
	public void update(int code) {
		if (code == INIT_ACTIVITY) {
			if (appService != null) {

				/*appService.RunRepeatble(ONLINE_CALLBACK_CODE, 0, 2000,
																	  progressDialog = MyProgressDialog
																		.show(Online.this, null, getString(R.string.updatinggameslist), true));*/
				update(ONLINE_CALLBACK_CODE);
			}
		} else if (code == ONLINE_CALLBACK_CODE) {
			int t = mainApp.getSharedData().getInt(AppConstants.ONLINE_GAME_LIST_TYPE, 1);
			ArrayList<GameListElement> tmp = new ArrayList<GameListElement>();
			gamesList.setVisibility(View.GONE);

			mainApp.getGameListItems().clear();
//			if (gamesAdapter != null) {
//				gamesAdapter.notifyDataSetChanged();
//			}
			//gamesList.setVisibility(View.VISIBLE);
			tmp.addAll(lccHolder.getChallengesAndSeeksData());

			//gamesList.setVisibility(View.GONE);
			mainApp.getGameListItems().addAll(tmp);
			if (gamesAdapter != null) {
				gamesAdapter.notifyDataSetChanged();
			}
			gamesList.setVisibility(View.VISIBLE);
			if (gamesAdapter == null) {

//				if (t == 0 || mainApp.isLiveChess()) { //  This cases creates up to 3 instance of new OnlineGamesAdapter, replaced with else if cases
//					gamesAdapter = new OnlineGamesAdapter(Online.this, R.layout.gamelistelement, mainApp.getGameListItems());
//				}
//				if (t == 1 && !mainApp.isLiveChess()) {
//					gamesAdapter = new OnlineGamesAdapter(Online.this, R.layout.gamelistelement, mainApp.getGameListItems());
//				}
//				if (t == 2 && !mainApp.isLiveChess()) {
//					gamesAdapter = new OnlineGamesAdapter(Online.this, R.layout.gamelistelement, mainApp.getGameListItems());
//				}
				if (t == 0 || mainApp.isLiveChess()) { //
					gamesAdapter = new OnlineGamesAdapter(coreContext, R.layout.gamelistelement, mainApp.getGameListItems());
				} else if (t == 1 && !mainApp.isLiveChess()) {
					gamesAdapter = new OnlineGamesAdapter(coreContext, R.layout.gamelistelement, mainApp.getGameListItems());
				} else if (t == 2 && !mainApp.isLiveChess()) {
					gamesAdapter = new OnlineGamesAdapter(coreContext, R.layout.gamelistelement, mainApp.getGameListItems());
				}
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
		if (view.getId() == R.id.tournaments) {// !_Important_! Use instead of switch due issue of ADT14
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
			String GOTO = "http://www." + LccHolder.HOST + "/echess/mobile-stats/"
					+ mainApp.getSharedData().getString(AppConstants.USERNAME, "");
			try {
				GOTO = URLEncoder.encode(GOTO, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST
					+ "/login.html?als=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
					+ "&goto=" + GOTO)));
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
			startActivity(new Intent(this, LiveNewGameActivity.class));
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
					if (mainApp.isLiveChess() && !intent.getExtras()
							.getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR)) {

						start.setVisibility(View.VISIBLE);
						if (gridview != null) {
							gridview.setVisibility(View.VISIBLE);
						}
						challengesListTitle.setVisibility(View.VISIBLE);
						startNewGameTitle.setVisibility(View.VISIBLE);
					}
				}
			});
		}
	};
}