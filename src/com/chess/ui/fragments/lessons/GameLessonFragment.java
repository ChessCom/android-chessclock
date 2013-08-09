package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.chess.FontsHelper;
import com.chess.MultiDirectionSlidingDrawer;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.LessonItem;
import com.chess.backend.entity.new_api.LessonListItem;
import com.chess.backend.entity.new_api.SuccessItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadLessonItemTask;
import com.chess.db.tasks.SaveLessonsLessonTask;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardLessons;
import com.chess.ui.engine.Move;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 20:06
 */
public class GameLessonFragment extends GameBaseFragment implements GameLessonFace, PopupListSelectionFace, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener, MultiDirectionSlidingDrawer.OnDrawerScrollListener {

	private static final String COURSE_ID = "course_id";
	private static final String LESSON_ID = "lesson_id";
	public static final String BOLD_DIVIDER = "##";

	private static final long DRAWER_UPDATE_DELAY = 100;
	private static final long TACTIC_ANSWER_DELAY = 1500;

	// Options ids
//	private static final int ID_KEY_SQUARES = 0;
//	private static final int ID_CORRECT_SQUARE = 1;
//	private static final int ID_KEY_PIECES = 2;
//	private static final int ID_CORRECT_PIECE = 3;
	private static final int ID_ANALYSIS_BOARD = 0;
//	private static final int ID_VS_COMPUTER = 1;
	private static final int ID_SKIP_LESSON = 1;
	private static final int ID_SHOW_ANSWER = 2;
	private static final int ID_SETTINGS = 3;
	/* When user use hints he decrease total points by values below. Values are given in percents*/
	private static final float HINT_1_COST = 2f;
	private static final float HINT_2_COST = 6f;
	private static final float HINT_3_COST = 10f;
	private static final float WRONG_MOVE_COST = 40f;
	private static final float ANALYSIS_COST = 4f;
	private static final float ANSWER_COST = 100f;
	public static final String FLOAT_FORMAT = "%.1f";

	private LessonUpdateListener lessonUpdateListener;
	private LessonDataUpdateListener saveLessonUpdateListener;
	private LessonDataUpdateListener lessonLoadListener;
	private SubmitLessonListener submitLessonListener;

	private int lessonId;
	private long courseId;
	private boolean isAnalysis;
	private LabelsConfig labelsConfig;

	private ControlsLessonsView controlsLessonsView;
	private ChessBoardLessonsView boardView;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private SparseArray<String> optionsArray;
	private LessonItem.Data lessonItem;
	private LessonItem.MentorLesson mentorLesson;
	private List<LessonItem.MentorPosition> positionsToLearn;

	private TextView lessonTitleTxt;
	private TextView commentTxt;
	private TextView descriptionTxt;
	private TextView positionDescriptionTxt;
	private TextView hintTxt;
	private View scoreLabel;
	private View ratingLabel;
	private TextView lessonPercentTxt;
	private TextView lessonsRatingTxt;
	private TextView lessonsRatingChangeTxt;

	private MultiDirectionSlidingDrawer slidingDrawer;
	private List<LessonItem.MentorPosition.PossibleMove> possibleMoves;
	private int startLearningPosition;
	private int currentLearningPosition;
	private int totalLearningPositionsCnt;
	private boolean need2update = true;
	private int usedHints;
	private int hintToShow;
	private View hintDivider;
	private CustomTypefaceSpan boldSpan;
	private ScrollView descriptionView;
	private int topBoardOffset;
	private int defaultDescriptionPadding;
	private int openDescriptionPadding;

	private SparseArray<Float> hintsCostMap;
	private LessonItem.UserLesson userLesson;
	private List<Integer> solvedPositionsList;
	private SparseArray<MoveCompleteItem> movesCompleteMap;
	private int scorePercent;
	private float pointsForLesson;
	private String moveToShow;

	public GameLessonFragment() { }

	public static GameLessonFragment createInstance(int lessonId, long courseId) {
		GameLessonFragment fragment = new GameLessonFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(LESSON_ID, lessonId);
		bundle.putLong(COURSE_ID, courseId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		boldSpan = new CustomTypefaceSpan("san-serif", FontsHelper.getInstance().getTypeFace(getActivity(), FontsHelper.BOLD_FONT));

		if (getArguments() != null) {
			lessonId = getArguments().getInt(LESSON_ID);
			courseId = getArguments().getLong(COURSE_ID);
		} else {
			lessonId = savedInstanceState.getInt(LESSON_ID);
			courseId = savedInstanceState.getLong(COURSE_ID);
		}

		labelsConfig = new LabelsConfig();

		saveLessonUpdateListener = new LessonDataUpdateListener(LessonDataUpdateListener.SAVE);
		lessonUpdateListener = new LessonUpdateListener();
		lessonLoadListener = new LessonDataUpdateListener(LessonDataUpdateListener.LOAD);
		solvedPositionsList = new ArrayList<Integer>();
		movesCompleteMap = new SparseArray<MoveCompleteItem>();
		submitLessonListener = new SubmitLessonListener();
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
			// check if we have that lesson in DB
			Cursor cursor = DbDataManager.executeQuery(getContentResolver(), DbHelper.getMentorLessonById(lessonId));
			if (cursor != null && cursor.moveToFirst()) { // we have saved lesson data
				new LoadLessonItemTask(lessonLoadListener, getContentResolver(), getUsername()).executeTask((long) lessonId);
			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_LESSON_BY_ID(lessonId));
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

				// TODO user restart parameter http GET http://api.c.com/v1/lessons/lessons/1 loginToken==4a3183b2355b85983d81a810c0191a27 restart==true
				new RequestJsonTask<LessonItem>(lessonUpdateListener).executeTask(loadItem);
			}
		} else {
			startLesson();
			adjustBoardForGame();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(LESSON_ID, lessonId);
		outState.putLong(COURSE_ID, courseId);
	}

	@Override
	public Boolean isUserColorWhite() {
		return labelsConfig.userSide == ChessBoard.WHITE_SIDE;
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
	public void nextPosition() {
		if (++currentLearningPosition < totalLearningPositionsCnt) {

			controlsLessonsView.showDefault();
			controlsLessonsView.dropUsedHints();
			usedHints = 0;
			showHintViews(false);

			// TODO add animation for next move in lesson

			adjustBoardForGame();
		}
	}

	@Override
	public void newGame() {
		getActivityFace().showPreviousFragment();
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
		return mentorLesson != null;
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


	/**
	 * moves calculation:
	 * <p/>
	 * - invalid move:     (40%) <p/>
	 * - wrong move:       (40%) <p/>
	 * - alternative move: (0%)  <p/>
	 * - default move:     the right move - no subtraction  <p/>
	 * - hint 1:  (2%)        <p/>
	 * - hint 2:  (6%)        <p/>
	 * - hint 3:  (10%)       <p/>
	 * - analysis board	(4%)  <p/>
	 * For details see http://www.chess.com/chessmentor/help
	 * <p/>
	 * For example, if you have a lesson that has two moves in it, the first having a move
	 * importance of 5, and the second with a move importance of 10, you have 15 total possible points.
	 * Let's say that on move 1 you use a STRONG HINT (-10%) and a SHOW KEY PIECES (-10%).
	 * Then we get (5 -10% -10%) = 4. Then on the second move we take 2 extra minutes of time (-5% each)
	 * and the analysis board (-15%). We then get (10 -5% -5% -15%) = 7.5. Add them together and we get 11.5
	 * out of 15 points, or 77% out of 100% on the lesson. That is how the score is calculated.
	 * The rating is calculated by taking the rating of the lesson and then plugging in your score
	 * (in this case 77%) to the Glicko formula to get your new rating. In chess a win is usually worth 1 point,
	 * a draw .5 points, and a loss 0 points. But with Chess Mentor your score can be anywhere from 0 to 1, and
	 * your rating is adjusted accordingly. You then receive a new rating, as does the lesson!
	 */
	@Override
	public void verifyMove() {
		LessonsBoardFace boardFace = getBoardFace();
		String lastUserMove = boardFace.getLastMoveStr();

		MoveCompleteItem moveCompleteItem = getCurrentCompleteItem();

		// iterate through possible moves and perform deduction
		boolean moveRecognized = false;
		boolean correctMove = false;
		for (LessonItem.MentorPosition.PossibleMove possibleMove : possibleMoves) {
			if (possibleMove.getMove().toLowerCase().contains(lastUserMove)) {

				if (possibleMove.getMoveType().equals(LessonItem.MOVE_DEFAULT)) { // Correct move
					controlsLessonsView.showCorrect();
					solvedPositionsList.add(currentLearningPosition);
					if (!TextUtils.isEmpty(possibleMove.getShortResponseMove())) {
						final Move move = boardFace.convertMoveCoordinate(possibleMove.getShortResponseMove());
						boardView.setMoveAnimator(move, true);
						boardView.resetValidMoves();
						boardFace.makeMove(move, true);
					}
					correctMove = true;
				} else if (possibleMove.getMoveType().equals(LessonItem.MOVE_ALTERNATE)) { // Alternate Correct Move
					// Correct move, try again!
					showToast(R.string.alternate_correct_move_ex);
					controlsLessonsView.showCorrect();
					solvedPositionsList.add(currentLearningPosition);

					correctMove = true;
				} else if (possibleMove.getMoveType().equals(LessonItem.MOVE_WRONG)) {
					controlsLessonsView.showWrong();
					moveCompleteItem.wrongMovesCnt++;
				}
				descriptionTxt.setText(Html.fromHtml(possibleMove.getMoveCommentary()));
				descriptionView.post(scrollDescriptionUp);

				moveRecognized = true;
				break;
			}
		}

		if (!moveRecognized) {
			Spanned wrongMoveComment = Html.fromHtml(getMentorPosition().getStandardWrongMoveCommentary());
			descriptionTxt.setText(wrongMoveComment);
			descriptionView.post(scrollDescriptionUp);
			controlsLessonsView.showWrong();
			moveCompleteItem.wrongMovesCnt++;
		}

		if (currentLearningPosition == totalLearningPositionsCnt - 1 && correctMove) { // calculate all progress for this lesson
			showCorrectMoveViews(true);

			// collect info about all moves for that lesson
			pointsForLesson = 0;
			int totalPointsForLesson = 0;
			for (int t = startLearningPosition; t < movesCompleteMap.size(); t++) {
				MoveCompleteItem item = movesCompleteMap.get(t);
				float pointsForMove = item.moveDifficulty;
				totalPointsForLesson += item.moveDifficulty;
				{ // subtract points for used hints
					float hintsSubtraction = 0;
					for (int z = 1; z <= item.usedHints; z++) {
						Float hintPercentCost = hintsCostMap.get(z);
						hintsSubtraction += item.moveDifficulty * hintPercentCost / 100;
					}

					pointsForMove -= hintsSubtraction;
					pointsForMove = pointsForMove < 0 ? 0 : pointsForMove;
				}

				{ // subtract points for wrong moves
					float subtraction = 0;
					for (int z = 0; z < item.wrongMovesCnt; z++) {
						subtraction += item.moveDifficulty * WRONG_MOVE_COST / 100;
					}
					pointsForMove -= subtraction;
					pointsForMove = pointsForMove < 0 ? 0 : pointsForMove;
				}

				{ // subtract points for analysis
					if (item.analysisUsed) {
						float subtraction = item.moveDifficulty * ANALYSIS_COST / 100;
						pointsForMove -= subtraction;
						pointsForMove = pointsForMove < 0 ? 0 : pointsForMove;
					}
				}

				{ // subtract points for shown answer
					if (item.answerWasShown) {
						float subtraction = item.moveDifficulty * ANSWER_COST / 100;
						pointsForMove -= subtraction;
						pointsForMove = pointsForMove < 0 ? 0 : pointsForMove;
					}
				}

				pointsForLesson += pointsForMove;
			}
			// Add them together and we get 11.5 out of 15 points, or 77% out of 100% on the lesson.
			scorePercent = (int) (pointsForLesson * 100 / totalPointsForLesson);

			lessonPercentTxt.setText(getString(R.string.percents, scorePercent) + StaticData.SYMBOL_PERCENT);
			float currentUserRating = userLesson.getCurrentPoints() + pointsForLesson;
			lessonsRatingTxt.setText(String.format(FLOAT_FORMAT, currentUserRating));
			String symbol = pointsForLesson > 0 ? StaticData.SYMBOL_PLUS : StaticData.SYMBOL_EMPTY;
			lessonsRatingChangeTxt.setText(StaticData.SYMBOL_LEFT_PAR + symbol + String.format(FLOAT_FORMAT, pointsForLesson)
					+ StaticData.SYMBOL_RIGHT_PAR);

			submitCorrectSolution();
			// show next lesson button
			controlsLessonsView.showNewGame();

		}
	}

	private void submitCorrectSolution() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_LESSON_BY_ID(lessonId));
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_CURRENT_POINTS, String.format("%.2f", pointsForLesson));  // you can pass float, 2 decimals please
		loadItem.addRequestParams(RestHelper.P_CURRENT_PERCENT, scorePercent);
		loadItem.addRequestParams(RestHelper.P_LAST_POS_NUMBER, currentLearningPosition);

		new RequestJsonTask<SuccessItem>(submitLessonListener).executeTask(loadItem);
	}

	private MoveCompleteItem getCurrentCompleteItem() {
		return movesCompleteMap.get(currentLearningPosition);
	}

	private LessonItem.MentorPosition getMentorPosition() {
		return positionsToLearn.get(currentLearningPosition);
	}

	private void showCorrectMoveViews(boolean show) {
		ratingLabel.setVisibility(show ? View.VISIBLE : View.GONE);
		scoreLabel.setVisibility(show ? View.VISIBLE : View.GONE);
		lessonPercentTxt.setVisibility(show ? View.VISIBLE : View.GONE);
		lessonsRatingTxt.setVisibility(show ? View.VISIBLE : View.GONE);
		lessonsRatingChangeTxt.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public void restart() {
		adjustBoardForGame();
		controlsLessonsView.showDefault();
	}

	@Override
	public void showHint() {
		if (usedHints < YourMoveDrawable.MAX_HINTS) {
			hintToShow = ++usedHints;
			getCurrentCompleteItem().usedHints = usedHints;
		} else {
			hintToShow = hintToShow > 2 ? 1 : ++hintToShow;
		}

		String hint = StaticData.SYMBOL_EMPTY;
		if (hintToShow == 1) {
			hint = getMentorPosition().getAdvice1();
		} else if (hintToShow == 2) {
			hint = getMentorPosition().getAdvice2();
		} else if (hintToShow == 3) {
			hint = getMentorPosition().getAdvice3();
		}

		String hintNumberStr = getString(R.string.hint_arg, hintToShow);
		CharSequence hintChars = BOLD_DIVIDER + hintNumberStr + BOLD_DIVIDER + StaticData.SYMBOL_SPACE + hint;
		hintChars = AppUtils.setSpanBetweenTokens(hintChars, BOLD_DIVIDER, boldSpan);

		showHintViews(true);
		hintTxt.setText(hintChars);

		descriptionView.postDelayed(scrollDescriptionDown, 100);
	}

	private void showHintViews(boolean show) {
		hintDivider.setVisibility(show ? View.VISIBLE : View.GONE);
		hintTxt.setVisibility(show ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_SKIP_LESSON) {
			newGame();
		} else if (code == ID_SHOW_ANSWER) {

			LessonItem.MentorPosition mentorPosition = getMentorPosition();
			LessonItem.MentorPosition.PossibleMove correctMove = mentorPosition.getCorrectMove();

			moveToShow = correctMove.getMove();

			ChessBoardLessons.resetInstance();
			boardView.setGameFace(this);

			getBoardFace().setupBoard(getMentorPosition().getFen());

			handler.postDelayed(showTacticMoveTask, TACTIC_ANSWER_DELAY);
			getCurrentCompleteItem().answerWasShown = true;

//		} else if (code == ID_KEY_SQUARES) {
//			showToast("key squares");

//		} else if (code == ID_CORRECT_SQUARE) {
//			showToast("correct square");

//		} else if (code == ID_KEY_PIECES) {
//			showToast("key pieces");

//		} else if (code == ID_CORRECT_PIECE) {
//			showToast("correct piece");

//		} else if (code == ID_VS_COMPUTER) {
//			getActivityFace().openFragment(GameCompFragment.createInstance()); // TODO pass FEN here
		} else if (code == ID_ANALYSIS_BOARD) {
			getCurrentCompleteItem().analysisUsed = true;
			switch2Analysis();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsBoardFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	private Runnable showTacticMoveTask = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);

			LessonsBoardFace boardFace = getBoardFace();

			final Move move = boardFace.convertMoveAlgebraic(moveToShow);
			boardView.setMoveAnimator(move, true);
			boardView.resetValidMoves();
			boardFace.makeMove(move, true);
			invalidateGameScreen();
		}
	};

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	@Override
	public void onDrawerOpened() {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				lessonTitleTxt.setVisibility(View.GONE);
				commentTxt.setVisibility(View.GONE);
			}
		}, 25);


		if (!solvedPositionsList.contains(currentLearningPosition)) {
			controlsLessonsView.showDefault();
		}

		descriptionView.setPadding(0, 0, 0, openDescriptionPadding);
		descriptionView.postDelayed(scrollDescriptionDown, 50);
	}

	@Override
	public void onDrawerClosed() {
		lessonTitleTxt.setVisibility(View.VISIBLE);
		commentTxt.setVisibility(View.VISIBLE);

		descriptionView.setPadding(0, 0, 0, defaultDescriptionPadding);
		descriptionView.post(scrollDescriptionDown);
	}

	@Override
	public void onScrollStarted() {
		descriptionView.setPadding(0, 0, 0, defaultDescriptionPadding);
	}

	@Override
	public void onScrollEnded() {

	}

	private Runnable scrollDescriptionDown = new Runnable() {
		@Override
		public void run() {
			descriptionView.fullScroll(View.FOCUS_DOWN);
		}
	};

	private Runnable scrollDescriptionUp = new Runnable() {
		@Override
		public void run() {
			descriptionView.fullScroll(View.FOCUS_UP);
		}
	};

	private class LessonUpdateListener extends ChessLoadUpdateListener<LessonItem> {

		private LessonUpdateListener() {
			super(LessonItem.class);
		}

		@Override
		public void updateData(LessonItem returnedObj) {
			super.updateData(returnedObj);

			lessonItem = returnedObj.getData();
			fillLessonData();

			new SaveLessonsLessonTask(saveLessonUpdateListener, lessonItem, getContentResolver(),
					getUsername()).executeTask();
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

		LessonItem.MentorPosition positionToSolve = getMentorPosition();
		if (getCurrentCompleteItem() == null) {
			MoveCompleteItem moveCompleteItem = new MoveCompleteItem();
			moveCompleteItem.moveDifficulty = positionToSolve.getMoveDifficulty();
			movesCompleteMap.put(currentLearningPosition, moveCompleteItem);
		}

		possibleMoves = positionToSolve.getPossibleMoves();

		boardFace.setupBoard(positionToSolve.getFen());
		boardView.resetValidMoves();

		labelsConfig.userSide = boardFace.isReside() ? ChessBoard.BLACK_SIDE : ChessBoard.WHITE_SIDE;

		invalidateGameScreen();
		controlsLessonsView.enableGameControls(true);

		lessonTitleTxt.setText(mentorLesson.getName());
		commentTxt.setText(mentorLesson.getGoalCommentary());
		descriptionTxt.setText(Html.fromHtml(mentorLesson.getAbout()));
		positionDescriptionTxt.setText(Html.fromHtml(positionToSolve.getAbout()));
		descriptionView.post(scrollDescriptionDown);

		showCorrectMoveViews(false);
	}

	private class LessonDataUpdateListener extends ChessLoadUpdateListener<LessonItem.Data> {

		static final int SAVE = 0;
		static final int LOAD = 1;

		private int listenerCode;

		private LessonDataUpdateListener(int listenerCode) {
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(LessonItem.Data returnedObj) {
			super.updateData(returnedObj);

			if (listenerCode == LOAD) {
				lessonItem = returnedObj;
				fillLessonData();
			}
			adjustBoardForGame();

			need2update = false;
		}
	}

	private void fillLessonData() {
		lessonItem.setId(lessonId);
		mentorLesson = lessonItem.getLesson();
		positionsToLearn = lessonItem.getPositions();

		userLesson = lessonItem.getUserLesson();
		userLesson.setLegalMoveCheck(lessonItem.getLegalMoveCheck());
		userLesson.setLegalPositionCheck(lessonItem.getLegalPositionCheck());
		userLesson.setLessonCompleted(lessonItem.isLessonCompleted());

		totalLearningPositionsCnt = positionsToLearn.size();

		startLearningPosition = userLesson.getCurrentPosition();
		currentLearningPosition = userLesson.getCurrentPosition();
		if (totalLearningPositionsCnt == currentLearningPosition) {
			currentLearningPosition--;
		}
//		currentPoints = userLesson.getCurrentPoints();  // TODO check if needed
	}

	private class SubmitLessonListener extends ChessLoadUpdateListener<SuccessItem> {

		private SubmitLessonListener() {
			super(SuccessItem.class);
		}

		@Override
		public void updateData(SuccessItem returnedObj) {
			if(returnedObj.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {

				LessonListItem lessonListItem = new LessonListItem();
				lessonListItem.setUser(getUsername());
				lessonListItem.setCourseId(courseId);
				lessonListItem.setId(lessonId);
				lessonListItem.setName(lessonItem.getLesson().getName());
				lessonListItem.setCompleted(true);

				DbDataManager.saveLessonListItemToDb(getContentResolver(), lessonListItem);
			} else {
				showToast(R.string.error);
			}
		}
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
//					int additionalOffset = (int) (1 * getResources().getDisplayMetrics().density);
					topBoardOffset = height - width - actionBarHeight - controlsViewHeight - handleHeight
							- statusBarHeight /*- additionalOffset*/;
					slidingDrawer.setTopOffset(topBoardOffset);

					openDescriptionPadding = width + handleHeight;
				}
			}, DRAWER_UPDATE_DELAY);

			slidingDrawer.setOnDrawerOpenListener(this);
			slidingDrawer.setOnDrawerCloseListener(this);
			slidingDrawer.setOnDrawerScrollListener(this);
		}

		defaultDescriptionPadding = (int) (20 * getResources().getDisplayMetrics().density);
		descriptionView = (ScrollView) view.findViewById(R.id.descriptionView);
		lessonTitleTxt = (TextView) view.findViewById(R.id.lessonTitleTxt);
		commentTxt = (TextView) view.findViewById(R.id.commentTxt);
		descriptionTxt = (TextView) view.findViewById(R.id.descriptionTxt);
		positionDescriptionTxt = (TextView) view.findViewById(R.id.positionDescriptionTxt);
		hintDivider = view.findViewById(R.id.hintDivider);
		hintTxt = (TextView) view.findViewById(R.id.hintTxt);

		{// lesson rating changes
			scoreLabel = view.findViewById(R.id.scoreLabel);
			ratingLabel = view.findViewById(R.id.ratingLabel);
			lessonPercentTxt = (TextView) view.findViewById(R.id.lessonPercentTxt);
			lessonsRatingTxt = (TextView) view.findViewById(R.id.lessonsRatingTxt);
			lessonsRatingChangeTxt = (TextView) view.findViewById(R.id.lessonsRatingChangeTxt);

			showCorrectMoveViews(false);
		}

		hintsCostMap = new SparseArray<Float>();
		hintsCostMap.put(1, HINT_1_COST);
		hintsCostMap.put(2, HINT_2_COST);
		hintsCostMap.put(3, HINT_3_COST);

		{// options list setup
			optionsArray = new SparseArray<String>();
//			optionsArray.put(ID_KEY_SQUARES, getString(R.string.key_squares));
//			optionsArray.put(ID_CORRECT_SQUARE, getString(R.string.correct_square));
//			optionsArray.put(ID_KEY_PIECES, getString(R.string.key_pieces));
//			optionsArray.put(ID_CORRECT_PIECE, getString(R.string.correct_piece));

			optionsArray.put(ID_ANALYSIS_BOARD, getString(R.string.analysis_board));

			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
//			optionsArray.put(ID_VS_COMPUTER, getString(R.string.vs_computer));
			optionsArray.put(ID_SKIP_LESSON, getString(R.string.skip_lesson));

			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}

	private class MoveCompleteItem {
		int usedHints;
		int wrongMovesCnt;
		int moveDifficulty;
		boolean analysisUsed;
		boolean answerWasShown;
	}

}
