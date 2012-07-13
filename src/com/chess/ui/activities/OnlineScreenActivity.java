package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListItem;
import com.chess.ui.adapters.OnlineChallengesGamesAdapter;
import com.chess.ui.adapters.OnlineCurrentGamesAdapter;
import com.chess.ui.adapters.OnlineFinishedGamesAdapter;
import com.chess.ui.adapters.SectionedAdapter;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.MopubHelper;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;
import java.util.List;

/**
 * OnlineScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:12
 */
public class OnlineScreenActivity extends LiveBaseActivity implements View.OnClickListener,
		AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";

	private static final int UPDATE_DELAY = 120000;
	private int currentListType;

	private GameListItem gameListElement;
	private static final int ACCEPT_DRAW = 0;
	private static final int DECLINE_DRAW = 1;
	private int successToastMsgId;

	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private AcceptDrawUpdateListener acceptDrawUpdateListener;
	private ListUpdateListener listUpdateListener;
	private LoadItem selectedLoadItem;
	private OnlineCurrentGamesAdapter currentGamesAdapter;
	private OnlineChallengesGamesAdapter challengesGamesAdapter;
	private OnlineFinishedGamesAdapter finishedGamesAdapter;
	//	private AbstractUpdateTask getDataTask;
	private SectionedAdapter sectionedAdapter;
	//	private List<AbstractUpdateTask<String, LoadItem>> taskPool;
	private List<AsyncTask<LoadItem, Void, Integer>> taskPool;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_screen);
		Log.d("TEST", "onCreate called");

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout
		if (MopubHelper.isShowAds(this)) {
			MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
		}

		init();

		DataHolder.getInstance().setLiveChess(false);

		ListView gamesList = (ListView) findViewById(R.id.onlineGamesList);
		gamesList.setOnItemClickListener(this);
		gamesList.setOnItemLongClickListener(this);
		gamesList.setAdapter(sectionedAdapter);

		findViewById(R.id.tournaments).setOnClickListener(this);
		findViewById(R.id.stats).setOnClickListener(this);
		findViewById(R.id.start).setOnClickListener(this);
	}

	private void init() {
		selectedLoadItem = new LoadItem();

		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();
		acceptDrawUpdateListener = new AcceptDrawUpdateListener();
		listUpdateListener = new ListUpdateListener();

		// init adapters
		List<GameListItem> itemList = new ArrayList<GameListItem>();
		sectionedAdapter = new SectionedAdapter(this);

		currentGamesAdapter = new OnlineCurrentGamesAdapter(this, itemList);
		challengesGamesAdapter = new OnlineChallengesGamesAdapter(this, itemList);
		finishedGamesAdapter = new OnlineFinishedGamesAdapter(this, itemList);

		sectionedAdapter.addSection(getString(R.string.current_games), currentGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.finished_games), finishedGamesAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		taskPool = new ArrayList<AsyncTask<LoadItem, Void, Integer>>();
//		taskPool = new ArrayList<AbstractUpdateTask<String, LoadItem>>();

		registerReceiver(challengesUpdateReceiver, new IntentFilter(IntentConstants.CHALLENGES_LIST_UPDATE));

		handler.postDelayed(updateListOrder, UPDATE_DELAY);

		updateStartingType(GameListItem.LIST_TYPE_CURRENT);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(challengesUpdateReceiver);
		handler.removeCallbacks(updateListOrder);

		cleanTaskPool();
//		if(getDataTask != null)
//			getDataTask.cancel(true);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d("TEST", "onStop called");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("TEST", "onDestroy called");
	}

	private void updateList(LoadItem listLoadItem) {
//		getDataTask = new GetStringObjTask(new ListUpdateListener()).executeTask(listLoadItem);
		Log.d("TEST", "_____________");
		Log.d("TEST", "updating list");
//		taskPool.add(new GetStringObjTask(new ListUpdateListener()).executeTask(listLoadItem));
		taskPool.add(new GetStringObjTask(listUpdateListener).executeTask(listLoadItem));
//		new GetStringObjTask(new ListUpdateListener()).executeTask(listLoadItem);
	}

	private Runnable updateListOrder = new Runnable() {
		@Override
		public void run() {
			updateStartingType(GameListItem.LIST_TYPE_CURRENT);

			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_DELAY);
		}
	};

	private class ListUpdateListener extends ChessUpdateListener {
		public ListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateListData(List<String> itemsList) {
			super.updateListData(itemsList);
			Log.d("TEST", "updateListData = " + itemsList);
		}

		@Override
		public void updateData(String returnedObj) {
			Log.d("TEST", "updateData = " + returnedObj);

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {

				switch (currentListType) {
					case GameListItem.LIST_TYPE_CURRENT:
						Log.d("TEST", "LIST_TYPE_CURRENT");
						currentGamesAdapter.setItemsList(ChessComApiParser.getCurrentOnlineGames(returnedObj));
						updateStartingType(GameListItem.LIST_TYPE_CHALLENGES);
						break;
					case GameListItem.LIST_TYPE_CHALLENGES:
						Log.d("TEST", "LIST_TYPE_CHALLENGES");
						challengesGamesAdapter.setItemsList(ChessComApiParser.getChallengesGames(returnedObj));
						updateStartingType(GameListItem.LIST_TYPE_FINISHED);
						break;
					case GameListItem.LIST_TYPE_FINISHED:
						Log.d("TEST", "LIST_TYPE_FINISHED");
						finishedGamesAdapter.setItemsList(ChessComApiParser.getFinishedOnlineGames(returnedObj));
						break;
					default:
						break;
				}
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				String status = returnedObj.split("[+]")[1];
				if (!isPaused)
					showSinglePopupDialog(R.string.error, status);

				if (status.equals(RestHelper.R_PLEASE_LOGIN_AGAIN))
					AppUtils.stopNotificationsUpdate(getContext());
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			Log.d("TEST", "errorHandle called");
		}
	}

	private class ChallengeInviteUpdateListener extends ChessUpdateListener {
		public ChallengeInviteUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (isPaused)
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				showToast(successToastMsgId);
				updateStartingType(GameListItem.LIST_TYPE_CURRENT);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	private class AcceptDrawUpdateListener extends ChessUpdateListener {
		public AcceptDrawUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (isFinishing())
				return;

			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				updateStartingType(GameListItem.LIST_TYPE_CURRENT);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
			}
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_PENDING_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_ACCEPTDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
		} else if (fragment.getTag().equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, String.valueOf(gameListElement.getGameId()));
			successToastMsgId = R.string.challengeaccepted;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onNeutralBtnCLick(DialogFragment fragment) {
		super.onNeutralBtnCLick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_PENDING_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_DECLINEDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_PENDING_TAG)) {
			Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
			intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
			intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
			startActivity(intent);

		} else if (fragment.getTag().equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, String.valueOf(gameListElement.getGameId()));
			successToastMsgId = R.string.challengedeclined;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				preferencesEditor.putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
				preferencesEditor.commit();

				Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
				intent.putExtra(GameListItem.TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));
				startActivity(intent);
			} else if (pos == 1) {
				String Draw = AppConstants.OFFERDRAW;
				if (gameListElement.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p"))
					Draw = AppConstants.ACCEPTDRAW;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, Draw);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

				new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			} else if (pos == 2) {

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListElement.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));

				new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			}
		}
	};


	private DialogInterface.OnClickListener challengeDialogListener = new DialogInterface.OnClickListener() {
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

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
	};

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.upgradeBtn) {
			startActivity(AppData.getMembershipAndroidIntent(this));
		} else if (view.getId() == R.id.tournaments) {

			String playerTournamentsLink = RestHelper.formTournamentsLink(AppData.getUserToken(this));
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playerTournamentsLink));
			startActivity(intent);

		} else if (view.getId() == R.id.stats) {

			String playerStatsLink = RestHelper.formStatsLink(AppData.getUserToken(this));
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(playerStatsLink));
			startActivity(intent);
		} else if (view.getId() == R.id.start) {
			startActivity(new Intent(this, OnlineNewGameActivity.class));
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
		gameListElement = (GameListItem) adapterView.getItemAtPosition(pos);

		if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
			clickOnChallenge();
		} else if (gameListElement.type == GameListItem.LIST_TYPE_CURRENT) {
			preferencesEditor.putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
			preferencesEditor.commit();

			if (gameListElement.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p")) {
				DataHolder.getInstance().setAcceptDraw(true);
				popupItem.setPositiveBtnId(R.string.accept);
				popupItem.setNeutralBtnId(R.string.decline);
				popupItem.setNegativeBtnId(R.string.game);
				popupDialogFragment.setButtons(3);
				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);

			} else {
				DataHolder.getInstance().setAcceptDraw(false);

				Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
				intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
				intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
				startActivity(intent);
			}
		} else if (gameListElement.type == GameListItem.LIST_TYPE_FINISHED) {
			preferencesEditor.putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), GameFinishedScreenActivity.class);
			intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		gameListElement = (GameListItem) adapterView.getItemAtPosition(pos);

		if (gameListElement.type == GameListItem.LIST_TYPE_CHALLENGES) {
			clickOnChallenge();
		} else if (gameListElement.type == GameListItem.LIST_TYPE_CURRENT) {
			new AlertDialog.Builder(getContext())
					.setItems(new String[]{
							getString(R.string.chat),
							getString(R.string.drawoffer),
							getString(R.string.resignorabort)},
							gameListItemDialogListener)
					.create().show();
		} else if (gameListElement.type == GameListItem.LIST_TYPE_FINISHED) {
			preferencesEditor.putString(AppConstants.OPPONENT, gameListElement.values.get(GameListItem.OPPONENT_USERNAME));
			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
			intent.putExtra(GameListItem.GAME_ID, gameListElement.getGameId());
			intent.putExtra(GameListItem.TIMESTAMP, gameListElement.values.get(GameListItem.TIMESTAMP));
			startActivity(intent);
		}
		return true;
	}

	private BroadcastReceiver challengesUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateList(selectedLoadItem);
		}
	};

	private void clickOnChallenge() {
		String title = getString(R.string.win_) + StaticData.SYMBOL_SPACE
				+ gameListElement.values.get(GameListItem.OPPONENT_WIN_COUNT)
				+ getString(R.string.loss_) + StaticData.SYMBOL_SPACE
				+ gameListElement.values.get(GameListItem.OPPONENT_LOSS_COUNT)
				+ getString(R.string.draw_) + StaticData.SYMBOL_SPACE
				+ gameListElement.values.get(GameListItem.OPPONENT_DRAW_COUNT);

		popupItem.setPositiveBtnId(R.string.accept);
		popupItem.setNegativeBtnId(R.string.decline);
		showPopupDialog(title, CHALLENGE_ACCEPT_TAG);
	}

	private void updateStartingType(int pos) {
		currentListType = pos;
		selectedLoadItem.clearParams();
		if (pos == GameListItem.LIST_TYPE_CURRENT) {
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
			selectedLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

		} else if (pos == GameListItem.LIST_TYPE_CHALLENGES) {
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_CHALLENGES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));

		} else if (pos == GameListItem.LIST_TYPE_FINISHED) {
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_FINISHED_GAMES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		}

		updateList(selectedLoadItem);

	}

	private void cleanTaskPool() {
		if (taskPool.size() > 0) {
			for (AsyncTask<LoadItem, Void, Integer> updateTask : taskPool) {
				updateTask.cancel(true);
//				updateTask = null;
				Log.d("TEST", "Tasks cleaned");
			}
		}
//		taskPool = null;
//		listUpdateListener = null;
	}


}
