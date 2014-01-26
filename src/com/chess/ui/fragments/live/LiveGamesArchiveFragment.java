package com.chess.ui.fragments.live;

import android.database.Cursor;
import android.os.Bundle;
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
import com.chess.backend.entity.api.LiveArchiveGameData;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveLiveArchiveGamesTask;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.LiveArchiveGamesAdapter;
import com.chess.ui.adapters.LiveArchiveGamesPaginationAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

import java.util.List;

import static com.chess.backend.RestHelper.P_AVATAR_SIZE;
import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.09.13
 * Time: 7:07
 */
public class LiveGamesArchiveFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private ListView listView;
	private LiveArchiveGamesAdapter archiveGamesAdapter;
	private TextView emptyView;
	private GamesCursorUpdateListener archiveGamesCursorUpdateListener;
	private SaveArchiveGamesListUpdateListener saveArchiveGamesListUpdateListener;
	private LiveArchiveGamesPaginationAdapter paginationAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();

		pullToRefresh(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedLiveArchiveGame(getActivity(), getUsername());

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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

		getActivityFace().openFragment(GameLiveArchiveFragment.createInstance(gameId));
	}

	protected void updateData() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_LIVE_ARCHIVE);
		loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(P_AVATAR_SIZE, RestHelper.V_AV_SIZE_TINY);

		paginationAdapter.updateLoadItem(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(archiveGamesCursorUpdateListener,
				DbHelper.getLiveArchiveListGames(getUsername()),
				getContentResolver()).executeTask();
	}

	private class ArchiveGamesUpdateListener extends ChessUpdateListener<LiveArchiveGameData> {

		public ArchiveGamesUpdateListener() {
			super(LiveArchiveGameData.class);
		}

		@Override
		public void updateListData(List<LiveArchiveGameData> itemsList) {
			super.updateListData(itemsList);

//			if (itemsList != null) {
//				boolean gamesLeft = DbDataManager.checkAndDeleteNonExistLiveArchiveGames(getContext(), itemsList, getUsername());
//
//				if (gamesLeft) {
			new SaveLiveArchiveGamesTask(saveArchiveGamesListUpdateListener, itemsList,
					getContentResolver(), getUsername()).executeTask();
//				} else {
//					archiveGamesAdapter.changeCursor(null);
//					showEmptyView(true);
//				}
//			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
				return;
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred");
				return;
			}
			super.errorHandle(resultCode);
		}

	}

	private class SaveArchiveGamesListUpdateListener extends ChessUpdateListener<LiveArchiveGameData> {

		@Override
		public void updateData(LiveArchiveGameData returnedObj) {
			loadDbGames();
		}
	}

	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		public GamesCursorUpdateListener() {
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			paginationAdapter.notifyDataSetChanged();
			archiveGamesAdapter.changeCursor(returnedObj);
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

	private void init() {
		archiveGamesAdapter = new LiveArchiveGamesAdapter(getActivity(), null, getImageFetcher());
		archiveGamesCursorUpdateListener = new GamesCursorUpdateListener();
		saveArchiveGamesListUpdateListener = new SaveArchiveGamesListUpdateListener();

		paginationAdapter = new LiveArchiveGamesPaginationAdapter(getActivity(), archiveGamesAdapter,
				new ArchiveGamesUpdateListener(), null);
	}

	private void widgetsInit(View view) {
		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(paginationAdapter);
	}

}
