package com.chess.ui.fragments.live;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
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
import com.chess.lcc.android.DataNotValidException;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.statics.StaticData;
import com.chess.ui.activities.LiveBaseActivity;
import com.chess.ui.adapters.LiveArchiveGamesAdapterTablet;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;
import com.chess.utilities.AppUtils;

import java.util.List;

import static com.chess.backend.RestHelper.P_AVATAR_SIZE;
import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 12:46
 */
public class LiveHomeFragmentTablet extends LiveHomeFragment implements ViewTreeObserver.OnGlobalLayoutListener {

	private LiveArchiveGamesAdapterTablet archiveGamesAdapter;
	private ArchiveGamesUpdateListener archiveGamesUpdateListener;
	private GamesCursorUpdateListener archiveGamesCursorUpdateListener;
	private SaveArchiveGamesListUpdateListener saveArchiveGamesListUpdateListener;

	private View currentGameHeaderView;
	private View topGameHeaderView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_play_games_frame, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (inLandscape()) {
			if (need2update) {
				boolean haveSavedData = DbDataManager.haveSavedLiveArchiveGame(getActivity(), getUsername());

				if (isNetworkAvailable()) {
					updateData();
				}

				if (haveSavedData) {
					loadDbGames();
				}
			} else {
				loadDbGames();
			}

			loadRecentFriends();
		}
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

	@Override
	protected void adjustFeaturesList(LiveConnectionHelper liveHelper) {
		if (inPortrait()) {
			super.adjustFeaturesList(liveHelper);

		} else {

			if (liveHelper.isActiveGamePresent() && !liveHelper.isCurrentGameObserved()) {
				currentGameHeaderView.setVisibility(View.VISIBLE);
				topGameHeaderView.setVisibility(View.GONE);
			} else {
				currentGameHeaderView.setVisibility(View.GONE);
				topGameHeaderView.setVisibility(View.VISIBLE);
			}
		}
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
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.friendsHeaderView) {
			if (isLCSBound) {
				try {
					LiveConnectionHelper liveHelper = getLiveHelper();
					liveFriends = liveHelper.getOnlineFriends();

					SparseArray<String> optionsMap = new SparseArray<String>();
					for (int i = 0; i < liveFriends.length; i++) {
						String friend = liveFriends[i];
						optionsMap.put(i, friend);
					}

					friendSelectFragment = PopupOptionsMenuFragment.createInstance(friendSelectedListener, optionsMap);
					friendSelectFragment.show(getFragmentManager(), FRIEND_SELECTION_TAG);
				} catch (DataNotValidException e) {
					e.printStackTrace();
				}
			}
		} else if (id == R.id.topGameHeaderView) {
			if (!isLCSBound) {
				showToast(R.string.still_connecting);
				return;
			}
			Fragment fragmentByTag;
			fragmentByTag = ((LiveBaseActivity) getActivity()).getGameLiveObserverFragment();
			if (fragmentByTag == null) {
				if (!isTablet) {
					fragmentByTag = new GameLiveObserveFragment();
				} else {
					fragmentByTag = new GameLiveObserveFragmentTablet();
				}
			}
//			getActivityFace().openFragment((BasePopupsFragment) fragmentByTag, true);
			getActivityFace().openFragment((BasePopupsFragment) fragmentByTag);
		} else if (id == R.id.currentGameHeaderView) {
			Fragment fragmentByTag;
			fragmentByTag = getFragmentManager().findFragmentByTag(GameLiveFragmentTablet.class.getSimpleName());
			if (fragmentByTag == null) {
				fragmentByTag = new GameLiveFragmentTablet();
			}
			getActivityFace().openFragment((BasePopupsFragment) fragmentByTag);
		} else if (id == R.id.statsHeaderView) {
			getActivityFace().openFragment(new StatsGameFragment());
		} else if (id == R.id.statsView1) {
			openStatsForUser(StatsGameFragment.LIVE_STANDARD, getUsername());
		} else if (id == R.id.statsView2) {
			openStatsForUser(StatsGameFragment.LIVE_BLITZ, getUsername());
		} else if (id == R.id.statsView3) {
			openStatsForUser(StatsGameFragment.LIVE_LIGHTNING, getUsername());
		} else if (view.getId() == R.id.inviteFriendView1) {
			getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(RIGHT_MENU_MODE, firstFriendUserName));
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.inviteFriendView2) {
			getActivityFace().changeRightFragment(LiveGameOptionsFragment.createInstance(RIGHT_MENU_MODE, secondFriendUserName));
			getActivityFace().toggleRightMenu();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (inPortrait()) {
			super.onItemClick(parent, view, position, id);
			return;
		}
		if (position != 0) {
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			long gameId = DbDataManager.getLong(cursor, DbScheme.V_ID);

			getActivityFace().openFragment(GameLiveArchiveFragment.createInstance(gameId));
		}
	}

	@Override
	public void onGlobalLayout() {
		if (getView() == null || getView().getViewTreeObserver() == null) {
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
		} else {
			getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}

		View boardview = getView().findViewById(R.id.boardview);
		int boardWidth = boardview.getWidth();
		int squareSize = boardWidth / 8; // one square size
		inviteOverlaySetup(getResources(), getView().findViewById(R.id.startOverlayView), squareSize);
		onlinePlayersCntTxt = (TextView) getView().findViewById(R.id.onlinePlayersCntTxt);
	}

	@Override
	protected void inviteOverlaySetup(Resources resources, View startOverlayView, int squareSize) {
		// let's make it to match board properties
		// it should be 2.5 squares inset from top of border and 3 squares tall + 1.5 squares from sides

		int borderOffset = 0;
		// now we add few pixel to compensate shadow addition
		int shadowOffset = 0;
		borderOffset += shadowOffset;
		int overlayHeight = squareSize * 3 + borderOffset + shadowOffset;

		int popupWidth = squareSize * 5 + shadowOffset * 2 + borderOffset;  // for tablets we need more width
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(popupWidth, overlayHeight);
		int topMargin = (int) (squareSize * 2.5f + borderOffset - shadowOffset * 2);

		float leftOffset = 2.2f;
		if (inPortrait()) {
			leftOffset = 1.5f;
		}
		params.setMargins((int) (squareSize * leftOffset - shadowOffset), topMargin, squareSize - borderOffset, 0);
		params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardLinLay);
		startOverlayView.setLayoutParams(params);
		startOverlayView.setVisibility(View.VISIBLE);
		// set min width
		startOverlayView.setMinimumWidth(squareSize * 6);
	}

	private void init() {
		archiveGamesAdapter = new LiveArchiveGamesAdapterTablet(getActivity(), null, getImageFetcher());
		archiveGamesUpdateListener = new ArchiveGamesUpdateListener();
		archiveGamesCursorUpdateListener = new GamesCursorUpdateListener();
		saveArchiveGamesListUpdateListener = new SaveArchiveGamesListUpdateListener();
	}

	@Override
	protected void widgetsInit(View view) {
		if (inPortrait()) {
			super.widgetsInit(view);
			return;
		}
		view.getViewTreeObserver().addOnGlobalLayoutListener(this);

		view.findViewById(R.id.gamePlayBtn).setOnClickListener(this);

		{ // Time mode adjustments
			int mode = getAppData().getDefaultLiveMode();
			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);

			timeSelectBtn = (Button) view.findViewById(R.id.timeSelectBtn);
			timeSelectBtn.setOnClickListener(this);
			timeSelectBtn.setText(AppUtils.getLiveModeButtonLabel(newGameButtonsArray[mode], getActivity()));
		}
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_live_home_options_tablet_header_view, null, false);
		initHeaderViews(headerView);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(archiveGamesAdapter);
		listView.setOnItemClickListener(this);

		ChessBoardBaseView boardView = (ChessBoardBaseView) view.findViewById(R.id.boardview);
		boardView.setGameFace(gameFaceHelper);
	}

	private void initHeaderViews(View view) {
		view.findViewById(R.id.newGameHeaderView).setOnClickListener(this);
		view.findViewById(R.id.friendsHeaderView).setOnClickListener(this);
		view.findViewById(R.id.statsHeaderView).setOnClickListener(this);
		view.findViewById(R.id.statsView1).setOnClickListener(this);
		view.findViewById(R.id.statsView2).setOnClickListener(this);
		view.findViewById(R.id.archiveHeaderView).setOnClickListener(this);

		currentGameHeaderView = view.findViewById(R.id.currentGameHeaderView);
		currentGameHeaderView.setOnClickListener(this);
		topGameHeaderView = view.findViewById(R.id.topGameHeaderView);
		topGameHeaderView.setOnClickListener(this);

		inviteFriendView1 = view.findViewById(R.id.inviteFriendView1);
		inviteFriendView2 = view.findViewById(R.id.inviteFriendView2);
		friendUserName1Txt = (TextView) view.findViewById(R.id.friendUserName1Txt);
		friendRealName1Txt = (TextView) view.findViewById(R.id.friendRealName1Txt);
		friendUserName2Txt = (TextView) view.findViewById(R.id.friendUserName2Txt);
		friendRealName2Txt = (TextView) view.findViewById(R.id.friendRealName2Txt);
	}

}
