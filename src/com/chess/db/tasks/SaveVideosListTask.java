package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.VideoItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;

public class SaveVideosListTask extends AbstractUpdateTask<VideoItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveVideosListTask(TaskUpdateInterface<VideoItem.Data> taskFace, List<VideoItem.Data> currentItems,
							  ContentResolver resolver) {
		super(taskFace, new ArrayList<VideoItem.Data>());
		this.itemList.addAll(currentItems);

		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		synchronized (itemList) {
			for (VideoItem.Data currentItem : itemList) {
				DbDataManager.updateVideoItem(contentResolver, currentItem);
			}
		}
		result = StaticData.RESULT_OK;

		return result;
	}

}
