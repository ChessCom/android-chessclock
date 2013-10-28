package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.statics.StaticData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.10.13
 * Time: 12:26
 */
public class SaveLessonsListTask extends AbstractUpdateTask<LessonSingleItem, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveLessonsListTask(TaskUpdateInterface<LessonSingleItem> taskFace, List<LessonSingleItem> currentItems,
							   ContentResolver resolver) {
		super(taskFace, new ArrayList<LessonSingleItem>());
		this.itemList.addAll(currentItems);
		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (LessonSingleItem currentItem : itemList) {
			DbDataManager.saveLessonListItemToDb(contentResolver, currentItem);
		}

		return StaticData.RESULT_OK;
	}
}