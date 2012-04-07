package com.chess.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.model.GameListItem;
import com.chess.model.PopupItem;
import com.chess.ui.adapters.OnlineGamesAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.MopubHelper;
import com.chess.utilities.Web;
import com.mopub.mobileads.MoPubView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * LiveScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:17
 */
public class LiveScreenActivity extends CoreActivityActionBar implements View.OnClickListener, PopupDialogFace {
	private ListView gamesList;
	private OnlineGamesAdapter gamesAdapter = null;
	private TextView challengesListTitle;
	private TextView startNewGameTitle;

	private Button currentGame;
	private Button start;
	private GridView gridview;

	private int temp_pos = -1;

	public static int ONLINE_CALLBACK_CODE = 32;

	private GameListItem gameListElement;

	private AcceptDrawDialogListener acceptDrawDialogListener;
	private GameListItemClickListener gameListItemClickListener;
	private GameListItemLongClickListener gameListItemLongClickListener;
	private GameListItemDialogListener gameListItemDialogListener;
	private ChallengeDialogListener challengeDialogListener;
	private IsDirectDialogChallengeListener isDirectDialogChallengeListener;
	private IsIndirencetDialogListener isIndirencetDialogListener;
	private PopupDialogFragment popupDialogFragment;
	private PopupItem popupItem;
	private BackgroundChessDrawable backgroundChessDrawable;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_screen);
		backgroundChessDrawable =  new BackgroundChessDrawable(this);
		backgroundChessDrawable.setChangingConfigurations(Configuration.ORIENTATION_LANDSCAPE);
		backgroundChessDrawable.setChangingConfigurations(Configuration.ORIENTATION_PORTRAIT);

		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		widgetsInit();
		init();

		gridview.setAdapter(new NewGamesButtonsAdapter());
	}

	private void widgetsInit(){
		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		if (MopubHelper.isShowAds(mainApp)) {
			MopubHelper.showBannerAd(upgradeBtn, (MoPubView) findViewById(R.id.mopub_adview), mainApp);
		}

		popupItem = new PopupItem();

		popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);

		challengesListTitle = (TextView) findViewById(R.id.challengesListTitle);
		startNewGameTitle = (TextView) findViewById(R.id.startNewGameTitle);

		start = (Button) findViewById(R.id.start);
		start.setOnClickListener(this);

		gridview = (GridView) findViewById(R.id.gridview);

		gamesList = (ListView) findViewById(R.id.GamesList);
		gamesList.setOnItemClickListener(gameListItemClickListener);
		gamesList.setOnItemLongClickListener(gameListItemLongClickListener);

		currentGame = (Button) findViewById(R.id.currentGame);
		currentGame.setOnClickListener(this);

		if (lccHolder.isConnected()) {
			start.setVisibility(View.VISIBLE);
			gridview.setVisibility(View.VISIBLE);
			challengesListTitle.setVisibility(View.VISIBLE);
			startNewGameTitle.setVisibility(View.VISIBLE);
		}

	}

	private void init() {
		mainApp.setLiveChess(extras.getBoolean(AppConstants.LIVE_CHESS));

		acceptDrawDialogListener = new AcceptDrawDialogListener();
		gameListItemClickListener = new GameListItemClickListener();
		gameListItemLongClickListener = new GameListItemLongClickListener();
		gameListItemDialogListener = new GameListItemDialogListener();
		challengeDialogListener = new ChallengeDialogListener();
		isDirectDialogChallengeListener = new IsDirectDialogChallengeListener();
		isIndirencetDialogListener = new IsIndirencetDialogListener();
	}

	@Override
	public void onLeftBtnClick(PopupDialogFragment fragment) {
		lccHolder.logout();
		backToHomeActivity();
	}

	@Override
	public void onRightBtnClick(PopupDialogFragment fragment) {
		fragment.getDialog().dismiss();
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
								"http://www." + LccHolder.HOST + AppConstants.API_SUBMIT_ECHESS_ACTION_ID + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") + AppConstants.CHESSID_PARAMETER + gameListElement.getGameId() + "&command=DECLINEDRAW&timestamp=" + gameListElement.values.get(GameListItem.TIMESTAMP),
								null/*progressDialog = MyProgressDialog.show(Online.this, null, getString(R.string.loading), true)*/
						);
					}
				}
				break;
				case DialogInterface.BUTTON_NEGATIVE: {
					startActivity(new Intent(coreContext, GameLiveScreenActivity.class).
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
		mainApp.setLiveChess(extras.getBoolean(AppConstants.LIVE_CHESS));
		//}
		if (mainApp.isLiveChess() && !lccHolder.isConnected()) {
			handler.post(new Runnable() {
				@Override
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
			@Override
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

		handler.post(new Runnable() {
			@Override
			public void run() {
				disableScreenLock();
			}
		});
	}



	@Override
	public void onConfigurationChanged(Configuration newConfig) {
//		popupDialogFragment.updatePopupItem(popupItem);

		// Checks the orientation of the screen
//		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			backgroundChessDrawable.setChangingConfigurations(Configuration.ORIENTATION_LANDSCAPE);
//			backgroundChessDrawable.setChangingConfigurations(Configuration.ORIENTATION_PORTRAIT);
//		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//
//		}
//		findViewById(R.id.mainView).requestLayout();
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		super.onConfigurationChanged(newConfig);
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



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_singOut:
				popupItem.setTitleId(R.string.confirm);
				popupItem.setMessageId(R.string.signout_confirm);

				popupDialogFragment.updatePopupItem(popupItem);
				popupDialogFragment.show(getSupportFragmentManager(), "dialog");
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class GameListItemDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListItem el = mainApp.getGameListItems().get(pos);

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
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.CHESSID_PARAMETER + gameListElement.getGameId()
						+ AppConstants.COMMAND_PARAMETER + Draw + AppConstants.TIMESTAMP_PARAMETER
						+ gameListElement.values.get(GameListItem.TIMESTAMP), "GET", null, null);

				if (result.contains(AppConstants.SUCCESS)) {
					mainApp.showToast(getString(R.string.accepted));
					update(GameBaseActivity.CALLBACK_SEND_MOVE);
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
					mainApp.showDialog(coreContext, AppConstants.ERROR, result.split("[+]")[1]);
				} else {
					//mainApp.showDialog(Online.this, "Error", result);
				}
			} else if (pos == 2) {
				String result = Web.Request("http://www." + LccHolder.HOST
						+ AppConstants.API_SUBMIT_ECHESS_ACTION_ID
						+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
						+ AppConstants.CHESSID_PARAMETER + gameListElement.getGameId()
						+ AppConstants.COMMAND_RESIGN__AND_TIMESTAMP_PARAMETER
						+ gameListElement.values.get(GameListItem.TIMESTAMP), "GET", null, null);

				if (result.contains(AppConstants.SUCCESS)) {
					update(GameBaseActivity.CALLBACK_SEND_MOVE);
				} else if (result.contains(AppConstants.ERROR_PLUS)) {
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

	private class ChallengeDialogListener implements DialogInterface.OnClickListener {

		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListItem el = mainApp.getGameListItems().get(pos);

			if (pos == 0) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
				LccHolder.LOG.info("Accept challenge: " + challenge);
				lccHolder.getAndroid().runAcceptChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.getGameId());
				update(2);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
				LccHolder.LOG.info("Decline challenge: " + challenge);
				lccHolder.getAndroid().runRejectChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.getGameId());
				update(3);
			}
		}
	}

	private class IsDirectDialogChallengeListener implements DialogInterface.OnClickListener {
		@Override
		public void onClick(DialogInterface d, int pos) {
//			final GameListItem el = mainApp.getGameListItems().get(pos);
			if (pos == 0) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
				LccHolder.LOG.info(AppConstants.CANCEL_MY_CHALLENGE + challenge);
				lccHolder.getAndroid().runCancelChallengeTask(challenge);
				lccHolder.removeChallenge(gameListElement.getGameId());
				update(4);
			} else if (pos == 1) {
				final Challenge challenge = lccHolder.getChallenge(gameListElement.getGameId());
				LccHolder.LOG.info(AppConstants.JUST_KEEP_MY_CHALLENGE + challenge);
			}
		}
	}

	private class IsIndirencetDialogListener implements DialogInterface.OnClickListener {
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

	private class GameListItemClickListener implements AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
			gameListElement = mainApp.getGameListItems().get(pos);
			if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
				final String title = mainApp.isLiveChess() ?
						gameListElement.values.get(GameListItem.OPPONENT_CHESS_TITLE) :
						"Win: " + gameListElement.values.get(GameListItem.OPPONENT_WIN_COUNT)
								+ " Loss: " + gameListElement.values.get(GameListItem.OPPONENT_LOSS_COUNT)
								+ " Draw: " + gameListElement.values.get(GameListItem.OPPONENT_DRAW_COUNT);

				if (mainApp.isLiveChess()) {
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
								.setItems(new String[]{"Cancel", "Keep"}, isIndirencetDialogListener)
								.create().show();
					}
				}
			} else if (gameListElement.type == GameListItem.LIST_TYPE_CURRENT) {
				mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				if (gameListElement.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p")) {
					mainApp.acceptdraw = true;
					temp_pos = pos;
					showDialog(0);
				} else {
					mainApp.acceptdraw = false;

					Intent intent = new Intent(coreContext, GameLiveScreenActivity.class);
					intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
					intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
					startActivity(intent);
				}
			} else if (gameListElement.type == 2) {
				mainApp.getSharedDataEditor().putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				mainApp.getSharedDataEditor().commit();

				Intent intent = new Intent(coreContext, GameLiveScreenActivity.class);
				intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_VIEW_FINISHED_ECHESS);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
				startActivity(intent);
			}
		}
	}

	private class NewGamesButtonsAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		private NewGamesButtonsAdapter() {
			this.inflater = LayoutInflater.from(coreContext);
		}

		@Override
		public int getCount() {
			return StartNewGameButtonsEnum.values().length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
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
				@Override
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
			//int t = mainApp.sharedData.getInt("gamestype", 1);
			ArrayList<GameListItem> tmp = new ArrayList<GameListItem>();
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
				gamesAdapter = new OnlineGamesAdapter(LiveScreenActivity.this, R.layout.gamelistelement, mainApp.getGameListItems());
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
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www."
					+ LccHolder.HOST + AppConstants.LOGIN_HTML_ALS
					+ mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
					+ "&goto=" + GOTO)));
		} else if (view.getId() == R.id.stats) {
			// TODO hide to RestHelper
			String GOTO = "http://www." + LccHolder.HOST + AppConstants.ECHESS_MOBILE_STATS
					+ mainApp.getUserName();
			try {
				GOTO = URLEncoder.encode(GOTO, "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST
					+ AppConstants.LOGIN_HTML_ALS + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "")
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
			handler.post(new Runnable() {
				@Override
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