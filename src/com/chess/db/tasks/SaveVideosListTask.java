package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.VideoSingleItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;
import com.chess.statics.StaticData;

import java.util.ArrayList;
import java.util.List;

public class SaveVideosListTask extends AbstractUpdateTask<VideoSingleItem.Data, Long> {

	private ContentResolver contentResolver;
	protected static String[] arguments = new String[1];

	public SaveVideosListTask(TaskUpdateInterface<VideoSingleItem.Data> taskFace, List<VideoSingleItem.Data> currentItems,
							  ContentResolver resolver) {
		super(taskFace, new ArrayList<VideoSingleItem.Data>());
		this.itemList.addAll(currentItems);
		this.contentResolver = resolver;
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (VideoSingleItem.Data currentItem : itemList) {
			DbDataManager.saveVideoItem(contentResolver, currentItem);
		}

		return StaticData.RESULT_OK;
	}

}
