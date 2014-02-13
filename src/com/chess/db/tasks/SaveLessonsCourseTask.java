package com.chess.db.tasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.chess.backend.entity.api.LessonCourseItem;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.StaticData;

import static com.chess.db.DbScheme.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.07.13
 * Time: 19:23
 */
public class SaveLessonsCourseTask extends AbstractUpdateTask<LessonCourseItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments1 = new String[1];
	private String username;

	public SaveLessonsCourseTask(TaskUpdateInterface<LessonCourseItem.Data> taskFace, LessonCourseItem.Data currentItem,
								 ContentResolver resolver, String username) {
		super(taskFace);
		this.username = username;
		this.item = currentItem;

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {

		{ // save course id, name, description
			final String[] arguments = arguments1;
			arguments[0] = String.valueOf(item.getId());

			// TODO implement beginTransaction logic for performance increase
			Uri uri = DbScheme.uriArray[DbScheme.Tables.LESSONS_COURSES.ordinal()];
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_ITEM_ID,
					DbDataManager.SELECTION_ITEM_ID, arguments, null);

			ContentValues values = new ContentValues();

			values.put(V_ID, item.getId());
			values.put(V_DESCRIPTION, item.getDescription());
			values.put(V_NAME, item.getCourseName());

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);
		}

		for (LessonSingleItem lesson : item.getLessons()) {
			lesson.setCourseId(item.getId());
			lesson.setUser(username);
			DbDataManager.saveLessonListItemToDb(contentResolver, lesson);
		}

		result = StaticData.RESULT_OK;

		return result;
	}


}
