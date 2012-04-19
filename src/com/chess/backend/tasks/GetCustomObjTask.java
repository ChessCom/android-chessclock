package com.chess.backend.tasks;

import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.TaskUpdateInterface;

public class GetCustomObjTask<T> extends AbstractUpdateTask<T,LoadItem> {

	public GetCustomObjTask(TaskUpdateInterface<T,LoadItem> taskFace) {
		super(taskFace);
	}

	@Override
	protected Integer doTheTask(LoadItem... loadItems) {
		String url = RestHelper.formCustomRequest(loadItems[0]);

		return result;
	}



}
