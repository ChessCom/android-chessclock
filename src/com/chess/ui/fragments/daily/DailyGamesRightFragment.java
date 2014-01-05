package com.chess.ui.fragments.daily;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.chess.backend.entity.api.*;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.model.GameOnlineItem;
import com.chess.statics.IntentConstants;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.*;
import com.chess.ui.engine.ChessBoardOnline;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.home.HomePlayFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
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
	//	private static final String CHALLENGE_ACCEPT_TAG = "challenge accept popup";
	private static final String UNABLE_TO_MOVE_TAG = "unable to move popup";

	private int successToastMsgId;


	private DailyUpdateListener challengeInviteUpdateListener;
	private DailyUpdateListener acceptDrawUpdateListener;

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
	private View topButtonsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// init adapters
		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_text_section_header_dark);

		challengesGamesAdapter = new DailyChallengesGamesAdapter(this, null, getImageFetcher());
		currentGamesMyCursorAdapter = new DailyCurrentGamesMyCursorRightAdapter(getContext(), null, getImageFetcher());
		currentGamesTheirCursorAdapter = new DailyCurrentGamesTheirCursorRightAdapter(getContext(), null, getImageFetcher());
		finishedGamesCursorAdapter = new DailyFinishedGamesCursorRightAdapter(getContext(), null, getImageFetcher());

		sectionedAdapter.addSection(getString(R.string.challenges), challengesGamesAdapter);
		sectionedAdapter.addSection(getString(R.string.new_my_move), currentGamesMyCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.new_their_move), currentGamesTheirCursorAdapter);
		sectionedAdapter.addSection(getString(R.string.finished_games), finishedGamesCursorAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_daily_games_right_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_start_new_game_button_view, null, false);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);
		topButtonsView = headerView.findViewById(R.id.topButtonsView);
		topButtonsView.setVisibility(View.GONE);

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

	}

	@Override
	public void onPause() {
		super.onPause();

		getActivityFace().removeOnOpenMenuListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();

		releaseResources();
	}

	private void init() {
		challengeInviteUpdateListener = new DailyUpdateListener(DailyUpdateListener.CHALLENGE);
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
				// update game state before accepting draw
				LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameListCurrentItem.getGameId());
				new RequestJsonTask<DailyCurrentGameItem>(new GameStateUpdatesListener()).executeTask(loadItem);
//				String draw = RestHelper.V_OFFERDRAW;
//				if (gameListCurrentItem.isDrawOffered() > 0)
//					draw = RestHelper.V_ACCEPTDRAW;
//
//				LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
//						draw, gameListCurrentItem.getTimestamp());
//				new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
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

			Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
			gameListCurrentItem = DbDataManager.getDailyCurrentGameListFromCursor(cursor);

			if (gameListCurrentItem.isDrawOffered() > 0) {
				popupItem.setNeutralBtnId(R.string.ic_play);
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
		boolean headerAdded = listView.getHeaderViewsCount() > 0; // use to check if header added
		int offset = headerAdded ? -1 : 0;

		int section = sectionedAdapter.getCurrentSection(pos + offset);

		if (section == CHALLENGES_SECTION) {
			clickOnChallenge((DailyChallengeItem.Data) adapterView.getItemAtPosition(pos));
		} else if (section == FINISHED_GAMES_SECTION) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(pos);
			DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameFromCursor(cursor);

			getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId()));
			getActivityFace().toggleRightMenu();
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
		if (getActivity() == null) {
			return;
		}

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_CHALLENGES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		new RequestJsonTask<DailyChallengeItem>(dailyGamesUpdateListener).executeTask(loadItem);

		loadDbGames();
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private void clickOnChallenge(DailyChallengeItem.Data gameListChallengeItem) {
		getActivityFace().openFragment(DailyInviteFragment.createInstance(gameListChallengeItem));
		getActivityFace().toggleRightMenu();
	}

	private class GameStateUpdatesListener extends ChessLoadUpdateListener<DailyCurrentGameItem> {

		private GameStateUpdatesListener() {
			super(DailyCurrentGameItem.class);
		}

		@Override
		public void updateData(DailyCurrentGameItem returnedObj) {
			String draw = RestHelper.V_OFFERDRAW;
			if (returnedObj.getData().isDrawOffered() > 0) {
				draw = RestHelper.V_ACCEPTDRAW;
			}

			LoadItem loadItem = LoadHelper.putGameAction(getUserToken(), gameListCurrentItem.getGameId(),
					draw, gameListCurrentItem.getTimestamp());
			new RequestJsonTask<BaseResponseItem>(acceptDrawUpdateListener).executeTask(loadItem);
		}
	}

	private class DailyUpdateListener extends ChessUpdateListener<BaseResponseItem> {
		public static final int CHALLENGE = 3;
		public static final int DRAW = 4;
		public static final int VACATION = 5;

		private int itemCode;

		public DailyUpdateListener(int itemCode) {
			super(BaseResponseItem.class);
			this.itemCode = itemCode;
		}

		@Override
		public void updateData(BaseResponseItem returnedObj) {
			switch (itemCode) {
				case CHALLENGE:
					showToast(successToastMsgId);
					// remove that item from challenges list adapter
					challengesGamesAdapter.remove(selectedChallengeItem);

					// clear notification badge
					DbDataManager.deleteNewChallengeNotification(getContentResolver(), getUsername(),
							selectedChallengeItem.getGameId());
					updateNotificationBadges();

					getActivity().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));

				case VACATION:

					break;
			}
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (itemCode == GameOnlineItem.CURRENT_TYPE || itemCode == GameOnlineItem.CHALLENGES_TYPE
					|| itemCode == GameOnlineItem.FINISHED_TYPE) {
				if (resultCode == StaticData.NO_NETWORK || resultCode == StaticData.UNKNOWN_ERROR) {
					showToast(R.string.host_unreachable_load_local);
					loadDbGames();
				}
			}
		}
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
			// update game state before accepting draw
			LoadItem loadItem = LoadHelper.getGameById(getUserToken(), gameListCurrentItem.getGameId());
			new RequestJsonTask<DailyCurrentGameItem>(new GameStateUpdatesListener()).executeTask(loadItem);
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
			ChessBoardOnline.resetInstance();
			getActivityFace().openFragment(GameDailyFragment.createInstance(gameListCurrentItem.getGameId()));
			getActivityFace().toggleRightMenu();
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

	private void declineChallenge() {
		LoadItem loadItem = LoadHelper.declineChallenge(getUserToken(), selectedChallengeItem.getGameId());
		successToastMsgId = R.string.challenge_declined;

		new RequestJsonTask<BaseResponseItem>(challengeInviteUpdateListener).executeTask(loadItem);
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
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (gameType) {
				case CURRENT_MY:
					currentGamesMyCursorAdapter.changeCursor(returnedObj);
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
			switch (gameType) {
				case CURRENT_MY:
					currentGamesMyCursorAdapter.changeCursor(null);
					break;
				case THEIR:
					currentGamesTheirCursorAdapter.changeCursor(null);
					break;
				case FINISHED:
					finishedGamesCursorAdapter.changeCursor(null);
					need2update = false;
					break;
			}

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
		public void updateData(DailyChallengeItem returnedObj) {
			super.updateData(returnedObj);

			challengesGamesAdapter.setItemsList(returnedObj.getData());
			sectionedAdapter.notifyDataSetChanged();

			topButtonsView.setVisibility(View.VISIBLE);

			DbDataManager.deleteAllPlayMoveNotifications(getContentResolver());

			updateNotificationBadges();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				showToast(ServerErrorCodes.getUserFriendlyMessage(getActivity(), serverCode));
			} else if (resultCode == StaticData.INTERNAL_ERROR) {
				showToast("Internal error occurred"); // TODO adjust properly
			}
		}
	}

	private void releaseResources() {
		if (challengeInviteUpdateListener != null) {
			challengeInviteUpdateListener.releaseContext();
			challengeInviteUpdateListener = null;
		}

		if (acceptDrawUpdateListener != null) {
			acceptDrawUpdateListener.releaseContext();
			acceptDrawUpdateListener = null;
		}

		if (saveCurrentGamesListUpdateListener != null) {
			saveCurrentGamesListUpdateListener.releaseContext();
			saveCurrentGamesListUpdateListener = null;
		}
		if (currentGamesMyCursorUpdateListener != null) {
			currentGamesMyCursorUpdateListener.releaseContext();
			currentGamesMyCursorUpdateListener = null;
		}

		if (dailyGamesUpdateListener != null) {
			dailyGamesUpdateListener.releaseContext();
			dailyGamesUpdateListener = null;
		}
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
