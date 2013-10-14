package com.chess.ui.fragments.daily;

import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.DailyCurrentGameData;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.backend.entity.api.DailyGamesAllItem;
import com.chess.db.DbScheme;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyCurrentGamesListTask;
import com.chess.db.tasks.SaveDailyFinishedGamesListTask;
import com.chess.model.GameOnlineItem;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.DailyCurrentGamesCursorAdapter;
import com.chess.ui.adapters.DailyFinishedGamesCursorAdapter;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.home.HomeTabsFragment;
import com.chess.ui.interfaces.FragmentTabsFace;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 7:42
 */
public class DailyGamesFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, ItemClickListenerFace {

	public static final int HOME_MODE = 0;
	public static final int DAILY_MODE = 1;

	private static final int CURRENT_GAMES_SECTION = 0;
	private static final int FINISHED_GAMES_SECTION = 1;

	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final long FRAGMENT_VISIBILITY_DELAY = 200;

	private DailyUpdateListener challengeInviteUpdateListener;
	private DailyUpdateListener acceptDrawUpdateListener;

	private IntentFilter listUpdateFilter;
	private BroadcastReceiver gamesUpdateReceiver;
	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesCursorUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	protected DailyGamesUpdateListener dailyGamesUpdateListener;

	private DailyCurrentGamesCursorAdapter currentGamesMyCursorAdapter;
	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private CustomSectionedAdapter sectionedAdapter;
	private DailyCurrentGameData gameListCurrentItem;

	private TextView emptyView;
	private ListView listView;
	private View loadingView;
	private Button startNewGameBtn;
	private List<DailyFinishedGameData> finishedGameDataList;
	private FragmentTabsFace parentFace;
	private int mode;

	public DailyGamesFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, HOME_MODE);
		setArguments(bundle);
	}

	public static DailyGamesFragment createInstance(FragmentTabsFace parentFace, int mode) {
		DailyGamesFragment fragment = new DailyGamesFragment();
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

		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_comp_archive_header,
				new int[]{CURRENT_GAMES_SECTION});

		currentGamesMyCursorAdapter = new DailyCurrentGamesCursorAdapter(getContext(), null, getImageFetcher());
		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null, getImageFetcher());

		sectionedAdapter.addSection(getString(R.string.new_my_move), currentGamesMyCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.completed), finishedGamesCursorAdapter);

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

		startNewGameBtn = (Button) view.findViewById(R.id.startNewGameBtn);
		startNewGameBtn.setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		init();

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, listUpdateFilter);

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedDailyGame(getActivity(), getUsername());

			if (AppUtils.isNetworkAvailable(getActivity())) {
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

	private void init() {
		challengeInviteUpdateListener = new DailyUpdateListener(DailyUpdateListener.INVITE);
		acceptDrawUpdateListener = new DailyUpdateListener(DailyUpdateListener.DRAW);
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT_MY);
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.FINISHED);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				getActivityFace().openFragment(DailyChatFragment.createInstance(gameListCurrentItem.getGameId(),
						gameListCurrentItem.getBlackAvatar())); // TODO adjust
			} else if (pos == 1) {
				String draw = RestHelper.V_OFFERDRAW;
				if (gameListCurrentItem.isDrawOffered() > 0) {
					draw = RestHelper.V_ACCEPTDRAW;
				}

				LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
						draw, gameListCurrentItem.getTimestamp());
				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
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
				if (mode == HOME_MODE) {
					parentFace.changeInternalFragment(HomeTabsFragment.NEW_GAME);
				} else {
					parentFace.changeInternalFragment(DailyHomeTabsFragment.NEW_GAME);
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
		int section = sectionedAdapter.getCurrentSection(position);

		if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId()));
		} else {

			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

			if (gameListCurrentItem.isDrawOffered() > 0) {
				popupItem.setNeutralBtnId(R.string.ic_play);
				popupItem.setButtons(3);

				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
			} else {
				ChessBoardOnline.resetInstance();
				long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

				getActivityFace().openFragment(GameDailyFragment.createInstance(gameId));
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int section = sectionedAdapter.getCurrentSection(pos);

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
							gameListItemDialogListener)
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
			updateData();
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
					DailyGamesFragment.this.updateData();
					break;
				case DRAW:
					DailyGamesFragment.this.updateData();
					break;
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (itemCode == GameOnlineItem.CURRENT_TYPE || itemCode == GameOnlineItem.CHALLENGES_TYPE
					|| itemCode == GameOnlineItem.FINISHED_TYPE) {
				if (resultCode == StaticData.NO_NETWORK || resultCode == StaticData.UNKNOWN_ERROR) {
					showToast(R.string.host_unreachable_load_local);
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
		logTest("load from DB");
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
			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
					RestHelper.V_ACCEPTDRAW, gameListCurrentItem.getTimestamp());

			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
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

	private class SaveCurrentGamesListUpdateListener extends ChessUpdateListener<DailyCurrentGameData> {

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

	private class SaveFinishedGamesListUpdateListener extends ChessUpdateListener<DailyFinishedGameData> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

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
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (gameType) {
				case CURRENT_MY:
					returnedObj.moveToFirst();
					// check if we need to show new game button in dailyGamesFragment
					boolean myTurnInDailyGames = false;
					do {
						if (DbDataManager.getInt(returnedObj, DbScheme.V_IS_MY_TURN) > 0) {
							myTurnInDailyGames = true;

						}
					} while (returnedObj.moveToNext());
					startNewGameBtn.setVisibility(myTurnInDailyGames ? View.GONE : View.VISIBLE);

					// restore position
					returnedObj.moveToFirst();

					currentGamesMyCursorAdapter.changeCursor(returnedObj);
//					if (AppUtils.isNetworkAvailable(getContext()) && !hostUnreachable /*&& !isRestarted*/) { // TODO adjust
//						updateData();
//					} else {
//						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
//								DbHelper.getDailyFinishedListGames(getContext()),
//								getContentResolver()).executeTask();
//					}

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
					finishedGamesCursorAdapter.changeCursor(returnedObj);
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
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
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

//			{ // finished
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
				showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
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
			if (listView.getAdapter().getCount() == 0) { // TODO check
				emptyView.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			}
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
