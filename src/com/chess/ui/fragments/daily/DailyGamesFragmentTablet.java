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
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.VacationItem;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameData;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameItem;
import com.chess.backend.entity.api.daily_games.DailyCurrentGamesItem;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.DailyCurrentGamesCursorAdapter;
import com.chess.ui.engine.ChessBoardDaily;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.RightPlayFragment;
import com.chess.ui.fragments.WebViewFragment;
import com.chess.ui.interfaces.ChallengeModeSetListener;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.ChallengeHelper;

import java.util.List;

import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

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
	private static final String IMAGE_CACHE_DIR = "boards";

	private DailyUpdateListener acceptDrawUpdateListener;

	private IntentFilter moveUpdateFilter;
	private GamesUpdateReceiver gamesUpdateReceiver;
	protected DailyGamesUpdateListener dailyGamesUpdateListener;

	private DailyCurrentGamesCursorAdapter currentGamesMyCursorAdapter;
	private DailyCurrentGameData gameListCurrentItem;

	private TextView emptyView;
	private GridView gridView;
	private FragmentParentFace parentFace;
	private int mode;
	private boolean startDailyGame;
	private ChallengeHelper challengeHelper;
	private boolean showMiniBoards;
	private SmartImageFetcher boardImgFetcher;

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

		init();

		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_daily_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivityFace().setPullToRefreshView(gridView, this);
	}

	@Override
	public void onResume() {
		super.onResume();

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
				loadDbGames();
			}
		} else {
			updateData();
			// load games to quickly update state after move was made
			loadDbGames();
		}

		if (showMiniBoards != getAppData().isMiniBoardsEnabled()) {
			showMiniBoards = getAppData().isMiniBoardsEnabled();
			currentGamesMyCursorAdapter.setShowMiniBoards(showMiniBoards);
			currentGamesMyCursorAdapter.notifyDataSetInvalidated();
		}

		boardImgFetcher.setExitTasksEarly(false);
	}

	@Override
	public void onPause() {
		super.onPause();

		unRegisterMyReceiver(gamesUpdateReceiver);

		boardImgFetcher.setPauseWork(false);
		boardImgFetcher.setExitTasksEarly(true);
		boardImgFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		boardImgFetcher.closeCache();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(MODE, mode);
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (isNetworkAvailable()) {
			updateData();
		} else {
			loadDbGames();
		}
	}

	@Override
	protected void afterLogin() {
		super.afterLogin();

		if (isNetworkAvailable()) {
			updateData();
		} else {
			loadDbGames();
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
				parentFace.changeFragment(new RightPlayFragment());
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
			getActivityFace().openFragment(new DailyGamesFinishedFragmentTablet());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

		if (gameListCurrentItem.isDrawOffered() > 0) {
			// draw_offered - 0 = no draw offered, 1 = white offered draw, 2 = black offered draw
			boolean iPlayWhite = gameListCurrentItem.getIPlayAs() == RestHelper.P_WHITE;
			boolean whiteOfferedDraw = gameListCurrentItem.isDrawOffered() == RestHelper.P_WHITE;
			if (iPlayWhite && whiteOfferedDraw || !iPlayWhite && !whiteOfferedDraw) {
				ChessBoardDaily.resetInstance();
				long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

				getActivityFace().openFragment(GameDailyFragmentTablet.createInstance(gameId));
			} else {
				popupItem.setNeutralBtnId(R.string.ic_play);
				popupItem.setButtons(3);

				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
			}
		} else {
			ChessBoardDaily.resetInstance();
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
						gameListItemDialogListener
				)
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
				// draw_offered - 0 = no draw offered, 1 = white offered draw, 2 = black offered draw
				boolean iPlayWhite = returnedObj.getData().getIPlayAs() == RestHelper.P_WHITE;
				boolean whiteOfferedDraw = returnedObj.getData().isDrawOffered() == RestHelper.P_WHITE;
				if ((!iPlayWhite || !whiteOfferedDraw) && (iPlayWhite || whiteOfferedDraw)) {
					draw = RestHelper.V_ACCEPTDRAW;
				}
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
			if (resultCode == StaticData.NO_NETWORK || resultCode == StaticData.UNKNOWN_ERROR) {
				loadDbGames();
			}
		}
	}

	protected void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_CURRENT);
		loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<DailyCurrentGamesItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getDailyCurrentListGames(getUsername()));

		if (cursor != null && cursor.moveToFirst()) {
			updateUiData(cursor);
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
			ChessBoardDaily.resetInstance();
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

	private void updateUiData(Cursor cursor) {
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
							"0",     // V_IS_OPPONENT_ON_VACATION,
							"0",     // V_IS_TOURNAMENT_GAME,
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

		getActivityFace().updateNotificationsBadges();
		need2update = false;
	}

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyCurrentGamesItem> {

		public DailyGamesUpdateListener() {
			super(DailyCurrentGamesItem.class);
		}

		@Override
		public void updateData(DailyCurrentGamesItem returnedObj) {
			super.updateData(returnedObj);
			boolean currentGamesLeft;
			{ // current games
				final List<DailyCurrentGameData> currentGamesList = returnedObj.getData();
				currentGamesLeft = DbDataManager.checkAndDeleteNonExistCurrentGames(getContentResolver(), currentGamesList, getUsername());

				if (currentGamesLeft) {
					for (DailyCurrentGameData currentItem : currentGamesList) {
						DbDataManager.saveDailyGame(getContentResolver(), currentItem, getUsername());
					}
					loadDbGames();
				} else {
					currentGamesMyCursorAdapter.changeCursor(null);
				}
			}
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

	private void init() {
		challengeHelper = new ChallengeHelper(this, true);

		{// initialize boardsFetcher
			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.15f); // Set memory cache to 25% of app memory

			boardImgFetcher = new SmartImageFetcher(getActivity());
			boardImgFetcher.setLoadingImage(R.drawable.board_green_default);
			boardImgFetcher.addImageCache(getFragmentManager(), cacheParams);
		}

		currentGamesMyCursorAdapter = new DailyCurrentGamesCursorAdapter(this, null, getImageFetcher(), boardImgFetcher);

		moveUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);

		if (inLandscape()) {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.add(R.id.optionsFragmentContainer, RightPlayFragment.createInstance(RIGHT_MENU_MODE))
					.commitAllowingStateLoss();
		}

		if (!getAppData().isUserSawHelpForPullToUpdate()) {
			showToastLong(R.string.help_toast_for_daily_games);
			getAppData().setUserSawHelpForPullToUpdate(true);
		}

		showMiniBoards = getAppData().isMiniBoardsEnabled();

		acceptDrawUpdateListener = new DailyUpdateListener(DailyUpdateListener.DRAW);

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

	private void widgetsInit(View view) {
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		gridView = (GridView) view.findViewById(R.id.gridView);
		gridView.setOnItemClickListener(this);
		gridView.setOnItemLongClickListener(this);
		gridView.setAdapter(currentGamesMyCursorAdapter);

		View completedGamesHeaderView = view.findViewById(R.id.completedGamesHeaderView);
		completedGamesHeaderView.setOnClickListener(this);

		if (gridView != null) {
			gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
				@Override
				public void onScrollStateChanged(AbsListView absListView, int scrollState) {
					// Pause fetcher to ensure smoother scrolling when flinging
					if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
						boardImgFetcher.setPauseWork(true);
						getImageFetcher().setPauseWork(true);
					} else {
						boardImgFetcher.setPauseWork(false);
						getImageFetcher().setPauseWork(false);
					}
				}

				@Override
				public void onScroll(AbsListView absListView, int firstVisibleItem,
									 int visibleItemCount, int totalItemCount) {
				}
			});
		}

		initUpgradeAndAdWidgets(view);

		if (!isNeedToUpgrade() || !showAdsForNewMembers) {// we need to bind to bottom if there is no ad banner
			((RelativeLayout.LayoutParams) completedGamesHeaderView.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
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
}
