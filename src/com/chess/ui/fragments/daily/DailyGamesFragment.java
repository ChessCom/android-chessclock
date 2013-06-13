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
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.*;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveDailyCurrentGamesListTask;
import com.chess.db.tasks.SaveDailyFinishedGamesListTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListFinishedItem;
import com.chess.model.GameOnlineItem;
import com.chess.ui.activities.old.ChatOnlineActivity;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.DailyCurrentGamesCursorAdapter;
import com.chess.ui.adapters.DailyFinishedGamesCursorAdapter;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.CommonLogicFragment;
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

	private static final int CURRENT_GAMES_SECTION = 0;
	private static final int FINISHED_GAMES_SECTION = 1;

	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";

	private OnlineUpdateListener challengeInviteUpdateListener;
	private OnlineUpdateListener acceptDrawUpdateListener;

	private IntentFilter listUpdateFilter;
	private BroadcastReceiver gamesUpdateReceiver;
	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesMyCursorUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	private DailyGamesUpdateListener dailyGamesUpdateListener;

	private DailyCurrentGamesCursorAdapter currentGamesMyCursorAdapter;
	private DailyFinishedGamesCursorAdapter finishedGamesCursorAdapter;
	private CustomSectionedAdapter sectionedAdapter;
	private DailyCurrentGameData gameListCurrentItem;

	private TextView emptyView;
	private ListView listView;
	private View loadingView;
	private boolean need2update = true;
	private Button startNewGameBtn;
	private List<DailyFinishedGameData> finishedGameDataList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_comp_archive_header,
				new int[]{CURRENT_GAMES_SECTION});

		currentGamesMyCursorAdapter = new DailyCurrentGamesCursorAdapter(getContext(), null);
		finishedGamesCursorAdapter = new DailyFinishedGamesCursorAdapter(getContext(), null);

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
	public void onStart() {
		super.onStart();
		init();

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, listUpdateFilter);

		if (need2update) {
			boolean haveSavedData = DBDataManager.haveSavedOnlineCurrentGame(getActivity());

			if (AppUtils.isNetworkAvailable(getActivity())) {
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
	public void onStop() {
		super.onStop();

		unRegisterMyReceiver(gamesUpdateReceiver);

		releaseResources();
	}

	private void init() {
		challengeInviteUpdateListener = new OnlineUpdateListener(OnlineUpdateListener.INVITE);
		acceptDrawUpdateListener = new OnlineUpdateListener(OnlineUpdateListener.DRAW);
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesMyCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT_MY);
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.FINISHED);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
				intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
				startActivity(intent);
			} else if (pos == 1) {
				String draw = RestHelper.V_OFFERDRAW;
				if (gameListCurrentItem.isDrawOfferPending())
					draw = RestHelper.V_ACCEPTDRAW;

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
				loadItem.setRequestMethod(RestHelper.PUT);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, draw);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
			} else if (pos == 2) {

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
				loadItem.setRequestMethod(RestHelper.PUT);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
				loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_RESIGN);
				loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
			}
		}
	};

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.startNewGameBtn) {

			getActivityFace().openFragment(new DailyGameSetupFragment());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		int section = sectionedAdapter.getCurrentSection(position);

		if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			GameListFinishedItem finishedItem = DBDataManager.getEchessFinishedListGameFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.newInstance(finishedItem.getGameId()));
		} else {

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			gameListCurrentItem = DBDataManager.getEchessGameListCurrentItemFromCursor(cursor);

			if (gameListCurrentItem.isDrawOfferPending()) {
				popupItem.setPositiveBtnId(R.string.accept);
				popupItem.setNeutralBtnId(R.string.decline);
				popupItem.setNegativeBtnId(R.string.game);

				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
				getLastPopupFragment().setButtons(3);

			} else {
				ChessBoardOnline.resetInstance();
				getActivityFace().openFragment(GameDailyFragment.newInstance(gameListCurrentItem.getGameId()));
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int section = sectionedAdapter.getCurrentSection(pos);

		if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			GameListFinishedItem finishedItem = DBDataManager.getEchessFinishedListGameFromCursor(cursor);

			Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);
		} else {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			gameListCurrentItem = DBDataManager.getEchessGameListCurrentItemFromCursor(cursor);

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

	private class OnlineUpdateListener extends ChessUpdateListener<BaseResponseItem> {
		public static final int INVITE = 3;
		public static final int DRAW = 4;

		private int itemCode;

		public OnlineUpdateListener(int itemCode) {
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
		public void errorHandle(String resultMessage) {
			if (isPaused)
				return;

			if (resultMessage.equals(RestHelper.R_YOU_ARE_ON_VACATION)) {
				showToast(R.string.no_challenges_during_vacation);
			} else {
				showSinglePopupDialog(R.string.error, resultMessage);
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

	private void updateData() {
		if (!AppUtils.isNetworkAvailable(getActivity())) {
			return;
		}

		// First we check ids of games what we have. Challenges also will be stored in DB
		// when we ask server about new ids of games and challenges
		// if server have new ids we get those games with ids

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES_ALL);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
//		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_ID);
		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(currentGamesMyCursorUpdateListener,
				DbHelper.getDailyCurrentListGamesParams(getActivity()),
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
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_ACCEPTDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

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
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_PUT_GAME_ACTION(gameListCurrentItem.getGameId()));
			loadItem.setRequestMethod(RestHelper.PUT);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
			loadItem.addRequestParams(RestHelper.P_COMMAND, RestHelper.V_DECLINEDRAW);
			loadItem.addRequestParams(RestHelper.P_TIMESTAMP, gameListCurrentItem.getTimestamp());

			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
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
			ChessBoardOnline.resetInstance();

//			Intent intent = new Intent(getContext(), GameOnlineScreenActivity.class); // TODO adjust for fragment
//			intent.putExtra(BaseGameItem.GAME_ID, gameListCurrentItem.getGameId());
//			startActivity(intent);
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
					DbHelper.getEchessFinishedListGamesParams(getContext()),
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
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (gameType) {
				case CURRENT_MY:
					currentGamesMyCursorAdapter.changeCursor(returnedObj);
//					if (AppUtils.isNetworkAvailable(getContext()) && !hostUnreachable /*&& !isRestarted*/) { // TODO adjust
//						updateData();
//					} else {
//						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
//								DbHelper.getEchessFinishedListGamesParams(getContext()),
//								getContentResolver()).executeTask();
//					}

					if (finishedGameDataList != null) {
						boolean gamesLeft = DBDataManager.checkAndDeleteNonExistFinishedGames(getContext(), finishedGameDataList);

						if (gamesLeft) {
							new SaveDailyFinishedGamesListTask(saveFinishedGamesListUpdateListener, finishedGameDataList,
									getContentResolver()).executeTask();
						} else {
							finishedGamesCursorAdapter.changeCursor(null);
						}
					} else {
						new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
								DbHelper.getEchessFinishedListGamesParams(getContext()),
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
				emptyView.setText(R.string.no_games);
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}

			showEmptyView(true);
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

			{ // current games
				final List<DailyCurrentGameData> currentGamesList = returnedObj.getData().getCurrent();
				boolean gamesLeft = DBDataManager.checkAndDeleteNonExistCurrentGames(getContext(), currentGamesList);

				if (gamesLeft) {
					new SaveDailyCurrentGamesListTask(saveCurrentGamesListUpdateListener, currentGamesList, getContentResolver()).executeTask();
				} else {
					currentGamesMyCursorAdapter.changeCursor(null);
				}

				// check if we need to show new game button in dailyGamesFragment
				boolean myTurnInDailyGames = false;
				for (DailyCurrentGameData dailyGame : currentGamesList) {
					if (dailyGame.isMyTurn()) {
						myTurnInDailyGames = true;
						break;
					}
				}

				startNewGameBtn.setVisibility(myTurnInDailyGames ? View.GONE : View.VISIBLE);
			}

//			{ // finished
			finishedGameDataList = returnedObj.getData().getFinished();
//				boolean gamesLeft = DBDataManager.checkAndDeleteNonExistFinishedGames(getContext(), finishedGameDataList);
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
				showToast(ServerErrorCode.getUserFriendlyMessage(getActivity(), serverCode));
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
