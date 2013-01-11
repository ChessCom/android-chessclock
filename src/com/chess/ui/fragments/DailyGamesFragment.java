package com.chess.ui.fragments;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.*;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveEchessCurrentGamesListTask;
import com.chess.db.tasks.SaveEchessFinishedGamesListTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListFinishedItem;
import com.chess.model.GameOnlineItem;
import com.chess.ui.activities.ChatOnlineActivity;
import com.chess.ui.activities.GameFinishedScreenActivity;
import com.chess.ui.activities.GameOnlineScreenActivity;
import com.chess.ui.adapters.DailyGamesSectionedAdapter;
import com.chess.ui.adapters.OnlineChallengesGamesAdapter;
import com.chess.ui.adapters.OnlineCurrentGamesCursorAdapter;
import com.chess.ui.adapters.OnlineFinishedGamesCursorAdapter;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.utilities.AppUtils;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 7:42
 */
public class DailyGamesFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, SlidingMenu.OnOpenedListener {

	private static final int CURRENT_GAMES_SECTION = 0;
	private static final int CHALLENGES_SECTION = 1;

	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";
	private static final String UNABLE_TO_MOVE_TAG = "unable to move popup";

	private int successToastMsgId;


	private OnlineUpdateListener challengeInviteUpdateListener;
	private OnlineUpdateListener acceptDrawUpdateListener;

	private IntentFilter listUpdateFilter;
	private BroadcastReceiver gamesUpdateReceiver;
	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesCursorUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	private DailyGamesUpdateListener dailyGamesUpdateListener;
	private VacationUpdateListener vacationDeleteUpdateListener;
	private VacationUpdateListener vacationGetUpdateListener;

	private LoadItem selectedLoadItem;
	private OnlineCurrentGamesCursorAdapter currentGamesCursorAdapter;
	private OnlineChallengesGamesAdapter challengesGamesAdapter;
	private OnlineFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private DailyGamesSectionedAdapter sectionedAdapter;
	private DailyCurrentGameData gameListCurrentItem;
	private DailyChallengeData gameListChallengeItem;

	private TextView emptyView;
	private ListView listView;
	private View loadingView;
	private boolean hostUnreachable;
	private boolean onVacation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// init adapters
		sectionedAdapter = new DailyGamesSectionedAdapter(getActivity());

		challengesGamesAdapter = new OnlineChallengesGamesAdapter(getContext(), null);
		currentGamesCursorAdapter = new OnlineCurrentGamesCursorAdapter(getContext(), null);
		finishedGamesCursorAdapter = new OnlineFinishedGamesCursorAdapter(getContext(), null);

		sectionedAdapter.addSection(getString(R.string.new_my_move), currentGamesCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.new_their_move), currentGamesCursorAdapter);
//		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
//		sectionedAdapter.addSection(getString(R.string.finished_games), finishedGamesCursorAdapter);

		listUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_daily_games_frame, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivityFace().addOnOpenMenuListener(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.onlineGamesList);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setAdapter(sectionedAdapter);

	}

	@Override
	public void onStart() {
		super.onStart();
		init();

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, listUpdateFilter);

		if (AppUtils.isNetworkAvailable(getActivity()) /*&& !isRestarted*/) {
			updateVacationStatus();
			updateGamesList();
		} else {
			emptyView.setText(R.string.no_network);
			showEmptyView(true);
		}

		if (DBDataManager.haveSavedOnlineCurrentGame(getActivity())) {
			loadDbGames();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		unRegisterMyReceiver(gamesUpdateReceiver);

		releaseResources();
	}

	private void init() {
		selectedLoadItem = new LoadItem();

		challengeInviteUpdateListener = new OnlineUpdateListener(OnlineUpdateListener.INVITE);
		acceptDrawUpdateListener = new OnlineUpdateListener(OnlineUpdateListener.DRAW);
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT);
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.FINISHED);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();
		vacationGetUpdateListener = new VacationUpdateListener(VacationUpdateListener.GET);
		vacationDeleteUpdateListener = new VacationUpdateListener(VacationUpdateListener.DELETE);
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
//				preferencesEditor.putString(AppConstants.OPPONENT, gameListCurrentItem.getOpponentUsername());
//				preferencesEditor.commit();

				Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
				intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
				startActivity(intent);
			} else if (pos == 1) {
				String draw = RestHelper.V_OFFERDRAW;
				if (gameListCurrentItem.isDrawOfferPending())
					draw = RestHelper.V_ACCEPTDRAW;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
				loadItem.setRequestMethod(RestHelper.PUT);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
			} else if (pos == 2) {

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
				loadItem.setRequestMethod(RestHelper.PUT);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int section = sectionedAdapter.getCurrentSection(pos);

		if (section == CURRENT_GAMES_SECTION) {
			if (onVacation) {
				popupItem.setNegativeBtnId(R.string.end_vacation);
				showPopupDialog(R.string.unable_to_move_on_vacation, UNABLE_TO_MOVE_TAG);
				return;
			}

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			gameListCurrentItem = DBDataManager.getEchessGameListCurrentItemFromCursor(cursor);

//			preferencesEditor.putString(AppConstants.OPPONENT, gameListCurrentItem.getOpponentUsername());
//			preferencesEditor.commit();

			if (gameListCurrentItem.isDrawOfferPending()) {
				popupItem.setPositiveBtnId(R.string.accept);
				popupItem.setNeutralBtnId(R.string.decline);
				popupItem.setNegativeBtnId(R.string.game);

				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
				getLastPopupFragment().setButtons(3);

			} else {
				ChessBoardOnline.resetInstance();
				Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
				intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());

				startActivity(intent);
			}
		} else if (section == CHALLENGES_SECTION) {
			clickOnChallenge((DailyChallengeData) adapterView.getItemAtPosition(pos));
		} else {

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			GameListFinishedItem finishedItem = DBDataManager.getEchessFinishedListGameFromCursor(cursor);
//			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
//			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), GameFinishedScreenActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int section = sectionedAdapter.getCurrentSection(pos);

		if (section == CURRENT_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			gameListCurrentItem = DBDataManager.getEchessGameListCurrentItemFromCursor(cursor);

			new AlertDialog.Builder(getContext())
					.setItems(new String[]{
							getString(R.string.chat),
							getString(R.string.drawoffer),
							getString(R.string.resignorabort)},
							gameListItemDialogListener)
					.create().show();

		} else if (section == CHALLENGES_SECTION) {
			clickOnChallenge((DailyChallengeData) adapterView.getItemAtPosition(pos));
		} else {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			GameListFinishedItem finishedItem = DBDataManager.getEchessFinishedListGameFromCursor(cursor);

//			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
//			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);
		}
		return true;
	}

	@Override
	public void onOpened() {
		getActivityFace().setBadgeValueForId(R.id.menu_games, 0);
	}

	private class GamesUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateGamesList();
		}
	}

	private void clickOnChallenge(DailyChallengeData gameListChallengeItem) {
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

	private class OnlineUpdateListener extends ActionBarUpdateListener<BaseResponseItem> {
		public static final int INVITE = 3;
		public static final int DRAW = 4;
		public static final int VACATION = 5;

		private int itemCode;

		public OnlineUpdateListener(int itemCode) {
			super(getInstance(),BaseResponseItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			if (isPaused || getActivity() == null) {
				return;
			}

			switch (itemCode) {
				case INVITE:
					showToast(successToastMsgId);
					updateGamesList();
					break;
				case DRAW:
					updateGamesList();
					break;
				case VACATION:

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

			if (resultMessage.equals(RestHelper.R_YOU_ARE_ON_VACATION)) {
				showToast(R.string.no_challenges_during_vacation);
			} else {
				showSinglePopupDialog(R.string.error, resultMessage);
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (itemCode == GameOnlineItem.CURRENT_TYPE || itemCode == GameOnlineItem.CHALLENGES_TYPE
					|| itemCode == GameOnlineItem.FINISHED_TYPE) {
				if (resultCode == StaticData.NO_NETWORK || resultCode == StaticData.UNKNOWN_ERROR) {
					showToast(R.string.host_unreachable_load_local);
					hostUnreachable = true;
					loadDbGames();
				}
			}
		}
	}

	private void updateGamesList() {
		if (!AppUtils.isNetworkAvailable(getActivity())) {
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES_ALL);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(currentGamesCursorUpdateListener,
				DbHelper.getEchessCurrentListGamesParams(getContext()),
				getContentResolver()).executeTask();
		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
				DbHelper.getEchessFinishedListGamesParams(getContext()),
				getContentResolver()).executeTask();
	}

	private void updateVacationStatus() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VACATIONS);
		loadItem.setRequestMethod(RestHelper.GET);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));

		new RequestJsonTask<VacationItem>(vacationGetUpdateListener).executeTask(loadItem);
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
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_ACCEPTDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

//			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
		} else if (tag.equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ANSWER_GAME_SEEK(gameListChallengeItem.getGameId()));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			successToastMsgId = R.string.challengeaccepted;

//			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
			new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
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
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_DECLINEDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

//			new GetStringObjTask(acceptDrawUpdateListener).executeTask(loadItem);
			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
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
			intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
			startActivity(intent);

		} else if (tag.equals(CHALLENGE_ACCEPT_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_ANSWER_GAME_SEEK(gameListChallengeItem.getGameId()));
			loadItem.setRequestMethod(RestHelper.DELETE);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			successToastMsgId = R.string.challengedeclined;

//			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
			new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
		} else if (tag.equals(UNABLE_TO_MOVE_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_VACATIONS);
			loadItem.setRequestMethod(RestHelper.DELETE);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));

			new RequestJsonTask<VacationItem>(vacationDeleteUpdateListener).executeTask(loadItem);
		}
		super.onNegativeBtnClick(fragment);
	}

	private class VacationUpdateListener extends ActionBarUpdateListener<VacationItem> {

		static final int GET = 0;
		static final int DELETE = 1;
		private int listenerCode;

		public VacationUpdateListener(int listenerCode) {
			super(getInstance(), VacationItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			if (getActivity() == null) {
				return;
			}

			switch (listenerCode) {
				case GET:
					onVacation = returnedObj.getData().isOnVacation();
					break;
				case DELETE:
					onVacation = false;
					updateGamesList();
					break;
			}
		}
	}

	private class SaveCurrentGamesListUpdateListener extends ActionBarUpdateListener<DailyCurrentGameData> {
		public SaveCurrentGamesListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(DailyCurrentGameData returnedObj) {
			if (getActivity() == null) {
				return;
			}

			new LoadDataFromDbTask(currentGamesCursorUpdateListener, DbHelper.getEchessCurrentListGamesParams(getContext()),
					getContentResolver()).executeTask();
		}
	}

	private class SaveFinishedGamesListUpdateListener extends ActionBarUpdateListener<DailyFinishedGameData> {
		public SaveFinishedGamesListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(DailyFinishedGameData returnedObj) {
			if (getActivity() == null) {
				return;
			}

			new LoadDataFromDbTask(finishedGamesCursorUpdateListener, DbHelper.getEchessFinishedListGamesParams(getContext()),
					getContentResolver()).executeTask();
		}
	}

	private class GamesCursorUpdateListener extends ActionBarUpdateListener<Cursor> {
		public static final int CURRENT = 0;
		public static final int FINISHED = 1;

		private int gameType;

		public GamesCursorUpdateListener(int gameType) {
			super(getInstance());
			this.gameType = gameType;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			if (getActivity() == null) {
				return;
			}

			switch (gameType) {
				case CURRENT:
					currentGamesCursorAdapter.changeCursor(returnedObj);
					if (AppUtils.isNetworkAvailable(getContext()) && !hostUnreachable /*&& !isRestarted*/) { // TODO adjust
//						updateGamesList();
					} else {
						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
								DbHelper.getEchessFinishedListGamesParams(getContext()),
								getContentResolver()).executeTask();
					}

					break;
				case FINISHED:
					finishedGamesCursorAdapter.changeCursor(returnedObj);
					break;
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.no_games);
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
		}
	}

	private class DailyGamesUpdateListener extends ActionBarUpdateListener<DailyGamesAllItem> {

		public DailyGamesUpdateListener() {
			super(getInstance(), DailyGamesAllItem.class);
		}

		@Override
		public void updateData(DailyGamesAllItem returnedObj) {
			if (getActivity() == null) {
				return;
			}

			hostUnreachable = false;
			challengesGamesAdapter.setItemsList(returnedObj.getData().getChallenges());
			new SaveEchessCurrentGamesListTask(saveCurrentGamesListUpdateListener, returnedObj.getData().getCurrent(),
					getContentResolver()).executeTask();
			new SaveEchessFinishedGamesListTask(saveFinishedGamesListUpdateListener, returnedObj.getData().getFinished(),
					getContentResolver()).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (resultCode == StaticData.INTERNAL_ERROR) {
				emptyView.setText("Internal error occurred");
				showEmptyView(true);
			}
		}
	}

	private void releaseResources() {
		challengeInviteUpdateListener.releaseContext();
		challengeInviteUpdateListener = null;
		acceptDrawUpdateListener.releaseContext();
		acceptDrawUpdateListener = null;
		saveCurrentGamesListUpdateListener.releaseContext();
		saveCurrentGamesListUpdateListener = null;
		saveFinishedGamesListUpdateListener.releaseContext();
		saveFinishedGamesListUpdateListener = null;
		currentGamesCursorUpdateListener.releaseContext();
		currentGamesCursorUpdateListener = null;
		finishedGamesCursorUpdateListener.releaseContext();
		finishedGamesCursorUpdateListener = null;

		dailyGamesUpdateListener.releaseContext();
		dailyGamesUpdateListener = null;
		vacationDeleteUpdateListener.releaseContext();
		vacationDeleteUpdateListener = null;
		vacationGetUpdateListener.releaseContext();
		vacationGetUpdateListener = null;
	}

	private void showEmptyView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
			loadingView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show) {
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
