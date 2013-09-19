package com.chess.db.tasks;

import android.content.ContentResolver;
import com.chess.backend.entity.api.TacticProblemItem;
import com.chess.backend.entity.api.TacticTrainerItem;
import com.chess.backend.interfaces.TaskUpdateInterface;
import com.chess.statics.AppData;
import com.chess.statics.StaticData;
import com.chess.backend.tasks.AbstractUpdateTask;
import com.chess.db.DbDataManager;

import java.util.ArrayList;
import java.util.List;


public class SaveTacticsBatchTask extends AbstractUpdateTask<TacticProblemItem.Data, Long> {

	private final String username;
	private ContentResolver contentResolver;
	private final List<TacticProblemItem.Data> tacticsBatch;

	public SaveTacticsBatchTask(TaskUpdateInterface<TacticProblemItem.Data> taskFace, List<TacticProblemItem.Data> tacticsBatch,
								ContentResolver resolver) {
		super(taskFace);
		this.tacticsBatch = new ArrayList<TacticProblemItem.Data>();
		this.tacticsBatch.addAll(tacticsBatch);
		this.contentResolver = resolver;
		AppData appData = new AppData(getTaskFace().getMeContext());
		username = appData.getUsername();
	}

	@Override
	protected Integer doTheTask(Long... ids) {
		for (TacticProblemItem.Data tacticsProblem : tacticsBatch) {
			TacticTrainerItem.Data trainerItem = new TacticTrainerItem.Data();
			trainerItem.setTacticsProblem(tacticsProblem);
			DbDataManager.saveTacticTrainerToDb(contentResolver, trainerItem, username);
		}

		return StaticData.RESULT_OK;
	}


}
