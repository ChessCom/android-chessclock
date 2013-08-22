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
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.BaseResponseItem;
import com.chess.backend.entity.api.DailyChallengeItem;
import com.chess.backend.entity.api.DailyCurrentGameData;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.BaseGameItem;
import com.chess.model.GameOnlineItem;
import com.chess.ui.activities.old.ChatOnlineActivity;
import com.chess.ui.adapters.*;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.01.13
 * Time: 17:36
 */
public class DailyGamesRightFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener, SlidingMenu.OnOpenedListener, ItemClickListenerFace {

	private static final int CHALLENGES_SECTION = 0;
	private static final int CURRENT_GAMES_SECTION = 1;
	private static final int FINISHED_GAMES_SECTION = 3;

	private static final String DRAW_OFFER_PENDING_TAG = "DRAW_OFFER_PENDING_TAG";
	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";
	private static final String UNABLE_TO_MOVE_TAG = "unable to move popup";

	private int successToastMsgId;


	private DailyUpdateListener challengeInviteUpdateListener;
	private DailyUpdateListener acceptDrawUpdateListener;

	private IntentFilter listUpdateFilter;
	private BroadcastReceiver gamesUpdateReceiver;
	private SaveCurrentGamesListUpdateListener saveCurrentGamesListUpdateListener;
	private SaveFinishedGamesListUpdateListener saveFinishedGamesListUpdateListener;
	private GamesCursorUpdateListener currentGamesTheirCursorUpdateListener;
	private GamesCursorUpdateListener currentGamesMyCursorUpdateListener;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;
	private DailyGamesUpdateListener dailyGamesUpdateListener;

	private DailyCurrentGamesMyCursorRightAdapter currentGamesMyCursorAdapter;
	private DailyCurrentGamesTheirCursorRightAdapter currentGamesTheirCursorAdapter;
	private DailyChallengesGamesAdapter challengesGamesAdapter;
	private DailyFinishedGamesCursorRightAdapter finishedGamesCursorAdapter;
	private CustomSectionedAdapter sectionedAdapter;
	private DailyCurrentGameData gameListCurrentItem;
	private DailyChallengeItem.Data selectedChallengeItem;

	private TextView emptyView;
	private ListView listView;
	private View loadingView;
	private boolean onVacation;
	private View headerView;
	private DailyChallengeItem.Data challengeToRemove;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_dark);

		challengesGamesAdapter = new DailyChallengesGamesAdapter(this, null);
		currentGamesMyCursorAdapter = new DailyCurrentGamesMyCursorRightAdapter(getContext(), null);
		currentGamesTheirCursorAdapter = new DailyCurrentGamesTheirCursorRightAdapter(getContext(), null);
		finishedGamesCursorAdapter = new DailyFinishedGamesCursorRightAdapter(getContext(), null);

		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.new_my_move), currentGamesMyCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.new_their_move), currentGamesTheirCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.finished_games), finishedGamesCursorAdapter);

		listUpdateFilter = new IntentFilter(IntentConstants.USER_MOVE_UPDATE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		headerView = inflater.inflate(R.layout.new_start_new_game_button_view, null, false); // init here because of inflater
		return inflater.inflate(R.layout.new_daily_games_right_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);
		listView.setAdapter(sectionedAdapter);

		headerView.findViewById(R.id.startNewGameBtn).setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		init();
		getActivityFace().addOnOpenMenuListener(this);

		gamesUpdateReceiver = new GamesUpdateReceiver();
		registerReceiver(gamesUpdateReceiver, listUpdateFilter);
	}

	@Override
	public void onPause() {
		super.onPause();

		getActivityFace().removeOnOpenMenuListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		unRegisterMyReceiver(gamesUpdateReceiver);

		releaseResources();
	}

	private void init() {
		challengeInviteUpdateListener = new DailyUpdateListener(DailyUpdateListener.INVITE);
		acceptDrawUpdateListener = new DailyUpdateListener(DailyUpdateListener.DRAW);
		saveCurrentGamesListUpdateListener = new SaveCurrentGamesListUpdateListener();
		saveFinishedGamesListUpdateListener = new SaveFinishedGamesListUpdateListener();
		currentGamesMyCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.CURRENT_MY);
		currentGamesTheirCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.THEIR);
		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener(GamesCursorUpdateListener.FINISHED);

		dailyGamesUpdateListener = new DailyGamesUpdateListener();
	}

	private DialogInterface.OnClickListener gameListItemDialogListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface d, int pos) {
			if (pos == 0) {
				getActivityFace().openFragment(DailyChatFragment.createInstance(gameListCurrentItem.getGameId(),
						gameListCurrentItem.getBlackAvatar())); // TODO adjust avatar
			} else if (pos == 1) {
				String draw = RestHelper.V_OFFERDRAW;
				if (gameListCurrentItem.isDrawOffered() > 0)
					draw = RestHelper.V_ACCEPTDRAW;

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
			getActivityFace().changeRightFragment(HomePlayFragment.createInstance(RIGHT_MENU_MODE));

		} else if (view.getId() == R.id.acceptBtn) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			selectedChallengeItem = challengesGamesAdapter.getItem(position);
			acceptChallenge();
		} else if (view.getId() == R.id.cancelBtn) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			selectedChallengeItem = challengesGamesAdapter.getItem(position);
			declineChallenge();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
		boolean headerAdded = listView.getHeaderViewsCount() > 0; // use to check if header added
		int offset = headerAdded ? -1 : 0;

		int section = sectionedAdapter.getCurrentSection(position + offset);

		if (section == CHALLENGES_SECTION) {
			clickOnChallenge((DailyChallengeItem.Data) adapterView.getItemAtPosition(position));
		} else if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId()));
			getActivityFace().toggleRightMenu();
		} else {
			if (onVacation) { // TODO remove
				popupItem.setNegativeBtnId(R.string.end_vacation);
				showPopupDialog(R.string.unable_to_move_on_vacation, UNABLE_TO_MOVE_TAG);
				return;
			}

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

			if (gameListCurrentItem.isDrawOffered() > 0) {
				popupItem.setPositiveBtnId(R.string.accept);
				popupItem.setNeutralBtnId(R.string.decline);
				popupItem.setNegativeBtnId(R.string.game);
				popupItem.setButtons(3);

				showPopupDialog(R.string.accept_draw_q, DRAW_OFFER_PENDING_TAG);
			} else {
				ChessBoardOnline.resetInstance();
				getActivityFace().openFragment(GameDailyFragment.createInstance(gameListCurrentItem.getGameId()));
				getActivityFace().toggleRightMenu();
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
		int section = sectionedAdapter.getCurrentSection(pos);

		if (section == CHALLENGES_SECTION) {
			clickOnChallenge((DailyChallengeItem.Data) adapterView.getItemAtPosition(pos));
		} else if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameFromCursor(cursor);

			Intent intent = new Intent(getContext(), ChatOnlineActivity.class);
			intent.putExtra(BaseGameItem.GAME_ID, finishedItem.getGameId());
			startActivity(intent);
		} else {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			gameListCurrentItem = DbDataManager.getDailyCurrentGameFromCursor(cursor);

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
	public void onOpened() {

	}

	@Override
	public void onOpenedRight() {
		setBadgeValueForId(R.id.menu_games, 0);
		if (getActivity() == null)
			return;

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_GAMES_CHALLENGES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		new RequestJsonTask<DailyChallengeItem>(dailyGamesUpdateListener).executeTask(loadItem);

		loadDbGames();
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

	private void clickOnChallenge(DailyChallengeItem.Data gameListChallengeItem) {
		getActivityFace().openFragment(DailyInviteFragment.createInstance(gameListChallengeItem));
		getActivityFace().toggleRightMenu();
	}

	private class DailyUpdateListener extends ChessUpdateListener<BaseResponseItem> {
		public static final int INVITE = 3;
		public static final int DRAW = 4;
		public static final int VACATION = 5;

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
					showToast(successToastMsgId);
					DailyGamesRightFragment.this.updateData();
					break;
				case DRAW:
					DailyGamesRightFragment.this.updateData();
					break;
				case VACATION:

					break;
			}

			// remove that item from challenges list adapter
			challengesGamesAdapter.remove(selectedChallengeItem);
		}

		@Override
		public void errorHandle(String resultMessage) {
			// redundant check? we already clean the tasks pool in onPause, or...?
			// No, cleaning the task pool doesn't stop task immediately if it already reached onPOstExecute state.
			// this check prevent illegalStateExc for fragments, when they showed after onSavedInstance was called
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

//		LoadItem loadItem = new LoadItem();
//		loadItem.setLoadPath(RestHelper.CMD_GAMES_ALL);
//		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getAppData().getUserToken(getActivity()));
//		//		loadItem.addRequestParams(RestHelper.P_FIELDS, RestHelper.V_ID);
//		new RequestJsonTask<DailyGamesAllItem>(dailyGamesUpdateListener).executeTask(loadItem);
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(currentGamesMyCursorUpdateListener,
				DbHelper.getDailyCurrentMyListGames(getUsername()),
				getContentResolver()).executeTask();
		new LoadDataFromDbTask(currentGamesTheirCursorUpdateListener,
				DbHelper.getDailyCurrentTheirListGames(getUsername()),
				getContentResolver()).executeTask();
		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
				DbHelper.getDailyFinishedListGames(getUsername()),
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
		} else if (tag.equals(CHALLENGE_ACCEPT_TAG)) {
			acceptChallenge();
		}
		super.onPositiveBtnClick(fragment);
	}

	private void acceptChallenge() {
		LoadItem loadItem = LoadHelper.acceptChallenge(getUserToken(), selectedChallengeItem.getGameId());
		successToastMsgId = R.string.challenge_accepted;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onNeutralBtnCLick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onNeutralBtnCLick(fragment);
			return;
		}

		if (tag.equals(DRAW_OFFER_PENDING_TAG)) {
			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
					RestHelper.V_DECLINEDRAW, gameListCurrentItem.getTimestamp());
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

		} else if (tag.equals(CHALLENGE_ACCEPT_TAG)) {
			declineChallenge();
		}
		super.onNegativeBtnClick(fragment);
	}

	private void declineChallenge() {
		LoadItem loadItem = LoadHelper.declineChallenge(getUserToken(), selectedChallengeItem.getGameId());
		successToastMsgId = R.string.challenge_declined;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
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
		public static final int THEIR = 1;
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
//								DbHelper.getDailyFinishedListGames(getContext()),
//								getContentResolver()).executeTask();
//					}

					break;
				case THEIR:
					currentGamesTheirCursorAdapter.changeCursor(returnedObj);

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

	private class DailyGamesUpdateListener extends ChessUpdateListener<DailyChallengeItem> {

		public DailyGamesUpdateListener() {
			super(DailyChallengeItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(DailyChallengeItem returnedObj) {
			super.updateData(returnedObj);

			challengesGamesAdapter.setItemsList(returnedObj.getData());
			sectionedAdapter.notifyDataSetChanged();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
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
//				emptyView.setVisibility(View.VISIBLE);
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
