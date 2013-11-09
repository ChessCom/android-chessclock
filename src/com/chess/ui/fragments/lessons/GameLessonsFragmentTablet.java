package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.ui.engine.ChessBoardLessons;
import com.chess.ui.views.chess_boards.ChessBoardLessonsView;
import com.chess.ui.views.game_controls.ControlsLessonsViewTablet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.11.13
 * Time: 21:32
 */
public class GameLessonsFragmentTablet extends GameLessonFragment {

	private ControlsLessonsViewTablet controlsView;


	public GameLessonsFragmentTablet() {}

	public static GameLessonsFragmentTablet createInstance(int lessonId, long courseId) {
		GameLessonsFragmentTablet fragment = new GameLessonsFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putInt(LESSON_ID, lessonId);
		bundle.putLong(COURSE_ID, courseId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void newGame() {
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
					showDefaultControls();
					updateUiData();
					getControlsView().showStart();
				} else {
					getActivityFace().showPreviousFragment();
				}
			} else {
				getActivityFace().showPreviousFragment();
			}
		}
	}

	@Override
	public void startLesson() {
		getControlsView().showDefault();
	}

	@Override
	protected ControlsLessonsViewTablet getControlsView() {
		return controlsView;
	}

	@Override
	protected void setControlsView(View controlsView) {
		this.controlsView = (ControlsLessonsViewTablet) controlsView;
	}

	@Override
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


		defaultDescriptionPadding = (int) (20 * getResources().getDisplayMetrics().density);
		descriptionView = (ScrollView) view.findViewById(R.id.descriptionView);
		lessonTitleTxt = (TextView) view.findViewById(R.id.lessonTitleTxt);
		commentTxt = (TextView) view.findViewById(R.id.commentTxt);
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

			optionsArray.put(ID_ANALYSIS_BOARD, getString(R.string.analysis_board));

			optionsArray.put(ID_SHOW_ANSWER, getString(R.string.show_answer));
//			optionsArray.put(ID_VS_COMPUTER, getString(R.string.vs_computer));
			optionsArray.put(ID_SKIP_LESSON, getString(R.string.skip_lesson));

			optionsArray.put(ID_SETTINGS, getString(R.string.settings));
		}
	}
}
