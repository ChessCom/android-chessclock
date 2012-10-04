package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
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
import com.chess.model.*;
import com.chess.ui.adapters.OnlineChallengesGamesAdapter;
import com.chess.ui.adapters.OnlineCurrentGamesAdapter;
import com.chess.ui.adapters.OnlineFinishedGamesAdapter;
import com.chess.ui.adapters.SectionedAdapter;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;
import com.chess.utilities.InneractiveAdHelper;
import com.chess.utilities.MopubHelper;
import com.inneractive.api.ads.InneractiveAd;
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
	private static final String UNABLE_TO_MOVE_TAG = "unable to move popup";

//	private int currentListType;
	private int successToastMsgId;

	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private AcceptDrawUpdateListener acceptDrawUpdateListener;
//	private ListUpdateListener listUpdateListener;
	private LoadItem selectedLoadItem;
	private OnlineCurrentGamesAdapter currentGamesAdapter;
	private OnlineChallengesGamesAdapter challengesGamesAdapter;
	private OnlineFinishedGamesAdapter finishedGamesAdapter;
	private SectionedAdapter sectionedAdapter;
	private List<AbstractUpdateTask<String, LoadItem>> taskPool;
	private GameListCurrentItem gameListCurrentItem;
	private GameListChallengeItem gameListChallengeItem;
	private VacationStatusUpdateListener vacationStatusUpdateListener;
	private boolean onVacation;
	private VacationLeaveStatusUpdateListener vacationLeaveStatusUpdateListener;
	private IntentFilter listUpdateFilter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_screen);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout

		if (AppUtils.isNeedToUpgrade(this)) {

			if (InneractiveAdHelper.IS_SHOW_BANNER_ADS) {
				InneractiveAdHelper.showBannerAd(upgradeBtn, (InneractiveAd) findViewById(R.id.inneractiveAd), this);
			} else {
				MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
			}
		}

		init();

		DataHolder.getInstance().setLiveChess(false);

		ListView gamesList = (ListView) findViewById(R.id.onlineGamesList);
		gamesList.setOnItemClickListener(this);
		gamesList.setOnItemLongClickListener(this);
		gamesList.setAdapter(sectionedAdapter);

		findViewById(R.id.tournaments).setOnClickListener(this);
		findViewById(R.id.statsBtn).setOnClickListener(this);

		listUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);
	}

	private void init() {
		selectedLoadItem = new LoadItem();

		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();
		acceptDrawUpdateListener = new AcceptDrawUpdateListener();
//		listUpdateListener = new ListUpdateListener(currentListType);
		vacationStatusUpdateListener = new VacationStatusUpdateListener();
		vacationLeaveStatusUpdateListener = new VacationLeaveStatusUpdateListener();

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

		showActionRefresh = true;
		showActionNewGame = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		taskPool = new ArrayList<AbstractUpdateTask<String, LoadItem>>();

		registerReceiver(gamesUpdateReceiver, listUpdateFilter);

		handler.postDelayed(updateListOrder, UPDATE_DELAY);

		updateVacationStatus();
		updateStartingType(GameOnlineItem.CURRENT_TYPE);
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(gamesUpdateReceiver);
		handler.removeCallbacks(updateListOrder);

		cleanTaskPool();
	}

//	private void updateList(LoadItem listLoadItem) {
//		taskPool.add(new GetStringObjTask(listUpdateListener).executeTask(listLoadItem));
//	}

	private Runnable updateListOrder = new Runnable() {
		@Override
		public void run() {
			updateStartingType(GameOnlineItem.CURRENT_TYPE);

			handler.removeCallbacks(this);
			handler.postDelayed(this, UPDATE_DELAY);
		}
	};

	private class ListUpdateListener extends ChessUpdateListener {
		private int currentListType;

		public ListUpdateListener(int currentListType) {
			super(getInstance());
			this.currentListType = currentListType;
		}

		@Override
		public void updateData(String returnedObj) {

//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {

			switch (currentListType) {
				case GameOnlineItem.CURRENT_TYPE:
					currentGamesAdapter.setItemsList(ChessComApiParser.getCurrentOnlineGames(returnedObj));
					updateStartingType(GameOnlineItem.CHALLENGES_TYPE);
					break;
				case GameOnlineItem.CHALLENGES_TYPE:
					challengesGamesAdapter.setItemsList(ChessComApiParser.getChallengesGames(returnedObj));
					updateStartingType(GameOnlineItem.FINISHED_TYPE);
					break;
				case GameOnlineItem.FINISHED_TYPE:
					finishedGamesAdapter.setItemsList(ChessComApiParser.getFinishedOnlineGames(returnedObj));
					break;
				default:
					break;
			}
		}

		@Override
		public void errorHandle(String resultMessage) {
			// redundant check? we already clean the tasks pool in onPause, or...?
			// no cleaning the task pool doesn't stop task immediately if it already reached onPOstExecute state.
			// this check prevent illegalStateExc for fragments, when they showed after onSavedInstance was called
			if (isPaused)
				return;

			if(resultMessage.equals(RestHelper.R_YOU_ARE_ON_VACATION)) {
				showToast(R.string.no_challenges_during_vacation);
			} else {
				showSinglePopupDialog(R.string.error, resultMessage);
			}

			switch (currentListType) { // Continue to update list
				case GameOnlineItem.CURRENT_TYPE:
					updateStartingType(GameOnlineItem.CHALLENGES_TYPE);
					break;
				case GameOnlineItem.CHALLENGES_TYPE:
					updateStartingType(GameOnlineItem.FINISHED_TYPE);
					break;
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

//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				showToast(successToastMsgId);
				updateStartingType(GameOnlineItem.CURRENT_TYPE);
//			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
//				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
//			}
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

//			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				updateStartingType(GameOnlineItem.CURRENT_TYPE);
//			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
//				showSinglePopupDialog(R.string.error, returnedObj.split("[+]")[1]);
//			}
		}
	}

	private void updateVacationStatus() {
		LoadItem listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.GET_VACATION_STATUS);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

		new GetStringObjTask(vacationStatusUpdateListener).execute(listLoadItem);
	}

	private class VacationStatusUpdateListener extends ChessUpdateListener {
		public VacationStatusUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				onVacation = returnedObj.contains("1");
//			}
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
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestampStr());

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
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestampStr());

			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(DRAW_OFFER_PENDING_TAG)) {
			ChessBoardOnline.resetInstance();
			Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
			intent.putExtra(BaseGameItem.GAME_INFO_ITEM, gameListCurrentItem);
			startActivity(intent);

		} else if (fragment.getTag().equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, String.valueOf(gameListChallengeItem.getGameId()));
			successToastMsgId = R.string.challengedeclined;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		} else if(fragment.getTag().equals(UNABLE_TO_MOVE_TAG)){
			LoadItem listLoadItem = new LoadItem();
			listLoadItem.setLoadPath(RestHelper.VACATION_RETURN);
			listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			new GetStringObjTask(vacationLeaveStatusUpdateListener).executeTask(listLoadItem);
		}
	}

	private class VacationLeaveStatusUpdateListener extends ChessUpdateListener {
		public VacationLeaveStatusUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
//			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				onVacation = false;
				updateStartingType(GameOnlineItem.CURRENT_TYPE);
//			}
		}
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				preferencesEditor.putString(AppConstants.OPPONENT, gameListCurrentItem.getOpponentUsername());
				preferencesEditor.commit();

				Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
				intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
				intent.putExtra(BaseGameItem.TIMESTAMP, gameListCurrentItem.getTimestampStr());
				startActivity(intent);
			} else if (pos == 1) {
				String draw = RestHelper.V_OFFERDRAW;
				if (gameListCurrentItem.getIsDrawOfferPending())
					draw = RestHelper.V_ACCEPTDRAW;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListCurrentItem.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestampStr());

				new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			} else if (pos == 2) {

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, String.valueOf(gameListCurrentItem.getGameId()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestampStr());

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
			Intent intent = new Intent(this, WebViewActivity.class);
			intent.putExtra(AppConstants.EXTRA_WEB_URL, playerTournamentsLink);
			intent.putExtra(AppConstants.EXTRA_TITLE, getString(R.string.tournaments));
			startActivity(intent);

		} else if (view.getId() == R.id.statsBtn) {

			String playerStatsLink = RestHelper.formStatsLink(AppData.getUserToken(this), AppData.getUserName(this));
			Intent intent = new Intent(this, WebViewActivity.class);
			intent.putExtra(AppConstants.EXTRA_WEB_URL, playerStatsLink);
			intent.putExtra(AppConstants.EXTRA_TITLE, getString(R.string.stats));
			startActivity(intent);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int section = sectionedAdapter.getCurrentSection(pos);

		if(section == CURRENT_GAMES_SECTION){
			if(onVacation){
				popupItem.setNegativeBtnId(R.string.end_vacation);
				showPopupDialog(R.string.unable_to_move_on_vacation, UNABLE_TO_MOVE_TAG);
				return;
			}

			gameListCurrentItem = (GameListCurrentItem) adapterView.getItemAtPosition(pos);

			preferencesEditor.putString(AppConstants.OPPONENT, gameListCurrentItem.getOpponentUsername());
			preferencesEditor.commit();

			if (gameListCurrentItem.getIsDrawOfferPending()) {
				DataHolder.getInstance().setAcceptDraw(true);
				popupItem.setPositiveBtnId(R.string.accept);
				popupItem.setNeutralBtnId(R.string.decline);
				popupItem.setNegativeBtnId(R.string.game);

				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
				getLastPopupFragment().setButtons(3);

			} else {
				DataHolder.getInstance().setAcceptDraw(false);
				ChessBoardOnline.resetInstance();
				Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
				intent.putExtra(BaseGameItem.GAME_INFO_ITEM, gameListCurrentItem);

                startActivity(intent);
			}
		} else if (section == CHALLENGES_SECTION) {
			clickOnChallenge((GameListChallengeItem) adapterView.getItemAtPosition(pos));
		} else {
			GameListFinishedItem finishedItem = (GameListFinishedItem) adapterView.getItemAtPosition(pos);
			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), GameFinishedScreenActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int section = sectionedAdapter.getCurrentSection(pos);

		if (section == CURRENT_GAMES_SECTION){
			gameListCurrentItem = (GameListCurrentItem) adapterView.getItemAtPosition(pos);

			new AlertDialog.Builder(getContext())
					.setItems(new String[]{
							getString(R.string.chat),
							getString(R.string.drawoffer),
							getString(R.string.resignorabort)},
							gameListItemDialogListener)
					.create().show();

		} else if (section == CHALLENGES_SECTION) {
			clickOnChallenge((GameListChallengeItem) adapterView.getItemAtPosition(pos));
		} else {
			GameListFinishedItem finishedItem = (GameListFinishedItem) adapterView.getItemAtPosition(pos);

			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			intent.putExtra(BaseGameItem.TIMESTAMP, finishedItem.getTimestampStr());
			startActivity(intent);
		}
		return true;
	}

	private BroadcastReceiver gamesUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateStartingType(GameOnlineItem.CURRENT_TYPE);
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
//		currentListType = pos;
		selectedLoadItem.clearParams();
		if (pos == GameOnlineItem.CURRENT_TYPE) {
			cleanTaskPool();
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
			selectedLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

			taskPool.add(new GetStringObjTask(new ListUpdateListener(pos)).executeTask(selectedLoadItem));


		} else if (pos == GameOnlineItem.CHALLENGES_TYPE) {
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_CHALLENGES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
			taskPool.add(new GetStringObjTask(new ListUpdateListener(pos)).executeTask(selectedLoadItem));

		} else if (pos == GameOnlineItem.FINISHED_TYPE) {
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_FINISHED_GAMES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
			taskPool.add(new GetStringObjTask(new ListUpdateListener(pos)).executeTask(selectedLoadItem));
		}

//		updateList(selectedLoadItem);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				updateStartingType(GameOnlineItem.CURRENT_TYPE);
				updateVacationStatus();
				break;
			case R.id.menu_new_game:
				startActivity(new Intent(this, OnlineNewGameActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void cleanTaskPool() {
		if (taskPool.size() > 0) {
			for (AbstractUpdateTask<String, LoadItem> updateTask : taskPool) {
				updateTask.cancel(true);
			}
		}
	}


}
