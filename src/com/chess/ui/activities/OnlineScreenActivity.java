package com.chess.ui.activities;

import android.app.AlertDialog;
import android.content.*;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.db.DBDataManager;
import com.chess.db.tasks.LoadEchessCurrentGamesListTask;
import com.chess.db.tasks.LoadEchessFinishedGamesListTask;
import com.chess.db.tasks.SaveEchessCurrentGamesListTask;
import com.chess.db.tasks.SaveEchessFinishedGamesListTask;
import com.chess.model.*;
import com.chess.ui.adapters.*;
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

	private int successToastMsgId;

	private ChallengeInviteUpdateListener challengeInviteUpdateListener;
	private AcceptDrawUpdateListener acceptDrawUpdateListener;
	private LoadItem selectedLoadItem;
//	private OnlineCurrentGamesAdapter currentGamesAdapter;
	private OnlineCurrentGamesCursorAdapter currentGamesCursorAdapter; // test
	private OnlineChallengesGamesAdapter challengesGamesAdapter;
//	private OnlineFinishedGamesAdapter finishedGamesAdapter;
	private OnlineFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private SectionedAdapter sectionedAdapter;
	private List<AbstractUpdateTask<String, LoadItem>> taskPool;
	private GameListCurrentItem gameListCurrentItem;
	private GameListChallengeItem gameListChallengeItem;
	private VacationStatusUpdateListener vacationStatusUpdateListener;
	private boolean onVacation;
	private VacationLeaveStatusUpdateListener vacationLeaveStatusUpdateListener;
	private IntentFilter listUpdateFilter;
	private BroadcastReceiver gamesUpdateReceiver;
	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private CurrentGamesCursorUpdateListener currentGamesCursorUpdateListener;
	private FinishedGamesCursorUpdateListener finishedGamesCursorUpdateListener;
	private CursorContentObserver cursorContentObserver;
	private TextView emptyView;
	private ListView listView;
	private View loadingView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_screen);

		Button upgradeBtn = (Button) findViewById(R.id.upgradeBtn);
		upgradeBtn.setOnClickListener(this);

		moPubView = (MoPubView) findViewById(R.id.mopub_adview); // init anyway as it is declared in layout

//		if (AppUtils.isNeedToUpgrade(this)) {
//
//			if (InneractiveAdHelper.IS_SHOW_BANNER_ADS) {
//				InneractiveAdHelper.showBannerAd(upgradeBtn, (InneractiveAd) findViewById(R.id.inneractiveAd), this);
//			} else {
//				MopubHelper.showBannerAd(upgradeBtn, moPubView, this);
//			}
//		}

//		init();

		AppData.setLiveChessMode(this, false);
		// init adapters
		sectionedAdapter = new SectionedAdapter(this);

		challengesGamesAdapter = new OnlineChallengesGamesAdapter(this, null);
		currentGamesCursorAdapter = new OnlineCurrentGamesCursorAdapter(getContext(), null);
		finishedGamesCursorAdapter = new OnlineFinishedGamesCursorAdapter(getContext(), null);

		sectionedAdapter.addSection(getString(R.string.current_games), currentGamesCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.finished_games), finishedGamesCursorAdapter);

		loadingView = findViewById(R.id.loadingView);
		emptyView = (TextView) findViewById(R.id.emptyView);

		listView = (ListView) findViewById(R.id.onlineGamesList);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setAdapter(sectionedAdapter);

		findViewById(R.id.tournaments).setOnClickListener(this);
		findViewById(R.id.statsBtn).setOnClickListener(this);

		listUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);
	}

	@Override
	protected void onStart() {
		super.onStart();
		init();
	}

	private void init() {
		selectedLoadItem = new LoadItem();

		cursorContentObserver = new CursorContentObserver();

		challengeInviteUpdateListener = new ChallengeInviteUpdateListener();
		acceptDrawUpdateListener = new AcceptDrawUpdateListener();
		vacationStatusUpdateListener = new VacationStatusUpdateListener();
		vacationLeaveStatusUpdateListener = new VacationLeaveStatusUpdateListener();
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesCursorUpdateListener = new CurrentGamesCursorUpdateListener();
		finishedGamesCursorUpdateListener = new FinishedGamesCursorUpdateListener();

		showActionRefresh = true;
		showActionNewGame = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		taskPool = new ArrayList<AbstractUpdateTask<String, LoadItem>>();

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, listUpdateFilter);

		handler.postDelayed(updateListOrder, UPDATE_DELAY);

		if (AppUtils.isNetworkAvailable(this)) {
			updateVacationStatus();
			updateStartingType(GameOnlineItem.CURRENT_TYPE);
		} else {
			emptyView.setText(R.string.no_network);
			showEmptyView(true);
		}
		new LoadEchessCurrentGamesListTask(currentGamesCursorUpdateListener).executeTask();
		new LoadEchessFinishedGamesListTask(finishedGamesCursorUpdateListener).executeTask();
	}

	@Override
	protected void onPause() {
		super.onPause();

		unRegisterMyReceiver(gamesUpdateReceiver);
		handler.removeCallbacks(updateListOrder);

		cleanTaskPool();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cleanAdapters();
		taskPool = null;
	}

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
			this.currentListType = currentListType;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(String returnedObj) {

			switch (currentListType) {
				case GameOnlineItem.CURRENT_TYPE:
					List<GameListCurrentItem> listCurrentItems = ChessComApiParser.getCurrentOnlineGames(returnedObj);

					new SaveEchessCurrentGamesListTask(saveCurrentGamesListUpdateListener, listCurrentItems).executeTask();
					break;
				case GameOnlineItem.CHALLENGES_TYPE:
					challengesGamesAdapter.setItemsList(ChessComApiParser.getChallengesGames(returnedObj));
					updateStartingType(GameOnlineItem.FINISHED_TYPE);
					break;
				case GameOnlineItem.FINISHED_TYPE:
					List<GameListFinishedItem> finishedItems = ChessComApiParser.getFinishedOnlineGames(returnedObj);

					new SaveEchessFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedItems).executeTask();
					break;
				default:
					break;
			}
		}

		@Override
		public void errorHandle(String resultMessage) {
			// redundant check? we already clean the tasks pool in onPause, or...?
			// No, cleaning the task pool doesn't stop task immediately if it already reached onPOstExecute state.
			// this check prevent illegalStateExc for fragments, when they showed after onSavedInstance was called
			if (isPaused)
				return;

			if(resultMessage.equals(RestHelper.R_YOU_ARE_ON_VACATION)) {
				showToast(R.string.no_challenges_during_vacation);
			} else {
				showSinglePopupDialog(R.string.error, resultMessage);
			}

		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (resultCode == StaticData.NO_NETWORK || resultCode == StaticData.UNKNOWN_ERROR) {
				new LoadEchessCurrentGamesListTask(currentGamesCursorUpdateListener).executeTask();
				new LoadEchessFinishedGamesListTask(finishedGamesCursorUpdateListener).executeTask();
			}
		}
	}

	private class SaveCurrentGamesListUpdateListener extends  ActionBarUpdateListener<GameListCurrentItem> {
		public SaveCurrentGamesListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(GameListCurrentItem returnedObj) {
			updateStartingType(GameOnlineItem.CHALLENGES_TYPE);
		}
	}

	private class SaveFinishedGamesListUpdateListener extends  ActionBarUpdateListener<GameListFinishedItem> {
		public SaveFinishedGamesListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(GameListFinishedItem returnedObj) {
//			new LoadEchessFinishedGamesListTask(finishedGamesCursorUpdateListener).executeTask();
		}
	}

	private class CurrentGamesCursorUpdateListener extends ActionBarUpdateListener<Cursor> {
		public CurrentGamesCursorUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);
			returnedObj.registerContentObserver(cursorContentObserver);
			currentGamesCursorAdapter.changeCursor(returnedObj);

			updateStartingType(GameOnlineItem.CHALLENGES_TYPE);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.no_games);
			} else if (resultCode == StaticData.UNKNOWN_ERROR){
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private class FinishedGamesCursorUpdateListener extends ActionBarUpdateListener<Cursor> {
		public FinishedGamesCursorUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);
			returnedObj.registerContentObserver(cursorContentObserver);
			finishedGamesCursorAdapter.changeCursor(returnedObj);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.no_games);
			} else if (resultCode == StaticData.UNKNOWN_ERROR){
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private class CursorContentObserver extends ContentObserver {

		public CursorContentObserver() {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
//			showToast("cursor changed");
			Log.d("TEST", "Cursor changed, self = " + selfChange);
//			currentGamesCursorAdapter.notifyDataSetChanged();
//			finishedGamesCursorAdapter.notifyDataSetChanged();
		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;    //To change body of overridden methods use File | Settings | File Templates.
		}
	}

	private class ChallengeInviteUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			if (isPaused)
				return;

			showToast(successToastMsgId);
			updateStartingType(GameOnlineItem.CURRENT_TYPE);
		}
	}

	private class AcceptDrawUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			if (isFinishing())
				return;

			updateStartingType(GameOnlineItem.CURRENT_TYPE);
		}
	}

	private void updateVacationStatus() {
		LoadItem listLoadItem = new LoadItem();
		listLoadItem.setLoadPath(RestHelper.GET_VACATION_STATUS);
		listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

		new GetStringObjTask(vacationStatusUpdateListener).execute(listLoadItem);
	}

	private class VacationStatusUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			onVacation = returnedObj.contains("1");
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(DRAW_OFFER_PENDING_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_CHESSID, gameListCurrentItem.getGameId());
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_ACCEPTDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
		} else if (tag.equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_ACCEPTINVITEID, gameListChallengeItem.getGameId());
			successToastMsgId = R.string.challengeaccepted;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		}
		super.onPositiveBtnClick(fragment);
	}

	@Override
	public void onNeutralBtnCLick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNeutralBtnCLick(fragment);
			return;
		}

		if (tag.equals(DRAW_OFFER_PENDING_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_CHESSID, gameListCurrentItem.getGameId());
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_DECLINEDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
		}
		super.onNeutralBtnCLick(fragment);
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNegativeBtnClick(fragment);
			return;
		}

		if (tag.equals(DRAW_OFFER_PENDING_TAG)) {
			ChessBoardOnline.resetInstance();

			Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
			intent.putExtra(BaseGameItem.GAME_INFO_ITEM, gameListCurrentItem);
			startActivity(intent);

		} else if (tag.equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.ECHESS_OPEN_INVITES);
			loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_DECLINEINVITEID, gameListChallengeItem.getGameId());
			successToastMsgId = R.string.challengedeclined;

			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		} else if(tag.equals(UNABLE_TO_MOVE_TAG)){
			LoadItem listLoadItem = new LoadItem();
			listLoadItem.setLoadPath(RestHelper.VACATION_RETURN);
			listLoadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));

			new GetStringObjTask(vacationLeaveStatusUpdateListener).executeTask(listLoadItem);
		}
		super.onNegativeBtnClick(fragment);
	}

	private class VacationLeaveStatusUpdateListener extends ChessUpdateListener {

		@Override
		public void updateData(String returnedObj) {
			onVacation = false;
			updateStartingType(GameOnlineItem.CURRENT_TYPE);
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
				startActivity(intent);
			} else if (pos == 1) {
				String draw = RestHelper.V_OFFERDRAW;
				if (gameListCurrentItem.isDrawOfferPending())
					draw = RestHelper.V_ACCEPTDRAW;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, gameListCurrentItem.getGameId());
				loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

				new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			} else if (pos == 2) {

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.ECHESS_SUBMIT_ACTION);
				loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_CHESSID, gameListCurrentItem.getGameId());
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

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			gameListCurrentItem =  DBDataManager.getEchessGameListCurrentItemFromCursor(cursor);

			preferencesEditor.putString(AppConstants.OPPONENT, gameListCurrentItem.getOpponentUsername());
			preferencesEditor.commit();

			if (gameListCurrentItem.isDrawOfferPending()) {
				popupItem.setPositiveBtnId(R.string.accept);
				popupItem.setNeutralBtnId(R.string.decline);
				popupItem.setNegativeBtnId(R.string.game);

				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
				getLastPopupFragment().setButtons(3);

			} else {
				ChessBoardOnline.resetInstance();
				Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
				intent.putExtra(BaseGameItem.GAME_INFO_ITEM, gameListCurrentItem);

				startActivity(intent);
			}
		} else if (section == CHALLENGES_SECTION) {
			clickOnChallenge((GameListChallengeItem) adapterView.getItemAtPosition(pos));
		} else {

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			GameListFinishedItem finishedItem = DBDataManager.getEchessFinishedListGameFromCursor(cursor);
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
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			gameListCurrentItem =  DBDataManager.getEchessGameListCurrentItemFromCursor(cursor);

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
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			GameListFinishedItem finishedItem = DBDataManager.getEchessFinishedListGameFromCursor(cursor);

			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);
		}
		return true;
	}

	private class GamesUpdateReceiver extends BroadcastReceiver  {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateStartingType(GameOnlineItem.CURRENT_TYPE);
		}
	}

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
		if (!AppUtils.isNetworkAvailable(this)) {
			return;
		}

		selectedLoadItem.clearParams();
		String userToken = AppData.getUserToken(this);
		if (pos == GameOnlineItem.CURRENT_TYPE) {
			cleanTaskPool();
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, userToken);
			selectedLoadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ALL_USERS_GAMES);

			taskPool.add(new GetStringObjTask(new ListUpdateListener(pos)).executeTask(selectedLoadItem));
		} else if (pos == GameOnlineItem.CHALLENGES_TYPE) {
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_CHALLENGES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, userToken);

			taskPool.add(new GetStringObjTask(new ListUpdateListener(pos)).executeTask(selectedLoadItem));
		} else if (pos == GameOnlineItem.FINISHED_TYPE) {
			selectedLoadItem.setLoadPath(RestHelper.ECHESS_FINISHED_GAMES);
			selectedLoadItem.addRequestParams(RestHelper.P_ID, userToken);

			taskPool.add(new GetStringObjTask(new ListUpdateListener(pos)).executeTask(selectedLoadItem));
		}
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
		if (taskPool != null && taskPool.size() > 0) {
			for (AbstractUpdateTask<String, LoadItem> updateTask : taskPool) {
				updateTask.cancel(true);
			}
		}
	}

	private void cleanAdapters() {
		currentGamesCursorUpdateListener = null;
		finishedGamesCursorUpdateListener = null;
		challengesGamesAdapter = null;
		sectionedAdapter = null;
	}

	private void showEmptyView(boolean show){
		if (show) {
			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			loadingView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show){
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (sectionedAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);
				loadingView.setVisibility(View.VISIBLE);
			}
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

}
