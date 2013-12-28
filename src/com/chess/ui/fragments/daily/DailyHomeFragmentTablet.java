package com.chess.ui.fragments.daily;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.chess.R;
import com.chess.backend.entity.api.DailyFinishedGameData;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.ui.adapters.DailyArchiveGamesCursorAdapter;
import com.chess.ui.fragments.friends.FriendsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.chess_boards.ChessBoardBaseView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 12:06
 */
public class DailyHomeFragmentTablet extends DailyHomeFragment implements ItemClickListenerFace, ViewTreeObserver.OnGlobalLayoutListener {

	private DailyArchiveGamesCursorAdapter finishedGamesCursorAdapter;
	private GamesCursorUpdateListener finishedGamesCursorUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		finishedGamesCursorUpdateListener = new GamesCursorUpdateListener();

		finishedGamesCursorAdapter = new DailyArchiveGamesCursorAdapter(getContext(), null, getImageFetcher());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_play_games_frame, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (inLandscape()) {
			loadDbGames();
			loadRecentOpponents();
		}
	}

	private void loadDbGames() {
		new LoadDataFromDbTask(finishedGamesCursorUpdateListener,
				DbHelper.getDailyFinishedListGames(getUsername()),
				getContentResolver()).executeTask();
	}

	private class GamesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			finishedGamesCursorAdapter.changeCursor(returnedObj);
			need2update = false;
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.friendsHeaderView) {
			getActivityFace().openFragment(new FriendsFragment());
		} else if (id == R.id.statsHeaderView){
			getActivityFace().openFragment(new StatsGameFragment());
		} else if (id == R.id.statsView1){
			getActivityFace().openFragment(StatsGameFragment.createInstance(StatsGameFragment.DAILY_CHESS, getUsername()));
		} else if (id == R.id.statsView2){
			getActivityFace().openFragment(StatsGameFragment.createInstance(StatsGameFragment.DAILY_CHESS960, getUsername()));
		} else if (view.getId() == R.id.inviteFriendView1) {
			getActivityFace().changeRightFragment(DailyGameOptionsFragment.createInstance(RIGHT_MENU_MODE, firstFriendUserName));
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.inviteFriendView2) {
			getActivityFace().changeRightFragment(DailyGameOptionsFragment.createInstance(RIGHT_MENU_MODE, secondFriendUserName));
			getActivityFace().toggleRightMenu();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (inPortrait()) {
			super.onItemClick(parent, view, position, id);
		} else {
			if (position != 0) { // don't click on header
				Cursor cursor = (Cursor) parent.getItemAtPosition(position);
				DailyFinishedGameData finishedItem = DbDataManager.getDailyFinishedGameListFromCursor(cursor);

				getActivityFace().openFragment(GameDailyFinishedFragment.createInstance(finishedItem.getGameId()));
			}
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

		Resources resources = getResources();

		{ // new game overlay setup
			View startOverlayView = getView().findViewById(R.id.startOverlayView);

			// let's make it to match board properties
			// it should be 2.5 squares inset from top of border and 3 squares tall + 1.5 squares from sides

			View boardview = getView().findViewById(R.id.boardview);
			int boardWidth = boardview.getWidth();
			int squareSize = boardWidth / 8; // one square size
			int borderOffset = resources.getDimensionPixelSize(R.dimen.invite_overlay_top_offset);
			// now we add few pixel to compensate shadow addition
			int shadowOffset = resources.getDimensionPixelSize(R.dimen.overlay_shadow_offset);
			borderOffset += shadowOffset;
			int overlayHeight = squareSize * 3 + borderOffset + shadowOffset;

			int popupWidth = squareSize * 5 + shadowOffset * 2 + borderOffset;  // for tablets we need more width
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(popupWidth, overlayHeight);
			int topMargin = (int) (squareSize * 2.5f + borderOffset - shadowOffset * 2);

			params.setMargins((int) (squareSize * 1.5f - shadowOffset), topMargin, squareSize - borderOffset, 0);
			params.addRule(RelativeLayout.ALIGN_TOP, R.id.boardView);
			startOverlayView.setLayoutParams(params);
			startOverlayView.setVisibility(View.VISIBLE);

			onlinePlayersCntTxt = (TextView) getView().findViewById(R.id.onlinePlayersCntTxt);
		}
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
			int mode = getAppData().getDefaultDailyMode();
			// set texts to buttons
			newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
			// TODO add sliding from outside animation for time modes in popup
			timeSelectBtn = (Button) view.findViewById(R.id.timeSelectBtn);
			timeSelectBtn.setOnClickListener(this);

			timeSelectBtn.setText(getDaysString(newGameButtonsArray[mode]));
		}

		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_daily_home_options_tablet_header_view, null, false);
		initHeaderViews(headerView);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(finishedGamesCursorAdapter);
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

		inviteFriendView1 = view.findViewById(R.id.inviteFriendView1);
		inviteFriendView2 = view.findViewById(R.id.inviteFriendView2);
		friendUserName1Txt = (TextView) view.findViewById(R.id.friendUserName1Txt);
		friendRealName1Txt = (TextView) view.findViewById(R.id.friendRealName1Txt);
		friendUserName2Txt = (TextView) view.findViewById(R.id.friendUserName2Txt);
		friendRealName2Txt = (TextView) view.findViewById(R.id.friendRealName2Txt);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
