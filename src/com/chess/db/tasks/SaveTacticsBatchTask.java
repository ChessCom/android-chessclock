package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.TacticItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;


public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticItem.Data, Long> {

	private final String username;
	private ContentResolver contentResolver;
	private final List<TacticItem.Data> tacticsBatch;

	public SaveTacticsBatchTask(TaskUpdateInterface<TacticItem.Data> taskFace, List<TacticItem.Data> tacticsBatch,
								ContentResolver resolver) {
		super(taskFace);
		this.tacticsBatch = new ArrayList<TacticItem.Data>();
		this.tacticsBatch.addAll(tacticsBatch);
		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		username = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (TacticItem.Data tacticItem : tacticsBatch) {
			DbDataManager.saveTacticBatchItemToDb(contentResolver, tacticItem, username);
		}

		return StaticData.RESULT_OK;
	}


}
