package com.chess.ui.fragments.lessons;

import android.os.Bundle;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.FontsHelper;
import com.chess.MultiDirectionSlidingDrawer;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.LessonItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.engine.ChessBoardLessons;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsBoardFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.LessonsBoardFace;
import com.chess.ui.interfaces.game_ui.GameLessonFace;
import com.chess.ui.views.chess_boards.ChessBoardLessonsView;
import com.chess.ui.views.drawables.YourMoveDrawable;
import com.chess.ui.views.game_controls.ControlsLessonsView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.CustomTypefaceSpan;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 20:06
 */
public class GameLessonFragment extends GameBaseFragment implements GameLessonFace, PopupListSelectionFace, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {

	private static final String LESSON_ID = "lesson_id";
	public static final String BOLD_DIVIDER = "##";

	private static final long DRAWER_UPDATE_DELAY = 100;

	// Options ids
	private static final int ID_KEY_SQUARES = 0;
	private static final int ID_CORRECT_SQUARE = 1;
	private static final int ID_KEY_PIECES = 2;
	private static final int ID_CORRECT_PIECE = 3;
	private static final int ID_ANALYSIS_BOARD = 4;
	private static final int ID_VS_COMPUTER = 5;
	private static final int ID_SKIP_LESSON = 6;
	private static final int ID_SHOW_ANSWER = 7;
	private static final int ID_SETTINGS = 8;

	private LessonUpdateListener lessonUpdateListener;
	private int lessonId;
	private ControlsLessonsView controlsLessonsView;
	private ChessBoardLessonsView boardView;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private SparseArray<String> optionsArray;
	private LessonItem.MentorLesson lessonItem;
	private List<LessonItem.MentorPosition> positionsToLearn;
	private TextView lessonTitleTxt;
	private TextView commentTxt;
	private TextView descriptionTxt;
	private TextView hintTxt;
	private MultiDirectionSlidingDrawer slidingDrawer;
	private boolean isAnalysis;
	private List<LessonItem.MentorPosition.PossibleMove> possibleMoves;
	private int currentLearningPosition;
	private int totalLearningPositionsCnt;
	private boolean need2update = true;
	private int usedHints;
	private View hintDivider;
	//	private ForegroundColorSpan boldSpan;
	private CustomTypefaceSpan boldSpan;

	public GameLessonFragment() {
	}

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

		boldSpan = new CustomTypefaceSpan("san-serif", FontsHelper.getInstance().getTypeFace(getActivity(), FontsHelper.BOLD_FONT));


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

		if (need2update) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_LESSON_BY_ID(lessonId));
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

			// TODO user restart parameter http GET http://api.c.com/v1/lessons/lessons/1 loginToken==4a3183b2355b85983d81a810c0191a27 restart==true
			new RequestJsonTask<LessonItem>(lessonUpdateListener).executeTask(loadItem);
		} else {
			startLesson();
			adjustBoardForGame();
		}
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
		if (!currentGameExist()) {
			return null;
		} else {
			return (long) lessonId;
		}
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
		if (currentLearningPosition < totalLearningPositionsCnt) {
			currentLearningPosition++;
			adjustBoardForGame();
		} else {
			// TODO disable skip & next buttons
			showToast("No more positions to learn");
		}
	}

	@Override
	public void switch2Analysis() {
		isAnalysis = !isAnalysis;
		if (!isAnalysis) {
			restoreGame();
		}
		getBoardFace().setAnalysis(isAnalysis);
		controlsLessonsView.showDefault();
	}

	@Override
	public void updateAfterMove() {

	}

	@Override
	public void invalidateGameScreen() {
		boardView.invalidate();
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
		return lessonItem != null;
	}

	@Override
	public LessonsBoardFace getBoardFace() {
		return ChessBoardLessons.getInstance(this);
	}

	@Override
	public void toggleSides() {

	}

	@Override
	protected void restoreGame() {
		if (!currentGameExist()) {
			return;
		}
		adjustBoardForGame();
	}

	@Override
	public void startLesson() {
		controlsLessonsView.showDefault();
		slidingDrawer.animateOpen();
	}

	@Override
	public void verifyMove() {
		// TODO show alternative correct moves

		LessonsBoardFace boardFace = getBoardFace();
		String lastUserMove = boardFace.getLastMoveStr();
		LessonItem.MentorPosition positionToLearn = positionsToLearn.get(currentLearningPosition);

		logTest("getAbout - " + positionToLearn.getAbout());
		logTest("getAdvice1 - " + positionToLearn.getAdvice1());
		logTest("getAdvice2 - " + positionToLearn.getAdvice2());
		logTest("getAdvice3 - " + positionToLearn.getAdvice3());
		logTest("getStandardWrongMoveCommentary - " + positionToLearn.getStandardWrongMoveCommentary());
		logTest("getStandardResponseMoveCommentary - " + positionToLearn.getStandardResponseMoveCommentary());

		logTest("___________________________________________");
		logTest("getMove - " + possibleMoves.get(currentLearningPosition).getMove());
		logTest("getMoveCommentary - " + possibleMoves.get(currentLearningPosition).getMoveCommentary());
		logTest("getResponseMoveCommentary - " + possibleMoves.get(currentLearningPosition).getResponseMoveCommentary());


		// iterate through possible moves and show corresponding result to user
		boolean moveRecognized = false;
		for (LessonItem.MentorPosition.PossibleMove possibleMove : possibleMoves) {
			if (possibleMove.getMove().equals(lastUserMove)) {

				if (possibleMove.getMoveType().equals(LessonItem.MOVE_DEFAULT)) {
					controlsLessonsView.showCorrect();
				} else if (possibleMove.getMoveType().equals(LessonItem.MOVE_ALTERNATE)) {
					showToast("Alternate correct move!");
					controlsLessonsView.showCorrect();
				} else if (possibleMove.getMoveType().equals(LessonItem.MOVE_WRONG)) {
					controlsLessonsView.showWrong();
				}
				descriptionTxt.setText(possibleMove.getMoveCommentary());

				moveRecognized = true;
			}
		}

		if (!moveRecognized) {
			descriptionTxt.setText(positionToLearn.getStandardWrongMoveCommentary());
			controlsLessonsView.showWrong();
		}
	}

	@Override
	public void restart() {
		adjustBoardForGame();
		controlsLessonsView.showAfterRetry();
	}

	@Override
	public void showHint() {
		if (usedHints < YourMoveDrawable.MAX_HINTS) {
			String hint = positionsToLearn.get(currentLearningPosition).getAdvice1();
			if (usedHints == 1) {
				hint = positionsToLearn.get(currentLearningPosition).getAdvice2();
			} else if (usedHints == 2) {
				hint = positionsToLearn.get(currentLearningPosition).getAdvice3();
			}
			String hintNumberStr = getString(R.string.hint_arg, ++usedHints);

			CharSequence hintChars = BOLD_DIVIDER + hintNumberStr + BOLD_DIVIDER + StaticData.SYMBOL_SPACE + hint;
			hintChars = AppUtils.setSpanBetweenTokens(hintChars, BOLD_DIVIDER, boldSpan);

			hintDivider.setVisibility(View.VISIBLE);
			hintTxt.setVisibility(View.VISIBLE);
			hintTxt.setText(hintChars);

			if (slidingDrawer.isOpened()) {
				slidingDrawer.animateClose();
			}
		}
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_SKIP_LESSON) {
			newGame();
			showToast("skip");

		} else if (code == ID_SHOW_ANSWER) {

			showToast("answer");
		} else if (code == ID_KEY_SQUARES) {
			showToast("key squares");

		} else if (code == ID_CORRECT_SQUARE) {
			showToast("correct square");

		} else if (code == ID_KEY_PIECES) {
			showToast("key pieces");

		} else if (code == ID_CORRECT_PIECE) {
			showToast("correct piece");

		} else if (code == ID_ANALYSIS_BOARD) {
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
			positionsToLearn = returnedObj.getData().getPositions();
			totalLearningPositionsCnt = positionsToLearn.size();
			adjustBoardForGame();

			need2update = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);

			showToast("Internal Error occurred");
			getActivityFace().showPreviousFragment();
		}
	}

	private void adjustBoardForGame() {
		ChessBoardLessons.resetInstance();
		LessonsBoardFace boardFace = getBoardFace();
		boardView.setGameUiFace(this);

		LessonItem.MentorPosition positionToSolve = positionsToLearn.get(currentLearningPosition);

		possibleMoves = positionToSolve.getLessonMoves();

		boardFace.setupBoard(positionToSolve.getFen());

		invalidateGameScreen();
		controlsLessonsView.enableGameControls(true);

		lessonTitleTxt.setText(lessonItem.getName());
		commentTxt.setText(lessonItem.getGoalCommentary());
		descriptionTxt.setText(Html.fromHtml(lessonItem.getAbout()));
	}

	private void widgetsInit(View view) {
		controlsLessonsView = (ControlsLessonsView) view.findViewById(R.id.controlsLessonsView);

		boardView = (ChessBoardLessonsView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(controlsLessonsView);

		controlsLessonsView.setBoardViewFace(boardView);

		setBoardView(boardView);

		ChessBoardLessons.resetInstance();
		boardView.setGameUiFace(this);
		controlsLessonsView.enableGameControls(false);


		{ // SlidingDrawer
			slidingDrawer = (MultiDirectionSlidingDrawer) view.findViewById(R.id.slidingDrawer);

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO adjust properly for tablets later
					int statusBarHeight = getStatusBarHeight();
					int width = getResources().getDisplayMetrics().widthPixels;
					int height = getResources().getDisplayMetrics().heightPixels;
					int actionBarHeight = getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height);
					int controlsViewHeight = getResources().getDimensionPixelSize(R.dimen.game_controls_button_height);
					int handleHeight = getResources().getDimensionPixelSize(R.dimen.drawer_handler_height);
					int additionalOffset = (int) (1 * getResources().getDisplayMetrics().density);
					int topBoardOffset = height - width - actionBarHeight - controlsViewHeight - handleHeight
							- statusBarHeight - additionalOffset;
					slidingDrawer.setTopOffset(topBoardOffset);

				}
			}, DRAWER_UPDATE_DELAY);
			slidingDrawer.setOnDrawerOpenListener(this);
			slidingDrawer.setOnDrawerCloseListener(this);
		}

		lessonTitleTxt = (TextView) view.findViewById(R.id.lessonTitleTxt);
		commentTxt = (TextView) view.findViewById(R.id.commentTxt);
		descriptionTxt = (TextView) view.findViewById(R.id.descriptionTxt);
		hintDivider = view.findViewById(R.id.hintDivider);
		hintTxt = (TextView) view.findViewById(R.id.hintTxt);

		{// options list setup
			optionsArray = new SparseArray<String>();
			optionsArray.put(ID_KEY_SQUARES, getString(R.string.key_squares));
			optionsArray.put(ID_CORRECT_SQUARE, getString(R.string.correct_square));
			optionsArray.put(ID_KEY_PIECES, getString(R.string.key_pieces));
			optionsArray.put(ID_CORRECT_PIECE, getString(R.string.correct_piece));

			optionsArray.put(ID_ANALYSIS_BOARD, getString(R.string.analysis_board));

			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
			optionsArray.put(ID_VS_COMPUTER, getString(R.string.vs_computer));
			optionsArray.put(ID_SKIP_LESSON, getString(R.string.skip_lesson));

			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

}
