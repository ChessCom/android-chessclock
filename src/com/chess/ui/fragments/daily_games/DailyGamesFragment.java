package com.chess.ui.fragments.daily_games;

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
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.*;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyCurrentGamesListTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListFinishedItem;
import com.chess.model.GameOnlineItem;
import com.chess.ui.activities.old.ChatOnlineActivity;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.DailyChallengesGamesAdapter;
import com.chess.ui.adapters.DailyCurrentGamesMyCursorAdapter;
import com.chess.ui.adapters.DailyCurrentGamesTheirCursorAdapter;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.NewGamesFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 7:42
 */
public class DailyGamesFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, SlidingMenu.OnOpenedListener, ItemClickListenerFace {

	private static final int CURRENT_GAMES_SECTION = 1;
	private static final int CHALLENGES_SECTION = 0;

	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";
	private static final String UNABLE_TO_MOVE_TAG = "unable to move popup";

	private int successToastMsgId;


	private OnlineUpdateListener challengeInviteUpdateListener;
	private OnlineUpdateListener acceptDrawUpdateListener;

	private IntentFilter listUpdateFilter;
	private BroadcastReceiver gamesUpdateReceiver;
	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	//	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesTheirCursorUpdateListener;
	private GamesCursorUpdateListener currentGamesMyCursorUpdateListener;
	//	private GamesCursorUpdateListener finishedGamesCursorUpdateListener; // doesn't load on this screen
	private DailyGamesUpdateListener dailyGamesUpdateListener;
	private VacationUpdateListener vacationDeleteUpdateListener;
	private VacationUpdateListener vacationGetUpdateListener;

	//	private LoadItem selectedLoadItem;
	private DailyCurrentGamesMyCursorAdapter currentGamesMyCursorAdapter;
	private DailyCurrentGamesTheirCursorAdapter currentGamesTheirCursorAdapter;
	private DailyChallengesGamesAdapter challengesGamesAdapter;
	//	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private CustomSectionedAdapter sectionedAdapter;
	private DailyCurrentGameData gameListCurrentItem;
	private DailyChallengeItem.Data gameListChallengeItem;

	private TextView emptyView;
	private ListView listView;
	private View loadingView;
	private boolean hostUnreachable;
	private boolean onVacation;
	private boolean need2update = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header);

		challengesGamesAdapter = new DailyChallengesGamesAdapter(this, null);
		currentGamesTheirCursorAdapter = new DailyCurrentGamesTheirCursorAdapter(getContext(), null);
		currentGamesMyCursorAdapter = new DailyCurrentGamesMyCursorAdapter(getContext(), null);
//		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null);

		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.new_my_move), currentGamesMyCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.new_their_move), currentGamesTheirCursorAdapter);
//		sectionedAdapter.addSection(getString(R.string.finished_games), finishedGamesCursorAdapter);  // TODO restore, will be opened in separate frame

		listUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_daily_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setAdapter(sectionedAdapter);

		view.findViewById(R.id.startNewGameBtn).setOnClickListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();
		init();

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, listUpdateFilter);

		if (need2update) {
			boolean haveSavedData = DBDataManager.haveSavedOnlineCurrentGame(getActivity());

			if (AppUtils.isNetworkAvailable(getActivity())) {
				updateVacationStatus();
				updateData();
			} else if (!haveSavedData) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				loadDbGames();
			}
		}

	}

	@Override
	public void onStop() {
		super.onStop();

		unRegisterMyReceiver(gamesUpdateReceiver);

		releaseResources();
	}

	private void init() {
//		selectedLoadItem = new LoadItem();

		challengeInviteUpdateListener = new OnlineUpdateListener(OnlineUpdateListener.INVITE);
		acceptDrawUpdateListener = new OnlineUpdateListener(OnlineUpdateListener.DRAW);
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
//		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesMyCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT_MY);
		currentGamesTheirCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT_THEIR);
//		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.FINISHED);

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
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.startNewGameBtn) {
			getActivityFace().changeRightFragment(NewGamesFragment.newInstance(NewGamesFragment.RIGHT_MENU_MODE));

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					getActivityFace().toggleMenu(SlidingMenu.RIGHT);
				}
			}, 50);

		} else if (view.getId() == R.id.acceptBtn) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			gameListChallengeItem = challengesGamesAdapter.getItem(position);
			acceptChallenge();
		} else if (view.getId() == R.id.cancelBtn) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			gameListChallengeItem = challengesGamesAdapter.getItem(position);
			declineChallenge();
		}
	}

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
				getActivityFace().openFragment(GameDailyFragment.newInstance(gameListCurrentItem.getGameId()));
//				Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
//				intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
//
//				startActivity(intent);
			}
		} else if (section == CHALLENGES_SECTION) {
			clickOnChallenge((DailyChallengeItem.Data) adapterView.getItemAtPosition(pos));
		} else {

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
				getActivityFace().openFragment(GameDailyFragment.newInstance(gameListCurrentItem.getGameId()));
//				Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class);
//				intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
//
//				startActivity(intent);
			}

/*			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			GameListFinishedItem finishedItem = DBDataManager.getEchessFinishedListGameFromCursor(cursor);
//			preferencesEditor.putString(AppConstants.OPPONENT, finishedItem.getOpponentUsername());
//			preferencesEditor.commit();

			Intent intent = new Intent(getContext(), GameFinishedScreenActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);*/
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
							getString(R.string.offer_draw),
							getString(R.string.resign_or_abort)},
							gameListItemDialogListener)
					.create().show();

		} else if (section == CHALLENGES_SECTION) {
			clickOnChallenge((DailyChallengeItem.Data) adapterView.getItemAtPosition(pos));
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

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class GamesUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateData();
		}
	}

	private void clickOnChallenge(DailyChallengeItem.Data gameListChallengeItem) {
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

	private class OnlineUpdateListener extends ChessUpdateListener<BaseResponseItem> {
		public static final int INVITE = 3;
		public static final int DRAW = 4;
		public static final int VACATION = 5;

		private int itemCode;

		public OnlineUpdateListener(int itemCode) {
			super(BaseResponseItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
//			Log.d("TEST", "VacationUpdateListener showProgress");

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
					DailyGamesFragment.this.updateData();
					break;
				case DRAW:
					DailyGamesFragment.this.updateData();
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

	private void updateData() {
		if (!AppUtils.isNetworkAvailable(getActivity())) {
			return;
		}

		// First we check ids of games what we have. Challenges also will be stored in DB
		// when we ask server about new ids of games and challenges
		// if server have new ids we get those games with ids

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES_ALL);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
//		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_GAME_ID);
		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(currentGamesMyCursorUpdateListener,
				DbHelper.getDailyCurrentMyListGamesParams(getContext()),
				getContentResolver()).executeTask();
		new LoadDataFromDbTask(currentGamesTheirCursorUpdateListener,
				DbHelper.getDailyCurrentTheirListGamesParams(getContext()),
				getContentResolver()).executeTask();

//		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
//				DbHelper.getEchessFinishedListGamesParams(getContext()),
//				getContentResolver()).executeTask();
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
			acceptChallenge();
		}
		super.onPositiveBtnClick(fragment);
	}

	private void acceptChallenge() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ANSWER_GAME_SEEK(gameListChallengeItem.getGameId()));
		loadItem.setRequestMethod(RestHelper.PUT);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		successToastMsgId = R.string.challenge_accepted;

//			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
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

//			Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class); // TODO adjust for fragment
//			intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
//			startActivity(intent);

		} else if (tag.equals(CHALLENGE_ACCEPT_TAG)) {
			declineChallenge();
		} else if (tag.equals(UNABLE_TO_MOVE_TAG)) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_VACATIONS);
			loadItem.setRequestMethod(RestHelper.DELETE);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));

			new RequestJsonTask<VacationItem>(vacationDeleteUpdateListener).executeTask(loadItem);
		}
		super.onNegativeBtnClick(fragment);
	}

	private void declineChallenge() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_ANSWER_GAME_SEEK(gameListChallengeItem.getGameId()));
		loadItem.setRequestMethod(RestHelper.DELETE);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		successToastMsgId = R.string.challenge_declined;

//			new GetStringObjTask(challengeInviteUpdateListener).executeTask(loadItem);
		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	private class VacationUpdateListener extends ChessUpdateListener<VacationItem> {

		static final int GET = 0;
		static final int DELETE = 1;
		private int listenerCode;

		public VacationUpdateListener(int listenerCode) {
			super(VacationItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
			super.updateData(returnedObj);

			switch (listenerCode) {
				case GET:
					onVacation = returnedObj.getData().isOnVacation();
					break;
				case DELETE:
					onVacation = false;
					DailyGamesFragment.this.updateData();
					break;
			}
		}
	}

	private class SaveCurrentGamesListUpdateListener extends ChessUpdateListener<DailyCurrentGameData> {
		public SaveCurrentGamesListUpdateListener() {
			super();
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(DailyCurrentGameData returnedObj) {
			super.updateData(returnedObj);

			loadDbGames();
		}
	}

	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {
		public static final int CURRENT_MY = 0;
		public static final int CURRENT_THEIR = 1;

		private int gameType;

		public GamesCursorUpdateListener(int gameType) {
			super();
			this.gameType = gameType;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (gameType) {
				case CURRENT_MY:
					currentGamesMyCursorAdapter.changeCursor(returnedObj);
//					if (AppUtils.isNetworkAvailable(getContext()) && !hostUnreachable /*&& !isRestarted*/) { // TODO adjust
////						updateData();
//					} else {
//						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
//								DbHelper.getEchessFinishedListGamesParams(getContext()),
//								getContentResolver()).executeTask();
//					}

					break;
				case CURRENT_THEIR:
					currentGamesTheirCursorAdapter.changeCursor(returnedObj);
//					if (AppUtils.isNetworkAvailable(getContext()) && !hostUnreachable /*&& !isRestarted*/) { // TODO adjust
////						updateData();
//					} else {
//						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
//								DbHelper.getEchessFinishedListGamesParams(getContext()),
//								getContentResolver()).executeTask();
//					}

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

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyGamesAllItem> {

		public DailyGamesUpdateListener() {
			super(DailyGamesAllItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(DailyGamesAllItem returnedObj) {
			super.updateData(returnedObj);

			hostUnreachable = false;
			challengesGamesAdapter.setItemsList(returnedObj.getData().getChallenges());

			new SaveDailyCurrentGamesListTask(saveCurrentGamesListUpdateListener, returnedObj.getData().getCurrent(),
					getContentResolver()).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				showToast(ServerErrorCode.getUserFriendlyMessage(getActivity(), serverCode));
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
//				showEmptyView(true);
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
		currentGamesMyCursorUpdateListener.releaseContext();
		currentGamesMyCursorUpdateListener = null;

		dailyGamesUpdateListener.releaseContext();
		dailyGamesUpdateListener = null;
		vacationDeleteUpdateListener.releaseContext();
		vacationDeleteUpdateListener = null;
		vacationGetUpdateListener.releaseContext();
		vacationGetUpdateListener = null;
	}

	private void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
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

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}
}
