package com.chess.ui.fragments.lessons;

import android.os.Bundle;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.MultiDirectionSlidingDrawer;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.LessonItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.engine.ChessBoardLessons;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.game_ui.GameLessonFace;
import com.chess.ui.views.chess_boards.ChessBoardLessonsView;
import com.chess.ui.views.game_controls.ControlsLessonsView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 20:06
 */
public class GameLessonFragment extends GameBaseFragment implements GameLessonFace, PopupListSelectionFace, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {

	private static final String LESSON_ID = "lesson_id";

	// Quick action ids
	private static final int ID_NEXT_TACTIC = 0;
	private static final int ID_SHOW_ANSWER = 1;
	private static final int ID_PRACTICE = 2;
	private static final int ID_SETTINGS = 3;
	private static final long DRAWER_UPDATE_DELAY = 100;

	private LessonUpdateListener lessonUpdateListener;
	private int lessonId;
	private ControlsLessonsView controlsLessonsView;
	private ChessBoardLessonsView boardView;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private SparseArray<String> optionsArray;
	private LessonItem.MentorLesson lessonItem;
	private List<LessonItem.MentorPosition> positions;
	private TextView descriptionTxt;
	private TextView lessonTitleTxt;
	private TextView commentTxt;
	private MultiDirectionSlidingDrawer slidingDrawer;

	public GameLessonFragment() {}

	public static GameLessonFragment createInstance(int lessonId) {
		GameLessonFragment fragment = new GameLessonFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(LESSON_ID, lessonId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			lessonId = getArguments().getInt(LESSON_ID);
		} else {
			lessonId = savedInstanceState.getInt(LESSON_ID);
		}
		lessonUpdateListener = new LessonUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_lessons_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

		widgetsInit(view);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_share, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_LESSON_BY_ID(lessonId));
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		// TODO user restart parameter http GET http://api.c.com/v1/lessons/lessons/1 loginToken==4a3183b2355b85983d81a810c0191a27 restart==true

		new RequestJsonTask<LessonItem>(lessonUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(LESSON_ID, lessonId);
	}

	@Override
	public Boolean isUserColorWhite() {
		return null;
	}

	@Override
	public Long getGameId() {
		return null;
	}

	@Override
	public void showOptions(View view) {
		if (optionsSelectFragment != null) {
			return;
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	@Override
	public void newGame() {

	}

	@Override
	public void switch2Analysis() {

	}

	@Override
	public void updateAfterMove() {

	}

	@Override
	public void invalidateGameScreen() {

	}

	@Override
	public String getWhitePlayerName() {
		return null;
	}

	@Override
	public String getBlackPlayerName() {
		return null;
	}

	@Override
	public boolean currentGameExist() {
		return false;
	}

	@Override
	public BoardFace getBoardFace() {
		return ChessBoardLessons.getInstance(this);
	}

	@Override
	public void toggleSides() {

	}

	@Override
	protected void restoreGame() {

	}

	@Override
	public void startLesson() {
		controlsLessonsView.showDefault();
		slidingDrawer.animateOpen();
	}

	@Override
	public void restart() {

	}

	@Override
	public void showHint() {

	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_NEXT_TACTIC) {
//			getNextTactic();
		} else if (code == ID_SHOW_ANSWER) {
//			showAnswer();
		} else if (code == ID_PRACTICE) {
			switch2Analysis();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsBoardFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	@Override
	public void onDrawerOpened() {
		lessonTitleTxt.setVisibility(View.GONE);
		commentTxt.setVisibility(View.GONE);

		controlsLessonsView.showDefault();
	}

	@Override
	public void onDrawerClosed() {
		lessonTitleTxt.setVisibility(View.VISIBLE);
		commentTxt.setVisibility(View.VISIBLE);
	}

	private class LessonUpdateListener extends ChessLoadUpdateListener<LessonItem> {

		private LessonUpdateListener() {
			super(LessonItem.class);
		}

		@Override
		public void updateData(LessonItem returnedObj) {
			super.updateData(returnedObj);

			lessonItem = returnedObj.getData().getLesson();

			positions = returnedObj.getData().getPositions();

			adjustBoardForGame();

		}
	}

	private void adjustBoardForGame() {
		ChessBoardLessons.resetInstance();
		final BoardFace boardFace = ChessBoardLessons.getInstance(this);
		boardView.setGameUiFace(this);

		LessonItem.MentorPosition mentorPosition = positions.get(0);

		List<LessonItem.MentorPosition.LessonMove> lessonMoves = mentorPosition.getLessonMoves();
		LessonItem.MentorPosition.LessonMove lessonMove = lessonMoves.get(0);
		boardFace.setupBoard(mentorPosition.getFen());

		boardFace.updateMoves(lessonMove.getMove() /*boardFace.getTacticMoves()[0]*/, true);

		invalidateGameScreen();
		boardFace.takeBack();
		boardView.invalidate();

		playLastMoveAnimation();

//		firstRun = false;
		controlsLessonsView.enableGameControls(true);
//		if (tacticItem.isRetry()) {
//			controlsTacticsView.showAfterRetry();
//		} else {
//			controlsTacticsView.showDefault();
//		}

		lessonTitleTxt.setText(lessonItem.getName());
		commentTxt.setText(lessonItem.getGoalCommentary());
		descriptionTxt.setText(Html.fromHtml(lessonItem.getAbout()));
	}

	private void widgetsInit(View view) {
		controlsLessonsView = (ControlsLessonsView) view.findViewById(R.id.controlsLessonsView);

		boardView = (ChessBoardLessonsView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsLessonsView);
		boardView.setGameFace(this);

		controlsLessonsView.setBoardViewFace(boardView);

		setBoardView(boardView);

//		final ChessBoard chessBoard = ChessBoardLessons.getInstance(this);
//		firstRun = chessBoard.isJustInitialized();
		boardView.setGameFace(this);

		controlsLessonsView.enableGameControls(false);

		// TODO adjust properly for tablets later

		slidingDrawer = (MultiDirectionSlidingDrawer) view.findViewById(R.id.slidingDrawer);

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				int statusBarHeight = getStatusBarHeight();
				int width = getResources().getDisplayMetrics().widthPixels;
				int height = getResources().getDisplayMetrics().heightPixels;
				int actionBarHeight = getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height);
				int controlsViewHeight = getResources().getDimensionPixelSize(R.dimen.game_controls_button_height);
				int handleHeight = getResources().getDimensionPixelSize(R.dimen.drawer_handler_height);
				int topBoardOffset = height - width - actionBarHeight - controlsViewHeight - handleHeight - statusBarHeight;
				slidingDrawer.setTopOffset(topBoardOffset);

			}
		}, DRAWER_UPDATE_DELAY);
		slidingDrawer.setOnDrawerOpenListener(this);
		slidingDrawer.setOnDrawerCloseListener(this);

		lessonTitleTxt = (TextView) view.findViewById(R.id.lessonTitleTxt);
		commentTxt = (TextView) view.findViewById(R.id.commentTxt);
		descriptionTxt = (TextView) view.findViewById(R.id.descriptionTxt);

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_NEXT_TACTIC, getString(R.string.next_tactic));
			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

}
