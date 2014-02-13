package com.chess.ui.fragments.lessons;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.engine.ChessBoardLessons;
import com.chess.ui.views.chess_boards.ChessBoardLessonsView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.11.13
 * Time: 21:32
 */
public class GameLessonsFragmentTablet extends GameLessonFragment {

	public GameLessonsFragmentTablet() {
	}

	public static GameLessonsFragmentTablet createInstance(int lessonId, long courseId) {
		GameLessonsFragmentTablet fragment = new GameLessonsFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putInt(LESSON_ID, lessonId);
		bundle.putLong(COURSE_ID, courseId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void widgetsInit(View view) {
		if (inPortrait()) {
			super.widgetsInit(view);
			return;
		}
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
}
