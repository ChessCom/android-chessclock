package com.chess.ui.fragments.daily;

import android.app.AlertDialog;
import android.content.*;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
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
import com.chess.backend.entity.api.daily_games.*;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyFinishedGamesListTask;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.DailyCurrentGamesCursorAdapter;
import com.chess.ui.adapters.DailyFinishedGamesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.RightPlayFragment;
import com.chess.ui.interfaces.ChallengeModeSetListener;
import com.chess.ui.interfaces.GameFaceHelper;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.chess_boards.ChessBoardDailyView;
import com.chess.utilities.ChallengeHelper;

import java.util.List;

import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 7:42
 */
public class DailyGamesFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, ItemClickListenerFace, ChallengeModeSetListener {

	public static final int HOME_MODE = 0;

	private static final int CURRENT_GAMES_SECTION = 0;
	private static final int FINISHED_GAMES_SECTION = 1;

	private static final String END_VACATION_TAG = "end vacation popup";
	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final String IMAGE_CACHE_DIR = "boards";

	private DailyUpdateListener acceptDrawUpdateListener;

	private IntentFilter moveUpdateFilter;
	private GamesUpdateReceiver gamesUpdateReceiver;
	private DailyFinishedGamesUpdateListener dailyFinishedGamesUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	protected DailyGamesUpdateListener dailyGamesUpdateListener;
	private SmartImageFetcher boardImgFetcher;

	private DailyCurrentGamesCursorAdapter currentGamesMyCursorAdapter;
	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private CustomSectionedAdapter sectionedAdapter;
	private DailyCurrentGameData gameListCurrentItem;

	private TextView emptyView;
	private ListView listView;
	private int mode;
	private GameFaceHelper gameFaceHelper;
	private ViewGroup newGameHeaderView;

	private ChallengeHelper challengeHelper;
	private Button startNewGameBtn;
	private boolean showMiniBoards;

	public DailyGamesFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, HOME_MODE);
		setArguments(bundle);
	}

	public static DailyGamesFragment createInstance(int mode) {
		DailyGamesFragment fragment = new DailyGamesFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
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

		challengeHelper = new ChallengeHelper(this);
		gameFaceHelper = new GameFaceHelper(getActivity());

		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.comp_archive_header,
				new int[]{CURRENT_GAMES_SECTION});

		{// initialize boardsFetcher
			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);
			cacheParams.setMemCacheSizePercent(0.15f); // Set memory cache to 25% of app memory

			boardImgFetcher = new SmartImageFetcher(getActivity());
			boardImgFetcher.setLoadingImage(R.drawable.board_green_default);
			boardImgFetcher.addImageCache(getFragmentManager(), cacheParams);
		}
		currentGamesMyCursorAdapter = new DailyCurrentGamesCursorAdapter(this, null, getImageFetcher(), boardImgFetcher);
		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null, getImageFetcher());

		sectionedAdapter.addSection(getString(R.string.my_move), currentGamesMyCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.completed), finishedGamesCursorAdapter);

		moveUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);

		pullToRefresh(true);

		if (!getAppData().isUserSawHelpForPullToUpdate()) {
			showToastLong(R.string.help_toast_for_daily_games);
			getAppData().setUserSawHelpForPullToUpdate(true);
		}

		showMiniBoards = getAppData().isMiniBoardsEnabled();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_daily_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);

		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					boardImgFetcher.setPauseWork(true);
				} else {
					boardImgFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
								 int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

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

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, moveUpdateFilter);

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
		if (view.getId() == R.id.timeSelectBtn) {
			View parent = (View) view.getParent();
			challengeHelper.show((View) parent.getParent());
		} else if (view.getId() == R.id.startNewGameBtn) {
			getActivityFace().changeRightFragment(RightPlayFragment.createInstance(RIGHT_MENU_MODE));
			getActivityFace().toggleRightMenu();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
		boolean headerAdded = listView.getHeaderViewsCount() > 0; // used to check if header added
		int offset = headerAdded ? -1 : 0;

		int section = sectionedAdapter.getCurrentSection(position + offset);

		if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId()));
		} else {

			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

			if (gameListCurrentItem.isDrawOffered() > 0) {
				// draw_offered - 0 = no draw offered, 1 = white offered draw, 2 = black offered draw
				boolean iPlayWhite = gameListCurrentItem.getIPlayAs() == RestHelper.P_WHITE;
				boolean whiteOfferedDraw = gameListCurrentItem.isDrawOffered() == RestHelper.P_WHITE;
				if (iPlayWhite && whiteOfferedDraw || !iPlayWhite && !whiteOfferedDraw) {
					long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

					getActivityFace().openFragment(GameDailyFragment.createInstance(gameId));
				} else {
					popupItem.setNeutralBtnId(R.string.ic_play);
					popupItem.setButtons(3);

					showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
				}
			} else {
				long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

				getActivityFace().openFragment(GameDailyFragment.createInstance(gameId));
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		boolean headerAdded = listView.getHeaderViewsCount() > 0; // used to check if header added
		int offset = headerAdded ? -1 : 0;

		int section = sectionedAdapter.getCurrentSection(pos + offset);

		if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId()));
		} else {
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
		}
		return true;
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class GamesUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			challengeHelper.dismiss();

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
			if (gameListCurrentItem.isDrawOffered() > 0) {
				// draw_offered - 0 = no draw offered, 1 = white offered draw, 2 = black offered draw
				boolean iPlayWhite = gameListCurrentItem.getIPlayAs() == RestHelper.P_WHITE;
				boolean whiteOfferedDraw = gameListCurrentItem.isDrawOffered() == RestHelper.P_WHITE;
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
		public static final int DRAW = 4;

		private int itemCode;

		public DailyUpdateListener(int itemCode) {
			super(BaseResponseItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			switch (itemCode) {
				case DRAW:
					DailyGamesFragment.this.updateData();
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
		if (TextUtils.isEmpty(getUserToken())) {
			return;
		}
		// get Current games first
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
			getActivityFace().openFragment(GameDailyFragment.createInstance(gameListCurrentItem.getGameId()));
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

	private class VacationUpdateListener extends ChessLoadUpdateListener<VacationItem> {

		public VacationUpdateListener() {
			super(VacationItem.class);
		}

		@Override
		public void updateData(VacationItem returnedObj) {
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

		@Override
		public void updateData(Cursor cursor) {
			super.updateData(cursor);

			finishedGamesCursorAdapter.changeCursor(cursor);
			need2update = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.no_games);
				showEmptyView(true);
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}
		}
	}

	private void updateUiData(Cursor cursor) {
		// check if we need to show new game button in dailyGamesFragment
		boolean myTurnInDailyGames = false;
		do {
			if (DbDataManager.getInt(cursor, DbScheme.V_IS_MY_TURN) > 0) {
				myTurnInDailyGames = true;
				break;
			}
		} while (cursor.moveToNext());
		// restore position
		cursor.moveToFirst();

		if (myTurnInDailyGames) {
			startNewGameBtn.setVisibility(View.GONE);
		} else {
			startNewGameBtn.setVisibility(View.VISIBLE);
		}

		currentGamesMyCursorAdapter.changeCursor(cursor);

		getActivityFace().updateNotificationsBadges();
		loadFromDbFinishedGames();
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

			getFinishedGames();
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
				showToast("Internal error occurred");
			}
			super.errorHandle(resultCode);
		}
	}

	protected void getFinishedGames() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_FINISHED);
		loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_USERNAME, getUsername());

		new RequestJsonTask<DailyFinishedGamesItem>(dailyFinishedGamesUpdateListener).executeTask(loadItem);
	}

	private class DailyFinishedGamesUpdateListener extends ChessUpdateListener<DailyFinishedGamesItem> {

		public DailyFinishedGamesUpdateListener() {
			super(DailyFinishedGamesItem.class);
		}

		@Override
		public void updateData(DailyFinishedGamesItem returnedObj) {
			super.updateData(returnedObj);

			List<DailyFinishedGameData> finishedGameDataList = returnedObj.getData().getGames();
			if (finishedGameDataList != null) {
				boolean gamesLeft = DbDataManager.checkAndDeleteNonExistFinishedGames(getContentResolver(), finishedGameDataList, getUsername());

				if (gamesLeft) {
					new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedGameDataList,
							getContentResolver(), getUsername()).executeTask();
				} else {
					finishedGamesCursorAdapter.changeCursor(null);
				}
			} else {
				loadFromDbFinishedGames();
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

	private void loadFromDbFinishedGames() {
		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
				DbHelper.getDailyFinishedListGames(getUsername()),
				getContentResolver()).executeTask();
	}


	@Override
	public void setDefaultDailyTimeMode(int mode) {
	}

	@Override
	public void setDefaultLiveTimeMode(int mode) {
	}

	private View createBoardView(ChessBoardDailyView boardView) {
		boardView.setGameFace(gameFaceHelper);
		int coordinateColorLight = getResources().getColor(R.color.transparent);
		int coordinateColorDark = getResources().getColor(R.color.transparent);
		boardView.setCustomCoordinatesColors(new int[]{coordinateColorLight, coordinateColorDark});

		return boardView;
	}

	private void init() {
		acceptDrawUpdateListener = new DailyUpdateListener(DailyUpdateListener.DRAW);
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		dailyFinishedGamesUpdateListener = new DailyFinishedGamesUpdateListener();
		dailyGamesUpdateListener = new DailyGamesUpdateListener();
	}

	private void widgetsInit(View view) {
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		startNewGameBtn = (Button) view.findViewById(R.id.startNewGameBtn);
		startNewGameBtn.setOnClickListener(this);

		Resources resources = getResources();
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		newGameHeaderView = (ViewGroup) inflater.inflate(R.layout.daily_games_header_view, null, false);

		int squareSize;
		{ // new game overlay setup
			View startOverlayView = newGameHeaderView.findViewById(R.id.startOverlayView);

			// let's make it to match board properties
			// it should be 2 squares inset from top of border and 4 squares tall + 1 squares from sides
			squareSize = resources.getDisplayMetrics().widthPixels / 8; // one square size
//			int borderOffset = resources.getDimensionPixelSize(R.dimen.invite_overlay_top_offset);
			int borderOffset = 0;
			// now we add few pixel to compensate shadow addition
//			int shadowOffset = resources.getDimensionPixelSize(R.dimen.overlay_shadow_offset);
			int shadowOffset = 0;
			borderOffset += shadowOffset;

			int overlayHeight = squareSize * 3 + borderOffset + shadowOffset;
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					overlayHeight);
			int topMargin = (int) (squareSize * 0.5f + borderOffset - shadowOffset * 2);

			params.setMargins(squareSize - borderOffset, topMargin, squareSize - borderOffset, 0);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardView);

			startOverlayView.setLayoutParams(params);
			startOverlayView.setVisibility(View.VISIBLE);
		}

		newGameHeaderView.findViewById(R.id.gamePlayBtn).setOnClickListener(this);

		{ // adjust boardView
			ChessBoardDailyView boardView = (ChessBoardDailyView) newGameHeaderView.findViewById(R.id.boardView);

			int height = squareSize * 4;

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
			boardView.setLayoutParams(params);
			createBoardView(boardView);
		}

		{ // Time mode adjustments


			// set texts to buttons
			boolean dailyMode = getAppData().isLastUsedDailyMode();
			if (dailyMode) {
				int timeMode = getAppData().getDefaultDailyMode();
				setDefaultLiveTimeMode(timeMode);
			} else {
				int timeMode = getAppData().getDefaultLiveMode();
				setDefaultLiveTimeMode(timeMode);
			}
		}

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setAdapter(sectionedAdapter);

		initUpgradeAndAdWidgets(view);

		if (!isNeedToUpgrade() || !showAdsForNewMembers) {// we need to bind to bottom if there is no ad banner
			((RelativeLayout.LayoutParams) startNewGameBtn.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		}
	}

	private void showEmptyView(boolean show) {
		if (show) {
			if (listView.getAdapter().getCount() == 0) { // TODO check
				emptyView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			}
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

}
