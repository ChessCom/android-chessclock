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
import com.chess.backend.entity.api.LiveArchiveGameItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveLiveArchiveGamesTask;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.LiveArchiveGamesAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.util.List;

import static com.chess.backend.RestHelper.P_AVATAR_SIZE;
import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.09.13
 * Time: 7:07
 */
public class LiveArchiveFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private ListView listView;
	private LiveArchiveGamesAdapter archiveGamesAdapter;
	private ArchiveGamesUpdateListener archiveGamesUpdateListener;
	private TextView emptyView;
	private GamesCursorUpdateListener archiveGamesCursorUpdateListener;
	private SaveArchiveGamesListUpdateListener saveArchiveGamesListUpdateListener;

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

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		listView.setAdapter(archiveGamesAdapter);

	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			boolean haveSavedData = DbDataManager.haveSavedLiveArchiveGame(getActivity(), getUsername());

			if (AppUtils.isNetworkAvailable(getActivity())) {
				updateData();
			}

			if (haveSavedData) {
				loadDbGames();
			}
		} else {
			loadDbGames();
		}
	}

	@Override
	public void onRefreshStarted(View view) {
		super.onRefreshStarted(view);
		if (AppUtils.isNetworkAvailable(getActivity())) {
			updateData();
		}
	}

	private void init() {
		archiveGamesAdapter = new LiveArchiveGamesAdapter(getActivity(), null, getImageFetcher());
		archiveGamesUpdateListener = new ArchiveGamesUpdateListener();
		archiveGamesCursorUpdateListener = new GamesCursorUpdateListener();
		saveArchiveGamesListUpdateListener = new SaveArchiveGamesListUpdateListener();
	}

	protected void updateData() {
		// First we check ids of games what we have. Challenges also will be stored in DB
		// when we ask server about new ids of games and challenges
		// if server have new ids we get those games with ids

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_LIVE_ARCHIVE);
		loadItem.addRequestParams(P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(P_AVATAR_SIZE, RestHelper.V_AV_SIZE_TINY);

		new RequestJsonTask<LiveArchiveGameItem>(archiveGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(archiveGamesCursorUpdateListener,
				DbHelper.getLiveArchiveListGames(getUsername()),
				getContentResolver()).executeTask();
	}

	private class ArchiveGamesUpdateListener extends ChessUpdateListener<LiveArchiveGameItem> {

		public ArchiveGamesUpdateListener() {
			super(LiveArchiveGameItem.class);
		}

		@Override
		public void updateData(LiveArchiveGameItem returnedObj) {
			super.updateData(returnedObj);

			List<LiveArchiveGameData> liveArchiveGames = returnedObj.getData().getGames();
			if (liveArchiveGames != null) {
				boolean gamesLeft = DbDataManager.checkAndDeleteNonExistLiveArchiveGames(getContext(), liveArchiveGames, getUsername());

				if (gamesLeft) {
					new SaveLiveArchiveGamesTask(saveArchiveGamesListUpdateListener, liveArchiveGames,
							getContentResolver(), getUsername()).executeTask();
				} else {
					archiveGamesAdapter.changeCursor(null);
					showEmptyView(true);
				}
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
				return;
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
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

			returnedObj.moveToFirst();

			archiveGamesAdapter.changeCursor(returnedObj);
			need2update = false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

		getActivityFace().openFragment(GameLiveArchiveFragment.createInstance(gameId));
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
			if (archiveGamesAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}
}
