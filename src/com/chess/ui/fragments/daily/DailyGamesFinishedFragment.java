package com.chess.ui.fragments.daily;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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
import com.chess.backend.entity.api.daily_games.DailyFinishedGameData;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyFinishedGamesListTask;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.DailyFinishedGamesCursorAdapter;
import com.chess.ui.adapters.DailyFinishedGamesPaginationAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.14
 * Time: 16:33
 */
public class DailyGamesFinishedFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		ItemClickListenerFace {

	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	protected DailyFinishedGamesUpdateListener dailyFinishedGamesUpdateListener;

	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;

	private TextView emptyView;
	protected ListView listView;
	private View loadingView;
	protected String username;
	protected DailyFinishedGamesPaginationAdapter paginationAdapter;

	public DailyGamesFinishedFragment(){
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static DailyGamesFinishedFragment createInstance(String username) {
		DailyGamesFinishedFragment fragment = new DailyGamesFinishedFragment();
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

		init();

		pullToRefresh(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.finished_games);

		emptyView = (TextView) view.findViewById(R.id.emptyView);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedAnyDailyGame(getActivity(), username); // TODO replace with pagination

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
			listView.setAdapter(paginationAdapter);
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (isNetworkAvailable()) {
			updateData();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

		getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId(), username));
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

		paginationAdapter.updateLoadItem(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
				DbHelper.getDailyFinishedListGames(username),
				getContentResolver()).executeTask();
	}

	private class DailyFinishedGamesUpdateListener extends ChessUpdateListener<DailyFinishedGameData> {

		public DailyFinishedGamesUpdateListener() {
			super(DailyFinishedGameData.class);
			useList = true;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);

			if (show) {
				showEmptyView(false);
			}
		}

		@Override
		public void updateListData(List<DailyFinishedGameData> itemsList) {
			super.updateListData(itemsList);

			new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, itemsList,
					getContentResolver(), username).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred");
			}
		}
	}

	private class SaveFinishedGamesListUpdateListener extends ChessUpdateListener<DailyFinishedGameData> {

		@Override
		public void updateData(DailyFinishedGameData returnedObj) {
			loadDbGames();
		}
	}

	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			showEmptyView(false);

			paginationAdapter.notifyDataSetChanged();
			finishedGamesCursorAdapter.changeCursor(returnedObj);
			paginationAdapter.notifyDataSetChanged();
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

	private void showEmptyView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void init() {
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();

		dailyFinishedGamesUpdateListener = new DailyFinishedGamesUpdateListener();

		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null, getImageFetcher());

		paginationAdapter = new DailyFinishedGamesPaginationAdapter(getActivity(), finishedGamesCursorAdapter,
				dailyFinishedGamesUpdateListener, null);
	}

	protected void widgetsInit(View view) {
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(paginationAdapter);
	}
}


