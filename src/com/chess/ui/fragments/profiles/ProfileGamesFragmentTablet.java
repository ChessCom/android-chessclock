package com.chess.ui.fragments.profiles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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
import com.chess.backend.entity.api.daily_games.DailyCurrentGameData;
import com.chess.backend.entity.api.daily_games.DailyGamesAllItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyCurrentGamesListTask;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.DailyCurrentGamesCursorAdapter;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.daily.DailyGamesFinishedFragmentTablet;
import com.chess.ui.fragments.daily.GameDailyFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 21:05
 */
public class ProfileGamesFragmentTablet extends ProfileBaseFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesMyCursorUpdateListener;
	protected DailyGamesUpdateListener dailyGamesUpdateListener;

	private DailyCurrentGamesCursorAdapter currentGamesMyCursorAdapter;

	private TextView emptyView;
	private GridView gridView;
	private View loadingView;
	private FragmentParentFace parentFace;

	public ProfileGamesFragmentTablet() {
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, "");
		setArguments(bundle);
	}

	public static ProfileGamesFragmentTablet createInstance(FragmentParentFace parentFace, String username) {
		ProfileGamesFragmentTablet fragment = new ProfileGamesFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init adapters
		currentGamesMyCursorAdapter = new DailyCurrentGamesCursorAdapter(this, null, getImageFetcher(), getImageFetcher());
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

		View optionsFragmentContainerView = view.findViewById(R.id.optionsFragmentContainerView);
		if (optionsFragmentContainerView != null) {
			optionsFragmentContainerView.setVisibility(View.GONE);
		}

		// adjust action bar buttons
		getActivityFace().showActionMenu(R.id.menu_message, false);
		getActivityFace().showActionMenu(R.id.menu_challenge, false);
		getActivityFace().showActionMenu(R.id.menu_notifications, true);
		getActivityFace().showActionMenu(R.id.menu_games, true);
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

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.completedGamesHeaderView) {
			if (parentFace != null) {
				parentFace.changeFragment(DailyGamesFinishedFragmentTablet.createInstance(username));
			}

		}
	}

	private void init() {
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		currentGamesMyCursorUpdateListener = new GamesCursorUpdateListener();

		dailyGamesUpdateListener = new DailyGamesUpdateListener();
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
		DailyCurrentGameData gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

		ChessBoardOnline.resetInstance();
		getActivityFace().openFragment(GameDailyFragment.createInstance(gameListCurrentItem.getGameId(), username));
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

	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			currentGamesMyCursorAdapter.changeCursor(returnedObj);
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

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyGamesAllItem> {

		public DailyGamesUpdateListener() {
			super(DailyGamesAllItem.class);
		}

		@Override
		public void updateData(DailyGamesAllItem returnedObj) {
			super.updateData(returnedObj);
			boolean currentGamesLeft;
			final List<DailyCurrentGameData> currentGamesList = returnedObj.getData().getCurrent();
			currentGamesLeft = DbDataManager.checkAndDeleteNonExistCurrentGames(getContentResolver(), currentGamesList, username);

			if (currentGamesLeft) {
				new SaveDailyCurrentGamesListTask(saveCurrentGamesListUpdateListener, currentGamesList,
						getContentResolver(), username).executeTask();
			} else {
				currentGamesMyCursorAdapter.changeCursor(null);
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
