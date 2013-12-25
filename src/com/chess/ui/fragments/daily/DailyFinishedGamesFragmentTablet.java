package com.chess.ui.fragments.daily;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.backend.entity.api.DailyFinishedGamesItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyFinishedGamesListTask;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.DailyFinishedGamesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 18:31
 */
public class DailyFinishedGamesFragmentTablet extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, ItemClickListenerFace {

	private static final long FRAGMENT_VISIBILITY_DELAY = 200;

	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	protected DailyFinishedGamesUpdateListener dailyFinishedGamesUpdateListener;

	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;

	private TextView emptyView;
	private ListView listView;
	private View loadingView;
	private String username;

	public DailyFinishedGamesFragmentTablet(){
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static DailyFinishedGamesFragmentTablet createInstance(String username) {
		DailyFinishedGamesFragmentTablet fragment = new DailyFinishedGamesFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
		} else {
			username = savedInstanceState.getString(USERNAME);
		}

		if (TextUtils.isEmpty(username)) {
			username = getUsername();
		}

		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null, getImageFetcher());

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
			transaction.add(R.id.optionsFragmentContainer, HomePlayFragment.createInstance(RIGHT_MENU_MODE))
					.commitAllowingStateLoss();
		}

		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_daily_finished_games_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.completed_daily);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setAdapter(finishedGamesCursorAdapter);

		if (!username.equals(getUsername())) {
			View optionsFragmentContainerView = view.findViewById(R.id.optionsFragmentContainerView);
			if (optionsFragmentContainerView != null) {
				optionsFragmentContainerView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getActivityFace().setPullToRefreshView(listView, this);
	}

	@Override
	public void onResume() {
		super.onResume();

		init();

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedAnyDailyGame(getActivity(), username); // TODO replace with pagination

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
	}

	@Override
	public void onPause() {
		super.onPause();

		handler.removeCallbacks(delayedLoadFromDb);
		releaseResources();
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
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener();

		dailyFinishedGamesUpdateListener = new DailyFinishedGamesUpdateListener();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

		getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId(), username));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
		DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

		getActivityFace().openFragment(GameDailyFinishedFragmentTablet.createInstance(finishedItem.getGameId()));
		return true;
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	protected void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_FINISHED);
		loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_USERNAME, username);
		new RequestJsonTask<DailyFinishedGamesItem>(dailyFinishedGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
				DbHelper.getDailyFinishedListGames(username),
				getContentResolver()).executeTask();
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

		public GamesCursorUpdateListener() {
			super();
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			finishedGamesCursorAdapter.changeCursor(returnedObj);
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

	private class DailyFinishedGamesUpdateListener extends ChessUpdateListener<DailyFinishedGamesItem> {

		public DailyFinishedGamesUpdateListener() {
			super(DailyFinishedGamesItem.class);
		}

		@Override
		public void updateData(DailyFinishedGamesItem returnedObj) {
			super.updateData(returnedObj);

			List<DailyFinishedGameData> finishedGameDataList = returnedObj.getData().getGames();
			if (finishedGameDataList != null) {
				boolean gamesLeft = DbDataManager.checkAndDeleteNonExistFinishedGames(getContentResolver(),
						finishedGameDataList, username);

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
		dailyFinishedGamesUpdateListener.releaseContext();
		dailyFinishedGamesUpdateListener = null;
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
			if (finishedGamesCursorAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}
}

