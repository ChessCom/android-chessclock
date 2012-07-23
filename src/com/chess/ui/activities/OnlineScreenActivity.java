package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListChallengeItem;
import com.chess.model.GameListCurrentItem;
import com.chess.model.GameListFinishedItem;
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
	private static final int CURRENT_GAMES_SECTION = 0;
	private static final int CHALLENGES_SECTION = 1;
	private static final int UPDATE_DELAY = 120000;

	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";

	private int currentListType;
	private int successToastMsgId;

	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private AcceptDrawUpdateListener acceptDrawUpdateListener;
	private ListUpdateListener listUpdateListener;
	private LoadItem selectedLoadItem;
	private OnlineCurrentGamesAdapter currentGamesAdapter;
	private OnlineChallengesGamesAdapter challengesGamesAdapter;
	private OnlineFinishedGamesAdapter finishedGamesAdapter;
	private SectionedAdapter sectionedAdapter;
	private List<AbstractUpdateTask<String, LoadItem>> taskPool;
	private GameListCurrentItem gameListCurrentItem;
	private GameListChallengeItem gameListChallengeItem;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_screen);

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
		List<GameListCurrentItem> currentItemList = new ArrayList<GameListCurrentItem>();
		List<GameListChallengeItem> challengesItemList = new ArrayList<GameListChallengeItem>();
		List<GameListFinishedItem> finishedItemList = new ArrayList<GameListFinishedItem>();
		sectionedAdapter = new SectionedAdapter(this);

		currentGamesAdapter = new OnlineCurrentGamesAdapter(this, currentItemList);
		challengesGamesAdapter = new OnlineChallengesGamesAdapter(this, challengesItemList);
		finishedGamesAdapter = new OnlineFinishedGamesAdapter(this, finishedItemList);

		sectionedAdapter.addSection(getString(R.string.current_games), currentGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.finished_games), finishedGamesAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		taskPool = new ArrayList<AbstractUpdateTask<String, LoadItem>>();

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
	}

	private void updateList(LoadItem listLoadItem) {
		taskPool.add(new GetStringObjTask(listUpdateListener).executeTask(listLoadItem));
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
		}

		@Override
		public void updateData(String returnedObj) {

			if (returnedObj.contains(RestHelper.R_SUCCESS)) {

				switch (currentListType) {
					case GameListItem.LIST_TYPE_CURRENT:
						currentGamesAdapter.setItemsList(ChessComApiParser.getCurrentOnlineGames(returnedObj));
						updateStartingType(GameListItem.LIST_TYPE_CHALLENGES);
						break;
					case GameListItem.LIST_TYPE_CHALLENGES:
						challengesGamesAdapter.setItemsList(ChessComApiParser.getChallengesGames(returnedObj));
						updateStartingType(GameListItem.LIST_TYPE_FINISHED);
						break;
					case GameListItem.LIST_TYPE_FINISHED:
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
			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListCurrentItem.getGameId()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_ACCEPTDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
		} else if (fragment.getTag().equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, String.valueOf(gameListChallengeItem.getGameId()));
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
			loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListCurrentItem.getGameId()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_DECLINEDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_PENDING_TAG)) {
			Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
			intent.putExtra(AppConstants.GAME_MODE, AppConstants.GAME_MODE_LIVE_OR_ECHESS);
			intent.putExtra(GameListItem.GAME_ID, gameListCurrentItem.getGameId());
			startActivity(intent);

		} else if (fragment.getTag().equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, String.valueOf(gameListChallengeItem.getGameId()));
			successToastMsgId = R.string.challengedeclined;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				preferencesEditor.putString(AppConstants.OPPONENT, gameListCurrentItem.getOpponentUsername());
				preferencesEditor.commit();

				Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
				intent.putExtra(GameListItem.GAME_ID, gameListCurrentItem.getGameId());
				intent.putExtra(GameListItem.TIMESTAMP, gameListCurrentItem.getTimestamp());
				startActivity(intent);
			} else if (pos == 1) {
				String Draw = AppConstants.OFFERDRAW;
				if (gameListCurrentItem.getIsDrawOfferPending().equals("p"))
					Draw = AppConstants.ACCEPTDRAW;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListCurrentItem.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, Draw);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

				new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			} else if (pos == 2) {

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListCurrentItem.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

				new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			}
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
		int sectionsCnt = ((SectionedAdapter)adapterView.getAdapter()).getSectionsCnt();
		int k;
		int headersCnt = 0;
		int passedItems = 0;
		for (k = 0; k < sectionsCnt; k++){
			headersCnt++;
			passedItems += sectionedAdapter.getSection(k).adapter.getCount();
			if(pos < headersCnt + passedItems){
				break;
			}
		}


		if(k == CURRENT_GAMES_SECTION){
			gameListCurrentItem = (GameListCurrentItem) adapterView.getItemAtPosition(pos);

			preferencesEditor.putString(AppConstants.OPPONENT, gameListCurrentItem.getOpponentUsername());
			preferencesEditor.commit();

			if (gameListCurrentItem.getIsDrawOfferPending().equals("p")) {
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
				intent.putExtra(GameListItem.GAME_ID, gameListCurrentItem.getGameId());
				startActivity(intent);
			}
		} else if (k == 1) {
			clickOnChallenge((GameListChallengeItem) adapterView.getItemAtPosition(pos));
		} else {
			GameListFinishedItem finishedItem = (GameListFinishedItem) adapterView.getItemAtPosition(pos);
			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), GameFinishedScreenActivity.class);
			intent.putExtra(GameListItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int sectionsCnt = ((SectionedAdapter)adapterView.getAdapter()).getSectionsCnt();
		int k;
		int headersCnt = 0;
		int passedItems = 0;
		for (k = 0; k < sectionsCnt; k++){
			headersCnt++;
			passedItems += sectionedAdapter.getSection(k).adapter.getCount();
			if(pos < headersCnt + passedItems){
				break;
			}
		}

		if (k == CURRENT_GAMES_SECTION){
			gameListCurrentItem = (GameListCurrentItem) adapterView.getItemAtPosition(pos);

			new AlertDialog.Builder(getContext())
					.setItems(new String[]{
							getString(R.string.chat),
							getString(R.string.drawoffer),
							getString(R.string.resignorabort)},
							gameListItemDialogListener)
					.create().show();

		} else if (k == CHALLENGES_SECTION) {
			clickOnChallenge((GameListChallengeItem) adapterView.getItemAtPosition(pos));
		} else {
			GameListFinishedItem finishedItem = (GameListFinishedItem) adapterView.getItemAtPosition(pos);

			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
			intent.putExtra(GameListItem.GAME_ID, finishedItem.getGameId());
			intent.putExtra(GameListItem.TIMESTAMP, finishedItem.getTimestamp());
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

	private void clickOnChallenge(GameListChallengeItem gameListChallengeItem) {
		this.gameListChallengeItem = gameListChallengeItem;
		
		String title = gameListChallengeItem.getOpponentUsername() + StaticData.SYMBOL_NEW_STR
				+ getString(R.string.win_) + StaticData.SYMBOL_SPACE + gameListChallengeItem.getOpponentWinCount()
				+ StaticData.SYMBOL_NEW_STR
				+ getString(R.string.loss_) + StaticData.SYMBOL_SPACE + gameListChallengeItem.getOpponentLossCount()
				+ StaticData.SYMBOL_NEW_STR
				+ getString(R.string.draw_) + StaticData.SYMBOL_SPACE + gameListChallengeItem.getOpponentDrawCount();

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
			for (AbstractUpdateTask<String, LoadItem> updateTask : taskPool) {
				updateTask.cancel(true);
			}
		}
	}


}
