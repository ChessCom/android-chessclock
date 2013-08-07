package com.chess.db.tasks;

import android.content.ContentResolver;
import android.database.Cursor;
import com.chess.backend.entity.new_api.LessonItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.QueryParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.07.13
 * Time: 17:09
 */
public class LoadLessonItemTask extends AbstractUpdateTask<LessonItem.Data, Long> {

	private ContentResolver contentResolver;
	private String username;

	public LoadLessonItemTask(TaskUpdateInterface<LessonItem.Data> taskFace, ContentResolver resolver, String username) {
		super(taskFace);
		this.username = username;
		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		int result = StaticData.EMPTY_DATA;

		Long lessonId = ids[0];
		item = new LessonItem.Data();

		{ // Load Mentor Lesson
			QueryParams params = DbHelper.getMentorLessonById(lessonId);
			Cursor cursor = contentResolver.query(params.getUri(), params.getProjection(), params.getSelection(),
					params.getArguments(), params.getOrder());

			if (cursor.moveToFirst()) {
				item.setLesson(DBDataManager.getLessonsMentorLessonFromCursor(cursor));
			}
		}

		{ // Load User Lesson
			QueryParams params = DbHelper.getUserLessonById(lessonId, username);
			Cursor cursor = contentResolver.query(params.getUri(), params.getProjection(), params.getSelection(),
					params.getArguments(), params.getOrder());

			if (cursor.moveToFirst()) {
				item.setUserLesson(DBDataManager.getLessonsUserLessonFromCursor(cursor));
			}
		}

		{ // Load Positions
			QueryParams params = DbHelper.getLessonPositionsById(lessonId);
			Cursor cursor = contentResolver.query(params.getUri(), params.getProjection(), params.getSelection(),
					params.getArguments(), params.getOrder());

			if (cursor.moveToFirst()) {
				List<LessonItem.MentorPosition> positions = new ArrayList<LessonItem.MentorPosition>();
				do {
					positions.add(DBDataManager.getLessonsPositionFromCursor(cursor));

				} while (cursor.moveToNext());

				item.setPositions(positions);
			}
		}

		{ // Load Position Moves
			for (LessonItem.MentorPosition position : item.getPositions()) {

				QueryParams params = DbHelper.getLessonPositionMovesById(lessonId, position.getPositionNumber());
				Cursor cursor = contentResolver.query(params.getUri(), params.getProjection(), params.getSelection(),
						params.getArguments(), params.getOrder());

				if (cursor.moveToFirst()) {
					List<LessonItem.MentorPosition.PossibleMove> possibleMoves = new ArrayList<LessonItem.MentorPosition.PossibleMove>();
					do {
						possibleMoves.add(DBDataManager.getLessonsPositionMoveFromCursor(cursor));
					} while(cursor.moveToNext());
					position.setPossibleMoves(possibleMoves);
				}
			}
		}
		result = StaticData.RESULT_OK;
		return result;
	}

}