package com.chess.ui.fragments.daily;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.*;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyCurrentGamesListTask;
import com.chess.db.tasks.SaveDailyFinishedGamesListTask;
import com.chess.model.GameOnlineItem;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.DailyCurrentGamesCursorAdapter;
import com.chess.ui.adapters.DailyFinishedGamesCursorAdapter;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.WebViewFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.interfaces.ChallengeModeSetListener;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.ChallengeHelper;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 16:03
 */
public class DailyGamesFragmentTablet extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, ItemClickListenerFace, ChallengeModeSetListener {

	public static final int HOME_MODE = 0;
	public static final int DAILY_MODE = 1;

	private static final String END_VACATION_TAG = "end vacation popup";
	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final long FRAGMENT_VISIBILITY_DELAY = 200;

	private DailyUpdateListener challengeInviteUpdateListener;
	private DailyUpdateListener acceptDrawUpdateListener;

	private IntentFilter moveUpdateFilter;
	private GamesUpdateReceiver gamesUpdateReceiver;
	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesCursorUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	protected DailyGamesUpdateListener dailyGamesUpdateListener;

	private DailyCurrentGamesCursorAdapter currentGamesMyCursorAdapter;
	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private DailyCurrentGameData gameListCurrentItem;

	private TextView emptyView;
	private GridView gridView;
	private View loadingView;
	private List<DailyFinishedGameData> finishedGameDataList;
	private FragmentParentFace parentFace;
	private int mode;
	private boolean startDailyGame;
	private ChallengeHelper challengeHelper;
	private boolean showMiniBoards;

	public DailyGamesFragmentTablet() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, HOME_MODE);
		setArguments(bundle);
	}

	public static DailyGamesFragmentTablet createInstance(FragmentParentFace parentFace, int mode) {
		DailyGamesFragmentTablet fragment = new DailyGamesFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mode = getArguments().getInt(MODE);
		} else {
			mode = savedInstanceState.getInt(MODE);
		}

		challengeHelper = new ChallengeHelper(this, true);

		currentGamesMyCursorAdapter = new DailyCurrentGamesCursorAdapter(this, null, getImageFetcher());
		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null, getImageFetcher());

		moveUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);

		if (inLandscape()) {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.add(R.id.optionsFragmentContainer, HomePlayFragment.createInstance(RIGHT_MENU_MODE))
					.commitAllowingStateLoss();
		}
		pullToRefresh(true);

		if (!getAppData().isUserSawHelpForPullToUpdate()) {
			showToastLong(R.string.help_toast_for_daily_games);
			getAppData().setUserSawHelpForPullToUpdate(true);
		}

		showMiniBoards = getAppData().isMiniBoardsEnabled();
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

		gridView = (GridView) view.findViewById(R.id.gridView);
		gridView.setOnItemClickListener(this);
		gridView.setOnItemLongClickListener(this);
		gridView.setAdapter(currentGamesMyCursorAdapter);

		view.findViewById(R.id.completedGamesHeaderView).setOnClickListener(this);

		if (gridView != null) {
			gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView absListView, int scrollState) {
					// Pause fetcher to ensure smoother scrolling when flinging
					if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
						getImageFetcher().setPauseWork(true);
					} else {
						getImageFetcher().setPauseWork(false);
					}
				}

				@Override
				public void onScroll(AbsListView absListView, int firstVisibleItem,
									 int visibleItemCount, int totalItemCount) {
				}
			});
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivityFace().setPullToRefreshView(gridView, this);
	}

	@Override
	public void onResume() {
		super.onResume();

		init();

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, moveUpdateFilter);

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedAnyDailyGame(getActivity(), getUsername());

			if (isNetworkAvailable()) {
				updateData();
			} else if (!haveSavedData) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				handler.postDelayed(delayedLoadFromDb, FRAGMENT_VISIBILITY_DELAY);
			}
		} else {
			loadDbGames();
		}

		if (showMiniBoards != getAppData().isMiniBoardsEnabled()) {
			showMiniBoards = getAppData().isMiniBoardsEnabled();
			currentGamesMyCursorAdapter.setShowMiniBoards(showMiniBoards);
			currentGamesMyCursorAdapter.notifyDataSetInvalidated();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		unRegisterMyReceiver(gamesUpdateReceiver);

		handler.removeCallbacks(delayedLoadFromDb);
		releaseResources();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(MODE, mode);
	}

	private Runnable delayedLoadFromDb = new Runnable() {
		@Override
		public void run() {
			if (getActivity() == null) {
				return;
			}
			loadDbGames();
		}
	};

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (isNetworkAvailable()) {
			updateData();
		}
	}

	private void init() {
		challengeInviteUpdateListener = new DailyUpdateListener(DailyUpdateListener.INVITE);
		acceptDrawUpdateListener = new DailyUpdateListener(DailyUpdateListener.DRAW);
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT_MY);
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.FINISHED);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();

		boolean dailyMode = getAppData().isLastUsedDailyMode();
		if (dailyMode) {
			int timeMode = getAppData().getDefaultDailyMode();
			setDefaultLiveTimeMode(timeMode);
		} else {
			int timeMode = getAppData().getDefaultLiveMode();
			setDefaultLiveTimeMode(timeMode);
		}
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				getActivityFace().openFragment(DailyChatFragment.createInstance(gameListCurrentItem.getGameId(),
						gameListCurrentItem.getBlackAvatar())); // TODO adjust
			} else if (pos == 1) {
				// update game state before accepting draw
				LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameListCurrentItem.getGameId());
				new RequestJsonTask<DailyCurrentGameItem>(new GameStateUpdatesListener()).executeTask(loadItem);
//				String draw = RestHelper.V_OFFERDRAW;
//				if (gameListCurrentItem.isDrawOffered() > 0) {
//					draw = RestHelper.V_ACCEPTDRAW;
//				}
//
//				LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
//						draw, gameListCurrentItem.getTimestamp());
//				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
			} else if (pos == 2) {

				LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
						RestHelper.V_RESIGN, gameListCurrentItem.getTimestamp());
				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
			}
		}
	};

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.startNewGameBtn) {
			if (parentFace != null) {
				parentFace.changeFragment(new HomePlayFragment());
			} else {
				getActivityFace().showPreviousFragment();
			}
		} else if (view.getId() == R.id.timeOptionBtn) {
			View parent = (View) view.getParent();
			challengeHelper.showOnSide(parent);
		} else if (view.getId() == R.id.playNewGameBtn) {
			if (startDailyGame) {
				challengeHelper.createDailyChallenge();
			} else {
				challengeHelper.createLiveChallenge();
			}
		} else if (view.getId() == R.id.tournamentsView) {
			String tournamentsLink = RestHelper.getInstance().getTournamentsLink(getUserToken());
			WebViewFragment webViewFragment = WebViewFragment.createInstance(tournamentsLink, getString(R.string.tournaments));
			getActivityFace().openFragment(webViewFragment);
		} else if (view.getId() == R.id.completedGamesHeaderView) {
			getActivityFace().openFragment(new DailyFinishedGamesFragmentTablet());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

		if (gameListCurrentItem.isDrawOffered() > 0) {
			popupItem.setNeutralBtnId(R.string.ic_play);
			popupItem.setButtons(3);

			showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
		} else {
			ChessBoardOnline.resetInstance();
			long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

			getActivityFace().openFragment(GameDailyFragmentTablet.createInstance(gameId));
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
		gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

		new AlertDialog.Builder(getContext())
				.setItems(new String[]{
						getString(R.string.chat),
						getString(R.string.offer_draw),
						getString(R.string.resign_or_abort)},
						gameListItemDialogListener)
				.create().show();
		return true;
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

	private class GameStateUpdatesListener extends ChessLoadUpdateListener<DailyCurrentGameItem> {

		private GameStateUpdatesListener() {
			super(DailyCurrentGameItem.class);
		}

		@Override
		public void updateData(DailyCurrentGameItem returnedObj) {
			String draw = RestHelper.V_OFFERDRAW;
			if (returnedObj.getData().isDrawOffered() > 0) {
				draw = RestHelper.V_ACCEPTDRAW;
			}

			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
					draw, gameListCurrentItem.getTimestamp());
			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
		}
	}

	private class DailyUpdateListener extends ChessUpdateListener<BaseResponseItem> {
		public static final int INVITE = 3;
		public static final int DRAW = 4;

		private int itemCode;

		public DailyUpdateListener(int itemCode) {
			super(BaseResponseItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			if (isPaused || getActivity() == null) {
				return;
			}

			switch (itemCode) {
				case INVITE:
					DailyGamesFragmentTablet.this.updateData();
					break;
				case DRAW:
					DailyGamesFragmentTablet.this.updateData();
					break;
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (itemCode == GameOnlineItem.CURRENT_TYPE || itemCode == GameOnlineItem.CHALLENGES_TYPE
					|| itemCode == GameOnlineItem.FINISHED_TYPE) {
				if (resultCode == StaticData.NO_NETWORK || resultCode == StaticData.UNKNOWN_ERROR) {
					loadDbGames();
				}
			}
		}
	}

	protected void updateData() {
		// First we check ids of games what we have. Challenges also will be stored in DB
		// when we ask server about new ids of games and challenges
		// if server have new ids we get those games with ids

		LoadItem loadItem = LoadHelper.getAllGames(getUserToken());
		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(currentGamesCursorUpdateListener,
				DbHelper.getDailyCurrentListGames(getUsername()),
				getContentResolver()).executeTask();
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(DRAW_OFFER_PENDING_TAG)) {
			// update game state before accepting draw
			LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameListCurrentItem.getGameId());
			new RequestJsonTask<DailyCurrentGameItem>(new GameStateUpdatesListener()).executeTask(loadItem);
		} else if (tag.equals(END_VACATION_TAG)) {
			LoadItem loadItem = LoadHelper.deleteOnVacation(getUserToken());
			new RequestJsonTask<VacationItem>(new VacationUpdateListener()).executeTask(loadItem);
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
			ChessBoardOnline.resetInstance();
			getActivityFace().openFragment(GameDailyFragmentTablet.createInstance(gameListCurrentItem.getGameId()));
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
			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
					RestHelper.V_DECLINEDRAW, gameListCurrentItem.getTimestamp());

			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
		}
		super.onNegativeBtnClick(fragment);
	}

	private class SaveCurrentGamesListUpdateListener extends ChessUpdateListener<DailyCurrentGameData> {

		@Override
		public void updateData(DailyCurrentGameData returnedObj) {
			super.updateData(returnedObj);

			loadDbGames();
		}
	}

	private class SaveFinishedGamesListUpdateListener extends ChessUpdateListener<DailyFinishedGameData> {

		@Override
		public void updateData(DailyFinishedGameData returnedObj) {
			new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
					DbHelper.getDailyFinishedListGames(getUsername()),
					getContentResolver()).executeTask();
		}
	}

	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {
		public static final int CURRENT_MY = 0;
		public static final int FINISHED = 2;

		private int gameType;

		public GamesCursorUpdateListener(int gameType) {
			super();
			this.gameType = gameType;
		}

		@Override
		public void updateData(Cursor cursor) {
			super.updateData(cursor);

			switch (gameType) {
				case CURRENT_MY:
					cursor.moveToFirst();
					// check if we need to show new game button in dailyGamesFragment
					boolean myTurnInDailyGames = false;
					do {
						if (DbDataManager.getInt(cursor, DbScheme.V_IS_MY_TURN) > 0) {
							myTurnInDailyGames = true;

						}
					} while (cursor.moveToNext());

					// show not your turn view
					if (!myTurnInDailyGames) {
						MatrixCursor extras = new MatrixCursor(DbDataManager.PROJECTION_DAILY_CURRENT_GAMES);
						extras.addRow(new String[]{
								"-1",     // _ID,
								"",     // V_USER,
								"0",     // V_ID,
								"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",     // V_FEN,
								"1",     // V_I_PLAY_AS,
								"New Game",     // V_WHITE_USERNAME,
								"New Game",     // V_BLACK_USERNAME,
								"",     // V_WHITE_AVATAR,
								"",     // V_BLACK_AVATAR,
								"0",     // V_WHITE_PREMIUM_STATUS,
								"0",     // V_BLACK_PREMIUM_STATUS,
								"0",     // V_GAME_TYPE,
								"0",     // V_IS_MY_TURN,
								"0",     // V_TIMESTAMP,
								"0",     // V_OPPONENT_OFFERED_DRAW,
								"0",     // V_IS_OPPONENT_ONLINE,
								"0",     // V_HAS_NEW_MESSAGE,
								"99999"     // V_TIME_REMAINING
						}
						);

						Cursor[] cursors = {extras, cursor};
						Cursor extendedCursor = new MergeCursor(cursors);

						// restore position
						extendedCursor.moveToFirst();

						currentGamesMyCursorAdapter.changeCursor(extendedCursor);
					} else {
						// restore position
						cursor.moveToFirst();

						currentGamesMyCursorAdapter.changeCursor(cursor);
					}


					// add first fake game to daily games
					currentGamesMyCursorAdapter.showNewGameAtFirst(!myTurnInDailyGames);

					if (finishedGameDataList != null) {
						boolean gamesLeft = DbDataManager.checkAndDeleteNonExistFinishedGames(getContentResolver(),
								finishedGameDataList, getUsername());

						if (gamesLeft) {
							new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedGameDataList,
									getContentResolver(), getUsername()).executeTask();
						} else {
							finishedGamesCursorAdapter.changeCursor(null);
						}
					} else {
						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
								DbHelper.getDailyFinishedListGames(getUsername()),
								getContentResolver()).executeTask();
					}
					break;
				case FINISHED:
					finishedGamesCursorAdapter.changeCursor(cursor);
					need2update = false;
					break;
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				if (gameType == CURRENT_MY) {
					new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
							DbHelper.getDailyFinishedListGames(getUsername()),
							getContentResolver()).executeTask();
				} else {
					emptyView.setText(R.string.no_games);
					showEmptyView(true);
				}
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}
		}
	}

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyGamesAllItem> {

		public DailyGamesUpdateListener() {
			super(DailyGamesAllItem.class);
		}

		@Override
		public void updateData(DailyGamesAllItem returnedObj) {
			super.updateData(returnedObj);
			boolean currentGamesLeft;
			{ // current games
				final List<DailyCurrentGameData> currentGamesList = returnedObj.getData().getCurrent();
				currentGamesLeft = DbDataManager.checkAndDeleteNonExistCurrentGames(getContentResolver(), currentGamesList, getUsername());

				if (currentGamesLeft) {
					new SaveDailyCurrentGamesListTask(saveCurrentGamesListUpdateListener, currentGamesList,
							getContentResolver(), getUsername()).executeTask();
				} else {
					currentGamesMyCursorAdapter.changeCursor(null);

				}
			}

			// finished
			finishedGameDataList = returnedObj.getData().getFinished();
			if (!currentGamesLeft) { // if SaveTask will not return to LoadFinishedGamesPoint
				if (finishedGameDataList != null) {
					boolean gamesLeft = DbDataManager.checkAndDeleteNonExistFinishedGames(getContentResolver(), finishedGameDataList, getUsername());

					if (gamesLeft) {
						new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedGameDataList,
								getContentResolver(), getUsername()).executeTask();
					} else {
						finishedGamesCursorAdapter.changeCursor(null);
					}
				} else {
					new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
							DbHelper.getDailyFinishedListGames(getUsername()),
							getContentResolver()).executeTask();
				}
			}

//				boolean gamesLeft = DbDataManager.checkAndDeleteNonExistFinishedGames(getContext(), finishedGameDataList);
//
//				if (gamesLeft) {
//					new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedGameDataList,
//							getContentResolver()).executeTask();
//				} else {
//					finishedGamesCursorAdapter.changeCursor(null);
//				}
//			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode != ServerErrorCodes.INVALID_LOGIN_TOKEN_SUPPLIED) {
					showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
					return;
				}
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
			}
			super.errorHandle(resultCode);
		}
	}

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
		}
	}

	@Override
	public void setDefaultDailyTimeMode(int mode) {
		String daysString = challengeHelper.getDailyModeButtonLabel(mode);
		currentGamesMyCursorAdapter.setTimeLabel(daysString);

		startDailyGame = true;
	}

	@Override
	public void setDefaultLiveTimeMode(int mode) {
		String liveLabel = challengeHelper.getLiveModeButtonLabel(mode);
		currentGamesMyCursorAdapter.setTimeLabel(liveLabel);

		startDailyGame = false;
	}


	private void releaseResources() {
		challengeInviteUpdateListener.releaseContext();
		challengeInviteUpdateListener = null;
		acceptDrawUpdateListener.releaseContext();
		acceptDrawUpdateListener = null;
		saveCurrentGamesListUpdateListener.releaseContext();
		saveCurrentGamesListUpdateListener = null;
		currentGamesCursorUpdateListener.releaseContext();
		currentGamesCursorUpdateListener = null;

		dailyGamesUpdateListener.releaseContext();
		dailyGamesUpdateListener = null;
	}

	private void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}
			if (gridView.getAdapter().getCount() == 0) { // TODO check
				emptyView.setVisibility(View.VISIBLE);
				gridView.setVisibility(View.GONE);
			}
		} else {
			emptyView.setVisibility(View.GONE);
			gridView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
//			if (sectionedAdapter.getCount() == 0) {
			if (currentGamesMyCursorAdapter.getCount() == 0) {
				gridView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			gridView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}
}
