package com.chess.ui.fragments.lessons;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.LessonProblemItem;
import com.chess.backend.entity.api.LessonRatingChangeItem;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadLessonItemTask;
import com.chess.db.tasks.SaveLessonsLessonTask;
import com.chess.model.GameAnalysisItem;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardLessons;
import com.chess.ui.engine.FenHelper;
import com.chess.ui.engine.Move;
import com.chess.ui.fragments.game.GameAnalyzeFragment;
import com.chess.ui.fragments.game.GameBaseFragment;
import com.chess.ui.fragments.popup_fragments.PopupCustomViewFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.fragments.settings.SettingsGeneralFragment;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.interfaces.boards.LessonsBoardFace;
import com.chess.ui.interfaces.game_ui.GameLessonFace;
import com.chess.ui.views.chess_boards.ChessBoardLessonsView;
import com.chess.ui.views.drawables.YourMoveDrawable;
import com.chess.ui.views.game_controls.ControlsLessonsView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.CustomTypefaceSpan;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.MultiDirectionSlidingDrawer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 20:06
 */
public class GameLessonFragment extends GameBaseFragment implements GameLessonFace, PopupListSelectionFace,
		MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener,
		MultiDirectionSlidingDrawer.OnDrawerScrollListener {

	protected static final String COURSE_ID = "course_id";
	protected static final String LESSON_ID = "lesson_id";
	public static final String BOLD_DIVIDER = "##";

	private static final long DRAWER_UPDATE_DELAY = 100;
	private static final long SHOW_FIRST_MOVE_DELAY = 700;
	private static final long SHOW_ANSWER_DELAY = 1700;


	// Options ids
//	private static final int ID_KEY_SQUARES = 0;
//	private static final int ID_CORRECT_SQUARE = 1;
//	private static final int ID_KEY_PIECES = 2;
//	private static final int ID_CORRECT_PIECE = 3;
	protected static final int ID_ANALYSIS_BOARD = 0;
	//	private static final int ID_VS_COMPUTER = 1;
	protected static final int ID_SKIP_LESSON = 1;
	protected static final int ID_SHOW_ANSWER = 2;
	protected static final int ID_REVIEW_LESSON = 3;
	protected static final int ID_SETTINGS = 4;

	/* When user use hints he decrease total points by values below. Values are given in percents*/
	protected static final float HINT_1_COST = 2f;
	protected static final float HINT_2_COST = 6f;
	protected static final float HINT_3_COST = 10f;
	private static final float WRONG_MOVE_COST = 40f;
	private static final float ANALYSIS_COST = 4f;
	private static final float ANSWER_COST = 100f;

	public static final String FLOAT_FORMAT = "%.1f";
	public static final String SUBMIT_FLOAT_FORMAT = "%.2f";

	private LessonUpdateListener lessonUpdateListener;
	private LessonDataUpdateListener saveLessonUpdateListener;
	private LessonDataUpdateListener lessonLoadListener;
	private SubmitLessonListener submitLessonListener;

	protected int lessonId;
	protected long courseId;

	private ControlsLessonsView controlsView;
	protected ChessBoardLessonsView boardView;
	private PopupOptionsMenuFragment optionsSelectFragment;
	protected SparseArray<String> optionsArray;
	private LessonProblemItem.Data lessonItem;
	private LessonProblemItem.MentorLesson mentorLesson;
	private List<LessonProblemItem.MentorPosition> positionsToLearn;

	protected TextView lessonTitleTxt;
	protected TextView goalCommentTxt;
	protected TextView descriptionTxt;
	protected TextView positionDescriptionTxt;
	protected View lessonDescriptionDivider;
	protected TextView hintTxt;
//	private View scoreLabel;
//	private View ratingLabel;
//	private TextView lessonPercentTxt;
//	private TextView lessonsRatingTxt;
//	private TextView lessonsRatingChangeTxt;

	private MultiDirectionSlidingDrawer slidingDrawer;
	private List<LessonProblemItem.MentorPosition.PossibleMove> possibleMoves;
	private int startLearningPosition;
	private int currentLearningPosition;
	private int totalLearningPositionsCnt;
	private int usedHints;
	private int hintToShow;
	protected View hintDivider;
	private CustomTypefaceSpan boldSpan;
	protected ScrollView descriptionView;
	private int topBoardOffset;
	protected int defaultDescriptionPadding;
	private int openDescriptionPadding;

	protected SparseArray<Float> hintsCostMap;
	private LessonProblemItem.UserLesson userLesson;
	private List<Integer> solvedPositionsList;
	private SparseArray<MoveCompleteItem> movesCompleteMap;
	private int scorePercent;
	private float pointsForLesson;
	private String moveToShow;
	private PopupCustomViewFragment completedPopupFragment;
	private int updatedUserRating;
	private boolean wrongState;
	protected View lessonCompleteView;
	protected TextView lessonPercentTxt;
	protected TextView yourRatingTxt;
	protected TextView lessonRatingTxt;
	protected TextView lessonRatingChangeTxt;
	protected boolean showLessonsResult;
	private String initialLessonTextStr;

	public GameLessonFragment() {
	}

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
			need2update = true; // we were killed, need to reload lesson data
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.game_lessons_frame, container, false);
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
	public void onResume() {
		super.onResume();

		if (need2update) {
			updateUiData();
		} else {
			startLesson();
			adjustBoardForGame();
			adjustTextForLessonStartView();
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (userLesson != null) { // we might leave this screen w/o loading data(no network case)
			DbDataManager.saveUserLessonToDb(getContentResolver(), userLesson, lessonId, getUsername());
		}
	}

	protected void updateUiData() {
		// check if we have that lesson in DB
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getMentorLessonById(lessonId));
		if (cursor != null && cursor.moveToFirst()) { // we have saved lesson data
			new LoadLessonItemTask(lessonLoadListener, getContentResolver(), getUsername()).executeTask((long) lessonId);
		} else {
			// drop flag here for lessons limit reached
			getAppData().setLessonLimitWasReached(false);

			getControlsView().enableGameControls(false);

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSON_BY_ID(lessonId));
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

			new RequestJsonTask<LessonProblemItem>(lessonUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(LESSON_ID, lessonId);
		outState.putLong(COURSE_ID, courseId);
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
	public void showOptions() {
		if (optionsSelectFragment != null) {
			return;
		}

		if (userLesson.isLessonCompleted()) {
			optionsArray.put(ID_REVIEW_LESSON, getString(R.string.review_lesson));
		} else {
			optionsArray.remove(ID_REVIEW_LESSON);
		}

		optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsArray);
		optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
	}

	@Override
	public void nextPosition() {
		if (currentLearningPosition < totalLearningPositionsCnt) {
			if (getMentorPosition().isFreeMove() && !getCurrentCompleteItem().answerWasShown) {
				showAnswer();
				return;
			}
		}

		loadNextPosition();
	}

	private void loadNextPosition() {
		if (currentLearningPosition <= totalLearningPositionsCnt) {
			currentLearningPosition++;
			userLesson.setCurrentPosition(currentLearningPosition);

			showDefaultState();
			adjustBoardForGame();
		}
	}

	@Override
	public void newGame() {
		if (showLessonsResult) {
			lessonCompleteView.setVisibility(View.VISIBLE);

			descriptionView.postDelayed(scrollDescriptionDown, 50);
			if (inPortrait()) {
				slidingDrawer.animateClose();
			}
			showLessonsResult = false;
			return;
		} else {
			lessonCompleteView.setVisibility(View.GONE);
		}

		Cursor courseCursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonCourseById((int) courseId));

		if (courseCursor != null && courseCursor.moveToFirst()) {  // if we have saved course
			Cursor lessonsListCursor = DbDataManager.query(getContentResolver(),
					DbHelper.getLessonsListByCourseId((int) courseId, getUsername()));
			if (lessonsListCursor.moveToFirst()) { // if we have saved lessons
				List<LessonSingleItem> lessons = new ArrayList<LessonSingleItem>();
				do {
					lessons.add(DbDataManager.getLessonsListItemFromCursor(lessonsListCursor));
				} while (lessonsListCursor.moveToNext());
				lessonsListCursor.close();

				int lessonsInCourse = lessons.size();
				boolean nextLessonFound = false;
				for (int i = 0; i < lessonsInCourse; i++) {
					LessonSingleItem lesson = lessons.get(i);
					if (lesson.getId() == lessonId && (i + 1 < lessonsInCourse)) { // get next lesson
						LessonSingleItem nextLesson = lessons.get(i + 1);
						lessonId = nextLesson.getId();
						nextLessonFound = true;
						break;
					}
				}

				if (nextLessonFound) {
					if (inPortrait()) {
						slidingDrawer.animateClose();
					}
					showDefaultState();
					updateUiData();
					getControlsView().showStart();
					return;
				}
			}
		}

		getActivityFace().showPreviousFragment();
	}

	@Override
	public void switch2Analysis() {
		GameAnalysisItem analysisItem = new GameAnalysisItem();
		analysisItem.setGameType(RestHelper.V_GAME_CHESS);
		analysisItem.setFen(getBoardFace().generateFullFen());
		analysisItem.setMovesList(getBoardFace().getMoveListSAN());
		analysisItem.copyLabelConfig(labelsConfig);

		getActivityFace().openFragment(GameAnalyzeFragment.createInstance(analysisItem));
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
		getControlsView().showDefault();
		if (inPortrait() && !slidingDrawer.isOpened()) {
			slidingDrawer.animateOpen();
		}
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
		final LessonsBoardFace boardFace = getBoardFace();

		// iterate through possible moves and perform deduction
		boolean moveRecognized = false;
		boolean correctMove = false;
		for (LessonProblemItem.MentorPosition.PossibleMove possibleMove : possibleMoves) {
			if (boardFace.isLastLessonMoveIsCorrect(possibleMove.getMove())) {

				if (possibleMove.getMoveType().equals(LessonProblemItem.MOVE_DEFAULT)) { // Correct move
					showCorrectState();
					correctMove = true;

					// Set move comment
					setGoalCommentText(possibleMove.getMoveCommentary());

					if (!TextUtils.isEmpty(possibleMove.getShortResponseMove())) { // if computer need to make move
						final Move move = boardFace.convertMoveCoordinate(possibleMove.getShortResponseMove());
						// play move animation
						boardView.setMoveAnimator(move, true);
						boardView.resetValidMoves();
						// make actual move
						boardFace.makeMove(move, true);
						invalidateGameScreen();

						setDescriptionText(possibleMove.getResponseMoveCommentary());

						// get next position's about
						if (currentLearningPosition + 1 < positionsToLearn.size()) {
							String nextLessonAbout = positionsToLearn.get(currentLearningPosition + 1).getAbout();
							setPositionDescriptionText(nextLessonAbout);
						}
						descriptionTxt.post(scrollDescriptionUp);
					}
				} else if (possibleMove.getMoveType().equals(LessonProblemItem.MOVE_ALTERNATE)) { // Alternate Correct Move
					// Correct move, try again!
					setDescriptionText(possibleMove.getMoveCommentary());
					showCorrectState();
					correctMove = true;
				} else if (possibleMove.getMoveType().equals(LessonProblemItem.MOVE_WRONG)) {
					if (!TextUtils.isEmpty(possibleMove.getMoveCommentary())) {
						setDescriptionText(possibleMove.getMoveCommentary());
					} else {
						setDescriptionText(getMentorPosition().getStandardWrongMoveCommentary());
					}
					showWrongState();
				}

				moveRecognized = true;
				break;
			}
		}

		if (!moveRecognized) {
			setDescriptionText(getMentorPosition().getStandardWrongMoveCommentary());
			goalCommentTxt.setVisibility(View.GONE);

			scrollToCurrentText();
			showWrongState();
		}

		if (currentLearningPosition == totalLearningPositionsCnt - 1 && correctMove) { // calculate all progress for this lesson

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

			if (pointsForLesson == 0) {
				int totalDifficulty = 0;
				for (int t = startLearningPosition; t < movesCompleteMap.size(); t++) {
					MoveCompleteItem completeItem = movesCompleteMap.get(t);
					totalDifficulty += completeItem.moveDifficulty;
				}
				if (totalDifficulty == 0) { // we might gain 0 points for moves which are not Free
					scorePercent = 100; // else for free moves we set 100%
				} else {
					// Add them together and we get 11.5 out of 15 points, or 77% out of 100% on the lesson.
					scorePercent = (int) (pointsForLesson * 100 / totalPointsForLesson);
				}
			} else {
				// Add them together and we get 11.5 out of 15 points, or 77% out of 100% on the lesson.
				scorePercent = (int) (pointsForLesson * 100 / totalPointsForLesson);
			}

			updatedUserRating = getAppData().getUserLessonsRating();

			saveLessonInfo();

			// Update server with whole lesson scores
			if (isNetworkAvailable()) {
				submitCorrectSolution();
			}

			// show next lesson button
			getControlsView().showNewGame();
		} else if (correctMove) {
			loadNextPosition();
		}
	}

	protected void showDefaultState() {
		getControlsView().showDefault();
		getControlsView().dropUsedHints();
		solvedPositionsList.clear();
		usedHints = 0;
		showHintViews(false);
	}

	private void showCorrectState() {
		getControlsView().showCorrect();
		solvedPositionsList.add(currentLearningPosition);
		wrongState = false;
	}

	private void showWrongState() {
		positionDescriptionTxt.setText(Symbol.EMPTY);
		lessonDescriptionDivider.setVisibility(View.GONE);
		getControlsView().showWrong();
		getCurrentCompleteItem().wrongMovesCnt++;
		wrongState = true;
	}

	private void saveLessonInfo() {
		LessonSingleItem lessonSingleItem = new LessonSingleItem();
		lessonSingleItem.setUser(getUsername());
		lessonSingleItem.setCourseId(courseId);
		lessonSingleItem.setId(lessonId);
		lessonSingleItem.setName(lessonItem.getLesson().getName());
		lessonSingleItem.setCompleted(true);
		lessonSingleItem.setStarted(false);

		DbDataManager.saveLessonListItemToDb(getContentResolver(), lessonSingleItem);

		userLesson.setLessonCompleted(true);
		DbDataManager.saveUserLessonToDb(getContentResolver(), userLesson, lessonId, getUsername());
	}

	private void submitCorrectSolution() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSON_BY_ID(lessonId));
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_CURRENT_POINTS, String.format(SUBMIT_FLOAT_FORMAT, pointsForLesson));  // you can pass float, 2 decimals please
		loadItem.addRequestParams(RestHelper.P_CURRENT_PERCENT, scorePercent);
		loadItem.addRequestParams(RestHelper.P_LAST_POS_NUMBER, totalLearningPositionsCnt);

		new RequestJsonTask<LessonRatingChangeItem>(submitLessonListener).executeTask(loadItem);
	}

	private MoveCompleteItem getCurrentCompleteItem() {
		return movesCompleteMap.get(currentLearningPosition);
	}

	private LessonProblemItem.MentorPosition getMentorPosition() {
		// TODO investigate real problem here, probably bcz of nextPosition for free move do incorrect counts
		if (currentLearningPosition >= positionsToLearn.size()) {
			currentLearningPosition = positionsToLearn.size() - 1;
		}

		return positionsToLearn.get(currentLearningPosition);
	}

	@Override
	public void restart() {
		adjustBoardForGame();
		if (getMentorPosition().isFreeMove()) {
			getControlsView().showCorrect();
		} else {
			getControlsView().showDefault();
		}

		if (currentLearningPosition > 1) { // get previous move correct comment
			LessonProblemItem.MentorPosition mentorPosition = positionsToLearn.get(currentLearningPosition - 1);
			LessonProblemItem.MentorPosition.PossibleMove correctMove = mentorPosition.getCorrectMove();
			if (correctMove != null) {
				if (!TextUtils.isEmpty(correctMove.getMoveCommentary())) {
					setDescriptionText(correctMove.getMoveCommentary());
				} else {
					setDescriptionText(correctMove.getResponseMoveCommentary());
				}
			}
		} else {
			setDescriptionText(initialLessonTextStr);
			scrollToCurrentText();
		}

		/*
 		case "thinking":
            if ($this->intPositionNumber > 0) {
                $this->intPositionNumber--;
                $objMove = CmMove::GetCorrectMoveByPositionId(
                $this->objPositionList[$this->intPositionNumber]->PositionId);

                $this->lblLessonText->Text = $objMove->MoveCommentary;
            }
        case "just_moved":
            if ($this->intPositionNumber == 0) {
                $this->lblLessonText->Text = $this->strInitialLessonText;
            } else {
                $objMove = CmMove::GetCorrectMoveByPositionId($this->objPositionList[$this->intPositionNumber - 1]->PositionId);
                $this->lblLessonText->Text = $objMove->ResponseMoveCommentary;

                if ($this->objPositionList[$this->intPositionNumber]->About) {
                    $this->lblLessonText->Text
                    .= '<br />---------------------------------------------------------<br />' .
                     $this->objPositionList[$this->intPositionNumber]->About;
                }
            }
		*/
	}

	@Override
	public void showHint() {
		String hint = Symbol.EMPTY;
		if (hintToShow == 0) {
			hint = getMentorPosition().getAdvice1();
		} else if (hintToShow == 1) {
			hint = getMentorPosition().getAdvice2();
		} else if (hintToShow == 2) {
			hint = getMentorPosition().getAdvice3();
		}

		if (usedHints < YourMoveDrawable.MAX_HINTS && !TextUtils.isEmpty(hint)) {
			hintToShow = ++usedHints;
			getCurrentCompleteItem().usedHints = usedHints;
		} else {
			hintToShow = hintToShow > 1 ? 0 : ++hintToShow;
		}

		if (TextUtils.isEmpty(hint)) {
			showToast(R.string.no_hint);
			return;
		}

		String hintNumberStr = getString(R.string.hint_arg, hintToShow);
		CharSequence hintChars = BOLD_DIVIDER + hintNumberStr + BOLD_DIVIDER + Symbol.SPACE + hint;
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_share:
				if (lessonItem == null) {
					showToast(R.string.nothing_to_share);
					return true;
				}
				submitShareIntent(getString(R.string.lesson_share_message,
						lessonItem.getLesson().getName()));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.shareBtn) {

			submitShareIntent(getString(R.string.lesson_completed_message,
					lessonItem.getLesson().getName(), scorePercent));

			completedPopupFragment.dismiss();
			completedPopupFragment = null;
		}
	}

	private void submitShareIntent(String message) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_completed_lesson_title));
		shareIntent.putExtra(Intent.EXTRA_TEXT, message);
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_SKIP_LESSON) {
			newGame();
		} else if (code == ID_SHOW_ANSWER) {

			showAnswer();
		} else if (code == ID_REVIEW_LESSON) {
			// drop position
			currentLearningPosition = 0;
			userLesson.setCurrentPosition(0);

			// hide scores
			lessonCompleteView.setVisibility(View.GONE);

			// request restart lesson from server
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSON_BY_ID(lessonId));
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_RESTART, RestHelper.V_TRUE);

			new RequestJsonTask<LessonProblemItem>(lessonUpdateListener).executeTask(loadItem);
//		} else if (code == ID_KEY_SQUARES) {  // TBD
//			showToast("key squares");

//		} else if (code == ID_CORRECT_SQUARE) { // TBD
//			showToast("correct square");

//		} else if (code == ID_KEY_PIECES) { // TBD
//			showToast("key pieces");

//		} else if (code == ID_CORRECT_PIECE) { // TBD
//			showToast("correct piece");

//		} else if (code == ID_VS_COMPUTER) {
//			getActivityFace().openFragment(GameCompFragment.createInstance(getBoardFace().generateFullFen()));
		} else if (code == ID_ANALYSIS_BOARD) {
			getCurrentCompleteItem().analysisUsed = true;
			switch2Analysis();
		} else if (code == ID_SETTINGS) {
			getActivityFace().openFragment(new SettingsGeneralFragment());
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	private void showAnswer() {
		// avoid changing currentLearningPosition
		getControlsView().enableGameControls(false);

		LessonProblemItem.MentorPosition mentorPosition = getMentorPosition();
		LessonProblemItem.MentorPosition.PossibleMove correctMove = mentorPosition.getCorrectMove();

		moveToShow = correctMove.getMove();

		ChessBoardLessons.resetInstance();
		boardView.setGameFace(this);
		if (inPortrait()) {
			slidingDrawer.open();
		}

		getBoardFace().setupBoard(getMentorPosition().getFen());

		handler.postDelayed(showNextMoveTask, SHOW_FIRST_MOVE_DELAY);
		getCurrentCompleteItem().answerWasShown = true;
	}

	private Runnable showNextMoveTask = new Runnable() {
		@Override
		public void run() {
			handler.removeCallbacks(this);
			if (getActivity() == null) {
				return;
			}

			LessonsBoardFace boardFace = getBoardFace();

			// get next valid move
			final Move move = boardFace.convertMoveAlgebraic(moveToShow);
			if (move == null) {
				return;
			}

			// play move animation
			boardView.setMoveAnimator(move, true);
			boardView.resetValidMoves();
			// make actual move
			boardFace.makeMove(move, true);
			invalidateGameScreen();

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					handler.removeCallbacks(this);

					if (getActivity() == null) {
						return;
					}
					getControlsView().enableGameControls(true);

					verifyMove();
				}
			}, SHOW_ANSWER_DELAY);
		}
	};

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	@Override
	public void onDrawerOpened() {
		if (getActivity() == null || lessonItem == null) {
			return;
		}

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				lessonTitleTxt.setVisibility(View.GONE);
				if (!TextUtils.isEmpty(goalCommentTxt.getText())) {
					goalCommentTxt.setVisibility(View.VISIBLE);
				} else {
					goalCommentTxt.setVisibility(View.GONE);
				}
			}
		}, 25);

		if (!solvedPositionsList.contains(currentLearningPosition) && !wrongState) {
			getControlsView().showDefault();
		}

		if (getMentorPosition().isFreeMove()) {
			controlsView.showCorrect();
		}

		descriptionView.setPadding(0, 0, 0, openDescriptionPadding);
		scrollToCurrentText();

		// mark this lesson as incomplete. There can be few incomplete lessons
		LessonSingleItem lessonSingleItem = new LessonSingleItem();
		lessonSingleItem.setUser(getUsername());
		lessonSingleItem.setCourseId(courseId);
		lessonSingleItem.setId(lessonId);
		lessonSingleItem.setName(lessonItem.getLesson().getName());
		lessonSingleItem.setCompleted(userLesson.isLessonCompleted());
		lessonSingleItem.setStarted(true);

		DbDataManager.saveLessonListItemToDb(getContentResolver(), lessonSingleItem);
	}

	@Override
	public void onDrawerClosed() {
		lessonTitleTxt.setVisibility(View.VISIBLE);
		if (!TextUtils.isEmpty(goalCommentTxt.getText())) {
			goalCommentTxt.setVisibility(View.VISIBLE);
		} else {
			goalCommentTxt.setVisibility(View.GONE);
		}

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

	private void scrollToCurrentText() {
		if (!TextUtils.isEmpty(positionDescriptionTxt.getText())) {
			descriptionTxt.post(scrollDescriptionDown);
		} else {
			descriptionTxt.post(scrollDescriptionUp);
		}
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

	private class LessonUpdateListener extends ChessLoadUpdateListener<LessonProblemItem> {

		private LessonUpdateListener() {
			super(LessonProblemItem.class);
		}

		@Override
		public void updateData(LessonProblemItem returnedObj) {
			super.updateData(returnedObj);

			lessonItem = returnedObj.getData();
			fillLessonData();

			new SaveLessonsLessonTask(saveLessonUpdateListener, lessonItem, getContentResolver(),
					getUsername()).executeTask();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.USER_HAS_REACHED_THE_DAILY_LIMIT_OF_LESSONS) {
					getAppData().setLessonLimitWasReached(true);
					return;
				}
			}
			super.errorHandle(resultCode);
			// in case of any error leave this fragment, as data will be unreliable
			getActivityFace().showPreviousFragment();
		}
	}

	private void adjustBoardForGame() {
		wrongState = false;

		ChessBoardLessons.resetInstance();
		LessonsBoardFace boardFace = getBoardFace();
		boardView.setGameUiFace(this);

		LessonProblemItem.MentorPosition positionToSolve = getMentorPosition();
		if (getCurrentCompleteItem() == null) {
			MoveCompleteItem moveCompleteItem = new MoveCompleteItem();
			moveCompleteItem.moveDifficulty = positionToSolve.getMoveDifficulty();
			movesCompleteMap.put(currentLearningPosition, moveCompleteItem);
		}

		possibleMoves = positionToSolve.getPossibleMoves();

		boardFace.setupBoard(positionToSolve.getFen());
		boardView.resetValidMoves();

		// based on FEN we detect which player is next to move
		boolean whiteToMove = positionToSolve.getFen().contains(FenHelper.WHITE_TO_MOVE);
		// fill labelsConfig for analysis mode
		labelsConfig.userSide = whiteToMove ? ChessBoard.WHITE_SIDE : ChessBoard.BLACK_SIDE;
		labelsConfig.topPlayerName = getString(R.string.comp);
		labelsConfig.topPlayerRating = Symbol.EMPTY;
		labelsConfig.bottomPlayerName = getUsername();
		labelsConfig.bottomPlayerRating = String.valueOf(getAppData().getUserLessonsRating());

		invalidateGameScreen();
		getControlsView().enableGameControls(true);

		lessonTitleTxt.setText(mentorLesson.getName());

		// add currentLearningPosition in case we load from DB
		solvedPositionsList.add(currentLearningPosition);

		if (inPortrait()) {
			if (slidingDrawer.isOpened() && getMentorPosition().isFreeMove()) {
				controlsView.showCorrect();
			} else if (slidingDrawer.isOpened()) {
				controlsView.showDefault();
			} else {
				controlsView.showStart();
			}
		} else {
			if (getMentorPosition().isFreeMove()) {
				controlsView.showCorrect();
			} else {
				controlsView.showDefault();
			}
		}
	}

	private void setDescriptionText(String descriptionStr) {
		Spanned description = Html.fromHtml(descriptionStr);
		if (!TextUtils.isEmpty(description)) {
			descriptionTxt.setText(description);
			descriptionTxt.setVisibility(View.VISIBLE);
			lessonDescriptionDivider.setVisibility(View.VISIBLE);
		} else {
			descriptionTxt.setVisibility(View.GONE);
			lessonDescriptionDivider.setVisibility(View.GONE);
		}
	}

	private void setPositionDescriptionText(String descriptionStr) {
		Spanned description = Html.fromHtml(descriptionStr);
		if (!TextUtils.isEmpty(description)) {
			positionDescriptionTxt.setText(description);
			positionDescriptionTxt.setVisibility(View.VISIBLE);
			lessonDescriptionDivider.setVisibility(View.VISIBLE);
		} else {
			positionDescriptionTxt.setVisibility(View.GONE);
			lessonDescriptionDivider.setVisibility(View.GONE);
		}
	}

	private void setGoalCommentText(String descriptionStr) {
		Spanned description = Html.fromHtml(descriptionStr);
		if (!TextUtils.isEmpty(description)) {
			goalCommentTxt.setText(description);
			goalCommentTxt.setVisibility(View.VISIBLE);
		} else {
			goalCommentTxt.setVisibility(View.GONE);
		}
	}

	private class LessonDataUpdateListener extends ChessLoadUpdateListener<LessonProblemItem.Data> {

		static final int SAVE = 0;
		static final int LOAD = 1;

		private int listenerCode;

		private LessonDataUpdateListener(int listenerCode) {
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(LessonProblemItem.Data returnedObj) {
			super.updateData(returnedObj);

			if (listenerCode == LOAD) {
				lessonItem = returnedObj;
				fillLessonData();
			}
			adjustBoardForGame();

			adjustTextForLessonStartView();
			need2update = false;
		}
	}

	private void adjustTextForLessonStartView() {
		if (currentLearningPosition == 0) {
			setGoalCommentText(mentorLesson.getGoalCommentary());

			String aboutLessonStr = mentorLesson.getAbout();
			String aboutPositionStr = getMentorPosition().getAbout();
			if (!TextUtils.isEmpty(aboutLessonStr)) {
				initialLessonTextStr = aboutLessonStr;
				setDescriptionText(initialLessonTextStr);

				if (!TextUtils.isEmpty(aboutPositionStr)) {
					setPositionDescriptionText(aboutPositionStr);
					scrollToCurrentText();
				}
			} else {
				initialLessonTextStr = aboutPositionStr;
				setDescriptionText(initialLessonTextStr);
			}
		} else {
			// hide comment
			goalCommentTxt.setVisibility(View.GONE);

			// set last move response comment
			LessonProblemItem.MentorPosition previousLearnPosition = positionsToLearn.get(currentLearningPosition - 1);
			LessonProblemItem.MentorPosition.PossibleMove correctMove = previousLearnPosition.getCorrectMove();

			initialLessonTextStr = correctMove.getResponseMoveCommentary(); // used for restart case if not on first position
			setDescriptionText(correctMove.getResponseMoveCommentary());

			// set next position's about
			String positionAboutStr = positionsToLearn.get(currentLearningPosition).getAbout();
			if (!TextUtils.isEmpty(positionAboutStr)) {
				setPositionDescriptionText(positionAboutStr);
			}
			scrollToCurrentText();
		}
	}

	private void fillLessonData() {
		lessonItem.setId(lessonId);
		mentorLesson = lessonItem.getLesson();
		positionsToLearn = lessonItem.getPositions();
		totalLearningPositionsCnt = positionsToLearn.size();

		userLesson = lessonItem.getUserLesson();
		userLesson.setLegalMoveCheck(lessonItem.getLegalMoveCheck());
		userLesson.setLegalPositionCheck(lessonItem.getLegalPositionCheck());
		userLesson.setLessonCompleted(lessonItem.isLessonCompleted());

		startLearningPosition = userLesson.getCurrentPosition();
		currentLearningPosition = userLesson.getCurrentPosition();

		if (totalLearningPositionsCnt == currentLearningPosition) {
			currentLearningPosition--;
		}

		movesCompleteMap.clear();
	}

	private class SubmitLessonListener extends ChessLoadUpdateListener<LessonRatingChangeItem> {

		private SubmitLessonListener() {
			super(LessonRatingChangeItem.class);
		}

		@Override
		public void updateData(LessonRatingChangeItem returnedObj) {
			LessonRatingChangeItem.Data ratingChange = returnedObj.getData();
			//	Move comment --> [next] --> Lesson result --> [next] --> Start next lesson
			{ // show Lesson Complete! View

				int pointsForLesson = 0;
				if (!lessonItem.isLessonCompleted() && ratingChange != null) { // For completed lesson ratingChange is null
					pointsForLesson = ratingChange.getChange();
					if (updatedUserRating == 0) {
						updatedUserRating = ratingChange.getNewRating() - ratingChange.getChange();
					}
					updatedUserRating += pointsForLesson;
				}

				lessonPercentTxt.setText(String.valueOf(scorePercent) + Symbol.PERCENT);
				lessonRatingTxt.setText(String.valueOf(updatedUserRating));
				if (!lessonItem.isLessonCompleted()) {
					String symbol = pointsForLesson > 0 ? Symbol.PLUS : Symbol.EMPTY;
					lessonRatingChangeTxt.setText(Symbol.wrapInPars(symbol + pointsForLesson));
					// save updated user lessons rating
					getAppData().setUserLessonsRating(updatedUserRating);
					lessonItem.setLessonCompleted(true);
				}

				showLessonsResult = true;
			}
		}
	}

	protected ControlsLessonsView getControlsView() {
		return controlsView;
	}

	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsLessonsView) controlsView;
	}

	private void init() {
		labelsConfig = new LabelsConfig();

		saveLessonUpdateListener = new LessonDataUpdateListener(LessonDataUpdateListener.SAVE);
		lessonUpdateListener = new LessonUpdateListener();
		lessonLoadListener = new LessonDataUpdateListener(LessonDataUpdateListener.LOAD);
		solvedPositionsList = new ArrayList<Integer>();
		movesCompleteMap = new SparseArray<MoveCompleteItem>();
		submitLessonListener = new SubmitLessonListener();
	}

	protected void widgetsInit(View view) {
		setControlsView(view.findViewById(R.id.controlsView));

		boardView = (ChessBoardLessonsView) view.findViewById(R.id.boardview);
		boardView.setFocusable(true);
		boardView.setControlsView(getControlsView());

		getControlsView().setBoardViewFace(boardView);

		setBoardView(boardView);

		ChessBoardLessons.resetInstance();
		boardView.setGameUiFace(this);
		getControlsView().enableGameControls(false);


		{ // SlidingDrawer
			slidingDrawer = (MultiDirectionSlidingDrawer) view.findViewById(R.id.slidingDrawer);

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (getActivity() == null) {
						return;
					}

					int statusBarHeight = getStatusBarHeight();
					int width = getResources().getDisplayMetrics().widthPixels;
					int height = getResources().getDisplayMetrics().heightPixels;
					int actionBarHeight = getResources().getDimensionPixelSize(R.dimen.actionbar_compat_height);
					int controlsViewHeight = getResources().getDimensionPixelSize(R.dimen.game_controls_button_height);
					int handleHeight = getResources().getDimensionPixelSize(R.dimen.drawer_handler_height);

					topBoardOffset = height - width - actionBarHeight - controlsViewHeight - handleHeight - statusBarHeight;
					slidingDrawer.setTopOffset(topBoardOffset);

					openDescriptionPadding = width + handleHeight;
				}
			}, DRAWER_UPDATE_DELAY);

			slidingDrawer.setOnDrawerOpenListener(this);
			slidingDrawer.setOnDrawerCloseListener(this);
			slidingDrawer.setOnDrawerScrollListener(this);
		}

		defaultDescriptionPadding = (int) (20 * density);
		descriptionView = (ScrollView) view.findViewById(R.id.descriptionView);
		lessonTitleTxt = (TextView) view.findViewById(R.id.lessonTitleTxt);
		goalCommentTxt = (TextView) view.findViewById(R.id.goalCommentTxt);
		descriptionTxt = (TextView) view.findViewById(R.id.descriptionTxt);
		lessonDescriptionDivider = view.findViewById(R.id.lessonDescriptionDivider);
		positionDescriptionTxt = (TextView) view.findViewById(R.id.positionDescriptionTxt);
		hintDivider = view.findViewById(R.id.hintDivider);
		hintTxt = (TextView) view.findViewById(R.id.hintTxt);

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

			optionsArray.put(ID_ANALYSIS_BOARD, getString(R.string.analysis));

			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
			optionsArray.put(ID_REVIEW_LESSON, getString(R.string.review_lesson));
//			optionsArray.put(ID_VS_COMPUTER, getString(R.string.vs_computer));
			optionsArray.put(ID_SKIP_LESSON, getString(R.string.skip_lesson));

			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}

		// lesson complete widgets
		lessonCompleteView = view.findViewById(R.id.lessonCompleteView);
		lessonPercentTxt = (TextView) view.findViewById(R.id.lessonPercentTxt);
		yourRatingTxt = (TextView) view.findViewById(R.id.yourRatingTxt);
		lessonRatingTxt = (TextView) view.findViewById(R.id.lessonRatingTxt);
		lessonRatingChangeTxt = (TextView) view.findViewById(R.id.lessonRatingChangeTxt);
	}

	private class MoveCompleteItem {
		int usedHints;
		int wrongMovesCnt;
		int moveDifficulty;
		boolean analysisUsed;
		boolean answerWasShown;
	}


/*
<?php
//
2 states:
    - thinking      => player need to make move
    - just moved    => computer need to make move
----------------------
//

//Initial text setup:

	if ($this->objLesson->About) {
		$this->strInitialLessonText .= $this->objLesson->About;
	}

	if ($this->objLesson->About && $this->objPositionList[0]->About) {
		$this->strInitialLessonText .= '<br />---------------------------------------------------------<br />';
	}

	$this->strInitialLessonText .= $this->objPositionList[0]->About;

// Back text setup
	$this->strBackText = "<br /><br /><strong>" . p('Chessmentor View Lesson Controller: Try Again, button label', 'Click \'TRY AGAIN\' to go back and make a different move.')->x() . "</strong>";

// ==========================================================

// Create label for comments

	protected function lblLessonText_Create() {
		if ($this->blnLessonComplete) {
			$this->lblLessonText->Text = '';
			$this->lblLessonText->Text .= '
			' . p('Chessmentor View Lesson Controller: Initial Score')->x() . ' <strong>' . $this->objUserCmLesson->InitialScore . '%</strong><br />
					' . p('Chessmentor View Lesson Controller: Last Score')->x() . ' <strong>' . $this->objUserCmLesson->LastScore . '%</strong> on
			' . ($this->objUserCmLesson->LastCompleteDate ? I18n::Date($this->objUserCmLesson->LastCompleteDate, 'column') : p('Chessmentor View Lesson Controller: N/A', 'N/A')->x()) . ' <br />
					' . p('Chessmentor View Lesson Controller: Number Of Attempts')->x() . ' <strong>' . $this->objUserCmLesson->NumAttempts . '</strong><br /><br />

			<strong><a href="' . $strRetryLessonUrl . '">' . p('Chessmentor View Lesson Controller: Click To Review This Lesson')->x() . ' &raquo;</a></strong><br />';
			//' . p('Chessmentor View Lesson Controller: Feel Free To Explore This Lesson And All Its Hints As Your Rating Will Not Be Affected')->x();
		} elseif ($this->intPositionNumber == 0) {
			$this->lblLessonText->Text = $this->strInitialLessonText;
		} else {
			$objMove = CmMove::GetCorrectMoveByPositionId($this->objPositionList[$this->intPositionNumber - 1]->PositionId);
			$this->lblLessonText->Text = $objMove->ResponseMoveCommentary;

			if ($this->objPositionList[$this->intPositionNumber]->About) {
				$this->lblLessonText->Text .= '<br />---------------------------------------------------------<br />' . $this->objPositionList[$this->intPositionNumber]->About;
			}
		}
	}


// ===========================================================

// Actions

// 1. Submit move

	protected function btnSubmit_Click($strFormId, $strControlId, $strParameter) {
		$this->strBackText = "<br /><br /><strong>" . p('Chessmentor View Lesson Controller: Try Again, button label', 'Click \'TRY AGAIN\' to go back and make a different move.')->x() . "</strong>";

		$objMove = CmMove::LoadByPositionIdMove($this->objPositionList[$this->intPositionNumber]->PositionId, $strCmMove);

		if (!$objMove) {
			$this->lblLessonText->Text = $this->objPositionList[$this->intPositionNumber]->StandardWrongMoveCommentary;
			$this->lblLessonText->Text .= $this->strBackText;
		} else {
			switch ($objMove->MoveType) {
				case "wrong":
					if ($objMove->MoveCommentary) {
						$this->lblLessonText->Text = $objMove->MoveCommentary;
					} else {
						$this->lblLessonText->Text = $this->objPositionList[$this->intPositionNumber]->StandardWrongMoveCommentary;
					}

					$this->lblLessonText->Text .= $this->strBackText;
				case "alternate":
					$this->lblLessonText->Text = $objMove->MoveCommentary;
					$this->lblLessonText->Text .= $this->strBackText;
				case "default":
					$this->lblLessonText->Text = $objMove->MoveCommentary;

			}
		}
	}

// 2. Back btn click (also Try again click)

	protected function btnBack_Click($strFormId, $strControlId, $strParameter) {
		switch ($this->strUserLessonState) {
			case "thinking":
				if ($this->intPositionNumber > 0) {
					$this->intPositionNumber--;
					$objMove = CmMove::GetCorrectMoveByPositionId($this->objPositionList[$this->intPositionNumber]->PositionId);

					$this->lblLessonText->Text = $objMove->MoveCommentary;
				}
			case "just_moved":
				if ($this->intPositionNumber == 0) {
					$this->lblLessonText->Text = $this->strInitialLessonText;
				} else {
					$objMove = CmMove::GetCorrectMoveByPositionId($this->objPositionList[$this->intPositionNumber - 1]->PositionId);
					$this->lblLessonText->Text = $objMove->ResponseMoveCommentary;

					if ($this->objPositionList[$this->intPositionNumber]->About) {
						$this->lblLessonText->Text .= '<br />---------------------------------------------------------<br />' . $this->objPositionList[$this->intPositionNumber]->About;
					}
				}
		}
	}

// 3. Continue btn click

	protected function btnContinue_Click($strFormId, $strControlId, $strParameter) {
		switch ($this->strUserLessonState) {
			case "just_moved":
				$objMove = CmMove::GetCorrectMoveByPositionId($this->objPositionList[$this->intPositionNumber]->PositionId);

				if ($objMove->ShortResponseMove) {
					$this->lblLessonText->Text = $objMove->ResponseMoveCommentary;

					if ($this->objPositionList[$this->intPositionNumber + 1]->About) {
						$this->lblLessonText->Text .= '<br />---------------------------------------------------------<br />' . $this->objPositionList[$this->intPositionNumber + 1]->About;
					}
				}
			case "thinking":
				$objMove = CmMove::GetCorrectMoveByPositionId($this->objPositionList[$this->intPositionNumber]->PositionId);

				if ($objMove) {
					$this->lblLessonText->Text = $objMove->MoveCommentary;
				}
		}
	}

// 4. Show answer link

	protected function lnkShowAnswer_Click($strFormId, $strControlId, $strParameter) {
		if ($this->strUserLessonState != 'thinking') {
		} elseif ($this->intPositionNumber == count($this->objPositionList)) {
		} else {
			$objMove = CmMove::GetCorrectMoveByPositionId($this->objPositionList[$this->intPositionNumber]->PositionId);
			$this->lblLessonText->Text = $objMove->MoveCommentary;
		}
	}

// 5. lesson finished

	protected function UpdateDisplayLessonFinished() {
		$this->lblLessonText-> Text.= '<br /><br /><strong style="color: #4c7637;">Your result:</strong><br />';
		$this->lblLessonText->Text .= p('Chessmentor View Lesson Controller: Score')->x() . ' <strong>' . $this->objUserCmLesson->LastScore . '%</strong><br />';

		$strRatingChange = "";
		if ($this->objUserCmSettings && $this->objAuthUser) {
			if ($this->objNewRatingInfo != null) {
				$intRatingChange = $this->objNewRatingInfo['change'];
				$strRatingChange .= " (";

				if ($intRatingChange > 0) {
					$strRatingChange .= "+";
				}

				$strRatingChange .= $intRatingChange;
				$strRatingChange .= ")";
			}

			$strDisplayRating = $this->objUserCmSettings->DisplayRating();
		} else {
			$strDisplayRating = I18n::RatingPhrase(ChessRating::Unrated)->x();
		}

		$this->lblLessonText->Text .= I18n::RatingPhrase(ChessRating::Rating)->x() . ' <strong>' . $strDisplayRating . $strRatingChange . '</strong><br /><br />';

		$strRetryLessonUrl = '/chessmentor/view_lesson?id=' . $this->objLesson->LessonId . '&restart=1';

		if ($this->blnIsCustomTrainingSession) {
			$strRetryLessonUrl .= '&c=1';
		} elseif ($this->blnIsStudyingCourseSequentially) {
			$strRetryLessonUrl .= '&c_id=' . $this->objLesson->CourseId;
		}

		$this->lblLessonText->Text .= '<strong><a href="' . $strRetryLessonUrl . '">' . p('Chessmentor View Lesson Controller: Click To Review This Lesson')->x() . ' &raquo;</a></strong><br />';
	}
*/
}
