package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.new_api.ForumItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DBDataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.07.13
 * Time: 17:13
 */
public class SaveForumsListTask extends AbstractUpdateTask<ForumItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveForumsListTask(TaskUpdateInterface<ForumItem.Data> taskFace, List<ForumItem.Data> currentItems,
							  ContentResolver resolver) {
		super(taskFace, new ArrayList<ForumItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (ForumItem.Data currentItem : itemList) {
				DBDataManager.updateForumItem(contentResolver, currentItem);
			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}

