package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.new_api.LessonItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.07.13
 * Time: 15:42
 */
public class SaveLessonsLessonTask extends AbstractUpdateTask<LessonItem.Data, Long> {

	private final long lessonId;
	private ContentResolver contentResolver;
	protected static String[] sArguments1 = new String[1];
	protected static String[] sArguments2 = new String[2];
	protected static String[] sArguments3 = new String[3];
	private String username;

	public SaveLessonsLessonTask(TaskUpdateInterface<LessonItem.Data> taskFace, LessonItem.Data currentItem,
								  ContentResolver resolver, String username) {
		super(taskFace);
		this.username = username;
		this.item = currentItem;
		lessonId = item.getId();
		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {

		saveMentorLesson(item.getLesson());
		saveLessonPositions(item.getPositions());
		saveUserLesson(item.getUserLesson());

		result = StaticData.RESULT_OK;

		return result;
	}

	private void saveMentorLesson(LessonItem.MentorLesson mentorLesson) {
		mentorLesson.setLessonId(lessonId);
		final String[] arguments1 = sArguments1;
		arguments1[0] = String.valueOf(mentorLesson.getLessonId());


		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_MENTOR_LESSONS.ordinal()];
		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
				DbDataManager.SELECTION_ITEM_ID, arguments1, null);

		ContentValues values = DbDataManager.putLessonsMentorLessonToValues(mentorLesson);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

	private void saveLessonPositions(List<LessonItem.MentorPosition> positions) {
		for (LessonItem.MentorPosition position : positions) {
			position.setLessonId(lessonId);

			final String[] arguments = sArguments2;
			arguments[0] = String.valueOf(position.getLessonId());
			arguments[1] = String.valueOf(position.getPositionNumber());


			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_POSITIONS.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_NUMBER,
					DbDataManager.SELECTION_ITEM_ID_AND_NUMBER, arguments, null);

			ContentValues values = DbDataManager.putLessonsPositionToValues(position);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();

			saveLessonPositionsMoves(position.getPossibleMoves(), position.getPositionNumber());
		}
	}

	private void saveLessonPositionsMoves(List<LessonItem.MentorPosition.PossibleMove> moves, int positionNumber) {
		for (LessonItem.MentorPosition.PossibleMove possibleMove : moves) {
			possibleMove.setLessonId(lessonId);
			possibleMove.setPositionNumber(positionNumber);

			final String[] arguments = sArguments3;
			arguments[0] = String.valueOf(possibleMove.getLessonId());
			arguments[1] = String.valueOf(possibleMove.getPositionNumber());
			arguments[2] = String.valueOf(possibleMove.getMoveNumber());


			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_POSITION_MOVES.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_POSITION_NUMBER,
					DbDataManager.SELECTION_ITEM_ID_POSITION_NUMBER, arguments, null);

			ContentValues values = DbDataManager.putLessonsPositionMoveToValues(possibleMove);

			if (cursor.moveToFirst()) {
				contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
			} else {
				contentResolver.insert(uri, values);
			}

			cursor.close();
		}
	}

	private void saveUserLesson(LessonItem.UserLesson userLesson) {
		userLesson.setLessonId(lessonId);
		userLesson.setUsername(username);

		final String[] arguments1 = sArguments2;
		arguments1[0] = String.valueOf(userLesson.getLessonId());
		arguments1[1] = username;


		// TODO implement beginTransaction logic for performance increase
		Uri uri = DbConstants.uriArray[DbConstants.Tables.LESSONS_USER_LESSONS.ordinal()];
		Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID_AND_USER,
				DbDataManager.SELECTION_ITEM_ID_AND_USER, arguments1, null);

		ContentValues values = DbDataManager.putLessonsUserLessonToValues(userLesson);

		if (cursor.moveToFirst()) {
			contentResolver.update(ContentUris.withAppendedId(uri, DbDataManager.getId(cursor)), values, null, null);
		} else {
			contentResolver.insert(uri, values);
		}

		cursor.close();
	}

}

