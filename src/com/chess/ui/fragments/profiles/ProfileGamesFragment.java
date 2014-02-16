package com.chess.ui.fragments.profiles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameData;
import com.chess.backend.entity.api.daily_games.DailyFinishedGameData;
import com.chess.backend.entity.api.daily_games.DailyGamesAllItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyCurrentGamesListTask;
import com.chess.db.tasks.SaveDailyFinishedGamesListTask;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.DailyCurrentGamesCursorAdapter;
import com.chess.ui.adapters.DailyFinishedGamesCursorAdapter;
import com.chess.ui.engine.ChessBoardDaily;
import com.chess.ui.fragments.daily.GameDailyFinishedFragment;
import com.chess.ui.fragments.daily.GameDailyFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.08.13
 * Time: 10:33
 */
public class ProfileGamesFragment extends ProfileBaseFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	private static final int CURRENT_GAMES_SECTION = 0;
	private static final int FINISHED_GAMES_SECTION = 1;

	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesMyCursorUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	protected DailyGamesUpdateListener dailyGamesUpdateListener;

	private DailyCurrentGamesCursorAdapter currentGamesMyCursorAdapter;
	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private CustomSectionedAdapter sectionedAdapter;

	private TextView emptyView;
	private ListView listView;
	private List<DailyFinishedGameData> finishedGameDataList;

	public ProfileGamesFragment() {
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static ProfileGamesFragment createInstance(String username) {
		ProfileGamesFragment fragment = new ProfileGamesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.comp_archive_header,
				new int[]{CURRENT_GAMES_SECTION});

		currentGamesMyCursorAdapter = new DailyCurrentGamesCursorAdapter(this, null, getImageFetcher(), getImageFetcher());
		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null, getImageFetcher());

		sectionedAdapter.addSection(getString(R.string.my_move), currentGamesMyCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.completed), finishedGamesCursorAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.home_daily_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(sectionedAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();

		init();

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedAnyDailyGame(getActivity(), username);

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
			loadDbGames();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		releaseResources();
	}

	private void init() {
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesMyCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT_MY);
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.FINISHED);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		int section = sectionedAdapter.getCurrentSection(position);

		if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId(), username));
		} else {

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			DailyCurrentGameData gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

			ChessBoardDaily.resetInstance();
			getActivityFace().openFragment(GameDailyFragment.createInstance(gameListCurrentItem.getGameId(), username));
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	protected void updateData() {
		LoadItem loadItem = LoadHelper.getAllGames(getUserToken());
		loadItem.addRequestParams(RestHelper.P_USERNAME, username);
		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(currentGamesMyCursorUpdateListener,
				DbHelper.getDailyCurrentListGames(username),
				getContentResolver()).executeTask();
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
					DbHelper.getDailyFinishedListGames(username),
					getContentResolver()).executeTask();
		}
	}

	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {
		public static final int CURRENT_MY = 0;
		public static final int FINISHED = 2;

		private int gameType;

		public GamesCursorUpdateListener(int gameType) {
			this.gameType = gameType;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (gameType) {
				case CURRENT_MY:
					currentGamesMyCursorAdapter.changeCursor(returnedObj);
					if (finishedGameDataList != null) {
						boolean gamesLeft = DbDataManager.checkAndDeleteNonExistFinishedGames(getContentResolver(), finishedGameDataList, username);

						if (gamesLeft) {
							new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedGameDataList,
									getContentResolver(), username).executeTask();
						} else {
							finishedGamesCursorAdapter.changeCursor(null);
						}
					} else {
						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
								DbHelper.getDailyFinishedListGames(username),
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
							DbHelper.getDailyFinishedListGames(username),
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
				currentGamesLeft = DbDataManager.checkAndDeleteNonExistCurrentGames(getContentResolver(), currentGamesList, username);

				if (currentGamesLeft) {
					new SaveDailyCurrentGamesListTask(saveCurrentGamesListUpdateListener, currentGamesList,
							getContentResolver(), username).executeTask();
				} else {
					currentGamesMyCursorAdapter.changeCursor(null);
				}
			}

			// finished
			finishedGameDataList = returnedObj.getData().getFinished();
			if (!currentGamesLeft) { // if SaveTask will not return to LoadFinishedGamesPoint
				if (finishedGameDataList != null) {
					boolean gamesLeft = DbDataManager.checkAndDeleteNonExistFinishedGames(getContentResolver(), finishedGameDataList, username);

					if (gamesLeft) {
						new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedGameDataList,
								getContentResolver(), username).executeTask();
					} else {
						finishedGamesCursorAdapter.changeCursor(null);
					}
				} else {
					new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
							DbHelper.getDailyFinishedListGames(username),
							getContentResolver()).executeTask();
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

	private void releaseResources() {
		saveCurrentGamesListUpdateListener.releaseContext();
		saveCurrentGamesListUpdateListener = null;
		currentGamesMyCursorUpdateListener.releaseContext();
		currentGamesMyCursorUpdateListener = null;

		dailyGamesUpdateListener.releaseContext();
		dailyGamesUpdateListener = null;
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